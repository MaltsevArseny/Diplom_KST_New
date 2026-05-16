# DigitalHub — Техническая документация

> **Аудитория**: разработчики · системные администраторы · аналитики · тестировщики · специалисты сопровождения  
> **Пользовательская документация**: [UserManual.md](UserManual.md) · [AdminManual.md](AdminManual.md)  
> **Техническое задание**: [ТЕХНИЧЕСКОЕ ЗАДАНИЕ.txt](docs/ТЕХНИЧЕСКОЕ%20ЗАДАНИЕ.txt)

---

## 1. Обзор системы

DigitalHub — автономное desktop-приложение для продажи компьютерного оборудования.  
Работает **полностью offline**, не требует сетевого подключения.

| Параметр | Значение |
|---|---|
| Версия | 3.2 |
| Платформа | Windows 10/11, Linux, macOS (Java cross-platform) |
| Java | 21 LTS (OpenJDK / Temurin) |
| UI-фреймворк | JavaFX 21.0.5 |
| БД | SQLite 3 (через JDBC), схема в 3НФ |
| Сборка | Apache Maven 3.8+ |
| Паттерн | MVVM (Model–View–ViewModel) |
| Темизация | Dark / Light, runtime-переключение `Alt+T` |
| Артефакт сборки | `dist/DigitalHub.jar` (FAT JAR) |
| История изменений | [CHANGELOG.md](docs/CHANGELOG.md) |

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
│   │   ├── SessionManager.java   # Singleton: текущий авторизованный пользователь
│   │   └── ThemeManager.java     # Singleton: dark/light тема, persist (Preferences), listener-API
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
│   │   ├── ProductService.java
│   │   └── ProductCache.java     # In-memory кэш категорий (Singleton, thread-safe)
│   └── view/                     # JavaFX экраны
│       ├── MainLayout.java       # Навбар + контент-область (пользователь)
│       ├── AdminLayout.java      # Топбар + сайдбар + контент-область (администратор)
│       ├── DialogHelper.java     # Утилита: стилизованные диалоги, Tooltip, стили
│       ├── ThemeToggle.java      # Утилита: кнопка переключения темы (☀/🌙)
│       ├── HelpView.java         # Встроенная справка (Markdown → JavaFX рендер)
│       ├── LoginView.java / RegisterView.java
│       ├── CatalogView.java / CartView.java / FavoritesView.java
│       ├── CheckoutView.java / OrdersView.java / ProfileView.java
│       └── Admin*.java           # AdminProductsView, AdminOrdersView, AdminUsersView, AdminReportsView
├── src/main/resources/
│   └── styles/
│       ├── dark-theme.css        # Тёмная тема (default)
│       └── light-theme.css       # Светлая тема
├── dist/
│   ├── DigitalHub.jar            # FAT JAR (генерируется при сборке)
│   ├── digitalhub.db             # Единственная БД приложения
│   ├── DigitalHub.bat            # Запуск из папки dist/ (автопоиск Java)
│   └── jre/                      # Portable JRE 21 (Temurin) — Java не нужна на ПК
├── docs/                          # Документация проекта
│   ├── CHANGELOG.md              # История версий
│   ├── DATABASE_SCHEMA.md        # Описание схемы БД
│   ├── ПОЯСНИТЕЛЬНАЯ_ЗАПИСКА.md  # Пояснительная записка диплома
│   ├── ПЛАН_ВЫСТУПЛЕНИЯ.md      # План выступления
│   └── ТЕХНИЧЕСКОЕ ЗАДАНИЕ.txt   # Техническое задание
├── scripts/                       # Скрипты сборки и запуска
│   ├── run.bat                   # Сборка + запуск (Windows)
│   ├── run.sh                    # Сборка + запуск (Linux / macOS)
│   └── run_tests.bat             # Запуск тестов (Windows)
├── pom.xml
├── UserManual.md                 # Руководство пользователя (используется в приложении)
├── AdminManual.md                # Руководство администратора (используется в приложении)
└── README.md
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

| Способ | Команда / Файл | Платформа |
|---|---|---|
| **Из дистрибутива (рекомендуется)** | `dist\DigitalHub.bat` | Windows |
| Основной скрипт (сборка + запуск) | `scripts\run.bat` | Windows |
| Кросс-платформенный | `bash scripts/run.sh` | Linux / macOS |
| Напрямую | `java -Ddb.path=dist/digitalhub.db -jar dist/DigitalHub.jar` | Любая |
| Maven compile only | `mvn compile` | Любая |

### 4.3 Что делает `dist\DigitalHub.bat`

`DigitalHub.bat` ищет Java 21 в следующем порядке:
1. `dist\jre\bin\java.exe` — **portable JRE** в папке рядом со скриптом (приоритет)
2. Папки `%ProgramFiles%` — Eclipse Adoptium, Microsoft, Oracle, OpenJDK (jdk-21*)
3. Переменная `JAVA_HOME`
4. `java` из системного `PATH`

Если Java не найдена — выводит ссылку: `https://adoptium.net`

### 4.4 Что делает `scripts\run.bat` / `scripts/run.sh`

1. Проверяет наличие Java 21+ и Maven в PATH
2. `mvn clean package -q` — полная сборка, FAT JAR → `dist/DigitalHub.jar`
3. `java -Ddb.path=dist/digitalhub.db -jar dist/DigitalHub.jar` — запуск

