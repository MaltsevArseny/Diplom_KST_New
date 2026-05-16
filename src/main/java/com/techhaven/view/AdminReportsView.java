package com.techhaven.view;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.techhaven.config.DatabaseManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminReportsView {

    private static final Logger LOG = Logger.getLogger(AdminReportsView.class.getName());
    private static final DateTimeFormatter SQL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DatabaseManager db = DatabaseManager.getInstance();

    private DatePicker fromPicker;
    private DatePicker toPicker;

    private HBox kpiRow;
    private HBox convRow;
    private VBox top10Box;
    private VBox catBox;
    private VBox catTopBox;

    // Кэш данных для экспорта
    private double[] lastKpi   = {0,0,0,0};
    private double[] lastConv  = {0,0,0,0};
    private List<Map<String, Object>> lastTop10   = new ArrayList<>();
    private List<Map<String, Object>> lastByCat   = new ArrayList<>();
    private Map<String, List<Map<String, Object>>> lastTop10Cat = new LinkedHashMap<>();

    public AdminReportsView(AdminLayout adminLayout) {}

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        Label heading = new Label("📊 Отчёты и аналитика");
        heading.getStyleClass().add("heading");

        HBox filterBar = buildFilterBar();

        kpiRow     = new HBox(16);
        convRow    = new HBox(16);
        top10Box   = new VBox(12);
        catBox     = new VBox(12);
        catTopBox  = new VBox(20);

        // Стиль обеих кнопок — единый
        String btnStyle     = "-fx-background-color:-th-bg-secondary;-fx-border-color:-th-border;-fx-border-width:1;" +
                              "-fx-border-radius:8;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:6 18;";
        String btnHoverStyle = "-fx-background-color:#2d2d48;-fx-border-color:-th-accent-hover;-fx-border-width:1;" +
                               "-fx-border-radius:8;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:6 18;";

        // Кнопка Печать — стилизованная иконка принтера
        javafx.scene.layout.StackPane pIcon = new javafx.scene.layout.StackPane();
        javafx.scene.shape.Rectangle pBody = new javafx.scene.shape.Rectangle(30, 22);
        pBody.setArcWidth(4); pBody.setArcHeight(4);
        pBody.setFill(javafx.scene.paint.Color.web("#475569"));
        javafx.scene.shape.Rectangle pPaper = new javafx.scene.shape.Rectangle(18, 10);
        pPaper.setArcWidth(2); pPaper.setArcHeight(2);
        pPaper.setFill(javafx.scene.paint.Color.WHITE);
        pPaper.setTranslateY(-8);
        javafx.scene.layout.StackPane.setAlignment(pPaper, Pos.TOP_CENTER);
        pIcon.getChildren().addAll(pBody, pPaper);
        pIcon.setMinHeight(30);
        Label printLabel = new Label("Печать");
        printLabel.setStyle("-fx-text-fill:-th-text-secondary;-fx-font-size:9px;-fx-font-weight:bold;");
        VBox printContent = new VBox(2, pIcon, printLabel);
        printContent.setAlignment(Pos.CENTER);
        Button printBtn = new Button();
        printBtn.setGraphic(printContent);
        printBtn.setStyle(btnStyle);
        printBtn.setTooltip(new javafx.scene.control.Tooltip("Предпросмотр и печать (открывается в браузере) [Ctrl+P]"));
        printBtn.setOnMouseEntered(e -> printBtn.setStyle(btnHoverStyle));
        printBtn.setOnMouseExited(e -> printBtn.setStyle(btnStyle));
        printBtn.setOnAction(e -> printReport());

        // Добавляем кнопку в filterBar справа
        Region fbSpacer = new Region();
        HBox.setHgrow(fbSpacer, Priority.ALWAYS);
        filterBar.getChildren().addAll(fbSpacer, printBtn);

        // Ctrl+P — горячая клавиша печати
        printBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                    new javafx.scene.input.KeyCodeCombination(
                        javafx.scene.input.KeyCode.P,
                        javafx.scene.input.KeyCombination.CONTROL_DOWN),
                    this::printReport);
            }
        });

        // Мозаика: топ-10 и категории — рядом
        HBox chartsRow = new HBox(16, wrapSection("🏆 Топ-10 товаров", top10Box),
                                      wrapSection("📂 По категориям",    catBox));
        HBox.setHgrow(chartsRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(chartsRow.getChildren().get(1), Priority.ALWAYS);



        VBox content = new VBox(20,
            heading, filterBar,
            new Separator(),
            section("📈 Ключевые показатели"),  kpiRow,
            section("👥 Конверсия клиентов"),          convRow,
            chartsRow,
            section("📋 Топ-10 по категориям"),           catTopBox
        );
        content.setPadding(new Insets(0, 8, 24, 0));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().add(scroll);
        // По умолчанию — последние 7 дней
        LocalDate today = LocalDate.now();
        fromPicker.setValue(today.minusDays(7));
        toPicker.setValue(today);
        refresh(fromPicker.getValue(), toPicker.getValue());
        return root;
    }

    // ─── Фильтр дат ────────────────────────────────────────────────────────
    private HBox buildFilterBar() {
        Label fl = new Label("С:");  fl.setStyle("-fx-text-fill: -th-text-secondary;");
        Label tl = new Label("По:"); tl.setStyle("-fx-text-fill: -th-text-secondary;");

        fromPicker = new DatePicker();
        fromPicker.setPromptText("Начало");
        fromPicker.setPrefWidth(145);

        toPicker = new DatePicker();
        toPicker.setPromptText("Конец");
        toPicker.setPrefWidth(145);

        Button apply = new Button("▶ Применить");
        apply.setTooltip(new javafx.scene.control.Tooltip("Применить фильтр по датам"));
        apply.getStyleClass().addAll("button", "btn-primary");
        apply.setOnAction(e -> refresh(fromPicker.getValue(), toPicker.getValue()));

        Button reset = new Button("× Сбросить");
        reset.setTooltip(new javafx.scene.control.Tooltip("Сбросить фильтр дат"));
        reset.getStyleClass().addAll("button", "btn-small");
        reset.setOnAction(e -> { fromPicker.setValue(null); toPicker.setValue(null); refresh(null, null); });

        Button d7  = qBtn("7д",  7);
        Button d30 = qBtn("30д", 30);
        Button d90 = qBtn("90д", 90);

        HBox bar = new HBox(8, fl, fromPicker, tl, toPicker, apply, reset, d7, d30, d90);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.setStyle("-fx-background-color:-th-bg-secondary;-fx-background-radius:10;-fx-border-color:-th-border;-fx-border-radius:10;");
        return bar;
    }

    private Button qBtn(String label, int days) {
        Button b = new Button(label);
        b.setTooltip(new javafx.scene.control.Tooltip("Показать данные за " + days + " дней"));
        b.getStyleClass().addAll("button", "btn-small");
        b.setOnAction(e -> {
            toPicker.setValue(LocalDate.now());
            fromPicker.setValue(LocalDate.now().minusDays(days));
            refresh(fromPicker.getValue(), toPicker.getValue());
        });
        return b;
    }

    // ─── Обновление всех блоков ─────────────────────────────────────────────

    private void refresh(LocalDate from, LocalDate to) {
        String f = from != null ? from.format(SQL_FMT) : null;
        String t = to   != null ? to.format(SQL_FMT)   : null;

        lastKpi    = kpi(f, t);
        lastConv   = conv(f, t);
        lastTop10  = top10(f, t);
        lastByCat  = byCat(f, t);
        lastTop10Cat = top10ByCat(f, t);

        // KPI — карточки с Tooltip
        kpiRow.getChildren().setAll(
            cardTip("💰 Суммарная выручка",  fmtD(lastKpi[0]),       "#7c3aed",
                "Общая сумма всех оплаченных заказов за выбранный период"),
            cardTip("📦 Заказов всего",     fmt(lastKpi[1], " шт"),   "#2563eb",
                "Сколько заказов было оформлено за выбранный период"),
            cardTip("🛒 Товаров продано",   fmt(lastKpi[2], " шт"),   "#059669",
                "Сколько штук товара было продано за выбранный период"),
            cardTip("🧾 Средний чек",       fmtD(lastKpi[3]),       "#d97706",
                "Средняя сумма одного заказа. Чем выше — тем выше покупательская активность")
        );

        // Конверсия — тоже с Tooltip
        double convPct   = lastConv[0] > 0 ? lastConv[1] / lastConv[0] * 100 : 0;
        double repeatPct = lastConv[1] > 0 ? lastConv[3] / lastConv[1] * 100 : 0;
        convRow.getChildren().setAll(
            cardTip("👤 Всего клиентов",    fmt(lastConv[0], " чел"), "#6366f1",
                "Все зарегистрированные пользователи магазина"),
            cardTip("🛍 Сделали заказ",   fmt(lastConv[1], " чел"), "#8b5cf6",
                "Пользователи, которые оформили хотя бы один заказ за выбранный период"),
            cardTip("📊 Конверсия",      String.format("%.1f%%", convPct),   "#ec4899",
                "Процент пользователей, которые делали покупки. Чем выше — тем больше посетителей становится покупателями"),
            cardTip("🔁 Повторные",    String.format("%.1f%%", repeatPct), "#14b8a6",
                "Доля покупателей с > 1 заказом = Повторные / Сделали × 100%%")
        );

        // Топ-10 — гистограмма по выручке
        top10Box.getChildren().setAll(buildBarChart(lastTop10, "name", "revenueRaw", "revenue", "category", "#7c3aed"));

        // По категориям — гистограмма по выручке
        catBox.getChildren().setAll(buildBarChart(lastByCat, "category", "revenueRaw", "revenue", null, "#059669"));

        // Топ-10 по категориям — ровно 3 плитки в ряд (группировка вручную)
        catTopBox.getChildren().clear();
        List<Map.Entry<String, List<Map<String, Object>>>> catEntries =
            new java.util.ArrayList<>(lastTop10Cat.entrySet());
        VBox tileRows = new VBox(10);
        for (int i = 0; i < catEntries.size(); i += 3) {
            HBox tileRow = new HBox(10);
            for (int j = i; j < Math.min(i + 3, catEntries.size()); j++) {
                Map.Entry<String, List<Map<String, Object>>> entry = catEntries.get(j);
                String[] cc = categoryColors(entry.getKey());
                Label hdr = new Label(cc[2] + " " + entry.getKey());
                hdr.setStyle("-fx-text-fill:" + cc[0] + ";-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:0 0 4 0;");
                VBox chart = buildBarChart(entry.getValue(), "name", "revenueRaw", "revenue", null, cc[0], 140);
                VBox tile = new VBox(6, hdr, chart);
                tile.setPadding(new Insets(10, 12, 10, 12));
                tile.setStyle(
                    "-fx-background-color:#1a1a2e;" +
                    "-fx-border-color:#2d2d48;-fx-border-width:1;" +
                    "-fx-border-radius:10;-fx-background-radius:10;");
                HBox.setHgrow(tile, Priority.ALWAYS);
                tileRow.getChildren().add(tile);
            }
            tileRows.getChildren().add(tileRow);
        }
        catTopBox.getChildren().add(tileRows);
    }

    // ─── SQL-запросы ────────────────────────────────────────────────────────
    private double[] kpi(String f, String t) {
        String sql = "SELECT COALESCE(SUM(o.total_amount),0) AS revenue," +
                     " COUNT(DISTINCT o.id) AS orders," +
                     " COALESCE(SUM(oi.quantity),0) AS items," +
                     " COALESCE(AVG(o.total_amount),0) AS avg_check" +
                     " FROM Orders o LEFT JOIN OrderItems oi ON oi.order_id=o.id" +
                     where("o.order_date", f, t, true);
        try (Connection c = db.getConnection(); PreparedStatement ps = ps(c, sql, f, t)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new double[]{
                rs.getDouble("revenue"), rs.getDouble("orders"),
                rs.getDouble("items"),   rs.getDouble("avg_check")};
        } catch (Exception e) { LOG.log(Level.WARNING, "kpi error", e); }
        return new double[]{0,0,0,0};
    }

    private double[] conv(String f, String t) {
        double total = 0, ordered = 0, repeat = 0;
        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM Users WHERE role='USER'");
            if (rs.next()) total = rs.getDouble(1);
        } catch (Exception e) { LOG.log(Level.WARNING, "conv total", e); }

        String sql2 = "SELECT COUNT(DISTINCT user_id) FROM Orders" + where("order_date", f, t, true);
        try (Connection c = db.getConnection(); PreparedStatement ps = ps(c, sql2, f, t)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) ordered = rs.getDouble(1);
        } catch (Exception e) { LOG.log(Level.WARNING, "conv ordered", e); }

        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery(
                "SELECT COUNT(*) FROM (SELECT user_id FROM Orders GROUP BY user_id HAVING COUNT(*)>1)");
            if (rs.next()) repeat = rs.getDouble(1);
        } catch (Exception e) { LOG.log(Level.WARNING, "conv repeat", e); }

        return new double[]{total, ordered, 0, repeat};
    }

    private List<Map<String, Object>> top10(String f, String t) {
        String sql = "SELECT p.name, cat.name AS category, SUM(oi.quantity) AS sold," +
                     " SUM(oi.quantity*oi.price_at_order) AS revenue" +
                     " FROM OrderItems oi" +
                     " JOIN Products p ON p.id=oi.product_id" +
                     " JOIN Categories cat ON p.category_id=cat.id" +
                     " JOIN Orders o ON o.id=oi.order_id" +
                     where("o.order_date", f, t, true) +
                     " GROUP BY oi.product_id ORDER BY revenue DESC LIMIT 10";
        return rows(sql, f, t, -1, null, (rs, rank) -> {
            Map<String, Object> r = new LinkedHashMap<>();
            int    sold    = rs.getInt("sold");
            double revenue = rs.getDouble("revenue");
            r.put("rank",       rank);
            r.put("name",       rs.getString("name"));
            r.put("category",   rs.getString("category"));
            r.put("sold",       sold + " шт");
            r.put("revenue",    fmtD(revenue));
            r.put("soldRaw",    (double) sold);
            r.put("revenueRaw", revenue);
            return r;
        });
    }

    private List<Map<String, Object>> byCat(String f, String t) {
        String sql = "SELECT cat.name AS category, SUM(oi.quantity) AS sold," +
                     " SUM(oi.quantity*oi.price_at_order) AS revenue," +
                     " AVG(oi.price_at_order) AS avg_price," +
                     " COUNT(DISTINCT oi.product_id) AS products" +
                     " FROM OrderItems oi" +
                     " JOIN Products p ON p.id=oi.product_id" +
                     " JOIN Categories cat ON p.category_id=cat.id" +
                     " JOIN Orders o ON o.id=oi.order_id" +
                     where("o.order_date", f, t, true) +
                     " GROUP BY cat.name ORDER BY revenue DESC";
        return rows(sql, f, t, -1, null, (rs, rank) -> {
            Map<String, Object> r = new LinkedHashMap<>();
            int    sold    = rs.getInt("sold");
            double revenue = rs.getDouble("revenue");
            r.put("category",   rs.getString("category"));
            r.put("sold",       sold + " шт");
            r.put("revenue",    fmtD(revenue));
            r.put("avgprice",   fmtD(rs.getDouble("avg_price")));
            r.put("products",   rs.getInt("products"));
            r.put("soldRaw",    (double) sold);
            r.put("revenueRaw", revenue);
            return r;
        });
    }

    private Map<String, List<Map<String, Object>>> top10ByCat(String f, String t) {
        List<String> cats = new ArrayList<>();
        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery(
                "SELECT name FROM Categories ORDER BY name");
            while (rs.next()) cats.add(rs.getString(1));
        } catch (Exception e) { LOG.log(Level.WARNING, "catlist", e); }

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (String cat : cats) {
            String wh  = where("o.order_date", f, t, true);
            String and = wh.isEmpty() ? " WHERE cat.name=?" : wh + " AND cat.name=?";
            String sql = "SELECT p.name, SUM(oi.quantity) AS sold," +
                         " SUM(oi.quantity*oi.price_at_order) AS revenue" +
                         " FROM OrderItems oi" +
                         " JOIN Products p ON p.id=oi.product_id" +
                         " JOIN Categories cat ON p.category_id=cat.id" +
                         " JOIN Orders o ON o.id=oi.order_id" +
                         and +
                         " GROUP BY oi.product_id ORDER BY revenue DESC LIMIT 10";
            int extra = (f != null ? 1 : 0) + (t != null ? 1 : 0);
            List<Map<String, Object>> list = rows(sql, f, t, extra, cat, (rs, rank) -> {
                Map<String, Object> r = new LinkedHashMap<>();
                int    sold    = rs.getInt("sold");
                double revenue = rs.getDouble("revenue");
                r.put("rank",       rank);
                r.put("name",       rs.getString("name"));
                r.put("sold",       sold + " шт");
                r.put("revenue",    fmtD(revenue));
                r.put("soldRaw",    (double) sold);
                r.put("revenueRaw", revenue);
                return r;
            });
            if (!list.isEmpty()) result.put(cat, list);
        }
        return result;
    }

    // ─── Печать: HTML в браузере c window.print() ─────────────────────────
    private void printReport() {
        try {
            File tmp = File.createTempFile("digitalhub_print_", ".html");
            tmp.deleteOnExit();
            java.nio.file.Files.writeString(tmp.toPath(), buildHtmlReport(), java.nio.charset.StandardCharsets.UTF_8);
            // Браузер откроет HTML и автоматически покажет диалог печати (Ctrl+P)
            if (java.awt.Desktop.isDesktopSupported() &&
                    java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(tmp.toURI());
            } else {
                DialogHelper.showError("Печать недоступна",
                    "Откройте файл вручную: " + tmp.getAbsolutePath());
            }
        } catch (java.io.IOException e) {
            LOG.log(Level.WARNING, "print error", e);
            DialogHelper.showError("Ошибка печати", e.getMessage());
        }
    }

    /** Генеррирует HTML-отчёт, точно повторяющий экран, с window.print() */
    private String buildHtmlReport() {
        double convPct   = lastConv[0] > 0 ? lastConv[1] / lastConv[0] * 100 : 0;
        double repeatPct = lastConv[1] > 0 ? lastConv[3] / lastConv[1] * 100 : 0;
        String period = fromPicker.getValue() != null || toPicker.getValue() != null ?
            (fromPicker.getValue() != null ? fromPicker.getValue().format(SQL_FMT) : "...") + " — " +
            (toPicker.getValue()   != null ? toPicker.getValue().format(SQL_FMT)   : "...") : "всё время";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='ru'><head><meta charset='UTF-8'>")
          .append("<title>Отчёт DigitalHub</title>")
          .append("<style>")
          .append("body{font-family:Arial,sans-serif;font-size:12px;color:#222;margin:20px;}")
          .append("h1{font-size:18px;text-align:center;margin-bottom:4px;}")
          .append("h2{font-size:14px;margin:16px 0 6px;border-bottom:2px solid #185ABD;padding-bottom:3px;color:#185ABD;}")
          .append("h3{font-size:12px;margin:10px 0 4px;color:#475569;}")
          .append(".kpi{display:flex;gap:12px;flex-wrap:wrap;margin-bottom:8px;}")
          .append(".kpi-card{border:1px solid #cbd5e1;border-radius:8px;padding:8px 14px;min-width:140px;}")
          .append(".kpi-card .val{font-size:16px;font-weight:bold;color:#185ABD;}")
          .append(".kpi-card .lbl{font-size:10px;color:#64748b;}")
          .append(".bar-row{display:flex;align-items:center;gap:6px;margin:2px 0;font-size:11px;}")
          .append(".bar-row .rank{min-width:18px;color:#94a3b8;font-size:10px;}")
          .append(".bar-row .name{min-width:180px;max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}")
          .append(".bar{height:12px;background:#185ABD;border-radius:3px;" +
                  "print-color-adjust:exact;-webkit-print-color-adjust:exact;}")
          .append(".bar-row .pct{color:#64748b;font-size:10px;white-space:nowrap;margin-left:4px;}")
          .append(".tiles{display:flex;flex-wrap:wrap;gap:10px;}")
          .append(".tile{border:1px solid #cbd5e1;border-radius:8px;padding:8px 10px;flex:0 0 30%;}")
          .append("@media print{" +
              "*{print-color-adjust:exact;-webkit-print-color-adjust:exact;}" +
              "body{margin:10px;}" +
              ".tile{break-inside:avoid;}" +
              ".bar{-webkit-print-color-adjust:exact;print-color-adjust:exact;}" +
              ".kpi-card{border:1px solid #cbd5e1 !important;}" +
              ".no-print{display:none;}" +
              "}")
          .append("</style>")
          .append("</head><body>");

        sb.append("<h1>Отчёт по продажам &laquo;DigitalHub&raquo;</h1>")
          .append("<p style='text-align:center;color:#64748b;font-size:11px'>Период: ")
          .append(period).append(" &bull; Сформирован: ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
          .append("</p>");

        // KPI
        sb.append("<h2>📈 Ключевые показатели</h2><div class='kpi'>");
        htmlKpiCard(sb, "💰 Суммарная выручка", fmtD(lastKpi[0]));
        htmlKpiCard(sb, "📦 Заказов всего", fmt(lastKpi[1], " шт"));
        htmlKpiCard(sb, "🛒 Товаров продано", fmt(lastKpi[2], " шт"));
        htmlKpiCard(sb, "🧾 Средний чек", fmtD(lastKpi[3]));
        htmlKpiCard(sb, "👤 Всего клиентов", fmt(lastConv[0], " чел"));
        htmlKpiCard(sb, "🛍 Сделали заказ", fmt(lastConv[1], " чел"));
        htmlKpiCard(sb, "📊 Конверсия", String.format("%.1f%%", convPct));
        htmlKpiCard(sb, "🔁 Повторные", String.format("%.1f%%", repeatPct));
        sb.append("</div>");

        // Топ-10 товаров
        sb.append("<h2>🏆 Топ-10 продаваемых товаров</h2>");
        htmlBarList(sb, lastTop10, "rank", "name", "revenue", "revenueRaw");

        // По категориям
        sb.append("<h2>📂 Продажи по категориям</h2>");
        htmlBarList(sb, lastByCat, null, "category", "revenue", "revenueRaw");

        // Топ-10 по категориям (плитки)
        sb.append("<h2>📋 Топ-10 по категориям</h2><div class='tiles'>");
        for (Map.Entry<String, List<Map<String, Object>>> e : lastTop10Cat.entrySet()) {
            sb.append("<div class='tile'>");
            sb.append("<h3>").append(escHtml(e.getKey())).append("</h3>");
            htmlBarList(sb, e.getValue(), null, "name", "revenue", "revenueRaw");
            sb.append("</div>");
        }
        sb.append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void htmlKpiCard(StringBuilder sb, String label, String value) {
        sb.append("<div class='kpi-card'><div class='val'>").append(escHtml(value))
          .append("</div><div class='lbl'>").append(escHtml(label)).append("</div></div>");
    }

    private void htmlBarList(StringBuilder sb, List<Map<String, Object>> data,
                             String rankKey, String nameKey, String fmtKey, String rawKey) {
        if (data == null || data.isEmpty()) return;
        double total = data.stream().mapToDouble(m -> toDouble(m.get(rawKey))).sum();
        if (total <= 0) total = 1;
        double max = data.stream().mapToDouble(m -> toDouble(m.get(rawKey))).max().orElse(1);
        if (max <= 0) max = 1;
        int auto = 1;
        for (Map<String, Object> row : data) {
            String rank  = rankKey != null ? str(row, rankKey) : String.valueOf(auto);
            String name  = str(row, nameKey);
            String val   = str(row, fmtKey);
            double raw   = toDouble(row.get(rawKey));
            int    pct   = (int) Math.round(raw / total * 100);
            int    barW  = (int) (raw / max * 120);
            sb.append("<div class='bar-row'>");
            sb.append("<span class='rank'>").append(rank).append(".</span>");
            sb.append("<span class='name' title='").append(escHtml(name)).append("'>").append(escHtml(name)).append("</span>");
            sb.append("<div class='bar' style='width:").append(Math.max(barW, 3)).append("px'></div>");
            sb.append("<span class='pct'>").append(escHtml(val)).append(" (").append(pct).append("%)</span>");
            sb.append("</div>");
            auto++;
        }
    }

    private static String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }



    // ─── Хелперы SQL ────────────────────────────────────────────────────────
    private String where(String col, String f, String t, boolean isWhere) {
        String kw = isWhere ? " WHERE" : " AND";
        if (f != null && t != null) return kw + " " + col + " BETWEEN ? AND ?";
        if (f != null)              return kw + " " + col + " >= ?";
        if (t != null)              return kw + " " + col + " <= ?";
        return "";
    }

    private PreparedStatement ps(Connection c, String sql, String f, String t) throws Exception {
        PreparedStatement ps = c.prepareStatement(sql);
        int i = 1;
        if (f != null) ps.setString(i++, f + " 00:00:00");
        if (t != null) ps.setString(i,   t + " 23:59:59");
        return ps;
    }

    @FunctionalInterface interface RowFn { Map<String, Object> map(ResultSet rs, int n) throws Exception; }

    private List<Map<String, Object>> rows(String sql, String f, String t,
                                            int extraIdx, String extraVal, RowFn fn) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = ps(c, sql, f, t)) {
            if (extraVal != null) ps.setString(extraIdx + 1, extraVal);
            ResultSet rs = ps.executeQuery();
            int n = 1;
            while (rs.next()) list.add(fn.map(rs, n++));
        } catch (Exception e) { LOG.log(Level.WARNING, "rows: " + sql, e); }
        return list;
    }

    // ─── UI хелперы ─────────────────────────────────────────────────────────
    /** Обёртка секции для мозаики — карточка с заголовком */
    private VBox wrapSection(String title, VBox chart) {
        Label hdr = new Label(title);
        hdr.setStyle("-fx-text-fill:-th-accent-light;-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:0 0 6 0;");
        VBox wrapper = new VBox(8, hdr, chart);
        wrapper.setPadding(new Insets(14, 16, 14, 16));
        wrapper.setStyle(
            "-fx-background-color:#1a1a2e;" +
            "-fx-border-color:#2d2d48;-fx-border-width:1;" +
            "-fx-border-radius:12;-fx-background-radius:12;");
        VBox.setVgrow(chart, Priority.ALWAYS);
        return wrapper;
    }



    /** \u041a\u0430\u0440\u0442\u043e\u0447\u043a\u0430 \u0441 Tooltip \u043d\u0430 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0438 \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u0435\u043b\u044f */
    private VBox cardTip(String title, String value, String color, String tip) {
        Label tl = new Label(title); tl.setStyle("-fx-text-fill:-th-text-secondary;-fx-font-size:12px;-fx-cursor:hand;");
        tl.setTooltip(new javafx.scene.control.Tooltip(tip));
        Label vl = new Label(value); vl.setStyle("-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;");
        VBox card = new VBox(6, tl, vl);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color:-th-bg-primary;-fx-border-color:" + color +
                      ";-fx-border-width:0 0 0 4;-fx-background-radius:12;-fx-border-radius:12;" +
                      "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),12,0,0,4);");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Label section(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:-th-text-primary;");
        l.setPadding(new Insets(6, 0, 2, 0));
        return l;
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v == null ? "" : v.toString();
    }
    private String fmtD(double v) { return String.format("%,.0f ₽", v); }
    private String fmt(double v, String suf) { return String.format("%,.0f", v) + suf; }

    /** Гистограмма с дефолтными размерами (широкие секции) */
    private VBox buildBarChart(
            List<Map<String, Object>> data,
            String labelKey, String rawKey, String fmtKey,
            String subKey, String color) {
        return buildBarChart(data, labelKey, rawKey, fmtKey, subKey, color, 195);
    }

    /** Гистограмма: бар занимает всё доступное пространство, пропорции через maxWidth binding */
    private VBox buildBarChart(
            List<Map<String, Object>> data,
            String labelKey, String rawKey, String fmtKey,
            String subKey, String color,
            double labelWidth) {
        List<Map<String, Object>> sorted = data.stream()
            .sorted((a, b) -> Double.compare(toDouble(b.get(rawKey)), toDouble(a.get(rawKey))))
            .toList();
        VBox box = new VBox(5);
        box.setPadding(new Insets(6, 4, 6, 4));
        if (sorted.isEmpty()) {
            Label empty = new Label("Нет данных за выбранный период");
            empty.setStyle("-fx-text-fill:-th-text-muted;-fx-font-size:13px;");
            box.getChildren().add(empty);
            return box;
        }
        double max = toDouble(sorted.get(0).get(rawKey));
        if (max <= 0) max = 1;
        final double MAX = max;
        double total = sorted.stream().mapToDouble(m -> toDouble(m.get(rawKey))).sum();
        if (total <= 0) total = 1;
        int i = 0;
        for (Map<String, Object> row : sorted) {
            String label = String.valueOf(row.getOrDefault(labelKey, ""));
            String sub   = subKey != null ? " • " + row.getOrDefault(subKey, "") : "";
            double val   = toDouble(row.get(rawKey));
            String fmt   = String.valueOf(row.getOrDefault(fmtKey, ""));
            double frac  = val / MAX;   // 0..1 относительно максимума
            int    pct   = (int) Math.round(val / total * 100);

            Label rankLbl = new Label(String.valueOf(++i) + ".");
            rankLbl.setStyle("-fx-text-fill:-th-text-muted;-fx-font-size:10px;");
            rankLbl.setPrefWidth(22); rankLbl.setMinWidth(22);

            Label nameLbl = new Label(label + sub);
            nameLbl.setStyle("-fx-text-fill:#cbd5e1;-fx-font-size:11px;");
            nameLbl.setPrefWidth(labelWidth);
            nameLbl.setMinWidth(80);   // минимум для плиток
            nameLbl.setMaxWidth(labelWidth);

            // Контейнер бара: разрастёт во всю доступную ширину (VBox вместо StackPane — не нужен отдельный импорт)
            Region barFill = new Region();
            barFill.setPrefHeight(14);
            barFill.setStyle("-fx-background-color:" + color + ";-fx-background-radius:3;");

            VBox barTrack = new VBox(barFill);
            barTrack.setAlignment(Pos.CENTER_LEFT);
            barTrack.setFillWidth(false); // ← не растягивать barFill, иначе все бары = 100%
            barTrack.setPrefHeight(14);
            HBox.setHgrow(barTrack, Priority.ALWAYS);

            // Привязываем ширину бара к ширине трека * долю frac (пропорционально максимуму)
            barFill.prefWidthProperty().bind(barTrack.widthProperty().multiply(frac));

            Label valLbl = new Label(fmt + " (" + pct + "%)");
            valLbl.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:10px;-fx-padding:2 0 0 6;");
            valLbl.setMinWidth(javafx.scene.control.Control.USE_PREF_SIZE);

            HBox barRow = new HBox(6, rankLbl, nameLbl, barTrack, valLbl);
            barRow.setAlignment(Pos.CENTER_LEFT);
            barRow.setPadding(new Insets(2, 4, 2, 4));
            if (i % 2 == 0) barRow.setStyle("-fx-background-color:rgba(255,255,255,0.03);-fx-background-radius:4;");
            box.getChildren().add(barRow);
        }
        return box;
    }

    private static double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        return 0;
    }

    /** Цвета и эмодзи категорий (единый источник — идентичен CatalogView, CartView и др.) */
    private String[] categoryColors(String category) {
        if (category == null) return new String[]{"#a78bfa", "rgba(124,58,237,0.15)", "📦"};
        return switch (category.toLowerCase().trim()) {
            case "процессоры", "процессор", "cpu"
                -> new String[]{"#f59e0b", "rgba(245,158,11,0.12)", "⚙"};
            case "видеокарты", "видеокарта", "gpu"
                -> new String[]{"#10b981", "rgba(16,185,129,0.12)", "🎮"};
            case "материнские платы", "материнская плата", "motherboard"
                -> new String[]{"#3b82f6", "rgba(59,130,246,0.12)", "🔌"};
            case "оперативная память", "озу", "ram", "память"
                -> new String[]{"#8b5cf6", "rgba(139,92,246,0.12)", "💾"};
            case "накопители", "ssd", "hdd", "диски"
                -> new String[]{"#06b6d4", "rgba(6,182,212,0.12)", "💿"};
            case "блоки питания", "бп", "psu"
                -> new String[]{"#f97316", "rgba(249,115,22,0.12)", "⚡"};
            case "корпуса", "корпус", "case"
                -> new String[]{"#64748b", "rgba(100,116,139,0.12)", "🖥"};
            case "охлаждение", "кулеры", "cooling"
                -> new String[]{"#2dd4bf", "rgba(45,212,191,0.12)", "❄"};
            case "периферия", "клавиатуры", "мыши", "peripherals"
                -> new String[]{"#ec4899", "rgba(236,72,153,0.12)", "🖱"};
            case "мониторы", "monitor"
                -> new String[]{"#a3e635", "rgba(163,230,53,0.12)", "🖥"};
            case "сетевое оборудование"
                -> new String[]{"#818cf8", "rgba(129,140,248,0.12)", "🌐"};
            default
                -> new String[]{"#a78bfa", "rgba(124,58,237,0.15)", "📦"};
        };
    }
}
