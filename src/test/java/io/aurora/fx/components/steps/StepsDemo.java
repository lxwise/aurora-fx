package io.aurora.fx.components.steps;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Steps 步骤条组件综合演示应用
 * <p>
 * 完整展示所有功能特性，包括：
 * <ul>
 *   <li>基础用法</li>
 *   <li>含状态的步骤条</li>
 *   <li>居中的步骤条</li>
 *   <li>带描述的步骤栏</li>
 *   <li>带图标的步骤条</li>
 *   <li>垂直的步骤条</li>
 *   <li>简洁风格的步骤条</li>
 *   <li>主题定制演示</li>
 *   <li>交互式控制演示</li>
 *   <li>业务示例：用户注册流程</li>
 * </ul>
 * </p>
 *
 * @author Steps Component
 * @version 1.0
 */
public class StepsDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F7FA;");

        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #F5F7FA;");

        // 标题
        Label titleLabel = new Label("Steps 步骤条组件演示");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.valueOf("#303133"));
        mainContainer.getChildren().add(titleLabel);

        Label subtitleLabel = new Label("对标 Element UI Steps，引导用户按照流程完成任务的分步导航条");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", 14));
        subtitleLabel.setTextFill(Color.valueOf("#909399"));
        mainContainer.getChildren().add(subtitleLabel);

        // 各个演示区域
        mainContainer.getChildren().addAll(
                buildBasicDemo(),
                buildStatusDemo(),
                buildCenterDemo(),
                buildDescriptionDemo(),
                buildIconDemo(),
                buildVerticalDemo(),
                buildSimpleDemo(),
                buildThemeDemo(),
                buildInteractiveDemo(),
                buildRegistrationDemo()
        );

        scrollPane.setContent(mainContainer);

        Scene scene = new Scene(scrollPane, 900, 750);
        primaryStage.setTitle("Steps 步骤条组件 - 综合演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 1. 基础用法
     */
    private VBox buildBasicDemo() {
        VBox card = createCard("基础用法", "简单的步骤条。设置 active 属性表明步骤的 index，从 0 开始。");

        Steps steps = new Steps()
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"))
                .active(1)
                .finishStatus(StepStatus.SUCCESS);

        Button nextBtn = new Button("Next Step");
        nextBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 4; -fx-padding: 8 20;");
        nextBtn.setOnAction(e -> {
            int next = steps.getActive() + 1;
            if (next > 2) next = 0;
            steps.setActive(next);
        });

        card.getChildren().addAll(steps.getNode(), nextBtn);
        return card;
    }

    /**
     * 2. 含状态的步骤条
     */
    private VBox buildStatusDemo() {
        VBox card = createCard("含状态的步骤条", "每一步骤显示出该步骤的状态。可以使用 title 属性设置标题。");

        Steps steps = new Steps()
                .space(200)
                .active(1)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Done"))
                .addStep(new Step("Processing"))
                .addStep(new Step("Step 3"));

        card.getChildren().add(steps.getNode());
        return card;
    }

    /**
     * 3. 居中的步骤条
     */
    private VBox buildCenterDemo() {
        VBox card = createCard("居中的步骤条", "标题和描述可以居中。设置 alignCenter 为 true。");

        Steps steps = new Steps()
                .active(2)
                .alignCenter(true)
                .addStep(new Step("Step 1", "Some description"))
                .addStep(new Step("Step 2", "Some description"))
                .addStep(new Step("Step 3", "Some description"))
                .addStep(new Step("Step 4", "Some description"));

        card.getChildren().add(steps.getNode());
        return card;
    }

    /**
     * 4. 带描述的步骤栏
     */
    private VBox buildDescriptionDemo() {
        VBox card = createCard("带描述的步骤栏", "每一步都有描述文本。");

        Steps steps = new Steps()
                .active(1)
                .addStep(new Step("Step 1", "This is a long description text for step one"))
                .addStep(new Step("Step 2", "This is a description"))
                .addStep(new Step("Step 3", "Some description"));

        card.getChildren().add(steps.getNode());
        return card;
    }

    /**
     * 5. 带图标的步骤条
     */
    private VBox buildIconDemo() {
        VBox card = createCard("带图标的步骤条", "支持通过 icon 属性或 iconSlot 设置自定义图标。");

        // 使用自定义SVG图标
        Node editIcon = createSvgIcon("M 3 17.25 V 21 h 3.75 L 17.81 9.94 l -3.75 -3.75 L 3 17.25 Z " +
                "M 20.71 7.04 c 0.39 -0.39 0.39 -1.02 0 -1.41 l -2.34 -2.34 c -0.39 -0.39 -1.02 -0.39 -1.41 0 " +
                "l -1.83 1.83 l 3.75 3.75 l 1.83 -1.83 Z", Color.valueOf("#409EFF"));
        Node uploadIcon = createSvgIcon("M 9 16 h 6 v -6 h 4 l -7 -7 -7 7 h 4 Z M 5 18 h 14 v 2 H 5 Z",
                Color.valueOf("#409EFF"));
        Node pictureIcon = createSvgIcon("M 21 19 V 5 c 0 -1.1 -0.9 -2 -2 -2 H 5 c -1.1 0 -2 0.9 -2 2 v 14 " +
                        "c 0 1.1 0.9 2 2 2 h 14 c 1.1 0 2 -0.9 2 -2 Z M 8.5 13.5 l 2.5 3.01 L 14.5 12 l 4.5 6 H 5 l 3.5 -4.5 Z",
                Color.valueOf("#409EFF"));

        Steps steps = new Steps()
                .active(1)
                .addStep(new Step("Step 1").iconSlot(editIcon))
                .addStep(new Step("Step 2").iconSlot(uploadIcon))
                .addStep(new Step("Step 3").iconSlot(pictureIcon));

        card.getChildren().add(steps.getNode());
        return card;
    }

    /**
     * 6. 垂直的步骤条
     */
    private VBox buildVerticalDemo() {
        VBox card = createCard("垂直的步骤条", "设置 direction 为 VERTICAL 即可垂直显示。");

        Steps steps = new Steps()
                .direction(Orientation.VERTICAL)
                .active(1)
                .addStep(new Step("Step 1", "这是第一步的描述信息"))
                .addStep(new Step("Step 2", "这是第二步的描述信息"))
                .addStep(new Step("Step 3", "这是第三步的描述信息"));

        steps.getNode().setMaxHeight(280);
        steps.getNode().setMinHeight(280);

        card.getChildren().add(steps.getNode());
        return card;
    }

    /**
     * 7. 简洁风格的步骤条
     */
    private VBox buildSimpleDemo() {
        VBox card = createCard("简洁风格的步骤条",
                "设置 simple 为 true 启用简洁风格。该模式下 alignCenter/description/direction/space 失效。");

        // 简洁模式 - 带图标
        Steps simple1 = new Steps()
                .simple(true)
                .active(1)
                .addStep(new Step("Step 1").iconSlot(
                        createSvgIcon("M 3 17.25 V 21 h 3.75 L 17.81 9.94 l -3.75 -3.75 L 3 17.25 Z",
                                Color.valueOf("#409EFF"))))
                .addStep(new Step("Step 2").iconSlot(
                        createSvgIcon("M 9 16 h 6 v -6 h 4 l -7 -7 -7 7 h 4 Z M 5 18 h 14 v 2 H 5 Z",
                                Color.valueOf("#409EFF"))))
                .addStep(new Step("Step 3").iconSlot(
                        createSvgIcon("M 21 19 V 5 c 0 -1.1 -0.9 -2 -2 -2 H 5 c -1.1 0 -2 0.9 -2 2 v 14",
                                Color.valueOf("#409EFF"))));

        // 简洁模式 - 带 finishStatus
        Steps simple2 = new Steps()
                .simple(true)
                .active(1)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"));

        VBox.setMargin(simple2.getNode(), new Insets(15, 0, 0, 0));
        card.getChildren().addAll(simple1.getNode(), simple2.getNode());
        return card;
    }

    /**
     * 8. 主题定制演示
     */
    private VBox buildThemeDemo() {
        VBox card = createCard("主题定制", "通过 StepsTheme 自定义颜色、字体等样式，内置 DEFAULT/DARK/BLUE/GREEN 预设主题。");

        // 深色主题
        Label darkLabel = new Label("深色主题 (DARK)");
        darkLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        darkLabel.setTextFill(Color.valueOf("#606266"));

        Steps darkSteps = new Steps()
                .theme(StepsTheme.DARK)
                .active(1)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"));
        darkSteps.getNode().setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 8;");

        // 蓝色主题
        Label blueLabel = new Label("蓝色主题 (BLUE)");
        blueLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        blueLabel.setTextFill(Color.valueOf("#606266"));

        Steps blueSteps = new Steps()
                .theme(StepsTheme.BLUE)
                .active(2)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"))
                .addStep(new Step("Step 4"));

        // 绿色主题
        Label greenLabel = new Label("绿色主题 (GREEN)");
        greenLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        greenLabel.setTextFill(Color.valueOf("#606266"));

        Steps greenSteps = new Steps()
                .theme(StepsTheme.GREEN)
                .active(1)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"));

        card.getChildren().addAll(
                darkLabel, darkSteps.getNode(),
                blueLabel, blueSteps.getNode(),
                greenLabel, greenSteps.getNode()
        );
        return card;
    }

    /**
     * 9. 交互式演示 - 动态控制
     */
    private VBox buildInteractiveDemo() {
        VBox card = createCard("交互式控制", "动态控制步骤条的各项属性，点击步骤可触发 onStepClick 事件。");

        Steps steps = new Steps()
                .active(0)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("选择产品", "浏览并选择您需要的产品"))
                .addStep(new Step("填写信息", "填写收货地址和联系方式"))
                .addStep(new Step("支付订单", "选择支付方式完成支付"))
                .addStep(new Step("完成", "订单处理完成"))
                .onChange(idx -> System.out.println("当前步骤: " + idx));

        // 步骤点击事件
        steps.onStepClick(idx -> {
            steps.setActive(idx);
            System.out.println("点击了步骤: " + idx + ", 已跳转");
        });

        // 控制面板
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        Button prevBtn = new Button("上一步");
        prevBtn.setStyle("-fx-background-color: #909399; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16;");
        prevBtn.setOnAction(e -> {
            if (!steps.prev()) {
                System.out.println("已在第一步，无法后退");
            }
        });

        Button nextBtn = new Button("下一步");
        nextBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16;");
        nextBtn.setOnAction(e -> {
            if (!steps.next()) {
                System.out.println("已在最后一步，无法前进");
            }
        });

        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-background-color: #E6A23C; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16;");
        resetBtn.setOnAction(e -> steps.goToFirst());

        // finishStatus 切换
        Label statusLabel = new Label("完成状态:");
        statusLabel.setTextFill(Color.valueOf("#606266"));
        ComboBox<StepStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(StepStatus.FINISH, StepStatus.SUCCESS, StepStatus.ERROR);
        statusCombo.setValue(StepStatus.SUCCESS);
        statusCombo.setOnAction(e -> steps.setFinishStatus(statusCombo.getValue()));

        // 居中切换
        CheckBox centerCheck = new CheckBox("居中");
        centerCheck.setOnAction(e -> steps.setAlignCenter(centerCheck.isSelected()));

        // 简洁模式
        CheckBox simpleCheck = new CheckBox("简洁模式");
        simpleCheck.setOnAction(e -> steps.setSimple(simpleCheck.isSelected()));

        // 垂直模式
        CheckBox verticalCheck = new CheckBox("垂直");
        verticalCheck.setOnAction(e -> steps.setDirection(
                verticalCheck.isSelected() ? Orientation.VERTICAL : Orientation.HORIZONTAL));

        // 状态信息显示
        Label statusInfo = new Label();
        statusInfo.setTextFill(Color.valueOf("#909399"));
        statusInfo.setFont(Font.font("Microsoft YaHei", 11));
        Runnable updateStatusInfo = () -> {
            statusInfo.setText(String.format("当前: 第%d步 / 共%d步 | %s%s",
                    steps.getActive() + 1, steps.getTotalSteps(),
                    steps.isFirst() ? "[第一步] " : "",
                    steps.isLast() ? "[最后一步]" : ""));
        };
        updateStatusInfo.run();
        steps.onChange(idx -> updateStatusInfo.run());

        VBox.setMargin(statusInfo, new Insets(10, 0, 0, 0));

        controls.getChildren().addAll(prevBtn, nextBtn, resetBtn,
                new Separator(Orientation.VERTICAL),
                statusLabel, statusCombo, centerCheck, simpleCheck, verticalCheck);

        card.getChildren().addAll(steps.getNode(), controls, statusInfo);
        return card;
    }

    /**
     * 10. 真实业务示例 - 用户注册流程
     * <p>
     * 模拟完整的用户注册业务流程，展示 Steps 组件在实际业务中的应用。
     * 包含：步骤状态管理、表单验证反馈、错误处理、进度导航等功能。
     * </p>
     */
    private VBox buildRegistrationDemo() {
        VBox card = createCard("业务示例：用户注册流程",
                "模拟真实的用户注册业务流程，展示步骤条在实际业务中的完整用法。包含表单验证、状态管理、错误处理等功能。");

        // 创建自定义主题 - 更符合业务场景的配色
        StepsTheme registrationTheme = new StepsTheme.Builder()
                .primaryColor(Color.valueOf("#667EEA"))
                .successColor(Color.valueOf("#48BB78"))
                .errorColor(Color.valueOf("#FC8181"))
                .warningColor(Color.valueOf("#F6AD55"))
                .waitColor(Color.valueOf("#CBD5E0"))
                .textColor(Color.valueOf("#2D3748"))
                .descriptionColor(Color.valueOf("#718096"))
                .titleFontSize(15)
                .iconSize(28)
                .build();

        // 创建步骤条
        Steps steps = new Steps()
                .theme(registrationTheme)
                .active(0)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("账号信息", "设置登录账号和密码"))
                .addStep(new Step("个人信息", "填写基本个人资料"))
                .addStep(new Step("手机验证", "验证手机号码"))
                .addStep(new Step("注册成功", "完成账号注册"));

        // 内容区域 - 根据步骤显示不同内容
        StackPane contentArea = new StackPane();
        contentArea.setMinHeight(200);
        contentArea.setMaxHeight(200);
        contentArea.setStyle("-fx-background-color: #F7FAFC; -fx-background-radius: 6; -fx-padding: 15;");

        // 创建表单数据模型（用于存储各步骤的输入数据）
        RegistrationFormData formData = new RegistrationFormData();

        // 步骤1：账号信息表单
        VBox step1Content = createStep1Content(formData);
        // 步骤2：个人信息表单
        VBox step2Content = createStep2Content(formData);
        // 步骤3：手机验证表单
        VBox step3Content = createStep3Content(formData);
        // 步骤4：完成页面
        VBox step4Content = createStep4Content(formData);

        contentArea.getChildren().addAll(step1Content, step2Content, step3Content, step4Content);
        step2Content.setVisible(false);
        step3Content.setVisible(false);
        step4Content.setVisible(false);

        // 步骤内容数组（必须在回调注册之前定义）
        final VBox[] stepContents = {step1Content, step2Content, step3Content, step4Content};

        // 错误提示标签
        final Label errorLabel = new Label();
        errorLabel.setTextFill(Color.valueOf("#FC8181"));
        errorLabel.setFont(Font.font("Microsoft YaHei", 11));
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        // 底部导航按钮
        HBox navigationBar = new HBox(15);
        navigationBar.setAlignment(Pos.CENTER_RIGHT);
        navigationBar.setPadding(new Insets(15, 0, 0, 0));

        Button prevBtn = new Button("上一步");
        prevBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #4A5568; " +
                "-fx-background-radius: 4; -fx-padding: 8 24; -fx-cursor: hand;");
        prevBtn.setOnAction(e -> steps.prev());

        Button nextBtn = new Button("下一步");
        nextBtn.setStyle("-fx-background-color: #667EEA; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 8 24; -fx-cursor: hand;");
        nextBtn.setOnAction(e -> {
            // 验证当前步骤
            String error = validateCurrentStep(steps.getActive(), formData, steps);
            if (error == null) {
                // 验证通过，进入下一步
                errorLabel.setVisible(false);
                steps.next();
            } else {
                // 验证失败，显示错误
                errorLabel.setText(error);
                errorLabel.setVisible(true);
            }
        });

        Button submitBtn = new Button("完成注册");
        submitBtn.setStyle("-fx-background-color: #48BB78; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 8 24; -fx-cursor: hand;");
        submitBtn.setVisible(false);
        submitBtn.setOnAction(e -> {
            // 验证手机验证步骤
            String error = validateCurrentStep(2, formData, steps);
            if (error == null) {
                errorLabel.setVisible(false);
                steps.next();
                submitBtn.setVisible(false);
                nextBtn.setVisible(false);
            } else {
                errorLabel.setText(error);
                errorLabel.setVisible(true);
            }
        });

        // 状态提示标签
        Label statusHint = new Label();
        statusHint.setTextFill(Color.valueOf("#718096"));
        statusHint.setFont(Font.font("Microsoft YaHei", 11));

        // 更新按钮状态（必须在 onChange 注册之前定义）
        Runnable updateNavigation = () -> {
            int current = steps.getActive();
            prevBtn.setDisable(current == 0);

            if (current == 3) {
                nextBtn.setVisible(false);
                submitBtn.setVisible(false);
                statusHint.setText("注册流程已完成！");
                statusHint.setTextFill(Color.valueOf("#48BB78"));
            } else if (current == 2) {
                nextBtn.setVisible(false);
                submitBtn.setVisible(true);
                statusHint.setText("验证手机后即可完成注册");
                statusHint.setTextFill(Color.valueOf("#718096"));
            } else {
                nextBtn.setVisible(true);
                submitBtn.setVisible(false);
                statusHint.setText(String.format("步骤 %d/%d：请填写%s信息",
                        current + 1, steps.getTotalSteps(),
                        steps.getSteps().get(current).getTitle()));
                statusHint.setTextFill(Color.valueOf("#718096"));
            }
        };

        // 步骤点击回调（仅在已完成步骤可跳转）
        steps.onStepClick(idx -> {
            if (idx < steps.getActive()) {
                // 切换到之前已完成的步骤
                steps.setActive(idx);
            }
        });

        // 步骤变化回调（统一处理页面切换和导航状态更新）
        steps.onChange(idx -> {
            // 1. 切换页面内容
            for (int i = 0; i < stepContents.length; i++) {
                stepContents[i].setVisible(i == idx);
            }
            // 2. 清除错误提示
            errorLabel.setVisible(false);
            errorLabel.setText("");
            // 3. 更新导航按钮状态
            updateNavigation.run();
            System.out.println("切换到步骤: " + idx);
        });

        // 初始化导航状态
        updateNavigation.run();

        navigationBar.getChildren().addAll(statusHint, new Region(), prevBtn, nextBtn, submitBtn);
        HBox.setHgrow(navigationBar.getChildren().get(1), Priority.ALWAYS);

        card.getChildren().addAll(steps.getNode(), contentArea, errorLabel, navigationBar);
        return card;
    }

    /** 注册表单数据模型 */
    private static class RegistrationFormData {
        // 步骤1：账号信息
        String username;
        String password;
        String confirmPassword;
        TextField usernameField;
        PasswordField passwordField;
        PasswordField confirmPasswordField;

        // 步骤2：个人信息
        String name;
        String email;
        String gender = "男";
        TextField nameField;
        TextField emailField;

        // 步骤3：手机验证
        String phone;
        String verifyCode;
        TextField phoneField;
        TextField codeField;

        // 当前步骤对象（用于清除错误状态）
        Step currentStep;
    }

    /** 步骤1内容：账号信息 */
    private VBox createStep1Content(RegistrationFormData formData) {
        VBox content = new VBox(12);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        Label userLabel = new Label("用户名：");
        userLabel.setTextFill(Color.valueOf("#4A5568"));
        formData.usernameField = new TextField();
        formData.usernameField.setPromptText("请输入用户名（4-16位字母数字）");
        formData.usernameField.setPrefWidth(280);
        formData.usernameField.textProperty().addListener((obs, old, val) -> {
            formData.username = val;
            // 输入变化时清除错误状态
            if (formData.currentStep != null && formData.currentStep.getStatus() == StepStatus.ERROR) {
                formData.currentStep.setStatus(null);
            }
        });

        Label pwdLabel = new Label("密码：");
        pwdLabel.setTextFill(Color.valueOf("#4A5568"));
        formData.passwordField = new PasswordField();
        formData.passwordField.setPromptText("请输入密码（6-20位）");
        formData.passwordField.setPrefWidth(280);
        formData.passwordField.textProperty().addListener((obs, old, val) -> {
            formData.password = val;
            if (formData.currentStep != null && formData.currentStep.getStatus() == StepStatus.ERROR) {
                formData.currentStep.setStatus(null);
            }
        });

        Label confirmLabel = new Label("确认密码：");
        confirmLabel.setTextFill(Color.valueOf("#4A5568"));
        formData.confirmPasswordField = new PasswordField();
        formData.confirmPasswordField.setPromptText("请再次输入密码");
        formData.confirmPasswordField.setPrefWidth(280);
        formData.confirmPasswordField.textProperty().addListener((obs, old, val) -> {
            formData.confirmPassword = val;
            if (formData.currentStep != null && formData.currentStep.getStatus() == StepStatus.ERROR) {
                formData.currentStep.setStatus(null);
            }
        });

        form.add(userLabel, 0, 0);
        form.add(formData.usernameField, 1, 0);
        form.add(pwdLabel, 0, 1);
        form.add(formData.passwordField, 1, 1);
        form.add(confirmLabel, 0, 2);
        form.add(formData.confirmPasswordField, 1, 2);

        Label hint = new Label("提示：用户名4-16位字母数字，密码6-20位");
        hint.setTextFill(Color.valueOf("#A0AEC0"));
        hint.setFont(Font.font("Microsoft YaHei", 10));

        content.getChildren().addAll(form, hint);
        return content;
    }

    /** 步骤2内容：个人信息 */
    private VBox createStep2Content(RegistrationFormData formData) {
        VBox content = new VBox(12);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        Label nameLabel = new Label("姓名：");
        nameLabel.setTextFill(Color.valueOf("#4A5568"));
        formData.nameField = new TextField();
        formData.nameField.setPromptText("请输入真实姓名");
        formData.nameField.setPrefWidth(280);
        formData.nameField.textProperty().addListener((obs, old, val) -> {
            formData.name = val;
            if (formData.currentStep != null && formData.currentStep.getStatus() == StepStatus.ERROR) {
                formData.currentStep.setStatus(null);
            }
        });

        Label emailLabel = new Label("邮箱：");
        emailLabel.setTextFill(Color.valueOf("#4A5568"));
        formData.emailField = new TextField();
        formData.emailField.setPromptText("请输入邮箱地址");
        formData.emailField.setPrefWidth(280);
        formData.emailField.textProperty().addListener((obs, old, val) -> {
            formData.email = val;
            System.out.println("Email changed to: " + val);
            if (formData.currentStep != null && formData.currentStep.getStatus() == StepStatus.ERROR) {
                formData.currentStep.setStatus(null);
            }
        });

        Label genderLabel = new Label("性别：");
        genderLabel.setTextFill(Color.valueOf("#4A5568"));
        HBox genderBox = new HBox(15);
        RadioButton maleBtn = new RadioButton("男");
        RadioButton femaleBtn = new RadioButton("女");
        ToggleGroup genderGroup = new ToggleGroup();
        maleBtn.setToggleGroup(genderGroup);
        femaleBtn.setToggleGroup(genderGroup);
        maleBtn.setSelected(true);
        maleBtn.setOnAction(e -> formData.gender = "男");
        femaleBtn.setOnAction(e -> formData.gender = "女");
        genderBox.getChildren().addAll(maleBtn, femaleBtn);

        form.add(nameLabel, 0, 0);
        form.add(formData.nameField, 1, 0);
        form.add(emailLabel, 0, 1);
        form.add(formData.emailField, 1, 1);
        form.add(genderLabel, 0, 2);
        form.add(genderBox, 1, 2);

        Label hint = new Label("提示：姓名不能为空，邮箱需为有效格式");
        hint.setTextFill(Color.valueOf("#A0AEC0"));
        hint.setFont(Font.font("Microsoft YaHei", 10));

        content.getChildren().addAll(form, hint);
        return content;
    }

    /** 步骤3内容：手机验证 */
    private VBox createStep3Content(RegistrationFormData formData) {
        VBox content = new VBox(12);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        Label phoneLabel = new Label("手机号：");
        phoneLabel.setTextFill(Color.valueOf("#4A5568"));
        HBox phoneBox = new HBox(10);
        formData.phoneField = new TextField();
        formData.phoneField.setPromptText("请输入11位手机号");
        formData.phoneField.setPrefWidth(180);
        formData.phoneField.textProperty().addListener((obs, old, val) -> {
            formData.phone = val;
            if (formData.currentStep != null && formData.currentStep.getStatus() == StepStatus.ERROR) {
                formData.currentStep.setStatus(null);
            }
        });

        Button sendCodeBtn = new Button("发送验证码");
        sendCodeBtn.setStyle("-fx-background-color: #667EEA; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 5 10;");
        phoneBox.getChildren().addAll(formData.phoneField, sendCodeBtn);

        Label codeLabel = new Label("验证码：");
        codeLabel.setTextFill(Color.valueOf("#4A5568"));
        formData.codeField = new TextField();
        formData.codeField.setPromptText("请输入6位验证码（输入123456通过）");
        formData.codeField.setPrefWidth(280);
        formData.codeField.textProperty().addListener((obs, old, val) -> {
            formData.verifyCode = val;
            if (formData.currentStep != null && formData.currentStep.getStatus() == StepStatus.ERROR) {
                formData.currentStep.setStatus(null);
            }
        });

        form.add(phoneLabel, 0, 0);
        form.add(phoneBox, 1, 0);
        form.add(codeLabel, 0, 1);
        form.add(formData.codeField, 1, 1);

        // 模拟发送验证码
        Label countdownLabel = new Label();
        countdownLabel.setTextFill(Color.valueOf("#667EEA"));

        sendCodeBtn.setOnAction(e -> {
            // 先验证手机号格式
            String phone = formData.phone;
            if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
                countdownLabel.setText("请输入正确的11位手机号！");
                countdownLabel.setTextFill(Color.valueOf("#FC8181"));
                return;
            }

            sendCodeBtn.setDisable(true);
            countdownLabel.setText("验证码已发送至您的手机（模拟：123456）");
            countdownLabel.setTextFill(Color.valueOf("#667EEA"));

            // 模拟倒计时
            new Thread(() -> {
                for (int i = 60; i >= 0; i--) {
                    final int seconds = i;
                    javafx.application.Platform.runLater(() -> {
                        if (seconds > 0) {
                            sendCodeBtn.setText(seconds + "s后重发");
                        } else {
                            sendCodeBtn.setText("发送验证码");
                            sendCodeBtn.setDisable(false);
                        }
                    });
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }).start();
        });

        Label hint = new Label("提示：手机号为11位有效号码，验证码输入 123456 可通过验证");
        hint.setTextFill(Color.valueOf("#A0AEC0"));
        hint.setFont(Font.font("Microsoft YaHei", 10));

        content.getChildren().addAll(form, countdownLabel, hint);
        return content;
    }

    /** 步骤4内容：注册成功 */
    private VBox createStep4Content(RegistrationFormData formData) {
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        // 成功图标
        Label successIcon = new Label("✓");
        successIcon.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 48));
        successIcon.setTextFill(Color.valueOf("#48BB78"));

        Label successTitle = new Label("恭喜您，注册成功！");
        successTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        successTitle.setTextFill(Color.valueOf("#2D3748"));

        // 显示注册信息摘要
        Label infoLabel = new Label();
        infoLabel.setText(String.format("用户名: %s | 姓名: %s | 手机: %s",
                formData.username != null ? formData.username : "-",
                formData.name != null ? formData.name : "-",
                formData.phone != null ? formData.phone : "-"));
        infoLabel.setTextFill(Color.valueOf("#718096"));

        Button loginBtn = new Button("立即登录");
        loginBtn.setStyle("-fx-background-color: #667EEA; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 10 30; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> {
            System.out.println("跳转到登录页面");
        });

        content.getChildren().addAll(successIcon, successTitle, infoLabel, loginBtn);
        return content;
    }

    /**
     * 验证当前步骤
     * @return 验证失败的错误信息，null表示验证通过
     */
    private String validateCurrentStep(int stepIndex, RegistrationFormData formData, Steps steps) {
        Step currentStep = steps.getSteps().get(stepIndex);
        formData.currentStep = currentStep; // 记录当前步骤，用于输入变化时清除错误状态

        switch (stepIndex) {
            case 0: // 账号信息验证
                // 用户名验证
                if (formData.username == null || formData.username.trim().isEmpty()) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "用户名不能为空";
                }
                if (!formData.username.matches("^[a-zA-Z0-9]{4,16}$")) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "用户名必须为4-16位字母或数字";
                }

                // 密码验证
                if (formData.password == null || formData.password.isEmpty()) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "密码不能为空";
                }
                if (formData.password.length() < 6 || formData.password.length() > 20) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "密码长度必须为6-20位";
                }

                // 确认密码验证
                if (formData.confirmPassword == null || formData.confirmPassword.isEmpty()) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "请确认密码";
                }
                if (!formData.password.equals(formData.confirmPassword)) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "两次输入的密码不一致";
                }

                currentStep.setStatus(null);
                return null;

            case 1: // 个人信息验证
                if (formData.name == null || formData.name.trim().isEmpty()) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "姓名不能为空";
                }
                if (formData.email == null || formData.email.trim().isEmpty()) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "邮箱不能为空";
                }
                if (!formData.email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "邮箱格式不正确";
                }

                currentStep.setStatus(null);
                return null;

            case 2: // 手机验证
                if (formData.phone == null || formData.phone.trim().isEmpty()) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "手机号不能为空";
                }
                if (!formData.phone.matches("^1[3-9]\\d{9}$")) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "手机号格式不正确（需11位有效号码）";
                }
                if (formData.verifyCode == null || formData.verifyCode.trim().isEmpty()) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "验证码不能为空";
                }
                if (!"123456".equals(formData.verifyCode)) {
                    currentStep.setStatus(StepStatus.ERROR);
                    return "验证码错误，请输入 123456";
                }

                currentStep.setStatus(null);
                return null;

            default:
                return null;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建演示卡片
     */
    private VBox createCard(String title, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#303133"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Microsoft YaHei", 12));
        descLabel.setTextFill(Color.valueOf("#909399"));
        descLabel.setWrapText(true);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #EBEEF5;");
        VBox.setMargin(separator, new Insets(5, 0, 10, 0));

        card.getChildren().addAll(titleLabel, descLabel, separator);
        return card;
    }

    /**
     * 创建简单的SVG图标
     */
    private Node createSvgIcon(String svgPath, Color color) {
        SVGPath path = new SVGPath();
        path.setContent(svgPath);
        path.setFill(color);
        path.setScaleX(0.8);
        path.setScaleY(0.8);

        StackPane pane = new StackPane(path);
        pane.setMinSize(24, 24);
        pane.setPrefSize(24, 24);
        pane.setMaxSize(24, 24);
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
