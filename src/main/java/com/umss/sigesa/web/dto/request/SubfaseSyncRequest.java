package com.umss.sigesa.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubfaseSyncRequest(
        Long id,
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 5000) String descripcion,
        @Min(0) Integer orden
) {
}
