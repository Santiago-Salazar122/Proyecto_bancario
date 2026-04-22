package com.bank.aplicacion;

import com.bank.domain.model.loan.Prestamo;
import com.bank.infrastructure.repositorios.PrestamoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PrestamoService {

    private static final String ESTADO_EN_ESTUDIO = "EN ESTUDIO";

    private final PrestamoRepository prestamoRepository;

    public PrestamoService(PrestamoRepository prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }

    public Prestamo solicitarPrestamo(BigDecimal montoSolicitado, Double tasaInteres, Integer plazoMeses) {
        validarMontoSolicitado(montoSolicitado);

        Prestamo prestamo = new Prestamo(montoSolicitado, tasaInteres, plazoMeses);
        prestamo.setEstado(ESTADO_EN_ESTUDIO);

        return prestamoRepository.save(prestamo);
    }

    private void validarMontoSolicitado(BigDecimal montoSolicitado) {
        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto solicitado debe ser mayor a 0.");
        }
    }
}