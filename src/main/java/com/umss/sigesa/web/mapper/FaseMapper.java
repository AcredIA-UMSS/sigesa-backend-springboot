package com.umss.sigesa.web.mapper;

import com.umss.sigesa.domain.model.Fase;
import com.umss.sigesa.web.dto.request.FaseCreateRequest;
import com.umss.sigesa.web.dto.request.FaseUpdateRequest;
import com.umss.sigesa.web.dto.request.SubfaseCreateRequest;
import com.umss.sigesa.web.dto.request.SubfaseSyncRequest;
import com.umss.sigesa.web.dto.request.SubfaseUpdateRequest;
import com.umss.sigesa.web.dto.response.FaseResponse;
import com.umss.sigesa.web.dto.response.FaseSummaryResponse;
import com.umss.sigesa.web.dto.response.SubfaseResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class FaseMapper {

    public Fase toEntity(FaseCreateRequest request) {
        return Fase.builder()
                .codigo(request.codigo())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .modalidad(request.modalidad())
                .orden(request.orden())
                .build();
    }

    public Fase toEntity(SubfaseCreateRequest request, Fase parent) {
        return Fase.builder()
                .codigo(request.codigo())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .modalidad(parent.getModalidad())
                .orden(request.orden())
                .parent(parent)
                .build();
    }

    public void updateEntity(FaseUpdateRequest request, Fase entity) {
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
        entity.setOrden(request.orden());
    }

    public void updateEntity(SubfaseUpdateRequest request, Fase entity) {
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
        entity.setOrden(request.orden());
    }

    public void updateEntity(SubfaseSyncRequest request, Fase entity) {
        entity.setCodigo(request.codigo());
        entity.setNombre(request.nombre());
        entity.setDescripcion(request.descripcion());
        entity.setOrden(request.orden());
    }

    public FaseResponse toResponse(Fase entity) {
        List<SubfaseResponse> subfases = entity.getSubfases().stream()
                .filter(subfase -> !subfase.isDeleted())
                .sorted(Comparator.comparing(Fase::getOrden, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Fase::getId))
                .map(this::toSubfaseResponse)
                .toList();

        return new FaseResponse(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getModalidad(),
                entity.getOrden(),
                subfases,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public FaseSummaryResponse toSummaryResponse(Fase entity) {
        long cantidadSubfases = entity.getSubfases().stream()
                .filter(subfase -> !subfase.isDeleted())
                .count();
        return toSummaryResponse(entity, (int) cantidadSubfases);
    }

    public FaseSummaryResponse toSummaryResponse(Fase entity, int cantidadSubfases) {
        return new FaseSummaryResponse(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getModalidad(),
                entity.getOrden(),
                cantidadSubfases,
                entity.getCreatedAt()
        );
    }

    public SubfaseResponse toSubfaseResponse(Fase entity) {
        return new SubfaseResponse(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getOrden(),
                0,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
