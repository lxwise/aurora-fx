package io.aurora.fx.theme;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * macOS San Francisco 主题完整组件演示
 * <p>
 * 展示所有 JavaFX 标准组件在 macOS Light 主题下的外观效果。
 * 组件按类别分组到 TabPane 中，涵盖基础控件、输入控件、容器控件、
 * 对话框组件、菜单组件和其他组件。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class MacOSThemeDemo extends Application {

    @Override
    public void start(Stage stage) {
        // 设置 macOS Light 主题
        Application.setUserAgentStylesheet(new MacOSLight().getUserAgentStylesheet());

        BorderPane root = new BorderPane();

        // 顶部：标题栏 + MenuBar
        VBox topArea = new VBox();
        topArea.getChildren().addAll(createMenuBar(), createHeader());
        root.setTop(topArea);

        // 中部：TabPane 展示所有组件
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setSide(Side.TOP);
        tabPane.getTabs().addAll(
                createBasicControlsTab(),
                createInputControlsTab(),
                createContainerControlsTab(),
                createTableTreeTab(),
                createDialogTab(),
                createOtherControlsTab()
        );
        root.setCenter(tabPane);

        // 底部状态栏
        root.setBottom(createStatusBar());

        Scene scene = new Scene(root, 1050, 780);
        stage.setScene(scene);
        stage.setTitle("macOS Theme Demo — San Francisco");
        stage.show();
    }

    // ==================== 顶部区域 ====================

    /** 创建 MenuBar 菜单栏 */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // 应用菜单
        Menu appMenu = new Menu("Aurora-FX");
        appMenu.getItems().addAll(
                new MenuItem("关于 Aurora-FX"),
                new SeparatorMenuItem(),
                new MenuItem("偏好设置..."),
                new SeparatorMenuItem(),
                new MenuItem("退出 Aurora-FX")
        );

        // 文件菜单
        Menu fileMenu = new Menu("文件");
        fileMenu.getItems().addAll(
                new MenuItem("新建"),
                new MenuItem("打开..."),
                new MenuItem("最近打开"),
                new SeparatorMenuItem(),
                new MenuItem("保存"),
                new MenuItem("另存为..."),
                new SeparatorMenuItem(),
                new MenuItem("关闭窗口")
        );

        // 编辑菜单
        Menu editMenu = new Menu("编辑");
        editMenu.getItems().addAll(
                new MenuItem("撤销"),
                new MenuItem("重做"),
                new SeparatorMenuItem(),
                new MenuItem("剪切"),
                new MenuItem("复制"),
                new MenuItem("粘贴"),
                new SeparatorMenuItem(),
                new MenuItem("全选")
        );

        // 显示菜单 - 含子菜单
        Menu viewMenu = new Menu("显示");
        CheckMenuItem showToolbar = new CheckMenuItem("显示工具栏");
        showToolbar.setSelected(true);
        CheckMenuItem showSidebar = new CheckMenuItem("显示边栏");
        showSidebar.setSelected(true);
        Menu appearanceMenu = new Menu("外观");
        ToggleGroup appGroup = new ToggleGroup();
        RadioMenuItem lightMode = new RadioMenuItem("浅色");
        lightMode.setToggleGroup(appGroup);
        lightMode.setSelected(true);
        RadioMenuItem darkMode = new RadioMenuItem("深色");
        darkMode.setToggleGroup(appGroup);
        RadioMenuItem autoMode = new RadioMenuItem("自动");
        autoMode.setToggleGroup(appGroup);
        appearanceMenu.getItems().addAll(lightMode, darkMode, autoMode);
        viewMenu.getItems().addAll(showToolbar, showSidebar, new SeparatorMenuItem(), appearanceMenu);

        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        helpMenu.getItems().addAll(
                new MenuItem("Aurora-FX 帮助"),
                new SeparatorMenuItem(),
                new MenuItem("报告问题...")
        );

        menuBar.getMenus().addAll(appMenu, fileMenu, editMenu, viewMenu, helpMenu);
        return menuBar;
    }

    /** 创建标题头 */
    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: -color-bg-subtle;");

        Label title = new Label("macOS San Francisco — 全组件演示");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label("MacOSLight");
        badge.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: -color-fg-emphasis; "
                + "-fx-padding: 4 12; -fx-background-radius: 12;");

        header.getChildren().addAll(title, spacer, badge);
        return header;
    }

    /** 创建状态栏 */
    private HBox createStatusBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-muted; -fx-border-width: 1 0 0 0;");
        bar.getChildren().addAll(
                new Label("就绪"),
                new Separator(Orientation.VERTICAL),
                new Label("主题: macOS Light"),
                new Separator(Orientation.VERTICAL),
                new Label("Accent: #007AFF")
        );
        return bar;
    }

    // ==================== Tab 1: 基础控件 ====================

    private Tab createBasicControlsTab() {
        Tab tab = new Tab("基础控件");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(16));

        // --- Button ---
        VBox buttonSection = createSection("Button 按钮");
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);
        Button normalBtn = new Button("普通按钮");
        Button defaultBtn = new Button("主按钮 (Default)");
        defaultBtn.setDefaultButton(true);
        Button disabledBtn = new Button("禁用按钮");
        disabledBtn.setDisable(true);
        Button dangerBtn = new Button("危险操作");
        dangerBtn.getStyleClass().add("danger");
        Button successBtn = new Button("成功操作");
        successBtn.getStyleClass().add("success");
        Button flatBtn = new Button("扁平按钮");
        flatBtn.getStyleClass().add("flat");
        buttons.getChildren().addAll(normalBtn, defaultBtn, disabledBtn, dangerBtn, successBtn, flatBtn);
        buttonSection.getChildren().add(buttons);

        // --- Label ---
        VBox labelSection = createSection("Label 标签");
        HBox labels = new HBox(12);
        labels.setAlignment(Pos.CENTER_LEFT);
        labels.getChildren().addAll(
                new Label("默认标签"),
                createStyledLabel("强调文本", "-fx-font-weight: bold; -fx-font-size: 16px;"),
                createStyledLabel("次要文本", "-fx-text-fill: -color-fg-muted;"),
                createStyledLabel("辅助文本", "-fx-text-fill: -color-fg-subtle;")
        );
        labelSection.getChildren().add(labels);

        // --- TextField / PasswordField ---
        VBox textFieldSection = createSection("TextField / PasswordField 文本输入");
        GridPane textGrid = new GridPane();
        textGrid.setHgap(12);
        textGrid.setVgap(8);
        TextField tf1 = new TextField();
        tf1.setPromptText("请输入文本...");
        TextField tf2 = new TextField("已有内容的输入框");
        TextField tf3 = new TextField("禁用状态");
        tf3.setDisable(true);
        PasswordField pf = new PasswordField();
        pf.setPromptText("请输入密码...");
        textGrid.addRow(0, new Label("普通输入:"), tf1);
        textGrid.addRow(1, new Label("有内容:"), tf2);
        textGrid.addRow(2, new Label("禁用:"), tf3);
        textGrid.addRow(3, new Label("密码:"), pf);
        textFieldSection.getChildren().add(textGrid);

        // --- TextArea ---
        VBox textAreaSection = createSection("TextArea 文本域");
        TextArea ta = new TextArea();
        ta.setPromptText("在此输入多行文本...\n支持换行和滚动");
        ta.setPrefRowCount(4);
        ta.setPrefColumnCount(40);
        textAreaSection.getChildren().add(ta);

        // --- CheckBox ---
        VBox checkBoxSection = createSection("CheckBox 复选框");
        HBox checks = new HBox(16);
        checks.setAlignment(Pos.CENTER_LEFT);
        CheckBox cb1 = new CheckBox("未选中");
        CheckBox cb2 = new CheckBox("已选中");
        cb2.setSelected(true);
        CheckBox cb3 = new CheckBox("混合");
        cb3.setIndeterminate(true);
        cb3.setAllowIndeterminate(true);
        CheckBox cb4 = new CheckBox("禁用");
        cb4.setDisable(true);
        CheckBox cb5 = new CheckBox("禁用已选");
        cb5.setSelected(true);
        cb5.setDisable(true);
        checks.getChildren().addAll(cb1, cb2, cb3, cb4, cb5);
        checkBoxSection.getChildren().add(checks);

        // --- RadioButton ---
        VBox radioSection = createSection("RadioButton 单选按钮");
        HBox radios = new HBox(16);
        radios.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup rGroup = new ToggleGroup();
        RadioButton rb1 = new RadioButton("选项 A");
        rb1.setToggleGroup(rGroup);
        rb1.setSelected(true);
        RadioButton rb2 = new RadioButton("选项 B");
        rb2.setToggleGroup(rGroup);
        RadioButton rb3 = new RadioButton("选项 C");
        rb3.setToggleGroup(rGroup);
        RadioButton rb4 = new RadioButton("禁用");
        rb4.setDisable(true);
        radios.getChildren().addAll(rb1, rb2, rb3, rb4);
        radioSection.getChildren().add(radios);

        // --- ToggleButton ---
        VBox toggleSection = createSection("ToggleButton 切换按钮");
        HBox toggles = new HBox(8);
        toggles.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup tGroup = new ToggleGroup();
        for (String text : new String[]{"左", "中", "右"}) {
            ToggleButton tb = new ToggleButton(text);
            tb.setToggleGroup(tGroup);
            toggles.getChildren().add(tb);
        }
        toggleSection.getChildren().add(toggles);

        // --- Hyperlink ---
        VBox linkSection = createSection("Hyperlink 超链接");
        HBox links = new HBox(16);
        links.setAlignment(Pos.CENTER_LEFT);
        Hyperlink link1 = new Hyperlink("普通链接");
        Hyperlink link2 = new Hyperlink("禁用链接");
        link2.setDisable(true);
        links.getChildren().addAll(link1, link2);
        linkSection.getChildren().add(links);

        content.getChildren().addAll(
                buttonSection, labelSection, textFieldSection, textAreaSection,
                checkBoxSection, radioSection, toggleSection, linkSection
        );
        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    // ==================== Tab 2: 输入控件 ====================

    private Tab createInputControlsTab() {
        Tab tab = new Tab("输入控件");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(16));

        // --- ComboBox ---
        VBox comboSection = createSection("ComboBox 下拉框");
        HBox combos = new HBox(16);
        combos.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> combo1 = new ComboBox<>(FXCollections.observableArrayList(
                "选项一", "选项二", "选项三", "选项四"));
        combo1.setPromptText("请选择...");
        ComboBox<String> combo2 = new ComboBox<>(FXCollections.observableArrayList(
                "可编辑 A", "可编辑 B", "可编辑 C"));
        combo2.setEditable(true);
        combo2.setPromptText("可编辑");
        ComboBox<String> combo3 = new ComboBox<>(FXCollections.observableArrayList("禁用项"));
        combo3.setValue("禁用项");
        combo3.setDisable(true);
        combos.getChildren().addAll(new Label("普通:"), combo1, new Label("可编辑:"), combo2, new Label("禁用:"), combo3);
        comboSection.getChildren().add(combos);

        // --- ChoiceBox ---
        VBox choiceSection = createSection("ChoiceBox 选择框");
        ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(
                "旧金山", "洛杉矶", "纽约", "西雅图", "东京"));
        choiceBox.setValue("旧金山");
        choiceSection.getChildren().add(choiceBox);

        // --- Spinner ---
        VBox spinnerSection = createSection("Spinner 数值选择器");
        HBox spinners = new HBox(16);
        spinners.setAlignment(Pos.CENTER_LEFT);
        Spinner<Integer> intSpinner = new Spinner<>(0, 100, 50);
        intSpinner.setEditable(true);
        intSpinner.setPrefWidth(120);
        Spinner<Double> dblSpinner = new Spinner<>(0.0, 1.0, 0.5, 0.1);
        dblSpinner.setPrefWidth(120);
        spinners.getChildren().addAll(new Label("整数:"), intSpinner, new Label("小数:"), dblSpinner);
        spinnerSection.getChildren().add(spinners);

        // --- Slider ---
        VBox sliderSection = createSection("Slider 滑块");
        VBox sliders = new VBox(10);
        Slider s1 = new Slider(0, 100, 40);
        s1.setShowTickLabels(true);
        s1.setShowTickMarks(true);
        s1.setMajorTickUnit(25);
        Slider s2 = new Slider(0, 100, 70);
        s2.setShowTickLabels(true);
        Slider s3 = new Slider(0, 100, 30);
        s3.setDisable(true);
        sliders.getChildren().addAll(new Label("带刻度:"), s1, new Label("简单:"), s2, new Label("禁用:"), s3);
        sliderSection.getChildren().add(sliders);

        // --- ProgressBar / ProgressIndicator ---
        VBox progressSection = createSection("ProgressBar / ProgressIndicator 进度");
        VBox progresses = new VBox(10);
        ProgressBar pb1 = new ProgressBar(0.0);
        ProgressBar pb2 = new ProgressBar(0.35);
        ProgressBar pb3 = new ProgressBar(0.7);
        ProgressBar pb4 = new ProgressBar(1.0);
        ProgressBar pb5 = new ProgressBar(-1);
        HBox indicators = new HBox(16);
        indicators.setAlignment(Pos.CENTER_LEFT);
        ProgressIndicator pi1 = new ProgressIndicator(0.6);
        pi1.setPrefSize(48, 48);
        ProgressIndicator pi2 = new ProgressIndicator(-1);
        pi2.setPrefSize(48, 48);
        indicators.getChildren().addAll(new Label("60%:"), pi1, new Label("不确定:"), pi2);
        progresses.getChildren().addAll(
                new Label("0%:"), pb1,
                new Label("35%:"), pb2,
                new Label("70%:"), pb3,
                new Label("100%:"), pb4,
                new Label("不确定模式:"), pb5,
                indicators
        );
        progressSection.getChildren().add(progresses);

        // --- ListView ---
        VBox listSection = createSection("ListView 列表视图");
        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(
                "macOS Sonoma", "macOS Ventura", "macOS Monterey",
                "macOS Big Sur", "macOS Catalina", "macOS Mojave",
                "macOS High Sierra", "macOS Sierra", "OS X El Capitan"));
        listView.setPrefHeight(160);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().selectRange(0, 2);
        listSection.getChildren().add(listView);

        // --- DatePicker / ColorPicker ---
        VBox pickerSection = createSection("DatePicker / ColorPicker 选择器");
        HBox pickers = new HBox(16);
        pickers.setAlignment(Pos.CENTER_LEFT);
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("选择日期...");
        ColorPicker colorPicker = new ColorPicker(Color.web("#007AFF"));
        pickers.getChildren().addAll(new Label("日期:"), datePicker, new Label("颜色:"), colorPicker);
        pickerSection.getChildren().add(pickers);

        content.getChildren().addAll(
                comboSection, choiceSection, spinnerSection, sliderSection,
                progressSection, listSection, pickerSection
        );
        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    // ==================== Tab 3: 容器控件 ====================

    private Tab createContainerControlsTab() {
        Tab tab = new Tab("容器控件");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(16));

        // --- Pane / StackPane ---
        VBox paneSection = createSection("Pane / StackPane 基础面板");
        StackPane stackDemo = new StackPane();
        stackDemo.setPrefSize(300, 80);
        stackDemo.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-radius: 6; -fx-background-radius: 6;");
        stackDemo.getChildren().add(new Label("StackPane - 元素层叠居中"));
        paneSection.getChildren().add(stackDemo);

        // --- HBox / VBox ---
        VBox boxSection = createSection("HBox / VBox 线性布局");
        HBox hboxDemo = new HBox(8);
        hboxDemo.setAlignment(Pos.CENTER);
        hboxDemo.setPadding(new Insets(8));
        hboxDemo.setStyle("-fx-background-color: -color-accent-subtle; -fx-background-radius: 6;");
        for (int i = 1; i <= 4; i++) {
            Label l = new Label("HBox-" + i);
            l.setStyle("-fx-background-color: -color-bg-default; -fx-padding: 6 12; -fx-border-color: -color-border-default; -fx-border-radius: 6; -fx-background-radius: 6;");
            hboxDemo.getChildren().add(l);
        }
        VBox vboxDemo = new VBox(6);
        vboxDemo.setPadding(new Insets(8));
        vboxDemo.setStyle("-fx-background-color: -color-success-subtle; -fx-background-radius: 6;");
        for (int i = 1; i <= 3; i++) {
            Label l = new Label("VBox 项目 " + i);
            l.setStyle("-fx-background-color: -color-bg-default; -fx-padding: 4 12; -fx-border-color: -color-border-default; -fx-border-radius: 6; -fx-background-radius: 6;");
            vboxDemo.getChildren().add(l);
        }
        HBox boxContainer = new HBox(16, hboxDemo, vboxDemo);
        boxSection.getChildren().add(boxContainer);

        // --- BorderPane ---
        VBox borderSection = createSection("BorderPane 边框布局");
        BorderPane bpDemo = new BorderPane();
        bpDemo.setPrefSize(400, 150);
        bpDemo.setStyle("-fx-border-color: -color-border-default; -fx-border-radius: 6;");
        bpDemo.setTop(makeBorderLabel("Top", "-color-accent-subtle"));
        bpDemo.setBottom(makeBorderLabel("Bottom", "-color-warning-subtle"));
        bpDemo.setLeft(makeBorderLabel("Left", "-color-success-subtle"));
        bpDemo.setRight(makeBorderLabel("Right", "-color-danger-subtle"));
        bpDemo.setCenter(makeBorderLabel("Center", "-color-neutral-subtle"));
        borderSection.getChildren().add(bpDemo);

        // --- GridPane ---
        VBox gridSection = createSection("GridPane 网格布局");
        GridPane gridDemo = new GridPane();
        gridDemo.setHgap(6);
        gridDemo.setVgap(6);
        gridDemo.setPadding(new Insets(8));
        gridDemo.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 6;");
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                Label cell = new Label("(" + r + "," + c + ")");
                cell.setStyle("-fx-background-color: -color-bg-default; -fx-padding: 6 12; "
                        + "-fx-border-color: -color-border-muted; -fx-border-radius: 5; -fx-background-radius: 5;");
                cell.setAlignment(Pos.CENTER);
                cell.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(cell, Priority.ALWAYS);
                gridDemo.add(cell, c, r);
            }
        }
        gridSection.getChildren().add(gridDemo);

        // --- ScrollPane ---
        VBox scrollSection = createSection("ScrollPane 滚动面板");
        VBox scrollContent = new VBox(4);
        for (int i = 1; i <= 30; i++) {
            scrollContent.getChildren().add(new Label("滚动内容行 #" + i + " — 这是一段较长的示例文本用于测试水平和垂直滚动效果"));
        }
        ScrollPane spDemo = new ScrollPane(scrollContent);
        spDemo.setPrefHeight(120);
        spDemo.setFitToWidth(false);
        scrollSection.getChildren().add(spDemo);

        // --- TitledPane ---
        VBox titledSection = createSection("TitledPane 标题面板");
        TitledPane tp1 = new TitledPane("可折叠面板 1", new Label("这是面板 1 的内容。\nTitledPane 可以展开和折叠。"));
        TitledPane tp2 = new TitledPane("可折叠面板 2 (默认折叠)", new Label("面板 2 的详细内容"));
        tp2.setExpanded(false);
        VBox titledPanes = new VBox(8, tp1, tp2);
        titledSection.getChildren().add(titledPanes);

        // --- Accordion ---
        VBox accordionSection = createSection("Accordion 手风琴");
        Accordion accordion = new Accordion();
        for (int i = 1; i <= 4; i++) {
            VBox accContent = new VBox(6);
            accContent.getChildren().addAll(
                    new Label("手风琴面板 " + i + " 的内容"),
                    new Button("面板 " + i + " 按钮")
            );
            accordion.getPanes().add(new TitledPane("手风琴面板 " + i, accContent));
        }
        accordion.setExpandedPane(accordion.getPanes().getFirst());
        accordionSection.getChildren().add(accordion);

        content.getChildren().addAll(
                paneSection, boxSection, borderSection, gridSection,
                scrollSection, titledSection, accordionSection
        );
        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    // ==================== Tab 4: TableView / TreeView ====================

    private Tab createTableTreeTab() {
        Tab tab = new Tab("表格/树");
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.55);

        // --- TableView ---
        VBox tableBox = new VBox(8);
        tableBox.setPadding(new Insets(12));
        Label tableTitle = new Label("TableView 表格视图");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableView<DemoItem> table = new TableView<>();
        table.setItems(createDemoData());

        TableColumn<DemoItem, String> nameCol = new TableColumn<>("名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(120);

        TableColumn<DemoItem, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        TableColumn<DemoItem, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);

        TableColumn<DemoItem, String> descCol = new TableColumn<>("描述");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(180);

        table.getColumns().addAll(nameCol, typeCol, statusCol, descCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().selectRange(0, 2);

        tableBox.getChildren().addAll(tableTitle, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        // --- TreeView ---
        VBox treeBox = new VBox(8);
        treeBox.setPadding(new Insets(12));
        Label treeTitle = new Label("TreeView 树视图");
        treeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TreeItem<String> treeRoot = new TreeItem<>("Applications");
        treeRoot.setExpanded(true);
        String[][] treeData = {
                {"System", "Finder", "Safari", "Terminal"},
                {"Utilities", "Activity Monitor", "Disk Utility"},
                {"Developer", "Xcode", "Instruments", "FileMerge"}
        };
        for (String[] group : treeData) {
            TreeItem<String> parent = new TreeItem<>(group[0]);
            parent.setExpanded(true);
            for (int i = 1; i < group.length; i++) {
                parent.getChildren().add(new TreeItem<>(group[i]));
            }
            treeRoot.getChildren().add(parent);
        }
        TreeView<String> treeView = new TreeView<>(treeRoot);

        treeBox.getChildren().addAll(treeTitle, treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        splitPane.getItems().addAll(tableBox, treeBox);
        tab.setContent(splitPane);
        return tab;
    }

    // ==================== Tab 5: 对话框 ====================

    private Tab createDialogTab() {
        Tab tab = new Tab("对话框");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(16));

        // --- Alert ---
        VBox alertSection = createSection("Alert 警告对话框");
        HBox alertBtns = new HBox(8);
        alertBtns.setAlignment(Pos.CENTER_LEFT);

        Button infoBtn = new Button("信息提示 (INFO)");
        infoBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("信息");
            alert.setHeaderText("操作成功");
            alert.setContentText("这是一个信息提示对话框，展示 macOS 主题效果。");
            alert.showAndWait();
        });

        Button warnBtn = new Button("警告 (WARNING)");
        warnBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText("注意事项");
            alert.setContentText("此操作可能会影响系统设置，请确认后再继续。");
            alert.showAndWait();
        });

        Button errorBtn = new Button("错误 (ERROR)");
        errorBtn.getStyleClass().add("danger");
        errorBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText("操作失败");
            alert.setContentText("无法完成请求的操作。请检查网络连接后重试。");
            alert.showAndWait();
        });

        Button confirmBtn = new Button("确认 (CONFIRMATION)");
        confirmBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认");
            alert.setHeaderText("确认删除");
            alert.setContentText("您确定要删除选中的项目吗？此操作不可撤销。");
            alert.showAndWait();
        });

        alertBtns.getChildren().addAll(infoBtn, warnBtn, errorBtn, confirmBtn);
        alertSection.getChildren().add(alertBtns);

        // --- Dialog (自定义) ---
        VBox dialogSection = createSection("Dialog 自定义对话框");
        Button customDialogBtn = new Button("打开自定义对话框");
        customDialogBtn.setOnAction(e -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("自定义对话框");
            dialog.setHeaderText("请输入信息");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            VBox dialogContent = new VBox(10);
            dialogContent.getChildren().addAll(
                    new Label("用户名:"), new TextField(),
                    new Label("Apple ID:"), new TextField()
            );
            dialog.getDialogPane().setContent(dialogContent);
            dialog.showAndWait();
        });
        dialogSection.getChildren().add(customDialogBtn);

        // --- TextInputDialog ---
        VBox textDialogSection = createSection("TextInputDialog 文本输入对话框");
        Button textDialogBtn = new Button("打开文本输入对话框");
        textDialogBtn.setOnAction(e -> {
            TextInputDialog tid = new TextInputDialog("默认值");
            tid.setTitle("输入");
            tid.setHeaderText("请输入项目名称");
            tid.setContentText("名称:");
            tid.showAndWait();
        });
        textDialogSection.getChildren().add(textDialogBtn);

        // --- ChoiceDialog ---
        VBox choiceDialogSection = createSection("ChoiceDialog 选择对话框");
        Button choiceDialogBtn = new Button("打开选择对话框");
        choiceDialogBtn.setOnAction(e -> {
            ChoiceDialog<String> cd = new ChoiceDialog<>("Swift", "Swift", "Objective-C", "Python", "JavaScript", "Kotlin");
            cd.setTitle("选择语言");
            cd.setHeaderText("请选择编程语言");
            cd.setContentText("语言:");
            cd.showAndWait();
        });
        choiceDialogSection.getChildren().add(choiceDialogBtn);

        // --- FileChooser / DirectoryChooser ---
        VBox fileChooserSection = createSection("FileChooser / DirectoryChooser 文件选择器");
        HBox fileButtons = new HBox(8);
        Button openFileBtn = new Button("打开文件选择器");
        openFileBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("选择文件");
            fc.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("所有文件", "*.*"),
                    new javafx.stage.FileChooser.ExtensionFilter("Swift 文件", "*.swift"),
                    new javafx.stage.FileChooser.ExtensionFilter("Java 文件", "*.java")
            );
            fc.showOpenDialog(null);
        });
        Button openDirBtn = new Button("打开目录选择器");
        openDirBtn.setOnAction(e -> {
            javafx.stage.DirectoryChooser dc = new javafx.stage.DirectoryChooser();
            dc.setTitle("选择目录");
            dc.showDialog(null);
        });
        fileButtons.getChildren().addAll(openFileBtn, openDirBtn);
        fileChooserSection.getChildren().add(fileButtons);

        content.getChildren().addAll(alertSection, dialogSection, textDialogSection, choiceDialogSection, fileChooserSection);
        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    // ==================== Tab 6: 其他控件 ====================

    private Tab createOtherControlsTab() {
        Tab tab = new Tab("其他控件");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(16));

        // --- SplitPane ---
        VBox splitSection = createSection("SplitPane 分割面板");
        SplitPane spDemo = new SplitPane();
        spDemo.setPrefHeight(120);
        spDemo.getItems().addAll(
                makeSplitChild("左侧面板", "-color-accent-subtle"),
                makeSplitChild("中间面板", "-color-success-subtle"),
                makeSplitChild("右侧面板", "-color-warning-subtle")
        );
        spDemo.setDividerPositions(0.33, 0.66);
        splitSection.getChildren().add(spDemo);

        // --- TabPane (嵌套) ---
        VBox nestedTabSection = createSection("TabPane 选项卡面板 (嵌套演示)");
        TabPane nestedTabPane = new TabPane();
        nestedTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        nestedTabPane.setPrefHeight(150);
        for (int i = 1; i <= 5; i++) {
            VBox tabContent = new VBox(8);
            tabContent.setPadding(new Insets(12));
            tabContent.getChildren().addAll(
                    new Label("嵌套选项卡 " + i + " 的内容"),
                    new Button("选项卡 " + i + " 的按钮"),
                    new TextField("选项卡 " + i + " 的输入框")
            );
            nestedTabPane.getTabs().add(new Tab("标签 " + i, tabContent));
        }
        nestedTabSection.getChildren().add(nestedTabPane);

        // --- Separator ---
        VBox separatorSection = createSection("Separator 分隔线");
        VBox sepDemo = new VBox(8);
        sepDemo.getChildren().addAll(
                new Label("水平分隔线:"),
                new Separator(),
                new Label("内容区域"),
                new Separator(),
                new Label("尾部区域")
        );
        HBox vertSepDemo = new HBox(12);
        vertSepDemo.setAlignment(Pos.CENTER);
        vertSepDemo.getChildren().addAll(
                new Label("项目 A"),
                new Separator(Orientation.VERTICAL),
                new Label("项目 B"),
                new Separator(Orientation.VERTICAL),
                new Label("项目 C")
        );
        vertSepDemo.setPrefHeight(30);
        sepDemo.getChildren().addAll(new Label("垂直分隔线:"), vertSepDemo);
        separatorSection.getChildren().add(sepDemo);

        // --- ContextMenu ---
        VBox contextSection = createSection("ContextMenu 上下文菜单");
        Label ctxLabel = new Label("右键点击此区域查看 ContextMenu");
        ctxLabel.setPrefSize(350, 60);
        ctxLabel.setAlignment(Pos.CENTER);
        ctxLabel.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        ContextMenu ctxMenu = new ContextMenu();
        ctxMenu.getItems().addAll(
                new MenuItem("剪切"),
                new MenuItem("复制"),
                new MenuItem("粘贴"),
                new SeparatorMenuItem(),
                new MenuItem("全选"),
                new SeparatorMenuItem(),
                new MenuItem("查看简介")
        );
        ctxLabel.setContextMenu(ctxMenu);
        contextSection.getChildren().add(ctxLabel);

        // --- Tooltip ---
        VBox tooltipSection = createSection("Tooltip 工具提示");
        HBox tooltipDemo = new HBox(12);
        Button tipBtn1 = new Button("悬停查看提示");
        tipBtn1.setTooltip(new Tooltip("这是一个工具提示\n支持多行文本"));
        Button tipBtn2 = new Button("另一个提示");
        tipBtn2.setTooltip(new Tooltip("macOS 风格的 Tooltip"));
        tooltipDemo.getChildren().addAll(tipBtn1, tipBtn2);
        tooltipSection.getChildren().add(tooltipDemo);

        // --- Pagination ---
        VBox paginationSection = createSection("Pagination 分页控件");
        Pagination pagination = new Pagination(10, 0);
        pagination.setMaxPageIndicatorCount(5);
        pagination.setPrefHeight(100);
        pagination.setPageFactory(index -> new Label("第 " + (index + 1) + " 页的内容"));
        paginationSection.getChildren().add(pagination);

        content.getChildren().addAll(
                splitSection, nestedTabSection, separatorSection,
                contextSection, tooltipSection, paginationSection
        );
        scroll.setContent(content);
        tab.setContent(scroll);
        return tab;
    }

    // ==================== 辅助方法 ====================

    /** 创建带标题的分组区域 */
    private VBox createSection(String title) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(12));
        section.setStyle("-fx-background-color: -color-bg-overlay; -fx-border-color: -color-border-muted; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        section.getChildren().add(titleLabel);
        return section;
    }

    /** 创建带样式的标签 */
    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    /** 创建 BorderPane 区域标签 */
    private Label makeBorderLabel(String text, String bgColor) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setPadding(new Insets(8));
        label.setStyle("-fx-background-color: " + bgColor + "; -fx-font-weight: bold;");
        return label;
    }

    /** 创建 SplitPane 子面板 */
    private VBox makeSplitChild(String text, String bgColor) {
        VBox box = new VBox(new Label(text));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + bgColor + ";");
        return box;
    }

    /** 创建演示数据 */
    private ObservableList<DemoItem> createDemoData() {
        return FXCollections.observableArrayList(
                new DemoItem("macOS Sonoma", "操作系统", "最新", "macOS 14 - 2023 年发布"),
                new DemoItem("macOS Ventura", "操作系统", "活跃", "macOS 13 - 2022 年发布"),
                new DemoItem("macOS Monterey", "操作系统", "维护", "macOS 12 - 2021 年发布"),
                new DemoItem("macOS Big Sur", "操作系统", "停止", "macOS 11 - 2020 年发布"),
                new DemoItem("Safari", "浏览器", "活跃", "Apple 内建浏览器"),
                new DemoItem("Xcode", "开发工具", "活跃", "Apple 集成开发环境"),
                new DemoItem("Swift", "编程语言", "活跃", "Apple 现代编程语言"),
                new DemoItem("SwiftUI", "UI 框架", "活跃", "Apple 声明式 UI 框架")
        );
    }

    // ==================== 数据模型 ====================

    /** 演示用数据项 */
    public static class DemoItem {
        private final SimpleStringProperty name;
        private final SimpleStringProperty type;
        private final SimpleStringProperty status;
        private final SimpleStringProperty description;

        public DemoItem(String name, String type, String status, String description) {
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
            this.status = new SimpleStringProperty(status);
            this.description = new SimpleStringProperty(description);
        }

        public String getName() { return name.get(); }
        public String getType() { return type.get(); }
        public String getStatus() { return status.get(); }
        public String getDescription() { return description.get(); }
    }

    // ==================== 入口 ====================

    public static void main(String[] args) {
        launch(args);
    }
}
