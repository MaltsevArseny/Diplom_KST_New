# Палитра тем DigitalHub — Single Source of Truth

> **Где менять цвета:** ВСЕ цвета приложения определены через CSS-переменные `-th-*` в секции `.root { }` двух файлов. Любые остальные места используют именно эти переменные — никаких hardcoded цветов в Java-коде или прочих CSS-селекторах **быть не должно**.

## Где править

| Тема | Файл | Секция |
|---|---|---|
| **Тёмная** (default) | `src/main/resources/styles/dark-theme.css` | `.root { ... }` строки **9-46** |
| **Светлая** (sand) | `src/main/resources/styles/light-theme.css` | `.root { ... }` строки **12-50** |

Изменение значения переменной в `.root` автоматически применяется ко ВСЕМ местам, где она используется — без правок Java-кода.

## Полный список переменных `-th-*`

| Переменная | Назначение | Где используется (примеры) | Dark | Light (sand) |
|---|---|---|---|---|
| `-th-bg-primary` | Основной фон окна / страницы | `.app-root`, `.content-area`, `.dialog-pane`, `.auth-container` | `#1e1e2e` | `#f5ede0` |
| `-th-bg-secondary` | Навбар, sidebar, header'ы таблиц | `.nav-bar`, `.sidebar`, `.search-field` фон, `.dialog-pane .header-panel` | `#252538` | `#ede0c8` |
| `-th-bg-card` | Карточки, поля ввода, ячейки таблицы | `.card`, `.product-card`, `.text-field`, `.button`, `.combo-box`, `.table-view`, `.table-row-cell` | `#2a2a3d` | `#f0e6d2` |
| `-th-bg-hover` | Hover-состояния | `.button:hover`, `.table-row-cell:hover`, `.window-control-button:hover` | `#353550` | `#e6d9bd` |
| `-th-accent` | Фирменный акцент (фиолетовый) | `.btn-primary`, активный таб, hyperlinks (light), border:focused | `#7c3aed` | `#7c3aed` |
| `-th-accent-hover` | Hover для accent-фонов | `.btn-primary:hover` | `#6d28d9` | `#6d28d9` |
| `-th-accent-light` | Светлый акцент (логотип, бейдж в dark) | Логотипы, текст бейджей в тёмной теме | `#a78bfa` | `#8b5cf6` |
| `-th-success` | Успех (зелёный) | `.btn-success`, `.badge-success`, цена, stock-available | `#10b981` | `#047857` |
| `-th-warning` | Предупреждение (жёлтый/янтарный) | `.btn-warning`, `.badge-warning`, stock-low | `#f59e0b` | `#b45309` |
| `-th-danger` | Опасность / ошибка (красный) | `.btn-danger`, `.error-label`, бейдж корзины, window-close-button | `#ef4444` | `#b91c1c` |
| `-th-text-primary` | Основной текст | `.label`, `.heading`, `.title-label`, body | `#f0f0f0` | `#3d2f1f` |
| `-th-text-secondary` | Приглушённый текст | `.label-secondary`, имя пользователя, sidebar-item, footer-цитата | `#a0a0b8` | `#5c4a35` |
| `-th-text-muted` | Подсказки, плейсхолдеры | `.label-muted`, prompt-text-fill, метки внутри карточек | `#6b6b80` | `#8a7456` |
| `-th-border` | Рамки полей и карточек | `.card`, `.text-field`, `.table-view`, `.combo-box`, `.spinner`, разделители | `#3a3a50` | `#d4c4a3` |
| `-th-border-focus` | Рамка при фокусе | `.text-field:focused`, `.search-field:focused` | `#7c3aed` | `#7c3aed` |
| `-th-shadow` | Тень для карточек | `dropshadow(..., -th-shadow, ...)` | `rgba(0,0,0,0.3)` | `rgba(61,47,31,0.10)` |
| `-th-cream` | **Замена белого** — для текста на акцентных фонах | `.btn-primary text-fill`, `.btn-danger text-fill`, tooltip text, check-box mark, badge text | `#f5f5f5` | `#faf3e0` |
| `-th-radius` | Базовый радиус скругления | (резерв на будущее) | `10` | `10` |
| `-th-radius-lg` | Большой радиус | (резерв на будущее) | `16` | `16` |

## Утилитарные CSS-классы (поверх палитры)

Эти классы лежат в самих theme.css ниже `.root { }` и применяются через `node.getStyleClass().add("...")` из Java-кода. Их значения цвета вычислены через переменные `-th-*`:

| Класс | Назначение | Используется в |
|---|---|---|
| `.app-root` | Фон корневого узла окна | `MainLayout`, `AdminLayout`, `LoginView` |
| `.window-control-button` | Кнопки `_` / `□` в кастомном топбаре | `MainLayout`, `AdminLayout`, `LoginView`, `HelpView` |
| `.window-close-button` | Кнопка `×` (модификатор поверх window-control-button) | те же |
| `.help-button` | Кнопка `❓ Справка` | `MainLayout`, `AdminLayout` |
| `.theme-toggle-button` | Кнопка `☀ / 🌙` | `MainLayout`, `AdminLayout`, `LoginView` |
| `.dialog-close-button` | Кнопка `×` в модальных диалогах | `DialogHelper.createCloseButton()` |
| `.role-badge-admin` | Бейдж «ADMIN» в админской панели | `AdminLayout` |
| `.sidebar-menu-title` | Заголовок «Меню» в sidebar | `AdminLayout` |
| `.drag-bar` | Зона перетаскивания окна | `LoginView` |
| `.undo-button` | Синяя кнопка «Отмена (5с) ↩» | `CartView`, `FavoritesView`, `AdminProductsView` |

## Как добавить новую тему (например, `sepia`)

1. **Скопировать** `light-theme.css` → `sepia-theme.css`.
2. **Изменить только** значения переменных в `.root { ... }` (строки 12-50). Селекторы под `.root` НЕ трогать.
3. **Добавить** `SEPIA("/styles/sepia-theme.css")` в `ThemeManager.Theme`.
4. **Расширить** `Theme.opposite()` (или превратить toggle в циклический обход).
5. **Обновить тест** `ThemeManagerTest.cssResourcesExist()` — добавить проверку `/styles/sepia-theme.css`.

## Чего НЕ делать

- ❌ **НЕ** добавлять hardcoded hex-цвета (`#ef4444`, `#1e1e2e`, ...) в Java `setStyle("...")`. Использовать переменные: `setStyle("-fx-background-color: -th-bg-card;")`.
- ❌ **НЕ** использовать `white` / `WHITE` ни в Java-коде, ни в CSS-селекторах. Всегда `-th-cream` — это даёт мягкий off-white в тёмной теме и кремовый в светлой.
- ❌ **НЕ** дублировать значения цветов между двумя theme.css. Если в обеих темах нужен один цвет (например, accent `#7c3aed`) — это нормально, переменная имеет одно значение в обеих, но определяется в каждом файле.
- ❌ **НЕ** менять имена переменных без обновления этой документации и всех мест использования.

## Семантические константы (НЕ темизуются — одинаковы в обеих темах)

Список цветов, которые сознательно остаются hardcoded по дизайн-замыслу — статусы заказов в `AdminOrdersView.statusColor()`, цвета аватаров (раскрашиваются по hash username), акценты уведомлений в `DialogHelper.showInfo/showError/showWarning`. Они представляют **семантику** (статус «Доставлен» всегда зелёный, статус «Отменён» всегда красный) и должны оставаться константой между темами.

См. также: [README.md § 7.bis](../README.md#7bis-темизация-dark--light).
