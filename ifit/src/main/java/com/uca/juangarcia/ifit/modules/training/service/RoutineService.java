package com.uca.juangarcia.ifit.modules.training.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uca.juangarcia.ifit.exception.UserIdNotFoundException;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.AnswerDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireResponseSummaryDto;
import com.uca.juangarcia.ifit.modules.questionnaire.service.QuestionnaireService;
import com.uca.juangarcia.ifit.modules.training.client.IFitAIClient;
import com.uca.juangarcia.ifit.modules.training.controller.dto.CreateRoutineRequestDto;
import com.uca.juangarcia.ifit.modules.exercises.service.ExerciseNameResolver;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineDayDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineExerciseDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineResponseDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.UpdateRoutineRequestDto;
import com.uca.juangarcia.ifit.modules.training.model.CoachType;
import com.uca.juangarcia.ifit.exception.RoutineIsActiveException;
import com.uca.juangarcia.ifit.exception.RoutineNotFoundException;
import com.uca.juangarcia.ifit.modules.training.mapper.RoutineDayMapper;
import com.uca.juangarcia.ifit.modules.training.mapper.RoutineMapper;
import com.uca.juangarcia.ifit.modules.training.model.Routine;
import com.uca.juangarcia.ifit.modules.training.model.RoutineDay;
import com.uca.juangarcia.ifit.modules.training.repository.RoutineDayRepository;
import com.uca.juangarcia.ifit.modules.training.repository.RoutineRepository;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.repository.AppUserRepository;

