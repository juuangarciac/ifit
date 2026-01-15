package com.uca.juangarcia.ifit.modules.auth.service.impl;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.uca.juangarcia.ifit.exception.KeycloakUserCreationException;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.UserDTO;
import com.uca.juangarcia.ifit.modules.auth.service.IKeycloakService;
import com.uca.juangarcia.ifit.modules.auth.util.KeycloakProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de Keycloak para gestión de usuarios.
 * 
 * Proporciona operaciones CRUD sobre usuarios en Keycloak:
 * - Creación de usuarios con asignación automática de roles
 * - Búsqueda y listado de usuarios
 * - Actualización de datos de usuario
 * - Eliminación de usuarios
 * 
 * @author Juan Garcia
 * @version 2.0
 */
@Service
@Slf4j
public class KeycloakServiceImpl implements IKeycloakService {

    /**
     * Lista todos los usuarios registrados en Keycloak.
     * 
     * @return lista de representaciones de usuarios
     */
    public List<UserRepresentation> findAllUsers() {
        log.debug("Retrieving all users from Keycloak");
        List<UserRepresentation> users = KeycloakProvider.getRealmResource()
                .users()
                .list();
        log.info("Retrieved {} users from Keycloak", users.size());
        return users;
    }

    /**
     * Busca usuarios por nombre de usuario en Keycloak.
     * 
     * @param username nombre de usuario a buscar
     * @return lista de usuarios que coinciden con el criterio de búsqueda
     */
    public List<UserRepresentation> searchUserByUsername(String username) {
        log.debug("Searching user by username: {}", username);
        List<UserRepresentation> users = KeycloakProvider.getRealmResource()
                .users()
                .searchByUsername(username, true);
        log.info("Found {} users matching username: {}", users.size(), username);
        return users;
    }

    /**
     * Crea un nuevo usuario en Keycloak.
     * 
     * Proceso de creación:
     * 1. Crea la representación del usuario en Keycloak
     * 2. Establece la contraseña del usuario
     * 3. Asigna roles (si no se especifican, asigna rol 'user' por defecto)
     * 
     * @param userDTO datos del usuario a crear
     * @return ID del usuario creado en Keycloak
     * @throws KeycloakUserCreationException si el usuario ya existe o hay un error en la creación
     */
    public String createUser(@NonNull UserDTO userDTO) throws KeycloakUserCreationException {
        log.info("Creating user in Keycloak: {}", userDTO.getEmail());
        
        UsersResource usersResource = KeycloakProvider.getUserResource();

        // Preparar representación del usuario
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userDTO.getFirstName());
        userRepresentation.setLastName(userDTO.getLastName());
        userRepresentation.setEmail(userDTO.getEmail());
        userRepresentation.setUsername(userDTO.getUsername());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        // Crear usuario en Keycloak
        Response response = usersResource.create(userRepresentation);
        int status = response.getStatus();

        if (status == 201) {
            // Extraer ID del usuario creado
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);
            log.debug("User created in Keycloak with ID: {}", userId);

            // Establecer contraseña
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);
            log.debug("Password set for user: {}", userId);

            // Asignar roles
            RealmResource realmResource = KeycloakProvider.getRealmResource();
            List<RoleRepresentation> rolesRepresentation;

            if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                // Asignar rol por defecto 'user'
                rolesRepresentation = List.of(realmResource.roles().get("user").toRepresentation());
                log.debug("Assigning default 'user' role to: {}", userId);
            } else {
                // Asignar roles especificados
                rolesRepresentation = realmResource.roles()
                        .list()
                        .stream()
                        .filter(role -> userDTO.getRoles()
                                .stream()
                                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                        .toList();
                log.debug("Assigning custom roles to user: {}", userId);
            }

            realmResource.users().get(userId).roles().realmLevel().add(rolesRepresentation);

            log.info("User created successfully in Keycloak with ID: {}", userId);
            return userId; // ✅ Devolver userId para sincronizar con BD

        } else if (status == 409) {
            log.error("User already exists in Keycloak: {}", userDTO.getEmail());
            throw new KeycloakUserCreationException("User already exists: " + userDTO.getEmail());
        } else {
            log.error("Error creating user in Keycloak. Status: {}", status);
            throw new KeycloakUserCreationException("Error creating user in Keycloak. Status: " + status);
        }
    }

    /**
     * Elimina un usuario de Keycloak.
     * 
     * @param userId ID del usuario a eliminar
     */
    public void deleteUser(String userId) {
        log.info("Deleting user from Keycloak: {}", userId);
        KeycloakProvider.getUserResource()
                .get(userId)
                .remove();
        log.info("User deleted successfully from Keycloak: {}", userId);
    }

    /**
     * Actualiza los datos de un usuario existente en Keycloak.
     * 
     * @param userId ID del usuario a actualizar
     * @param userDTO nuevos datos del usuario
     */
    public void updateUser(String userId, @NonNull UserDTO userDTO) {
        log.info("Updating user in Keycloak: {}", userId);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userDTO.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(Collections.singletonList(credentialRepresentation));

        UserResource usersResource = KeycloakProvider.getUserResource().get(userId);
        usersResource.update(user);
        
        log.info("User updated successfully in Keycloak: {}", userId);
    }
}
