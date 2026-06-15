package com.umss.sigesa.web.controller;

import com.umss.sigesa.domain.exception.CodigoDuplicadoException;
import com.umss.sigesa.domain.exception.FaseNotFoundException;
import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import com.umss.sigesa.service.FaseService;
import com.umss.sigesa.web.advice.GlobalExceptionHandler;
import com.umss.sigesa.web.dto.response.FaseResponse;
import com.umss.sigesa.web.dto.response.FaseSummaryResponse;
import com.umss.sigesa.web.dto.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class FaseControllerTest {

    @Mock
    private FaseService faseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FaseController controller = new FaseController(faseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new SortHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void crear_debeRetornar201() throws Exception {
        when(faseService.crear(any())).thenReturn(new FaseResponse(
                1L, "COD", "Nombre", "Desc", ModalidadAcreditacion.ARCUSUR, 1,
                List.of(), LocalDateTime.now(), LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/fases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "COD",
                                  "nombre": "Nombre",
                                  "descripcion": "Desc",
                                  "modalidad": "ARCUSUR",
                                  "orden": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("COD"));
    }

    @Test
    void crear_debeRetornar400SiValidacionFalla() throws Exception {
        mockMvc.perform(post("/api/v1/fases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "",
                                  "nombre": "",
                                  "modalidad": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.codigo").exists());
    }

    @Test
    void listar_debeRetornar200() throws Exception {
        when(faseService.listar(any(), any(), eq(false), any(), any(), any()))
                .thenReturn(new PageResponse<>(
                        List.of(new FaseSummaryResponse(
                                1L, "COD", "Nombre", ModalidadAcreditacion.ARCUSUR, 1, 0, LocalDateTime.now()
                        )),
                        0, 20, 1, 1, true, true
                ));

        mockMvc.perform(get("/api/v1/fases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value("COD"));
    }

    @Test
    void obtenerPorId_debeRetornar404() throws Exception {
        when(faseService.obtenerPorId(99L)).thenThrow(new FaseNotFoundException("No encontrada"));

        mockMvc.perform(get("/api/v1/fases/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_debeRetornar409SiCodigoDuplicado() throws Exception {
        when(faseService.actualizar(eq(1L), any()))
                .thenThrow(new CodigoDuplicadoException("DUP"));

        mockMvc.perform(put("/api/v1/fases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"N","descripcion":null,"orden":1,"subfases":null}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminar_debeRetornar204() throws Exception {
        doNothing().when(faseService).eliminarSoft(1L);

        mockMvc.perform(delete("/api/v1/fases/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarHard_debeInvocarServicio() throws Exception {
        doNothing().when(faseService).eliminarHard(1L);

        mockMvc.perform(delete("/api/v1/fases/1").param("hard", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    void crearSubfase_debeRetornar404SiFaseNoExiste() throws Exception {
        when(faseService.crearSubfase(eq(1L), any()))
                .thenThrow(new FaseNotFoundException("No"));

        mockMvc.perform(post("/api/v1/fases/1/subfases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"S1","nombre":"Sub","descripcion":null,"orden":1}
                                """))
                .andExpect(status().isNotFound());
    }
}
