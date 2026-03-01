package com.techhaven.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    @DisplayName("Enum содержит USER и ADMIN")
    void enumValues() {
        assertEquals(2, Role.values().length);
        assertEquals("USER", Role.USER.getValue());
        assertEquals("ADMIN", Role.ADMIN.getValue());
    }

    @Test
    @DisplayName("fromString парсит корректные значения")
    void fromStringValid() {
        assertEquals(Role.USER, Role.fromString("USER"));
        assertEquals(Role.ADMIN, Role.fromString("ADMIN"));
    }

    @Test
    @DisplayName("fromString — case insensitive")
    void fromStringCaseInsensitive() {
        assertEquals(Role.ADMIN, Role.fromString("admin"));
        assertEquals(Role.USER, Role.fromString("user"));
        assertEquals(Role.ADMIN, Role.fromString("Admin"));
    }

    @Test
    @DisplayName("fromString — null и неизвестные значения → USER")
    void fromStringFallback() {
        assertEquals(Role.USER, Role.fromString(null));
        assertEquals(Role.USER, Role.fromString(""));
        assertEquals(Role.USER, Role.fromString("UNKNOWN"));
    }
}
