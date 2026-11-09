package edu.cse3230.ums.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    // Unit Test 3: verify all expected roles exist
    @Test
    void testRoleEnumValues() {
        Role[] roles = Role.values();

        assertEquals(3, roles.length);
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.TEACHER, Role.valueOf("TEACHER"));
        assertEquals(Role.STUDENT, Role.valueOf("STUDENT"));
    }

    // Unit Test 4: verify invalid roles throw IllegalArgumentException (fixed)
    @Test
    void testInvalidRoleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("SUPERADMIN"),
                "Parsing non-existent role 'SUPERADMIN' should throw IllegalArgumentException");
    }
}
