package com.drag.foreignnationals.etranger.integrationTest.security;



import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AdminIT extends AbstractMySqlIT {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------
    // Reset DB before each test
    // ---------------------------------------------------
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }


    // ===================================================
    // ✅ POSITIVE TESTS
    // ===================================================

    // ---------------------------------------------------
    // 1) Add User
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddUser_whenAdmin() throws Exception {

        SignupRequest request = new SignupRequest();
        request.setUsername("newUser");
        request.setPassword("123456");
        request.setLastName("lolo");
        request.setFirstName("fofo");
        request.setDateOfBirth(LocalDate.now());
        request.setRole(Role.USER);

        mockMvc.perform(post("/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.role").value("USER"));

        assertThat(userRepository.findByUsername("newUser")).isPresent();
    }

    // ---------------------------------------------------
    // 2) Lock User
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldLockUser() throws Exception {

        User user = new User();
        user.setUsername("lockMe");
        user.setPassword("1234");
        user.setLastName("lolo");
        user.setFirstName("fofo");
        user.setDateOfBirth(LocalDate.now());
        user.setRole(Role.USER);
        user.setLocked(false);
        user.setActive(true);

        user = userRepository.save(user);

        mockMvc.perform(post("/admin/users/" + user.getId() + "/lock"))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).get();
        assertThat(updated.isLocked()).isTrue();
    }

    // ---------------------------------------------------
    // 3) Unlock User
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUnlockUser() throws Exception {

        User user = new User();
        user.setUsername("unlockMe");
        user.setPassword("1234");
        user.setRole(Role.USER);
        user.setLocked(true);
        user.setActive(true);

        user = userRepository.save(user);

        mockMvc.perform(post("/admin/users/" + user.getId() + "/unlock"))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).get();
        assertThat(updated.isLocked()).isFalse();
    }

    // ---------------------------------------------------
    // 4) Change Role
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldChangeUserRole() throws Exception {

        User user = new User();
        user.setUsername("roleUser");
        user.setPassword("1234");
        user.setRole(Role.USER);
        user.setActive(true);

        user = userRepository.save(user);

        mockMvc.perform(post("/admin/users/" + user.getId() + "/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).get();
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
    }

    // ---------------------------------------------------
    // 5) List Users
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListUsers() throws Exception {

        User u1 = new User();
        u1.setUsername("user1");
        u1.setPassword("1234");
        u1.setRole(Role.USER);
        u1.setActive(true);

        User u2 = new User();
        u2.setUsername("user2");
        u2.setPassword("1234");
        u2.setRole(Role.ADMIN);
        u2.setActive(true);

        userRepository.saveAll(List.of(u1, u2));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    // ===================================================
    // ❌ NEGATIVE TESTS
    // ===================================================

    // ---------------------------------------------------
    // 6) Forbidden if not ADMIN
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectAccess_whenNotAdmin() throws Exception {

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------
    // 7) Forbidden if not authenticated
    // ---------------------------------------------------
    @Test
    void shouldRejectAccess_whenAnonymous() throws Exception {

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------
    // 8) Lock non-existing user → 500 (RuntimeException)
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFail_whenLockUserDoesNotExist() throws Exception {

        mockMvc.perform(post("/admin/users/999/lock"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------------------------------------------
    // 9) Unlock non-existing user → 500
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFail_whenUnlockUserDoesNotExist() throws Exception {

        mockMvc.perform(post("/admin/users/999/unlock"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------------------------------------------
    // 10) Change role non-existing user → 500
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFail_whenChangeRoleUserDoesNotExist() throws Exception {

        mockMvc.perform(post("/admin/users/999/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------------------------------------------
    // 11) Invalid Role value → 400 Bad Request
    // ---------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequest_whenRoleIsInvalid() throws Exception {

        User user = new User();
        user.setUsername("testUser");
        user.setPassword("1234");
        user.setRole(Role.USER);
        user.setActive(true);

        user = userRepository.save(user);

        mockMvc.perform(post("/admin/users/" + user.getId() + "/role")
                        .param("role", "SUPERADMIN"))
                .andExpect(status().isBadRequest());
    }
}
