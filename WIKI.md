# Aurora-FX Wiki — 综合 API 文档索引

<p align="center">
  <a href="https://github.com/lxwise/aurora-fx/">
    <img src="./doc/aurora-fx.png" alt="Aurora-FX Logo" width="180">
  </a>
</p>

<p align="center">
  <b>Aurora-FX</b> 是一款高性能 JavaFX 自定义 UI 组件库，以"曙光"为名，<br/>
  致力于为开发者提供优雅、高效的界面开发体验。
</p>

<p align="center">
  <img src="https://img.shields.io/hexpm/l/plug.svg" alt="License"/>
  <img src="https://img.shields.io/badge/build-maven-green" alt="Build"/>
  <img src="https://img.shields.io/badge/java-25-%23F27E3F" alt="Java"/>
  <img src="https://img.shields.io/badge/javafx-25-%23F27E3F" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/atlantafx-2.1.0-blue" alt="atlantafx"/>
  <img src="https://img.shields.io/badge/ikonli-12.3.1-blueviolet" alt="ikonli"/>
</p>

---

## 目录

- [项目简介](#项目简介)
- [项目特性](#项目特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [模块文档索引](#模块文档索引)
  - [并发与异步](#并发与异步)
  - [事件总线](#事件总线)
  - [基础工具](#基础工具)
  - [UI 主题](#ui-主题)
  - [UI 组件](#ui-组件)
- [按特性查找](#按特性查找)
- [示例代码索引](#示例代码索引)
- [构建与发布](#构建与发布)
- [贡献指南](#贡献指南)
- [开源协议](#开源协议)

---

## 项目简介

Aurora-FX 是一款基于 **JavaFX 25 / Java 25** 的现代化 UI 组件库，构建在
[**atlantafx**](https://github.com/mkpaz/atlantafx) 与 [**Ikonli**](https://kordamp.org/ikonli/)
之上，提供了一系列开箱即用的桌面端 UI 组件、主题系统、并发与事件总线工具。

| 项目坐标 | 值 |
| -------- | -- |
| **GroupId** | `io.github.lxwise` |
| **ArtifactId** | `aurora-fx` |
| **版本** | `0.0.1` |
| **License** | Apache License 2.0 |
| **Java** | 25 |
| **JavaFX** | 25 |
| **atlantafx** | 2.1.0 |
| **Ikonli** | 12.3.1 |

**项目主页：**
- 📦 GitHub: <https://github.com/lxwise/aurora-fx>
- 📦 Gitee: <https://gitee.com/lxwise/aurora-fx>
- 📦 Maven Central: <https://repo1.maven.org/maven2/io/github/lxwise/aurora-fx/>

---

## 项目特性

- 🎨 **现代化主题** — 内置 macOS 与 Windows 11 风格亮/暗主题，支持运行时切换
- 🧩 **丰富组件** — Avatar 头像、动态表单、图片查看器、行式按钮、步骤条、漫游引导、平移按钮、文件上传、验证码……
- ⚡ **强大并发** — `ProcessChain` 链式任务调度 + `ObservableExecutor` 可观察执行器
- 📡 **轻量事件** — 单例 `EventBus`，支持继承分发、key 路由、异步发布
- 🛠️ **跨平台路径** — `SystemUtils` + `Paths` 自动识别 Windows/macOS/Linux 标准目录
- 🔌 **开箱即用** — Maven Central 一行依赖即可引入

---

## 技术栈

```
┌─────────────────────────────────────────────────────────┐
│                     Aurora-FX 0.0.1                     │
├─────────────────────────────────────────────────────────┤
│ JavaFX 25       │ atlantafx 2.1.0 │ Ikonli 12.3.1       │
│ (controls/base/ │ (现代化主题基础) │ (AntDesign / Boxicons)│
│  swing/graphics)│                 │                      │
├─────────────────────────────────────────────────────────┤
│             Java 25 + Maven 3.9 (build)                  │
└─────────────────────────────────────────────────────────┘
```

| 维度 | 选型 |
| ---- | ---- |
| 编译目标 | JDK 25 |
| 构建工具 | Maven 3.9+ |
| UI 主题基础 | atlantafx-base |
| 图标 | ikonli-javafx + ikonli-antdesignicons-pack + ikonli-boxicons-pack |
| 测试 | JUnit Jupiter 5.10.2 |
| 编码 | UTF-8 |

---

## 项目结构

```
aurora-fx/
├── doc/                          ← 📚 所有 API 文档
│   ├── aurora-fx.png             # Logo
│   ├── concurrent-api.md         # 并发模块 API
│   ├── event-api.md              # 事件模块 API
│   ├── path-api.md               # 跨平台路径工具 API
│   ├── theme-api.md              # 主题系统 API
│   ├── component-avatar.md       # 头像组件 API
│   ├── component-dynamic-form.md # 动态表单组件 API
│   ├── component-line-button.md  # 行式按钮组件 API
│   ├── component-steps.md        # 步骤条组件 API
│   ├── component-tour.md         # 漫游引导组件 API
│   ├── component-translation-button.md  # 平移按钮组件 API
│   ├── component-upload.md       # 文件上传组件 API
│   └── component-verify-code.md  # 验证码组件 API
│
├── src/
│   ├── main/java/io/aurora/fx/
│   │   ├── common/               # 公共工具
│   │   │   ├── enums/            # 通用枚举
│   │   │   ├── path/             # 跨平台路径
│   │   │   └── utils/            # 通用工具类
│   │   ├── components/           # UI 组件
│   │   │   ├── avatar/           # 头像
│   │   │   ├── dynamicForm/      # 动态表单
│   │   │   ├── imageViewer/      # 图片查看器
│   │   │   ├── lineButton/       # 行式按钮
│   │   │   ├── steps/            # 步骤条
│   │   │   ├── tour/             # 漫游引导
│   │   │   ├── translationButton/# 平移按钮
│   │   │   ├── upload/           # 文件上传
│   │   │   └── verifyCode/       # 验证码
│   │   ├── concurrent/           # 并发框架
│   │   ├── event/                # 事件总线
│   │   └── theme/                # 主题系统
│   ├── main/resources/io/aurora/fx/  # CSS / 图片资源
│   └── test/                     # 演示与测试代码
│
├── README.md                     # 项目说明
├── WIKI.md                       # 本文件 — 综合 Wiki 索引
├── pom.xml
└── LICENSE
```

---

## 快速开始

### 1. Maven 依赖

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>aurora-fx</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. Gradle 依赖

```groovy
implementation 'io.github.lxwise:aurora-fx:0.0.1'
```

### 3. Hello Aurora-FX

```java
import io.aurora.fx.components.avatar.Avatar;
import io.aurora.fx.components.avatar.AvatarShape;
import io.aurora.fx.concurrent.ProcessChain;
import io.aurora.fx.theme.OSThemeFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Demo extends Application {
    @Override
    public void start(Stage stage) {
        // 1. 应用主题
        OSThemeFactory.applyDefault();

        // 2. 创建组件
        Avatar avatar = new Avatar();
        avatar.setShape(AvatarShape.CIRCLE);

        // 3. 异步加载头像
        ProcessChain.create()
            .addSupplierInExecutor(() -> userService.getAvatarUrl())
            .addConsumerInPlatformThread(url -> avatar.setImage(url))
            .onException(Throwable::printStackTrace)
            .run();

        stage.setScene(new Scene(new VBox(avatar), 320, 240));
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
```

---

## 模块文档索引

### 并发与异步

| 文档 | 路径 | 适用场景 |
| ---- | ---- | -------- |
| **[Concurrent API](./doc/concurrent-api.md)** | `io.aurora.fx.concurrent` | 任务链 / 异步任务 / 状态反馈 / 线程池 |

**核心 API：**
- [`ProcessChain`](./doc/concurrent-api.md#processchain--任务链) — 链式任务调度（**最常用**）
- [`ObservableExecutor`](./doc/concurrent-api.md#observableexecutor--可观察执行器) — 可观察执行器
- [`ExceptionHandler`](./doc/concurrent-api.md#exceptionhandler--异常处理器) — 异常统一处理
- [`DataFxTask` / `DataFxService`](./doc/concurrent-api.md#datafxtask) — 任务/服务基类
- [`PublishingTask`](./doc/concurrent-api.md#publishingtask) — 流式发布任务

### 事件总线

| 文档 | 路径 | 适用场景 |
| ---- | ---- | -------- |
| **[Event API](./doc/event-api.md)** | `io.aurora.fx.event` | 跨组件通信 / 主题变更广播 / 全局通知 |

**核心 API：**
- [`Event`](./doc/event-api.md#event--事件基类) — 事件基类
- [`EventBus`](./doc/event-api.md#eventbus--事件总线) — 单例事件总线
- [`NodeNoticeEvent`](./doc/event-api.md#nodenoticeevent--组件通信事件) — 组件间通信（按 key 路由）
- [`LoadingEvent` / `NoticeEvent` / `ThemeEvent`](./doc/event-api.md#内置事件类型) — 内置事件

### 基础工具

| 文档 | 路径 | 适用场景 |
| ---- | ---- | -------- |
| **[Path API](./doc/path-api.md)** | `io.aurora.fx.common.path` | 跨平台路径 / 系统目录获取 / 平台识别 |

**核心 API：**
- `SystemUtils` — 系统类型检测、平台属性
- `Paths` — 用户目录、配置目录、临时目录、应用数据目录的跨平台获取

### UI 主题

| 文档 | 路径 | 适用场景 |
| ---- | ---- | -------- |
| **[Theme API](./doc/theme-api.md)** | `io.aurora.fx.theme` | macOS/Windows11 亮暗主题 / 主题切换 |

**核心 API：**
- `AbstractOSTheme` — 主题抽象基类
- `MacOSDark` / `MacOSLight` / `MacOSSystemDark`
- `Windows11Dark` / `Windows11Light`
- `OSThemeFactory` — 主题工厂

### UI 组件

| 组件 | 文档 | 关键能力 |
| ---- | ---- | -------- |
| 🖼️ **Avatar 头像** | [component-avatar](./doc/component-avatar.md) | 8 种形状、文字/图片源、主题预设 |
| 📋 **DynamicForm 动态表单** | [component-dynamic-form](./doc/component-dynamic-form.md) | Form / FormItem / FormModel / 校验器 |
| 🔍 **ImageViewer 图片查看器** | *源码：`io.aurora.fx.components.imageViewer`* | 缩放、平移、旋转 |
| 📐 **LineButton 行式按钮** | [component-line-button](./doc/component-line-button.md) | EXTEND / RISE 动画 |
| 🪜 **Steps 步骤条** | [component-steps](./doc/component-steps.md) | 多步骤导航、横向/纵向布局 |
| 🧭 **Tour 漫游引导** | [component-tour](./doc/component-tour.md) | 12+1 种定位、遮罩、链式步骤 |
| 🌐 **TranslationButton 平移按钮** | [component-translation-button](./doc/component-translation-button.md) | 4 种平移方向 |
| 📤 **Upload 文件上传** | [component-upload](./doc/component-upload.md) | FileUploader + 8 个独立 Node 组件 |
| 🔢 **VerifyCode 验证码** | [component-verify-code](./doc/component-verify-code.md) | 多种类型、自定义生成器 |

---

## 按特性查找

### "我想 …"

| 需求 | 推荐文档 |
| ---- | -------- |
| 在按钮点击后调用接口并刷新 UI | [Concurrent — ProcessChain](./doc/concurrent-api.md#processchain--任务链) |
| 显示一个全局通知/Toast | [Event — NoticeEvent](./doc/event-api.md#noticeevent--通知事件) |
| 不同 Controller 之间通信 | [Event — NodeNoticeEvent](./doc/event-api.md#nodenoticeevent--组件通信事件) |
| 监听主题切换 | [Event — ThemeChangeEvent](./doc/event-api.md#themechangeevent--主题变更事件) |
| 获取应用数据目录、临时目录 | [Path API](./doc/path-api.md) |
| 切换 macOS / Windows 11 主题 | [Theme API](./doc/theme-api.md) |
| 显示用户头像（圆形/方形） | [Avatar](./doc/component-avatar.md) |
| 用配置驱动构建一个表单 | [DynamicForm](./doc/component-dynamic-form.md) |
| 引导新用户走流程 | [Tour](./doc/component-tour.md) |
| 上传单/多文件，含拖拽与进度 | [Upload](./doc/component-upload.md) |
| 显示注册/向导多步骤 | [Steps](./doc/component-steps.md) |
| 验证用户输入图形/字符验证码 | [VerifyCode](./doc/component-verify-code.md) |
| 自定义异步任务、进度反馈 | [Concurrent — DataFxTask](./doc/concurrent-api.md#datafxtask) |
| 流式分批推送数据到列表 | [Concurrent — PublishingTask](./doc/concurrent-api.md#publishingtask) |

### "我遇到了 …"

| 问题 | 文档 |
| ---- | ---- |
| 后台线程更新 UI 抛异常 | [Concurrent — 线程模型](./doc/concurrent-api.md#线程模型与最佳实践) |
| 订阅者执行慢导致界面卡顿 | [Event — 线程模型](./doc/event-api.md#线程模型与最佳实践) |
| 退订订阅后内存仍未释放 | [Event — 最佳实践](./doc/event-api.md#线程模型与最佳实践) |
| 任务链中某一步失败如何统一处理 | [Concurrent — onException](./doc/concurrent-api.md#异常与收尾) |
| 不同操作系统上路径不一致 | [Path API](./doc/path-api.md) |

---

## 示例代码索引

项目内置丰富的演示与测试代码，路径：`src/test/java/io/aurora/fx/`

| 演示 | 路径 |
| ---- | ---- |
| 启动入口 | `StartApp.java` |
| 控件展示 | `JavaFXDemoLauncher.java` |
| 用户注册向导（综合示例） | `components/UserRegistrationWizard.java` |
| 主题演示 | `theme/OSThemeDemo.java` / `theme/MacOSThemeDemo.java` / `theme/Windows11ThemeDemo.java` |
| Path 工具演示 | `common/path/PathsDemo.java` |
| 并发演示 | `concurrent/client` / `concurrent/process` / `concurrent/task` |
| 各组件演示 | `components/<组件名>/*Demo.java` |

---

## 构建与发布

### 本地构建

```bash
mvn clean package
```

生成的 jar 位于 `target/aurora-fx-0.0.1.jar`，同时输出 `-sources.jar` 与 `-javadoc.jar`。

### Maven 插件

| 插件 | 版本 | 用途 |
| ---- | ---- | ---- |
| `maven-resources-plugin` | 3.2.0 | UTF-8 资源处理 |
| `maven-source-plugin` | 2.2.1 | 生成 `-sources.jar` |
| `maven-javadoc-plugin` | 3.1.1 | 生成 `-javadoc.jar`（`-Xdoclint:none`） |

### 编译参数

| 参数 | 值 |
| ---- | -- |
| `maven.compiler.source` | `25` |
| `maven.compiler.target` | `25` |
| `project.build.sourceEncoding` | `UTF-8` |

---

## 贡献指南

欢迎提交 PR / Issue：

1. Fork 仓库 → 创建 feature 分支
2. 遵循现有代码风格（包名小驼峰、类名大驼峰、JavaDoc 中文）
3. 新增组件需提供：
   - 源码（`src/main/java/io/aurora/fx/components/<name>/`）
   - 资源（CSS 在 `src/main/resources/io/aurora/fx/components/<name>/`）
   - 测试 / 演示（`src/test/java/io/aurora/fx/components/<name>/`）
   - **API 文档**（建议命名 `doc/component-<name>.md`，结构参照本仓库其它组件文档）
4. 新模块需在本 `WIKI.md` 中添加索引

> 维护者：[lxwise](mailto:lstart980@gmail.com)

---

## 开源协议

本项目采用 [Apache License 2.0](./LICENSE) 协议开源。

---

> 💡 **提示**：本 Wiki 是项目所有 API 文档的总入口。各模块的详细 API 说明请前往 [`doc/`](./doc/) 目录对应文件。
> 📌 在 GitHub 上发布时，所有相对链接都将自动解析。
