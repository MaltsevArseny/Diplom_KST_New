package com.techhaven.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.Label;

/**
 * Общие UI-правила для экранов заказов.
 */
final class OrderUiSupport {
    static final int ORDER_CARD_GAP = 6;
    static final int ORDER_CARD_BASE_FONT_SIZE = 12;

    private static final DateTimeFormatter DISPLAY_DATE =
        DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter SOURCE_DATE_TIME_SECONDS =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SOURCE_DATE_TIME_MINUTES =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private OrderUiSupport() {}

    static int cardColumnsForWidth(double width) {
        if (width >= 1500) return 5;
        if (width >= 1150) return 4;
        if (width >= 850) return 3;
        if (width >= 560) return 2;
        return 1;
    }

    static double cardWidthForContainer(double width) {
        int columns = cardColumnsForWidth(width);
        double gaps = Math.max(0, columns - 1) * ORDER_CARD_GAP;
        return Math.max(220, (Math.max(width, 220) - gaps) / columns);
    }

    static String formatDate(String raw) {
        if (raw == null || raw.isBlank()) return "—";
        try {
            LocalDate d = LocalDate.parse(raw.substring(0, Math.min(10, raw.length())));
            return d.format(DISPLAY_DATE);
        } catch (Exception e) {
            return raw;
        }
    }

    static String formatDateTime(LocalDateTime value) {
        return value == null ? "—" : value.format(DISPLAY_DATE_TIME);
    }

    static String formatDateTime(String raw) {
        if (raw == null || raw.isBlank()) return "—";
        String normalized = raw.trim().replace('T', ' ').replaceAll("\\.\\d+$", "");
        try {
            if (normalized.length() >= 19) {
                LocalDateTime ldt = LocalDateTime.parse(
                    normalized.substring(0, 19), SOURCE_DATE_TIME_SECONDS);
                return ldt.format(DISPLAY_DATE_TIME);
            }
            if (normalized.length() >= 16) {
                LocalDateTime ldt = LocalDateTime.parse(
                    normalized.substring(0, 16), SOURCE_DATE_TIME_MINUTES);
                return ldt.format(DISPLAY_DATE_TIME);
            }
            return formatDate(normalized);
        } catch (Exception e) {
            return raw;
        }
    }

    static String statusColor(String status) {
        if (status == null) return "#6b7280";
        return switch (status) {
            case "Новый"       -> "#3b82f6";
            case "В обработке" -> "#f97316";
            case "Подтверждён" -> "#a855f7";
            case "Собран"      -> "#eab308";
            case "Отправлен"   -> "#ec4899";
            case "Доставлен"   -> "#22c55e";
            case "Выдан"       -> "#14b8a6";
            case "Завершён"    -> "#0f766e";
            case "Отменён"     -> "#ef4444";
            default            -> "#6b7280";
        };
    }

    static String statusBadgeStyle(String status) {
        return "-fx-background-color:" + statusColor(status) + ";-fx-text-fill:-th-cream;" +
            "-fx-font-weight:bold;-fx-padding:2 8;-fx-background-radius:6;-fx-font-size:13px;";
    }

    static Label statusBadge(String status) {
        Label badge = new Label(status == null ? "—" : status);
        badge.setStyle(statusBadgeStyle(status));
        return badge;
    }
}
