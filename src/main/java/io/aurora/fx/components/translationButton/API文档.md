# Aurora-FX TranslationButton 组件 — 完整 API 文档

> 版本: 0.0.1 | 最后更新: 2025 年
> 参考 RXControls RXTranslationButton，提供增强的平移按钮组件

---

## 目录

1. [架构概览](#1-架构概览)
2. [快速开始](#2-快速开始)
3. [核心组件 — TranslationButton](#3-核心组件--translationbutton)
4. [辅助类](#4-辅助类)
   - [TranslationDirection — 方向枚举](#41-translationdirection--方向枚举)
   - [TranslationButtonTheme — 主题配置](#42-translationbuttontheme--主题配置)
5. [Skin 实现](#5-skin-实现)
6. [样式定制参考](#6-样式定制参考)
7. [常见问题 FAQ](#7-常见问题-faq)

---

## 1. 架构概览

```
┌─────────────────────────────────────────────┐
│           TranslationButton (Labeled)       │
│           TranslationButtonSkin (SkinBase)  │
├─────────────────────────────────────────────┤
│  hoverLabel    │  nonHoverLabel             │
│  (悬停层 Label) │  (非悬停层 Label)           │
├─────────────────────────────────────────────┤
│  TranslationDirection  │  TranslationButtonTheme │
│  (平移方向枚举)        │  (主题 Builder 配置)     │
└─────────────────────────────────────────────┘
```

### 设计原则

- **Control/Skin 分离**: `TranslationButton` 继承 `Labeled`，UI 渲染由 `TranslationButtonSkin` 负责
- **双层 Label**: hoverLabel 和 nonHoverLabel 通过平移动画交替显示
- **Builder 主题**: `TranslationButtonTheme` 通过 Builder 模式提供丰富的主题定制
- **链式 API**: 所有属性均提供链式 setter 方法
- **事件回调**: 支持 `onAction` 事件回调
- **内存安全**: Skin 层的 `dispose()` 完整释放所有绑定和监听器

---

## 2. 快速开始

### 方式一：基础用法

```java
TranslationButton btn = new TranslationButton("悬停我");
root.getChildren().add(btn);
```

### 方式二：链式配置

```java
TranslationButton btn = new TranslationButton("Click Me")
    .direction(TranslationDirection.LEFT_TO_RIGHT)
    .animationTime(Duration.millis(200))
    .theme(TranslationButtonTheme.PRIMARY)
    .onAction(e -> System.out.println("Clicked!"));
```

### 方式三：带图标的悬停效果

```java
TranslationButton btn = new TranslationButton("提交");
btn.theme(TranslationButtonTheme.PRIMARY);

// 悬停时显示图标（使用 ikonli 图标库）
FontIcon icon = new FontIcon(AntDesignIconsOutlined.ARROW_RIGHT);
icon.setIconSize(18);
icon.setIconColor(Color.WHITE);
btn.getHoverLabel().setGraphic(icon);
btn.getHoverLabel().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
```

---

## 3. 核心组件 — TranslationButton

`io.aurora.fx.components.translationButton.TranslationButton extends Labeled`

### 3.1 构造函数

| 构造函数 | 说明 |
|---------|------|
| `TranslationButton()` | 默认构造，文本为 "Translation"，默认尺寸 160×60 |
| `TranslationButton(String text)` | 指定文本 |
| `TranslationButton(String text, Node graphic)` | 指定文本和图标 |

### 3.2 属性一览

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `direction` | `ObjectProperty<TranslationDirection>` | `BOTTOM_TO_TOP` | 平移动画方向 |
| `animationTime` | `ObjectProperty<Duration>` | `130ms` | 动画持续时间 |
| `theme` | `ObjectProperty<TranslationButtonTheme>` | `DEFAULT` | 主题配置 |
| `onAction` | `ObjectProperty<EventHandler<ActionEvent>>` | `null` | 点击事件回调 |

> 注：继承自 `Labeled` 的属性（`text`、`graphic`、`font`、`alignment` 等）均可直接使用。

### 3.3 链式 API

| 方法 | 说明 |
|------|------|
| `direction(TranslationDirection)` | 设置平移方向，返回 this |
| `animationTime(Duration)` | 设置动画时长，返回 this |
| `theme(TranslationButtonTheme)` | 设置主题，返回 this |
| `onAction(EventHandler<ActionEvent>)` | 设置点击回调，返回 this |

### 3.4 公共方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `getHoverLabel()` | `Label getHoverLabel()` | 获取悬停层 Label（可自定义图标和文字） |
| `getNonHoverLabel()` | `Label getNonHoverLabel()` | 获取非悬停层 Label |

### 3.5 完整使用示例

```java
// 自定义主题 + 图标悬停
TranslationButtonTheme customTheme = TranslationButtonTheme.builder()
    .backgroundColor(Color.valueOf("#F3E8FF"))
    .textColor(Color.valueOf("#7C3AED"))
    .hoverBackgroundColor(Color.valueOf("#7C3AED"))
    .hoverTextColor(Color.WHITE)
    .borderColor(Color.valueOf("#C4B5FD"))
    .borderRadius(20)
    .build();

TranslationButton btn = new TranslationButton("提交");
btn.setPrefSize(140, 45);
btn.theme(customTheme);
btn.direction(TranslationDirection.LEFT_TO_RIGHT);

FontIcon icon = new FontIcon(AntDesignIconsOutlined.ARROW_RIGHT);
icon.setIconSize(18);
icon.setIconColor(Color.WHITE);
btn.getHoverLabel().setGraphic(icon);
btn.getHoverLabel().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

btn.onAction(e -> System.out.println("提交按钮被点击"));
```

---

## 4. 辅助类

### 4.1 TranslationDirection — 方向枚举

`io.aurora.fx.components.translationButton.TranslationDirection`

| 枚举值 | 说明 |
|--------|------|
| `BOTTOM_TO_TOP` | 从下向上平移（默认） |
| `TOP_TO_BOTTOM` | 从上向下平移 |
| `LEFT_TO_RIGHT` | 从左向右平移 |
| `RIGHT_TO_LEFT` | 从右向左平移 |

### 4.2 TranslationButtonTheme — 主题配置

`io.aurora.fx.components.translationButton.TranslationButtonTheme`

通过 Builder 模式提供主题定制能力。

#### 预设主题

| 常量 | 背景色 | 文字色 | 悬停背景色 | 说明 |
|------|--------|--------|-----------|------|
| `DEFAULT` | `#ECF5FF` | `#409EFF` | `#409EFF` | 默认浅蓝主题 |
| `DARK` | `#2C3E50` | `white` | `#34495E` | 深色主题 |
| `PRIMARY` | `#409EFF` | `white` | `#66B1FF` | 主要按钮 |
| `SUCCESS` | `#67C23A` | `white` | `#85CE61` | 成功按钮 |
| `DANGER` | `#F56C6C` | `white` | `#F78989` | 危险按钮 |

#### Builder 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `backgroundColor` | `Color` | `#ECF5FF` | 默认背景色 |
| `textColor` | `Color` | `#409EFF` | 默认文字颜色 |
| `hoverBackgroundColor` | `Color` | `#409EFF` | 悬停背景色 |
| `hoverTextColor` | `Color` | `white` | 悬停文字颜色 |
| `borderColor` | `Color` | `#B3D8FF` | 边框颜色 |
| `borderRadius` | `double` | `4` | 圆角大小 |
| `fontSize` | `double` | `14` | 字体大小 |
| `fontFamily` | `String` | `"System"` | 字体族 |
| `animationDuration` | `double` | `130` | 动画时长（毫秒） |

#### 自定义主题示例

```java
TranslationButtonTheme theme = TranslationButtonTheme.builder()
    .backgroundColor(Color.valueOf("#409EFF"))
    .textColor(Color.WHITE)
    .hoverBackgroundColor(Color.valueOf("#66B1FF"))
    .hoverTextColor(Color.WHITE)
    .borderColor(Color.valueOf("#409EFF"))
    .borderRadius(22)
    .fontSize(16)
    .build();
```

---

## 5. Skin 实现

`io.aurora.fx.components.translationButton.TranslationButtonSkin extends SkinBase<TranslationButton>`

### 内部结构

- **rootPane** (`StackPane`): 根容器，包含裁剪区域
- **hoverLabel** (`Label`): 悬停层 Label，默认绑定 control 的 graphic
- **nonHoverLabel** (`Label`): 非悬停层 Label，绑定 control 的文本属性
- **rectClip** (`Rectangle`): 裁剪区域，防止动画溢出

### 动画机制

- 鼠标进入 → `animEnter` Timeline 播放（nonHoverLabel 移出，hoverLabel 移入）
- 鼠标退出 → `animExit` Timeline 播放（hoverLabel 移出，nonHoverLabel 移入）
- 方向变化 → 自动重建动画 KeyFrame

### 重要说明

- hoverLabel 的 `graphic` 通过 `ChangeListener` 同步（非 `bind`），允许用户通过 `getHoverLabel().setGraphic()` 覆盖
- `dispose()` 方法完整释放所有绑定、监听器和事件过滤器

---

## 6. 样式定制参考

### CSS 样式类

| 样式类 | 作用于 | 说明 |
|--------|--------|------|
| `.aurora-translation-button` | 控件本身 | 光标、内边距 |
| `.aurora-translation-button .translation-pane` | 根容器 | 背景色、圆角、边框 |
| `.aurora-translation-button .non-hover-label` | 非悬停 Label | 文字颜色、对齐 |
| `.aurora-translation-button .hover-label` | 悬停 Label | 对齐方式 |

### 可覆盖 CSS 属性

| CSS 属性 | 默认值 | 说明 |
|----------|--------|------|
| `-fx-cursor` | `hand` | 光标样式 |
| `-fx-padding` | `0` | 内边距 |
| `-fx-background-color` (pane) | `#ECF5FF` | 容器背景色 |
| `-fx-background-radius` (pane) | `4` | 容器圆角 |
| `-fx-border-color` (pane) | `#B3D8FF` | 容器边框色 |
| `-fx-border-radius` (pane) | `4` | 容器边框圆角 |
| `-fx-text-fill` (non-hover) | `#409EFF` | 非悬停文字色 |

---

## 7. 常见问题 FAQ

**Q: 调用 `getHoverLabel().setGraphic()` 报错 "A bound value cannot be set"？**
A: 已在最新版本中修复。hoverLabel 的 graphic 改为 ChangeListener 同步，不再使用 bind，可以自由设置。

**Q: 如何让悬停层显示图标而不是文字？**
A: 通过 `getHoverLabel().setGraphic(icon)` 设置图标，再调用 `getHoverLabel().setContentDisplay(ContentDisplay.GRAPHIC_ONLY)` 隐藏文字。

**Q: 如何自定义动画时长？**
A: 通过 `setAnimationTime(Duration.millis(300))` 或链式 `.animationTime(Duration.millis(300))` 设置。

**Q: 动画方向可以动态切换吗？**
A: 可以，通过 `setDirection()` 动态切换，Skin 会自动重建动画。
