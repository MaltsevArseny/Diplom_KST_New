package com.techhaven.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecurityManagerTest {

    private static SecurityManager sm;

    @BeforeAll
    static void init() {
        sm = SecurityManager.getInstance();
    }

    // ─── Singleton ──────────────────────────────────────────────────

    @Test
    @DisplayName("getInstance возвращает один и тот же объект")
    void singletonInstance() {
        SecurityManager a = SecurityManager.getInstance();
        SecurityManager b = SecurityManager.getInstance();
        assertSame(a, b);
    }

    // ─── hashPassword / verifyPassword ──────────────────────────────

    @Test
    @DisplayName("hashPassword возвращает формат salt:hash (Base64)")
    void hashFormat() {
        String hash = sm.hashPassword("Test1234!");
        assertNotNull(hash);
        assertTrue(hash.contains(":"), "Хеш должен содержать разделитель ':'");
        String[] parts = hash.split(":");
        assertEquals(2, parts.length);
        assertFalse(parts[0].isEmpty());
        assertFalse(parts[1].isEmpty());
    }

    @Test
    @DisplayName("verifyPassword подтверждает корректный пароль")
    void verifyCorrectPassword() {
        String password = "SecurePass123!";
        String hash = sm.hashPassword(password);
        assertTrue(sm.verifyPassword(password, hash));
    }

    @Test
    @DisplayName("verifyPassword отклоняет неверный пароль")
    void verifyWrongPassword() {
        String hash = sm.hashPassword("CorrectPass1!");
        assertFalse(sm.verifyPassword("WrongPass2!", hash));
    }

    @Test
    @DisplayName("Разные вызовы hashPassword дают разные хеши (уникальный salt)")
    void differentSalts() {
        String hash1 = sm.hashPassword("SamePassword1!");
        String hash2 = sm.hashPassword("SamePassword1!");
        assertNotEquals(hash1, hash2, "Каждый хеш должен быть уникальным (разные salt)");
        // Но оба должны верифицироваться
        assertTrue(sm.verifyPassword("SamePassword1!", hash1));
        assertTrue(sm.verifyPassword("SamePassword1!", hash2));
    }

    @Test
    @DisplayName("verifyPassword с невалидным форматом хеша возвращает false")
    void verifyInvalidHashFormat() {
        assertFalse(sm.verifyPassword("test", "not-a-valid-hash"));
        assertFalse(sm.verifyPassword("test", ""));
        assertFalse(sm.verifyPassword("test", "a:b:c"));
    }

    // ─── encrypt / decrypt ──────────────────────────────────────────

    @Test
    @DisplayName("encrypt → decrypt — round-trip")
    void encryptDecryptRoundTrip() {
        String original = "+79001234567";
        String encrypted = sm.encrypt(original);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted, "Зашифрованная строка не должна совпадать с исходной");
        assertTrue(encrypted.contains(":"), "Формат: iv:encrypted");

        String decrypted = sm.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    @DisplayName("encrypt возвращает разные результаты при каждом вызове (разные IV)")
    void encryptDifferentIV() {
        String text = "Москва, ул. Тверская, д. 1";
        String enc1 = sm.encrypt(text);
        String enc2 = sm.encrypt(text);
        assertNotEquals(enc1, enc2, "Каждое шифрование должно использовать уникальный IV");
        // Оба расшифровываются обратно
        assertEquals(text, sm.decrypt(enc1));
        assertEquals(text, sm.decrypt(enc2));
    }

    @Test
    @DisplayName("decrypt незашифрованного текста без ':' возвращает его же (fallback)")
    void decryptPlainText() {
        String plain = "просто текст без двоеточия";
        assertEquals(plain, sm.decrypt(plain));
    }

    @Test
    @DisplayName("encrypt/decrypt кириллического текста")
    void encryptDecryptCyrillic() {
        String original = "Адрес: г. Санкт-Петербург, Невский проспект, 123";
        String encrypted = sm.encrypt(original);
        String decrypted = sm.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    @DisplayName("encrypt/decrypt пустой строки")
    void encryptDecryptEmpty() {
        String encrypted = sm.encrypt("");
        String decrypted = sm.decrypt(encrypted);
        assertEquals("", decrypted);
    }

    // ─── validatePassword ───────────────────────────────────────────

    @Test
    @DisplayName("validatePassword — корректный пароль возвращает null")
    void validateCorrectPassword() {
        assertNull(SecurityManager.validatePassword("Test123!A"));
        assertNull(SecurityManager.validatePassword("MyPass1@x"));
    }

    @Test
    @DisplayName("validatePassword — null или короткий пароль")
    void validateShortPassword() {
        assertNotNull(SecurityManager.validatePassword(null));
        assertNotNull(SecurityManager.validatePassword(""));
        assertNotNull(SecurityManager.validatePassword("Ab1!"));
        assertNotNull(SecurityManager.validatePassword("Ab1!567")); // 7 символов
    }

    @Test
    @DisplayName("validatePassword — нет заглавной буквы")
    void validateNoUppercase() {
        String error = SecurityManager.validatePassword("test1234!");
        assertNotNull(error);
        assertTrue(error.contains("заглавную"));
    }

    @Test
    @DisplayName("validatePassword — нет строчной буквы")
    void validateNoLowercase() {
        String error = SecurityManager.validatePassword("TEST1234!");
        assertNotNull(error);
        assertTrue(error.contains("строчную"));
    }

    @Test
    @DisplayName("validatePassword — нет цифры")
    void validateNoDigit() {
        String error = SecurityManager.validatePassword("TestPass!");
        assertNotNull(error);
        assertTrue(error.contains("цифру"));
    }

    @Test
    @DisplayName("validatePassword — нет спецсимвола")
    void validateNoSpecial() {
        String error = SecurityManager.validatePassword("TestPass1");
        assertNotNull(error);
        assertTrue(error.contains("спецсимвол"));
    }

    // ─── isValidEmail ───────────────────────────────────────────────

    @Test
    @DisplayName("isValidEmail — корректные адреса")
    void validEmails() {
        assertTrue(SecurityManager.isValidEmail("user@example.com"));
        assertTrue(SecurityManager.isValidEmail("test.user+tag@domain.co.uk"));
        assertTrue(SecurityManager.isValidEmail("admin@digitalhub.local"));
    }

    @Test
    @DisplayName("isValidEmail — некорректные адреса")
    void invalidEmails() {
        assertFalse(SecurityManager.isValidEmail(null));
        assertFalse(SecurityManager.isValidEmail(""));
        assertFalse(SecurityManager.isValidEmail("user"));
        assertFalse(SecurityManager.isValidEmail("user@"));
        assertFalse(SecurityManager.isValidEmail("@domain.com"));
    }

    // ─── isValidPhone ───────────────────────────────────────────────

    @Test
    @DisplayName("isValidPhone — корректные номера")
    void validPhones() {
        assertTrue(SecurityManager.isValidPhone("+79001234567"));
        assertTrue(SecurityManager.isValidPhone("+70000000000"));
    }

    @Test
    @DisplayName("isValidPhone — некорректные номера")
    void invalidPhones() {
        assertFalse(SecurityManager.isValidPhone(null));
        assertFalse(SecurityManager.isValidPhone(""));
        assertFalse(SecurityManager.isValidPhone("89001234567")); // без +7
        assertFalse(SecurityManager.isValidPhone("+7900123456"));  // 10 цифр вместо 11
        assertFalse(SecurityManager.isValidPhone("+790012345678")); // 11 цифр
        assertFalse(SecurityManager.isValidPhone("+1234567890"));   // не +7
    }

    // ─── isValidUsername ────────────────────────────────────────────

    @Test
    @DisplayName("isValidUsername — корректные имена")
    void validUsernames() {
        assertTrue(SecurityManager.isValidUsername("Иван"));
        assertTrue(SecurityManager.isValidUsername("abc"));
        assertTrue(SecurityManager.isValidUsername("A".repeat(50)));
    }

    @Test
    @DisplayName("isValidUsername — некорректные имена")
    void invalidUsernames() {
        assertFalse(SecurityManager.isValidUsername(null));
        assertFalse(SecurityManager.isValidUsername(""));
        assertFalse(SecurityManager.isValidUsername("ab")); // меньше 3
        assertFalse(SecurityManager.isValidUsername("A".repeat(51))); // больше 50
    }
}
