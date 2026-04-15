package io.aurora.fx.components.verifyCode;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JavaFX验证码系统 - 基于VerifyPane统一接口的演示程序
 * <p>
 * 本演示程序展示如何使用VerifyPane统一接口操作三种验证码类型：
 * 1. 滑动拼图验证码 (SliderVerifyPane implements VerifyPane)
 * 2. 文字点选验证码 (TextClickVerifyPane implements VerifyPane)
 * 3. 算术验证验证码 (ArithmeticVerifyPane implements VerifyPane)
 * <p>
 * 核心演示点：
 * - 使用VerifyPane接口类型接收不同实现类（多态）
 * - 通过getRoot()获取组件根容器添加到界面
 * - 统一的方式设置回调和处理验证结果
 * - 统一调用refresh()和reset()方法
 *
 * @author JavaFX Team
 * @since 1.0.0
 */
public class VerifyPaneCodeDemo extends Application {
    private static final List<String> BACKGROUND_IMAGES = new ArrayList<>();
    //private static final List<String> BACKGROUND_IMAGES = Arrays.asList(
//        "https://i0.hdslb.com/bfs/article/4bfb3057864685e8e9ad8c056d07c2874fde1afd.jpg",
//        "https://i2.hdslb.com/bfs/archive/1a0ac15ec89f78ea76a47adff7d22e467a8ae9c8.jpg",
//        "https://i0.hdslb.com/bfs/archive/c5e8d57f5c9962dd6eb7ba49ed6cf271e5231a2a.jpg",
//        "https://pic.rmb.bdstatic.com/85fef0d4bbc654d1031bb0978f55b9fd.jpeg@s_0,w_2000"
//);
    // 状态标签
    private Label statusLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadBackgroundImages();


        primaryStage.setTitle("VerifyPane统一接口演示");

        // 创建主界面
        VBox root = createMainUI();

        Scene scene = new Scene(root, 900, 750);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 图片加载方法
     */
    private void loadBackgroundImages() {
        BACKGROUND_IMAGES.clear();

        for (int i = 1; i <= 5; i++) {
            String path = "images/bg" + i + ".png";

            URL resource = getClass().getClassLoader().getResource(path);
            if (resource != null) {
                // ✔ 推荐：直接存 classpath 相对路径
                BACKGROUND_IMAGES.add(path);
                System.out.println("已加载: " + path);
            } else {
                System.err.println("未找到: " + path);
            }
        }

        if (BACKGROUND_IMAGES.isEmpty()) {
            throw new RuntimeException("没有加载到任何图片！");
        }
    }

    /**
     * 创建主界面
     */
    private VBox createMainUI() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #f5f5f5;");

        // 标题
        Label titleLabel = new Label("VerifyPane 统一接口演示");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        // 副标题
        Label subtitleLabel = new Label("基于VerifyPane接口多态使用三种验证码组件");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", 13));
        subtitleLabel.setTextFill(Color.valueOf("#666666"));

        // 状态标签
        statusLabel = new Label("请选择下方任意验证码进行演示");
        statusLabel.setFont(Font.font("Microsoft YaHei", 12));
        statusLabel.setTextFill(Color.valueOf("#1e90ff"));
        statusLabel.setStyle("-fx-padding: 8 15; -fx-background-color: #e6f7ff; -fx-background-radius: 4;");

        // 创建选项卡面板
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 添加三种验证码演示选项卡 - 都使用VerifyPane接口
        tabPane.getTabs().addAll(
                new Tab("滑动拼图", createVerifyPaneDemo(VerifyType.SLIDER)),
                new Tab("文字点选", createVerifyPaneDemo(VerifyType.TEXT_CLICK)),
                new Tab("算术验证", createVerifyPaneDemo(VerifyType.ARITHMETIC)),
                new Tab("统一接口", createInterfaceDemo())
        );

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        container.getChildren().addAll(titleLabel, subtitleLabel, statusLabel, tabPane);

