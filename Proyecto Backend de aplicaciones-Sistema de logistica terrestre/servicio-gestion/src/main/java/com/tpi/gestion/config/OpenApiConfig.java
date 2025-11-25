package com.tpi.gestion.config;

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
 * Configuración de OpenAPI/Swagger para el Servicio de Gestión.
 * 
 * Proporciona documentación interactiva de la API REST en:
 * - Swagger UI: http://localhost:8081/api/gestion/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8081/api/gestion/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gestionOpenAPI() {
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
                .title("API - Servicio de Gestión")
                .description("""
                    Microservicio de Gestión de Contenedores.
                    
                    **Responsabilidades:**
                    - Gestión de Clientes (CRUD)
                    - Gestión de Contenedores (CRUD)
                    - Gestión de Depósitos (CRUD)
                    - Gestión de Tarifas (CRUD)
                    
                    **Autenticación:**
                    Requiere JWT token de Keycloak. Obtén el token desde:
                    POST http://localhost:9090/realms/tpi-backend/protocol/openid-connect/token
                    
                    **Roles disponibles:**
                    - CLIENTE: Puede consultar sus propios contenedores
                    - OPERADOR: Acceso completo a todas las operaciones
                    
                    **Puerto:** 8081
                    **Context Path:** /api/gestion
                    **Base de Datos:** PostgreSQL (Schema: gestion)
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipo de Desarrollo TPI")
                    .email("desarrollo@tpi.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8081/api/gestion")
                    .description("Servidor Local - Desarrollo"),
                new Server()
                    .url("http://localhost:8080/api/gestion")
                    .description("A través del API Gateway")
            ));
    }
}
