package com.hostal.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnulacionRequest {

    @NotBlank(message = "El motivo de anulación es obligatorio")
    private String motivo;
}
