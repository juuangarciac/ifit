package com.uca.juangarcia.ifit.junit;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.uca.juangarcia.ifit.exception.dto.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.exception.dto.EmailAlreadyExistsException;
import com.uca.juangarcia.ifit.exception.dto.EmailNotFoundException;
import com.uca.juangarcia.ifit.exception.dto.ExperienceLevelNotFoundException;
import com.uca.juangarcia.ifit.exception.dto.UserIdNotFoundException;
import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;
import com.uca.juangarcia.ifit.modules.coach.service.CoachModelTypeService;
import com.uca.juangarcia.ifit.modules.user.dto.AppUserResponseDto;
import com.uca.juangarcia.ifit.modules.user.dto.CreateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.dto.UpdateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.mapper.AppUserMapper;
import com.uca.juangarcia.ifit.modules.user.model.AppRole;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;
import com.uca.juangarcia.ifit.modules.user.repository.AppUserRepository;
import com.uca.juangarcia.ifit.modules.user.service.AppRoleService;
import com.uca.juangarcia.ifit.modules.user.service.AppUserService;
import com.uca.juangarcia.ifit.modules.user.service.ExperienceLevelService;

/**
 * Tests unitarios para AppUserServiceV2.
 * 
 * <p>Estos tests verifican la lógica de negocio del servicio de usuarios,
 * usando Mockito para simular las dependencias.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppUserService Tests")
class AppUserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private AppRoleService appRoleService;

    @Mock
    private CoachModelTypeService coachModelTypeService;

    @Mock
    private ExperienceLevelService experienceLevelService;

    @Mock
    private AppUserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService userService;

    private AppUser testUser;
    private AppUserResponseDto testUserDto;
    private AppRole testRole;
    private CoachModelType testCoach;
    private ExperienceLevel testLevel;

    @BeforeEach
    void setUp() {
        // Configurar rol de prueba
        testRole = new AppRole();
        testRole.setId(1L);
        testRole.setName("USER");

        // Configurar coach de prueba
        testCoach = new CoachModelType();
        testCoach.setId(1L);
        testCoach.setName("GPT-4");

        // Configurar nivel de experiencia de prueba
        testLevel = new ExperienceLevel();
        testLevel.setId(1L);
        testLevel.setName("INTERMEDIATE");

        // Configurar usuario de prueba
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setName("Juan García");
        testUser.setEmail("juan@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setVerified(false);
        testUser.setIsRegistrationComplete(false);
        testUser.setRole(testRole);

        // Configurar DTO de respuesta de prueba
        testUserDto = new AppUserResponseDto(
            1L,
            "Juan García",
            "juan@example.com",
            false,
            false,
            LocalDateTime.now(),
            null,
            "USER",
            null,
            null
        );

        // Configurar el valor por defecto del rol
        ReflectionTestUtils.setField(userService, "defaultUserRole", "USER");
    }

    @Nested
    @DisplayName("Find All Users Tests")
    class FindAllUsersTests {

        @Test
        @DisplayName("Should return all users successfully")
        void shouldReturnAllUsers() {
            // Given
            List<AppUser> users = Arrays.asList(testUser);
            when(userRepository.findAll()).thenReturn(users);
            when(userMapper.toResponseDto(any(AppUser.class))).thenReturn(testUserDto);

            // When
            List<AppUserResponseDto> result = userService.findAllUsers();

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("juan@example.com");
            verify(userRepository).findAll();
            verify(userMapper).toResponseDto(testUser);
        }

        @Test
        @DisplayName("Should throw exception when no users found")
        void shouldThrowExceptionWhenNoUsersFound() {
            // Given
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> userService.findAllUsers())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No users found");
            
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find All Users Paginated Tests")
    class FindAllUsersPaginatedTests {

        @Test
        @DisplayName("Should return paginated users successfully")
        void shouldReturnPaginatedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<AppUser> usersPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);
            
            when(userRepository.findAll(pageable)).thenReturn(usersPage);
            when(userMapper.toResponseDto(any(AppUser.class))).thenReturn(testUserDto);

            // When
            Page<AppUserResponseDto> result = userService.findAllUsers(pageable);

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("juan@example.com");
            verify(userRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Find User By ID Tests")
    class FindUserByIdTests {

        @Test
        @DisplayName("Should find user by ID successfully")
        void shouldFindUserById() throws UserIdNotFoundException {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponseDto(testUser)).thenReturn(testUserDto);

            // When
            AppUserResponseDto result = userService.findUserById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("juan@example.com");
            verify(userRepository).findById(1L);
            verify(userMapper).toResponseDto(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.findUserById(999L))
                .isInstanceOf(UserIdNotFoundException.class);
            
            verify(userRepository).findById(999L);
        }

        @Test
        @DisplayName("Should throw exception when ID is null")
        void shouldThrowExceptionWhenIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.findUserById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null");
            
            verify(userRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Find User By getEmail Tests")
    class FindUserBygetEmailTests {

        @Test
        @DisplayName("Should find user by email successfully")
        void shouldFindUserByEmail() throws EmailNotFoundException {
            // Given
            when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(testUser));
            when(userMapper.toResponseDto(testUser)).thenReturn(testUserDto);

            // When
            AppUserResponseDto result = userService.findUserByEmail("juan@example.com");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("juan@example.com");
            verify(userRepository).findByEmail("juan@example.com");
            verify(userMapper).toResponseDto(testUser);
        }

        @Test
        @DisplayName("Should throw exception when email not found")
        void shouldThrowExceptionWhenEmailNotFound() {
            // Given
            when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.findUserByEmail("notfound@example.com"))
                .isInstanceOf(EmailNotFoundException.class);
            
            verify(userRepository).findByEmail("notfound@example.com");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.findUserByEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null");
            
            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("Should throw exception when email is blank")
        void shouldThrowExceptionWhenEmailIsBlank() {
            // When & Then
            assertThatThrownBy(() -> userService.findUserByEmail("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null or blank");
            
            verify(userRepository, never()).findByEmail(any());
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() throws EmailAlreadyExistsException {
            // Given
            CreateAppUserRequestDto createDto = new CreateAppUserRequestDto(
                "Juan García",
                "password123",
                "juan@example.com",
                "USER"
            );

            when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.empty());
            when(userMapper.toEntity(createDto)).thenReturn(testUser);
            when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
            when(appRoleService.findRoleByName("USER")).thenReturn(testRole);
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
            when(userMapper.toResponseDto(testUser)).thenReturn(testUserDto);

            // When
            AppUserResponseDto result = userService.createUser(createDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("juan@example.com");
            verify(userRepository).findByEmail("juan@example.com");
            verify(passwordEncoder).encode("password123");
            verify(appRoleService).findRoleByName("USER");
            verify(userRepository).save(any(AppUser.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            CreateAppUserRequestDto createDto = new CreateAppUserRequestDto(
                "Juan García",
                "password123",
                "juan@example.com",
                "USER"
            );

            when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.createUser(createDto))
                .isInstanceOf(EmailAlreadyExistsException.class);
            
            verify(userRepository).findByEmail("juan@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateAppUserRequestDto cannot be null");
            
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() throws UserIdNotFoundException, EmailAlreadyExistsException {
            // Given
            UpdateAppUserRequestDto updateDto = new UpdateAppUserRequestDto(
                "Juan García Pérez",
                "newemail@example.com"
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
            when(userMapper.toResponseDto(testUser)).thenReturn(testUserDto);

            // When
            AppUserResponseDto result = userService.updateUser(1L, updateDto);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(userMapper).updateEntityFromDto(testUser, updateDto);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            UpdateAppUserRequestDto updateDto = new UpdateAppUserRequestDto("New Name", null);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(999L, updateDto))
                .isInstanceOf(UserIdNotFoundException.class);
            
            verify(userRepository).findById(999L);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when new email already exists")
        void shouldThrowExceptionWhenNewEmailExists() {
            // Given
            AppUser existingUser = new AppUser();
            existingUser.setId(2L);
            existingUser.setEmail("existing@example.com");

            UpdateAppUserRequestDto updateDto = new UpdateAppUserRequestDto(null, "existing@example.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(EmailAlreadyExistsException.class);
            
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Set Coach Model Type Tests")
    class SetCoachModelTypeTests {

        @Test
        @DisplayName("Should set coach model type successfully")
        void shouldSetCoachModelTypeSuccessfully() throws CoachModelTypeNotFoundException, UserIdNotFoundException {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            // when(coachModelTypeService.getById(1L)).thenReturn(testCoach);
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
            when(userMapper.toResponseDto(testUser)).thenReturn(testUserDto);

            // When
            AppUserResponseDto result = userService.setCoachModelType(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(coachModelTypeService).getById(1L);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user ID is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.setCoachModelType(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when coach ID is null")
        void shouldThrowExceptionWhenCoachIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.setCoachModelType(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coach ID cannot be null");
        }
    }

    @Nested
    @DisplayName("Set Experience Level Tests")
    class SetExperienceLevelTests {

        @Test
        @DisplayName("Should set experience level successfully")
        void shouldSetExperienceLevelSuccessfully() throws ExperienceLevelNotFoundException, UserIdNotFoundException {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            // when(experienceLevelService.getById(1L)).thenReturn(testLevel);
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
            when(userMapper.toResponseDto(testUser)).thenReturn(testUserDto);

            // When
            AppUserResponseDto result = userService.setExperienceLevel(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(experienceLevelService).getById(1L);
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("Mark Registration Complete Tests")
    class MarkRegistrationCompleteTests {

        @Test
        @DisplayName("Should mark registration as complete successfully")
        void shouldMarkRegistrationCompleteSuccessfully() throws UserIdNotFoundException {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
            when(userMapper.toResponseDto(testUser)).thenReturn(testUserDto);

            // When
            AppUserResponseDto result = userService.markRegistrationComplete(1L);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user ID is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.markRegistrationComplete(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null");
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws UserIdNotFoundException {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userService.deleteUser(1L);

            // Then
            verify(userRepository).findById(1L);
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserIdNotFoundException.class);
            
            verify(userRepository).findById(999L);
            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Exists By Email Tests")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(testUser));

            // When
            boolean result = userService.existsByEmail("juan@example.com");

            // Then
            assertThat(result).isTrue();
            verify(userRepository).findByEmail("juan@example.com");
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            // When
            boolean result = userService.existsByEmail("notfound@example.com");

            // Then
            assertThat(result).isFalse();
            verify(userRepository).findByEmail("notfound@example.com");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            assertThatThrownBy(() -> userService.existsByEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null");
        }
    }
}
