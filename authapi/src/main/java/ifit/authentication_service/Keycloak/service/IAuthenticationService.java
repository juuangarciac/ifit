package ifit.authentication_service.Keycloak.service;

import ifit.authentication_service.Keycloak.controllers.dto.LoginRequestDTO;
import ifit.authentication_service.Keycloak.controllers.dto.LoginResponseDTO;


public interface IAuthenticationService {
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}
