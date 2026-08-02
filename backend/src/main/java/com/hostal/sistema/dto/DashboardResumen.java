package com.hostal.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class DashboardResumen {
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal ganancia;
}
