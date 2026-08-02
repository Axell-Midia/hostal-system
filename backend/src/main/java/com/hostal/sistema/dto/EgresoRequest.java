package com.hostal.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request DTO para crear un egreso.
 * NO incluye fecha, sede ni usuario — estos se derivan del backend.
 */
@Getter
@Setter
public class EgresoRequest {

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    private String descripcion;
}
