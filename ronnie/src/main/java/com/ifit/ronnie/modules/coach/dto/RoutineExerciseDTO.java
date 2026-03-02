package com.ifit.ronnie.modules.coach.dto;

public class RoutineExerciseDTO {
    private String exerciseName;

    private Integer sets;

    private String reps;

    private Integer restSeconds;

    private String notes;

    private Integer orderIndex;

    public RoutineExerciseDTO() {
    }

    public RoutineExerciseDTO(String exerciseName, Integer sets, String reps, Integer restSeconds, String notes,
            Integer orderIndex) {
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.notes = notes;
        this.orderIndex = orderIndex;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public Integer getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(Integer restSeconds) {
        this.restSeconds = restSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
