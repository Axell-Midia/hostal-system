package com.hostal.sistema.repository;

import com.hostal.sistema.model.Ingreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface IngresoRepository extends JpaRepository<Ingreso, Long> {

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i " +
           "WHERE i.usuario.sede.id = :sedeId AND i.anulado = false " +
           "AND i.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumBySedeAndFecha(@Param("sedeId") Long sedeId,
                                 @Param("desde") Instant desde,
                                 @Param("hasta") Instant hasta);

    @Query("SELECT i FROM Ingreso i " +
           "WHERE i.usuario.sede.id = :sedeId " +
           "ORDER BY i.fecha DESC")
    List<Ingreso> findUltimosBySede(@Param("sedeId") Long sedeId, Pageable pageable);

    @Query("SELECT i FROM Ingreso i " +
           "WHERE i.usuario.sede.id = :sedeId " +
           "AND i.fecha BETWEEN :desde AND :hasta " +
           "ORDER BY i.fecha DESC")
    Page<Ingreso> findBySedeAndFilters(@Param("sedeId") Long sedeId,
                                        @Param("desde") Instant desde,
                                        @Param("hasta") Instant hasta,
                                        Pageable pageable);

    @Query("SELECT i FROM Ingreso i " +
           "WHERE i.usuario.sede.id = :sedeId " +
           "AND i.fecha BETWEEN :desde AND :hasta " +
           "AND (LOWER(i.observacion) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(i.tipoHabitacion) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY i.fecha DESC")
    Page<Ingreso> findBySedeAndFiltersWithSearch(@Param("sedeId") Long sedeId,
                                                  @Param("desde") Instant desde,
                                                  @Param("hasta") Instant hasta,
                                                  @Param("search") String search,
                                                  Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i " +
           "WHERE i.usuario.sede.id = :sedeId AND i.anulado = false " +
           "AND i.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumBySedeAndFilters(@Param("sedeId") Long sedeId,
                                   @Param("desde") Instant desde,
                                   @Param("hasta") Instant hasta);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i " +
           "WHERE i.usuario.sede.id = :sedeId AND i.anulado = false " +
           "AND i.fecha BETWEEN :desde AND :hasta " +
           "AND (LOWER(i.observacion) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(i.tipoHabitacion) LIKE LOWER(CONCAT('%', :search, '%')))")
    BigDecimal sumBySedeAndFiltersWithSearch(@Param("sedeId") Long sedeId,
                                              @Param("desde") Instant desde,
                                              @Param("hasta") Instant hasta,
                                              @Param("search") String search);

    @Query("SELECT i.tipoHabitacion, COUNT(i), COALESCE(SUM(i.monto), 0) FROM Ingreso i " +
           "WHERE i.usuario.sede.id = :sedeId AND i.anulado = false " +
           "AND i.fecha BETWEEN :desde AND :hasta " +
           "GROUP BY i.tipoHabitacion ORDER BY SUM(i.monto) DESC")
    List<Object[]> reportePorTipoHabitacion(@Param("sedeId") Long sedeId,
                                             @Param("desde") Instant desde,
                                             @Param("hasta") Instant hasta);
}
