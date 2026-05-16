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
| Версия | 3.3.0 (2026-05-16) |
| Платформа | Windows 10/11, Linux, macOS (Java cross-platform) |
| Java | 21 LTS (OpenJDK / Temurin) |
| UI-фреймворк | JavaFX 21.0.5 (controls / fxml / graphics) |
| БД | SQLite 3 (через JDBC `org.xerial:sqlite-jdbc:3.42.0.0`), схема в 3НФ |
| Сборка | Apache Maven 3.8+ (maven-shade-plugin → FAT JAR) |
| Тесты | JUnit 5.10.2, 302 теста в 31 классе |
| Паттерн | MVVM (Model–View–ViewModel) + слой сервисов |
| Темизация | Dark / Light, runtime-переключение `Alt+T`, persist в `Preferences` |
| Артефакт сборки | `dist/DigitalHub.jar` (FAT JAR, mainClass = `com.techhaven.Launcher`) |
| Поставка | `dist/` содержит JAR + portable JRE 21 (`dist/jre/`) — Java на ПК не обязательна |
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
│   │   ├── AppConfig.java        # Константы: длительности таймеров, лимиты, иммутабельная конфигурация
│   │   ├── AppPaths.java         # Резолв путей рядом с JAR (CodeSource-based), dataDir / dbFile / productImagesDir
│   │   ├── DatabaseManager.java  # Singleton: инициализация схемы из schema.sql, сидирование, миграции
│   │   ├── EventBus.java         # Pub/sub шина (cart/favorites/orders/products события)
│   │   ├── SeedProducts.java     # Генератор каталога 500 товаров
│   │   ├── SessionManager.java   # Singleton: текущий авторизованный пользователь, requireAdmin()
│   │   └── ThemeManager.java     # Singleton: dark/light тема, persist (Preferences), listener-API
│   ├── security/
│   │   └── SecurityManager.java  # PBKDF2 хеш/верификация, AES-256-CBC шифрование PII, валидация
│   ├── repository/               # Слой доступа к данным (JDBC + PreparedStatement)
│   │   ├── I{User,Product,Order,Cart,Favorite}Repository.java  # Контракты для testability
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java   # +findPaged / decrementStock(conn, ...) atomic-list
│   │   ├── OrderRepository.java     # +overload-методы с Connection (участие в транзакции)
│   │   ├── CartRepository.java      # +addToCart суммирует quantity при дубле user_id+product_id
│   │   └── FavoriteRepository.java
│   ├── service/                  # Бизнес-логика
│   │   ├── AuthService.java      # Аутентификация, блокировка аккаунта
│   │   ├── CartService.java
│   │   ├── FavoriteService.java
│   │   ├── OrderService.java     # Транзакционный placeOrder, role-checks на updateStatus/Delivery
│   │   ├── ProductService.java   # CRUD с role-checks и validate()
│   │   ├── UserService.java      # Admin-операции (lock/unlock/list) с requireAdmin()
│   │   ├── ProductCache.java     # In-memory кэш категорий (Singleton, thread-safe)
│   │   └── UndoService.java      # Отложенное удаление с возможностью отмены (5 сек)
│   └── view/                     # JavaFX экраны
│       ├── MainLayout.java       # Навбар + контент-область (пользователь)
│       ├── AdminLayout.java      # Топбар + сайдбар + контент-область (администратор)
│       ├── DialogHelper.java     # Утилита: стилизованные диалоги, Tooltip, стили
│       ├── FormStyles.java       # Общие стили форм (input/label/section)
│       ├── ThemeToggle.java      # Утилита: кнопка переключения темы (☀/🌙)
│       ├── HelpView.java         # Встроенная справка (Markdown → JavaFX рендер)
│       ├── LoginView.java / RegisterView.java
│       ├── CatalogView.java / CartView.java / FavoritesView.java
│       ├── CheckoutView.java / OrdersView.java / ProfileView.java
│       ├── ProductDetailView.java # Полноэкранная карточка товара (характеристики, действия)
│       ├── Admin*.java           # AdminProductsView, AdminOrdersView, AdminUsersView, AdminReportsView
│       └── component/
│           └── NotificationPanel.java  # Toast-уведомления (success/info/warn/error)
├── src/main/resources/
│   ├── schema.sql                # DDL: таблицы, CHECK-constraints, индексы (3НФ)
│   ├── seed.sql                  # (резерв; основное сидирование — программное в DatabaseManager)
│   ├── app.properties            # Базовые ключи конфигурации
│   ├── logging.properties        # Настройки java.util.logging
│   ├── images/logo.jpg
│   └── styles/
│       ├── dark-theme.css        # Тёмная тема (default)
│       └── light-theme.css       # Светлая тема (sand-палитра)
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
| JUnit Jupiter | `org.junit.jupiter:junit-jupiter` | 5.10.2 | Юнит-тесты (test scope) |
| JUnit 4 | `junit:junit` | 4.13.1 | Совместимость с legacy-runner (test scope) |
| Maven Compiler | `maven-compiler-plugin` | 3.11.0 | `--release=21`, `--add-reads techhaven=ALL-UNNAMED` |
| Maven Shade | `maven-shade-plugin` | 3.6.0 | Сборка FAT JAR с `mainClass=com.techhaven.Launcher` |
| Surefire | `maven-surefire-plugin` | 3.2.5 | Тесты в classpath-режиме (`useModulePath=false`) с `-Ddb.path=target/test.db` |
| JaCoCo | `jacoco-maven-plugin` | 0.8.13 | Покрытие (`mvn verify` → `target/site/jacoco/`), исключение `view/**` |
| javafx-maven-plugin | `org.openjfx:javafx-maven-plugin` | 0.0.8 | `mvn javafx:run` (опционально, для dev-запуска) |

