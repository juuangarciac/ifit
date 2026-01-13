package com.uca.juangarcia.ifit.modules.questionnaire.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;
import com.uca.juangarcia.ifit.modules.coach.repository.CoachModelTypeRepository;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.AnswerDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.CreateQuestionnaireRequestDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.OptionDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireResponseDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireResponseSummaryDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireSummaryDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireWithFirstQuestionDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.UpdateQuestionnaireRequestDto;
import com.uca.juangarcia.ifit.modules.questionnaire.mapper.QuestionnaireMapper;
import com.uca.juangarcia.ifit.modules.questionnaire.model.Question;
import com.uca.juangarcia.ifit.modules.questionnaire.model.QuestionOption;
import com.uca.juangarcia.ifit.modules.questionnaire.model.Questionnaire;
import com.uca.juangarcia.ifit.modules.questionnaire.model.QuestionnaireResponse;
import com.uca.juangarcia.ifit.modules.questionnaire.model.UserAnswer;
import com.uca.juangarcia.ifit.modules.questionnaire.repository.QuestionOptionRepository;
import com.uca.juangarcia.ifit.modules.questionnaire.repository.QuestionRepository;
import com.uca.juangarcia.ifit.modules.questionnaire.repository.QuestionnaireRepository;
import com.uca.juangarcia.ifit.modules.questionnaire.repository.QuestionnaireResponseRepository;
import com.uca.juangarcia.ifit.modules.questionnaire.repository.UserAnswerRepository;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;
import com.uca.juangarcia.ifit.modules.user.repository.AppUserRepository;
import com.uca.juangarcia.ifit.modules.user.repository.ExperienceLevelRepository;
import com.uca.juangarcia.ifit.shared.exception.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.ExperienceLevelNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.QuestionNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.QuestionnaireNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.UserIdNotFoundException;

