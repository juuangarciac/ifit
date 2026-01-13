package com.uca.juangarcia.ifit.modules.questionnaire.model;

/**
 * Types of questions supported in the questionnaire system
 */
public enum QuestionType {
    /**
     * Binary question with Yes/No options
     * Example: "Do you have any injuries?"
     */
    BINARY,
    
    /**
     * Multiple choice question with several options
     * Example: "What is your experience level?" (Beginner/Intermediate/Advanced)
     */
    MULTIPLE_CHOICE,
    
    /**
     * Free text input
     * Example: "Describe your fitness goals"
     */
    TEXT_INPUT,
    
    /**
     * Numeric input
     * Example: "How old are you?"
     */
    NUMERIC,
    
    /**
     * Scale from 1 to N
     * Example: "Rate your current fitness level (1-10)"
     */
    SCALE
}
