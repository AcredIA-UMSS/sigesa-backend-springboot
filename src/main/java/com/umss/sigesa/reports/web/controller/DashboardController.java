package com.umss.sigesa.reports.web.controller;

import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.dto.ReportDetailRowDTO;
import com.umss.sigesa.reports.dto.ReportKpiDTO;
import com.umss.sigesa.reports.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<List<ReportKpiDTO>> getKpis(FilterPayload filter) {
        Pageable p = PageRequest.of(0, 20);
        return ResponseEntity.ok(dashboardService.getKpis(filter, p, "unknown"));
    }

    @GetMapping("/data")
    public ResponseEntity<Page<ReportDetailRowDTO>> getData(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, FilterPayload filter) {
        Pageable p = PageRequest.of(page, size);
        return ResponseEntity.ok(dashboardService.getPaginatedData(filter, p, "unknown"));
    }
}
