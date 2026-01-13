package ifit.authentication_service.Keycloak.service;

import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;

import ifit.authentication_service.Keycloak.controllers.dto.UserDTO;

public interface IKeycloakService {

    List<UserRepresentation> findAllUsers();
    List<UserRepresentation> searchUserByUsername(String username);
    String createUser(UserDTO userDTO);
    void deleteUser(String userId);
    void updateUser(String userId, UserDTO userDTO);
}
