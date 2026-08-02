package com.hostal.sistema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ingreso")
@Getter
@Setter
@NoArgsConstructor
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_habitacion", nullable = false, length = 20)
    private String tipoHabitacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    private String observacion;

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Boolean anulado = false;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por")
    private Usuario anuladoPor;

    @Column(name = "fecha_anulacion")
    private Instant fechaAnulacion;
}
