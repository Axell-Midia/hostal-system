package com.hostal.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class MovimientoResponse {
    private Long id;
    private String tipo; // "INGRESO" o "EGRESO"
    private String descripcion;
    private BigDecimal monto;
    private Instant fecha;
    private Boolean anulado;
    private String categoria;
    private String tipoHabitacion;
}
