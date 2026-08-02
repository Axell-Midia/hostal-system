package com.hostal.sistema.repository;

import com.hostal.sistema.model.Egreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface EgresoRepository extends JpaRepository<Egreso, Long> {

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Egreso e " +
           "WHERE e.usuario.sede.id = :sedeId AND e.anulado = false " +
           "AND e.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumBySedeAndFecha(@Param("sedeId") Long sedeId,
                                 @Param("desde") Instant desde,
                                 @Param("hasta") Instant hasta);

    @Query("SELECT e FROM Egreso e " +
           "WHERE e.usuario.sede.id = :sedeId " +
           "AND e.fecha BETWEEN :desde AND :hasta " +
           "ORDER BY e.fecha DESC")
    Page<Egreso> findBySedeAndFilters(@Param("sedeId") Long sedeId,
                                      @Param("desde") Instant desde,
                                      @Param("hasta") Instant hasta,
                                      Pageable pageable);

    @Query("SELECT e FROM Egreso e " +
           "WHERE e.usuario.sede.id = :sedeId " +
           "AND e.fecha BETWEEN :desde AND :hasta " +
           "AND (LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(e.categoria) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.fecha DESC")
    Page<Egreso> findBySedeAndFiltersWithSearch(@Param("sedeId") Long sedeId,
                                                 @Param("desde") Instant desde,
                                                 @Param("hasta") Instant hasta,
                                                 @Param("search") String search,
                                                 Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Egreso e " +
           "WHERE e.usuario.sede.id = :sedeId AND e.anulado = false " +
           "AND e.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumBySedeAndFilters(@Param("sedeId") Long sedeId,
                                   @Param("desde") Instant desde,
                                   @Param("hasta") Instant hasta);

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Egreso e " +
           "WHERE e.usuario.sede.id = :sedeId AND e.anulado = false " +
           "AND e.fecha BETWEEN :desde AND :hasta " +
           "AND (LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(e.categoria) LIKE LOWER(CONCAT('%', :search, '%')))")
    BigDecimal sumBySedeAndFiltersWithSearch(@Param("sedeId") Long sedeId,
                                              @Param("desde") Instant desde,
                                              @Param("hasta") Instant hasta,
                                              @Param("search") String search);

    @Query("SELECT e.categoria, COUNT(e), COALESCE(SUM(e.monto), 0) FROM Egreso e " +
           "WHERE e.usuario.sede.id = :sedeId AND e.anulado = false " +
           "AND e.fecha BETWEEN :desde AND :hasta " +
           "GROUP BY e.categoria ORDER BY SUM(e.monto) DESC")
    List<Object[]> reportePorCategoria(@Param("sedeId") Long sedeId,
                                        @Param("desde") Instant desde,
                                        @Param("hasta") Instant hasta);
}
