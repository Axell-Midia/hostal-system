package com.hostal.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ReporteGrupoResponse {
    private String grupo; // tipo_habitacion o categoria
    private Long cantidad;
    private BigDecimal total;
}
