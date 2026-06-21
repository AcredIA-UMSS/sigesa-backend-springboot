package com.umss.sigesa.reports.service;

import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.dto.ReportDetailRowDTO;
import com.umss.sigesa.reports.dto.ReportKpiDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DashboardService {
    List<ReportKpiDTO> getKpis(FilterPayload filter, Pageable pageable, String actor);
    Page<ReportDetailRowDTO> getPaginatedData(FilterPayload filter, Pageable pageable, String actor);
}
