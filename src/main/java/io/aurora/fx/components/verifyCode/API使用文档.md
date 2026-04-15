# JavaFX 验证码组件库 API 使用文档

## 目录
1. [概述](#概述)
2. [快速开始](#快速开始)
3. [核心接口详解](#核心接口详解)
4. [配置类详解](#配置类详解)
5. [验证码组件详解](#验证码组件详解)
6. [工厂类详解](#工厂类详解)
7. [主题系统详解](#主题系统详解)
8. [工具类详解](#工具类详解)
9. [事件处理详解](#事件处理详解)
10. [最佳实践](#最佳实践)
11. [完整示例代码](#完整示例代码)
12. [常见问题](#常见问题)

---

## 概述

JavaFX 验证码组件库是一套完整的验证码解决方案，提供三种验证码类型，支持统一接口、主题定制、行为轨迹检测等高级功能。

### 支持的验证码类型

| 类型 | 说明 | 适用场景 |
|------|------|----------|
| **滑动拼图** | 拖动滑块完成拼图 | 高安全性场景 |
| **文字点选** | 按顺序点击指定文字 | 中等安全场景 |
| **算术验证** | 计算并输入算式结果 | 低安全场景、教育类应用 |

### 核心特性

- **统一接口设计**：所有组件实现 `VerifyPane` 接口
- **Builder 模式配置**：链式调用，简洁优雅
- **主题定制**：支持预设主题和自定义主题
- **行为轨迹检测**：防止机器人攻击
- **开箱即用**：提供工厂类快速创建

---

## 快速开始

### 环境要求

- Java 8 或更高版本
- JavaFX 8 或更高版本

### 最小示例

```java
import com.javafx.test.verifyCode.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class QuickStart extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. 创建配置（指定背景图片）
        VerifyConfig config = VerifyConfig.createSlider()
                .size(350, 200)
                .backgroundImages(Arrays.asList(
                        "D:/images/bg1.jpg"  // 替换为您的图片路径
                ));

        // 2. 创建验证码组件
        SliderVerifyPane sliderPane = new SliderVerifyPane(config);

        // 3. 设置验证完成回调
        sliderPane.setOnVerifyComplete(result -> {
            if (result.isSuccess()) {
                System.out.println("✅ 验证成功！");
            } else {
                System.out.println("❌ 验证失败：" + result.getMessage());
            }
        });

        // 4. 加载验证码图片
        try {
            VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(
                    config.getRandomBackgroundImage(),
                    config
            );
            sliderPane.setVerifyImage(image);
        } catch (IOException e) {
            System.out.println("加载验证码失败：" + e.getMessage());
        }

        // 5. 添加到界面
        VBox root = new VBox(20);
        root.getChildren().add(sliderPane);

        Scene scene = new Scene(root, 450, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("验证码演示");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### 运行效果

运行上述代码，您将看到一个滑动拼图验证码组件。拖动滑块完成拼图后，控制台会输出验证结果。

---

## 核心接口详解

### VerifyPane 接口

`VerifyPane` 是所有验证码组件的统一接口，定义了标准操作方法。

#### 接口定义

```java
public interface VerifyPane {
    
    /**
     * 获取验证码组件的根容器
     * @return JavaFX Pane对象，可直接添加到场景中
     */
    Pane getRoot();

    /**
     * 获取当前验证状态
     * @return 验证状态枚举值
     */
    VerifyState getState();

    /**
     * 获取验证状态属性（用于绑定）
     * @return 验证状态的ObjectProperty
     */
    ObjectProperty<VerifyState> stateProperty();

    /**
     * 设置验证完成回调
     * @param callback 验证完成时的回调函数
     */
    void setOnVerifyComplete(Consumer<VerifyResult> callback);

    /**
     * 设置刷新回调
     * @param callback 刷新时的回调函数
     */
    void setOnRefresh(Runnable callback);

    /**
     * 刷新验证码
     * 生成新的验证码内容
     */
    void refresh();

    /**
     * 重置验证码状态
     * 清空用户操作，恢复到初始状态
     */
    void reset();

    /**
     * 获取验证码配置
     * @return 当前配置对象
     */
    VerifyConfig getConfig();
}
```

#### 使用示例

```java
// 使用接口类型声明（推荐）
VerifyPane verifyPane = new SliderVerifyPane();

// 获取根容器添加到界面
Pane root = verifyPane.getRoot();
container.getChildren().add(root);

// 监听状态变化
verifyPane.stateProperty().addListener((obs, oldVal, newVal) -> {
    System.out.println("状态变化：" + oldVal + " -> " + newVal);
});

// 获取当前状态
VerifyState currentState = verifyPane.getState();

// 刷新验证码
verifyPane.refresh();

// 重置状态
verifyPane.reset();
```

### VerifyState 枚举

```java
public enum VerifyState {
    READY("ready"),       // 准备就绪，等待用户操作
    LOADING("loading"),   // 加载中
    VERIFYING("verifying"), // 验证中
    SUCCESS("success"),   // 验证成功
    FAIL("fail");         // 验证失败

    private final String code;
    
    // 获取状态代码
    public String getCode() { return code; }
    
    // 根据代码获取状态
    public static VerifyState fromCode(String code) { ... }
}
```

#### 使用示例

```java
// 状态判断
if (verifyPane.getState() == VerifyState.SUCCESS) {
    System.out.println("验证已通过");
}

// 获取状态代码
String code = verifyPane.getState().getCode(); // "success"

// 从代码转换状态
VerifyState state = VerifyState.fromCode("ready"); // VerifyState.READY
```

### VerifyResult 类

```java
public class VerifyResult {
    private boolean success;           // 验证是否成功
    private String message;            // 结果消息
    private long duration;             // 验证耗时（毫秒）
    private TrajectoryData trajectoryData; // 行为轨迹数据
    private VerifyType verifyType;     // 验证码类型
    private String errorCode;          // 错误代码

    // 静态工厂方法
    public static VerifyResult success() { ... }
    public static VerifyResult success(String message) { ... }
    public static VerifyResult fail(String message) { ... }
    public static VerifyResult fail(String message, String errorCode) { ... }

    // Getter/Setter
    public boolean isSuccess() { ... }
    public String getMessage() { ... }
    public long getDuration() { ... }
    public String getErrorCode() { ... }
    ...
}
```

#### 使用示例

```java
verifyPane.setOnVerifyComplete(result -> {
    // 判断是否成功
    if (result.isSuccess()) {
        System.out.println("✅ 验证成功！");
        System.out.println("耗时：" + result.getDuration() + "ms");
    } else {
        System.out.println("❌ 验证失败");
        System.out.println("错误信息：" + result.getMessage());
        System.out.println("错误码：" + result.getErrorCode());
    }
});
```

### VerifyType 枚举

```java
public enum VerifyType {
    SLIDER("滑动拼图验证", "slider"),
    TEXT_CLICK("文字点选验证", "text_click"),
    ARITHMETIC("算术验证码", "arithmetic"),
    MIXED("混合验证", "mixed");

    private final String displayName;
    private final String code;

    public String getDisplayName() { ... }
    public String getCode() { ... }
}
```

---

## 配置类详解

### VerifyConfig 类

`VerifyConfig` 是验证码配置类，使用 Builder 模式支持链式调用。

#### 通用配置

```java
public class VerifyConfig {
    // 验证码类型
    private VerifyType verifyType = VerifyType.SLIDER;
    
    // 难度级别 (1-简单, 2-中等, 3-困难)
    private int difficulty = 1;
    
    // 验证容差值（像素）
    private int tolerance = 8;
    
    // 是否启用行为轨迹检测
    private boolean enableBehaviorTracking = true;
    
    // 背景图片路径列表
    private List<String> backgroundImages;
    
    // 主题配置
    private VerifyTheme theme = VerifyTheme.DEFAULT;
    
    // 语言区域
    private Locale locale = Locale.getDefault();
}
```

#### 滑动拼图专用配置

```java
// 背景图尺寸
private int srcWidth = 350;
private int srcHeight = 200;

// 滑块尺寸
private int sliderWidth = 50;
private int sliderHeight = 50;

// 滑块凸起圆半径
private int circleRadius = 5;

// 滑块内边距
private int rectanglePadding = 8;
```

#### 文字点选专用配置

```java
// 需要点击的文字数量
private int clickTextCount = 3;

// 干扰文字数量
private int interferenceTextCount = 5;

// 文字大小范围 [min, max]
private int[] fontSizeRange = {16, 24};

// 文字颜色
private Color textColor = Color.BLACK;

// 提示文字列表
private List<String> textPool = Arrays.asList(
    "春", "夏", "秋", "冬", "风", "花", "雪", "月",
    "山", "水", "云", "天", "地", "人", "和", "美"
);
```

#### 算术验证码专用配置

```java
// 算术运算符
private List<String> operators = Arrays.asList("+", "-", "×");

// 数字范围 [min, max]
private int[] numberRange = {1, 50};

// 是否允许负数结果
private boolean allowNegativeResult = false;
```

### 配置方法列表

#### 静态工厂方法

```java
// 创建滑块验证码配置
VerifyConfig sliderConfig = VerifyConfig.createSlider();

// 创建文字点选验证码配置
VerifyConfig textConfig = VerifyConfig.createTextClick();

// 创建算术验证码配置
VerifyConfig arithmeticConfig = VerifyConfig.createArithmetic();

// 创建混合验证码配置
VerifyConfig mixedConfig = VerifyConfig.createMixed();

// 创建指定类型的默认配置
VerifyConfig config = VerifyConfig.createDefault(VerifyType.SLIDER);
```

#### Builder 模式方法

```java
// 通用配置
VerifyConfig config = new VerifyConfig()
    .verifyType(VerifyType.SLIDER)           // 设置类型
    .size(400, 250)                          // 设置尺寸
    .tolerance(10)                           // 设置容差
    .difficulty(2)                           // 设置难度
    .theme(VerifyTheme.BLUE)                 // 设置主题
    .enableBehaviorTracking(true)            // 启用行为检测
    .backgroundImages(Arrays.asList("bg1.jpg", "bg2.jpg")); // 背景图片

// 滑动拼图专用
VerifyConfig sliderConfig = VerifyConfig.createSlider()
    .size(400, 250)
    .sliderSize(50, 50)
    .tolerance(10)
    .backgroundImages(imageList);

// 文字点选专用
VerifyConfig textConfig = VerifyConfig.createTextClick()
    .size(350, 200)
    .clickTextCount(3)
    .interferenceTextCount(5)
    .tolerance(15)
    .textPool(Arrays.asList("春", "夏", "秋", "冬"));

// 算术验证码专用
VerifyConfig arithmeticConfig = VerifyConfig.createArithmetic()
    .numberRange(10, 99)
    .operators(Arrays.asList("+", "-", "×"))
    .allowNegativeResult(false);
```

### 难度级别说明

```java
// 难度级别会自动调整相关参数
VerifyConfig config = VerifyConfig.createSlider()
    .difficulty(1); // 简单

// 难度影响：
// 级别 1 (简单)：
//   - 容差值较大
//   - 点击文字数量少
//   - 数字范围小
//   - 干扰元素少

// 级别 2 (中等)：默认级别

// 级别 3 (困难)：
//   - 容差值较小
//   - 点击文字数量多
//   - 数字范围大
//   - 干扰元素多
```

### 容差值说明

```java
// 容差值决定了验证通过的宽松程度
VerifyConfig config = VerifyConfig.createSlider()
    .tolerance(10); // 允许10像素的误差

// 不同验证码类型的默认容差：
// 滑动拼图：8像素
// 文字点选：15像素（考虑字体大小和视觉误差）
// 算术验证码：0（不需要位置容差）
```

---

## 验证码组件详解

### SliderVerifyPane（滑动拼图验证码）

#### 构造方法

```java
// 默认构造
SliderVerifyPane sliderPane = new SliderVerifyPane();

// 使用配置
VerifyConfig config = VerifyConfig.createSlider()
    .size(400, 250)
    .tolerance(10);
SliderVerifyPane sliderPane = new SliderVerifyPane(config);
```

#### 主要方法

```java
// 设置验证码图片
VerifyImage image = VerifyImageUtil.generateSliderVerifyImage("bg.jpg", config);
sliderPane.setVerifyImage(image);

// 获取当前验证码图片
VerifyImage currentImage = sliderPane.getVerifyImage();

// 设置验证完成回调
sliderPane.setOnVerifyComplete(result -> {
    if (result.isSuccess()) {
        System.out.println("验证成功");
    }
});

// 设置刷新回调
sliderPane.setOnRefresh(() -> {
    System.out.println("验证码已刷新");
});

// 刷新验证码
sliderPane.refresh();

// 重置状态
sliderPane.reset();

// 获取配置
VerifyConfig config = sliderPane.getConfig();
```

#### 完整示例

```java
import com.javafx.test.verifyCode.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class SliderDemo extends Application {

    private Label statusLabel;
    private SliderVerifyPane sliderPane;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // 状态标签
        statusLabel = new Label("请拖动滑块完成拼图");

        // 创建配置
        VerifyConfig config = VerifyConfig.createSlider()
                .size(400, 250)
                .tolerance(10)
                .difficulty(2)
                .theme(VerifyTheme.BLUE)
                .backgroundImages(Arrays.asList(
                        "D:/images/bg1.jpg",
                        "D:/images/bg2.jpg",
                        "D:/images/bg3.jpg"
                ));

        // 创建组件
        sliderPane = new SliderVerifyPane(config);

        // 设置验证回调
        sliderPane.setOnVerifyComplete(result -> {
            handleVerifyResult(result);
        });

        // 设置刷新回调
        sliderPane.setOnRefresh(() -> {
            refreshVerifyImage();
        });

        // 初始化验证码
        refreshVerifyImage();

        root.getChildren().addAll(sliderPane, statusLabel);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("滑动拼图验证码");
        primaryStage.show();
    }

    private void handleVerifyResult(VerifyResult result) {
        Platform.runLater(() -> {
            if (result.isSuccess()) {
                statusLabel.setText("✅ 验证成功！耗时：" + result.getDuration() + "ms");
                statusLabel.setStyle("-fx-text-fill: #52c41a;");
            } else {
                statusLabel.setText("❌ 验证失败：" + result.getMessage());
                statusLabel.setStyle("-fx-text-fill: #ff4d4f;");

                // 延迟刷新
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() -> sliderPane.refresh());
                }).start();
            }
        });
    }

    private void refreshVerifyImage() {
        try {
            String imagePath = config.getRandomBackgroundImage();
            VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(imagePath, config);
            sliderPane.setVerifyImage(image);
            statusLabel.setText("请拖动滑块完成拼图");
            statusLabel.setStyle("-fx-text-fill: #666;");
        } catch (IOException e) {
            statusLabel.setText("加载验证码失败：" + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ff4d4f;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### TextClickVerifyPane（文字点选验证码）

#### 构造方法

```java
// 默认构造
TextClickVerifyPane textPane = new TextClickVerifyPane();

// 使用配置
VerifyConfig config = VerifyConfig.createTextClick()
    .clickTextCount(3)
    .interferenceTextCount(5);
TextClickVerifyPane textPane = new TextClickVerifyPane(config);
```

#### 主要方法

```java
// 设置验证码数据
VerifyImageUtil.TextClickVerifyData data = 
    VerifyImageUtil.generateTextClickVerify(config);
textPane.setVerifyData(data);

// 获取当前验证码数据
VerifyImageUtil.TextClickVerifyData currentData = textPane.getVerifyData();

// 其他方法与 SliderVerifyPane 相同
```

#### 完整示例

```java
import com.javafx.test.verifyCode.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TextClickDemo extends Application {

    private Label statusLabel;
    private TextClickVerifyPane textPane;
    private VerifyConfig config;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        statusLabel = new Label("请按顺序点击图中文字");

        // 创建配置
        config = VerifyConfig.createTextClick()
                .size(350, 200)
                .clickTextCount(3)
                .interferenceTextCount(5)
                .tolerance(15)
                .textPool(Arrays.asList(
                        "春", "夏", "秋", "冬", "风", "花", "雪", "月"
                ));

        // 创建组件
        textPane = new TextClickVerifyPane(config);

        // 设置回调
        textPane.setOnVerifyComplete(result -> {
            handleVerifyResult(result);
        });

        textPane.setOnRefresh(() -> {
            refreshVerifyData();
        });

        // 初始化
        refreshVerifyData();

        root.getChildren().addAll(textPane, statusLabel);

        Scene scene = new Scene(root, 450, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("文字点选验证码");
        primaryStage.show();
    }

    private void handleVerifyResult(VerifyResult result) {
        Platform.runLater(() -> {
            if (result.isSuccess()) {
                statusLabel.setText("✅ 验证成功！");
                statusLabel.setStyle("-fx-text-fill: #52c41a;");
            } else {
                statusLabel.setText("❌ " + result.getMessage());
                statusLabel.setStyle("-fx-text-fill: #ff4d4f;");

                // 延迟刷新
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() -> textPane.refresh());
                }).start();
            }
        });
    }

    private void refreshVerifyData() {
        VerifyImageUtil.TextClickVerifyData data =
                VerifyImageUtil.generateTextClickVerify(config);
        textPane.setVerifyData(data);
        statusLabel.setText("请按顺序点击图中文字");
        statusLabel.setStyle("-fx-text-fill: #666;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### ArithmeticVerifyPane（算术验证码）

#### 构造方法

```java
// 默认构造
ArithmeticVerifyPane arithmeticPane = new ArithmeticVerifyPane();

// 使用配置
VerifyConfig config = VerifyConfig.createArithmetic()
    .numberRange(10, 99)
    .operators(Arrays.asList("+", "-", "×"));
ArithmeticVerifyPane arithmeticPane = new ArithmeticVerifyPane(config);
```

#### 主要方法

```java
// 设置验证码数据
VerifyImageUtil.ArithmeticVerifyData data = 
    VerifyImageUtil.generateArithmeticVerify(config);
arithmeticPane.setVerifyData(data);

// 获取算式表达式
String expression = arithmeticPane.getExpression(); // "23 + 45"

// 其他方法与 SliderVerifyPane 相同
```

#### 完整示例

```java
import com.javafx.test.verifyCode.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;

public class ArithmeticDemo extends Application {

    private Label statusLabel;
    private ArithmeticVerifyPane arithmeticPane;
    private VerifyConfig config;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        statusLabel = new Label("请计算并输入结果");

        // 创建配置
        config = VerifyConfig.createArithmetic()
                .numberRange(10, 99)
                .operators(Arrays.asList("+", "-", "×"))
                .allowNegativeResult(false);

        // 创建组件
        arithmeticPane = new ArithmeticVerifyPane(config);

        // 设置回调
        arithmeticPane.setOnVerifyComplete(result -> {
            handleVerifyResult(result);
        });

        arithmeticPane.setOnRefresh(() -> {
            refreshVerifyData();
        });

        // 初始化
        refreshVerifyData();

        root.getChildren().addAll(arithmeticPane, statusLabel);

        Scene scene = new Scene(root, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("算术验证码");
        primaryStage.show();
    }

    private void handleVerifyResult(VerifyResult result) {
        Platform.runLater(() -> {
            if (result.isSuccess()) {
                statusLabel.setText("✅ 回答正确！");
                statusLabel.setStyle("-fx-text-fill: #52c41a;");
            } else {
                statusLabel.setText("❌ " + result.getMessage());
                statusLabel.setStyle("-fx-text-fill: #ff4d4f;");

                // 延迟刷新
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() -> arithmeticPane.refresh());
                }).start();
            }
        });
    }

    private void refreshVerifyData() {
        VerifyImageUtil.ArithmeticVerifyData data =
                VerifyImageUtil.generateArithmeticVerify(config);
        arithmeticPane.setVerifyData(data);
        statusLabel.setText("请计算并输入结果");
        statusLabel.setStyle("-fx-text-fill: #666;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 工厂类详解

### VerifyCodeFactory

`VerifyCodeFactory` 提供统一的验证码创建入口，简化组件创建流程。

#### 创建方法

```java
// ==================== 滑动拼图验证码 ====================

// 使用默认配置创建
SliderVerifyPane slider1 = VerifyCodeFactory.createSlider();

// 使用自定义配置创建
VerifyConfig config = VerifyConfig.createSlider()
    .size(400, 250)
    .tolerance(10);
SliderVerifyPane slider2 = VerifyCodeFactory.createSlider(config);

// 创建并设置回调
SliderVerifyPane slider3 = VerifyCodeFactory.createSlider(
    config,
    result -> {
        if (result.isSuccess()) {
            System.out.println("验证成功");
        }
    }
);

// 完整参数创建
SliderVerifyPane slider4 = VerifyCodeFactory.createSlider(
    backgroundImages,  // 背景图片列表
    400, 250,          // 宽高
    10,                // 容差
    result -> {        // 回调
        // 处理结果
    }
);

// ==================== 文字点选验证码 ====================

// 使用默认配置
TextClickVerifyPane text1 = VerifyCodeFactory.createTextClick();

// 使用自定义配置
VerifyConfig textConfig = VerifyConfig.createTextClick()
    .clickTextCount(3)
    .interferenceTextCount(5);
TextClickVerifyPane text2 = VerifyCodeFactory.createTextClick(textConfig);

// 创建并设置回调
TextClickVerifyPane text3 = VerifyCodeFactory.createTextClick(
    textConfig,
    result -> {
        // 处理结果
    }
);

// ==================== 算术验证码 ====================

// 使用默认配置
ArithmeticVerifyPane arithmetic1 = VerifyCodeFactory.createArithmetic();

// 使用自定义配置
VerifyConfig arithmeticConfig = VerifyConfig.createArithmetic()
    .numberRange(10, 99)
    .operators(Arrays.asList("+", "-", "×"));
ArithmeticVerifyPane arithmetic2 = VerifyCodeFactory.createArithmetic(arithmeticConfig);

// 创建并设置回调
ArithmeticVerifyPane arithmetic3 = VerifyCodeFactory.createArithmetic(
    arithmeticConfig,
    result -> {
        // 处理结果
    }
);

// ==================== 通用创建方法 ====================

// 根据类型创建
Pane verifyPane = VerifyCodeFactory.create(VerifyType.SLIDER, config);

// 创建随机类型
Pane randomPane = VerifyCodeFactory.createRandom();
Pane randomPane2 = VerifyCodeFactory.createRandom(config);
```

#### 快速集成方法

```java
// ==================== 快速集成到容器 ====================

VBox container = new VBox();
List<String> backgroundImages = Arrays.asList("bg1.jpg", "bg2.jpg");

// 快速集成滑动拼图
SliderVerifyPane slider = VerifyCodeFactory.integrateSlider(
    container,           // 目标容器
    backgroundImages,    // 背景图片列表
    result -> {          // 验证完成回调
        if (result.isSuccess()) {
            System.out.println("验证成功，继续业务逻辑");
        }
    }
);
// 组件已自动添加到 container，验证码已初始化

// 快速集成文字点选
TextClickVerifyPane textClick = VerifyCodeFactory.integrateTextClick(
    container,
    result -> {
        // 处理验证结果
    }
);

// 快速集成算术验证码
ArithmeticVerifyPane arithmetic = VerifyCodeFactory.integrateArithmetic(
    container,
    result -> {
        // 处理验证结果
    }
);
```

#### 工厂类使用示例

```java
import com.javafx.test.verifyCode.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FactoryDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(30);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("工厂类快速创建示例");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        // 使用工厂方法快速创建
        HBox verifyBox = new HBox(30);
        verifyBox.setStyle("-fx-alignment: center;");

        // 1. 滑动拼图 - 一行代码创建
        SliderVerifyPane slider = VerifyCodeFactory.createSlider(
                VerifyConfig.createSlider().theme(VerifyTheme.BLUE),
                result -> System.out.println("滑动拼图: " + (result.isSuccess() ? "成功" : "失败"))
        );

        // 2. 文字点选 - 一行代码创建
        TextClickVerifyPane textClick = VerifyCodeFactory.createTextClick(
                VerifyConfig.createTextClick().clickTextCount(3),
                result -> System.out.println("文字点选: " + (result.isSuccess() ? "成功" : "失败"))
        );
        // 初始化数据
        textClick.setVerifyData(VerifyImageUtil.generateTextClickVerify(textClick.getConfig()));

        // 3. 算术验证 - 一行代码创建
        ArithmeticVerifyPane arithmetic = VerifyCodeFactory.createArithmetic(
                VerifyConfig.createArithmetic().numberRange(1, 20),
                result -> System.out.println("算术验证: " + (result.isSuccess() ? "成功" : "失败"))
        );
        // 初始化数据
        arithmetic.setVerifyData(VerifyImageUtil.generateArithmeticVerify(arithmetic.getConfig()));

        verifyBox.getChildren().addAll(slider, textClick, arithmetic);

        // 控制按钮
        HBox controlBox = new HBox(10);
        Button refreshAllBtn = new Button("全部刷新");
        refreshAllBtn.setOnAction(e -> {
            slider.refresh();
            textClick.refresh();
            arithmetic.refresh();
        });
        controlBox.getChildren().add(refreshAllBtn);

        root.getChildren().addAll(titleLabel, verifyBox, controlBox);

        Scene scene = new Scene(root, 1200, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("工厂类演示");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 主题系统详解

### VerifyTheme 类

`VerifyTheme` 提供验证码组件的视觉样式配置。

#### 预设主题

```java
// 默认主题（浅色）
VerifyTheme.DEFAULT

// 深色主题
VerifyTheme.DARK

// 蓝色主题
VerifyTheme.BLUE

// 绿色主题
VerifyTheme.GREEN
```

#### 使用预设主题

```java
VerifyConfig config = VerifyConfig.createSlider()
    .theme(VerifyTheme.DARK);  // 使用深色主题
```

#### 自定义主题

```java
// 使用 Builder 创建自定义主题
VerifyTheme customTheme = VerifyTheme.builder()
    // 颜色配置
    .primaryColor(Color.valueOf("#1890ff"))      // 主色调
    .successColor(Color.valueOf("#52c41a"))      // 成功颜色
    .errorColor(Color.valueOf("#ff4d4f"))        // 错误颜色
    .warningColor(Color.valueOf("#faad14"))      // 警告颜色
    .backgroundColor(Color.valueOf("#f0f2f5"))   // 背景颜色
    .cardBackgroundColor(Color.WHITE)            // 卡片背景
    .textColor(Color.valueOf("#333333"))         // 文字颜色
    .secondaryTextColor(Color.valueOf("#666666")) // 次要文字
    .borderColor(Color.valueOf("#d9d9d9"))       // 边框颜色
    .sliderTrackColor(Color.valueOf("#f0f0f0"))  // 滑块轨道
    .sliderThumbColor(Color.WHITE)               // 滑块按钮
    
    // 字体配置
    .fontFamily("Microsoft YaHei")  // 字体
    .baseFontSize(14)               // 基础字号
    .titleFontSize(16)              // 标题字号
    .smallFontSize(12)              // 小字号
    
    // 尺寸配置
    .borderRadius(8)    // 圆角大小
    .padding(10)        // 内边距
    .shadowRadius(10)   // 阴影半径
    .build();

// 应用自定义主题
VerifyConfig config = VerifyConfig.createSlider()
    .theme(customTheme);
```

#### 主题样式生成方法

```java
VerifyTheme theme = VerifyTheme.BLUE;

// 生成卡片样式
String cardStyle = theme.getCardStyle();
// 返回: -fx-background-color: rgba(255, 255, 255, 1.00); ...

// 生成滑块轨道样式
String trackStyle = theme.getSliderTrackStyle();

// 生成滑块按钮样式
String thumbStyle = theme.getSliderThumbStyle(false, false);
String thumbSuccessStyle = theme.getSliderThumbStyle(true, false);
String thumbFailStyle = theme.getSliderThumbStyle(false, true);

// 生成刷新按钮样式
String refreshBtnStyle = theme.getRefreshButtonStyle();

// 生成状态标签样式
String statusStyle = theme.getStatusLabelStyle("SUCCESS");
```

#### 主题使用完整示例

```java
import com.javafx.test.verifyCode.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ThemeDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(30);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("主题定制演示");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        HBox themeBox = new HBox(30);
        themeBox.setStyle("-fx-alignment: center;");

        // 1. 默认主题
        VBox defaultBox = createThemeBox("默认主题", VerifyTheme.DEFAULT);

        // 2. 深色主题
        VBox darkBox = createThemeBox("深色主题", VerifyTheme.DARK);

        // 3. 蓝色主题
        VBox blueBox = createThemeBox("蓝色主题", VerifyTheme.BLUE);

        // 4. 自定义主题
        VerifyTheme customTheme = VerifyTheme.builder()
                .primaryColor(Color.valueOf("#722ed1"))
                .successColor(Color.valueOf("#13c2c2"))
                .errorColor(Color.valueOf("#eb2f96"))
                .backgroundColor(Color.valueOf("#f9f0ff"))
                .build();
        VBox customBox = createThemeBox("自定义主题", customTheme);

        themeBox.getChildren().addAll(defaultBox, darkBox, blueBox, customBox);

        root.getChildren().addAll(titleLabel, themeBox);

        Scene scene = new Scene(root, 1400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("主题系统演示");
        primaryStage.show();
    }

    private VBox createThemeBox(String title, VerifyTheme theme) {
        VBox box = new VBox(10);
        box.setStyle("-fx-alignment: center;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        VerifyConfig config = VerifyConfig.createSlider()
                .size(300, 180)
                .theme(theme);

        SliderVerifyPane slider = new SliderVerifyPane(config);

        box.getChildren().addAll(titleLabel, slider);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 工具类详解

### VerifyImageUtil 类

`VerifyImageUtil` 提供验证码图片的生成、转换等工具方法。

#### 滑动拼图验证码生成

```java
// 根据文件路径生成
VerifyImage image = VerifyImageUtil.generateSliderVerifyImage("bg.jpg");

// 根据文件路径和配置生成
VerifyConfig config = VerifyConfig.createSlider().size(400, 250);
VerifyImage image = VerifyImageUtil.generateSliderVerifyImage("bg.jpg", config);

// 根据配置生成（自动选择背景图）
VerifyConfig config = VerifyConfig.createSlider()
    .backgroundImages(Arrays.asList("bg1.jpg", "bg2.jpg"));
VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(config);
```

#### 文字点选验证码生成

```java
VerifyConfig config = VerifyConfig.createTextClick()
    .clickTextCount(3)
    .interferenceTextCount(5);

VerifyImageUtil.TextClickVerifyData data = 
    VerifyImageUtil.generateTextClickVerify(config);

// 获取数据
String imageBase64 = data.getImageBase64();
String hint = data.getHint();  // "请点击：春、夏、秋"
List<Position> positions = data.getPositions();  // 正确位置
int width = data.getWidth();
int height = data.getHeight();
```

#### 算术验证码生成

```java
VerifyConfig config = VerifyConfig.createArithmetic()
    .numberRange(10, 99)
    .operators(Arrays.asList("+", "-", "×"));

VerifyImageUtil.ArithmeticVerifyData data = 
    VerifyImageUtil.generateArithmeticVerify(config);

// 获取数据
String imageBase64 = data.getImageBase64();
String expression = data.getExpression();  // "23 + 45"
int answer = data.getAnswer();  // 68
int width = data.getWidth();
int height = data.getHeight();
```

#### 图片转换方法

```java
// Base64 转 BufferedImage
BufferedImage image = VerifyImageUtil.base64ToImage(base64String);

// BufferedImage 转 Base64
String base64 = VerifyImageUtil.imageToBase64(image);

// 调整图片尺寸
BufferedImage resized = VerifyImageUtil.resizeImage(image, 400, 250);
```

---

## 事件处理详解

### 验证完成回调

```java
verifyPane.setOnVerifyComplete(result -> {
    // 判断是否成功
    if (result.isSuccess()) {
        // 验证成功处理
        System.out.println("✅ 验证成功！");
        System.out.println("耗时：" + result.getDuration() + "ms");
        
        // 执行业务逻辑
        // 例如：提交表单、解锁功能等
        
    } else {
        // 验证失败处理
        System.out.println("❌ 验证失败");
        System.out.println("错误信息：" + result.getMessage());
        System.out.println("错误码：" + result.getErrorCode());
        
        // 根据错误码处理
        switch (result.getErrorCode()) {
            case "ROBOT_DETECTED":
                showAlert("检测到异常操作，请重试");
                break;
            case "POSITION_MISMATCH":
                showAlert("位置不匹配，请精确操作");
                break;
            case "WRONG_ANSWER":
                showAlert("答案错误，请重新计算");
                break;
            default:
                showAlert(result.getMessage());
        }
    }
});
```

### 刷新回调

```java
verifyPane.setOnRefresh(() -> {
    System.out.println("验证码已刷新");
    
    // 可以在这里记录日志
    // 或执行其他刷新相关操作
});
```

### 状态监听

```java
// 监听状态变化
verifyPane.stateProperty().addListener((observable, oldValue, newValue) -> {
    System.out.println("状态变化：" + oldValue + " -> " + newValue);
    
    switch (newValue) {
        case READY:
            System.out.println("准备就绪");
            break;
        case LOADING:
            System.out.println("加载中...");
            break;
        case VERIFYING:
            System.out.println("验证中...");
            break;
        case SUCCESS:
            System.out.println("验证成功！");
            break;
        case FAIL:
            System.out.println("验证失败！");
            break;
    }
});

// 绑定状态到UI
Label statusLabel = new Label();
statusLabel.textProperty().bind(
    verifyPane.stateProperty().asString("状态: %s")
);
```

---

## 最佳实践

### 1. 验证失败自动刷新

```java
verifyPane.setOnVerifyComplete(result -> {
    handleVerifyResult(result);
    
    // 验证失败时延迟刷新，让用户看到错误提示
    if (!result.isSuccess()) {
        new Thread(() -> {
            try {
                Thread.sleep(1500); // 延迟1.5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> verifyPane.refresh());
        }).start();
    }
});
```

### 2. 使用统一接口编程

```java
// 推荐：使用接口类型声明
public VerifyPane createVerifyPane(VerifyType type) {
    VerifyPane verifyPane;
    
    switch (type) {
        case SLIDER:
            verifyPane = new SliderVerifyPane(config);
            break;
        case TEXT_CLICK:
            verifyPane = new TextClickVerifyPane(config);
            break;
        case ARITHMETIC:
            verifyPane = new ArithmeticVerifyPane(config);
            break;
        default:
            verifyPane = new SliderVerifyPane(config);
    }
    
    // 统一设置回调
    verifyPane.setOnVerifyComplete(this::handleResult);
    
    return verifyPane;
}

// 统一处理
public void handleResult(VerifyResult result) {
    if (result.isSuccess()) {
        // 成功处理
    } else {
        // 失败处理
    }
}
```

### 3. 资源管理

```java
// 使用 try-catch 处理图片加载异常
try {
    VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(
        imagePath, config
    );
    sliderPane.setVerifyImage(image);
} catch (IOException e) {
    // 错误处理
    showError("加载验证码失败：" + e.getMessage());
    
    // 记录日志
    logger.error("验证码加载失败", e);
}
```

### 4. 线程安全

```java
// 在后台线程执行耗时操作
new Thread(() -> {
    try {
        // 耗时操作
        Thread.sleep(1000);
        
        // 更新UI必须在JavaFX线程
        Platform.runLater(() -> {
            verifyPane.refresh();
        });
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}).start();
```

---

## 完整示例代码

### 综合演示程序

```java
import com.javafx.test.verifyCode.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

/**
 * 验证码组件综合演示程序
 * 展示三种验证码类型的使用方法和最佳实践
 */
public class CompleteDemo extends Application {

    private Label statusLabel;
    private SliderVerifyPane sliderPane;
    private TextClickVerifyPane textClickPane;
    private ArithmeticVerifyPane arithmeticPane;

    @Override
    public void start(Stage primaryStage) {
        // 主容器
        VBox root = new VBox(30);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // 标题
        Label titleLabel = new Label("🛡️ 验证码组件综合演示");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.valueOf("#333"));

        // 状态栏
        statusLabel = new Label("系统就绪，请选择验证码类型进行验证");
        statusLabel.setFont(Font.font("Microsoft YaHei", 14));
        statusLabel.setTextFill(Color.valueOf("#666"));

        // 创建三种验证码
        HBox verifyBox = new HBox(40);
        verifyBox.setAlignment(Pos.CENTER);

        VBox sliderBox = createSliderVerifyBox();
        VBox textBox = createTextClickVerifyBox();
        VBox arithmeticBox = createArithmeticVerifyBox();

        verifyBox.getChildren().addAll(sliderBox, textBox, arithmeticBox);

        // 控制按钮
        HBox controlBox = createControlBox();

        root.getChildren().addAll(titleLabel, statusLabel, verifyBox, controlBox);

        Scene scene = new Scene(root, 1300, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("验证码组件库演示");
        primaryStage.show();
    }

    private VBox createSliderVerifyBox() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label titleLabel = new Label("🔲 滑动拼图");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#1890ff"));

        // 创建配置
        VerifyConfig config = VerifyConfig.createSlider()
                .size(350, 200)
                .tolerance(10)
                .difficulty(2)
                .theme(VerifyTheme.BLUE);

        // 创建组件
        sliderPane = new SliderVerifyPane(config);

        // 设置回调
        sliderPane.setOnVerifyComplete(result -> {
            handleVerifyResult("滑动拼图", result);
            if (!result.isSuccess()) {
                delayRefresh(sliderPane);
            }
        });

        sliderPane.setOnRefresh(() -> {
            updateStatus("滑动拼图验证码已刷新");
        });

        // 初始化
        refreshSliderVerify();

        box.getChildren().addAll(titleLabel, sliderPane);
        return box;
    }

    private VBox createTextClickVerifyBox() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label titleLabel = new Label("🔤 文字点选");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#52c41a"));

        // 创建配置
        VerifyConfig config = VerifyConfig.createTextClick()
                .size(350, 200)
                .clickTextCount(3)
                .interferenceTextCount(5)
                .tolerance(15)
                .theme(VerifyTheme.GREEN);

        // 创建组件
        textClickPane = new TextClickVerifyPane(config);

        // 设置回调
        textClickPane.setOnVerifyComplete(result -> {
            handleVerifyResult("文字点选", result);
            if (!result.isSuccess()) {
                delayRefresh(textClickPane);
            }
        });

        textClickPane.setOnRefresh(() -> {
            refreshTextClickVerify();
            updateStatus("文字点选验证码已刷新");
        });

        // 初始化
        refreshTextClickVerify();

        box.getChildren().addAll(titleLabel, textClickPane);
        return box;
    }

    private VBox createArithmeticVerifyBox() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label titleLabel = new Label("🔢 算术验证");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#722ed1"));

        // 创建配置
        VerifyConfig config = VerifyConfig.createArithmetic()
                .numberRange(10, 99)
                .operators(Arrays.asList("+", "-", "×"))
                .theme(VerifyTheme.DEFAULT);

        // 创建组件
        arithmeticPane = new ArithmeticVerifyPane(config);

        // 设置回调
        arithmeticPane.setOnVerifyComplete(result -> {
            handleVerifyResult("算术验证", result);
            if (!result.isSuccess()) {
                delayRefresh(arithmeticPane);
            }
        });

        arithmeticPane.setOnRefresh(() -> {
            refreshArithmeticVerify();
            updateStatus("算术验证码已刷新");
        });

        // 初始化
        refreshArithmeticVerify();

        box.getChildren().addAll(titleLabel, arithmeticPane);
        return box;
    }

    private HBox createControlBox() {
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER);

        Button refreshAllBtn = new Button("🔄 全部刷新");
        refreshAllBtn.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 10 20;");
        refreshAllBtn.setOnAction(e -> {
            sliderPane.refresh();
            textClickPane.refresh();
            arithmeticPane.refresh();
            updateStatus("所有验证码已刷新");
        });

        Button resetAllBtn = new Button("↩️ 全部重置");
        resetAllBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 10 20;");
        resetAllBtn.setOnAction(e -> {
            sliderPane.reset();
            textClickPane.reset();
            arithmeticPane.reset();
            updateStatus("所有验证码已重置");
        });

        controlBox.getChildren().addAll(refreshAllBtn, resetAllBtn);
        return controlBox;
    }

    private void handleVerifyResult(String type, VerifyResult result) {
        Platform.runLater(() -> {
            if (result.isSuccess()) {
                updateStatus("✅ " + type + "验证成功！耗时：" + result.getDuration() + "ms");
            } else {
                updateStatus("❌ " + type + "验证失败：" + result.getMessage());
            }
        });
    }

    private void delayRefresh(VerifyPane pane) {
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> pane.refresh());
        }).start();
    }

    private void refreshSliderVerify() {
        try {
            VerifyImage image = VerifyImageUtil.generateSliderVerifyImage(
                    sliderPane.getConfig()
            );
            sliderPane.setVerifyImage(image);
        } catch (IOException e) {
            updateStatus("加载滑动拼图验证码失败：" + e.getMessage());
        }
    }

    private void refreshTextClickVerify() {
        VerifyImageUtil.TextClickVerifyData data =
                VerifyImageUtil.generateTextClickVerify(textClickPane.getConfig());
        textClickPane.setVerifyData(data);
    }

    private void refreshArithmeticVerify() {
        VerifyImageUtil.ArithmeticVerifyData data =
                VerifyImageUtil.generateArithmeticVerify(arithmeticPane.getConfig());
        arithmeticPane.setVerifyData(data);
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 常见问题

### Q1: 如何修改验证码尺寸？

```java
VerifyConfig config = VerifyConfig.createSlider()
    .size(400, 250);  // 宽度400，高度250
```

### Q2: 如何自定义背景图片？

```java
List<String> images = Arrays.asList(
    "D:/images/bg1.jpg",
    "D:/images/bg2.jpg",
    "D:/images/bg3.jpg"
);

VerifyConfig config = VerifyConfig.createSlider()
    .backgroundImages(images);
```

### Q3: 验证失败如何自动刷新？

```java
verifyPane.setOnVerifyComplete(result -> {
    if (!result.isSuccess()) {
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> verifyPane.refresh());
        }).start();
    }
});
```

### Q4: 如何监听状态变化？

```java
verifyPane.stateProperty().addListener((obs, oldVal, newVal) -> {
    System.out.println("状态变化：" + oldVal + " -> " + newVal);
});
```

### Q5: 如何创建随机类型的验证码？

```java
Pane randomPane = VerifyCodeFactory.createRandom();
```

---

## 版本信息

- **版本**: 1.0.0
- **作者**: JavaFX Team
- **更新日期**: 2024

## 相关文件位置

所有验证码相关类位于包 `com.javafx.test.verifyCode` 下。

## 演示程序

- `VerifyCodeDemo.java` - 基础使用演示
- `VerifyPaneCodeDemo.java` - 统一接口演示
