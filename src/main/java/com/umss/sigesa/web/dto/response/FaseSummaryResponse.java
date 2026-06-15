package com.umss.sigesa.web.dto.response;

import com.umss.sigesa.domain.model.ModalidadAcreditacion;

import java.time.LocalDateTime;

public record FaseSummaryResponse(
        Long id,
        String codigo,
        String nombre,
        ModalidadAcreditacion modalidad,
        Integer orden,
        int cantidadSubfases,
        LocalDateTime createdAt
) {
}
