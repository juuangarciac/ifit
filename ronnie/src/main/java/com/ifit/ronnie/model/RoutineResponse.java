package com.ifit.ronnie.model;

public class RoutineResponse {
    private String message;
    private Routine routine;

    public RoutineResponse() {}

    public RoutineResponse(String message, Routine routine) {
        this.message = message;
        this.routine = routine;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Routine getRoutine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }
}
