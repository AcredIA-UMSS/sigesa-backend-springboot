package com.umss.sigesa.web.controller;

import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import com.umss.sigesa.service.FaseService;
import com.umss.sigesa.web.dto.request.FaseCreateRequest;
import com.umss.sigesa.web.dto.request.FaseUpdateRequest;
import com.umss.sigesa.web.dto.request.SubfaseCreateRequest;
import com.umss.sigesa.web.dto.request.SubfaseUpdateRequest;
import com.umss.sigesa.web.dto.response.FaseResponse;
import com.umss.sigesa.web.dto.response.FaseSummaryResponse;
import com.umss.sigesa.web.dto.response.PageResponse;
import com.umss.sigesa.web.dto.response.SubfaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fases")
@RequiredArgsConstructor
public class FaseController {

    private final FaseService faseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaseResponse crear(@Valid @RequestBody FaseCreateRequest request) {
        return faseService.crear(request);
    }

    @GetMapping
    public PageResponse<FaseSummaryResponse> listar(
            @RequestParam(required = false) ModalidadAcreditacion modalidad,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean incluirEliminadas,
            @RequestParam(required = false) Integer ordenMin,
            @RequestParam(required = false) Integer ordenMax,
            @PageableDefault(sort = "orden", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return faseService.listar(modalidad, q, incluirEliminadas, ordenMin, ordenMax, pageable);
    }

    @GetMapping("/{id}")
    public FaseResponse obtenerPorId(@PathVariable Long id) {
        return faseService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public FaseResponse actualizar(@PathVariable Long id, @Valid @RequestBody FaseUpdateRequest request) {
        return faseService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean hard) {
        if (hard) {
            faseService.eliminarHard(id);
        } else {
            faseService.eliminarSoft(id);
        }
    }

    @PostMapping("/{faseId}/subfases")
    @ResponseStatus(HttpStatus.CREATED)
    public SubfaseResponse crearSubfase(
            @PathVariable Long faseId,
            @Valid @RequestBody SubfaseCreateRequest request
    ) {
        return faseService.crearSubfase(faseId, request);
    }

    @GetMapping("/{faseId}/subfases")
    public PageResponse<SubfaseResponse> listarSubfases(
            @PathVariable Long faseId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean incluirEliminadas,
            @RequestParam(required = false) Integer ordenMin,
            @RequestParam(required = false) Integer ordenMax,
            Pageable pageable
    ) {
        return faseService.listarSubfases(faseId, q, incluirEliminadas, ordenMin, ordenMax, pageable);
    }

    @GetMapping("/{faseId}/subfases/{subfaseId}")
    public SubfaseResponse obtenerSubfase(
            @PathVariable Long faseId,
            @PathVariable Long subfaseId
    ) {
        return faseService.obtenerSubfasePorId(faseId, subfaseId);
    }

    @PutMapping("/{faseId}/subfases/{subfaseId}")
    public SubfaseResponse actualizarSubfase(
            @PathVariable Long faseId,
            @PathVariable Long subfaseId,
            @Valid @RequestBody SubfaseUpdateRequest request
    ) {
        return faseService.actualizarSubfase(faseId, subfaseId, request);
    }

    @DeleteMapping("/{faseId}/subfases/{subfaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarSubfase(
            @PathVariable Long faseId,
            @PathVariable Long subfaseId,
            @RequestParam(defaultValue = "false") boolean hard
    ) {
        if (hard) {
            faseService.eliminarSubfaseHard(faseId, subfaseId);
        } else {
            faseService.eliminarSubfaseSoft(faseId, subfaseId);
        }
    }
}
