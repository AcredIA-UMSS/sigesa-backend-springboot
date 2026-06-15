package com.umss.sigesa.repository;

import com.umss.sigesa.domain.model.Fase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FaseRepository extends JpaRepository<Fase, Long>, JpaSpecificationExecutor<Fase> {

    Optional<Fase> findByIdAndDeletedAtIsNull(Long id);

    Optional<Fase> findByCodigoAndDeletedAtIsNull(String codigo);

    boolean existsByCodigoAndDeletedAtIsNull(String codigo);

    boolean existsByCodigoAndIdNotAndDeletedAtIsNull(String codigo, Long id);

    @EntityGraph(attributePaths = "subfases")
    Optional<Fase> findWithSubfasesByIdAndParentIsNullAndDeletedAtIsNull(Long id);

    Page<Fase> findByParentIsNullAndDeletedAtIsNull(Pageable pageable);

    long countByParentIdAndDeletedAtIsNull(Long parentId);
}