---

## 3.bis. Резолв путей файлов (AppPaths)

Приложение **не зависит от текущей рабочей директории** запуска. Все runtime-ресурсы резолвятся через `com.techhaven.config.AppPaths`:

| Ресурс | По умолчанию | Override |
|---|---|---|
| **Data-директория** (БД, картинки) | Папка рядом с JAR'ом (определяется через `CodeSource.getLocation()`) | `-Dapp.data.dir=<dir>` |
| **БД SQLite** | `dataDir/digitalhub.db` | `-Ddb.path=<file>` |
| **Картинки товаров** | `dataDir/product_images/` | — (резолвится из dataDir) |
| **UserManual.md / AdminManual.md** | Поиск: dataDir → parent → CWD → classpath `/help/` | — |

**Эффект:** запуск `java -jar /full/path/to/DigitalHub.jar` создаст БД рядом с JAR'ом, а не там, откуда запущена команда. Это позволяет:
- Перенести `dist/` куда угодно — приложение продолжит работать
- Запустить из IDE без поломок (fallback на `user.dir`)
- Изолировать тестовую БД (`mvn test` → `target/test.db` через surefire systemPropertyVariables)

См. `src/main/java/com/techhaven/config/AppPaths.java` и 10 unit-тестов в `src/test/java/com/techhaven/config/AppPathsTest.java`.

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

### 4.7 Кросс-платформенная сборка JavaFX

В `pom.xml` определены **автоматические Maven-профили** по OS family:

| Profile | Activation | `javafx.platform` |
|---|---|---|
| `windows` | `os.family = windows` | `win` |
| `macos` | `os.family = mac` | `mac` |
| `linux` | `os.family = unix, os.name = linux` | `linux` |

Maven сам подтягивает нужный classifier-jar (`javafx-graphics-21.0.5-win.jar`, `-mac.jar`, `-linux.jar`) во время сборки. Принудительное переопределение: `mvn package -Pmacos` или `-Djavafx.platform=linux`.

**Почему это важно:** artifact `javafx-graphics-21.0.5.jar` (без classifier) — пустой placeholder. Реальные классы и `module-info.class` лежат в classifier-jar. Без активного профиля Maven подтягивал бы только placeholder → runtime `Module javafx.graphics not found`.

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
5. Генерация **170 тестовых заказов** (`seedUsersAndOrders()`), равномерно распределённых с 01.01.2025 по 01.03.2026. Распределение по покупателям: user1=5, user2=10, user3=15, user4=20, user5=30, user6=40, user7=50, user8=0 (резервный аккаунт без истории). Каждый заказ получает 1–4 позиции, итоговую сумму, плановую дату/интервал доставки и историю смен статусов.
6. Гарантированное наличие нескольких товаров с `stock_quantity = 0` (для проверки фильтра «Нет в наличии»).