/**
 * Servicio para la gestión de rutinas de entrenamiento.
 *
 * <p>
 * Este servicio proporciona operaciones CRUD y funcionalidades específicas para
 * gestionar rutinas de entrenamiento, incluyendo:
 * <ul>
 * <li>Creación de nuevas rutinas con días y ejercicios</li>
 * <li>Búsqueda y consulta de rutinas por usuario</li>
 * <li>Actualización completa y parcial de rutinas</li>
 * <li>Activación/desactivación de rutinas</li>
 * <li>Eliminación de rutinas</li>
 * </ul>
 *
 * <p>
 * Todas las operaciones de escritura se ejecutan dentro de transacciones para
 * garantizar la integridad de los datos.
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
    private final RoutineDayRepository routineDayRepository;
    private final AppUserRepository userRepository;
    private final RoutineMapper routineMapper;
    private final RoutineDayMapper dayMapper;
    private final QuestionnaireService questionnaireService;
    private final IFitAIClient aiClient;
    private final ExerciseNameResolver exerciseNameResolver;

    /**
     * Constructor con inyección de dependencias.
     */
    public RoutineService(
            RoutineRepository routineRepository,
            RoutineDayRepository routineDayRepository,
            AppUserRepository userRepository,
            RoutineMapper routineMapper,
            RoutineDayMapper dayMapper, QuestionnaireService questionnaireService,
            IFitAIClient aiClient,
            ExerciseNameResolver exerciseNameResolver) {
        this.routineRepository = routineRepository;
        this.routineDayRepository = routineDayRepository;
        this.userRepository = userRepository;
        this.routineMapper = routineMapper;
        this.dayMapper = dayMapper;
        this.questionnaireService = questionnaireService;
        this.aiClient = aiClient;
        this.exerciseNameResolver = exerciseNameResolver;
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
        logger.info("Creating routine for user with ID: {}", requestDto.getUserId());

        // Validar que el usuario existe
        AppUser user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new UserIdNotFoundException(requestDto.getUserId()));

        // Desactivar cualquier rutina activa previa del usuario
        deactivatePreviousActiveRoutine(requestDto.getUserId());

        // Crear la rutina
        Routine routine = new Routine();
        routine.setUser(user);
        routine.setDescription(requestDto.getDescription());
        routine.setTrainingDays(requestDto.getTrainingDays());
        routine.setCurrentDay(1);
        routine.setActive(true);

        // Guardar rutina
        Routine savedRoutine = routineRepository.save(routine);

        // Agregar días con sus ejercicios
        List<RoutineDay> days = new ArrayList<>();
        if (requestDto.getDays() != null) {
            requestDto.getDays().forEach(dayDto -> {
                RoutineDay day = dayMapper.toEntity(dayDto);
                day.setRoutine(savedRoutine); // Establecer la relación con la rutina guardada
                day = routineDayRepository.save(day); 
                days.add(day);
            });
        }
        routine.setDays(days);
        
        // Guardar rutina
        routineRepository.save(routine);

        logger.info("Routine created with ID: {}", savedRoutine.getId());
        return routineMapper.toResponseDto(savedRoutine);
    }

    /**
     * Obtiene todas las rutinas del sistema.
     *
     * @return lista de todas las rutinas
     */
    public List<RoutineResponseDto> findAllRoutines() {
        logger.info("Getting all routines");
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
        logger.info("Getting paginated routines: page={}, size={}",
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
        logger.info("Finding routine with ID: {}", id);

        Routine routine = routineRepository.findByIdWithDaysAndExercises(id)
                .orElseThrow(() -> new RoutineNotFoundException("Routine with ID " + id + " not found"));

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
        logger.info("Finding routines for user with ID: {}", userId);

        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new UserIdNotFoundException(userId);
        }

        List<Routine> routines = routineRepository.findByUserIdAndDeletedFalse(userId);
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
        logger.info("Finding paginated routines for user {}: page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        if (!userRepository.existsById(userId)) {
            throw new UserIdNotFoundException(userId);
        }

        Page<Routine> routinePage = routineRepository.findByUserIdAndDeletedFalse(userId, pageable);
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
        logger.info("Finding active routines for user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserIdNotFoundException(userId);
        }

        List<Routine> routines = routineRepository.findByUserIdAndIsActiveAndDeletedFalse(userId, true);
        return routineMapper.toResponseDtoList(routines);
    }

    /**
     * Actualiza una rutina existente.
     * Soporta actualizaciones parciales (campos nulos no se actualizan).
     * 
     * @param id        identificador de la rutina
     * @param updateDto datos a actualizar
     * @return DTO con los datos actualizados
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @Transactional
    public RoutineResponseDto updateRoutine(Long id, UpdateRoutineRequestDto updateDto)
            throws RoutineNotFoundException {
        logger.info("Updating routine with ID: {}", id);

        Routine routine = routineRepository.findByIdWithDaysAndExercises(id)
                .orElseThrow(() -> new RoutineNotFoundException("Routine with ID " + id + " not found"));

        // Actualizar campos si vienen en el DTO
        if (updateDto.getDescription() != null) {
            routine.setDescription(updateDto.getDescription());
        }

        if (updateDto.getTrainingDays() != null) {
            routine.setTrainingDays(updateDto.getTrainingDays());
        }

        if (updateDto.getIsActive() != null) {
            if (updateDto.getIsActive()) {
                deactivatePreviousActiveRoutine(routine.getUser().getId());
            }
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
        logger.info("Routine updated: {}", id);

        return routineMapper.toResponseDto(updatedRoutine);
    }

    /**
     * Activa o desactiva una rutina.
     * 
     * @param id       identificador de la rutina
     * @param isActive true para activar, false para desactivar
     * @return DTO con los datos actualizados
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @Transactional
    public RoutineResponseDto toggleRoutineActive(Long id, boolean isActive) throws RoutineNotFoundException {
        logger.info("Changing active state of routine {} to: {}", id, isActive);

        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RoutineNotFoundException("Routine with ID " + id + " not found"));

        if (isActive) {
            deactivatePreviousActiveRoutine(routine.getUser().getId());
        }

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
        logger.info("Deleting routine with ID: {}", id);

        if (!routineRepository.existsById(id)) {
            throw new RoutineNotFoundException("Routine with ID " + id + " not found");
        }

        routineRepository.deleteById(id);
        logger.info("Routine deleted: {}", id);
    }

    /**
     * Verifica si una rutina pertenece a un usuario específico.
     * 
     * @param routineId identificador de la rutina
     * @param userId    identificador del usuario
     * @return true si la rutina pertenece al usuario, false en caso contrario
     */
    public boolean routineBelongsToUser(Long routineId, Long userId) {
        return routineRepository.findByIdAndUserIdAndDeletedFalse(routineId, userId).isPresent();
    }

    /**
     * Cuenta las rutinas activas y no eliminadas de un usuario.
     *
     * @param userId identificador del usuario
     * @return número de rutinas activas
     */
    public long countActiveRoutinesByUserId(Long userId) {
        return routineRepository.countByUserIdAndIsActiveAndDeletedFalse(userId, true);
    }

    /**
     * Genera una rutina personalizada basada en las respuestas del cuestionario
     * y el coach seleccionado.
     *
     * @param userId     ID del usuario
     * @param responseId ID de la respuesta del cuestionario completado
     * @param coachType  Coach de IA seleccionado (por defecto MASTER)
     * @return DTO con la rutina generada
     * @throws UserIdNotFoundException si el usuario no existe
     */
    public RoutineResponseDto generateRoutine(Long userId, Long responseId, CoachType coachType, String note) throws UserIdNotFoundException {
        logger.info("Starting routine generation for userId: {}, responseId: {}, coach: {}",
                userId, responseId, coachType);

        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");

        if (responseId == null)
            throw new IllegalArgumentException("Response ID cannot be null");

        CoachType resolvedCoach = coachType != null ? coachType : CoachType.MASTER;

        // 1. Obtener perfil del usuario y respuestas al cuestionario
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException(userId));

        QuestionnaireResponseSummaryDto summary = questionnaireService.getResponseSummary(responseId);

        logger.debug("Retrieved questionnaire summary: {} answers", summary.getAnswers().size());

        // 2. Construir el prompt con el perfil del usuario
        String prompt = buildRoutinePrompt(user, summary, note);

        logger.debug("Prompt built successfully. Length: {} characters, coach: {}",
                prompt.length(), resolvedCoach);

        // 3. Obtener memoryId
        int memoryId = aiClient.getMaxMemoryId().getMaxMemoryId();

        logger.debug("Generated memoryId: {}", memoryId);

        // 4. Llamar al endpoint del coach seleccionado para generar la rutina
        RoutineResponseDto routineResponseDto = aiClient.generateRoutine(memoryId, prompt, user.getKeycloakId(), resolvedCoach);

        routineResponseDto.setUserId(userId);

        // 5. Reconciliar los nombres de ejercicio con el catálogo real para que el
        //    usuario pueda encontrarlos (la IA y el catálogo usan vocabularios distintos).
        normalizeExerciseNames(routineResponseDto);

        logger.info("Routine generated successfully for userId: {}, coach: {}", userId, resolvedCoach);

        return routineResponseDto;
    }

    /**
     * Sustituye cada nombre de ejercicio generado por la IA por su nombre canónico
     * en el catálogo ({@code exercise_catalog}) y enlaza el {@code exerciseId}
     * correspondiente. Si un ejercicio no se puede reconciliar, se conserva el
     * nombre original sin enlace y se registra para diagnóstico.
     *
     * @param routine rutina recién generada (se modifica in situ)
     */
    private void normalizeExerciseNames(RoutineResponseDto routine) {
        if (routine == null || routine.getDays() == null) {
            return;
        }

        int total = 0;
        int linked = 0;
        for (RoutineDayDto day : routine.getDays()) {
            if (day.getExercises() == null) {
                continue;
            }
            for (RoutineExerciseDto exercise : day.getExercises()) {
                total++;
                String rawName = exercise.getExerciseName();
                var match = exerciseNameResolver.resolve(rawName);
                if (match.isPresent()) {
                    String canonical = match.get().canonicalName();
                    if (!canonical.equals(rawName)) {
                        logger.info("Exercise name normalized: '{}' -> '{}'", rawName, canonical);
                    }
                    exercise.setExerciseName(canonical);
                    exercise.setExerciseId(match.get().id());
                    linked++;
                } else {
                    logger.warn("Exercise '{}' not found in catalog; kept as-is without link", rawName);
                }
            }
        }
        logger.info("Catalog reconciliation: {}/{} exercises linked to the catalog", linked, total);
    }

    /**
     * Construye un prompt personalizado basado en el perfil del usuario,
     * las respuestas al cuestionario y la especialidad del coach seleccionado.
     */
    private String buildRoutinePrompt(AppUser user, QuestionnaireResponseSummaryDto summary, String note) {

        StringBuilder sb = new StringBuilder();

        sb.append("PERFIL DEL USUARIO:\n");
        sb.append("Usuario: ").append(user.getName()).append("\n");
        sb.append("Nivel de experiencia: ")
                .append(user.getExperienceLevel().getName())
                .append(" - ")
                .append(user.getExperienceLevel().getDescription())
                .append("\n");
        sb.append("Nivel de catálogo a usar: ")
                .append(resolveCatalogLevel(user.getExperienceLevel().getName()))
                .append("\n");

        sb.append("Cuestionario: ").append(summary.getQuestionnaireName()).append("\n");
        if (summary.getQuestionnaireDescription() != null && !summary.getQuestionnaireDescription().isBlank()) {
            sb.append("Descripción: ").append(summary.getQuestionnaireDescription()).append("\n");
        }
        sb.append("\n");

        sb.append("RESPUESTAS AL CUESTIONARIO:\n");
        for (AnswerDto answer : summary.getAnswers()) {
            sb.append("- ").append(answer.questionText()).append("\n");
            String selectedOption = "Prefiero no responder".equals(answer.selectedOption())
                    ? "[No respondida]"
                    : answer.selectedOption();
            sb.append("  Respuesta: ").append(selectedOption).append("\n");

            if (answer.additionalText() != null && !answer.additionalText().isBlank()) {
                sb.append("  Detalle: ").append(answer.additionalText()).append("\n");
            }
            sb.append("\n");
        }

        if (note != null && !note.isBlank()) {
            sb.append("NOTA DEL USUARIO:\n");
            sb.append(note.trim()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Realiza un soft-delete sobre una rutina, marcándola como eliminada.
     * La rutina deja de ser visible para el usuario pero no se borra de la base de datos.
     * Si era la rutina activa, se desactiva automáticamente.
     *
     * @param id identificador de la rutina
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @Transactional
    public RoutineResponseDto softDeleteRoutine(Long id) throws RoutineNotFoundException, RoutineIsActiveException {
        logger.info("Soft-deleting routine with ID: {}", id);

        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RoutineNotFoundException("Routine with ID " + id + " not found"));

        if (routine.isActive()) {
            throw new RoutineIsActiveException(id);
        }

        routine.setDeleted(true);
        routine.setUpdatedAt(LocalDateTime.now());
        routine = routineRepository.save(routine);

        logger.info("Routine with ID: {} soft-deleted", id);
        return routineMapper.toResponseDto(routine);
    }

    private void deactivatePreviousActiveRoutine(Long userId) {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");
        List<Routine> active = routineRepository.findByUserIdAndIsActiveAndDeletedFalse(userId, true);
        if (!active.isEmpty()) {
            active.forEach(r -> r.setActive(false));
            routineRepository.saveAll(active);
        }
    }

    private String resolveCatalogLevel(String experienceLevelName) {
        if (experienceLevelName == null) return "BEGINNER";
        return switch (experienceLevelName.toLowerCase()) {
            case "intermedio" -> "INTERMEDIATE";
            case "avanzado" -> "ADVANCED";
            default -> "BEGINNER";
        };
    }

    /**
     * Obtiene un día específico de una rutina por su número de día.
     *
     * @param routineId
     * @param day
     * @return
     * @throws RoutineNotFoundException
     */
    public RoutineDayDto getRoutineDayByRoutineIdAndDay(Long routineId, Integer day) throws RoutineNotFoundException {
        logger.info("Finding day {} of routine with ID: {}", day, routineId);

        RoutineDay routineDay = routineDayRepository.findRoutineDayByRoutineIdAndDay(routineId, day)
                .orElseThrow(() -> new RoutineNotFoundException(
                "Day " + day + " of routine with ID " + routineId + " not found"));

        return dayMapper.toDto(routineDay);
    }

    /**
     * Marca un día de una rutina como completado.
     *
     * @param routineId
     * @param day
     * @return
     * @throws RoutineNotFoundException

     */
    @Transactional
    public RoutineResponseDto setRoutineDayAsCompleted(Long routineId, Integer day) throws RoutineNotFoundException {

        logger.info("Marking day {} of routine with ID: {} as completed", day, routineId);
        
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("Routine with ID " + routineId + " not found"));

        RoutineDay routineDay = routineDayRepository.findRoutineDayByRoutineIdAndDay(routineId, day)
                .orElseThrow(() -> new RoutineNotFoundException(
                "Day " + day + " of routine with ID " + routineId + " not found"));

        List<RoutineDay> routineDays = routineDayRepository.findByRoutineId(routineId);

        RoutineDay maxDay = routineDays.get(routineDays.size() - 1);
        
        if (day.equals(maxDay.getDayNumber())) {
            routine.setCurrentDay(1);
        } else {
            routine.setCurrentDay(day + 1);
        }

        routine = routineRepository.save(routine);
        logger.info("Day {} of routine with ID: {} marked as completed. Current day updated to: {}",
                day, routineId, routine.getCurrentDay());

        return routineMapper.toResponseDto(routine);
    }

    /**
     * Desactiva una rutina, marcándola como inactiva en lugar de eliminarla.
     * 
        * @param routineId ID de la rutina a desactivar
        * @return DTO con los datos de la rutina actualizada
        * @throws RoutineNotFoundException si no existe la rutina
     */
    @Transactional
    public RoutineResponseDto setRoutineAsCompleted(Long routineId) 
        throws RoutineNotFoundException {

        logger.info("Deactivating routine with ID: {}", routineId);

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("Routine with ID " + routineId + " not found"));
        
        routine.setActive(false);
        routine.setUpdatedAt(LocalDateTime.now());
        routine = routineRepository.save(routine);

        logger.info("Routine with ID: {} deactivated", routineId);

        return routineMapper.toResponseDto(routine);
    }
}
