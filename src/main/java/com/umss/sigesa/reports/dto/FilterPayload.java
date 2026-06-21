package com.umss.sigesa.reports.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterPayload {
    private Integer gestion;
    private List<Long> careerIds;
    private List<Long> facultyIds;
    private String processType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<String> statuses;
}
