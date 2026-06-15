package com.umss.sigesa.service;

import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import com.umss.sigesa.web.dto.request.FaseCreateRequest;
import com.umss.sigesa.web.dto.request.FaseUpdateRequest;
import com.umss.sigesa.web.dto.request.SubfaseCreateRequest;
import com.umss.sigesa.web.dto.request.SubfaseUpdateRequest;
import com.umss.sigesa.web.dto.response.FaseResponse;
import com.umss.sigesa.web.dto.response.FaseSummaryResponse;
import com.umss.sigesa.web.dto.response.PageResponse;
import com.umss.sigesa.web.dto.response.SubfaseResponse;
import org.springframework.data.domain.Pageable;

public interface FaseService {

    FaseResponse crear(FaseCreateRequest request);

    SubfaseResponse crearSubfase(Long faseId, SubfaseCreateRequest request);

    FaseResponse obtenerPorId(Long id);

    SubfaseResponse obtenerSubfasePorId(Long faseId, Long subfaseId);

    PageResponse<FaseSummaryResponse> listar(
            ModalidadAcreditacion modalidad,
            String q,
            Boolean incluirEliminadas,
            Integer ordenMin,
            Integer ordenMax,
            Pageable pageable
    );

    PageResponse<SubfaseResponse> listarSubfases(
            Long faseId,
            String q,
            Boolean incluirEliminadas,
            Integer ordenMin,
            Integer ordenMax,
            Pageable pageable
    );

    FaseResponse actualizar(Long id, FaseUpdateRequest request);

    SubfaseResponse actualizarSubfase(Long faseId, Long subfaseId, SubfaseUpdateRequest request);

    void eliminarSoft(Long id);

    void eliminarSubfaseSoft(Long faseId, Long subfaseId);

    void eliminarHard(Long id);

    void eliminarSubfaseHard(Long faseId, Long subfaseId);
}
