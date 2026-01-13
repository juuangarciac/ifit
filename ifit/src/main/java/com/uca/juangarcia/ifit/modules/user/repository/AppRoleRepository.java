package com.uca.juangarcia.ifit.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.user.model.AppRole;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    public Optional<AppRole> findById(long id);
    public Optional<AppRole> findByName(String name);
}
