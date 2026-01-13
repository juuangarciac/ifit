package com.uca.juangarcia.ifit.modules.questionnaire.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.questionnaire.model.UserAnswer;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    /**
     * Find all answers for a specific response
     */
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.response.id = :responseId ORDER BY ua.answeredAt ASC")
    List<UserAnswer> findByResponseId(@Param("responseId") Long responseId);
    
    /**
     * Find answer by response ID and question ID
     */
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.response.id = :responseId AND ua.question.id = :questionId")
    Optional<UserAnswer> findByResponseIdAndQuestionId(
        @Param("responseId") Long responseId,
        @Param("questionId") Long questionId
    );
    
    /**
     * Find all answers with AI generated descriptions
     */
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.aiGeneratedDescription IS NOT NULL")
    List<UserAnswer> findAllWithAiDescriptions();
    
    /**
     * Count answers for a specific response
     */
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.response.id = :responseId")
    Long countByResponseId(@Param("responseId") Long responseId);
}
