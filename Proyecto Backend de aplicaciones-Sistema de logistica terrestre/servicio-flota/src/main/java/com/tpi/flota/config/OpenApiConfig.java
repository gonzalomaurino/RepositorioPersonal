package com.tpi.flota.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para el Servicio de Flota.
 * 
 * Proporciona documentación interactiva de la API REST en:
 * - Swagger UI: http://localhost:8082/api/flota/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8082/api/flota/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flotaOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Ingresa el JWT token obtenido de Keycloak")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .info(new Info()
                .title("API - Servicio de Flota")
                .description("""
                    Microservicio de Gestión de Flota de Camiones.
                    
                    **Responsabilidades:**
                    - Gestión de Camiones (CRUD)
                    - Control de disponibilidad de vehículos
                    - Gestión de capacidades (peso y volumen)
                    - Asignación de camiones a tramos
                    
                    **Autenticación:**
                    Requiere JWT token de Keycloak. Obtén el token desde:
                    POST http://localhost:9090/realms/tpi-backend/protocol/openid-connect/token
                    
                    **Roles disponibles:**
                    - OPERADOR: Acceso completo a todas las operaciones
                    - TRANSPORTISTA: Puede consultar información de su camión asignado
                    
                    **Puerto:** 8082
                    **Context Path:** /api/flota
                    **Base de Datos:** PostgreSQL (Schema: flota)
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipo de Desarrollo TPI")
                    .email("desarrollo@tpi.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8082/api/flota")
                    .description("Servidor Local - Desarrollo"),
                new Server()
                    .url("http://localhost:8080/api/flota")
                    .description("A través del API Gateway")
            ));
    }
}
