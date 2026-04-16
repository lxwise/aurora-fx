package io.aurora.fx.components.dynamicForm;

import java.util.Map;

/**
 * 表单生命周期事件
 * <p>
 * 定义表单组件的各种生命周期事件类型，支持事件监听和拦截。
 * 参考 Ant Design 的 Form 事件体系和 Vue 的生命周期钩子设计。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * form.on(FormEvent.Type.BEFORE_VALIDATE, event -> {
 *     System.out.println("验证即将开始，字段: " + event.getFieldName());
 * });
 *
 * form.on(FormEvent.Type.FIELD_CHANGE, event -> {
 *     System.out.println(event.getFieldName() + " 从 " + event.getOldValue() + " 变为 " + event.getNewValue());
 * });
 *
 * form.on(FormEvent.Type.SUBMIT, event -> {
 *     Map<String, Object> data = event.getFormData();
 *     // 提交到后端
 * });
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 2.0
 */
public class FormEvent {

    /**
     * 表单事件类型枚举
     */
    public enum Type {
        /** 字段值变更 */
        FIELD_CHANGE,

        /** 验证开始前（可取消） */
        BEFORE_VALIDATE,

        /** 验证完成后 */
        AFTER_VALIDATE,

        /** 字段验证完成 */
        FIELD_VALIDATED,

        /** 表单提交前（可取消） */
        BEFORE_SUBMIT,

        /** 表单提交成功 */
        SUBMIT,

        /** 表单重置前 */
        BEFORE_RESET,

        /** 表单重置后 */
        AFTER_RESET,

        /** 表单项添加 */
        ITEM_ADDED,

        /** 表单项移除 */
        ITEM_REMOVED,

        /** 主题变更 */
        THEME_CHANGE,

        /** 表单数据持久化 */
        PERSISTED,

        /** 表单数据回滚 */
        ROLLED_BACK,

        /** 表单释放 */
        DISPOSED
    }

    /** 事件类型 */
    private final Type type;

    /** 相关字段名（可能为 null） */
    private final String fieldName;

    /** 旧值（用于 FIELD_CHANGE） */
    private final Object oldValue;

    /** 新值（用于 FIELD_CHANGE） */
    private final Object newValue;

    /** 表单数据快照（用于 SUBMIT 等） */
    private final Map<String, Object> formData;

    /** 验证结果（用于 AFTER_VALIDATE / FIELD_VALIDATED） */
    private final FormValidationResult validationResult;

    /** 是否已被取消（用于可取消事件） */
    private boolean cancelled = false;

    /** 事件来源 Form */
    private final Form source;

    // ==================== 构造方法 ====================

    private FormEvent(Builder builder) {
        this.type = builder.type;
        this.fieldName = builder.fieldName;
        this.oldValue = builder.oldValue;
        this.newValue = builder.newValue;
        this.formData = builder.formData;
        this.validationResult = builder.validationResult;
        this.source = builder.source;
    }

    // ==================== Getters ====================

    public Type getType() { return type; }
    public String getFieldName() { return fieldName; }
    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
    public Map<String, Object> getFormData() { return formData; }
    public FormValidationResult getValidationResult() { return validationResult; }
    public boolean isCancelled() { return cancelled; }
    public Form getSource() { return source; }

    /**
     * 取消此事件（仅对 BEFORE_VALIDATE、BEFORE_SUBMIT 有效）
     */
    public void cancel() {
        this.cancelled = true;
    }

    // ==================== Builder ====================

    public static Builder builder(Type type) {
        return new Builder(type);
    }

    /**
     * 表单事件构建器
     */
    public static class Builder {
        private final Type type;
        private String fieldName;
        private Object oldValue;
        private Object newValue;
        private Map<String, Object> formData;
        private FormValidationResult validationResult;
        private Form source;

        Builder(Type type) {
            this.type = type;
        }

        public Builder fieldName(String fieldName) { this.fieldName = fieldName; return this; }
        public Builder oldValue(Object oldValue) { this.oldValue = oldValue; return this; }
        public Builder newValue(Object newValue) { this.newValue = newValue; return this; }
        public Builder formData(Map<String, Object> formData) { this.formData = formData; return this; }
        public Builder validationResult(FormValidationResult result) { this.validationResult = result; return this; }
        public Builder source(Form source) { this.source = source; return this; }

        public FormEvent build() {
            return new FormEvent(this);
        }
    }

    // ==================== 事件监听器接口 ====================

    /**
     * 表单事件监听器
     */
    @FunctionalInterface
    public interface Listener {
        /**
         * 处理表单事件
         *
         * @param event 表单事件
         */
        void onEvent(FormEvent event);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FormEvent{type=").append(type);
        if (fieldName != null) sb.append(", field=").append(fieldName);
        if (newValue != null) sb.append(", newValue=").append(newValue);
        sb.append('}');
        return sb.toString();
    }
}
