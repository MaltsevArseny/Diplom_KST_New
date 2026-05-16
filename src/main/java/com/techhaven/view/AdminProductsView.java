package com.techhaven.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.techhaven.model.Product;
import com.techhaven.service.ProductService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AdminProductsView {
    private static final Logger LOGGER = Logger.getLogger(AdminProductsView.class.getName());
    private final ProductService productService = new ProductService();
    private TableView<Product> table;
    private List<Product> allProducts = List.of();
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> stockFilter;

    public AdminProductsView(AdminLayout adminLayout) {
        // adminLayout не используется в текущей версии
    }

    @SuppressWarnings("unchecked")
    public Parent getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        // Заголовок
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("🖥 Управление товарами");
        heading.getStyleClass().add("heading");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Добавить товар");
        addBtn.setTooltip(new javafx.scene.control.Tooltip("Добавить новый товар"));
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showProductDialog(null));

        header.getChildren().addAll(heading, spacer, addBtn);

        // ── Тулбар поиска и фильтра ──────────────────────────────────────
        searchField = new TextField();
        searchField.setPromptText("🔍 Поиск по названию...");
        searchField.setPrefWidth(280);
        searchField.setStyle(
            "-fx-background-color: -th-bg-card; -fx-text-fill: -th-text-primary; -fx-border-color: -th-border;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 13px;"
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());

        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("📂 Тип товара");
        categoryFilter.setPrefWidth(220);
        categoryFilter.setOnAction(e -> applyFilter());

        Button clearBtn = new Button("× Сбросить");
        clearBtn.setTooltip(new javafx.scene.control.Tooltip("Сбросить фильтры"));
        clearBtn.getStyleClass().addAll("button", "btn-small");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            categoryFilter.setValue(null);
            categoryFilter.setPromptText("📂 Тип товара");
            stockFilter.setValue("Все");
        });

        // Фильтр по остатку
        stockFilter = new ComboBox<>(FXCollections.observableArrayList(
            "Все", "Более 3 шт.", "1–3 шт.", "Нет в наличии (0)"
        ));
        stockFilter.setValue("Все");
        stockFilter.setPrefWidth(180);
        stockFilter.setOnAction(e -> applyFilter());

        Label countLabel = new Label();
        countLabel.setStyle("-fx-text-fill: -th-text-secondary; -fx-font-size: 12px;");
        table = new TableView<>();
        table.itemsProperty().addListener((obs, old, items) -> {
            if (items != null) countLabel.setText("Отображается: " + items.size() + " / " + allProducts.size());
        });

        HBox toolbar = new HBox(10, searchField, categoryFilter, stockFilter, clearBtn, countLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-font-size: 13px;");

        // Подсветка строк по остатку
        table.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    setTooltip(null);
                } else if (item.getStockQuantity() <= 0) {
                    setStyle("-fx-background-color: rgba(239,68,68,0.12); -fx-text-fill: -th-danger; -fx-opacity: 0.85;");
                    setTooltip(new javafx.scene.control.Tooltip("⚠ Нет в наличии (остаток: 0)"));
                } else if (item.getStockQuantity() <= 3) {
                    setStyle("-fx-background-color: rgba(245,158,11,0.12); -fx-text-fill: -th-warning;");
                    setTooltip(new javafx.scene.control.Tooltip("⚠ Мало на складе (остаток: " + item.getStockQuantity() + ")"));
                } else {
                    setStyle("");
                    setTooltip(null);
                }
            }
        });

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        addTooltip(idCol, "Уникальный идентификатор товара");

        TableColumn<Product, String> imageCol = new TableColumn<>("Фото");
        imageCol.setPrefWidth(70);
        imageCol.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        imageCol.setStyle("-fx-alignment: CENTER;");
        addTooltip(imageCol, "Изображение товара");
        imageCol.setCellFactory(col -> new TableCell<Product, String>() {
            private final ImageView iv = new ImageView();
            {
                iv.setFitWidth(50);
                iv.setFitHeight(50);
                iv.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = loadProductImage(path);
                        if (img != null) {
                            iv.setImage(img);
                            setGraphic(iv);
                        } else {
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        TableColumn<Product, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(nameCol, "Название товара");

        TableColumn<Product, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        categoryCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(categoryCol, "Категория товара в каталоге");

        TableColumn<Product, Double> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
        addTooltip(priceCol, "Цена товара в рублях");
        priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_RIGHT);
                setText(empty || item == null ? null : String.format("%,.0f ₽", item));
            }
        });

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Остаток");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        stockCol.setPrefWidth(80);
        addTooltip(stockCol, "Количество товара на складе");
        stockCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null); setGraphic(null); setStyle("");
                } else if (item <= 0) {
                    Label badge = new Label("0 ⚠");
                    badge.setStyle(
                        "-fx-background-color: rgba(239,68,68,0.2); -fx-text-fill: -th-danger;" +
                        "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 13px;"
                    );
                    setGraphic(badge); setText(null); setStyle("");
                } else if (item <= 3) {
                    Label badge = new Label(item + " ⚠");
                    badge.setStyle(
                        "-fx-background-color: rgba(245,158,11,0.2); -fx-text-fill: -th-warning;" +
                        "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 13px;"
                    );
                    setGraphic(badge); setText(null); setStyle("");
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: -th-success; -fx-font-weight: bold;");
                    setGraphic(null);
                }
            }
        });

        TableColumn<Product, String> specCol = new TableColumn<>("Характеристики");
        specCol.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        specCol.setPrefWidth(150);
        specCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addTooltip(specCol, "Технические характеристики товара");

        TableColumn<Product, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setPrefWidth(100);
        addTooltip(actionsCol, "Редактирование и удаление товара");
        actionsCol.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button editBtn   = new Button("\u270F");  // ✒ карандаш
            private final Button deleteBtn = new Button("\uD83D\uDDD1"); // сметник
            {
                editBtn.getStyleClass().addAll("button", "btn-small");
                editBtn.setStyle("-fx-font-size:16px;-fx-padding:4 10;");
                editBtn.setTooltip(new javafx.scene.control.Tooltip("Редактировать товар"));

                deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
                deleteBtn.setStyle("-fx-font-size:16px;-fx-padding:4 10;");
                deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Удалить товар"));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product p = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showProductDialog(p));
                    
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
                    pause.setOnFinished(ev -> {
                        productService.deleteProduct(p.getId());
                        loadProducts();
                    });

                    deleteBtn.setOnAction(e -> {
                        // Показываем сообщение и кнопку отмены прямо в ячейке
                        Label undoMsg = new Label("Удалено");
                        undoMsg.setStyle("-fx-text-fill: -th-danger; -fx-font-weight: bold;");
                        
                        Button undoBtn = new Button("↩");
                        undoBtn.getStyleClass().addAll("button", "btn-small");
                        undoBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
                        undoBtn.setOnAction(ue -> {
                            pause.stop();
                            loadProducts(); // Просто обновляем таблицу
                        });
                        
                        HBox undoBox = new HBox(8, undoMsg, undoBtn);
                        undoBox.setAlignment(Pos.CENTER);
                        setGraphic(undoBox);
                        pause.play();
                    });
                    
                    HBox box = new HBox(6, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, imageCol, nameCol, categoryCol, priceCol, stockCol, specCol, actionsCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        loadProducts();

        root.getChildren().addAll(header, toolbar, table);
        return root;
    }

    private void loadProducts() {
        allProducts = productService.getAllProducts();
        // Заполняем категории
        if (categoryFilter != null) {
            java.util.Set<String> cats = new java.util.LinkedHashSet<>();
            cats.add("Все категории");
            for (Product p : allProducts) if (p.getCategory() != null && !p.getCategory().isBlank()) cats.add(p.getCategory());
            String prev = categoryFilter.getValue();
            categoryFilter.setItems(FXCollections.observableArrayList(cats));
            if (prev != null && cats.contains(prev)) categoryFilter.setValue(prev);
        }
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String cat   = categoryFilter != null ? categoryFilter.getValue() : null;
        boolean allCats = cat == null || cat.equals("Все категории");
        String stock = java.util.Objects.requireNonNullElse(
            stockFilter != null ? stockFilter.getValue() : null, "Все");

        ObservableList<Product> filtered = FXCollections.observableArrayList();
        for (Product p : allProducts) {
            boolean nameOk = query.isEmpty() || (p.getName() != null && p.getName().toLowerCase().contains(query));
            boolean catOk  = allCats || (p.getCategory() != null && p.getCategory().equalsIgnoreCase(cat));
            //noinspection DataFlowIssue
            boolean stockOk = stock == null || switch (stock) {
                case "Более 3 шт."       -> p.getStockQuantity() > 3;
                case "1–3 шт."           -> p.getStockQuantity() >= 1 && p.getStockQuantity() <= 3;
                case "Нет в наличии (0)" -> p.getStockQuantity() <= 0;
                default                  -> true;
            };
            if (nameOk && catOk && stockOk) filtered.add(p);
        }
        table.setItems(filtered);
    }

    private void showProductDialog(Product existing) {
        Stage stage = DialogHelper.createStage(com.techhaven.MainApp.getPrimaryStage(), true);
        stage.setTitle(existing == null ? "Новый товар" : "Редактирование товара");

        // ─── Поля формы ─────────────────────────────────────────────────
        TextField nameField = styledField(existing != null ? existing.getName() : "", "Название товара");
        TextArea  descField = new TextArea(existing != null ? existing.getDescription() : "");
        descField.setPromptText("Описание");
        descField.setPrefRowCount(3);
        descField.setStyle(fieldStyle());
        descField.setPrefWidth(320);
        TextField catField   = styledField(existing != null ? existing.getCategory() : "", "Категория");
        TextField priceField = styledField(existing != null ? String.valueOf(existing.getPrice()) : "", "Цена (₽)");
        TextField stockField = styledField(existing != null ? String.valueOf(existing.getStockQuantity()) : "", "Кол-во на складе");
        TextField specField  = styledField(existing != null ? existing.getSpecifications() : "", "Характеристики");

        // ─── Фото ────────────────────────────────────────────────────
        final String[] imgPath = {existing != null ? existing.getImagePath() : null};
        ImageView preview = new ImageView();
        preview.setFitWidth(100); preview.setFitHeight(100); preview.setPreserveRatio(true);
        if (imgPath[0] != null && !imgPath[0].isEmpty()) {
            Image img = loadProductImage(imgPath[0]);
            if (img != null) preview.setImage(img);
        }

        Label imgLabel = new Label(imgPath[0] != null ? getFileName(imgPath[0]) : "Фото не выбрано");
        imgLabel.setStyle("-fx-text-fill:-th-text-secondary;-fx-font-size:11px;");
        imgLabel.setWrapText(true); imgLabel.setMaxWidth(190);

        Button chooseImg = new Button("📷 Выбрать фото");
        chooseImg.setTooltip(new javafx.scene.control.Tooltip("Выбрать изображение товара"));
        chooseImg.getStyleClass().addAll("button", "btn-small");
        chooseImg.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Выберите изображение");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png","*.jpg","*.jpeg","*.gif","*.bmp"));
            File f = fc.showOpenDialog(stage);
            if (f != null) {
                String saved = copyImageToProductsDir(f);
                if (saved != null) {
                    imgPath[0] = saved;
                    imgLabel.setText(f.getName());
                    Image img = loadProductImage(saved);
                    if (img != null) preview.setImage(img);
                }
            }
        });
        Button clearImg = new Button("× Удалить");
        clearImg.setTooltip(new javafx.scene.control.Tooltip("Удалить фото товара"));
        clearImg.getStyleClass().addAll("button", "btn-small");
        clearImg.setOnAction(e -> { imgPath[0] = null; preview.setImage(null); imgLabel.setText("Фото не выбрано"); });

        VBox imageBox = new VBox(8,
            preview, imgLabel,
            new HBox(6, chooseImg, clearImg)
        );
        imageBox.setAlignment(Pos.TOP_LEFT);
        imageBox.setPadding(new Insets(10));
        imageBox.setStyle("-fx-background-color:-th-bg-secondary;-fx-background-radius:10;-fx-border-color:-th-border;-fx-border-radius:10;");

        // ─── Сетка формы ──────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);

        grid.add(fieldLabel("Название:"),        0, 0); grid.add(nameField,  1, 0);
        grid.add(fieldLabel("Описание:"),       0, 1); grid.add(descField,  1, 1);
        grid.add(fieldLabel("Категория:"),       0, 2); grid.add(catField,   1, 2);
        grid.add(fieldLabel("Цена (₽):"),        0, 3); grid.add(priceField, 1, 3);
        grid.add(fieldLabel("Кол-во на складе:"), 0, 4); grid.add(stockField, 1, 4);
        grid.add(fieldLabel("Характеристики:"),  0, 5); grid.add(specField,  1, 5);
        grid.add(fieldLabel("Фото:"),             0, 6); grid.add(imageBox,  1, 6);

        // ─── Кнопки ───────────────────────────────────────────────────
        final boolean[] saved = {false};
        Button saveBtn = new Button("💾 Сохранить");
        saveBtn.setTooltip(new javafx.scene.control.Tooltip("Сохранить изменения"));
        saveBtn.getStyleClass().addAll("button", "btn-primary");
        saveBtn.setPrefWidth(160);
        saveBtn.setPrefHeight(38);
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> {
            try {
                Product p = existing != null ? existing : new Product();
                p.setName(nameField.getText().trim());
                p.setDescription(descField.getText().trim());
                p.setCategory(catField.getText().trim());
                p.setPrice(Double.parseDouble(priceField.getText().trim()));
                p.setStockQuantity(Integer.parseInt(stockField.getText().trim()));
                p.setSpecifications(specField.getText().trim());
                p.setImagePath(imgPath[0]);
                if (existing != null) productService.updateProduct(p);
                else productService.createProduct(p);
                saved[0] = true;
                stage.close();
            } catch (NumberFormatException ex) {
                DialogHelper.showError("Ошибка ввода", "Некорректные числовые значения — проверьте цену и количество.");
            }
        });

        Button cancelBtn = new Button("\u2715 Отмена");
        cancelBtn.setTooltip(new javafx.scene.control.Tooltip("Отменить и закрыть"));
        cancelBtn.getStyleClass().addAll("button", "btn-small");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(38);
        cancelBtn.setStyle(
            "-fx-background-color:-th-border;" +
            "-fx-text-fill:-th-text-primary;" +
            "-fx-background-radius:8;" +
            "-fx-border-radius:8;" +
            "-fx-font-size:13px;" +
            "-fx-cursor:hand;"
        );
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> stage.close());

        HBox btnRow = new HBox(10, saveBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(16, 0, 0, 0));


        // ─── Заголовок ─────────────────────────────────────────────────
        Label title = new Label(existing == null ? "Новый товар" : "Редактирование товара");
        title.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:-th-text-primary;");

        HBox header = new HBox(title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 14, 0));

        // ─── Карточка диалога ─────────────────────────────────────────
        VBox card = new VBox(header, grid, btnRow);
        card.setPadding(new Insets(24));
        card.setMaxWidth(560);
        card.setStyle(DialogHelper.cardStyle());

        ScrollPane scroll = new ScrollPane(card);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setMaxHeight(700);

        DialogHelper.applyTransparentSceneAndWait(card, stage);

        if (saved[0]) loadProducts();
    }

    /** Стилизованное поле ввода */
    private TextField styledField(String val, String prompt) {
        TextField f = new TextField(val);
        f.setPromptText(prompt);
        f.setStyle(fieldStyle());
        f.setPrefWidth(320);
        return f;
    }

    /** Стиль полей ввода */
    private String fieldStyle() {
        return "-fx-background-color:-th-bg-secondary;" +
               "-fx-text-fill:-th-text-primary;" +
               "-fx-prompt-text-fill:#6b6b8a;" +
               "-fx-border-color:-th-border;" +
               "-fx-border-radius:8;" +
               "-fx-background-radius:8;" +
               "-fx-pref-height:38px;";
    }

    /** Метка колонки формы */
    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:-th-text-secondary;-fx-font-size:13px;");
        l.setMinWidth(140);
        return l;
    }

    /**
     * Копирует выбранное изображение в папку product_images и возвращает относительный путь.
     */
    private String copyImageToProductsDir(File sourceFile) {
        try {
            Path imagesDir = Paths.get("product_images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }
            String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            Path target = imagesDir.resolve(fileName);
            Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return "product_images/" + fileName;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Не удалось скопировать изображение", e);
            DialogHelper.showError("Ошибка загрузки", "Не удалось загрузить изображение:\n" + e.getMessage());
            return null;
        }
    }

    /**
     * Загружает изображение по пути (относительному или абсолютному).
     */
    private Image loadProductImage(String path) {
        try {
            // Попробуем как ресурс из classpath
            if (path.startsWith("/")) {
                var stream = getClass().getResourceAsStream(path);
                if (stream != null) return new Image(stream);
            }
            // Как файл на диске
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
            // Попробуем из resources
            var stream = getClass().getResourceAsStream("/" + path);
            if (stream != null) return new Image(stream);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Не удалось загрузить изображение: " + path, e);
        }
        return null;
    }

    private String getFileName(String path) {
        if (path == null) return "";
        int i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return i >= 0 ? path.substring(i + 1) : path;
    }
    /** Устанавливает подсказку (tooltip) на заголовок столбца таблицы */
    private <T> void addTooltip(TableColumn<Product, T> col, String text) {
        Label label = new Label(col.getText());
        label.setTooltip(new Tooltip(text));
        label.setStyle("-fx-text-fill: inherit; -fx-font-weight: inherit;");
        col.setGraphic(label);
        col.setText("");
    }

}
