package com.umss.sigesa.service.impl;

import com.umss.sigesa.domain.exception.CodigoDuplicadoException;
import com.umss.sigesa.domain.exception.FaseNotFoundException;
import com.umss.sigesa.domain.exception.JerarquiaInvalidaException;
import com.umss.sigesa.domain.model.Fase;
import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import com.umss.sigesa.repository.FaseRepository;
import com.umss.sigesa.web.dto.request.FaseCreateRequest;
import com.umss.sigesa.web.dto.request.FaseUpdateRequest;
import com.umss.sigesa.web.dto.request.SubfaseCreateRequest;
import com.umss.sigesa.web.dto.request.SubfaseSyncRequest;
import com.umss.sigesa.web.dto.request.SubfaseUpdateRequest;
import com.umss.sigesa.web.dto.response.FaseResponse;
import com.umss.sigesa.web.dto.response.PageResponse;
import com.umss.sigesa.web.dto.response.SubfaseResponse;
import com.umss.sigesa.web.mapper.FaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaseServiceImplTest {

    @Mock
    private FaseRepository faseRepository;

    private FaseMapper faseMapper;

    @InjectMocks
    private FaseServiceImpl faseService;

    private Fase faseRaiz;
    private Fase subfase;

    @BeforeEach
    void setUp() {
        faseMapper = new FaseMapper();
        faseService = new FaseServiceImpl(faseRepository, faseMapper);

        subfase = Fase.builder()
                .id(10L)
                .codigo("SUB-1")
                .nombre("Subfase 1")
                .descripcion("Desc sub")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .orden(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        faseRaiz = Fase.builder()
                .id(1L)
                .codigo("ROOT-1")
                .nombre("Fase raíz")
                .descripcion("Desc raíz")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .orden(1)
                .subfases(new ArrayList<>(List.of(subfase)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        subfase.setParent(faseRaiz);
    }

    @Test
    void crear_debePersistirFaseConSubfases() {
        FaseCreateRequest request = new FaseCreateRequest(
                "NEW-ROOT",
                "Nueva fase",
                "Descripción",
                ModalidadAcreditacion.CEUB,
                1,
                List.of(new SubfaseCreateRequest("NEW-SUB", "Sub", "Sub desc", 1))
        );

        when(faseRepository.existsByCodigoAndDeletedAtIsNull("NEW-ROOT")).thenReturn(false);
        when(faseRepository.existsByCodigoAndDeletedAtIsNull("NEW-SUB")).thenReturn(false);
        when(faseRepository.save(any(Fase.class))).thenAnswer(invocation -> {
            Fase saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.getSubfases().forEach(sub -> sub.setId(101L));
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            saved.getSubfases().forEach(sub -> {
                sub.setCreatedAt(LocalDateTime.now());
                sub.setUpdatedAt(LocalDateTime.now());
            });
            return saved;
        });

        FaseResponse response = faseService.crear(request);

        assertThat(response.codigo()).isEqualTo("NEW-ROOT");
        assertThat(response.subfases()).hasSize(1);
        assertThat(response.subfases().getFirst().codigo()).isEqualTo("NEW-SUB");
    }

    @Test
    void crear_debeRechazarCodigoDuplicado() {
        when(faseRepository.existsByCodigoAndDeletedAtIsNull("DUP")).thenReturn(true);

        FaseCreateRequest request = new FaseCreateRequest(
                "DUP", "Nombre", null, ModalidadAcreditacion.ARCUSUR, null, null
        );

        assertThatThrownBy(() -> faseService.crear(request))
                .isInstanceOf(CodigoDuplicadoException.class);
    }

    @Test
    void crear_debeRechazarCodigosDuplicadosEntreSubfases() {
        when(faseRepository.existsByCodigoAndDeletedAtIsNull("ROOT")).thenReturn(false);

        FaseCreateRequest request = new FaseCreateRequest(
                "ROOT",
                "Raíz",
                null,
                ModalidadAcreditacion.ARCUSUR,
                1,
                List.of(
                        new SubfaseCreateRequest("IGUAL", "A", null, 1),
                        new SubfaseCreateRequest("IGUAL", "B", null, 2)
                )
        );

        assertThatThrownBy(() -> faseService.crear(request))
                .isInstanceOf(CodigoDuplicadoException.class);
    }

    @Test
    void crearSubfase_debeCrearBajoFaseRaiz() {
        when(faseRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(faseRaiz));
        when(faseRepository.existsByCodigoAndDeletedAtIsNull("SUB-NEW")).thenReturn(false);
        when(faseRepository.save(any(Fase.class))).thenAnswer(invocation -> {
            Fase saved = invocation.getArgument(0);
            saved.setId(20L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        SubfaseResponse response = faseService.crearSubfase(
                1L,
                new SubfaseCreateRequest("SUB-NEW", "Nueva sub", null, 2)
        );

        assertThat(response.codigo()).isEqualTo("SUB-NEW");
        ArgumentCaptor<Fase> captor = ArgumentCaptor.forClass(Fase.class);
        verify(faseRepository).save(captor.capture());
        assertThat(captor.getValue().getParent()).isEqualTo(faseRaiz);
        assertThat(captor.getValue().getModalidad()).isEqualTo(ModalidadAcreditacion.ARCUSUR);
    }

    @Test
    void crearSubfase_debeRechazarSiPadreEsSubfase() {
        when(faseRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(subfase));

        assertThatThrownBy(() -> faseService.crearSubfase(
                10L,
                new SubfaseCreateRequest("X", "X", null, 1)
        )).isInstanceOf(JerarquiaInvalidaException.class);
    }

    @Test
    void obtenerPorId_debeRetornarArbolCompleto() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(faseRaiz));

        FaseResponse response = faseService.obtenerPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.subfases()).hasSize(1);
    }

    @Test
    void obtenerPorId_debeLanzarSiNoExiste() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> faseService.obtenerPorId(99L))
                .isInstanceOf(FaseNotFoundException.class);
    }

    @Test
    void obtenerSubfasePorId_debeRetornarSubfase() {
        when(faseRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(subfase));
        when(faseRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(faseRaiz));

        SubfaseResponse response = faseService.obtenerSubfasePorId(1L, 10L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void obtenerSubfasePorId_debeRechazarSiNoPerteneceAlPadre() {
        Fase otraRaiz = Fase.builder().id(2L).codigo("R2").nombre("R2")
                .modalidad(ModalidadAcreditacion.ARCUSUR).build();
        subfase.setParent(otraRaiz);

        when(faseRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(subfase));

        assertThatThrownBy(() -> faseService.obtenerSubfasePorId(1L, 10L))
                .isInstanceOf(FaseNotFoundException.class);
    }

    @Test
    void listar_debeRetornarPaginaConConteoDeSubfases() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Fase> page = new PageImpl<>(List.of(faseRaiz), pageable, 1);

        when(faseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(faseRepository.countByParentIdAndDeletedAtIsNull(1L)).thenReturn(1L);

        PageResponse<?> response = faseService.listar(
                ModalidadAcreditacion.ARCUSUR, "root", false, 1, 5, pageable
        );

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void listarSubfases_debeValidarFaseRaiz() {
        when(faseRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(faseRaiz));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Fase> page = new PageImpl<>(List.of(subfase), pageable, 1);
        when(faseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<SubfaseResponse> response = faseService.listarSubfases(
                1L, null, false, null, null, pageable
        );

        assertThat(response.content()).hasSize(1);
    }

    @Test
    void actualizar_debeActualizarCamposBasicos() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(faseRaiz));

        FaseResponse response = faseService.actualizar(
                1L,
                new FaseUpdateRequest("Nombre actualizado", "Nueva desc", 2, null)
        );

        assertThat(response.nombre()).isEqualTo("Nombre actualizado");
        assertThat(response.descripcion()).isEqualTo("Nueva desc");
        assertThat(response.orden()).isEqualTo(2);
    }

    @Test
    void actualizar_debeSincronizarSubfases() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(faseRaiz));
        when(faseRepository.existsByCodigoAndIdNotAndDeletedAtIsNull("SUB-1", 10L)).thenReturn(false);
        when(faseRepository.existsByCodigoAndDeletedAtIsNull("SUB-NUEVA")).thenReturn(false);

        FaseUpdateRequest request = new FaseUpdateRequest(
                "Raíz",
                "Desc",
                1,
                List.of(
                        new SubfaseSyncRequest(10L, "SUB-1", "Sub actualizada", "Desc", 1),
                        new SubfaseSyncRequest(null, "SUB-NUEVA", "Nueva", null, 2)
                )
        );

        FaseResponse response = faseService.actualizar(1L, request);

        assertThat(response.subfases()).hasSize(2);
        assertThat(faseRaiz.getSubfases()).hasSize(2);
    }

    @Test
    void actualizar_debeSoftDeleteSubfasesAusentes() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(faseRaiz));

        FaseUpdateRequest request = new FaseUpdateRequest(
                "Raíz",
                null,
                1,
                List.of()
        );

        faseService.actualizar(1L, request);

        assertThat(subfase.getDeletedAt()).isNotNull();
    }

    @Test
    void actualizarSubfase_debeActualizarDatos() {
        when(faseRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(subfase));
        when(faseRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(faseRaiz));
        when(faseRepository.existsByCodigoAndIdNotAndDeletedAtIsNull("SUB-UPD", 10L)).thenReturn(false);

        SubfaseResponse response = faseService.actualizarSubfase(
                1L,
                10L,
                new SubfaseUpdateRequest("SUB-UPD", "Actualizada", "Desc", 3)
        );

        assertThat(response.codigo()).isEqualTo("SUB-UPD");
        assertThat(response.nombre()).isEqualTo("Actualizada");
    }

    @Test
    void eliminarSoft_debeMarcarFaseYSubfases() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(faseRaiz));

        faseService.eliminarSoft(1L);

        assertThat(faseRaiz.getDeletedAt()).isNotNull();
        assertThat(subfase.getDeletedAt()).isNotNull();
        verify(faseRepository, never()).delete(any(Fase.class));
    }

    @Test
    void eliminarSubfaseSoft_debeMarcarSoloSubfase() {
        when(faseRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(subfase));
        when(faseRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(faseRaiz));

        faseService.eliminarSubfaseSoft(1L, 10L);

        assertThat(subfase.getDeletedAt()).isNotNull();
        assertThat(faseRaiz.getDeletedAt()).isNull();
    }

    @Test
    void eliminarHard_debeEliminarFisicamente() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(faseRaiz));

        faseService.eliminarHard(1L);

        verify(faseRepository).delete(faseRaiz);
    }

    @Test
    void eliminarSubfaseHard_debeEliminarSubfase() {
        when(faseRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(subfase));
        when(faseRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(faseRaiz));

        faseService.eliminarSubfaseHard(1L, 10L);

        verify(faseRepository).delete(subfase);
    }

    @Test
    void actualizar_debeRechazarSubfaseInexistenteEnSincronizacion() {
        when(faseRepository.findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(faseRaiz));

        FaseUpdateRequest request = new FaseUpdateRequest(
                "Raíz",
                null,
                1,
                List.of(new SubfaseSyncRequest(999L, "X", "X", null, 1))
        );

        assertThatThrownBy(() -> faseService.actualizar(1L, request))
                .isInstanceOf(FaseNotFoundException.class);
    }
}
