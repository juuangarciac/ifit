package com.uca.juangarcia.ifit.modules.training.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uca.juangarcia.ifit.exception.dto.UserIdNotFoundException;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.AnswerDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireResponseSummaryDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.service.QuestionnaireService;
import com.uca.juangarcia.ifit.modules.training.client.RonnieClient;
import com.uca.juangarcia.ifit.modules.training.controller.dto.CreateRoutineRequestDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineResponseDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.UpdateRoutineRequestDto;
import com.uca.juangarcia.ifit.modules.training.exception.RoutineNotFoundException;
import com.uca.juangarcia.ifit.modules.training.mapper.RoutineDayMapper;
import com.uca.juangarcia.ifit.modules.training.mapper.RoutineMapper;
import com.uca.juangarcia.ifit.modules.training.model.Routine;
import com.uca.juangarcia.ifit.modules.training.model.RoutineDay;
import com.uca.juangarcia.ifit.modules.training.repository.RoutineRepository;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.repository.AppUserRepository;

/**
 * Servicio para la gestión de rutinas de entrenamiento.
 * 
 * <p>Este servicio proporciona operaciones CRUD y funcionalidades específicas
 * para gestionar rutinas de entrenamiento, incluyendo:
 * <ul>
 *   <li>Creación de nuevas rutinas con días y ejercicios</li>
 *   <li>Búsqueda y consulta de rutinas por usuario</li>
 *   <li>Actualización completa y parcial de rutinas</li>
 *   <li>Activación/desactivación de rutinas</li>
 *   <li>Eliminación de rutinas</li>
 * </ul>
 * 
 * <p>Todas las operaciones de escritura se ejecutan dentro de transacciones
 * para garantizar la integridad de los datos.
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
public class RoutineService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutineService.class);
    
    private final RoutineRepository routineRepository;
    private final AppUserRepository userRepository;
    private final RoutineMapper routineMapper;
    private final RoutineDayMapper dayMapper;
    private final QuestionnaireService questionnaireService;
    private final RonnieClient ronnieClient;

    /**
     * Constructor con inyección de dependencias.
     */
    public RoutineService(
            RoutineRepository routineRepository,
            AppUserRepository userRepository,
            RoutineMapper routineMapper,
            RoutineDayMapper dayMapper
            ,QuestionnaireService questionnaireService,
            RonnieClient ronnieClient) {
        this.routineRepository = routineRepository;
        this.userRepository = userRepository;
        this.routineMapper = routineMapper;
        this.dayMapper = dayMapper;
        this.questionnaireService = questionnaireService;
        this.ronnieClient = ronnieClient;
    }
    
    /**
     * Crea una nueva rutina de entrenamiento.
     * 
     * @param requestDto datos de la rutina a crear
     * @return DTO con los datos de la rutina creada
     * @throws UserIdNotFoundException si el usuario no existe
     */
    @Transactional
    public RoutineResponseDto createRoutine(CreateRoutineRequestDto requestDto) throws UserIdNotFoundException {
        logger.info("Creando rutina para usuario con ID: {}", requestDto.getUserId());
        
        // Validar que el usuario existe
        AppUser user = userRepository.findById(requestDto.getUserId())
            .orElseThrow(() -> new UserIdNotFoundException(requestDto.getUserId()));
        
        // Crear la rutina
        Routine routine = new Routine();
        routine.setUser(user);
        routine.setDescription(requestDto.getDescription());
        routine.setTrainingDays(requestDto.getTrainingDays());
        routine.setActive(true);
        
        // Agregar días con sus ejercicios
        if (requestDto.getDays() != null) {
            requestDto.getDays().forEach(dayDto -> {
                RoutineDay day = dayMapper.toEntity(dayDto);
                routine.addDay(day);
            });
        }
        
        // Guardar rutina
        Routine savedRoutine = routineRepository.save(routine);
        
        logger.info("Rutina creada con ID: {}", savedRoutine.getId());
        return routineMapper.toResponseDto(savedRoutine);
    }
    
    /**
     * Obtiene todas las rutinas del sistema.
     * 
     * @return lista de todas las rutinas
     */
    public List<RoutineResponseDto> findAllRoutines() {
        logger.info("Obteniendo todas las rutinas");
        List<Routine> routines = routineRepository.findAll();
        return routineMapper.toResponseDtoList(routines);
    }
    
    /**
     * Obtiene rutinas con paginación.
     * 
     * @param pageable configuración de paginación
     * @return página de rutinas
     */
    public Page<RoutineResponseDto> findAllRoutines(Pageable pageable) {
        logger.info("Obteniendo rutinas paginadas: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        Page<Routine> routinePage = routineRepository.findAll(pageable);
        return routineMapper.toResponseDtoPage(routinePage);
    }
    
    /**
     * Obtiene una rutina por su ID.
     * 
     * @param id identificador de la rutina
     * @return DTO con los datos de la rutina
     * @throws RoutineNotFoundException si no existe la rutina
     */
    public RoutineResponseDto findRoutineById(Long id) throws RoutineNotFoundException {
        logger.info("Buscando rutina con ID: {}", id);
        
        Routine routine = routineRepository.findByIdWithDaysAndExercises(id)
            .orElseThrow(() -> new RoutineNotFoundException("Rutina con ID " + id + " no encontrada"));
        
        return routineMapper.toResponseDto(routine);
    }
    
    /**
     * Obtiene todas las rutinas de un usuario específico.
     * 
     * @param userId identificador del usuario
     * @return lista de rutinas del usuario
     * @throws UserIdNotFoundException si el usuario no existe
     */
    public List<RoutineResponseDto> findRoutinesByUserId(Long userId) throws UserIdNotFoundException {
        logger.info("Buscando rutinas del usuario con ID: {}", userId);
        
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        
        List<Routine> routines = routineRepository.findByUserId(userId);
        return routineMapper.toResponseDtoList(routines);
    }
    
    /**
     * Obtiene rutinas de un usuario con paginación.
     * 
     * @param userId identificador del usuario
     * @param pageable configuración de paginación
     * @return página de rutinas del usuario
     * @throws UserIdNotFoundException si el usuario no existe
     */
    public Page<RoutineResponseDto> findRoutinesByUserId(Long userId, Pageable pageable) 
            throws UserIdNotFoundException {
        logger.info("Buscando rutinas paginadas del usuario {}: page={}, size={}", 
                   userId, pageable.getPageNumber(), pageable.getPageSize());
        
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        
        Page<Routine> routinePage = routineRepository.findByUserId(userId, pageable);
        return routineMapper.toResponseDtoPage(routinePage);
    }
    
    /**
     * Obtiene las rutinas activas de un usuario.
     * 
     * @param userId identificador del usuario
     * @return lista de rutinas activas
     * @throws UserIdNotFoundException si el usuario no existe
     */
    public List<RoutineResponseDto> findActiveRoutinesByUserId(Long userId) throws UserIdNotFoundException {
        logger.info("Buscando rutinas activas del usuario con ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        
        List<Routine> routines = routineRepository.findByUserIdAndIsActive(userId, true);
        return routineMapper.toResponseDtoList(routines);
    }
    
    /**
     * Actualiza una rutina existente.
     * Soporta actualizaciones parciales (campos nulos no se actualizan).
     * 
     * @param id identificador de la rutina
     * @param updateDto datos a actualizar
     * @return DTO con los datos actualizados
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @Transactional
    public RoutineResponseDto updateRoutine(Long id, UpdateRoutineRequestDto updateDto) 
            throws RoutineNotFoundException {
        logger.info("Actualizando rutina con ID: {}", id);
        
        Routine routine = routineRepository.findByIdWithDaysAndExercises(id)
            .orElseThrow(() -> new RoutineNotFoundException("Rutina con ID " + id + " no encontrada"));
        
        // Actualizar campos si vienen en el DTO
        if (updateDto.getDescription() != null) {
            routine.setDescription(updateDto.getDescription());
        }
        
        if (updateDto.getTrainingDays() != null) {
            routine.setTrainingDays(updateDto.getTrainingDays());
        }
        
        if (updateDto.getIsActive() != null) {
            routine.setActive(updateDto.getIsActive());
        }
        
        // Si se envían días, reemplazar completamente los días existentes
        if (updateDto.getDays() != null) {
            // Limpiar días existentes
            routine.getDays().clear();
            
            // Agregar nuevos días
            updateDto.getDays().forEach(dayDto -> {
                RoutineDay day = dayMapper.toEntity(dayDto);
                routine.addDay(day);
            });
        }
        
        routine.setUpdatedAt(LocalDateTime.now());
        
        Routine updatedRoutine = routineRepository.save(routine);
        logger.info("Rutina actualizada: {}", id);
        
        return routineMapper.toResponseDto(updatedRoutine);
    }
    
    /**
     * Activa o desactiva una rutina.
     * 
     * @param id identificador de la rutina
     * @param isActive true para activar, false para desactivar
     * @return DTO con los datos actualizados
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @Transactional
    public RoutineResponseDto toggleRoutineActive(Long id, boolean isActive) throws RoutineNotFoundException {
        logger.info("Cambiando estado activo de rutina {} a: {}", id, isActive);
        
        Routine routine = routineRepository.findById(id)
            .orElseThrow(() -> new RoutineNotFoundException("Rutina con ID " + id + " no encontrada"));
        
        routine.setActive(isActive);
        routine.setUpdatedAt(LocalDateTime.now());
        
        Routine updatedRoutine = routineRepository.save(routine);
        return routineMapper.toResponseDto(updatedRoutine);
    }
    
    /**
     * Elimina una rutina del sistema.
     * 
     * @param id identificador de la rutina a eliminar
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @Transactional
    public void deleteRoutine(Long id) throws RoutineNotFoundException {
        logger.info("Eliminando rutina con ID: {}", id);
        
        if (!routineRepository.existsById(id)) {
            throw new RoutineNotFoundException("Rutina con ID " + id + " no encontrada");
        }
        
        routineRepository.deleteById(id);
        logger.info("Rutina eliminada: {}", id);
    }
    
    /**
     * Verifica si una rutina pertenece a un usuario específico.
     * 
     * @param routineId identificador de la rutina
     * @param userId identificador del usuario
     * @return true si la rutina pertenece al usuario, false en caso contrario
     */
    public boolean routineBelongsToUser(Long routineId, Long userId) {
        return routineRepository.findByIdAndUserId(routineId, userId).isPresent();
    }
    
    /**
     * Cuenta las rutinas activas de un usuario.
     * 
     * @param userId identificador del usuario
     * @return número de rutinas activas
     */
    public long countActiveRoutinesByUserId(Long userId) {
        return routineRepository.countByUserIdAndIsActive(userId, true);
    }

        /**
     * Genera una rutina personalizada basada en las respuestas del cuestionario.
     * 
     * @param userId ID del usuario (usado para memoryId en Ronnie)
     * @param responseId ID de la respuesta del cuestionario completado
     * @return JSON string con la rutina generada por Ronnie
     * @throws RuntimeException si hay error obteniendo el resumen o generando la rutina
     */
    public String generateRoutine(String userId, Long responseId) {
        logger.info("Starting routine generation for userId: {}, responseId: {}", userId, responseId);
        
        // 1. Obtener resumen del cuestionario
        QuestionnaireResponseSummaryDTO summary = questionnaireService.getResponseSummary(responseId);
        
        logger.debug("Retrieved questionnaire summary: {} answers", summary.getAnswers().size());
        
        // 2. Construir el prompt personalizado
        String prompt = buildRoutinePrompt(summary);
        
        logger.debug("Prompt built successfully. Length: {} characters", prompt.length());
        
        // 3. Generar memoryId a partir del userId
        // Convertir el userId (String) a un int para usarlo como memoryId
        int memoryId = generateMemoryId(userId);
        
        logger.debug("Generated memoryId: {} from userId: {}", memoryId, userId);
        
        // 4. Llamar a Ronnie para generar la rutina
        String routineJson = ronnieClient.generateRoutine(memoryId, prompt);
        
        logger.info("Routine generated successfully for userId: {}", userId);
        
        return routineJson;
    }
    
    /**
     * Construye un prompt detallado para el coach de IA basado en las respuestas del cuestionario.
     * 
     * Este método replica la lógica del método BuildRoutinePrompt de C#, adaptado a Java.
     * El prompt incluye:
     * - Perfil del usuario
     * - Todas las respuestas del cuestionario con detalles
     * - Instrucciones específicas para generar la rutina
     * 
     * @param summary Resumen completo de respuestas del cuestionario
     * @return String con el prompt formateado
     */
    private String buildRoutinePrompt(QuestionnaireResponseSummaryDTO summary) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // Introducción del prompt
        promptBuilder.append("Por favor, genera una rutina de entrenamiento personalizada basada en la siguiente información del usuario:\n");
        promptBuilder.append("\n");
        
        // Información del usuario del cuestionario
        promptBuilder.append("=== PERFIL DEL USUARIO ===\n");
        promptBuilder.append("Usuario: ").append(summary.getUserName()).append("\n");
        promptBuilder.append("Cuestionario: ").append(summary.getQuestionnaireName()).append("\n");
        promptBuilder.append("\n");
        
        // Respuestas del cuestionario
        promptBuilder.append("=== RESPUESTAS AL CUESTIONARIO ===\n");
        for (AnswerDTO answer : summary.getAnswers()) {
            promptBuilder.append("• ").append(answer.getQuestionText()).append("\n");
            promptBuilder.append("  Respuesta: ").append(answer.getSelectedOption()).append("\n");
            
            // Incluir texto adicional si existe
            if (answer.getAdditionalText() != null && !answer.getAdditionalText().isBlank()) {
                promptBuilder.append("  Detalles: ").append(answer.getAdditionalText()).append("\n");
            }
            
            promptBuilder.append("\n");
        }
        
        // Instrucciones específicas para el coach
        promptBuilder.append("=== INSTRUCCIONES ===\n");
        promptBuilder.append("Con base en esta información, por favor genera una rutina que incluya:\n");
        promptBuilder.append("1. Planificación semanal completa (días de entrenamiento y descanso)\n");
        promptBuilder.append("2. Ejercicios específicos para cada día\n");
        promptBuilder.append("3. Series, repeticiones y/o duración de cada ejercicio\n");
        promptBuilder.append("4. Consideraciones especiales (calentamiento, enfriamiento, progresión)\n");
        promptBuilder.append("5. Recomendaciones adicionales según el perfil del usuario\n");
        promptBuilder.append("\n");
        promptBuilder.append("Adapta la rutina al nivel de experiencia, objetivos y disponibilidad del usuario.\n");
        
        return promptBuilder.toString();
    }
    
    /**
     * Genera un memoryId único basado en el userId.
     * 
     * El memoryId se usa en Ronnie para mantener el contexto de conversación.
     * Usamos el hashCode del userId para generar un int único pero reproducible.
     * 
     * @param userId ID del usuario (String)
     * @return int que representa el memoryId
     */
    private int generateMemoryId(String userId) {
        // Usar hashCode() para convertir el String userId a int
        // Esto garantiza que el mismo userId siempre genere el mismo memoryId
        return Math.abs(userId.hashCode());
    }
}
