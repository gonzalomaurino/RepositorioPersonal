package com.tpi.gateway.controller;

import com.tpi.gateway.dto.LoginRequest;
import com.tpi.gateway.dto.RefreshTokenRequest;
import com.tpi.gateway.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * AuthController - Simplifica la obtención de tokens de Keycloak
 * 
 * Este controlador actúa como un "proxy" para facilitar la autenticación
 * con Keycloak sin necesidad de hacer peticiones complejas manualmente.
 * 
 * Endpoints disponibles:
 * - POST /auth/login: obtener token con username/password
 * - POST /auth/refresh: renovar token con refresh_token
 * - GET /auth/info: información del servicio
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Value("${keycloak.auth.token-uri}")
    private String tokenUri;

    @Value("${keycloak.auth.client-id}")
    private String clientId;

    @Value("${keycloak.auth.client-secret:}")
    private String clientSecret;

    private final WebClient webClient;

    public AuthController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * POST /auth/login
     * 
     * Obtiene un access_token y refresh_token de Keycloak usando credenciales.
     * 
     * Body JSON:
     * {
     *   "username": "cliente@tpi.com",
     *   "password": "cliente123"
     * }
     * 
     * Response:
     * {
     *   "access_token": "eyJhbGc...",
     *   "refresh_token": "eyJhbGc...",
     *   "expires_in": 300,
     *   "refresh_expires_in": 1800,
     *   "token_type": "Bearer"
     * }
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody LoginRequest loginRequest) {
        log.info("🔐 Solicitud de login para usuario: {}", loginRequest.getUsername());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("username", loginRequest.getUsername());
        formData.add("password", loginRequest.getPassword());
        
        if (clientSecret != null && !clientSecret.isBlank()) {
            formData.add("client_secret", clientSecret);
        }

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(
                    status -> status.value() == 401,
                    response -> {
                        log.error("❌ Credenciales inválidas para usuario: {}", loginRequest.getUsername());
                        return Mono.error(new RuntimeException("Credenciales inválidas"));
                    }
                )
                .bodyToMono(TokenResponse.class)
                .map(tokenResponse -> {
                    log.info("✅ Token obtenido exitosamente para: {}", loginRequest.getUsername());
                    log.debug("   Expira en: {} segundos", tokenResponse.getExpiresIn());
                    return ResponseEntity.ok(tokenResponse);
                })
                .onErrorResume(error -> {
                    log.error("❌ Error al obtener token: {}", error.getMessage());
                    return Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(null)
                    );
                });
    }

    /**
     * POST /auth/refresh
     * 
     * Renueva un access_token usando el refresh_token.
     * 
     * Body JSON:
     * {
     *   "refreshToken": "eyJhbGc..."
     * }
     * 
     * Response:
     * {
     *   "access_token": "eyJhbGc...",
     *   "refresh_token": "eyJhbGc...",
     *   "expires_in": 300,
     *   "refresh_expires_in": 1800,
     *   "token_type": "Bearer"
     * }
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refresh(@RequestBody RefreshTokenRequest refreshRequest) {
        log.info("🔄 Solicitud de refresh token");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("refresh_token", refreshRequest.getRefreshToken());
        
        if (clientSecret != null && !clientSecret.isBlank()) {
            formData.add("client_secret", clientSecret);
        }

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(
                    status -> status.value() == 400 || status.value() == 401,
                    response -> {
                        log.error("❌ Refresh token inválido o expirado");
                        return Mono.error(new RuntimeException("Refresh token inválido"));
                    }
                )
                .bodyToMono(TokenResponse.class)
                .map(tokenResponse -> {
                    log.info("✅ Token renovado exitosamente");
                    log.debug("   Expira en: {} segundos", tokenResponse.getExpiresIn());
                    return ResponseEntity.ok(tokenResponse);
                })
                .onErrorResume(error -> {
                    log.error("❌ Error al renovar token: {}", error.getMessage());
                    return Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(null)
                    );
                });
    }

    /**
     * GET /auth/info
     * 
     * Retorna información sobre el servicio de autenticación.
     * Útil para verificar que el endpoint está activo.
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "service", "Auth Token Generator",
            "version", "1.0.0",
            "keycloak_token_uri", tokenUri,
            "client_id", clientId,
            "endpoints", Map.of(
                "login", "POST /auth/login",
                "refresh", "POST /auth/refresh",
                "info", "GET /auth/info"
            ),
            "description", "Simplifica la obtención de tokens JWT desde Keycloak para testing y desarrollo"
        )));
    }
}