Все операции идемпотентны: используется `INSERT OR IGNORE` для справочников, товаров и пользователей; для заказов — проверка `SELECT COUNT(*) FROM Orders > 0` перед вставкой. Повторный запуск не создаёт дубликатов.

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
| Шифрование PII | AES-256-CBC: email и телефон пользователя, контактный телефон заказа, адрес доставки |
| SQL-инъекции | `PreparedStatement` во всех запросах |
| Блокировка аккаунта | 5 неудачных попыток → блок на 5 минут |
| Логирование | `java.util.logging` (стандартный JDK Logger) |
| Транзакции оформления заказа | Единое соединение с `setAutoCommit(false)`; при любом сбое — `rollback` (заказ/позиции/сток/история/корзина согласованы) |
| Защита склада от race | `UPDATE Products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?` — атомарное условное списание |
| Целостность данных | CHECK-constraints на `price ≥ 0`, `stock_quantity ≥ 0`, `quantity > 0`, `total_amount ≥ 0`; UNIQUE-индексы `Cart(user_id, product_id)`, `Favorites(user_id, product_id)` |
| Проверки прав admin | Service-уровень: `SessionManager.requireAdmin()` в `ProductService.createProduct/updateProduct/deleteProduct`, `OrderService.updateStatus/updatePlannedDelivery`, `UserService.*` (defense-in-depth поверх UI). `OrderService.updateStatus` дополнительно разрешает USER отменить только собственный незавершённый заказ. |
| Валидация товаров | `ProductService.validate()` — непустое имя, неотрицательные цена/остаток, существование категории в справочнике, отсутствие дубля имени |

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

### Палитра (SSoT)

**ВСЕ цвета** приложения определены через CSS-переменные `-th-*` **в одном месте на каждую тему** — в секции `.root { ... }` соответствующего файла. Прочие селекторы и Java-`setStyle()` используют только эти переменные. Hardcoded HEX в коде запрещён.

📘 **Полная таблица** переменных, их значений и мест использования: [docs/THEME_PALETTE.md](docs/THEME_PALETTE.md)

Ключевые переменные:

```css
-th-bg-primary    /* основной фон страницы */
-th-bg-secondary  /* навбар, sidebar, header'ы таблиц */
-th-bg-card       /* карточки, поля ввода */
-th-bg-hover      /* hover-состояния */
-th-accent        /* акцентный фиолетовый */
-th-accent-light  /* светлый акцент (логотип) */
-th-text-primary  /* основной текст */
-th-text-secondary /* приглушённый текст */
-th-text-muted    /* подсказки, плейсхолдеры */
-th-border        /* рамки полей */
-th-success / -th-warning / -th-danger
-th-cream         /* замена белого: текст на акцентных фонах */
```

### Светлая тема — песочная палитра (sand)

Светлая тема использует **тёплые песочные оттенки** вместо нейтрального бело-серого:

| Переменная | Значение | Назначение |
|---|---|---|
| `-th-bg-primary` | `#f5ede0` | мягкий песок (фон) |
| `-th-bg-secondary` | `#ede0c8` | глубокий песок (навбар) |
| `-th-bg-card` | `#f0e6d2` | средний песок (карточки) |
| `-th-text-primary` | `#3d2f1f` | глубокий тёплый коричневый |
| `-th-cream` | `#faf3e0` | кремовый для текста на акцентах |

