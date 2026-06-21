package com.umss.sigesa.reports.service;

import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.dto.ReportKpiDTO;
import com.umss.sigesa.reports.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DashboardServiceImplTest {

    @Test
    void getKpis_returnsSampleKpi() {
        DashboardServiceImpl svc = new DashboardServiceImpl();
        FilterPayload filter = new FilterPayload();
        var kpis = svc.getKpis(filter, PageRequest.of(0, 10), "actor1");
        assertThat(kpis).isNotNull();
        assertThat(kpis).isNotEmpty();
        ReportKpiDTO k = kpis.get(0);
        assertThat(k.key()).isEqualTo("sample_kpi");
    }
}
