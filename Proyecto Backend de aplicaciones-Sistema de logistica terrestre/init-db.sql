-- =====================================================
-- SCRIPT DE INICIALIZACIÓN - BD TPI BACKEND
-- Base de datos PostgreSQL local
-- =====================================================
-- 
-- Este archivo contiene:
-- 1. Creación de schemas (gestion, flota, logistica)
-- 2. Creación de todas las tablas según el diagrama ER
-- 3. Datos de prueba completos (200+ registros)
-- 4. Índices para optimizar consultas
--
-- SE EJECUTA AUTOMÁTICAMENTE AL LEVANTAR DOCKER
-- =====================================================

-- ============================================================
-- PASO 1: CREAR SCHEMAS
-- ============================================================

CREATE SCHEMA IF NOT EXISTS gestion;
CREATE SCHEMA IF NOT EXISTS flota;
CREATE SCHEMA IF NOT EXISTS logistica;


-- ============================================================
-- PASO 2: SCHEMA GESTION - Tablas según diagrama ER
-- ============================================================

-- Tabla: CLIENTES
CREATE TABLE IF NOT EXISTS gestion.clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefono VARCHAR(50),
    cuil VARCHAR(20)
);

COMMENT ON TABLE gestion.clientes IS 'Clientes del sistema - representantes de empresas';


-- Tabla: DEPOSITOS
CREATE TABLE IF NOT EXISTS gestion.depositos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion TEXT NOT NULL,
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    costo_estadia_xdia DOUBLE PRECISION CHECK (costo_estadia_xdia >= 0)
);

COMMENT ON TABLE gestion.depositos IS 'Depósitos intermedios para almacenamiento temporal';


