package com.tpi.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configuración del JWT Decoder sin validación de issuer.
 * Solo valida la firma del token usando las claves públicas de Keycloak.
 */
@Configuration
public class JwtDecoderConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri) {
        // Crear decoder que SOLO valida la firma con las claves públicas de Keycloak
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Deshabilitar TODAS las validaciones por defecto (incluyendo issuer, exp, nbf)
        // Solo queremos validar la firma
        jwtDecoder.setJwtValidator(jwt -> 
            org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
        );
        
        return jwtDecoder;
    }
}
