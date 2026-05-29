package io.aurora.fx.components.tour;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Tour 标准 Node 组件演示
 * <p>
 * 覆盖 JavaFX 全部常用标准组件，并为每个组件独立提供 Tour 引导示例：
 * <ul>
 *   <li>Button / ToggleButton / Hyperlink</li>
 *   <li>TextField / PasswordField / TextArea</li>
 *   <li>CheckBox / RadioButton</li>
 *   <li>ComboBox / ChoiceBox / DatePicker / ColorPicker / Spinner</li>
 *   <li>Slider / ProgressBar / ProgressIndicator / ScrollBar</li>
 *   <li>ListView / TableView / TreeView / TreeTableView</li>
 *   <li>Canvas / ImageView / Separator / Pagination / TitledPane</li>
 * </ul>
 * 使用左侧导航 + 右侧详情布局，避免任何 ScrollPane 嵌套与坐标转换不确定性。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class TourStandardNodeDemo extends Application {

    /** 注册所有演示项的工厂表 */
    private final Map<String, Supplier<Pane>> registry = new LinkedHashMap<>();

    /** 中央展示容器 */
    private final StackPane displayArea = new StackPane();

    @Override
    public void start(Stage stage) {
        registerAllDemos();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F7FA;");

        // 顶部标题
        VBox header = new VBox(4);
        header.setPadding(new Insets(18, 24, 14, 24));
        header.setStyle("-fx-background-color: white;"
                + "-fx-border-color: #EBEEF5; -fx-border-width: 0 0 1 0;");
        Label title = new Label("Tour 标准 Node 组件演示");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        title.setTextFill(Color.valueOf("#303133"));
        Label sub = new Label("从左侧选择一个组件，点击右侧的 \"启动引导\" 按钮查看 Tour 演示");
        sub.setFont(Font.font("Microsoft YaHei", 12));
        sub.setTextFill(Color.valueOf("#909399"));
        header.getChildren().addAll(title, sub);
        root.setTop(header);

        // 左侧导航
        ListView<String> nav = new ListView<>();
        nav.setItems(FXCollections.observableArrayList(registry.keySet()));
        nav.setPrefWidth(200);
        nav.setStyle("-fx-background-color: white;"
                + "-fx-border-color: #EBEEF5; -fx-border-width: 0 1 0 0;");
        nav.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv != null) showDemo(nv);
        });
        root.setLeft(nav);

        // 中央展示
        displayArea.setPadding(new Insets(20));
        displayArea.setStyle("-fx-background-color: #F5F7FA;");
        root.setCenter(displayArea);

        // 默认选中第一项
        nav.getSelectionModel().selectFirst();

        StackPane sceneRoot = new StackPane(root);
        Scene scene = new Scene(sceneRoot, 1180, 760);
        stage.setTitle("Tour 标准 Node 组件演示");
        stage.setScene(scene);
        stage.show();
    }

    private void showDemo(String name) {
        Supplier<Pane> supplier = registry.get(name);
        if (supplier == null) return;
        displayArea.getChildren().setAll(supplier.get());
    }

    private void registerAllDemos() {
        registry.put("Button",            this::buildButtonDemo);
        registry.put("ToggleButton",      this::buildToggleButtonDemo);
        registry.put("Hyperlink",         this::buildHyperlinkDemo);
        registry.put("TextField",         this::buildTextFieldDemo);
        registry.put("PasswordField",     this::buildPasswordFieldDemo);
        registry.put("TextArea",          this::buildTextAreaDemo);
        registry.put("TextArea-Content",  this::buildTextAreaContentDemo);
        registry.put("CheckBox",          this::buildCheckBoxDemo);
        registry.put("RadioButton",       this::buildRadioButtonDemo);
        registry.put("ComboBox",          this::buildComboBoxDemo);
        registry.put("ChoiceBox",         this::buildChoiceBoxDemo);
        registry.put("DatePicker",        this::buildDatePickerDemo);
        registry.put("ColorPicker",       this::buildColorPickerDemo);
        registry.put("Spinner",           this::buildSpinnerDemo);
        registry.put("Slider",            this::buildSliderDemo);
        registry.put("ProgressBar",       this::buildProgressBarDemo);
        registry.put("ProgressIndicator", this::buildProgressIndicatorDemo);
        registry.put("ScrollBar",         this::buildScrollBarDemo);
        registry.put("ListView",          this::buildListViewDemo);
        registry.put("TableView",         this::buildTableViewDemo);
        registry.put("TreeView",          this::buildTreeViewDemo);
        registry.put("TreeTableView",     this::buildTreeTableViewDemo);
        registry.put("Canvas",            this::buildCanvasDemo);
        registry.put("ImageView",         this::buildImageViewDemo);
        registry.put("Separator",         this::buildSeparatorDemo);
        registry.put("Pagination",        this::buildPaginationDemo);
        registry.put("TitledPane",        this::buildTitledPaneDemo);
    }

    // ============================================================
    //  通用工具
    // ============================================================

    private Pane wrap(String title, String desc, Node target, Tour tour) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label tl = new Label(title);
        tl.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        tl.setTextFill(Color.valueOf("#303133"));

        Label dl = new Label(desc);
        dl.setFont(Font.font("Microsoft YaHei", 12));
        dl.setTextFill(Color.valueOf("#909399"));
        dl.setWrapText(true);

        Separator sep = new Separator();

        Button start = primaryButton("启动 " + title + " 引导");
        start.setOnAction(e -> tour.show(start.getScene()));

        HBox targetRow = new HBox(target);
        targetRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(tl, dl, sep, targetRow, start);
        return card;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white;"
                + "-fx-background-radius: 4; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }

    private Tour tourOf(Node target, String title, String desc, TourPlacement placement) {
        return TourFactory.builder()
                .step(target, title, desc, placement)
                .build();
    }

    // ============================================================
    //  各组件的 Tour 演示
    // ============================================================

    private Pane buildButtonDemo() {
        Button btn = new Button("点击我");
        btn.setStyle("-fx-background-color: #67C23A; -fx-text-fill: white;"
                + "-fx-background-radius: 4; -fx-padding: 8 18; -fx-cursor: hand;");
        return wrap("Button", "最常见的可点击按钮控件", btn,
                tourOf(btn, "Button", "这是一个 Button，点击触发动作。", TourPlacement.RIGHT));
    }

    private Pane buildToggleButtonDemo() {
        ToggleButton tb = new ToggleButton("开/关");
        return wrap("ToggleButton", "可切换选中状态的按钮", tb,
                tourOf(tb, "ToggleButton", "再次点击可切换选中态。", TourPlacement.RIGHT));
    }

    private Pane buildHyperlinkDemo() {
        Hyperlink link = new Hyperlink("点击访问");
        return wrap("Hyperlink", "外观类似超链接的按钮控件", link,
                tourOf(link, "Hyperlink", "类似网页超链接，点击触发动作。", TourPlacement.BOTTOM));
    }

    private Pane buildTextFieldDemo() {
        TextField tf = new TextField();
        tf.setPromptText("请输入文本");
        tf.setPrefWidth(220);
        return wrap("TextField", "单行文本输入框", tf,
                tourOf(tf, "TextField", "可输入单行文本，常用于表单。", TourPlacement.BOTTOM));
    }

    private Pane buildPasswordFieldDemo() {
        PasswordField pf = new PasswordField();
        pf.setPromptText("请输入密码");
        pf.setPrefWidth(220);
        return wrap("PasswordField", "密码输入框，自动遮蔽字符", pf,
                tourOf(pf, "PasswordField", "字符自动遮蔽，用于密码输入。", TourPlacement.BOTTOM));
    }

    private Pane buildTextAreaDemo() {
        TextArea ta = new TextArea();
        ta.setPromptText("多行文本…");
        ta.setPrefSize(360, 120);
        return wrap("TextArea", "多行文本输入框", ta,
                tourOf(ta, "TextArea", "可输入多行文本，支持换行与滚动。", TourPlacement.RIGHT));
    }

    /**
     * TextArea 文本内容专属引导演示。
     * <p>
     * 演示如何对 TextArea <em>内部文本</em>（而不是控件本身）做精准 Tour 引导：
     * 通过 {@link TourTarget#of(Rectangle2D)} 把目标设为一个场景坐标矩形，
     * 矩形位置由 TextArea 在场景中的 bounds 加上估算的字符行高/列宽推导出来，
     * 即可对标题行、列表段、署名行等不同片段分别弹出引导卡片。
     * </p>
     */
    private Pane buildTextAreaContentDemo() {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(false);
        ta.setFont(Font.font("Consolas", 14));
        ta.setPrefSize(440, 220);
        ta.setStyle("-fx-control-inner-background: #FAFAFA;");
        ta.setText(String.join("\n",
                "# Tour 引导组件",
                "",
                "功能要点：",
                "  • 12 种 Placement 定位",
                "  • 自定义遮罩 / 镂空 / 高亮描边",
                "  • 多步骤切换与回调 (onShow / onHide / onChange)",
                "  • 支持 Node / Rectangle2D / 空目标三种目标形态",
                "",
                "—— Aurora-FX Team"));

        // 使用估算行高 / 列宽推算文本片段在场景中的矩形
        // (TextArea 内部 padding 约 4~8 px，Consolas 14pt 行高 ≈ 18px，列宽 ≈ 8px)
        final double padTop = 6;
        final double padLeft = 6;
        final double lineH = 18;
        final double colW = 8;

        Button start = primaryButton("启动 TextArea 文本内容引导");
        start.setOnAction(e -> {
            // 在点击时获取最新场景坐标，避免 TextArea 尚未 layout
            Bounds sb = ta.localToScene(ta.getBoundsInLocal());
            double bx = sb.getMinX();
            double by = sb.getMinY();
            double bw = sb.getWidth();

            // 第 1 行：标题  "# Tour 引导组件"
            Rectangle2D titleLine = new Rectangle2D(
                    bx + padLeft, by + padTop,
                    Math.min(bw - 2 * padLeft, 18 * colW), lineH);

            // 第 4~7 行：功能要点列表
            Rectangle2D bulletBlock = new Rectangle2D(
                    bx + padLeft, by + padTop + 3 * lineH,
                    Math.min(bw - 2 * padLeft, 50 * colW), 4 * lineH);

            // 第 9 行：署名 "—— Aurora-FX Team"
            Rectangle2D signLine = new Rectangle2D(
                    bx + padLeft, by + padTop + 9 * lineH,
                    Math.min(bw - 2 * padLeft, 24 * colW), lineH);

            Tour tour = TourFactory.builder()
                    .mask(true)
                    .maskConfig(TourMaskConfig.builder()
                            .padding(2)
                            .cornerRadius(4)
                            .highlight(true)
                            .highlightColor(Color.web("#409EFF"))
                            .highlightWidth(2)
                            .opacity(0.45)
                            .build())
                    .step(new TourStep()
                            .target(ta)
                            .title("TextArea 控件")
                            .description("先看整体：这是一个多行文本框，承载下文将逐段引导的文本内容。")
                            .placement(TourPlacement.BOTTOM))
                    .step(new TourStep()
                            .target(titleLine)
                            .title("标题行")
                            .description("使用 Rectangle2D 目标精准定位到第 1 行的文档标题。")
                            .placement(TourPlacement.BOTTOM))
                    .step(new TourStep()
                            .target(bulletBlock)
                            .title("功能要点段")
                            .description("用一个跨多行的矩形覆盖一整段列表，引导可指向任意文本块。")
                            .placement(TourPlacement.BOTTOM))
                    .step(new TourStep()
                            .target(signLine)
                            .title("署名行")
                            .description("最后是文档署名行，演示如何指向文本末尾的固定行。")
                            .placement(TourPlacement.TOP))
                    .build();

            tour.show(start.getScene());
        });

        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label tl = new Label("TextArea 文本内容");
        tl.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        tl.setTextFill(Color.valueOf("#303133"));
        Label dl = new Label("演示如何针对 TextArea 内部文本片段（而非控件整体）进行 Tour 引导："
                + "通过 Rectangle2D 目标 + 估算行高，分别指向标题行、功能列表段、署名行等不同段落。");
        dl.setFont(Font.font("Microsoft YaHei", 12));
        dl.setTextFill(Color.valueOf("#909399"));
        dl.setWrapText(true);
        card.getChildren().addAll(tl, dl, new Separator(), ta, start);
        return card;
    }

    private Pane buildCheckBoxDemo() {
        CheckBox cb = new CheckBox("同意条款");
        return wrap("CheckBox", "多选框，可选中/取消", cb,
                tourOf(cb, "CheckBox", "勾选后表示同意；可独立切换。", TourPlacement.RIGHT));
    }

    private Pane buildRadioButtonDemo() {
        ToggleGroup g = new ToggleGroup();
        RadioButton r1 = new RadioButton("选项 A");
        RadioButton r2 = new RadioButton("选项 B");
        RadioButton r3 = new RadioButton("选项 C");
        r1.setToggleGroup(g);
        r2.setToggleGroup(g);
        r3.setToggleGroup(g);
        r1.setSelected(true);
        HBox row = new HBox(15, r1, r2, r3);
        row.setAlignment(Pos.CENTER_LEFT);
        return wrap("RadioButton", "单选按钮组，互斥选中", row,
                TourFactory.builder()
                        .step(r1, "选项 A", "默认选中。", TourPlacement.BOTTOM)
                        .step(r2, "选项 B", "互斥切换。", TourPlacement.BOTTOM)
                        .step(r3, "选项 C", "同一组内互斥。", TourPlacement.BOTTOM)
                        .build());
    }

    private Pane buildComboBoxDemo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("北京", "上海", "广州", "深圳");
        cb.setPromptText("选择城市");
        return wrap("ComboBox", "下拉选择框，支持搜索/编辑", cb,
                tourOf(cb, "ComboBox", "下拉选择项目，支持自定义编辑。", TourPlacement.BOTTOM));
    }

    private Pane buildChoiceBoxDemo() {
        ChoiceBox<String> cb = new ChoiceBox<>();
        cb.getItems().addAll("低", "中", "高");
        cb.getSelectionModel().selectFirst();
        return wrap("ChoiceBox", "简化版下拉框，UI 更轻", cb,
                tourOf(cb, "ChoiceBox", "比 ComboBox 简洁，仅支持选择。", TourPlacement.BOTTOM));
    }

    private Pane buildDatePickerDemo() {
        DatePicker dp = new DatePicker(LocalDate.now());
        return wrap("DatePicker", "日期选择器", dp,
                tourOf(dp, "DatePicker", "点击图标弹出日期面板。", TourPlacement.BOTTOM));
    }

    private Pane buildColorPickerDemo() {
        ColorPicker cp = new ColorPicker(Color.valueOf("#409EFF"));
        return wrap("ColorPicker", "颜色选择器", cp,
                tourOf(cp, "ColorPicker", "支持自定义颜色与历史选择。", TourPlacement.BOTTOM));
    }

    private Pane buildSpinnerDemo() {
        Spinner<Integer> sp = new Spinner<>(0, 100, 10);
        sp.setEditable(true);
        sp.setPrefWidth(140);
        return wrap("Spinner", "数值微调控件", sp,
                tourOf(sp, "Spinner", "通过箭头或键盘微调数值。", TourPlacement.RIGHT));
    }

    private Pane buildSliderDemo() {
        Slider s = new Slider(0, 100, 30);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.setPrefWidth(320);
        return wrap("Slider", "拖动滑块输入数值", s,
                tourOf(s, "Slider", "支持显示刻度，可键盘微调。", TourPlacement.TOP));
    }

    private Pane buildProgressBarDemo() {
        ProgressBar pb = new ProgressBar(0.65);
        pb.setPrefWidth(320);
        return wrap("ProgressBar", "进度条", pb,
                tourOf(pb, "ProgressBar", "展示任务进度，支持不确定模式。", TourPlacement.TOP));
    }

    private Pane buildProgressIndicatorDemo() {
        ProgressIndicator pi = new ProgressIndicator(0.45);
        pi.setPrefSize(60, 60);
        return wrap("ProgressIndicator", "圆形进度指示器", pi,
                tourOf(pi, "ProgressIndicator", "圆形进度，常用于加载提示。", TourPlacement.RIGHT));
    }

    private Pane buildScrollBarDemo() {
        ScrollBar sb = new ScrollBar();
        sb.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        sb.setPrefWidth(300);
        return wrap("ScrollBar", "独立滚动条控件", sb,
                tourOf(sb, "ScrollBar", "独立的可滚动条控件。", TourPlacement.TOP));
    }

    private Pane buildListViewDemo() {
        ListView<String> lv = new ListView<>(FXCollections.observableArrayList(
                "Java", "Kotlin", "Scala", "Groovy", "Clojure"));
        lv.setPrefSize(260, 180);
        return wrap("ListView", "可滚动列表视图", lv,
                tourOf(lv, "ListView", "展示列表数据，支持单/多选。", TourPlacement.RIGHT));
    }

    private Pane buildTableViewDemo() {
        TableView<Person> tv = new TableView<>();
        TableColumn<Person, String> c1 = new TableColumn<>("姓名");
        c1.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<Person, String> c2 = new TableColumn<>("城市");
        c2.setCellValueFactory(d -> d.getValue().cityProperty());
        tv.getColumns().add(c1);
        tv.getColumns().add(c2);
        tv.getItems().addAll(
                new Person("张三", "北京"),
                new Person("李四", "上海"),
                new Person("王五", "广州"));
        tv.setPrefSize(360, 180);
        return wrap("TableView", "表格视图，支持列排序与选择", tv,
                tourOf(tv, "TableView", "结构化数据展示，可排序、过滤、自定义单元格。",
                        TourPlacement.RIGHT));
    }

    private Pane buildTreeViewDemo() {
        TreeItem<String> root = new TreeItem<>("项目");
        root.setExpanded(true);
        TreeItem<String> src = new TreeItem<>("src");
        src.getChildren().addAll(new TreeItem<>("main"), new TreeItem<>("test"));
        TreeItem<String> doc = new TreeItem<>("doc");
        root.getChildren().addAll(src, doc);
        TreeView<String> tv = new TreeView<>(root);
        tv.setPrefSize(260, 200);
        return wrap("TreeView", "树形视图，展示层级结构", tv,
                tourOf(tv, "TreeView", "展示层级数据，可展开/折叠子节点。",
                        TourPlacement.RIGHT));
    }

    private Pane buildTreeTableViewDemo() {
        TreeTableView<Person> ttv = new TreeTableView<>();
        TreeTableColumn<Person, String> c1 = new TreeTableColumn<>("姓名");
        c1.setCellValueFactory(d -> d.getValue().getValue().nameProperty());
        TreeTableColumn<Person, String> c2 = new TreeTableColumn<>("城市");
        c2.setCellValueFactory(d -> d.getValue().getValue().cityProperty());
        ttv.getColumns().add(c1);
        ttv.getColumns().add(c2);
        TreeItem<Person> root = new TreeItem<>(new Person("员工", "公司"));
        root.setExpanded(true);
        root.getChildren().addAll(
                new TreeItem<>(new Person("张三", "北京")),
                new TreeItem<>(new Person("李四", "上海")));
        ttv.setRoot(root);
        ttv.setPrefSize(360, 200);
        return wrap("TreeTableView", "结合树与表格的视图", ttv,
                tourOf(ttv, "TreeTableView", "层级 + 多列展示，支持展开。", TourPlacement.RIGHT));
    }

    private Pane buildCanvasDemo() {
        Canvas canvas = new Canvas(280, 180);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.valueOf("#67C23A"));
        g.fillRoundRect(20, 20, 240, 140, 16, 16);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        g.fillText("Canvas 自定义绘制", 60, 100);
        return wrap("Canvas", "通过 GraphicsContext 自定义绘制", canvas,
                tourOf(canvas, "Canvas", "可用代码自由绘制矢量/位图内容。",
                        TourPlacement.RIGHT));
    }

    private Pane buildImageViewDemo() {
        ImageView iv = new ImageView();
        iv.setFitWidth(120);
        iv.setFitHeight(120);
        // 使用占位矩形效果，避免引入图片资源依赖
        Region placeholder = new Region();
        placeholder.setPrefSize(120, 120);
        placeholder.setStyle("-fx-background-color: linear-gradient(to bottom right, #667EEA, #764BA2);"
                + "-fx-background-radius: 8;");
        StackPane container = new StackPane(placeholder);
        Label tip = new Label("Image");
        tip.setTextFill(Color.WHITE);
        tip.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        container.getChildren().add(tip);
        return wrap("ImageView", "图像显示控件（此处使用占位 Region 演示）", container,
                tourOf(container, "ImageView", "可加载本地或网络图像并自适应缩放。",
                        TourPlacement.RIGHT));
    }

    private Pane buildSeparatorDemo() {
        Separator sep = new Separator();
        sep.setPrefWidth(320);
        return wrap("Separator", "分隔线，区隔内容区域", sep,
                tourOf(sep, "Separator", "用于在视觉上区隔不同区域。", TourPlacement.TOP));
    }

    private Pane buildPaginationDemo() {
        Pagination pg = new Pagination(8, 0);
        pg.setPrefSize(360, 80);
        pg.setPageFactory(i -> {
            Label l = new Label("第 " + (i + 1) + " 页内容");
            l.setFont(Font.font("Microsoft YaHei", 12));
            return new StackPane(l);
        });
        return wrap("Pagination", "分页控件", pg,
                tourOf(pg, "Pagination", "用于分页显示大量内容。", TourPlacement.TOP));
    }

    private Pane buildTitledPaneDemo() {
        TitledPane tp = new TitledPane("标题面板", new Label("折叠面板内容区"));
        tp.setPrefWidth(280);
        return wrap("TitledPane", "可折叠的标题面板", tp,
                tourOf(tp, "TitledPane", "点击标题展开 / 折叠内容。", TourPlacement.RIGHT));
    }

    // ============================================================
    //  内部类型
    // ============================================================

    /** TableView/TreeTableView 演示用数据模型 */
    public static class Person {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty city = new SimpleStringProperty();

        public Person(String n, String c) {
            name.set(n);
            city.set(c);
        }

        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty cityProperty() { return city; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
