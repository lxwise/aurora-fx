package io.aurora.fx.components.dynamicForm;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Form 表单组件完整功能演示
 * <p>
 * 对标 Element UI Form 组件，展示以下核心功能：
 * </p>
 * <ol>
 *   <li>典型表单 - 基础表单元素 (input, select, radio, checkbox, switch, textarea)</li>
 *   <li>行内表单 - 水平紧凑布局</li>
 *   <li>对齐方式 - 标签位置切换 (left / right / top)</li>
 *   <li>表单验证 - required、min/max、pattern、自定义校验</li>
 *   <li>动态表单 - 动态增删表单项 + 条件渲染</li>
 *   <li>尺寸控制 - large / default / small / mini</li>
 *   <li>状态管理 - 脏检测/持久化/撤销重做</li>
 *   <li>分组布局 - Group/Section 语义分组</li>
 *   <li>计算属性 - computed / watch / batchUpdate</li>
 *   <li>高级验证 - 跨字段/条件/警告级别/枚举白名单</li>
 *   <li>栅格布局 - 多列栅格/列间距/自适应</li>
 *   <li>主题切换 - Element UI / Ant Design / Naive UI 主题</li>
 *   <li>事件系统 - 表单生命周期事件监听</li>
 *   <li>插件扩展 - FormFieldFactory 自定义字段渲染器</li>
 * </ol>
 *
 * @author Form Component
 * @version 1.0
 */
public class FormDemo extends Application {

    private static final Logger LOGGER = Logger.getLogger(FormDemo.class.getName());

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 14px;");

        tabPane.getTabs().addAll(
                new Tab("典型表单", wrapScroll(createBasicFormDemo())),
                new Tab("行内表单", wrapScroll(createInlineFormDemo())),
                new Tab("对齐方式", wrapScroll(createAlignmentDemo())),
                new Tab("表单验证", wrapScroll(createValidationDemo())),
                new Tab("动态表单", wrapScroll(createDynamicFormDemo())),
                new Tab("尺寸控制", wrapScroll(createSizeDemo())),
                new Tab("状态管理", wrapScroll(createStateManagementDemo())),
                new Tab("分组布局", wrapScroll(createGroupLayoutDemo())),
                new Tab("计算属性", wrapScroll(createComputedPropertyDemo())),
                new Tab("高级验证", wrapScroll(createAdvancedValidationDemo())),
                new Tab("栅格布局", wrapScroll(createGridLayoutDemo())),
                new Tab("主题切换", wrapScroll(createThemeSwitchDemo())),
                new Tab("事件系统", wrapScroll(createEventSystemDemo())),
                new Tab("插件扩展", wrapScroll(createPluginDemo()))
        );

        Scene scene = new Scene(tabPane, 1000, 750);
        primaryStage.setTitle("Form 表单组件演示 v2.0 - 对标 Element UI / Ant Design / Naive UI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** 包裹 ScrollPane */
    private Node wrapScroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPadding(new Insets(10));
        return sp;
    }

