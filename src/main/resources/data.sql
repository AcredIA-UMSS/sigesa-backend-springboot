-- Datos de prueba: Universidad Mayor de San Simón (UMSS) - Cochabamba, Bolivia
-- Procesos de acreditación institucional ARCUSUR y CEUB

-- ========== FASES RAÍZ ARCUSUR ==========

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
VALUES (
    'ARC-AUTOEVAL-UMSS',
    'Autoevaluación institucional',
    'Diagnóstico integral de la gestión académica, investigación y extensión de la UMSS conforme a criterios ARCUSUR.',
    'ARCUSUR',
    1,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
VALUES (
    'ARC-EVAL-EXTERN-UMSS',
    'Evaluación externa por pares',
    'Visita de pares evaluadores ARCUSUR a sedes UMSS: Cochabamba, Sacaba, Llallagua y Montero.',
    'ARCUSUR',
    2,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
VALUES (
    'ARC-DICTAMEN-UMSS',
    'Dictamen y resolución de acreditación',
    'Emisión del dictamen final ARCUSUR sobre la acreditación institucional de la UMSS.',
    'ARCUSUR',
    3,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);

-- Subfases ARCUSUR - Autoevaluación
INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'ARC-AUTO-CURRICULUM',
    'Currículo y diseño curricular',
    'Mallas curriculares de Ingeniería Civil, Medicina, Derecho y Arquitectura UMSS.',
    'ARCUSUR',
    1,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'ARC-AUTOEVAL-UMSS';

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'ARC-AUTO-DOCENTES',
    'Cuerpo docente y formación',
    'Registro de docentes tiempo completo, categorías académicas y programas de posgrado UMSS.',
    'ARCUSUR',
    2,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'ARC-AUTOEVAL-UMSS';

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'ARC-AUTO-INFRA',
    'Infraestructura universitaria',
    'Evidencias fotográficas de aulas, laboratorios del Parque Científico y biblioteca central UMSS.',
    'ARCUSUR',
    3,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'ARC-AUTOEVAL-UMSS';

-- Subfases ARCUSUR - Evaluación externa
INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'ARC-EXT-INFORME',
    'Informe de pares evaluadores',
    'Documento consolidado del equipo de evaluación externa ARCUSUR.',
    'ARCUSUR',
    1,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'ARC-EVAL-EXTERN-UMSS';

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'ARC-EXT-VISITA',
    'Registro de visita in situ',
    'Actas y evidencias de la visita a facultades y direcciones centrales UMSS.',
    'ARCUSUR',
    2,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'ARC-EVAL-EXTERN-UMSS';

-- ========== FASES RAÍZ CEUB ==========

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
VALUES (
    'CEUB-PLAN-UMSS',
    'Planificación de evaluación CEUB',
    'Definición del plan de evaluación por brechas para programas acreditables de la UMSS.',
    'CEUB',
    1,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
VALUES (
    'CEUB-EVID-UMSS',
    'Recolección de evidencias CEUB',
    'Compilación de evidencias documentales e imágenes por estándares CEUB en la UMSS.',
    'CEUB',
    2,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
VALUES (
    'CEUB-RESOL-UMSS',
    'Resolución de acreditación CEUB',
    'Resolución del Ministerio de Educación sobre acreditación CEUB de programas UMSS.',
    'CEUB',
    3,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);

-- Subfases CEUB - Recolección de evidencias
INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'CEUB-EVID-DOC',
    'Documentos normativos UMSS',
    'Estatuto orgánico, reglamentos internos y resoluciones rectorales vigentes.',
    'CEUB',
    1,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'CEUB-EVID-UMSS';

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'CEUB-EVID-IMG',
    'Imágenes de infraestructura',
    'Fotografías de campus Umantata, Uyuni y unidades desconcentradas UMSS.',
    'CEUB',
    2,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'CEUB-EVID-UMSS';

INSERT INTO fase (codigo, nombre, descripcion, modalidad, orden, parent_id, deleted_at, created_at, updated_at, created_by, updated_by)
SELECT
    'CEUB-EVID-INVEST',
    'Evidencias de investigación',
    'Proyectos Fondo de Investigación UMSS (FIN) y publicaciones indexadas.',
    'CEUB',
    3,
    id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM fase WHERE codigo = 'CEUB-EVID-UMSS';

-- Fase raíz sin subfases (CEUB resolución)
-- CEUB-RESOL-UMSS queda sin subfases intencionalmente
