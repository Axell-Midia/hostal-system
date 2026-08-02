package com.hostal.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class EgresoResponse {
    private Long id;
    private BigDecimal monto;
    private String categoria;
    private String descripcion;
    private Instant fecha;
    private Boolean anulado;
    private String motivoAnulacion;
    private String sede;
    private String registradoPor;
}