        return container;
    }

    /**
     * 创建基于VerifyPane接口的验证码演示区域
     * 这是核心演示方法：使用VerifyPane接口类型统一处理不同验证码组件
     *
     * @param type 验证码类型
     * @return 演示区域容器
     */
    private VBox createVerifyPaneDemo(VerifyType type) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(15));
        container.setAlignment(Pos.TOP_CENTER);

        // 根据类型创建说明标签
        String title = getVerifyTypeTitle(type);
        Label descLabel = createDescLabel(title);

        // 显示代码示例
        Label codeLabel = createCodeLabel(getCodeExample(type));

        // ============================================
        // 核心演示：使用VerifyPane接口创建组件
        // ============================================
        VerifyPane verifyPane = createVerifyPane(type);

        // 设置验证完成回调（统一接口方法）
        verifyPane.setOnVerifyComplete(result -> {
            handleVerifyResult(result, type.getDisplayName());
            // 验证失败时延迟刷新验证码，让用户看到错误提示
            if (!result.isSuccess()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // 延迟1.5秒，让用户看到错误提示
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    javafx.application.Platform.runLater(() -> verifyPane.refresh());
                }).start();
            }
        });

        // 设置刷新回调（统一接口方法）
        verifyPane.setOnRefresh(() -> {
            refreshVerifyPane(verifyPane, type);
        });

        // 初始化验证码数据
        refreshVerifyPane(verifyPane, type);

        // ============================================
        // 核心演示：通过getRoot()获取根容器添加到界面
        // ============================================
        Pane verifyRoot = verifyPane.getRoot();

        // 创建控制按钮区域
        HBox controlBox = createControlBox(verifyPane);

        container.getChildren().addAll(descLabel, codeLabel, verifyRoot, controlBox);
        return container;
    }

    /**
     * 创建VerifyPane接口实现实例
     * 使用工厂模式根据类型创建对应组件
     *
     * @param type 验证码类型
     * @return VerifyPane接口实例
     */
    private VerifyPane createVerifyPane(VerifyType type) {
        switch (type) {
            case SLIDER:
                // 创建滑动拼图验证码
                return new SliderVerifyPane();

            case TEXT_CLICK:
                // 创建文字点选验证码（使用自定义配置）
                VerifyConfig textConfig = VerifyConfig.createTextClick()
                        .clickTextCount(3)
                        .interferenceTextCount(5)
                        .tolerance(15)
                        .size(350, 200);
                return new TextClickVerifyPane(textConfig);

            case ARITHMETIC:
                // 创建算术验证码（使用自定义配置）
                VerifyConfig arithmeticConfig = VerifyConfig.createArithmetic()
                        .numberRange(10, 99)
                        .operators(Arrays.asList("+", "-", "×"));
                return new ArithmeticVerifyPane(arithmeticConfig);

            default:
                throw new IllegalArgumentException("不支持的验证码类型: " + type);
        }
    }

    /**
     * 刷新验证码
     * 使用VerifyPane接口统一刷新不同组件
     *
     * @param verifyPane VerifyPane接口实例
     * @param type       验证码类型
     */
    private void refreshVerifyPane(VerifyPane verifyPane, VerifyType type) {
        try {
            switch (type) {
                case SLIDER:
                    // 刷新滑块验证码
                    SliderVerifyPane sliderPane = (SliderVerifyPane) verifyPane;
                    String imagePath = BACKGROUND_IMAGES.get(
                            (int) (Math.random() * BACKGROUND_IMAGES.size())
                    );
                    VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(
                            imagePath,
                            sliderPane.getConfig()
                    );
                    sliderPane.setVerifyImage(image);
                    break;

                case TEXT_CLICK:
                    // 刷新文字点选验证码
                    TextClickVerifyPane textPane = (TextClickVerifyPane) verifyPane;
                    VerifyImageUtil.TextClickVerifyData textData =
                            VerifyImageUtil.generateTextClickVerify(textPane.getConfig());
                    textPane.setVerifyData(textData);
                    break;

                case ARITHMETIC:
                    // 刷新算术验证码
                    ArithmeticVerifyPane arithmeticPane = (ArithmeticVerifyPane) verifyPane;
                    VerifyImageUtil.ArithmeticVerifyData arithmeticData =
                            VerifyImageUtil.generateArithmeticVerify(arithmeticPane.getConfig());
                    arithmeticPane.setVerifyData(arithmeticData);
                    break;
            }
        } catch (IOException e) {
            updateStatus("刷新验证码失败: " + e.getMessage());
        }
    }

    /**
     * 创建控制按钮区域
     * 演示使用VerifyPane接口的通用方法
     *
     * @param verifyPane VerifyPane接口实例
     * @return 控制按钮容器
     */
    private HBox createControlBox(VerifyPane verifyPane) {
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10, 0, 0, 0));

        // 刷新按钮 - 调用统一接口的refresh()方法
        Button refreshBtn = new Button("刷新验证码");
        refreshBtn.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> verifyPane.refresh());

        // 重置按钮 - 调用统一接口的reset()方法
        Button resetBtn = new Button("重置状态");
        resetBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        resetBtn.setOnAction(e -> verifyPane.reset());

        // 状态显示 - 使用统一接口的getState()方法
        Label stateLabel = new Label("状态: " + verifyPane.getState().getCode());
        stateLabel.setStyle("-fx-text-fill: #666;");

        // 绑定状态属性（使用统一接口的stateProperty()）
        verifyPane.stateProperty().addListener((obs, oldVal, newVal) -> {
            stateLabel.setText("状态: " + newVal.getCode());
        });

        controlBox.getChildren().addAll(refreshBtn, resetBtn, stateLabel);
        return controlBox;
    }

    /**
     * 创建统一接口使用演示区域
     */
    private VBox createInterfaceDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("VerifyPane 统一接口核心方法");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.valueOf("#333"));

        // 接口定义
        Label interfaceLabel = createCodeLabel(
                "public interface VerifyPane {\n" +
                        "    // 获取根容器 - 用于添加到界面\n" +
                        "    Pane getRoot();\n\n" +
                        "    // 获取当前状态\n" +
                        "    VerifyState getState();\n\n" +
                        "    // 获取状态属性（支持绑定）\n" +
                        "    ObjectProperty<VerifyState> stateProperty();\n\n" +
                        "    // 设置验证完成回调\n" +
                        "    void setOnVerifyComplete(Consumer<VerifyResult> callback);\n\n" +
                        "    // 设置刷新回调\n" +
                        "    void setOnRefresh(Runnable callback);\n\n" +
                        "    // 刷新验证码\n" +
                        "    void refresh();\n\n" +
                        "    // 重置验证码状态\n" +
                        "    void reset();\n\n" +
                        "    // 获取验证码配置\n" +
                        "    VerifyConfig getConfig();\n" +
                        "}"
        );

        // 使用示例
        Label usageLabel = new Label("多态使用示例：");
        usageLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        usageLabel.setTextFill(Color.valueOf("#1e90ff"));

        Label usageCodeLabel = createCodeLabel(
                "// 1. 使用接口类型声明\n" +
                        "VerifyPane verifyPane;\n\n" +
                        "// 2. 根据条件创建不同实现\n" +
                        "if (type == VerifyType.SLIDER) {\n" +
                        "    verifyPane = new SliderVerifyPane(config);\n" +
                        "} else if (type == VerifyType.TEXT_CLICK) {\n" +
                        "    verifyPane = new TextClickVerifyPane(config);\n" +
                        "} else {\n" +
                        "    verifyPane = new ArithmeticVerifyPane(config);\n" +
                        "}\n\n" +
                        "// 3. 统一方式设置回调\n" +
                        "verifyPane.setOnVerifyComplete(result -> {\n" +
                        "    // 处理验证结果\n" +
                        "});\n\n" +
                        "// 4. 通过getRoot()获取组件添加到界面\n" +
                        "Pane root = verifyPane.getRoot();\n" +
                        "myContainer.getChildren().add(root);\n\n" +
                        "// 5. 统一方式刷新和重置\n" +
                        "verifyPane.refresh();\n" +
                        "verifyPane.reset();"
        );

        container.getChildren().addAll(
                titleLabel,
                interfaceLabel,
                usageLabel,
                usageCodeLabel
        );

        return container;
    }

    /**
     * 处理验证结果
     */
    private void handleVerifyResult(VerifyResult result, String typeName) {
        if (result.isSuccess()) {
            updateStatus(typeName + "验证成功！耗时: " + result.getDuration() + "ms");
        } else {
            String errorMsg = getErrorMessage(result.getErrorCode());
            updateStatus(typeName + "验证失败: " + errorMsg);
        }
    }

    /**
     * 获取错误消息
     */
    private String getErrorMessage(String errorCode) {
        if (errorCode == null) return "未知错误";
        switch (errorCode) {
            case "ROBOT_DETECTED":
                return "检测到异常操作";
            case "POSITION_MISMATCH":
                return "位置不匹配";
            case "WRONG_ANSWER":
                return "答案错误";
            default:
                return errorCode;
        }
    }

    /**
     * 获取验证码类型标题
     */
    private String getVerifyTypeTitle(VerifyType type) {
        switch (type) {
            case SLIDER:
                return "SliderVerifyPane - 滑动拼图验证码";
            case TEXT_CLICK:
                return "TextClickVerifyPane - 文字点选验证码";
            case ARITHMETIC:
                return "ArithmeticVerifyPane - 算术验证验证码";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取代码示例
     */
    private String getCodeExample(VerifyType type) {
        switch (type) {
            case SLIDER:
                return "// 创建滑动拼图验证码\n" +
                        "VerifyPane verifyPane = new SliderVerifyPane(config);\n" +
                        "Pane root = verifyPane.getRoot();\n" +
                        "container.getChildren().add(root);";
            case TEXT_CLICK:
                return "// 创建文字点选验证码\n" +
                        "VerifyPane verifyPane = new TextClickVerifyPane(config);\n" +
                        "Pane root = verifyPane.getRoot();\n" +
                        "container.getChildren().add(root);";
            case ARITHMETIC:
                return "// 创建算术验证码\n" +
                        "VerifyPane verifyPane = new ArithmeticVerifyPane(config);\n" +
                        "Pane root = verifyPane.getRoot();\n" +
                        "container.getChildren().add(root);";
            default:
                return "";
        }
    }

    /**
     * 更新状态标签
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * 创建说明标签
     */
    private Label createDescLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        label.setTextFill(Color.valueOf("#333"));
        return label;
    }

    /**
     * 创建代码示例标签
     */
    private Label createCodeLabel(String code) {
        Label label = new Label(code);
        label.setFont(Font.font("Consolas", 11));
        label.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12; -fx-background-radius: 4;");
        label.setWrapText(true);
        return label;
    }
}
