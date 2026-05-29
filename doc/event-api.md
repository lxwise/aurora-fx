# Event 事件模块 API 文档

> **包路径**：`io.aurora.fx.event`
> **设计模式**：发布-订阅 / 单例事件总线
> **核心目标**：在 JavaFX 应用内的不同组件、Controller、模块之间提供解耦的事件通信机制，支持继承分发、按 key 路由、同步/异步发布。

## 目录

- [模块总览](#模块总览)
- [架构图](#架构图)
- [快速上手](#快速上手)
- [核心 API](#核心-api)
  - [Event — 事件基类](#event--事件基类)
  - [EventBus — 事件总线](#eventbus--事件总线)
- [内置事件类型](#内置事件类型)
  - [LoadingEvent — 加载事件](#loadingevent--加载事件)
  - [NoticeEvent — 通知事件](#noticeevent--通知事件)
  - [NoticeCloseEvent — 通知关闭事件](#noticecloseevent--通知关闭事件)
  - [NodeNoticeEvent — 组件通信事件](#nodenoticeevent--组件通信事件)
  - [ThemeEvent — 主题事件](#themeevent--主题事件)
  - [ThemeChangeEvent — 主题变更事件](#themechangeevent--主题变更事件)
  - [TooltipEvent — 提示事件](#tooltipevent--提示事件)
  - [ExitPublishEvent — 退出发布事件](#exitpublishevent--退出发布事件)
- [继承分发规则](#继承分发规则)
- [自定义事件](#自定义事件)
- [线程模型与最佳实践](#线程模型与最佳实践)

---

## 模块总览

| 类/接口 | 类型 | 作用 |
| ------- | ---- | ---- |
| `Event` | 类 | **基类**，提供 UUID 唯一标识、`publish()` / `publishAsync()` 自发布 |
| `EventBus` | 单例类 | 全局事件总线，`subscribe` / `unsubscribe` / `publish` |
| `LoadingEvent` | 类 | 加载状态事件（含静态 `LOADING` / `STOP`） |
| `NoticeEvent` | 类 | atlantafx Notification 通知事件 |
| `NoticeCloseEvent` | 类 | 关闭某个 Notification |
| `NodeNoticeEvent` | 类 | 组件间消息通信，支持 `targetKey` 路由 |
| `ThemeEvent` | final 类 | 主题相关事件（含 `EventType` 枚举） |
| `ThemeChangeEvent` | 类 | 主题切换通知（标记类） |
| `TooltipEvent` | 类 | 一般性提示信息事件 |
| `ExitPublishEvent` | 类 | 退出/卸载通知（标记类） |

---

## 架构图

```
   组件A（Controller）                组件B（Skin）              组件C（任意类）
        │                                  │                            │
        │ event.publish()                  │ EventBus.subscribe(...)    │ event.publishAsync()
        │                                  │                            │
        ▼                                  ▼                            ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           EventBus（单例）                                   │
│   subscribers: Map<Class<?>, Set<Consumer>>  (ConcurrentHashMap +            │
│                                                CopyOnWriteArraySet)         │
│                                                                              │
│   publish 时按 isAssignableFrom 进行继承分发                                 │
│   订阅父类 → 收子类事件                                                      │
└─────────────────────────────────────────────────────────────────────────────┘
        │                                  │                            │
        ▼                                  ▼                            ▼
   订阅者A（Lambda）             订阅者B（Lambda）              订阅者C（Lambda）
```

---

## 快速上手

```java
// 1. 订阅
EventBus.getInstance().subscribe(NoticeEvent.class, evt -> {
    System.out.println("收到通知: " + evt.notification().getMessage());
});

// 2. 发布（同步）
new NoticeEvent(
    new Notification("保存成功", new FontIcon("ant-check"))
).duration(Duration.seconds(3)).publish();

// 3. 异步发布
new ThemeChangeEvent().publishAsync();

// 4. 退订
Consumer<NoticeEvent> handler = evt -> ...;
EventBus.getInstance().subscribe(NoticeEvent.class, handler);
EventBus.getInstance().unsubscribe(NoticeEvent.class, handler);
```

---

## 核心 API

### Event — 事件基类

> 路径：`io.aurora.fx.event.Event`
> 类型：`public class Event`

#### 字段

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| `uuid` | `final String` | 32 位 UUID（去除 `-`），用于 `equals/hashCode` |

#### 方法

| 方法 | 说明 |
| ---- | ---- |
| `boolean equals(Object)` | 基于 UUID 判等 |
| `int hashCode()` | UUID hashCode |
| `static <E extends Event> void publish(E event)` | 静态发布 |
| `void publish()` | **同步**发布自身（在调用方线程执行所有订阅者） |
| `void publishAsync()` | **异步**发布自身（通过 `ObservableExecutor` 后台执行） |

#### 用法

```java
public class OrderCreatedEvent extends Event {
    private final long orderId;
    public OrderCreatedEvent(long id) { this.orderId = id; }
    public long getOrderId() { return orderId; }
}

// 发布
new OrderCreatedEvent(1001L).publish();
```

---

### EventBus — 事件总线

> 路径：`io.aurora.fx.event.EventBus`
> 类型：`public class EventBus`（**单例**）

#### 私有内部状态

```java
private final Map<Class<?>, Set<Consumer>> subscribers
        = new ConcurrentHashMap<>();
```

- 线程安全：`ConcurrentHashMap` + `CopyOnWriteArraySet`

#### 公共 API

| 方法 | 说明 |
| ---- | ---- |
| `static EventBus getInstance()` | 获取单例 |
| `<E extends Event> void subscribe(Class<? extends E> type, Consumer<E> sub)` | 订阅指定类型事件 |
| `<E extends Event> void unsubscribe(Class<? extends E> type, Consumer<E> sub)` | 退订指定类型事件 |
| `<E extends Event> void publish(E event)` | 发布事件（继承分发） |

#### 异常处理

订阅者抛出异常时，**不会中断**其他订阅者的执行；异常通过 `Platform.runLater` 重抛到 JavaFX 线程，方便统一捕获。

#### 示例

```java
EventBus bus = EventBus.getInstance();

// 订阅父类 → 可收到所有子类事件
bus.subscribe(Event.class, evt -> {
    System.out.println("收到任意事件: " + evt.getClass().getSimpleName());
});

// 订阅具体类
Consumer<OrderCreatedEvent> handler = evt ->
    System.out.println("订单 #" + evt.getOrderId() + " 创建");
bus.subscribe(OrderCreatedEvent.class, handler);

// 退订
bus.unsubscribe(OrderCreatedEvent.class, handler);
```

---

## 内置事件类型

### LoadingEvent — 加载事件

> 路径：`io.aurora.fx.event.LoadingEvent`

#### 静态常量

| 常量 | 说明 |
| ---- | ---- |
| `static LoadingEvent LOADING` | 表示开始加载（`loading=true`） |
| `static LoadingEvent STOP` | 表示停止加载（`loading=false`） |

#### 构造

| 构造方法 | 说明 |
| -------- | ---- |
| `LoadingEvent(Boolean loading)` | 仅状态 |
| `LoadingEvent(Boolean loading, Task<?> work)` | 状态 + 关联任务 |

#### 实例方法

| 方法 | 说明 |
| ---- | ---- |
| `boolean loading()` | 是否正在加载 |
| `Task<?> getWork()` | 关联任务 |
| `void setWork(Task<?> work)` | 设置任务（**注意 work 是静态字段**） |

#### 示例

```java
EventBus.getInstance().subscribe(LoadingEvent.class, evt -> {
    spinner.setVisible(evt.loading());
});

// 发布
LoadingEvent.LOADING.publish();
// 执行任务 ...
LoadingEvent.STOP.publish();
```

---

### NoticeEvent — 通知事件

> 路径：`io.aurora.fx.event.NoticeEvent`
> 依赖：`atlantafx.base.controls.Notification`

| 方法 | 说明 |
| ---- | ---- |
| `NoticeEvent(Notification)` | 构造 |
| `NoticeEvent duration(Duration)` | 链式设置展示时长（不允许 `INDEFINITE` / `UNKNOWN`） |
| `Duration duration()` | 获取展示时长（默认 3s） |
| `Notification notification()` | 获取通知对象 |

#### 示例

```java
new NoticeEvent(new Notification("操作成功"))
    .duration(Duration.seconds(2))
    .publish();
```

---

### NoticeCloseEvent — 通知关闭事件

> 路径：`io.aurora.fx.event.NoticeCloseEvent`

| 方法 | 说明 |
| ---- | ---- |
| `NoticeCloseEvent(Notification)` | 构造 |
| `Notification notification()` | 取目标通知 |

```java
new NoticeCloseEvent(myNotification).publish();
```

---

### NodeNoticeEvent — 组件通信事件

> 路径：`io.aurora.fx.event.NodeNoticeEvent`

#### 字段

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| `source` | `Object` | 事件来源（可为 `null`） |
| `payload` | `Object` | 携带数据 |
| `targetKey` | `String` | 路由 key |

#### 构造

| 构造 | 说明 |
| ---- | ---- |
| `NodeNoticeEvent()` | 全空 |
| `NodeNoticeEvent(Object payload, String targetKey)` | 仅 payload + key |
| `NodeNoticeEvent(Object source, Object payload, String targetKey)` | 完整 |

#### 实例方法

| 方法 | 说明 |
| ---- | ---- |
| `Object getSource()` | 来源 |
| `Object getPayload()` | 数据 |
| `String getTargetKey()` | 路由 key |
| `equals/hashCode` | 基于 `super` + 三字段 |

#### 静态便捷订阅

| 方法 | 说明 |
| ---- | ---- |
| `static void subscribeByKey(String key, Consumer<NodeNoticeEvent>)` | 仅响应 `targetKey` 匹配的事件 |
| `static void subscribeByKeys(Map<String, Consumer<NodeNoticeEvent>>)` | 多 key 路由表 |

#### 示例

```java
// 订阅
NodeNoticeEvent.subscribeByKey("user-table-refresh", evt -> {
    List<User> users = (List<User>) evt.getPayload();
    tableView.setItems(FXCollections.observableArrayList(users));
});

// 多 key 路由
NodeNoticeEvent.subscribeByKeys(Map.of(
    "open-dialog", evt -> dialog.show(),
    "close-dialog", evt -> dialog.close()
));

// 发布
new NodeNoticeEvent(this, userList, "user-table-refresh").publish();
```

---

### ThemeEvent — 主题事件

> 路径：`io.aurora.fx.event.ThemeEvent`
> 类型：`public final class ThemeEvent extends Event`

#### 内部枚举

```java
public enum EventType {
    THEME_CHANGE,   // 主题切换（基础字体大小或颜色变化）
    FONT_CHANGE,    // 字体变化
    COLOR_CHANGE,   // 颜色变化
    THEME_ADD,      // 新主题加入
    THEME_REMOVE    // 主题移除
}
```

| 方法 | 说明 |
| ---- | ---- |
| `ThemeEvent(EventType)` | 构造 |
| `EventType getEventType()` | 取类型 |
| `String toString()` | `ThemeEvent{eventType=XX} <super>` |

```java
new ThemeEvent(ThemeEvent.EventType.THEME_CHANGE).publish();
```

---

### ThemeChangeEvent — 主题变更事件

> 路径：`io.aurora.fx.event.ThemeChangeEvent`

无字段、无方法，仅作为**标记事件**：

```java
public class ThemeChangeEvent extends Event {}
```

```java
EventBus.getInstance().subscribe(ThemeChangeEvent.class,
    evt -> applyNewTheme());
new ThemeChangeEvent().publish();
```

---

### TooltipEvent — 提示事件

> 路径：`io.aurora.fx.event.TooltipEvent`

#### 内部枚举

```java
public enum TooltipType { info }
```

#### 主要 API

| 方法 | 说明 |
| ---- | ---- |
| `TooltipEvent(TooltipType, String)` | 完整构造 |
| `static TooltipEvent info(String message)` | 工厂：信息提示 |
| `TooltipType type()` | 类型 |
| `String tooltip()` | 文本 |

```java
TooltipEvent.info("数据已保存").publish();
```

---

### ExitPublishEvent — 退出发布事件

> 路径：`io.aurora.fx.event.ExitPublishEvent`

标记类，用于通知"退出/卸载"流程：

```java
public class ExitPublishEvent extends Event {}
```

```java
new ExitPublishEvent().publish();
```

---

## 继承分发规则

`EventBus.publish` 中的关键代码：

```java
subscribers.keySet().stream()
    .filter(type -> type.isAssignableFrom(eventType))
    .flatMap(type -> subscribers.get(type).stream())
    .forEach(subscriber -> publish(event, subscriber));
```

含义：**订阅父类的处理器会收到所有子类事件。**

| 订阅 | 发布 | 是否触发 |
| ---- | ---- | -------- |
| `Event.class` | `NoticeEvent` | ✅ |
| `Event.class` | `ThemeEvent` | ✅ |
| `NoticeEvent.class` | `NoticeEvent` | ✅ |
| `NoticeEvent.class` | `NoticeCloseEvent` | ❌（兄弟类） |
| `ThemeEvent.class` | `ThemeChangeEvent` | ❌（兄弟类） |

> 同理 `unsubscribe` 也对所有"父类型链上订阅集合"中匹配的 `Consumer` 实例移除。

---

## 自定义事件

#### 1. 简单标记事件

```java
public class MyDataChangedEvent extends Event {}

// 订阅
EventBus.getInstance().subscribe(MyDataChangedEvent.class,
    evt -> reload());

// 发布
new MyDataChangedEvent().publish();
```

#### 2. 携带 payload 的事件

```java
public class UserUpdatedEvent extends Event {
    private final User user;
    public UserUpdatedEvent(User u) { this.user = u; }
    public User getUser() { return user; }
}

EventBus.getInstance().subscribe(UserUpdatedEvent.class, evt -> {
    User u = evt.getUser();
    refresh(u);
});

new UserUpdatedEvent(user).publish();
```

#### 3. 利用 NodeNoticeEvent 做组件通信（推荐）

无需自定义类，按 `targetKey` 路由：

```java
NodeNoticeEvent.subscribeByKey("global-search", evt -> {
    String keyword = (String) evt.getPayload();
    doSearch(keyword);
});
new NodeNoticeEvent(searchKeyword, "global-search").publish();
```

---

## 线程模型与最佳实践

#### 线程模型

| 发布方式 | 订阅者执行线程 |
| -------- | -------------- |
| `event.publish()` | **调用方线程**（同步） |
| `event.publishAsync()` | `ObservableExecutor` **后台线程**（异步） |
| `Event.publish(event)` | **调用方线程**（同步） |

> 异常通过 `Platform.runLater` 转发到 FX 主线程重抛。

#### 最佳实践

1. **UI 更新前切换线程**：若订阅者要更新控件而事件是 `publishAsync` 来的，订阅者内部需 `Platform.runLater`。
2. **避免长任务订阅**：订阅者不应执行耗时操作；应只做 UI 刷新或轻量响应，必要时 `ObservableExecutor.execute(...)`。
3. **及时退订**：长生命周期组件（如全局菜单）订阅后，`Stage.setOnHidden` / `Skin.dispose` 中务必 `unsubscribe`，避免内存泄漏。
4. **使用 NodeNoticeEvent**：组件 ⇄ 组件通信优先选 `NodeNoticeEvent` + `targetKey`，避免为每对通信单独建类。
5. **善用继承分发**：可用 `subscribe(Event.class, ...)` 做全局监控（日志/审计）。
6. **并发安全**：`EventBus` 内部使用 `ConcurrentHashMap` + `CopyOnWriteArraySet`，订阅者集合在迭代期间允许增删，无需外部同步。
