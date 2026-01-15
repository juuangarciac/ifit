package com.uca.juangarcia.ifit.modules.user.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(columnDefinition="boolean default false")
    private boolean isRegistrationComplete = false;

    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private String verificationCode;

    @Column(columnDefinition = "boolean default false")
    private boolean isVerified = false;

    @Column
    private LocalDateTime verificationCodeExpiresAt;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private AppRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachmodeltype_id", nullable = true)
    private CoachModelType coachModelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiencelevel_id", nullable = true)
    private ExperienceLevel experienceLevel;

    public AppUser() {}

    public AppUser(String name, String password, String email, boolean isRegistrationComplete, String keycloakId, LocalDateTime createdAt,
            LocalDateTime updatedAt, String verificationCode, boolean isVerified,
            LocalDateTime verificationCodeExpiresAt, AppRole role, CoachModelType coachModelType,
            ExperienceLevel experienceLevel) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.isRegistrationComplete = isRegistrationComplete;
        this.keycloakId = keycloakId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.verificationCode = verificationCode;
        this.isVerified = isVerified;
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
        this.role = role;
        this.coachModelType = coachModelType;
        this.experienceLevel = experienceLevel;
    }

    public AppUser(AppUser user){
        this.id = user.getId();
        this.setName(user.getName());
        this.setPassword(user.getPassword());
        this.setEmail(user.getEmail());
        this.setIsRegistrationComplete(user.isRegistrationComplete());
        this.setKeycloakId(user.getKeycloakId());
        this.setCreatedAt(user.getCreatedAt());
        this.setUpdatedAt(user.getUpdatedAt());
        this.setRole(user.getRole());
        this.setCoachModelType(user.getCoachModelType());
        this.setExperienceLevel(user.getExperienceLevel());
    }
    
    public void setId(long l) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public boolean isRegistrationComplete() {
        return isRegistrationComplete;
    }

    public void setIsRegistrationComplete(boolean isRegistrationComplete) {
        this.isRegistrationComplete = isRegistrationComplete;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKeycloakId() {
        return keycloakId;
    }
    
    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
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

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public LocalDateTime getVerificationCodeExpiresAt() {
        return verificationCodeExpiresAt;
    }

    public void setVerificationCodeExpiresAt(LocalDateTime verificationCodeExpiresAt) {
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }

    public AppRole getRole() {
        return role;
    }

    public void setRole(AppRole role) {
        this.role = role;
    }

    public CoachModelType getCoachModelType() {
        return coachModelType;
    }

    public void setCoachModelType(CoachModelType coachModelType) {
        this.coachModelType = coachModelType;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppUser other = (AppUser) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        else if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AppUser [id=" + id + ", name=" + name + ", password=" + password + ", email=" + email + ", createdAt="
                + createdAt + ", updatedAt=" + updatedAt + ", role=" + role + ", coachModelType=" + coachModelType
                + "]";
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(role)
                .stream()
                .map(r -> (GrantedAuthority) () -> r.getName())
                .collect(Collectors.toSet());
    }    
}
