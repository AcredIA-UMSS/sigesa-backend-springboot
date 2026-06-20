# Diseño Técnico — Reporting / Dashboard (Backend) — v1.1 (Final)

> **Proyecto:** SIGESA — Sistema de Gestión de Acreditación Universitaria
> **Feature:** Reporting / Dashboard (backend)
> **Versión del documento:** 1.1
> **Stack:** Java 21 · Spring Boot · Spring Data JPA · Hibernate Types (jsonb) · H2 (dev) / PostgreSQL (prod)

---

## 1. Objetivo
Proveer un diseño técnico backend claro, accionable y alineado con el estilo de `base_design_system.md` para implementar un módulo de reportes y dashboards que: (a) atienda las necesidades interactivas de la UI (KPIs y tablas paginadas) con baja latencia; (b) soporte exportaciones pesadas en formato Excel (.xlsx) por procesamiento asíncrono; (c) garantice seguridad de datos mediante inyección obligatoria de límites de alcance (RBAC) y limpieza estricta de datos exportados.

Este documento explica el porqué y el cómo de cada decisión, proporcionando DDL, snippets Java (Lombok + hibernate-types), DTOs, contratos de API y un plan de pruebas.

---

## 2. Alcance (ampliado y justificado)
Tabla y explicaciones: Prioriza el *por qué*.

| Incluido | Por qué | Excluido | Por qué no en v1.1 |
|---|---|---|---|
| Endpoints Dashboard (Sync): GET /api/v1/dashboard/... | UI necesita respuestas rápidas; evitar overhead de creación de Run | Frontend/UI rendering | Responsabilidad de frontend (layouts, charts) |
| Endpoints Reports (Async) para exportación .xlsx | Exportaciones pueden ser pesadas; procesarlas async protege la UI | CSV simple | .xlsx permite multi-hoja y mejor formateo para usuarios de coordinación |
| Pipeline unificado de filtros con inyección RBAC | Evita fugas de datos y asegura coherencia entre KPIs y filas | Confiar en filtros cliente | Inseguro; no garantiza alcance del usuario |
| Mapping JSONB usando hibernate-types | Mantiene tipos Java robustos para params y filtros | Almacenamiento de JSON como String | Pierde seguridad de tipos y búsqueda indexada |
| Caching de consultas (Caffeine) para KPIs | Reduce latencia en dashboard | Cache para export heavy runs | Exports must be fresh/snapshot-based |
| Export streaming a S3/MinIO y pre-signed URLs | Evita OOM y permite descarga segura | Almacenamiento en DB blob | Ineficiente para grandes archivos |

---

## 3. Modelo de Dominio (entidades y rationale)
Objetivo: mantener modelos minimalistas, auditables y versionados.

Entities (detallado con propósito):

- ReportDefinition
  - Propósito: describe un reporte reutilizable (metadata). Incluye `filters_allowed` y `metrics` en JSONB para flexibilidad. Versionado obligatorio (`version`) para trazabilidad.
  - Campos clave: `codigo` (único, legible), `audiences` (who may see/execute), `filtersAllowed` (restricción de parámetros permitidos).

- ReportRun
  - Propósito: historial inmutable de exportaciones (async) y su meta (KPIs snapshot + download_url).
  - Campos clave: `status` (ENUM, control estricto), `params` (JSONB), `result_metadata` (JSONB con KPIs), `download_url` (pre-signed), `created_by`.
  - Rationale: separar sync reads de runs evita contención y facilita auditoría.

- FilterPayload (value object)
  - Propósito: representación tipada de filtros aplicables (gestion, careerIds, facultyIds, processType, dateFrom/dateTo, status). Es la única fuente de truth que toca la BD.
  - Uso: mutado por el SecurityInjector antes de consultas.

- ReportMetric (POJO en metrics JSON)
  - Propósito: define nombre, expression (SQL/DSL), tipo de agregación y time_grain.

---

