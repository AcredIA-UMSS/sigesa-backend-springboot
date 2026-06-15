package com.umss.sigesa.web.dto.request;

import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FaseCreateRequest(
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 5000) String descripcion,
        @NotNull ModalidadAcreditacion modalidad,
        @Min(0) Integer orden,
        @Valid List<@Valid SubfaseCreateRequest> subfases
) {
}
