package com.umss.sigesa.web.dto.response;

import java.time.LocalDateTime;

public record SubfaseResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer orden,
        int cantidadEvidencias,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
