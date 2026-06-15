package com.umss.sigesa.web.dto.response;

import com.umss.sigesa.domain.model.ModalidadAcreditacion;

import java.time.LocalDateTime;
import java.util.List;

public record FaseResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        ModalidadAcreditacion modalidad,
        Integer orden,
        List<SubfaseResponse> subfases,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
