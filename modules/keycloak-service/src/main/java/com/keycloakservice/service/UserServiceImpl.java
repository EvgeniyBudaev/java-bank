package com.keycloakservice.service;

import com.keycloakservice.controller.dto.request.RequestUserCreateDto;
import com.keycloakservice.controller.dto.request.RequestUserUpdateDto;
import com.keycloakservice.controller.dto.response.ResponseUserDetailDto;
import com.keycloakservice.controller.dto.response.ResponseUserDto;
import com.keycloakservice.shared.exception.InternalServerException;
import com.keycloakservice.shared.exception.NotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.keycloak.admin.client.Keycloak;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    @Value("${app.keycloak.realm}")
    private String realm;
    private final Keycloak keycloak;
    @Value("${app.keycloak.defaultUserGroup}")
    private String defaultUserGroup;
    @Value("${app.keycloak.defaultUserRole}")
    private String defaultUserRole;
    @Value("${app.keycloak.defaultUserRoleType}")
    private String defaultUserRoleType;

    @PostConstruct
    public void testConnection() {
        try {
            var realms = keycloak.realms().findAll();
            log.info("✅ Keycloak connection successful. Realms count: {}", realms.size());
        } catch (Exception e) {
            log.error("❌ Keycloak connection failed: {}", e.getMessage());
        }
    }

    @Override
    public ResponseUserDto createUser(RequestUserCreateDto dto) {
        UsersResource usersResource = getUsersResource();

        // 1. Проверка: существует ли пользователь с таким username
        List<UserRepresentation> existingUsers = usersResource.searchByUsername(dto.username(), true);
        if (!existingUsers.isEmpty()) {
            log.warn("⚠️ User '{}' already exists", dto.username());
            throw new RuntimeException("User with username '" + dto.username() + "' already exists");
        }

        // 2. Создание пользователя
        UserRepresentation userRepresentation = getUserRepresentation(dto);
        Response response = usersResource.create(userRepresentation);
        log.info("Create user status code: {}", response.getStatus());

        if (response.getStatus() == 409) {
            throw new RuntimeException("Conflict: User with this email or username already exists");
        }

        if (!Objects.equals(201, response.getStatus())) {
            throw new RuntimeException("Failed to create user. Status code: " + response.getStatus());
        }

        // 3. Получение созданного пользователя (поиск по username, т.к. ID генерируется внутри)
        List<UserRepresentation> createdUsers = usersResource.searchByUsername(dto.username(), true);

        if (createdUsers.isEmpty()) {
            throw new RuntimeException("User created but not found in search");
        }
        UserRepresentation user = createdUsers.get(0);
        UserResource userResource = usersResource.get(user.getId());
        log.info("✅ New user has been created with ID: {}", user.getId());

        // 4. Назначение группы
        assignGroup(userResource, defaultUserGroup);

        // 5. Назначение роли
        assignRole(userResource, defaultUserRole, defaultUserRoleType);

        // 6. Формирование ответа
        ResponseUserDto responseUserDto = new ResponseUserDto();
        responseUserDto.setId(user.getId());
        responseUserDto.setUsername(user.getUsername());
        responseUserDto.setFirstName(user.getFirstName());
        responseUserDto.setLastName(user.getLastName());
        responseUserDto.setEmail(user.getEmail());
        responseUserDto.setIsEnabled(user.isEnabled());
        responseUserDto.setIsEmailVerified(user.isEmailVerified());

        return responseUserDto;
    }

    @Override
    public UserRepresentation updateUser(RequestUserUpdateDto dto) {
        UsersResource usersResource = getUsersResource();
        UserResource userResource = usersResource.get(dto.id());

        try {
            // Проверка существования пользователя перед обновлением
            userResource.toRepresentation();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw new NotFoundException("User not found", "User with id '" + dto.id() + "' does not exist");
            }
            throw new InternalServerException("Failed to check user existence", "Keycloak error: " + e.getMessage());
        }

        // Обновление данных
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        // Важно: обновляем только переданные поля, не перезаписывая весь объект целиком без проверки
        userResource.update(user);

        return userResource.toRepresentation();
    }

    @Override
    public void deleteUser(String userId) {
        UsersResource usersResource = getUsersResource();
        try {
            // Проверяем существование перед удалением (опционально, но надежнее)
            UserResource userResource = usersResource.get(userId);
            userResource.toRepresentation(); // Если пользователя нет, здесь упадет ошибка

            usersResource.delete(userId);
            log.info("User deleted: {}", userId);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw new NotFoundException("User not found", "User with id '" + userId + "' does not exist");
            }
            throw new InternalServerException("Failed to delete user", "Keycloak error: " + e.getMessage());
        }
    }

    @Override
    public UserResource getUser(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }

    @Override
    public UserRepresentation getUserDetailAll(String userId) {
        UsersResource usersResource = getUsersResource();
        UserResource userResource = usersResource.get(userId);
        try {
            return userResource.toRepresentation();
        } catch (Exception e) {
            // Проверяем, является ли ошибкой 404 от Keycloak
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                log.warn("User not found: {}", userId);
                throw new NotFoundException("User not found", "User with id '" + userId + "' does not exist");
            }
            // Иначе пробрасываем как внутреннюю ошибку
            throw new InternalServerException("Failed to retrieve user", "Keycloak error: " + e.getMessage());
        }
    }

    @Override
    public ResponseUserDetailDto getUserDetail(String userId) {
        UsersResource usersResource = getUsersResource();
        UserResource userResource = usersResource.get(userId);

        try {
            // 1. Получаем представление пользователя с ролями
            UserRepresentation user = userResource.toRepresentation(true);

            // 2. Отдельно получаем группы
            List<GroupRepresentation> groups = userResource.groups();

            // 3. Получаем роли (явно запрашиваем realm-роли)
            List<RoleRepresentation> roles = userResource.roles().realmLevel().listAll();

            // 4. Маппинг на DTO
            ResponseUserDetailDto dto = new ResponseUserDetailDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());

            // Преобразуем объекты ролей в список имен
            if (roles != null) {
                dto.setRoles(roles.stream()
                        .map(RoleRepresentation::getName)
                        .toList());
            } else {
                dto.setRoles(Collections.emptyList());
            }

            // Преобразуем объекты групп в список имен
            if (groups != null) {
                dto.setGroups(groups.stream()
                        .map(GroupRepresentation::getName)
                        .toList());
            } else {
                dto.setGroups(Collections.emptyList());
            }

            return dto;

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                log.warn("User not found: {}", userId);
                throw new NotFoundException(
                        "User not found",
                        "User with id '" + userId + "' does not exist"
                );
            }
            throw new InternalServerException(
                    "Failed to retrieve user",
                    "Keycloak error: " + e.getMessage()
            );
        }
    }

    @Override
    public void sendVerificationEmail(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.get(userId).sendVerifyEmail();
    }

    @Override
    public void forgotPassword(String username) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(username, true);
        UserRepresentation userRepresentation = userRepresentations.get(0);
        UserResource userResource = usersResource.get(userRepresentation.getId());
        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    @Override
    public List<RoleRepresentation> getUserRoles(String userId) {
        return getUser(userId).roles().realmLevel().listAll();
    }

    @Override
    public List<GroupRepresentation> getUserGroups(String userId) {
        return getUser(userId).groups();
    }

    private static UserRepresentation getUserRepresentation(RequestUserCreateDto dto) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(dto.username());
        userRepresentation.setEmail(dto.email());
        userRepresentation.setEmailVerified(false);
        userRepresentation.setFirstName(dto.firstName());
        userRepresentation.setLastName(dto.lastName());

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(dto.password());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        userRepresentation.setCredentials(List.of(credentialRepresentation));
        return userRepresentation;
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(realm).users();
    }

    private void assignGroup(UserResource userResource, String groupId) {
        try {
            userResource.joinGroup(groupId);
            log.info("✅ User joined group: {}", groupId);
        } catch (Exception e) {
            log.warn("⚠️ Failed to join group '{}': {}", groupId, e.getMessage());
            // Можно выбросить BusinessException, если группа критична
        }
    }

    private void assignRole(UserResource userResource, String roleName, String roleType) {
        try {
            if ("client".equalsIgnoreCase(roleType)) {
                log.warn("Client role assignment requires clientId configuration");
                return;
            }

            RoleRepresentation role = keycloak.realm(realm)
                    .roles()
                    .get(roleName)
                    .toRepresentation();

            userResource.roles().realmLevel().add(Collections.singletonList(role));
            log.info("✅ Role '{}' assigned", roleName);

        } catch (Exception e) {
            log.warn("⚠️ Failed to assign role '{}': {}", roleName, e.getMessage());
        }
    }
}
