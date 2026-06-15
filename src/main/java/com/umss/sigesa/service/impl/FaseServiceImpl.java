package com.umss.sigesa.service.impl;

import com.umss.sigesa.domain.exception.CodigoDuplicadoException;
import com.umss.sigesa.domain.exception.FaseNotFoundException;
import com.umss.sigesa.domain.exception.JerarquiaInvalidaException;
import com.umss.sigesa.domain.model.Fase;
import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import com.umss.sigesa.repository.FaseRepository;
import com.umss.sigesa.repository.FaseSpecifications;
import com.umss.sigesa.service.FaseService;
import com.umss.sigesa.web.dto.request.FaseCreateRequest;
import com.umss.sigesa.web.dto.request.FaseUpdateRequest;
import com.umss.sigesa.web.dto.request.SubfaseCreateRequest;
import com.umss.sigesa.web.dto.request.SubfaseSyncRequest;
import com.umss.sigesa.web.dto.request.SubfaseUpdateRequest;
import com.umss.sigesa.web.dto.response.FaseResponse;
import com.umss.sigesa.web.dto.response.FaseSummaryResponse;
import com.umss.sigesa.web.dto.response.PageResponse;
import com.umss.sigesa.web.dto.response.SubfaseResponse;
import com.umss.sigesa.web.mapper.FaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaseServiceImpl implements FaseService {

    private final FaseRepository faseRepository;
    private final FaseMapper faseMapper;

    @Override
    @Transactional
    public FaseResponse crear(FaseCreateRequest request) {
        validarCodigoUnico(request.codigo(), null);

        Fase fase = faseMapper.toEntity(request);

        if (request.subfases() != null) {
            validarCodigosSubfasesUnicos(request.subfases(), Set.of(request.codigo()));
            for (SubfaseCreateRequest subfaseRequest : request.subfases()) {
                validarCodigoUnico(subfaseRequest.codigo(), null);
                fase.getSubfases().add(faseMapper.toEntity(subfaseRequest, fase));
            }
        }

        Fase saved = faseRepository.save(fase);
        return faseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SubfaseResponse crearSubfase(Long faseId, SubfaseCreateRequest request) {
        Fase faseRaiz = obtenerFaseRaizActiva(faseId);
        validarCodigoUnico(request.codigo(), null);

        Fase subfase = faseMapper.toEntity(request, faseRaiz);
        Fase saved = faseRepository.save(subfase);
        return faseMapper.toSubfaseResponse(saved);
    }

    @Override
    public FaseResponse obtenerPorId(Long id) {
        Fase fase = faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(id)
                .orElseThrow(() -> new FaseNotFoundException("Fase no encontrada con id: " + id));
        return faseMapper.toResponse(fase);
    }

    @Override
    public SubfaseResponse obtenerSubfasePorId(Long faseId, Long subfaseId) {
        Fase subfase = obtenerSubfaseActiva(faseId, subfaseId);
        return faseMapper.toSubfaseResponse(subfase);
    }

    @Override
    public PageResponse<FaseSummaryResponse> listar(
            ModalidadAcreditacion modalidad,
            String q,
            Boolean incluirEliminadas,
            Integer ordenMin,
            Integer ordenMax,
            Pageable pageable
    ) {
        boolean incluir = Boolean.TRUE.equals(incluirEliminadas);
        Specification<Fase> spec = FaseSpecifications.combinar(
                FaseSpecifications.esFaseRaiz(),
                modalidad != null ? FaseSpecifications.conModalidad(modalidad) : null,
                FaseSpecifications.incluirEliminadas(incluir),
                FaseSpecifications.buscarPorTexto(q),
                FaseSpecifications.ordenMinimo(ordenMin),
                FaseSpecifications.ordenMaximo(ordenMax)
        );

        Page<Fase> page = faseRepository.findAll(spec, pageable);
        return PageResponse.from(page, fase -> faseMapper.toSummaryResponse(
                fase,
                (int) faseRepository.countByParentIdAndDeletedAtIsNull(fase.getId())
        ));
    }

    @Override
    public PageResponse<SubfaseResponse> listarSubfases(
            Long faseId,
            String q,
            Boolean incluirEliminadas,
            Integer ordenMin,
            Integer ordenMax,
            Pageable pageable
    ) {
        obtenerFaseRaizActiva(faseId);

        boolean incluir = Boolean.TRUE.equals(incluirEliminadas);
        Specification<Fase> spec = FaseSpecifications.combinar(
                FaseSpecifications.perteneceAFase(faseId),
                FaseSpecifications.incluirEliminadas(incluir),
                FaseSpecifications.buscarPorTexto(q),
                FaseSpecifications.ordenMinimo(ordenMin),
                FaseSpecifications.ordenMaximo(ordenMax)
        );

        Page<Fase> page = faseRepository.findAll(spec, pageable);
        return PageResponse.from(page, faseMapper::toSubfaseResponse);
    }

    @Override
    @Transactional
    public FaseResponse actualizar(Long id, FaseUpdateRequest request) {
        Fase fase = faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(id)
                .orElseThrow(() -> new FaseNotFoundException("Fase no encontrada con id: " + id));

        faseMapper.updateEntity(request, fase);

        if (request.subfases() != null) {
            sincronizarSubfases(fase, request.subfases());
        }

        return faseMapper.toResponse(fase);
    }

    @Override
    @Transactional
    public SubfaseResponse actualizarSubfase(Long faseId, Long subfaseId, SubfaseUpdateRequest request) {
        Fase subfase = obtenerSubfaseActiva(faseId, subfaseId);
        validarCodigoUnico(request.codigo(), subfase.getId());
        faseMapper.updateEntity(request, subfase);
        return faseMapper.toSubfaseResponse(subfase);
    }

    @Override
    @Transactional
    public void eliminarSoft(Long id) {
        Fase fase = faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(id)
                .orElseThrow(() -> new FaseNotFoundException("Fase no encontrada con id: " + id));

        marcarEliminada(fase);
        fase.getSubfases().stream()
                .filter(subfase -> !subfase.isDeleted())
                .forEach(this::marcarEliminada);
    }

    @Override
    @Transactional
    public void eliminarSubfaseSoft(Long faseId, Long subfaseId) {
        Fase subfase = obtenerSubfaseActiva(faseId, subfaseId);
        marcarEliminada(subfase);
    }

    @Override
    @Transactional
    public void eliminarHard(Long id) {
        Fase fase = faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(id)
                .orElseThrow(() -> new FaseNotFoundException("Fase no encontrada con id: " + id));
        faseRepository.delete(fase);
    }

    @Override
    @Transactional
    public void eliminarSubfaseHard(Long faseId, Long subfaseId) {
        Fase subfase = obtenerSubfaseActiva(faseId, subfaseId);
        faseRepository.delete(subfase);
    }

    private void sincronizarSubfases(Fase fase, List<SubfaseSyncRequest> subfasesRequest) {
        Set<String> codigosEnRequest = new HashSet<>();
        codigosEnRequest.add(fase.getCodigo());

        for (SubfaseSyncRequest subfaseRequest : subfasesRequest) {
            if (!codigosEnRequest.add(subfaseRequest.codigo())) {
                throw new CodigoDuplicadoException(subfaseRequest.codigo());
            }
        }

        Map<Long, Fase> subfasesExistentes = fase.getSubfases().stream()
                .filter(subfase -> !subfase.isDeleted())
                .collect(Collectors.toMap(Fase::getId, Function.identity()));

        Set<Long> idsEnRequest = subfasesRequest.stream()
                .map(SubfaseSyncRequest::id)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        for (SubfaseSyncRequest subfaseRequest : subfasesRequest) {
            if (subfaseRequest.id() == null) {
                validarCodigoUnico(subfaseRequest.codigo(), null);
                Fase nuevaSubfase = Fase.builder()
                        .codigo(subfaseRequest.codigo())
                        .nombre(subfaseRequest.nombre())
                        .descripcion(subfaseRequest.descripcion())
                        .modalidad(fase.getModalidad())
                        .orden(subfaseRequest.orden())
                        .parent(fase)
                        .build();
                fase.getSubfases().add(nuevaSubfase);
            } else {
                Fase subfase = subfasesExistentes.get(subfaseRequest.id());
                if (subfase == null) {
                    throw new FaseNotFoundException("Subfase no encontrada con id: " + subfaseRequest.id());
                }
                validarCodigoUnico(subfaseRequest.codigo(), subfase.getId());
                faseMapper.updateEntity(subfaseRequest, subfase);
            }
        }

        subfasesExistentes.values().stream()
                .filter(subfase -> !idsEnRequest.contains(subfase.getId()))
                .forEach(this::marcarEliminada);
    }

    private Fase obtenerFaseRaizActiva(Long id) {
        Fase fase = faseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new FaseNotFoundException("Fase no encontrada con id: " + id));

        if (fase.isSubfase()) {
            throw new JerarquiaInvalidaException("La fase con id " + id + " no es una fase raíz");
        }
        return fase;
    }

    private Fase obtenerSubfaseActiva(Long faseId, Long subfaseId) {
        Fase subfase = faseRepository.findByIdAndDeletedAtIsNull(subfaseId)
                .orElseThrow(() -> new FaseNotFoundException("Subfase no encontrada con id: " + subfaseId));

        if (!subfase.isSubfase()) {
            throw new JerarquiaInvalidaException("La fase con id " + subfaseId + " no es una subfase");
        }

        if (subfase.getParent() == null || !subfase.getParent().getId().equals(faseId)) {
            throw new FaseNotFoundException(
                    "Subfase " + subfaseId + " no pertenece a la fase raíz " + faseId
            );
        }

        obtenerFaseRaizActiva(faseId);
        return subfase;
    }

    private void validarCodigoUnico(String codigo, Long idExcluido) {
        boolean duplicado = idExcluido == null
                ? faseRepository.existsByCodigoAndDeletedAtIsNull(codigo)
                : faseRepository.existsByCodigoAndIdNotAndDeletedAtIsNull(codigo, idExcluido);

        if (duplicado) {
            throw new CodigoDuplicadoException(codigo);
        }
    }

    private void validarCodigosSubfasesUnicos(List<SubfaseCreateRequest> subfases, Set<String> codigosExistentes) {
        Set<String> codigos = new HashSet<>(codigosExistentes);
        for (SubfaseCreateRequest subfase : subfases) {
            if (!codigos.add(subfase.codigo())) {
                throw new CodigoDuplicadoException(subfase.codigo());
            }
        }
    }

    private void marcarEliminada(Fase fase) {
        fase.setDeletedAt(LocalDateTime.now());
    }
}
