package com.techhaven.model;

/**
 * Роль пользователя в системе.
 */
public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Парсинг из строки БД (case-insensitive, с fallback на USER).
     */
    public static Role fromString(String s) {
        if (s == null) return USER;
        for (Role r : values()) {
            if (r.value.equalsIgnoreCase(s)) return r;
        }
        return USER;
    }
}
