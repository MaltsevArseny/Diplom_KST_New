package com.techhaven.config;

import com.techhaven.model.User;

/**
 * Хранилище текущей сессии пользователя (Singleton, in-memory).
 *
 * <p>После успешного входа ({@link #login}) сохраняет объект User
 * для доступа из любого места приложения. При выходе ({@link #logout}) — очищает.</p>
 */
public class SessionManager {
    private static SessionManager instance;
    /** Текущий авторизованный пользователь (или null) */
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /** @return ID пользователя или -1 если не авторизован */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
