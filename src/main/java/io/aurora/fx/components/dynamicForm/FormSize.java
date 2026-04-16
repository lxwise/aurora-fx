package io.aurora.fx.components.dynamicForm;

/**
 * 表单尺寸枚举
 * <p>
 * 对标 Element UI / Ant Design 的 size 属性，定义表单控件的四种标准尺寸。
 * 表单中所有子组件可继承该尺寸设置，实现统一的视觉风格。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * Form form = new Form().size(FormSize.LARGE);
 * FormItem item = new FormItem().size(FormSize.SMALL);  // 覆盖表单级设置
 * FormItem mini = new FormItem().size(FormSize.MINI);   // 最紧凑尺寸
 * }</pre>
 *
 * <h3>尺寸对照（参考 Element UI）</h3>
 * <table>
 *   <tr><th>尺寸</th><th>控件高度</th><th>字体大小</th><th>适用场景</th></tr>
 *   <tr><td>LARGE</td><td>40px</td><td>16px</td><td>宽松布局、后台管理</td></tr>
 *   <tr><td>DEFAULT</td><td>32px</td><td>14px</td><td>标准布局（默认）</td></tr>
 *   <tr><td>SMALL</td><td>24px</td><td>12px</td><td>紧凑布局、数据表格内</td></tr>
 *   <tr><td>MINI</td><td>20px</td><td>11px</td><td>超紧凑、工具栏、筛选条</td></tr>
 * </table>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public enum FormSize {

    /** 大尺寸 - 适用于宽松布局 */
    LARGE("large", 40, 16, 12, 8),

    /** 默认尺寸 - 标准布局 */
    DEFAULT("default", 32, 14, 10, 6),

    /** 小尺寸 - 适用于紧凑布局 */
    SMALL("small", 24, 12, 8, 4),

    /** 迷你尺寸 - 适用于超紧凑布局（工具栏、筛选条等） */
    MINI("mini", 20, 11, 6, 2);

    private final String value;
    private final double controlHeight;
    private final double fontSize;
    private final double horizontalPadding;
    private final double verticalPadding;

    FormSize(String value, double controlHeight, double fontSize,
             double horizontalPadding, double verticalPadding) {
        this.value = value;
        this.controlHeight = controlHeight;
        this.fontSize = fontSize;
        this.horizontalPadding = horizontalPadding;
        this.verticalPadding = verticalPadding;
    }

    /** 获取尺寸名称 */
    public String getValue() { return value; }

    /** 获取控件高度（px） */
    public double getControlHeight() { return controlHeight; }

    /** 获取字体大小（px） */
    public double getFontSize() { return fontSize; }

    /** 获取水平内边距（px） */
    public double getHorizontalPadding() { return horizontalPadding; }

    /** 获取垂直内边距（px） */
    public double getVerticalPadding() { return verticalPadding; }

    /**
     * 生成此尺寸对应的 CSS 样式字符串
     *
     * @return 内联 CSS 样式片段
     */
    public String toCssStyle() {
        return String.format(
                "-fx-font-size: %.0fpx; -fx-pref-height: %.0fpx; " +
                "-fx-padding: %.0fpx %.0fpx %.0fpx %.0fpx;",
                fontSize, controlHeight,
                verticalPadding, horizontalPadding,
                verticalPadding, horizontalPadding);
    }

    /**
     * 根据值名称查找枚举
     *
     * @param value 尺寸名称（"large", "default", "small", "mini"）
     * @return 匹配的枚举值，未找到返回 {@link #DEFAULT}
     */
    public static FormSize fromValue(String value) {
        if (value == null) return DEFAULT;
        for (FormSize s : values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        return DEFAULT;
    }

    @Override
    public String toString() {
        return value;
    }
}
