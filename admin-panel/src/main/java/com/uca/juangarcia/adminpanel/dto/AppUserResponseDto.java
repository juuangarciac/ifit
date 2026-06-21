package com.uca.juangarcia.adminpanel.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Réplica (lectura) del {@code AppUserResponseDto} de iFit.
 *
 * <p>Se usan {@link JsonAlias} en los booleanos para tolerar tanto la forma
 * {@code verified}/{@code registrationComplete} como {@code isVerified}/{@code isRegistrationComplete},
 * según cómo serialice Jackson los getters {@code isXxx()} del backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AppUserResponseDto(
        Long id,
        String name,
        String email,
        @JsonAlias({"isVerified"}) boolean verified,
        @JsonAlias({"isRegistrationComplete"}) boolean registrationComplete,
        String roleName,
        String coachModelTypeName,
        String experienceLevelName
) implements Serializable {
}
