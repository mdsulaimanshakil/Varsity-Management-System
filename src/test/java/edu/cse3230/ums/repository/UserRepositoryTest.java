package edu.cse3230.ums.repository;

import edu.cse3230.ums.entity.AppUser;
import edu.cse3230.ums.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repo;

    private AppUser savedAdmin;

    @BeforeEach
    void setUp() {
        repo.deleteAll();

        AppUser admin = new AppUser();
        admin.setUsername("admin_test");
        admin.setPassword("pass");
        admin.setFullName("Admin User");
        admin.setEmail("admin@test.edu");
        admin.setDepartment("Administration");
        admin.setRole(Role.ADMIN);
        savedAdmin = repo.save(admin);

        AppUser student = new AppUser();
        student.setUsername("student_test");
        student.setPassword("pass");
        student.setFullName("Student User");
        student.setEmail("student@test.edu");
        student.setDepartment("CSE");
        student.setRole(Role.STUDENT);
        repo.save(student);
    }

    // [PASS] Unit Test 5: findByUsername for existing and non-existing
    @Test
    void findByUsername_existingAndNonExisting() {
        assertTrue(repo.findByUsername("admin_test").isPresent());
        assertTrue(repo.findByUsername("nobody").isEmpty());
    }

    // [PASS] Unit Test 6: findByRole and findByIdAndRole
    @Test
    void findByRole_returnsCorrectUsers() {
        List<AppUser> admins = repo.findByRole(Role.ADMIN);
        assertEquals(1, admins.size());
        assertEquals("admin_test", admins.get(0).getUsername());

        assertTrue(repo.findByIdAndRole(savedAdmin.getId(), Role.ADMIN).isPresent());
        assertTrue(repo.findByIdAndRole(savedAdmin.getId(), Role.STUDENT).isEmpty());
    }

    // [FAIL 3/4 - Unit]: asserts false for an existing username
    @Test
    void existsByUsername_shouldReturnFalseForExistingUser() {
        assertFalse(repo.existsByUsername("admin_test"),
                "existsByUsername should return false for existing usernames");
    }
}
