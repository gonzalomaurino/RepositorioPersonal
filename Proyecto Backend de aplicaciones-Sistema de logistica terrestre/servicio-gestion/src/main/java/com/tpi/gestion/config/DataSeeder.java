package com.tpi.gestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedGestionData(JdbcTemplate jdbc) {
        return args -> {
            try {
                log.info("DataSeeder: iniciando verificación e inserción de datos de prueba en esquema gestion...");
                
                // Verificar si existen registros
                Integer clientes = jdbc.queryForObject("select count(1) from gestion.clientes", Integer.class);
                Integer contenedores = jdbc.queryForObject("select count(1) from gestion.contenedores", Integer.class);
                Integer depositos = jdbc.queryForObject("select count(1) from gestion.depositos", Integer.class);
                Integer tarifas = jdbc.queryForObject("select count(1) from gestion.tarifas", Integer.class);
                log.info("DataSeeder: conteos actuales -> clientes={}, contenedores={}, depositos={}, tarifas={}", 
                    clientes, contenedores, depositos, tarifas);

                // Insertar Cliente id=1 si no existe
                Integer existeCliente1 = jdbc.queryForObject("select count(1) from gestion.clientes where id = 1", Integer.class);
                if (existeCliente1 == null || existeCliente1 == 0) {
                    log.info("Insertando cliente de prueba id=1");
                    try {
                        jdbc.update(
                            "insert into gestion.clientes (id, nombre, apellido, email, telefono, cuil) values (?, ?, ?, ?, ?, ?)",
                            1L, "Juan", "Perez", "juan.perez@test.com", "+54-11-1234-5678", "20-12345678-9"
                        );
                        log.info("Cliente id=1 insertado correctamente");
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Cliente id=1 ya existe o conflicto de integridad: {}", e.getMessage());
                    }
                } else {
                    log.info("Cliente id=1 ya existe, no se inserta");
                }

                // Insertar Contenedor id=1 con código CONT-001 si no existe
                Integer existeContenedor1 = jdbc.queryForObject("select count(1) from gestion.contenedores where id = 1", Integer.class);
                if (existeContenedor1 == null || existeContenedor1 == 0) {
                    // Asegurar que el cliente 1 existe
                    Integer cliente1Existe = jdbc.queryForObject("select count(1) from gestion.clientes where id = 1", Integer.class);
                    if (cliente1Existe != null && cliente1Existe > 0) {
                        log.info("Insertando contenedor de prueba id=1 (CONT-001)");
                        try {
                            // Primero verificar si existe por código
                            Integer existePorCodigo = jdbc.queryForObject("select count(1) from gestion.contenedores where codigo_identificacion = ?", Integer.class, "CONT-001");
                            if (existePorCodigo == null || existePorCodigo == 0) {
                                jdbc.update(
                                    "insert into gestion.contenedores (id, codigo_identificacion, peso, volumen, id_cliente) values (?, ?, ?, ?, ?)",
                                    1L, "CONT-001", 1500.0, 2.5, 1L
                                );
                                log.info("Contenedor id=1 insertado correctamente");
                            } else {
                                log.info("Contenedor CONT-001 ya existe, actualizando ID a 1");
                                jdbc.update("update gestion.contenedores set id = 1 where codigo_identificacion = ? and id != 1", "CONT-001");
                            }
                        } catch (org.springframework.dao.DataIntegrityViolationException e) {
                            log.warn("Contenedor id=1 ya existe o conflicto de integridad: {}", e.getMessage());
                        }
                    } else {
                        log.warn("Cliente id=1 no existe, no se puede insertar contenedor id=1");
                    }
                } else {
                    log.info("Contenedor id=1 ya existe, no se inserta");
                }

                // Insertar Depósito id=1 si no existe
                Integer existeDeposito1 = jdbc.queryForObject("select count(1) from gestion.depositos where id = 1", Integer.class);
                if (existeDeposito1 == null || existeDeposito1 == 0) {
                    log.info("Insertando deposito de prueba id=1");
                    try {
                        jdbc.update(
                            "insert into gestion.depositos (id, nombre, direccion, latitud, longitud, costo_estadia_xdia) values (?, ?, ?, ?, ?, ?)",
                            1L, "Deposito Central", "Av. Principal 123", -31.4200, -64.1888, 100.0
                        );
                        log.info("Deposito id=1 insertado correctamente");
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Deposito id=1 ya existe o conflicto de integridad: {}", e.getMessage());
                    }
                } else {
                    log.info("Deposito id=1 ya existe, no se inserta");
                }

                // Insertar Tarifa id=1 si no existe
                Integer existeTarifa1 = jdbc.queryForObject("select count(1) from gestion.tarifas where id = 1", Integer.class);
                if (existeTarifa1 == null || existeTarifa1 == 0) {
                    log.info("Insertando tarifa de prueba id=1");
                    try {
                        jdbc.update(
                            "insert into gestion.tarifas (id, descripcion, rango_peso_min, rango_peso_max, rango_volumen_min, rango_volumen_max, valor) values (?, ?, ?, ?, ?, ?, ?)",
                            1L, "Tarifa Estandar", 0.0, 5000.0, 0.0, 10.0, 5000.0
                        );
                        log.info("Tarifa id=1 insertada correctamente");
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Tarifa id=1 ya existe o conflicto de integridad: {}", e.getMessage());
                    }
                } else {
                    log.info("Tarifa id=1 ya existe, no se inserta");
                }
            } catch (Exception e) {
                log.warn("DataSeeder: no se pudo ejecutar (posible ausencia de tablas aún): {}", e.getMessage());
            }
        };
    }
}

