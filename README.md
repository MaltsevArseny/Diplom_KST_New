# DigitalHub — Техническая документация

> **Аудитория**: разработчики · системные администраторы · аналитики · тестировщики · специалисты сопровождения  
> **Пользовательская документация**: [UserManual.md](UserManual.md) · [AdminManual.md](AdminManual.md)  
> **Техническое задание**: [ТЕХНИЧЕСКОЕ ЗАДАНИЕ.txt](ТЕХНИЧЕСКОЕ%20ЗАДАНИЕ.txt)

---

## 1. Обзор системы

DigitalHub — автономное desktop-приложение для продажи компьютерного оборудования.  
Работает **полностью offline**, не требует сетевого подключения.

| Параметр | Значение |
|---|---|
| Версия | 3.1 |
| Платформа | Windows 10/11 (64-bit) |
| Java | 21 LTS (OpenJDK / Temurin) |
| UI-фреймворк | JavaFX 21.0.5 |
| БД | SQLite 3 (через JDBC), схема в 3НФ |
| Сборка | Apache Maven 3.8+ |
| Паттерн | MVVM (Model–View–ViewModel) |
| Артефакт сборки | `dist/DigitalHub.jar` (FAT JAR) |

---

## 2. Структура проекта

```
Diplom_KST_New/
├── src/main/java/com/techhaven/
│   ├── MainApp.java              # Точка входа (JavaFX Application)
│   ├── Launcher.java             # Загрузчик для FAT JAR (обход classpath-ограничений JavaFX)
│   ├── model/                    # POJO-модели: User, Product, Order, OrderItem, CartItem, Favorite
│   ├── config/
│   │   ├── DatabaseManager.java  # Singleton: инициализация схемы, сидирование, миграции
│   │   ├── SeedProducts.java     # Генератор каталога 500 товаров
│   │   └── SessionManager.java   # Singleton: текущий авторизованный пользователь
│   ├── security/
│   │   └── SecurityManager.java  # PBKDF2 хеш/верификация, AES-256-CBC шифрование PII, валидация
│   ├── repository/               # Слой доступа к данным (JDBC + PreparedStatement)
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   ├── OrderRepository.java
│   │   └── ...
│   ├── service/                  # Бизнес-логика
│   │   ├── AuthService.java      # Аутентификация, блокировка аккаунта
│   │   ├── CartService.java
│   │   ├── FavoriteService.java
│   │   ├── OrderService.java
│   │   └── ProductService.java
│   └── view/                     # JavaFX экраны
│       ├── MainLayout.java       # Навбар + контент-область (пользователь)
│       ├── AdminLayout.java      # Топбар + сайдбар + контент-область (администратор)
│       ├── DialogHelper.java     # Утилита: стилизованные диалоги, Tooltip, стили
│       ├── HelpView.java         # Встроенная справка (Markdown → JavaFX рендер)
│       ├── LoginView.java / RegisterView.java
│       ├── CatalogView.java / CartView.java / FavoritesView.java
│       ├── CheckoutView.java / OrdersView.java / ProfileView.java
│       └── Admin*.java           # AdminProductsView, AdminOrdersView, AdminUsersView, AdminReportsView
├── src/main/resources/
│   └── styles/dark-theme.css     # Единственный CSS для JavaFX сцен
├── dist/
│   ├── DigitalHub.jar            # FAT JAR (генерируется при сборке)
│   ├── digitalhub.db             # Единственная БД приложения
│   └── DigitalHub.bat            # Запуск из папки dist/
├── run.bat                       # Сборка + запуск из корня проекта
├── pom.xml
├── UserManual.md                 # Руководство пользователя
├── AdminManual.md                # Руководство администратора
└── ТЕХНИЧЕСКОЕ ЗАДАНИЕ.txt
```

---

## 3. Зависимости (pom.xml)

