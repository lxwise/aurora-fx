<p align="center">
  <a href="https://github.com/lxwise/aurora-fx/">
    <img src="./doc/aurora-fx.png" alt="Aurora-FX">
  </a>
</p>

<p align="center">
  <b>Aurora-FX</b> 是一款高性能 JavaFX 自定义 UI 组件库，以"曙光"为名，<br/>
  致力于为开发者提供优雅、高效的界面开发体验。
</p>

<p align="center">
   <a target="_blank" href="https://github.com/lxwise/aurora-fx">
      <img src="https://img.shields.io/hexpm/l/plug.svg" alt="License"/>
      <img src="https://img.shields.io/badge/build-maven-green" alt="Build"/>
      <img src="https://img.shields.io/badge/java-25-%23F27E3F" alt="Java"/>
      <img src="https://img.shields.io/badge/javafx-25-%23F27E3F" alt="JavaFX"/>
      <img src="https://img.shields.io/badge/atlantafx-2.1.0-blue" alt="atlantafx"/>
      <img src="https://img.shields.io/badge/ikonli-12.3.1-blueviolet" alt="ikonli"/>
   </a>
</p>

---

## 📑 目录

- [项目地址](#-项目地址)
- [核心特性](#-核心特性)
- [组件矩阵](#-组件矩阵)
- [安装和使用](#-安装和使用)
- [代码示例](#-代码示例)
- [完整文档](#-完整文档)
- [项目结构](#-项目结构)
- [Star](#-star)
- [开源协议](#-开源协议)
- [联系作者](#-联系作者)

---

## 📍 项目地址

| 平台 | 链接 |
| ---- | ---- |
| **GitHub** | <https://github.com/lxwise/aurora-fx> |
| **Gitee** | <https://gitee.com/lxwise/aurora-fx> |
| **Maven Central** | <https://repo1.maven.org/maven2/io/github/lxwise/aurora-fx/> |

---

## ✨ 核心特性

- 🎨 **现代化主题** — 内置 macOS 与 Windows 11 风格的亮/暗主题，支持运行时切换
- 🧩 **丰富组件** — 从基础控件到业务组件，开箱即用
- ⚡ **强大并发** — `ProcessChain` 链式调度、`ObservableExecutor` 可观察执行器，UI 线程切换零样板
- 📡 **轻量事件** — 单例 `EventBus`，支持继承分发、按 key 路由、同步/异步发布
- 🛠️ **跨平台路径** — 自动识别 Windows / macOS / Linux 标准目录
- 📖 **完善文档** — 每个模块均提供详细 API 文档，参见 [`doc/`](./doc/) 目录或 [WIKI.md](./WIKI.md)

---

## 🧩 组件矩阵

| 组件 | 说明 | API 文档 |
| ---- | ---- | -------- |
| **Avatar** | 头像组件，8 种形状 + 主题预设 | [📘](./doc/component-avatar.md) |
| **DynamicForm** | 配置驱动的动态表单（含校验、绑定模式） | [📘](./doc/component-dynamic-form.md) |
| **ImageViewer** | 图片查看器，支持缩放/平移/旋转 | — |
| **LineButton** | 行式按钮，EXTEND / RISE 两种动画 | [📘](./doc/component-line-button.md) |
| **Steps** | 步骤条，多步骤导航 | [📘](./doc/component-steps.md) |
| **Tour** | 漫游引导，12+1 种定位、链式步骤 | [📘](./doc/component-tour.md) |
| **TranslationButton** | 平移按钮，4 种平移方向 | [📘](./doc/component-translation-button.md) |
| **Upload** | 文件上传，FileUploader + 8 个独立 Node 子组件 | [📘](./doc/component-upload.md) |
| **VerifyCode** | 验证码组件，多类型 + 自定义生成器 | [📘](./doc/component-verify-code.md) |

---

## 📦 安装和使用

### Maven

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>aurora-fx</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation group: 'io.github.lxwise', name: 'aurora-fx', version: '0.0.1'
}
```

### 环境要求

| 项 | 版本 |
| -- | ---- |
| JDK | 25 |
| JavaFX | 25 |
| Maven | 3.9+ |

---

## 💻 代码示例

### 1. 任务链：调用接口并刷新 UI

```java
ProcessChain.create()
    .addRunnableInPlatformThread(() -> {
        // 第一步：在 JavaFX 主线程更新 UI
        button.setDisable(true);
    })
    .addSupplierInExecutor(() -> {
        // 第二步：后台线程执行耗时操作（例如调接口）
        return "后台任务结果";
    })
    .addConsumerInPlatformThread(result -> {
        // 第三步：拿到接口结果，回到 UI 线程刷新界面
        label.setText(result);
    })
    .onException(e -> {
        // 异常统一处理
        System.err.println("执行出错：" + e.getMessage());
    })
    .withFinal(() -> System.out.println("任务链结束"))
    .run();
```

### 2. 任务链：保存 / 更新 / 删除

```java
ProcessChain.create()
    .addRunnableInExecutor(() -> System.err.println("执行操作请求"))
    .addRunnableInPlatformThread(() -> {
        // 在 JavaFX 主线程更新 UI（关闭弹窗、刷新列表）
        dialog.close();
        query();
    })
    .onException(Throwable::printStackTrace)
    .run();
```

### 3. 事件总线：跨组件通信

```java
// 订阅
NodeNoticeEvent.subscribeByKey("user-table-refresh", evt -> {
    List<User> users = (List<User>) evt.getPayload();
    tableView.setItems(FXCollections.observableArrayList(users));
});

// 发布
new NodeNoticeEvent(this, userList, "user-table-refresh").publish();
```

### 4. 头像组件

```java
Avatar avatar = new Avatar();
avatar.setShape(AvatarShape.CIRCLE);
avatar.setImage("https://avatar.example.com/u/1.png");
```

更多示例代码请见 [`src/test/java/io/aurora/fx/`](./src/test/java/io/aurora/fx/) 目录。

---

## 📚 完整文档

> **🌟 推荐入口：[WIKI.md](./WIKI.md)** — 综合 API 文档索引，所有模块统一导航

所有详细 API 文档存放在 [`doc/`](./doc/) 目录：

| 模块 | 文档 |
| ---- | ---- |
| 🌟 **Wiki 总入口** | [WIKI.md](./WIKI.md) |
| ⚡ 并发 | [doc/concurrent-api.md](./doc/concurrent-api.md) |
| 📡 事件 | [doc/event-api.md](./doc/event-api.md) |
| 🛠️ 跨平台路径 | [doc/path-api.md](./doc/path-api.md) |
| 🎨 主题 | [doc/theme-api.md](./doc/theme-api.md) |
| 🧩 Avatar | [doc/component-avatar.md](./doc/component-avatar.md) |
| 🧩 DynamicForm | [doc/component-dynamic-form.md](./doc/component-dynamic-form.md) |
| 🧩 LineButton | [doc/component-line-button.md](./doc/component-line-button.md) |
| 🧩 Steps | [doc/component-steps.md](./doc/component-steps.md) |
| 🧩 Tour | [doc/component-tour.md](./doc/component-tour.md) |
| 🧩 TranslationButton | [doc/component-translation-button.md](./doc/component-translation-button.md) |
| 🧩 Upload | [doc/component-upload.md](./doc/component-upload.md) |
| 🧩 VerifyCode | [doc/component-verify-code.md](./doc/component-verify-code.md) |

---

## 📂 项目结构

```
aurora-fx/
├── doc/                          # 📚 所有 API 文档（详见 WIKI.md）
├── src/
│   ├── main/java/io/aurora/fx/
│   │   ├── common/               # 公共工具（path / utils / enums）
│   │   ├── components/           # UI 组件（avatar / dynamicForm / ...）
│   │   ├── concurrent/           # 并发框架（ProcessChain 等）
│   │   ├── event/                # 事件总线
│   │   └── theme/                # 主题系统
│   ├── main/resources/
│   └── test/                     # 演示与单元测试
├── README.md
├── WIKI.md                       # 🌟 综合 Wiki 索引
├── pom.xml
└── LICENSE
```

---

## ⭐ Star

如果这个项目对你有帮助，欢迎给个 **Star** 🌟！
你的 Star 是对作者最大的鼓励，也能让更多志同道合的小伙伴看到本项目。

**同时也欢迎提交 PR，一起改进项目！**

---

## 📜 开源协议

本项目采用 [Apache License 2.0](./LICENSE) 协议。

---

## 📮 联系作者

- 作者：**lxwise**
- 邮箱：<lstart980@gmail.com>

> 最后，希望本项目能够为你带来帮助与收获。如果你有任何建议或意见，欢迎随时联系我。让我们一起分享知识，共同成长！
