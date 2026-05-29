# Aurora-FX Tour 漫游引导组件 API 文档

> **版本**：1.1
> **运行环境**：JavaFX 21+ / Java 17+（项目实际使用 JavaFX 25 + Java 25）
> **设计参考**：Element Plus `Tour`、Ant Design `Tour`、Flutter `showcaseview`
> **包路径**：`io.aurora.fx.components.tour`

本文档目标：让开发者**无需查看源码**即可掌握 Tour 组件的全部能力。所有接口、参数、返回值、回调、样式属性、内部坐标算法、ScrollPane / 窗口位置变化处理机制均做完整说明。

---

## 目录

1. [概述与特性](#1-概述与特性)
2. [快速开始](#2-快速开始)
3. [核心类总览](#3-核心类总览)
4. [TourPlacement —— 12 种定位枚举](#4-tourplacement)
5. [TourType —— 弹窗类型枚举](#5-tourtype)
6. [TourTarget —— 目标包装器](#6-tourtarget)
7. [TourMaskConfig —— 遮罩配置](#7-tourmaskconfig)
8. [TourTheme —— 主题配置](#8-tourtheme)
9. [TourStep —— 单步配置](#9-tourstep)
10. [Tour —— 主控制器](#10-tour)
11. [TourFactory —— 工厂与 Builder](#11-tourfactory)
12. [事件回调机制](#12-事件回调机制)
13. [配置选项默认值汇总表](#13-配置选项默认值汇总表)
14. [自定义样式 / CSS 类名](#14-自定义样式--css-类名)
15. [使用示例（场景驱动）](#15-使用示例场景驱动)
16. [坐标转换与定位算法](#16-坐标转换与定位算法)
17. [ScrollPane 与窗口位置变化处理机制](#17-scrollpane-与窗口位置变化处理机制)
18. [JavaFX 组件兼容性说明](#18-javafx-组件兼容性说明)
19. [性能注意事项与最佳实践](#19-性能注意事项与最佳实践)
20. [常见问题与故障排除](#20-常见问题与故障排除)

---

## 1. 概述与特性

`Tour` 是基于 JavaFX 的**漫游式新手引导组件**，可以为任意 `Node`（按钮、表单、列表、画布……）显示分步骤的引导卡片，并通过遮罩高亮目标区域。

### 核心特性

| 特性 | 说明 |
| --- | --- |
| **12 + 1 种定位** | `top` / `bottom` / `left` / `right` 各 3 种对齐 + `center`（空目标居中） |
| **自适应翻转** | 当原始 placement 方向空间不足时自动翻到对侧（element-plus 同款行为） |
| **三种目标形态** | `Node` 自动跟随 / `Rectangle2D` 固定坐标 / `empty()` 屏幕居中 |
| **模态 / 非模态** | `mask=true` 时遮罩拦截事件，`mask=false` 时弹窗悬浮但不阻塞操作 |
| **遮罩深度可配** | 支持镂空圆角、外扩 padding、高亮描边、点击关闭 |
| **多套预设主题** | `DEFAULT` / `DARK` / `PRIMARY_BLUE` / `PRIMARY_GREEN`，并可 Builder 完全自定义 |
| **完整生命周期回调** | `onOpen` / `onClose` / `onFinish` / `onChange` 与每步 `onShow` / `onHide` |
| **自定义插槽** | `indicatorSlot` / `contentSlot` / `footerSlot` 替换默认 UI |
| **键盘 / 遮罩关闭** | ESC 关闭、`dismissOnMaskClick` 点击遮罩关闭 |
| **滚动与窗口跟随** | 监听 `localToSceneTransform` + 全祖先链 + ScrollPane `hvalue/vvalue` + Scene 尺寸变化 |
| **资源安全释放** | `dispose()` 一键解绑全部监听器，避免内存泄漏 |

### 设计哲学

- **Element Plus 对齐**：12 种 placement、自适应翻转、`PRIMARY` 风格高亮按钮均参照 element-plus。
- **JavaFX 原生**：所有可观测属性均提供 `xxxProperty()`，便于绑定到 ViewModel。
- **零侵入**：Tour 不会修改宿主容器结构，只在最顶层挂载一个 overlay `Pane`，关闭时自动移除。

---

## 2. 快速开始

最少 4 行代码即可启动一个引导：

```java
Tour tour = TourFactory.builder()
        .step(button1, "标题 1", "请先点击此按钮", TourPlacement.BOTTOM)
        .step(button2, "标题 2", "再操作此按钮", TourPlacement.RIGHT)
        .step(button3, "完成", "全部就绪！", TourPlacement.TOP)
        .build();

tour.show(scene);   // 也可以传 Pane：tour.show(rootPane);
```

退出方式：

- 点击主按钮直至最后一步触发 `onFinish`
- 点击右上角关闭按钮（`showClose=true` 时）
- 按 `ESC` 键（`closeOnEsc=true` 时）
- 点击遮罩区域（`maskConfig.dismissOnMaskClick=true` 时）
- 代码主动调用 `tour.close()` / `tour.finish()`

---

## 3. 核心类总览

```
io.aurora.fx.components.tour
├─ Tour                  主控制器 - 引导的生命周期与 UI 协调
├─ TourStep              单步配置 - 标题/描述/目标/局部主题/局部遮罩/插槽/回调
├─ TourTarget            目标包装器（Node / Rectangle2D / empty）
├─ TourPlacement   enum  12 种弹窗定位 + CENTER
├─ TourType        enum  DEFAULT / PRIMARY 弹窗风格
├─ TourMaskConfig        遮罩颜色/不透明度/padding/圆角/高亮/点击关闭
├─ TourTheme             主题（颜色 + 字体 + 尺寸 一站式）
└─ TourFactory           创建 Tour 实例 / 演示 Pane / 提供链式 Builder
```

继承关系：

- `Tour` / `TourStep` / `TourTarget` / `TourMaskConfig` / `TourTheme` 均不继承任何 JavaFX 控件，是纯 POJO + 控制类。
- `Tour` 内部使用 `Pane`（overlay）、`Path`（mask）、`VBox`（popup）、`Polygon`（arrow）。

---

## 4. TourPlacement

定义引导卡片相对目标的 12 种定位方向 + 1 个 `CENTER`（仅用于空目标）。

### 完整枚举值表

| 枚举值 | value | 含义 | 弹窗位置 |
| --- | --- | --- | --- |
| `TOP` | `top` | 上方居中 | 弹窗在目标上方，左右居中对齐目标中心 |
| `TOP_START` | `top-start` | 上方左对齐 | 弹窗在目标上方，左边缘对齐目标左边缘 |
| `TOP_END` | `top-end` | 上方右对齐 | 弹窗在目标上方，右边缘对齐目标右边缘 |
| `BOTTOM` | `bottom` | 下方居中（**默认**） | 弹窗在目标下方，左右居中 |
| `BOTTOM_START` | `bottom-start` | 下方左对齐 | 弹窗在目标下方，左边缘对齐 |
| `BOTTOM_END` | `bottom-end` | 下方右对齐 | 弹窗在目标下方，右边缘对齐 |
| `LEFT` | `left` | 左侧居中 | 弹窗在目标左侧，上下居中 |
| `LEFT_START` | `left-start` | 左侧顶对齐 | 弹窗在目标左侧，上边缘对齐 |
| `LEFT_END` | `left-end` | 左侧底对齐 | 弹窗在目标左侧，下边缘对齐 |
| `RIGHT` | `right` | 右侧居中 | 弹窗在目标右侧，上下居中 |
| `RIGHT_START` | `right-start` | 右侧顶对齐 | 弹窗在目标右侧，上边缘对齐 |
| `RIGHT_END` | `right-end` | 右侧底对齐 | 弹窗在目标右侧，下边缘对齐 |
| `CENTER` | `center` | 容器中央 | **仅供空目标使用**，弹窗在 host 中心 |

### 公共方法

```java
String  getValue();                      // 获取小写连字符字符串值
boolean isTop();                         // 当前是否为 TOP / TOP_START / TOP_END
boolean isBottom();                      // 当前是否为 BOTTOM 系列
boolean isLeft();                        // 当前是否为 LEFT 系列
boolean isRight();                       // 当前是否为 RIGHT 系列
boolean isHorizontalAxis();              // TOP/BOTTOM 系列返回 true（弹窗在水平方向延展）
boolean isVerticalAxis();                // LEFT/RIGHT 系列返回 true（弹窗在垂直方向延展）

static TourPlacement fromValue(String);  // 字符串解析；空/未知返回 BOTTOM
```

### 自适应翻转规则（重要）

Tour 在内部会调用 `flipPlacementIfNeeded(...)`：当目标贴近容器边界，原始方向无法容纳整个 popup，且对侧能容纳时，会自动翻转：

- `BOTTOM*` ⇄ `TOP*`：bottom 空间不足时翻为 top（保留对齐方式）
- `TOP*` ⇄ `BOTTOM*`：top 空间不足时翻为 bottom
- `RIGHT*` ⇄ `LEFT*`：right 空间不足时翻为 left
- `LEFT*` ⇄ `RIGHT*`：left 空间不足时翻为 right

无可翻转空间时**保持原 placement**。

---

## 5. TourType

```java
public enum TourType {
    DEFAULT,    // 浅色背景 + 深色文字（白底卡片，配合 mask=true 使用）
    PRIMARY     // 主色填充 + 白色文字（建议与 mask=false 组合，做轻量提示）
}
```

| 值 | 推荐场景 | 视觉效果 |
| --- | --- | --- |
| `DEFAULT` | 强引导、新手教程 | 弹窗白底、indicator 蓝色圆点、按钮蓝色主按钮 |
| `PRIMARY` | 非模态轻提示 | 弹窗主色背景（默认蓝）、indicator 白色圆点、按钮白底彩字 |

`PRIMARY` 类型下次按钮文本默认变为白色，secondary 按钮使用半透明白底。

---

## 6. TourTarget

`Tour` 的目标抽象。屏蔽 Node / 矩形 / 空目标三种目标，统一返回场景坐标 `Rectangle2D`。

### 工厂方法

| 方法 | 参数 | 返回 | 说明 |
| --- | --- | --- | --- |
| `TourTarget.of(Node node)` | `node` 可为 `null` | `TourTarget` | 推荐方式，自动跟随节点位置变化 |
| `TourTarget.of(Rectangle2D rect)` | `rect` 可为 `null` | `TourTarget` | 固定场景坐标矩形 |
| `TourTarget.of(double x, double y, double w, double h)` | 场景坐标分量 | `TourTarget` | 等同于上一项 |
| `TourTarget.empty()` | 无 | `TourTarget` | 空目标，弹窗居中显示 |

### 实例方法

| 方法 | 返回 | 说明 |
| --- | --- | --- |
| `boolean isEmpty()` | `boolean` | 是否为空目标 |
| `boolean isNodeBased()` | `boolean` | 是否为 Node 包装 |
| `Rectangle2D resolveSceneBounds()` | `Rectangle2D` 或 `null` | 实时计算场景坐标矩形；Node 未挂载 Scene 或宽高 ≤ 0 时返回 `null` |
| `Point2D resolveCenter()` | `Point2D` 或 `null` | 实时计算目标中心点（场景坐标） |
| `Node getNode()` | `Node` | 包装的 Node（rect 模式返回 null） |
| `Rectangle2D getRect()` | `Rectangle2D` | 包装的矩形（node 模式返回 null） |

### 边界情况

- 当 `isNodeBased()` 且 `node.getScene() == null` 时，`resolveSceneBounds()` 返回 `null`，Tour 视为"空目标"，弹窗会落到 `CENTER`。
- 当 `Node` 已被布局但宽高均为 0（如未 visible 的 spacer），同样视为空目标。

---

## 7. TourMaskConfig

控制遮罩层（半透明黑色蒙版 + 镂空高亮区）外观。

### 完整属性表

| 属性 | 类型 | 默认 | 范围 / 说明 |
| --- | --- | --- | --- |
| `color` | `Color` | `#000000` | 遮罩主色（不含 alpha，alpha 通过 `opacity` 控制） |
| `opacity` | `double` | `0.5` | 遮罩不透明度，自动 clamp 到 `[0, 1]` |
| `padding` | `double` | `4.0` | 镂空区相对目标的外扩像素，自动 ≥ 0 |
| `cornerRadius` | `double` | `4.0` | 镂空圆角半径，自动 ≥ 0 |
| `highlight` | `boolean` | `false` | 是否在镂空区四周绘制描边 |
| `highlightColor` | `Color` | `#FFFFFF` (alpha 0.85) | 描边颜色 |
| `highlightWidth` | `double` | `2.0` | 描边宽度 |
| `dismissOnMaskClick` | `boolean` | `false` | 点击遮罩区域是否关闭 Tour |

### Builder 用法

```java
TourMaskConfig mask = TourMaskConfig.builder()
        .color(Color.web("#0F1117"))     // 蒙版颜色
        .opacity(0.55)                    // 不透明度
        .padding(8)                       // 镂空区比目标外扩 8px
        .cornerRadius(12)                 // 大圆角
        .highlight(true)                  // 启用描边
        .highlightColor(Color.web("#5BA8FF"))
        .highlightWidth(2)
        .dismissOnMaskClick(false)        // 点击遮罩不关闭
        .build();
```

### 预设

`TourMaskConfig.DEFAULT` —— 等价于 `builder().build()`。

### Tip：模态 vs 非模态

- **模态**（`Tour.mask(true)` + 默认 maskConfig）：遮罩拦截鼠标事件，强制用户跟随引导。
- **非模态**（`Tour.mask(false)`）：不显示遮罩，下层 UI 完全可交互。
- **半模态**（`Tour.mask(true)` + `dismissOnMaskClick(true)`）：显示遮罩但允许点击空白处关闭。

---

## 8. TourTheme

集中管理弹窗外观（颜色 + 字体 + 尺寸）的不可变对象，必须通过 `Builder` 构造。

### 颜色属性

| 属性 | 默认 | 用途 |
| --- | --- | --- |
| `primaryColor` | `#409EFF` | 主色：主按钮背景、indicator 激活态、PRIMARY 类型弹窗背景 |
| `popupBackground` | `WHITE` | 弹窗背景色（设置时同步更新 `arrowFillColor`） |
| `popupBorderColor` | `#EBEEF5` | 弹窗边框色 |
| `titleColor` | `#303133` | 标题文字颜色 |
| `descriptionColor` | `#606266` | 描述文字颜色 |
| `buttonTextColor` | `WHITE` | 主按钮文字色 |
| `secondaryButtonBg` | `#F4F4F5` | 次按钮背景 |
| `secondaryButtonText` | `#606266` | 次按钮文字 |
| `closeIconColor` | `#909399` | 关闭按钮 X 描边色 |
| `indicatorActiveColor` | `#409EFF` | 圆点 indicator 激活色 |
| `indicatorInactiveColor` | `#DCDFE6` | 圆点 indicator 非激活色 |
| `arrowFillColor` | `WHITE` | 箭头填充色（默认与 popupBackground 同步） |

### 字体属性

| 属性 | 默认 | 用途 |
| --- | --- | --- |
| `fontFamily` | `"Microsoft YaHei"` | 字体族 |
| `titleFontSize` | `15` | 标题字号 |
| `descriptionFontSize` | `13` | 描述字号 |
| `buttonFontSize` | `12` | 按钮字号 |
| `indicatorFontSize` | `12` | 指示器字号（如显示 "1/4"） |

### 尺寸属性

| 属性 | 默认 | 用途 |
| --- | --- | --- |
| `popupMinWidth` | `260` | 弹窗最小宽度 |
| `popupMaxWidth` | `360` | 弹窗最大宽度 |
| `popupCornerRadius` | `8` | 弹窗圆角 |
| `popupPadding` | `16` | 弹窗内边距 |
| `popupArrowSize` | `8` | 箭头三角形大小 |
| `popupOffset` | `12` | 弹窗与目标之间的间距 |
| `indicatorDotSize` | `6` | 圆点直径 |
| `dropShadowRadius` | `12` | 投影模糊半径 |

### 预设主题

| 常量 | 风格描述 | 适用 |
| --- | --- | --- |
| `TourTheme.DEFAULT` | 浅色 + 主蓝 #409EFF | 默认场景 |
| `TourTheme.DARK` | 深色卡片（#2B2B2B）+ 主蓝 | 深色应用、夜间模式 |
| `TourTheme.PRIMARY_BLUE` | 主色为 #1890FF（Ant Design 蓝） | 与 `TourType.PRIMARY` 搭配 |
| `TourTheme.PRIMARY_GREEN` | 主色为 #52C41A | 与 `TourType.PRIMARY` 搭配 |

### Builder

```java
TourTheme theme = TourTheme.builder()
        .primaryColor(Color.web("#722ED1"))    // 紫色主题
        .popupBackground(Color.web("#FAFAFA"))
        .popupBorderColor(Color.web("#E0E0E0"))
        .titleFontSize(16)
        .descriptionFontSize(13)
        .popupCornerRadius(10)
        .popupArrowSize(10)
        .dropShadowRadius(20)
        .fontFamily("PingFang SC")
        .build();
```

### 工具方法

```java
public static String toCssColor(Color color);   // 把 Color 转为 "rgba(r,g,b,a)" 字符串，用于 -fx-* 样式
```

---

## 9. TourStep

单个引导步骤的完整配置。所有可观察字段均提供 JavaFX `Property` 对外暴露，便于绑定。

### 构造方法

```java
new TourStep();                              // 空步骤，后续通过链式方法配置
new TourStep("标题");                        // 仅设置标题
new TourStep("标题", "描述");                // 标题 + 描述
new TourStep(node, "标题", "描述");          // 完整四要素：node + title + description（placement 默认 BOTTOM）
```

### 链式 API

| 方法 | 参数 | 说明 |
| --- | --- | --- |
| `title(String)` | 标题文本 | 设置标题 |
| `description(String)` | 描述文本 | 设置描述 |
| `placement(TourPlacement)` | 12 种之一 | `null` 时回退为 `BOTTOM` |
| `target(Node)` | 目标节点 | 等价于 `target(TourTarget.of(node))` |
| `target(Rectangle2D)` | 场景矩形 | 用于指向坐标区域（如 TextArea 内文本片段） |
| `target(TourTarget)` | 目标包装 | `null` 时回退为 `TourTarget.empty()` |
| `theme(TourTheme)` | 局部主题 | `null` 表示沿用 Tour 全局主题 |
| `maskConfig(TourMaskConfig)` | 局部遮罩 | `null` 沿用全局 |
| `type(TourType)` | 局部类型 | `null` 沿用全局 |
| `showMask(Boolean)` | 三态 | `null` 表示沿用全局 `Tour.mask(...)`；`true/false` 强制本步开关 |
| `nextText(String)` | 主按钮文案 | 覆盖该步骤主按钮文字 |
| `prevText(String)` | 次按钮文案 | 覆盖该步骤上一步按钮文字 |
| `indicatorSlot(Node)` | 自定义节点 | 替换默认圆点 indicator |
| `contentSlot(Node)` | 自定义节点 | 替换 description 文本区 |
| `footerSlot(Node)` | 自定义节点 | 替换默认按钮 / indicator 区 |
| `onShow(Consumer<TourStep>)` | 回调 | 该步骤显示时触发 |
| `onHide(Consumer<TourStep>)` | 回调 | 该步骤切换离开 / 关闭时触发 |

### 资源管理

```java
public void dispose();   // 清空属性 + 解除回调引用，避免内存泄漏
```

`Tour.dispose()` 会自动对所有步骤调用此方法。

### 示例

```java
TourStep step = new TourStep()
        .target(myButton)
        .title("功能入口")
        .description("点击这里可以进入新功能")
        .placement(TourPlacement.BOTTOM_START)
        .nextText("我知道了")
        .onShow(s -> System.out.println("显示步骤: " + s.getTitle()))
        .onHide(s -> System.out.println("离开步骤: " + s.getTitle()));
```

---

## 10. Tour

引导主控制器。负责挂载 overlay、渲染 popup、绑定监听、调度生命周期。

### 构造与初始化

```java
public Tour();   // 创建空 Tour；初始化 overlay/mask/popup/arrow 但未挂载
```

### Builder 风格全局配置

| 方法 | 参数 / 默认 | 说明 |
| --- | --- | --- |
| `addStep(TourStep)` | 步骤 | 追加一步 |
| `addSteps(TourStep...)` | 多步 | 批量追加 |
| `mask(boolean)` | `true` | 是否显示遮罩 |
| `maskConfig(TourMaskConfig)` | DEFAULT | 全局遮罩配置；`null` 时回退到 DEFAULT |
| `theme(TourTheme)` | DEFAULT | 全局主题；`null` 回退到 DEFAULT |
| `type(TourType)` | DEFAULT | 全局类型；`null` 回退到 DEFAULT |
| `showClose(boolean)` | `true` | 是否显示关闭按钮 |
| `showArrow(boolean)` | `true` | 是否显示箭头 |
| `showIndicators(boolean)` | `true` | 是否显示步骤指示器 |
| `closeOnEsc(boolean)` | `true` | ESC 是否关闭 Tour |
| `prevButtonText(String)` | `"上一步"` | 上一步按钮默认文案 |
| `nextButtonText(String)` | `"下一步"` | 下一步按钮默认文案 |
| `finishButtonText(String)` | `"完成"` | 最后一步按钮默认文案 |
| `onOpen(Runnable)` | `null` | 引导启动回调 |
| `onClose(Runnable)` | `null` | 引导关闭回调（含 finish） |
| `onFinish(Runnable)` | `null` | 引导走完最后一步的回调 |
| `onChange(Consumer<Integer>)` | `null` | 步骤索引变化回调 |

### 生命周期方法

| 方法 | 异常情况 | 说明 |
| --- | --- | --- |
| `Tour show(Scene scene)` | scene 为 null → 仅记日志返回；root 不是 Pane → 仅记日志返回 | 在 Scene 中启动 |
| `Tour show(Pane container)` | disposed 已释放 / active 已运行 / steps 为空 → 直接返回 | 在指定 Pane 中启动 |
| `void close()` | 未 active 时直接返回 | 关闭引导（不触发 onFinish） |
| `void finish()` | 未 active 时直接返回 | 关闭并触发 `onFinish` |
| `void next()` | 未 active 时直接返回；最后一步会调用 `finish()` | 跳转到下一步 |
| `void prev()` | 未 active / 已是首步时返回 | 跳转到上一步 |
| `void goTo(int index)` | 越界时仅记日志 | 跳转到指定索引 |
| `void dispose()` | 已 dispose 时返回 | 释放全部资源（包含 active 时自动 close） |

`show(Scene)` 内部会检查 `scene.getRoot() instanceof Pane`：若不是 Pane 子类（如 Group），会记录警告并不启动。**建议根节点用 BorderPane / StackPane**。

### Property 访问

```java
ObservableList<TourStep>          getSteps();
IntegerProperty                   currentProperty();
BooleanProperty                   maskProperty();
ObjectProperty<TourMaskConfig>    maskConfigProperty();
ObjectProperty<TourTheme>         themeProperty();
ObjectProperty<TourType>          typeProperty();
BooleanProperty                   showCloseProperty();
BooleanProperty                   showArrowProperty();
BooleanProperty                   showIndicatorsProperty();
BooleanProperty                   closeOnEscProperty();
StringProperty                    prevButtonTextProperty();
StringProperty                    nextButtonTextProperty();
StringProperty                    finishButtonTextProperty();
```

### Getter 快捷方法

```java
int             getCurrent();
boolean         isMask();
TourMaskConfig  getMaskConfig();
TourTheme       getTheme();
TourType        getType();
boolean         isShowClose();
boolean         isShowArrow();
boolean         isShowIndicators();
boolean         isCloseOnEsc();
boolean         isActive();      // 引导是否正在运行
boolean         isDisposed();    // 是否已释放
List<TourStep>  getStepList();   // 返回 ArrayList 拷贝（避免外部修改 observable）
```

---

## 11. TourFactory

提供两类入口：

### 直接创建

```java
Tour                       TourFactory.createTour();      // 等价 new Tour()
BasicTourPane              TourFactory.createBasic();     // 演示 Pane（基础用法）
NonModalTourPane           TourFactory.createNonModal();  // 演示 Pane（非模态）
PlacementTourPane          TourFactory.createPlacement(); // 演示 Pane（12 种定位）
CustomMaskTourPane         TourFactory.createCustomMask();
CustomIndicatorTourPane    TourFactory.createCustomIndicator();
CenterTourPane             TourFactory.createCenter();
InteractiveTourPane        TourFactory.createInteractive();
```

### Builder 链式 API

`TourFactory.builder()` 返回一个 `Builder`，提供与 `Tour` 同名的链式方法，并对 `step` 提供更便捷的重载：

```java
Tour tour = TourFactory.builder()
        .mask(true)
        .type(TourType.DEFAULT)
        .theme(TourTheme.DEFAULT)
        .maskConfig(TourMaskConfig.DEFAULT)
        .showClose(true)
        .showArrow(true)
        .showIndicators(true)
        .closeOnEsc(true)
        .prevButtonText("上一步")
        .nextButtonText("下一步")
        .finishButtonText("完成")
        .step(button1, "标题", "描述")                           // 默认 BOTTOM
        .step(button2, "标题", "描述", TourPlacement.RIGHT)      // 指定 placement
        .step(new TourStep().target(rect2D).title("..."))        // 完全自定义
        .onOpen(()  -> { /* ... */ })
        .onClose(() -> { /* ... */ })
        .onFinish(() -> { /* ... */ })
        .onChange(idx -> { /* ... */ })
        .build();
```

---

## 12. 事件回调机制

### Tour 全局回调

| 回调 | 类型 | 触发时机 | 异常处理 |
| --- | --- | --- | --- |
| `onOpen` | `Runnable` | `show(...)` 成功挂载 overlay 后、渲染第一步前 | 异常被捕获并记录 WARNING |
| `onClose` | `Runnable` | `close()` 中触发；`finish()` 不会触发 | 异常被捕获 |
| `onFinish` | `Runnable` | `finish()` 中触发；最后一步点击"完成"会自动调用 finish | 异常被捕获 |
| `onChange` | `Consumer<Integer>` | `goTo(idx)` 中变更 current 后；`next()` / `prev()` 间接触发 | 异常被捕获 |

### 单步回调（TourStep）

| 回调 | 类型 | 触发时机 | 备注 |
| --- | --- | --- | --- |
| `onShow` | `Consumer<TourStep>` | 该步骤渲染完毕、popup 可见时 | 适合做埋点、动画启动 |
| `onHide` | `Consumer<TourStep>` | 该步骤被切换离开（next/prev/goTo）或 close/finish 前 | 适合做清理 |

### 触发顺序示意

```
show()
   ├─ onOpen
   ├─ render step[0]
   │    └─ step[0].onShow
   ├─ next()
   │    ├─ step[0].onHide
   │    ├─ onChange(1)
   │    └─ step[1].onShow
   ├─ ... 
   └─ finish() / close()
        ├─ stepCurrent.onHide
        └─ onClose（close）/ onFinish（finish）
```

### 线程模型

所有回调均在 **JavaFX Application Thread** 上触发，可直接读写 UI。

---

## 13. 配置选项默认值汇总表

| 配置项 | 默认值 | 出处 |
| --- | --- | --- |
| `Tour.mask` | `true` | Tour |
| `Tour.maskConfig` | `TourMaskConfig.DEFAULT` | Tour |
| `Tour.theme` | `TourTheme.DEFAULT` | Tour |
| `Tour.type` | `TourType.DEFAULT` | Tour |
| `Tour.showClose` | `true` | Tour |
| `Tour.showArrow` | `true` | Tour |
| `Tour.showIndicators` | `true` | Tour |
| `Tour.closeOnEsc` | `true` | Tour |
| `Tour.prevButtonText` | `"上一步"` | Tour |
| `Tour.nextButtonText` | `"下一步"` | Tour |
| `Tour.finishButtonText` | `"完成"` | Tour |
| `TourStep.placement` | `BOTTOM` | TourStep |
| `TourStep.target` | `TourTarget.empty()` | TourStep |
| `TourStep.theme` / `maskConfig` / `type` / `showMask` | `null`（沿用全局） | TourStep |
| `TourMaskConfig.color` | `#000000` | TourMaskConfig |
| `TourMaskConfig.opacity` | `0.5` | TourMaskConfig |
| `TourMaskConfig.padding` | `4` | TourMaskConfig |
| `TourMaskConfig.cornerRadius` | `4` | TourMaskConfig |
| `TourMaskConfig.highlight` | `false` | TourMaskConfig |
| `TourMaskConfig.highlightColor` | `rgba(255,255,255,0.85)` | TourMaskConfig |
| `TourMaskConfig.highlightWidth` | `2` | TourMaskConfig |
| `TourMaskConfig.dismissOnMaskClick` | `false` | TourMaskConfig |

---

## 14. 自定义样式 / CSS 类名

Tour 在内部为关键节点设置了 CSS class，外部可通过外部样式表进一步定制：

| 节点 | CSS class | 说明 |
| --- | --- | --- |
| Overlay 根节点 | `.aurora-tour-overlay` | 覆盖层 Pane，不可见但承载 mask/popup/arrow |
| Popup 容器 | `.aurora-tour-popup` | VBox，承载标题/描述/按钮 |
| 箭头 | `.aurora-tour-arrow` | Polygon，指向目标方向 |

### 自定义示例

```css
.aurora-tour-popup {
    -fx-background-color: #1F2937;
    -fx-text-fill: white;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 16, 0, 0, 4);
}
.aurora-tour-arrow {
    -fx-fill: #1F2937;
}
```

把样式表加入 Scene：

```java
scene.getStylesheets().add(getClass().getResource("/path/tour-custom.css").toExternalForm());
```

> 注意：`TourTheme` 通过 inline style 直接设置颜色，会**覆盖** CSS 类样式。若想以 CSS 为主，建议把 `TourTheme.popupBackground / titleColor / ...` 都设为 `Color.TRANSPARENT` 或保留默认再依赖 CSS 覆盖外观。

---

## 15. 使用示例（场景驱动）

### 15.1 基础用法 - 三步引导

```java
Tour tour = TourFactory.builder()
        .step(homeBtn, "首页", "回到主界面", TourPlacement.BOTTOM)
        .step(searchBtn, "搜索", "查找内容", TourPlacement.BOTTOM)
        .step(profileBtn, "个人中心", "管理你的账户", TourPlacement.LEFT_END)
        .onFinish(() -> System.out.println("引导完成"))
        .build();
tour.show(scene);
```

### 15.2 非模态 + PRIMARY 风格

```java
Tour tour = TourFactory.builder()
        .mask(false)
        .type(TourType.PRIMARY)
        .theme(TourTheme.PRIMARY_BLUE)
        .step(saveBtn, "保存", "随时点击保存进度", TourPlacement.BOTTOM_END)
        .build();
tour.show(scene);
```

### 15.3 空目标居中欢迎页

```java
Tour tour = TourFactory.builder()
        .step(new TourStep()
                .title("欢迎使用 Aurora-FX")
                .description("接下来 5 步带你了解全部功能")
                .placement(TourPlacement.CENTER))
        .step(button1, "...", "...")
        .build();
tour.show(scene);
```

### 15.4 自定义遮罩 + 镂空高亮

```java
TourMaskConfig mask = TourMaskConfig.builder()
        .opacity(0.6)
        .padding(8)
        .cornerRadius(12)
        .highlight(true)
        .highlightColor(Color.web("#5BA8FF"))
        .highlightWidth(2)
        .dismissOnMaskClick(true)
        .build();

Tour tour = TourFactory.builder()
        .maskConfig(mask)
        .step(target1, "...", "...")
        .build();
```

### 15.5 自定义指示器（进度条）

```java
ProgressBar bar = new ProgressBar(0.0);
bar.setPrefWidth(120);

Tour tour = new Tour();
TourStep s1 = new TourStep(target1, "步骤 1", "...")
        .indicatorSlot(bar);
tour.addStep(s1);

tour.onChange(idx -> bar.setProgress((idx + 1) / (double) tour.getSteps().size()));
```

### 15.6 完全自定义 footer

```java
HBox footer = new HBox(8);
Button skip = new Button("跳过引导");
Button next = new Button("下一步");
footer.getChildren().addAll(skip, next);
skip.setOnAction(e -> tour.close());
next.setOnAction(e -> tour.next());

step.footerSlot(footer);
```

### 15.7 指向 TextArea 内某一段文本（Rectangle2D 目标）

```java
TextArea ta = new TextArea("第一行\n第二行\n第三行");
ta.setFont(Font.font("Consolas", 14));

start.setOnAction(e -> {
    Bounds sb = ta.localToScene(ta.getBoundsInLocal());
    // 指向第二行（行高约 18px，padding ~ 6px）
    Rectangle2D line2 = new Rectangle2D(
            sb.getMinX() + 6, sb.getMinY() + 6 + 1 * 18,
            sb.getWidth() - 12, 18);

    Tour tour = TourFactory.builder()
            .step(new TourStep()
                    .target(line2)
                    .title("第二行")
                    .description("精确指向 TextArea 内的第二行文本")
                    .placement(TourPlacement.RIGHT))
            .build();
    tour.show(start.getScene());
});
```

> 完整可运行示例参见测试目录：`src/test/java/io/aurora/fx/components/tour/TourStandardNodeDemo.java` 中的 **TextArea-Content** 演示。

### 15.8 监听步骤变化做埋点

```java
Tour tour = TourFactory.builder()
        .onChange(idx -> analytics.report("tour_step_" + idx))
        .step(...)
        .build();
```

### 15.9 程序化跳转

```java
tour.show(scene);
Platform.runLater(() -> tour.goTo(2));   // 跳到第 3 步
```

### 15.10 释放资源

```java
@Override
public void stop() {
    if (tour != null) tour.dispose();
}
```

---

## 16. 坐标转换与定位算法

> 本节是 Tour 在复杂布局（嵌套 ScrollPane、缩放窗口、含 transform 的 Group）下保持精准定位的核心。理解算法可帮助你在异常场景下排查问题。

### 16.1 坐标系全景

Tour 内部涉及 4 个坐标系：

1. **Node local 坐标**：`node.boundsInLocal`
2. **Scene 坐标**：`node.localToScene(boundsInLocal)`，所有目标计算的"中转坐标"
3. **Host local 坐标**：`host.sceneToLocal(sceneBounds)`，最终落到 overlay 的坐标
4. **Window/Screen 坐标**：Tour 不依赖此层

### 16.2 主要算法步骤

```
renderCurrentStep()
   ├─ unbindTarget()                  # 清理上一步监听
   ├─ bindTarget(currentStep)         # 注册新监听
   ├─ buildPopupContent(step, theme)  # 构建标题/描述/按钮/指示器
   └─ scheduleLayout()                # 合并多次触发到同一帧
        └─ Platform.runLater(layoutOverlay)

layoutOverlay()
   ├─ 仅对 target 节点的 parent 链做 applyCss + layout（不污染 popup 子树）
   ├─ Rectangle2D sceneRect  = step.getTarget().resolveSceneBounds()
   ├─ Rectangle2D hostRect   = sceneRectToHost(sceneRect)
   ├─ updateMaskPath(hostRect, maskConfig)
   ├─ positionPopup(step, hostRect, theme)
   └─ popup.toFront(); arrow.toFront();   # 防御 z-order
```

### 16.3 sceneRectToHost - 4 角点稳健转换

```java
Bounds sceneB = new BoundingBox(sceneRect.getMinX(), sceneRect.getMinY(),
                                 sceneRect.getWidth(), sceneRect.getHeight());
Bounds local  = host.sceneToLocal(sceneB);   // 自动处理 4 角点变换
return new Rectangle2D(local.getMinX(), local.getMinY(),
                       Math.max(0, local.getWidth()),
                       Math.max(0, local.getHeight()));
```

使用 `Bounds` 整体变换替代单点变换 —— 即使 host 存在旋转、缩放、翻转，得到的矩形仍然是 axis-aligned 的合法外接框。

### 16.4 自适应翻转 (`flipPlacementIfNeeded`)

伪码：

```
if placement == BOTTOM*:
    if 下方放不下 popup AND 上方能放下 popup:
        翻转为对应的 TOP*
elif placement == TOP*:    类似
elif placement == LEFT*:   类似
elif placement == RIGHT*:  类似
```

翻转保留对齐方式（START/END/居中），与 element-plus 行为一致。

### 16.5 软约束 (`softClampHorizontal/Vertical`)

仅在与目标垂直的轴向做轻微 clamp，避免 popup 完全飞出 host 边界，**且不会把 popup 拉离指向轴**：

```java
private double softClampHorizontal(double x, double pw, double hostW) {
    if (pw >= hostW) return x;            // popup 比 host 还宽时不动
    if (x < 0)              return Math.max(x, -pw + 24);
    if (x + pw > hostW)     return Math.min(x, hostW - 24);
    return x;
}
```

`24` 是兜底可见像素数 —— 即使被裁切也至少留 24px 让用户可见。

### 16.6 CENTER 路径

```java
popup.setLayoutX((hostW - pwActual) / 2);
popup.setLayoutY((hostH - phActual) / 2);
```

不使用 `Math.max(0, ...)`，允许 popup > host 时使用负坐标实现真正的中心对齐。

### 16.7 箭头定位

`positionArrow(...)` 根据最终生效的 placement 把 Polygon 三角形旋转到正确方向，并贴在 popup 朝向目标的边上，水平/垂直坐标根据目标中心点对齐。

---

## 17. ScrollPane 与窗口位置变化处理机制

> Tour 难点：JavaFX 的 ScrollPane 通过 `Affine` transform 实现滚动，**滚动不会触发 boundsInParent 变化**。窗口移动到屏幕另一位置同样不触发 bounds 事件。Tour 通过多重监听确保这些场景下都能正确跟随。

### 17.1 监听器全景

`bindTarget(node)` 中注册了下列监听：

| 监听对象 | 监听属性 | 用途 |
| --- | --- | --- |
| 目标 Node | `boundsInLocalProperty` | 节点自身尺寸变化 |
| 目标 Node | `boundsInParentProperty` | 节点在 parent 中位置变化 |
| 目标 Node | `layoutXProperty` / `layoutYProperty` | 节点 layout 变化 |
| 目标 Node | `sceneProperty` | 节点挂载 / 卸载场景 |
| 目标 Node | `localToSceneTransformProperty` | **关键**：感知任何祖先 transform / 滚动变化 |
| **每个祖先节点** | `boundsInParent` + `layoutX/Y` | 父链布局变化 |
| **每个 ScrollPane 祖先** | `hvalueProperty` + `vvalueProperty` | **关键**：滚动不会触发 bounds，必须直接监听滚动条值 |
| Scene | `widthProperty` + `heightProperty` | 窗口缩放时重新定位 |
| Host | `widthProperty` + `heightProperty` | overlay 大小同步 |

### 17.2 调度合并

任何监听触发都会调用 `scheduleLayout()`：使用 token + `Platform.runLater` 合并同一帧内的多次触发，仅在最新 token 下执行 `layoutOverlay`，避免无谓重布局。

### 17.3 解绑 (`unbindTarget`)

每次 `goTo`/`next`/`prev` 切换步骤前都会解除上一目标的全部监听，并清理 `trackedAncestors` / `trackedScrollPanes`。`close()` / `finish()` / `dispose()` 同样会调用。

### 17.4 窗口非全屏 / 屏幕任意位置

由于所有监听基于 Node 的 **scene 坐标**（而非 screen 坐标），窗口被拖到屏幕任意位置都不影响 Tour 定位。不需要额外处理。

### 17.5 已知限制

- 若目标 Node 嵌套在自定义 `Skin` 内，且该 Skin 通过自有动画移动节点而**未触发** `boundsInParent` 变化，需要手动调用 `tour.goTo(tour.getCurrent())` 或在动画 onFinished 里调用以触发刷新。
- 若 host 容器本身被嵌入到 `WebView` 等非标准 JavaFX 容器内，`sceneToLocal` 行为不保证，建议把 Tour 的 host 设为最外层 Stage 根。

---

## 18. JavaFX 组件兼容性说明

### 18.1 受支持目标节点

任何继承自 `javafx.scene.Node` 的对象均可作为目标。已在演示中验证的 27 类标准控件：

`Button` `ToggleButton` `Hyperlink` `TextField` `PasswordField` `TextArea`
`CheckBox` `RadioButton` `ComboBox` `ChoiceBox` `DatePicker` `ColorPicker`
`Spinner` `Slider` `ProgressBar` `ProgressIndicator` `ScrollBar`
`ListView` `TableView` `TreeView` `TreeTableView`
`Canvas` `ImageView` `Separator` `Pagination` `TitledPane`
（再加上 TextArea 内部的文本片段 - 通过 Rectangle2D 目标）

### 18.2 host 容器要求

- `Tour.show(Scene)` 要求 `scene.getRoot() instanceof Pane`。**Group 不被支持**（无法挂载子节点到 overlay 的方式）。
- 推荐的根布局：`StackPane` / `BorderPane` / `AnchorPane` / `Pane`。

### 18.3 ScrollPane 嵌套

Tour 完整支持 ScrollPane 滚动跟随。但需注意：

- `ScrollPane.content` 必须是 `Pane`（默认即是）
- 滚动到目标完全不可见时，Tour 仍会按目标 scene 坐标定位 popup —— 即使 popup 落到 host 可见区外，软约束会保持其至少 24px 可见。
- 若希望"目标不可见就不显示 popup"，可在 `onShow` 中检测 target bounds 是否在 ScrollPane 视口内，由业务决定是否 `tour.close()`。

### 18.4 Stage 模态对话框

Tour 不依赖 Stage modality。但若在模态 Stage 弹出之前就 `show(...)` 了 Tour，新的模态 Stage 可能盖住 Tour overlay —— 这是 Stage 层级问题，不属于 Tour bug。建议在打开模态对话框前先 `tour.close()`。

### 18.5 WebView 内嵌网页内容

Tour 无法定位 WebView 内部 HTML 元素。请把目标设为 WebView 节点本身，或自行计算 WebView 网页内某区域在场景中的 Rectangle2D 后用 `target(Rectangle2D)`。

---

## 19. 性能注意事项与最佳实践

### 19.1 性能要点

1. **延迟构建 Tour**：仅在用户点击"开始引导"时构造 Tour 实例。组件构造涉及监听绑定，不必在应用启动时全做。
2. **复用 Tour**：若同一引导多次出现，可保留 Tour 实例，每次 `tour.show(scene)` 即可（但需在上一次 `close()` 之后）。
3. **及时 `dispose()`**：组件销毁时务必释放资源，避免节点监听泄漏。
4. **大型容器避免在 onChange 中重复 layout**：`onChange` 已经在帧调度内触发，不要再手动调用 `host.layout()`。
5. **自定义插槽节点不要过重**：indicatorSlot/contentSlot/footerSlot 在每次 `renderCurrentStep` 时被插入 popup 并触发布局，重型节点会拖慢切换。

### 19.2 最佳实践

- **首选 Node 目标**：尽量传 Node 而非 Rectangle2D，自动跟随且无需关心 scene 坐标计算。
- **用 BorderPane / StackPane 作根**：满足 host 必须是 Pane 子类的硬性要求。
- **复杂布局开启 `dismissOnMaskClick`**：用户随时可点击空白处退出，提高体验。
- **指引文案 ≤ 80 字**：超出会让 popup 高度激增，自适应翻转概率上升。
- **PRIMARY 类型搭配 mask=false**：作为轻量提示，比模态弹窗体验更好。
- **避免在 `onShow` 中再启动新 Tour**：会导致 active 标记冲突，应先 close 再 show。

---

## 20. 常见问题与故障排除

### Q1. 启动 Tour 后 popup 不显示，控制台无错误

**排查**：
1. `scene.getRoot()` 是否为 `Pane` 子类？若是 `Group`，请改为 `StackPane`/`BorderPane`。
2. `steps` 是否为空？空步骤直接返回不报错。
3. 目标 Node 是否已挂载到 Scene？未挂载时 `resolveSceneBounds()` 返回 null，弹窗会落到 CENTER。

### Q2. 滚动 ScrollPane 后 popup 没有跟随

**排查**：
1. 检查项目是否使用了 1.0 之前的旧版本 —— 1.1 版本通过 `localToSceneTransformProperty` + `hvalue/vvalue` 双重监听已修复。
2. 自定义 ScrollPane 实现可能屏蔽了 `hvalue/vvalue`，请改用标准 `javafx.scene.control.ScrollPane`。

### Q3. 窗口非全屏时弹窗整体偏移到上方

**已修复**（1.1）。根因：`softClamp` 在 popup 尺寸 ≥ host 尺寸时被错误地归零。当前实现：popup ≥ host 时保留原坐标。

### Q4. 自定义 indicator 圆点完全不可见

**已修复**（1.1）。根因：旧版本对 host 整体调用 `applyCss + layout` 会递归到 overlay 子树，导致带状态的 indicator 节点被异常重置。当前实现：仅对目标节点祖先链调 applyCss/layout。

### Q5. 弹窗显示在错误位置（屏幕角落）

**排查**：
1. 目标 Node 是否在 `getScene() == null` 状态？
2. 容器是否使用了 `setManaged(false)` 但未 `resize/setLayoutX/Y`？
3. 检查 host 是否同时被多次添加到不同父节点。

### Q6. 切换主题后效果未生效

**说明**：`TourTheme` 是不可变对象，需重新构造并通过 `tour.theme(newTheme)` 设置。

```java
TourTheme dark = TourTheme.builder().popupBackground(Color.BLACK).build();
tour.theme(dark);
// 已 active 时需要调用 goTo 触发重渲染
if (tour.isActive()) tour.goTo(tour.getCurrent());
```

### Q7. 多个 Tour 同时启动

**禁止**。`Tour.show(...)` 内部检测 `active` 字段，重复 show 会被忽略。请保证同时只有一个 Tour 处于 active 状态。

### Q8. ESC 键不响应

**排查**：
1. `closeOnEsc` 是否被设为 false？
2. 是否被业务代码 `EventFilter` 提前消费？Tour 内部使用的是 `addEventFilter`，会在事件处理早期收到。

### Q9. 关闭 Tour 后 popup 残留在场景中

**排查**：异常导致 `detachOverlay()` 未执行。请检查日志中 WARNING："Tour 关闭异常"。可调用 `tour.dispose()` 强制清理。

### Q10. 指向 TextArea 内某段文字时矩形位置不准

**说明**：JavaFX `TextArea` 内部使用 `Skin` 渲染文本，没有公开 API 获取每个字符位置。本组件示例使用估算行高 / 列宽。若要精准定位，建议：
1. 使用 `Text` 节点替代 `TextArea`，可以通过 `text.getBoundsInLocal()` 获取真实位置。
2. 或使用 `TextArea.lookup(".text")` 获取内部 Text 节点（依赖 skin 实现，不保证稳定）。

---

## 附录 A：完整 API 索引（按类）

### Tour
`Tour()` `addStep` `addSteps` `mask` `maskConfig` `theme` `type` `showClose` `showArrow` `showIndicators` `closeOnEsc` `prevButtonText` `nextButtonText` `finishButtonText` `onOpen` `onClose` `onFinish` `onChange` `show(Scene)` `show(Pane)` `close` `next` `prev` `goTo` `finish` `dispose` `getSteps` `currentProperty` `maskProperty` `maskConfigProperty` `themeProperty` `typeProperty` `showCloseProperty` `showArrowProperty` `showIndicatorsProperty` `closeOnEscProperty` `prevButtonTextProperty` `nextButtonTextProperty` `finishButtonTextProperty` `getCurrent` `isMask` `getMaskConfig` `getTheme` `getType` `isShowClose` `isShowArrow` `isShowIndicators` `isCloseOnEsc` `isActive` `isDisposed` `getStepList`

### TourStep
`title` `description` `placement` `target(Node)` `target(Rectangle2D)` `target(TourTarget)` `theme` `maskConfig` `type` `showMask` `nextText` `prevText` `indicatorSlot` `contentSlot` `footerSlot` `onShow` `onHide` `dispose` + 全部对应的 `xxxProperty()` / getter / setter

### TourTarget
`of(Node)` `of(Rectangle2D)` `of(double,double,double,double)` `empty()` `isEmpty` `isNodeBased` `resolveSceneBounds` `resolveCenter` `getNode` `getRect`

### TourPlacement
12 + 1 枚举值 + `getValue` `isTop` `isBottom` `isLeft` `isRight` `isHorizontalAxis` `isVerticalAxis` `fromValue`

### TourMaskConfig.Builder
`color` `opacity` `padding` `cornerRadius` `highlight` `highlightColor` `highlightWidth` `dismissOnMaskClick` `build`

### TourTheme.Builder
`primaryColor` `popupBackground` `popupBorderColor` `titleColor` `descriptionColor` `buttonTextColor` `secondaryButtonBg` `secondaryButtonText` `closeIconColor` `indicatorActiveColor` `indicatorInactiveColor` `arrowFillColor` `fontFamily` `titleFontSize` `descriptionFontSize` `buttonFontSize` `indicatorFontSize` `popupMinWidth` `popupMaxWidth` `popupCornerRadius` `popupPadding` `popupArrowSize` `popupOffset` `indicatorDotSize` `dropShadowRadius` `build` + 静态 `toCssColor(Color)`

### TourFactory
`createTour` `builder` `createBasic` `createNonModal` `createPlacement` `createCustomMask` `createCustomIndicator` `createCenter` `createInteractive`

### TourFactory.Builder
`mask` `maskConfig` `theme` `type` `showClose` `showArrow` `showIndicators` `closeOnEsc` `prevButtonText` `nextButtonText` `finishButtonText` `step(TourStep)` `step(Node,String,String)` `step(Node,String,String,TourPlacement)` `onOpen` `onClose` `onFinish` `onChange` `build`

---

## 附录 B：版本历史

### 1.1（当前）
- 修复 ScrollPane 滚动 / 窗口非全屏时 popup 偏移到上方
- 修复 CustomMask 之后多个示例 indicator 不可见（削减 host 全局 applyCss/layout 副作用）
- `sceneRectToHost` 改用 4 角点 Bounds 变换，支持 host 旋转/缩放
- `softClamp` 不再在 popup ≥ host 时退化归零
- `CENTER` 路径允许负坐标真正居中
- `popup.toFront()` / `arrow.toFront()` 防御 z-order 异常
- 新增 `TourPlacement.isVerticalAxis()` 与 `isHorizontalAxis()` 对偶
- 新增 `TourStandardNodeDemo`：覆盖 27 个标准 JavaFX 控件 + TextArea 内部文本片段
- 改造 `TourPaneDemo` 为 TabPane 取代 ScrollPane

### 1.0
- 初版：12 + 1 种 Placement、Mask 配置、主题系统、TourFactory Builder、8 类演示 Pane

---

> 文档完。如需更多示例代码，参见 `src/test/java/io/aurora/fx/components/tour/` 下的 9 个 Demo 类。