## 4. Base de Datos (DDL y explicaciones)
Se prioriza Postgres en prod; se incluye `report_run_status` ENUM y JSONB mapping.

```sql
-- Enum para status de ejecución: asegura valores válidos y permite consultas indexadas
CREATE TYPE IF NOT EXISTS report_run_status AS ENUM ('PENDING','PROCESSING','COMPLETED','FAILED');

CREATE TABLE report_definition (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo VARCHAR(100) NOT NULL UNIQUE,
  nombre VARCHAR(255) NOT NULL,
  descripcion TEXT,
  owner_role VARCHAR(50), -- rol responsable (ej. CC, TD)
  audiences JSONB, -- lista de roles o grupos que pueden ver/ejecutar
  filters_allowed JSONB, -- schema de filtros permitidos (ej. {"careerId": "LONG", ...})
  metrics JSONB, -- definición de métricas y expresiones
  version INT DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP
);

-- Tabla de ejecuciones (report_run) para exportaciones async
CREATE TABLE report_run (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  report_definition_id BIGINT NOT NULL REFERENCES report_definition(id),
  params JSONB NOT NULL, -- FilterPayload serializado
  status report_run_status NOT NULL DEFAULT 'PENDING',
  started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  finished_at TIMESTAMP,
  result_json JSONB, -- optional raw result snapshot
  result_metadata JSONB, -- KPIs snapshot (structured) para quick view
  download_url VARCHAR(1000), -- pre-signed URL to S3/MinIO
  created_by VARCHAR(100)
);

-- Indexes para aceleración de consultas frecuentes y filtros compuestos
CREATE INDEX idx_report_def_codigo ON report_definition(codigo);
CREATE INDEX idx_report_run_started_at ON report_run(started_at);
CREATE INDEX idx_report_run_status ON report_run(status);
CREATE INDEX idx_report_run_def ON report_run(report_definition_id);
-- Evidence indexes (assumes evidencia table exists)
CREATE INDEX IF NOT EXISTS idx_evidence_career ON evidencia (career_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_evidence_faculty ON evidencia (faculty_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_evidence_process_type ON evidencia (process_type) WHERE deleted_at IS NULL;
```

Notas de diseño:
- JSONB permite consultas y indexado (GIN) si es necesario para búsquedas por claves de params.
- H2 en dev puede almacenar JSON en TEXT; tests deben mockear comportamiento JSONB o usar testcontainers Postgres para integración real.

---

## 5. Capa de Persistencia (JPA + hibernate-types)
Recomendación: incluir `com.vladmihalcea:hibernate-types-52` para mapear JSONB a Map/POJO.

Ejemplo entity con mapeo JSONB:

```java
@Entity
@Table(name = "report_definition")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportDefinition {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String codigo;

  @Column(nullable = false, length = 255)
  private String nombre;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Type(com.vladmihalcea.hibernate.type.json.JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> filtersAllowed; // typed access to filter schema

  @Type(com.vladmihalcea.hibernate.type.json.JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> metrics; // metric definitions

  private Integer version;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
```

Repository signatures:

```java
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long>, JpaSpecificationExecutor<ReportDefinition> {
  Optional<ReportDefinition> findByCodigoAndDeletedAtIsNull(String codigo);
}

public interface ReportRunRepository extends JpaRepository<ReportRun, Long> {
  Page<ReportRun> findByCreatedBy(String createdBy, Pageable pageable);
}
```

---

## 6. DTOs y FilterPayload (tipado y propósito)
Se debe evitar exponer entidades. DTOs definen columnas exportables y API contract.

FilterPayload (POJO usado a lo largo del pipeline):
```java
public class FilterPayload {
  private Integer gestion; // por qué: año o gestión académica
  private List<Long> careerIds; // por qué: permite filtrado por carreras específicas
  private List<Long> facultyIds; // por qué: ámbito facultad
  private String processType; // NATIONAL | REGIONAL — por qué: cambia reglas de negocio
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private List<String> statuses; // APROBADO, EN_PROCESO, etc.
}
```

