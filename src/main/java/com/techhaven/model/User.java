package com.techhaven.model;

import java.time.LocalDateTime;

/**
 * Модель пользователя системы (покупатель или администратор).
 *
 * <p>Соответствует таблице {@code Users} в БД.
 * Пароль хранится в виде хеша PBKDF2 (формат {@code salt:hash}).
 * Email и телефон шифруются AES-256-CBC для защиты персональных данных.</p>
 *
 * @see com.techhaven.repository.UserRepository
 * @see com.techhaven.security.SecurityManager
 */
public class User {
    private int id;                   // PK — уникальный идентификатор
    private String username;          // Имя пользователя (3–50 символов)
    private String email;             // Email (зашифрован AES-256, уникален)
    private String phone;             // Телефон +7XXXXXXXXXX (зашифрован)
    private String passwordHash;      // Хеш пароля PBKDF2 (salt:hash)
    private String role;              // Роль: "USER" или "ADMIN"
    private int failedAttempts;       // Счётчик неудачных попыток входа (brute-force защита)
    private String lockUntil;         // Временная блокировка после 5 неудачных попыток
    private String blockReason;       // Причина блокировки администратором (null = активен)
    private LocalDateTime createdAt;  // Дата/время регистрации
    private LocalDateTime updatedAt;  // Дата/время последнего обновления профиля
    private String lastLogin;         // Дата/время последнего успешного входа

    public User() {}

    public User(String username, String email, String phone, String passwordHash, String role) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.failedAttempts = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public String getLockUntil() { return lockUntil; }
    public void setLockUntil(String lockUntil) { this.lockUntil = lockUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    public boolean isAdmin() { return com.techhaven.model.Role.ADMIN.getValue().equals(role); }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
