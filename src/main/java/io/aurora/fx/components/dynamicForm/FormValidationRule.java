package io.aurora.fx.components.dynamicForm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 表单验证规则
 * <p>
 * 对标 Element UI 的 rules 配置，参照 async-validator 标准实现。
 * 支持 required、type、min/max、pattern、自定义校验器、异步校验、
 * 跨字段验证、条件验证、枚举白名单、验证组等多种验证规则。
 * 参考 FormsFX 提供了 intRange、doubleRange、stringLength、regex 等快捷工厂方法。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 必填规则
 * FormValidationRule.required("请输入活动名称")
 *
 * // Builder 模式组合规则
 * FormValidationRule.builder()
 *     .required(true)
 *     .min(3).max(20)
 *     .message("长度在 3 到 20 个字符")
 *     .trigger("blur")
 *     .build()
 *
 * // 预定义规则
 * FormValidationRule.email("请输入正确的邮箱地址")
 * FormValidationRule.phone("请输入正确的手机号码")
 * FormValidationRule.intRange(1, 100, "请输入 1-100 之间的整数")
 *
 * // 跨字段验证（密码确认）
 * FormValidationRule.equalTo("password", "两次密码输入不一致")
 *
 * // 枚举白名单验证
 * FormValidationRule.enumValues(Arrays.asList("A", "B", "C"), "请选择有效选项")
 *
 * // 条件验证（仅当某条件满足时才验证）
 * FormValidationRule.builder()
 *     .required(true)
 *     .message("请输入详细地址")
 *     .when(model -> Boolean.TRUE.equals(model.getFieldValue("needDelivery")))
 *     .build()
 *
 * // 自定义校验器
 * FormValidationRule.custom((rule, value) -> {
 *     if (!"expected".equals(value)) return "值不符合预期";
 *     return null;
 * }, "blur")
 *
 * // 异步校验器
 * FormValidationRule.async(value -> {
 *     return CompletableFuture.supplyAsync(() -> checkRemote(value) ? "已存在" : null);
 * }, "blur")
 *
 * // 警告级别规则（不阻止提交）
 * FormValidationRule.builder()
 *     .validator((r, v) -> v.toString().length() < 5 ? "建议至少输入5个字符" : null)
 *     .warningOnly(true)
 *     .build()
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormValidationRule {

    /** 是否必填 */
    private boolean required;

    /** 错误消息 */
    private String message;

    /** 触发方式: "blur", "change" */
    private String trigger;

    /** 字段类型: "string", "number", "boolean", "array", "email", "url", "integer", "date" */
    private String type;

    /** 最小长度/值 */
    private Integer min;

    /** 最大长度/值 */
    private Integer max;

    /** 最小数值（用于 doubleRange） */
    private Double minValue;

    /** 最大数值（用于 doubleRange） */
    private Double maxValue;

    /** 正则表达式模式 */
    private String pattern;

    /**
     * 自定义校验器（同步）
     * <p>参数: (rule, value) -> errorMessage (返回 null 表示验证通过)</p>
     */
    private BiFunction<FormValidationRule, Object, String> validator;

    /**
     * 异步校验器
     * <p>参数: value -> CompletableFuture&lt;errorMessage&gt; (返回 null 表示验证通过)</p>
     */
    private Function<Object, CompletableFuture<String>> asyncValidator;

    /** 是否为异步规则 */
    private boolean async;

    // ==================== v2.0 新增属性 ====================

    /** 跨字段引用名（用于 equalTo 等） */
    private String crossField;

    /** 枚举白名单值 */
    private List<Object> enumValues;

    /** 条件验证谓词（返回 true 时才执行验证） */
    private Predicate<FormModel> when;

    /** 验证组（用于分组验证） */
    private String group;

    /** 是否仅为警告（不阻止提交） */
    private boolean warningOnly;

    /** 去抖延迟（毫秒，0=不去抖） */
    private long debounce;

    /** 优先级（越小越先执行） */
    private int priority;

    /** 私有构造 - 通过工厂方法或 Builder 创建 */
    private FormValidationRule() {
        this.priority = 100; // 默认优先级
    }

    // ==================== 快捷工厂方法 ====================

    /**
     * 创建必填规则
     *
     * @param message 错误消息
     * @return 必填验证规则
     */
    public static FormValidationRule required(String message) {
        FormValidationRule rule = new FormValidationRule();
        rule.required = true;
        rule.message = message;
        rule.trigger = "blur";
        return rule;
    }

    /**
     * 创建必填规则（默认消息）
     *
     * @return 必填验证规则
     */
    public static FormValidationRule required() {
        return required("此字段为必填项");
    }

    /**
     * 创建邮箱验证规则
     *
     * @param message 错误消息，传 null 使用默认
     * @return 邮箱验证规则
     */
    public static FormValidationRule email(String message) {
        return builder()
                .type("email")
                .message(message != null ? message : "请输入正确的邮箱地址")
                .trigger("blur")
                .build();
    }

    /**
     * 创建 URL 验证规则
     *
     * @param message 错误消息，传 null 使用默认
     * @return URL 验证规则
     */
    public static FormValidationRule url(String message) {
        return builder()
                .type("url")
                .message(message != null ? message : "请输入正确的URL地址")
                .trigger("blur")
                .build();
    }

    /**
     * 创建手机号验证规则（中国大陆格式）
     *
     * @param message 错误消息，传 null 使用默认
     * @return 手机号验证规则
     */
    public static FormValidationRule phone(String message) {
        return builder()
                .pattern("^1[3-9]\\d{9}$")
                .message(message != null ? message : "请输入正确的手机号码")
                .trigger("blur")
                .build();
    }

    /**
     * 创建长度范围验证规则（字符串长度或集合大小）
     *
     * @param min     最小长度
     * @param max     最大长度
     * @param message 错误消息
     * @return 长度验证规则
     */
    public static FormValidationRule length(int min, int max, String message) {
        return builder()
                .min(min)
                .max(max)
                .message(message)
                .trigger("blur")
                .build();
    }

    /**
     * 创建字符串长度验证规则（FormsFX 风格快捷方法）
     *
     * @param min     最小长度
     * @param max     最大长度
     * @param message 错误消息，传 null 使用默认
     * @return 字符串长度验证规则
     */
    public static FormValidationRule stringLength(int min, int max, String message) {
        return builder()
                .type("string")
                .min(min)
                .max(max)
                .message(message != null ? message : "长度应在 " + min + " 到 " + max + " 个字符之间")
                .trigger("blur")
                .build();
    }

    /**
     * 创建整数范围验证规则（FormsFX 风格）
     *
     * @param min     最小值
     * @param max     最大值
     * @param message 错误消息，传 null 使用默认
     * @return 整数范围验证规则
     */
    public static FormValidationRule intRange(int min, int max, String message) {
        return builder()
                .type("integer")
                .minValue((double) min)
                .maxValue((double) max)
                .message(message != null ? message : "请输入 " + min + " 到 " + max + " 之间的整数")
                .trigger("blur")
                .build();
    }

    /**
     * 创建浮点数范围验证规则（FormsFX 风格）
     *
     * @param min     最小值
     * @param max     最大值
     * @param message 错误消息，传 null 使用默认
     * @return 浮点数范围验证规则
     */
    public static FormValidationRule doubleRange(double min, double max, String message) {
        return builder()
                .type("number")
                .minValue(min)
                .maxValue(max)
                .message(message != null ? message : "请输入 " + min + " 到 " + max + " 之间的数值")
                .trigger("blur")
                .build();
    }

    /**
     * 创建正则表达式验证规则
     *
     * @param pattern 正则表达式
     * @param message 错误消息
     * @return 正则验证规则
     */
    public static FormValidationRule pattern(String pattern, String message) {
        return builder()
                .pattern(pattern)
                .message(message)
                .trigger("blur")
                .build();
    }

    /**
     * 创建自定义校验规则（同步）
     *
     * @param validator 校验函数 (rule, value) -> errorMessage
     * @param trigger   触发方式
     * @return 自定义验证规则
     */
    public static FormValidationRule custom(BiFunction<FormValidationRule, Object, String> validator,
                                            String trigger) {
        return builder()
                .validator(validator)
                .trigger(trigger != null ? trigger : "blur")
                .build();
    }

    /**
     * 创建异步校验规则（如远程唯一性校验、API 调用等）
     *
     * @param asyncValidator 异步校验函数 value -> CompletableFuture&lt;errorMessage&gt;
     * @param trigger        触发方式
     * @return 异步验证规则
     */
    public static FormValidationRule async(Function<Object, CompletableFuture<String>> asyncValidator,
                                           String trigger) {
        return builder()
                .asyncValidator(asyncValidator)
                .trigger(trigger != null ? trigger : "blur")
                .build();
    }

    // ==================== v2.0 新增工厂方法 ====================

    /**
     * 创建跨字段等值验证规则（密码确认等场景）
     * <p>
     * 参考 Ant Design 的 dependencies 机制和 Element UI 的自定义密码确认验证
     * </p>
     *
     * @param otherFieldName 需要匹配的另一个字段名
     * @param message        错误消息
     * @return 跨字段验证规则
     */
    public static FormValidationRule equalTo(String otherFieldName, String message) {
        FormValidationRule rule = new FormValidationRule();
        rule.crossField = otherFieldName;
        rule.message = message != null ? message : "两次输入不一致";
        rule.trigger = "blur";
        return rule;
    }

    /**
     * 创建枚举白名单验证规则
     * <p>
     * 值必须在指定列表内，参考 async-validator 的 enum 类型
     * </p>
     *
     * @param values  合法值列表
     * @param message 错误消息
     * @return 枚举验证规则
     */
    public static FormValidationRule enumValues(List<Object> values, String message) {
        FormValidationRule rule = new FormValidationRule();
        rule.enumValues = values != null ? new ArrayList<>(values) : Collections.emptyList();
        rule.message = message != null ? message : "请选择有效选项";
        rule.trigger = "change";
        return rule;
    }

    /**
     * 创建身份证号验证规则（中国大陆18位）
     *
     * @param message 错误消息，传 null 使用默认
     * @return 身份证验证规则
     */
    public static FormValidationRule idCard(String message) {
        return builder()
                .pattern("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$")
                .message(message != null ? message : "请输入正确的身份证号码")
                .trigger("blur")
                .build();
    }

    /**
     * 创建 IP 地址验证规则
     *
     * @param message 错误消息，传 null 使用默认
     * @return IP 验证规则
     */
    public static FormValidationRule ipAddress(String message) {
        return builder()
                .pattern("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
                .message(message != null ? message : "请输入正确的IP地址")
                .trigger("blur")
                .build();
    }

    /**
     * 创建仅数字验证规则
     *
     * @param message 错误消息，传 null 使用默认
     * @return 仅数字验证规则
     */
    public static FormValidationRule numeric(String message) {
        return builder()
                .pattern("^\\d+(\\.\\d+)?$")
                .message(message != null ? message : "请输入数字")
                .trigger("blur")
                .build();
    }

    /**
     * 创建仅字母验证规则
     *
     * @param message 错误消息，传 null 使用默认
     * @return 仅字母验证规则
     */
    public static FormValidationRule alpha(String message) {
        return builder()
                .pattern("^[a-zA-Z]+$")
                .message(message != null ? message : "请输入字母")
                .trigger("blur")
                .build();
    }

    /**
     * 创建字母数字验证规则
     *
     * @param message 错误消息，传 null 使用默认
     * @return 字母数字验证规则
     */
    public static FormValidationRule alphaNumeric(String message) {
        return builder()
                .pattern("^[a-zA-Z0-9]+$")
                .message(message != null ? message : "请输入字母或数字")
                .trigger("blur")
                .build();
    }

    // ==================== Builder ====================

    /**
     * 创建规则构建器
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Getters ====================

    public boolean isRequired() { return required; }
    public String getMessage() { return message; }
    public String getTrigger() { return trigger; }
    public String getType() { return type; }
    public Integer getMin() { return min; }
    public Integer getMax() { return max; }
    public Double getMinValue() { return minValue; }
    public Double getMaxValue() { return maxValue; }
    public String getPattern() { return pattern; }
    public BiFunction<FormValidationRule, Object, String> getValidator() { return validator; }
    public Function<Object, CompletableFuture<String>> getAsyncValidator() { return asyncValidator; }
    public boolean isAsync() { return async; }

    // v2.0 新增 Getters
    public String getCrossField() { return crossField; }
    public List<Object> getEnumValues() { return enumValues; }
    public Predicate<FormModel> getWhen() { return when; }
    public String getGroup() { return group; }
    public boolean isWarningOnly() { return warningOnly; }
    public long getDebounce() { return debounce; }
    public int getPriority() { return priority; }

    // ==================== Builder Class ====================

    /**
     * 验证规则构建器
     * <p>支持链式调用构建复杂验证规则，同时支持同步和异步校验器、
     * 跨字段验证、条件验证、枚举白名单等高级功能</p>
     */
    public static class Builder {
        private final FormValidationRule rule = new FormValidationRule();

        public Builder required(boolean required) {
            rule.required = required;
            return this;
        }

        public Builder message(String message) {
            rule.message = message;
            return this;
        }

        public Builder trigger(String trigger) {
            rule.trigger = trigger;
            return this;
        }

        public Builder type(String type) {
            rule.type = type;
            return this;
        }

        public Builder min(int min) {
            rule.min = min;
            return this;
        }

        public Builder max(int max) {
            rule.max = max;
            return this;
        }

        public Builder minValue(double minValue) {
            rule.minValue = minValue;
            return this;
        }

        public Builder maxValue(double maxValue) {
            rule.maxValue = maxValue;
            return this;
        }

        public Builder pattern(String pattern) {
            rule.pattern = pattern;
            return this;
        }

        public Builder validator(BiFunction<FormValidationRule, Object, String> validator) {
            rule.validator = validator;
            return this;
        }

        /**
         * 设置异步校验器
         *
         * @param asyncValidator value -> CompletableFuture&lt;errorMessage&gt;
         * @return this
         */
        public Builder asyncValidator(Function<Object, CompletableFuture<String>> asyncValidator) {
            rule.asyncValidator = asyncValidator;
            rule.async = true;
            return this;
        }

        // ==================== v2.0 新增 Builder 方法 ====================

        /**
         * 设置跨字段引用（用于 equalTo 等校验）
         *
         * @param fieldName 需要比较的另一个字段名
         * @return this
         */
        public Builder crossField(String fieldName) {
            rule.crossField = fieldName;
            return this;
        }

        /**
         * 设置枚举白名单
         *
         * @param values 合法值列表
         * @return this
         */
        public Builder enumValues(List<Object> values) {
            rule.enumValues = values != null ? new ArrayList<>(values) : null;
            return this;
        }

        /**
         * 设置条件验证谓词
         * <p>仅当谓词返回 true 时才执行此规则的验证</p>
         *
         * @param predicate 条件谓词，参数为当前 FormModel
         * @return this
         */
        public Builder when(Predicate<FormModel> predicate) {
            rule.when = predicate;
            return this;
        }

        /**
         * 设置验证组
         * <p>用于分组验证，只验证指定组的规则</p>
         *
         * @param group 组名
         * @return this
         */
        public Builder group(String group) {
            rule.group = group;
            return this;
        }

        /**
         * 设置为仅警告模式（不阻止提交）
         *
         * @param warningOnly true 表示仅为警告
         * @return this
         */
        public Builder warningOnly(boolean warningOnly) {
            rule.warningOnly = warningOnly;
            return this;
        }

        /**
         * 设置去抖延迟
         *
         * @param millis 延迟毫秒数
         * @return this
         */
        public Builder debounce(long millis) {
            rule.debounce = millis;
            return this;
        }

        /**
         * 设置优先级（越小越先执行）
         *
         * @param priority 优先级
         * @return this
         */
        public Builder priority(int priority) {
            rule.priority = priority;
            return this;
        }

        /**
         * 构建验证规则
         *
         * @return 构建完成的验证规则
         */
        public FormValidationRule build() {
            if (rule.message == null && !rule.async) {
                rule.message = buildDefaultMessage();
            }
            return rule;
        }

        private String buildDefaultMessage() {
            if (rule.required) return "此字段为必填项";
            if (rule.crossField != null) return "两次输入不一致";
            if (rule.enumValues != null) return "请选择有效选项";
            if (rule.minValue != null && rule.maxValue != null)
                return "数值应在 " + rule.minValue + " 到 " + rule.maxValue + " 之间";
            if (rule.min != null && rule.max != null)
                return "长度应在 " + rule.min + " 到 " + rule.max + " 之间";
            if (rule.min != null) return "长度不能小于 " + rule.min;
            if (rule.max != null) return "长度不能大于 " + rule.max;
            if (rule.pattern != null) return "格式不正确";
            if (rule.type != null) return "类型不匹配";
            return "验证失败";
        }
    }
}
