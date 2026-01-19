package com.larderhub.infrastructure.adapter.out.persistence;

import com.larderhub.domain.ports.out.DbHealthOutputPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * INFRASTRUCTURE ADAPTER (Output - Persistence): HealthPersistenceAdapter
 * 
 * Implementa el puerto de salida (DbHealthOutputPort).
 * Es la pieza que SABE cómo hablar con Postgres.
 * 
 * Anotaciones:
 * 
 * @Repository -> Le dice a Spring que esto maneja datos y traduce excepciones
 *             de SQL a las de Spring.
 */
@Repository
public class HealthPersistenceAdapter implements DbHealthOutputPort {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public boolean isDbUp() {
    try {
      // Hacemos una query nativa simple. Si no lanza excepción, la DB está viva.
      entityManager.createNativeQuery("SELECT 1").getSingleResult();
      return true;
    } catch (Exception e) {
      // Si falla la conexión o hay timeout, devolvemos false.
      return false;
    }
  }
}
