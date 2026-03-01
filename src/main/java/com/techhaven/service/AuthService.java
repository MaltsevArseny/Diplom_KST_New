package com.techhaven.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import com.techhaven.config.SessionManager;
import com.techhaven.model.User;
import com.techhaven.repository.UserRepository;
import com.techhaven.security.SecurityManager;

/**
 * Сервис аутентификации и регистрации пользователей.
 *
 * <p>Реализует защиту от brute-force: блокировка аккаунта после
 * {@link com.techhaven.config.AppConfig#MAX_LOGIN_ATTEMPTS} неудачных попыток
 * на {@link com.techhaven.config.AppConfig#LOCK_DURATION_MINUTES} минут.</p>
 *
 * @see AuthResult
 */
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    /** Максимальное число неудачных попыток до блокировки */
    private static final int MAX_ATTEMPTS = com.techhaven.config.AppConfig.MAX_LOGIN_ATTEMPTS;
    /** Длительность блокировки в минутах после исчерпания попыток */
    private static final int LOCK_MINUTES = com.techhaven.config.AppConfig.LOCK_DURATION_MINUTES;

    private final UserRepository userRepo = new UserRepository();
    private final SecurityManager security = SecurityManager.getInstance();

    /**
     * Результат авторизации.
     * success=true + user — успех, success=false + message — ошибка.
     */
    public static class AuthResult {
        /** Успешность операции */
        public final boolean success;
        /** Сообщение для пользователя (ошибка или приветствие) */
        public final String message;
        /** Объект пользователя (null при ошибке) */
        public final User user;

        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }

    /**
     * Авторизация пользователя
     */
    public AuthResult login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "Введите email", null);
        }
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Введите пароль", null);
        }

        User user = userRepo.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            return new AuthResult(false, "Пользователь не найден", null);
        }

        // --- Проверка блокировки аккаунта ---
        String lockUntilStr = user.getLockUntil();
        if (lockUntilStr != null && !lockUntilStr.isEmpty()) {
            try {
                // Парсим дату блокировки с поддержкой двух форматов:
                String normalized = lockUntilStr.replace(" ", "T").replaceAll("\\.\\d+$", "");
                LocalDateTime lockUntil = LocalDateTime.parse(normalized,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                if (LocalDateTime.now().isBefore(lockUntil)) {
                    long days = java.time.Duration.between(LocalDateTime.now(), lockUntil).toDays();
                    // Если заблокирован администратором (99 лет) — не показываем таймер
                    String msg = days > 365
                        ? "Аккаунт заблокирован администратором."
                        : "Аккаунт заблокирован. Попробуйте через " +
                          (java.time.Duration.between(LocalDateTime.now(), lockUntil).toMinutes() + 1) + " мин.";
                    return new AuthResult(false, msg, null);
                } else {
                    // Блокировка истекла — сбрасываем
                    userRepo.updateFailedAttempts(user.getId(), 0, null);
                    user.setFailedAttempts(0);
                    user.setLockUntil(null);
                }
            } catch (Exception e) {
                // Формат даты непонятен — считаем заблокированным (безопаснее)
                return new AuthResult(false, "Аккаунт заблокирован администратором.", null);
            }
        } else if (user.getFailedAttempts() >= MAX_ATTEMPTS) {
            // failed_attempts >= 5, но lock_until пустой — считаем заблокированным
            return new AuthResult(false, "Аккаунт заблокирован. Обратитесь к администратору.", null);
        }

        // --- Проверка пароля через PBKDF2 ---
        if (!security.verifyPassword(password, user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                // Исчерпаны все попытки — блокируем на LOCK_MINUTES минут
                String lockUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                userRepo.updateFailedAttempts(user.getId(), attempts, lockUntil);
                return new AuthResult(false,
                    "Аккаунт заблокирован на " + LOCK_MINUTES + " минут после " + MAX_ATTEMPTS + " неудачных попыток", null);
            } else {
                userRepo.updateFailedAttempts(user.getId(), attempts, null);
                int remaining = MAX_ATTEMPTS - attempts;
                return new AuthResult(false,
                    "Неверный пароль. Осталось попыток: " + remaining, null);
            }
        }

        // --- Успешный вход — сбрасываем счётчик неудачных попыток ---
        if (user.getFailedAttempts() > 0) {
            userRepo.updateFailedAttempts(user.getId(), 0, null);
        }

        // Обновляем last_login и сохраняем сессию
        userRepo.updateLastLogin(user.getId());
        SessionManager.getInstance().login(user);
        LOGGER.log(java.util.logging.Level.INFO, "Пользователь вошёл: {0}", user.getEmail());
        return new AuthResult(true, "Добро пожаловать, " + user.getUsername() + "!", user);
    }

    /**
     * Регистрация нового пользователя
     */
    public AuthResult register(String username, String email, String phone, String password) {
        // Валидация
        if (!SecurityManager.isValidUsername(username)) {
            return new AuthResult(false, "Имя пользователя должно быть от 3 до 50 символов", null);
        }
        if (!SecurityManager.isValidEmail(email)) {
            return new AuthResult(false, "Некорректный формат email", null);
        }
        if (!SecurityManager.isValidPhone(phone)) {
            return new AuthResult(false, "Формат телефона: +7XXXXXXXXXX", null);
        }
        String passwordError = SecurityManager.validatePassword(password);
        if (passwordError != null) {
            return new AuthResult(false, passwordError, null);
        }
        if (userRepo.emailExists(email.trim().toLowerCase())) {
            return new AuthResult(false, "Пользователь с таким email уже существует", null);
        }

        String hash = security.hashPassword(password);
        User user = new User(username.trim(), email.trim().toLowerCase(), phone.trim(), hash, com.techhaven.model.Role.USER.getValue());
        user = userRepo.create(user);

        if (user != null && user.getId() > 0) {
            SessionManager.getInstance().login(user);
            LOGGER.log(java.util.logging.Level.INFO, "Новый пользователь: {0}", user.getEmail());
            return new AuthResult(true, "Регистрация успешна!", user);
        }

        return new AuthResult(false, "Ошибка регистрации. Попробуйте позже.", null);
    }

    /**
     * Выход
     */
    public void logout() {
        SessionManager.getInstance().logout();
    }
}
