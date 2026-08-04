package com.hostal.sistema.controller;

import com.hostal.sistema.dto.MovimientosPageResponse;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.repository.UsuarioRepository;
import com.hostal.sistema.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos", description = "Listado de movimientos (ingresos y egresos)")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Listar movimientos", description = "Lista movimientos con filtros, búsqueda y paginación")
    public ResponseEntity<MovimientosPageResponse> listar(
            @Parameter(description = "Tipo de movimiento: TODOS, INGRESO, EGRESO")
            @RequestParam(defaultValue = "TODOS") String tipo,
            @Parameter(description = "Fecha de inicio (ISO-8601)")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha de fin (ISO-8601)")
            @RequestParam(required = false) String hasta,
            @Parameter(description = "Texto de búsqueda")
            @RequestParam(required = false) String search,
            @Parameter(description = "Número de página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size) {

        Long userId = resolveUserId();
        Instant desdeInstant = parseOrDefault(desde, Instant.EPOCH);
        Instant hastaInstant = parseOrDefault(hasta, Instant.now().plus(Duration.ofDays(3650)));

        return ResponseEntity.ok(movimientoService.listar(userId, tipo, desdeInstant, hastaInstant, search, page, size));
    }

    private Instant parseOrDefault(String value, Instant defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Long resolveUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"))
                .getId();
    }
}
