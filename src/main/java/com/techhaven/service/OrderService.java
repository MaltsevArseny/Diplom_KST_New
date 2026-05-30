package com.techhaven.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.techhaven.config.DatabaseManager;
import com.techhaven.config.SessionManager;
import com.techhaven.model.CartItem;
import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
import com.techhaven.model.OrderStatus;
import com.techhaven.model.OrderStatusHistory;
import com.techhaven.model.Product;
import com.techhaven.repository.CartRepository;
import com.techhaven.repository.OrderRepository;
import com.techhaven.repository.ProductRepository;

/**
 * Сервис управления заказами.
 *
 * <p>Основной метод — {@link #placeOrder}: валидация → проверка остатков →
 * создание Order + OrderItem → списание со склада → очистка корзины.
 * Возвращает null при успехе или строку ошибки.</p>
 */
public class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    // --- Константы сообщений валидации (используются в тестах для assert) ---
    static final String MSG_NOT_LOGGED_IN = "Необходимо войти в систему";
    static final String MSG_EMPTY_ADDRESS = "Введите адрес доставки";
    static final String MSG_INVALID_PHONE = "Некорректный контактный телефон (+7XXXXXXXXXX)";
    static final String MSG_EMPTY_INTERVAL = "Выберите интервал доставки";
    static final String MSG_EMPTY_CART = "Корзина пуста";
    static final String MSG_CREATE_ERROR = "Ошибка создания заказа";
    static final String MSG_STOCK_RACE = "Товар закончился, пока вы оформляли заказ: ";
    static final String MSG_EMPTY_RECEIPT_CODE = "Введите штрих-код или цифровой код заказа";
    static final String MSG_ORDER_NOT_FOUND_BY_CODE = "Заказ с таким кодом не найден";
    static final String MSG_ORDER_ALREADY_ISSUED = "Заказ уже выдан";
    static final String MSG_CANCELLED_ORDER_CANNOT_BE_ISSUED = "Отменённый заказ нельзя выдать";
    static final String MSG_ORDER_NOT_DELIVERED = "Заказ можно выдать только в статусе «Доставлен»";
    static final String MSG_ISSUE_ERROR = "Ошибка выдачи заказа";

    private final OrderRepository orderRepo = new OrderRepository();
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();

    /**
     * Оформление заказа из корзины.
     *
     * @return null при успехе или строка ошибки для отображения пользователю
     */
    public String placeOrder(String deliveryAddress, String contactPhone, String deliveryTimeInterval, String comment) {
        // Шаг 1: Проверка авторизации
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId < 0) return MSG_NOT_LOGGED_IN;

        // Шаг 2: Валидация полей (адрес, телефон +7XXXXXXXXXX, интервал)
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            return MSG_EMPTY_ADDRESS;
        }
        if (contactPhone == null || !contactPhone.matches("^\\+7\\d{10}$")) {
            return MSG_INVALID_PHONE;
        }
        if (deliveryTimeInterval == null || deliveryTimeInterval.trim().isEmpty()) {
            return MSG_EMPTY_INTERVAL;
        }

        // Шаг 3: Проверка наличия товаров в корзине
        List<CartItem> cartItems = cartRepo.findByUserId(userId);
        if (cartItems.isEmpty()) {
            return MSG_EMPTY_CART;
        }

        // Шаг 4: Проверка остатков на складе для каждого товара
        for (CartItem item : cartItems) {
            Product product = productRepo.findById(item.getProductId());
            if (product == null) {
                return "Товар \"" + item.getProductName() + "\" больше не доступен";
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                return "Недостаточно на складе: " + item.getProductName() +
                       " (доступно: " + product.getStockQuantity() + ")";
            }
        }

        // Шаг 5: Расчёт итоговой суммы заказа
        double total = cartItems.stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum();

        // Шаг 6-9: Атомарное оформление заказа в одной транзакции.
        // Если на любом шаге происходит сбой (включая race на остатках —
        // см. ProductRepository.decrementStock), вся транзакция откатывается:
        // ни заказ, ни позиции, ни история, ни списание стока в БД не остаются.
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(com.techhaven.model.OrderStatus.NEW.getDisplayName());
        order.setDeliveryAddress(deliveryAddress.trim());
        order.setContactPhone(contactPhone.trim());
        order.setDeliveryTimeInterval(deliveryTimeInterval.trim());
        order.setComment(comment != null ? comment.trim() : null);
        order.setTotalAmount(total);

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Order created = orderRepo.create(conn, order);
                if (created == null || created.getId() <= 0) {
                    conn.rollback();
                    return MSG_CREATE_ERROR;
                }

                for (CartItem item : cartItems) {
                    OrderItem orderItem = new OrderItem(
                        created.getId(), item.getProductId(),
                        item.getQuantity(), item.getProductPrice()
                    );
                    orderRepo.createOrderItem(conn, orderItem);

                    // Атомарное списание: UPDATE ... WHERE stock_quantity >= qty.
                    // Если на складе уже меньше (параллельная транзакция успела
                    // забрать товар) — UPDATE затронет 0 строк, откатываем всё.
                    boolean ok = productRepo.decrementStock(conn, item.getProductId(), item.getQuantity());
                    if (!ok) {
                        conn.rollback();
                        return MSG_STOCK_RACE + item.getProductName();
                    }
                }

                orderRepo.addStatusHistory(conn, created.getId(),
                    com.techhaven.model.OrderStatus.NEW.getDisplayName(), userId);
                cartRepo.clearCart(conn, userId);

                conn.commit();
                LOGGER.info(String.format("Заказ #%d оформлен", created.getId()));
                return null; // null = success
            } catch (SQLException txEx) {
                try { conn.rollback(); } catch (SQLException ignore) {}
                LOGGER.log(Level.SEVERE, "Ошибка транзакции оформления заказа", txEx);
                return MSG_CREATE_ERROR;
            }
        } catch (SQLException connEx) {
            LOGGER.log(Level.SEVERE, "Не удалось открыть транзакцию для оформления заказа", connEx);
            return MSG_CREATE_ERROR;
        }
    }

    /**
     * Получить ранее использованные телефоны (для выбора при оформлении)
     */
    public List<String> getUsedPhones() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return orderRepo.findUsedPhones(userId);
    }

    /**
     * Получить последний заказ (для автозаполнения)
     */
    public Order getLastOrder() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return orderRepo.findLastByUserId(userId);
    }

    public List<Order> getMyOrders() {
        return orderRepo.findByUserId(SessionManager.getInstance().getCurrentUserId());
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public List<OrderItem> getOrderItems(int orderId) {
        return orderRepo.findOrderItems(orderId);
    }

    /**
     * Ищет заказ по данным получения: штрих-коду или цифровому коду.
     *
     * @throws SecurityException если вызывающий не имеет роли ADMIN
     */
    public Order findOrderByReceiptCode(String receiptCode) {
        SessionManager.getInstance().requireAdmin();
        int orderId = OrderReceiptService.parseOrderId(receiptCode);
        return orderId > 0 ? orderRepo.findById(orderId) : null;
    }

    /**
     * Выдаёт заказ клиенту по штрих-коду или цифровому коду.
     *
     * <p>Выдача со склада является отдельной admin-операцией и переводит
     * найденный доставленный заказ в статус «Выдан» с записью в
     * историю статусов. Ручная последовательная смена статусов остаётся
     * строгой и проверяется в {@link #updateStatus(int, String)}.</p>
     *
     * @return null при успехе или строка ошибки для отображения администратору
     * @throws SecurityException если вызывающий не имеет роли ADMIN
     */
    public String issueOrderByReceiptCode(String receiptCode) {
        SessionManager session = SessionManager.getInstance();
        session.requireAdmin();

        int orderId = OrderReceiptService.parseOrderId(receiptCode);
        if (orderId <= 0) return MSG_EMPTY_RECEIPT_CODE;

        Order order = orderRepo.findById(orderId);
        if (order == null) return MSG_ORDER_NOT_FOUND_BY_CODE;

        OrderStatus currentStatus = OrderStatus.fromString(order.getStatus());
        if (currentStatus == OrderStatus.ISSUED || currentStatus == OrderStatus.COMPLETED) return MSG_ORDER_ALREADY_ISSUED;
        if (currentStatus == OrderStatus.CANCELLED) return MSG_CANCELLED_ORDER_CANNOT_BE_ISSUED;
        if (currentStatus != OrderStatus.DELIVERED) return MSG_ORDER_NOT_DELIVERED;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                String issued = OrderStatus.ISSUED.getDisplayName();
                orderRepo.updateStatus(conn, orderId, issued);
                orderRepo.addStatusHistory(conn, orderId, issued, session.getCurrentUserId());
                conn.commit();
                LOGGER.info(String.format("Заказ #%d выдан клиенту", orderId));
                return null;
            } catch (SQLException txEx) {
                try { conn.rollback(); } catch (SQLException ignore) {}
                LOGGER.log(Level.SEVERE, "Ошибка транзакции выдачи заказа", txEx);
                return MSG_ISSUE_ERROR;
            }
        } catch (SQLException connEx) {
            LOGGER.log(Level.SEVERE, "Не удалось открыть транзакцию для выдачи заказа", connEx);
            return MSG_ISSUE_ERROR;
        }
    }

    /** Допустимый порядок статусов (без «Отменён» — отмена допустима из любого) */
    private static final List<String> STATUS_ORDER = List.of(
        "Новый", "В обработке", "Подтверждён", "Собран", "Отправлен", "Доставлен", "Выдан", "Завершён"
    );

    static final String MSG_INVALID_TRANSITION = "Недопустимый переход статуса";

    /**
     * Обновляет статус заказа с проверкой корректности перехода.
     *
     * <p>Право вызова: ADMIN всегда; USER только если отменяет собственный
     * незавершённый заказ (newStatus = «Отменён» и order.userId совпадает
     * с текущей сессией).</p>
     *
     * @return null при успехе, строка ошибки при недопустимом переходе
     * @throws SecurityException если у вызывающего нет прав на эту операцию
     */
    public String updateStatus(int orderId, String newStatus) {
        Order order = orderRepo.findById(orderId);
        if (order == null) return "Заказ не найден";

        // ── Проверка прав: ADMIN или владелец-USER при отмене ────────────────
        String checkNorm = newStatus == null ? "" : newStatus
            .replace("Завершен", "Завершён")
            .replace("Отменен", "Отменён")
            .replace("Подтвержден", "Подтверждён");
        boolean isCancel = "Отменён".equals(checkNorm);
        SessionManager session = SessionManager.getInstance();
        boolean isOwner = session.getCurrentUserId() == order.getUserId();
        if (!session.isAdmin() && !(isCancel && isOwner)) {
            throw new SecurityException("Изменение статуса доступно администратору; "
                + "владелец заказа может только отменить свой незавершённый заказ");
        }

        String currentStatus = order.getStatus();
        // Нормализуем е→ё (в БД хранится ё)
        String normCurrent = currentStatus.replace("Завершен", "Завершён")
            .replace("Отменен", "Отменён").replace("Подтвержден", "Подтверждён");
        String normNew = newStatus.replace("Завершен", "Завершён")
            .replace("Отменен", "Отменён").replace("Подтвержден", "Подтверждён");

        // Отмена допустима из любого статуса до фактической выдачи или закрытия.
        if ("Отменён".equals(normNew)) {
            if ("Выдан".equals(normCurrent) || "Завершён".equals(normCurrent)) {
                return MSG_INVALID_TRANSITION + ": выданный или завершённый заказ нельзя отменить";
            }
            orderRepo.updateStatus(orderId, normNew);
            orderRepo.addStatusHistory(orderId, normNew,
                SessionManager.getInstance().getCurrentUserId());
            return null;
        }

        int currentIdx = STATUS_ORDER.indexOf(normCurrent);
        int newIdx = STATUS_ORDER.indexOf(normNew);

        // Проверяем, что переход — строго на следующий статус
        if (currentIdx < 0 || newIdx < 0 || newIdx != currentIdx + 1) {
            return MSG_INVALID_TRANSITION + ": из «" + currentStatus + "» можно перейти только в «"
                + (currentIdx >= 0 && currentIdx < STATUS_ORDER.size() - 1
                    ? STATUS_ORDER.get(currentIdx + 1) : "—") + "»";
        }

        orderRepo.updateStatus(orderId, normNew);
        orderRepo.addStatusHistory(orderId, normNew,
            SessionManager.getInstance().getCurrentUserId());
        return null;
    }

    /**
     * Назначает плановую дату и интервал доставки заказа.
     *
     * @throws SecurityException если вызывающий не имеет роли ADMIN
     */
    public void updatePlannedDelivery(int orderId, String date, String interval) {
        SessionManager.getInstance().requireAdmin();
        orderRepo.updatePlannedDelivery(orderId, date, interval);
        // Не добавляем запись в OrderStatusHistory — это не смена статуса,
        // а назначение даты доставки (факт фиксируется в поле planned_delivery_date)
    }

    public List<OrderStatusHistory> getStatusHistory(int orderId) {
        return orderRepo.getStatusHistory(orderId);
    }

    public Order getOrderById(int orderId) {
        return orderRepo.findById(orderId);
    }

    /**
     * Количество незавершённых заказов текущего пользователя.
     * Используется для бейджа в навигации («Заказы (3)»).
     */
    public int getIncompleteOrdersCount() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId < 0) return 0;
        // Фильтруем заказы с нетерминальным статусом.
        return (int) orderRepo.findByUserId(userId).stream()
            .filter(o -> !com.techhaven.model.OrderStatus.fromString(o.getStatus()).isTerminal())
            .count();
    }
}
