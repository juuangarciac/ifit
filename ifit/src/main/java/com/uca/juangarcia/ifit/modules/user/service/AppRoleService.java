package com.uca.juangarcia.ifit.modules.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.uca.juangarcia.ifit.modules.user.model.AppRole;
import com.uca.juangarcia.ifit.modules.user.repository.AppRoleRepository;


@Service
public class AppRoleService {

    @Autowired
    private AppRoleRepository appRoleRepository;

    public boolean existsById(long id) {
        Assert.notNull(id, "ID must not be null");
        Assert.isTrue(id > 0, "ID must be greater than zero");
        return appRoleRepository.findById(id).isPresent();
    }

    public AppRole findRoleByName(String name) {
        Assert.notNull(name, "Name must not be null");
        Assert.hasText(name, "Name must not be empty");
        return appRoleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role with name '" + name + "' does not exist"));
    }
}   
