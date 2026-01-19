package com.larderhub.application.service;

import com.larderhub.domain.model.SystemHealth;
import com.larderhub.domain.ports.in.health.GetSystemHealthUseCase;
import com.larderhub.domain.ports.out.health.DbHealthOutputPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * APPLICATION SERVICE: SystemHealthService
 * 
 * ¿Qué es?
 * Es el cerebro que orquesta el caso de uso.
 * 
 * Implementa: El puerto de entrada (GetSystemHealthUseCase).
 * Usa: El puerto de salida (DbHealthOutputPort).
 * 
 * Aquí es donde la "magia" ocurre:
 * 1. Llama al puerto de salida para ver si la BBDD tira.
 * 2. Transforma ese booleano en lógica de negocio (Strings "Connected" /
 * "Down").
 * 3. Devuelve el modelo de dominio.
 */
@Service // Inyección de Spring: Le dice al framework "Oye, soy un Bean, instanciame".
@RequiredArgsConstructor // Lombok: Me crea el constructor con los campos final automáticamente.
public class SystemHealthService implements GetSystemHealthUseCase {

  private final DbHealthOutputPort dbHealthOutputPort;

  @Override
  public SystemHealth execute() {
    boolean isDbUp = dbHealthOutputPort.isDbUp();

    String dbStatus = isDbUp ? "Connected" : "Disconnected";
    String overallStatus = isDbUp ? "OK" : "Partial";

    return new SystemHealth(overallStatus, dbStatus);
  }
}
