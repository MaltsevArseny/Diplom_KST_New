# Changelog

Все значимые изменения в проекте DigitalHub документируются в этом файле.
Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.0.0/).
Проект следует [Semantic Versioning](https://semver.org/lang/ru/).

---

## [3.3.0] — 2026-05-16

### Безопасность данных и согласованность БД

#### Транзакционное оформление заказа (`OrderService.placeOrder`)
Раньше создание заказа состояло из 5 независимых SQL-операций (вставка `Orders`, цикл `OrderItems`, списание остатков, запись истории, очистка корзины), каждая в своей auto-commit транзакции. Сбой посередине оставлял БД в несогласованном состоянии: заказ есть, но позиций нет; позиции есть, но корзина не очищена; и т.п.

**Реализация:**
- В `OrderRepository`, `ProductRepository`, `CartRepository` добавлены overload-методы с параметром `Connection` (для участия в общей транзакции вызывающего).
- `OrderService.placeOrder` теперь открывает **одно соединение** с `setAutoCommit(false)`, выполняет все шаги (вставка заказа → позиции + atomic-списание стока → история → очистка корзины) и `commit`'ит только если всё прошло; при любом `SQLException` или провале atomic-stock — полный `rollback`.

#### Защита склада от ухода в минус (`ProductRepository.decrementStock`)
- Метод `updateStock(productId, quantity)` использовал бесусловный `UPDATE Products SET stock_quantity = stock_quantity - ? WHERE id = ?` — при гонке (например, два одновременных placeOrder на последний товар) остаток уходил в минус.
- Добавлен `decrementStock(conn, productId, quantity)` с условием `WHERE id = ? AND stock_quantity >= ?` — возвращает `boolean`, `false` означает «уже не хватает», вызывающий откатывает транзакцию и возвращает `MSG_STOCK_RACE`.

#### CHECK-constraints и UNIQUE-индексы (`schema.sql`)
- `Products.price >= 0`, `Products.stock_quantity >= 0`
- `Orders.total_amount >= 0`
- `OrderItems.quantity > 0`, `OrderItems.price_at_order >= 0`
- `Cart.quantity > 0`
- `CREATE UNIQUE INDEX idx_cart_user_product ON Cart(user_id, product_id)` — один товар у пользователя в корзине = одна запись
- `CREATE UNIQUE INDEX idx_favorites_user_product ON Favorites(user_id, product_id)` — то же для избранного
- Идемпотентный dedup перед созданием UNIQUE-индексов: `DELETE FROM Cart WHERE id NOT IN (SELECT MIN(id) ...)`, аналогично для `Favorites` — безопасная миграция существующих БД.

Существующие БД новые CHECK не получат (SQLite не поддерживает `ALTER TABLE ADD CONSTRAINT`), но защита остаётся на уровне приложения (валидация в сервисах) и применится для всех новых БД.

#### Service-уровень проверки прав администратора (defense-in-depth)
Раньше admin-операции защищались только UI-слоем (показ кнопки только админу) — это broken-by-design: вызов сервиса напрямую обходил защиту.

- `SessionManager.requireAdmin()` — бросает `SecurityException` если текущий пользователь не ADMIN.
- `ProductService.createProduct / updateProduct / deleteProduct` — добавлен `requireAdmin()`.
- `OrderService.updateStatus` — ADMIN всегда; USER может только отменить (`Отменён`) собственный незавершённый заказ.
- `OrderService.updatePlannedDelivery` — только ADMIN.
- Создан `service/UserService.java` с методами `listUsers`, `listAllIncludingAdmins`, `lockUser`, `unlockUser` (все требуют ADMIN); `AdminUsersView` переведён на этот сервис.

### Валидация и UX

#### Полная валидация товаров (`ProductService.validate`)
Перед `createProduct`/`updateProduct` проверяется:
- непустое имя
- цена ≥ 0 (даже на старых БД, где CHECK не применился)
- остаток ≥ 0
- заполненная категория, существующая в справочнике `Categories`
- отсутствие дубля имени (с исключением самого редактируемого товара через `excludeId`)

При ошибке бросается `IllegalArgumentException` с локализованным сообщением; `AdminProductsView` отображает его через `DialogHelper.showError`.

#### Email нормализуется в lowercase при изменении профиля
`ProfileView` сохранял email в смешанном регистре, а `AuthService.login` ищет пользователя по `email.trim().toLowerCase()` → после смены email пользователь не мог войти. Теперь поле приводится к нижнему регистру перед сохранением.

#### Отчёт «Повторные» уважает выбранный период
`AdminReportsView.conv()` считал повторных покупателей за всё время `SELECT COUNT(*) FROM (SELECT user_id FROM Orders GROUP BY user_id HAVING COUNT(*) > 1)`, игнорируя `from`/`to` пикеры. Теперь подзапрос фильтруется по `order_date` диапазону.

### Читаемость в светлой теме (inline-style audit)

В [3.2.1] добавлена runtime-смена светлой/тёмной темы через CSS-токены `-th-*`. Однако в Java-view'ах остались **inline-`setStyle(...)`** вызовы с жёстко зашитыми цветами из dark-палитры (`#d0d0e8`, `#cbd5e1`, `#94a3b8`, `-th-cream` на песочном фоне) — в светлой теме они становились нечитаемыми.

**Заменено на токены:**
- `AdminReportsView.cardTip`: значение KPI `-th-cream` → `-th-text-primary`, фон `-th-bg-primary` → `-th-bg-card`
- `AdminReportsView.buildBarChart`: подпись `#cbd5e1` → `-th-text-primary`, значение `#94a3b8` → `-th-text-secondary`
- `AdminOrdersView` столбцы «Доставка (жел.)»/«Доставка (уст.)»: `#d0d0e8` → `-th-text-primary` (4 места)
- `AdminOrdersView` состав заказа в диалоге: `#cbd5e1` → `-th-text-primary`
- `AdminOrdersView` метаданные истории статусов: `#94a3b8` → `-th-text-secondary`
- `AdminUsersView` причина блокировки и заголовок диалога: `#f87171` → `-th-danger`; иконка admin-badge: `#c4b5fd` → `-th-accent-light`
- `OrdersView` комментарий заказа: `#94a3b8` → `-th-text-muted`; ссылка на товар: `#60a5fa` → `-th-accent`
- `CatalogView.product-card` hover-фон: `#353555` → `-th-bg-hover` (на светлой теме хардкод "затемнял" карточку настолько, что коричневый текст становился нечитаемым).

#### TOC в справках — явные гиперссылки
`HelpView` уже рендерил TOC как кликабельные `Hyperlink`'и через `scrollToAnchor`, но визуально они выглядели как обычный фиолетовый текст — пользователь не знал, что они кликабельны. Добавлены `-fx-underline: true` и `-fx-cursor: hand` сразу (без необходимости hover'а).

