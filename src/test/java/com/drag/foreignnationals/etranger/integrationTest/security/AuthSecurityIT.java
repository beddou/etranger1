package com.drag.foreignnationals.etranger.integrationTest.security;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.security.entity.RefreshToken;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.security.filter.login.RateLimitingFilter;
import com.drag.foreignnationals.etranger.security.repository.RefreshTokenRepository;
import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import com.drag.foreignnationals.etranger.security.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(properties = "app.security.rate-limited-endpoints=/auth/login")
public class AuthSecurityIT extends AbstractMySqlIT {


    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RefreshTokenService refreshTokenService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EntityManager entityManager;

    @BeforeEach
    void clean() {
        refreshTokenRepository.deleteAll();
        // This executes a hard delete, ignoring Hibernate filters
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.flush();
    }

    @Nested
    @DisplayName("Login & Token Tests")
    class LoginTests {

        @Test
        void loginShouldReturnTokensForValidUser() throws Exception {
            createUser("nadia", "1234", Role.USER);

            performLogin("nadia", "1234")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void refreshTokenShouldGenerateNewAccess() throws Exception {
            createUser("nadia", "1234", Role.USER);
            String response = performLogin("nadia", "1234").andReturn().getResponse().getContentAsString();
            String refreshToken = JsonPath.read(response, "$.refreshToken");

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        }

        @Test
        void accessTokenShouldAllowProtectedEndpoint() throws Exception {

            createUser("nadia", "1234", Role.USER);
            // Login first

            String response = performLogin("nadia", "1234").andReturn().getResponse().getContentAsString();
            String accessToken = JsonPath.read(response, "$.accessToken");

            // Call secured endpoint
            mockMvc.perform(get("/api/test")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("secured ok"));
        }

        @Test
        void invalidRefreshTokenShouldReturn401() throws Exception {

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                {
                  "refreshToken": "fake-token"
                }
                """))
                    .andExpect(status().isUnauthorized());
        }


        @Test
        void lockedUserCannotLogin() throws Exception {

            User user = createUser("locked_user", "1234", Role.USER);
            // Lock the user

            user.setLocked(true);
            userRepository.save(user);

            performLogin("locked_user", "1234")
                    .andExpect(status().isUnauthorized());
        }


        @Test
        void softDeletedUserCannotLogin() throws Exception {
            User user = createUser("deleted_user", "1234", Role.USER);
            // Simulate soft delete
            user.setDeleted(true);
            user.setActive(false);
            userRepository.save(user);

            performLogin("deleted_user", "1234")
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void loginShouldBeRateLimited() throws Exception {

            // Perform 10 successful requests
            for (int i = 0; i < 10; i++) {
                performLogin("nadia", "1234").andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 429,
                            "Expected status 401 or 429 but was " + status);
                });
                refreshTokenRepository.deleteAll();
            }

            // The 6th request should fail
            performLogin("nadia", "1234").andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    @DisplayName("Role & Permission Tests")
    class PermissionTests {

        @Test
        void userCannotAccessAdmin() throws Exception {
            createUser("standard", "1234", Role.USER);
            String token = loginAndGetToken("standard", "1234");

            mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden()); // 403
        }

        @Test
        void adminCanAccessAdmin() throws Exception {
            createUser("boss", "admin123", Role.ADMIN);
            String token = loginAndGetToken("boss", "admin123");

            mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void logoutShouldDeleteToken() throws Exception {
        User user = createUser("logout_user", "1234", Role.USER);
        RefreshToken rt = refreshTokenService.createRefreshToken(user.getId());

        mockMvc.perform(post("/auth/logout")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.findByToken(rt.getToken())).isEmpty();
    }

    // ======================================================
    // PRIVATE HELPERS
    // ======================================================

    private User createUser(String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return userRepository.save(user);
    }

    private ResultActions performLogin(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", password
                ))));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String res = performLogin(username, password).andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.accessToken");
    }
}