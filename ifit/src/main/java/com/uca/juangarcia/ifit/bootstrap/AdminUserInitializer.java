package com.uca.juangarcia.ifit.bootstrap;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uca.juangarcia.ifit.modules.user.model.AppRole;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.repository.AppUserRepository;
import com.uca.juangarcia.ifit.modules.user.service.AppRoleService;

/**
 * Bootstrap idempotente del usuario administrador.
 *
 * <p>Como el entorno de desarrollo usa {@code spring.jpa.hibernate.ddl-auto=create-drop},
 * la tabla {@code user} se vacía en cada arranque. Este runner garantiza que, tras el
 * arranque, exista un administrador coherente con Keycloak (mismo {@code keycloak_id})
 * y con rol {@code ROLE_ADMIN} en la base de datos local, sin necesidad de re-registrar
 * y promover manualmente cada vez.
 *
 * <p>La autenticación se valida contra Keycloak (no contra {@code user.password}), por lo
 * que la contraseña almacenada aquí es solo un valor de relleno: el usuario inicia sesión
 * con sus credenciales de Keycloak. El usuario debe existir previamente en Keycloak con el
 * realm role {@code admin} (cuyo composite aporta {@code admin_client_role}).
 *
 * <p>Se puede desactivar con {@code ifit.bootstrap.admin.enabled=false} (recomendado en
 * producción, donde el aprovisionamiento de administradores debe hacerse por otra vía).
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Component
@Order(1)
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

    private final AppUserRepository userRepository;
    private final AppRoleService appRoleService;
    private final PasswordEncoder passwordEncoder;

    @Value("${ifit.bootstrap.admin.enabled:true}")
    private boolean enabled;

    @Value("${ifit.bootstrap.admin.email:adminifit96@gmail.com}")
    private String adminEmail;

    @Value("${ifit.bootstrap.admin.keycloak-id:f7918823-e99e-4b65-bbad-5834cc6890af}")
    private String adminKeycloakId;

    @Value("${ifit.bootstrap.admin.name:iFit Admin}")
    private String adminName;

    public AdminUserInitializer(AppUserRepository userRepository,
                                AppRoleService appRoleService,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.appRoleService = appRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            log.info("[admin-bootstrap] Disabled (ifit.bootstrap.admin.enabled=false) - skipping.");
            return;
        }

        AppRole adminRole = appRoleService.findRoleByName(ADMIN_ROLE_NAME);

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                existing -> ensureAdmin(existing, adminRole),
                () -> createAdmin(adminRole));
    }

    /**
     * Asegura que un usuario existente tenga rol admin y el keycloak_id correcto (idempotente).
     */
    private void ensureAdmin(AppUser user, AppRole adminRole) {
        boolean changed = false;

        if (user.getRole() == null || !ADMIN_ROLE_NAME.equals(user.getRole().getName())) {
            user.setRole(adminRole);
            changed = true;
        }
        if (user.getKeycloakId() == null || !user.getKeycloakId().equals(adminKeycloakId)) {
            user.setKeycloakId(adminKeycloakId);
            changed = true;
        }

        if (changed) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("[admin-bootstrap] Existing user '{}' promoted/synced to {}.", adminEmail, ADMIN_ROLE_NAME);
        } else {
            log.info("[admin-bootstrap] Admin '{}' already present and coherent - no changes.", adminEmail);
        }
    }

    /**
     * Crea el usuario administrador desde cero (caso típico tras un create-drop).
     */
    private void createAdmin(AppRole adminRole) {
        AppUser admin = new AppUser();
        admin.setName(adminName);
        admin.setEmail(adminEmail);
        // La contraseña local no se usa para login (lo hace Keycloak); valor de relleno.
        admin.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        admin.setKeycloakId(adminKeycloakId);
        admin.setRole(adminRole);
        admin.setVerified(true);
        admin.setIsRegistrationComplete(true);
        admin.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);
        log.info("[admin-bootstrap] Admin user '{}' created with role {} (keycloakId={}).",
                adminEmail, ADMIN_ROLE_NAME, adminKeycloakId);
    }
}