### Тесты

- **+10 тестов** (286 → 296):
  - `OrderServiceTest.placeOrderDoesNotAllowNegativeStock` — заказ не создан, стоковая запись не уходит в минус при недостаточном остатке
  - `OrderServiceTest.placeOrderRollsBackOnFailure` — все 4 эффекта успешного заказа согласованы (orders+1, stock-1, history++, cart=0)
  - `OrderServiceTest.userCannotChangeStatusToInProcess` — USER без admin-прав получает SecurityException при смене статуса
  - `ProductServiceTest.validate{Empty,Negative,Duplicate,NonExistentCategory}*` — 7 кейсов валидации товара
- Существующие admin-only тесты `validStatusTransitionReturnsNull` / `cancelFromCompletedIsForbidden` / `updatePlannedDeliveryForSeedOrder` переключаются на admin-сессию через `loginAdmin()` helper перед operations.
- **Итого: 296/296 ✓**

---

## [3.2.2] — 2026-05-16

### Архитектура: единая точка резолва путей (AppPaths)

Добавлен `config/AppPaths.java` — централизованный резолвер runtime-путей. Раньше пути были разбросаны по коду с зависимостью от текущей рабочей директории:
- `DatabaseManager:31` — `System.getProperty("db.path", "dist/digitalhub.db")` — относительный
- `HelpView:140-142` — поиск manuals через `"../"`/`"../../"` hack
- `AdminProductsView:526` — `Paths.get("product_images")` — относительный

