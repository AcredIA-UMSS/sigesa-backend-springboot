package com.umss.sigesa.reports.dto;

import java.math.BigDecimal;

public record ReportKpiDTO(String key, String description, BigDecimal value) {}
