# JavaFX Steps 步骤条组件 API 参考文档

> 版本：1.0  
> 更新日期：2026-04-16  
> 适用环境：JavaFX 21+

---

## 目录

1. [概述](#概述)
2. [核心类详解](#核心类详解)
   - [StepStatus - 状态枚举](#stepstatus---状态枚举)
   - [Step - 步骤对象](#step---步骤对象)
   - [StepsTheme - 主题配置](#stepstheme---主题配置)
   - [Steps - 核心组件](#steps---核心组件)
   - [StepsComponentFactory - 工厂类](#stepscomponentfactory---工厂类)
3. [使用场景示例](#使用场景示例)
4. [主题定制指南](#主题定制指南)
5. [错误处理与异常](#错误处理与异常)
6. [性能优化建议](#性能优化建议)
7. [最佳实践](#最佳实践)

---

## 概述

JavaFX Steps 组件是一套企业级的步骤条UI组件，参考 Element UI Steps 设计，支持：

- **多种布局**：水平、垂直、简洁三种模式
- **状态管理**：WAIT、PROCESS、FINISH、SUCCESS、ERROR 五种状态
- **主题定制**：完整的颜色、字体、尺寸配置
- **Builder模式**：链式调用，配置简洁
- **线程安全**：所有UI更新通过 Platform.runLater 执行

---

## 核心类详解

### StepStatus - 状态枚举

步骤状态枚举，定义步骤的五种显示状态。

#### 枚举值

| 枚举值 | 说明 | 颜色（默认主题） |
|--------|------|-----------------|
| `WAIT` | 等待状态，步骤未开始 | #C0C4CC（灰色） |
| `PROCESS` | 进行中状态，当前激活步骤 | #409EFF（蓝色） |
| `FINISH` | 完成状态，步骤已完成 | #303133（深灰） |
| `SUCCESS` | 成功状态，步骤成功完成 | #67C23A（绿色） |
| `ERROR` | 错误状态，步骤执行失败 | #F56C6C（红色） |

#### 使用示例

```java
// 设置步骤状态
Step step = new Step("步骤标题");
step.setStatus(StepStatus.PROCESS);

// 在 Builder 中使用
Step step = Step.builder()
    .title("验证步骤")
    .status(StepStatus.ERROR)
    .build();
```

---

### Step - 步骤对象

单个步骤对象，包含标题、描述、状态、图标等属性。

#### 构造方法

| 方法签名 | 说明 |
|----------|------|
| `Step()` | 创建空步骤 |
| `Step(String title)` | 创建带标题的步骤 |
| `Step(String title, String description)` | 创建带标题和描述的步骤 |

#### 属性列表

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `title` | StringProperty | "" | 步骤标题 |
| `description` | StringProperty | "" | 步骤描述 |
| `status` | ObjectProperty&lt;StepStatus&gt; | null | 步骤状态（null时自动计算） |
| `icon` | ObjectProperty&lt;Node&gt; | null | 自定义图标节点 |
| `index` | int (只读) | 0 | 步骤索引（由 Steps 自动管理） |

#### 插槽机制

步骤支持三种自定义插槽：

| 插槽类型 | 设置方法 | 用途 |
|----------|----------|------|
| `titleSlot` | `setTitleSlot(Node)` | 自定义标题区域 |
| `descriptionSlot` | `setDescriptionSlot(Node)` | 自定义描述区域 |
| `iconSlot` | `setIconSlot(Node)` | 自定义图标区域 |

#### 主要方法

```java
// 标题操作
public String getTitle()
public void setTitle(String title)
public StringProperty titleProperty()

// 描述操作
public String getDescription()
public void setDescription(String description)
public StringProperty descriptionProperty()

// 状态操作
public StepStatus getStatus()
public void setStatus(StepStatus status)
public ObjectProperty<StepStatus> statusProperty()

// 图标操作
public Node getIcon()
public void setIcon(Node icon)
public ObjectProperty<Node> iconProperty()

// 插槽操作
public void setTitleSlot(Node slot)
public void setDescriptionSlot(Node slot)
public void setIconSlot(Node slot)
public void clearSlots()

// 资源释放
public void dispose()
```

#### Builder 模式

```java
Step step = Step.builder()
    .title("用户验证")
    .description("验证用户身份信息")
    .status(StepStatus.PROCESS)
    .icon(new FontIcon())
    .build();
```

#### 完整示例

```java
// 示例1：基础用法
Step step1 = new Step("第一步", "填写基本信息");

// 示例2：带自定义图标
FontIcon icon = new FontIcon(FontawesomeIcon.CHECK);
icon.setIconColor(Color.GREEN);
Step step2 = new Step("完成");
step2.setIcon(icon);

// 示例3：使用插槽自定义标题
HBox customTitle = new HBox(
    new Label("VIP "),
    new Label("会员注册")
);
customTitle.setStyle("-fx-background-color: #FFD700; -fx-padding: 2 6;");
step2.setTitleSlot(customTitle);

// 示例4：Builder 模式
Step step = Step.builder()
    .title("审核")
    .description("等待管理员审核")
    .status(StepStatus.WAIT)
    .build();
```

---

### StepsTheme - 主题配置

主题配置类，管理步骤条的颜色、字体和尺寸。

#### 预设主题

| 主题常量 | 说明 |
|----------|------|
| `DEFAULT` | 默认蓝色主题 |
| `DARK` | 深色主题 |
| `SUCCESS` | 成功主题（绿色） |
| `MINIMAL` | 简约主题（灰色） |

#### 颜色属性

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `primaryColor` | Color | #409EFF | 主色调（进行中状态） |
| `successColor` | Color | #67C23A | 成功状态颜色 |
| `errorColor` | Color | #F56C6C | 错误状态颜色 |
| `warningColor` | Color | #E6A23C | 警告状态颜色 |
| `waitColor` | Color | #C0C4CC | 等待状态颜色 |
| `finishColor` | Color | #303133 | 完成状态颜色 |
| `textColor` | Color | #303133 | 主文字颜色 |
| `descriptionColor` | Color | #909399 | 描述文字颜色 |
| `lineColor` | Color | #C0C4CC | 连接线颜色 |
| `finishLineColor` | Color | #409EFF | 完成连接线颜色 |
| `backgroundColor` | Color | TRANSPARENT | 背景颜色 |

#### 尺寸属性

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `fontFamily` | String | "System" | 字体族 |
| `titleFontSize` | double | 14 | 标题字体大小 |
| `descriptionFontSize` | double | 12 | 描述字体大小 |
| `iconFontSize` | double | 14 | 图标字体大小 |
| `iconSize` | double | 24 | 图标尺寸 |
| `lineHeight` | double | 2 | 连接线粗细 |
| `simpleArrowSize` | double | 12 | 简洁模式箭头大小 |
| `stepPadding` | double | 10 | 步骤间内边距 |

#### Builder 模式创建主题

```java
// 创建自定义主题
StepsTheme customTheme = new StepsTheme.Builder()
    .primaryColor(Color.valueOf("#FF6B6B"))
    .successColor(Color.valueOf("#4ECDC4"))
    .errorColor(Color.valueOf("#FF6B6B"))
    .textColor(Color.valueOf("#2C3E50"))
    .titleFontSize(16)
    .iconSize(32)
    .lineHeight(3)
    .build();

// 应用主题
Steps steps = new Steps();
steps.setTheme(customTheme);
```

#### 快速创建深色主题

```java
StepsTheme darkTheme = StepsTheme.DARK;
steps.setTheme(darkTheme);
```

---

### Steps - 核心组件

步骤条核心组件，管理步骤集合、布局和显示逻辑。

#### 构造方法

| 方法签名 | 说明 |
|----------|------|
| `Steps()` | 创建空的步骤条 |
| `Steps(Step... steps)` | 创建带初始步骤的步骤条 |

#### 核心属性

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `active` | IntegerProperty | 0 | 当前激活步骤索引 |
| `direction` | ObjectProperty&lt;Orientation&gt; | HORIZONTAL | 布局方向 |
| `finishStatus` | ObjectProperty&lt;StepStatus&gt; | FINISH | 已完成步骤显示状态 |
| `processStatus` | ObjectProperty&lt;StepStatus&gt; | PROCESS | 当前步骤显示状态 |
| `alignCenter` | BooleanProperty | false | 是否居中对齐 |
| `simple` | BooleanProperty | false | 是否简洁模式 |
| `space` | ObjectProperty&lt;Number&gt; | -1 | 步间距（-1为自动） |
| `theme` | ObjectProperty&lt;StepsTheme&gt; | DEFAULT | 主题配置 |
| `steps` | ObservableList&lt;Step&gt; | 空 | 步骤列表 |

#### 链式配置方法

```java
// 所有配置方法都返回 this，支持链式调用
public Steps active(int active)
public Steps direction(Orientation direction)
public Steps finishStatus(StepStatus status)
public Steps processStatus(StepStatus status)
public Steps alignCenter(boolean alignCenter)
public Steps simple(boolean simple)
public Steps space(double space)
public Steps theme(StepsTheme theme)
public Steps addStep(Step step)
public Steps addSteps(Step... steps)
public Steps steps(ObservableList<Step> steps)
```

#### 回调方法

```java
// 步骤变化回调
public Steps onChange(Consumer<Integer> callback)

// 步骤点击回调
public Steps onStepClick(Consumer<Integer> callback)
```

#### 主要方法

| 方法签名 | 返回类型 | 说明 |
|----------|----------|------|
| `getActive()` | int | 获取当前激活索引 |
| `setActive(int)` | void | 设置当前激活索引 |
| `activeProperty()` | IntegerProperty | 获取active属性 |
| `getSteps()` | ObservableList&lt;Step&gt; | 获取步骤列表 |
| `getStepCount()` | int | 获取步骤数量 |
| `getCurrentStep()` | Step | 获取当前步骤对象 |
| `next()` | void | 下一步（索引+1） |
| `prev()` | void | 上一步（索引-1） |
| `reset()` | void | 重置到第一步 |
| `first()` | void | 跳转到第一步 |
| `last()` | void | 跳转到最后一步 |
| `isFirst()` | boolean | 是否在第一步 |
| `isLast()` | boolean | 是否在最后一步 |
| `canNext()` | boolean | 是否可以下一步 |
| `canPrev()` | boolean | 是否可以上一步 |
| `getNode()` | Region | 获取JavaFX节点 |
| `dispose()` | void | 释放资源 |

#### 资源管理

```java
// 正确的资源释放方式
Steps steps = new Steps();
try {
    // 使用组件...
} finally {
    steps.dispose(); // 确保释放资源
}
```

---

### StepsComponentFactory - 工厂类

统一的组件创建入口，提供快速创建方法和Builder模式配置。

#### 快速创建方法

| 方法签名 | 返回类型 | 说明 |
|----------|----------|------|
| `createBasic()` | BasicStepsPane | 基础用法组件 |
| `createStatus()` | StatusStepsPane | 含状态步骤条 |
| `createCenter()` | CenterStepsPane | 居中步骤条 |
| `createDescription()` | DescriptionStepsPane | 带描述步骤栏 |
| `createIcon()` | IconStepsPane | 带图标步骤条 |
| `createVertical()` | VerticalStepsPane | 垂直步骤条 |
| `createSimple()` | SimpleStepsPane | 简洁风格步骤条 |
| `createTheme()` | ThemeStepsPane | 主题演示组件 |
| `createInteractive()` | InteractiveStepsPane | 交互式组件 |

#### Builder 模式

```java
// 创建配置后的组件
BasicStepsPane pane = StepsComponentFactory.builder()
    .title("用户注册流程")
    .description("请完成以下步骤")
    .active(1)
    .finishStatus(StepStatus.SUCCESS)
    .onChange(index -> System.out.println("当前步骤: " + index))
    .buildBasic();

// 获取内部 Steps 对象
Steps steps = pane.getSteps();
```

#### 工具方法

```java
// 创建 SVG 图标
Node icon = StepsComponentFactory.createSvgIcon("M12 2C6...", Color.BLUE);

// 创建默认步骤数组
Step[] steps = StepsComponentFactory.createDefaultSteps(5);
```

---

## 使用场景示例

### 场景一：基础用法

```java
public class BasicExample extends Application {
    @Override
    public void start(Stage stage) {
        // 创建步骤
        Step[] steps = {
            new Step("步骤一"),
            new Step("步骤二"),
            new Step("步骤三")
        };
        
        // 创建步骤条
        Steps stepsBar = new Steps(steps)
            .active(1); // 当前在第二步
        
        // 显示
        StackPane root = new StackPane(stepsBar.getNode());
        stage.setScene(new Scene(root, 600, 200));
        stage.show();
    }
}
```

### 场景二：带描述和状态

```java
Steps steps = new Steps()
    .addStep(new Step("用户注册", "填写基本信息"))
    .addStep(new Step("邮箱验证", "验证邮箱地址"))
    .addStep(new Step("身份认证", "上传证件照片"))
    .addStep(new Step("完成注册", "开始使用服务"))
    .active(2)
    .finishStatus(StepStatus.SUCCESS); // 已完成步骤显示成功状态
```

### 场景三：垂直布局

```java
Steps steps = new Steps()
    .addStep(new Step("选择商品", "浏览并选择心仪商品"))
    .addStep(new Step("确认订单", "核对订单信息"))
    .addStep(new Step("支付订单", "选择支付方式"))
    .addStep(new Step("完成购买", "等待商品发货"))
    .direction(Orientation.VERTICAL) // 垂直布局
    .active(1);
```

### 场景四：简洁模式

```java
// 简洁模式：alignCenter、description、direction、space 属性失效
Steps steps = new Steps()
    .addStep(new Step("已下单"))
    .addStep(new Step("运输中"))
    .addStep(new Step("已签收"))
    .simple(true) // 启用简洁模式
    .active(1);
```

### 场景五：居中对齐

```java
Steps steps = new Steps()
    .addStep(new Step("步骤一"))
    .addStep(new Step("步骤二"))
    .addStep(new Step("步骤三"))
    .alignCenter(true) // 居中对齐
    .active(0);
```

### 场景六：自定义主题

```java
// 创建紫色主题
StepsTheme purpleTheme = new StepsTheme.Builder()
    .primaryColor(Color.valueOf("#9B59B6"))
    .successColor(Color.valueOf("#27AE60"))
    .errorColor(Color.valueOf("#E74C3C"))
    .textColor(Color.valueOf("#2C3E50"))
    .titleFontSize(16)
    .iconSize(28)
    .build();

Steps steps = new Steps()
    .addStep(new Step("设计", "UI/UX设计阶段"))
    .addStep(new Step("开发", "功能开发阶段"))
    .addStep(new Step("测试", "质量测试阶段"))
    .addStep(new Step("上线", "产品发布阶段"))
    .theme(purpleTheme)
    .active(1);
```

### 场景七：交互控制

```java
Steps steps = new Steps()
    .addStep(new Step("第一步"))
    .addStep(new Step("第二步"))
    .addStep(new Step("第三步"))
    .onChange(index -> {
        System.out.println("步骤变化: " + index);
        // 可以在这里添加业务逻辑
        if (index == 2) {
            // 最后一步时执行特殊操作
        }
    })
    .onStepClick(index -> {
        System.out.println("点击了步骤: " + index);
        // 允许用户点击跳转到任意步骤
        steps.setActive(index);
    });

// 导航按钮
Button nextBtn = new Button("下一步");
nextBtn.setOnAction(e -> steps.next());

Button prevBtn = new Button("上一步");
prevBtn.setOnAction(e -> steps.prev());
```

### 场景八：使用工厂类

```java
// 快速创建
InteractiveStepsPane pane = StepsComponentFactory.createInteractive();
Steps steps = pane.getSteps();

// Builder 配置创建
InteractiveStepsPane pane = StepsComponentFactory.builder()
    .title("订单流程")
    .description("请按照步骤完成订单")
    .active(0)
    .finishStatus(StepStatus.SUCCESS)
    .onChange(index -> updateUI(index))
    .buildInteractive();
```

---

## 主题定制指南

### 完全自定义主题

```java
StepsTheme customTheme = new StepsTheme.Builder()
    // 颜色配置
    .primaryColor(Color.valueOf("#3498DB"))    // 主色调
    .successColor(Color.valueOf("#2ECC71"))    // 成功色
    .errorColor(Color.valueOf("#E74C3C"))      // 错误色
    .warningColor(Color.valueOf("#F39C12"))    // 警告色
    .waitColor(Color.valueOf("#BDC3C7"))       // 等待色
    .finishColor(Color.valueOf("#34495E"))     // 完成色
    .textColor(Color.valueOf("#2C3E50"))       // 文字色
    .descriptionColor(Color.valueOf("#7F8C8D")) // 描述色
    .lineColor(Color.valueOf("#BDC3C7"))       // 线条色
    .finishLineColor(Color.valueOf("#3498DB")) // 完成线条色
    .backgroundColor(Color.WHITE)              // 背景色
    // 字体配置
    .fontFamily("Microsoft YaHei")
    .titleFontSize(16)
    .descriptionFontSize(13)
    .iconFontSize(15)
    // 尺寸配置
    .iconSize(30)
    .lineHeight(3)
    .simpleArrowSize(14)
    .stepPadding(12)
    .build();
```

### 深色主题示例

```java
StepsTheme darkTheme = new StepsTheme.Builder()
    .primaryColor(Color.valueOf("#4FC3F7"))
    .successColor(Color.valueOf("#81C784"))
    .errorColor(Color.valueOf("#E57373"))
    .warningColor(Color.valueOf("#FFB74D"))
    .waitColor(Color.valueOf("#616161"))
    .finishColor(Color.valueOf("#E0E0E0"))
    .textColor(Color.WHITE)
    .descriptionColor(Color.valueOf("#B0BEC5"))
    .lineColor(Color.valueOf("#424242"))
    .backgroundColor(Color.valueOf("#1E1E1E"))
    .build();
```

---

## 错误处理与异常

### 边界条件处理

组件内部已处理以下边界情况：

| 场景 | 处理方式 |
|------|----------|
| active < 0 | 自动修正为 0 |
| active >= steps.size() | 自动修正为最大索引 |
| 空步骤列表 | 组件正常显示，active 只能为 0 |
| steps 列表减少时 active 超出 | 自动调整 active 到有效范围 |

### 常见异常

| 异常类型 | 触发场景 | 处理建议 |
|----------|----------|----------|
| `NullPointerException` | 主题为 null 时 | 使用 StepsTheme.DEFAULT 或自定义主题 |
| `IndexOutOfBoundsException` | 不会发生 | 组件内部已做边界检查 |

### 日志记录

组件使用 `java.util.logging.Logger` 记录以下日志：

| 级别 | 消息 |
|------|------|
| FINE | 布局刷新、简洁模式启用、资源释放 |
| WARNING | 移除监听器异常、清理步骤异常 |

---

## 性能优化建议

### 1. 避免频繁刷新

```java
// 推荐：批量添加步骤
Steps steps = new Steps();
steps.addSteps(step1, step2, step3, step4); // 一次添加

// 不推荐：逐个添加后刷新
steps.addStep(step1);
steps.addStep(step2); // 每次添加都触发刷新
```

### 2. 复用主题对象

```java
// 推荐：复用主题
StepsTheme theme = StepsTheme.DARK;
steps1.setTheme(theme);
steps2.setTheme(theme); // 同一主题对象

// 不推荐：每次创建新主题
steps1.setTheme(new StepsTheme.Builder().build());
steps2.setTheme(new StepsTheme.Builder().build());
```

### 3. 及时释放资源

```java
// 推荐：在窗口关闭时释放
stage.setOnCloseRequest(e -> steps.dispose());

// 或在组件不再使用时
@Override
public void stop() {
    steps.dispose();
}
```

### 4. 使用简洁模式

对于简单场景，简洁模式渲染更快：

```java
Steps steps = new Steps()
    .simple(true)
    .addStep(new Step("步骤一"))
    .addStep(new Step("步骤二"));
```

---

## 最佳实践

### 1. 资源管理

```java
public class MyController {
    private Steps steps;
    
    public void initialize() {
        steps = new Steps();
        // 初始化组件...
    }
    
    public void dispose() {
        if (steps != null) {
            steps.dispose();
            steps = null;
        }
    }
}
```

### 2. 线程安全

```java
// 错误：在后台线程直接修改
new Thread(() -> {
    steps.setActive(2); // 可能在非JavaFX线程执行
}).start();

// 正确：使用 Platform.runLater
new Thread(() -> {
    Platform.runLater(() -> steps.setActive(2));
}).start();
```

### 3. 状态管理

```java
// 推荐：使用 finishStatus 和 processStatus
Steps steps = new Steps()
    .finishStatus(StepStatus.SUCCESS) // 已完成步骤显示成功图标
    .processStatus(StepStatus.ERROR)  // 当前步骤显示错误状态
    .active(2);

// 为特定步骤设置独立状态
Step errorStep = new Step("验证失败");
errorStep.setStatus(StepStatus.ERROR);
```

### 4. 响应式绑定

```java
// 与其他组件属性绑定
Label statusLabel = new Label();
statusLabel.textProperty().bind(
    Bindings.createStringBinding(
        () -> "当前步骤: " + (steps.activeProperty().get() + 1),
        steps.activeProperty()
    )
);

// 按钮禁用状态绑定
Button nextBtn = new Button("下一步");
nextBtn.disableProperty().bind(steps.isLastProperty());
```

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-04-16 | 初始版本，包含核心功能 |

---

## 相关资源

- [JavaFX 官方文档](https://openjfx.io/)
- [Element UI Steps 组件](https://element.eleme.io/#/zh-CN/component/steps)
