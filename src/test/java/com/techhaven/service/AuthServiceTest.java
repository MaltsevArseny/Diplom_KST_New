package com.techhaven.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.techhaven.security.SecurityManager;

class AuthServiceTest {

    @Test
    void validatePasswordRejectsShort() {
        String error = SecurityManager.validatePassword("Abc1@");
        assertNotNull(error, "Short password should be rejected");
    }

    @Test
    void validatePasswordRejectsNoUpperCase() {
        String error = SecurityManager.validatePassword("abcdefg1@");
        assertNotNull(error, "Password without uppercase should be rejected");
    }

    @Test
    void validatePasswordRejectsNoLowerCase() {
        String error = SecurityManager.validatePassword("ABCDEFG1@");
        assertNotNull(error, "Password without lowercase should be rejected");
    }

    @Test
    void validatePasswordRejectsNoDigit() {
        String error = SecurityManager.validatePassword("Abcdefgh@");
        assertNotNull(error, "Password without digit should be rejected");
    }

    @Test
    void validatePasswordRejectsNoSpecialChar() {
        String error = SecurityManager.validatePassword("Abcdefg1");
        assertNotNull(error, "Password without special char should be rejected");
    }

    @Test
    void validatePasswordAcceptsValid() {
        String error = SecurityManager.validatePassword("ValidPass1@");
        assertNull(error, "Valid password should be accepted");
    }

    @Test
    void validatePasswordRejectsNull() {
        String error = SecurityManager.validatePassword(null);
        assertNotNull(error, "Null password should be rejected");
    }

    @Test
    void validatePasswordRejectsEmpty() {
        String error = SecurityManager.validatePassword("");
        assertNotNull(error, "Empty password should be rejected");
    }

    @Test
    void hashPasswordProducesDifferentHashes() {
        SecurityManager sm = SecurityManager.getInstance();
        String hash1 = sm.hashPassword("TestPass1@");
        String hash2 = sm.hashPassword("TestPass1@");
        // Hashes should differ due to random salt
        assertNotEquals(hash1, hash2, "Same password should produce different hashes (random salt)");
    }

    @Test
    void verifyPasswordCorrect() {
        SecurityManager sm = SecurityManager.getInstance();
        String hash = sm.hashPassword("MySecret1@");
        assertTrue(sm.verifyPassword("MySecret1@", hash));
    }

    @Test
    void verifyPasswordIncorrect() {
        SecurityManager sm = SecurityManager.getInstance();
        String hash = sm.hashPassword("MySecret1@");
        assertFalse(sm.verifyPassword("WrongPass1@", hash));
    }
}
