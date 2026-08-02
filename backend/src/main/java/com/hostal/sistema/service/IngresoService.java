package com.hostal.sistema.service;

import com.hostal.sistema.dto.*;
import com.hostal.sistema.exception.OperacionNoPermitidaException;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.model.Ingreso;
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
public class IngresoService {

    private final IngresoRepository ingresoRepository;
    private final EgresoRepository egresoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public IngresoResponse crear(Long userId, IngresoRequest request) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Ingreso ingreso = new Ingreso();
        ingreso.setTipoHabitacion(request.getTipoHabitacion().toUpperCase());
        ingreso.setMonto(request.getMonto());
        ingreso.setObservacion(request.getObservacion());
        ingreso.setFecha(Instant.now());
        ingreso.setUsuario(usuario);
        ingreso.setAnulado(false);

        Ingreso guardado = ingresoRepository.save(ingreso);

        return IngresoResponse.builder()
                .id(guardado.getId())
                .tipoHabitacion(guardado.getTipoHabitacion())
                .monto(guardado.getMonto())
                .observacion(guardado.getObservacion())
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

        Page<Ingreso> ingresos;
        if (hasSearch) {
            ingresos = ingresoRepository.findBySedeAndFiltersWithSearch(
                    sedeId, desde, hasta, s, PageRequest.of(page, size));
        } else {
            ingresos = ingresoRepository.findBySedeAndFilters(
                    sedeId, desde, hasta, PageRequest.of(page, size));
        }

        List<MovimientoResponse> movimientos = ingresos.getContent().stream()
                .map(i -> MovimientoResponse.builder()
                        .id(i.getId())
                        .tipo("INGRESO")
                        .descripcion(i.getTipoHabitacion())
                        .monto(i.getMonto())
                        .fecha(i.getFecha())
                        .anulado(i.getAnulado())
                        .categoria(null)
                        .tipoHabitacion(i.getTipoHabitacion())
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
                .totalElements(ingresos.getTotalElements())
                .totalPages(ingresos.getTotalPages())
                .build();
    }

    @Transactional
    public IngresoResponse anular(Long userId, Long id, String motivo) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Ingreso ingreso = ingresoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ingreso no encontrado"));

        if (!ingreso.getUsuario().getSede().getId().equals(usuario.getSede().getId())) {
            throw new OperacionNoPermitidaException("El ingreso no pertenece a su sede");
        }

        if (ingreso.getAnulado()) {
            throw new OperacionNoPermitidaException("El ingreso ya fue anulado");
        }

        ingreso.setAnulado(true);
        ingreso.setMotivoAnulacion(motivo);
        ingreso.setAnuladoPor(usuario);
        ingreso.setFechaAnulacion(Instant.now());

        Ingreso anulado = ingresoRepository.save(ingreso);

        return IngresoResponse.builder()
                .id(anulado.getId())
                .tipoHabitacion(anulado.getTipoHabitacion())
                .monto(anulado.getMonto())
                .observacion(anulado.getObservacion())
                .fecha(anulado.getFecha())
                .anulado(anulado.getAnulado())
                .motivoAnulacion(anulado.getMotivoAnulacion())
                .sede(usuario.getSede().getNombre())
                .registradoPor(usuario.getNombreCompleto())
                .build();
    }
}