Export DTOs (ejemplo para hoja Detallada):
```java
public record ReportDetailRowDTO(
  String careerName,
  String facultyName,
  String indicatorName,
  String evidenceDescription,
  String status,
  LocalDate submissionDate,
  Long evidenceCount
) {}
```

Export DTOs (hoja Resumen / KPIs):
```java
public record ReportKpiDTO(String key, String description, BigDecimal value) {}
```

Rationale: los DTOs contienen etiquetas legibles (nombres y descripciones) en vez de códigos crípticos; son la única fuente usada para escribir celdas en Excel.

---

## 7. Capa de Servicio — contratos y separación Sync vs Async
Interface pública (resumen):

```java
public interface DashboardService {
  DashboardKpiResponse getKpis(FilterPayload filter, Pageable pageable, String actor);
  Page<ReportDetailRowDTO> getPaginatedData(FilterPayload filter, Pageable pageable, String actor);
}

public interface ReportService {
  ReportDefinitionResponse createDefinition(ReportDefinitionCreateRequest req, String actor);
  ReportRunResponse submitExport(Long definitionId, FilterPayload filter, String actor); // creates ReportRun and returns runId
  ReportRunResponse getRun(Long runId, String actor);
}
```

Arquitectura de ejecución:
- DashboardService methods: Synchronous read-only; validate and mutate FilterPayload via SecurityInjector; hit DB or Caffeine cache; return DTOs.
- ReportService.submitExport: create ReportRun (status=PROCESSING), return 202 + runId; schedule async worker (Virtual Threads) to compute KPIs, stream rows to Excel using DTOs, upload to storage and update ReportRun with download_url and status.

SecurityInjector (cross-cutting):
- Implementar como Aspect (`@Around`) o como primer paso en controller that calls `FilterSanitizer.applySecurityContext(filter, SecurityContextHolder.getContext())`.
- Must enforce scope: if actor has role CC tied to career X, enforce filter.careerIds = [X]; if actor broader (JD), allow broader or all.

---

## 8. Export Engine (.xlsx) — rules and implementation notes
Requirements:
- Use streaming API (Alibaba EasyExcel recommended for simplicity or Apache POI SXSSFWorkbook for standard libs).
- Two sheets: "Resumen y Métricas" and "Datos Detallados".
- Always serialize DTOs -> Excel columns; NEVER dump entities.
- Cell content: prefer human-readable descriptions; e.g., `careerName` instead of `careerId`.
- Prevent inclusion of technical columns: exclude fields like `gtin`, `internal_id`, `Unnamed: 0`, surrogate keys.
- Memory: stream rows directly to output stream; flush periodically.

Pseudo-flow of async worker:
1. Load ReportDefinition and validated FilterPayload.
2. Compute KPIs (using same sanitized FilterPayload) and persist `result_metadata` in ReportRun.
3. Stream detailed rows as ReportDetailRowDTO via a JDBC cursor or optimized query; write to sheet 2.
4. Write sheet 1 with filters and KPI rows.
5. Upload file to S3/MinIO and obtain pre-signed URL.
6. Update ReportRun: status=COMPLETED, download_url, finished_at.
7. On failure set status=FAILED with error metadata.

Security: pre-signed URLs expire; set short TTL and require auth when appropriate.

---

## 9. API REST — endpoints, semantics y por qué
Explicar cada ruta y su propósito.

