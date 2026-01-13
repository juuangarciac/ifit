package com.uca.juangarcia.ifit.modules.auth.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;
import com.uca.juangarcia.ifit.modules.coach.repository.CoachModelTypeRepository;
import com.uca.juangarcia.ifit.modules.questionnaire.model.Question;
import com.uca.juangarcia.ifit.modules.questionnaire.model.Questionnaire;
import com.uca.juangarcia.ifit.modules.questionnaire.repository.QuestionRepository;
import com.uca.juangarcia.ifit.modules.questionnaire.repository.QuestionnaireRepository;
import com.uca.juangarcia.ifit.modules.user.model.AppRole;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;
import com.uca.juangarcia.ifit.modules.user.repository.AppRoleRepository;
import com.uca.juangarcia.ifit.modules.user.repository.AppUserRepository;
import com.uca.juangarcia.ifit.modules.user.repository.ExperienceLevelRepository;
import com.uca.juangarcia.ifit.shared.exception.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.ExperienceLevelNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.QuestionNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.QuestionnaireNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.UserIdNotFoundException;

/**
 * This class is provided to enhance the security of the application's data.
 * Its methods perform validations and checks on input data against existing
 * database records.
 * 
 * Updated for new questionnaire system with decision tree architecture.
 * 
 * @author Juan Garcia Candon
 */
@Service
public class ValidatorService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private CoachModelTypeRepository coachModelTypeRepository;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private ExperienceLevelRepository experienceLevelRepository;

    @Autowired
    private QuestionRepository questionRepository;

    /**
     * Validates that the given AppUser exists in the database.
     * 
     * @param id User ID
     * @return AppUser entity
     * @throws UserIdNotFoundException if user not found
     * @throws IllegalArgumentException if id is null
     */
    public AppUser validateAppUser(Long id) throws UserIdNotFoundException {
        Assert.notNull(id, "User ID cannot be null");

        return appUserRepository.findById(id)
            .orElseThrow(() -> new UserIdNotFoundException(id));
    }

    /**
     * Validates that the given Question exists in the database.
     * 
     * @param id Question ID
     * @return Question entity
     * @throws QuestionNotFoundException if question not found
     * @throws IllegalArgumentException if id is null
     */
    public Question validateQuestion(Long id) throws QuestionNotFoundException {
        Assert.notNull(id, "Question ID cannot be null");

        return questionRepository.findById(id)
            .orElseThrow(() -> new QuestionNotFoundException(id));
    }

    /**
     * Validates that the given Questionnaire exists in the database.
     * 
     * @param id Questionnaire ID
     * @return Questionnaire entity
     * @throws QuestionnaireNotFoundException if questionnaire not found
     * @throws IllegalArgumentException if id is null
     */
    public Questionnaire validateQuestionnaire(Long id) throws QuestionnaireNotFoundException {
        Assert.notNull(id, "Questionnaire ID cannot be null");

        return questionnaireRepository.findById(id)
            .orElseThrow(() -> new QuestionnaireNotFoundException(id));
    }

    /**
     * Validates that the given AppRole exists in the database.
     * 
     * @param id Role ID
     * @return AppRole entity
     * @throws IllegalArgumentException if role not found or id is null
     */
    public AppRole validateAppRole(Long id) {
        Assert.notNull(id, "Role ID cannot be null");

        Optional<AppRole> response = appRoleRepository.findById(id);
        Assert.isTrue(response.isPresent(), "Role with ID " + id + " does not exist");

        return response.get();
    }

    /**
     * Validates that the given CoachModelType exists in the database.
     * 
     * @param id CoachModelType ID
     * @return CoachModelType entity
     * @throws CoachModelTypeNotFoundException if coach model type not found
     * @throws IllegalArgumentException if id is null
     */
    public CoachModelType validateCoachModelType(Long id) throws CoachModelTypeNotFoundException {
        Assert.notNull(id, "CoachModelType ID cannot be null");

        return coachModelTypeRepository.findById(id)
            .orElseThrow(() -> new CoachModelTypeNotFoundException(id.toString()));
    }

    /**
     * Validates that the given ExperienceLevel exists in the database.
     * 
     * @param id ExperienceLevel ID
     * @return ExperienceLevel entity
     * @throws ExperienceLevelNotFoundException if experience level not found
     * @throws IllegalArgumentException if id is null
     */
    public ExperienceLevel validateExperienceLevel(Long id) throws ExperienceLevelNotFoundException {
        Assert.notNull(id, "ExperienceLevel ID cannot be null");

        return experienceLevelRepository.findById(id)
            .orElseThrow(() -> new ExperienceLevelNotFoundException(id));
    }
}