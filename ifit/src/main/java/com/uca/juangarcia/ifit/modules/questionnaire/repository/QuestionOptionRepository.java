package com.uca.juangarcia.ifit.modules.questionnaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.questionnaire.model.QuestionOption;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    
    /**
     * Find options by question ID, ordered by displayOrder
     */
    @Query("SELECT qo FROM QuestionOption qo WHERE qo.question.id = :questionId ORDER BY qo.displayOrder ASC")
    List<QuestionOption> findByQuestionIdOrderByDisplayOrderAsc(@Param("questionId") Long questionId);
    
    /**
     * Find options that require text input
     */
    List<QuestionOption> findByRequiresTextInputTrue();
}
