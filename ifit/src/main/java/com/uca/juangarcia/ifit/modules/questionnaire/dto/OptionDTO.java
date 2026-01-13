package com.uca.juangarcia.ifit.modules.questionnaire.dto;

/**
 * DTO for QuestionOption - Represents an answer option for a question
 */
public class OptionDTO {
    
    private Long id;
    private String text;
    private Boolean requiresTextInput;
    private String textInputPrompt;
    private String textInputPlaceholder;
    
    // Constructors
    public OptionDTO() {
    }
    
    public OptionDTO(Long id, String text, Boolean requiresTextInput, 
                    String textInputPrompt, String textInputPlaceholder) {
        this.id = id;
        this.text = text;
        this.requiresTextInput = requiresTextInput;
        this.textInputPrompt = textInputPrompt;
        this.textInputPlaceholder = textInputPlaceholder;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String text;
        private Boolean requiresTextInput;
        private String textInputPrompt;
        private String textInputPlaceholder;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder text(String text) {
            this.text = text;
            return this;
        }
        
        public Builder requiresTextInput(Boolean requiresTextInput) {
            this.requiresTextInput = requiresTextInput;
            return this;
        }
        
        public Builder textInputPrompt(String textInputPrompt) {
            this.textInputPrompt = textInputPrompt;
            return this;
        }
        
        public Builder textInputPlaceholder(String textInputPlaceholder) {
            this.textInputPlaceholder = textInputPlaceholder;
            return this;
        }
        
        public OptionDTO build() {
            return new OptionDTO(id, text, requiresTextInput, textInputPrompt, textInputPlaceholder);
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
    
    public Boolean getRequiresTextInput() {
        return requiresTextInput;
    }
    
    public void setRequiresTextInput(Boolean requiresTextInput) {
        this.requiresTextInput = requiresTextInput;
    }
    
    public String getTextInputPrompt() {
        return textInputPrompt;
    }
    
    public void setTextInputPrompt(String textInputPrompt) {
        this.textInputPrompt = textInputPrompt;
    }
    
    public String getTextInputPlaceholder() {
        return textInputPlaceholder;
    }
    
    public void setTextInputPlaceholder(String textInputPlaceholder) {
        this.textInputPlaceholder = textInputPlaceholder;
    }
}
