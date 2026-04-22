package com.bank.infrastructure.api.v1;

import com.bank.aplicacion.PrestamoService;
import com.bank.domain.model.loan.Prestamo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<Prestamo> solicitarPrestamo(@RequestBody SolicitudPrestamoRequest request) {
        Prestamo prestamo = prestamoService.solicitarPrestamo(
            request.getMontoSolicitado(),
            request.getTasaInteres(),
            request.getPlazoMeses()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(prestamo);
    }

    public static class SolicitudPrestamoRequest {

        private BigDecimal montoSolicitado;
        private Double tasaInteres;
        private Integer plazoMeses;

        public SolicitudPrestamoRequest() {
        }

        public BigDecimal getMontoSolicitado() {
            return montoSolicitado;
        }

        public void setMontoSolicitado(BigDecimal montoSolicitado) {
            this.montoSolicitado = montoSolicitado;
        }

        public Double getTasaInteres() {
            return tasaInteres;
        }

        public void setTasaInteres(Double tasaInteres) {
            this.tasaInteres = tasaInteres;
        }

        public Integer getPlazoMeses() {
            return plazoMeses;
        }

        public void setPlazoMeses(Integer plazoMeses) {
            this.plazoMeses = plazoMeses;
        }
    }
}