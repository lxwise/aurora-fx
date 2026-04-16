package io.aurora.fx.components.dynamicForm;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Form 表单组件
 * <p>
 * 对标 Element UI 的 el-form、Ant Design 的 Form 和 Naive UI 的 NForm 组件，
 * 提供表单数据收集、验证和提交功能。支持行内布局、栅格布局、标签对齐方式配置、
 * 完整的表单验证系统、动态增删表单项、统一尺寸控制、主题定制、
 * 生命周期事件和插件扩展机制等企业级功能。
 * </p>
 *
 * <h3>典型表单</h3>
 * <pre>{@code
 * FormModel model = new FormModel()
 *     .field("name", "")
 *     .field("region", "");
 *
 * Form form = new Form()
 *     .model(model)
 *     .labelWidth(120)
 *     .addItem(new FormItem("活动名称", "name", new TextField()))
 *     .addItem(new FormItem("活动区域", "region", comboBox));
 * root.getChildren().add(form.getNode());
 * }</pre>
 *
 * <h3>栅格布局</h3>
 * <pre>{@code
 * Form form = new Form().model(model).columns(2).gutter(16)
 *     .addItem(new FormItem("姓名", "name", nameField).span(12))
 *     .addItem(new FormItem("年龄", "age", ageField).span(12));
 * }</pre>
 *
 * <h3>事件监听</h3>
 * <pre>{@code
 * form.on(FormEvent.Type.FIELD_CHANGE, event -> {
 *     System.out.println(event.getFieldName() + " changed");
 * });
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class Form {

    private static final Logger LOGGER = Logger.getLogger(Form.class.getName());

    // ==================== 核心属性 ====================

    /** 数据模型 */
    private FormModel model;

    /** 验证规则: 字段名 -> 规则列表 */
    private Map<String, List<FormValidationRule>> rules = new LinkedHashMap<>();

    /** 是否行内表单 */
    private final BooleanProperty inline = new SimpleBooleanProperty(false);

    /** 标签位置 */
    private final ObjectProperty<FormLabelPosition> labelPosition =
            new SimpleObjectProperty<>(FormLabelPosition.RIGHT);

    /** 标签宽度 */
    private final DoubleProperty labelWidth = new SimpleDoubleProperty(80);

    /** 标签后缀 */
    private final StringProperty labelSuffix = new SimpleStringProperty("");

    /** 是否显示验证错误信息 */
    private final BooleanProperty showMessage = new SimpleBooleanProperty(true);

    /** 是否禁用整个表单 */
    private final BooleanProperty disabled = new SimpleBooleanProperty(false);

    /** 尺寸 */
    private final ObjectProperty<FormSize> size = new SimpleObjectProperty<>(FormSize.DEFAULT);

    /** 主题 */
    private final ObjectProperty<FormTheme> theme = new SimpleObjectProperty<>(FormTheme.DEFAULT);

    /** 绑定模式 */
    private final ObjectProperty<FormBindingMode> bindingMode =
            new SimpleObjectProperty<>(FormBindingMode.CONTINUOUS);

    // ==================== v2.0 布局属性 ====================

    /** 栅格列数（0 表示不使用栅格） */
    private final IntegerProperty columns = new SimpleIntegerProperty(0);

    /** 栅格列间距 */
    private final DoubleProperty gutter = new SimpleDoubleProperty(16);

    // ==================== UI ====================

    /** 根容器 */
    private final StackPane rootPane = new StackPane();

    /** 表单项布局容器 */
    private Pane layoutPane;

    /** 表单项列表 */
    private final List<FormItem> items = new ArrayList<>();

    /** 分组列表 */
    private final List<FormGroup> groups = new ArrayList<>();

    /** 区域列表 */
    private final List<FormSection> sections = new ArrayList<>();

    /** 状态管理器（懒初始化） */
    private FormStateManager stateManager;

    /** 是否已释放 */
    private volatile boolean disposed = false;

    // ==================== 事件系统 ====================

    /** 验证完成回调（保留兼容性） */
    private Consumer<FormValidationResult> onValidate;

    /** 提交回调（保留兼容性） */
    private Consumer<Map<String, Object>> onSubmit;

    /** 事件监听器映射 */
    private final Map<FormEvent.Type, List<FormEvent.Listener>> eventListeners = new LinkedHashMap<>();

    // ==================== 监听器存储 ====================

    private final List<Object[]> propertyListeners = new ArrayList<>();

    /** 模型字段变更监听器引用（用于 dispose 清理） */
    private FormModel.FieldChangeCallback modelChangeCallback;

    // ==================== 构造方法 ====================

    public Form() {
        initLayout();
        bindListeners();
    }

    // ==================== 初始化 ====================

    private void initLayout() {
        rootPane.getStyleClass().add("el-form");
        rebuildLayout();
    }

    private void rebuildLayout() {
        rootPane.getChildren().clear();

        if (inline.get()) {
            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(16);
            flowPane.setVgap(10);
            flowPane.setAlignment(Pos.CENTER_LEFT);
            flowPane.setPadding(new Insets(10));
            layoutPane = flowPane;
        } else if (columns.get() > 0) {
            // 栅格布局
            GridPane gridPane = new GridPane();
            gridPane.setHgap(gutter.get());
            gridPane.setVgap(theme.get() != null ? theme.get().getItemSpacing() : 18);
            gridPane.setPadding(new Insets(10));
            // 设置列约束
            int cols = columns.get();
            for (int i = 0; i < cols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100.0 / cols);
                cc.setHgrow(Priority.ALWAYS);
                gridPane.getColumnConstraints().add(cc);
            }
            layoutPane = gridPane;
        } else {
            VBox vbox = new VBox();
            vbox.setSpacing(theme.get() != null ? theme.get().getItemSpacing() : 18);
            vbox.setPadding(new Insets(10));
            layoutPane = vbox;
        }

        // 重新添加所有 FormItem
        readdItemsToLayout();
        rootPane.getChildren().add(layoutPane);
    }

    private void readdItemsToLayout() {
        if (layoutPane instanceof GridPane) {
            GridPane grid = (GridPane) layoutPane;
            grid.getChildren().clear();
            int cols = columns.get();
            int row = 0, col = 0;
            for (FormItem item : items) {
                if (!item.isVisible()) continue;
                int itemSpan = Math.min(item.getSpan(), 24);
                int gridSpan = Math.max(1, (int) Math.round((double) itemSpan / 24 * cols));
                if (col + gridSpan > cols) {
                    row++;
                    col = 0;
                }
                GridPane.setConstraints(item.getNode(), col, row, gridSpan, 1);
                grid.getChildren().add(item.getNode());
                col += gridSpan;
                if (col >= cols) {
                    row++;
                    col = 0;
                }
            }
        } else {
            layoutPane.getChildren().clear();
            for (FormItem item : items) {
                layoutPane.getChildren().add(item.getNode());
            }
        }
    }

    private void bindListeners() {
        // inline 切换 → 重建布局
        trackPropertyListener(inline, (obs, oldV, newV) -> rebuildLayout());

        // columns 变更 → 重建布局
        trackPropertyListener(columns, (obs, oldV, newV) -> rebuildLayout());

        // gutter 变更 → 重建布局
        trackPropertyListener(gutter, (obs, oldV, newV) -> rebuildLayout());

        // 标签位置 → 传播到所有未自定义的 FormItem
        trackPropertyListener(labelPosition, (obs, oldV, newV) -> {
            for (FormItem item : items) {
                if (item.labelPositionProperty().get() == null) {
                    item.buildLayout(newV);
                }
            }
        });

        // 标签宽度 → 传播
        trackPropertyListener(labelWidth, (obs, oldV, newV) -> {
            for (FormItem item : items) {
                item.buildLayout(item.getEffectiveLabelPosition());
            }
        });

        // 标签后缀 → 传播
        trackPropertyListener(labelSuffix, (obs, oldV, newV) -> {
            for (FormItem item : items) {
                item.updateLabelText();
            }
        });

        // 尺寸 → 传播
        trackPropertyListener(size, (obs, oldV, newV) -> {
            for (FormItem item : items) {
                item.applySize(item.getEffectiveSize());
            }
        });

        // 主题 → 传播
        trackPropertyListener(theme, (obs, oldV, newV) -> {
            for (FormItem item : items) {
                item.applyTheme(newV);
            }
            fireEvent(FormEvent.builder(FormEvent.Type.THEME_CHANGE).source(this).build());
        });

        // 禁用 → 传播
        trackPropertyListener(disabled, (obs, oldV, newV) -> {
            for (FormItem item : items) {
                item.applyDisabled(newV);
            }
        });

        // showMessage → 传播
        trackPropertyListener(showMessage, (obs, oldV, newV) -> {
            for (FormItem item : items) {
                item.showMessageProperty().set(newV);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> void trackPropertyListener(ObservableValue<T> property, ChangeListener<? super T> listener) {
        property.addListener(listener);
        propertyListeners.add(new Object[]{property, listener});
    }

    // ==================== 表单项管理 ====================

    /**
     * 添加表单项
     */
    public Form addItem(FormItem item) {
        if (disposed || item == null) return this;
        item.setParentForm(this);
        items.add(item);
        if (layoutPane != null) {
            if (layoutPane instanceof GridPane) {
                readdItemsToLayout();
            } else {
                layoutPane.getChildren().add(item.getNode());
            }
        }
        fireEvent(FormEvent.builder(FormEvent.Type.ITEM_ADDED)
                .source(this).fieldName(item.getProp()).build());
        return this;
    }

    /**
     * 添加多个表单项
     */
    public Form addItems(FormItem... formItems) {
        for (FormItem item : formItems) {
            addItem(item);
        }
        return this;
    }

    /**
     * 在指定位置插入表单项
     */
    public Form insertItem(int index, FormItem item) {
        if (disposed || item == null) return this;
        item.setParentForm(this);
        int safeIndex = Math.max(0, Math.min(index, items.size()));
        items.add(safeIndex, item);
        if (layoutPane != null) {
            if (layoutPane instanceof GridPane) {
                readdItemsToLayout();
            } else {
                layoutPane.getChildren().add(safeIndex, item.getNode());
            }
        }
        return this;
    }

    /**
     * 移除表单项
     */
    public Form removeItem(FormItem item) {
        if (disposed || item == null) return this;
        items.remove(item);
        if (layoutPane != null) {
            if (layoutPane instanceof GridPane) {
                readdItemsToLayout();
            } else {
                layoutPane.getChildren().remove(item.getNode());
            }
        }
        fireEvent(FormEvent.builder(FormEvent.Type.ITEM_REMOVED)
                .source(this).fieldName(item.getProp()).build());
        item.dispose();
        return this;
    }

    /**
     * 根据 prop 名称移除表单项
     */
    public Form removeItem(String prop) {
        if (disposed) return this;
        FormItem item = findItem(prop);
        if (item != null) removeItem(item);
        return this;
    }

    /**
     * 根据 prop 查找表单项
     */
    public FormItem findItem(String prop) {
        if (prop == null) return null;
        for (FormItem item : items) {
            if (prop.equals(item.getProp())) {
                return item;
            }
        }
        return null;
    }

    public List<FormItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getItemCount() {
        return items.size();
    }

    // ==================== 分组/区域管理 ====================

    public Form addGroup(FormGroup group) {
        if (disposed || group == null) return this;
        groups.add(group);
        for (FormItem item : group.getItems()) {
            item.setParentForm(this);
            items.add(item);
        }
        Node groupNode = group.buildNode(theme.get() != null ? theme.get().getItemSpacing() : 18);
        if (layoutPane != null) {
            layoutPane.getChildren().add(groupNode);
        }
        return this;
    }

    public Form addSection(FormSection section) {
        if (disposed || section == null) return this;
        sections.add(section);
        for (FormItem item : section.getAllItems()) {
            item.setParentForm(this);
            items.add(item);
        }
        Node sectionNode = section.buildNode(15, theme.get() != null ? theme.get().getItemSpacing() : 18);
        if (layoutPane != null) {
            layoutPane.getChildren().add(sectionNode);
        }
        return this;
    }

    public List<FormGroup> getGroups() { return Collections.unmodifiableList(groups); }
    public List<FormSection> getSections() { return Collections.unmodifiableList(sections); }

    // ==================== 事件系统 ====================

    /**
     * 注册事件监听器
     * <p>参考 Ant Design 的 Form 事件和 Vue 的 emit 机制</p>
     *
     * @param type     事件类型
     * @param listener 监听器
     * @return this
     */
    public Form on(FormEvent.Type type, FormEvent.Listener listener) {
        if (type != null && listener != null) {
            eventListeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
        }
        return this;
    }

    /**
     * 移除事件监听器
     */
    public Form off(FormEvent.Type type, FormEvent.Listener listener) {
        List<FormEvent.Listener> listeners = eventListeners.get(type);
        if (listeners != null) {
            listeners.remove(listener);
        }
        return this;
    }

    /**
     * 触发事件
     */
    private void fireEvent(FormEvent event) {
        if (event == null) return;
        List<FormEvent.Listener> listeners = eventListeners.get(event.getType());
        if (listeners != null) {
            for (FormEvent.Listener listener : listeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "事件监听器异常: " + event.getType(), e);
                }
            }
        }
    }

    // ==================== 状态管理 ====================

    public FormStateManager getStateManager() {
        if (stateManager == null) {
            if (model == null) {
                throw new IllegalStateException("需要先设置 model 才能使用状态管理");
            }
            stateManager = new FormStateManager(model);
        }
        return stateManager;
    }

    public void persist() {
        if (disposed || model == null) return;
        getStateManager().persist();
        fireEvent(FormEvent.builder(FormEvent.Type.PERSISTED)
                .source(this).formData(model.toMap()).build());
    }

    public void rollback() {
        if (disposed || model == null) return;
        getStateManager().rollback();
        clearValidate();
        fireEvent(FormEvent.builder(FormEvent.Type.ROLLED_BACK)
                .source(this).formData(model.toMap()).build());
    }

    public boolean isDirty() {
        if (model == null) return false;
        return getStateManager().isDirty();
    }

    public ReadOnlyBooleanProperty dirtyProperty() {
        return getStateManager().dirtyProperty();
    }

    // ==================== 验证 ====================

    /**
     * 验证整个表单
     */
    public boolean validate() {
        if (disposed || model == null) return true;

        // 触发 BEFORE_VALIDATE 事件
        FormEvent beforeEvent = FormEvent.builder(FormEvent.Type.BEFORE_VALIDATE).source(this).build();
        fireEvent(beforeEvent);
        if (beforeEvent.isCancelled()) return true;

        Map<String, List<FormValidationRule>> mergedRules = getMergedRules();
        FormValidationResult result = FormValidator.validateWithModel(model, mergedRules, null);

        // 更新所有 FormItem 的错误状态
        updateItemErrors(result);

        if (onValidate != null) {
            try {
                onValidate.accept(result);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "验证回调异常", e);
            }
        }

        // 触发 AFTER_VALIDATE 事件
        fireEvent(FormEvent.builder(FormEvent.Type.AFTER_VALIDATE)
                .source(this).validationResult(result).build());

        return result.isValid();
    }

    /**
     * 验证整个表单（带回调）
     */
    public void validate(Consumer<Boolean> callback) {
        boolean result = validate();
        if (callback != null) {
            try {
                callback.accept(result);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "验证回调异常", e);
            }
        }
    }

    /**
     * 验证指定字段
     */
    public boolean validateField(String prop) {
        return validateField(prop, null);
    }

    /**
     * 验证指定字段（带触发条件过滤）
     */
    boolean validateField(String prop, String trigger) {
        if (disposed || model == null || prop == null) return true;

        List<FormValidationRule> fieldRules = getFieldRules(prop);
        if (fieldRules.isEmpty()) return true;

        // 按 trigger 过滤
        List<FormValidationRule> applicableRules;
        if (trigger != null) {
            applicableRules = new ArrayList<>();
            for (FormValidationRule rule : fieldRules) {
                if (trigger.equals(rule.getTrigger()) || rule.getTrigger() == null) {
                    applicableRules.add(rule);
                }
            }
        } else {
            applicableRules = fieldRules;
        }

        if (applicableRules.isEmpty()) return true;

        Object value = model.getFieldValue(prop);
        List<String> errors = FormValidator.validateField(prop, value, applicableRules, model);

        // 更新对应 FormItem 的状态
        FormItem item = findItem(prop);
        if (item != null) {
            if (!errors.isEmpty()) {
                item.errorProperty().set(true);
                item.errorMessageProperty().set(errors.get(0));
            } else {
                item.errorProperty().set(false);
                item.errorMessageProperty().set(null);
            }
        }

        // 触发 FIELD_VALIDATED 事件
        Map<String, List<String>> errorMap = errors.isEmpty()
                ? Collections.emptyMap()
                : Collections.singletonMap(prop, errors);
        fireEvent(FormEvent.builder(FormEvent.Type.FIELD_VALIDATED)
                .source(this).fieldName(prop)
                .validationResult(errors.isEmpty()
                        ? FormValidationResult.success()
                        : FormValidationResult.failure(errorMap))
                .build());

        return errors.isEmpty();
    }

    /**
     * 验证指定的多个字段
     *
     * @param props 字段名列表
     * @return 验证结果
     */
    public FormValidationResult validateFields(String... props) {
        if (disposed || model == null || props == null) return FormValidationResult.success();
        Map<String, List<FormValidationRule>> mergedRules = getMergedRules();
        FormValidationResult result = FormValidator.validateFields(model, mergedRules, props);
        // 更新相关 FormItem 的错误状态
        Set<String> propSet = new HashSet<>(Arrays.asList(props));
        for (FormItem item : items) {
            if (item.getProp() != null && propSet.contains(item.getProp())) {
                List<String> errors = result.getFieldErrors(item.getProp());
                item.errorProperty().set(!errors.isEmpty());
                item.errorMessageProperty().set(!errors.isEmpty() ? errors.get(0) : null);
            }
        }
        return result;
    }

    /**
     * 重置所有表单字段为初始值，并清除验证消息
     */
    public void resetFields() {
        if (disposed) return;
        fireEvent(FormEvent.builder(FormEvent.Type.BEFORE_RESET).source(this).build());
        if (model != null) {
            model.reset();
        }
        clearValidate();
        fireEvent(FormEvent.builder(FormEvent.Type.AFTER_RESET).source(this).build());
    }

    /**
     * 清除所有验证消息
     */
    public void clearValidate() {
        if (disposed) return;
        for (FormItem item : items) {
            item.errorProperty().set(false);
            item.errorMessageProperty().set(null);
        }
    }

    /**
     * 清除指定字段的验证消息
     */
    public void clearValidate(String... props) {
        if (disposed) return;
        Set<String> propSet = new HashSet<>(Arrays.asList(props));
        for (FormItem item : items) {
            if (item.getProp() != null && propSet.contains(item.getProp())) {
                item.errorProperty().set(false);
                item.errorMessageProperty().set(null);
            }
        }
    }

    /**
     * 滚动到指定字段并聚焦
     */
    public void scrollToField(String prop) {
        if (disposed) return;
        FormItem item = findItem(prop);
        if (item == null || item.getNode() == null) return;

        item.getNode().requestFocus();

        Node parent = rootPane.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                ScrollPane scroll = (ScrollPane) parent;
                Node node = item.getNode();
                double y = node.getBoundsInParent().getMinY();
                double contentHeight = ((Region) scroll.getContent()).getHeight();
                if (contentHeight > 0) {
                    scroll.setVvalue(y / contentHeight);
                }
                break;
            }
            parent = parent.getParent();
        }
    }

    /**
     * 异步验证整个表单
     */
    public void validateAsync(Consumer<FormValidationResult> callback) {
        if (disposed || model == null) {
            if (callback != null) callback.accept(FormValidationResult.success());
            return;
        }

        Map<String, List<FormValidationRule>> mergedRules = getMergedRules();
        FormValidator.validateAsync(model, mergedRules).thenAccept(result -> {
            javafx.application.Platform.runLater(() -> {
                updateItemErrors(result);
                if (callback != null) {
                    try {
                        callback.accept(result);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "异步验证回调异常", e);
                    }
                }
                fireEvent(FormEvent.builder(FormEvent.Type.AFTER_VALIDATE)
                        .source(this).validationResult(result).build());
            });
        }).exceptionally(ex -> {
            LOGGER.log(Level.WARNING, "异步验证失败", ex);
            return null;
        });
    }

    /**
     * 提交表单
     */
    public void submit() {
        if (disposed) return;

        // 触发 BEFORE_SUBMIT 事件
        FormEvent beforeEvent = FormEvent.builder(FormEvent.Type.BEFORE_SUBMIT).source(this).build();
        fireEvent(beforeEvent);
        if (beforeEvent.isCancelled()) return;

        if (validate() && onSubmit != null) {
            try {
                Map<String, Object> data = model != null ? model.toMap() : Collections.emptyMap();
                onSubmit.accept(data);
                fireEvent(FormEvent.builder(FormEvent.Type.SUBMIT)
                        .source(this).formData(data).build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "提交回调异常", e);
            }
        }
    }

    /**
     * 获取表单数据快照
     *
     * @return 字段名 -> 值映射
     */
    public Map<String, Object> getFormData() {
        return model != null ? model.toMap() : Collections.emptyMap();
    }

    // ==================== 规则管理 ====================

    private List<FormValidationRule> getFieldRules(String prop) {
        List<FormValidationRule> result = new ArrayList<>();
        if (rules != null) {
            List<FormValidationRule> formRules = rules.get(prop);
            if (formRules != null) result.addAll(formRules);
        }
        FormItem item = findItem(prop);
        if (item != null && item.getRules() != null) {
            result.addAll(item.getRules());
        }
        return result;
    }

    private Map<String, List<FormValidationRule>> getMergedRules() {
        Map<String, List<FormValidationRule>> merged = new LinkedHashMap<>(rules != null ? rules : Collections.emptyMap());
        for (FormItem item : items) {
            if (item.getProp() != null && item.getRules() != null && !item.getRules().isEmpty()) {
                merged.merge(item.getProp(), item.getRules(), (existing, itemRules) -> {
                    List<FormValidationRule> combined = new ArrayList<>(existing);
                    combined.addAll(itemRules);
                    return combined;
                });
            }
        }
        return merged;
    }

    private void updateItemErrors(FormValidationResult result) {
        for (FormItem item : items) {
            String prop = item.getProp();
            if (prop != null) {
                List<String> errors = result.getFieldErrors(prop);
                item.errorProperty().set(!errors.isEmpty());
                item.errorMessageProperty().set(!errors.isEmpty() ? errors.get(0) : null);
            }
        }
    }

    public Form addRule(String prop, FormValidationRule rule) {
        if (disposed) return this;
        rules.computeIfAbsent(prop, k -> new ArrayList<>()).add(rule);
        return this;
    }

    public Form addRules(String prop, FormValidationRule... ruleArray) {
        if (disposed) return this;
        List<FormValidationRule> list = rules.computeIfAbsent(prop, k -> new ArrayList<>());
        Collections.addAll(list, ruleArray);
        return this;
    }

    public Form removeRules(String prop) {
        if (disposed) return this;
        rules.remove(prop);
        FormItem item = findItem(prop);
        if (item != null) {
            item.rules(null);
            item.requiredProperty().set(false);
        }
        clearValidate(prop);
        return this;
    }

    // ==================== 链式 API ====================

    public Form model(FormModel model) {
        // 清理之前模型的监听器
        if (this.model != null && modelChangeCallback != null) {
            this.model.removeGlobalChangeCallback(modelChangeCallback);
            modelChangeCallback = null;
        }
        this.model = model;
        // 安装模型字段变更监听器用于触发事件
        if (model != null) {
            modelChangeCallback = (fieldName, oldValue, newValue) -> {
                fireEvent(FormEvent.builder(FormEvent.Type.FIELD_CHANGE)
                        .source(this).fieldName(fieldName)
                        .oldValue(oldValue).newValue(newValue).build());
            };
            model.onAnyFieldChange(modelChangeCallback);
        }
        return this;
    }
    public Form rules(Map<String, List<FormValidationRule>> rules) {
        this.rules = rules != null ? rules : new LinkedHashMap<>(); return this;
    }
    public Form inline(boolean inline) { this.inline.set(inline); return this; }
    public Form labelPosition(FormLabelPosition pos) { this.labelPosition.set(pos); return this; }
    public Form labelWidth(double width) { this.labelWidth.set(width); return this; }
    public Form labelSuffix(String suffix) { this.labelSuffix.set(suffix); return this; }
    public Form showMessage(boolean show) { this.showMessage.set(show); return this; }
    public Form disabled(boolean disabled) { this.disabled.set(disabled); return this; }
    public Form size(FormSize size) { this.size.set(size); return this; }
    public Form theme(FormTheme theme) { this.theme.set(theme); return this; }
    public Form bindingMode(FormBindingMode mode) { this.bindingMode.set(mode); return this; }
    public Form columns(int columns) { this.columns.set(columns); return this; }
    public Form gutter(double gutter) { this.gutter.set(gutter); return this; }
    public Form onValidate(Consumer<FormValidationResult> callback) { this.onValidate = callback; return this; }
    public Form onSubmit(Consumer<Map<String, Object>> callback) { this.onSubmit = callback; return this; }

    // ==================== Getters ====================

    public Node getNode() { return rootPane; }
    public FormModel getModel() { return model; }
    public Map<String, List<FormValidationRule>> getRules() { return rules; }
    public boolean isInline() { return inline.get(); }
    public BooleanProperty inlineProperty() { return inline; }
    public FormLabelPosition getLabelPosition() { return labelPosition.get(); }
    public ObjectProperty<FormLabelPosition> labelPositionProperty() { return labelPosition; }
    public double getLabelWidth() { return labelWidth.get(); }
    public DoubleProperty labelWidthProperty() { return labelWidth; }
    public String getLabelSuffix() { return labelSuffix.get(); }
    public StringProperty labelSuffixProperty() { return labelSuffix; }
    public boolean getShowMessage() { return showMessage.get(); }
    public BooleanProperty showMessageProperty() { return showMessage; }
    public FormSize getSize() { return size.get(); }
    public ObjectProperty<FormSize> sizeProperty() { return size; }
    public FormTheme getTheme() { return theme.get(); }
    public ObjectProperty<FormTheme> themeProperty() { return theme; }
    public boolean isDisabled() { return disabled.get(); }
    public BooleanProperty disabledProperty() { return disabled; }
    public FormBindingMode getBindingMode() { return bindingMode.get(); }
    public ObjectProperty<FormBindingMode> bindingModeProperty() { return bindingMode; }
    public int getColumns() { return columns.get(); }
    public IntegerProperty columnsProperty() { return columns; }
    public double getGutter() { return gutter.get(); }
    public DoubleProperty gutterProperty() { return gutter; }

    // ==================== 资源释放 ====================

    @SuppressWarnings("unchecked")
    public void dispose() {
        if (disposed) return;
        disposed = true;

        fireEvent(FormEvent.builder(FormEvent.Type.DISPOSED).source(this).build());

        // 移除模型变更监听
        if (model != null && modelChangeCallback != null) {
            model.removeGlobalChangeCallback(modelChangeCallback);
        }

        // 释放状态管理器
        if (stateManager != null) {
            stateManager.dispose();
        }

        // 移除所有属性监听器
        for (Object[] entry : propertyListeners) {
            try {
                ObservableValue<Object> prop = (ObservableValue<Object>) entry[0];
                ChangeListener<Object> listener = (ChangeListener<Object>) entry[1];
                prop.removeListener(listener);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "移除属性监听器异常", e);
            }
        }
        propertyListeners.clear();

        // 释放所有 FormItem
        for (FormItem item : items) {
            item.dispose();
        }
        items.clear();

        // 清理事件监听器
        eventListeners.clear();

        if (layoutPane != null) {
            layoutPane.getChildren().clear();
        }
        rootPane.getChildren().clear();
        model = null;
        rules = null;
        onValidate = null;
        onSubmit = null;
    }
}
