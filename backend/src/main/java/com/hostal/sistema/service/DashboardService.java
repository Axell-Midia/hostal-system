package com.hostal.sistema.service;

import com.hostal.sistema.dto.DashboardResumen;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.model.Usuario;
import com.hostal.sistema.repository.EgresoRepository;
import com.hostal.sistema.repository.IngresoRepository;
import com.hostal.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IngresoRepository ingresoRepository;
    private final EgresoRepository egresoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardResumen obtenerResumen(Long userId, Instant desde, Instant hasta) {
        Long sedeId = getSedeId(userId);

        BigDecimal totalIngresos = ingresoRepository.sumBySedeAndFecha(sedeId, desde, hasta);
        BigDecimal totalEgresos = egresoRepository.sumBySedeAndFecha(sedeId, desde, hasta);
        BigDecimal ganancia = totalIngresos.subtract(totalEgresos);

        return DashboardResumen.builder()
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .ganancia(ganancia)
                .build();
    }

    private Long getSedeId(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return usuario.getSede().getId();
    }
}
