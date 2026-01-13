package com.uca.juangarcia.ifit.modules.coach.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa los diferentes tipos de modelos de coach de IA
 * disponibles en la aplicación.
 * 
 * <p>Un CoachModelType define un modelo de IA específico que puede ser asignado
 * a los usuarios para proporcionarles coaching personalizado. Cada tipo tiene
 * características únicas en términos de capacidades, personalidad y enfoque.
 * 
 * <p>Ejemplos de tipos de modelo incluyen:
 * <ul>
 *   <li>GPT-4 - Modelo de OpenAI con razonamiento avanzado</li>
 *   <li>Claude - Modelo de Anthropic enfocado en seguridad y precisión</li>
 *   <li>Gemini - Modelo de Google con capacidades multimodales</li>
 * </ul>
 * 
 * <p>Los administradores pueden habilitar o deshabilitar modelos según
 * disponibilidad, costos o políticas de la aplicación.
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "coachmodeltype")
public class CoachModelType {
    
    /**
     * Identificador único del tipo de modelo de coach.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre único del modelo de coach.
     * No puede ser nulo y debe ser único en el sistema.
     * 
     * <p>Ejemplos: "GPT-4", "Claude-3", "Gemini-Pro"
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Descripción detallada del modelo de coach.
     * Explica las capacidades, características y casos de uso del modelo.
     */
    @Column(length = 500)
    private String description;

    /**
     * Emoji o carácter unicode representativo del modelo.
     * Usado en la interfaz de usuario para identificación visual rápida.
     * 
     * <p>Ejemplos: "🤖", "🧠", "⚡"
     */
    @Column(length = 10)
    private String emojiCharacter;

    /**
     * Indica si el modelo está actualmente habilitado y disponible para asignación.
     * Los modelos deshabilitados no pueden ser asignados a nuevos usuarios.
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * Fecha y hora de creación del registro.
     * Se establece automáticamente al crear el tipo de modelo.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización del registro.
     * Se actualiza automáticamente cada vez que se modifica el tipo de modelo.
     */
    @Column
    private LocalDateTime updatedAt;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public CoachModelType() {
    }

    /**
     * Constructor con todos los campos para facilitar la creación de instancias.
     * 
     * @param name nombre único del modelo
     * @param description descripción del modelo
     * @param emojiCharacter emoji representativo
     * @param enabled estado de habilitación
     * @param createdAt fecha de creación
     * @param updatedAt fecha de última actualización
     */
    public CoachModelType(String name, String description, String emojiCharacter, 
                         Boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.emojiCharacter = emojiCharacter;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmojiCharacter() {
        return emojiCharacter;
    }

    public void setEmojiCharacter(String emojiCharacter) {
        this.emojiCharacter = emojiCharacter;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Compara este CoachModelType con otro objeto para determinar igualdad.
     * Dos tipos de modelo son iguales si tienen el mismo ID o el mismo nombre.
     * 
     * @param obj el objeto a comparar
     * @return true si los objetos son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CoachModelType other = (CoachModelType) obj;
        
        // Comparar por ID si ambos están presentes
        if (id != null && other.id != null) {
            return Objects.equals(id, other.id);
        }
        
        // Si no hay IDs, comparar por nombre (único)
        return Objects.equals(name, other.name);
    }

    /**
     * Genera un código hash para este CoachModelType.
     * Basado en el ID si está presente, o en el nombre en caso contrario.
     * 
     * @return el código hash
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(name);
    }

    /**
     * Representación en String del CoachModelType.
     * Útil para logging y debugging.
     * 
     * @return una cadena con la información básica del tipo de modelo
     */
    @Override
    public String toString() {
        return "CoachModelType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                '}';
    }
}
