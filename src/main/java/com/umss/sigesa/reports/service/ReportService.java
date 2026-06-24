package com.umss.sigesa.reports.service;

import com.umss.sigesa.reports.dto.FilterPayload;
import com.umss.sigesa.reports.domain.ReportRun;
import com.umss.sigesa.reports.domain.ReportDefinition;

public interface ReportService {
    ReportDefinition createDefinition(ReportDefinition def, String actor);
    ReportRun submitExport(Long definitionId, FilterPayload filter, String actor);
    ReportRun getRun(Long runId, String actor);
}
