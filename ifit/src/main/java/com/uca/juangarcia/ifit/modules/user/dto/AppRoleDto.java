package com.uca.juangarcia.ifit.modules.user.dto;

import com.uca.juangarcia.ifit.modules.user.model.AppRole;

public class AppRoleDto {
    private long id;
    private String name;

    public AppRoleDto() {
    }

    public AppRoleDto(AppRole role) {
        this.id = role.getId();
        this.name = role.getName();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}