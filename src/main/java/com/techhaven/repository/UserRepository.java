package com.techhaven.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.techhaven.config.DatabaseManager;
import com.techhaven.model.User;

/**
 * Реализация доступа к таблице Users в SQLite.
 * Управляет учётными записями: CRUD, блокировка/разблокировка,
 * отслеживание неудачных попыток входа.
 */
public class UserRepository implements IUserRepository {
    private static final Logger LOGGER = Logger.getLogger(UserRepository.class.getName());
    private final DatabaseManager db = DatabaseManager.getInstance();

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка поиска пользователя по email", e);
        }
        return null;
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка поиска пользователя по id", e);
        }
        return null;
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка проверки email", e);
        }
        return false;
    }

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO Users (username, email, phone, password_hash, role,
                                  failed_attempts, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 0, datetime('now','localtime'), datetime('now','localtime'))""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, com.techhaven.security.SecurityManager.getInstance().encrypt(user.getPhone()));
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole() != null ? user.getRole() : com.techhaven.model.Role.USER.getValue());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
            LOGGER.log(Level.INFO, "Пользователь создан: {0}", user.getEmail());
            return user;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка создания пользователя", e);
        }
        return null;
    }

    @Override
    public void updateFailedAttempts(int userId, int attempts, String lockUntil) {
        String sql = "UPDATE Users SET failed_attempts = ?, lock_until = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setString(2, lockUntil);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления попыток входа", e);
        }
    }

    @Override
    public void updatePhone(int userId, String phone) {
        String sql = "UPDATE Users SET phone = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, com.techhaven.security.SecurityManager.getInstance().encrypt(phone));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления телефона", e);
        }
    }

    @Override
    public boolean updateProfile(int userId, String username, String email, String phone) {
        String sql = "UPDATE Users SET username = ?, email = ?, phone = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, com.techhaven.security.SecurityManager.getInstance().encrypt(phone));
            ps.setInt(4, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления профиля", e);
        }
        return false;
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE Users SET password_hash = ?, updated_at = datetime('now','localtime') WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка обновления пароля", e);
        }
        return false;
    }

    /** Проверяет, занят ли email другим пользователем (для смены email) */
    @Override
    public boolean emailExistsForOther(String email, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ? AND id != ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeUserId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка проверки email", e);
        }
        return false;
    }


    @Override
    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = 'USER' ORDER BY created_at DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения списка пользователей", e);
        }
        return users;
    }

    /** Возвращает всех пользователей независимо от роли: ADMIN первыми, потом USER */
    @Override
    public List<User> findAllIncludingAdmins() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY CASE role WHEN 'ADMIN' THEN 0 ELSE 1 END, created_at DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка получения всех пользователей", e);
        }
        return users;
    }


    /** Фиксирует дату и время последнего успешного входа (локальное время) */
    @Override
    public void updateLastLogin(int userId) {
        String now = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "UPDATE Users SET last_login = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, now);
            ps.setString(2, now);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка записи last_login", e);
        }
    }

    @Override
    public void unlockUser(int userId) {
        String sql = """
                UPDATE Users SET failed_attempts = 0, lock_until = NULL,
                       block_reason = NULL, updated_at = datetime('now','localtime')
                WHERE id = ?""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка разблокировки пользователя", e);
        }
    }

    /**
     * Блокирует пользователя вручную (lock_until +99 лет, локальное время).
     *
     * @throws IllegalArgumentException если администратор пытается заблокировать себя
     */
    @Override
    public void lockUser(int userId, String reason) {
        // Защита от самоблокировки: admin не должен мочь заблокировать себя
        int currentUserId = com.techhaven.config.SessionManager.getInstance().getCurrentUserId();
        if (currentUserId != -1 && userId == currentUserId) {
            throw new IllegalArgumentException("Администратор не может заблокировать собственную учётную запись");
        }

        String lockUntil = java.time.LocalDateTime.now().plusYears(99)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String safeReason = (reason == null || reason.isBlank()) ? "Заблокировано администратором" : reason.trim();
        String sql = """
                UPDATE Users SET failed_attempts = 5, lock_until = ?, block_reason = ?,
                       updated_at = datetime('now','localtime')
                WHERE id = ?""";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lockUntil);
            ps.setString(2, safeReason);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка блокировки пользователя", e);
        }
    }


    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(com.techhaven.security.SecurityManager.getInstance().decrypt(rs.getString("phone")));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setFailedAttempts(rs.getInt("failed_attempts"));
        user.setLockUntil(rs.getString("lock_until"));
        user.setBlockReason(rs.getString("block_reason"));
        String created = rs.getString("created_at");
        if (created != null) {
            try { user.setCreatedAt(LocalDateTime.parse(created.replace(" ", "T"))); } catch (Exception ignored) {}
        }
        user.setLastLogin(rs.getString("last_login"));
        return user;
    }
}