Теперь приложение **работает из любой папки запуска** — все ресурсы резолвятся относительно расположения JAR'а через `CodeSource.getLocation()` с fallback на `user.dir` для IDE.

**Изменено:**
- `DatabaseManager.DB_URL` → `AppPaths.dbFile()`
- `HelpView.readMarkdownFile()` → `AppPaths.findDocumentFile()`  
- `AdminProductsView.copyImageToProductsDir/loadProductImage` → `AppPaths.productImagesDir()` / `AppPaths.dataDir()`
- `scripts/run.bat` — убран hardcoded `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21` (приложение упало бы, если у пользователя другая Java). Поиск: portable JRE → JAVA_HOME → ProgramFiles → PATH.
- `scripts/run.sh` — переход в `dist/` перед запуском JAR (AppPaths корректно определит dataDir).
- `pom.xml` surefire — `<systemPropertyVariables><db.path>${project.build.directory}/test.db</db.path>` — гарантирует, что **тесты не пишут в production БД** (проблема обнаружена в 3.2.1: 15 заказов попали в `dist/digitalhub.db` за один прогон).

**Override через system properties:**
- `-Dapp.data.dir=<path>` — задаёт корневую директорию данных
- `-Ddb.path=<file>` — переопределяет путь к БД (для тестов / миграции)

### Чистка `SecurityManager`
- `"UTF-8"` строкой (deprecated в Java 7+) → `StandardCharsets.UTF_8` (3 места)
- Убраны `UnsupportedEncodingException` из catch-блоков (после замены — unreachable)

### Тесты
- **+10 тестов** `AppPathsTest`: override через `-Dapp.data.dir`, создание директорий, поиск документов в data-dir / parent, default-значения, кэширование, абсолютный путь
- **Итого: 286/286 ✓** (276 → 286), все проходят

### Verification: запуск без установленной Java
Smoke-test: запуск `dist/jre/bin/java.exe -jar dist/DigitalHub.jar` (embedded Temurin JRE 21.0.6) — приложение стартует и закрывается с exit 0. **Машина пользователя НЕ обязана иметь Java**, JRE поставляется в комплекте.

---

## [3.2.1] — 2026-05-16

### Изменено
- **Светлая тема: переход на песочную палитру** (sand). Фон: `#f5ede0`, карточки: `#f0e6d2`, текст: `#3d2f1f`. Чисто-белый цвет (`#ffffff`) исключён полностью из светлой темы — ни в фонах, ни в шрифтах.
- **`-th-cream` переменная** добавлена в обе темы как замена `white` для текста на акцентных фонах: `#faf3e0` в светлой (кремовый), `#f5f5f5` в тёмной (мягкий off-white). 39 inline `setStyle("...white...")` заменены на `setStyle("...-th-cream...")` в 12 view-файлах + 8 селекторов в `dark-theme.css`.
- **`HelpView` отрефакторен** — hardcoded темные hex (`#1a1a2e`, `#2a2a44`, `#1e1e30`, `#252540`, `#d0d0e8`, `#c8c8dc`, `#818cf8`, `#a3e635`) заменены на CSS-переменные `-th-*`. Справка теперь корректно отображается в светлой теме.
- **`AdminReportsView` тайлы** (3 точки: hover-стиль кнопки печати + 2 wrapper-блока «Топ-10 по категориям») заменены с `#1a1a2e`/`#2d2d48` на `-th-bg-card`/`-th-border`.
- **Дубль `undoBtn.setStyle("#3b82f6 + white")`** в `CartView`, `FavoritesView`, `AdminProductsView` устранён — вынесен в CSS-класс `.undo-button`, определённый в обеих темах.

### Cross-platform
- **`pom.xml` Maven profiles** — автоматическая активация `javafx.platform` (`win`/`mac`/`linux`) по OS family. Раньше было `<javafx.platform>win</javafx.platform>` hardcoded — сборка на macOS/Linux подтягивала только пустые placeholder jar'ы → runtime `Module javafx.graphics not found`. Принудительное переопределение: `mvn package -Pmacos` или `-Djavafx.platform=linux`.

