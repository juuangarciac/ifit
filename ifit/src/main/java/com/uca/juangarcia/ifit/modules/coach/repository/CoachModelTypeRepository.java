package com.uca.juangarcia.ifit.modules.coach.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;

/**
 * Repositorio para la gestión de persistencia de tipos de modelo de coach.
 * 
 * <p>Este repositorio proporciona métodos para realizar operaciones CRUD
 * sobre la entidad {@link CoachModelType}, así como consultas personalizadas
 * para búsqueda y filtrado de modelos.
 * 
 * <p>Extiende {@link JpaRepository} para heredar operaciones básicas de
 * persistencia y aprovechar las capacidades de Spring Data JPA.
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CoachModelTypeRepository extends JpaRepository<CoachModelType, Long> {
    
    /**
     * Busca un tipo de modelo de coach por su nombre.
     * 
     * <p>El nombre debe coincidir exactamente (case-sensitive).
     * 
     * @param name nombre del modelo a buscar
     * @return Optional con el modelo si existe, Optional.empty() si no existe
     */
    Optional<CoachModelType> findByName(String name);
    
    /**
     * Busca todos los tipos de modelo de coach que están habilitados.
     * 
     * <p>Retorna únicamente los modelos con enabled = true, que son los
     * disponibles para asignación a usuarios.
     * 
     * @return lista de modelos habilitados (puede estar vacía)
     */
    List<CoachModelType> findByEnabledTrue();
    
    /**
     * Busca todos los tipos de modelo de coach que están deshabilitados.
     * 
     * <p>Retorna únicamente los modelos con enabled = false.
     * 
     * @return lista de modelos deshabilitados (puede estar vacía)
     */
    List<CoachModelType> findByEnabledFalse();
    
    /**
     * Verifica si existe un tipo de modelo con el nombre especificado.
     * 
     * <p>Este método es útil para validaciones antes de crear o actualizar.
     * 
     * @param name nombre del modelo a verificar
     * @return true si existe un modelo con ese nombre, false en caso contrario
     */
    boolean existsByName(String name);
}