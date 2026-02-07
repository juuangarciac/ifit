package com.uca.juangarcia.ifit.modules.training.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.training.model.RoutineExercise;



/**
 * Repositorio para gestionar operaciones de persistencia de ejercicios de rutina.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Repository
public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {
    
    /**
     * Encuentra todos los ejercicios de un día de rutina específico.
     */
    List<RoutineExercise> findByRoutineDayIdOrderByOrderIndexAsc(Long routineDayId);
    
    /**
     * Elimina todos los ejercicios de un día de rutina.
     */
    void deleteByRoutineDayId(Long routineDayId);
}