**БЕЛЫЙ цвет (#ffffff) не используется** ни в фонах, ни в шрифтах. Текст на акцентных кнопках — `-th-cream`.

### Добавить новую тему

См. [docs/THEME_PALETTE.md](docs/THEME_PALETTE.md) — раздел «Как добавить новую тему».

### Семантические константы (НЕ темизуются)

Сохранены ~25 hardcoded HEX в коде — это **семантические** цвета (статусы заказов `AdminOrdersView.statusColor()`, цвета аватаров, акценты уведомлений `DialogHelper.showInfo/Error/Warning`). Они одинаковы в обеих темах по дизайн-замыслу: статус «Доставлен» зелёный в любой теме.

### PDF / печатные отчёты

`AdminReportsView` использует браузерный `window.print()` — HTML-отчёт имеет **собственную палитру** (синий брандинг `#185ABD` на белом фоне), независимую от темы приложения. Это сознательное решение: печатный документ всегда выглядит как печатный, не как скриншот экрана.

---

## 8. Тестирование

### Запуск тестов

```
mvn test             # только тесты (302 теста, ~10 сек)
mvn verify           # тесты + JaCoCo-отчёт (target/site/jacoco/index.html)
```

> **Безопасность:** surefire передаёт `-Ddb.path=${project.build.directory}/test.db` через `systemPropertyVariables`, поэтому интеграционные тесты пишут в **изолированную** `target/test.db`, а не в production `digitalhub.db`. Это исключает порчу пользовательских данных при `mvn test`.

### Результаты последнего прогона

| Метрика | Значение |
|---|---|
| Фреймворк | JUnit 5 (junit-jupiter 5.10.2) |
| Покрытие | JaCoCo 0.8.13 (`view/**` исключён из отчёта — JavaFX runtime в CI не инициализируется) |
| Тестовых классов | **31** |
| Тестов всего | **302** |
| Failures / Errors / Skipped | **0 / 0 / 0** |
| Результат | BUILD SUCCESS |
| Время | ~10 секунд |

### Тестовые классы (31 шт., 302 теста)

#### `com.techhaven.model` — 9 классов / 54 теста

| Класс | Тестов | Что проверяется |
|---|---|---|
| `Product` | 8 | Конструкторы, getFormattedPrice, getStockStatus |
| `User` | 6 | Конструкторы, isAdmin, поля блокировки |
| `CartItem` | 12 | Subtotal, форматирование, transient-поля |
| `Order` | 3 | Все поля, getFormattedTotal |
| `OrderItem` | 6 | Subtotal, форматирование |
| `Favorite` | 5 | Конструкторы, transient-поля |
| `OrderStatusHistory` | 3 | Конструкторы, все поля |
| `Role` | 4 | enum values, fromString, fallback |
| `OrderStatus` | 7 | 8 статусов, fromString, нормализация ё/е, isTerminal |

#### `com.techhaven.config` — 6 классов / 54 теста

| Класс | Тестов | Что проверяется |
|---|---|---|
| `AppConfig` | 4 | Значения по умолчанию auth / security / UI |
| `AppPaths` | 10 | Override через `-Dapp.data.dir`, создание директорий, поиск документов в data-dir / parent / CWD, default `dataDir`, кэширование, абсолютный путь к `dbFile` |
| `DatabaseManager` | 11 | Singleton, connection, tables exist, seed data, FK на справочники |
| `EventBus` | 7 | subscribe/publish, unsubscribe, clearAll, error isolation |
| `SessionManager` | 8 | Singleton, login/logout, isAdmin, `requireAdmin()`, getCurrentUserId |
| `ThemeManager` | 14 | Singleton, default DARK, set/get, toggle, persist (Preferences), listener add/remove/notify, `Theme.opposite`, наличие CSS-ресурсов в classpath |

#### `com.techhaven.security` — 1 класс / 23 теста

| Класс | Тестов | Что проверяется |
|---|---|---|
| `SecurityManager` | 23 | Singleton, PBKDF2 hash/verify, AES-256 encrypt/decrypt, `validate*`, `isValid*`, обработка null/невалидных входов |

#### `com.techhaven.repository` — 5 классов / 51 тест

| Класс | Тестов | Что проверяется |
|---|---|---|
| `UserRepository` | 13 | findByEmail, findById, emailExists, findAll*, emailExistsForOther, update |
| `ProductRepository` | 10 | findAll, findPaged, getTotalCount, findById, categories, search |
| `OrderRepository` | 9 | findAll, findById, findByUserId, findOrderItems, statusHistory |
| `CartRepository` | 11 | addToCart (суммирование quantity при дубле), getCartItems, removeFromCart, updateQuantity, clearCart |
| `FavoriteRepository` | 8 | addFavorite, getFavorites, removeFavorite, isFavorite |

#### `com.techhaven.service` — 7 классов / 107 тестов

| Класс | Тестов | Что проверяется |
|---|---|---|
| `AuthService` | 11 | Валидация пароля, hash/verify, null/empty |
| `CartService` | 8 | getCartItems, addToCart, removeFromCart, getTotal, isInCart |
| `FavoriteService` | 4 | getFavorites, getFavoriteCount, isFavorite, addAndRemove |
| `OrderService` | 33 | placeOrder валидация, success path, чтение заказов, updateStatus, защита от ухода стока в минус (атомарный `decrementStock` + транзакция), `placeOrderRollsBackOnFailure` (orders+1, stock-1, history++, cart=0), USER не может менять статус кроме отмены своего заказа |
| `ProductCache` | 12 | cache hit/miss, invalidate, счётчики, конкурентный доступ, неизменяемость |
| `ProductService` | 31 | getAllProducts, search, pagination, categories, `validate()` (7 кейсов: пустое имя, отриц. цена, отриц. сток, пустая / несуществующая категория, дубль имени, same-name on edit), role-checks |
| `UndoService` | 8 | Singleton, isPendingDeletion, forceExecute, undo |

#### `com.techhaven.view` — 3 класса / 13 тестов

| Класс | Тестов | Что проверяется |
|---|---|---|
| `ButtonTooltip` | 1 | CSS-стили для Tooltip |
| `HelpViewAnchorTest` | 6 | Корректное построение anchor'ов TOC, scrollToAnchor, фильтрация неподдерживаемых якорей |
| `ThemeContractTest` | 6 | Совпадение набора `-th-*` переменных между `dark-theme.css` и `light-theme.css`, отсутствие невостребованных переменных |

### Запуск через surefire-plugin

Конфигурация `pom.xml`:
- `useModulePath=false` — тесты идут в classpath-режиме, не активируя JPMS module-info (тесты не зависят от JavaFX runtime).
- `systemPropertyVariables.db.path = ${project.build.directory}/test.db` — изоляция БД.
- `argLine` пробрасывает `@{argLine}` (для агентов вроде JaCoCo) + `-Djava.util.logging.config.file=…`.

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
| Справка не открывается | Файл `UserManual.md` / `AdminManual.md` не найден | `AppPaths.findDocumentFile` ищет в dataDir → parent dataDir → CWD → classpath `/help/`. Положите файл в одну из этих локаций или пересоберите JAR (классы попадут в classpath) |
| `mvn test` падает с «Module javafx.graphics not found» | Профиль OS не активировался, classifier-jar пуст | Запустить с `-P windows`/`-P macos`/`-P linux` или `-Djavafx.platform=win/mac/linux` |
| Тесты пишут в production БД | Старая версия pom.xml без `systemPropertyVariables.db.path` | Обновить `pom.xml` (3.2.2+); проверить, что surefire-блок содержит `<db.path>${project.build.directory}/test.db</db.path>` |

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

- **Новый экран**: создать `*View.java` в `com.techhaven.view`, добавить вызов из `MainLayout` или `AdminLayout`.
- **Новая таблица БД**: добавить `CREATE TABLE IF NOT EXISTS …` в `src/main/resources/schema.sql` (DDL грузится `DatabaseManager.loadResource("/schema.sql")`).
- **Новая категория товаров**: добавить запись в `seedCategories()` (`DatabaseManager.java:115`) и расширить цвета в `categoryColors()` в `CatalogView.java` / `CartView.java` / `FavoritesView.java`.
- **Обновление справки**: отредактировать `UserManual.md` или `AdminManual.md` в корне проекта — изменения применяются без пересборки (HelpView ищет файлы через `AppPaths.findDocumentFile()`).
- **Смена пути к БД**: `-Ddb.path=<абсолютный путь>` или `-Dapp.data.dir=<папка>` при запуске `java -jar`.
- **Новая цветовая тема**: см. [docs/THEME_PALETTE.md](docs/THEME_PALETTE.md) — `src/main/resources/styles/<name>-theme.css` + новое значение `ThemeManager.Theme`.
- **Новые тесты**: класс в `src/test/java/com/techhaven/<package>/`, наследование от JUnit 5 (`@Test`). Интеграционные тесты используют `target/test.db` автоматически (через surefire).
