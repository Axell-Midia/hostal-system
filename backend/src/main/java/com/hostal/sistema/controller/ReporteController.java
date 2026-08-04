package com.hostal.sistema.controller;

import com.hostal.sistema.dto.DashboardResumen;
import com.hostal.sistema.dto.ReporteGrupoResponse;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.repository.UsuarioRepository;
import com.hostal.sistema.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Reportes financieros y exportación")
public class ReporteController {

    private final ReporteService reporteService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/rango")
    @Operation(summary = "Reporte por rango de fechas", description = "Retorna resumen de ingresos, egresos y ganancia en un período")
    public ResponseEntity<DashboardResumen> reportePorRango(
            @Parameter(description = "Fecha de inicio (ISO-8601)")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha de fin (ISO-8601)")
            @RequestParam(required = false) String hasta) {

        Long userId = resolveUserId();
        Instant desdeInstant = parseOrDefault(desde, Instant.EPOCH);
        Instant hastaInstant = parseOrDefault(hasta, Instant.now().plus(Duration.ofDays(3650)));

        return ResponseEntity.ok(reporteService.reportePorRango(userId, desdeInstant, hastaInstant));
    }

    @GetMapping("/ingresos-por-habitacion")
    @Operation(summary = "Ingresos por tipo de habitación", description = "Agrupa ingresos por tipo de habitación con cantidad y total")
    public ResponseEntity<List<ReporteGrupoResponse>> reporteIngresosPorTipoHabitacion(
            @Parameter(description = "Fecha de inicio (ISO-8601)")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha de fin (ISO-8601)")
            @RequestParam(required = false) String hasta) {

        Long userId = resolveUserId();
        Instant desdeInstant = parseOrDefault(desde, Instant.EPOCH);
        Instant hastaInstant = parseOrDefault(hasta, Instant.now().plus(Duration.ofDays(3650)));

        return ResponseEntity.ok(reporteService.reporteIngresosPorTipoHabitacion(userId, desdeInstant, hastaInstant));
    }

    @GetMapping("/egresos-por-categoria")
    @Operation(summary = "Egresos por categoría", description = "Agrupa egresos por categoría con cantidad y total")
    public ResponseEntity<List<ReporteGrupoResponse>> reporteEgresosPorCategoria(
            @Parameter(description = "Fecha de inicio (ISO-8601)")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha de fin (ISO-8601)")
            @RequestParam(required = false) String hasta) {

        Long userId = resolveUserId();
        Instant desdeInstant = parseOrDefault(desde, Instant.EPOCH);
        Instant hastaInstant = parseOrDefault(hasta, Instant.now().plus(Duration.ofDays(3650)));

        return ResponseEntity.ok(reporteService.reporteEgresosPorCategoria(userId, desdeInstant, hastaInstant));
    }

    @GetMapping("/exportar-csv")
    @Operation(summary = "Exportar movimientos a CSV", description = "Descarga un archivo CSV con los movimientos filtrados")
    public ResponseEntity<String> exportarCsv(
            @Parameter(description = "Tipo de movimiento: TODOS, INGRESO, EGRESO")
            @RequestParam(defaultValue = "TODOS") String tipo,
            @Parameter(description = "Fecha de inicio (ISO-8601)")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Fecha de fin (ISO-8601)")
            @RequestParam(required = false) String hasta) {

        Long userId = resolveUserId();
        Instant desdeInstant = parseOrDefault(desde, Instant.EPOCH);
        Instant hastaInstant = parseOrDefault(hasta, Instant.now().plus(Duration.ofDays(3650)));

        String csv = reporteService.exportarCsv(userId, tipo, desdeInstant, hastaInstant);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movimientos.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
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
