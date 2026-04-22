package com.bank.domain.model.loan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto_solicitado", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoSolicitado;

    @Column(name = "tasa_interes", nullable = false)
    private Double tasaInteres;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    protected Prestamo() {
    }

    public Prestamo(BigDecimal montoSolicitado, Double tasaInteres, Integer plazoMeses) {
        this.montoSolicitado = montoSolicitado;
        this.tasaInteres = tasaInteres;
        this.plazoMeses = plazoMeses;
        this.estado = "EN ESTUDIO";
    }

    @PrePersist
    public void asegurarEstadoInicial() {
        if (estado == null || estado.isBlank()) {
            estado = "EN ESTUDIO";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}