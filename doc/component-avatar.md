# Aurora-FX Avatar 组件 — 完整 API 文档

> 版本: 0.0.1 | 最后更新: 2025 年
> 参考 RXControls RXAvatar，提供增强的头像展示组件

---

## 目录

1. [架构概览](#1-架构概览)
2. [快速开始](#2-快速开始)
3. [核心组件 — Avatar](#3-核心组件--avatar)
4. [辅助类](#4-辅助类)
   - [AvatarShape — 形状枚举](#41-avatarshape--形状枚举)
   - [AvatarTheme — 主题配置](#42-avatartheme--主题配置)
5. [Skin 实现](#5-skin-实现)
6. [样式定制参考](#6-样式定制参考)
7. [常见问题 FAQ](#7-常见问题-faq)

---

## 1. 架构概览

```
┌─────────────────────────────────────────────┐
│              Avatar (Control)                │
│              AvatarSkin (SkinBase)           │
├─────────────────────────────────────────────┤
│  ImageView  │  Label (placeholder)          │
│  (图片裁剪)  │  (占位符)                     │
├─────────────────────────────────────────────┤
│  AvatarShape     │  AvatarTheme              │
│  (8种形状枚举)   │  (主题 Builder 配置)       │
└─────────────────────────────────────────────┘
```

### 设计原则

- **Control/Skin 分离**: `Avatar` 继承 `Control`，UI 渲染由 `AvatarSkin` 负责
- **多种形状**: 支持 CIRCLE、SQUARE、HEXAGON_H、HEXAGON_V、DIAMOND、PENTAGON、STAR、ROUNDED_SQUARE 八种裁剪形状
- **Builder 主题**: `AvatarTheme` 通过 Builder 模式提供边框、阴影、背景等配置
- **链式 API**: 所有属性均提供链式 setter 方法
- **异步加载**: 支持图片后台加载和进度监听
- **内存安全**: Skin 层的 `dispose()` 完整释放所有绑定和监听器

---

## 2. 快速开始

### 方式一：基础用法

```java
Avatar avatar = new Avatar(new Image("avatar.png"));
root.getChildren().add(avatar);
```

### 方式二：链式配置

```java
Avatar avatar = new Avatar()
    .image(new Image("avatar.png"))
    .avatarShape(AvatarShape.CIRCLE)
    .size(80)
    .arcWidth(10)
    .arcHeight(10)
    .theme(AvatarTheme.BORDERED)
    .placeholder("U");
```

### 方式三：URL 加载（后台加载）

```java
Avatar avatar = new Avatar("https://example.com/avatar.png");
avatar.size(64);
avatar.avatarShape(AvatarShape.ROUNDED_SQUARE);
```

---

## 3. 核心组件 — Avatar

`io.aurora.fx.components.avatar.Avatar extends Control`

### 3.1 构造函数

| 构造函数 | 说明 |
|---------|------|
| `Avatar()` | 默认构造，尺寸 100×100，圆形 |
| `Avatar(Image image)` | 指定图片 |
| `Avatar(String imageUrl)` | 通过 URL 加载（后台加载） |
| `Avatar(String imageUrl, boolean backgroundLoading)` | 通过 URL 加载，指定是否后台加载 |

### 3.2 属性一览

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `image` | `ObjectProperty<Image>` | `null` | 头像图片 |
| `avatarShape` | `ObjectProperty<AvatarShape>` | `CIRCLE` | 裁剪形状 |
| `theme` | `ObjectProperty<AvatarTheme>` | `DEFAULT` | 主题配置 |
| `arcWidth` | `DoubleProperty` | `0` | 方形圆角宽度（仅 SQUARE 形状生效） |
| `arcHeight` | `DoubleProperty` | `0` | 方形圆角高度（仅 SQUARE 形状生效） |
| `placeholder` | `StringProperty` | `""` | 占位文字（图片未加载时显示） |

### 3.3 链式 API

| 方法 | 说明 |
|------|------|
| `image(Image)` | 设置图片，返回 this |
| `avatarShape(AvatarShape)` | 设置形状，返回 this |
| `size(double)` | 设置尺寸（宽高相同），返回 this |
| `arcWidth(double)` | 设置方形圆角宽度，返回 this |
| `arcHeight(double)` | 设置方形圆角高度，返回 this |
| `theme(AvatarTheme)` | 设置主题，返回 this |
| `placeholder(String)` | 设置占位文字，返回 this |

### 3.4 完整使用示例

```java
// 用户头像列表
HBox avatarRow = new HBox(10);

String[] names = {"张三", "李四", "王五"};
for (String name : names) {
    Avatar avatar = new Avatar()
        .size(48)
        .avatarShape(AvatarShape.CIRCLE)
        .theme(AvatarTheme.BORDERED)
        .placeholder(name.substring(0, 1));
    avatarRow.getChildren().add(avatar);
}

// 带阴影的头像
Avatar shadowAvatar = new Avatar(new Image("user.png"));
shadowAvatar.size(80);
shadowAvatar.avatarShape(AvatarShape.ROUNDED_SQUARE);
shadowAvatar.theme(AvatarTheme.SHADOW);

// 自定义主题
AvatarTheme custom = AvatarTheme.builder()
    .borderColor(Color.valueOf("#F56C6C"))
    .borderWidth(3)
    .shadowRadius(8)
    .shadowOpacity(0.25)
    .build();
Avatar customAvatar = new Avatar(new Image("vip.png"));
customAvatar.size(80);
customAvatar.theme(custom);
```

---

## 4. 辅助类

### 4.1 AvatarShape — 形状枚举

`io.aurora.fx.components.avatar.AvatarShape`

| 枚举值 | 说明 |
|--------|------|
| `CIRCLE` | 圆形（默认） |
| `SQUARE` | 正方形（支持 `arcWidth`/`arcHeight` 圆角） |
| `HEXAGON_H` | 水平六边形 |
| `HEXAGON_V` | 垂直六边形 |
| `DIAMOND` | 菱形 |
| `PENTAGON` | 正五边形 |
| `STAR` | 五角星 |
| `ROUNDED_SQUARE` | 自动圆角方形（圆角为尺寸的 1/4） |

### 4.2 AvatarTheme — 主题配置

`io.aurora.fx.components.avatar.AvatarTheme`

通过 Builder 模式提供主题定制能力。

#### 预设主题

| 常量 | 边框 | 阴影 | 说明 |
|------|------|------|------|
| `DEFAULT` | 无 | 无 | 默认无边框无阴影 |
| `DARK` | `#4A4A6A` | 无 | 深色主题 |
| `BORDERED` | `#409EFF` 2px | 无 | 带蓝色边框 |
| `SHADOW` | 无 | 半径 10，透明度 0.3 | 阴影效果 |

#### Builder 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `borderColor` | `Color` | `TRANSPARENT` | 边框颜色 |
| `backgroundColor` | `Color` | `#F0F0F0` | 占位背景色 |
| `placeholderColor` | `Color` | `#C0C4CC` | 占位文字颜色 |
| `hoverBorderColor` | `Color` | `TRANSPARENT` | 悬停边框颜色 |
| `borderWidth` | `double` | `0` | 边框宽度 |
| `shadowRadius` | `double` | `0` | 阴影半径 |
| `shadowOpacity` | `double` | `0.15` | 阴影透明度 |
| `arcSize` | `double` | `0` | 默认圆角（仅 SQUARE 形状且未设置 arcWidth 时生效） |

#### 自定义主题示例

```java
AvatarTheme theme = AvatarTheme.builder()
    .borderColor(Color.valueOf("#409EFF"))
    .borderWidth(2)
    .shadowRadius(12)
    .shadowOpacity(0.2)
    .backgroundColor(Color.valueOf("#ECF5FF"))
    .placeholderColor(Color.valueOf("#409EFF"))
    .build();
```

---

## 5. Skin 实现

`io.aurora.fx.components.avatar.AvatarSkin extends SkinBase<Avatar>`

### 内部结构

- **rootPane** (`Group`): 根容器
- **imageView** (`ImageView`): 图片显示，preserveRatio=true
- **placeholderLabel** (`Label`): 占位符 Label
- **borderShape** (`Shape`): 边框形状

### 裁剪算法

| 形状 | 实现方式 | 关键参数 |
|------|---------|---------|
| CIRCLE | `Circle` clip | r = min(w, h) / 2 |
| SQUARE | `Rectangle` clip | arcWidth/arcHeight 可配置 |
| HEXAGON_H | `Polygon` clip | rate = 1.16 |
| HEXAGON_V | `Polygon` clip | rate = 0.871 |
| DIAMOND | `Polygon` clip | 4 顶点 |
| PENTAGON | `Polygon` clip | 5 顶点，间隔 72° |
| STAR | `Polygon` clip | 10 顶点，内半径 r × 0.382 |
| ROUNDED_SQUARE | `Rectangle` clip | arcSize = r × 0.5 |

### 异步加载机制

1. `imageProperty` 变化 → 移除旧图片的进度监听 → 添加新图片的进度监听
2. 进度 ≥ 1.0 → 触发 `clipImageView()` 重新裁剪
3. 进度 < 1.0 → 显示占位符

### 边框渲染

- 边框通过独立的 `Shape` 节点绘制（StrokeType.OUTSIDE）
- 边框形状与裁剪形状一致
- `borderWidth ≤ 0` 时不渲染边框

---

## 6. 样式定制参考

### CSS 样式类

| 样式类 | 作用于 | 说明 |
|--------|--------|------|
| `.aurora-avatar` | 控件本身 | 最小尺寸 |
| `.aurora-avatar .avatar-group` | 根 Group 容器 | — |
| `.aurora-avatar .avatar-image` | ImageView | 图片平滑 |
| `.aurora-avatar .avatar-placeholder` | 占位符 Label | 对齐、文字色、字重 |

### 可覆盖 CSS 属性

| CSS 属性 | 默认值 | 说明 |
|----------|--------|------|
| `-fx-min-width` | `32` | 最小宽度 |
| `-fx-min-height` | `32` | 最小高度 |
| `-fx-alignment` (placeholder) | `center` | 占位符对齐 |
| `-fx-text-fill` (placeholder) | `#C0C4CC` | 占位符文字色 |
| `-fx-font-weight` (placeholder) | `bold` | 占位符字重 |

---

## 7. 常见问题 FAQ

**Q: 为什么 `shape` 属性改名为 `avatarShape`？**
A: `Region` 基类有 `final` 的 `shapeProperty()` 和 `getShape()` 方法，子类无法覆盖。使用 `avatarShape` 避免命名冲突。

**Q: 如何实现鼠标悬停时改变边框颜色？**
A: 设置 `hoverBorderColor` 属性。可通过主题配置：
```java
AvatarTheme theme = AvatarTheme.builder()
    .borderColor(Color.valueOf("#409EFF"))
    .hoverBorderColor(Color.valueOf("#66B1FF"))
    .borderWidth(2)
    .build();
```

**Q: 图片加载失败怎么办？**
A: 设置 `placeholder` 属性，加载失败或未加载时会显示占位文字。

**Q: 如何实现六边形头像？**
A: 设置 `avatarShape(AvatarShape.HEXAGON_H)` 或 `avatarShape(AvatarShape.HEXAGON_V)`，分别对应水平和垂直方向的六边形。

**Q: ROUNDED_SQUARE 和带 arcWidth 的 SQUARE 有什么区别？**
A: `ROUNDED_SQUARE` 自动设置圆角为尺寸的 1/4，无需手动配置；`SQUARE` 需要手动设置 `arcWidth`/`arcHeight` 来控制圆角大小。
