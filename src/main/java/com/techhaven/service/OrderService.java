package com.techhaven.service;

import java.util.List;
import java.util.logging.Logger;

import com.techhaven.config.SessionManager;
import com.techhaven.model.CartItem;
import com.techhaven.model.Order;
import com.techhaven.model.OrderItem;
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

        // Шаг 6: Создание записи заказа в БД
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(com.techhaven.model.OrderStatus.NEW.getDisplayName());
        order.setDeliveryAddress(deliveryAddress.trim());
        order.setContactPhone(contactPhone.trim());
        order.setDeliveryTimeInterval(deliveryTimeInterval.trim());
        order.setComment(comment != null ? comment.trim() : null);
        order.setTotalAmount(total);
        order = orderRepo.create(order);

        if (order == null || order.getId() <= 0) {
            return MSG_CREATE_ERROR;
        }

        // Шаг 7: Создание позиций заказа и списание остатков со склада
        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem(
                order.getId(), item.getProductId(),
                item.getQuantity(), item.getProductPrice()
            );
            orderRepo.createOrderItem(orderItem);

            // Уменьшение остатков
            productRepo.updateStock(item.getProductId(), item.getQuantity());
        }

        // Шаг 8: Запись первого статуса в историю
        orderRepo.addStatusHistory(order.getId(), com.techhaven.model.OrderStatus.NEW.getDisplayName(), userId);

        // Шаг 9: Очистка корзины после успешного оформления
        cartRepo.clearCart(userId);

        LOGGER.info(String.format("Заказ #%d оформлен", order.getId()));
        return null; // null = success
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

    /** Допустимый порядок статусов (без «Отменён» — отмена допустима из любого) */
    private static final List<String> STATUS_ORDER = List.of(
        "Новый", "В обработке", "Подтверждён", "Собран", "Отправлен", "Доставлен", "Завершён"
    );

    static final String MSG_INVALID_TRANSITION = "Недопустимый переход статуса";

    /**
     * Обновляет статус заказа с проверкой корректности перехода.
     * @return null при успехе, строка ошибки при недопустимом переходе
     */
    public String updateStatus(int orderId, String newStatus) {
        Order order = orderRepo.findById(orderId);
        if (order == null) return "Заказ не найден";

        String currentStatus = order.getStatus();
        // Нормализуем е→ё (в БД хранится ё)
        String normCurrent = currentStatus.replace("Завершен", "Завершён")
            .replace("Отменен", "Отменён").replace("Подтвержден", "Подтверждён");
        String normNew = newStatus.replace("Завершен", "Завершён")
            .replace("Отменен", "Отменён").replace("Подтвержден", "Подтверждён");

        // Отмена допустима из любого статуса (кроме уже завершённых)
        if ("Отменён".equals(normNew)) {
            if ("Завершён".equals(normCurrent)) {
                return MSG_INVALID_TRANSITION + ": завершённый заказ нельзя отменить";
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

    public void updatePlannedDelivery(int orderId, String date, String interval) {
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
        // Фильтруем заказы с нетерминальным статусом (не "Доставлен", не "Отменен")
        return (int) orderRepo.findByUserId(userId).stream()
            .filter(o -> !com.techhaven.model.OrderStatus.fromString(o.getStatus()).isTerminal())
            .count();
    }
}
