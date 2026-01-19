package com.larderhub.domain.ports.out.health;

/**
 * OUTPUT PORT: DbHealthOutputPort
 * 
 * ¿Qué es?
 * Es una interfaz que define una NECESIDAD que tiene el dominio.
 * El dominio dice: "Necesito saber si la base de datos funciona", pero no sabe
 * CÓMO verificarlo.
 * 
 * ¿Quién implementa esto?
 * La capa de Infraestructura implementará esto consultando Postgres de verdad.
 */
public interface DbHealthOutputPort {
  boolean isDbUp();
}
