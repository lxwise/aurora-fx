package io.aurora.fx.components.dynamicForm;

/**
 * 表单绑定模式
 * <p>
 * 参考 FormsFX 的 BindingMode 设计，控制表单数据何时持久化到模型。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 手动模式：用户调用 persist() 后才提交到模型
 * Form form = new Form().bindingMode(FormBindingMode.MANUAL);
 * form.persist(); // 显式持久化
 *
 * // 连续模式：每次编辑自动持久化（默认行为）
 * Form form = new Form().bindingMode(FormBindingMode.CONTINUOUS);
 *
 * // 延迟模式：用户停止输入一段时间后自动持久化
 * Form form = new Form().bindingMode(FormBindingMode.LAZY);
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public enum FormBindingMode {

    /**
     * 手动模式
     * <p>
     * 用户输入先保存在临时缓冲区，只有调用 {@code persist()} 后才写入模型。
     * 调用 {@code reset()} 可以回滚到上次持久化的值。适用于需要"提交/取消"
     * 语义的表单场景。
     * </p>
     */
    MANUAL,

    /**
     * 连续模式（默认）
     * <p>
     * 每次用户编辑都直接写入模型，与现有行为一致。
     * 适用于实时搜索、过滤器等无需显式提交的场景。
     * </p>
     */
    CONTINUOUS,

    /**
     * 延迟模式
     * <p>
     * 用户输入控件失去焦点时才写入模型。
     * 适用于需要减少更新频率但不需要手动提交的场景。
     * 参考 Vue 的 v-model.lazy 修饰符。
     * </p>
     */
    LAZY
}
