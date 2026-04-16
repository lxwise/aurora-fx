package io.aurora.fx.components.dynamicForm;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 表单主题配置
 * <p>
 * 定义表单组件的视觉风格，包括颜色、圆角、字体、间距等样式属性。
 * 提供多种预定义主题（Element UI、Ant Design、Naive UI 风格），同时支持自定义主题。
 * 支持 CSS 变量生成，方便与外部样式表集成。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 使用预定义主题
 * Form form = new Form().theme(FormTheme.DEFAULT);
 * Form form2 = new Form().theme(FormTheme.ANT_DESIGN);
 * Form form3 = new Form().theme(FormTheme.NAIVE_UI);
 *
 * // 自定义主题
 * FormTheme custom = new FormTheme()
 *     .primaryColor(Color.web("#1890FF"))
 *     .errorColor(Color.RED)
 *     .borderRadius(8)
 *     .itemSpacing(20);
 * Form form = new Form().theme(custom);
 *
 * // 动态主题切换（使用 Observable 属性）
 * ObjectProperty<FormTheme> currentTheme = FormTheme.currentThemeProperty();
 * currentTheme.set(FormTheme.DARK);
 *
 * // 生成 CSS 变量
 * String cssVars = FormTheme.DEFAULT.toCssVariables();
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormTheme {

    // ==================== 颜色属性 ====================

    private Color primaryColor;
    private Color successColor;
    private Color warningColor;
    private Color errorColor;
    private Color infoColor;
    private Color textColor;
    private Color labelColor;
    private Color borderColor;
    private Color disabledColor;
    private Color backgroundColor;
    private Color hoverColor;
    private Color focusBorderColor;
    private Color placeholderColor;

    // ==================== 布局属性 ====================

    private double borderRadius;
    private double borderWidth;
    private String fontFamily;
    private double itemSpacing;
    private double labelFontSize;
    private double inputFontSize;

    // ==================== 全局当前主题（支持动态切换） ====================

    private static final ObjectProperty<FormTheme> CURRENT_THEME =
            new SimpleObjectProperty<>(null);

    /**
     * 获取全局当前主题属性（可绑定到多个 Form 实例）
     *
     * @return 全局主题 Observable 属性
     */
    public static ObjectProperty<FormTheme> currentThemeProperty() {
        return CURRENT_THEME;
    }

    /**
     * 设置全局当前主题
     *
     * @param theme 主题
     */
    public static void setCurrentTheme(FormTheme theme) {
        CURRENT_THEME.set(theme);
    }

    /**
     * 获取全局当前主题
     *
     * @return 当前主题，未设置返回 null
     */
    public static FormTheme getCurrentTheme() {
        return CURRENT_THEME.get();
    }

    // ==================== 预定义主题 ====================

    /** 默认主题（Element UI 风格） */
    public static final FormTheme DEFAULT = new FormTheme()
            .primaryColor(Color.web("#409EFF"))
            .successColor(Color.web("#67C23A"))
            .warningColor(Color.web("#E6A23C"))
            .errorColor(Color.web("#F56C6C"))
            .infoColor(Color.web("#909399"))
            .textColor(Color.web("#303133"))
            .labelColor(Color.web("#606266"))
            .borderColor(Color.web("#DCDFE6"))
            .disabledColor(Color.web("#C0C4CC"))
            .backgroundColor(Color.WHITE)
            .hoverColor(Color.web("#409EFF", 0.1))
            .focusBorderColor(Color.web("#409EFF"))
            .placeholderColor(Color.web("#C0C4CC"))
            .borderRadius(4)
            .borderWidth(1)
            .fontFamily("System")
            .itemSpacing(18)
            .labelFontSize(14)
            .inputFontSize(14);

    /** 暗色主题（Element UI Dark） */
    public static final FormTheme DARK = new FormTheme()
            .primaryColor(Color.web("#409EFF"))
            .successColor(Color.web("#67C23A"))
            .warningColor(Color.web("#E6A23C"))
            .errorColor(Color.web("#F56C6C"))
            .infoColor(Color.web("#909399"))
            .textColor(Color.web("#E5EAF3"))
            .labelColor(Color.web("#A3A6AD"))
            .borderColor(Color.web("#4C4D4F"))
            .disabledColor(Color.web("#6C6E72"))
            .backgroundColor(Color.web("#1D1E1F"))
            .hoverColor(Color.web("#409EFF", 0.2))
            .focusBorderColor(Color.web("#409EFF"))
            .placeholderColor(Color.web("#6C6E72"))
            .borderRadius(4)
            .borderWidth(1)
            .fontFamily("System")
            .itemSpacing(18)
            .labelFontSize(14)
            .inputFontSize(14);

    /** Ant Design 风格主题 */
    public static final FormTheme ANT_DESIGN = new FormTheme()
            .primaryColor(Color.web("#1677FF"))
            .successColor(Color.web("#52C41A"))
            .warningColor(Color.web("#FAAD14"))
            .errorColor(Color.web("#FF4D4F"))
            .infoColor(Color.web("#1677FF"))
            .textColor(Color.web("#000000D9"))
            .labelColor(Color.web("#000000D9"))
            .borderColor(Color.web("#D9D9D9"))
            .disabledColor(Color.web("#00000040"))
            .backgroundColor(Color.WHITE)
            .hoverColor(Color.web("#1677FF", 0.06))
            .focusBorderColor(Color.web("#1677FF"))
            .placeholderColor(Color.web("#BFBFBF"))
            .borderRadius(6)
            .borderWidth(1)
            .fontFamily("System")
            .itemSpacing(24)
            .labelFontSize(14)
            .inputFontSize(14);

    /** Ant Design 暗色主题 */
    public static final FormTheme ANT_DESIGN_DARK = new FormTheme()
            .primaryColor(Color.web("#1668DC"))
            .successColor(Color.web("#49AA19"))
            .warningColor(Color.web("#D89614"))
            .errorColor(Color.web("#DC4446"))
            .infoColor(Color.web("#1668DC"))
            .textColor(Color.web("#FFFFFFD9"))
            .labelColor(Color.web("#FFFFFFA6"))
            .borderColor(Color.web("#424242"))
            .disabledColor(Color.web("#FFFFFF40"))
            .backgroundColor(Color.web("#141414"))
            .hoverColor(Color.web("#1668DC", 0.15))
            .focusBorderColor(Color.web("#1668DC"))
            .placeholderColor(Color.web("#FFFFFF40"))
            .borderRadius(6)
            .borderWidth(1)
            .fontFamily("System")
            .itemSpacing(24)
            .labelFontSize(14)
            .inputFontSize(14);

    /** Naive UI 风格主题 */
    public static final FormTheme NAIVE_UI = new FormTheme()
            .primaryColor(Color.web("#18A058"))
            .successColor(Color.web("#18A058"))
            .warningColor(Color.web("#F0A020"))
            .errorColor(Color.web("#D03050"))
            .infoColor(Color.web("#2080F0"))
            .textColor(Color.web("#333639"))
            .labelColor(Color.web("#333639"))
            .borderColor(Color.web("#E0E0E6"))
            .disabledColor(Color.web("#C2C2C8"))
            .backgroundColor(Color.WHITE)
            .hoverColor(Color.web("#18A058", 0.08))
            .focusBorderColor(Color.web("#18A058"))
            .placeholderColor(Color.web("#C2C2C8"))
            .borderRadius(3)
            .borderWidth(1)
            .fontFamily("System")
            .itemSpacing(18)
            .labelFontSize(14)
            .inputFontSize(14);

    /** Naive UI 暗色主题 */
    public static final FormTheme NAIVE_UI_DARK = new FormTheme()
            .primaryColor(Color.web("#63E2B7"))
            .successColor(Color.web("#63E2B7"))
            .warningColor(Color.web("#F2C97D"))
            .errorColor(Color.web("#E88080"))
            .infoColor(Color.web("#70C0E8"))
            .textColor(Color.web("#FFFFFFD9"))
            .labelColor(Color.web("#FFFFFFA6"))
            .borderColor(Color.web("#FFFFFF24"))
            .disabledColor(Color.web("#FFFFFF3D"))
            .backgroundColor(Color.web("#101014"))
            .hoverColor(Color.web("#63E2B7", 0.1))
            .focusBorderColor(Color.web("#63E2B7"))
            .placeholderColor(Color.web("#FFFFFF3D"))
            .borderRadius(3)
            .borderWidth(1)
            .fontFamily("System")
            .itemSpacing(18)
            .labelFontSize(14)
            .inputFontSize(14);

    // ==================== 链式 Setters ====================

    public FormTheme primaryColor(Color c) { this.primaryColor = c; return this; }
    public FormTheme successColor(Color c) { this.successColor = c; return this; }
    public FormTheme warningColor(Color c) { this.warningColor = c; return this; }
    public FormTheme errorColor(Color c) { this.errorColor = c; return this; }
    public FormTheme infoColor(Color c) { this.infoColor = c; return this; }
    public FormTheme textColor(Color c) { this.textColor = c; return this; }
    public FormTheme labelColor(Color c) { this.labelColor = c; return this; }
    public FormTheme borderColor(Color c) { this.borderColor = c; return this; }
    public FormTheme disabledColor(Color c) { this.disabledColor = c; return this; }
    public FormTheme backgroundColor(Color c) { this.backgroundColor = c; return this; }
    public FormTheme hoverColor(Color c) { this.hoverColor = c; return this; }
    public FormTheme focusBorderColor(Color c) { this.focusBorderColor = c; return this; }
    public FormTheme placeholderColor(Color c) { this.placeholderColor = c; return this; }
    public FormTheme borderRadius(double r) { this.borderRadius = r; return this; }
    public FormTheme borderWidth(double w) { this.borderWidth = w; return this; }
    public FormTheme fontFamily(String f) { this.fontFamily = f; return this; }
    public FormTheme itemSpacing(double s) { this.itemSpacing = s; return this; }
    public FormTheme labelFontSize(double s) { this.labelFontSize = s; return this; }
    public FormTheme inputFontSize(double s) { this.inputFontSize = s; return this; }

    // ==================== Getters ====================

    public Color getPrimaryColor() { return primaryColor; }
    public Color getSuccessColor() { return successColor; }
    public Color getWarningColor() { return warningColor; }
    public Color getErrorColor() { return errorColor; }
    public Color getInfoColor() { return infoColor; }
    public Color getTextColor() { return textColor; }
    public Color getLabelColor() { return labelColor; }
    public Color getBorderColor() { return borderColor; }
    public Color getDisabledColor() { return disabledColor; }
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getHoverColor() { return hoverColor; }
    public Color getFocusBorderColor() { return focusBorderColor; }
    public Color getPlaceholderColor() { return placeholderColor; }
    public double getBorderRadius() { return borderRadius; }
    public double getBorderWidth() { return borderWidth; }
    public String getFontFamily() { return fontFamily; }
    public double getItemSpacing() { return itemSpacing; }
    public double getLabelFontSize() { return labelFontSize; }
    public double getInputFontSize() { return inputFontSize; }

    // ==================== CSS 生成 ====================

    /**
     * 生成完整的 JavaFX CSS 样式字符串，可直接应用到 Form 根节点
     *
     * @return JavaFX CSS 样式字符串
     */
    public String toCss() {
        StringBuilder css = new StringBuilder();
        css.append(String.format("-fx-background-color: %s; ", toHex(backgroundColor)));
        css.append(String.format("-fx-font-family: \"%s\"; ", fontFamily != null ? fontFamily : "System"));
        css.append(String.format("-fx-font-size: %.0fpx; ", inputFontSize));
        return css.toString();
    }

    /**
     * 生成 CSS 变量映射，用于与外部 CSS 文件配合使用
     * <p>
     * 变量命名遵循 {@code --form-*} 前缀规范，如：
     * <pre>
     * --form-primary-color: #409EFF;
     * --form-error-color: #F56C6C;
     * --form-border-radius: 4px;
     * </pre>
     *
     * @return CSS 变量名 -> 值的映射
     */
    public Map<String, String> toCssVariables() {
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("--form-primary-color", toHex(primaryColor));
        vars.put("--form-success-color", toHex(successColor));
        vars.put("--form-warning-color", toHex(warningColor));
        vars.put("--form-error-color", toHex(errorColor));
        vars.put("--form-info-color", toHex(infoColor));
        vars.put("--form-text-color", toHex(textColor));
        vars.put("--form-label-color", toHex(labelColor));
        vars.put("--form-border-color", toHex(borderColor));
        vars.put("--form-disabled-color", toHex(disabledColor));
        vars.put("--form-background-color", toHex(backgroundColor));
        vars.put("--form-hover-color", toHex(hoverColor));
        vars.put("--form-focus-border-color", toHex(focusBorderColor));
        vars.put("--form-placeholder-color", toHex(placeholderColor));
        vars.put("--form-border-radius", borderRadius + "px");
        vars.put("--form-border-width", borderWidth + "px");
        vars.put("--form-font-family", fontFamily != null ? fontFamily : "System");
        vars.put("--form-item-spacing", itemSpacing + "px");
        vars.put("--form-label-font-size", labelFontSize + "px");
        vars.put("--form-input-font-size", inputFontSize + "px");
        return vars;
    }

    /**
     * 生成 JavaFX 控件输入框样式
     *
     * @return 输入框 CSS 样式字符串
     */
    public String toInputCss() {
        return String.format(
                "-fx-border-color: %s; -fx-border-radius: %.0f; " +
                "-fx-background-radius: %.0f; -fx-border-width: %.0f; " +
                "-fx-font-size: %.0fpx; -fx-text-fill: %s;",
                toHex(borderColor), borderRadius,
                borderRadius, borderWidth,
                inputFontSize, toHex(textColor));
    }

    /**
     * 生成聚焦状态的输入框样式
     *
     * @return 聚焦时的 CSS 样式字符串
     */
    public String toInputFocusCss() {
        return String.format(
                "-fx-border-color: %s; -fx-border-radius: %.0f; " +
                "-fx-background-radius: %.0f; -fx-border-width: %.0f; " +
                "-fx-effect: dropshadow(gaussian, %s, 4, 0, 0, 0);",
                toHex(focusBorderColor), borderRadius,
                borderRadius, borderWidth,
                toRgba(focusBorderColor, 0.2));
    }

    /**
     * 基于当前主题创建副本并修改
     *
     * @return 主题副本
     */
    public FormTheme copy() {
        return new FormTheme()
                .primaryColor(this.primaryColor)
                .successColor(this.successColor)
                .warningColor(this.warningColor)
                .errorColor(this.errorColor)
                .infoColor(this.infoColor)
                .textColor(this.textColor)
                .labelColor(this.labelColor)
                .borderColor(this.borderColor)
                .disabledColor(this.disabledColor)
                .backgroundColor(this.backgroundColor)
                .hoverColor(this.hoverColor)
                .focusBorderColor(this.focusBorderColor)
                .placeholderColor(this.placeholderColor)
                .borderRadius(this.borderRadius)
                .borderWidth(this.borderWidth)
                .fontFamily(this.fontFamily)
                .itemSpacing(this.itemSpacing)
                .labelFontSize(this.labelFontSize)
                .inputFontSize(this.inputFontSize);
    }

    // ==================== 工具方法 ====================

    /**
     * 将 Color 转为 CSS 十六进制字符串
     *
     * @param color JavaFX Color
     * @return CSS 格式的颜色字符串，如 "#409EFF"
     */
    public static String toHex(Color color) {
        if (color == null) return "#000000";
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * 将 Color 转为 CSS rgba 字符串
     *
     * @param color JavaFX Color
     * @param alpha 透明度 (0.0 ~ 1.0)
     * @return CSS 格式的颜色字符串，如 "rgba(64,158,255,0.50)"
     */
    public static String toRgba(Color color, double alpha) {
        if (color == null) return "rgba(0,0,0,1.00)";
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                alpha);
    }

    @Override
    public String toString() {
        return "FormTheme{primary=" + toHex(primaryColor) + ", bg=" + toHex(backgroundColor) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormTheme that = (FormTheme) o;
        return Double.compare(borderRadius, that.borderRadius) == 0
                && Double.compare(borderWidth, that.borderWidth) == 0
                && Double.compare(itemSpacing, that.itemSpacing) == 0
                && Objects.equals(primaryColor, that.primaryColor)
                && Objects.equals(errorColor, that.errorColor)
                && Objects.equals(textColor, that.textColor)
                && Objects.equals(labelColor, that.labelColor)
                && Objects.equals(borderColor, that.borderColor)
                && Objects.equals(backgroundColor, that.backgroundColor)
                && Objects.equals(fontFamily, that.fontFamily);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryColor, errorColor, textColor, backgroundColor, borderRadius, fontFamily);
    }
}
