# Concurrent 并发模块 API 文档

> **包路径**：`io.aurora.fx.concurrent`
> **设计灵感**：DataFX 框架
> **核心目标**：在 JavaFX 应用中以"链式 + 声明式"风格优雅地编排异步任务，自动处理 UI 线程切换、异常传递、进度反馈。

## 目录

- [模块总览](#模块总览)
- [架构图](#架构图)
- [快速上手](#快速上手)
- [核心 API](#核心-api)
  - [ProcessChain — 任务链](#processchain--任务链)
  - [ObservableExecutor — 可观察执行器](#observableexecutor--可观察执行器)
  - [ExceptionHandler — 异常处理器](#exceptionhandler--异常处理器)
  - [ThreadPoolExecutorFactory — 默认线程池工厂](#threadpoolexecutorfactory--默认线程池工厂)
  - [ThreadPoolConfiguration — 线程池配置](#threadpoolconfiguration--线程池配置)
- [任务抽象](#任务抽象)
  - [DataFxTask](#datafxtask)
  - [DataFxService](#datafxservice)
  - [PublishingTask](#publishingtask)
  - [CallableBasedDataFxTask / RunnableBasedDataFxTask](#callablebaseddatafxtask--runnablebaseddatafxtask)
- [函数式接口](#函数式接口)
  - [DataFxCallable](#datafxcallable)
  - [DataFxRunnable](#datafxrunnable)
  - [Publisher](#publisher)
- [状态反馈机制](#状态反馈机制)
  - [TaskStateHandler](#taskstatehandler)
  - [TaskWithStateHandler](#taskwithstatehandler)
  - [TaskStateHandlerManager](#taskstatehandlermanager)
- [辅助类](#辅助类)
  - [ProcessDescription](#processdescription)
  - [Assert](#assert)
- [线程模型与最佳实践](#线程模型与最佳实践)

---

## 模块总览

Aurora-FX 并发模块在 JavaFX 原生 `Task` / `Service` / `Worker` 之上提供更高级的封装：

| 类/接口 | 类型 | 作用 |
| ------- | ---- | ---- |
| `ProcessChain<T>` | 类 | **核心** 链式任务编排器，串联多个步骤并自动调度线程 |
| `ObservableExecutor` | 类 | 实现 `Executor` 的可观察执行器，自动统计/清理任务、注册异常监听 |
| `ExceptionHandler` | 类 | 全局/局部异常捕获器，可观察 `Throwable` 属性 |
| `ThreadPoolExecutorFactory` | 类 | 默认线程池单例工厂（守护线程 + 自定义 ThreadFactory） |
| `ThreadPoolConfiguration` | 类 | 线程池参数配置单例 |
| `DataFxTask<V>` | 抽象类 | `Task` 的扩展，支持 cancelable 属性与 then 回调 |
| `DataFxService<V>` | 抽象类 | `Service` 的扩展，桥接 `DataFxTask` |
| `PublishingTask<T>` | 抽象类 | 边执行边推送中间结果的任务 |
| `CallableBasedDataFxTask` | 类 | `DataFxCallable` 适配器 |
| `RunnableBasedDataFxTask` | 类 | `DataFxRunnable` 适配器 |
| `DataFxCallable<V>` | 接口 | 带状态反馈的 `Callable<V>` |
| `DataFxRunnable` | 接口 | 带状态反馈的 `Runnable` |
| `Publisher<T>` | 接口 | 中间结果发布器 |
| `TaskStateHandler` | 接口 | 任务状态更新接口（标题/消息/进度/可取消） |
| `TaskWithStateHandler` | 接口 | 任务侧状态注入接口（带默认方法） |
| `TaskStateHandlerManager` | 类 | 弱引用映射管理任务⇄状态处理器 |
| `ProcessDescription<V,T>` | 类 | 单步任务描述（函数 + 线程类型） |
| `Assert` | 类 | 非空断言工具类 |

---

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                       业务调用方（Controller）                   │
└─────────────────────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│                  ProcessChain（任务链编排）                      │
│   addRunnableInPlatformThread / addSupplierInExecutor / ...     │
└─────────────────────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│              ObservableExecutor（可观察执行器）                  │
│  ① 统计当前服务  ② 自动清理完成任务  ③ 注册异常监听              │
└─────────────────────────────────────────────────────────────────┘
                │                              │
                ▼                              ▼
┌──────────────────────────────┐   ┌──────────────────────────────┐
│ ThreadPoolExecutorFactory    │   │ ExceptionHandler             │
│  默认线程池（守护 + 异常转发）│   │  exceptionProperty 可观察    │
└──────────────────────────────┘   └──────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│   DataFxTask / DataFxService / PublishingTask （任务执行体）     │
└─────────────────────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  TaskStateHandler ⇄ TaskWithStateHandler （状态反馈机制）         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 快速上手

```java
ProcessChain.create()
    .addRunnableInPlatformThread(() -> button.setDisable(true)) // 1. UI 线程：禁用按钮
    .addSupplierInExecutor(() -> userService.loadAll())          // 2. 后台线程：网络请求
    .addConsumerInPlatformThread(list -> tableView.setItems(     // 3. UI 线程：刷新表格
            FXCollections.observableArrayList(list)))
    .onException(e -> showError(e))                              // 异常统一处理
    .withFinal(() -> button.setDisable(false))                   // 收尾：恢复按钮
    .run();
```

---

## 核心 API

### ProcessChain — 任务链

> 路径：`io.aurora.fx.concurrent.ProcessChain`
> 类型：`public class ProcessChain<T>`
> 泛型 `T`：链上**最后一个**步骤的返回类型。

#### 构造与创建

| 方法 | 说明 |
| ---- | ---- |
| `ProcessChain()` | 使用默认 `ObservableExecutor` |
| `ProcessChain(Executor executorService)` | 使用自定义执行器 |
| `static ProcessChain<Void> create()` | 工厂方法（默认） |
| `static ProcessChain<Void> create(Executor executor)` | 工厂方法（自定义） |

#### 步骤添加方法（线程感知）

所有 `add*InPlatformThread` 都在 **JavaFX UI 线程** 执行；
所有 `add*InExecutor` 都在 **后台线程**（Executor）执行。

| 方法 | 输入 | 输出 | 用途 |
| ---- | ---- | ---- | ---- |
| `addRunnableInPlatformThread(Runnable)` | — | `Void` | UI 线程跑无返回值任务（如更新控件） |
| `addRunnableInExecutor(Runnable)` | — | `Void` | 后台线程跑无返回值任务（如调接口） |
| `addFunctionInPlatformThread(Function<T,V>)` | `T` | `V` | UI 线程：上一步结果转换 |
| `addFunctionInExecutor(Function<T,V>)` | `T` | `V` | 后台线程：上一步结果转换 |
| `addConsumerInPlatformThread(Consumer<T>)` | `T` | `Void` | UI 线程：消费上一步结果 |
| `addConsumerInExecutor(Consumer<T>)` | `T` | `Void` | 后台线程：消费上一步结果 |
| `addSupplierInPlatformThread(Supplier<V>)` | — | `V` | UI 线程：生产新数据 |
| `addSupplierInExecutor(Supplier<V>)` | — | `V` | 后台线程：生产新数据（推荐网络/IO） |
| `addPublishingTask(Supplier<List<V>>, Consumer<Publisher<V>>)` | — | `List<V>` | 后台流式数据推送（自动 `runAndWait`） |
| `addPublishingTask(List<V>, Consumer<Publisher<V>>)` | — | `List<V>` | 同上，传入已有列表 |
| `addPublishingTask(Consumer<Publisher<V>>)` | — | `List<V>` | 同上，使用 `FXCollections.observableArrayList()` |
| `addProcessDescription(ProcessDescription<T,V>)` | `T` | `V` | 自定义低层步骤 |
| `waitFor(Worker<V>)` | — | `V` | 阻塞等待另一个 `Worker` 完成 |

#### 异常与收尾

| 方法 | 说明 |
| ---- | ---- |
| `onException(Consumer<Throwable>)` | 注册异常处理回调 |
| `onException(ExceptionHandler)` | 直接传入自定义 `ExceptionHandler` |
| `withFinal(Runnable)` | 任务链完成（成功 or 失败）后必执行 |

#### 执行方法

| 方法 | 返回值 | 说明 |
| ---- | ------ | ---- |
| `run()` | `Task<T>` | 执行 1 次（等价 `repeat(1)`） |
| `repeat(int count)` | `Task<T>` | 重复执行 `count` 次 |
| `repeat(int count, Duration pause)` | `Task<T>` | 重复 + 每次间隔 |
| `repeatInfinite()` | `Task<T>` | 无限循环 |
| `repeatInfinite(Duration pause)` | `Task<T>` | 无限循环 + 间隔 |

#### 完整示例

```java
ProcessChain.create()
    .addRunnableInPlatformThread(() -> {
        progressBar.setProgress(-1);
        button.setDisable(true);
    })
    .addSupplierInExecutor(() -> "后台任务结果")  // 模拟接口请求
    .addFunctionInPlatformThread(result -> {
        label.setText(result);
        return result.length();
    })
    .addConsumerInPlatformThread(len -> System.out.println("结果长度: " + len))
    .onException(e -> {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.showAndWait();
    })
    .withFinal(() -> {
        progressBar.setProgress(0);
        button.setDisable(false);
    })
    .run();
```

#### 流式发布示例（addPublishingTask）

```java
ProcessChain.create()
    .addPublishingTask(publisher -> {
        for (int i = 1; i <= 100; i++) {
            publisher.publish("行 " + i);
            Thread.sleep(50);
        }
    })
    .addConsumerInPlatformThread(allRows -> listView.setItems(
        FXCollections.observableArrayList(allRows)))
    .run();
```

---

### ObservableExecutor — 可观察执行器

> 路径：`io.aurora.fx.concurrent.ObservableExecutor`
> 实现：`java.util.concurrent.Executor`

#### 特性

- 自动维护**当前运行中**的 `Service<?>` 列表（任务完成自动移除）
- 自动为每个任务注册 `ExceptionHandler.observeWorker`
- 提供 `submit(Service)` / `submit(Task)` / `submit(Callable)` / `submit(Runnable)` 多种重载
- 单例模式：`getDefaultInstance()`

#### 构造

| 构造方法 | 说明 |
| -------- | ---- |
| `ObservableExecutor()` | 默认线程池 + 默认异常处理器 |
| `ObservableExecutor(Executor)` | 指定执行器 + 默认异常处理器 |
| `ObservableExecutor(ExceptionHandler)` | 默认线程池 + 指定异常处理器 |
| `ObservableExecutor(Executor, ExceptionHandler)` | 全自定义 |

#### 关键方法

| 方法 | 返回值 | 说明 |
| ---- | ------ | ---- |
| `<T> Worker<T> submit(Service<T>)` | `Worker<T>` | 启动 Service |
| `<T> Worker<T> submit(Task<T>)` | `Worker<T>` | 启动 Task |
| `<T> Worker<T> submit(Callable<T>)` | `Worker<T>` | 启动 Callable（含 `DataFxCallable`） |
| `Worker<Void> submit(Runnable)` | `Worker<Void>` | 启动 Runnable（含 `DataFxRunnable`） |
| `void execute(Runnable)` | — | 实现 `Executor` 接口 |
| `ReadOnlyListProperty<Service<?>> currentServicesProperty()` | 列表属性 | 可绑定到 UI 显示当前运行任务 |
| `ProcessChain<Void> createProcessChain()` | `ProcessChain<Void>` | 工厂：基于此执行器的任务链 |
| `static ObservableExecutor getDefaultInstance()` | 单例 | 全局默认实例 |

#### 示例：监听运行中任务数

```java
ObservableExecutor exec = ObservableExecutor.getDefaultInstance();
exec.currentServicesProperty().sizeProperty()
    .addListener((obs, o, n) -> statusBar.setText("运行中: " + n + " 个任务"));
```

---

### ExceptionHandler — 异常处理器

> 路径：`io.aurora.fx.concurrent.ExceptionHandler`

#### 核心字段

- `ObjectProperty<Throwable> exception` — 当前异常的可观察属性

#### 静态 API

| 方法 | 说明 |
| ---- | ---- |
| `static synchronized ExceptionHandler getDefaultInstance()` | 全局默认实例（懒加载） |
| `static ChangeListener<Throwable> getLoggerListener()` | 默认日志监听器 |
| `static void setExceptionLogging(boolean log)` | 启停异常日志 |
| `static boolean isLogException()` | 是否启用日志 |

#### 实例 API

| 方法 | 说明 |
| ---- | ---- |
| `Throwable getException()` | 取当前异常 |
| `void setException(Throwable)` | 设置异常（自动 FX 线程切换） |
| `<T> void observeWorker(Worker<T>)` | 监听 Worker 异常 |
| `ObjectProperty<Throwable> exceptionProperty()` | 异常属性（绑定 UI） |

#### 行为细节

`setException` 会判断当前是否在 JavaFX 主线程：
- 若是 → 直接 `set`
- 否 → `ConcurrentUtils.runAndWait` 切回主线程（捕获 `InterruptedException` / `ExecutionException`）

#### 示例

```java
ExceptionHandler handler = ExceptionHandler.getDefaultInstance();
handler.exceptionProperty().addListener((obs, old, ex) -> {
    if (ex != null) {
        new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
    }
});
ExceptionHandler.setExceptionLogging(true);
```

---

### ThreadPoolExecutorFactory — 默认线程池工厂

> 路径：`io.aurora.fx.concurrent.ThreadPoolExecutorFactory`

#### 主要方法

| 方法 | 说明 |
| ---- | ---- |
| `static synchronized ThreadPoolExecutor getThreadPoolExecutor()` | 单例懒加载 |

#### 线程池特性

| 项 | 取值 / 行为 |
| -- | ----------- |
| 核心线程数 | `ThreadPoolConfiguration.getDefaultThreadPoolStartSize()`（默认 2） |
| 最大线程数 | `ThreadPoolConfiguration.getDefaultThreadMaxSize()`（默认 32） |
| 超时时间 | `getDefaultThreadTimeout()` 毫秒（默认 3000） |
| 阻塞队列 | 自定义 `LinkedBlockingQueue`：未达到最大线程数时拒绝入队，强制扩容 |
| 拒绝策略 | `AbortPolicy` |
| 线程组 | `getThreadGroupName()` 命名（默认 "DataChainFX thread pool"） |
| 是否守护线程 | `true`（程序退出自动结束） |
| 优先级 | `Thread.MIN_PRIORITY` |
| 核心线程超时 | `allowCoreThreadTimeOut(true)` |
| 异常处理 | 通过 `Thread.UncaughtExceptionHandler` 转发给 `ExceptionHandler` |

---

### ThreadPoolConfiguration — 线程池配置

> 路径：`io.aurora.fx.concurrent.ThreadPoolConfiguration`
> 单例：`getInstance()`

| 方法 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `isExceptionLoggingActive()` | `true` | 是否记录异常 |
| `getDefaultThreadMaxSize()` | `32` | 最大线程数 |
| `getDefaultThreadPoolStartSize()` | `2` | 初始线程数 |
| `getThreadGroupName()` | `"DataChainFX thread pool"` | 线程组名 |
| `getDefaultThreadTimeout()` | `3000` | 空闲超时 (ms) |

> **扩展方式**：本类目前为 `final` 单例，如需自定义参数可继承 `ObservableExecutor` 并传入自定义 `Executor`。

---

## 任务抽象

### DataFxTask

> 路径：`io.aurora.fx.concurrent.DataFxTask`
> 类型：`public abstract class DataFxTask<V> extends javafx.concurrent.Task<V> implements TaskStateHandler`

#### 扩展能力

- `BooleanProperty cancelable` — 是否允许取消（默认 `true`）
- `then(Consumer<V>)` — 任务成功后回调（自动通过 `ConcurrentUtils.then`）
- 重写 `cancel(boolean)`：若 `cancelable=false` 抛 `RuntimeException("Task is not cancelable!")`
- 实现 `TaskStateHandler` 的全部 `update*` 方法

#### 用法

```java
DataFxTask<List<User>> task = new DataFxTask<>() {
    @Override
    protected List<User> call() throws Exception {
        updateTaskTitle("加载用户");
        for (int i = 0; i < 10; i++) {
            updateTaskProgress(i, 10);
            updateTaskMessage("第 " + i + " 项");
            Thread.sleep(200);
        }
        return userRepository.findAll();
    }
};
task.then(users -> tableView.setItems(FXCollections.observableArrayList(users)));
ObservableExecutor.getDefaultInstance().submit(task);
```

---

### DataFxService

> 路径：`io.aurora.fx.concurrent.DataFxService`
> 类型：`public abstract class DataFxService<V> extends javafx.concurrent.Service<V>`

#### 关键行为

- 重写 `executeTask(Task<V>)`：若 `task instanceof DataFxTask`，自动绑定 `cancelable` 属性
- 若未指定 `executor`，使用 `ObservableExecutor.getDefaultInstance()`
- 暴露 `ReadOnlyBooleanProperty cancelableProperty()` 与 `boolean isCancelable()`

#### 用法

```java
public class LoadUserService extends DataFxService<List<User>> {
    @Override
    protected Task<List<User>> createTask() {
        return new DataFxTask<>() {
            @Override
            protected List<User> call() {
                return userRepository.findAll();
            }
        };
    }
}
```

---

### PublishingTask

> 路径：`io.aurora.fx.concurrent.PublishingTask`
> 类型：`public abstract class PublishingTask<T> extends Task<ObservableList<T>> implements Publisher<T>`

#### 核心 API

| 方法 | 说明 |
| ---- | ---- |
| `ObservableList<T> getPublishedValues()` | 已发布数据列表 |
| `protected abstract void callTask()` | 用户实现的任务体 |
| `void publish(T... values)` | 发布数据（自动 `Platform.runLater` 入队） |

#### 用法

```java
PublishingTask<String> task = new PublishingTask<>() {
    @Override
    protected void callTask() throws Exception {
        for (int i = 1; i <= 5; i++) {
            publish("第 " + i + " 项");
            Thread.sleep(500);
        }
    }
};
listView.setItems(task.getPublishedValues());  // 实时刷新
new Thread(task).start();
```

---

### CallableBasedDataFxTask / RunnableBasedDataFxTask

> 路径：
> - `io.aurora.fx.concurrent.CallableBasedDataFxTask<V>`
> - `io.aurora.fx.concurrent.RunnableBasedDataFxTask`

将普通 `Callable` / `Runnable`（**或** `DataFxCallable` / `DataFxRunnable`）封装为 `DataFxTask`。
对于 `DataFxCallable` / `DataFxRunnable`，构造时会自动调用 `injectStateHandler(this)` 完成状态注入。

```java
DataFxRunnable taskBody = () -> {
    updateTaskTitle("处理中…");
    for (int i = 1; i <= 100; i++) {
        updateTaskProgress(i, 100);
    }
};
DataFxTask<Void> task = new RunnableBasedDataFxTask(taskBody);
ObservableExecutor.getDefaultInstance().submit(task);
```

---

## 函数式接口

### DataFxCallable

```java
@FunctionalInterface
public interface DataFxCallable<V> extends Callable<V>, TaskWithStateHandler { }
```

继承自 `Callable<V>` + `TaskWithStateHandler`，在 `call()` 内可调用 `updateTaskTitle/Message/Progress`。

### DataFxRunnable

```java
@FunctionalInterface
public interface DataFxRunnable extends Runnable, TaskWithStateHandler { }
```

无返回值版本。

### Publisher

```java
@FunctionalInterface
public interface Publisher<T> {
    void publish(T... values);
}
```

通常由 `PublishingTask` 实现，或在 `ProcessChain.addPublishingTask` 内部创建。

---

## 状态反馈机制

### TaskStateHandler

```java
public interface TaskStateHandler {
    void updateTaskTitle(String title);
    void updateTaskMessage(String message);
    void updateTaskProgress(double workDone, double max);
    void updateTaskProgress(long workDone, long max);
    void setCancelable(boolean cancelable);
}
```

### TaskWithStateHandler

为任务侧（`DataFxCallable` / `DataFxRunnable`）提供 **default 方法**：
- `injectStateHandler(TaskStateHandler)` — 由框架自动调用
- `getStateHandler()` — 获取已注入的处理器
- `updateTaskTitle / Message / Progress / setCancelable` — 转发到状态处理器

### TaskStateHandlerManager

弱引用映射表，`add(task, handler)` / `get(task)`，避免任务 GC 后状态处理器残留。

---

## 辅助类

### ProcessDescription

封装 `ProcessChain` 中单步：
- `Function<V,T> function` — 执行逻辑
- `ThreadType threadType` — `PLATFORM` 或 `EXECUTOR`

### Assert

```java
public class Assert {
    public static <T> T requireNonNull(T param, String name) {
        return Objects.requireNonNull(param,
                "Value " + name + " should not be null!");
    }
}
```

非空断言的内部统一封装。

---

## 线程模型与最佳实践

```
┌────────────────────────┐         ┌────────────────────────┐
│  JavaFX Application    │         │   后台线程池           │
│  Thread (UI 线程)      │ ←─────→ │ DataChainFX thread...  │
└────────────────────────┘         └────────────────────────┘
   - 仅在此更新 UI                      - 网络/IO/计算
   - addRunnableInPlatformThread       - addRunnableInExecutor
   - addConsumerInPlatformThread       - addSupplierInExecutor
```

#### 推荐模式

1. **耗时操作放后台**：所有 IO / 网络 / 大数据处理 → `addSupplierInExecutor` 或 `addFunctionInExecutor`
2. **UI 更新强制主线程**：任何 `setText` / `setItems` / `Stage` 操作 → `addRunnableInPlatformThread` 或 `addConsumerInPlatformThread`
3. **统一异常入口**：`onException` 用于显示错误对话框；不要在每步 `try-catch`
4. **避免阻塞主线程**：禁止在 `addRunnableInPlatformThread` 中执行 `Thread.sleep` 或 IO
5. **流式数据**：分批返回的列表 → `addPublishingTask` 而非一次性 `addSupplier`
6. **可取消任务**：复杂任务实现 `DataFxTask` 并重写 `cancel`，UI 上绑定 `cancelableProperty()`

#### 反模式（应避免）

- ❌ 在 `addSupplierInExecutor` 内访问/修改 JavaFX 控件
- ❌ 嵌套调用 `ProcessChain.run()`（链应平铺）
- ❌ 在 `withFinal` 内执行耗时操作（应仅做轻量收尾）
- ❌ 共享 `ProcessChain` 实例并发执行（每次链应 fresh `create()`）
