package com.techhaven.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        User u = new User();
        assertEquals(0, u.getId());
        assertNull(u.getUsername());
        assertNull(u.getEmail());
        assertNull(u.getRole());
        assertEquals(0, u.getFailedAttempts());
    }

    @Test
    @DisplayName("Параметризированный конструктор")
    void parameterizedConstructor() {
        User u = new User("TestUser", "test@mail.com", "+71234567890", "hash123", "USER");
        assertEquals("TestUser", u.getUsername());
        assertEquals("test@mail.com", u.getEmail());
        assertEquals("+71234567890", u.getPhone());
        assertEquals("hash123", u.getPasswordHash());
        assertEquals("USER", u.getRole());
        assertEquals(0, u.getFailedAttempts());
        assertNotNull(u.getCreatedAt());
        assertNotNull(u.getUpdatedAt());
    }

    @Test
    @DisplayName("isAdmin возвращает true для роли ADMIN")
    void isAdmin() {
        User admin = new User("Admin", "admin@test.com", "+70000000000", "hash", "ADMIN");
        assertTrue(admin.isAdmin());

        User user = new User("User", "user@test.com", "+71111111111", "hash", "USER");
        assertFalse(user.isAdmin());
    }

    @Test
    @DisplayName("isAdmin при null роли возвращает false")
    void isAdminNull() {
        User u = new User();
        assertFalse(u.isAdmin());
    }

    @Test
    @DisplayName("Геттеры и сеттеры блокировки и доп. полей")
    void lockingFields() {
        User u = new User();
        u.setId(10);
        u.setFailedAttempts(3);
        u.setLockUntil("2026-01-01 12:00:00");
        u.setBlockReason("Слишком много попыток");
        u.setLastLogin("2026-02-28 10:00:00");

        assertEquals(10, u.getId());
        assertEquals(3, u.getFailedAttempts());
        assertEquals("2026-01-01 12:00:00", u.getLockUntil());
        assertEquals("Слишком много попыток", u.getBlockReason());
        assertEquals("2026-02-28 10:00:00", u.getLastLogin());
    }

    @Test
    @DisplayName("Все сеттеры и геттеры для основных полей")
    void allSettersGetters() {
        User u = new User();
        LocalDateTime now = LocalDateTime.now();

        u.setUsername("Иван");
        u.setEmail("ivan@test.ru");
        u.setPhone("+79998887766");
        u.setPasswordHash("hash:value");
        u.setRole("ADMIN");
        u.setCreatedAt(now);
        u.setUpdatedAt(now);

        assertEquals("Иван", u.getUsername());
        assertEquals("ivan@test.ru", u.getEmail());
        assertEquals("+79998887766", u.getPhone());
        assertEquals("hash:value", u.getPasswordHash());
        assertEquals("ADMIN", u.getRole());
        assertEquals(now, u.getCreatedAt());
        assertEquals(now, u.getUpdatedAt());
    }
}
