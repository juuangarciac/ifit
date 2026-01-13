package com.uca.juangarcia.ifit.modules.user.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AppUserDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Size(min = 3, max = 30)
    @NotBlank 
    private String name;

    @Size(min = 8, max = 20)
    @NotBlank 
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Email
    @NotBlank 
    private String email;

    private boolean isRegistrationComplete;

    private boolean isVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long appRoleId;

    private Long coachModelTypeId;

    private Long experienceLevelId;


    public AppUserDto() {}

    public AppUserDto(@Size(min = 5, max = 30) @NotBlank String name, 
            @Size(min = 8, max = 20) @NotBlank String password, @Email @NotBlank String email, boolean isRegistrationComplete, boolean isVerified,
            LocalDateTime createdAt, LocalDateTime updatedAt, Long appRoleId, Long coachModelTypeId,
            Long experienceLevelId) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.isRegistrationComplete = isRegistrationComplete;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.appRoleId = appRoleId;
        this.coachModelTypeId = coachModelTypeId;
        this.experienceLevelId = experienceLevelId;
    }

    public AppUserDto(AppUser appUser){
        this.id = appUser.getId();
        this.name = appUser.getName();
        this.password = appUser.getPassword();
        this.email = appUser.getEmail();
        this.isRegistrationComplete = appUser.isRegistrationComplete();
        this.isVerified = appUser.isVerified();
        this.createdAt = appUser.getCreatedAt();
        this.updatedAt = appUser.getUpdatedAt();

        if (appUser.getRole() != null) {
            this.appRoleId = appUser.getRole().getId();
        }

        if (appUser.getCoachModelType() != null) {
            this.coachModelTypeId = appUser.getCoachModelType().getId();
        }

        if (appUser.getExperienceLevel() != null) {
            this.experienceLevelId = appUser.getExperienceLevel().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRegistrationComplete() {
        return isRegistrationComplete;
    }

    public void setRegistrationComplete(boolean isRegistrationComplete) {
        this.isRegistrationComplete = isRegistrationComplete;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
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

    public Long getAppRoleId() {
        return appRoleId;
    }

    public void setAppRoleId(Long appRoleId) {
        this.appRoleId = appRoleId;
    }

    public Long getCoachModelTypeId() {
        return coachModelTypeId;
    }

    public void setCoachModelTypeId(Long coachModelTypeId) {
        this.coachModelTypeId = coachModelTypeId;
    }

    public Long getExperienceLevelId() {
        return experienceLevelId;
    }

    public void setExperienceLevelId(Long experienceLevelId) {
        this.experienceLevelId = experienceLevelId;
    }
}
