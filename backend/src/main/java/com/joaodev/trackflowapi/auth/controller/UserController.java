package com.joaodev.trackflowapi.auth.controller;

import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.dto.UpdateRoleRequest;
import com.joaodev.trackflowapi.auth.dto.UserResponse;
import com.joaodev.trackflowapi.auth.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        var created = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userManagementService.listUsers().stream().map(UserResponse::from).toList();
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return UserResponse.from(userManagementService.changeRole(id, request.role()));
    }

    @PatchMapping("/{id}/deactivate")
    public UserResponse deactivate(@PathVariable Long id) {
        return updateStatus(id, false);
    }

    @PatchMapping("/{id}/activate")
    public UserResponse activate(@PathVariable Long id) {
        return updateStatus(id, true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        userManagementService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private UserResponse updateStatus(Long userId, boolean isActive) {
        return UserResponse.from(userManagementService.setActive(userId, isActive));
    }
}
