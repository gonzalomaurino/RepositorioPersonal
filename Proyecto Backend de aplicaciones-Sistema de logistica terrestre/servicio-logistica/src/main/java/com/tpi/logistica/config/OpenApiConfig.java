package com.tpi.logistica.config;

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
 * Configuración de OpenAPI/Swagger para el Servicio de Logística.
 * 
 * Proporciona documentación interactiva de la API REST en:
 * - Swagger UI: http://localhost:8083/api/logistica/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8083/api/logistica/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logisticaOpenAPI() {
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
                .title("API - Servicio de Logística")
                .description("""
                    Microservicio de Gestión de Logística y Transporte.
                    
                    **Responsabilidades:**
                    - Gestión de Solicitudes de Transporte (CRUD)
                    - Gestión de Rutas (CRUD)
                    - Gestión de Tramos (CRUD)
                    - Cálculo de tarifas y costos
                    - Integración con Google Maps API
                    - Seguimiento de estado de solicitudes
                    
                    **Autenticación:**
                    Requiere JWT token de Keycloak. Obtén el token desde:
                    POST http://localhost:9090/realms/tpi-backend/protocol/openid-connect/token
                    
                    **Roles disponibles:**
                    - CLIENTE: Puede crear solicitudes y consultar estado
                    - OPERADOR: Acceso completo a todas las operaciones
                    - TRANSPORTISTA: Puede iniciar y finalizar tramos
                    
                    **Puerto:** 8083
                    **Context Path:** /api/logistica
                    **Base de Datos:** PostgreSQL (Schema: logistica)
                    **Integraciones:** Google Maps Distance Matrix API
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipo de Desarrollo TPI")
                    .email("desarrollo@tpi.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8083/api/logistica")
                    .description("Servidor Local - Desarrollo"),
                new Server()
                    .url("http://localhost:8080/api/logistica")
                    .description("A través del API Gateway")
            ));
    }
}
