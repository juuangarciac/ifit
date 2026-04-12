package com.uca.juangarcia.ifit.modules.exercises.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uca.juangarcia.ifit.modules.exercises.model.ExerciseCatalog;

public interface ExerciseCatalogRepository extends JpaRepository<ExerciseCatalog, Long> {

    @Query("""
        SELECT e FROM ExerciseCatalog e
        WHERE (:level    IS NULL OR e.level    = :level)
          AND (:category IS NULL OR e.category = :category)
          AND (:equipment IS NULL OR e.equipment = :equipment)
          AND (:muscle   IS NULL OR e.primaryMuscles LIKE %:muscle%)
        """)
    Page<ExerciseCatalog> findWithFilters(
        @Param("level")     String level,
        @Param("category")  String category,
        @Param("equipment") String equipment,
        @Param("muscle")    String muscle,
        Pageable pageable
    );
}
