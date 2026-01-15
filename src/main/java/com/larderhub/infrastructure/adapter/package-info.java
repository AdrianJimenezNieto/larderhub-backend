/**
 * INFRASTRUCTURE LAYER - ADAPTERS (Los Traductores)
 * 
 * Esta capa es la única que sabe que estamos usando Spring Boot, Postgres o
 * REST.
 * 
 * Se divide en dos lados:
 * 1. INPUT ADAPTERS (Driving): Controladores REST, Consumers de Kafka.
 * - Reciben llamadas HTTP y llaman a los Casos de Uso de la Capa de Aplicación.
 * - Traducen JSON -> Objetos de Dominio/DTOs.
 * 
 * 2. OUTPUT ADAPTERS (Driven): Implementaciones de Repositorios, Clientes HTTP
 * externos.
 * - Implementan las interfaces (Output Ports) definidas en el Dominio.
 * - Traducen Objetos de Dominio -> Entidades JPA (Tablas SQL) o JSON para APIs
 * externas.
 */
package com.larderhub.infrastructure.adapter;
