package com.uca.juangarcia.ifit.modules.exercises.model;

import java.util.List;

import com.uca.juangarcia.ifit.utils.StringListConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa un ejercicio del catálogo de ejercicios disponibles.
 *
 * <p>Catálogo de referencia estático. No tiene relación directa con RoutineExercise,
 * que pertenece al módulo de rutinas de entrenamiento.
 *
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "exercise_catalog")
public class ExerciseCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "force", length = 50)
    private String force;

    @Column(length = 50)
    private String level;

    @Column(length = 50)
    private String mechanic;

    @Column(length = 100)
    private String equipment;

    @Column(name = "primaryMuscles", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> primaryMuscles;

    @Column(name = "secondaryMuscles", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> secondaryMuscles;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> instructions;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> images;

    @Column(length = 255)
    private String code;

    // Constructors

    public ExerciseCatalog() {}

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getForce() { return force; }
    public void setForce(String force) { this.force = force; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMechanic() { return mechanic; }
    public void setMechanic(String mechanic) { this.mechanic = mechanic; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public List<String> getPrimaryMuscles() { return primaryMuscles; }
    public void setPrimaryMuscles(List<String> primaryMuscles) { this.primaryMuscles = primaryMuscles; }

    public List<String> getSecondaryMuscles() { return secondaryMuscles; }
    public void setSecondaryMuscles(List<String> secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
