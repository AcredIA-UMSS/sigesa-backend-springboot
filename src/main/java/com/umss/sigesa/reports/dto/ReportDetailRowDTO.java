package com.umss.sigesa.reports.dto;

import java.time.LocalDate;

public record ReportDetailRowDTO(
    String careerName,
    String facultyName,
    String indicatorName,
    String evidenceDescription,
    String status,
    LocalDate submissionDate,
    Long evidenceCount
) {}
