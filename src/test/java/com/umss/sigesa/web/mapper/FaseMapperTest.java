package com.umss.sigesa.web.mapper;

import com.umss.sigesa.domain.model.Fase;
import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import com.umss.sigesa.web.dto.request.FaseCreateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FaseMapperTest {

    private final FaseMapper mapper = new FaseMapper();

    @Test
    void toResponse_debeExcluirSubfasesEliminadas() {
        Fase raiz = Fase.builder()
                .id(1L)
                .codigo("R")
                .nombre("Raíz")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .subfases(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Fase activa = Fase.builder()
                .id(2L)
                .codigo("A")
                .nombre("Activa")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .parent(raiz)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Fase eliminada = Fase.builder()
                .id(3L)
                .codigo("E")
                .nombre("Eliminada")
                .modalidad(ModalidadAcreditacion.ARCUSUR)
                .parent(raiz)
                .deletedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        raiz.getSubfases().addAll(List.of(activa, eliminada));

        assertThat(mapper.toResponse(raiz).subfases()).hasSize(1);
    }

    @Test
    void toEntity_debeMapearCamposDeCreacion() {
        FaseCreateRequest request = new FaseCreateRequest(
                "COD", "Nombre", "Desc", ModalidadAcreditacion.CEUB, 2, null
        );

        Fase entity = mapper.toEntity(request);

        assertThat(entity.getCodigo()).isEqualTo("COD");
        assertThat(entity.getModalidad()).isEqualTo(ModalidadAcreditacion.CEUB);
        assertThat(entity.getOrden()).isEqualTo(2);
    }
}
