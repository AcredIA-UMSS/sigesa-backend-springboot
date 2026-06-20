# Base Design System — Backend (SIGESA)

**Versión:** 1.0
**Derivado de:** app/sigesa-backend/design_phases.md
**Stack:** Java 21 · Spring Boot · Spring Data JPA · H2/Postgres

## Objetivo
Documento maestro que define las convenciones y plantillas a seguir para los documentos de diseño backend del proyecto SIGESA.

## Convenciones principales
- Header obligatorio: Proyecto, Feature, Versión, Stack, Autor, Fecha.
- Estructura del documento (siempre): Objetivo → Alcance → Modelo de Dominio → BD → Persistencia → DTOs → Servicio → Borrado/Retención → API REST → Errores → Paquetes → Pruebas → Supuestos → Próximos pasos.
- Terminología: usar el glosario (context/03_domain_glossary.md). Preferir: Evidence/Evidencia, Phase/Fase, [CC], [TD], [JD].
- Entidades JPA: no exponerlas en controladores; siempre mapear a DTOs (MapStruct recomendado).
- Auditoría: `created_at`, `updated_at`, `created_by`, `updated_by` (usar @EnableJpaAuditing y anotaciones de Spring Data).
- Soft delete: `deleted_at TIMESTAMP`; consultas filtran `deleted_at IS NULL` por defecto.
- Unicidad y codigo: clave única global para `codigo` con constraints que incluyen soft-deleted (documentar política de reutilización).

## Plantillas rápidas
- API pagination: `page`, `size`, `sort` (ej. `sort=orden,asc`).
- PageResponse wrapper: content, page, size, totalElements, totalPages, first, last.
- Error response uniforme: { timestamp, status, error, message, path }

## Testing & Calidad
- Cobertura objetivo por capa: Service >= 90% (JaCoCo), Controller tests `@WebMvcTest`, Repository `@DataJpaTest`.
- Casos críticos: invariantes de dominio (jerarquía, unicidad, soft delete), y Sad Paths (RBAC, filtros inválidos).

## Paquete sugerido
```
com.umss.sigesa
├── domain
├── repository
├── service
├── web
└── config
```

## Quick Start (implementación mínima)
1. Definir entidad JPA con Lombok y auditable.
2. Crear Repository extends JpaRepository + Specification executor.
3. Implementar Service con transacciones y pruebas unitarias.
4. Exponer Controller con DTOs y validaciones `@Valid`.
5. Añadir pruebas `@DataJpaTest` y `@WebMvcTest`.

---

*Este documento sirve como plantilla base para todos los diseños backend.*