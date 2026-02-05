package com.drag.foreignnationals.etranger.integrationTest.security;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.entity.RefreshToken;
import com.drag.foreignnationals.etranger.entity.User;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.repository.RefreshTokenRepository;
import com.drag.foreignnationals.etranger.repository.UserRepository;
import com.drag.foreignnationals.etranger.service.RefreshTokenService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthSecurityIT extends AbstractMySqlIT{

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RefreshTokenService refreshTokenService;

    @BeforeEach
    void setupUser() {

        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User();
        user.setUsername("nadia");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setLastName("beddou");
        user.setFirstName("nadia");
        user.setDateOfBirth(LocalDate.now());
        user.setRole(Role.USER);
        user.setActive(true);
        user.setLocked(false);

        userRepository.save(user);
    }

    @Test
    void loginShouldReturnAccessAndRefreshTokens() throws Exception {

        String body = """
        {
          "username": "nadia",
          "password": "1234"          
        }
        """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void accessTokenShouldAllowProtectedEndpoint() throws Exception {

        // Login first
        String loginBody = """
        {
          "username": "nadia",
          "password": "1234"
        }
        """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        String accessToken = JsonPath.read(response, "$.accessToken");

        // Call secured endpoint
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("secured ok"));
    }

    @Test
    void refreshTokenShouldGenerateNewAccessToken() throws Exception {

        // Login first
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username": "nadia",
                  "password": "1234"
                }
                """))
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();

        String refreshToken = JsonPath.read(loginResponse, "$.refreshToken");

        // Refresh request
        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken)))
                .andReturn();

        String refreshResponse = refreshResult.getResponse().getContentAsString();

        String newAccessToken = JsonPath.read(refreshResponse, "$.accessToken");

        assertThat(newAccessToken).isNotBlank();
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
    void logoutShouldRemoveRefreshToken() throws Exception {

        User user = userRepository.findByUsername("nadia").orElseThrow();


        // Create refresh token
        RefreshToken token = refreshTokenService.createRefreshToken(user.getId());

        // Logout
        mockMvc.perform(post("/auth/logout")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk());

        // Token must be deleted
        assertThat(refreshTokenRepository.findByToken(token.getToken()))
                .isEmpty();
    }

    @Test
    void lockedUserCannotLogin() throws Exception {

        // Lock the user
        User user = userRepository.findByUsername("nadia").orElseThrow();
        user.setLocked(true);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username": "nadia",
                  "password": "1234"
                }
                """))
                .andExpect(status().isUnauthorized()); // 401
    }
    @Test
    void userRoleCannotAccessAdminEndpoint() throws Exception {
        User admin = new User();
        admin.setUsername("said");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setLastName("beddou");
        admin.setFirstName("nadia");
        admin.setDateOfBirth(LocalDate.now());
        admin.setRole(Role.USER);
        admin.setActive(true);
        admin.setLocked(false);
        userRepository.save(admin);

        String token = loginAndGetAccessToken("said", "1234");

        mockMvc.perform(get("/admin/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    void adminRoleCanAccessAdminEndpoint() throws Exception {

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setLastName("beddou");
        admin.setFirstName("nadia");
        admin.setDateOfBirth(LocalDate.now());
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setLocked(false);

        userRepository.save(admin);

        String token = loginAndGetAccessToken("admin", "adminpass");

        mockMvc.perform(get("/admin/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("admin ok"));
    }




    //"""""""""""""HELPER METHOD"""""""""""""""""""""""""
    private String loginAndGetAccessToken(String username, String password) throws Exception {

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password)))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        return JsonPath.read(response, "$.accessToken");
    }




}
