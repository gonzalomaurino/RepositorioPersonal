package com.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http

            .csrf(csrf -> csrf.disable())
            


            .authorizeExchange(exchanges -> exchanges
                // Endpoints públicos
                .pathMatchers("/auth/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Endpoints específicos por rol - TRANSPORTISTA
                .pathMatchers(HttpMethod.PATCH, "/api/flota/camiones/*/disponibilidad").hasAnyRole("TRANSPORTISTA", "OPERADOR")
                .pathMatchers(HttpMethod.PATCH, "/api/logistica/tramos/*/iniciar").hasAnyRole("TRANSPORTISTA", "OPERADOR")
                .pathMatchers(HttpMethod.PATCH, "/api/logistica/tramos/*/finalizar").hasAnyRole("TRANSPORTISTA", "OPERADOR")
                .pathMatchers(HttpMethod.GET, "/api/logistica/tramos/camion/*").hasAnyRole("TRANSPORTISTA", "OPERADOR")
                
                // Endpoints específicos por rol - OPERADOR (escritura) - deben ir antes de las reglas generales
                .pathMatchers(HttpMethod.POST, "/api/gestion/**").hasAnyRole("OPERADOR", "TRANSPORTISTA", "CLIENTE")
                .pathMatchers(HttpMethod.PUT, "/api/gestion/**").hasAnyRole("OPERADOR", "TRANSPORTISTA", "CLIENTE")
                .pathMatchers(HttpMethod.DELETE, "/api/gestion/**").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.POST, "/api/flota/**").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                .pathMatchers(HttpMethod.PUT, "/api/flota/**").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                .pathMatchers(HttpMethod.DELETE, "/api/flota/**").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.POST, "/api/logistica/solicitudes/estimar-ruta").hasAnyRole("OPERADOR", "CLIENTE")
                .pathMatchers(HttpMethod.POST, "/api/logistica/solicitudes/*/asignar-ruta").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.GET, "/api/logistica/solicitudes/pendientes").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.POST, "/api/logistica/solicitudes").hasAnyRole("OPERADOR", "CLIENTE")
                .pathMatchers(HttpMethod.POST, "/api/logistica/solicitudes/completa").hasAnyRole("OPERADOR", "CLIENTE")
                .pathMatchers(HttpMethod.PUT, "/api/logistica/solicitudes/**").hasAnyRole("OPERADOR", "CLIENTE")
                .pathMatchers(HttpMethod.DELETE, "/api/logistica/solicitudes/**").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.POST, "/api/logistica/tramos").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.PUT, "/api/logistica/tramos/**").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.DELETE, "/api/logistica/tramos/**").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.POST, "/api/logistica/rutas").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.PUT, "/api/logistica/rutas/**").hasRole("OPERADOR")
                .pathMatchers(HttpMethod.DELETE, "/api/logistica/rutas/**").hasRole("OPERADOR")
                
                // GET endpoints - cualquier rol autenticado puede leer
                .pathMatchers(HttpMethod.GET, "/api/**").authenticated()
                
                // Cualquier otro endpoint requiere autenticación
                .anyExchange().authenticated()
            )
            

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            
            if (realmAccess == null) {
                return List.of();
            }
            
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            
            if (roles == null || roles.isEmpty()) {
                return List.of();
            }
            

            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
        });
        
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
