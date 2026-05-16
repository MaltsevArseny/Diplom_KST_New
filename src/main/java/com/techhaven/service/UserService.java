package com.techhaven.service;

import java.util.List;

import com.techhaven.config.SessionManager;
import com.techhaven.model.User;
import com.techhaven.repository.UserRepository;

/**
 * Сервис управления пользователями (admin-операции).
 *
 * <p>Все методы этого сервиса требуют роли ADMIN у текущей сессии
 * ({@link SessionManager#requireAdmin()}). Это defense-in-depth поверх
 * UI-уровня: даже если кнопка блокировки попала к обычному пользователю
 * по ошибке, операция не выполнится.</p>
 *
 * <p>Для операций самого пользователя над собственным профилем
 * (смена email, телефона, пароля) используется напрямую
 * {@link UserRepository#updateProfile(int, String, String, String)} —
 * там guard'ом служит UI-форма «Личный кабинет», где id фиксирован
 * текущей сессией.</p>
 */
public class UserService {

    private final UserRepository userRepo = new UserRepository();

    /** Список USER-пользователей (без админов). Admin-операция. */
    public List<User> listUsers() {
        SessionManager.getInstance().requireAdmin();
        return userRepo.findAllUsers();
    }

    /** Список всех пользователей включая админов. Admin-операция. */
    public List<User> listAllIncludingAdmins() {
        SessionManager.getInstance().requireAdmin();
        return userRepo.findAllIncludingAdmins();
    }

    /**
     * Блокирует пользователя (lockUntil +99 лет).
     * Admin-операция; защита от самоблокировки наследуется
     * от {@link UserRepository#lockUser(int, String)}.
     */
    public void lockUser(int userId, String reason) {
        SessionManager.getInstance().requireAdmin();
        userRepo.lockUser(userId, reason);
    }

    /** Снимает блокировку пользователя. Admin-операция. */
    public void unlockUser(int userId) {
        SessionManager.getInstance().requireAdmin();
        userRepo.unlockUser(userId);
    }
}
