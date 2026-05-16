-- ============================================================
-- Схема базы данных TechHaven (SQLite)
-- Версия: 3.1
-- Нормальная форма: 3NF
-- ============================================================
-- Все даты/время хранятся в формате ISO-8601 (TEXT), т.к. SQLite
-- не имеет встроенного типа DATETIME. При записи используется
-- datetime('now','localtime') для хранения локального времени.
-- ============================================================
PRAGMA foreign_keys = ON;
-- ────────────────────────────────────────────────────────────
-- Справочник категорий товаров (3NF — вынесен из Products)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор категории
    name TEXT NOT NULL UNIQUE -- Название категории (Процессоры, Видеокарты и т.д.)
);
-- ────────────────────────────────────────────────────────────
-- Справочник статусов заказов (3NF — вынесен из Orders)
-- Жизненный цикл: Новый → В обработке → Подтверждён → Собран
--                 → Отправлен → Доставлен → Завершён
-- Альтернативный финал: Отменён (из любого нетерминального)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS OrderStatuses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор статуса
    name TEXT NOT NULL UNIQUE,
    -- Отображаемое название статуса (на русском)
    sort_order INTEGER NOT NULL DEFAULT 0 -- Порядок сортировки в UI (0 = первый)
);
-- ────────────────────────────────────────────────────────────
-- Пользователи (покупатели и администраторы)
-- Пароли хешируются PBKDF2WithHmacSHA256 с солью.
-- Email и телефон шифруются AES-256-CBC (PII-данные).
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор пользователя
    username TEXT NOT NULL,
    -- Имя пользователя (3–50 символов)
    email TEXT NOT NULL UNIQUE,
    -- Email (зашифрован AES-256, уникален)
    phone TEXT NOT NULL,
    -- Телефон в формате +7XXXXXXXXXX (зашифрован)
    password_hash TEXT NOT NULL,
    -- Хеш пароля (PBKDF2, формат: salt:hash)
    role TEXT NOT NULL DEFAULT 'USER',
    -- Роль: 'USER' (покупатель) или 'ADMIN'
    failed_attempts INTEGER DEFAULT 0,
    -- Счётчик неудачных попыток входа (brute-force защита)
    lock_until TEXT,
    -- Дата/время блокировки после 5 неудачных попыток
    last_login TEXT,
    -- Дата/время последнего успешного входа
    block_reason TEXT,
    -- Причина блокировки администратором (NULL = не заблокирован)
    created_at TEXT DEFAULT (datetime('now')),
    -- Дата/время регистрации
    updated_at TEXT DEFAULT (datetime('now')) -- Дата/время последнего обновления профиля
);
-- ────────────────────────────────────────────────────────────
-- Товары (каталог интернет-магазина электроники)
-- category_id ссылается на справочник Categories (3NF).
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор товара
    name TEXT NOT NULL,
    -- Название товара (уникально, см. индекс ниже)
    description TEXT,
    -- Краткое описание товара
    category_id INTEGER NOT NULL REFERENCES Categories(id),
    -- FK → Categories.id
    price REAL CHECK (price IS NULL OR price >= 0),
    -- Цена товара в рублях (₽), не может быть отрицательной
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0),
    -- Остаток на складе (шт.), не может уйти в минус
    specifications TEXT,
    -- Технические характеристики (формат: ключ:значение через ;)
    image_path TEXT,
    -- Относительный путь к изображению товара
    created_at TEXT DEFAULT (datetime('now')),
    -- Дата/время добавления товара
    updated_at TEXT DEFAULT (datetime('now')) -- Дата/время последнего изменения
);
-- ────────────────────────────────────────────────────────────
-- Заказы
-- status_id ссылается на справочник OrderStatuses (3NF).
-- Покупатель указывает желаемую дату/время доставки
-- (delivery_time_interval), администратор назначает
-- фактическую (planned_delivery_date + planned_delivery_interval).
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный номер заказа
    user_id INTEGER,
    -- FK → Users.id (кто оформил)
    order_date TEXT DEFAULT (datetime('now')),
    -- Дата/время оформления заказа
    status_id INTEGER NOT NULL REFERENCES OrderStatuses(id),
    -- FK → OrderStatuses.id
    delivery_address TEXT,
    -- Адрес доставки (указывает покупатель)
    contact_phone TEXT,
    -- Контактный телефон для курьера
    delivery_time_interval TEXT,
    -- Желаемый интервал доставки (от покупателя)
    comment TEXT,
    -- Комментарий покупателя к заказу
    planned_delivery_date TEXT,
    -- Плановая дата доставки (назначает администратор)
    planned_delivery_interval TEXT,
    -- Плановый интервал доставки (назначает администратор)
    total_amount REAL CHECK (total_amount IS NULL OR total_amount >= 0),
    -- Итоговая сумма заказа в рублях (₽), не может быть отрицательной
    created_at TEXT DEFAULT (datetime('now')),
    -- Системная дата создания записи
    updated_at TEXT DEFAULT (datetime('now')),
    -- Системная дата последнего изменения
    FOREIGN KEY(user_id) REFERENCES Users(id)
);
-- ────────────────────────────────────────────────────────────
-- Позиции заказа (состав заказа — товары и количество)
-- При удалении заказа позиции удаляются каскадно.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS OrderItems (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор позиции
    order_id INTEGER,
    -- FK → Orders.id (к какому заказу)
    product_id INTEGER,
    -- FK → Products.id (какой товар)
    quantity INTEGER CHECK (quantity IS NULL OR quantity > 0),
    -- Количество единиц товара (только положительное)
    price_at_order REAL CHECK (price_at_order IS NULL OR price_at_order >= 0),
    -- Цена за единицу на момент заказа (₽), не отрицательная
    created_at TEXT DEFAULT (datetime('now')),
    -- Дата/время добавления позиции
    FOREIGN KEY(order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY(product_id) REFERENCES Products(id)
);
-- ────────────────────────────────────────────────────────────
-- Корзина покупателя
-- Пара (user_id, product_id) логически уникальна.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Cart (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор записи
    user_id INTEGER,
    -- FK → Users.id (чья корзина)
    product_id INTEGER,
    -- FK → Products.id (какой товар)
    quantity INTEGER DEFAULT 1 CHECK (quantity > 0),
    -- Количество единиц (≥ 1)
    created_at TEXT DEFAULT (datetime('now')),
    -- Дата/время добавления в корзину
    updated_at TEXT DEFAULT (datetime('now')),
    -- Дата/время изменения количества
    FOREIGN KEY(user_id) REFERENCES Users(id),
    FOREIGN KEY(product_id) REFERENCES Products(id)
);
-- ────────────────────────────────────────────────────────────
-- Избранное (список желаний покупателя)
-- Пара (user_id, product_id) логически уникальна.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор записи
    user_id INTEGER,
    -- FK → Users.id (чьё избранное)
    product_id INTEGER,
    -- FK → Products.id (какой товар)
    created_at TEXT DEFAULT (datetime('now')),
    -- Дата/время добавления в избранное
    FOREIGN KEY(user_id) REFERENCES Users(id),
    FOREIGN KEY(product_id) REFERENCES Products(id)
);
-- ────────────────────────────────────────────────────────────
-- История изменений статусов заказов (аудит-лог)
-- Каждая смена статуса фиксируется отдельной записью.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS OrderStatusHistory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Уникальный идентификатор записи
    order_id INTEGER,
    -- FK → Orders.id (какой заказ)
    status_id INTEGER NOT NULL REFERENCES OrderStatuses(id),
    -- FK → OrderStatuses.id (новый статус)
    changed_at TEXT DEFAULT (datetime('now')),
    -- Дата/время смены статуса
    changed_by INTEGER,
    -- ID пользователя, изменившего статус (admin)
    FOREIGN KEY(order_id) REFERENCES Orders(id)
);
-- ────────────────────────────────────────────────────────────
-- Идемпотентная очистка возможных дублей перед созданием
-- UNIQUE-индексов (на случай мигрирующей БД, где индексов ещё
-- не было). Оставляем самую раннюю запись (минимальный id).
-- ────────────────────────────────────────────────────────────
DELETE FROM Cart WHERE id NOT IN (SELECT MIN(id) FROM Cart GROUP BY user_id, product_id);
DELETE FROM Favorites WHERE id NOT IN (SELECT MIN(id) FROM Favorites GROUP BY user_id, product_id);
-- ────────────────────────────────────────────────────────────
-- Индексы для ускорения частых запросов
-- ────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_users_email ON Users(email);
-- Поиск пользователя по email (логин)
CREATE INDEX IF NOT EXISTS idx_products_category ON Products(category_id);
-- Фильтр товаров по категории
CREATE INDEX IF NOT EXISTS idx_orders_user ON Orders(user_id);
-- Заказы конкретного пользователя
CREATE INDEX IF NOT EXISTS idx_orders_status ON Orders(status_id);
-- Фильтр заказов по статусу
CREATE INDEX IF NOT EXISTS idx_orderitems_order ON OrderItems(order_id);
-- Позиции конкретного заказа
CREATE UNIQUE INDEX IF NOT EXISTS idx_products_name ON Products(name);
-- Уникальность названия товара
CREATE UNIQUE INDEX IF NOT EXISTS idx_cart_user_product ON Cart(user_id, product_id);
-- Один товар у пользователя в корзине = одна запись (защита от логических дублей)
CREATE UNIQUE INDEX IF NOT EXISTS idx_favorites_user_product ON Favorites(user_id, product_id);
-- Один товар в избранном у пользователя = одна запись