| Библиотека | groupId / artifactId | Версия | Назначение |
|---|---|---|---|
| JavaFX Controls | `org.openjfx:javafx-controls` | 21.0.5 | UI-компоненты |
| JavaFX Graphics | `org.openjfx:javafx-graphics` | 21.0.5 | Графический движок |
| JavaFX FXML | `org.openjfx:javafx-fxml` | 21.0.5 | FXML-поддержка |
| SQLite JDBC | `org.xerial:sqlite-jdbc` | 3.42.0.0 | Драйвер SQLite |
| Maven Shade | `org.apache.maven.plugins:maven-shade-plugin` | — | Сборка FAT JAR |

---

## 4. Сборка и запуск

### 4.1 Требования окружения

```
Java JDK 21+  — https://adoptium.net/
Maven  3.8+   — https://maven.apache.org/
```

Проверка: `java -version` и `mvn --version`.

### 4.2 Способы запуска

| Способ | Команда / Файл | Рабочая директория |
|---|---|---|
| Основной скрипт | `run.bat` | Корень проекта |
| Из дистрибутива | `dist\DigitalHub.bat` | `dist\` |
| Напрямую | `java -Ddb.path=dist/digitalhub.db -jar dist\DigitalHub.jar` | Корень проекта |
| Maven compile only | `mvn compile` | Корень проекта |

### 4.3 Что делает `run.bat`

1. `mvn clean package -q` — полная сборка, FAT JAR → `dist/DigitalHub.jar`
2. `java -Ddb.path=dist/digitalhub.db -jar dist\DigitalHub.jar` — запуск

> **Системное свойство `-Ddb.path`** — путь к БД, читается в `DatabaseManager.java`.  
> Default: `dist/digitalhub.db`. При запуске из `dist\DigitalHub.bat` передаётся `db.path=digitalhub.db`.

---

## 5. База данных

| Параметр | Значение |
|---|---|
| СУБД | SQLite 3 |
| Единственный файл | `dist/digitalhub.db` |
| Строка подключения | `jdbc:sqlite:<db.path>` |
| Нормализация | Третья нормальная форма (3НФ) |
| Инициализация | `DatabaseManager.initializeDatabase()` при первом старте |
| Сидирование | Автоматически при первом запуске: заполняет каталог 500 товарами |

### Схема БД (3НФ)

| Таблица | Ключевые поля | Назначение |
|---|---|---|
| `Categories` | id, name | Справочник категорий (11 записей) |
| `OrderStatuses` | id, name, sort\_order | Справочник статусов заказов (8 записей) |
| `Users` | id, username, email, phone, password\_hash, role, failed\_attempts, lock\_until | Пользователи |
| `Products` | id, name, **category\_id** (FK → Categories), price, stock\_quantity, description, specifications | Товарный каталог |
| `Orders` | id, user\_id, **status\_id** (FK → OrderStatuses), total\_amount, delivery\_address, contact\_phone | Заказы |
| `OrderItems` | id, order\_id, product\_id, quantity, price\_at\_order | Позиции заказов |
| `OrderStatusHistory` | id, order\_id, **status\_id** (FK → OrderStatuses), changed\_by, changed\_at | История статусов |
| `Cart` | id, user\_id, product\_id, quantity | Корзина |
| `Favorites` | id, user\_id, product\_id | Избранное |

> Справочные таблицы `Categories` и `OrderStatuses` обеспечивают ссылочную целостность через внешние ключи.  
> Для сброса БД удалите `dist/digitalhub.db` — файл пересоздастся при следующем запуске.

### Автоматическое сидирование

При первом запуске `DatabaseManager.initializeDatabase()` выполняет:
1. Создание таблиц и индексов
2. Заполнение справочников категорий (11 шт.) и статусов (8 шт.)
3. Сидирование 500 товаров по 11 категориям (`seedAllProducts()`)
4. Создание тестовых пользователей (`seedUsersAndOrders()`):
   - 8 покупателей (роль USER)
   - 3 администратора (роль ADMIN)
5. Генерация 170 тестовых заказов (`seedUsersAndOrders()`), равномерно распределённых с 01.01.2025 по 01.03.2026

Все операции идемпотентны — при повторном запуске дубликаты не создаются.

### Тестовые учётные записи

| Роль | Имя | Email (логин) | Пароль |
|---|---|---|---|
| USER | Александр Ковалев | user1@techhaven.ru | User1Pass! |
| USER | Мария Сидорова | user2@techhaven.ru | User2Pass! |
| USER | Дмитрий Волков | user3@techhaven.ru | User3Pass! |
| USER | Елена Новикова | user4@techhaven.ru | User4Pass! |
| USER | Сергей Морозов | user5@techhaven.ru | User5Pass! |
| USER | Ольга Попова | user6@techhaven.ru | User6Pass! |
| USER | Николай Соколов | user7@techhaven.ru | User7Pass! |
| USER | Татьяна Лебедева | user8@techhaven.ru | User8Pass! |
| **ADMIN** | **Андрей Захаров** | **admin1@techhaven.ru** | **Admin1Pass!** |
| **ADMIN** | **Наталья Козлова** | **admin2@techhaven.ru** | **Admin2Pass!** |
| **ADMIN** | **Игорь Смирнов** | **admin3@techhaven.ru** | **Admin3Pass!** |

---

## 6. Безопасность

| Механизм | Реализация |
|---|---|
| Хеширование паролей | PBKDF2WithHmacSHA256, 100 000 итераций, 16-байт salt |
| Шифрование PII | AES-256-CBC (телефон, адрес доставки) |
| SQL-инъекции | `PreparedStatement` во всех запросах |
| Блокировка аккаунта | 5 неудачных попыток → блок на 5 минут |
| Логирование | `java.util.logging` (стандартный JDK Logger) |

---

## 7. Горячие клавиши

| Контекст | Клавиша | Действие |
|---|---|---|
| Пользователь | `Alt+1..5` | Навигация: Каталог / Корзина / Избранное / Заказы / Профиль |
| Пользователь | `F1` | Открыть UserManual (справка) |
| Администратор | `Alt+1..4` | Навигация: Товары / Заказы / Пользователи / Отчёты |
| Администратор | `F1` | Открыть AdminManual (справка) |
| Администратор | `Ctrl+P` | Печать отчёта (в разделе «Отчёты») |
| Оба | `Alt+Q` | Выход из аккаунта |
| Оба | `Alt+M` | Свернуть окно |
| Оба | `Alt+W` | Закрыть приложение |

---

## 8. Тестирование

### Запуск тестов

```
mvn test -B          # только тесты
mvn verify           # тесты + JaCoCo-отчёт покрытия
```

### Результаты

| Метрика | Значение |
|---|---|
| Фреймворк | JUnit 5 (junit-jupiter 5.10.2) |
| Покрытие | JaCoCo 0.8.13 (`mvn verify` → `target/site/jacoco/index.html`) |
| Тестовых классов | 26 |
| Тестов всего | 235 |
| Результат | BUILD SUCCESS |

### Покрытие по пакетам (JaCoCo)

| Пакет | Инструкции | Ветки | Методы |
|---|---|---|---|
| `com.techhaven.model` | **98 %** | 95 % | 172 |
| `com.techhaven.config` | **94 %** | 56 % | 24 |
| `com.techhaven.security` | **87 %** | 92 % | 12 |
| `com.techhaven.repository` | **57 %** | 56 % | 61 |
| `com.techhaven.service` | **46 %** | 37 % | 53 |
| **Итого** | **84 %** | **55 %** | **335** |

> View-классы (JavaFX UI) исключены из отчёта — unit-тесты не могут инициализировать JavaFX runtime в CI.

### Тестовые классы

| Класс | Тестов | Что проверяется |
|---|---|---|
| `Product` | 5 | Конструкторы, getFormattedPrice, getStockStatus |
| `User` | 6 | Конструкторы, isAdmin, блокировка |
| `CartItem` | 8 | Subtotal, форматирование, transient-поля |
| `Order` | 3 | Все поля, getFormattedTotal |
| `OrderItem` | 6 | Subtotal, форматирование |
| `Favorite` | 5 | Конструкторы, transient-поля |
| `OrderStatusHistory` | 3 | Конструкторы, все поля |
| `Role` | 4 | enum values, fromString, fallback |
| `OrderStatus` | 7 | 8 статусов, fromString, нормализация ё/е, isTerminal |
| `SecurityManager` | 23 | Singleton, hash/verify, AES encrypt/decrypt, validate*, isValid* |
| `AppConfig` | 4 | Значения по умолчанию auth/security/UI |
| `EventBus` | 7 | subscribe/publish, unsubscribe, clearAll, error isolation |
| `SessionManager` | 8 | Singleton, login/logout, isAdmin, getCurrentUserId |
| `DatabaseManager` | 11 | Singleton, connection, tables exist, seed data, 3НФ categories |
| `UserRepository` | 10 | findByEmail, findById, emailExists, findAll*, emailExistsForOther |
| `ProductRepository` | 10 | findAll, findPaged, getTotalCount, findById, categories, search |
| `OrderRepository` | 9 | findAll, findById, findByUserId, findOrderItems, statusHistory |
| `CartRepository` | 11 | addToCart, getCartItems, removeFromCart, updateQuantity, clearCart |
| `FavoriteRepository` | 8 | addFavorite, getFavorites, removeFavorite, isFavorite |
| `ProductService` | 24 | getAllProducts, search, pagination, categories |
| `AuthService` | 11 | Валидация пароля, hash/verify, null/empty |
| `OrderService` | 30 | placeOrder валидация, success path, чтение заказов, updateStatus |
| `CartService` | 6 | getCartItems, addToCart, removeFromCart, getTotal, isInCart |
| `FavoriteService` | 4 | getFavorites, getFavoriteCount, isFavorite, addAndRemove |
| `UndoService` | 8 | Singleton, isPendingDeletion, forceExecute, undo |
| `ButtonTooltip` | 1 | CSS-стили для Tooltip |

### Запуск через surefire-plugin

Конфигурация в `pom.xml` включает `--add-reads`/`--add-opens` для совместимости с JPMS (`module-info.java`).

---

## 9. Диагностика и решение проблем

| Симптом | Причина | Решение |
|---|---|---|
| `java` не распознана | Java не установлена или не в PATH | Установите JDK 21+, добавьте `%JAVA_HOME%\bin` в PATH |
| `mvn` не распознан | Maven не установлен | Установите Maven 3.8+, добавьте `%MAVEN_HOME%\bin` в PATH |
| Ошибка запуска JAR | Неверный classpath JavaFX | JAR собирается как FAT JAR через shade plugin — запускать через `java -jar`, не через `mvn javafx:run` |
| БД повреждена / пустые данные | Файл БД повреждён | Удалите `dist/digitalhub.db` — пересоздастся автоматически |
| Изменения не применяются | Запущена другая копия DigitalHub.jar | Закройте все копии, пересоберите (`run.bat`) |
| Окно не запускается | Отсутствуют native-библиотеки JavaFX | Убедитесь, что `maven-shade-plugin` включён в `pom.xml` |
| Справка не открывается | Файл `UserManual.md` / `AdminManual.md` не найден | Файлы должны находиться в корне проекта или на уровень выше `dist/` |

---

## 10. Статусная модель заказа

```
Новый ──► В обработке ──► Подтверждён ──► Собран ──► Отправлен ──► Доставлен ──► Завершён
   └──────────────────────────────────────────────────────────────────────────► Отменён
```

Каждое изменение статуса записывается в `OrderStatusHistory` (исполнитель + timestamp).  
Статусы хранятся в справочнике `OrderStatuses` (3НФ) и связаны через `status_id`.

---

## 11. Расширение и сопровождение

- **Новый экран**: создать `*View.java` в `com.techhaven.view`, добавить вызов из `MainLayout` или `AdminLayout`
- **Новая таблица БД**: добавить `CREATE TABLE IF NOT EXISTS` + `ALTER TABLE IF NOT EXISTS` в `DatabaseManager.initializeDatabase()`
- **Новая категория товаров**: добавить запись в `seedCategories()` в `DatabaseManager` и обновить `categoryColors()` в `CatalogView.java` / `CartView.java` / `FavoritesView.java`
- **Обновление справки**: отредактировать `UserManual.md` или `AdminManual.md` — изменения применяются без пересборки
- **Смена пути к БД**: передать `-Ddb.path=<путь>` при запуске `java -jar`
