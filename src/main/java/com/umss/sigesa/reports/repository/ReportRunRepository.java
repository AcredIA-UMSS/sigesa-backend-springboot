package com.umss.sigesa.reports.repository;

import com.umss.sigesa.reports.domain.ReportRun;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRunRepository extends JpaRepository<ReportRun, Long> {
    Page<ReportRun> findByCreatedBy(String createdBy, Pageable pageable);
}
