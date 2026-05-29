# Aurora-FX LineButton 组件 — 完整 API 文档

> 版本: 0.0.1 | 最后更新: 2025 年
> 参考 RXControls RXLineButton，提供增强的线条按钮组件

---

## 目录

1. [架构概览](#1-架构概览)
2. [快速开始](#2-快速开始)
3. [核心组件 — LineButton](#3-核心组件--linebutton)
4. [辅助类](#4-辅助类)
   - [LineAnimationType — 动画类型枚举](#41-lineanimationtype--动画类型枚举)
   - [LineButtonTheme — 主题配置](#42-linebuttontheme--主题配置)
5. [Skin 实现](#5-skin-实现)
6. [样式定制参考](#6-样式定制参考)
7. [常见问题 FAQ](#7-常见问题-faq)

---

## 1. 架构概览

```
┌─────────────────────────────────────────────┐
│           LineButton (Labeled)              │
│           LineButtonSkin (SkinBase)         │
├─────────────────────────────────────────────┤
│  Label      │  Line (在 Pane + clip 中)     │
│  (文字标签)  │  (线条动画)                    │
├─────────────────────────────────────────────┤
│  LineAnimationType │  LineButtonTheme        │
│  (动画类型枚举)    │  (主题 Builder 配置)     │
└─────────────────────────────────────────────┘
```

### 设计原则

- **Control/Skin 分离**: `LineButton` 继承 `Labeled`，UI 渲染由 `LineButtonSkin` 负责
- **双动画类型**: EXTEND（从中心延伸）和 RISE（从下方上升渐显）
- **Builder 主题**: `LineButtonTheme` 通过 Builder 模式提供文字颜色、线条颜色等配置
- **链式 API**: 所有属性均提供链式 setter 方法
- **事件回调**: 支持 `onAction` 事件回调
- **内存安全**: Skin 层的 `dispose()` 完整释放所有绑定、监听器和事件过滤器

---

## 2. 快速开始

### 方式一：基础用法

```java
LineButton btn = new LineButton("首页");
root.getChildren().add(btn);
```

### 方式二：链式配置

```java
LineButton btn = new LineButton("Settings")
    .lineType(LineAnimationType.RISE)
    .spacing(4)
    .animationTime(Duration.millis(200))
    .theme(LineButtonTheme.PRIMARY)
    .onAction(e -> System.out.println("Clicked!"));
```

### 方式三：带图标的导航按钮

```java
FontIcon icon = new FontIcon(AntDesignIconsOutlined.HOME);
icon.setIconSize(14);
LineButton btn = new LineButton("首页", icon);
btn.setContentDisplay(ContentDisplay.TOP);
btn.setGraphicTextGap(4);
btn.theme(LineButtonTheme.PRIMARY);
```

---

## 3. 核心组件 — LineButton

`io.aurora.fx.components.lineButton.LineButton extends Labeled`

### 3.1 构造函数

| 构造函数 | 说明 |
|---------|------|
| `LineButton()` | 默认构造，文本为 "LineButton"，默认尺寸 150×60 |
| `LineButton(String text)` | 指定文本 |
| `LineButton(String text, Node graphic)` | 指定文本和图标 |

### 3.2 属性一览

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `lineType` | `ObjectProperty<LineAnimationType>` | `EXTEND` | 线条动画类型 |
| `animationTime` | `ObjectProperty<Duration>` | `130ms` | 动画持续时间 |
| `theme` | `ObjectProperty<LineButtonTheme>` | `DEFAULT` | 主题配置 |
| `spacing` | `DoubleProperty` | `0` | 线条与文字的间距 |
| `offsetY` | `DoubleProperty` | `15` | RISE 动画的偏移量 |
| `onAction` | `ObjectProperty<EventHandler<ActionEvent>>` | `null` | 点击事件回调 |

> 注：继承自 `Labeled` 的属性（`text`、`graphic`、`font`、`alignment` 等）均可直接使用。

### 3.3 链式 API

| 方法 | 说明 |
|------|------|
| `lineType(LineAnimationType)` | 设置动画类型，返回 this |
| `animationTime(Duration)` | 设置动画时长，返回 this |
| `theme(LineButtonTheme)` | 设置主题，返回 this |
| `spacing(double)` | 设置线条间距，返回 this |
| `offsetY(double)` | 设置 RISE 动画偏移量，返回 this |
| `onAction(EventHandler<ActionEvent>)` | 设置点击回调，返回 this |

### 3.4 公共方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `getLine()` | `Line getLine()` | 获取线条节点（可直接设置样式） |
| `getLabel()` | `Label getLabel()` | 获取内部 Label 节点 |

### 3.5 完整使用示例

```java
// 导航栏
HBox navBar = new HBox(0);
navBar.setStyle("-fx-background-color: white; -fx-padding: 0 24; " +
    "-fx-border-color: #E4E7ED; -fx-border-width: 0 0 1 0;");

String[] items = {"首页", "产品", "文档", "关于", "联系"};
for (String item : items) {
    LineButton btn = new LineButton(item);
    btn.setLineType(LineAnimationType.EXTEND);
    btn.setSpacing(2);
    btn.setTheme(LineButtonTheme.PRIMARY);
    btn.setPrefSize(100, 45);
    btn.onAction(e -> System.out.println("选中: " + item));
    navBar.getChildren().add(btn);
}

// 自定义主题
LineButtonTheme customTheme = LineButtonTheme.builder()
    .textColor(Color.valueOf("#67C23A"))
    .lineColor(Color.valueOf("#67C23A"))
    .hoverTextColor(Color.valueOf("#85CE61"))
    .lineWidth(2)
    .build();
LineButton customBtn = new LineButton("自定义绿色");
customBtn.theme(customTheme);
```

---

## 4. 辅助类

### 4.1 LineAnimationType — 动画类型枚举

`io.aurora.fx.components.lineButton.LineAnimationType`

| 枚举值 | 说明 |
|--------|------|
| `EXTEND` | 延伸动画：线条从中心向两侧延伸（scaleX 0→1） |
| `RISE` | 上升动画：线条从下方上升并渐显（translateY + opacity） |

### 4.2 LineButtonTheme — 主题配置

`io.aurora.fx.components.lineButton.LineButtonTheme`

通过 Builder 模式提供主题定制能力。

#### 预设主题

| 常量 | 文字色 | 线条色 | 悬停文字色 | 说明 |
|------|--------|--------|-----------|------|
| `DEFAULT` | `#303133` | `#409EFF` | `#409EFF` | 默认深灰文字蓝色线条 |
| `DARK` | `#E0E0E0` | `#409EFF` | `#66B1FF` | 深色主题 |
| `PRIMARY` | `#409EFF` | `#409EFF` | `#66B1FF` | 主要色 |
| `DANGER` | `#F56C6C` | `#F56C6C` | `#F78989` | 危险色 |

#### Builder 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `textColor` | `Color` | `#303133` | 默认文字颜色 |
| `hoverTextColor` | `Color` | `#409EFF` | 悬停文字颜色 |
| `lineColor` | `Color` | `#409EFF` | 线条颜色 |
| `backgroundColor` | `Color` | `TRANSPARENT` | 背景颜色 |
| `fontSize` | `double` | `14` | 字体大小 |
| `fontFamily` | `String` | `"System"` | 字体族 |
| `lineWidth` | `double` | `1.5` | 线条宽度 |
| `animationDuration` | `double` | `130` | 动画时长（毫秒） |

#### 自定义主题示例

```java
LineButtonTheme theme = LineButtonTheme.builder()
    .textColor(Color.valueOf("#67C23A"))
    .lineColor(Color.valueOf("#67C23A"))
    .hoverTextColor(Color.valueOf("#85CE61"))
    .lineWidth(2)
    .fontSize(16)
    .build();
```

---

## 5. Skin 实现

`io.aurora.fx.components.lineButton.LineButtonSkin extends SkinBase<LineButton>`

### 内部结构

- **label** (`Label`): 文字标签，绑定 control 的所有文本属性
- **line** (`Line`): 线条节点
- **linePane** (`Pane`): 线条容器，带矩形裁剪
- **clipRect** (`Rectangle`): 裁剪区域

### 动画机制

#### EXTEND 动画

1. 线条初始 `scaleX = 0`（不可见）
2. 鼠标进入 → `scaleX` 从 0 动画到 1（从中心向两侧延伸）
3. 鼠标退出 → `scaleX` 从 1 动画到 0（从两侧向中心收缩）

#### RISE 动画

1. 线条初始 `translateY = offsetY`，`opacity = 0`（不可见）
2. 鼠标进入 → `translateY` 从 offsetY 动画到 0，`opacity` 从 0 到 1
3. 鼠标退出 → `translateY` 从 0 动画到 offsetY，`opacity` 从 1 到 0

### 线条位置跟踪

- 监听 Label 的 `boundsInParent` 变化 → 自动更新线条的 startX/endX/startY/endY
- `spacing` 属性控制线条与文字底部的额外间距

### 重要说明

- 所有事件处理器和监听器在构造方法中初始化（避免 "可能尚未初始化" 编译错误）
- `dispose()` 方法完整释放所有绑定、监听器和事件过滤器

---

## 6. 样式定制参考

### CSS 样式类

| 样式类 | 作用于 | 说明 |
|--------|--------|------|
| `.aurora-line-button` | 控件本身 | 光标、背景、内边距 |
| `.aurora-line-button .line` | 线条节点 | 线条颜色、宽度 |

### 可覆盖 CSS 属性

| CSS 属性 | 默认值 | 说明 |
|----------|--------|------|
| `-fx-cursor` | `hand` | 光标样式 |
| `-fx-background-color` | `transparent` | 背景色 |
| `-fx-padding` | `8 16` | 内边距 |
| `-fx-stroke` (line) | `#409EFF` | 线条颜色 |
| `-fx-stroke-width` (line) | `1.5` | 线条宽度 |

---

## 7. 常见问题 FAQ

**Q: EXTEND 和 RISE 动画有什么区别？**
A: EXTEND 是线条从中心向两侧延伸（水平缩放），RISE 是线条从下方上升并渐显（垂直位移+透明度）。EXTEND 适合导航栏，RISE 适合更动感的交互。

**Q: 如何调整线条与文字的间距？**
A: 通过 `spacing` 属性：`btn.setSpacing(4)` 或链式 `.spacing(4)`。

**Q: 如何直接操作线条节点？**
A: 通过 `getLine()` 获取 `Line` 节点，可直接设置 stroke、strokeWidth 等属性。注意：主题变化时会覆盖手动设置的线条样式。

**Q: 如何在导航栏中使用？**
A: 将多个 LineButton 放入 HBox，设置统一的 PRIMARY 主题和 EXTEND 动画类型，配合 `spacing(2)` 使线条紧贴文字底部。

**Q: `offsetY` 属性的作用是什么？**
A: 仅在 RISE 动画中生效，控制线条上升的初始偏移量。默认 15px，值越大动画幅度越大。