> **Системное свойство `-Ddb.path`** — путь к БД, читается в `DatabaseManager.java`.  
> Default: `dist/digitalhub.db`. При запуске из `dist\DigitalHub.bat` передаётся `db.path=digitalhub.db`.

### 4.5 Запуск на Linux / macOS

```bash
# Сделать скрипт исполняемым (один раз):
chmod +x scripts/run.sh

# Сборка + запуск:
./scripts/run.sh

# Или запустить уже собранный JAR напрямую:
java -Ddb.path=dist/digitalhub.db -jar dist/DigitalHub.jar
```

> **Примечание:** JavaFX требует наличия нативных библиотек для текущей ОС. FAT JAR собирается с зависимостями, но для работы на Linux/macOS необходимо установить графическую среду (GTK/X11 на Linux или macOS 12+).

### 4.6 Как обновить приложение

1. Скачайте новую версию исходного кода (или выполните `git pull`)
2. Запустите `scripts\run.bat` (Windows) или `./scripts/run.sh` (Linux/macOS) — проект пересоберётся автоматически
3. База данных (`dist/digitalhub.db`) **не удаляется** — все пользователи и заказы сохраняются
4. Если после обновления БД выглядит пустой — удалите `dist/digitalhub.db`; при следующем запуске данные пересоздадутся
5. Историю изменений смотрите в [CHANGELOG.md](docs/CHANGELOG.md)

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
| Оба | `Alt+T` | Переключить тему (тёмная ↔ светлая) |

---

## 7.bis. Темизация (Dark / Light)

Приложение поддерживает две темы оформления с переключением «на лету» без перезапуска.

### Компоненты темизации

| Компонент | Файл | Назначение |
|---|---|---|
| `ThemeManager` | `config/ThemeManager.java` | Singleton: хранит активную тему, persist в `Preferences`, listener-API, метод `applyTo(Scene)` |
| `ThemeToggle` | `view/ThemeToggle.java` | Утилита: создание кнопки-переключателя, обновление иконки `☀ / 🌙` |
| `dark-theme.css` | `resources/styles/dark-theme.css` | Тёмная палитра (default) |
| `light-theme.css` | `resources/styles/light-theme.css` | Светлая палитра (зеркало dark) |

### Где переключать тему

- **Кнопка** `☀ Светлая` / `🌙 Тёмная` в навбаре пользователя, топбаре администратора, окне логина.
- **Hot-key** `Alt+T` из любого экрана.

### Persist

Выбор сохраняется в `java.util.prefs.Preferences` — узел `com/techhaven`, ключ `ui.theme`. Не зависит от рабочей директории и переживает переустановку приложения (хранится в реестре Windows / `~/.java/.userPrefs` на Linux).

Default при первом запуске — `DARK` (поведение совместимо с версиями до 3.2).

### CSS-переменные

Темы определяют единый набор переменных в `.root { ... }`. Переменные доступны как из CSS-селекторов, так и из inline-`setStyle("-fx-...: -th-bg-primary;")`:

```css
-th-bg-primary    /* основной фон */
-th-bg-secondary  /* навбар, sidebar */
-th-bg-card       /* карточки, поля ввода */
-th-bg-hover      /* hover-состояния */
-th-accent        /* акцентный фиолетовый */
-th-accent-light  /* светлый акцент (логотип, badge) */
-th-text-primary  /* основной текст */
-th-text-secondary /* приглушённый текст */
-th-text-muted    /* подсказки, плейсхолдеры */
-th-border        /* рамки полей */
-th-success / -th-warning / -th-danger
```

### Добавить новую тему

1. Создать `src/main/resources/styles/<name>-theme.css` (копия dark с другими значениями переменных в `.root`).
2. Добавить элемент `<NAME>("/styles/<name>-theme.css")` в `ThemeManager.Theme`.
3. При необходимости расширить `Theme.opposite()` логику циклического обхода.
4. Обновить тест `cssResourcesExist()` в `ThemeManagerTest`.

### Ограничения

В коде сохраняется ~40 inline-`setStyle("...#RRGGBB...")` — это семантические цвета (статусы заказов в `AdminOrdersView.statusColor()`, цвета аватаров, акценты-уведомления). Они константны между темами по дизайн-замыслу: статус «Доставлен» зелёный в обеих темах.

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
| Тестовых классов | 28 |
| Тестов всего | 261 |
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
| `ProductCache` | 12 | cache hit/miss, invalidate, счётчики, конкурентный доступ, неизменяемость |
| `AuthService` | 11 | Валидация пароля, hash/verify, null/empty |
| `OrderService` | 30 | placeOrder валидация, success path, чтение заказов, updateStatus |
| `CartService` | 6 | getCartItems, addToCart, removeFromCart, getTotal, isInCart |
| `FavoriteService` | 4 | getFavorites, getFavoriteCount, isFavorite, addAndRemove |
| `UndoService` | 8 | Singleton, isPendingDeletion, forceExecute, undo |
| `ButtonTooltip` | 1 | CSS-стили для Tooltip |
| `ThemeManager` | 14 | Singleton, default DARK, set/get, toggle, persist (Preferences), listener add/remove/notify, CSS-resource existence |

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
- **Новая цветовая тема**: см. раздел [7.bis. Темизация](#7bis-темизация-dark--light) — `src/main/resources/styles/<name>-theme.css` + новое значение `ThemeManager.Theme`
