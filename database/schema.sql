-- ============================================================
-- Sistema de Gestión Administrativa - Hostal
-- Schema SQL + Seed Data
-- ============================================================

-- Limpiar tablas si existen (para desarrollo)
DROP TABLE IF EXISTS egreso CASCADE;
DROP TABLE IF EXISTS ingreso CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS sede CASCADE;

-- ============================================================
-- TABLA: sede
-- ============================================================
CREATE TABLE sede (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
-- TABLA: usuario
-- Sin columna de rol; todos son recepcionistas.
-- ============================================================
CREATE TABLE usuario (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    nombre_completo  VARCHAR(100) NOT NULL,
    id_sede          BIGINT       NOT NULL REFERENCES sede(id),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ============================================================
-- TABLA: ingreso
-- NO contiene columna id_sede; la sede se obtiene vía JOIN con usuario.
-- ============================================================
CREATE TABLE ingreso (
    id                 BIGSERIAL PRIMARY KEY,
    tipo_habitacion    VARCHAR(20)    NOT NULL,
    monto              NUMERIC(10,2)  NOT NULL,
    observacion        TEXT,
    fecha              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    id_usuario         BIGINT         NOT NULL REFERENCES usuario(id),
    anulado            BOOLEAN        NOT NULL DEFAULT FALSE,
    motivo_anulacion   TEXT,
    anulado_por        BIGINT         REFERENCES usuario(id),
    fecha_anulacion    TIMESTAMPTZ,

    -- Monto siempre positivo
    CONSTRAINT chk_ingreso_monto_positivo CHECK (monto > 0),

    -- Tipo de habitación válido
    CONSTRAINT chk_ingreso_tipo_habitacion CHECK (
        tipo_habitacion IN ('SIMPLE', 'DOBLE', 'MATRIMONIAL', 'TRIPLE', 'CUADRUPLE')
    ),

    -- Si se anula, el motivo es obligatorio y no vacío
    CONSTRAINT chk_ingreso_anulacion_motivo CHECK (
        (anulado = FALSE)
        OR (anulado = TRUE AND motivo_anulacion IS NOT NULL AND TRIM(motivo_anulacion) <> '')
    )
);

-- ============================================================
-- TABLA: egreso
-- NO contiene columna id_sede; la sede se obtiene vía JOIN con usuario.
-- ============================================================
CREATE TABLE egreso (
    id                 BIGSERIAL PRIMARY KEY,
    monto              NUMERIC(10,2)  NOT NULL,
    categoria          VARCHAR(20)    NOT NULL,
    descripcion        TEXT,
    fecha              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    id_usuario         BIGINT         NOT NULL REFERENCES usuario(id),
    anulado            BOOLEAN        NOT NULL DEFAULT FALSE,
    motivo_anulacion   TEXT,
    anulado_por        BIGINT         REFERENCES usuario(id),
    fecha_anulacion    TIMESTAMPTZ,

    -- Monto siempre positivo
    CONSTRAINT chk_egreso_monto_positivo CHECK (monto > 0),

    -- Categoría válida y siempre en mayúsculas
    CONSTRAINT chk_egreso_categoria CHECK (
        categoria IN ('ELECTRICIDAD', 'AGUA', 'INTERNET', 'OTROS')
    ),

    -- Si la categoría es OTROS, la descripción es obligatoria
    CONSTRAINT chk_egreso_otros_descripcion CHECK (
        (categoria <> 'OTROS')
        OR (categoria = 'OTROS' AND descripcion IS NOT NULL AND TRIM(descripcion) <> '')
    ),

    -- Si se anula, el motivo es obligatorio y no vacío
    CONSTRAINT chk_egreso_anulacion_motivo CHECK (
        (anulado = FALSE)
        OR (anulado = TRUE AND motivo_anulacion IS NOT NULL AND TRIM(motivo_anulacion) <> '')
    )
);

-- ============================================================
-- ÍNDICES para consultas frecuentes
-- ============================================================
CREATE INDEX idx_ingreso_usuario_fecha   ON ingreso(id_usuario, fecha);
CREATE INDEX idx_ingreso_usuario_anulado ON ingreso(id_usuario, anulado);
CREATE INDEX idx_egreso_usuario_fecha    ON egreso(id_usuario, fecha);
CREATE INDEX idx_egreso_usuario_anulado  ON egreso(id_usuario, anulado);

-- ============================================================
-- DATOS SEMILLA
-- ============================================================

-- Sedes
INSERT INTO sede (id, nombre) VALUES
    (1, 'Sede Centro'),
    (2, 'Sede Norte');

-- Usuarios con contraseñas BCrypt
-- centro123 -> BCrypt hash
-- norte123  -> BCrypt hash
-- Estas contraseñas son para desarrollo/testing. Regenerar en producción.
INSERT INTO usuario (id, username, password_hash, nombre_completo, id_sede, activo) VALUES
    (1, 'centro', '$2a$10$YeLbuFvpLVp1wNEdPEIc3eVoycGtkNuWRG1d/lnQivo0aiiZkvCrK', 'Recepcionista Centro', 1, TRUE),
    (2, 'norte',  '$2a$10$dwvVIO5DVMVk2RzIPLmD7uf7DM4Wi1fESK2t/Of/1M90BpXn2a/3i',  'Recepcionista Norte',  2, TRUE);

-- Resetear secuencias
SELECT setval('sede_id_seq', 2);
SELECT setval('usuario_id_seq', 2);

-- Ingresos de ejemplo para Sede Centro (usuario 1)
INSERT INTO ingreso (tipo_habitacion, monto, observacion, fecha, id_usuario) VALUES
    ('SIMPLE',      35.00, 'Huésped regular',          NOW() - INTERVAL '6 days', 1),
    ('DOBLE',       60.00, NULL,                        NOW() - INTERVAL '5 days', 1),
    ('MATRIMONIAL', 75.00, 'Incluye desayuno',          NOW() - INTERVAL '4 days', 1),
    ('TRIPLE',      90.00, NULL,                        NOW() - INTERVAL '3 days', 1),
    ('SIMPLE',      35.00, NULL,                        NOW() - INTERVAL '2 days', 1),
    ('CUADRUPLE',  120.00, 'Familia con niños',         NOW() - INTERVAL '1 day',  1),
    ('DOBLE',       60.00, NULL,                        NOW(),                      1),
    ('MATRIMONIAL', 75.00, 'Reserva anticipada',        NOW(),                      1);

-- Ingresos de ejemplo para Sede Norte (usuario 2)
INSERT INTO ingreso (tipo_habitacion, monto, observacion, fecha, id_usuario) VALUES
    ('SIMPLE',      40.00, NULL,                        NOW() - INTERVAL '5 days', 2),
    ('DOBLE',       65.00, 'Turista extranjero',        NOW() - INTERVAL '3 days', 2),
    ('TRIPLE',      95.00, NULL,                        NOW() - INTERVAL '2 days', 2),
    ('MATRIMONIAL', 80.00, NULL,                        NOW() - INTERVAL '1 day',  2),
    ('SIMPLE',      40.00, 'Descuento por estadía larga', NOW(),                   2);

-- Egresos de ejemplo para Sede Centro (usuario 1)
INSERT INTO egreso (monto, categoria, descripcion, fecha, id_usuario) VALUES
    (150.00, 'ELECTRICIDAD', NULL,                       NOW() - INTERVAL '5 days', 1),
    ( 80.00, 'AGUA',         NULL,                       NOW() - INTERVAL '4 days', 1),
    ( 60.00, 'INTERNET',     NULL,                       NOW() - INTERVAL '2 days', 1),
    ( 45.00, 'OTROS',        'Compra de sábanas nuevas', NOW() - INTERVAL '1 day',  1),
    (200.00, 'ELECTRICIDAD', NULL,                       NOW(),                      1);

-- Egresos de ejemplo para Sede Norte (usuario 2)
INSERT INTO egreso (monto, categoria, descripcion, fecha, id_usuario) VALUES
    (130.00, 'ELECTRICIDAD', NULL,                         NOW() - INTERVAL '4 days', 2),
    ( 70.00, 'AGUA',         NULL,                         NOW() - INTERVAL '3 days', 2),
    ( 55.00, 'INTERNET',     NULL,                         NOW() - INTERVAL '1 day',  2),
    ( 30.00, 'OTROS',        'Reparación de cerradura',    NOW(),                      2);
