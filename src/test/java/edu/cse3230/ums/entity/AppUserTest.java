package edu.cse3230.ums.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppUserTest {

    // Unit Test 1: verify getters and setters
    @Test
    void testGettersAndSetters() {
        AppUser user = new AppUser();

        user.setId(1L);
        user.setUsername("john");
        user.setPassword("secret");
        user.setFullName("John Doe");
        user.setEmail("john@university.edu");
        user.setDepartment("CSE");
        user.setRole(Role.STUDENT);

        assertEquals(1L, user.getId());
        assertEquals("john", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("John Doe", user.getFullName());
        assertEquals("john@university.edu", user.getEmail());
        assertEquals("CSE", user.getDepartment());
        assertEquals(Role.STUDENT, user.getRole());
    }

    // Unit Test 2: verify default constructor fields are null
    @Test
    void testDefaultConstructorFieldsAreNull() {
        AppUser user = new AppUser();

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getFullName());
        assertNull(user.getEmail());
        assertNull(user.getDepartment());
        assertNull(user.getRole(), "Default role is null when not explicitly set");
    }
}
