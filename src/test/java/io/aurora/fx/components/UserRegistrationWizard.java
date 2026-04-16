package io.aurora.fx.components;

import io.aurora.fx.components.dynamicForm.*;
import io.aurora.fx.components.steps.Step;
import io.aurora.fx.components.steps.StepStatus;
import io.aurora.fx.components.steps.Steps;
import io.aurora.fx.components.steps.StepsTheme;
import io.aurora.fx.components.verifyCode.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 用户注册向导 — 生产环境综合示例
 * <p>
 * 业务场景：多步骤用户注册流程，集成以下所有组件包：
 * <ul>
 *   <li><b>DynamicForm</b> — Form/FormItem/FormModel/FormValidator/FormValidationRule/
 *       FormValidationResult/FormStateManager/FormBindingMode/FormSize/FormLabelPosition/
 *       FormTheme/FormGroup/FormSection/FormEvent/FormFieldFactory</li>
 *   <li><b>Steps</b> — Steps/Step/StepStatus/StepsTheme</li>
 *   <li><b>Upload</b> — 头像上传（FlowPane + FileChooser + 拖拽 + 进度 + 预览）</li>
 *   <li><b>VerifyCode</b> — VerifyCodeFactory/ArithmeticVerifyPane/VerifyConfig/
 *       VerifyResult/VerifyType/VerifyTheme/VerifyPane/VerifyCodeController</li>
 * </ul>
 *
 * <h3>流程</h3>
 * <pre>
 * Step 1 — 账户信息：用户名/邮箱/密码/确认密码（跨字段验证、去抖、计算属性）
 * Step 2 — 个人资料：姓名/年龄/城市/个人简介/头像上传（分组布局、栅格、条件验证）
 * Step 3 — 安全验证：算术验证码（VerifyCode 全组件集成）
 * Step 4 — 确认提交：只读摘要（状态管理、事件系统、主题切换）
 * </pre>
 *
 * @author Production Demo
 * @version 1.0
 */
public class UserRegistrationWizard extends Application {

    private static final Logger LOGGER = Logger.getLogger(UserRegistrationWizard.class.getName());

    // ==================== 核心组件 ====================

    /** Steps 步骤条 */
    private Steps steps;

    /** 步骤内容面板（StackPane 切换） */
    private StackPane contentPane;

    /** 各步骤内容节点 */
    private Node[] stepContents;

    /** Form 组件（Step 1/2 共享同一 FormModel） */
    private Form accountForm;
    private Form profileForm;

    /** 数据模型 — 贯穿整个注册流程 */
    private FormModel registrationModel;

    /** 状态管理器 */
    private FormStateManager stateManager;

    /** 验证码通过标记 */
    private final BooleanProperty captchaVerified = new SimpleBooleanProperty(false);

    /** 验证码控制器 */
    private VerifyCodeController verifyController;

    /** 上传文件列表 */
    private final List<File> avatarFiles = new CopyOnWriteArrayList<>();
    private FlowPane avatarPane;

