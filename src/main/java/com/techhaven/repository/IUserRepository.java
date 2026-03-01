package com.techhaven.repository;

import java.util.List;

import com.techhaven.model.User;

/**
 * Интерфейс репозитория пользователей.
 * Определяет контракт для доступа к данным пользователей,
 * включая аутентификацию, управление профилями и блокировку.
 */
public interface IUserRepository {
    /** Найти пользователя по ID. */
    User findById(int id);

    /** Найти пользователя по email (для аутентификации). */
    User findByEmail(String email);

    /** Проверить, зарегистрирован ли email. */
    boolean emailExists(String email);

    /** Создать нового пользователя. */
    User create(User user);

    /** Обновить счётчик неудачных попыток входа и время блокировки. */
    void updateFailedAttempts(int userId, int attempts, String lockUntil);

    /** Обновить дату последнего входа. */
    void updateLastLogin(int userId);

    /** Обновить телефон пользователя. */
    void updatePhone(int userId, String phone);

    /** Обновить профиль пользователя (имя, email, телефон). */
    boolean updateProfile(int userId, String username, String email, String phone);

    /** Обновить хеш пароля. */
    boolean updatePassword(int userId, String newPasswordHash);

    /** Проверить, используется ли email другим пользователем. */
    boolean emailExistsForOther(String email, int excludeUserId);

    /** Получить всех пользователей (кроме admin). */
    List<User> findAllUsers();

    /** Получить всех пользователей, включая администраторов. */
    List<User> findAllIncludingAdmins();

    /** Разблокировать пользователя. */
    void unlockUser(int userId);

    /** Заблокировать пользователя по причине. */
    void lockUser(int userId, String reason);
}