### Документация
- **`docs/THEME_PALETTE.md`** — новый документ: Single Source of Truth для всех `-th-*` переменных. Таблица: переменная × тема × места использования (Java-классы, CSS-селекторы). Инструкция «как добавить новую тему». Список семантических констант (статусы заказов, цвета аватаров), которые сознательно остаются hardcoded.
- **`README.md` § 7.bis** — секция «Темизация» переписана с акцентом на SSoT, добавлена ссылка на `THEME_PALETTE.md` и описание sand-палитры.
- **`README.md` § 4.7** — описание Maven cross-platform профилей.
- **`dark-theme.css` / `light-theme.css` headers** — добавлен явный маркер «★★★ ЕДИНАЯ ТОЧКА ИЗМЕНЕНИЯ ЦВЕТОВ ★★★» со ссылкой на `THEME_PALETTE.md`.

### Тесты
- Все 270 тестов проходят (`mvn clean test -B` — BUILD SUCCESS, 11 сек).

---

## [3.2.0] — 2026-05-16

### Добавлено
- **Светлая тема оформления** (`light-theme.css`): полное зеркало `dark-theme.css` с теми же селекторами и набором CSS-переменных `-th-*`, но светлой палитрой
- **`ThemeManager`** (`config/ThemeManager.java`): singleton хранит активную тему, сохраняет выбор в `java.util.prefs.Preferences` (узел `com/techhaven`, ключ `ui.theme`), оповещает слушателей при смене, метод `applyTo(Scene)` снимает старый theme-CSS и подключает актуальный
- **`ThemeToggle`** (`view/ThemeToggle.java`): утилита для UI-кнопки переключателя ☀/🌙 — обновляет иконку, переключает и применяет тему
- **Кнопка переключения темы** в навбаре пользователя (`MainLayout`), топбаре администратора (`AdminLayout`) и окне входа (`LoginView`)
- **Горячая клавиша `Alt+T`** для переключения темы из любого экрана
- **14 новых тестов** для `ThemeManager` (JUnit 5): дефолт = DARK, set/get, toggle, persist в Preferences, fallback при невалидном значении, listener add/remove/notify, getCssPath, наличие CSS-ресурсов в classpath, singleton identity, Theme.opposite, isDark/isLight согласованность

### Изменено
- `MainApp.start()`, `MainApp.setScene()` (обе перегрузки), `MainApp.showLogin()` — вместо явного `scene.getStylesheets().add(".../dark-theme.css")` теперь `ThemeManager.getInstance().applyTo(scene)`
- `DialogHelper.buildScene()` — то же изменение
- **Рефакторинг inline-стилей**: 174 замены в 14 view-файлах — hardcoded HEX заменены на CSS-переменные `-th-*` (например, `setStyle("...#1e1e2e...")` → `setStyle("...-th-bg-primary...")`)
- **Корневые backgrounds** в `MainLayout`, `AdminLayout`, `LoginView` — вместо inline `setStyle("-fx-background-color: #1e1e2e;")` теперь `getStyleClass().add("app-root")`
- **Кнопки управления окном** (`_`, `□`, `×`) в `MainLayout`, `AdminLayout`, `LoginView` — теперь CSS-классы `window-control-button` и `window-close-button` вместо строки `winBtnBase`
- **Кнопка «❓ Справка»** — CSS-класс `help-button` вместо inline-стиля
- **Бейдж «ADMIN»** в `AdminLayout` — CSS-класс `role-badge-admin`
- **`DialogHelper.createCloseButton()`** — CSS-класс `dialog-close-button` вместо ~30 строк inline-стиля
- **`DialogHelper.cardStyle()`** — все hex заменены на CSS-переменные

### Сохранено как константы
- Цвета статусов заказов (`AdminOrdersView.statusColor()` — «Новый» = синий, «Доставлен» = зелёный и т.п.) — семантические, одинаковые в обеих темах
- Цвета аватаров пользователей — раскрашиваются по хэшу username, едины для тем
- Акценты уведомлений (`DialogHelper.showInfo/showError/showWarning`)

### Документация
- `README.md` — версия 3.2, новый раздел «7.bis. Темизация (Dark / Light)» с описанием архитектуры, перечнем CSS-переменных, инструкцией по добавлению новой темы
- `UserManual.md` — добавлено описание кнопки темы и `Alt+T` в разделах «Навигация», «Горячие клавиши», «Частые вопросы»
- `AdminManual.md` — то же для интерфейса администратора

