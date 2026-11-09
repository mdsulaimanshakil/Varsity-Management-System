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

    // Unit Test 5: verify findByUsername for existing and non-existing user
    @Test
    void findByUsername_existingAndNonExisting() {
        assertTrue(repo.findByUsername("admin_test").isPresent());
        assertTrue(repo.findByUsername("nobody").isEmpty());
    }

    // Unit Test 6: verify existsByUsername and findByRole (fixed)
    @Test
    void existsByUsername_andFindByRole() {
        // Correct assertion: existsByUsername returns true for existing user, false for nonexistent
        assertTrue(repo.existsByUsername("admin_test"), "existsByUsername should return true for existing user");
        assertFalse(repo.existsByUsername("nonexistent"), "existsByUsername should return false for nonexistent user");

        // Role filtering
        List<AppUser> admins = repo.findByRole(Role.ADMIN);
        assertEquals(1, admins.size());
        assertEquals("admin_test", admins.get(0).getUsername());

        assertTrue(repo.findByIdAndRole(savedAdmin.getId(), Role.ADMIN).isPresent());
        assertTrue(repo.findByIdAndRole(savedAdmin.getId(), Role.STUDENT).isEmpty());
    }
}
