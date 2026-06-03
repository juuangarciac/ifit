package com.uca.juangarcia.ifit.modules.training.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.training.model.Routine;

/**
 * Repositorio para gestionar operaciones de persistencia de rutinas.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
    
    /**
     * Encuentra todas las rutinas visibles (no eliminadas) de un usuario.
     */
    List<Routine> findByUserIdAndDeletedFalse(Long userId);

    /**
     * Encuentra rutinas visibles (no eliminadas) de un usuario con paginación.
     */
    Page<Routine> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /**
     * Encuentra rutinas visibles (no eliminadas) filtradas por estado activo.
     */
    List<Routine> findByUserIdAndIsActiveAndDeletedFalse(Long userId, boolean isActive);

    /**
     * Encuentra una rutina visible (no eliminada) de un usuario por su ID.
     */
    Optional<Routine> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    /**
     * Cuenta las rutinas activas y no eliminadas de un usuario.
     */
    long countByUserIdAndIsActiveAndDeletedFalse(Long userId, boolean isActive);

    /**
     * Encuentra todas las rutinas con sus días y ejercicios cargados (evita N+1).
     * Excluye rutinas eliminadas con soft-delete.
     */
    @Query("SELECT DISTINCT r FROM Routine r " +
           "LEFT JOIN FETCH r.days d " +
           "LEFT JOIN FETCH d.exercises " +
           "WHERE r.id = :routineId AND r.deleted = false")
    Optional<Routine> findByIdWithDaysAndExercises(@Param("routineId") Long routineId);

    /**
     * Encuentra rutinas activas y no eliminadas de un usuario con fetch join optimizado.
     */
    @Query("SELECT DISTINCT r FROM Routine r " +
           "LEFT JOIN FETCH r.days d " +
           "LEFT JOIN FETCH d.exercises " +
           "WHERE r.user.id = :userId AND r.isActive = true AND r.deleted = false")
    List<Routine> findActiveRoutinesWithDetailsByUserId(@Param("userId") Long userId);
}
