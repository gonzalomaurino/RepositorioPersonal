package com.tpi.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/gestion")
    public ResponseEntity<Map<String, Object>> gestionFallback() {
        return buildFallbackResponse("Servicio de Gestión");
    }

    @GetMapping("/flota")
    public ResponseEntity<Map<String, Object>> flotaFallback() {
        return buildFallbackResponse("Servicio de Flota");
    }

    @GetMapping("/logistica")
    public ResponseEntity<Map<String, Object>> logisticaFallback() {
        return buildFallbackResponse("Servicio de Logística");
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " no está disponible en este momento. Por favor, intente más tarde.");
        response.put("service", serviceName);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
