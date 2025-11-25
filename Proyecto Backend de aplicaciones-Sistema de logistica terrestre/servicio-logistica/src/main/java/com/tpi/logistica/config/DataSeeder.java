package com.tpi.logistica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.sql.Timestamp;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedLogisticaData(JdbcTemplate jdbc) {
        return args -> {
            try {
                log.info("DataSeeder: iniciando verificación e inserción de datos de prueba en esquema logistica...");
                // Verificar si existen registros
                Integer solicitudes = jdbc.queryForObject("select count(1) from logistica.solicitudes", Integer.class);
                Integer rutas = jdbc.queryForObject("select count(1) from logistica.rutas", Integer.class);
                Integer tramos = jdbc.queryForObject("select count(1) from logistica.tramos", Integer.class);
                log.info("DataSeeder: conteos actuales -> solicitudes={}, rutas={}, tramos={}", solicitudes, rutas, tramos);

                // Insertar Solicitud id=1 con número de seguimiento SEG-2024-001 si no existe
                Integer existeSolicitud1 = jdbc.queryForObject("select count(1) from logistica.solicitudes where id = 1", Integer.class);
                if (existeSolicitud1 == null || existeSolicitud1 == 0) {
                    // Verificar si existe solicitud con el número de seguimiento esperado
                    Integer existeSegNum = jdbc.queryForObject("select count(1) from logistica.solicitudes where numero_seguimiento = ?", Integer.class, "SEG-2024-001");
                    if (existeSegNum == null || existeSegNum == 0) {
                        log.info("Insertando solicitud de prueba id=1 con numero SEG-2024-001");
                        jdbc.update(
                            "insert into logistica.solicitudes (id, numero_seguimiento, id_contenedor, id_cliente, origen_direccion, origen_latitud, origen_longitud, destino_direccion, destino_latitud, destino_longitud, estado) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            1L, "SEG-2024-001", 1L, 1L, "Av. Colon 100", -31.4200, -64.1888, "Bv. San Juan 500", -31.4100, -64.1700, "PENDIENTE"
                        );
                    } else {
                        // Si existe el número de seguimiento pero no con ID 1, actualizar el ID
                        log.info("Actualizando solicitud existente SEG-2024-001 a id=1");
                        jdbc.update("update logistica.solicitudes set id = 1 where numero_seguimiento = ? and id != 1", "SEG-2024-001");
                    }
                }

                // Insertar Ruta id=1 para solicitud 1 si no existe
                Integer existeRuta1 = jdbc.queryForObject("select count(1) from logistica.rutas where id = 1", Integer.class);
                if (existeRuta1 == null || existeRuta1 == 0) {
                    // Verificar que la solicitud 1 existe
                    Integer solicitud1Existe = jdbc.queryForObject("select count(1) from logistica.solicitudes where id = 1", Integer.class);
                    if (solicitud1Existe != null && solicitud1Existe > 0) {
                        log.info("Insertando ruta de prueba id=1 para solicitud id=1");
                        try {
                            jdbc.update(
                                "insert into logistica.rutas (id, id_solicitud) values (?, ?)",
                                1L, 1L
                            );
                            log.info("Ruta id=1 insertada correctamente");
                        } catch (org.springframework.dao.DataIntegrityViolationException e) {
                            log.warn("Ruta id=1 ya existe o conflicto de integridad: {}", e.getMessage());
                        }
                    } else {
                        log.warn("Solicitud id=1 no existe, no se puede insertar ruta id=1");
                    }
                } else {
                    log.info("Ruta id=1 ya existe, no se inserta");
                }

                // Insertar Tramo id=1 para ruta 1 si no existe, en estado ESTIMADO
                Integer existeTramo1 = jdbc.queryForObject("select count(1) from logistica.tramos where id = 1", Integer.class);
                if (existeTramo1 == null || existeTramo1 == 0) {
                    // Verificar que la ruta 1 existe
                    Integer ruta1Existe = jdbc.queryForObject("select count(1) from logistica.rutas where id = 1", Integer.class);
                    if (ruta1Existe != null && ruta1Existe > 0) {
                        log.info("Insertando tramo de prueba id=1 para ruta id=1 en estado ESTIMADO");
                        try {
                            jdbc.update(
                                "insert into logistica.tramos (id, id_ruta, origen_descripcion, destino_descripcion, distancia_km, estado) values (?, ?, ?, ?, ?, ?)",
                                1L, 1L, "Origen", "Destino", 10.0, "ESTIMADO"
                            );
                            log.info("Tramo id=1 insertado correctamente");
                        } catch (org.springframework.dao.DataIntegrityViolationException e) {
                            log.warn("Tramo id=1 ya existe o conflicto de integridad: {}", e.getMessage());
                        }
                    } else {
                        log.warn("Ruta id=1 no existe, no se puede insertar tramo id=1");
                    }
                } else {
                    log.info("Tramo id=1 ya existe, no se inserta");
                }
            } catch (Exception e) {
                log.warn("DataSeeder: no se pudo ejecutar (posible ausencia de tablas aún): {}", e.getMessage());
            }
        };
    }
}


