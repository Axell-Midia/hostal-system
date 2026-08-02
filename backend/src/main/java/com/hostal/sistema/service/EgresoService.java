package com.hostal.sistema.service;

import com.hostal.sistema.dto.*;
import com.hostal.sistema.exception.OperacionNoPermitidaException;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.model.Egreso;
import com.hostal.sistema.model.Usuario;
import com.hostal.sistema.repository.EgresoRepository;
import com.hostal.sistema.repository.IngresoRepository;
import com.hostal.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EgresoService {

    private final EgresoRepository egresoRepository;
    private final IngresoRepository ingresoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EgresoResponse crear(Long userId, EgresoRequest request) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String categoria = request.getCategoria().toUpperCase();

        if ("OTROS".equals(categoria) && (request.getDescripcion() == null || request.getDescripcion().isBlank())) {
            throw new OperacionNoPermitidaException("La categoría OTROS requiere una descripción");
        }

        Egreso egreso = new Egreso();
        egreso.setMonto(request.getMonto());
        egreso.setCategoria(categoria);
        egreso.setDescripcion(request.getDescripcion());
        egreso.setFecha(Instant.now());
        egreso.setUsuario(usuario);
        egreso.setAnulado(false);

        Egreso guardado = egresoRepository.save(egreso);

        return EgresoResponse.builder()
                .id(guardado.getId())
                .monto(guardado.getMonto())
                .categoria(guardado.getCategoria())
                .descripcion(guardado.getDescripcion())
                .fecha(guardado.getFecha())
                .anulado(guardado.getAnulado())
                .motivoAnulacion(guardado.getMotivoAnulacion())
                .sede(usuario.getSede().getNombre())
                .registradoPor(usuario.getNombreCompleto())
                .build();
    }

    public MovimientosPageResponse listarConFiltros(Long userId, Instant desde, Instant hasta,
                                                     String search, int page, int size) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Long sedeId = usuario.getSede().getId();
        boolean hasSearch = search != null && !search.isBlank();
        String s = hasSearch ? search.trim() : "";

        Page<Egreso> egresos;
        if (hasSearch) {
            egresos = egresoRepository.findBySedeAndFiltersWithSearch(
                    sedeId, desde, hasta, s, PageRequest.of(page, size));
        } else {
            egresos = egresoRepository.findBySedeAndFilters(
                    sedeId, desde, hasta, PageRequest.of(page, size));
        }

        List<MovimientoResponse> movimientos = egresos.getContent().stream()
                .map(e -> MovimientoResponse.builder()
                        .id(e.getId())
                        .tipo("EGRESO")
                        .descripcion(e.getDescripcion())
                        .monto(e.getMonto())
                        .fecha(e.getFecha())
                        .anulado(e.getAnulado())
                        .categoria(e.getCategoria())
                        .tipoHabitacion(null)
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalIngresos;
        BigDecimal totalEgresos;
        if (hasSearch) {
            totalIngresos = ingresoRepository.sumBySedeAndFiltersWithSearch(sedeId, desde, hasta, s);
            totalEgresos = egresoRepository.sumBySedeAndFiltersWithSearch(sedeId, desde, hasta, s);
        } else {
            totalIngresos = ingresoRepository.sumBySedeAndFilters(sedeId, desde, hasta);
            totalEgresos = egresoRepository.sumBySedeAndFilters(sedeId, desde, hasta);
        }

        return MovimientosPageResponse.builder()
                .movimientos(movimientos)
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .ganancia(totalIngresos.subtract(totalEgresos))
                .page(page)
                .size(size)
                .totalElements(egresos.getTotalElements())
                .totalPages(egresos.getTotalPages())
                .build();
    }

    @Transactional
    public EgresoResponse anular(Long userId, Long id, String motivo) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Egreso egreso = egresoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Egreso no encontrado"));

        if (!egreso.getUsuario().getSede().getId().equals(usuario.getSede().getId())) {
            throw new OperacionNoPermitidaException("El egreso no pertenece a su sede");
        }

        if (egreso.getAnulado()) {
            throw new OperacionNoPermitidaException("El egreso ya fue anulado");
        }

        egreso.setAnulado(true);
        egreso.setMotivoAnulacion(motivo);
        egreso.setAnuladoPor(usuario);
        egreso.setFechaAnulacion(Instant.now());

        Egreso anulado = egresoRepository.save(egreso);

        return EgresoResponse.builder()
                .id(anulado.getId())
                .monto(anulado.getMonto())
                .categoria(anulado.getCategoria())
                .descripcion(anulado.getDescripcion())
                .fecha(anulado.getFecha())
                .anulado(anulado.getAnulado())
                .motivoAnulacion(anulado.getMotivoAnulacion())
                .sede(usuario.getSede().getNombre())
                .registradoPor(usuario.getNombreCompleto())
                .build();
    }
}
