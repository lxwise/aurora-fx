package io.aurora.fx.components.dynamicForm;

import java.util.*;

/**
 * 表单验证结果
 * <p>
 * 封装表单验证的完整结果，包含整体验证状态、每个字段的错误/警告消息列表，
 * 以及字段级别的验证状态枚举。支持错误和警告两种消息级别。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * FormValidationResult result = form.validate();
 * if (!result.isValid()) {
 *     // 获取第一条错误
 *     String firstError = result.getFirstError();
 *     // 获取指定字段的错误
 *     List<String> nameErrors = result.getFieldErrors("name");
 *     // 获取所有警告
 *     Map<String, List<String>> warnings = result.getWarnings();
 *     // 获取字段验证状态
 *     FieldStatus status = result.getFieldStatus("name"); // ERROR / WARNING / SUCCESS
 * }
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormValidationResult {

    /**
     * 字段验证状态枚举
     */
    public enum FieldStatus {
        /** 验证通过 */
        SUCCESS,
        /** 有警告信息 */
        WARNING,
        /** 验证失败 */
        ERROR,
        /** 验证中（用于异步验证） */
        VALIDATING,
        /** 未验证 */
        PENDING
    }

    /** 是否验证通过 */
    private final boolean valid;

    /** 字段错误映射: 字段名 -> 错误消息列表 */
    private final Map<String, List<String>> errors;

    /** 字段警告映射: 字段名 -> 警告消息列表 */
    private final Map<String, List<String>> warnings;

    /** 字段验证状态映射 */
    private final Map<String, FieldStatus> fieldStatuses;

    /** 验证耗时（毫秒） */
    private long validationDuration;

    // ==================== 构造方法 ====================

    public FormValidationResult(boolean valid, Map<String, List<String>> errors) {
        this(valid, errors, Collections.emptyMap());
    }

    public FormValidationResult(boolean valid,
                                Map<String, List<String>> errors,
                                Map<String, List<String>> warnings) {
        this.valid = valid;
        this.errors = errors != null ? Collections.unmodifiableMap(errors) : Collections.emptyMap();
        this.warnings = warnings != null ? Collections.unmodifiableMap(warnings) : Collections.emptyMap();
        this.fieldStatuses = buildFieldStatuses();
    }

    // ==================== 错误查询 ====================

    /** 是否验证通过（无错误） */
    public boolean isValid() { return valid; }

    /** 获取所有错误 */
    public Map<String, List<String>> getErrors() { return errors; }

    /**
     * 获取指定字段的错误消息列表
     *
     * @param field 字段名
     * @return 错误消息列表，无错误时返回空列表
     */
    public List<String> getFieldErrors(String field) {
        return errors.getOrDefault(field, Collections.emptyList());
    }

    /**
     * 获取指定字段的第一条错误消息
     *
     * @param field 字段名
     * @return 第一条错误消息，无错误时返回 null
     */
    public String getFirstError(String field) {
        List<String> fieldErrors = getFieldErrors(field);
        return fieldErrors.isEmpty() ? null : fieldErrors.get(0);
    }

    /**
     * 获取所有字段中的第一条错误消息
     *
     * @return 第一条错误消息，全部通过时返回 null
     */
    public String getFirstError() {
        for (List<String> fieldErrors : errors.values()) {
            if (!fieldErrors.isEmpty()) return fieldErrors.get(0);
        }
        return null;
    }

    /**
     * 获取错误字段数量
     *
     * @return 有错误的字段数
     */
    public int getErrorFieldCount() {
        return errors.size();
    }

    /**
     * 获取所有错误字段名
     *
     * @return 错误字段名集合
     */
    public Set<String> getErrorFieldNames() {
        return errors.keySet();
    }

    /**
     * 获取所有错误消息的扁平列表
     *
     * @return 所有错误消息列表
     */
    public List<String> getAllErrors() {
        List<String> all = new ArrayList<>();
        for (List<String> fieldErrors : errors.values()) {
            all.addAll(fieldErrors);
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * 获取错误消息总数
     *
     * @return 所有字段的错误消息总数
     */
    public int getTotalErrorCount() {
        int count = 0;
        for (List<String> fieldErrors : errors.values()) {
            count += fieldErrors.size();
        }
        return count;
    }

    // ==================== 警告查询 ====================

    /** 获取所有警告 */
    public Map<String, List<String>> getWarnings() { return warnings; }

    /**
     * 获取指定字段的警告消息列表
     *
     * @param field 字段名
     * @return 警告消息列表，无警告时返回空列表
     */
    public List<String> getFieldWarnings(String field) {
        return warnings.getOrDefault(field, Collections.emptyList());
    }

    /**
     * 是否有警告信息
     *
     * @return true 表示有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 获取警告字段数量
     */
    public int getWarningFieldCount() {
        return warnings.size();
    }

    // ==================== 字段状态查询 ====================

    /**
     * 获取指定字段的验证状态
     *
     * @param field 字段名
     * @return 字段验证状态
     */
    public FieldStatus getFieldStatus(String field) {
        return fieldStatuses.getOrDefault(field, FieldStatus.PENDING);
    }

    /**
     * 获取所有字段的验证状态
     *
     * @return 字段名 -> 状态映射
     */
    public Map<String, FieldStatus> getFieldStatuses() {
        return Collections.unmodifiableMap(fieldStatuses);
    }

    /**
     * 检查指定字段是否有错误
     *
     * @param field 字段名
     * @return true 表示有错误
     */
    public boolean hasFieldError(String field) {
        return errors.containsKey(field) && !errors.get(field).isEmpty();
    }

    // ==================== 验证耗时 ====================

    /**
     * 获取验证耗时
     *
     * @return 验证耗时（毫秒）
     */
    public long getValidationDuration() { return validationDuration; }

    /**
     * 设置验证耗时
     *
     * @param duration 耗时（毫秒）
     * @return this
     */
    public FormValidationResult validationDuration(long duration) {
        this.validationDuration = duration;
        return this;
    }

    // ==================== 合并 ====================

    /**
     * 合并两个验证结果
     *
     * @param other 另一个验证结果
     * @return 合并后的结果
     */
    public FormValidationResult merge(FormValidationResult other) {
        if (other == null) return this;
        Map<String, List<String>> mergedErrors = new LinkedHashMap<>(this.errors);
        for (Map.Entry<String, List<String>> entry : other.errors.entrySet()) {
            mergedErrors.merge(entry.getKey(), entry.getValue(), (a, b) -> {
                List<String> combined = new ArrayList<>(a);
                combined.addAll(b);
                return combined;
            });
        }
        Map<String, List<String>> mergedWarnings = new LinkedHashMap<>(this.warnings);
        for (Map.Entry<String, List<String>> entry : other.warnings.entrySet()) {
            mergedWarnings.merge(entry.getKey(), entry.getValue(), (a, b) -> {
                List<String> combined = new ArrayList<>(a);
                combined.addAll(b);
                return combined;
            });
        }
        boolean mergedValid = this.valid && other.valid;
        return new FormValidationResult(mergedValid, mergedErrors, mergedWarnings);
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建验证成功的结果
     */
    public static FormValidationResult success() {
        return new FormValidationResult(true, Collections.emptyMap());
    }

    /**
     * 创建验证失败的结果
     *
     * @param errors 字段错误映射
     */
    public static FormValidationResult failure(Map<String, List<String>> errors) {
        return new FormValidationResult(false, errors);
    }

    /**
     * 创建带警告的验证结果
     *
     * @param valid    是否通过
     * @param errors   错误映射
     * @param warnings 警告映射
     */
    public static FormValidationResult of(boolean valid,
                                          Map<String, List<String>> errors,
                                          Map<String, List<String>> warnings) {
        return new FormValidationResult(valid, errors, warnings);
    }

    // ==================== 内部方法 ====================

    private Map<String, FieldStatus> buildFieldStatuses() {
        Map<String, FieldStatus> statuses = new LinkedHashMap<>();
        // 先设置警告状态
        for (String field : warnings.keySet()) {
            if (!warnings.get(field).isEmpty()) {
                statuses.put(field, FieldStatus.WARNING);
            }
        }
        // 错误状态覆盖警告
        for (String field : errors.keySet()) {
            if (!errors.get(field).isEmpty()) {
                statuses.put(field, FieldStatus.ERROR);
            }
        }
        return statuses;
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        if (valid && warnings.isEmpty()) {
            return "FormValidationResult{valid=true}";
        }
        StringBuilder sb = new StringBuilder("FormValidationResult{valid=").append(valid);
        if (!errors.isEmpty()) sb.append(", errors=").append(errors);
        if (!warnings.isEmpty()) sb.append(", warnings=").append(warnings);
        if (validationDuration > 0) sb.append(", duration=").append(validationDuration).append("ms");
        sb.append('}');
        return sb.toString();
    }
}
