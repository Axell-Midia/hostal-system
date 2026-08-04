package com.hostal.sistema.controller;

import com.hostal.sistema.dto.DashboardResumen;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.repository.UsuarioRepository;
import com.hostal.sistema.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Resumen financiero del hostal")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Obtener resumen del dashboard", description = "Retorna totales de ingresos, egresos y ganancia en un rango de fechas")
    public ResponseEntity<DashboardResumen> getResumen(
            @Parameter(description = "Fecha de inicio (ISO-8601)")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha de fin (ISO-8601)")
            @RequestParam(required = false) String hasta) {

        Long userId = resolveUserId();
        Instant desdeInstant = parseOrDefault(desde, Instant.EPOCH);
        Instant hastaInstant = parseOrDefault(hasta, Instant.now().plus(Duration.ofDays(3650)));

        return ResponseEntity.ok(dashboardService.obtenerResumen(userId, desdeInstant, hastaInstant));
    }

    private Instant parseOrDefault(String value, Instant defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Instant.parse(value);
    }

    private Long resolveUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"))
                .getId();
    }
}