    /** 上传线程池 */
    private final ExecutorService uploadExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "avatar-upload");
        t.setDaemon(true);
        return t;
    });

    /** 导航按钮 */
    private Button prevBtn, nextBtn;

    /** 当前主题索引 */
    private int currentThemeIndex = 0;
    private static final FormTheme[] FORM_THEMES = {
            FormTheme.DEFAULT, FormTheme.ANT_DESIGN, FormTheme.NAIVE_UI,
            FormTheme.DARK, FormTheme.ANT_DESIGN_DARK, FormTheme.NAIVE_UI_DARK
    };
    private static final StepsTheme[] STEPS_THEMES = {
            StepsTheme.DEFAULT, StepsTheme.BLUE, StepsTheme.GREEN, StepsTheme.DARK,
            StepsTheme.DEFAULT, StepsTheme.BLUE
    };

    /** 已释放标记 */
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    // ==================== Application 入口 ====================

    @Override
    public void start(Stage primaryStage) {
        try {
            initModel();
            registerCustomFields();

            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));
            root.setStyle("-fx-background-color: #F5F7FA;");

            // 顶部：步骤条
            root.setTop(buildStepsBar());

            // 中央：步骤内容
            contentPane = new StackPane();
            contentPane.setPadding(new Insets(15, 0, 10, 0));
            stepContents = new Node[]{
                    buildStep1_AccountInfo(),
                    buildStep2_ProfileDetails(),
                    buildStep3_SecurityVerify(),
                    buildStep4_ReviewSubmit()
            };
            contentPane.getChildren().add(stepContents[0]);
            root.setCenter(contentPane);

            // 底部：导航按钮 + 状态栏
            root.setBottom(buildBottomBar());

            Scene scene = new Scene(root, 900, 900);
            primaryStage.setTitle("用户注册向导 — JavaFX 组件综合演示");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> dispose());
            primaryStage.show();

            LOGGER.info("用户注册向导启动完成");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "启动失败", e);
            showError("启动失败: " + e.getMessage());
        }
    }

    // ==================== 模型初始化 ====================

    /**
     * 初始化数据模型 — 包含所有字段、计算属性、Watch 监听器
     */
    private void initModel() {
        registrationModel = new FormModel()
                // Step 1 — 账户信息
                .field("username", "")
                .field("email", "")
                .field("password", "")
                .field("confirmPassword", "")
                // Step 2 — 个人资料
                .field("firstName", "")
                .field("lastName", "")
                .field("age", 18)
                .field("city", "北京")
                .field("bio", "")
                .field("needNewsletter", false)
                .field("newsletterEmail", "")
                .field("avatarPath", "")
                // 元数据
                .field("registrationTime", "")
                .field("termsAccepted", false);

        // 计算属性：fullName 依赖 firstName + lastName
        registrationModel.computed("fullName", m ->
                        m.getString("firstName") + " " + m.getString("lastName"),
                "firstName", "lastName"
        );

        // 计算属性：密码强度
        registrationModel.computed("passwordStrength", m -> {
            String pwd = m.getString("password");
            if (pwd.length() < 6) return "弱";
            boolean hasUpper = pwd.chars().anyMatch(Character::isUpperCase);
            boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);
            boolean hasSpecial = pwd.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
            int score = (hasUpper ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
            return score >= 2 ? "强" : "中";
        }, "password");

        // Watch 监听器：当 email 变化时同步到 newsletterEmail
        registrationModel.watch("email", (oldVal, newVal) -> {
            if (newVal != null && registrationModel.getBoolean("needNewsletter", false)) {
                registrationModel.setFieldValue("newsletterEmail", newVal.toString());
            }
        });

        // 全局字段变更回调（日志）
        registrationModel.onAnyFieldChange((fieldName, oldVal, newVal) ->
                LOGGER.fine(() -> String.format("字段变更 [%s]: %s → %s", fieldName, oldVal, newVal))
        );

        // 状态管理器 — 50 级撤销
        stateManager = new FormStateManager(registrationModel, 50);
        stateManager.onPersist(data ->
                LOGGER.info("数据已持久化, 字段数: " + data.size())
        );

        LOGGER.info("数据模型初始化完成, 字段数: " + registrationModel.getFieldCount());
    }

    /**
     * 注册自定义字段渲染器（FormFieldFactory 插件机制演示）
     */
    private void registerCustomFields() {
        // 注册 "city-selector" 类型
        FormFieldFactory.registerWithBinding("city-selector",
                (fieldName, config) -> {
                    ComboBox<String> combo = new ComboBox<>(
                            FXCollections.observableArrayList("北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "南京")
                    );
                    combo.setPromptText("选择城市");
                    combo.setPrefWidth(200);
                    return combo;
                },
                (control, model, fieldName) -> {
                    @SuppressWarnings("unchecked")
                    ComboBox<String> combo = (ComboBox<String>) control;
                    Object currentVal = model.getFieldValue(fieldName);
                    if (currentVal != null) combo.setValue(currentVal.toString());
                    combo.valueProperty().addListener((obs, o, n) -> model.setFieldValue(fieldName, n));
                }
        );

        // 注册 "password-strength" 只读展示器
        FormFieldFactory.register("password-strength", (fieldName, config) -> {
            Label strengthLabel = new Label("—");
            strengthLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            strengthLabel.setMinWidth(60);
            return strengthLabel;
        });

        LOGGER.info("自定义字段渲染器注册完成: " + FormFieldFactory.getRegisteredTypes());
    }

    // ==================== 步骤条构建 ====================

    private Node buildStepsBar() {
        steps = new Steps()
                .addStep(new Step("账户信息", "设置用户名和密码"))
                .addStep(new Step("个人资料", "完善个人信息"))
                .addStep(new Step("安全验证", "完成人机验证"))
                .addStep(new Step("确认提交", "检查并提交"))
                .active(0)
                .finishStatus(StepStatus.SUCCESS)
                .processStatus(StepStatus.PROCESS)
                .alignCenter(true)
                .theme(StepsTheme.BLUE)
                .onChange(this::onStepChanged);

        VBox wrapper = new VBox(steps.getNode());
        wrapper.setPadding(new Insets(5, 20, 5, 20));
        wrapper.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        return wrapper;
    }

    // ==================== Step 1: 账户信息 ====================

    private Node buildStep1_AccountInfo() {
        // 验证规则
        Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
        rules.put("username", Arrays.asList(
                FormValidationRule.required("请输入用户名"),
                FormValidationRule.stringLength(3, 20, "用户名长度 3-20 个字符"),
                FormValidationRule.alphaNumeric("仅允许字母和数字")
        ));
        rules.put("email", Arrays.asList(
                FormValidationRule.required("请输入邮箱"),
                FormValidationRule.email("邮箱格式不正确")
        ));
        rules.put("password", Arrays.asList(
                FormValidationRule.required("请输入密码"),
                FormValidationRule.length(8, 32, "密码长度 8-32 位")
        ));
        rules.put("confirmPassword", Arrays.asList(
                FormValidationRule.required("请确认密码"),
                FormValidationRule.equalTo("password", "两次密码不一致")
        ));

        // 控件
        TextField usernameField = new TextField();
        usernameField.setPromptText("3-20个字母或数字");

        TextField emailField = new TextField();
        emailField.setPromptText("example@email.com");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("至少8位");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("再次输入密码");

        // 密码强度指示器（通过 FormFieldFactory 创建）
        Node strengthNode = FormFieldFactory.create("password-strength", "passwordStrength", null);
        if (strengthNode instanceof Label) {
            Label strengthLabel = (Label) strengthNode;
            registrationModel.watch("passwordStrength", (oldVal, newVal) -> {
                Platform.runLater(() -> {
                    String strength = newVal != null ? newVal.toString() : "—";
                    strengthLabel.setText(strength);
                    switch (strength) {
                        case "弱": strengthLabel.setTextFill(Color.RED); break;
                        case "中": strengthLabel.setTextFill(Color.ORANGE); break;
                        case "强": strengthLabel.setTextFill(Color.GREEN); break;
                        default: strengthLabel.setTextFill(Color.GRAY);
                    }
                });
            });
        }

        // 构建 Form
        accountForm = new Form()
                .model(registrationModel)
                .rules(rules)
                .labelWidth(120)
                .labelSuffix("：")
                .labelPosition(FormLabelPosition.RIGHT)
                .size(FormSize.DEFAULT)
                .theme(FormTheme.ANT_DESIGN)
                .bindingMode(FormBindingMode.CONTINUOUS)
                .showMessage(true);

        // 事件监听
        accountForm.on(FormEvent.Type.FIELD_CHANGE, event ->
                LOGGER.fine("Step1 字段变更: " + event.getFieldName())
        );
        accountForm.on(FormEvent.Type.AFTER_VALIDATE, event -> {
            FormValidationResult result = event.getValidationResult();
            if (result != null && !result.isValid()) {
                LOGGER.info("Step1 验证失败: " + result.getTotalErrorCount() + " 项错误");
            }
        });

        FormItem usernameItem = new FormItem("用户名", "username", usernameField).required(true);
        FormItem emailItem = new FormItem("邮箱", "email", emailField).required(true);
        FormItem passwordItem = new FormItem("密码", "password", passwordField).required(true);
        FormItem confirmItem = new FormItem("确认密码", "confirmPassword", confirmField).required(true);
        FormItem strengthItem = new FormItem("密码强度", "passwordStrength", strengthNode);

        // 使用 FormGroup + FormSection 组织
        FormGroup accountGroup = FormGroup.of("登录凭证", usernameItem, emailItem)
                .description("设置您的登录账号信息");
        FormGroup securityGroup = FormGroup.of("安全设置", passwordItem, confirmItem, strengthItem)
                .description("请设置一个强密码");
        FormSection accountSection = FormSection.of("账户信息", accountGroup, securityGroup)
                .description("第一步：创建您的登录账号");

        accountForm.addSection(accountSection);

        ScrollPane scrollPane = new ScrollPane(accountForm.getNode());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return wrapStepContent(scrollPane);
    }

    // ==================== Step 2: 个人资料 ====================

    private Node buildStep2_ProfileDetails() {
        // 验证规则
        Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
        rules.put("firstName", Collections.singletonList(
                FormValidationRule.required("请输入姓")
        ));
        rules.put("lastName", Collections.singletonList(
                FormValidationRule.required("请输入名")
        ));
        rules.put("age", Collections.singletonList(
                FormValidationRule.intRange(1, 120, "年龄范围 1-120")
        ));
        // 条件验证：勾选订阅时才要求填写邮箱
        rules.put("newsletterEmail", Collections.singletonList(
                FormValidationRule.builder()
                        .required(true)
                        .message("请输入订阅邮箱")
                        .when(m -> m.getBoolean("needNewsletter", false))
                        .build()
        ));
        rules.put("termsAccepted", Collections.singletonList(
                FormValidationRule.builder()
                        .required(true)
                        .message("请同意用户协议")
                        .validator((rule, value) -> {
                            if (value instanceof Boolean && (Boolean) value) return null;
                            return "请勾选同意用户协议";
                        })
                        .build()
        ));

        // 控件
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("姓");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("名");
        Spinner<Integer> ageSpinner = new Spinner<>(1, 120, 18);
        ageSpinner.setEditable(true);
        ageSpinner.setPrefWidth(120);
        TextArea bioField = new TextArea();
        bioField.setPromptText("简单介绍一下自己...");
        bioField.setPrefRowCount(3);
        CheckBox newsletterCb = new CheckBox("订阅新闻通讯");
        TextField newsletterEmailField = new TextField();
        newsletterEmailField.setPromptText("订阅邮箱");
        CheckBox termsCb = new CheckBox("我已阅读并同意《用户协议》");

        // 城市选择器（通过 FormFieldFactory 插件创建）
        Node cityNode = FormFieldFactory.create("city-selector", "city", null);
        FormFieldFactory.FieldBinder cityBinder = FormFieldFactory.getBinder("city-selector");
        if (cityBinder != null && cityNode != null) {
            cityBinder.bind(cityNode, registrationModel, "city");
        }

        // fullName 只读展示
        Label fullNameLabel = new Label();
        fullNameLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #409EFF; -fx-font-weight: bold;");
        registrationModel.watch("fullName", (oldVal, newVal) ->
                Platform.runLater(() -> fullNameLabel.setText(newVal != null ? newVal.toString() : ""))
        );

        // 头像上传区域
        Node avatarUploadArea = buildAvatarUploadArea();

        // 构建 Form — 使用栅格布局
        profileForm = new Form()
                .model(registrationModel)
                .rules(rules)
                .labelWidth(100)
                .labelSuffix("：")
                .labelPosition(FormLabelPosition.RIGHT)
                .size(FormSize.DEFAULT)
                .theme(FormTheme.ANT_DESIGN)
                .columns(2)
                .gutter(16);

        FormItem firstNameItem = new FormItem("姓", "firstName", firstNameField).required(true).span(12);
        FormItem lastNameItem = new FormItem("名", "lastName", lastNameField).required(true).span(12);
        FormItem fullNameItem = new FormItem("全名", "fullName", fullNameLabel).span(24);
        FormItem ageItem = new FormItem("年龄", "age", ageSpinner).span(12);
        FormItem cityItem = new FormItem("城市", "city", cityNode != null ? cityNode : new TextField()).span(12);
        FormItem bioItem = new FormItem("个人简介", "bio", bioField).span(24).description("选填，不超过200字");
        FormItem avatarItem = new FormItem("头像", "avatarPath", avatarUploadArea).span(24);
        FormItem newsletterItem = new FormItem("", "needNewsletter", newsletterCb).span(24);
        FormItem nlEmailItem = new FormItem("订阅邮箱", "newsletterEmail", newsletterEmailField).span(24);
        FormItem termsItem = new FormItem("", "termsAccepted", termsCb).span(24).required(true);

        // 使用 FormGroup 分组
        FormGroup basicGroup = FormGroup.of("基本信息", firstNameItem, lastNameItem, fullNameItem, ageItem, cityItem)
                .description("填写个人基本信息");
        FormGroup detailGroup = FormGroup.of("详细信息", bioItem, avatarItem)
                .description("上传头像和个人简介");
        FormGroup prefsGroup = FormGroup.of("偏好设置", newsletterItem, nlEmailItem, termsItem)
                .collapsible(true).collapsed(false)
                .description("可选配置");

        FormSection profileSection = FormSection.of("个人资料", basicGroup, detailGroup, prefsGroup)
                .description("第二步：完善您的个人资料");

        profileForm.addSection(profileSection);

        // 事件：主题变更
        profileForm.on(FormEvent.Type.THEME_CHANGE, event ->
                LOGGER.info("profileForm 主题已变更")
        );

        ScrollPane scrollPane = new ScrollPane(profileForm.getNode());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return wrapStepContent(scrollPane);
    }

    // ==================== 头像上传区域 ====================

    /**
     * 构建头像上传区域 — 对标 ImageUploaderPro 的功能
     * 支持点击选择、拖拽上传、缩略图预览、删除
     */
    private Node buildAvatarUploadArea() {
        avatarPane = new FlowPane();
        avatarPane.setHgap(10);
        avatarPane.setVgap(10);
        avatarPane.setPadding(new Insets(5));
        avatarPane.setMinHeight(100);

        // 上传按钮
        VBox uploadBtn = new VBox();
        uploadBtn.setPrefSize(80, 80);
        uploadBtn.setAlignment(Pos.CENTER);
        uploadBtn.setStyle("-fx-border-color: #d9d9d9; -fx-border-style: dashed; "
                + "-fx-border-radius: 6; -fx-cursor: hand; -fx-background-color: #fafafa;");
        Label plusLabel = new Label("+");
        plusLabel.setStyle("-fx-font-size: 24; -fx-text-fill: #909399;");
        Label hintLabel = new Label("上传");
        hintLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #909399;");
        uploadBtn.getChildren().addAll(plusLabel, hintLabel);

        uploadBtn.setOnMouseClicked(e -> {
            if (avatarFiles.size() >= 3) {
                showWarning("最多上传 3 张图片");
                return;
            }
            FileChooser chooser = new FileChooser();
            chooser.setTitle("选择头像图片");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            Stage stage = (Stage) avatarPane.getScene().getWindow();
            File file = chooser.showOpenDialog(stage);
            if (file != null) handleAvatarUpload(file);
        });

        // 拖拽支持
        avatarPane.setOnDragOver(event -> {
            if (event.getGestureSource() != avatarPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        avatarPane.setOnDragDropped(event -> {
            if (event.getDragboard().hasFiles()) {
                for (File f : event.getDragboard().getFiles()) {
                    if (f.getName().matches(".*\\.(png|jpg|jpeg|gif)$")) {
                        handleAvatarUpload(f);
                    }
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        avatarPane.getChildren().add(uploadBtn);
        return avatarPane;
    }

    /**
     * 处理头像上传
     */
    private void handleAvatarUpload(File sourceFile) {
        if (avatarFiles.size() >= 3) {
            showWarning("最多上传 3 张图片");
            return;
        }

        // 创建缩略图预览
        StackPane thumbContainer = new StackPane();
        thumbContainer.setPrefSize(80, 80);
        thumbContainer.setStyle("-fx-border-color: #d9d9d9; -fx-border-radius: 6;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(70);

        try {
            Image thumb = new Image(sourceFile.toURI().toString(), 80, 80, true, true);
            ImageView imageView = new ImageView(thumb);
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);

            // 删除按钮
            Button delBtn = new Button("×");
            delBtn.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; "
                    + "-fx-font-size: 10; -fx-padding: 0 4; -fx-background-radius: 10;");
            StackPane.setAlignment(delBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(delBtn, new Insets(2));
            delBtn.setOnAction(ev -> {
                avatarFiles.remove(sourceFile);
                avatarPane.getChildren().remove(thumbContainer);
                registrationModel.setFieldValue("avatarPath",
                        avatarFiles.isEmpty() ? "" : avatarFiles.get(0).getAbsolutePath());
            });

            thumbContainer.getChildren().addAll(imageView, delBtn, progressBar);

            // 模拟异步上传
            Task<Void> uploadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    long total = sourceFile.length();
                    long copied = 0;
                    byte[] buf = new byte[4096];
                    Path dest = Paths.get(System.getProperty("user.dir"), "uploads");
                    Files.createDirectories(dest);
                    Path target = dest.resolve(UUID.randomUUID() + "_" + sourceFile.getName());
                    try (InputStream in = new FileInputStream(sourceFile);
                         OutputStream out = Files.newOutputStream(target)) {
                        int read;
                        while ((read = in.read(buf)) != -1) {
                            out.write(buf, 0, read);
                            copied += read;
                            updateProgress(copied, total);
                            Thread.sleep(5); // 模拟网络延迟
                        }
                    }
                    return null;
                }
            };

            progressBar.progressProperty().bind(uploadTask.progressProperty());
            uploadTask.setOnSucceeded(ev -> {
                progressBar.setVisible(false);
                thumbContainer.setStyle("-fx-border-color: #67C23A; -fx-border-radius: 6;");
                avatarFiles.add(sourceFile);
                registrationModel.setFieldValue("avatarPath", sourceFile.getAbsolutePath());
                LOGGER.info("头像上传成功: " + sourceFile.getName());
            });
            uploadTask.setOnFailed(ev -> {
                progressBar.setVisible(false);
                thumbContainer.setStyle("-fx-border-color: #F56C6C; -fx-border-radius: 6;");
                LOGGER.warning("头像上传失败: " + sourceFile.getName());
            });

            // 插入到上传按钮前面
            int insertIdx = avatarPane.getChildren().size() - 1;
            avatarPane.getChildren().add(insertIdx, thumbContainer);
            uploadExecutor.submit(uploadTask);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "处理头像上传失败", e);
            showWarning("图片加载失败: " + e.getMessage());
        }
    }

    // ==================== Step 3: 安全验证 ====================

    private Node buildStep3_SecurityVerify() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));

        Label title = new Label("安全验证");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#303133"));

        Label desc = new Label("请完成以下验证以证明您不是机器人");
        desc.setTextFill(Color.web("#909399"));

        // 验证码配置
        VerifyConfig verifyConfig = VerifyConfig.arithmetic()
                .numberRange(1, 50)
                .difficulty(1)
                .theme(VerifyTheme.BLUE);

        verifyController = new VerifyCodeController(verifyConfig);
        ArithmeticVerifyPane arithmeticPane = verifyController.createArithmeticPane();

        // 生成验证数据
        try {
            VerifyImageUtil.ArithmeticVerifyData data = VerifyImageUtil.generateArithmeticVerify(verifyConfig);
            arithmeticPane.setVerifyData(data);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "生成验证码失败", e);
        }

        // 验证完成回调
        arithmeticPane.setOnVerifyComplete(result -> {
            Platform.runLater(() -> {
                captchaVerified.set(result.isSuccess());
                if (result.isSuccess()) {
                    LOGGER.info("验证码验证成功, 耗时: " + result.getDuration() + "ms");
                    showSuccess("验证成功！请点击下一步继续");
                    updateNavigationButtons();
                } else {
                    LOGGER.info("验证码验证失败: " + result.getMessage());
                }
            });
        });

        // 验证状态展示
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 13;");
        captchaVerified.addListener((obs, o, n) -> {
            if (n) {
                statusLabel.setText("✓ 验证已通过");
                statusLabel.setTextFill(Color.web("#67C23A"));
            } else {
                statusLabel.setText("✗ 请完成验证");
                statusLabel.setTextFill(Color.web("#F56C6C"));
            }
        });
        statusLabel.setText("✗ 请完成验证");
        statusLabel.setTextFill(Color.web("#F56C6C"));

        container.getChildren().addAll(title, desc, arithmeticPane, statusLabel);
        return wrapStepContent(container);
    }

    // ==================== Step 4: 确认提交 ====================

    private Node buildStep4_ReviewSubmit() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(15));

        Label title = new Label("注册信息确认");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#303133"));

        // 信息摘要表格
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(8);
        summaryGrid.setPadding(new Insets(15));
        summaryGrid.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #EBEEF5; -fx-border-radius: 8;");

        // 动态构建摘要（在显示时刷新）
        container.setUserData(summaryGrid); // 存储引用以便后续刷新

        // 主题切换按钮
        HBox themeBar = new HBox(10);
        themeBar.setAlignment(Pos.CENTER);
        Button themeBtn = new Button("切换主题");
        themeBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; "
                + "-fx-background-radius: 4; -fx-cursor: hand;");
        themeBtn.setOnAction(e -> switchTheme());

        // 脏状态指示
        Label dirtyLabel = new Label();
        dirtyLabel.setStyle("-fx-font-size: 12;");
        stateManager.dirtyProperty().addListener((obs, o, n) -> {
            dirtyLabel.setText(n ? "⚠ 有未保存的更改" : "✓ 数据已保存");
            dirtyLabel.setTextFill(n ? Color.ORANGE : Color.GREEN);
        });
        dirtyLabel.setText("✓ 数据已保存");
        dirtyLabel.setTextFill(Color.GREEN);

        // 撤销/重做
        Button undoBtn = new Button("撤销");
        undoBtn.setOnAction(e -> {
            stateManager.pushUndoSnapshot();
            if (stateManager.undo()) {
                LOGGER.info("执行撤销");
            }
        });
        Button redoBtn = new Button("重做");
        redoBtn.setOnAction(e -> {
            if (stateManager.redo()) {
                LOGGER.info("执行重做");
            }
        });

        themeBar.getChildren().addAll(themeBtn, new Separator(Orientation.VERTICAL), dirtyLabel,
                new Separator(Orientation.VERTICAL), undoBtn, redoBtn);

        // 提交按钮
        Button submitBtn = new Button("提交注册");
        submitBtn.setStyle("-fx-background-color: #67C23A; -fx-text-fill: white; "
                + "-fx-font-size: 16; -fx-padding: 10 40; -fx-background-radius: 6; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> performSubmit());

        HBox submitBar = new HBox(submitBtn);
        submitBar.setAlignment(Pos.CENTER);
        submitBar.setPadding(new Insets(10, 0, 0, 0));

        container.getChildren().addAll(title, themeBar, summaryGrid, submitBar);
        return wrapStepContent(new ScrollPane(container) {{
            setFitToWidth(true);
            setStyle("-fx-background-color: transparent;");
        }});
    }

    /**
     * 刷新确认页摘要
     */
    private void refreshSummary() {
        if (stepContents == null || stepContents.length < 4) return;
        Node step4 = stepContents[3];
        // 向下查找 VBox -> GridPane
        if (step4 instanceof VBox) {
            VBox wrapper = (VBox) step4;
            for (Node child : wrapper.getChildren()) {
                if (child instanceof ScrollPane) {
                    Node spContent = ((ScrollPane) child).getContent();
                    if (spContent instanceof VBox) {
                        VBox inner = (VBox) spContent;
                        Object userData = inner.getUserData();
                        if (userData instanceof GridPane) {
                            populateSummary((GridPane) userData);
                        }
                    }
                }
            }
        }
    }

    private void populateSummary(GridPane grid) {
        grid.getChildren().clear();
        String[][] rows = {
                {"用户名", registrationModel.getString("username")},
                {"邮箱", registrationModel.getString("email")},
                {"全名", registrationModel.getString("fullName")},
                {"年龄", String.valueOf(registrationModel.getFieldValue("age"))},
                {"城市", registrationModel.getString("city")},
                {"个人简介", registrationModel.getString("bio").isEmpty() ? "（未填写）" : registrationModel.getString("bio")},
                {"头像", avatarFiles.isEmpty() ? "（未上传）" : avatarFiles.size() + " 张已上传"},
                {"订阅通讯", registrationModel.getBoolean("needNewsletter", false) ? "是" : "否"},
                {"密码强度", registrationModel.getString("passwordStrength")},
                {"验证码", captchaVerified.get() ? "已通过" : "未验证"},
                {"用户协议", registrationModel.getBoolean("termsAccepted", false) ? "已同意" : "未同意"},
        };
        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0] + "：");
            key.setStyle("-fx-font-weight: bold; -fx-text-fill: #606266;");
            key.setMinWidth(80);
            Label val = new Label(rows[i][1]);
            val.setStyle("-fx-text-fill: #303133;");
            val.setWrapText(true);
            grid.add(key, 0, i);
            grid.add(val, 1, i);
        }
    }

    // ==================== 导航栏 ====================

    private Node buildBottomBar() {
        HBox navBar = new HBox(15);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, -1);");

        prevBtn = new Button("上一步");
        prevBtn.setStyle("-fx-background-color: #DCDFE6; -fx-text-fill: #606266; "
                + "-fx-padding: 8 24; -fx-background-radius: 4; -fx-cursor: hand;");
        prevBtn.setOnAction(e -> navigatePrev());
        prevBtn.setDisable(true);

        nextBtn = new Button("下一步");
        nextBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; "
                + "-fx-padding: 8 24; -fx-background-radius: 4; -fx-cursor: hand;");
        nextBtn.setOnAction(e -> navigateNext());

        // 步骤指示
        Label stepIndicator = new Label("步骤 1/4");
        stepIndicator.setStyle("-fx-text-fill: #909399; -fx-font-size: 13;");
        steps.activeProperty().addListener((obs, o, n) ->
                stepIndicator.setText("步骤 " + (n.intValue() + 1) + "/4")
        );

        Region spacerLeft = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        Region spacerRight = new Region();
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        navBar.getChildren().addAll(prevBtn, spacerLeft, stepIndicator, spacerRight, nextBtn);
        return navBar;
    }

    // ==================== 导航逻辑 ====================

    private void navigateNext() {
        int current = steps.getActive();
        switch (current) {
            case 0: // 验证 Step 1
                stateManager.pushUndoSnapshot();
                if (!accountForm.validate()) {
                    showWarning("请修正错误后继续");
                    return;
                }
                steps.next();
                break;
            case 1: // 验证 Step 2
                stateManager.pushUndoSnapshot();
                if (!profileForm.validate()) {
                    showWarning("请修正错误后继续");
                    return;
                }
                steps.next();
                break;
            case 2: // 验证码
                if (!captchaVerified.get()) {
                    showWarning("请先完成安全验证");
                    return;
                }
                refreshSummary();
                steps.next();
                break;
            case 3: // 提交
                performSubmit();
                break;
        }
    }

    private void navigatePrev() {
        steps.prev();
    }

    private void onStepChanged(int newActive) {
        Platform.runLater(() -> {
            contentPane.getChildren().clear();
            if (newActive >= 0 && newActive < stepContents.length) {
                contentPane.getChildren().add(stepContents[newActive]);
            }
            updateNavigationButtons();
            if (newActive == 3) {
                refreshSummary();
            }
        });
    }

    private void updateNavigationButtons() {
        int current = steps.getActive();
        prevBtn.setDisable(current == 0);
        if (current == 3) {
            nextBtn.setText("提交注册");
            nextBtn.setStyle("-fx-background-color: #67C23A; -fx-text-fill: white; "
                    + "-fx-padding: 8 24; -fx-background-radius: 4; -fx-cursor: hand;");
        } else {
            nextBtn.setText("下一步");
            nextBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; "
                    + "-fx-padding: 8 24; -fx-background-radius: 4; -fx-cursor: hand;");
        }
    }

    // ==================== 提交逻辑 ====================

    private void performSubmit() {
        // 设置注册时间
        registrationModel.setFieldValue("registrationTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 使用批量更新冻结通知
        registrationModel.batchUpdate(() -> {
            registrationModel.setFieldValue("registrationTime",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });

        // 持久化
        stateManager.persist();

        // 获取完整数据
        Map<String, Object> formData = registrationModel.toMap();
        LOGGER.info("=========== 注册提交 ===========");
        formData.forEach((k, v) -> LOGGER.info(k + " = " + v));
        LOGGER.info("================================");

        // 成功提示
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("注册成功");
        alert.setHeaderText("恭喜，注册完成！");
        StringBuilder sb = new StringBuilder();
        sb.append("用户名: ").append(registrationModel.getString("username")).append("\n");
        sb.append("邮箱: ").append(registrationModel.getString("email")).append("\n");
        sb.append("全名: ").append(registrationModel.getString("fullName")).append("\n");
        sb.append("注册时间: ").append(registrationModel.getString("registrationTime"));
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    // ==================== 主题切换 ====================

    private void switchTheme() {
        currentThemeIndex = (currentThemeIndex + 1) % FORM_THEMES.length;
        FormTheme formTheme = FORM_THEMES[currentThemeIndex];
        StepsTheme stepsTheme = STEPS_THEMES[currentThemeIndex];

        if (accountForm != null) accountForm.theme(formTheme);
        if (profileForm != null) profileForm.theme(formTheme);
        if (steps != null) steps.theme(stepsTheme);

        LOGGER.info("主题切换至索引: " + currentThemeIndex);
    }

    // ==================== 工具方法 ====================

    private Node wrapStepContent(Node content) {
        VBox wrapper = new VBox(content);
        VBox.setVgrow(content, Priority.ALWAYS);
        wrapper.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        wrapper.setPadding(new Insets(10));
        return wrapper;
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // ==================== 资源释放 ====================

    /**
     * 释放所有资源 — 遵循生产环境资源管理最佳实践
     */
    private void dispose() {
        if (disposed.getAndSet(true)) return;
        LOGGER.info("开始释放资源...");
        try {
            // 释放表单
            if (accountForm != null) accountForm.dispose();
            if (profileForm != null) profileForm.dispose();

            // 释放状态管理器
            if (stateManager != null) stateManager.dispose();

            // 释放步骤条
            if (steps != null) steps.dispose();

            // 取消去抖任务
            FormValidator.cancelAllDebounced();
            FormValidator.clearPatternCache();

            // 清理插件工厂
            FormFieldFactory.clearAll();

            // 关闭上传线程池
            uploadExecutor.shutdownNow();

            // 释放验证码资源
            VerifyStageManager.shutdown();

            LOGGER.info("所有资源释放完成");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "资源释放异常", e);
        }
    }

    @Override
    public void stop() {
        dispose();
    }

    // ==================== 启动入口 ====================

    public static void main(String[] args) {
        launch(args);
    }
}
