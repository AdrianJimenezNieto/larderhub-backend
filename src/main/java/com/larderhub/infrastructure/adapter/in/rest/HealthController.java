package com.larderhub.infrastructure.adapter.in.rest;

import com.larderhub.domain.model.SystemHealth;
import com.larderhub.domain.ports.in.GetSystemHealthUseCase;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * INFRASTRUCTURE ADAPTER (Input - REST): HealthController
 * 
 * Este es el punto de entrada desde el exterior (Internet).
 * 
 * Responsabilidades:
 * 1. Escuchar peticiones HTTP en /api/health.
 * 2. Transformar la petición (si la hubiera) a algo que entienda el dominio.
 * 3. Llamar al Caso de Uso (Puerto de Entrada).
 * 4. Transformar la respuesta del Dominio a JSON (DTO).
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

  private final GetSystemHealthUseCase getSystemHealthUseCase;

  @GetMapping
  public ResponseEntity<HealthResponse> checkHealth() {
    // 1. Llamamos al dominio
    SystemHealth health = getSystemHealthUseCase.execute();

    // 2. Adaptamos la respuesta (Domain -> DTO)
    // El usuario pidió JSON {'status': 'OK', 'db':'Connected'}
    // Mapeamos 'dbStatus' del dominio al campo 'db' del JSON.
    HealthResponse response = new HealthResponse(
        health.getStatus(),
        health.getDbStatus());

    return ResponseEntity.ok(response);
  }

  // DTO (Data Transfer Object)
  // Usamos un 'record' de Java (disponible desde Java 14+) que es ideal para DTOs
  // inmutables.
  // Esto se serializará automáticamente a JSON.
  private record HealthResponse(String status, String db) {
  }
}
