package io.aurora.fx.components.lineButton;

import javafx.scene.paint.Color;

/**
 * 线条按钮主题配置
 * <p>
 * 通过 Builder 模式提供主题定制，支持文字颜色、线条颜色、字体等配置。
 * 内置多套预设主题（DEFAULT / DARK / PRIMARY / DANGER）。
 * </p>
 *
 * <pre>{@code
 * LineButtonTheme theme = LineButtonTheme.builder()
 *     .textColor(Color.valueOf("#303133"))
 *     .lineColor(Color.valueOf("#409EFF"))
 *     .hoverTextColor(Color.valueOf("#409EFF"))
 *     .build();
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class LineButtonTheme {

    // ==================== 颜色配置 ====================

    /** 默认文字颜色 */
    private final Color textColor;
    /** 悬停文字颜色 */
    private final Color hoverTextColor;
    /** 线条颜色 */
    private final Color lineColor;
    /** 背景颜色 */
    private final Color backgroundColor;

    // ==================== 尺寸配置 ====================

    /** 字体大小 */
    private final double fontSize;
    /** 字体族 */
    private final String fontFamily;
    /** 线条宽度 */
    private final double lineWidth;
    /** 动画时长（毫秒） */
    private final double animationDuration;

    // ==================== 预设主题 ====================

    public static final LineButtonTheme DEFAULT = builder().build();

    public static final LineButtonTheme DARK = builder()
            .textColor(Color.valueOf("#E0E0E0"))
            .hoverTextColor(Color.valueOf("#66B1FF"))
            .lineColor(Color.valueOf("#409EFF"))
            .backgroundColor(Color.TRANSPARENT)
            .build();

    public static final LineButtonTheme PRIMARY = builder()
            .textColor(Color.valueOf("#409EFF"))
            .hoverTextColor(Color.valueOf("#66B1FF"))
            .lineColor(Color.valueOf("#409EFF"))
            .build();

    public static final LineButtonTheme DANGER = builder()
            .textColor(Color.valueOf("#F56C6C"))
            .hoverTextColor(Color.valueOf("#F78989"))
            .lineColor(Color.valueOf("#F56C6C"))
            .build();

    // ==================== 构造方法 ====================

    private LineButtonTheme(Builder builder) {
        this.textColor = builder.textColor;
        this.hoverTextColor = builder.hoverTextColor;
        this.lineColor = builder.lineColor;
        this.backgroundColor = builder.backgroundColor;
        this.fontSize = builder.fontSize;
        this.fontFamily = builder.fontFamily;
        this.lineWidth = builder.lineWidth;
        this.animationDuration = builder.animationDuration;
    }

    // ==================== Builder ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Color textColor = Color.valueOf("#303133");
        private Color hoverTextColor = Color.valueOf("#409EFF");
        private Color lineColor = Color.valueOf("#409EFF");
        private Color backgroundColor = Color.TRANSPARENT;
        private double fontSize = 14;
        private String fontFamily = "System";
        private double lineWidth = 1.5;
        private double animationDuration = 130;

        public Builder textColor(Color c) { this.textColor = c; return this; }
        public Builder hoverTextColor(Color c) { this.hoverTextColor = c; return this; }
        public Builder lineColor(Color c) { this.lineColor = c; return this; }
        public Builder backgroundColor(Color c) { this.backgroundColor = c; return this; }
        public Builder fontSize(double s) { this.fontSize = s; return this; }
        public Builder fontFamily(String f) { this.fontFamily = f; return this; }
        public Builder lineWidth(double w) { this.lineWidth = w; return this; }
        public Builder animationDuration(double d) { this.animationDuration = d; return this; }

        public LineButtonTheme build() {
            return new LineButtonTheme(this);
        }
    }

    // ==================== 样式生成 ====================

    /** 生成默认文字样式 */
    public String getTextStyle() {
        return String.format(
                "-fx-text-fill: %s; -fx-font-size: %.0f; -fx-font-family: \"%s\";",
                toCssColor(textColor), fontSize, fontFamily);
    }

    /** 生成线条样式 */
    public String getLineStyle() {
        return String.format(
                "-fx-stroke: %s; -fx-stroke-width: %.1f;",
                toCssColor(lineColor), lineWidth);
    }

    // ==================== 工具方法 ====================

    public static String toCssColor(Color color) {
        if (color == null) return "transparent";
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }

    // ==================== Getters ====================

    public Color getTextColor() { return textColor; }
    public Color getHoverTextColor() { return hoverTextColor; }
    public Color getLineColor() { return lineColor; }
    public Color getBackgroundColor() { return backgroundColor; }
    public double getFontSize() { return fontSize; }
    public String getFontFamily() { return fontFamily; }
    public double getLineWidth() { return lineWidth; }
    public double getAnimationDuration() { return animationDuration; }
}
