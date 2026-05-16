package com.techhaven.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Менеджер безопасности приложения (Singleton).
 *
 * <p>Обеспечивает:
 * <ul>
 *   <li>Хеширование паролей (PBKDF2 + SHA-256)</li>
 *   <li>Шифрование/расшифрование PII-данных (AES-256-CBC)</li>
 *   <li>Валидацию полей (email, телефон, пароль, имя)</li>
 * </ul></p>
 */
public class SecurityManager {
    private static final Logger LOGGER = Logger.getLogger(SecurityManager.class.getName());

    /** Количество итераций PBKDF2 — чем больше, тем медленнее brute-force */
    private static final int PBKDF2_ITERATIONS = com.techhaven.config.AppConfig.PBKDF2_ITERATIONS;
    /** Длина соли в байтах (16 байт = 128 бит) */
    private static final int SALT_LENGTH = com.techhaven.config.AppConfig.SALT_LENGTH;
    /** Длина выходного ключа в битах (256 бит для AES-256) */
    private static final int KEY_LENGTH = com.techhaven.config.AppConfig.KEY_LENGTH;
    /** Алгоритм деривации ключа из пароля */
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    /** Алгоритм симметричного шифрования: AES в режиме CBC с PKCS5 padding */
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static SecurityManager instance;
    /** Ключ AES-256 для шифрования PII (телефон, адрес) */
    private SecretKey aesKey;

    private SecurityManager() {
        initializeAESKey();
    }

    public static synchronized SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }

    /**
     * Генерирует детерминированный AES-256 ключ из фиксированной парольной фразы.
     * В продакшене ключ следует загружать из зашифрованного хранилища (Vault, KMS).
     */
    private void initializeAESKey() {
        try {
            String passphrase = "DigitalHub-AES-Key-2024";
            byte[] salt = "DigitalHubSalt12".getBytes(StandardCharsets.UTF_8);
            // Деривация ключа: passphrase → PBKDF2 → 256-bit AES ключ
            PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 10000, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            aesKey = new SecretKeySpec(keyBytes, "AES");
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Ошибка инициализации AES ключа", e);
        }
    }

    /**
     * Хеширование пароля с помощью PBKDF2WithHmacSHA256
     */
    public String hashPassword(String password) {
        try {
            // 1. Генерируем криптографически стойкую случайную соль
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // 2. Деривация хеша: пароль + соль → PBKDF2 → 256-bit хеш
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // 3. Формат хранения: "Base64(соль):Base64(хеш)"
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return saltBase64 + ":" + hashBase64;
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Ошибка хеширования пароля", e);
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }

    /**
     * Проверка пароля
     */
    public boolean verifyPassword(String password, String storedHash) {
        try {
            // Разбираем формат "Base64(соль):Base64(хеш)"
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            // Хешируем введённый пароль с той же солью
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            // Сравнение за постоянное время (защита от timing-атак)
            if (actualHash.length != expectedHash.length) return false;
            int result = 0;
            for (int i = 0; i < actualHash.length; i++) {
                result |= actualHash[i] ^ expectedHash[i];
            }
            return result == 0;
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Ошибка проверки пароля", e);
            return false;
        }
    }

    /**
     * Шифрование PII данных с помощью AES-256
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            // Случайный IV (вектор инициализации) для каждого шифрования
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Формат: "Base64(IV):Base64(шифротекст)" — IV нужен для расшифровки
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            String encBase64 = Base64.getEncoder().encodeToString(encrypted);
            return ivBase64 + ":" + encBase64;
        } catch (java.security.NoSuchAlgorithmException | javax.crypto.NoSuchPaddingException | java.security.InvalidKeyException | java.security.InvalidAlgorithmParameterException | javax.crypto.IllegalBlockSizeException | javax.crypto.BadPaddingException e) {
            LOGGER.log(Level.SEVERE, "Ошибка шифрования", e);
            return plainText; // Fallback
        }
    }

    /**
     * Расшифрование PII данных
     */
    public String decrypt(String encryptedText) {
        try {
            // Если текст не зашифрован (нет разделителя ":" ) — возвращаем как есть
            if (encryptedText == null || encryptedText.isEmpty()) return encryptedText;
            if (!encryptedText.contains(":")) return encryptedText;
            String[] parts = encryptedText.split(":");
            if (parts.length != 2) return encryptedText;

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (java.security.NoSuchAlgorithmException | javax.crypto.NoSuchPaddingException | java.security.InvalidKeyException | java.security.InvalidAlgorithmParameterException | javax.crypto.IllegalBlockSizeException | javax.crypto.BadPaddingException e) {
            LOGGER.log(Level.SEVERE, "Ошибка расшифрования", e);
            return encryptedText; // Fallback
        }
    }

    /**
     * Валидация пароля по критериям ТЗ
     */
    public static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Пароль должен содержать минимум 8 символов";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Пароль должен содержать минимум 1 заглавную букву";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Пароль должен содержать минимум 1 строчную букву";
        }
        if (!password.matches(".*\\d.*")) {
            return "Пароль должен содержать минимум 1 цифру";
        }
        if (!password.matches(".*[!@$%^&].*")) {
            return "Пароль должен содержать минимум 1 спецсимвол (!@$%^&)";
        }
        return null; // Valid
    }

    /**
     * Валидация email по RFC 5322 simplified
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Валидация телефона +7XXXXXXXXXX
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("^\\+7\\d{10}$");
    }

    /**
     * Валидация имени пользователя 3-50 символов
     */
    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && username.length() <= 50;
    }
}