---

## [3.1.0] — 2026-03-15

### Добавлено
- **Кэш категорий** (`ProductCache`): список категорий теперь хранится в памяти и не запрашивается из БД при каждом открытии каталога — снижает latency при навигации
- **Кросс-платформенный запуск** (`run.sh`): скрипт сборки и запуска для Linux и macOS с проверкой версии Java и наличия Maven
- **CHANGELOG.md**: история версий проекта (этот файл)
- Раздел «Запуск на Linux/macOS» и «Как обновить» в `README.md`
- **8 новых тестов** для `ProductCache` (JUnit 5): hit/miss кэша, инвалидация, конкурентный доступ, интеграция с сервисом

### Изменено
- `ProductService.getCategories()` теперь обращается к `ProductCache` вместо прямого запроса к БД
- `ProductService.createProduct()`, `updateProduct()`, `deleteProduct()` инвалидируют кэш категорий

---

## [3.0.0] — 2026-03-01

### Добавлено
- Нормализация базы данных до **3НФ**: справочники `Categories` (11 записей) и `OrderStatuses` (8 записей) вынесены в отдельные таблицы с внешними ключами
- Интерфейсы репозиториев (`IProductRepository`, `ICartRepository`, `IOrderRepository`, `IUserRepository`, `IFavoriteRepository`) для декаплинга и тестируемости
- Javadoc-документация на все публичные методы
- Полный рефакторинг: enum-ы для ролей и статусов, шина событий (`EventBus`)
- Очистка кода: ноль предупреждений компилятора

### Изменено
- Схема БД: `Products.category` → `Products.category_id` (references `Categories.id`)
- Схема БД: `Orders.status` → `Orders.status_id` (references `OrderStatuses.id`)
- `ProductRepository`: все SELECT используют JOIN с `Categories`

---

## [2.2.0] — 2026-02-15

### Добавлено
- **Аналитика продаж** (панель администратора): KPI-карточки, топ-10 товаров, продажи по категориям, конверсия клиентов, печать отчёта через браузер
- Фильтр по дате в аналитике (7д / 30д / 90д / произвольный период)
- **Встроенная справка** `HelpView` — открывается без перехода на другой экран
- **Горячие клавиши** для всех разделов: `Alt+1..5`, `F1`, `Alt+Q`, `Alt+M`, `Alt+W`, `Ctrl+P`
- 170 тестовых заказов с реалистичной историей статусов (период 01.01.2025 – 01.03.2026)

### Исправлено
- Дубликаты в seed-данных товаров устранены через `INSERT OR IGNORE` + уникальный индекс по названию

---

## [2.1.0] — 2026-01-25

### Добавлено
- **Прогресс-трекер заказа**: визуальные шаги Новый → В обработке → … → Завершён / Отменён
- Группировка товаров в корзине и избранном по категориям
- Паттерн **Undo** (5 секунд) при удалении из корзины и избранного
- Контроль складских остатков: кнопка `+` блокируется при достижении максимума
- Автозаполнение формы оформления заказа из предыдущего заказа

---

## [2.0.0] — 2026-01-10

### Добавлено
- **Панель администратора**: управление товарами (CRUD), заказами (смена статуса), пользователями (блокировка/разблокировка, создание администраторов)
- Шифрование PII **AES-256**: телефоны и адреса доставки хранятся в зашифрованном виде
- Блокировка аккаунта после 5 неудачных попыток входа (на 5 минут)
- 11 категорий товаров с цветовыми метками и emoji
- Пагинация каталога: «Показать ещё» загружает следующие 15 товаров

---

## [1.0.0] — 2025-12-01

### Добавлено
- Регистрация и авторизация с хешированием паролей **PBKDF2** (100 000 итераций)
- Каталог товаров с поиском и фильтрацией по категории и цене
- Корзина: добавление, изменение количества, удаление
- Избранное: сохранение понравившихся товаров
- Оформление заказа: адрес, телефон, дата и временной интервал доставки
- История заказов
- Личный кабинет: редактирование профиля, смена пароля
- База данных SQLite (один файл, не требует сервера)
- Собственный заголовок окна (без стандартной рамки Windows) с поддержкой перетаскивания
