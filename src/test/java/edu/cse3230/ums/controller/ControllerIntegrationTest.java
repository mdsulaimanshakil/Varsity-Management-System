package edu.cse3230.ums.controller;

import edu.cse3230.ums.entity.AppUser;
import edu.cse3230.ums.entity.Role;
import edu.cse3230.ums.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that boot the full application context with an in-memory H2 database
 * and exercise the REST endpoints through MockMvc with real security.
 *
 * Exactly 3 integration tests:
 * Test 1 – Admin CRUD round-trip with authentication
 * Test 2 – Student lifecycle: create -> read -> update -> delete
 * Test 3 – Role-based access control and /api/auth/me (fixed: student gets 403 on /api/admins)
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        repo.deleteAll();

        repo.save(buildUser("admin", "admin123", "Test Admin", "admin@test.edu", "Administration", Role.ADMIN));
        repo.save(buildUser("teacher", "teacher123", "Test Teacher", "teacher@test.edu", "CSE", Role.TEACHER));
        repo.save(buildUser("student", "student123", "Test Student", "student@test.edu", "CSE", Role.STUDENT));
    }

    // ── Integration Test 1: Admin CRUD round-trip ────────────────────────
    @Test
    void adminCrud_createAndListAdmins() throws Exception {
        String json = """
                {
                    "username": "newadmin",
                    "password": "pass123",
                    "fullName": "New Admin",
                    "email": "newadmin@test.edu",
                    "department": "IT"
                }
                """;

        mockMvc.perform(post("/api/admins")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newadmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(get("/api/admins")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", hasItems("admin", "newadmin")));
    }

    // ── Integration Test 2: Student full lifecycle ───────────────────────
    @Test
    void studentLifecycle_createReadUpdateDelete() throws Exception {
        String createJson = """
                {
                    "username": "jane",
                    "password": "pass123",
                    "fullName": "Jane Student",
                    "email": "jane@test.edu",
                    "department": "EEE"
                }
                """;

        String location = mockMvc.perform(post("/api/students")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jane Student"));

        String id = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(put("/api/students/" + id)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Jane Updated\",\"department\":\"CSE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jane Updated"));

        mockMvc.perform(delete("/api/students/" + id)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students/" + id)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    // ── Integration Test 3: RBAC and /api/auth/me (fixed) ─────────────────
    @Test
    void securityRbac_andAuthMe() throws Exception {
        // Unauthenticated request should return 401 Unauthorized
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isUnauthorized());

        // Correct RBAC assertion: Student is forbidden from accessing Admin endpoint (403 Forbidden)
        mockMvc.perform(get("/api/admins")
                        .with(httpBasic("student", "student123")))
                .andExpect(status().isForbidden());

        // /api/auth/me returns current authenticated user and hides password
        mockMvc.perform(get("/api/auth/me")
                        .with(httpBasic("teacher", "teacher123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("teacher"))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // ── Helper ───────────────────────────────────────────────────────────
    private AppUser buildUser(String username, String password, String fullName,
                              String email, String department, Role role) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setFullName(fullName);
        u.setEmail(email);
        u.setDepartment(department);
        u.setRole(role);
        return u;
    }
}
