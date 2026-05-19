package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal del Sistema Bancario.
 *
 * @SpringBootApplication activa:
 *   - Configuración automática de Spring Boot
 *   - Escaneo de componentes (@Service, @Repository, @Controller) en el paquete com.bank
 *   - Configuración dual: JPA para MySQL + MongoDB para Bitácora
 *
 * @EnableScheduling activa las tareas programadas (@Scheduled).
 *   Necesario para ExpirePendingTransfersService que vence automáticamente
 *   las transferencias que llevan más de 60 minutos esperando aprobación.
 */
@SpringBootApplication
@EnableScheduling
public class BankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }
}