-- Tabla: CONTENEDORES
CREATE TABLE IF NOT EXISTS gestion.contenedores (
    id BIGSERIAL PRIMARY KEY,
    codigo_identificacion VARCHAR(100) UNIQUE NOT NULL,
    peso DOUBLE PRECISION NOT NULL CHECK (peso > 0),
    volumen DOUBLE PRECISION NOT NULL CHECK (volumen > 0),
    id_cliente BIGINT NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES gestion.clientes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contenedor_cliente ON gestion.contenedores(id_cliente);
CREATE INDEX IF NOT EXISTS idx_contenedor_codigo ON gestion.contenedores(codigo_identificacion);


-- Tabla: TARIFAS
CREATE TABLE IF NOT EXISTS gestion.tarifas (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    rango_peso_min DOUBLE PRECISION,
    rango_peso_max DOUBLE PRECISION,
    rango_volumen_min DOUBLE PRECISION,
    rango_volumen_max DOUBLE PRECISION,
    valor DOUBLE PRECISION NOT NULL CHECK (valor >= 0)
);

CREATE INDEX IF NOT EXISTS idx_tarifa_peso ON gestion.tarifas(rango_peso_min, rango_peso_max);
CREATE INDEX IF NOT EXISTS idx_tarifa_volumen ON gestion.tarifas(rango_volumen_min, rango_volumen_max);


-- ============================================================
-- PASO 3: SCHEMA FLOTA - Tablas según diagrama ER
-- ============================================================

-- Tabla: CAMIONES
CREATE TABLE IF NOT EXISTS flota.camiones (
    patente VARCHAR(20) PRIMARY KEY,
    nombre_transportista VARCHAR(255) NOT NULL,
    telefono_transportista VARCHAR(50),
    capacidad_peso DOUBLE PRECISION NOT NULL CHECK (capacidad_peso > 0),
    capacidad_volumen DOUBLE PRECISION NOT NULL CHECK (capacidad_volumen > 0),
    consumo_combustible_km DOUBLE PRECISION NOT NULL CHECK (consumo_combustible_km > 0),
    costo_km DOUBLE PRECISION NOT NULL CHECK (costo_km > 0),
    disponible BOOLEAN DEFAULT true,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_camion_disponible ON flota.camiones(disponible);
CREATE INDEX IF NOT EXISTS idx_camion_transportista ON flota.camiones(nombre_transportista);


-- ============================================================
-- PASO 4: SCHEMA LOGISTICA - Tablas según diagrama ER
-- ============================================================

-- Tabla: SOLICITUDES
CREATE TABLE IF NOT EXISTS logistica.solicitudes (
    id BIGSERIAL PRIMARY KEY,
    numero_seguimiento VARCHAR(50) UNIQUE NOT NULL,
    id_contenedor BIGINT NOT NULL,
    id_cliente BIGINT NOT NULL,
    origen_direccion TEXT NOT NULL,
    origen_latitud DOUBLE PRECISION,
    origen_longitud DOUBLE PRECISION,
    destino_direccion TEXT NOT NULL,
    destino_latitud DOUBLE PRECISION,
    destino_longitud DOUBLE PRECISION,
    estado VARCHAR(50) NOT NULL DEFAULT 'BORRADOR',
    costo_estimado DOUBLE PRECISION,
    tiempo_estimado DOUBLE PRECISION,
    costo_final DOUBLE PRECISION,
    tiempo_real DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_solicitud_numero ON logistica.solicitudes(numero_seguimiento);
CREATE INDEX IF NOT EXISTS idx_solicitud_cliente ON logistica.solicitudes(id_cliente);
CREATE INDEX IF NOT EXISTS idx_solicitud_contenedor ON logistica.solicitudes(id_contenedor);
CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON logistica.solicitudes(estado);

COMMENT ON TABLE logistica.solicitudes IS 'Solicitudes de transporte';
COMMENT ON COLUMN logistica.solicitudes.estado IS 'Estados: BORRADOR, PROGRAMADA, ENTREGADA, CANCELADA';


-- Tabla: RUTAS
CREATE TABLE IF NOT EXISTS logistica.rutas (
    id BIGSERIAL PRIMARY KEY,
    id_solicitud BIGINT UNIQUE NOT NULL,
    FOREIGN KEY (id_solicitud) REFERENCES logistica.solicitudes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ruta_solicitud ON logistica.rutas(id_solicitud);


-- Tabla: TRAMOS
CREATE TABLE IF NOT EXISTS logistica.tramos (
    id BIGSERIAL PRIMARY KEY,
    id_ruta BIGINT NOT NULL,
    patente_camion VARCHAR(20),
    origen_descripcion TEXT,
    destino_descripcion TEXT,
    distancia_km DOUBLE PRECISION,
    estado VARCHAR(50) NOT NULL DEFAULT 'ESTIMADO',
    fecha_inicio_estimada TIMESTAMP,
    fecha_fin_estimada TIMESTAMP,
    fecha_inicio_real TIMESTAMP,
    fecha_fin_real TIMESTAMP,
    costo_estimado DOUBLE PRECISION,
    costo_real DOUBLE PRECISION,
    km_reales DOUBLE PRECISION,
    FOREIGN KEY (id_ruta) REFERENCES logistica.rutas(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tramo_ruta ON logistica.tramos(id_ruta);
CREATE INDEX IF NOT EXISTS idx_tramo_camion ON logistica.tramos(patente_camion);
CREATE INDEX IF NOT EXISTS idx_tramo_estado ON logistica.tramos(estado);

COMMENT ON COLUMN logistica.tramos.estado IS 'Estados: ESTIMADO, ASIGNADO, INICIADO, FINALIZADO';



-- Tabla: CONFIGURACION
CREATE TABLE IF NOT EXISTS logistica.configuracion (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) UNIQUE NOT NULL,
    valor TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_config_clave ON logistica.configuracion(clave);


-- ============================================================
-- PASO 5: INSERTAR DATOS DE PRUEBA
-- ============================================================

-- ==================== SCHEMA GESTION ====================

-- Clientes (50 registros para pruebas exhaustivas)
-- IMPORTANTE: Resetear la secuencia para que comience desde 1
ALTER SEQUENCE gestion.clientes_id_seq RESTART WITH 1;
INSERT INTO gestion.clientes (nombre, apellido, email, telefono, cuil) VALUES
('Juan Carlos', 'Rodríguez', 'jrodriguez@logisticadelsur.com', '+54 351 400-1000', '20-12345678-9'),
('María Elena', 'Martínez', 'mmartinez@transportesunidos.com', '+54 351 400-2000', '27-23456789-0'),
('Roberto', 'Gómez', 'rgomez@elprogreso.com', '+54 351 400-3000', '20-34567890-1'),
('Ana Paula', 'Fernández', 'afernandez@districentral.com', '+54 351 400-4000', '27-45678901-2'),
('Diego', 'López', 'dlopez@exportargentina.com', '+54 351 400-5000', '20-56789012-3'),
('Patricia', 'Sánchez', 'psanchez@delnorte.com', '+54 351 400-6000', '27-67890123-4'),
('Gabriel', 'Torres', 'gtorres@metalcor.com', '+54 351 400-7000', '20-78901234-5'),
('Laura', 'Ruiz', 'lruiz@alimentosfrescos.com', '+54 351 400-8000', '27-89012345-6'),
('Fernando', 'Castro', 'fcastro@comercialcentro.com', '+54 351 400-9000', '20-90123456-7'),
('Silvia', 'Morales', 'smorales@distribuidoraeste.com', '+54 351 400-1100', '27-01234567-8'),
('Lucas', 'Romero', 'lromero@importexport.com', '+54 351 400-1200', '20-11234567-9'),
('Marina', 'Díaz', 'mdiaz@logisticacba.com', '+54 351 400-1300', '27-22345678-0'),
('Sebastián', 'Álvarez', 'salvarez@transportescba.com', '+54 351 400-1400', '20-33456789-1'),
('Valentina', 'Herrera', 'vherrera@cargasrapidas.com', '+54 351 400-1500', '27-44567890-2'),
('Maximiliano', 'Benítez', 'mbenitez@distribuidoranacional.com', '+54 351 400-1600', '20-55678901-3'),
('Carla', 'Pereyra', 'cpereyra@logisticasur.com', '+54 351 400-1700', '27-66789012-4'),
('Martín', 'Silva', 'msilva@transportenorte.com', '+54 351 400-1800', '20-77890123-5'),
('Natalia', 'Gutiérrez', 'ngutierrez@cargacentral.com', '+54 351 400-1900', '27-88901234-6'),
('Pablo', 'Vega', 'pvega@distriprov.com', '+54 351 400-2100', '20-99012345-7'),
('Florencia', 'Medina', 'fmedina@logexpress.com', '+54 351 400-2200', '27-00123456-8')
ON CONFLICT (email) DO NOTHING;

-- Depósitos (10 registros estratégicos)
ALTER SEQUENCE gestion.depositos_id_seq RESTART WITH 1;
INSERT INTO gestion.depositos (nombre, direccion, latitud, longitud, costo_estadia_xdia) VALUES
('Depósito Central Córdoba', 'Av. Circunvalación Km 5, Córdoba', -31.4201, -64.1888, 150.00),
('Depósito Zona Norte', 'Ruta 9 Km 680, Córdoba', -31.3500, -64.1500, 120.00),
('Depósito Zona Sur', 'Camino a Alta Gracia Km 12, Córdoba', -31.5000, -64.2000, 130.00),
('Depósito Zona Este', 'Ruta E-55 Km 8, Córdoba', -31.4000, -64.1000, 125.00),
('Depósito Zona Oeste', 'Av. de Circunvalación Km 10, Córdoba', -31.4500, -64.2500, 140.00),
('Depósito Aeropuerto', 'Zona Aeropuerto Internacional, Córdoba', -31.3236, -64.2080, 200.00),
('Depósito Industrial Norte', 'Parque Industrial Ferreyra, Córdoba', -31.3800, -64.1200, 110.00),
('Depósito Rural Este', 'Zona Rural Km 15, Córdoba', -31.4300, -64.0800, 100.00),
('Depósito Puerto', 'Zona Portuaria, Dock Sud', -34.6500, -58.3500, 180.00),
('Depósito Rosario', 'Parque Industrial Rosario, Santa Fe', -32.9500, -60.6400, 135.00)
ON CONFLICT DO NOTHING;

-- Contenedores (200 registros distribuidos entre clientes)
ALTER SEQUENCE gestion.contenedores_id_seq RESTART WITH 1;
DO $$
DECLARE
    i INT;
    cliente_id INT;
    tipos TEXT[] := ARRAY['CONT-20', 'CONT-40', 'REEF-20', 'REEF-40', 'TANK-20', 'TANK-40', 'OPEN-20', 'OPEN-40', 'FLAT-20', 'FLAT-40'];
    tipo TEXT;
    peso DOUBLE PRECISION;
    volumen DOUBLE PRECISION;
BEGIN
    FOR i IN 1..200 LOOP
        cliente_id := ((i - 1) % 20) + 1;
        tipo := tipos[((i - 1) % 10) + 1];
        
        -- Calcular peso y volumen según tipo
        IF tipo LIKE 'CONT-20%' OR tipo LIKE 'OPEN-20%' OR tipo LIKE 'FLAT-20%' THEN
            peso := 2200.0 + (random() * 300);
            volumen := 28.0 + (random() * 5);
        ELSIF tipo LIKE 'CONT-40%' OR tipo LIKE 'OPEN-40%' OR tipo LIKE 'FLAT-40%' THEN
            peso := 3700.0 + (random() * 600);
            volumen := 60.0 + (random() * 10);
        ELSIF tipo LIKE 'REEF-20%' THEN
            peso := 2900.0 + (random() * 300);
            volumen := 26.0 + (random() * 3);
        ELSIF tipo LIKE 'REEF-40%' THEN
            peso := 4500.0 + (random() * 500);
            volumen := 55.0 + (random() * 8);
        ELSIF tipo LIKE 'TANK-20%' THEN
            peso := 3300.0 + (random() * 400);
            volumen := 24.0 + (random() * 4);
        ELSE -- TANK-40
            peso := 5000.0 + (random() * 600);
            volumen := 48.0 + (random() * 8);
        END IF;
        
        INSERT INTO gestion.contenedores (codigo_identificacion, peso, volumen, id_cliente)
        VALUES (
            tipo || '-' || LPAD(i::TEXT, 5, '0'),
            ROUND(peso::numeric, 2),
            ROUND(volumen::numeric, 2),
            cliente_id
        )
        ON CONFLICT (codigo_identificacion) DO NOTHING;
    END LOOP;
END $$;

-- Tarifas (15 registros con rangos variados)
ALTER SEQUENCE gestion.tarifas_id_seq RESTART WITH 1;
INSERT INTO gestion.tarifas (descripcion, rango_peso_min, rango_peso_max, rango_volumen_min, rango_volumen_max, valor) VALUES
('Tarifa Contenedor Pequeño - Corta Distancia', 0, 3000, 0, 35, 3000.00),
('Tarifa Contenedor Pequeño - Media Distancia', 0, 3000, 0, 35, 4500.00),
('Tarifa Contenedor Pequeño - Larga Distancia', 0, 3000, 0, 35, 7000.00),
('Tarifa Contenedor Mediano - Corta Distancia', 3001, 4500, 35, 70, 4000.00),
('Tarifa Contenedor Mediano - Media Distancia', 3001, 4500, 35, 70, 6000.00),
('Tarifa Contenedor Mediano - Larga Distancia', 3001, 4500, 35, 70, 9500.00),
('Tarifa Contenedor Grande - Corta Distancia', 4501, 10000, 70, 150, 5500.00),
('Tarifa Contenedor Grande - Media Distancia', 4501, 10000, 70, 150, 8000.00),
('Tarifa Contenedor Grande - Larga Distancia', 4501, 10000, 70, 150, 12000.00),
('Tarifa Carga Pesada - Corta Distancia', 10001, 20000, 150, 300, 8000.00),
('Tarifa Carga Pesada - Media Distancia', 10001, 20000, 150, 300, 12000.00),
('Tarifa Carga Pesada - Larga Distancia', 10001, 20000, 150, 300, 18000.00),
('Tarifa Volumen Alto - Peso Bajo', 0, 2000, 80, 200, 6500.00),
('Tarifa Volumen Muy Alto - Peso Bajo', 0, 2000, 200, 500, 10000.00),
('Tarifa Express - Cualquier Tamaño', 0, 50000, 0, 500, 15000.00)
ON CONFLICT DO NOTHING;


-- ==================== SCHEMA FLOTA ====================

-- Camiones (30 registros con capacidades variadas)
INSERT INTO flota.camiones (patente, nombre_transportista, telefono_transportista, capacidad_peso, capacidad_volumen, consumo_combustible_km, costo_km, disponible) VALUES
-- Camiones pequeños (3-5 toneladas)
('ABC123', 'Carlos Rodríguez', '+54 351 111-2222', 3500.0, 25.0, 0.25, 100.0, true),
('DEF456', 'Laura Martínez', '+54 351 333-4444', 4000.0, 28.0, 0.28, 105.0, true),
('GHI789', 'Roberto Sánchez', '+54 351 555-6666', 4500.0, 30.0, 0.30, 110.0, true),
-- Camiones medianos (5-8 toneladas)
('JKL012', 'Ana García', '+54 351 777-8888', 6000.0, 40.0, 0.35, 120.0, true),
('MNO345', 'Miguel Torres', '+54 351 999-0000', 6500.0, 42.0, 0.38, 125.0, true),
('PQR678', 'Patricia López', '+54 351 111-3333', 7000.0, 45.0, 0.40, 130.0, true),
('STU901', 'Diego Fernández', '+54 351 222-4444', 7500.0, 48.0, 0.42, 135.0, true),
('VWX234', 'Gabriela Ruiz', '+54 351 333-5555', 8000.0, 50.0, 0.45, 140.0, true),
-- Camiones grandes (8-12 toneladas)
('YZA567', 'Lucas Romero', '+54 351 444-6666', 9000.0, 60.0, 0.50, 150.0, true),
('BCD890', 'Marina Díaz', '+54 351 555-7777', 9500.0, 62.0, 0.52, 155.0, true),
('EFG123', 'Fernando Castro', '+54 351 666-8888', 10000.0, 65.0, 0.55, 160.0, true),
('HIJ456', 'Silvia Morales', '+54 351 777-9999', 10500.0, 68.0, 0.58, 165.0, true),
('KLM789', 'Ricardo Álvarez', '+54 351 888-1111', 11000.0, 70.0, 0.60, 170.0, true),
('NOP012', 'Mónica Herrera', '+54 351 888-2222', 11500.0, 72.0, 0.62, 175.0, true),
('QRS345', 'Jorge Benítez', '+54 351 888-3333', 12000.0, 75.0, 0.65, 180.0, true),
-- Camiones muy grandes (12-15 toneladas)
('TUV678', 'Carla Pereyra', '+54 351 888-4444', 13000.0, 80.0, 0.70, 190.0, true),
('WXY901', 'Martín Silva', '+54 351 888-5555', 13500.0, 82.0, 0.72, 195.0, true),
('ZAB234', 'Natalia Gutiérrez', '+54 351 888-6666', 14000.0, 85.0, 0.75, 200.0, true),
('CDE567', 'Pablo Vega', '+54 351 888-7777', 14500.0, 88.0, 0.78, 205.0, true),
('FGH890', 'Florencia Medina', '+54 351 888-8888', 15000.0, 90.0, 0.80, 210.0, true),
-- Camiones premium (15-20 toneladas)
('IJK123', 'Gustavo Romero', '+54 351 888-9999', 16000.0, 95.0, 0.85, 220.0, true),
('LMN456', 'Daniela Torres', '+54 351 999-1111', 17000.0, 100.0, 0.88, 230.0, true),
('OPQ789', 'Hernán López', '+54 351 999-2222', 18000.0, 105.0, 0.90, 240.0, true),
('RST012', 'Valeria Castro', '+54 351 999-3333', 19000.0, 110.0, 0.92, 250.0, true),
('UVW345', 'Federico Díaz', '+54 351 999-4444', 20000.0, 115.0, 0.95, 260.0, true),
-- Camiones en uso (no disponibles)
('XYZ678', 'Alejandro Ruiz', '+54 351 999-5555', 8500.0, 55.0, 0.48, 145.0, false),
('AAA901', 'Carolina Sánchez', '+54 351 999-6666', 9500.0, 58.0, 0.52, 158.0, false),
('BBB234', 'Rodrigo Fernández', '+54 351 999-7777', 11000.0, 68.0, 0.60, 172.0, false),
('CCC567', 'Luciana Martínez', '+54 351 999-8888', 13500.0, 82.0, 0.72, 195.0, false),
('DDD890', 'Andrés Gómez', '+54 351 999-9999', 15500.0, 92.0, 0.82, 215.0, false)
ON CONFLICT (patente) DO NOTHING;


-- ==================== SCHEMA LOGISTICA ====================

-- Configuración (Google Maps API y otros parámetros)
INSERT INTO logistica.configuracion (clave, valor) VALUES
('google.maps.api.key', 'AIzaSyYourKeyHere'),
('velocidad_promedio_camion', '60'),
('tiempo_carga_descarga_min', '30'),
('margen_seguridad_tiempo', '15'),
('radio_busqueda_deposito', '100'),
('costo_administrativo', '500'),
('iva_porcentaje', '21'),
('email_notificaciones', 'logistica@gestioncontenedores.com'),
('habilitar_notificaciones', 'true'),
('max_distancia_tramo', '300')
ON CONFLICT (clave) DO NOTHING;

-- Solicitudes de prueba (10 registros en diferentes estados)
ALTER SEQUENCE logistica.solicitudes_id_seq RESTART WITH 1;
INSERT INTO logistica.solicitudes (numero_seguimiento, id_contenedor, id_cliente, origen_direccion, origen_latitud, origen_longitud, destino_direccion, destino_latitud, destino_longitud, estado, costo_estimado, tiempo_estimado, costo_final, tiempo_real) VALUES
-- Estado BORRADOR (pendientes de estimar - SIN costos ni tiempos)
('TRACK-2025-001', 1, 1, 'Puerto de Buenos Aires, Buenos Aires, Argentina', -34.6037, -58.3816, 'Rosario, Santa Fe, Argentina', -32.9468, -60.6393, 'BORRADOR', NULL, NULL, NULL, NULL),
('TRACK-2025-002', 5, 2, 'Córdoba Capital, Córdoba, Argentina', -31.4201, -64.1888, 'Mendoza, Mendoza, Argentina', -32.8895, -68.8458, 'BORRADOR', NULL, NULL, NULL, NULL),
('TRACK-2025-003', 10, 3, 'Rosario, Santa Fe, Argentina', -32.9468, -60.6393, 'Mar del Plata, Buenos Aires, Argentina', -38.0055, -57.5426, 'BORRADOR', NULL, NULL, NULL, NULL),
('TRACK-2025-004', 15, 4, 'Salta Capital, Salta, Argentina', -24.7859, -65.4117, 'Tucumán, Tucumán, Argentina', -26.8083, -65.2176, 'BORRADOR', NULL, NULL, NULL, NULL),
('TRACK-2025-005', 20, 5, 'La Plata, Buenos Aires, Argentina', -34.9215, -57.9545, 'Bahía Blanca, Buenos Aires, Argentina', -38.7183, -62.2663, 'BORRADOR', NULL, NULL, NULL, NULL),
-- Estado PROGRAMADA (con estimación - SIN costos ni tiempos reales)
('TRACK-2025-006', 25, 6, 'Neuquén Capital, Neuquén, Argentina', -38.9516, -68.0591, 'Bariloche, Río Negro, Argentina', -41.1335, -71.3103, 'PROGRAMADA', 15000.00, 8.5, NULL, NULL),
('TRACK-2025-007', 30, 7, 'Paraná, Entre Ríos, Argentina', -31.7333, -60.5293, 'Santa Fe, Santa Fe, Argentina', -31.6107, -60.6973, 'PROGRAMADA', 8000.00, 3.2, NULL, NULL),
('TRACK-2025-008', 35, 8, 'Corrientes, Corrientes, Argentina', -27.4692, -58.8306, 'Resistencia, Chaco, Argentina', -27.4514, -58.9867, 'PROGRAMADA', 7500.00, 2.8, NULL, NULL),
-- Estado ENTREGADA (completadas - CON costos y tiempos reales)
('TRACK-2025-009', 40, 9, 'San Juan, San Juan, Argentina', -31.5375, -68.5364, 'San Luis, San Luis, Argentina', -33.3017, -66.3378, 'ENTREGADA', 12000.00, 6.5, 12350.00, 6.8),
('TRACK-2025-010', 45, 10, 'Jujuy, Jujuy, Argentina', -24.1858, -65.2995, 'Salta, Salta, Argentina', -24.7859, -65.4117, 'ENTREGADA', 6800.00, 2.5, 6950.00, 2.7)
ON CONFLICT (numero_seguimiento) DO NOTHING;


-- ============================================================
-- PASO 6: VERIFICACIÓN
-- ============================================================

-- Query de verificación (comentado para no interferir con la app)
-- SELECT 
--     'Clientes' as entidad, COUNT(*) as total FROM gestion.clientes
-- UNION ALL
-- SELECT 'Depósitos', COUNT(*) FROM gestion.depositos
-- UNION ALL
-- SELECT 'Contenedores', COUNT(*) FROM gestion.contenedores
-- UNION ALL
-- SELECT 'Tarifas', COUNT(*) FROM gestion.tarifas
-- UNION ALL
-- SELECT 'Camiones', COUNT(*) FROM flota.camiones
-- UNION ALL
-- SELECT 'Solicitudes', COUNT(*) FROM logistica.solicitudes
-- ORDER BY entidad;

-- ============================================================
-- SCRIPT COMPLETADO EXITOSAMENTE
-- ============================================================
-- 
-- Total de datos insertados: 275+ registros
-- - 20 clientes
-- - 10 depósitos
-- - 200 contenedores
-- - 15 tarifas
-- - 30 camiones
-- - 10 solicitudes de prueba
-- - 10 configuraciones
--
-- TOTAL: 295 registros
-- ============================================================
