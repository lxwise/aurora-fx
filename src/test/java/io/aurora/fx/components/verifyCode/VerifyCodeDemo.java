package io.aurora.fx.components.verifyCode;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
 * JavaFX验证码系统完整演示程序
 * 
 * 本演示程序展示了验证码组件库的所有功能：
 * 1. 三种验证码类型的使用（滑动拼图、文字点选、算术验证）
 * 2. VerifyCodeFactory工厂类的使用
 * 3. 主题定制和配置选项
 * 4. 事件处理和结果验证
 * 5. 错误处理机制
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public class VerifyCodeDemo extends Application {

    private static final List<String> BACKGROUND_IMAGES = new ArrayList<>();

    // 主容器
    private VBox mainContainer;
    private Label statusLabel;

    /**
     * 应用程序入口
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadBackgroundImages();

        // 设置窗口标题
        primaryStage.setTitle("JavaFX验证码系统演示");

        // 创建主界面
        createMainUI();

        // 创建场景
        Scene scene = new Scene(mainContainer, 900, 800);
        
        // 设置窗口最小尺寸
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
    private void createMainUI() {
        // 主容器使用VBox垂直布局
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // 创建标题
        Label titleLabel = new Label("JavaFX验证码系统演示");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        // 创建副标题
        Label subtitleLabel = new Label("支持滑动拼图、文字点选、算术验证三种类型");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", 14));
        subtitleLabel.setTextFill(Color.valueOf("#666666"));

        // 状态标签
        statusLabel = new Label("请完成下方任意验证码进行演示");
        statusLabel.setFont(Font.font("Microsoft YaHei", 12));
        statusLabel.setTextFill(Color.valueOf("#1e90ff"));
        statusLabel.setStyle("-fx-padding: 10; -fx-background-color: #e6f7ff; -fx-background-radius: 4;");

        // 创建选项卡面板，用于展示三种验证码
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 添加三种验证码的演示选项卡
        Tab sliderTab = new Tab("滑动拼图", createSliderDemo());
        Tab textClickTab = new Tab("文字点选", createTextClickDemo());
        Tab arithmeticTab = new Tab("算术验证", createArithmeticDemo());
        Tab advancedTab = new Tab("高级配置", createAdvancedDemo());

        tabPane.getTabs().addAll(sliderTab, textClickTab, arithmeticTab, advancedTab);

        // 将所有组件添加到主容器
        mainContainer.getChildren().addAll(
                titleLabel, 
                subtitleLabel, 
                statusLabel, 
                tabPane
        );

        // 设置VBox增长优先级，让选项卡面板占据剩余空间
        VBox.setVgrow(tabPane, Priority.ALWAYS);
    }

    /**
     * 创建滑动拼图验证码演示区域
     */
    private VBox createSliderDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        // 说明标签
        Label descLabel = new Label("方式一：使用VerifyCodeFactory快速创建");
        descLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        descLabel.setTextFill(Color.valueOf("#333"));

        // 代码示例标签
        Label codeLabel = new Label(
            "SliderVerifyPane slider = VerifyCodeFactory.createSlider(backgroundImages);\n" +
            "slider.setOnVerifyComplete(result -> {\n" +
            "    if (result.isSuccess()) {\n" +
            "        // 验证成功处理\n" +
            "    }\n" +
            "});"
        );
        codeLabel.setFont(Font.font("Consolas", 11));
        codeLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 4;");

        // 使用工厂方法创建滑块验证码
        SliderVerifyPane sliderPane = VerifyCodeFactory.createSlider(BACKGROUND_IMAGES);
        
        // 设置验证完成回调
        sliderPane.setOnVerifyComplete(result -> {
            handleVerifyResult(result, "滑动拼图");
            // 验证失败时延迟刷新验证码，让用户看到错误提示
            if (!result.isSuccess()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // 延迟1.5秒，让用户看到错误提示
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    javafx.application.Platform.runLater(() -> sliderPane.refresh());
                }).start();
            }
        });

        // 设置刷新回调
        sliderPane.setOnRefresh(() -> {
            try {
                // 生成新的验证码
                VerifyImage newImage = VerifyImageUtil.generateSliderVerifyImage(
                        BACKGROUND_IMAGES.get((int) (Math.random() * BACKGROUND_IMAGES.size())),
                        sliderPane.getConfig()
                );
                sliderPane.setVerifyImage(newImage);
                updateStatus("滑动拼图验证码已刷新");
            } catch (IOException e) {
                handleError("刷新验证码失败", e);
            }
        });

        // 初始化验证码
        try {
            VerifyImage verifyImage = VerifyImageUtil.generateSliderVerifyImage(
                    BACKGROUND_IMAGES.get(0),
                    sliderPane.getConfig()
            );
            sliderPane.setVerifyImage(verifyImage);
        } catch (IOException e) {
            handleError("初始化验证码失败", e);
        }

        container.getChildren().addAll(descLabel, codeLabel, sliderPane);
        return container;
    }

    /**
     * 创建文字点选验证码演示区域
     */
    private VBox createTextClickDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        // 说明标签
        Label descLabel = new Label("方式二：使用自定义配置创建");
        descLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        descLabel.setTextFill(Color.valueOf("#333"));

        // 代码示例标签
        Label codeLabel = new Label(
            "VerifyConfig config = VerifyConfig.textClick()\n" +
            "    .clickTextCount(3)\n" +
            "    .interferenceTextCount(5)\n" +
            "    .tolerance(10);\n" +
            "TextClickVerifyPane textClick = VerifyCodeFactory.createTextClick(config);"
        );
        codeLabel.setFont(Font.font("Consolas", 11));
        codeLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 4;");

        // 创建自定义配置
        VerifyConfig config = VerifyConfig.createTextClick()
                .clickTextCount(3)           // 需要点击3个文字
                .interferenceTextCount(5)    // 5个干扰文字
                .tolerance(10)               // 容差10像素
                .size(350, 200);             // 设置尺寸

        // 使用工厂方法创建文字点选验证码
        TextClickVerifyPane textClickPane = VerifyCodeFactory.createTextClick(config);
        
        // 设置验证完成回调
        textClickPane.setOnVerifyComplete(result -> {
            handleVerifyResult(result, "文字点选");
            // 验证失败时延迟刷新验证码，让用户看到错误提示
            if (!result.isSuccess()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // 延迟1.5秒，让用户看到错误提示
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    javafx.application.Platform.runLater(() -> textClickPane.refresh());
                }).start();
            }
        });

        // 设置刷新回调
        textClickPane.setOnRefresh(() -> {
            VerifyImageUtil.TextClickVerifyData newData = 
                    VerifyImageUtil.generateTextClickVerify(textClickPane.getConfig());
            textClickPane.setVerifyData(newData);
            updateStatus("文字点选验证码已刷新");
        });

        // 初始化验证码
        VerifyImageUtil.TextClickVerifyData verifyData = 
                VerifyImageUtil.generateTextClickVerify(config);
        textClickPane.setVerifyData(verifyData);

        container.getChildren().addAll(descLabel, codeLabel, textClickPane);
        return container;
    }

    /**
     * 创建算术验证码演示区域
     */
    private VBox createArithmeticDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        // 说明标签
        Label descLabel = new Label("方式三：使用默认配置快速创建");
        descLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        descLabel.setTextFill(Color.valueOf("#333"));

        // 代码示例标签
        Label codeLabel = new Label(
            "// 使用默认配置\n" +
            "ArithmeticVerifyPane arithmetic = VerifyCodeFactory.createArithmetic();\n" +
            "\n" +
            "// 或使用自定义配置\n" +
            "VerifyConfig config = VerifyConfig.arithmetic()\n" +
            "    .numberRange(1, 100);"
        );
        codeLabel.setFont(Font.font("Consolas", 11));
        codeLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 4;");

        // 创建自定义配置 - 中等难度
        VerifyConfig config = VerifyConfig.createArithmetic()
                .numberRange(10, 99)         // 数字范围10-99
                .operators(Arrays.asList("+", "-", "×")); // 只使用加减乘

        // 使用工厂方法创建算术验证码
        ArithmeticVerifyPane arithmeticPane = VerifyCodeFactory.createArithmetic(config);
        
        // 设置验证完成回调
        arithmeticPane.setOnVerifyComplete(result -> {
            handleVerifyResult(result, "算术验证");
            // 验证失败时延迟刷新验证码，让用户看到错误提示
            if (!result.isSuccess()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // 延迟1.5秒，让用户看到错误提示
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    javafx.application.Platform.runLater(() -> arithmeticPane.refresh());
                }).start();
            }
        });

        // 设置刷新回调
        arithmeticPane.setOnRefresh(() -> {
            VerifyImageUtil.ArithmeticVerifyData newData = 
                    VerifyImageUtil.generateArithmeticVerify(arithmeticPane.getConfig());
            arithmeticPane.setVerifyData(newData);
            updateStatus("算术验证码已刷新");
        });

        // 初始化验证码
        VerifyImageUtil.ArithmeticVerifyData verifyData = 
                VerifyImageUtil.generateArithmeticVerify(config);
        arithmeticPane.setVerifyData(verifyData);

        container.getChildren().addAll(descLabel, codeLabel, arithmeticPane);
        return container;
    }

    /**
     * 创建高级配置演示区域
     */
    private VBox createAdvancedDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        // 标题
        Label titleLabel = new Label("高级配置示例");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.valueOf("#333"));

        // 1. 主题定制示例
        Label themeLabel = new Label("1. 主题定制");
        themeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        themeLabel.setTextFill(Color.valueOf("#1e90ff"));

        Label themeCodeLabel = new Label(
            "// 使用预设主题\n" +
            "VerifyConfig config = VerifyConfig.slider()\n" +
            "    .theme(VerifyTheme.DARK);  // 深色主题\n" +
            "\n" +
            "// 或自定义主题\n" +
            "VerifyTheme customTheme = VerifyTheme.builder()\n" +
            "    .primaryColor(Color.valueOf(\"#1890ff\"))\n" +
            "    .successColor(Color.valueOf(\"#52c41a\"))\n" +
            "    .borderRadius(12)\n" +
            "    .build();"
        );
        themeCodeLabel.setFont(Font.font("Consolas", 10));
        themeCodeLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 4;");

        // 2. 难度设置示例
        Label difficultyLabel = new Label("2. 难度设置");
        difficultyLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        difficultyLabel.setTextFill(Color.valueOf("#1e90ff"));

        Label difficultyCodeLabel = new Label(
            "// 简单难度\n" +
            "VerifyConfig easyConfig = VerifyConfig.slider().difficulty(1);\n" +
            "// 中等难度（默认）\n" +
            "VerifyConfig normalConfig = VerifyConfig.slider().difficulty(2);\n" +
            "// 困难难度\n" +
            "VerifyConfig hardConfig = VerifyConfig.slider().difficulty(3);"
        );
        difficultyCodeLabel.setFont(Font.font("Consolas", 10));
        difficultyCodeLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 4;");

        // 3. 快速集成示例
        Label integrateLabel = new Label("3. 快速集成到现有容器");
        integrateLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        integrateLabel.setTextFill(Color.valueOf("#1e90ff"));

        Label integrateCodeLabel = new Label(
            "VBox myContainer = new VBox();\n" +
            "VerifyCodeFactory.integrateSlider(\n" +
            "    myContainer,           // 目标容器\n" +
            "    backgroundImages,      // 背景图片列表\n" +
            "    result -> {            // 验证回调\n" +
            "        if (result.isSuccess()) {\n" +
            "            // 验证成功\n" +
            "        }\n" +
            "    }\n" +
            ");"
        );
        integrateCodeLabel.setFont(Font.font("Consolas", 10));
        integrateCodeLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 4;");

        // 4. 错误处理示例
        Label errorLabel = new Label("4. 错误处理");
        errorLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        errorLabel.setTextFill(Color.valueOf("#1e90ff"));

        Label errorCodeLabel = new Label(
            "try {\n" +
            "    VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(\n" +
            "        filePath, config\n" +
            "    );\n" +
            "} catch (VerifyException e) {\n" +
            "    // 处理验证码异常\n" +
            "    System.err.println(\"错误代码: \" + e.getErrorCode());\n" +
            "} catch (IOException e) {\n" +
            "    // 处理IO异常\n" +
            "    System.err.println(\"读取图片失败\");\n" +
            "}"
        );
        errorCodeLabel.setFont(Font.font("Consolas", 10));
        errorCodeLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 4;");

        // 5. 随机验证码类型
        Label randomLabel = new Label("5. 随机验证码类型");
        randomLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        randomLabel.setTextFill(Color.valueOf("#1e90ff"));

        Button randomButton = new Button("创建随机类型验证码");
        randomButton.setStyle(
            "-fx-background-color: #1e90ff; -fx-text-fill: white; " +
            "-fx-font-size: 14; -fx-padding: 10 20; -fx-background-radius: 4;"
        );
        randomButton.setOnAction(e -> {
            // 这里可以弹出一个新窗口展示随机验证码
            showRandomVerifyDialog();
        });

        container.getChildren().addAll(
                titleLabel,
                themeLabel, themeCodeLabel,
                difficultyLabel, difficultyCodeLabel,
                integrateLabel, integrateCodeLabel,
                errorLabel, errorCodeLabel,
                randomLabel, randomButton
        );

        return container;
    }

    /**
     * 处理验证结果
     * @param result 验证结果
     * @param type 验证码类型
     */
    private void handleVerifyResult(VerifyResult result, String type) {
        if (result.isSuccess()) {
            // 验证成功
            updateStatus(type + "验证成功！耗时: " + result.getDuration() + "ms");

            /*            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText(type + "验证成功！耗时: " + result.getDuration() + "ms");

            alert.showAndWait();*/
            // 可以在这里执行业务逻辑，比如：
            // - 提交表单
            // - 解锁功能
            // - 记录验证成功日志
            
        } else {
            // 验证失败
            String errorMsg;
            switch (result.getErrorCode()) {
                case "ROBOT_DETECTED":
                    errorMsg = "检测到异常操作，请手动验证";
                    break;
                case "POSITION_MISMATCH":
                    errorMsg = "位置不匹配，请重试";
                    break;
                case "WRONG_ANSWER":
                    errorMsg = "答案错误";
                    break;
                default:
                    errorMsg = result.getMessage();
            }
            updateStatus(type + "验证失败: " + errorMsg);
        }
    }

    /**
     * 处理错误
     * @param message 错误消息
     * @param e 异常对象
     */
    private void handleError(String message, Exception e) {
        updateStatus("错误: " + message);
        e.printStackTrace();
        
        // 显示错误对话框
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    /**
     * 更新状态标签
     * @param message 状态消息
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * 显示随机验证码对话框
     */
    private void showRandomVerifyDialog() {
        // 创建对话框
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("随机验证码");
        dialog.setHeaderText("系统随机选择了一种验证码类型");

        // 创建对话框内容
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // 使用工厂方法创建随机验证码
        Pane randomPane = VerifyCodeFactory.createRandom();
        
        // 如果是具体类型，设置回调
        if (randomPane instanceof SliderVerifyPane) {
            ((SliderVerifyPane) randomPane).setOnVerifyComplete(result -> {
                if (result.isSuccess()) {
                    dialog.setResult(null);
                    dialog.close();
                    updateStatus("随机验证码验证成功！");
                }
            });
            
            // 初始化滑块验证码
            try {
                VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(
                        BACKGROUND_IMAGES.get(0),
                        ((SliderVerifyPane) randomPane).getConfig()
                );
                ((SliderVerifyPane) randomPane).setVerifyImage(image);
            } catch (IOException e) {
                handleError("初始化失败", e);
            }
        } else if (randomPane instanceof TextClickVerifyPane) {
            ((TextClickVerifyPane) randomPane).setOnVerifyComplete(result -> {
                if (result.isSuccess()) {
                    dialog.setResult(null);
                    dialog.close();
                    updateStatus("随机验证码验证成功！");
                }
            });
            
            // 初始化文字点选验证码
            VerifyImageUtil.TextClickVerifyData data = 
                    VerifyImageUtil.generateTextClickVerify(((TextClickVerifyPane) randomPane).getConfig());
            ((TextClickVerifyPane) randomPane).setVerifyData(data);
        } else if (randomPane instanceof ArithmeticVerifyPane) {
            ((ArithmeticVerifyPane) randomPane).setOnVerifyComplete(result -> {
                if (result.isSuccess()) {
                    dialog.setResult(null);
                    dialog.close();
                    updateStatus("随机验证码验证成功！");
                }
            });
            
            // 初始化算术验证码
            VerifyImageUtil.ArithmeticVerifyData data = 
                    VerifyImageUtil.generateArithmeticVerify(((ArithmeticVerifyPane) randomPane).getConfig());
            ((ArithmeticVerifyPane) randomPane).setVerifyData(data);
        }

        content.getChildren().add(randomPane);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
}
