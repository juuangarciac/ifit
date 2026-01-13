package com.uca.juangarcia.ifit.modules.coach.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;

import jakarta.validation.constraints.NotBlank;

public class CoachModelTypeDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String emojiCharacter;

    @JsonIgnore
    private Boolean is_enable;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;

    public CoachModelTypeDto() {}

    public CoachModelTypeDto(String name, String description, String emojiCharacter, Boolean is_enable,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.emojiCharacter = emojiCharacter;
        this.is_enable = is_enable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public CoachModelTypeDto(CoachModelType coachModelType) {
        if (coachModelType != null) {
            this.id = coachModelType.getId();
            this.name = coachModelType.getName();
            this.description = coachModelType.getDescription();
            this.emojiCharacter = coachModelType.getEmojiCharacter();
            this.is_enable = coachModelType.getEnabled();
            this.createdAt = coachModelType.getCreatedAt();
            this.updatedAt = coachModelType.getUpdatedAt();
        }
    }
    
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmojiCharacter() {
        return emojiCharacter;
    }

    public void setEmojiCharacter(String emojiCharacter) {
        this.emojiCharacter = emojiCharacter;
    }

    public Boolean getIs_enable() {
        return is_enable;
    }

    public void setIs_enable(Boolean is_enable) {
        this.is_enable = is_enable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
