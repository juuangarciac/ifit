package com.uca.juangarcia.ifit.modules.questionnaire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.questionnaire.model.QuestionnaireResponse;

@Repository
public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    
    /**
     * Find all responses by user ID
     */
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId ORDER BY qr.startedAt DESC")
    List<QuestionnaireResponse> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find all completed responses by user ID
     */
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId AND qr.isCompleted = true ORDER BY qr.completedAt DESC")
    List<QuestionnaireResponse> findCompletedByUserId(@Param("userId") Long userId);
    
    /**
     * Find active (incomplete) responses by user ID
     */
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId AND qr.isCompleted = false AND qr.isActive = true ORDER BY qr.startedAt DESC")
    List<QuestionnaireResponse> findActiveByUserId(@Param("userId") Long userId);
    
    /**
     * Find response by user ID and questionnaire ID
     */
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId AND qr.questionnaire.id = :questionnaireId ORDER BY qr.startedAt DESC")
    List<QuestionnaireResponse> findByUserIdAndQuestionnaireId(
        @Param("userId") Long userId,
        @Param("questionnaireId") Long questionnaireId
    );
    
    /**
     * Find latest completed response for a user and questionnaire
     */
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.user.id = :userId " +
           "AND qr.questionnaire.id = :questionnaireId AND qr.isCompleted = true " +
           "ORDER BY qr.completedAt DESC")
    Optional<QuestionnaireResponse> findLatestCompletedByUserAndQuestionnaire(
        @Param("userId") Long userId,
        @Param("questionnaireId") Long questionnaireId
    );
    
    /**
     * Find all responses by questionnaire ID
     */
    @Query("SELECT qr FROM QuestionnaireResponse qr WHERE qr.questionnaire.id = :questionnaireId ORDER BY qr.startedAt DESC")
    List<QuestionnaireResponse> findByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}
