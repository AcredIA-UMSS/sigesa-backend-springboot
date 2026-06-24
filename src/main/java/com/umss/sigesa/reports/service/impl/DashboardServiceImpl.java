package com.umss.sigesa.reports.service.impl;

import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.dto.ReportDetailRowDTO;
import com.umss.sigesa.reports.dto.ReportKpiDTO;
import com.umss.sigesa.reports.service.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public List<ReportKpiDTO> getKpis(FilterPayload filter, Pageable pageable, String actor) {
        List<ReportKpiDTO> list = new ArrayList<>();
        list.add(new ReportKpiDTO("sample_kpi", "Sample KPI", BigDecimal.valueOf(123)));
        return list;
    }

    @Override
    public Page<ReportDetailRowDTO> getPaginatedData(FilterPayload filter, Pageable pageable, String actor) {
        List<ReportDetailRowDTO> rows = new ArrayList<>();
        rows.add(new ReportDetailRowDTO("Sample Career","Sample Faculty","Sample Indicator","Sample", null, null, 1L));
        return new PageImpl<>(rows, pageable, rows.size());
    }
}
