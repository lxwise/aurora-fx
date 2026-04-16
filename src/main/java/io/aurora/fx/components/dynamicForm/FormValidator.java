package io.aurora.fx.components.dynamicForm;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 表单验证引擎
 * <p>
 * 参照 async-validator 标准实现，支持 required、type、min/max、minValue/maxValue、
 * pattern、自定义校验器、异步校验、跨字段验证、条件验证、枚举白名单、验证组、
 * 去抖验证等多种验证能力。提供整体验证、单字段验证、分组验证和异步验证四种模式。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 同步整体验证
 * FormValidationResult result = FormValidator.validate(model, rules);
 *
 * // 单字段验证
 * List<String> errors = FormValidator.validateField("name", value, fieldRules);
 *
 * // 异步验证
 * FormValidator.validateAsync(model, rules).thenAccept(result -> { ... });
 *
 * // 分组验证
 * FormValidationResult result = FormValidator.validateGroup(model, rules, "step1");
 *
 * // 跨字段验证
 * FormValidationResult result = FormValidator.validateWithModel(model, rules);
 *
 * // 去抖验证
 * FormValidator.validateFieldDebounced("email", value, rules, 300, result -> { ... });
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormValidator {

    private static final Logger LOGGER = Logger.getLogger(FormValidator.class.getName());

    /** 预编译正则缓存，避免重复编译（线程安全） */
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    /** 去抖任务映射：field -> scheduledFuture */
    private static final Map<String, ScheduledFuture<?>> DEBOUNCE_TASKS = new ConcurrentHashMap<>();

    /** 去抖调度器 */
    private static final ScheduledExecutorService DEBOUNCE_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "form-validator-debounce");
                t.setDaemon(true);
                return t;
            });

    private FormValidator() {} // 工具类不可实例化

    // ==================== 同步 API ====================

    /**
     * 验证整个表单模型（同步，仅执行同步规则）
     *
     * @param model 表单数据模型
     * @param rules 验证规则映射（字段名 -> 规则列表）
     * @return 验证结果
     */
    public static FormValidationResult validate(FormModel model,
                                                Map<String, List<FormValidationRule>> rules) {
        return validateWithModel(model, rules, null);
    }

    /**
     * 验证整个表单模型（支持跨字段验证和条件验证）
     *
     * @param model 表单数据模型
     * @param rules 验证规则映射
     * @param group 验证组（null 表示验证所有规则）
     * @return 验证结果
     */
    public static FormValidationResult validateWithModel(FormModel model,
                                                         Map<String, List<FormValidationRule>> rules,
                                                         String group) {
        if (model == null || rules == null || rules.isEmpty()) {
            return FormValidationResult.success();
        }

        long startTime = System.currentTimeMillis();
        Map<String, List<String>> allErrors = new LinkedHashMap<>();
        Map<String, List<String>> allWarnings = new LinkedHashMap<>();
        boolean allValid = true;

        for (Map.Entry<String, List<FormValidationRule>> entry : rules.entrySet()) {
            String field = entry.getKey();
            List<FormValidationRule> fieldRules = entry.getValue();
            Object value = model.getFieldValue(field);

            // 按优先级排序
            List<FormValidationRule> sortedRules = new ArrayList<>(fieldRules);
            sortedRules.sort(Comparator.comparingInt(FormValidationRule::getPriority));

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            for (FormValidationRule rule : sortedRules) {
                // 跳过异步规则
                if (rule.isAsync()) continue;

                // 分组过滤
                if (group != null && rule.getGroup() != null && !group.equals(rule.getGroup())) {
                    continue;
                }

                // 条件验证
                if (rule.getWhen() != null) {
                    try {
                        if (!rule.getWhen().test(model)) continue;
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "条件验证谓词执行异常: " + field, e);
                        continue;
                    }
                }

                String error = validateSingleRule(rule, value, model);
                if (error != null) {
                    if (rule.isWarningOnly()) {
                        warnings.add(error);
                    } else {
                        errors.add(error);
                    }
                }
            }

            if (!errors.isEmpty()) {
                allErrors.put(field, errors);
                allValid = false;
            }
            if (!warnings.isEmpty()) {
                allWarnings.put(field, warnings);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        FormValidationResult result = allValid
                ? (allWarnings.isEmpty()
                    ? FormValidationResult.success()
                    : FormValidationResult.of(true, Collections.emptyMap(), allWarnings))
                : FormValidationResult.of(false, allErrors, allWarnings);
        result.validationDuration(duration);
        return result;
    }

    /**
     * 分组验证（只验证指定组的规则）
     *
     * @param model 表单数据模型
     * @param rules 验证规则映射
     * @param group 验证组名
     * @return 验证结果
     */
    public static FormValidationResult validateGroup(FormModel model,
                                                     Map<String, List<FormValidationRule>> rules,
                                                     String group) {
        return validateWithModel(model, rules, group);
    }

    /**
     * 验证单个字段（同步）
     *
     * @param field 字段名
     * @param value 字段值
     * @param rules 该字段的验证规则列表
     * @return 错误消息列表，通过验证时返回空列表
     */
    public static List<String> validateField(String field, Object value,
                                             List<FormValidationRule> rules) {
        return validateField(field, value, rules, null);
    }

    /**
     * 验证单个字段（支持跨字段验证）
     *
     * @param field 字段名
     * @param value 字段值
     * @param rules 该字段的验证规则列表
     * @param model 表单模型（用于跨字段验证，可为 null）
     * @return 错误消息列表
     */
    public static List<String> validateField(String field, Object value,
                                             List<FormValidationRule> rules,
                                             FormModel model) {
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> errors = new ArrayList<>();
        List<FormValidationRule> sortedRules = new ArrayList<>(rules);
        sortedRules.sort(Comparator.comparingInt(FormValidationRule::getPriority));

        for (FormValidationRule rule : sortedRules) {
            // 跳过异步规则
            if (rule.isAsync()) continue;
            // 跳过警告规则
            if (rule.isWarningOnly()) continue;

            // 条件验证
            if (rule.getWhen() != null && model != null) {
                try {
                    if (!rule.getWhen().test(model)) continue;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "条件验证谓词执行异常: " + field, e);
                    continue;
                }
            }

            String error = validateSingleRule(rule, value, model);
            if (error != null) {
                errors.add(error);
            }
        }
        return errors;
    }

    // ==================== 去抖验证 ====================

    /**
     * 去抖验证单个字段（防止频繁输入导致的过度验证）
     * <p>
     * 参考前端框架的 debounce 机制，在用户停止输入指定毫秒后才执行验证。
     * </p>
     *
     * @param field        字段名
     * @param value        字段值
     * @param rules        验证规则列表
     * @param delayMs      去抖延迟（毫秒）
     * @param callback     验证完成回调
     */
    public static void validateFieldDebounced(String field, Object value,
                                              List<FormValidationRule> rules,
                                              long delayMs,
                                              java.util.function.Consumer<List<String>> callback) {
        // 取消之前的任务
        ScheduledFuture<?> prev = DEBOUNCE_TASKS.get(field);
        if (prev != null && !prev.isDone()) {
            prev.cancel(false);
        }

        // 安排新任务
        ScheduledFuture<?> future = DEBOUNCE_SCHEDULER.schedule(() -> {
            List<String> errors = validateField(field, value, rules);
            if (callback != null) {
                javafx.application.Platform.runLater(() -> callback.accept(errors));
            }
        }, delayMs, TimeUnit.MILLISECONDS);

        DEBOUNCE_TASKS.put(field, future);
    }

    // ==================== 异步 API ====================

    /**
     * 异步验证整个表单（同时执行同步 + 异步规则）
     *
     * @param model 表单数据模型
     * @param rules 验证规则映射
     * @return 异步验证结果
     */
    public static CompletableFuture<FormValidationResult> validateAsync(
            FormModel model, Map<String, List<FormValidationRule>> rules) {
        if (model == null || rules == null || rules.isEmpty()) {
            return CompletableFuture.completedFuture(FormValidationResult.success());
        }

        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Map.Entry<String, List<String>>>> futures = new ArrayList<>();

        for (Map.Entry<String, List<FormValidationRule>> entry : rules.entrySet()) {
            String field = entry.getKey();
            List<FormValidationRule> fieldRules = entry.getValue();
            Object value = model.getFieldValue(field);

            CompletableFuture<Map.Entry<String, List<String>>> fieldFuture =
                    validateFieldAsync(field, value, fieldRules, model)
                            .thenApply(errors -> Map.entry(field, errors));
            futures.add(fieldFuture);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, List<String>> allErrors = new LinkedHashMap<>();
                    boolean allValid = true;
                    for (CompletableFuture<Map.Entry<String, List<String>>> f : futures) {
                        Map.Entry<String, List<String>> result = f.join();
                        if (!result.getValue().isEmpty()) {
                            allErrors.put(result.getKey(), result.getValue());
                            allValid = false;
                        }
                    }
                    long duration = System.currentTimeMillis() - startTime;
                    FormValidationResult result = allValid
                            ? FormValidationResult.success()
                            : FormValidationResult.failure(allErrors);
                    result.validationDuration(duration);
                    return result;
                });
    }

    /**
     * 异步验证单个字段（同步规则立即执行，异步规则并行执行后合并结果）
     *
     * @param field 字段名
     * @param value 字段值
     * @param rules 该字段的验证规则列表
     * @return 异步错误消息列表
     */
    public static CompletableFuture<List<String>> validateFieldAsync(
            String field, Object value, List<FormValidationRule> rules) {
        return validateFieldAsync(field, value, rules, null);
    }

    /**
     * 异步验证单个字段（支持跨字段验证）
     *
     * @param field 字段名
     * @param value 字段值
     * @param rules 该字段的验证规则列表
     * @param model 表单模型（用于跨字段验证，可为 null）
     * @return 异步错误消息列表
     */
    public static CompletableFuture<List<String>> validateFieldAsync(
            String field, Object value, List<FormValidationRule> rules, FormModel model) {
        if (rules == null || rules.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        // 先收集同步错误
        List<String> syncErrors = new ArrayList<>();
        List<CompletableFuture<String>> asyncFutures = new ArrayList<>();

        for (FormValidationRule rule : rules) {
            // 条件验证
            if (rule.getWhen() != null && model != null) {
                try {
                    if (!rule.getWhen().test(model)) continue;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "条件验证谓词执行异常: " + field, e);
                    continue;
                }
            }

            if (rule.isAsync() && rule.getAsyncValidator() != null) {
                // 异步规则
                asyncFutures.add(
                        rule.getAsyncValidator().apply(value)
                                .exceptionally(ex -> {
                                    LOGGER.log(Level.WARNING, "异步验证异常: " + field, ex);
                                    return "验证过程出错";
                                })
                );
            } else if (!rule.isWarningOnly()) {
                // 同步规则
                String error = validateSingleRule(rule, value, model);
                if (error != null) {
                    syncErrors.add(error);
                }
            }
        }

        if (asyncFutures.isEmpty()) {
            return CompletableFuture.completedFuture(syncErrors);
        }

        return CompletableFuture.allOf(asyncFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<String> allErrors = new ArrayList<>(syncErrors);
                    for (CompletableFuture<String> f : asyncFutures) {
                        String error = f.join();
                        if (error != null && !error.isEmpty()) {
                            allErrors.add(error);
                        }
                    }
                    return allErrors;
                });
    }

    /**
     * 检查规则列表中是否包含异步规则
     *
     * @param rules 规则映射
     * @return true 表示包含异步规则
     */
    public static boolean hasAsyncRules(Map<String, List<FormValidationRule>> rules) {
        if (rules == null) return false;
        for (List<FormValidationRule> fieldRules : rules.values()) {
            for (FormValidationRule rule : fieldRules) {
                if (rule.isAsync()) return true;
            }
        }
        return false;
    }

    // ==================== 批量字段验证 ====================

    /**
     * 验证指定的多个字段
     *
     * @param model  表单数据模型
     * @param rules  完整验证规则映射
     * @param fields 要验证的字段名
     * @return 验证结果
     */
    public static FormValidationResult validateFields(FormModel model,
                                                      Map<String, List<FormValidationRule>> rules,
                                                      String... fields) {
        if (model == null || rules == null || fields == null || fields.length == 0) {
            return FormValidationResult.success();
        }

        Map<String, List<FormValidationRule>> filteredRules = new LinkedHashMap<>();
        for (String field : fields) {
            List<FormValidationRule> fieldRules = rules.get(field);
            if (fieldRules != null) {
                filteredRules.put(field, fieldRules);
            }
        }

        return validateWithModel(model, filteredRules, null);
    }

    // ==================== 单规则验证 ====================

    /**
     * 使用单条规则验证值
     *
     * @param rule  验证规则
     * @param value 被验证的值
     * @return 错误消息，通过时返回 null
     */
    private static String validateSingleRule(FormValidationRule rule, Object value, FormModel model) {
        try {
            // 1. 自定义校验器优先
            if (rule.getValidator() != null) {
                return rule.getValidator().apply(rule, value);
            }

            // 2. 跨字段验证
            if (rule.getCrossField() != null && model != null) {
                Object otherValue = model.getFieldValue(rule.getCrossField());
                if (!Objects.equals(value, otherValue)) {
                    return rule.getMessage();
                }
                return null;
            }

            // 3. 枚举白名单验证
            if (rule.getEnumValues() != null && !rule.getEnumValues().isEmpty()) {
                if (value != null && !rule.getEnumValues().contains(value)) {
                    // 也尝试字符串比较
                    String strValue = String.valueOf(value);
                    boolean found = false;
                    for (Object ev : rule.getEnumValues()) {
                        if (strValue.equals(String.valueOf(ev))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) return rule.getMessage();
                }
                return null;
            }

            // 4. required 校验
            if (rule.isRequired()) {
                if (isEmpty(value)) {
                    return rule.getMessage();
                }
            }

            // 5. 空值跳过后续校验（非必填时空值合法）
            if (isEmpty(value)) {
                return null;
            }

            // 6. type 校验
            if (rule.getType() != null) {
                String typeError = validateType(rule.getType(), value, rule.getMessage());
                if (typeError != null) return typeError;
            }

            // 7. pattern 校验
            if (rule.getPattern() != null) {
                String patternError = validatePattern(rule.getPattern(), value, rule.getMessage());
                if (patternError != null) return patternError;
            }

            // 8. 数值范围校验 (minValue/maxValue)
            if (rule.getMinValue() != null || rule.getMaxValue() != null) {
                String rangeError = validateValueRange(rule.getMinValue(), rule.getMaxValue(), value, rule.getMessage());
                if (rangeError != null) return rangeError;
            }

            // 9. min/max 校验（长度/大小）
            if (rule.getMin() != null || rule.getMax() != null) {
                String rangeError = validateRange(rule.getMin(), rule.getMax(), value, rule.getMessage());
                if (rangeError != null) return rangeError;
            }

            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "验证规则执行异常", e);
            return "验证过程出错";
        }
    }

    // ==================== 各类验证逻辑 ====================

    /**
     * 判断值是否为空
     */
    private static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String) return ((String) value).trim().isEmpty();
        if (value instanceof Collection) return ((Collection<?>) value).isEmpty();
        return false;
    }

    /**
     * 类型验证
     */
    private static String validateType(String type, Object value, String message) {
        switch (type.toLowerCase()) {
            case "string":
                if (!(value instanceof String)) return message;
                break;
            case "number":
                if (!(value instanceof Number)) {
                    if (value instanceof String) {
                        try {
                            Double.parseDouble((String) value);
                        } catch (NumberFormatException e) {
                            return message;
                        }
                    } else {
                        return message;
                    }
                }
                break;
            case "integer":
                if (value instanceof Integer || value instanceof Long) break;
                if (value instanceof String) {
                    try {
                        Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        return message;
                    }
                } else if (value instanceof Number) {
                    double d = ((Number) value).doubleValue();
                    if (d != Math.floor(d)) return message;
                } else {
                    return message;
                }
                break;
            case "boolean":
                if (!(value instanceof Boolean)) return message;
                break;
            case "array":
                if (!(value instanceof List)) return message;
                break;
            case "email":
                if (!matchesPattern("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
                        String.valueOf(value))) return message;
                break;
            case "url":
                if (!matchesPattern("^(https?://)?([\\w\\-]+\\.)+[\\w\\-]+(/[\\w\\-./?%&=]*)?$",
                        String.valueOf(value))) return message;
                break;
            case "date":
                if (!(value instanceof java.time.LocalDate)
                        && !(value instanceof java.time.LocalDateTime)
                        && !(value instanceof Date)) {
                    // 尝试字符串解析
                    if (value instanceof String) {
                        try {
                            java.time.LocalDate.parse((String) value);
                        } catch (Exception e) {
                            return message;
                        }
                    } else {
                        return message;
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * 正则表达式验证
     */
    private static String validatePattern(String pattern, Object value, String message) {
        String strValue = String.valueOf(value);
        if (!matchesPattern(pattern, strValue)) {
            return message;
        }
        return null;
    }

    /**
     * 数值范围验证
     */
    private static String validateValueRange(Double min, Double max, Object value, String message) {
        double numVal;
        if (value instanceof Number) {
            numVal = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                numVal = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return message;
            }
        } else {
            return message;
        }
        if (min != null && numVal < min) return message;
        if (max != null && numVal > max) return message;
        return null;
    }

    /**
     * 范围验证（字符串看长度，集合看大小，数字看数值）
     */
    private static String validateRange(Integer min, Integer max, Object value, String message) {
        if (value instanceof Number) {
            double numVal = ((Number) value).doubleValue();
            if (min != null && numVal < min) return message;
            if (max != null && numVal > max) return message;
            return null;
        }

        int length;
        if (value instanceof String) {
            length = ((String) value).length();
        } else if (value instanceof Collection) {
            length = ((Collection<?>) value).size();
        } else {
            length = String.valueOf(value).length();
        }

        if (min != null && length < min) return message;
        if (max != null && length > max) return message;
        return null;
    }

    /**
     * 正则匹配（带缓存）
     */
    private static boolean matchesPattern(String regex, String value) {
        Pattern pattern = PATTERN_CACHE.computeIfAbsent(regex, Pattern::compile);
        return pattern.matcher(value).matches();
    }

    /**
     * 清理正则缓存
     */
    public static void clearPatternCache() {
        PATTERN_CACHE.clear();
    }

    /**
     * 取消指定字段的待执行去抖验证任务
     *
     * @param field 字段名
     */
    public static void cancelDebounced(String field) {
        if (field != null) {
            ScheduledFuture<?> future = DEBOUNCE_TASKS.remove(field);
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
        }
    }

    /**
     * 取消所有待执行的去抖验证任务
     */
    public static void cancelAllDebounced() {
        for (ScheduledFuture<?> future : DEBOUNCE_TASKS.values()) {
            if (!future.isDone()) {
                future.cancel(false);
            }
        }
        DEBOUNCE_TASKS.clear();
    }
}
