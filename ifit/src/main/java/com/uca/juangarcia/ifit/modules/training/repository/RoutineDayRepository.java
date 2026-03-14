package com.uca.juangarcia.ifit.modules.training.repository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("SELECT rd FROM RoutineDay rd WHERE rd.routine.id = :routineId ORDER BY rd.dayNumber ASC")
    List<RoutineDay> findByRoutineId(Long routineId);
    
    /**
     * Elimina todos los días de una rutina.
     */
    void deleteByRoutineId(Long routineId);

    /**
     * Encuentra un día de rutina específico por el ID de la rutina y el número del día.
     * @param routineId
     * @param dayNumber
     * @return
     */
    @Query("SELECT rd FROM RoutineDay rd WHERE rd.routine.id = :routineId AND rd.dayNumber = :dayNumber")
    Optional<RoutineDay> findRoutineDayByRoutineIdAndDay(
        @Param("routineId") Long routineId, 
        @Param("dayNumber") Integer dayNumber
    );
}