| Método | Ruta | Descripción detallada (por qué) |
|---|---|---|
| GET | /api/v1/dashboard/kpis | Endpoint Sync para KPIs de dashboard. Recupera KPIs usando FilterPayload inyectado. No crea ReportRun. Rápido para UI. |
| GET | /api/v1/dashboard/data | Endpoint Sync paginado para filas detalladas. Ideal para tables; limita page/size to reasonable maxima. |
| POST | /api/v1/reports | Crea ReportDefinition (roles: [CC],[TD]). Se versiona. |
| PUT  | /api/v1/reports/{id} | Actualiza definition; increments version; invalidates cache. |
| POST | /api/v1/reports/{id}/export | Endpoint Async: crea ReportRun (PROCESSING) y devuelve 202 + runId. Delegates to Virtual Threads worker for Excel generation. |
| GET  | /api/v1/reports/runs/{runId} | Estado de la ejecución y, si COMPLETED, `download_url`. |

Request/Response semantics:
- POST export: body = FilterPayload (client filters). Server will mutate via SecurityInjector and persist mutated `params` in ReportRun for audit.
- Responses include clear guidance on next steps (e.g., poll `/reports/runs/{runId}`).

Errors:
- 422 if filters invalid (e.g., careerId not in catalog)
- 403 if actor unauthorized for requested scope
- 202 accepted for accepted exports

---

## 10. Manejo de Errores, Observabilidad y Retries
- Retries: export worker retries transient DB/storage errors with exponential backoff (max 3 attempts).
- Observability: emit events/metrics for queue length, export durations, failure rates.
- Failure handling: persist `error` field in ReportRun.result_metadata; return 500 on unexpected errors for sync endpoints.

---

## 11. Estructura de Paquetes (explicación)
```
com.umss.sigesa.reports
├── domain         -- JPA entities (ReportDefinition, ReportRun)
├── repository     -- Spring Data repositories
├── service        -- DashboardService (sync) and ReportService (async)
├── service.impl   -- Implementations and async worker
├── web
│   ├── controller -- DashboardController (sync) and ReportController (async)
│   └── dto        -- DTOs and FilterPayload
└── config         -- SecurityInjector, Async config (VirtualThreads executor)
```
Rationale: separación clara entre sync and async simplifies testing and prevents accidental creation of runs by UI reads.

---

## 12. Pruebas (detalladas)
Targets: Service unit tests >= 90% coverage; Controller integration `@WebMvcTest`; Repository `@DataJpaTest`.

Critical tests:
- SecurityInjector enforces scope for CC (attempt to request data outside career -> 403/filtered)
- DashboardService returns cached KPIs and respects pagination
- ReportService.submitExport creates ReportRun and worker completes: file generated, upload invoked, run status COMPLETED
- Export worker sanitization: verify exported DTO columns, no internal ids
- Sad Path: invalid filter values -> 422
- Failure: worker transient failure -> retry and eventual FAILED with metadata

---

## 13. Supuestos y limitaciones
- JWT contains role and optionally bound career/faculty claims for CC users.
- Evidence versioning exists and is referenced; reports reference evidence_version_id when needed.
- H2 in dev stores JSON as TEXT; integration tests should use Postgres (testcontainers) to validate JSONB mappings.

---

## 14. Próximos pasos (implementación inmediata)
1. Add hibernate-types dependency and configure `@Type` mapping.
2. Implement FilterPayload POJO and SecurityInjector Aspect.
3. Implement DashboardService (sync) first; add Caffeine cache for KPIs.
4. Implement ReportService.submitExport: create ReportRun, enqueue Virtual Thread to run export; implement export worker producing .xlsx streaming to MinIO and update run.
5. Write unit + integration tests and CI job with JaCoCo threshold.

---

## Quick Start (developer steps)
1. Add dependency: `com.vladmihalcea:hibernate-types-52` and configure JsonType.
2. Implement entities ReportDefinition and ReportRun with JSONB mapping.
3. Implement SecurityInjector to mutate FilterPayload from JWT claims.
4. Create DashboardController endpoints (GET /dashboard/kpis, /dashboard/data).
5. Create ReportController POST /reports/{id}/export and async worker using Virtual Threads.

---

*Este documento respeta la plantilla de `base_design_system.md` y documenta el porqué de las decisiones clave (seguridad, performance, export hygiene).*

