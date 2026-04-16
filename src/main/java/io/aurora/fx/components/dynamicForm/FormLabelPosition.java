package io.aurora.fx.components.dynamicForm;

/**
 * 表单标签位置枚举
 * <p>
 * 对标 Element UI 的 label-position 属性，
 * 控制表单项标签相对于输入控件的位置。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 设置表单级标签位置
 * Form form = new Form().labelPosition(FormLabelPosition.TOP);
 *
 * // 单独设置某个表单项的标签位置（覆盖表单级设置）
 * FormItem item = new FormItem().labelPosition(FormLabelPosition.LEFT);
 * }</pre>
 *
 * @author Form Component
 * @version 1.0
 */
public enum FormLabelPosition {

    /** 标签在左侧，文本左对齐 */
    LEFT,

    /** 标签在左侧，文本右对齐（默认） */
    RIGHT,

    /** 标签在控件顶部 */
    TOP
}
