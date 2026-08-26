package com.joaodev.trackflowapi.auth.controller;

import com.joaodev.trackflowapi.auth.dto.CreateUserRequest;
import com.joaodev.trackflowapi.auth.dto.UserResponse;
import com.joaodev.trackflowapi.auth.dto.AuthResponse;
import com.joaodev.trackflowapi.auth.dto.LoginRequest;
import com.joaodev.trackflowapi.shipment.dto.CreateShipmentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class UserControllerIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken() {
        LoginRequest request = new LoginRequest(
                "admin@trackflow.dev", "ChangeMe123!");
        AuthResponse response = restTemplate.postForObject(
                "/api/auth/login", request, AuthResponse.class);
        assertThat(response).isNotNull();
        return response.token();
    }

    private HttpEntity<Object> withAuth(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void adminCanCreateNewUser() {
        String token = adminToken();
        String email = "ops-" + UUID.randomUUID() + "@trackflow.dev";
        CreateUserRequest request = new CreateUserRequest(email, "password123", "OPS");

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/api/users", HttpMethod.POST, withAuth(request, token), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assert response.getBody() != null;
        assertThat(response.getBody().email()).isEqualTo(email);
        assertThat(response.getBody().role()).isEqualTo("OPS");
        assertThat(response.getBody().active()).isTrue();
    }

    @Test
    void nonAdminCannotAccessUserManagement() {
        String adminToken = adminToken();
        String opsEmail = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        restTemplate.exchange("/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(
                        opsEmail, "password123", "OPS"), adminToken), UserResponse.class);

        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(
                        opsEmail, "password123"), AuthResponse.class);

        assert opsLogin != null;
        ResponseEntity<?> response = restTemplate.exchange(
                "/api/users", HttpMethod.GET, withAuth(null, opsLogin.token()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCanDeactivateUserAndDeactivatedUserCannotLogin() {
        String adminToken = adminToken();
        String email = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        ResponseEntity<UserResponse> created = restTemplate.exchange(
                "/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(
                        email, "password123", "OPS"), adminToken), UserResponse.class);

        assert created.getBody() != null;
        Long userId = created.getBody().id();

        ResponseEntity<UserResponse> deactivated = restTemplate.exchange(
                "/api/users/" + userId + "/deactivate", HttpMethod.PATCH,
                withAuth(null, adminToken), UserResponse.class);

        assert deactivated.getBody() != null;
        assertThat(deactivated.getBody().active()).isFalse();

        ResponseEntity<Map> loginAttempt = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(email, "password123"), Map.class);

        assertThat(loginAttempt.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCanListUsers() {
        String token = adminToken();

        ResponseEntity<UserResponse[]> response = restTemplate.exchange(
                "/api/users", HttpMethod.GET, withAuth(null, token), UserResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(UserResponse::email).contains("admin@trackflow.dev");
    }

    @Test
    void unauthenticatedRequestToCreateShipmentReturnsUnauthorized() {
        CreateShipmentRequest request = new CreateShipmentRequest(
                "São Paulo", "Manaus", "Correios");

        ResponseEntity<?> response = restTemplate.postForEntity(
                "/api/shipments", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void adminCanDeleteUser() {
        String token = adminToken();
        String email = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        ResponseEntity<UserResponse> created = restTemplate.exchange(
                "/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(email, "password123", "OPS"), token), UserResponse.class);

        assert created.getBody() != null;
        Long userId = created.getBody().id();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/users/" + userId, HttpMethod.DELETE, withAuth(null, token), Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<UserResponse[]> listResponse = restTemplate.exchange(
                "/api/users", HttpMethod.GET, withAuth(null, token), UserResponse[].class);

        assertThat(listResponse.getBody())
                .extracting(UserResponse::email)
                .doesNotContain(email);
    }

    @Test
    void deletedUserCannotLogin() {
        String token = adminToken();
        String email = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        ResponseEntity<UserResponse> created = restTemplate.exchange(
                "/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(email, "password123", "OPS"), token), UserResponse.class);

        assert created.getBody() != null;
        Long userId = created.getBody().id();

        restTemplate.exchange("/api/users/" + userId, HttpMethod.DELETE, withAuth(null, token), Void.class);

        ResponseEntity<?> loginAttempt = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(email, "password123"), Map.class);

        assertThat(loginAttempt.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCannotDeleteOwnAccount() {
        String token = adminToken();

        ResponseEntity<UserResponse[]> listResponse = restTemplate.exchange(
                "/api/users", HttpMethod.GET, withAuth(null, token), UserResponse[].class);

        assert listResponse.getBody() != null;
        Long adminId = java.util.Arrays.stream(listResponse.getBody())
                .filter(u -> u.email().equals("admin@trackflow.dev"))
                .findFirst()
                .orElseThrow()
                .id();

        ResponseEntity<?> response = restTemplate.exchange(
                "/api/users/" + adminId, HttpMethod.DELETE, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void nonAdminCannotDeleteUser() {
        String adminToken = adminToken();
        String opsEmail = "ops-" + UUID.randomUUID() + "@trackflow.dev";

        ResponseEntity<UserResponse> opsCreated = restTemplate.exchange(
                "/api/users", HttpMethod.POST,
                withAuth(new CreateUserRequest(opsEmail, "password123", "OPS"), adminToken), UserResponse.class);

        assert opsCreated.getBody() != null;
        Long opsId = opsCreated.getBody().id();

        AuthResponse opsLogin = restTemplate.postForObject(
                "/api/auth/login", new LoginRequest(opsEmail, "password123"), AuthResponse.class);
        assert opsLogin != null;

        ResponseEntity<?> response = restTemplate.exchange(
                "/api/users/" + opsId, HttpMethod.DELETE, withAuth(null, opsLogin.token()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deletingNonExistentUserReturnsNotFound() {
        String token = adminToken();

        ResponseEntity<?> response = restTemplate.exchange(
                "/api/users/999999", HttpMethod.DELETE, withAuth(null, token), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}