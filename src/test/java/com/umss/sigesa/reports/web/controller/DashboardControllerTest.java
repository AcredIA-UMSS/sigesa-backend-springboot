package com.umss.sigesa.reports.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.reports.dto.ReportKpiDTO;
import com.umss.sigesa.reports.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(controllers = DashboardController.class)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getKpis_returnsOkAndJson() throws Exception {
        ReportKpiDTO sample = new ReportKpiDTO("k1","Sample", BigDecimal.ONE);
        Mockito.when(dashboardService.getKpis(any(), any(PageRequest.class), anyString())).thenReturn(List.of(sample));

        mvc.perform(get("/api/v1/dashboard/kpis").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("k1"));
    }
}
