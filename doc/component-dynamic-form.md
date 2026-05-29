# JavaFX DynamicForm 组件库 API 文档 v2.0

> **对标框架**: Element UI · Ant Design · Naive UI · FormsFX · TornadoFX  
> **平台**: JavaFX 11+  
> **包名**: `com.javafx.test.DynamicForm`

---

## 目录

1. [架构概览与组件关系](#1-架构概览与组件关系)
2. [Form — 表单主组件](#2-form--表单主组件)
3. [FormItem — 表单项容器](#3-formitem--表单项容器)
4. [FormModel — 数据模型](#4-formmodel--数据模型)
5. [FormValidator — 验证引擎](#5-formvalidator--验证引擎)
6. [FormValidationRule — 验证规则](#6-formvalidationrule--验证规则)
7. [FormValidationResult — 验证结果](#7-formvalidationresult--验证结果)
8. [FormStateManager — 状态管理器](#8-formstatemanager--状态管理器)
9. [FormEvent — 事件系统](#9-formevent--事件系统)
10. [FormFieldFactory — 插件工厂](#10-formfieldfactory--插件工厂)
11. [FormTheme — 主题配置](#11-formtheme--主题配置)
12. [FormSize — 尺寸枚举](#12-formsize--尺寸枚举)
13. [FormLabelPosition — 标签位置](#13-formlabelposition--标签位置)
14. [FormBindingMode — 绑定模式](#14-formbindingmode--绑定模式)
15. [FormGroup — 分组容器](#15-formgroup--分组容器)
16. [FormSection — 区域容器](#16-formsection--区域容器)
17. [代码示例大全](#17-代码示例大全)
18. [错误处理与异常](#18-错误处理与异常)
19. [性能优化与最佳实践](#19-性能优化与最佳实践)
20. [特殊功能详细说明](#20-特殊功能详细说明)

---

## 1. 架构概览与组件关系

### 1.1 组件层次结构

```
Form (表单主组件)
├── FormModel (数据模型 - 持有所有字段的 Observable 属性)
├── FormItem[] (表单项容器 - 包裹标签+控件+错误提示)
│   ├── Node (JavaFX 控件: TextField/ComboBox/CheckBox/...)
│   └── FormValidationRule[] (项级验证规则)
├── FormGroup[] (分组容器)
│   └── FormItem[]
├── FormSection[] (区域容器)
│   └── FormGroup[]
├── FormStateManager (状态管理器 - 脏检测/撤销重做)
├── FormTheme (主题配置)
├── FormEvent → FormEvent.Listener (事件系统)
└── FormValidator (验证引擎 - 静态工具类)
    └── FormValidationRule (验证规则)
    └── FormValidationResult (验证结果)
```

### 1.2 组件间协作流程

**数据流**: 用户输入 → FormItem 自动绑定 → FormModel 属性更新 → 触发验证 → 更新 FormItem 错误状态

**事件流**: 操作触发 → Form.fireEvent() → 遍历 FormEvent.Listener → 回调执行

**验证流**: Form.validate() → 合并 Form 级和 FormItem 级规则 → FormValidator 执行 → FormValidationResult → 更新 FormItem UI

### 1.3 设计理念

| 特性 | 对标框架 | 说明 |
|------|---------|------|
| 数据驱动表单 | Element UI el-form | FormModel 持有所有数据，控件自动绑定 |
| 验证规则系统 | async-validator | required/type/pattern/min/max/自定义/异步 |
| 计算属性 | Vue computed | 依赖追踪，自动更新 |
| Watch 监听器 | Vue watch | 字段变化深度观察 |
| 批量更新 | React batch | 抑制中间通知 |
| 状态管理 | FormsFX/TornadoFX | persist/rollback/undo/redo |
| 主题系统 | Ant Design tokens | 6种预定义主题 + 自定义 + CSS 变量 |
| 事件系统 | Vue emit | 14种生命周期事件 |
| 插件扩展 | Ant Design custom | FormFieldFactory 注册自定义渲染器 |
| 栅格布局 | Element UI row/col | columns/gutter/span |
| 分组布局 | FormsFX Group/Section | 语义化分组、折叠、可见性 |

---

## 2. Form — 表单主组件

### 2.1 概述

`Form` 是表单的顶层容器组件，对标 Element UI 的 `el-form`、Ant Design 的 `Form` 和 Naive UI 的 `NForm`。负责：
- 管理 FormItem 集合
- 持有 FormModel 数据模型引用
- 持有验证规则并驱动验证流程
- 管理表单布局（垂直/行内/栅格）
- 维护生命周期事件系统
- 提供状态管理入口

### 2.2 构造方法

```java
Form form = new Form();
```

无参构造，所有配置通过链式 API 设置。

### 2.3 链式配置 API

| 方法 | 参数类型 | 默认值 | 说明 |
|------|---------|--------|------|
| `model(FormModel)` | FormModel | null | 设置数据模型（**必须**），切换模型时自动清理旧监听器 |
| `rules(Map)` | Map<String, List<FormValidationRule>> | empty | 设置表单级验证规则 |
| `inline(boolean)` | boolean | false | 行内布局模式（水平紧凑排列） |
| `labelPosition(FormLabelPosition)` | FormLabelPosition | RIGHT | 标签位置：LEFT/RIGHT/TOP |
| `labelWidth(double)` | double | 80.0 | 标签区域宽度（px） |
| `labelSuffix(String)` | String | "" | 标签后缀（如 "："） |
| `showMessage(boolean)` | boolean | true | 是否显示验证错误信息 |
| `disabled(boolean)` | boolean | false | 是否禁用整个表单 |
| `size(FormSize)` | FormSize | DEFAULT | 全局尺寸（LARGE/DEFAULT/SMALL/MINI） |
| `theme(FormTheme)` | FormTheme | DEFAULT | 主题配置 |
| `bindingMode(FormBindingMode)` | FormBindingMode | CONTINUOUS | 绑定模式 |
| `columns(int)` | int | 0 | 栅格列数（0=不使用栅格） |
| `gutter(double)` | double | 16.0 | 栅格列间距（px） |
| `onValidate(Consumer)` | Consumer<FormValidationResult> | null | 验证完成回调 |
| `onSubmit(Consumer)` | Consumer<Map<String, Object>> | null | 提交回调 |

**示例**:
```java
Form form = new Form()
    .model(model)
    .rules(rules)
    .labelWidth(120)
    .labelSuffix("：")
    .labelPosition(FormLabelPosition.RIGHT)
    .size(FormSize.DEFAULT)
    .theme(FormTheme.ANT_DESIGN)
    .columns(2)
    .gutter(16)
    .onSubmit(data -> saveToServer(data));
```

### 2.4 表单项管理

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `addItem(FormItem)` | Form | 添加表单项（自动绑定父表单） |
| `addItems(FormItem...)` | Form | 批量添加表单项 |
| `insertItem(int, FormItem)` | Form | 在指定位置插入 |
| `removeItem(FormItem)` | Form | 移除表单项（自动 dispose） |
| `removeItem(String prop)` | Form | 按 prop 名称移除 |
| `findItem(String prop)` | FormItem | 按 prop 查找，null 安全 |
| `getItems()` | List<FormItem> | 获取不可修改的表单项列表 |
| `getItemCount()` | int | 获取表单项数量 |

### 2.5 验证 API

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `validate()` | boolean | 同步验证整个表单，返回是否通过 |
| `validate(Consumer<Boolean>)` | void | 同步验证 + 回调 |
| `validateField(String prop)` | boolean | 验证指定字段 |
| `validateFields(String...)` | FormValidationResult | 验证多个指定字段 |
| `validateAsync(Consumer)` | void | 异步验证（支持异步规则） |
| `resetFields()` | void | 重置所有字段为初始值并清除验证 |
| `clearValidate()` | void | 清除所有验证消息 |
| `clearValidate(String...)` | void | 清除指定字段的验证消息 |
| `scrollToField(String)` | void | 滚动到指定字段并聚焦 |

### 2.6 规则管理

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `addRule(String, FormValidationRule)` | Form | 动态添加单条规则 |
| `addRules(String, FormValidationRule...)` | Form | 动态添加多条规则 |
| `removeRules(String)` | Form | 移除指定字段的所有规则 |

### 2.7 状态管理

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getStateManager()` | FormStateManager | 获取状态管理器（懒初始化） |
| `persist()` | void | 持久化当前值 + 触发 PERSISTED 事件 |
| `rollback()` | void | 回滚 + 清除验证 + 触发 ROLLED_BACK 事件 |
| `isDirty()` | boolean | 是否有脏数据 |
| `dirtyProperty()` | ReadOnlyBooleanProperty | 脏状态可绑定属性 |

### 2.8 事件系统

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `on(FormEvent.Type, Listener)` | Form | 注册事件监听器 |
| `off(FormEvent.Type, Listener)` | Form | 移除事件监听器 |

### 2.9 分组/区域

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `addGroup(FormGroup)` | Form | 添加分组 |
| `addSection(FormSection)` | Form | 添加区域 |
| `getGroups()` | List<FormGroup> | 获取所有分组 |
| `getSections()` | List<FormSection> | 获取所有区域 |

### 2.10 其他

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getNode()` | Node | 获取根 UI 节点（添加到 Scene 中） |
| `getModel()` | FormModel | 获取数据模型 |
| `getFormData()` | Map<String, Object> | 获取表单数据快照 |
| `submit()` | void | 提交表单（先验证再回调） |
| `dispose()` | void | 释放所有资源 |

### 2.11 Observable 属性

所有配置属性都提供 `xxxProperty()` 方法返回 JavaFX Property 对象，可用于绑定：
`inlineProperty()`, `labelPositionProperty()`, `labelWidthProperty()`, `sizeProperty()`, `themeProperty()`, `disabledProperty()`, `columnsProperty()`, `gutterProperty()` 等。

---

## 3. FormItem — 表单项容器

### 3.1 概述

对标 Element UI 的 `el-form-item`，作为表单输入项的容器，包含标签区域、输入控件区域、描述文本区域和验证错误消息区域。支持自动绑定常见 JavaFX 控件到 FormModel。

### 3.2 构造方法

```java
new FormItem()                                    // 空构造
new FormItem("活动名称", "name")                    // label + prop
new FormItem("活动名称", "name", new TextField())   // label + prop + 控件
```

| 参数 | 类型 | 说明 |
|------|------|------|
| label | String | 标签文本 |
| prop | String | 模型字段名（用于验证和数据绑定） |
| content | Node | JavaFX 控件节点 |

### 3.3 链式配置 API

| 方法 | 参数类型 | 默认值 | 说明 |
|------|---------|--------|------|
| `label(String)` | String | "" | 标签文本 |
| `prop(String)` | String | null | 模型字段名 |
| `content(Node)` | Node | null | 设置内容控件 |
| `labelPosition(FormLabelPosition)` | FormLabelPosition | null(继承) | 标签位置 |
| `labelWidth(double)` | double | null(继承) | 标签宽度 |
| `required(boolean)` | boolean | false | 显示必填星号 |
| `size(FormSize)` | FormSize | null(继承) | 尺寸 |
| `showMessage(boolean)` | boolean | true | 是否显示错误消息 |
| `tooltip(String)` | String | null | 鼠标悬停提示 |
| `placeholder(String)` | String | null | 占位文本 |
| `editable(boolean)` | boolean | true | 是否可编辑 |
| `span(int)` | int | 24 | 栅格跨列数（1-24） |
| `description(String)` | String | null | 描述文本（显示在控件下方） |
| `visible(boolean)` | boolean | true | 是否可见 |
| `fieldType(String)` | String | null | 自定义字段类型标识 |
| `rules(List)` | List<FormValidationRule> | null | 项级验证规则 |

### 3.4 自动绑定支持的控件类型

FormItem 添加到 Form 后，会自动将以下控件类型绑定到 FormModel：

| 控件类型 | 绑定属性 | 模型值类型 | 备注 |
|---------|---------|-----------|------|
| TextField | textProperty | String | 双向绑定 + 焦点失去触发验证 |
| PasswordField | textProperty | String | 同 TextField |
| TextArea | textProperty | String | 同 TextField |
| ComboBox | valueProperty | Object | 选择变更触发验证 |
| ChoiceBox | valueProperty | Object | 选择变更触发验证 |
| CheckBox | selectedProperty | Boolean | 选择变更触发验证 |
| DatePicker | valueProperty | LocalDate | 焦点失去触发验证 |
| Spinner | valueProperty | Number | 值变更触发验证 |
| ToggleButton | selectedProperty | Boolean | 选择变更触发验证 |
| Slider | valueProperty | Double | 值变更触发验证 |
| ColorPicker | valueProperty | Color | 值变更触发验证 |
| ListView | selectionModel | Object/List | 支持单选和多选 |

### 3.5 手动绑定方法

```java
// RadioButton 组绑定
item.bindRadioGroup(ToggleGroup group, String fieldName);

// CheckBox 组绑定（多选列表）
item.bindCheckBoxGroup(List<CheckBox> checkBoxes, String fieldName);
```

### 3.6 验证状态

```java
FormValidationResult.FieldStatus status = item.getValidationStatus();
// PENDING / VALIDATING / SUCCESS / WARNING / ERROR
```

### 3.7 资源释放

```java
item.dispose(); // 移除所有监听器，清空节点，断开引用
```

---

## 4. FormModel — 数据模型

### 4.1 概述

持有表单所有字段的 Observable 属性。每个字段以 `ObjectProperty<Object>` 包装，支持数据变更监听、重置为初始值、类型安全取值、快照/恢复、计算属性、Watch 监听器、批量更新等。

### 4.2 字段定义

```java
FormModel model = new FormModel()
    .field("name", "")                              // 字符串字段
    .field("age", 25)                               // 数值字段
    .field("active", true)                          // 布尔字段
    .field("tags", FXCollections.observableArrayList()) // 列表字段
    .field("name", "", "用户名称");                   // 带标签的字段
```

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `field(String name, Object initialValue)` | FormModel | 定义字段 |
| `field(String name, Object initialValue, String label)` | FormModel | 定义字段 + 标签元数据 |
| `FormModel.fromMap(Map<String, Object>)` | FormModel | 从 Map 创建 |

### 4.3 读写操作

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `setFieldValue(String, Object)` | void | 设置字段值（只读字段被跳过） |
| `setFieldValues(Map)` | void | 批量设置字段值 |
| `getFieldValue(String)` | Object | 获取字段值 |
| `getFieldValue(String, Class<T>)` | T | 类型安全获取（自动转换） |
| `getString(String)` | String | 获取字符串值，null → "" |
| `getInt(String, int)` | int | 获取整数值，异常返回默认值 |
| `getDouble(String, double)` | double | 获取浮点数值 |
| `getBoolean(String, boolean)` | boolean | 获取布尔值 |
| `getList(String)` | List<T> | 获取列表值 |
| `fieldProperty(String)` | ObjectProperty<Object> | 获取 Observable 属性 |

### 4.4 计算属性（computed）

```java
// 定义计算属性：fullName 依赖 firstName 和 lastName
model.computed("fullName", m ->
    m.getString("firstName") + " " + m.getString("lastName"),
    "firstName", "lastName"
);

// 当 firstName 或 lastName 变化时，fullName 自动重新计算
// 计算属性是只读的，调用 setFieldValue 会被跳过
```

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `computed(String name, Function<FormModel, Object>, String... deps)` | FormModel | 定义计算属性 |

**注意事项**:
- 计算属性自动标记为只读，不能通过 `setFieldValue` 修改
- 依赖字段必须在定义计算属性之前已存在
- 计算函数中的异常会被捕获并记录日志，不会传播

### 4.5 Watch 监听器

```java
// 监听单个字段
model.watch("name", (oldVal, newVal) -> {
    System.out.println("name 从 " + oldVal + " 变为 " + newVal);
});

// 监听多个字段
model.watchFields((fieldName, oldVal, newVal) -> {
    System.out.println(fieldName + " changed");
}, "field1", "field2", "field3");
```

### 4.6 批量更新

```java
// 批量更新期间所有变更通知被延迟到 runnable 完成后统一触发
model.batchUpdate(() -> {
    model.setFieldValue("name", "Alice");
    model.setFieldValue("age", 25);
    model.setFieldValue("city", "北京");
});
// 此时才会触发变更通知（仅对实际变更的字段触发一次）
```

### 4.7 字段变更回调

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `onAnyFieldChange(FieldChangeCallback)` | FormModel | 注册全局变更监听 |
| `onFieldChange(String, BiConsumer)` | FormModel | 注册单字段变更监听 |
| `removeGlobalChangeCallback(FieldChangeCallback)` | void | 移除全局回调 |
| `removeFieldChangeCallbacks(String)` | void | 移除字段级回调 |

### 4.8 元数据与查询

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `setFieldMeta(String, String, Object)` | FormModel | 设置字段元数据 |
| `getFieldMeta(String, String)` | Object | 获取元数据 |
| `getFieldLabel(String)` | String | 获取标签（快捷） |
| `readOnly(String)` | FormModel | 设置只读 |
| `isReadOnly(String)` | boolean | 检查是否只读 |
| `hasField(String)` | boolean | 字段是否存在 |
| `getFieldNames()` | Set<String> | 所有字段名（不可修改） |
| `getFieldCount()` | int | 字段数量 |
| `removeField(String)` | FormModel | 移除字段 |

### 4.9 快照与重置

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `snapshot()` | Map<String, Object> | 创建当前值快照 |
| `restoreSnapshot(Map)` | void | 从快照恢复 |
| `reset()` | void | 重置所有字段为初始值 |
| `resetField(String)` | void | 重置单个字段 |
| `updateInitialValue(String, Object)` | void | 更新初始值 |
| `toMap()` | Map<String, Object> | 导出为 Map |
| `isFieldChanged(String)` | boolean | 字段是否已修改 |
| `getChangedFields()` | Set<String> | 获取已修改字段名 |

---

## 5. FormValidator — 验证引擎

### 5.1 概述

静态工具类，参照 async-validator 标准实现。不可实例化。提供同步验证、异步验证、去抖验证、分组验证等多种模式。内部维护正则缓存和去抖调度器。

### 5.2 同步验证 API

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `validate(FormModel, Map)` | FormValidationResult | 验证整个模型 |
| `validateWithModel(FormModel, Map, String group)` | FormValidationResult | 带分组的验证 |
| `validateGroup(FormModel, Map, String)` | FormValidationResult | 分组验证（等同 validateWithModel） |
| `validateField(String, Object, List)` | List<String> | 验证单个字段 |
| `validateField(String, Object, List, FormModel)` | List<String> | 验证单个字段（支持跨字段） |
| `validateFields(FormModel, Map, String...)` | FormValidationResult | 验证指定的多个字段 |

### 5.3 异步验证 API

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `validateAsync(FormModel, Map)` | CompletableFuture<FormValidationResult> | 异步验证整个表单 |
| `validateFieldAsync(String, Object, List)` | CompletableFuture<List<String>> | 异步验证单个字段 |
| `validateFieldAsync(String, Object, List, FormModel)` | CompletableFuture<List<String>> | 带模型的异步验证 |
| `hasAsyncRules(Map)` | boolean | 检查是否有异步规则 |

### 5.4 去抖验证

```java
// 用户停止输入 300ms 后才执行验证
FormValidator.validateFieldDebounced("email", value, rules, 300, errors -> {
    // 在 JavaFX Application Thread 中回调
    System.out.println("验证结果: " + errors);
});
```

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `validateFieldDebounced(String, Object, List, long, Consumer)` | void | 去抖验证 |
| `cancelDebounced(String field)` | void | 取消指定字段的去抖任务 |
| `cancelAllDebounced()` | void | 取消所有去抖任务 |
| `clearPatternCache()` | void | 清理正则缓存 |

### 5.5 验证规则执行顺序

1. 自定义校验器（`validator`）优先
2. 跨字段验证（`crossField`）
3. 枚举白名单验证（`enumValues`）
4. 必填校验（`required`）
5. 空值跳过后续校验
6. 类型校验（`type`）
7. 正则校验（`pattern`）
8. 数值范围校验（`minValue/maxValue`）
9. 长度/大小校验（`min/max`）

---

## 6. FormValidationRule — 验证规则

### 6.1 概述

对标 Element UI 的 rules 配置，参照 async-validator 标准。支持 Builder 模式和快捷工厂方法。

### 6.2 快捷工厂方法

| 方法 | 说明 | 示例 |
|------|------|------|
| `required(String)` | 必填 | `FormValidationRule.required("请输入姓名")` |
| `required()` | 必填（默认消息） | `FormValidationRule.required()` |
| `email(String)` | 邮箱格式 | `FormValidationRule.email(null)` |
| `url(String)` | URL 格式 | `FormValidationRule.url("请输入URL")` |
| `phone(String)` | 中国手机号 | `FormValidationRule.phone(null)` |
| `length(int, int, String)` | 长度范围 | `FormValidationRule.length(2, 20, "长度2-20")` |
| `stringLength(int, int, String)` | 字符串长度 | `FormValidationRule.stringLength(3, 50, null)` |
| `intRange(int, int, String)` | 整数范围 | `FormValidationRule.intRange(1, 100, null)` |
| `doubleRange(double, double, String)` | 浮点数范围 | `FormValidationRule.doubleRange(0.0, 99.9, null)` |
| `pattern(String, String)` | 正则表达式 | `FormValidationRule.pattern("^\\d+$", "仅数字")` |
| `custom(BiFunction, String)` | 自定义同步 | 见下方示例 |
| `async(Function, String)` | 异步验证 | 见下方示例 |
| `equalTo(String, String)` | 跨字段等值 | `FormValidationRule.equalTo("password", "密码不一致")` |
| `enumValues(List, String)` | 枚举白名单 | `FormValidationRule.enumValues(list, "无效选项")` |
| `idCard(String)` | 身份证号（18位） | `FormValidationRule.idCard(null)` |
| `ipAddress(String)` | IP 地址 | `FormValidationRule.ipAddress(null)` |
| `numeric(String)` | 仅数字 | `FormValidationRule.numeric(null)` |
| `alpha(String)` | 仅字母 | `FormValidationRule.alpha(null)` |
| `alphaNumeric(String)` | 字母数字 | `FormValidationRule.alphaNumeric(null)` |

### 6.3 Builder 模式

```java
FormValidationRule rule = FormValidationRule.builder()
    .required(true)                    // 是否必填
    .message("错误消息")               // 错误提示
    .trigger("blur")                   // 触发方式: "blur" / "change"
    .type("string")                    // 类型: string/number/integer/boolean/array/email/url/date
    .min(3).max(20)                    // 长度范围
    .minValue(0.0).maxValue(100.0)     // 数值范围
    .pattern("^[a-zA-Z]+$")           // 正则
    .validator((rule, value) -> null)  // 自定义校验器
    .asyncValidator(val -> future)     // 异步校验器
    .crossField("otherField")         // 跨字段引用
    .enumValues(Arrays.asList("A","B"))// 枚举白名单
    .when(model -> condition)          // 条件验证谓词
    .group("step1")                    // 验证组
    .warningOnly(true)                 // 仅警告模式
    .debounce(300)                     // 去抖延迟（ms）
    .priority(10)                      // 优先级（越小越先）
    .build();
```

### 6.4 Getter 方法

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `isRequired()` | boolean | 是否必填 |
| `getMessage()` | String | 错误消息 |
| `getTrigger()` | String | 触发方式 |
| `getType()` | String | 字段类型 |
| `getMin()` / `getMax()` | Integer | 长度范围 |
| `getMinValue()` / `getMaxValue()` | Double | 数值范围 |
| `getPattern()` | String | 正则表达式 |
| `getValidator()` | BiFunction | 自定义校验器 |
| `getAsyncValidator()` | Function | 异步校验器 |
| `isAsync()` | boolean | 是否异步规则 |
| `getCrossField()` | String | 跨字段引用 |
| `getEnumValues()` | List<Object> | 枚举白名单 |
| `getWhen()` | Predicate<FormModel> | 条件谓词 |
| `getGroup()` | String | 验证组 |
| `isWarningOnly()` | boolean | 是否仅警告 |
| `getDebounce()` | long | 去抖延迟 |
| `getPriority()` | int | 优先级 |

---

## 7. FormValidationResult — 验证结果

### 7.1 概述

封装验证的完整结果，包含整体状态、每个字段的错误/警告消息列表，以及字段级验证状态。

### 7.2 字段状态枚举 (FieldStatus)

| 值 | 说明 |
|----|------|
| `SUCCESS` | 验证通过 |
| `WARNING` | 有警告信息 |
| `ERROR` | 验证失败 |
| `VALIDATING` | 验证中（异步） |
| `PENDING` | 未验证 |

### 7.3 工厂方法

```java
FormValidationResult.success()                     // 验证通过
FormValidationResult.failure(errorMap)              // 验证失败
FormValidationResult.of(valid, errors, warnings)   // 带警告
```

### 7.4 查询 API

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `isValid()` | boolean | 是否验证通过 |
| `getErrors()` | Map<String, List<String>> | 所有错误 |
| `getFieldErrors(String)` | List<String> | 指定字段的错误 |
| `getFirstError(String)` | String | 指定字段第一条错误 |
| `getFirstError()` | String | 所有字段中第一条错误 |
| `getErrorFieldCount()` | int | 有错误的字段数 |
| `getErrorFieldNames()` | Set<String> | 错误字段名集合 |
| `getAllErrors()` | List<String> | 所有错误消息列表 |
| `getTotalErrorCount()` | int | 错误总数 |
| `getWarnings()` | Map<String, List<String>> | 所有警告 |
| `getFieldWarnings(String)` | List<String> | 指定字段的警告 |
| `hasWarnings()` | boolean | 是否有警告 |
| `getFieldStatus(String)` | FieldStatus | 字段验证状态 |
| `hasFieldError(String)` | boolean | 字段是否有错误 |
| `getValidationDuration()` | long | 验证耗时（ms） |
| `merge(FormValidationResult)` | FormValidationResult | 合并两个结果 |

---

## 8. FormStateManager — 状态管理器

### 8.1 概述

参考 FormsFX 的 persist/reset 机制和 TornadoFX 的 ViewModel 理念，提供脏数据检测、快照、撤销/重做、表单比较、自动保存等高级功能。

### 8.2 构造方法

```java
FormStateManager state = new FormStateManager(model);             // 默认50级撤销
FormStateManager state = new FormStateManager(model, 100);        // 自定义撤销深度
// 或通过 Form 懒初始化:
FormStateManager state = form.getStateManager();
```

### 8.3 脏数据检测

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `isDirty()` | boolean | 任意字段与持久化值不同 |
| `dirtyProperty()` | ReadOnlyBooleanProperty | 可绑定到 UI |
| `getDirtyFields()` | Set<String> | 所有脏字段名 |
| `isFieldDirty(String)` | boolean | 指定字段是否脏 |
| `getDirtyFieldCount()` | int | 脏字段数量 |

### 8.4 持久化/回滚

| 方法 | 说明 |
|------|------|
| `persist()` | 将当前值设为新的持久化基准 |
| `rollback()` | 恢复到上次 persist 的值 |
| `onPersist(Consumer)` | 添加持久化完成监听器 |

### 8.5 撤销/重做

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `pushUndoSnapshot()` | void | 手动保存快照到撤销栈 |
| `undo()` | boolean | 撤销（返回是否成功） |
| `redo()` | boolean | 重做（返回是否成功） |
| `canUndo()` | boolean | 是否可撤销 |
| `canRedo()` | boolean | 是否可重做 |
| `canUndoProperty()` | ReadOnlyBooleanProperty | 可绑定属性 |
| `canRedoProperty()` | ReadOnlyBooleanProperty | 可绑定属性 |
| `getUndoStackSize()` | int | 撤销栈深度 |
| `getRedoStackSize()` | int | 重做栈深度 |

### 8.6 自动保存

```java
// 每 5 秒检查，有脏数据则自动 persist 并回调
state.enableAutoSave(5000, data -> saveToServer(data));

// 禁用自动保存
state.disableAutoSave();
```

### 8.7 表单比较

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `diff(FormModel)` | Map<String, Object[]> | 与另一个模型比较差异 |
| `diffWithPersisted()` | Map<String, Object[]> | 与持久化值比较差异 |

返回的 `Object[]` 格式为 `[当前值, 对比值]`。

### 8.8 变更历史

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getChangeHistory()` | List<HistoryEntry> | 完整历史记录 |
| `getRecentHistory(int)` | List<HistoryEntry> | 最近 N 条记录 |
| `clearChangeHistory()` | void | 清除历史 |
| `clearHistory()` | void | 清除所有状态 |

**HistoryEntry** 包含: `getTimestamp()` (long), `getAction()` (String), `getFieldName()` (String)

---

## 9. FormEvent — 事件系统

### 9.1 事件类型枚举

| 类型 | 说明 | 可取消 | 携带数据 |
|------|------|--------|---------|
| `FIELD_CHANGE` | 字段值变更 | 否 | fieldName, oldValue, newValue |
| `BEFORE_VALIDATE` | 验证开始前 | **是** | - |
| `AFTER_VALIDATE` | 验证完成后 | 否 | validationResult |
| `FIELD_VALIDATED` | 字段验证完成 | 否 | fieldName, validationResult |
| `BEFORE_SUBMIT` | 提交前 | **是** | - |
| `SUBMIT` | 提交成功 | 否 | formData |
| `BEFORE_RESET` | 重置前 | 否 | - |
| `AFTER_RESET` | 重置后 | 否 | - |
| `ITEM_ADDED` | 表单项添加 | 否 | fieldName |
| `ITEM_REMOVED` | 表单项移除 | 否 | fieldName |
| `THEME_CHANGE` | 主题变更 | 否 | - |
| `PERSISTED` | 数据持久化 | 否 | formData |
| `ROLLED_BACK` | 数据回滚 | 否 | formData |
| `DISPOSED` | 表单释放 | 否 | - |

### 9.2 使用方法

```java
// 注册事件监听器
form.on(FormEvent.Type.FIELD_CHANGE, event -> {
    System.out.println(event.getFieldName() + " → " + event.getNewValue());
});

// 取消可取消的事件
form.on(FormEvent.Type.BEFORE_SUBMIT, event -> {
    if (!ready) event.cancel(); // 阻止提交
});

// 移除监听器
FormEvent.Listener myListener = event -> { ... };
form.on(FormEvent.Type.SUBMIT, myListener);
form.off(FormEvent.Type.SUBMIT, myListener);
```

### 9.3 FormEvent API

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getType()` | Type | 事件类型 |
| `getFieldName()` | String | 相关字段名 |
| `getOldValue()` | Object | 旧值 |
| `getNewValue()` | Object | 新值 |
| `getFormData()` | Map<String, Object> | 表单数据快照 |
| `getValidationResult()` | FormValidationResult | 验证结果 |
| `getSource()` | Form | 事件来源 Form |
| `isCancelled()` | boolean | 是否已取消 |
| `cancel()` | void | 取消事件 |

### 9.4 Builder

```java
FormEvent event = FormEvent.builder(FormEvent.Type.FIELD_CHANGE)
    .source(form)
    .fieldName("name")
    .oldValue("old")
    .newValue("new")
    .build();
```

---

## 10. FormFieldFactory — 插件工厂

### 10.1 概述

静态工厂类，提供可扩展的字段渲染器注册和创建机制。所有已注册的渲染器全局共享（跨 Form 实例）。

### 10.2 API

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `register(String type, FieldRenderer)` | void | 注册渲染器 |
| `registerWithBinding(String, FieldRenderer, FieldBinder)` | void | 注册渲染器 + 绑定器 |
| `unregister(String type)` | void | 注销渲染器 |
| `create(String type, String fieldName, Map config)` | Node | 创建控件 |
| `getBinder(String type)` | FieldBinder | 获取绑定器 |
| `isRegistered(String type)` | boolean | 是否已注册 |
| `getRegisteredTypes()` | Set<String> | 所有已注册类型 |
| `clearAll()` | void | 清除所有注册 |

### 10.3 接口定义

```java
// 渲染器接口
@FunctionalInterface
public interface FieldRenderer {
    Node create(String fieldName, Map<String, Object> config);
}

// 绑定器接口
@FunctionalInterface
public interface FieldBinder {
    void bind(Node control, FormModel model, String fieldName);
}
```

### 10.4 使用示例

```java
// 注册自定义评分控件
FormFieldFactory.register("rating", (fieldName, config) -> {
    int max = config != null && config.containsKey("max") ? (int) config.get("max") : 5;
    Slider slider = new Slider(0, max, 0);
    slider.setSnapToTicks(true);
    return slider;
});

// 创建实例
Node ratingNode = FormFieldFactory.create("rating", "score", Map.of("max", 10));

// 注册带自动绑定的渲染器
FormFieldFactory.registerWithBinding("star-rating",
    (fieldName, config) -> new StarRatingControl(),
    (control, model, fieldName) -> {
        StarRatingControl src = (StarRatingControl) control;
        src.ratingProperty().addListener((obs, o, n) -> model.setFieldValue(fieldName, n));
    }
);
```

---

## 11. FormTheme — 主题配置

### 11.1 预定义主题

| 常量 | 说明 | 主色 | 背景色 |
|------|------|------|--------|
| `FormTheme.DEFAULT` | Element UI 风格 | #409EFF | #FFFFFF |
| `FormTheme.DARK` | Element UI 暗色 | #409EFF | #1D1E1F |
| `FormTheme.ANT_DESIGN` | Ant Design 风格 | #1677FF | #FFFFFF |
| `FormTheme.ANT_DESIGN_DARK` | Ant Design 暗色 | #1668DC | #141414 |
| `FormTheme.NAIVE_UI` | Naive UI 风格 | #18A058 | #FFFFFF |
| `FormTheme.NAIVE_UI_DARK` | Naive UI 暗色 | #63E2B7 | #101014 |

### 11.2 自定义主题

```java
FormTheme custom = new FormTheme()
    .primaryColor(Color.web("#1890FF"))
    .successColor(Color.web("#52C41A"))
    .warningColor(Color.web("#FAAD14"))
    .errorColor(Color.RED)
    .infoColor(Color.GRAY)
    .textColor(Color.BLACK)
    .labelColor(Color.DARKGRAY)
    .borderColor(Color.LIGHTGRAY)
    .disabledColor(Color.SILVER)
    .backgroundColor(Color.WHITE)
    .hoverColor(Color.web("#1890FF", 0.1))
    .focusBorderColor(Color.web("#1890FF"))
    .placeholderColor(Color.SILVER)
    .borderRadius(8)
    .borderWidth(1)
    .fontFamily("Microsoft YaHei")
    .itemSpacing(20)
    .labelFontSize(14)
    .inputFontSize(14);
```

### 11.3 CSS 生成方法

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `toCss()` | String | JavaFX 背景/字体 CSS |
| `toInputCss()` | String | 输入框 CSS |
| `toInputFocusCss()` | String | 聚焦状态 CSS |
| `toCssVariables()` | Map<String, String> | CSS 变量映射 |
| `copy()` | FormTheme | 创建副本 |

### 11.4 全局主题管理

```java
// 设置/获取全局当前主题
FormTheme.setCurrentTheme(FormTheme.ANT_DESIGN);
FormTheme current = FormTheme.getCurrentTheme();

// 可绑定属性
ObjectProperty<FormTheme> themeProperty = FormTheme.currentThemeProperty();
```

### 11.5 工具方法

```java
String hex = FormTheme.toHex(Color.web("#409EFF"));         // "#409EFF"
String rgba = FormTheme.toRgba(Color.web("#409EFF"), 0.5);  // "rgba(64,158,255,0.50)"
```

---

## 12. FormSize — 尺寸枚举

| 枚举值 | 控件高度 | 字体大小 | 适用场景 |
|--------|---------|---------|---------|
| `LARGE` | 40px | 16px | 宽松布局、后台管理 |
| `DEFAULT` | 32px | 14px | 标准布局（默认） |
| `SMALL` | 24px | 12px | 紧凑布局、数据表格内 |
| `MINI` | 20px | 11px | 超紧凑、工具栏、筛选条 |

**方法**: `getValue()`, `getControlHeight()`, `getFontSize()`, `getHorizontalPadding()`, `getVerticalPadding()`, `toCssStyle()`, `FormSize.fromValue(String)`

---

## 13. FormLabelPosition — 标签位置

| 枚举值 | 说明 |
|--------|------|
| `LEFT` | 标签在左侧，文本左对齐 |
| `RIGHT` | 标签在左侧，文本右对齐（默认） |
| `TOP` | 标签在控件顶部 |

---

## 14. FormBindingMode — 绑定模式

| 枚举值 | 说明 | 对标 |
|--------|------|------|
| `MANUAL` | 手动模式，调用 persist() 才写入模型 | FormsFX |
| `CONTINUOUS` | 连续模式（默认），每次编辑直接写入 | 标准行为 |
| `LAZY` | 延迟模式，失去焦点时写入 | Vue v-model.lazy |

---

## 15. FormGroup — 分组容器

### 15.1 工厂方法

```java
FormGroup group = FormGroup.of(item1, item2, item3);
FormGroup group = FormGroup.of("基本信息", item1, item2);
```

### 15.2 链式 API

| 方法 | 说明 |
|------|------|
| `title(String)` | 分组标题 |
| `description(String)` | 分组描述 |
| `collapsible(boolean)` | 是否可折叠 |
| `collapsed(boolean)` | 是否默认折叠 |
| `visible(boolean)` | 是否可见 |
| `add(FormItem)` | 添加表单项 |

### 15.3 验证状态

| 方法 | 说明 |
|------|------|
| `hasErrors()` | 分组内是否有验证错误 |
| `getErrorCount()` | 有错误的表单项数量 |

---

## 16. FormSection — 区域容器

### 16.1 工厂方法

```java
FormSection section = FormSection.of(group1, group2);
FormSection section = FormSection.of("个人信息", group1, group2);
```

### 16.2 链式 API

| 方法 | 说明 |
|------|------|
| `title(String)` | 区域标题（带分隔线） |
| `description(String)` | 区域描述 |
| `visible(boolean)` | 是否可见 |
| `add(FormGroup)` | 添加分组 |

### 16.3 验证状态

| 方法 | 说明 |
|------|------|
| `hasErrors()` | 区域内是否有验证错误 |
| `getErrorCount()` | 错误总数 |
| `getAllItems()` | 获取所有 FormItem |

---

## 17. 代码示例大全

### 17.1 基础用法 — 简单表单创建

```java
// 1. 创建数据模型
FormModel model = new FormModel()
    .field("name", "")
    .field("email", "")
    .field("age", 0);

// 2. 定义验证规则
Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();
rules.put("name", Arrays.asList(
    FormValidationRule.required("请输入姓名"),
    FormValidationRule.stringLength(2, 20, null)
));
rules.put("email", Collections.singletonList(
    FormValidationRule.email(null)
));

// 3. 创建控件
TextField nameField = new TextField();
nameField.setPromptText("请输入姓名");
TextField emailField = new TextField();
emailField.setPromptText("请输入邮箱");

// 4. 组装表单
Form form = new Form()
    .model(model)
    .rules(rules)
    .labelWidth(100)
    .labelSuffix("：");

form.addItems(
    new FormItem("姓名", "name", nameField),
    new FormItem("邮箱", "email", emailField)
);

// 5. 添加到界面
root.getChildren().add(form.getNode());

// 6. 验证和提交
Button submitBtn = new Button("提交");
submitBtn.setOnAction(e -> {
    if (form.validate()) {
        Map<String, Object> data = form.getFormData();
        System.out.println("提交: " + data);
    }
});
```

### 17.2 高级功能 — 计算属性

```java
FormModel model = new FormModel()
    .field("unitPrice", 0.0)
    .field("quantity", 0)
    .field("discount", 1.0);

// total 会自动跟随依赖字段变化更新
model.computed("total", m -> {
    double price = m.getDouble("unitPrice", 0);
    int qty = m.getInt("quantity", 0);
    double disc = m.getDouble("discount", 1.0);
    return price * qty * disc;
}, "unitPrice", "quantity", "discount");

// 监听 total 变化
model.watch("total", (oldVal, newVal) -> {
    System.out.println("总价变更: " + newVal);
});
```

### 17.3 高级功能 — 跨字段验证

```java
// 密码确认场景
rules.put("password", Arrays.asList(
    FormValidationRule.required("请输入密码"),
    FormValidationRule.length(8, 32, "密码长度 8-32 位")
));
rules.put("confirmPassword", Arrays.asList(
    FormValidationRule.required("请确认密码"),
    FormValidationRule.equalTo("password", "两次密码不一致")
));
```

### 17.4 高级功能 — 条件验证

```java
// 仅当勾选了"需要配送"时，地址字段才必填
rules.put("address", Collections.singletonList(
    FormValidationRule.builder()
        .required(true)
        .message("请输入配送地址")
        .when(m -> m.getBoolean("needDelivery", false))
        .build()
));
```

### 17.5 高级功能 — 验证组

```java
// 分步表单：第一步只验证 step1 组的规则
rules.put("name", Collections.singletonList(
    FormValidationRule.builder().required(true).message("请输入姓名").group("step1").build()
));
rules.put("address", Collections.singletonList(
    FormValidationRule.builder().required(true).message("请输入地址").group("step2").build()
));

// 只验证第一步
FormValidationResult result = FormValidator.validateGroup(model, rules, "step1");
```

### 17.6 布局 — 栅格布局

```java
Form form = new Form()
    .model(model)
    .columns(3)       // 3 列
    .gutter(16)       // 16px 间距
    .labelWidth(80);

form.addItems(
    new FormItem("姓名", "name", nameField).span(8),    // 占 1/3
    new FormItem("年龄", "age", ageField).span(8),      // 占 1/3
    new FormItem("邮箱", "email", emailField).span(8),  // 占 1/3
    new FormItem("地址", "addr", addrField).span(24)    // 占满一行
);
```

### 17.7 交互 — 事件系统

```java
// 监听字段变更
form.on(FormEvent.Type.FIELD_CHANGE, event -> {
    System.out.println(event.getFieldName() + ": " +
        event.getOldValue() + " → " + event.getNewValue());
});

// 拦截提交
form.on(FormEvent.Type.BEFORE_SUBMIT, event -> {
    if (!isNetworkAvailable()) {
        event.cancel(); // 取消提交
        showNotification("网络不可用，请稍后重试");
    }
});

// 监听验证结果
form.on(FormEvent.Type.AFTER_VALIDATE, event -> {
    FormValidationResult result = event.getValidationResult();
    if (!result.isValid()) {
        showNotification("共 " + result.getTotalErrorCount() + " 项错误");
    }
});
```

### 17.8 交互 — 主题切换

```java
// 动态切换主题
form.theme(FormTheme.ANT_DESIGN);
form.theme(FormTheme.NAIVE_UI_DARK);

// 查看 CSS 变量
Map<String, String> vars = FormTheme.ANT_DESIGN.toCssVariables();
vars.forEach((key, value) -> System.out.println(key + ": " + value));
```

### 17.9 交互 — 插件扩展

```java
// 注册自定义日期范围选择器
FormFieldFactory.registerWithBinding("date-range",
    (fieldName, config) -> {
        HBox box = new HBox(8);
        DatePicker start = new DatePicker();
        DatePicker end = new DatePicker();
        box.getChildren().addAll(start, new Label("至"), end);
        return box;
    },
    (control, model, fieldName) -> {
        HBox box = (HBox) control;
        DatePicker start = (DatePicker) box.getChildren().get(0);
        DatePicker end = (DatePicker) box.getChildren().get(2);
        start.valueProperty().addListener((o, ov, nv) ->
            model.setFieldValue(fieldName + "_start", nv));
        end.valueProperty().addListener((o, ov, nv) ->
            model.setFieldValue(fieldName + "_end", nv));
    }
);

// 使用
Node dateRange = FormFieldFactory.create("date-range", "period", null);
form.addItem(new FormItem("日期范围", "period", dateRange));
```

### 17.10 状态管理 — 脏检测与撤销重做

```java
FormStateManager state = form.getStateManager();

// 绑定脏状态到保存按钮
saveBtn.disableProperty().bind(state.dirtyProperty().not());

// 保存快照后修改
state.pushUndoSnapshot();
model.setFieldValue("name", "新值");

// 撤销
if (state.canUndo()) {
    state.undo();
}

// 持久化
state.persist();

// 查看差异
Map<String, Object[]> diff = state.diffWithPersisted();
diff.forEach((field, vals) -> {
    System.out.println(field + ": " + vals[1] + " → " + vals[0]);
});

// 自动保存
state.enableAutoSave(30000, data -> {
    System.out.println("自动保存: " + data);
});
```

### 17.11 状态管理 — 批量更新

```java
// 批量更新期间，计算属性和监听器不会被中间值触发
model.batchUpdate(() -> {
    model.setFieldValue("firstName", "张");
    model.setFieldValue("lastName", "三");
    model.setFieldValue("age", 25);
    model.setFieldValue("city", "北京");
});
// batchUpdate 结束后，所有变更通知一次性触发
```

### 17.12 分组布局

```java
FormGroup basicGroup = FormGroup.of("基本信息",
    new FormItem("姓名", "name", nameField),
    new FormItem("年龄", "age", ageField)
).description("填写个人基本信息");

FormGroup advancedGroup = FormGroup.of("高级选项",
    new FormItem("超时", "timeout", spinner)
).collapsible(true).collapsed(true);

FormSection section = FormSection.of("用户配置", basicGroup, advancedGroup);
form.addSection(section);
```

---

## 18. 错误处理与异常

### 18.1 异常类型

| 场景 | 异常类型 | 说明 |
|------|---------|------|
| `new FormStateManager(null)` | NullPointerException | model 参数不能为 null |
| `form.getStateManager()` (无 model) | IllegalStateException | 需要先设置 model |
| `FormFieldFactory.register(null, ...)` | IllegalArgumentException | type 和 renderer 不能为空 |
| 验证规则执行异常 | (被捕获) | 记录日志，返回 "验证过程出错" |
| 异步验证异常 | (被捕获) | exceptionally 返回 "验证过程出错" |
| 条件谓词异常 | (被捕获) | 记录日志，跳过该规则 |
| 事件监听器异常 | (被捕获) | 记录日志，不影响其他监听器 |
| 计算属性异常 | (被捕获) | 记录日志，值保持不变 |

### 18.2 错误处理最佳实践

```java
// 1. 自定义校验器中不要抛出异常，返回错误消息
FormValidationRule.custom((rule, value) -> {
    try {
        // 验证逻辑
        return null; // 通过
    } catch (Exception e) {
        return "验证失败: " + e.getMessage(); // 返回错误消息
    }
}, "blur");

// 2. 异步校验器使用 exceptionally 处理异常
FormValidationRule.async(value ->
    CompletableFuture.supplyAsync(() -> {
        // 远程调用
        return null;
    }).exceptionally(ex -> "服务器错误: " + ex.getMessage()),
    "blur"
);

// 3. 在 dispose 时确保资源释放
try {
    form.dispose();
} finally {
    FormValidator.cancelAllDebounced();
}
```

### 18.3 常见错误场景

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| 验证不触发 | 未设置 model | 确保调用 `form.model(model)` |
| 数据绑定不生效 | FormItem 未添加到 Form | 确保 `form.addItem(item)` 在设置控件后 |
| 计算属性不更新 | 依赖字段未在 computed 之前定义 | 先 `field()` 再 `computed()` |
| 撤销无效 | 未保存快照 | 操作前调用 `pushUndoSnapshot()` |
| 内存泄漏 | 未调用 dispose | 窗口关闭时调用 `form.dispose()` |
| 跨字段验证不生效 | 使用了不带 model 的验证方法 | 使用 `validateWithModel` |

---

## 19. 性能优化与最佳实践

### 19.1 大数据量表单

- **使用栅格布局**: `columns(n)` 减少垂直滚动距离
- **延迟加载**: 对不可见的 FormGroup 使用 `collapsed(true)` 延迟渲染
- **批量更新**: 初始化多字段时使用 `model.batchUpdate()` 减少通知次数
- **避免过度验证**: 设置合适的 `trigger`（"blur" 比 "change" 更高效）

### 19.2 频繁更新场景

- **去抖验证**: 对搜索/过滤字段使用 `debounce` 属性或 `validateFieldDebounced`
- **计算属性**: 使用 `computed()` 替代手动监听多个字段的变化
- **批量更新**: 一次性修改多个字段时使用 `batchUpdate()` 包裹

### 19.3 内存管理

- **dispose 规范**: 窗口关闭时调用 `form.dispose()`，会级联释放所有 FormItem
- **监听器跟踪**: FormItem 内部自动跟踪所有绑定监听器，dispose 时统一清理
- **静态资源**: `FormValidator.cancelAllDebounced()` + `FormValidator.clearPatternCache()` 清理静态缓存
- **自动保存**: `FormStateManager.disableAutoSave()` 停止定时器

### 19.4 线程安全

- **JavaFX 线程规则**: 所有 UI 操作必须在 JavaFX Application Thread
- **异步验证**: `validateAsync` 的回调自动通过 `Platform.runLater()` 调度到 UI 线程
- **去抖验证**: 回调自动通过 `Platform.runLater()` 调度
- **自动保存**: persist 操作自动通过 `Platform.runLater()` 调度
- **全局变更回调**: 使用 `CopyOnWriteArrayList` 保证迭代安全
- **批量更新**: `batchMode` 使用 `volatile`，`batchChangedFields` 使用同步列表

### 19.5 向后兼容性

- 所有 v1.0 的 API 保持不变
- 新增功能通过可选参数和新方法提供
- 默认值与 v1.0 行为一致
- `onValidate` 和 `onSubmit` 回调保留，同时提供更强大的事件系统

---

## 20. 特殊功能详细说明

### 20.1 计算属性依赖追踪

**原理**: 调用 `computed(name, fn, deps...)` 时：
1. 创建一个内部的 `ComputedField` 对象，记录计算函数和依赖列表
2. 为该字段创建 ObjectProperty（如果不存在）并标记为只读
3. 立即执行一次计算，设置初始值
4. 为每个依赖字段的 `ObjectProperty` 添加 `ChangeListener`
5. 当任何依赖字段变化时，自动重新调用计算函数更新值

**限制**: 依赖字段必须在定义计算属性之前已通过 `field()` 定义。计算函数应避免副作用。

### 20.2 Watch 监听器

**原理**: `watch(field, handler)` 等价于 `onFieldChange(field, handler)`，为指定字段的 ObjectProperty 添加 ChangeListener。

**与计算属性的区别**: 计算属性是声明式的（自动计算值），Watch 是命令式的（执行副作用）。

### 20.3 批量更新实现原理

1. 设置 `batchMode = true`
2. 执行用户代码（多次 setFieldValue）
3. 每次 setFieldValue 时，`installChangeListener` 检测到 batchMode，只记录字段名到 `batchChangedFields`，不触发回调
4. 用户代码执行完毕（finally 块）：
   - 设置 `batchMode = false`
   - 遍历 `batchChangedFields`，为每个变更字段触发一次 `fireFieldChange`
   - 清空记录

**注意**: 批量更新期间计算属性仍会被触发更新（因为它们直接监听 ObjectProperty 的 ChangeListener），但变更通知会被延迟。

### 20.4 去抖验证

**原理**: 使用 `ScheduledExecutorService`（daemon 线程）实现。每次调用 `validateFieldDebounced` 时：
1. 取消该字段之前的定时任务
2. 安排新的延迟任务（`schedule`）
3. 延迟结束后在后台线程执行验证
4. 通过 `Platform.runLater` 将结果回调到 UI 线程

### 20.5 跨字段验证

**原理**: `FormValidationRule.equalTo("otherField", "消息")` 设置 `crossField` 属性。验证时 `FormValidator.validateSingleRule` 检测到 `crossField` 不为 null，从 FormModel 获取另一个字段的值进行比较。

**要求**: 跨字段验证需要使用 `validateWithModel` 或 `Form.validate()`（它们会传入 model 参数）。

### 20.6 主题切换动态效果

主题切换通过以下流程实现：
1. 调用 `form.theme(newTheme)` 设置 theme 属性
2. Form 的 `bindListeners` 中 theme 变更监听器被触发
3. 遍历所有 FormItem，调用 `applyTheme(newTheme)` 更新标签颜色、错误颜色等
4. 触发 `THEME_CHANGE` 事件
5. 主题不直接修改控件样式（保持 CSS 优先级），而是通过颜色属性影响标签和错误消息

### 20.7 插件扩展机制

**工作原理**:
1. 通过 `FormFieldFactory.register(type, renderer)` 注册渲染器到全局静态 Map
2. 可选注册 `FieldBinder` 用于自动绑定到 FormModel
3. 创建 FormItem 时使用 `FormFieldFactory.create(type, field, config)` 获取控件实例
4. 如有绑定器，调用 `FormFieldFactory.getBinder(type).bind(node, model, field)` 完成绑定
5. FormItem 的 `fieldType` 属性可存储类型标识，供后续查询

**类型标识**: 全部转为小写存储，匹配时不区分大小写。

---

## 附录 A: 文件清单

| 文件 | 行数 | 说明 |
|------|------|------|
| Form.java | ~947 | 表单主组件 |
| FormItem.java | ~1000 | 表单项容器 |
| FormModel.java | ~826 | 数据模型 |
| FormValidator.java | ~700 | 验证引擎 |
| FormValidationRule.java | ~650 | 验证规则 |
| FormValidationResult.java | ~341 | 验证结果 |
| FormStateManager.java | ~486 | 状态管理器 |
| FormEvent.java | ~193 | 事件系统 |
| FormFieldFactory.java | ~193 | 插件工厂 |
| FormTheme.java | ~451 | 主题配置 |
| FormSize.java | ~107 | 尺寸枚举 |
| FormLabelPosition.java | ~33 | 标签位置枚举 |
| FormBindingMode.java | ~57 | 绑定模式枚举 |
| FormGroup.java | ~210 | 分组容器 |
| FormSection.java | ~179 | 区域容器 |
| FormDemo.java | ~1606 | 完整功能演示（14个Tab） |

## 附录 B: 版本历史

### v2.0 新增特性

- 计算属性（computed）和 Watch 监听器
- 批量更新（batchUpdate）
- 跨字段验证（equalTo）和条件验证（when）
- 验证组（group）和警告级别（warningOnly）
- 枚举白名单验证（enumValues）
- 去抖验证（debounce）
- 栅格布局（columns/gutter/span）
- 6种预定义主题（Element UI/Ant Design/Naive UI 明暗各一）
- CSS 变量生成（toCssVariables）
- 完整的生命周期事件系统（14种事件类型）
- 插件扩展机制（FormFieldFactory）
- 自动保存（enableAutoSave）
- 变更历史记录（HistoryEntry）
- MINI 尺寸支持
- LAZY 绑定模式
- FormItem 描述文本、可见性控制
- FormGroup 折叠功能、验证状态跟踪
- FormSection 可见性控制、验证摘要

### v2.0 代码质量改进

- 所有组件添加完整的 Javadoc 注释
- dispose() 方法完善资源释放
- 线程安全改进（volatile/CopyOnWriteArrayList/ConcurrentHashMap）
- 空值安全（null 检查、默认值）
- 异常捕获和日志记录
- 正则表达式缓存
- CSS 样式累加修复
