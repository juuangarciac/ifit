package com.uca.juangarcia.ifit.modules.auth.service;

import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginResponseDTO;

public interface IAuthenticationService {
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}
