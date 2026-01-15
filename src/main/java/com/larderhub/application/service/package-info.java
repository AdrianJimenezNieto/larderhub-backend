/**
 * APPLICATION LAYER - THE ORCHESTRATOR (El Director de Orquesta)
 * 
 * Aquí implementamos los Puertos de Entrada (Casos de Uso) definidos en el
 * Dominio.
 * 
 * ¿Qué hace esta capa?
 * 1. Recibe una petición desde fuera (a través de un adaptador)..
 * 2. Orquesta la lógica llamando a las Entidades o Servicios de Dominio.
 * 3. Usa los Puertos de Salida (Repositorios) para persistir los cambios.
 * 
 * Diferencia Clave con Dominio:
 * - Dominio: "Si la despensa está llena, no cabe más." (Regla de negocio).
 * - Aplicación: "Busca la despensa X, intenta meter producto, si falla lanza
 * error, si ok guarda." (Flujo).
 */
package com.larderhub.application.service;
