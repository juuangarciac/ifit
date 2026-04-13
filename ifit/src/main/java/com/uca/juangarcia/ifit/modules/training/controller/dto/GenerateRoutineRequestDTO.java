package com.uca.juangarcia.ifit.modules.training.controller.dto;

import com.uca.juangarcia.ifit.modules.training.model.CoachType;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitar la generación de una rutina personalizada.
 *
 * El frontend envía el userId, el responseId del cuestionario completado
 * y opcionalmente el coach seleccionado. Si no se especifica, se usa MASTER.
 */
public class GenerateRoutineRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Response ID is required")
    private Long responseId;

    private CoachType coachType = CoachType.MASTER;

    // Constructors
    public GenerateRoutineRequestDto() {
    }

    public GenerateRoutineRequestDto(Long userId, Long responseId) {
        this.userId = userId;
        this.responseId = responseId;
    }

    public GenerateRoutineRequestDto(Long userId, Long responseId, CoachType coachType) {
        this.userId = userId;
        this.responseId = responseId;
        this.coachType = coachType;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getResponseId() {
        return responseId;
    }

    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    public CoachType getCoachType() {
        return coachType;
    }

    public void setCoachType(CoachType coachType) {
        this.coachType = coachType != null ? coachType : CoachType.MASTER;
    }

    @Override
    public String toString() {
        return "GenerateRoutineRequestDto{" +
                "userId=" + userId +
                ", responseId=" + responseId +
                ", coachType=" + coachType +
                '}';
    }
}