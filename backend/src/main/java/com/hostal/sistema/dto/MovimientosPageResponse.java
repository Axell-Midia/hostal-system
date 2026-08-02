package com.hostal.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MovimientosPageResponse {
    private List<MovimientoResponse> movimientos;
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal ganancia;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
