/**
 * OUTPUT PORTS (Puertos de Salida) - SPI
 * 
 * Aquí definimos las interfaces de lo que el dominio NECESITA para funcionar,
 * pero que no sabe CÓMO se hace (ej: UserRepository, EmailSender).
 * 
 * Es la técnica de Inversión de Dependencia: El dominio pide "Guarda este
 * usuario",
 * y la capa de infraestructura implementará cómo guardarlo (SQL, Mongo,
 * File...).
 */
package com.larderhub.domain.ports.out;
