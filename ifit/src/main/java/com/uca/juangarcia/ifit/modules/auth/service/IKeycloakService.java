package com.uca.juangarcia.ifit.modules.auth.service;

import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;

import com.uca.juangarcia.ifit.modules.auth.controllers.dto.UserDTO;


public interface IKeycloakService {

    List<UserRepresentation> findAllUsers();
    List<UserRepresentation> searchUserByUsername(String username);
    String createUser(UserDTO userDTO);
    void deleteUser(String userId);
    void updateUser(String userId, UserDTO userDTO);
    void markEmailAsVerified(String userId);
}
