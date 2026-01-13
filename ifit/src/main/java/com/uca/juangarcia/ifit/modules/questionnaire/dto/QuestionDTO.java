package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import java.util.List;

import com.uca.juangarcia.ifit.modules.questionnaire.model.QuestionType;

/**
 * DTO for Question - Used to send question data to the frontend
 */
public class QuestionDTO {
    
    private Long id;
    private String text;
    private QuestionType type;
    private List<OptionDTO> options;
    
    // Constructors
    public QuestionDTO() {
    }
    
    public QuestionDTO(Long id, String text, QuestionType type, List<OptionDTO> options) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String text;
        private QuestionType type;
        private List<OptionDTO> options;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder text(String text) {
            this.text = text;
            return this;
        }
        
        public Builder type(QuestionType type) {
            this.type = type;
            return this;
        }
        
        public Builder options(List<OptionDTO> options) {
            this.options = options;
            return this;
        }
        
        public QuestionDTO build() {
            return new QuestionDTO(id, text, type, options);
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public QuestionType getType() {
        return type;
    }
    
    public void setType(QuestionType type) {
        this.type = type;
    }
    
    public List<OptionDTO> getOptions() {
        return options;
    }
    
    public void setOptions(List<OptionDTO> options) {
        this.options = options;
    }
}
