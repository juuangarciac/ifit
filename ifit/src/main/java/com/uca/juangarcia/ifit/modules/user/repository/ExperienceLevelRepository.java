package com.uca.juangarcia.ifit.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;

@Repository
public interface ExperienceLevelRepository extends JpaRepository<ExperienceLevel, Long> {
    
    public Optional<ExperienceLevel> findById(long id);
    public Optional<ExperienceLevel> findByName(String name);
}
