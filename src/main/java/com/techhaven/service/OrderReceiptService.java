package com.techhaven.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.techhaven.model.Order;

/**
 * Формирует данные, по которым покупатель получает заказ на складе.
 */
public final class OrderReceiptService {
    private static final Pattern NON_DIGIT = Pattern.compile("\\D+");
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter SOURCE_DATE_TIME_SECONDS =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SOURCE_DATE_TIME_MINUTES =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private OrderReceiptService() {}

    /**
     * Восьмизначный цифровой код заказа. Основан на первичном ключе Orders.id.
     */
    public static String orderCode(Order order) {
        if (order == null || order.getId() <= 0) return "";
        return String.format("%08d", order.getId());
    }

    public static String orderCode(int orderId) {
        if (orderId <= 0) return "";
        return String.format("%08d", orderId);
    }

    public static String barcodeValue(Order order) {
        return orderCode(order);
    }

    public static String formatOrderDate(Order order) {
        return order == null ? "—" : formatOrderDate(order.getOrderDate());
    }

    public static String formatOrderDate(String raw) {
        LocalDateTime parsed = parseDateTime(raw);
        if (parsed == null) return "—";
        return parsed.format(DISPLAY_DATE_TIME);
    }

    public static String normalizeBarcodeDigits(String value) {
        if (value == null) return "";
        String digits = NON_DIGIT.matcher(value).replaceAll("");
        return digits.length() % 2 == 0 ? digits : "0" + digits;
    }

    /**
     * Извлекает номер заказа из данных, считанных со штрих-кода или введённых вручную.
     */
    public static int parseOrderId(String value) {
        if (value == null || value.isBlank()) return -1;
        String digits = NON_DIGIT.matcher(value).replaceAll("");
        if (digits.isEmpty()) return -1;
        try {
            long id = Long.parseLong(digits);
            return id > 0 && id <= Integer.MAX_VALUE ? (int) id : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Возвращает sequence code values для Code 128-C: Start C, пары цифр,
     * checksum и Stop. Используется JavaFX-компонентом штрих-кода.
     */
    public static List<Integer> code128CValues(String value) {
        String digits = normalizeBarcodeDigits(value);
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("Barcode value must contain digits");
        }

        List<Integer> values = new ArrayList<>();
        values.add(105); // Start Code C

        int checksum = 105;
        int position = 1;
        for (int i = 0; i < digits.length(); i += 2) {
            int pair = Integer.parseInt(digits.substring(i, i + 2));
            values.add(pair);
            checksum += pair * position;
            position++;
        }

        values.add(checksum % 103);
        values.add(106); // Stop
        return values;
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String normalized = raw.trim().replace('T', ' ').replaceAll("\\.\\d+$", "");

        try {
            if (normalized.length() >= 19) {
                return LocalDateTime.parse(normalized.substring(0, 19), SOURCE_DATE_TIME_SECONDS);
            }
            if (normalized.length() >= 16) {
                return LocalDateTime.parse(normalized.substring(0, 16), SOURCE_DATE_TIME_MINUTES);
            }
            if (normalized.length() >= 10) {
                return java.time.LocalDate.parse(normalized.substring(0, 10))
                    .atStartOfDay();
            }
        } catch (Exception ignored) {
            // Некорректную дату показываем как отсутствующую, не ломая экран заказа.
        }
        return null;
    }
}
