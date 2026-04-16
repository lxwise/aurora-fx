package io.aurora.fx.components.dynamicForm;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 表单数据模型
 * <p>
 * 持有表单所有字段的 Observable 属性，支持数据变更监听、重置为初始值、
 * 类型安全取值、快照/恢复、计算属性、Watch 监听器、批量更新等高级操作。
 * 每个字段以 {@link ObjectProperty} 包装，方便与 JavaFX 控件进行双向绑定。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * FormModel model = new FormModel()
 *     .field("name", "")
 *     .field("region", "")
 *     .field("delivery", false)
 *     .field("type", FXCollections.observableArrayList());
 *
 * // 计算属性（依赖其他字段自动更新）
 * model.computed("fullName", m ->
 *     m.getString("firstName") + " " + m.getString("lastName"),
 *     "firstName", "lastName"
 * );
 *
 * // Watch 监听器（深度观察变化）
 * model.watch("name", (oldVal, newVal) -> {
 *     System.out.println("name 从 " + oldVal + " 变为 " + newVal);
 * });
 *
 * // 批量更新（抑制中间变更通知）
 * model.batchUpdate(() -> {
 *     model.setFieldValue("name", "Alice");
 *     model.setFieldValue("age", 25);
 * });
 *
 * // 快捷类型安全取值
 * String name = model.getString("name");
 * int age = model.getInt("age", 0);
 * boolean delivery = model.getBoolean("delivery", false);
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormModel {

    private static final Logger LOGGER = Logger.getLogger(FormModel.class.getName());

    /** 字段属性映射 */
    private final Map<String, ObjectProperty<Object>> fieldProperties = new LinkedHashMap<>();

    /** 字段初始值映射（用于 reset） */
    private final Map<String, Object> initialValues = new LinkedHashMap<>();

    /** 字段元数据（label、type hint 等） */
    private final Map<String, Map<String, Object>> fieldMetadata = new LinkedHashMap<>();

    /** 全局字段变更监听器 (fieldName, oldValue, newValue) */
    private final List<FieldChangeCallback> globalChangeCallbacks = new CopyOnWriteArrayList<>();

    /** 字段级变更监听器 fieldName -> callbacks */
    private final Map<String, List<BiConsumer<Object, Object>>> fieldChangeCallbacks = new LinkedHashMap<>();

    /** 已安装的 ChangeListener 映射，用于 dispose 清理 */
    private final Map<String, ChangeListener<Object>> installedListeners = new LinkedHashMap<>();

    // ==================== v2.0 新增 ====================

    /** 计算属性定义：computedName -> {function, dependencies[]} */
    private final Map<String, ComputedField> computedFields = new LinkedHashMap<>();

    /** 批量更新标志 */
    private volatile boolean batchMode = false;

    /** 批量更新期间的变更记录（使用同步列表避免并发问题） */
    private final List<String> batchChangedFields = Collections.synchronizedList(new ArrayList<>());

    /** 字段只读标记 */
    private final Set<String> readOnlyFields = new LinkedHashSet<>();

    // ==================== 工厂方法 ====================

    /**
     * 从 Map 创建模型（每个 entry 作为一个字段）
     *
     * @param data 字段名 -> 初始值映射
     * @return 新的 FormModel 实例
     */
    public static FormModel fromMap(Map<String, Object> data) {
        FormModel model = new FormModel();
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                model.field(entry.getKey(), entry.getValue());
            }
        }
        return model;
    }

    // ==================== 字段定义 ====================

    /**
     * 定义一个表单字段（链式调用）
     *
     * @param name         字段名称
     * @param initialValue 初始值
     * @return this
     */
    public FormModel field(String name, Object initialValue) {
        if (name == null || name.isEmpty()) {
            LOGGER.warning("字段名不能为空");
            return this;
        }
        Object storedInitial = cloneValue(initialValue);
        ObjectProperty<Object> prop = new SimpleObjectProperty<>(initialValue);
        fieldProperties.put(name, prop);
        initialValues.put(name, storedInitial);
        // 安装变更监听器
        installChangeListener(name, prop);
        return this;
    }

    /**
     * 定义字段并附带元数据（链式调用）
     *
     * @param name         字段名称
     * @param initialValue 初始值
     * @param label        字段标签（用于 UI 显示和错误提示）
     * @return this
     */
    public FormModel field(String name, Object initialValue, String label) {
        field(name, initialValue);
        setFieldMeta(name, "label", label);
        return this;
    }

    // ==================== 计算属性 ====================

    /**
     * 定义计算属性（依赖其他字段自动更新）
     * <p>
     * 参考 Vue 的 computed 属性设计，当依赖字段值变更时自动重新计算。
     * </p>
     *
     * @param name         计算属性名称
     * @param computeFn    计算函数（参数为当前 FormModel）
     * @param dependencies 依赖的字段名称列表
     * @return this
     */
    public FormModel computed(String name, Function<FormModel, Object> computeFn, String... dependencies) {
        if (name == null || computeFn == null) {
            LOGGER.warning("计算属性名和计算函数不能为空");
            return this;
        }

        // 创建属性（如果不存在）
        if (!fieldProperties.containsKey(name)) {
            field(name, null);
        }
        readOnlyFields.add(name);

        ComputedField cf = new ComputedField(computeFn, dependencies);
        computedFields.put(name, cf);

        // 初始计算
        updateComputed(name, cf);

        // 为每个依赖字段安装监听器
        for (String dep : dependencies) {
            ObjectProperty<Object> depProp = fieldProperties.get(dep);
            if (depProp != null) {
                ChangeListener<Object> listener = (obs, oldV, newV) -> updateComputed(name, cf);
                depProp.addListener(listener);
                // 使用特殊前缀来跟踪计算属性的监听器
                installedListeners.put("computed:" + name + ":" + dep, listener);
            }
        }

        return this;
    }

    /**
     * 更新计算属性的值
     */
    private void updateComputed(String name, ComputedField cf) {
        try {
            Object newValue = cf.computeFn.apply(this);
            ObjectProperty<Object> prop = fieldProperties.get(name);
            if (prop != null) {
                prop.set(newValue);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "计算属性更新异常: " + name, e);
        }
    }

    // ==================== Watch 监听器 ====================

    /**
     * Watch 一个字段的变化（类似 Vue watch）
     *
     * @param fieldName 字段名
     * @param handler   变更处理器 (oldValue, newValue)
     * @return this
     */
    public FormModel watch(String fieldName, BiConsumer<Object, Object> handler) {
        return onFieldChange(fieldName, handler);
    }

    /**
     * Watch 多个字段的变化
     *
     * @param handler    变更处理器 (fieldName, oldValue, newValue)
     * @param fieldNames 要监听的字段名
     * @return this
     */
    public FormModel watchFields(FieldChangeCallback handler, String... fieldNames) {
        if (handler == null || fieldNames == null) return this;
        for (String fn : fieldNames) {
            onFieldChange(fn, (oldV, newV) -> handler.onFieldChanged(fn, oldV, newV));
        }
        return this;
    }

    // ==================== 批量更新 ====================

    /**
     * 批量更新字段值（抑制中间变更通知，完成后统一触发）
     * <p>
     * 参考 Vue 的 nextTick / React 的 batch update 机制，
     * 在批量更新期间所有变更通知会被延迟到 runnable 执行完毕后一次性触发。
     * </p>
     *
     * @param runnable 批量更新操作
     */
    public void batchUpdate(Runnable runnable) {
        if (runnable == null) return;
        batchMode = true;
        batchChangedFields.clear();
        try {
            runnable.run();
        } finally {
            batchMode = false;
            // 触发所有变更通知
            for (String field : batchChangedFields) {
                Object value = getFieldValue(field);
                fireFieldChange(field, null, value);
            }
            batchChangedFields.clear();
        }
    }

    // ==================== 读写操作 ====================

    /**
     * 设置字段值
     *
     * @param name  字段名
     * @param value 新值
     */
    public void setFieldValue(String name, Object value) {
        if (readOnlyFields.contains(name)) {
            LOGGER.warning("无法设置只读字段（计算属性）: " + name);
            return;
        }
        ObjectProperty<Object> prop = fieldProperties.get(name);
        if (prop != null) {
            prop.set(value);
        } else {
            LOGGER.warning("字段不存在: " + name);
        }
    }

    /**
     * 批量设置字段值
     *
     * @param values 字段名 -> 值的映射
     */
    public void setFieldValues(Map<String, Object> values) {
        if (values == null) return;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            setFieldValue(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取字段值
     *
     * @param name 字段名
     * @return 字段当前值，字段不存在时返回 null
     */
    public Object getFieldValue(String name) {
        ObjectProperty<Object> prop = fieldProperties.get(name);
        return prop != null ? prop.get() : null;
    }

    /**
     * 获取字段值（类型安全）
     *
     * @param name 字段名
     * @param type 期望类型
     * @param <T>  返回类型
     * @return 字段值，类型不匹配或字段不存在时返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(String name, Class<T> type) {
        Object value = getFieldValue(name);
        if (value == null) return null;
        if (type.isInstance(value)) return type.cast(value);
        try {
            if (type == String.class) return (T) String.valueOf(value);
            if (type == Boolean.class && value instanceof String) return (T) Boolean.valueOf((String) value);
            if (type == Integer.class && value instanceof String) return (T) Integer.valueOf((String) value);
            if (type == Double.class && value instanceof String) return (T) Double.valueOf((String) value);
            if (type == Long.class && value instanceof String) return (T) Long.valueOf((String) value);
        } catch (Exception e) {
            LOGGER.warning("类型转换失败: " + name + " -> " + type.getSimpleName());
        }
        return null;
    }

    // ==================== 快捷类型取值方法 ====================

    /**
     * 获取 String 类型字段值
     *
     * @param name 字段名
     * @return 字符串值，字段不存在或为 null 时返回空字符串
     */
    public String getString(String name) {
        Object v = getFieldValue(name);
        return v != null ? String.valueOf(v) : "";
    }

    /**
     * 获取 int 类型字段值
     *
     * @param name         字段名
     * @param defaultValue 默认值
     * @return 整数值
     */
    public int getInt(String name, int defaultValue) {
        Object v = getFieldValue(name);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch (NumberFormatException e) { /* ignore */ }
        }
        return defaultValue;
    }

    /**
     * 获取 double 类型字段值
     *
     * @param name         字段名
     * @param defaultValue 默认值
     * @return 浮点数值
     */
    public double getDouble(String name, double defaultValue) {
        Object v = getFieldValue(name);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
            try { return Double.parseDouble((String) v); } catch (NumberFormatException e) { /* ignore */ }
        }
        return defaultValue;
    }

    /**
     * 获取 boolean 类型字段值
     *
     * @param name         字段名
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        Object v = getFieldValue(name);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) return Boolean.parseBoolean((String) v);
        return defaultValue;
    }

    /**
     * 获取 List 类型字段值
     *
     * @param name 字段名
     * @param <T>  列表元素类型
     * @return 列表值，字段不存在或类型不匹配时返回空列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String name) {
        Object v = getFieldValue(name);
        if (v instanceof List) return (List<T>) v;
        return Collections.emptyList();
    }

    /**
     * 获取字段的 Observable 属性（用于监听和绑定）
     *
     * @param name 字段名
     * @return 字段属性，若不存在则自动创建
     */
    public ObjectProperty<Object> fieldProperty(String name) {
        return fieldProperties.computeIfAbsent(name, k -> {
            ObjectProperty<Object> prop = new SimpleObjectProperty<>();
            initialValues.putIfAbsent(k, null);
            installChangeListener(k, prop);
            return prop;
        });
    }

    // ==================== 字段变更回调 ====================

    /**
     * 注册全局字段变更回调（任意字段变更时触发）
     *
     * @param callback (fieldName, oldValue, newValue)
     * @return this
     */
    public FormModel onAnyFieldChange(FieldChangeCallback callback) {
        if (callback != null) {
            globalChangeCallbacks.add(callback);
        }
        return this;
    }

    /**
     * 注册单字段变更回调
     *
     * @param fieldName 字段名
     * @param callback  (oldValue, newValue)
     * @return this
     */
    public FormModel onFieldChange(String fieldName, BiConsumer<Object, Object> callback) {
        if (fieldName != null && callback != null) {
            fieldChangeCallbacks.computeIfAbsent(fieldName, k -> new CopyOnWriteArrayList<>()).add(callback);
        }
        return this;
    }

    /**
     * 移除全局变更回调
     */
    public void removeGlobalChangeCallback(FieldChangeCallback callback) {
        globalChangeCallbacks.remove(callback);
    }

    /**
     * 移除指定字段的所有变更回调
     *
     * @param fieldName 字段名
     */
    public void removeFieldChangeCallbacks(String fieldName) {
        if (fieldName != null) {
            fieldChangeCallbacks.remove(fieldName);
        }
    }

    /**
     * 全局字段变更回调接口
     */
    @FunctionalInterface
    public interface FieldChangeCallback {
        void onFieldChanged(String fieldName, Object oldValue, Object newValue);
    }

    // ==================== 字段元数据 ====================

    /**
     * 设置字段元数据
     *
     * @param fieldName 字段名
     * @param key       元数据键（如 "label", "typeHint", "description"）
     * @param value     元数据值
     * @return this
     */
    public FormModel setFieldMeta(String fieldName, String key, Object value) {
        fieldMetadata.computeIfAbsent(fieldName, k -> new LinkedHashMap<>()).put(key, value);
        return this;
    }

    /**
     * 获取字段元数据
     *
     * @param fieldName 字段名
     * @param key       元数据键
     * @return 元数据值，不存在时返回 null
     */
    public Object getFieldMeta(String fieldName, String key) {
        Map<String, Object> meta = fieldMetadata.get(fieldName);
        return meta != null ? meta.get(key) : null;
    }

    /**
     * 获取字段标签（快捷方法）
     *
     * @param fieldName 字段名
     * @return 字段标签，未设置时返回字段名
     */
    public String getFieldLabel(String fieldName) {
        Object label = getFieldMeta(fieldName, "label");
        return label != null ? label.toString() : fieldName;
    }

    // ==================== 只读字段 ====================

    /**
     * 设置字段为只读
     *
     * @param fieldName 字段名
     * @return this
     */
    public FormModel readOnly(String fieldName) {
        readOnlyFields.add(fieldName);
        return this;
    }

    /**
     * 检查字段是否为只读
     *
     * @param fieldName 字段名
     * @return true 表示只读
     */
    public boolean isReadOnly(String fieldName) {
        return readOnlyFields.contains(fieldName);
    }

    // ==================== 快照与恢复 ====================

    /**
     * 创建当前模型值的快照
     *
     * @return 字段名 -> 值的快照映射
     */
    public Map<String, Object> snapshot() {
        return new LinkedHashMap<>(toMap());
    }

    /**
     * 从快照恢复模型值
     *
     * @param snapshotData 之前通过 {@link #snapshot()} 获取的快照
     */
    public void restoreSnapshot(Map<String, Object> snapshotData) {
        if (snapshotData == null) return;
        for (Map.Entry<String, Object> entry : snapshotData.entrySet()) {
            if (fieldProperties.containsKey(entry.getKey()) && !readOnlyFields.contains(entry.getKey())) {
                setFieldValue(entry.getKey(), entry.getValue());
            }
        }
    }

    // ==================== 重置操作 ====================

    /**
     * 重置所有字段为初始值
     */
    @SuppressWarnings("unchecked")
    public void reset() {
        for (Map.Entry<String, Object> entry : initialValues.entrySet()) {
            if (readOnlyFields.contains(entry.getKey())) continue;
            ObjectProperty<Object> prop = fieldProperties.get(entry.getKey());
            if (prop != null) {
                resetFieldInternal(prop, entry.getValue());
            }
        }
    }

    /**
     * 重置指定字段为初始值
     *
     * @param name 字段名
     */
    public void resetField(String name) {
        if (readOnlyFields.contains(name)) return;
        Object initVal = initialValues.get(name);
        ObjectProperty<Object> prop = fieldProperties.get(name);
        if (prop != null) {
            resetFieldInternal(prop, initVal);
        }
    }

    @SuppressWarnings("unchecked")
    private void resetFieldInternal(ObjectProperty<Object> prop, Object initVal) {
        if (initVal instanceof List) {
            Object current = prop.get();
            List<?> initList = (List<?>) initVal;
            if (current instanceof ObservableList) {
                ObservableList<Object> obsList = (ObservableList<Object>) current;
                obsList.clear();
                obsList.addAll((Collection<Object>) initList);
            } else {
                prop.set(FXCollections.observableArrayList((Collection<Object>) initList));
            }
        } else {
            prop.set(initVal);
        }
    }

    /**
     * 更新字段的初始值（影响后续 reset 行为）
     *
     * @param name     字段名
     * @param newInitial 新的初始值
     */
    public void updateInitialValue(String name, Object newInitial) {
        if (fieldProperties.containsKey(name)) {
            initialValues.put(name, cloneValue(newInitial));
        }
    }

    // ==================== 查询操作 ====================

    /**
     * 获取所有字段名
     *
     * @return 不可修改的字段名集合
     */
    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(fieldProperties.keySet());
    }

    /**
     * 获取字段数量
     *
     * @return 字段总数
     */
    public int getFieldCount() {
        return fieldProperties.size();
    }

    /**
     * 检查字段是否存在
     *
     * @param name 字段名
     * @return 存在返回 true
     */
    public boolean hasField(String name) {
        return fieldProperties.containsKey(name);
    }

    /**
     * 将模型数据导出为 Map
     *
     * @return 字段名 -> 值的映射
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, ObjectProperty<Object>> entry : fieldProperties.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    /**
     * 获取初始值映射（不可修改）
     *
     * @return 字段名 -> 初始值
     */
    public Map<String, Object> getInitialValues() {
        return Collections.unmodifiableMap(initialValues);
    }

    /**
     * 检查指定字段当前值是否与初始值不同
     *
     * @param name 字段名
     * @return true 表示字段值已被修改
     */
    public boolean isFieldChanged(String name) {
        Object current = getFieldValue(name);
        Object initial = initialValues.get(name);
        return !Objects.equals(current, initial);
    }

    /**
     * 获取所有已修改的字段名
     *
     * @return 已修改字段的名称集合
     */
    public Set<String> getChangedFields() {
        Set<String> changed = new LinkedHashSet<>();
        for (String field : fieldProperties.keySet()) {
            if (isFieldChanged(field)) {
                changed.add(field);
            }
        }
        return changed;
    }

    /**
     * 移除一个字段
     *
     * @param name 字段名
     * @return this
     */
    public FormModel removeField(String name) {
        if (name == null) return this;
        ChangeListener<Object> listener = installedListeners.remove(name);
        ObjectProperty<Object> prop = fieldProperties.remove(name);
        if (prop != null && listener != null) {
            prop.removeListener(listener);
        }
        initialValues.remove(name);
        fieldMetadata.remove(name);
        fieldChangeCallbacks.remove(name);
        readOnlyFields.remove(name);
        computedFields.remove(name);
        return this;
    }

    // ==================== 内部方法 ====================

    /**
     * 为字段属性安装变更监听器（触发回调通知）
     */
    private void installChangeListener(String fieldName, ObjectProperty<Object> prop) {
        // 避免重复安装
        if (installedListeners.containsKey(fieldName)) {
            prop.removeListener(installedListeners.get(fieldName));
        }
        ChangeListener<Object> listener = (obs, oldV, newV) -> {
            if (batchMode) {
                if (!batchChangedFields.contains(fieldName)) {
                    batchChangedFields.add(fieldName);
                }
            } else {
                fireFieldChange(fieldName, oldV, newV);
            }
        };
        prop.addListener(listener);
        installedListeners.put(fieldName, listener);
    }

    /**
     * 触发字段变更通知
     */
    private void fireFieldChange(String fieldName, Object oldValue, Object newValue) {
        // 全局回调
        for (FieldChangeCallback cb : globalChangeCallbacks) {
            try {
                cb.onFieldChanged(fieldName, oldValue, newValue);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "全局变更回调异常: " + fieldName, e);
            }
        }
        // 字段级回调
        List<BiConsumer<Object, Object>> callbacks = fieldChangeCallbacks.get(fieldName);
        if (callbacks != null) {
            for (BiConsumer<Object, Object> cb : callbacks) {
                try {
                    cb.accept(oldValue, newValue);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "字段变更回调异常: " + fieldName, e);
                }
            }
        }
    }

    private Object cloneValue(Object value) {
        if (value instanceof ObservableList) {
            return FXCollections.observableArrayList((ObservableList<?>) value);
        }
        if (value instanceof List) {
            return new ArrayList<>((List<?>) value);
        }
        return value; // 不可变类型（String, Number, Boolean 等）无需克隆
    }

    // ==================== 内部类 ====================

    /**
     * 计算属性定义
     */
    private static class ComputedField {
        final Function<FormModel, Object> computeFn;
        final String[] dependencies;

        ComputedField(Function<FormModel, Object> computeFn, String[] dependencies) {
            this.computeFn = computeFn;
            this.dependencies = dependencies;
        }
    }

    // ==================== 资源释放 ====================

    /**
     * 移除所有变更监听器和回调，释放资源
     */
    public void dispose() {
        // 移除属性监听器
        for (Map.Entry<String, ChangeListener<Object>> entry : installedListeners.entrySet()) {
            String key = entry.getKey();
            // 计算属性监听器的 key 格式为 "computed:computedName:depField"，取 depField
            String fieldName;
            if (key.startsWith("computed:")) {
                String[] parts = key.split(":", 3);
                fieldName = parts.length >= 3 ? parts[2] : key;
            } else {
                fieldName = key;
            }
            ObjectProperty<Object> prop = fieldProperties.get(fieldName);
            if (prop != null) {
                prop.removeListener(entry.getValue());
            }
        }
        installedListeners.clear();
        globalChangeCallbacks.clear();
        fieldChangeCallbacks.clear();
        computedFields.clear();
        readOnlyFields.clear();
        batchChangedFields.clear();
    }
}
