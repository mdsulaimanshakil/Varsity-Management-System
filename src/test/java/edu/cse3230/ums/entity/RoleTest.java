package edu.cse3230.ums.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    // [PASS] Unit Test 3: verify all existing roles
    @Test
    void testRoleEnumValues() {
        Role[] roles = Role.values();

        assertEquals(3, roles.length);
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.TEACHER, Role.valueOf("TEACHER"));
        assertEquals(Role.STUDENT, Role.valueOf("STUDENT"));
    }

    // [PASS] Unit Test 4: verify enum ordinals
    @Test
    void testRoleEnumOrdinals() {
        assertEquals(0, Role.ADMIN.ordinal());
        assertEquals(1, Role.TEACHER.ordinal());
        assertEquals(2, Role.STUDENT.ordinal());
    }

    // [FAIL 2/4 - Unit]: expects non-existent SUPERADMIN role
    @Test
    void testSuperAdminRoleExists() {
        assertDoesNotThrow(() -> Role.valueOf("SUPERADMIN"),
                "Role enum should include a SUPERADMIN value");
    }
}