/**
 * Servicio para gestionar cuestionarios.
 * 
 * <p>Este servicio maneja:
 * <ul>
 *   <li>CRUD de cuestionarios (crear, leer, actualizar, eliminar)</li>
 *   <li>Iniciar sesiones de cuestionarios para usuarios</li>
 *   <li>Responder preguntas y navegar por el árbol de decisión</li>
 *   <li>Obtener resúmenes de respuestas de usuarios</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 3.0
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
public class QuestionnaireService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireService.class);
    
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionnaireResponseRepository responseRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final AppUserRepository userRepository;
    private final CoachModelTypeRepository coachModelTypeRepository;
    private final ExperienceLevelRepository experienceLevelRepository;
    private final QuestionnaireMapper questionnaireMapper;

    /**
     * Constructor con inyección de dependencias.
     */
    public QuestionnaireService(
            QuestionnaireRepository questionnaireRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            QuestionnaireResponseRepository responseRepository,
            UserAnswerRepository userAnswerRepository,
            AppUserRepository userRepository,
            CoachModelTypeRepository coachModelTypeRepository,
            ExperienceLevelRepository experienceLevelRepository,
            QuestionnaireMapper questionnaireMapper) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.responseRepository = responseRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userRepository = userRepository;
        this.coachModelTypeRepository = coachModelTypeRepository;
        this.experienceLevelRepository = experienceLevelRepository;
        this.questionnaireMapper = questionnaireMapper;
    }
    
    // ========================================================================
    // CRUD OPERATIONS - Questionnaire
    // ========================================================================
    
    /**
     * Obtiene todos los cuestionarios habilitados (versión compacta).
     * 
     * @return Lista de DTOs de resumen de cuestionarios
     */
    public List<QuestionnaireSummaryDto> getAllQuestionnaires() {
        logger.debug("Finding all enabled questionnaires");
        
        List<Questionnaire> questionnaires = questionnaireRepository.findByIsEnabledTrue();
        
        logger.info("Found {} enabled questionnaires", questionnaires.size());
        
        return questionnaires.stream()
                .map(questionnaireMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un cuestionario por su ID (versión completa).
     * 
     * @param id ID del cuestionario
     * @return DTO completo del cuestionario
     * @throws QuestionnaireNotFoundException si no se encuentra el cuestionario
     */
    public QuestionnaireDTO getQuestionnaireById(Long id) throws QuestionnaireNotFoundException {
        logger.debug("Finding questionnaire by id: {}", id);
        
        Questionnaire questionnaire = questionnaireRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Questionnaire not found with id: {}", id);
                return new QuestionnaireNotFoundException("Questionnaire not found with id: " + id);
            });
        
        logger.info("Found questionnaire: {}", questionnaire.getName());
        
        return questionnaireMapper.toDto(questionnaire);
    }
    
    /**
     * Obtiene un cuestionario con su primera pregunta incluida.
     * Útil para iniciar un cuestionario reduciendo llamadas a la API.
     * 
     * @param id ID del cuestionario
     * @return DTO con cuestionario y primera pregunta
     * @throws QuestionnaireNotFoundException si no se encuentra el cuestionario
     */
    public QuestionnaireWithFirstQuestionDto getQuestionnaireWithFirstQuestion(Long id) 
            throws QuestionnaireNotFoundException {
        logger.debug("Finding questionnaire with first question, id: {}", id);
        
        Questionnaire questionnaire = questionnaireRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Questionnaire not found with id: {}", id);
                return new QuestionnaireNotFoundException("Questionnaire not found with id: " + id);
            });
        
        if (questionnaire.getFirstQuestion() == null) {
            logger.error("Questionnaire {} has no first question configured", id);
            throw new IllegalStateException("Questionnaire has no first question configured");
        }
        
        QuestionDTO firstQuestionDto = toQuestionDTO(questionnaire.getFirstQuestion());
        
        return questionnaireMapper.toWithFirstQuestionDto(questionnaire, firstQuestionDto);
    }
    
    /**
     * Crea un nuevo cuestionario.
     * 
     * @param dto DTO con los datos del nuevo cuestionario
     * @return DTO del cuestionario creado
     * @throws CoachModelTypeNotFoundException si el coach especificado no existe
     * @throws ExperienceLevelNotFoundException si el nivel especificado no existe
     * @throws QuestionNotFoundException si la primera pregunta especificada no existe
     */
    @Transactional
    public QuestionnaireDTO createQuestionnaire(CreateQuestionnaireRequestDto dto) 
            throws CoachModelTypeNotFoundException, ExperienceLevelNotFoundException, QuestionNotFoundException {
        logger.debug("Creating new questionnaire: {}", dto.name());
        
        // Convertir DTO a Entity
        Questionnaire questionnaire = questionnaireMapper.toEntity(dto);
        
        // Asignar relaciones si los IDs no son null
        if (dto.coachModelTypeId() != null) {
            CoachModelType coach = coachModelTypeRepository.findById(dto.coachModelTypeId())
                .orElseThrow(() -> {
                    logger.error("Coach model type not found with id: {}", dto.coachModelTypeId());
                    return new CoachModelTypeNotFoundException(
                        "Coach model type not found with id: " + dto.coachModelTypeId());
                });
            questionnaire.setCoachModelType(coach);
        }
        
        if (dto.experienceLevelId() != null) {
            ExperienceLevel level = experienceLevelRepository.findById(dto.experienceLevelId())
                .orElseThrow(() -> {
                    logger.error("Experience level not found with id: {}", dto.experienceLevelId());
                    return new ExperienceLevelNotFoundException(
                        "Experience level not found with id: " + dto.experienceLevelId());
                });
            questionnaire.setExperienceLevel(level);
        }
        
        if (dto.firstQuestionId() != null) {
            Question question = questionRepository.findById(dto.firstQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException(dto.firstQuestionId()));
            questionnaire.setFirstQuestion(question);
        }
        
        // Guardar
        Questionnaire saved = questionnaireRepository.save(questionnaire);
        
        logger.info("Questionnaire created successfully: {} (ID: {})", saved.getName(), saved.getId());
        
        return questionnaireMapper.toDto(saved);
    }
    
    /**
     * Actualiza un cuestionario existente.
     * 
     * @param id ID del cuestionario a actualizar
     * @param dto DTO con los nuevos datos
     * @return DTO del cuestionario actualizado
     * @throws QuestionnaireNotFoundException si no se encuentra el cuestionario
     */
    @Transactional
    public QuestionnaireDTO updateQuestionnaire(Long id, UpdateQuestionnaireRequestDto dto) 
            throws QuestionnaireNotFoundException, CoachModelTypeNotFoundException, 
                   ExperienceLevelNotFoundException, QuestionNotFoundException {
        logger.debug("Updating questionnaire with id: {}", id);
        
        Questionnaire questionnaire = questionnaireRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Questionnaire not found for update with id: {}", id);
                return new QuestionnaireNotFoundException("Questionnaire not found with id: " + id);
            });
        
        // Actualizar campos básicos
        questionnaireMapper.updateEntityFromDto(questionnaire, dto);
        
        // Actualizar relaciones si están presentes en el DTO
        if (dto.coachModelTypeId() != null) {
            CoachModelType coach = coachModelTypeRepository.findById(dto.coachModelTypeId())
                .orElseThrow(() -> {
                    logger.error("Coach model type not found with id: {}", dto.coachModelTypeId());
                    return new CoachModelTypeNotFoundException(
                        "Coach model type not found with id: " + dto.coachModelTypeId());
                });
            questionnaire.setCoachModelType(coach);
        }
        
        if (dto.experienceLevelId() != null) {
            ExperienceLevel level = experienceLevelRepository.findById(dto.experienceLevelId())
                .orElseThrow(() -> {
                    logger.error("Experience level not found with id: {}", dto.experienceLevelId());
                    return new ExperienceLevelNotFoundException(
                        "Experience level not found with id: " + dto.experienceLevelId());
                });
            questionnaire.setExperienceLevel(level);
        }
        
        if (dto.firstQuestionId() != null) {
            Question question = questionRepository.findById(dto.firstQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException(dto.firstQuestionId()));
            questionnaire.setFirstQuestion(question);
        }
        
        // Guardar (updatedAt se actualiza automáticamente)
        Questionnaire updated = questionnaireRepository.save(questionnaire);
        
        logger.info("Questionnaire updated successfully: {}", updated.getName());
        
        return questionnaireMapper.toDto(updated);
    }
    
    /**
     * Elimina un cuestionario.
     * 
     * @param id ID del cuestionario a eliminar
     * @throws QuestionnaireNotFoundException si no se encuentra el cuestionario
     */
    @Transactional
    public void deleteQuestionnaire(Long id) throws QuestionnaireNotFoundException {
        logger.debug("Deleting questionnaire with id: {}", id);
        
        if (!questionnaireRepository.existsById(id)) {
            logger.error("Questionnaire not found for deletion with id: {}", id);
            throw new QuestionnaireNotFoundException("Questionnaire not found with id: " + id);
        }
        
        questionnaireRepository.deleteById(id);
        
        logger.info("Questionnaire deleted successfully: id={}", id);
    }
    
    // ========================================================================
    // QUESTIONNAIRE SESSION OPERATIONS - User Responses
    // ========================================================================
    
    /**
     * Inicia un nuevo cuestionario para un usuario.
     * Crea una sesión de QuestionnaireResponse y devuelve la primera pregunta.
     * 
     * @param userId ID del usuario que inicia el cuestionario
     * @param questionnaireId ID del cuestionario a iniciar
     * @return DTO con la primera pregunta
     * @throws UserIdNotFoundException si no se encuentra el usuario
     * @throws QuestionnaireNotFoundException si no se encuentra el cuestionario
     */
    @Transactional
    public QuestionnaireResponseDTO startQuestionnaire(Long userId, Long questionnaireId) 
            throws UserIdNotFoundException, QuestionnaireNotFoundException {
        logger.debug("Starting questionnaire {} for user {}", questionnaireId, userId);
        
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
            .orElseThrow(() -> {
                logger.error("Questionnaire not found with id: {}", questionnaireId);
                return new QuestionnaireNotFoundException("Questionnaire not found with id: " + questionnaireId);
            });
        
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found with id: {}", userId);
                return new UserIdNotFoundException(userId);
            });
        
        if (questionnaire.getFirstQuestion() == null) {
            logger.error("Questionnaire {} has no first question", questionnaireId);
            throw new IllegalStateException("Questionnaire has no first question configured");
        }
        
        // Crear nueva sesión
        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setUser(user);
        response.setQuestionnaire(questionnaire);
        response.setIsCompleted(false);
        response.setIsActive(true);
        
        responseRepository.save(response);
        
        logger.info("Questionnaire session started: responseId={}, user={}, questionnaire={}", 
                    response.getId(), user.getEmail(), questionnaire.getName());
        
        return QuestionnaireResponseDTO.builder()
            .responseId(response.getId())
            .currentQuestion(toQuestionDTO(questionnaire.getFirstQuestion()))
            .isCompleted(false)
            .totalQuestionsAnswered(0)
            .build();
    }
    
    /**
     * Registra la respuesta del usuario a una pregunta y devuelve la siguiente pregunta.
     * 
     * @param responseId ID de la sesión de cuestionario
     * @param questionId ID de la pregunta actual
     * @param selectedOptionId ID de la opción seleccionada
     * @param additionalText Texto adicional (opcional)
     * @return DTO con la siguiente pregunta o indicación de finalización
     */
    @Transactional
    public QuestionnaireResponseDTO answerQuestion(
        Long responseId,
        Long questionId,
        Long selectedOptionId,
        String additionalText
    ) {
        logger.debug("Answering question {} in response {}", questionId, responseId);
        
        QuestionnaireResponse response = responseRepository.findById(responseId)
            .orElseThrow(() -> {
                logger.error("Questionnaire response not found with id: {}", responseId);
                return new RuntimeException("Questionnaire response not found with id: " + responseId);
            });
        
        if (response.getIsCompleted()) {
            logger.warn("Attempted to answer completed questionnaire: responseId={}", responseId);
            throw new IllegalStateException("Questionnaire is already completed");
        }
        
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> {
                logger.error("Question not found with id: {}", questionId);
                return new RuntimeException("Question not found with id: " + questionId);
            });
        
        QuestionOption selectedOption = questionOptionRepository.findById(selectedOptionId)
            .orElseThrow(() -> {
                logger.error("Option not found with id: {}", selectedOptionId);
                return new RuntimeException("Option not found with id: " + selectedOptionId);
            });
        
        // Validar que la opción pertenece a la pregunta
        if (!selectedOption.getQuestion().getId().equals(questionId)) {
            logger.error("Option {} does not belong to question {}", selectedOptionId, questionId);
            throw new IllegalArgumentException("Selected option does not belong to this question");
        }
        
        // Validar texto adicional si es requerido
        if (selectedOption.getRequiresTextInput() && 
            (additionalText == null || additionalText.trim().isEmpty())) {
            logger.warn("Additional text required but not provided for option {}", selectedOptionId);
            throw new IllegalArgumentException("Additional text is required for this option");
        }
        
        // Guardar respuesta del usuario
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setResponse(response);
        userAnswer.setQuestion(question);
        userAnswer.setSelectedOption(selectedOption);
        userAnswer.setAdditionalText(additionalText);
        
        userAnswerRepository.save(userAnswer);
        
        Long totalAnswered = userAnswerRepository.countByResponseId(responseId);
        
        logger.info("Answer recorded: responseId={}, questionId={}, optionId={}, totalAnswered={}", 
                    responseId, questionId, selectedOptionId, totalAnswered);
        
        // Navegar a la siguiente pregunta
        Question nextQuestion = selectedOption.getNextQuestion();
        
        if (nextQuestion == null) {
            // Cuestionario completado
            response.markAsCompleted();
            responseRepository.save(response);
            
            logger.info("Questionnaire completed: responseId={}, totalQuestions={}", 
                        responseId, totalAnswered);
            
            return QuestionnaireResponseDTO.builder()
                .responseId(response.getId())
                .currentQuestion(null)
                .isCompleted(true)
                .totalQuestionsAnswered(totalAnswered.intValue())
                .build();
        }
        
        // Devolver siguiente pregunta
        return QuestionnaireResponseDTO.builder()
            .responseId(response.getId())
            .currentQuestion(toQuestionDTO(nextQuestion))
            .isCompleted(false)
            .totalQuestionsAnswered(totalAnswered.intValue())
            .build();
    }
    
    /**
     * Obtiene el resumen completo de las respuestas de un cuestionario.
     * 
     * @param responseId ID de la sesión de cuestionario
     * @return DTO con resumen completo de respuestas
     */
    public QuestionnaireResponseSummaryDTO getResponseSummary(Long responseId) {
        logger.debug("Getting response summary for responseId: {}", responseId);
        
        QuestionnaireResponse response = responseRepository.findById(responseId)
            .orElseThrow(() -> {
                logger.error("Questionnaire response not found with id: {}", responseId);
                return new RuntimeException("Questionnaire response not found with id: " + responseId);
            });
        
        List<UserAnswer> answers = userAnswerRepository.findByResponseId(responseId);
        
        logger.info("Retrieved response summary: responseId={}, totalAnswers={}", 
                    responseId, answers.size());
        
        return QuestionnaireResponseSummaryDTO.builder()
            .responseId(response.getId())
            .userId(response.getUser().getId())
            .userName(response.getUser().getEmail())
            .questionnaireId(response.getQuestionnaire().getId())
            .questionnaireName(response.getQuestionnaire().getName())
            .questionnaireDescription(response.getQuestionnaire().getDescription())
            .answers(answers.stream()
                .map(this::toAnswerDTO)
                .collect(Collectors.toList()))
            .startedAt(response.getStartedAt())
            .completedAt(response.getCompletedAt())
            .isCompleted(response.getIsCompleted())
            .build();
    }
    
    /**
     * Obtiene todas las respuestas de cuestionarios de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de respuestas del usuario
     */
    public List<QuestionnaireResponse> getUserResponses(Long userId) {
        logger.debug("Getting all responses for user: {}", userId);
        
        List<QuestionnaireResponse> responses = responseRepository.findByUserId(userId);
        
        logger.info("Found {} responses for user {}", responses.size(), userId);
        
        return responses;
    }

    /**
     * Obtiene todas las respuestas completadas de cuestionarios de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de respuestas completadas
     */
    public List<QuestionnaireResponse> getUserCompletedResponses(Long userId) {
        logger.debug("Getting completed responses for user: {}", userId);
        
        List<QuestionnaireResponse> responses = responseRepository.findCompletedByUserId(userId);
        
        logger.info("Found {} completed responses for user {}", responses.size(), userId);
        
        return responses;
    }
    
    /**
     * Obtiene todas las respuestas activas (no completadas) de cuestionarios de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de respuestas activas
     */
    public List<QuestionnaireResponse> getUserActiveResponses(Long userId) {
        logger.debug("Getting active responses for user: {}", userId);
        
        List<QuestionnaireResponse> responses = responseRepository.findActiveByUserId(userId);
        
        logger.info("Found {} active responses for user {}", responses.size(), userId);
        
        return responses;
    }
    
    // ========================================================================
    // HELPER METHODS - Convert entities to DTOs
    // ========================================================================
    
    private QuestionDTO toQuestionDTO(Question question) {
        return QuestionDTO.builder()
            .id(question.getId())
            .text(question.getText())
            .type(question.getType())
            .options(question.getOptions().stream()
                .map(this::toOptionDTO)
                .collect(Collectors.toList()))
            .build();
    }
    
    private OptionDTO toOptionDTO(QuestionOption option) {
        return OptionDTO.builder()
            .id(option.getId())
            .text(option.getText())
            .requiresTextInput(option.getRequiresTextInput())
            .textInputPrompt(option.getTextInputPrompt())
            .textInputPlaceholder(option.getTextInputPlaceholder())
            .build();
    }
    
    private AnswerDTO toAnswerDTO(UserAnswer answer) {
        return AnswerDTO.builder()
            .answerId(answer.getId())
            .questionText(answer.getQuestion().getText())
            .selectedOption(answer.getSelectedOption() != null ? 
                answer.getSelectedOption().getText() : null)
            .additionalText(answer.getAdditionalText())
            .aiDescription(answer.getAiGeneratedDescription())
            .answeredAt(answer.getAnsweredAt())
            .build();
    }
}