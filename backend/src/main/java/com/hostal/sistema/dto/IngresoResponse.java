package com.hostal.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class IngresoResponse {
    private Long id;
    private String tipoHabitacion;
    private BigDecimal monto;
    private String observacion;
    private Instant fecha;
    private Boolean anulado;
    private String motivoAnulacion;
    private String sede;
    private String registradoPor;
}
