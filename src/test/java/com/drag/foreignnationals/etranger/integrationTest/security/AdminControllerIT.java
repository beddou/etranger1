package com.drag.foreignnationals.etranger.integrationTest.security;


import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.enums.Role;
import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.entity.CustomRevisionEntity;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AdminControllerIT extends AbstractMySqlIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    private User createTestUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("encoded_pass");
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setActive(true);
        user.setLocked(false);
        return userRepository.save(user);
    }

    /*============================================================
    1. SECURITY & ROLE ACCESS
    ============================================================*/
    @Nested
    class AuthorizationTests {

        @Test
        @WithMockUser(roles = "USER") // Insufficient role
        void userRoleShouldBeForbidden() throws Exception {
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
        }

        @Test
        void unauthenticatedShouldBeUnauthorized() throws Exception {
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
        }
    }

    /*============================================================
    2. USER MANAGEMENT (ADMIN ONLY)
    ============================================================*/
    @Nested
    @WithMockUser(roles = "ADMIN")
    class AdminActions {

        @Test
        void shouldAddUserSuccessfully() throws Exception {
            SignupRequest request = new SignupRequest();
            request.setUsername("newuser");
            request.setPassword("password123");
            request.setFirstName("Nadia");
            request.setLastName("Beddou");
            request.setDateOfBirth(LocalDate.now());
            //User user = createTestUser("newuser", Role.USER);

            mockMvc.perform(post("/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("newuser"))
                    .andExpect(jsonPath("$.locked").value(false));
        }

        @Test
        void shouldLockUserSuccessfully() throws Exception {
            User user = createTestUser("to_lock", Role.USER);

            mockMvc.perform(patch("/admin/users/{id}/lock", user.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.locked").value(true));

            assertThat(userRepository.findById(user.getId()).get().isLocked()).isTrue();
        }

        @Test
        void shouldUnlockUser() throws Exception {
            User user = createTestUser("to_unLock", Role.USER);
            user.setLocked(true);
            user.setActive(true);

            user = userRepository.save(user);

            mockMvc.perform(patch("/admin/users/" + user.getId() + "/unlock"))
                    .andExpect(status().isOk());

            User updated = userRepository.findById(user.getId()).get();
            assertThat(updated.isLocked()).isFalse();
        }

        @Test
        void shouldChangeUserRole() throws Exception {
            User user = createTestUser("change_me", Role.USER);

            mockMvc.perform(patch("/admin/users/{id}/role", user.getId())
                            .param("role", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void shouldFailWhenUserNotFound() throws Exception {
            // Testing your refactored findUserOrThrow() helper
            mockMvc.perform(patch("/admin/users/999/lock"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User not found with ID: 999"));
        }

        @Test
        void shouldPreventDuplicateUsername() throws Exception {
            createTestUser("existing", Role.USER);

            SignupRequest request = new SignupRequest();
            request.setUsername("existing"); // Duplicate
            request.setPassword("password");
            request.setFirstName("Fn");
            request.setLastName("Ln");
            request.setDateOfBirth(LocalDate.now());

            mockMvc.perform(post("/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict()); // Or isBadRequest depending on ErrorCode mapping
        }
    }

    /*============================================================
    3. ADVANCED BUSINESS LOGIC TESTS
    ============================================================*/
    @Nested
    @WithMockUser(username = "superadmin", roles = "ADMIN")
    class AdvancedAdminActions {

        @Test
        void shouldPreventAdminFromLockingThemselves() throws Exception {
            // GIVEN: The admin exists in DB with the same name as @WithMockUser
            User admin = createTestUser("superadmin", Role.ADMIN);

            // WHEN + THEN: Admin tries to lock their own ID
            mockMvc.perform(patch("/admin/users/{id}/lock", admin.getId()))
                    .andExpect(status().isUnauthorized()) // Caught by BusinessException
                    .andExpect(jsonPath("$.message").value("Security Breach: You cannot lock your own account!"));

            // Verify DB state remains unlocked
            assertThat(userRepository.findById(admin.getId()).get().isLocked()).isFalse();
        }

        @Test
        void shouldFailWhenAddingUserWithInvalidData() throws Exception {
            SignupRequest badRequest = new SignupRequest();
            badRequest.setUsername(""); // Invalid: Empty
            badRequest.setPassword("123"); // Likely too short for your @Valid constraints

            mockMvc.perform(post("/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        void listUsersShouldNotReturnPasswords() throws Exception {
            createTestUser("user1", Role.USER);
            createTestUser("user2", Role.USER);

            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    // Crucial: Ensure the Mapper/DTO is not leaking passwords
                    .andExpect(jsonPath("$[0].password").doesNotExist());
        }

        @Test
        void shouldHandleInvalidRoleParameter() throws Exception {
            User user = createTestUser("role_test", Role.USER);

            // Sending an invalid enum string "MANAGER"
            mockMvc.perform(patch("/admin/users/{id}/role", user.getId())
                            .param("role", "MANAGER"))
                    .andExpect(status().isBadRequest());
            // Spring automatically throws MethodArgumentTypeMismatchException
        }
    }

    @Nested
    @WithMockUser(username = "admin_boss", roles = "ADMIN")
    class SoftDeleteTests {

        @Test
        void shouldSoftDeleteTransparently() throws Exception {
            User user = createTestUser("ghost_user", Role.USER);

            // Act
            mockMvc.perform(delete("/admin/users/{id}", user.getId()))
                    .andExpect(status().isNoContent());

            // 1. Standard repository search returns empty (because of @Where)
            assertThat(userRepository.findByUsername("ghost_user")).isEmpty();
            assertThat(userRepository.findById(user.getId())).isEmpty();

            // 2. Verify with Native Query that data IS still there (for Audit)
            // You would need a native query in your repository to bypass the @Where clause
            boolean existsInDb = userRepository.findByUsernameIncludeDeleted(user.getUsername()).isPresent();
            assertThat(existsInDb).isTrue();
        }

        @Test
        void shouldPreventAdminFromDeletingThemselves() throws Exception {
            User admin = createTestUser("admin_boss", Role.ADMIN);

            mockMvc.perform(delete("/admin/users/{id}", admin.getId()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("You cannot delete your own account."));

            // VERIFY: Admin is still active
            assertThat(userRepository.findById(admin.getId()).get().isDeleted()).isFalse();
        }
    }

    /*============================================================
   3. AUDITING TESTS
   ============================================================*/
    @Nested

    @WithMockUser(username = "admin_user", roles = "ADMIN")
    class AudintingTest {
        @Test
        @WithMockUser(username = "admin_user", roles = "ADMIN")
        void shouldAuditUserRoleChange() {
            // 1. Create User
            User user = createTestUser("admin_boss", Role.USER);
            user = userRepository.save(user); // Revision 1

            // 2. Change Role
            user.setRole(Role.ADMIN);
            userRepository.save(user); // Revision 2

            // 3. Verify Audit
            Revisions<Integer, User> revisions = userRepository.findRevisions(user.getId());

            assertThat(revisions.getContent()).hasSize(2);

            // Check Revision 2
            Revision<Integer, User> lastRev = revisions.getLatestRevision();
            assertThat(lastRev.getEntity().getRole()).isEqualTo(Role.ADMIN);

            // Check WHO did it
            CustomRevisionEntity info = (CustomRevisionEntity) lastRev.getMetadata().getDelegate();
            assertThat(info.getModifierUser()).isEqualTo("admin_user");
        }
    }
}
