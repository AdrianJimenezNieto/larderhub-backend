package com.larderhub.domain.ports.in.health;

import com.larderhub.domain.model.SystemHealth;

/**
 * INPUT PORT (Caso de Uso): GetSystemHealthUseCase
 * 
 * ¿Qué es?
 * Es la interfaz que defines las operaciones disponibles para este contexto.
 * Le dice al mundo: "Mi aplicación sabe consultar su estado de salud".
 * 
 * ¿Por qué una interfaz?
 * Para desacoplar "qué quiero hacer" de "cómo lo hago".
 */
public interface GetSystemHealthUseCase {
  SystemHealth execute();
}
