package com.uca.juangarcia.ifit.exception.dto;

public class UserIdNotFoundException extends Exception {

    private Long userId;

    public UserIdNotFoundException(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String getMessage() {
        return "User with ID " + userId + " not found.";
    }

}