    /** 创建区域标题 */
    private Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web("#303133"));
        lbl.setPadding(new Insets(10, 0, 5, 0));
        return lbl;
    }

    /** 创建状态输出标签 */
    private Label statusLabel() {
        Label lbl = new Label();
        lbl.setTextFill(Color.web("#67C23A"));
        lbl.setFont(Font.font(13));
        lbl.setPadding(new Insets(5, 0, 0, 0));
        return lbl;
    }

    // ================================================================
    // 1. 典型表单
    // ================================================================

    @SuppressWarnings("unchecked")
    private Node createBasicFormDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("典型表单"));

        Label desc = new Label("最基础的表单包括各种输入表单项，如 input、select、radio、checkbox 等。");
        desc.setTextFill(Color.web("#909399"));
        container.getChildren().add(desc);

        // 数据模型
        FormModel model = new FormModel()
                .field("name", "")
                .field("region", "")
                .field("date", null)
                .field("delivery", false)
                .field("type", FXCollections.observableArrayList())
                .field("resource", "")
                .field("desc", "");

        // 控件
        TextField nameField = new TextField();
        nameField.setPromptText("请输入活动名称");

        ComboBox<String> regionBox = new ComboBox<>();
        regionBox.getItems().addAll("区域一", "区域二");
        regionBox.setPromptText("请选择活动区域");
        regionBox.setPrefWidth(300);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("选择日期");

        CheckBox deliveryBox = new CheckBox("即时配送");

        CheckBox typeCb1 = new CheckBox("线上活动");
        CheckBox typeCb2 = new CheckBox("推广活动");
        CheckBox typeCb3 = new CheckBox("线下活动");
        CheckBox typeCb4 = new CheckBox("单纯品牌曝光");
        HBox typeGroup = new HBox(15, typeCb1, typeCb2, typeCb3, typeCb4);
        typeGroup.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup resourceGroup = new ToggleGroup();
        RadioButton rb1 = new RadioButton("线上品牌赞助");
        RadioButton rb2 = new RadioButton("线下场地");
        rb1.setToggleGroup(resourceGroup);
        rb2.setToggleGroup(resourceGroup);
        HBox radioGroup = new HBox(15, rb1, rb2);
        radioGroup.setAlignment(Pos.CENTER_LEFT);

        TextArea descArea = new TextArea();
        descArea.setPromptText("请输入活动形式");
        descArea.setPrefRowCount(3);

        // 构建表单
        Form form = new Form()
                .model(model)
                .labelWidth(100);

        FormItem typeItem = new FormItem("活动性质", "type").content(typeGroup);
        FormItem resourceItem = new FormItem("特殊资源", "resource").content(radioGroup);

        form.addItems(
                new FormItem("活动名称", "name", nameField),
                new FormItem("活动区域", "region", regionBox),
                new FormItem("活动时间", "date", datePicker),
                new FormItem("即时配送", "delivery", deliveryBox),
                typeItem,
                resourceItem,
                new FormItem("活动形式", "desc", descArea)
        );

        // 设置绑定
        typeItem.bindCheckBoxGroup(Arrays.asList(typeCb1, typeCb2, typeCb3, typeCb4), "type");
        resourceItem.bindRadioGroup(resourceGroup, "resource");

        // 按钮
        Label statusLbl = statusLabel();
        Button submitBtn = new Button("创建");
        submitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            statusLbl.setText("提交数据: " + model.toMap());
            statusLbl.setTextFill(Color.web("#67C23A"));
        });

        Button cancelBtn = new Button("取消");
        cancelBtn.setStyle("-fx-padding: 6 20;");
        cancelBtn.setOnAction(e -> {
            model.reset();
            form.clearValidate();
            statusLbl.setText("已重置");
        });

        HBox btnRow = new HBox(10, submitBtn, cancelBtn);
        btnRow.setPadding(new Insets(0, 0, 0, 112));

        container.getChildren().addAll(form.getNode(), btnRow, statusLbl);
        return container;
    }

    // ================================================================
    // 2. 行内表单
    // ================================================================

    private Node createInlineFormDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("行内表单"));

        Label desc = new Label("设置 inline 属性为 true，表单域变为行内布局，适用于简单查询场景。");
        desc.setTextFill(Color.web("#909399"));
        container.getChildren().add(desc);

        FormModel model = new FormModel()
                .field("user", "")
                .field("region", "")
                .field("date", null);

        TextField userField = new TextField();
        userField.setPromptText("审批人");
        userField.setPrefWidth(180);

        ComboBox<String> regionBox = new ComboBox<>();
        regionBox.getItems().addAll("区域一", "区域二");
        regionBox.setPromptText("活动区域");
        regionBox.setPrefWidth(180);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("选择日期");
        datePicker.setPrefWidth(180);

        Label statusLbl = statusLabel();

        Form form = new Form()
                .model(model)
                .inline(true)
                .labelWidth(70);

        form.addItems(
                new FormItem("审批人", "user", userField),
                new FormItem("活动区域", "region", regionBox),
                new FormItem("活动时间", "date", datePicker)
        );

        Button queryBtn = new Button("查询");
        queryBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        queryBtn.setOnAction(e -> {
            statusLbl.setText("查询参数: " + model.toMap());
            statusLbl.setTextFill(Color.web("#409EFF"));
        });

        // 按钮也作为 FormItem（无 prop）
        FormItem btnItem = new FormItem();
        btnItem.setContent(queryBtn);
        form.addItem(btnItem);

        container.getChildren().addAll(form.getNode(), statusLbl);
        return container;
    }

    // ================================================================
    // 3. 对齐方式
    // ================================================================

    private Node createAlignmentDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("对齐方式"));

        Label desc = new Label("通过 label-position 属性改变标签位置，可选值为 left、right、top。"
                + "\n可以在 Form 级设置，也可以在 FormItem 级单独覆盖。");
        desc.setTextFill(Color.web("#909399"));
        container.getChildren().add(desc);

        FormModel model = new FormModel()
                .field("name", "")
                .field("region", "")
                .field("type", "");

        // 控制面板
        HBox controlPanel = new HBox(20);
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.setPadding(new Insets(5, 0, 10, 0));

        Label formAlignLabel = new Label("Form 对齐:");
        formAlignLabel.setFont(Font.font(13));
        ToggleGroup formAlignGroup = new ToggleGroup();
        RadioButton leftRb = new RadioButton("Left");
        RadioButton rightRb = new RadioButton("Right");
        RadioButton topRb = new RadioButton("Top");
        leftRb.setToggleGroup(formAlignGroup);
        rightRb.setToggleGroup(formAlignGroup);
        topRb.setToggleGroup(formAlignGroup);
        rightRb.setSelected(true);
        controlPanel.getChildren().addAll(formAlignLabel, leftRb, rightRb, topRb);

        // 表单
        Form form = new Form()
                .model(model)
                .labelWidth(100)
                .labelPosition(FormLabelPosition.RIGHT);

        TextField nameField = new TextField();
        nameField.setPromptText("请输入名称");
        TextField regionField = new TextField();
        regionField.setPromptText("请输入活动区域");
        TextField typeField = new TextField();
        typeField.setPromptText("请输入活动形式");

        form.addItems(
                new FormItem("Name", "name", nameField),
                new FormItem("Activity zone", "region", regionField),
                new FormItem("Activity form", "type", typeField)
        );

        // 切换对齐
        formAlignGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV == leftRb) form.labelPosition(FormLabelPosition.LEFT);
            else if (newV == rightRb) form.labelPosition(FormLabelPosition.RIGHT);
            else if (newV == topRb) form.labelPosition(FormLabelPosition.TOP);
        });

        container.getChildren().addAll(controlPanel, form.getNode());
        return container;
    }

    // ================================================================
    // 4. 表单验证
    // ================================================================

    @SuppressWarnings("unchecked")
    private Node createValidationDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("表单验证"));

        Label desc = new Label("为 rules 属性传入验证规则，支持 required、min/max、pattern、自定义校验等。");
        desc.setTextFill(Color.web("#909399"));
        container.getChildren().add(desc);

        FormModel model = new FormModel()
                .field("name", "")
                .field("region", "")
                .field("date", null)
                .field("delivery", false)
                .field("type", FXCollections.observableArrayList())
                .field("resource", "")
                .field("desc", "");

        // 验证规则
        Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
        rules.put("name", Arrays.asList(
                FormValidationRule.required("请输入活动名称"),
                FormValidationRule.length(2, 20, "长度在 2 到 20 个字符")
        ));
        rules.put("region", Collections.singletonList(
                FormValidationRule.required("请选择活动区域")
        ));
        rules.put("date", Collections.singletonList(
                FormValidationRule.required("请选择活动时间")
        ));
        rules.put("type", Collections.singletonList(
                FormValidationRule.builder()
                        .required(true)
                        .type("array")
                        .message("请至少选择一个活动性质")
                        .trigger("change")
                        .build()
        ));
        rules.put("resource", Collections.singletonList(
                FormValidationRule.required("请选择特殊资源")
        ));
        rules.put("desc", Collections.singletonList(
                FormValidationRule.required("请填写活动形式")
        ));

        // 控件
        TextField nameField = new TextField();
        nameField.setPromptText("请输入活动名称");

        ComboBox<String> regionBox = new ComboBox<>();
        regionBox.getItems().addAll("区域一", "区域二");
        regionBox.setPromptText("请选择活动区域");
        regionBox.setPrefWidth(300);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("选择日期");

        CheckBox deliveryBox = new CheckBox("即时配送");

        CheckBox typeCb1 = new CheckBox("线上活动");
        CheckBox typeCb2 = new CheckBox("推广活动");
        CheckBox typeCb3 = new CheckBox("线下活动");
        CheckBox typeCb4 = new CheckBox("单纯品牌曝光");
        HBox typeGroup = new HBox(15, typeCb1, typeCb2, typeCb3, typeCb4);
        typeGroup.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup resourceGroup = new ToggleGroup();
        RadioButton rb1 = new RadioButton("线上品牌赞助");
        RadioButton rb2 = new RadioButton("线下场地");
        rb1.setToggleGroup(resourceGroup);
        rb2.setToggleGroup(resourceGroup);
        HBox radioRow = new HBox(15, rb1, rb2);
        radioRow.setAlignment(Pos.CENTER_LEFT);

        TextArea descArea = new TextArea();
        descArea.setPromptText("请填写活动形式");
        descArea.setPrefRowCount(3);

        // 构建表单
        Form form = new Form()
                .model(model)
                .rules(rules)
                .labelWidth(100);

        FormItem typeItem = new FormItem("活动性质", "type").content(typeGroup);
        FormItem resourceItem = new FormItem("特殊资源", "resource").content(radioRow);

        form.addItems(
                new FormItem("活动名称", "name", nameField),
                new FormItem("活动区域", "region", regionBox),
                new FormItem("活动时间", "date", datePicker),
                new FormItem("即时配送", "delivery", deliveryBox),
                typeItem,
                resourceItem,
                new FormItem("活动形式", "desc", descArea)
        );

        typeItem.bindCheckBoxGroup(Arrays.asList(typeCb1, typeCb2, typeCb3, typeCb4), "type");
        resourceItem.bindRadioGroup(resourceGroup, "resource");

        // 按钮
        Label statusLbl = statusLabel();

        Button submitBtn = new Button("提交");
        submitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            form.validate(valid -> {
                if (valid) {
                    statusLbl.setText("验证通过! 数据: " + model.toMap());
                    statusLbl.setTextFill(Color.web("#67C23A"));
                } else {
                    statusLbl.setText("验证失败，请检查表单");
                    statusLbl.setTextFill(Color.web("#F56C6C"));
                }
            });
        });

        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-padding: 6 20;");
        resetBtn.setOnAction(e -> {
            form.resetFields();
            statusLbl.setText("已重置");
            statusLbl.setTextFill(Color.web("#909399"));
        });

        HBox btnRow = new HBox(10, submitBtn, resetBtn);
        btnRow.setPadding(new Insets(0, 0, 0, 112));

        container.getChildren().addAll(form.getNode(), btnRow, statusLbl);
        return container;
    }

    // ================================================================
    // 5. 动态表单
    // ================================================================

    private Node createDynamicFormDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("动态增减表单项"));

        Label desc = new Label("支持动态添加或删除表单项，以及根据条件渲染不同的表单内容。");
        desc.setTextFill(Color.web("#909399"));
        container.getChildren().add(desc);

        // --- Part 1: 动态域名列表 ---
        Label part1Title = new Label("1. 动态添加/删除域名");
        part1Title.setFont(Font.font("System", FontWeight.BOLD, 14));
        container.getChildren().add(part1Title);

        FormModel domainModel = new FormModel()
                .field("email", "");

        Form domainForm = new Form()
                .model(domainModel)
                .labelWidth(100);

        // 邮箱字段
        TextField emailField = new TextField();
        emailField.setPromptText("请输入邮箱");
        FormItem emailItem = new FormItem("邮箱", "email", emailField)
                .rules(Collections.singletonList(FormValidationRule.email("请输入正确的邮箱")));
        domainForm.addItem(emailItem);

        AtomicInteger domainCounter = new AtomicInteger(0);
        List<String> dynamicDomainProps = new ArrayList<>();

        Button addDomainBtn = new Button("+ 新增域名");
        addDomainBtn.setStyle("-fx-text-fill: #409EFF; -fx-cursor: hand;");

        Label domainStatusLbl = statusLabel();

        addDomainBtn.setOnAction(e -> {
            int idx = domainCounter.incrementAndGet();
            String propName = "domain_" + idx;
            dynamicDomainProps.add(propName);
            domainModel.field(propName, "");

            TextField domainField = new TextField();
            domainField.setPromptText("请输入域名 " + idx);
            domainField.setPrefWidth(250);

            Button removeBtn = new Button("删除");
            removeBtn.setStyle("-fx-text-fill: #F56C6C; -fx-cursor: hand;");

            HBox fieldRow = new HBox(10, domainField, removeBtn);
            fieldRow.setAlignment(Pos.CENTER_LEFT);

            FormItem domainItem = new FormItem("域名 " + idx, propName)
                    .content(fieldRow)
                    .rules(Collections.singletonList(FormValidationRule.required("域名不能为空")));

            // 手动绑定（因为 content 是 HBox，不是直接的 TextField）
            domainField.textProperty().addListener((obs, oldV, newV) -> domainModel.setFieldValue(propName, newV));

            // 插入到按钮行之前
            int insertIdx = domainForm.getItemCount() - 1; // 在按钮之前
            domainForm.insertItem(insertIdx, domainItem);

            removeBtn.setOnAction(ev -> {
                domainForm.removeItem(domainItem);
                dynamicDomainProps.remove(propName);
            });
        });

        // 按钮行
        FormItem addBtnItem = new FormItem();
        HBox addBtnRow = new HBox(10, addDomainBtn);
        addBtnItem.setContent(addBtnRow);
        domainForm.addItem(addBtnItem);

        // 提交按钮
        Button domainSubmitBtn = new Button("提交");
        domainSubmitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        domainSubmitBtn.setOnAction(e -> {
            domainForm.validate(valid -> {
                if (valid) {
                    domainStatusLbl.setText("验证通过! " + domainModel.toMap());
                    domainStatusLbl.setTextFill(Color.web("#67C23A"));
                } else {
                    domainStatusLbl.setText("请检查填写内容");
                    domainStatusLbl.setTextFill(Color.web("#F56C6C"));
                }
            });
        });

        Button domainResetBtn = new Button("重置");
        domainResetBtn.setStyle("-fx-padding: 6 20;");
        domainResetBtn.setOnAction(e -> {
            // 移除所有动态域名项
            for (String propName : new ArrayList<>(dynamicDomainProps)) {
                domainForm.removeItem(propName);
            }
            dynamicDomainProps.clear();
            domainCounter.set(0);
            domainForm.resetFields();
            domainStatusLbl.setText("已重置");
        });

        HBox domainBtnRow = new HBox(10, domainSubmitBtn, domainResetBtn);
        domainBtnRow.setPadding(new Insets(0, 0, 0, 112));

        container.getChildren().addAll(domainForm.getNode(), domainBtnRow, domainStatusLbl);

        // --- Part 2: 条件渲染 ---
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        container.getChildren().add(separator);

        Label part2Title = new Label("2. 条件渲染 - 根据选择显示不同表单");
        part2Title.setFont(Font.font("System", FontWeight.BOLD, 14));
        container.getChildren().add(part2Title);

        FormModel condModel = new FormModel()
                .field("activityType", "线上")
                .field("onlineUrl", "")
                .field("offlineAddress", "")
                .field("offlineCapacity", "");

        Form condForm = new Form()
                .model(condModel)
                .labelWidth(100);

        ComboBox<String> actTypeBox = new ComboBox<>();
        actTypeBox.getItems().addAll("线上", "线下");
        actTypeBox.setValue("线上");
        actTypeBox.setPrefWidth(200);

        TextField urlField = new TextField();
        urlField.setPromptText("请输入活动链接");
        FormItem urlItem = new FormItem("活动链接", "onlineUrl", urlField)
                .rules(Collections.singletonList(FormValidationRule.url("请输入正确的URL")));

        TextField addrField = new TextField();
        addrField.setPromptText("请输入活动地址");
        FormItem addrItem = new FormItem("活动地址", "offlineAddress", addrField)
                .rules(Collections.singletonList(FormValidationRule.required("请输入活动地址")));

        TextField capField = new TextField();
        capField.setPromptText("请输入容纳人数");
        FormItem capItem = new FormItem("容纳人数", "offlineCapacity", capField);

        condForm.addItems(
                new FormItem("活动类型", "activityType", actTypeBox),
                urlItem // 默认显示线上字段
        );

        Label condStatusLbl = statusLabel();

        // 监听活动类型切换
        actTypeBox.valueProperty().addListener((obs, oldV, newV) -> {
            if ("线上".equals(newV)) {
                // 使用 prop 名称移除，避免实例引用失效
                condForm.removeItem("offlineAddress");
                condForm.removeItem("offlineCapacity");
                // 重新创建 urlItem
                FormItem newUrlItem = new FormItem("活动链接", "onlineUrl");
                TextField newUrlField = new TextField();
                newUrlField.setPromptText("请输入活动链接");
                newUrlItem.content(newUrlField);
                newUrlItem.rules(Collections.singletonList(FormValidationRule.url("请输入正确的URL")));
                condForm.addItem(newUrlItem);
            } else {
                condForm.removeItem("onlineUrl");
                // 重新创建线下字段
                FormItem newAddrItem = new FormItem("活动地址", "offlineAddress");
                TextField newAddrField = new TextField();
                newAddrField.setPromptText("请输入活动地址");
                newAddrItem.content(newAddrField);
                newAddrItem.rules(Collections.singletonList(FormValidationRule.required("请输入活动地址")));

                FormItem newCapItem = new FormItem("容纳人数", "offlineCapacity");
                TextField newCapField = new TextField();
                newCapField.setPromptText("请输入容纳人数");
                newCapItem.content(newCapField);

                condForm.addItem(newAddrItem);
                condForm.addItem(newCapItem);
            }
            condForm.clearValidate();
        });

        Button condSubmitBtn = new Button("提交");
        condSubmitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        condSubmitBtn.setOnAction(e -> {
            condForm.validate(valid -> {
                condStatusLbl.setText(valid
                        ? "验证通过! " + condModel.toMap()
                        : "请检查填写内容");
                condStatusLbl.setTextFill(valid ? Color.web("#67C23A") : Color.web("#F56C6C"));
            });
        });

        HBox condBtnRow = new HBox(10, condSubmitBtn);
        condBtnRow.setPadding(new Insets(5, 0, 0, 112));

        container.getChildren().addAll(condForm.getNode(), condBtnRow, condStatusLbl);
        return container;
    }

    // ================================================================
    // 6. 尺寸控制
    // ================================================================

    private Node createSizeDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("尺寸控制"));

        Label desc = new Label("表单中所有子组件继承 Form 的 size 属性，支持 large / default / small 三种尺寸。");
        desc.setTextFill(Color.web("#909399"));
        container.getChildren().add(desc);

        // 尺寸选择
        HBox sizePanel = new HBox(15);
        sizePanel.setAlignment(Pos.CENTER_LEFT);
        Label sizeLabel = new Label("当前尺寸:");
        sizeLabel.setFont(Font.font(13));

        ToggleGroup sizeGroup = new ToggleGroup();
        RadioButton largeRb = new RadioButton("Large");
        RadioButton defaultRb = new RadioButton("Default");
        RadioButton smallRb = new RadioButton("Small");
        largeRb.setToggleGroup(sizeGroup);
        defaultRb.setToggleGroup(sizeGroup);
        smallRb.setToggleGroup(sizeGroup);
        defaultRb.setSelected(true);
        sizePanel.getChildren().addAll(sizeLabel, largeRb, defaultRb, smallRb);
        container.getChildren().add(sizePanel);

        // 表单
        FormModel model = new FormModel()
                .field("name", "")
                .field("region", "")
                .field("date", null);

        TextField nameField = new TextField();
        nameField.setPromptText("请输入活动名称");

        ComboBox<String> regionBox = new ComboBox<>();
        regionBox.getItems().addAll("区域一", "区域二");
        regionBox.setPromptText("请选择活动区域");
        regionBox.setPrefWidth(250);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("选择日期");

        Form form = new Form()
                .model(model)
                .labelWidth(100)
                .size(FormSize.DEFAULT);

        form.addItems(
                new FormItem("活动名称", "name", nameField),
                new FormItem("活动区域", "region", regionBox),
                new FormItem("活动时间", "date", datePicker)
        );

        Button submitBtn = new Button("提交");
        submitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");

        FormItem btnItem = new FormItem();
        btnItem.setContent(submitBtn);
        form.addItem(btnItem);

        // 尺寸切换
        sizeGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV == largeRb) form.size(FormSize.LARGE);
            else if (newV == defaultRb) form.size(FormSize.DEFAULT);
            else if (newV == smallRb) form.size(FormSize.SMALL);
        });

        container.getChildren().add(form.getNode());
        return container;
    }

    // ================================================================
    // 7. 状态管理演示
    // ================================================================

    private Node createStateManagementDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("状态管理"));

        Label desc = new Label("演示脏数据检测、持久化/回滚、撤销/重做等表单状态管理功能。\n"
                + "修改字段后可以观察脏状态变化，使用按钮操作不同的状态管理功能。");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        // 数据模型
        FormModel model = new FormModel()
                .field("username", "张三")
                .field("email", "zhangsan@example.com")
                .field("age", "25")
                .field("city", "北京");

        // 控件
        TextField usernameField = new TextField();
        usernameField.setPromptText("请输入用户名");

        TextField emailField = new TextField();
        emailField.setPromptText("请输入邮箱");

        TextField ageField = new TextField();
        ageField.setPromptText("请输入年龄");

        ComboBox<String> cityBox = new ComboBox<>();
        cityBox.getItems().addAll("北京", "上海", "广州", "深圳", "杭州");
        cityBox.setPromptText("请选择城市");
        cityBox.setPrefWidth(250);

        // 构建表单
        Form form = new Form()
                .model(model)
                .labelWidth(100);

        form.addItems(
                new FormItem("用户名", "username", usernameField)
                        .tooltip("输入您的用户名"),
                new FormItem("邮箱", "email", emailField)
                        .tooltip("输入有效的邮箱地址"),
                new FormItem("年龄", "age", ageField),
                new FormItem("城市", "city", cityBox)
        );

        // 获取状态管理器
        FormStateManager stateManager = form.getStateManager();

        // 状态显示面板
        Label dirtyLabel = new Label("脏状态: false");
        dirtyLabel.setFont(Font.font(13));
        Label dirtyFieldsLabel = new Label("脏字段: 无");
        dirtyFieldsLabel.setFont(Font.font(13));
        Label statusLbl = statusLabel();

        // 脏状态绑定
        stateManager.dirtyProperty().addListener((obs, oldV, newV) -> {
            dirtyLabel.setText("脏状态: " + newV);
            dirtyLabel.setTextFill(newV ? Color.web("#E6A23C") : Color.web("#67C23A"));
            dirtyFieldsLabel.setText("脏字段: " +
                    (newV ? String.join(", ", stateManager.getDirtyFields()) : "无"));
        });

        VBox statePanel = new VBox(5, dirtyLabel, dirtyFieldsLabel);
        statePanel.setPadding(new Insets(10));
        statePanel.setStyle("-fx-background-color: #F5F7FA; -fx-background-radius: 4;");

        // 操作按钮
        Button persistBtn = new Button("持久化 (Persist)");
        persistBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 15; -fx-cursor: hand;");
        persistBtn.setOnAction(e -> {
            form.persist();
            statusLbl.setText("已持久化当前值为基准");
            statusLbl.setTextFill(Color.web("#67C23A"));
        });

        Button rollbackBtn = new Button("回滚 (Rollback)");
        rollbackBtn.setStyle("-fx-background-color: #E6A23C; -fx-text-fill: white; -fx-padding: 6 15; -fx-cursor: hand;");
        rollbackBtn.setOnAction(e -> {
            form.rollback();
            statusLbl.setText("已回滚到上次持久化状态");
            statusLbl.setTextFill(Color.web("#E6A23C"));
        });

        Button snapshotBtn = new Button("保存快照");
        snapshotBtn.setStyle("-fx-padding: 6 15; -fx-cursor: hand;");
        snapshotBtn.setOnAction(e -> {
            stateManager.pushUndoSnapshot();
            statusLbl.setText("已保存撤销快照");
            statusLbl.setTextFill(Color.web("#409EFF"));
        });

        Button undoBtn = new Button("撤销 (Undo)");
        undoBtn.setStyle("-fx-padding: 6 15; -fx-cursor: hand;");
        undoBtn.disableProperty().bind(stateManager.canUndoProperty().not());
        undoBtn.setOnAction(e -> {
            if (stateManager.undo()) {
                statusLbl.setText("撤销成功");
                statusLbl.setTextFill(Color.web("#409EFF"));
            }
        });

        Button redoBtn = new Button("重做 (Redo)");
        redoBtn.setStyle("-fx-padding: 6 15; -fx-cursor: hand;");
        redoBtn.disableProperty().bind(stateManager.canRedoProperty().not());
        redoBtn.setOnAction(e -> {
            if (stateManager.redo()) {
                statusLbl.setText("重做成功");
                statusLbl.setTextFill(Color.web("#409EFF"));
            }
        });

        Button diffBtn = new Button("查看差异");
        diffBtn.setStyle("-fx-padding: 6 15; -fx-cursor: hand;");
        diffBtn.setOnAction(e -> {
            var diff = stateManager.diffWithPersisted();
            if (diff.isEmpty()) {
                statusLbl.setText("与持久化值无差异");
                statusLbl.setTextFill(Color.web("#67C23A"));
            } else {
                StringBuilder sb = new StringBuilder("差异: ");
                diff.forEach((field, vals) ->
                        sb.append(field).append("[").append(vals[1]).append("->").append(vals[0]).append("] "));
                statusLbl.setText(sb.toString());
                statusLbl.setTextFill(Color.web("#E6A23C"));
            }
        });

        HBox btnRow1 = new HBox(10, persistBtn, rollbackBtn, snapshotBtn);
        HBox btnRow2 = new HBox(10, undoBtn, redoBtn, diffBtn);
        btnRow1.setPadding(new Insets(0, 0, 0, 112));
        btnRow2.setPadding(new Insets(0, 0, 0, 112));

        container.getChildren().addAll(form.getNode(), statePanel, btnRow1, btnRow2, statusLbl);
        return container;
    }

    // ================================================================
    // 8. 分组布局演示
    // ================================================================

    private Node createGroupLayoutDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("分组布局"));

        Label desc = new Label("参考 FormsFX 的 Group/Section 语义分组功能，\n"
                + "将表单项组织为带标题的分组和区域，支持折叠展开。");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        // 数据模型
        FormModel model = new FormModel()
                .field("name", "")
                .field("age", "")
                .field("email", "")
                .field("phone", "")
                .field("address", "")
                .field("zipcode", "")
                .field("company", "")
                .field("position", "")
                .field("salary", 50.0);

        // 验证规则
        Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
        rules.put("name", Collections.singletonList(FormValidationRule.required("请输入姓名")));
        rules.put("email", Collections.singletonList(FormValidationRule.email("请输入正确的邮箱")));
        rules.put("phone", Collections.singletonList(FormValidationRule.phone("请输入正确的手机号")));

        // 控件
        TextField nameField = new TextField();
        nameField.setPromptText("请输入姓名");
        TextField ageField = new TextField();
        ageField.setPromptText("请输入年龄");

        TextField emailField = new TextField();
        emailField.setPromptText("请输入邮箱");
        TextField phoneField = new TextField();
        phoneField.setPromptText("请输入手机号");

        TextField addressField = new TextField();
        addressField.setPromptText("请输入详细地址");
        TextField zipcodeField = new TextField();
        zipcodeField.setPromptText("请输入邮政编码");

        TextField companyField = new TextField();
        companyField.setPromptText("请输入公司名称");
        TextField positionField = new TextField();
        positionField.setPromptText("请输入职位");

        Slider salarySlider = new Slider(0, 100, 50);
        salarySlider.setShowTickLabels(true);
        salarySlider.setShowTickMarks(true);
        salarySlider.setMajorTickUnit(25);
        salarySlider.setBlockIncrement(5);

        // 构建分组
        FormGroup basicGroup = FormGroup.of("基本信息",
                new FormItem("姓名", "name", nameField),
                new FormItem("年龄", "age", ageField)
        ).description("请填写个人基本信息");

        FormGroup contactGroup = FormGroup.of("联系方式",
                new FormItem("邮箱", "email", emailField),
                new FormItem("电话", "phone", phoneField)
        );

        FormGroup addressGroup = FormGroup.of("地址信息",
                new FormItem("详细地址", "address", addressField),
                new FormItem("邮政编码", "zipcode", zipcodeField)
        ).collapsible(true);

        FormGroup workGroup = FormGroup.of("工作信息",
                new FormItem("公司", "company", companyField),
                new FormItem("职位", "position", positionField),
                new FormItem("期望薪资(万)", "salary", salarySlider)
                        .tooltip("拖动滑块选择期望薪资范围")
        ).collapsible(true).collapsed(true);

        // 构建区域
        FormSection personalSection = FormSection.of("个人信息", basicGroup, contactGroup);
        FormSection otherSection = FormSection.of("其他信息", addressGroup, workGroup);

        // 构建表单
        Form form = new Form()
                .model(model)
                .rules(rules)
                .labelWidth(110);

        form.addSection(personalSection);
        form.addSection(otherSection);

        // 操作按钮
        Label statusLbl = statusLabel();

        Button submitBtn = new Button("提交");
        submitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            form.validate(valid -> {
                if (valid) {
                    statusLbl.setText("验证通过! 数据: " + model.toMap());
                    statusLbl.setTextFill(Color.web("#67C23A"));
                } else {
                    statusLbl.setText("验证失败，请检查表单");
                    statusLbl.setTextFill(Color.web("#F56C6C"));
                }
            });
        });

        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-padding: 6 20;");
        resetBtn.setOnAction(e -> {
            form.resetFields();
            statusLbl.setText("已重置");
            statusLbl.setTextFill(Color.web("#909399"));
        });

        HBox btnRow = new HBox(10, submitBtn, resetBtn);
        btnRow.setPadding(new Insets(5, 0, 0, 0));

        container.getChildren().addAll(form.getNode(), btnRow, statusLbl);
        return container;
    }

    // ================================================================
    // 9. 计算属性 & Watch & 批量更新
    // ================================================================

    private Node createComputedPropertyDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("计算属性 & Watch & 批量更新"));

        Label desc = new Label("参考 Vue computed / watch 机制：\n"
                + "1. 计算属性自动依赖追踪和更新  2. Watch 监听字段变化  3. 批量更新抑制中间通知");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        // 数据模型
        FormModel model = new FormModel()
                .field("firstName", "")
                .field("lastName", "")
                .field("price", "0")
                .field("quantity", "0");

        // 计算属性：fullName 依赖 firstName + lastName
        model.computed("fullName", m ->
                m.getString("firstName") + " " + m.getString("lastName"), "firstName", "lastName");

        // 计算属性：total 依赖 price * quantity
        model.computed("total", m -> {
            double p = m.getDouble("price", 0);
            double q = m.getDouble("quantity", 0);
            return String.format("%.2f", p * q);
        }, "price", "quantity");

        // 控件
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("名");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("姓");
        TextField fullNameField = new TextField();
        fullNameField.setEditable(false);
        fullNameField.setStyle("-fx-background-color: #F5F7FA;");

        TextField priceField = new TextField();
        priceField.setPromptText("单价");
        TextField quantityField = new TextField();
        quantityField.setPromptText("数量");
        TextField totalField = new TextField();
        totalField.setEditable(false);
        totalField.setStyle("-fx-background-color: #F5F7FA;");

        Form form = new Form().model(model).labelWidth(100);
        form.addItems(
                new FormItem("名", "firstName", firstNameField),
                new FormItem("姓", "lastName", lastNameField),
                new FormItem("全名(计算)", "fullName", fullNameField).editable(false),
                new FormItem("单价", "price", priceField),
                new FormItem("数量", "quantity", quantityField),
                new FormItem("总价(计算)", "total", totalField).editable(false)
        );

        // Watch 日志区域
        TextArea watchLog = new TextArea();
        watchLog.setEditable(false);
        watchLog.setPrefRowCount(5);
        watchLog.setStyle("-fx-font-size: 12px;");

        model.watch("firstName", (oldV, newV) ->
                watchLog.appendText("[Watch] firstName: " + oldV + " → " + newV + "\n"));
        model.watch("lastName", (oldV, newV) ->
                watchLog.appendText("[Watch] lastName: " + oldV + " → " + newV + "\n"));
        model.watch("total", (oldV, newV) ->
                watchLog.appendText("[Watch] total 重新计算: " + newV + "\n"));

        Label watchTitle = new Label("Watch 监听日志:");
        watchTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        // 批量更新按钮
        Label statusLbl = statusLabel();
        Button batchBtn = new Button("批量更新 (batchUpdate)");
        batchBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        batchBtn.setOnAction(e -> {
            watchLog.appendText("--- batchUpdate 开始 ---\n");
            model.batchUpdate(() -> {
                model.setFieldValue("firstName", "张");
                model.setFieldValue("lastName", "三");
                model.setFieldValue("price", "99.5");
                model.setFieldValue("quantity", "3");
            });
            watchLog.appendText("--- batchUpdate 结束（通知已合并触发） ---\n");
            statusLbl.setText("批量更新完成: " + model.toMap());
            statusLbl.setTextFill(Color.web("#67C23A"));
        });

        Button clearLogBtn = new Button("清除日志");
        clearLogBtn.setOnAction(e -> watchLog.clear());
        HBox btnRow = new HBox(10, batchBtn, clearLogBtn);
        btnRow.setPadding(new Insets(0, 0, 0, 112));

        container.getChildren().addAll(form.getNode(), watchTitle, watchLog, btnRow, statusLbl);
        return container;
    }

    // ================================================================
    // 10. 高级验证
    // ================================================================

    private Node createAdvancedValidationDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("高级验证"));

        Label desc = new Label("跨字段验证(密码确认)、条件验证(when)、枚举白名单、警告级别验证等");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        FormModel model = new FormModel()
                .field("password", "")
                .field("confirmPassword", "")
                .field("needDelivery", false)
                .field("address", "")
                .field("level", "")
                .field("nickname", "");

        // 验证规则
        Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
        rules.put("password", Arrays.asList(
                FormValidationRule.required("请输入密码"),
                FormValidationRule.length(6, 20, "密码长度 6-20 位")
        ));
        // 跨字段验证 - 确认密码必须与密码一致
        rules.put("confirmPassword", Arrays.asList(
                FormValidationRule.required("请确认密码"),
                FormValidationRule.equalTo("password", "两次密码输入不一致")
        ));
        // 条件验证 - 仅当勾选配送时验证地址
        rules.put("address", Collections.singletonList(
                FormValidationRule.builder()
                        .required(true)
                        .message("请输入配送地址")
                        .when(m -> m.getBoolean("needDelivery", false))
                        .build()
        ));
        // 枚举白名单
        rules.put("level", Collections.singletonList(
                FormValidationRule.enumValues(
                        Arrays.asList("初级", "中级", "高级"),
                        "请选择有效等级")
        ));
        // 警告级别（不阻止提交）
        rules.put("nickname", Collections.singletonList(
                FormValidationRule.builder()
                        .validator((r, v) -> {
                            String s = v != null ? v.toString() : "";
                            return s.length() < 3 ? "建议昵称至少3个字符" : null;
                        })
                        .warningOnly(true)
                        .message("建议昵称至少3个字符")
                        .build()
        ));

        PasswordField pwdField = new PasswordField();
        pwdField.setPromptText("请输入密码");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("请再次输入密码");
        CheckBox deliveryBox = new CheckBox("需要配送");
        TextField addrField = new TextField();
        addrField.setPromptText("请输入配送地址");
        ComboBox<String> levelBox = new ComboBox<>();
        levelBox.getItems().addAll("初级", "中级", "高级", "无效选项");
        levelBox.setPromptText("选择等级");
        levelBox.setPrefWidth(250);
        TextField nicknameField = new TextField();
        nicknameField.setPromptText("请输入昵称(可选)");

        Form form = new Form().model(model).rules(rules).labelWidth(120);
        form.addItems(
                new FormItem("密码", "password", pwdField),
                new FormItem("确认密码", "confirmPassword", confirmField),
                new FormItem("需要配送", "needDelivery", deliveryBox),
                new FormItem("配送地址", "address", addrField)
                        .description("仅当勾选'需要配送'时此项为必填"),
                new FormItem("等级", "level", levelBox)
                        .description("只允许 初级/中级/高级"),
                new FormItem("昵称", "nickname", nicknameField)
                        .description("此字段仅为警告级别，不阻止提交")
        );

        Label statusLbl = statusLabel();
        Button submitBtn = new Button("提交");
        submitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> form.validate(valid -> {
            statusLbl.setText(valid
                    ? "验证通过! " + model.toMap()
                    : "验证失败，请检查表单");
            statusLbl.setTextFill(valid ? Color.web("#67C23A") : Color.web("#F56C6C"));
        }));

        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-padding: 6 20;");
        resetBtn.setOnAction(e -> { form.resetFields(); statusLbl.setText("已重置"); });

        HBox btnRow = new HBox(10, submitBtn, resetBtn);
        btnRow.setPadding(new Insets(0, 0, 0, 132));
        container.getChildren().addAll(form.getNode(), btnRow, statusLbl);
        return container;
    }

    // ================================================================
    // 11. 栅格布局
    // ================================================================

    private Node createGridLayoutDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("栅格布局"));

        Label desc = new Label("参考 Element UI 的栅格系统，使用 columns 控制列数，span 控制每项的占列宽度。");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        // 列数控制
        HBox colPanel = new HBox(10);
        colPanel.setAlignment(Pos.CENTER_LEFT);
        Label colLabel = new Label("列数:");
        Spinner<Integer> colSpinner = new Spinner<>(1, 4, 2);
        colSpinner.setPrefWidth(80);
        Label gutterLabel = new Label("间距:");
        Spinner<Integer> gutterSpinner = new Spinner<>(0, 40, 16, 4);
        gutterSpinner.setPrefWidth(80);
        colPanel.getChildren().addAll(colLabel, colSpinner, gutterLabel, gutterSpinner);
        container.getChildren().add(colPanel);

        FormModel model = new FormModel()
                .field("name", "").field("age", "").field("email", "")
                .field("phone", "").field("city", "").field("zip", "");

        Form form = new Form().model(model).columns(2).gutter(16).labelWidth(80);

        form.addItems(
                new FormItem("姓名", "name", newTextField("请输入姓名")).span(12),
                new FormItem("年龄", "age", newTextField("请输入年龄")).span(12),
                new FormItem("邮箱", "email", newTextField("请输入邮箱")).span(12),
                new FormItem("电话", "phone", newTextField("请输入电话")).span(12),
                new FormItem("城市", "city", newTextField("请输入城市")).span(12),
                new FormItem("邮编", "zip", newTextField("请输入邮编")).span(12)
        );

        colSpinner.valueProperty().addListener((obs, o, n) -> form.columns(n));
        gutterSpinner.valueProperty().addListener((obs, o, n) -> form.gutter(n));

        Label statusLbl = statusLabel();
        Button submitBtn = new Button("提交");
        submitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            statusLbl.setText("数据: " + model.toMap());
            statusLbl.setTextFill(Color.web("#67C23A"));
        });

        HBox btnRow = new HBox(10, submitBtn);
        btnRow.setPadding(new Insets(5, 0, 0, 0));
        container.getChildren().addAll(form.getNode(), btnRow, statusLbl);
        return container;
    }

    // ================================================================
    // 12. 主题切换
    // ================================================================

    private Node createThemeSwitchDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("主题切换"));

        Label desc = new Label("提供 6 种预定义主题 (Element UI / Ant Design / Naive UI 风格)，支持动态实时切换。");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        FormModel model = new FormModel()
                .field("username", "").field("email", "").field("bio", "");

        Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
        rules.put("username", Collections.singletonList(FormValidationRule.required("请输入用户名")));
        rules.put("email", Collections.singletonList(FormValidationRule.email("请输入正确的邮箱")));

        Form form = new Form().model(model).rules(rules).labelWidth(100).theme(FormTheme.DEFAULT);

        TextField userField = new TextField(); userField.setPromptText("请输入用户名");
        TextField emailField = new TextField(); emailField.setPromptText("请输入邮箱");
        TextArea bioArea = new TextArea(); bioArea.setPromptText("请输入简介"); bioArea.setPrefRowCount(3);

        form.addItems(
                new FormItem("用户名", "username", userField),
                new FormItem("邮箱", "email", emailField),
                new FormItem("简介", "bio", bioArea)
        );

        // 主题选择
        HBox themePanel = new HBox(10);
        themePanel.setAlignment(Pos.CENTER_LEFT);
        themePanel.setPadding(new Insets(5, 0, 5, 0));
        Label themeLabel = new Label("选择主题:");
        themeLabel.setFont(Font.font(13));

        Map<String, FormTheme> themeMap = new LinkedHashMap<>();
        themeMap.put("Element UI", FormTheme.DEFAULT);
        themeMap.put("Element UI Dark", FormTheme.DARK);
        themeMap.put("Ant Design", FormTheme.ANT_DESIGN);
        themeMap.put("Ant Design Dark", FormTheme.ANT_DESIGN_DARK);
        themeMap.put("Naive UI", FormTheme.NAIVE_UI);
        themeMap.put("Naive UI Dark", FormTheme.NAIVE_UI_DARK);

        ComboBox<String> themeBox = new ComboBox<>();
        themeBox.getItems().addAll(themeMap.keySet());
        themeBox.setValue("Element UI");
        themeBox.setPrefWidth(200);

        Label themeInfo = statusLabel();

        themeBox.valueProperty().addListener((obs, o, n) -> {
            FormTheme selected = themeMap.get(n);
            if (selected != null) {
                form.theme(selected);
                themeInfo.setText("当前主题: " + n + " | 主色: " + FormTheme.toHex(selected.getPrimaryColor())
                        + " | 圆角: " + selected.getBorderRadius() + "px");
                themeInfo.setTextFill(Color.web("#409EFF"));
            }
        });
        themePanel.getChildren().addAll(themeLabel, themeBox);

        // CSS 变量展示
        Button showCssBtn = new Button("查看 CSS 变量");
        showCssBtn.setStyle("-fx-padding: 6 15;");
        TextArea cssArea = new TextArea();
        cssArea.setEditable(false);
        cssArea.setPrefRowCount(6);
        cssArea.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;");
        cssArea.setVisible(false);
        cssArea.setManaged(false);

        showCssBtn.setOnAction(e -> {
            boolean vis = !cssArea.isVisible();
            cssArea.setVisible(vis);
            cssArea.setManaged(vis);
            if (vis) {
                FormTheme current = themeMap.getOrDefault(themeBox.getValue(), FormTheme.DEFAULT);
                StringBuilder sb = new StringBuilder();
                current.toCssVariables().forEach((k, v) -> sb.append(k).append(": ").append(v).append(";\n"));
                cssArea.setText(sb.toString());
            }
        });

        HBox actionRow = new HBox(10, showCssBtn);
        actionRow.setPadding(new Insets(0, 0, 0, 112));

        Button submitBtn = new Button("验证");
        submitBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> form.validate(valid ->
                themeInfo.setText(valid ? "验证通过!" : "验证失败")));
        HBox btnRow = new HBox(10, submitBtn);
        btnRow.setPadding(new Insets(0, 0, 0, 112));

        container.getChildren().addAll(themePanel, form.getNode(), themeInfo, actionRow, cssArea, btnRow);
        return container;
    }

    // ================================================================
    // 13. 事件系统
    // ================================================================

    private Node createEventSystemDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("事件系统"));

        Label desc = new Label("参考 Ant Design / Vue 事件机制，支持表单全生命周期事件监听：\n"
                + "FIELD_CHANGE / BEFORE_VALIDATE / AFTER_VALIDATE / BEFORE_SUBMIT / SUBMIT / RESET / THEME_CHANGE 等");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        FormModel model = new FormModel()
                .field("name", "")
                .field("email", "");

        Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
        rules.put("name", Collections.singletonList(FormValidationRule.required("请输入姓名")));
        rules.put("email", Collections.singletonList(FormValidationRule.email("请输入邮箱")));

        Form form = new Form().model(model).rules(rules).labelWidth(100)
                .onSubmit(data -> {/* handled by event */});

        form.addItems(
                new FormItem("姓名", "name", newTextField("请输入姓名")),
                new FormItem("邮箱", "email", newTextField("请输入邮箱"))
        );

        // 事件日志
        TextArea eventLog = new TextArea();
        eventLog.setEditable(false);
        eventLog.setPrefRowCount(10);
        eventLog.setStyle("-fx-font-size: 12px; -fx-font-family: monospace;");

        // 注册所有事件监听
        for (FormEvent.Type type : FormEvent.Type.values()) {
            form.on(type, event -> {
                String msg = String.format("[%s] %s", event.getType(), event);
                javafx.application.Platform.runLater(() -> eventLog.appendText(msg + "\n"));
            });
        }

        Label logTitle = new Label("事件日志 (实时):");
        logTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        // 操作按钮
        Button validateBtn = new Button("验证");
        validateBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 15; -fx-cursor: hand;");
        validateBtn.setOnAction(e -> form.validate(valid -> {}));

        Button submitBtn = new Button("提交");
        submitBtn.setStyle("-fx-background-color: #67C23A; -fx-text-fill: white; -fx-padding: 6 15; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> form.submit());

        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-padding: 6 15;");
        resetBtn.setOnAction(e -> form.resetFields());

        Button themeBtn = new Button("切换主题");
        themeBtn.setStyle("-fx-padding: 6 15;");
        themeBtn.setOnAction(e -> {
            FormTheme current = form.getTheme();
            form.theme(current == FormTheme.DEFAULT ? FormTheme.ANT_DESIGN : FormTheme.DEFAULT);
        });

        Button clearBtn = new Button("清除日志");
        clearBtn.setOnAction(e -> eventLog.clear());

        HBox btnRow = new HBox(10, validateBtn, submitBtn, resetBtn, themeBtn, clearBtn);
        btnRow.setPadding(new Insets(0, 0, 0, 112));

        container.getChildren().addAll(form.getNode(), btnRow, logTitle, eventLog);
        return container;
    }

    // ================================================================
    // 14. 插件扩展
    // ================================================================

    @SuppressWarnings("unchecked")
    private Node createPluginDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getChildren().add(sectionTitle("插件扩展 - FormFieldFactory"));

        Label desc = new Label("通过 FormFieldFactory 注册自定义字段渲染器和绑定器，\n"
                + "支持扩展任意自定义 UI 控件类型。");
        desc.setTextFill(Color.web("#909399"));
        desc.setWrapText(true);
        container.getChildren().add(desc);

        // 注册自定义评分渲染器（使用 Slider 模拟星级评分）
        FormFieldFactory.register("rating", (fieldName, config) -> {
            int max = config != null && config.containsKey("max") ? (int) config.get("max") : 5;
            Slider slider = new Slider(0, max, 0);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit(1);
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);
            slider.setBlockIncrement(1);
            slider.setPrefWidth(250);
            return slider;
        });

        // 注册带绑定的标签控件
        FormFieldFactory.registerWithBinding("tag-input",
                (fieldName, config) -> {
                    TextField tf = new TextField();
                    tf.setPromptText("输入后回车添加标签");
                    return tf;
                },
                (control, model, fieldName) -> {
                    TextField tf = (TextField) control;
                    tf.setOnAction(e -> {
                        String text = tf.getText().trim();
                        if (!text.isEmpty()) {
                            List<String> tags = (List<String>) model.getFieldValue(fieldName);
                            if (tags == null) tags = new ArrayList<>();
                            else tags = new ArrayList<>(tags);
                            tags.add(text);
                            model.setFieldValue(fieldName, tags);
                            tf.clear();
                        }
                    });
                }
        );

        // 展示已注册类型
        Label registeredLabel = new Label("已注册的自定义类型: " +
                String.join(", ", FormFieldFactory.getRegisteredTypes()));
        registeredLabel.setTextFill(Color.web("#409EFF"));
        registeredLabel.setFont(Font.font(13));
        container.getChildren().add(registeredLabel);

        // 创建表单
        FormModel model = new FormModel()
                .field("name", "")
                .field("rating", 0.0)
                .field("tags", new ArrayList<String>());

        Form form = new Form().model(model).labelWidth(100);

        TextField nameField = new TextField();
        nameField.setPromptText("请输入名称");

        // 通过工厂创建评分控件
        Node ratingNode = FormFieldFactory.create("rating", "rating",
                Collections.singletonMap("max", 5));
        Node tagInputNode = FormFieldFactory.create("tag-input", "tags", null);

        // 手动绑定 tag-input
        FormFieldFactory.FieldBinder tagBinder = FormFieldFactory.getBinder("tag-input");
        if (tagBinder != null && tagInputNode != null) {
            tagBinder.bind(tagInputNode, model, "tags");
        }

        form.addItems(
                new FormItem("名称", "name", nameField),
                new FormItem("评分", "rating", ratingNode != null ? ratingNode : new Label("渲染器未注册"))
                        .description("拖动滑块评分 0-5"),
                new FormItem("标签", "tags", tagInputNode != null ? tagInputNode : new Label("渲染器未注册"))
                        .description("输入内容后按回车添加标签")
        );

        // 状态展示
        Label statusLbl = statusLabel();
        Button showDataBtn = new Button("查看数据");
        showDataBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-padding: 6 20; -fx-cursor: hand;");
        showDataBtn.setOnAction(e -> {
            statusLbl.setText("模型数据: " + model.toMap());
            statusLbl.setTextFill(Color.web("#67C23A"));
        });

        Button unregisterBtn = new Button("注销 rating");
        unregisterBtn.setStyle("-fx-padding: 6 15;");
        unregisterBtn.setOnAction(e -> {
            FormFieldFactory.unregister("rating");
            registeredLabel.setText("已注册的自定义类型: " +
                    String.join(", ", FormFieldFactory.getRegisteredTypes()));
            statusLbl.setText("已注销 rating 渲染器");
            statusLbl.setTextFill(Color.web("#E6A23C"));
        });

        HBox btnRow = new HBox(10, showDataBtn, unregisterBtn);
        btnRow.setPadding(new Insets(0, 0, 0, 112));
        container.getChildren().addAll(form.getNode(), btnRow, statusLbl);
        return container;
    }

    // ================================================================
    // 工具方法
    // ================================================================

    /** 快速创建 TextField */
    private TextField newTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    // ================================================================
    // Main
    // ================================================================

    public static void main(String[] args) {
        launch(args);
    }
}
