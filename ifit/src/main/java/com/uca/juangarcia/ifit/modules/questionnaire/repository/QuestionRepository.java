package com.uca.juangarcia.ifit.modules.questionnaire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.questionnaire.model.Question;
import com.uca.juangarcia.ifit.modules.questionnaire.model.QuestionType;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * Find all enabled questions
     */
    List<Question> findByIsEnabledTrue();
    
    /**
     * Find questions by type
     */
    List<Question> findByType(QuestionType type);
    
    /**
     * Find enabled questions by type
     */
    List<Question> findByTypeAndIsEnabledTrue(QuestionType type);
}
