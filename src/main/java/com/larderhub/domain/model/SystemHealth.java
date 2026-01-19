package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DOMAIN MODEL: SystemHealth
 * 
 * ¿Qué es?
 * Es una clase pura de Java (POJO) que representa el concepto de "Salud del
 * Sistema" en nuestro negocio.
 * 
 * Reglas:
 * 1. No sabe nada de JSON (nada de @JsonProperty).
 * 2. No sabe nada de Base de Datos (nada de @Entity, @Table).
 * 3. Es agnóstica de frameworks (solo usamos Lombok por comodidad, pero se
 * podría hacer con getters manuales).
 */
@Getter
@AllArgsConstructor
public class SystemHealth {
  private final String status;
  private final String dbStatus;
}
