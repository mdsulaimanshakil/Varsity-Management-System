package edu.cse3230.ums;

import edu.cse3230.ums.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class UniversityManagementApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repo;

    @Test
    void notLoggedInReturns401() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginAndLogout() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "admin").param("password", "admin123"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").param("username", "admin").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout")).andExpect(status().isOk());
    }

    @Test
    void passwordsAreHashed() {
        String stored = repo.findByUsername("admin").orElseThrow().getPassword();
        assertThat(stored).startsWith("$2a$").isNotEqualTo("admin123");
    }

    @Test
    void adminCanAccessEverything() throws Exception {
        mvc.perform(get("/api/admins").with(httpBasic("admin", "admin123"))).andExpect(status().isOk());
        mvc.perform(get("/api/teachers").with(httpBasic("admin", "admin123"))).andExpect(status().isOk());
        mvc.perform(get("/api/students").with(httpBasic("admin", "admin123"))).andExpect(status().isOk());
    }

    @Test
    void teacherCanAccessTeachersAndStudentsOnly() throws Exception {
        mvc.perform(get("/api/teachers").with(httpBasic("teacher", "teacher123"))).andExpect(status().isOk());
        mvc.perform(get("/api/students").with(httpBasic("teacher", "teacher123"))).andExpect(status().isOk());
        mvc.perform(get("/api/admins").with(httpBasic("teacher", "teacher123"))).andExpect(status().isForbidden());
    }

    @Test
    void studentCanAccessStudentsOnly() throws Exception {
        mvc.perform(get("/api/students").with(httpBasic("student", "student123"))).andExpect(status().isOk());
        mvc.perform(get("/api/teachers").with(httpBasic("student", "student123"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admins").with(httpBasic("student", "student123"))).andExpect(status().isForbidden());
    }

    @Test
    void studentCrud() throws Exception {
        String json = """
                {"username":"rafi","password":"secret12","fullName":"Rafi Ahmed",
                 "email":"rafi@university.edu","department":"CSE"}""";

        String location = mvc.perform(post("/api/students").with(httpBasic("teacher", "teacher123"))
                        .contentType("application/json").content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(post("/api/students").with(httpBasic("teacher", "teacher123"))
                        .contentType("application/json").content(json))
                .andExpect(status().isConflict());

        mvc.perform(get(location).with(httpBasic("rafi", "secret12"))).andExpect(status().isOk());

        mvc.perform(put(location).with(httpBasic("admin", "admin123"))
                        .contentType("application/json")
                        .content("{\"fullName\":\"Rafi A.\",\"department\":\"EEE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Rafi A."));


        mvc.perform(delete(location).with(httpBasic("admin", "admin123"))).andExpect(status().isNoContent());
        mvc.perform(get(location).with(httpBasic("admin", "admin123"))).andExpect(status().isNotFound());
    }
}
