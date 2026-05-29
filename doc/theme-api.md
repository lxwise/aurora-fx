# Aurora-FX Theme 主题系统 — 完整 API 文档

> 版本: 0.0.1 | 最后更新: 2025 年  
> 基于 AtlantaFX Theme 接口，提供 Windows 11 Fluent Design 和 macOS Vibrancy 双操作系统风格主题

---

## 目录

1. [架构概览](#1-架构概览)
2. [快速开始](#2-快速开始)
3. [抽象基类 — AbstractOSTheme](#3-抽象基类--abstractostheme)
4. [主题实现类](#4-主题实现类)
   - [Windows11Light — Windows 11 浅色主题](#41-windows11light)
   - [Windows11Dark — Windows 11 深色主题](#42-windows11dark)
   - [MacOSLight — macOS 浅色主题](#43-macoslight)
   - [MacOSDark — macOS 深色主题](#44-macosdark)
   - [MacOSSystemDark — macOS 系统深色主题（别名）](#45-macossystemdark)
5. [工厂类 — OSThemeFactory](#5-工厂类--osthemefactory)
6. [语义化 CSS 变量体系](#6-语义化-css-变量体系)
   - [基础色阶变量](#61-基础色阶变量)
   - [语义色彩变量](#62-语义色彩变量)
   - [图表色彩变量](#63-图表色彩变量)
7. [组件覆盖样式](#7-组件覆盖样式)
   - [Windows 11 Fluent Design 组件覆盖](#71-windows-11-fluent-design-组件覆盖)
   - [macOS Vibrancy 组件覆盖](#72-macos-vibrancy-组件覆盖)
8. [文字排版系统](#8-文字排版系统)
9. [海拔阴影系统](#9-海拔阴影系统)
10. [主题切换与集成](#10-主题切换与集成)
11. [自定义主题扩展](#11-自定义主题扩展)
12. [设计语言对比](#12-设计语言对比)
13. [常见问题 FAQ](#13-常见问题-faq)

---

## 1. 架构概览

```
┌─────────────────────────────────────────────────────┐
│              OSThemeFactory（统一入口）                │
│  allThemes() / forName() / recommendedTheme()       │
├──────────────────┬──────────────────────────────────┤
│   Windows 11     │         macOS                    │
│  ┌────────────┐  │  ┌────────────┐                  │
│  │Win11Light  │  │  │MacOSLight  │                  │
│  │Win11Dark   │  │  │MacOSDark   │                  │
│  └────────────┘  │  │MacOSSysDark│（别名）           │
│                  │  └────────────┘                  │
├──────────────────┴──────────────────────────────────┤
│           AbstractOSTheme（抽象基类）                 │
│           implements atlantafx.base.theme.Theme     │
├─────────────────────────────────────────────────────┤
│  .root CSS 变量体系（~110 个语义化颜色变量）           │
│  组件覆盖样式（Button / TextInput / ScrollBar ...）   │
└─────────────────────────────────────────────────────┘
```

### 设计原则

- **UserAgent Stylesheet**: 以 `Application.setUserAgentStylesheet()` 方式全局生效，零侵入覆盖所有 JavaFX 原生组件和 AtlantaFX 组件
- **语义化变量**: 所有颜色通过 `.root` 区 CSS 变量定义，业务代码引用语义变量（如 `-color-accent-emphasis`）而非硬编码色值
- **双模式支持**: 每个操作系统风格均提供浅色 / 深色两套主题
- **自动检测**: `OSThemeFactory` 可自动检测当前操作系统和深色模式偏好
- **单例缓存**: 工厂内部缓存主题实例，避免重复创建

---

## 2. 快速开始

### 方式一：直接实例化主题

```java
// Windows 11 浅色主题
Application.setUserAgentStylesheet(new Windows11Light().getUserAgentStylesheet());

// macOS 深色主题
Application.setUserAgentStylesheet(new MacOSDark().getUserAgentStylesheet());
```

### 方式二：通过工厂类

```java
// 自动推荐当前操作系统的最佳主题
Theme theme = OSThemeFactory.recommendedTheme();
Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());

// 推荐浅色主题
Theme light = OSThemeFactory.recommendedLightTheme();
Application.setUserAgentStylesheet(light.getUserAgentStylesheet());
```

### 方式三：按名称查找

```java
Theme theme = OSThemeFactory.forName("Windows 11 Light");
if (theme != null) {
    Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
}
```

### 方式四：运行时切换主题

```java
// 在 ComboBox 中列出所有主题
ComboBox<Theme> themeSelector = new ComboBox<>();
themeSelector.getItems().addAll(OSThemeFactory.allThemes());

themeSelector.setOnAction(e -> {
    Theme selected = themeSelector.getValue();
    Application.setUserAgentStylesheet(selected.getUserAgentStylesheet());
});
```

---

## 3. 抽象基类 — AbstractOSTheme

`io.aurora.fx.theme.AbstractOSTheme implements atlantafx.base.theme.Theme`

### 3.1 类层级

```
atlantafx.base.theme.Theme（接口）
    └── AbstractOSTheme（抽象类）
            ├── Windows11Light
            ├── Windows11Dark
            ├── MacOSLight
            ├── MacOSDark
            │       └── MacOSSystemDark（别名子类）
```

### 3.2 构造函数

| 构造函数 | 访问级别 | 说明 |
|---------|----------|------|
| `AbstractOSTheme(String name, boolean darkMode, String stylesheet)` | `protected` | 子类调用，指定主题名称、深色模式标志和 CSS 路径 |

**参数说明**:

| 参数 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `name` | `String` | 非 null、非空 | 主题名称，用于显示和查找 |
| `darkMode` | `boolean` | — | `true` 为深色模式 |
| `stylesheet` | `String` | 非 null、非空 | CSS 样式表资源路径（`toExternalForm()` 格式） |

**异常**: 如果 `name` 或 `stylesheet` 为 null 或空字符串，抛出 `IllegalArgumentException`

### 3.3 接口方法

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getName()` | `String` | 返回主题名称 |
| `isDarkMode()` | `boolean` | 是否为深色模式 |
| `getUserAgentStylesheet()` | `String` | 返回 CSS 样式表路径 |
| `getUserAgentStylesheetBSS()` | `String` | 返回 BSS 编译样式表路径（当前返回 `null`） |
| `toString()` | `String` | 返回主题的字符串表示 |

> **关于 BSS**: BSS 是 JavaFX 的二进制 CSS 格式，加载速度比纯 CSS 更快。当前实现返回 `null`，JavaFX 将自动使用 CSS 文件。如需启用 BSS，可使用 AtlantaFX 提供的 `ThemeCompiler` 编译 CSS 文件并重写此方法。

---

## 4. 主题实现类

### 4.1 Windows11Light

`io.aurora.fx.theme.Windows11Light extends AbstractOSTheme`

**Windows 11 浅色主题** — 基于 Microsoft Fluent Design System

| 常量 | 值 | 说明 |
|------|---|------|
| `NAME` | `"Windows 11 Light"` | 主题名称 |
| `STYLESHEET` | 自动解析资源路径 | 指向 `windows11-light.css` |

**设计特征**:
- 圆角控件边框（6-8px 圆角半径）
- 柔和阴影与深度层次
- Windows 11 标准色彩系统（Accent: `#0078D4`）
- Segoe UI Variable 字体族
- Mica / Acrylic 风格的半透明背景效果
- 底部强调焦点指示器（Win11 标志性设计）

```java
Application.setUserAgentStylesheet(new Windows11Light().getUserAgentStylesheet());
// 或
Application.setUserAgentStylesheet(OSThemeFactory.windows11Light().getUserAgentStylesheet());
```

---

### 4.2 Windows11Dark

`io.aurora.fx.theme.Windows11Dark extends AbstractOSTheme`

**Windows 11 深色主题** — 基于 Microsoft Fluent Design System 深色模式

| 常量 | 值 | 说明 |
|------|---|------|
| `NAME` | `"Windows 11 Dark"` | 主题名称 |
| `STYLESHEET` | 自动解析资源路径 | 指向 `windows11-dark.css` |

**设计特征**:
- 深色背景色彩系统（Accent: `#60CDFF`）
- 与浅色版相同的圆角和阴影规格
- Segoe UI Variable 字体族

```java
Application.setUserAgentStylesheet(new Windows11Dark().getUserAgentStylesheet());
// 或
Application.setUserAgentStylesheet(OSThemeFactory.windows11Dark().getUserAgentStylesheet());
```

---

### 4.3 MacOSLight

`io.aurora.fx.theme.MacOSLight extends AbstractOSTheme`

**macOS 浅色主题** — 基于 Apple Human Interface Guidelines

| 常量 | 值 | 说明 |
|------|---|------|
| `NAME` | `"macOS Light"` | 主题名称 |
| `STYLESHEET` | 自动解析资源路径 | 指向 `macos-light.css` |

**设计特征**:
- 圆润边角（4-5px 圆角半径）
- 极窄精致阴影与深度层次
- macOS 标准色彩系统（Accent: `#007AFF`）
- SF Pro / -apple-system 字体族（13px 默认字号）
- Vibrancy 风格的半透明背景效果
- 全环 accent 聚焦光晕（macOS 标志性设计）
- 胶囊形自动隐藏滚动条

```java
Application.setUserAgentStylesheet(new MacOSLight().getUserAgentStylesheet());
// 或
Application.setUserAgentStylesheet(OSThemeFactory.macOSLight().getUserAgentStylesheet());
```

---

### 4.4 MacOSDark

`io.aurora.fx.theme.MacOSDark extends AbstractOSTheme`

**macOS 深色主题** — 基于 Apple Human Interface Guidelines 深色模式

| 常量 | 值 | 说明 |
|------|---|------|
| `NAME` | `"macOS Dark"` | 主题名称 |
| `STYLESHEET` | 自动解析资源路径 | 指向 `macos-dark.css` |

**设计特征**:
- 深色色彩系统（Accent: `#0A84FF`）
- 与浅色版相同的圆角和阴影规格
- SF Pro / -apple-system 字体族

```java
Application.setUserAgentStylesheet(new MacOSDark().getUserAgentStylesheet());
// 或
Application.setUserAgentStylesheet(OSThemeFactory.macOSDark().getUserAgentStylesheet());
```

---

### 4.5 MacOSSystemDark

`io.aurora.fx.theme.MacOSSystemDark extends MacOSDark`

**macOS 系统深色主题** — `MacOSDark` 的语义别名

| 常量 | 值 | 说明 |
|------|---|------|
| `NAME` | `"macOS System Dark"` | 主题名称（区别于父类） |

功能和行为与 `MacOSDark` 完全一致，仅名称不同，提供语义化的命名方式。

```java
Application.setUserAgentStylesheet(new MacOSSystemDark().getUserAgentStylesheet());
```

---

## 5. 工厂类 — OSThemeFactory

`io.aurora.fx.theme.OSThemeFactory`

所有操作系统主题的统一创建入口。工具类，不可实例化。

### 5.1 主题获取方法

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `allThemes()` | `List<Theme>` | 获取所有 5 个主题（不可变列表） |
| `lightThemes()` | `List<Theme>` | 获取所有浅色主题（不可变列表） |
| `darkThemes()` | `List<Theme>` | 获取所有深色主题（不可变列表） |
| `forName(String name)` | `Theme` | 按名称查找主题，未找到返回 `null` |
| `windows11Light()` | `Windows11Light` | 获取 Windows 11 浅色主题实例 |
| `windows11Dark()` | `Windows11Dark` | 获取 Windows 11 深色主题实例 |
| `macOSLight()` | `MacOSLight` | 获取 macOS 浅色主题实例 |
| `macOSDark()` | `MacOSDark` | 获取 macOS 深色主题实例 |

### 5.2 自动检测方法

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `recommendedLightTheme()` | `Theme` | 根据当前 OS 推荐浅色主题 |
| `recommendedDarkTheme()` | `Theme` | 根据当前 OS 推荐深色主题 |
| `recommendedTheme()` | `Theme` | 根据当前 OS + 系统深色模式偏好推荐主题 |

**自动检测逻辑**:

| 操作系统 | 推荐浅色主题 | 推荐深色主题 |
|---------|------------|------------|
| Windows | `Windows11Light` | `Windows11Dark` |
| macOS / Darwin | `MacOSLight` | `MacOSDark` |
| 其他（Linux 等） | `Windows11Light`（默认） | `Windows11Dark`（默认） |

`recommendedTheme()` 会尝试通过 JavaFX Platform Preferences API 检测系统是否处于深色模式，无法检测时默认返回浅色主题。

### 5.3 可用主题名称一览

| 主题名称 | 对应类 | 深色模式 |
|---------|--------|---------|
| `"Windows 11 Light"` | `Windows11Light` | `false` |
| `"Windows 11 Dark"` | `Windows11Dark` | `true` |
| `"macOS Light"` | `MacOSLight` | `false` |
| `"macOS Dark"` | `MacOSDark` | `true` |
| `"macOS System Dark"` | `MacOSSystemDark` | `true` |

### 5.4 完整使用示例

```java
import io.aurora.fx.theme.OSThemeFactory;
import atlantafx.base.theme.Theme;

public class ThemeExample extends Application {
    @Override
    public void start(Stage stage) {
        // 方式一：自动推荐
        Theme theme = OSThemeFactory.recommendedTheme();
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());

        // 方式二：构建主题切换 UI
        ComboBox<Theme> selector = new ComboBox<>();
        selector.getItems().addAll(OSThemeFactory.allThemes());
        selector.setConverter(new StringConverter<>() {
            @Override public String toString(Theme t) { return t == null ? "" : t.getName(); }
            @Override public Theme fromString(String s) { return OSThemeFactory.forName(s); }
        });
        selector.setOnAction(e -> {
            Theme selected = selector.getValue();
            Application.setUserAgentStylesheet(selected.getUserAgentStylesheet());
        });

        // 方式三：浅色/深色切换
        ToggleButton darkToggle = new ToggleButton("深色模式");
        darkToggle.setOnAction(e -> {
            Theme t = darkToggle.isSelected()
                ? OSThemeFactory.recommendedDarkTheme()
                : OSThemeFactory.recommendedLightTheme();
            Application.setUserAgentStylesheet(t.getUserAgentStylesheet());
        });

        VBox root = new VBox(10, selector, darkToggle);
        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }
}
```

---

## 6. 语义化 CSS 变量体系

所有主题共享同一套语义化 CSS 变量名，在 `.root` 区定义。业务代码和自定义样式应使用这些变量而非硬编码颜色值，确保主题切换时自动适配。

### 6.1 基础色阶变量

每个色系提供 10 级色阶（0 最浅 → 9 最深），适用于精细的视觉层次控制。

| 变量前缀 | 范围 | Windows 11 Light 基准色 | macOS Light 基准色 | 说明 |
|---------|------|------------------------|-------------------|------|
| `-color-base-{0~9}` | 0~9 | `#faf9f8` → `#1b1a19` | `#f5f5f7` → `#1d1d1f` | 中性灰阶 |
| `-color-accent-{0~9}` | 0~9 | `#deecf9` → `#003052` | `#d6eaff` → `#001f33` | 主题强调色 |
| `-color-success-{0~9}` | 0~9 | `#dff6dd` → `#063006` | `#d4f5d8` → `#073812` | 成功状态色 |
| `-color-warning-{0~9}` | 0~9 | `#fff4ce` → `#3d2400` | `#fff2cc` → `#332002` | 警告状态色 |
| `-color-danger-{0~9}` | 0~9 | `#fde7e9` → `#4f0009` | `#fce4e4` → `#520e0e` | 危险状态色 |

**额外基础变量**:

| 变量 | Windows 11 Light | macOS Light | 说明 |
|------|-----------------|-------------|------|
| `-color-dark` | `#1b1a19` | `#1d1d1f` | 最深色 |
| `-color-light` | `#ffffff` | `#ffffff` | 最浅色 |

### 6.2 语义色彩变量

高层语义变量，推荐在业务代码中优先使用。

#### 前景色（文字/图标）

| 变量 | Windows 11 Light | macOS Light | 用途 |
|------|-----------------|-------------|------|
| `-color-fg-default` | `#1a1a1a` | `#1d1d1f` | 默认文字颜色 |
| `-color-fg-muted` | `#616161` | `#6e6e73` | 次要文字颜色 |
| `-color-fg-subtle` | `#8a8a8a` | `#86868b` | 辅助/占位文字 |
| `-color-fg-emphasis` | `#ffffff` | `#ffffff` | 强调背景上的文字 |

#### 背景色

| 变量 | Windows 11 Light | macOS Light | 用途 |
|------|-----------------|-------------|------|
| `-color-bg-default` | `#f3f3f3` | `#ffffff` | 页面默认背景 |
| `-color-bg-overlay` | `#ffffff` | `#ffffff` | 浮层/卡片背景 |
| `-color-bg-subtle` | `#faf9f8` | `#f5f5f7` | 次级背景 |
| `-color-bg-inset` | `#ffffff` | `#f5f5f7` | 内嵌区域背景 |

#### 边框色

| 变量 | Windows 11 Light | macOS Light | 用途 |
|------|-----------------|-------------|------|
| `-color-border-default` | `#d1d1d1` | `#d2d2d7` | 默认边框 |
| `-color-border-muted` | `#e0e0e0` | `#e0e0e5` | 弱化边框 |
| `-color-border-subtle` | `rgb(234,234,234)` | `rgb(230,230,235)` | 极淡边框 |

#### 阴影色

| 变量 | Windows 11 Light | macOS Light | 用途 |
|------|-----------------|-------------|------|
| `-color-shadow-default` | `rgba(0,0,0,0.13)` | `rgba(0,0,0,0.10)` | 投影阴影色 |

#### 中性色

| 变量 | Windows 11 Light | macOS Light | 用途 |
|------|-----------------|-------------|------|
| `-color-neutral-emphasis-plus` | `#1a1a1a` | `#1d1d1f` | 最强中性强调 |
| `-color-neutral-emphasis` | `#8a8a8a` | `#86868b` | 中性强调 |
| `-color-neutral-muted` | `rgba(0,0,0,0.06)` | `rgba(142,142,147,0.18)` | 柔和中性背景 |
| `-color-neutral-subtle` | `rgba(0,0,0,0.03)` | `rgba(142,142,147,0.08)` | 极淡中性背景 |

#### 功能色（accent / success / warning / danger）

每个功能色提供 4 个语义级别：

| 后缀 | 用途 | 示例（accent） |
|------|------|---------------|
| `-fg` | 文字/前景 | `-color-accent-fg` |
| `-emphasis` | 强调/填充 | `-color-accent-emphasis` |
| `-muted` | 柔和（30% 透明度） | `-color-accent-muted` |
| `-subtle` | 极淡背景 | `-color-accent-subtle` |

**完整功能色变量表**:

| 变量 | Win11 Light | macOS Light | 说明 |
|------|------------|-------------|------|
| `-color-accent-fg` | `#0078d4` | `#007aff` | 链接/强调文字 |
| `-color-accent-emphasis` | `#0078d4` | `#007aff` | 主按钮/强调背景 |
| `-color-accent-muted` | `rgba(0,120,212,0.3)` | `rgba(0,122,255,0.3)` | 柔和强调 |
| `-color-accent-subtle` | `#deecf9` | `#d6eaff` | 极淡强调背景 |
| `-color-success-fg` | `#0f7b0f` | `#28a745` | 成功文字 |
| `-color-success-emphasis` | `#47bb47` | `#34c759` | 成功强调 |
| `-color-success-muted` | `rgba(15,123,15,0.3)` | `rgba(52,199,89,0.3)` | 柔和成功 |
| `-color-success-subtle` | `#dff6dd` | `#d4f5d8` | 极淡成功背景 |
| `-color-warning-fg` | `#9d5d00` | `#ff9f0a` | 警告文字 |
| `-color-warning-emphasis` | `#c19c00` | `#ffb000` | 警告强调 |
| `-color-warning-muted` | `rgba(157,93,0,0.3)` | `rgba(255,159,10,0.3)` | 柔和警告 |
| `-color-warning-subtle` | `#fff4ce` | `#fff2cc` | 极淡警告背景 |
| `-color-danger-fg` | `#c42b1c` | `#ff3b30` | 危险文字 |
| `-color-danger-emphasis` | `#c42b1c` | `#ff3b30` | 危险强调 |
| `-color-danger-muted` | `rgba(196,43,28,0.3)` | `rgba(255,59,48,0.3)` | 柔和危险 |
| `-color-danger-subtle` | `#fde7e9` | `#fce4e4` | 极淡危险背景 |

### 6.3 图表色彩变量

提供 8 组图表配色，每组 3 个透明度级别。

| 变量 | 透明度 | Windows 11 Light 色值 |
|------|--------|----------------------|
| `-color-chart-{1~8}` | 100% | `#0078d4`, `#47bb47`, `#c19c00`, `#c42b1c`, `#8661c5`, `#038387`, `#e74856`, `#8a8a8a` |
| `-color-chart-{1~8}-alpha70` | 70% | 同上色值，70% 不透明度 |
| `-color-chart-{1~8}-alpha20` | 20% | 同上色值，20% 不透明度 |

---

## 7. 组件覆盖样式

每个主题的 CSS 文件约 5200 行，前 ~4967 行为 AtlantaFX 基础样式（含 `.root` 变量定义），后 ~240 行为操作系统风格的组件覆盖区域。

### 7.1 Windows 11 Fluent Design 组件覆盖

设计关键词：**圆角 6-8px、柔和扩散阴影、底部强调焦点、薄型滚动条**

| 组件 | CSS 选择器 | 覆盖特征 |
|------|-----------|---------|
| **Button** | `.button` | 6px 圆角 + 柔和阴影（2px idle → 4px hover → 1px pressed） |
| **TextInput** | `.text-input` | 6px 圆角 + **底部 accent bar 聚焦**（Win11 标志性） |
| **TextArea** | `.text-area` | 6px 圆角，聚焦时无阴影 |
| **CheckBox** | `.check-box > .box` | 4px 圆角 |
| **ComboBox** | `.combo-box-base` | 6px 圆角 |
| **Spinner** | `.spinner` | 6px 圆角 |
| **ContextMenu** | `.context-menu` | 8px 圆角 + 16px Acrylic 阴影 |
| **Dialog** | `.dialog-pane` | 8px 圆角 |
| **Tooltip** | `.tooltip` | 6px 圆角 + 8px 阴影 + 94% 不透明度 |
| **ScrollBar** | `.scroll-bar` | 4px 薄型 + hover 展开 10px + 35% 默认透明 |
| **MenuButton** | `.menu-button` | 6px 圆角 + 柔和阴影 |
| **MenuBar** | `.menu-bar` | 直角（0px），子菜单按钮 6px |
| **MenuItem** | `.menu-item` | 4px 圆角 hover |
| **TabPane** | `.tab-pane > ... > .tab` | 6px 顶部圆角 |
| **ListView/TableView/TreeView** | `.list-cell` / `.table-row-cell` / `.tree-cell` | 4px 行圆角 |
| **TitledPane** | `.titled-pane > .title` | 6px 顶部圆角 |
| **Slider** | `.slider > .track` | 4px 圆角轨道 |
| **ProgressBar** | `.progress-bar > .track/.bar` | 4px / 3px 圆角 |
| **Separator** | `.separator > .line` | 0.5px 薄型分割线 |

**Win11 底部强调焦点指示器实现**:

```css
.text-input:focused {
  -fx-background-color: -color-input-border-focused, -color-input-border, -color-input-bg-focused;
  -fx-background-insets: 0 0 0 0, 0 0 2px 0, 1px 1px 3px 1px;
  -fx-background-radius: 6px, 6px, 5px;
}
```

通过三层背景叠加，底部 2px 显示 accent 色，形成 Windows 11 标志性的底部强调条。

---

### 7.2 macOS Vibrancy 组件覆盖

设计关键词：**圆角 4-5px、无阴影按钮、全环聚焦光晕、胶囊滚动条、opacity 反馈**

| 组件 | CSS 选择器 | 覆盖特征 |
|------|-----------|---------|
| **Button** | `.button` | 5px 圆角 + **无阴影** + hover 降低 opacity 0.88 + pressed opacity 0.75 |
| **Button:focused** | `.button:focused` | **全环 accent 光晕**（macOS 标志性） |
| **TextInput** | `.text-input` | 5px 圆角 |
| **TextInput:focused** | `.text-input:focused` | **全环 accent 光晕**（4px 扩散） |
| **TextArea** | `.text-area` | 5px 圆角 + 聚焦全环光晕 |
| **CheckBox** | `.check-box > .box` | 4px 圆角 |
| **ComboBox** | `.combo-box-base` | 5px 圆角 |
| **Spinner** | `.spinner` | 5px 圆角 |
| **ContextMenu** | `.context-menu` | 6px 圆角 + 10px 极窄阴影 |
| **Dialog** | `.dialog-pane` | 10px 圆角 |
| **Tooltip** | `.tooltip` | 4px 圆角 + 4px 极窄阴影 + 96% 不透明度 |
| **ScrollBar** | `.scroll-bar` | **胶囊形** 100px thumb radius + **默认隐藏** opacity 0 + hover 显示 0.80 |
| **MenuButton** | `.menu-button` | 5px 圆角 + 无阴影 |
| **MenuBar** | `.menu-bar` | 直角，子菜单按钮 4px |
| **MenuItem** | `.menu-item` | 4px 圆角 hover |
| **TabPane** | `.tab-pane > ... > .tab` | 5px 顶部圆角 |
| **ListView/TableView/TreeView** | `.list-cell` / `.table-row-cell` / `.tree-cell` | 3px 行圆角 |
| **TitledPane** | `.titled-pane > .title` | 5px 顶部圆角 |
| **Slider** | `.slider > .track` | 4px 圆角轨道 |
| **ProgressBar** | `.progress-bar > .track/.bar` | 4px / 3px 圆角 |
| **Separator** | `.separator > .line` | 0.5px 细线 |

**macOS 全环聚焦光晕实现**:

```css
.button:focused {
  -fx-effect: dropshadow(three-pass-box, -color-accent-emphasis, 3px, 0.35, 0, 0);
}
.text-input:focused {
  -fx-effect: dropshadow(three-pass-box, -color-accent-emphasis, 4px, 0.40, 0, 0);
}
```

四方向均匀扩散的 accent 色光晕，是 macOS 标志性的聚焦视觉反馈。

---

## 8. 文字排版系统

两套主题共享相同的排版类名体系，字体族和基准字号不同。

### 8.1 全局字体配置

| 属性 | Windows 11 | macOS |
|------|-----------|-------|
| 字体族 | `"Segoe UI Variable", "Segoe UI", system-ui, sans-serif` | `-apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", "Segoe UI", sans-serif` |
| 基准字号 | `14px` | `13px` |

### 8.2 排版 CSS 类

| CSS 类名 | 字号 | 字重 | 用途 |
|---------|------|------|------|
| `.title-1` | `2em` | `bolder` | 一级标题 |
| `.title-2` | `1.75em` | `bolder` | 二级标题 |
| `.title-3` | `1.5em` | `bolder` | 三级标题 |
| `.title-4` | `1.25em` | `normal` | 四级标题 |
| `.text-caption` | `1em` | `bold` | 标题说明 |
| `.text-small` | `0.8em` | — | 小号文字 |
| `.text-bold` | — | `bold` | 粗体 |

### 8.3 文字颜色类

| CSS 类名 | 引用变量 | 用途 |
|---------|---------|------|
| `.text.accent` | `-color-accent-fg` | 强调文字 |
| `.text.success` | `-color-success-fg` | 成功文字 |
| `.text.warning` | `-color-warning-fg` | 警告文字 |
| `.text.danger` | `-color-danger-fg` | 危险文字 |
| `.text-muted` | `-color-fg-muted` | 次要文字 |
| `.text-subtle` | `-color-fg-subtle` | 辅助文字 |
| `.text-on-emphasis` | `-color-fg-emphasis` | 强调背景上的文字 |

```java
// 使用排版类
Label title = new Label("标题");
title.getStyleClass().add("title-1");

Label subtitle = new Label("副标题");
subtitle.getStyleClass().addAll("text-muted", "text-small");
```

---

## 9. 海拔阴影系统

提供 4 级预定义海拔阴影，通过 CSS 类直接使用。

### Windows 11 海拔阴影

| CSS 类名 | 阴影参数 | 视觉效果 |
|---------|---------|---------|
| `.elevated-1` | `2px spread, 0.30 ratio, Y-offset 1` | 微弱浮起（按钮级） |
| `.elevated-2` | `6px spread, 0.30 ratio, Y-offset 2` | 卡片悬浮 |
| `.elevated-3` | `14px spread, 0.32 ratio, Y-offset 4` | 弹出层/下拉 |
| `.elevated-4` | `22px spread, 0.32 ratio, Y-offset 6` | 对话框/模态 |

### macOS 海拔阴影

| CSS 类名 | 阴影参数 | 与 Win11 差异 |
|---------|---------|-------------|
| `.elevated-1` | `2px spread, 0.18 ratio, Y-offset 0.5` | 更窄、更精致 |
| `.elevated-2` | `5px spread, 0.20 ratio, Y-offset 1.5` | 扩散更小 |
| `.elevated-3` | `10px spread, 0.22 ratio, Y-offset 3` | Win11 14px vs macOS 10px |
| `.elevated-4` | `16px spread, 0.22 ratio, Y-offset 5` | Win11 22px vs macOS 16px |

```java
// 为卡片添加海拔阴影
VBox card = new VBox();
card.getStyleClass().add("elevated-2");
```

---

## 10. 主题切换与集成

### 10.1 应用启动时设置主题

```java
@Override
public void start(Stage stage) {
    // 推荐方式：自动检测操作系统
    Application.setUserAgentStylesheet(
        OSThemeFactory.recommendedTheme().getUserAgentStylesheet()
    );

    // 后续创建 Scene 和 UI...
}
```

### 10.2 运行时动态切换

```java
// 主题切换时，所有 UI 组件自动更新外观
public void switchTheme(Theme newTheme) {
    Application.setUserAgentStylesheet(newTheme.getUserAgentStylesheet());
}
```

### 10.3 与自定义 CSS 共存

```java
// UserAgent Stylesheet 作为基础层
Application.setUserAgentStylesheet(
    OSThemeFactory.windows11Light().getUserAgentStylesheet()
);

// 应用级 CSS 作为覆盖层（在 Scene 上加载）
scene.getStylesheets().add(
    getClass().getResource("/custom-overrides.css").toExternalForm()
);
```

> **优先级**: `setStyle()` 内联样式 > Scene CSS 文件 > UserAgent Stylesheet

### 10.4 在业务代码中引用主题变量

```css
/* custom-overrides.css */
.my-card {
    -fx-background-color: -color-bg-overlay;
    -fx-border-color: -color-border-muted;
    -fx-border-radius: 8px;
    -fx-background-radius: 8px;
    -fx-effect: dropshadow(three-pass-box, -color-shadow-default, 6px, 0.3, 0, 2);
}
.my-card:hover {
    -fx-border-color: -color-accent-muted;
}
.my-link {
    -fx-text-fill: -color-accent-fg;
}
.my-error-text {
    -fx-text-fill: -color-danger-fg;
}
```

```java
// 在 Java 代码中引用
label.setStyle("-fx-text-fill: -color-fg-muted;");
card.setStyle("-fx-background-color: -color-bg-overlay; "
    + "-fx-border-color: -color-border-muted;");
```

---

## 11. 自定义主题扩展

### 11.1 创建新的操作系统主题

```java
public class UbuntuLight extends AbstractOSTheme {
    public static final String NAME = "Ubuntu Light";
    public static final String STYLESHEET = UbuntuLight.class.getResource(
            "ubuntu-light.css").toExternalForm();

    public UbuntuLight() {
        super(NAME, false, STYLESHEET);
    }
}
```

### 11.2 CSS 文件结构

自定义主题 CSS 文件应包含：

1. **`.root` 变量定义区**（必需）：定义 ~110 个语义化颜色变量
2. **基础组件样式区**（推荐使用 AtlantaFX 基础样式）
3. **组件覆盖区**（自定义部分）：覆盖特定组件的圆角、阴影、焦点效果等

```css
/* ubuntu-light.css */
.root {
  -color-accent-5: #E95420; /* Ubuntu 橙色 */
  -color-accent-emphasis: #E95420;
  -color-accent-fg: #E95420;
  -fx-font-family: "Ubuntu", "Noto Sans", sans-serif;
  /* ... 其他变量 ... */
}

/* 组件覆盖 */
.button {
  -fx-background-radius: 4px, 3px;
}
```

### 11.3 注册到工厂（可选）

如需将自定义主题注册到 `OSThemeFactory`，可继承或组合工厂类。当前工厂使用 `final` 修饰且主题列表为静态不可变列表，建议通过组合方式扩展：

```java
public class ExtendedThemeFactory {
    private static final List<Theme> EXTENDED_THEMES = new ArrayList<>();
    static {
        EXTENDED_THEMES.addAll(OSThemeFactory.allThemes());
        EXTENDED_THEMES.add(new UbuntuLight());
    }
    public static List<Theme> allThemes() { return List.copyOf(EXTENDED_THEMES); }
}
```

---

## 12. 设计语言对比

| 特征 | Windows 11 Fluent Design | macOS Vibrancy |
|------|-------------------------|----------------|
| **哲学** | 现代、柔和、分层 | 精致、优雅、统一 |
| **按钮圆角** | 6px | 5px |
| **按钮阴影** | 柔和扩散阴影 | 无阴影（opacity 反馈） |
| **聚焦指示器** | 底部 accent bar | 全环 accent 光晕 |
| **滚动条** | 薄型 4px + hover 10px + 35% 透明 | 胶囊形 7px + 默认隐藏 + hover 显示 |
| **上下文菜单** | 8px 圆角 + 16px 阴影 | 6px 圆角 + 10px 窄阴影 |
| **对话框** | 8px 圆角 | 10px 圆角 |
| **列表行** | 4px 圆角 | 3px 圆角 |
| **基准字号** | 14px | 13px |
| **字体族** | Segoe UI Variable | SF Pro / -apple-system |
| **阴影风格** | 柔和扩散（大范围 + 高透明度） | 极窄精致（小范围 + 低透明度） |
| **强调色** | `#0078D4`（浅色） / `#60CDFF`（深色） | `#007AFF`（浅色） / `#0A84FF`（深色） |

---

## 13. 常见问题 FAQ

### Q: 主题切换时需要重新创建 Scene 吗？

A: 不需要。`Application.setUserAgentStylesheet()` 会自动刷新所有已存在的 Scene 和 Node，无需重建 UI。

### Q: 可以同时使用两套主题吗？

A: 不可以。`UserAgentStylesheet` 是全局唯一的，一次只能激活一个主题。但可以在 Scene 级别叠加额外的 CSS 文件。

### Q: 主题 CSS 变量在 Java 代码中能用吗？

A: 可以。在 `setStyle()` 中直接引用变量名即可，如 `node.setStyle("-fx-background-color: -color-accent-emphasis;");`。JavaFX 会在运行时解析为当前主题的实际颜色值。

### Q: 为什么 macOS 主题的按钮没有阴影？

A: 这是忠实还原 macOS 原生设计。macOS 的按钮使用 opacity 变化（hover 0.88 → pressed 0.75）作为交互反馈，而非阴影变化。聚焦时使用全环 accent 光晕。

### Q: MacOSSystemDark 和 MacOSDark 有什么区别？

A: `MacOSSystemDark` 是 `MacOSDark` 的语义别名子类，功能完全相同，仅 `getName()` 返回 `"macOS System Dark"` 而非 `"macOS Dark"`。

### Q: 如何获取当前正在使用的主题？

A: 可以通过 `Application.getUserAgentStylesheet()` 获取当前样式表路径，然后与各主题的 `getUserAgentStylesheet()` 比较。或在应用中自行维护当前主题引用。

### Q: BSS 编译有什么好处？

A: BSS 是 JavaFX 的二进制 CSS 格式，加载速度比纯 CSS 更快。对于 5000+ 行的主题 CSS，BSS 可以显著减少首次加载时间。当前实现返回 `null`，JavaFX 会自动解析 CSS 文件。可使用 AtlantaFX 的 `ThemeCompiler` 工具编译。

### Q: 支持哪些 JavaFX 组件？

A: 主题覆盖了所有 JavaFX 原生组件（Button、TextField、ComboBox、ListView、TableView、TreeView、TabPane、ScrollBar、Slider、ProgressBar、Dialog、ContextMenu、Tooltip 等）以及 AtlantaFX 的扩展组件。

### Q: 在 Linux 上推荐什么主题？

A: `recommendedLightTheme()` 和 `recommendedDarkTheme()` 在 Linux 上默认返回 Windows 11 主题。如果偏好更紧凑的 UI，可以手动选择 macOS 主题（13px 字号更适合高 DPI 屏幕）。

---

> **文档版本**: v1.0  
> **适用版本**: Aurora-FX 0.0.1+  
> **技术栈**: Java 25 + JavaFX 25 + AtlantaFX 2.1.0
