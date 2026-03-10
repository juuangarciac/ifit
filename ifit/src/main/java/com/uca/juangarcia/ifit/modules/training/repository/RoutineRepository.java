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
     * Encuentra todas las rutinas de un usuario específico.
     */
    List<Routine> findByUserId(Long userId);
    
    /**
     * Encuentra todas las rutinas activas de un usuario.
     */
    Routine findByUserIdAndIsActive(Long userId, boolean isActive);
    
    /**
     * Encuentra rutinas de un usuario con paginación.
     */
    Page<Routine> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Encuentra una rutina específica de un usuario.
     */
    Optional<Routine> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Cuenta las rutinas activas de un usuario.
     */
    long countByUserIdAndIsActive(Long userId, boolean isActive);
    
    /**
     * Encuentra todas las rutinas con sus días y ejercicios cargados (evita N+1).
     */
    @Query("SELECT DISTINCT r FROM Routine r " +
           "LEFT JOIN FETCH r.days d " +
           "LEFT JOIN FETCH d.exercises " +
           "WHERE r.id = :routineId")
    Optional<Routine> findByIdWithDaysAndExercises(@Param("routineId") Long routineId);
    
    /**
     * Encuentra rutinas activas de un usuario con fetch join optimizado.
     */
    @Query("SELECT DISTINCT r FROM Routine r " +
           "LEFT JOIN FETCH r.days d " +
           "LEFT JOIN FETCH d.exercises " +
           "WHERE r.user.id = :userId AND r.isActive = true")
    List<Routine> findActiveRoutinesWithDetailsByUserId(@Param("userId") Long userId);
}
