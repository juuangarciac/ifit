package com.uca.juangarcia.ifit.modules.training.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.training.model.RoutineDay;


/**
 * Repositorio para gestionar operaciones de persistencia de días de rutina.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Repository
public interface RoutineDayRepository extends JpaRepository<RoutineDay, Long> {
    
    /**
     * Encuentra todos los días de una rutina específica.
     */
    List<RoutineDay> findByRoutineIdOrderByDayNumberAsc(Long routineId);
    
    /**
     * Elimina todos los días de una rutina.
     */
    void deleteByRoutineId(Long routineId);
}
