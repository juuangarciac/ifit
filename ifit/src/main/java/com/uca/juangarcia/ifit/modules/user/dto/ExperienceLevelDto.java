package com.uca.juangarcia.ifit.modules.user.dto;

import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ExperienceLevelDto {

    @NotNull
    private long id;

    @Size(min = 5, max = 30)
    @NotBlank 
    private String name;

    private String description;

    public ExperienceLevelDto() {}

    public ExperienceLevelDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ExperienceLevelDto(ExperienceLevel experienceLevel){
        this.id = experienceLevel.getId();
        this.name = experienceLevel.getName();
        this.description = experienceLevel.getDescription();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {   
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

    
}
