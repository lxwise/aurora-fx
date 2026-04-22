package io.aurora.fx.theme;

import atlantafx.base.theme.Theme;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * 操作系统主题演示应用
 * <p>
 * 提供完整的可视化测试界面，展示所有主题在 JavaFX 标准组件上的效果。
 * 包含主题切换功能和所有主要控件的展示。
 * </p>
 *
 * <h3>启动方式</h3>
 * <pre>{@code
 * OSThemeDemo.main(args);
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class OSThemeDemo extends Application {

    private static final String[] THEME_NAMES = {
            "Windows 11 Light", "Windows 11 Dark",
            "macOS Light", "macOS Dark", "macOS System Dark"
    };

    private VBox root;
    private ComboBox<String> themeCombo;
    private Label themeNameLabel;
    private Label themeModeLabel;

    @Override
    public void start(Stage stage) {
        // 默认使用 Windows 11 Light 主题
        Application.setUserAgentStylesheet(new Windows11Light().getUserAgentStylesheet());

        root = new VBox(12);
        root.setPadding(new Insets(16));

        // 主题切换区域
        root.getChildren().add(createThemeSwitcher());

        // 选项卡面板展示所有组件
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                createBasicControlsTab(),
                createInputControlsTab(),
                createContainerControlsTab(),
                createTableTreeTab(),
                createMenuControlsTab()
        );

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        root.getChildren().add(tabPane);

        Scene scene = new Scene(root, 900, 750);
        stage.setScene(scene);
        stage.setTitle("Aurora-FX OS Theme Demo");
        stage.show();
    }

    // ==================== 主题切换 ====================

    private HBox createThemeSwitcher() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 8;");

        Label label = new Label("选择主题:");
        label.setStyle("-fx-font-weight: bold;");

        themeCombo = new ComboBox<>(FXCollections.observableArrayList(THEME_NAMES));
        themeCombo.setValue(THEME_NAMES[0]);
        themeCombo.setOnAction(e -> switchTheme(themeCombo.getValue()));

        themeNameLabel = new Label();
        themeNameLabel.setStyle("-fx-text-fill: -color-accent-fg; -fx-font-weight: bold;");
        themeModeLabel = new Label();
        themeModeLabel.setStyle("-fx-text-fill: -color-fg-muted;");

        updateThemeInfo(new Windows11Light());

        box.getChildren().addAll(label, themeCombo, new Separator(), themeNameLabel, themeModeLabel);
        return box;
    }

    private void switchTheme(String themeName) {
        if (themeName == null) return;

        Theme theme = OSThemeFactory.forName(themeName);
        if (theme != null) {
            Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
            updateThemeInfo(theme);
        }
    }

    private void updateThemeInfo(Theme theme) {
        themeNameLabel.setText(theme.getName());
        themeModeLabel.setText(theme.isDarkMode() ? "(深色模式)" : "(浅色模式)");
    }

    // ==================== 基础控件 Tab ====================

    private Tab createBasicControlsTab() {
        Tab tab = new Tab("基础控件");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(12));

        VBox content = new VBox(16);

        // Button
        content.getChildren().add(createSection("Button 按钮", createButtonDemo()));

        // ToggleButton
        content.getChildren().add(createSection("ToggleButton 切换按钮", createToggleButtonDemo()));

        // CheckBox
        content.getChildren().add(createSection("CheckBox 复选框", createCheckBoxDemo()));

        // RadioButton
        content.getChildren().add(createSection("RadioButton 单选按钮", createRadioButtonDemo()));

        // Hyperlink
        content.getChildren().add(createSection("Hyperlink 超链接", createHyperlinkDemo()));

        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    private HBox createButtonDemo() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(
                new Button("普通按钮"),
                new Button("悬停我"),
                createDefaultButton("主按钮"),
                new Button("禁用按钮")
        );
        box.getChildren().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .skip(3)
                .forEach(b -> b.setDisable(true));
        return box;
    }

    private Button createDefaultButton(String text) {
        Button btn = new Button(text);
        btn.setDefaultButton(true);
        return btn;
    }

    private HBox createToggleButtonDemo() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup group = new ToggleGroup();
        for (String text : new String[]{"选项 A", "选项 B", "选项 C"}) {
            ToggleButton tb = new ToggleButton(text);
            tb.setToggleGroup(group);
            box.getChildren().add(tb);
        }
        return box;
    }

    private HBox createCheckBoxDemo() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        CheckBox cb1 = new CheckBox("未选中");
        CheckBox cb2 = new CheckBox("已选中");
        cb2.setSelected(true);
        CheckBox cb3 = new CheckBox("不确定");
        cb3.setIndeterminate(true);
        CheckBox cb4 = new CheckBox("禁用");
        cb4.setDisable(true);
        box.getChildren().addAll(cb1, cb2, cb3, cb4);
        return box;
    }

    private HBox createRadioButtonDemo() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton("选项 1");
        rb1.setToggleGroup(group);
        rb1.setSelected(true);
        RadioButton rb2 = new RadioButton("选项 2");
        rb2.setToggleGroup(group);
        RadioButton rb3 = new RadioButton("禁用");
        rb3.setDisable(true);
        box.getChildren().addAll(rb1, rb2, rb3);
        return box;
    }

    private HBox createHyperlinkDemo() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(
                new Hyperlink("普通链接"),
                new Hyperlink("访问过的链接"),
                new Hyperlink("禁用链接")
        );
        box.getChildren().stream()
                .filter(n -> n instanceof Hyperlink)
                .map(n -> (Hyperlink) n)
                .skip(2)
                .forEach(h -> h.setDisable(true));
        return box;
    }

    // ==================== 输入控件 Tab ====================

    private Tab createInputControlsTab() {
        Tab tab = new Tab("输入控件");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(12));

        VBox content = new VBox(16);

        // TextField / PasswordField
        content.getChildren().add(createSection("TextField / PasswordField", createTextFieldDemo()));

        // TextArea
        content.getChildren().add(createSection("TextArea 文本域", createTextAreaDemo()));

        // ComboBox / ChoiceBox
        content.getChildren().add(createSection("ComboBox / ChoiceBox", createComboDemo()));

        // Spinner
        content.getChildren().add(createSection("Spinner 数值选择器", createSpinnerDemo()));

        // Slider
        content.getChildren().add(createSection("Slider 滑块", createSliderDemo()));

        // ProgressBar
        content.getChildren().add(createSection("ProgressBar 进度条", createProgressBarDemo()));

        // DatePicker
        content.getChildren().add(createSection("DatePicker 日期选择器", createDatePickerDemo()));

        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    private VBox createTextFieldDemo() {
        VBox box = new VBox(8);
        TextField tf1 = new TextField();
        tf1.setPromptText("请输入文本...");
        TextField tf2 = new TextField("已有内容");
        PasswordField pf = new PasswordField();
        pf.setPromptText("请输入密码...");
        TextField tf3 = new TextField("禁用状态");
        tf3.setDisable(true);
        box.getChildren().addAll(tf1, tf2, pf, tf3);
        return box;
    }

    private VBox createTextAreaDemo() {
        VBox box = new VBox(8);
        TextArea ta = new TextArea();
        ta.setPromptText("在此输入多行文本...");
        ta.setPrefRowCount(4);
        box.getChildren().add(ta);
        return box;
    }

    private HBox createComboDemo() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> comboBox = new ComboBox<>(
                FXCollections.observableArrayList("选项 1", "选项 2", "选项 3", "选项 4"));
        comboBox.setPromptText("ComboBox");
        comboBox.setEditable(true);
        ChoiceBox<String> choiceBox = new ChoiceBox<>(
                FXCollections.observableArrayList("选项 A", "选项 B", "选项 C"));
        choiceBox.setValue("选项 A");
        box.getChildren().addAll(comboBox, choiceBox);
        return box;
    }

    private HBox createSpinnerDemo() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        Spinner<Integer> spinner = new Spinner<>(0, 100, 50);
        spinner.setEditable(true);
        Spinner<Double> spinner2 = new Spinner<>(0.0, 1.0, 0.5, 0.1);
        box.getChildren().addAll(spinner, spinner2);
        return box;
    }

    private VBox createSliderDemo() {
        VBox box = new VBox(8);
        Slider slider1 = new Slider(0, 100, 50);
        slider1.setShowTickLabels(true);
        slider1.setShowTickMarks(true);
        Slider slider2 = new Slider(0, 100, 75);
        slider2.setShowTickLabels(true);
        box.getChildren().addAll(new Label("带刻度标记:"), slider1, new Label("简单滑块:"), slider2);
        return box;
    }

    private VBox createProgressBarDemo() {
        VBox box = new VBox(8);
        ProgressBar pb1 = new ProgressBar(0.6);
        ProgressBar pb2 = new ProgressBar();
        pb2.setProgress(-1); // 不确定模式
        box.getChildren().addAll(
                new Label("60% 完成:"), pb1,
                new Label("不确定模式:"), pb2
        );
        return box;
    }

    private HBox createDatePickerDemo() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(new DatePicker());
        return box;
    }

    // ==================== 容器控件 Tab ====================

    private Tab createContainerControlsTab() {
        Tab tab = new Tab("容器控件");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(12));

        VBox content = new VBox(16);

        // TitledPane / Accordion
        content.getChildren().add(createSection("TitledPane / Accordion", createAccordionDemo()));

        // TabPane (嵌套)
        content.getChildren().add(createSection("TabPane (嵌套)", createNestedTabPane()));

        // SplitPane
        content.getChildren().add(createSection("SplitPane 分割面板", createSplitPaneDemo()));

        // ScrollPane
        content.getChildren().add(createSection("ScrollPane 滚动面板", createScrollPaneDemo()));

        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    private Accordion createAccordionDemo() {
        Accordion accordion = new Accordion();
        for (int i = 1; i <= 4; i++) {
            TitledPane tp = new TitledPane("面板 " + i, new Label("这是面板 " + i + " 的内容"));
            accordion.getPanes().add(tp);
        }
        accordion.setExpandedPane(accordion.getPanes().get(0));
        return accordion;
    }

    private TabPane createNestedTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("标签 1", new Label("标签 1 内容")),
                new Tab("标签 2", new Label("标签 2 内容")),
                new Tab("标签 3", new Label("标签 3 内容"))
        );
        return tabPane;
    }

    private SplitPane createSplitPaneDemo() {
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(
                createStyledBox("左侧面板"),
                createStyledBox("中间面板"),
                createStyledBox("右侧面板")
        );
        splitPane.setDividerPositions(0.33, 0.66);
        return splitPane;
    }

    private ScrollPane createScrollPaneDemo() {
        VBox longContent = new VBox(8);
        for (int i = 1; i <= 20; i++) {
            longContent.getChildren().add(new Label("滚动内容行 " + i));
        }
        ScrollPane sp = new ScrollPane(longContent);
        sp.setPrefHeight(120);
        sp.setFitToWidth(true);
        return sp;
    }

    // ==================== 表格/树 Tab ====================

    private Tab createTableTreeTab() {
        Tab tab = new Tab("表格/树");
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        // TableView
        TableView<DemoItem> table = new TableView<>();
        table.setItems(createDemoData());
        TableColumn<DemoItem, String> col1 = new TableColumn<>("名称");
        col1.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<DemoItem, String> col2 = new TableColumn<>("类型");
        col2.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<DemoItem, String> col3 = new TableColumn<>("状态");
        col3.setCellValueFactory(new PropertyValueFactory<>("status"));
        table.getColumns().addAll(col1, col2, col3);
        table.setPrefWidth(400);

        // TreeView
        TreeView<String> tree = new TreeView<>();
        TreeItem<String> treeRoot = new TreeItem<>("根节点");
        for (int i = 1; i <= 3; i++) {
            TreeItem<String> parent = new TreeItem<>("目录 " + i);
            for (int j = 1; j <= 3; j++) {
                parent.getChildren().add(new TreeItem<>("项目 " + i + "." + j));
            }
            treeRoot.getChildren().add(parent);
        }
        treeRoot.setExpanded(true);
        tree.setRoot(treeRoot);

        splitPane.getItems().addAll(table, tree);
        tab.setContent(splitPane);
        return tab;
    }

    // ==================== 菜单控件 Tab ====================

    private Tab createMenuControlsTab() {
        Tab tab = new Tab("菜单控件");
        VBox content = new VBox(16);
        content.setPadding(new Insets(12));

        // MenuBar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("文件");
        fileMenu.getItems().addAll(
                new MenuItem("新建"), new MenuItem("打开"),
                new SeparatorMenuItem(),
                new MenuItem("保存"), new MenuItem("另存为"),
                new SeparatorMenuItem(),
                new MenuItem("退出")
        );
        Menu editMenu = new Menu("编辑");
        editMenu.getItems().addAll(
                new MenuItem("撤销"), new MenuItem("重做"),
                new SeparatorMenuItem(),
                new MenuItem("剪切"), new MenuItem("复制"), new MenuItem("粘贴")
        );
        Menu helpMenu = new Menu("帮助");
        helpMenu.getItems().addAll(new MenuItem("关于"));
        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);

        content.getChildren().add(createSection("MenuBar 菜单栏", menuBar));

        // ContextMenu
        Label contextLabel = new Label("右键点击此文字查看上下文菜单");
        contextLabel.setPadding(new Insets(12));
        contextLabel.setStyle("-fx-border-color: -color-border-default; -fx-border-radius: 6; -fx-background-color: -color-bg-subtle;");
        ContextMenu ctxMenu = new ContextMenu(
                new MenuItem("操作 1"), new MenuItem("操作 2"),
                new SeparatorMenuItem(),
                new MenuItem("操作 3")
        );
        contextLabel.setContextMenu(ctxMenu);

        content.getChildren().add(createSection("ContextMenu 上下文菜单", contextLabel));

        // Separator
        content.getChildren().add(createSection("Separator 分隔线", new Separator()));

        tab.setContent(content);
        return tab;
    }

    // ==================== 辅助方法 ====================

    private TitledPane createSection(String title, Node content) {
        TitledPane pane = new TitledPane();
        pane.setText(title);
        pane.setContent(content instanceof Parent ? (Parent) content : new Label(Objects.toString(content)));
        pane.setExpanded(true);
        return pane;
    }

    private VBox createStyledBox(String text) {
        VBox box = new VBox(new Label(text));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12));
        return box;
    }

    private ObservableList<DemoItem> createDemoData() {
        return FXCollections.observableArrayList(
                new DemoItem("项目 A", "类型 1", "活跃"),
                new DemoItem("项目 B", "类型 2", "完成"),
                new DemoItem("项目 C", "类型 1", "暂停"),
                new DemoItem("项目 D", "类型 3", "活跃"),
                new DemoItem("项目 E", "类型 2", "完成"),
                new DemoItem("项目 F", "类型 1", "活跃")
        );
    }

    // ==================== 数据模型 ====================

    /**
     * 演示数据项
     */
    public static class DemoItem {
        private final String name;
        private final String type;
        private final String status;

        public DemoItem(String name, String type, String status) {
            this.name = name;
            this.type = type;
            this.status = status;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getStatus() { return status; }
    }

    // ==================== 入口 ====================

    public static void main(String[] args) {
        launch(args);
    }
}
