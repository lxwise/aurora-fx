package io.aurora.fx.components.dynamicForm;

import javafx.scene.Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 表单字段渲染器工厂（插件机制）
 * <p>
 * 提供可扩展的字段渲染器注册和创建机制，允许用户注册自定义控件类型。
 * 参考 Ant Design 的自定义表单控件机制和 FormsFX 的 Renderer 设计。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 注册自定义渲染器
 * FormFieldFactory.register("rating", (fieldName, config) -> {
 *     RatingControl rating = new RatingControl();
 *     rating.setMax(5);
 *     return rating;
 * });
 *
 * // 注册带自动绑定的渲染器
 * FormFieldFactory.registerWithBinding("rating",
 *     (fieldName, config) -> new RatingControl(),
 *     (control, model, fieldName) -> {
 *         RatingControl rc = (RatingControl) control;
 *         rc.ratingProperty().addListener((obs, o, n) ->
 *             model.setFieldValue(fieldName, n));
 *     }
 * );
 *
 * // 创建控件实例
 * Node ratingNode = FormFieldFactory.create("rating", "score", null);
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 2.0
 */
public class FormFieldFactory {

    private static final Logger LOGGER = Logger.getLogger(FormFieldFactory.class.getName());

    /**
     * 字段渲染器接口
     */
    @FunctionalInterface
    public interface FieldRenderer {
        /**
         * 创建字段控件
         *
         * @param fieldName 字段名
         * @param config    额外配置（可选）
         * @return 创建的 JavaFX 控件
         */
        Node create(String fieldName, Map<String, Object> config);
    }

    /**
     * 字段绑定器接口 - 将控件绑定到 FormModel
     */
    @FunctionalInterface
    public interface FieldBinder {
        /**
         * 将控件绑定到模型字段
         *
         * @param control   控件节点
         * @param model     数据模型
         * @param fieldName 字段名
         */
        void bind(Node control, FormModel model, String fieldName);
    }

    /** 已注册的渲染器映射 */
    private static final Map<String, FieldRenderer> RENDERERS = new ConcurrentHashMap<>();

    /** 已注册的绑定器映射 */
    private static final Map<String, FieldBinder> BINDERS = new ConcurrentHashMap<>();

    /** 不可实例化 */
    private FormFieldFactory() {}

    // ==================== 注册 API ====================

    /**
     * 注册自定义字段渲染器
     *
     * @param type     类型标识（如 "rating", "tree-select", "cascader"）
     * @param renderer 渲染器实例
     */
    public static void register(String type, FieldRenderer renderer) {
        if (type == null || renderer == null) {
            throw new IllegalArgumentException("类型和渲染器不能为空");
        }
        RENDERERS.put(type.toLowerCase(), renderer);
        LOGGER.fine("注册字段渲染器: " + type);
    }

    /**
     * 注册带绑定器的自定义字段渲染器
     *
     * @param type     类型标识
     * @param renderer 渲染器
     * @param binder   绑定器
     */
    public static void registerWithBinding(String type, FieldRenderer renderer, FieldBinder binder) {
        register(type, renderer);
        if (binder != null) {
            BINDERS.put(type.toLowerCase(), binder);
        }
    }

    /**
     * 注销字段渲染器
     *
     * @param type 类型标识
     */
    public static void unregister(String type) {
        if (type != null) {
            RENDERERS.remove(type.toLowerCase());
            BINDERS.remove(type.toLowerCase());
            LOGGER.fine("注销字段渲染器: " + type);
        }
    }

    // ==================== 创建 API ====================

    /**
     * 创建字段控件
     *
     * @param type      类型标识
     * @param fieldName 字段名
     * @param config    额外配置（可传 null）
     * @return 创建的控件节点，类型未注册时返回 null
     */
    public static Node create(String type, String fieldName, Map<String, Object> config) {
        if (type == null) return null;
        FieldRenderer renderer = RENDERERS.get(type.toLowerCase());
        if (renderer == null) {
            LOGGER.warning("未注册的字段渲染器类型: " + type);
            return null;
        }
        try {
            return renderer.create(fieldName, config);
        } catch (Exception e) {
            LOGGER.warning("创建字段控件失败: " + type + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取字段绑定器
     *
     * @param type 类型标识
     * @return 绑定器实例，未注册时返回 null
     */
    public static FieldBinder getBinder(String type) {
        return type != null ? BINDERS.get(type.toLowerCase()) : null;
    }

    /**
     * 检查类型是否已注册
     *
     * @param type 类型标识
     * @return true 表示已注册
     */
    public static boolean isRegistered(String type) {
        return type != null && RENDERERS.containsKey(type.toLowerCase());
    }

    /**
     * 获取所有已注册的类型名
     *
     * @return 类型名集合
     */
    public static java.util.Set<String> getRegisteredTypes() {
        return java.util.Collections.unmodifiableSet(RENDERERS.keySet());
    }

    /**
     * 清除所有注册的渲染器和绑定器
     */
    public static void clearAll() {
        RENDERERS.clear();
        BINDERS.clear();
    }
}
