package com.techhaven.config;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.techhaven.model.User;

class SessionManagerTest {

    @AfterEach
    void cleanup() {
        SessionManager.getInstance().logout();
    }

    @Test
    void singletonAlwaysReturnsSameInstance() {
        SessionManager a = SessionManager.getInstance();
        SessionManager b = SessionManager.getInstance();
        assertSame(a, b);
    }

    @Test
    void initiallyNotLoggedIn() {
        SessionManager sm = SessionManager.getInstance();
        assertFalse(sm.isLoggedIn());
        assertNull(sm.getCurrentUser());
        assertEquals(-1, sm.getCurrentUserId());
        assertFalse(sm.isAdmin());
    }

    @Test
    void loginSetsCurrentUser() {
        User user = new User("testUser", "test@test.com", "+70001234567", "hash123", "USER");
        user.setId(42);

        SessionManager sm = SessionManager.getInstance();
        sm.login(user);

        assertTrue(sm.isLoggedIn());
        assertEquals(42, sm.getCurrentUserId());
        assertEquals("testUser", sm.getCurrentUser().getUsername());
        assertFalse(sm.isAdmin());
    }

    @Test
    void loginAsAdmin() {
        User admin = new User("admin", "admin@test.com", "+70001234567", "hash123", "ADMIN");
        admin.setId(1);

        SessionManager sm = SessionManager.getInstance();
        sm.login(admin);

        assertTrue(sm.isLoggedIn());
        assertTrue(sm.isAdmin());
    }

    @Test
    void logoutClearsSession() {
        User user = new User("testUser", "test@test.com", "+70001234567", "hash123", "USER");
        user.setId(42);

        SessionManager sm = SessionManager.getInstance();
        sm.login(user);
        assertTrue(sm.isLoggedIn());

        sm.logout();
        assertFalse(sm.isLoggedIn());
        assertNull(sm.getCurrentUser());
        assertEquals(-1, sm.getCurrentUserId());
    }

    @Test
    void getCurrentUserIdReturnsMinusOneWhenNotLoggedIn() {
        assertEquals(-1, SessionManager.getInstance().getCurrentUserId());
    }

    @Test
    void isAdminReturnsFalseWhenNotLoggedIn() {
        assertFalse(SessionManager.getInstance().isAdmin());
    }

    @Test
    void doubleLoginReplacesPreviousUser() {
        User first = new User("first", "first@test.com", "+70000000001", "h1", "USER");
        first.setId(1);
        User second = new User("second", "second@test.com", "+70000000002", "h2", "ADMIN");
        second.setId(2);

        SessionManager sm = SessionManager.getInstance();
        sm.login(first);
        assertEquals(1, sm.getCurrentUserId());

        sm.login(second);
        assertEquals(2, sm.getCurrentUserId());
        assertTrue(sm.isAdmin());
    }
}
