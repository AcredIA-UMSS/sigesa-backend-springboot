package com.umss.sigesa.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FaseUpdateRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 5000) String descripcion,
        @Min(0) Integer orden,
        @Valid List<@Valid SubfaseSyncRequest> subfases
) {
}
