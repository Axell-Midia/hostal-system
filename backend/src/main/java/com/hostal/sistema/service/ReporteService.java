package com.hostal.sistema.service;

import com.hostal.sistema.dto.DashboardResumen;
import com.hostal.sistema.dto.MovimientoResponse;
import com.hostal.sistema.dto.ReporteGrupoResponse;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.model.Egreso;
import com.hostal.sistema.model.Ingreso;
import com.hostal.sistema.model.Usuario;
import com.hostal.sistema.repository.EgresoRepository;
import com.hostal.sistema.repository.IngresoRepository;
import com.hostal.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final IngresoRepository ingresoRepository;
    private final EgresoRepository egresoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardResumen reportePorRango(Long userId, Instant desde, Instant hasta) {
        Long sedeId = getSedeId(userId);

        BigDecimal totalIngresos = ingresoRepository.sumBySedeAndFecha(sedeId, desde, hasta);
        BigDecimal totalEgresos = egresoRepository.sumBySedeAndFecha(sedeId, desde, hasta);

        return DashboardResumen.builder()
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .ganancia(totalIngresos.subtract(totalEgresos))
                .build();
    }

    public List<ReporteGrupoResponse> reporteIngresosPorTipoHabitacion(Long userId, Instant desde, Instant hasta) {
        Long sedeId = getSedeId(userId);

        return ingresoRepository.reportePorTipoHabitacion(sedeId, desde, hasta).stream()
                .map(row -> ReporteGrupoResponse.builder()
                        .grupo((String) row[0])
                        .cantidad((Long) row[1])
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    public List<ReporteGrupoResponse> reporteEgresosPorCategoria(Long userId, Instant desde, Instant hasta) {
        Long sedeId = getSedeId(userId);

        return egresoRepository.reportePorCategoria(sedeId, desde, hasta).stream()
                .map(row -> ReporteGrupoResponse.builder()
                        .grupo((String) row[0])
                        .cantidad((Long) row[1])
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    public String exportarCsv(Long userId, String tipo, Instant desde, Instant hasta) {
        Long sedeId = getSedeId(userId);
        StringBuilder csv = new StringBuilder();

        boolean incluirIngresos = tipo == null || tipo.isBlank() || "TODOS".equals(tipo) || "INGRESO".equals(tipo);
        boolean incluirEgresos = tipo == null || tipo.isBlank() || "TODOS".equals(tipo) || "EGRESO".equals(tipo);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        if (incluirIngresos) {
            List<Ingreso> ingresos = ingresoRepository.findBySedeAndFilters(
                    sedeId, desde, hasta, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            for (Ingreso i : ingresos) {
                csv.append("INGRESO")
                   .append(",").append(i.getId())
                   .append(",").append(i.getTipoHabitacion())
                   .append(",").append(i.getMonto())
                   .append(",").append(i.getFecha() != null ? formatter.format(i.getFecha()) : "")
                   .append(",").append(Boolean.TRUE.equals(i.getAnulado()) ? "SI" : "NO")
                   .append("\n");
            }
        }

        if (incluirEgresos) {
            List<Egreso> egresos = egresoRepository.findBySedeAndFilters(
                    sedeId, desde, hasta, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            for (Egreso e : egresos) {
                csv.append("EGRESO")
                   .append(",").append(e.getId())
                   .append(",").append(e.getCategoria())
                   .append(",").append(e.getMonto())
                   .append(",").append(e.getFecha() != null ? formatter.format(e.getFecha()) : "")
                   .append(",").append(Boolean.TRUE.equals(e.getAnulado()) ? "SI" : "NO")
                   .append("\n");
            }
        }

        return csv.toString();
    }

    private Long getSedeId(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return usuario.getSede().getId();
    }
}
