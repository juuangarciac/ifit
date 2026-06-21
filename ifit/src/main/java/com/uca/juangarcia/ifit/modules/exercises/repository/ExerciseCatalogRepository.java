package com.uca.juangarcia.ifit.modules.exercises.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uca.juangarcia.ifit.modules.exercises.dto.ExerciseNameRef;
import com.uca.juangarcia.ifit.modules.exercises.model.ExerciseCatalog;

public interface ExerciseCatalogRepository extends JpaRepository<ExerciseCatalog, Long> {

    /**
     * Devuelve todos los ejercicios del catálogo como proyección id+nombre.
     * Usado por el normalizador de nombres para construir su índice en memoria.
     */
    @Query("SELECT new com.uca.juangarcia.ifit.modules.exercises.dto.ExerciseNameRef(e.id, e.name) "
            + "FROM ExerciseCatalog e")
    List<ExerciseNameRef> findAllNameRefs();

    @Query(
        value = """
            SELECT * FROM exercise_catalog
            WHERE (:level     IS NULL OR level     = :level)
              AND (:category  IS NULL OR category  = :category)
              AND (:equipment IS NULL OR equipment = :equipment)
              AND (:muscle    IS NULL
                   OR LOWER(primary_muscles)   LIKE LOWER(CONCAT('%', :muscle, '%'))
                   OR LOWER(secondary_muscles) LIKE LOWER(CONCAT('%', :muscle, '%')))
            """,
        countQuery = """
            SELECT COUNT(*) FROM exercise_catalog
            WHERE (:level     IS NULL OR level     = :level)
              AND (:category  IS NULL OR category  = :category)
              AND (:equipment IS NULL OR equipment = :equipment)
              AND (:muscle    IS NULL
                   OR LOWER(primary_muscles)   LIKE LOWER(CONCAT('%', :muscle, '%'))
                   OR LOWER(secondary_muscles) LIKE LOWER(CONCAT('%', :muscle, '%')))
            """,
        nativeQuery = true
    )
    Page<ExerciseCatalog> findWithFilters(
        @Param("level")     String level,
        @Param("category")  String category,
        @Param("equipment") String equipment,
        @Param("muscle")    String muscle,
        Pageable pageable
    );
}
