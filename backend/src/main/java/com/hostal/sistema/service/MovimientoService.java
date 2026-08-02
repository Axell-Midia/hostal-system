package com.hostal.sistema.service;

import com.hostal.sistema.dto.MovimientoResponse;
import com.hostal.sistema.dto.MovimientosPageResponse;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.model.Usuario;
import com.hostal.sistema.repository.EgresoRepository;
import com.hostal.sistema.repository.IngresoRepository;
import com.hostal.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final IngresoRepository ingresoRepository;
    private final EgresoRepository egresoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INGRESO_SELECT = """
            SELECT i.id AS id, 'INGRESO' AS tipo, i.tipo_habitacion AS descripcion,
                   i.monto AS monto, i.fecha AS fecha, i.anulado AS anulado,
                   NULL AS categoria, i.tipo_habitacion AS tipo_habitacion
            FROM ingreso i
            JOIN usuario u ON i.id_usuario = u.id
            WHERE u.id_sede = :sedeId AND i.fecha BETWEEN :desde AND :hasta
            """;

    private static final String EGRESO_SELECT = """
            SELECT e.id AS id, 'EGRESO' AS tipo, COALESCE(e.descripcion, '') AS descripcion,
                   e.monto AS monto, e.fecha AS fecha, e.anulado AS anulado,
                   e.categoria AS categoria, NULL AS tipo_habitacion
            FROM egreso e
            JOIN usuario u ON e.id_usuario = u.id
            WHERE u.id_sede = :sedeId AND e.fecha BETWEEN :desde AND :hasta
            """;

    private static final String INGRESO_SEARCH =
            " AND (LOWER(i.observacion) LIKE LOWER(:search) OR LOWER(i.tipo_habitacion) LIKE LOWER(:search)) ";
    private static final String EGRESO_SEARCH =
            " AND (LOWER(e.descripcion) LIKE LOWER(:search) OR LOWER(e.categoria) LIKE LOWER(:search)) ";

    private static final RowMapper<MovimientoResponse> MOVIMIENTO_MAPPER = (rs, rowNum) ->
            MovimientoResponse.builder()
                    .id(rs.getLong("id"))
                    .tipo(rs.getString("tipo"))
                    .descripcion(rs.getString("descripcion"))
                    .monto(rs.getBigDecimal("monto"))
                    .fecha(rs.getTimestamp("fecha").toInstant())
                    .anulado(rs.getBoolean("anulado"))
                    .categoria(rs.getString("categoria"))
                    .tipoHabitacion(rs.getString("tipo_habitacion"))
                    .build();

    public MovimientosPageResponse listar(Long userId, String tipo, Instant desde, Instant hasta,
                                           String search, int page, int size) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Long sedeId = usuario.getSede().getId();
        boolean hasSearch = search != null && !search.isBlank();
        String patron = hasSearch ? "%" + search.trim() + "%" : "";
        int safeSize = Math.max(1, size);

        boolean incluirIngresos = tipo == null || tipo.isBlank() || "TODOS".equals(tipo) || "INGRESO".equals(tipo);
        boolean incluirEgresos = tipo == null || tipo.isBlank() || "TODOS".equals(tipo) || "EGRESO".equals(tipo);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("sedeId", sedeId);
        params.addValue("desde", Timestamp.from(desde));
        params.addValue("hasta", Timestamp.from(hasta));
        if (hasSearch) {
            params.addValue("search", patron);
        }

        List<String> selects = new ArrayList<>();
        if (incluirIngresos) {
            selects.add(INGRESO_SELECT + (hasSearch ? INGRESO_SEARCH : ""));
        }
        if (incluirEgresos) {
            selects.add(EGRESO_SELECT + (hasSearch ? EGRESO_SEARCH : ""));
        }

        String unionSql = String.join(" UNION ALL ", selects);

        String countSql = "SELECT COUNT(*) FROM (" + unionSql + ") total";
        long totalElements = jdbcTemplate.queryForObject(countSql, params, Long.class);

        String pageSql = unionSql + " ORDER BY fecha DESC LIMIT :limit OFFSET :offset";
        List<MovimientoResponse> paginados = jdbcTemplate.query(
                pageSql,
                params.addValue("limit", safeSize).addValue("offset", (long) page * safeSize),
                MOVIMIENTO_MAPPER);

        BigDecimal totalIngresos;
        BigDecimal totalEgresos;
        if (hasSearch) {
            totalIngresos = ingresoRepository.sumBySedeAndFiltersWithSearch(sedeId, desde, hasta, search.trim());
            totalEgresos = egresoRepository.sumBySedeAndFiltersWithSearch(sedeId, desde, hasta, search.trim());
        } else {
            totalIngresos = ingresoRepository.sumBySedeAndFilters(sedeId, desde, hasta);
            totalEgresos = egresoRepository.sumBySedeAndFilters(sedeId, desde, hasta);
        }
        if (totalIngresos == null) totalIngresos = BigDecimal.ZERO;
        if (totalEgresos == null) totalEgresos = BigDecimal.ZERO;

        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        return MovimientosPageResponse.builder()
                .movimientos(paginados)
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .ganancia(totalIngresos.subtract(totalEgresos))
                .page(page)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
