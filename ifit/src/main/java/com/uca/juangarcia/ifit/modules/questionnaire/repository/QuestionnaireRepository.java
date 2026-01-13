package com.uca.juangarcia.ifit.modules.questionnaire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.questionnaire.model.Questionnaire;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    
    /**
     * Find questionnaire by name
     */
    Optional<Questionnaire> findByName(String name);
    
    /**
     * Find all enabled questionnaires
     */
    List<Questionnaire> findByIsEnabledTrue();
    
    /**
     * Find questionnaire by coach model type and experience level
     */
    @Query("SELECT q FROM Questionnaire q WHERE q.coachModelType.id = :coachModelTypeId " +
           "AND q.experienceLevel.id = :experienceLevelId AND q.isEnabled = true")
    Optional<Questionnaire> findByCoachModelTypeAndExperienceLevel(
        @Param("coachModelTypeId") Long coachModelTypeId,
        @Param("experienceLevelId") Long experienceLevelId
    );
    
    /**
     * Find questionnaires by coach model type
     */
    @Query("SELECT q FROM Questionnaire q WHERE q.coachModelType.id = :coachModelTypeId AND q.isEnabled = true")
    List<Questionnaire> findByCoachModelType(@Param("coachModelTypeId") Long coachModelTypeId);
    
    /**
     * Find questionnaires by experience level
     */
    @Query("SELECT q FROM Questionnaire q WHERE q.experienceLevel.id = :experienceLevelId AND q.isEnabled = true")
    List<Questionnaire> findByExperienceLevel(@Param("experienceLevelId") Long experienceLevelId);
}
