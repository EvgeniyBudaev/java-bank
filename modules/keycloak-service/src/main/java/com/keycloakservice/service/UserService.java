package com.keycloakservice.service;

import com.keycloakservice.controller.dto.request.RequestUserCreateDto;
import com.keycloakservice.controller.dto.request.RequestUserUpdateDto;
import com.keycloakservice.controller.dto.response.ResponseUserDetailDto;
import com.keycloakservice.controller.dto.response.ResponseUserDto;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface UserService {
    ResponseUserDto createUser(RequestUserCreateDto dto);

    UserRepresentation updateUser(RequestUserUpdateDto dto);

    void deleteUser(String userId);

    UserResource getUser(String userId);

    UserRepresentation getUserDetailAll(String userId);

    ResponseUserDetailDto getUserDetail(String userId);

    void sendVerificationEmail(String userId);

    void forgotPassword(String username);

    List<RoleRepresentation> getUserRoles(String userId);

    List<GroupRepresentation> getUserGroups(String userId);
}
