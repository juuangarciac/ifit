package com.uca.juangarcia.adminpanel.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.uca.juangarcia.adminpanel.dto.CreateQuestionnaireRequestDto;
import com.uca.juangarcia.adminpanel.dto.QuestionnaireDto;
import com.uca.juangarcia.adminpanel.dto.QuestionnaireWithFirstQuestionDto;
import com.uca.juangarcia.adminpanel.dto.UpdateQuestionnaireRequestDto;

/**
 * Cliente REST de cuestionarios. Los writes (POST, PUT, DELETE) exigen
 * {@code admin_client_role} en el backend.
 */
@Service
public class QuestionnaireApiClient {

    private static final ParameterizedTypeReference<List<QuestionnaireDto>> QUESTIONNAIRE_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final AuthService auth;

    public QuestionnaireApiClient(RestClient gatewayRestClient, AuthService auth) {
        this.client = gatewayRestClient;
        this.auth = auth;
    }

    /** {@code GET /ifit/api/v1/questionnaires} — todos los cuestionarios. */
    public List<QuestionnaireDto> listAll() {
        return client.get()
                .uri("/ifit/api/v1/questionnaires")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(QUESTIONNAIRE_LIST);
    }

    /** {@code GET /ifit/api/v1/questionnaires/{id}} — obtener cuestionario por ID. */
    public QuestionnaireDto getById(Long id) {
        return client.get()
                .uri("/ifit/api/v1/questionnaires/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(QuestionnaireDto.class);
    }

    /** {@code GET /ifit/api/v1/questionnaires/{id}/with-first-question} — obtener cuestionario con primera pregunta. */
    public QuestionnaireWithFirstQuestionDto getByIdWithFirstQuestion(Long id) {
        return client.get()
                .uri("/ifit/api/v1/questionnaires/{id}/with-first-question", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(QuestionnaireWithFirstQuestionDto.class);
    }

    /** {@code POST /ifit/api/v1/questionnaires} — crear cuestionario (admin). */
    public QuestionnaireDto create(CreateQuestionnaireRequestDto dto) {
        return client.post()
                .uri("/ifit/api/v1/questionnaires")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(QuestionnaireDto.class);
    }

    /** {@code PUT /ifit/api/v1/questionnaires/{id}} — actualizar cuestionario (admin). */
    public QuestionnaireDto update(Long id, UpdateQuestionnaireRequestDto dto) {
        return client.put()
                .uri("/ifit/api/v1/questionnaires/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(QuestionnaireDto.class);
    }

    /** {@code DELETE /ifit/api/v1/questionnaires/{id}} — eliminar cuestionario (admin). */
    public void delete(Long id) {
        client.delete()
                .uri("/ifit/api/v1/questionnaires/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .toBodilessEntity();
    }

    private String bearer() {
        return "Bearer " + auth.getAccessToken();
    }
}
