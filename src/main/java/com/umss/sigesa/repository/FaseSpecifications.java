package com.umss.sigesa.repository;

import com.umss.sigesa.domain.model.Fase;
import com.umss.sigesa.domain.model.ModalidadAcreditacion;
import org.springframework.data.jpa.domain.Specification;

public final class FaseSpecifications {

    private FaseSpecifications() {
    }

    public static Specification<Fase> esFaseRaiz() {
        return (root, query, cb) -> cb.isNull(root.get("parent"));
    }

    public static Specification<Fase> perteneceAFase(Long faseId) {
        return (root, query, cb) -> cb.equal(root.get("parent").get("id"), faseId);
    }

    public static Specification<Fase> conModalidad(ModalidadAcreditacion modalidad) {
        return (root, query, cb) -> cb.equal(root.get("modalidad"), modalidad);
    }

    public static Specification<Fase> activas() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Fase> incluirEliminadas(boolean incluirEliminadas) {
        return incluirEliminadas ? null : activas();
    }

    public static Specification<Fase> buscarPorTexto(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String pattern = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("codigo")), pattern),
                cb.like(cb.lower(root.get("nombre")), pattern)
        );
    }

    public static Specification<Fase> ordenMinimo(Integer ordenMin) {
        if (ordenMin == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orden"), ordenMin);
    }

    public static Specification<Fase> ordenMaximo(Integer ordenMax) {
        if (ordenMax == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("orden"), ordenMax);
    }

    @SafeVarargs
    public static Specification<Fase> combinar(Specification<Fase>... specs) {
        Specification<Fase> result = (root, query, cb) -> cb.conjunction();
        for (Specification<Fase> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}
