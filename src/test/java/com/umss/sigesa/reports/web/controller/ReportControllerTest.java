package com.umss.sigesa.reports.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.domain.ReportRun;
import com.umss.sigesa.reports.service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ReportController.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void export_returnsAcceptedAndRun() throws Exception {
        ReportRun run = ReportRun.builder().id(1L).status("PENDING").createdBy("actor").build();
        Mockito.when(reportService.submitExport(anyLong(), any(FilterPayload.class), anyString())).thenReturn(run);

        mvc.perform(post("/api/v1/reports/1/export").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(new FilterPayload())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1));
    }
}
