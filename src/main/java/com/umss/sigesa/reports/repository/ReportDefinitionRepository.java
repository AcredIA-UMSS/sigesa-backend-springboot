package com.umss.sigesa.reports.repository;

import com.umss.sigesa.reports.domain.ReportDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long>, JpaSpecificationExecutor<ReportDefinition> {
    Optional<ReportDefinition> findByCodigoAndDeletedAtIsNull(String codigo);
}
