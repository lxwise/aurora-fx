package io.aurora.fx.components.translationButton;

import javafx.scene.paint.Color;

/**
 * 平移按钮主题配置
 * <p>
 * 通过 Builder 模式提供丰富的主题定制能力，支持颜色、字体、动画时长等配置。
 * 内置多套预设主题（DEFAULT / DARK / PRIMARY / SUCCESS / DANGER）。
 * </p>
 *
 * <pre>{@code
 * TranslationButtonTheme theme = TranslationButtonTheme.builder()
 *     .backgroundColor(Color.valueOf("#409EFF"))
 *     .textColor(Color.WHITE)
 *     .hoverBackgroundColor(Color.valueOf("#66B1FF"))
 *     .build();
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class TranslationButtonTheme {

    // ==================== 颜色配置 ====================

    /** 默认背景色 */
    private final Color backgroundColor;
    /** 默认文字颜色 */
    private final Color textColor;
    /** 悬停背景色 */
    private final Color hoverBackgroundColor;
    /** 悬停文字颜色 */
    private final Color hoverTextColor;
    /** 边框颜色 */
    private final Color borderColor;

    // ==================== 尺寸配置 ====================

    /** 圆角 */
    private final double borderRadius;
    /** 字体大小 */
    private final double fontSize;
    /** 字体族 */
    private final String fontFamily;
    /** 动画时长（毫秒） */
    private final double animationDuration;

    // ==================== 预设主题 ====================

    public static final TranslationButtonTheme DEFAULT = builder().build();

    public static final TranslationButtonTheme DARK = builder()
            .backgroundColor(Color.valueOf("#2C3E50"))
            .textColor(Color.WHITE)
            .hoverBackgroundColor(Color.valueOf("#34495E"))
            .hoverTextColor(Color.valueOf("#ECF0F1"))
            .borderColor(Color.valueOf("#4A4A6A"))
            .build();

    public static final TranslationButtonTheme PRIMARY = builder()
            .backgroundColor(Color.valueOf("#409EFF"))
            .textColor(Color.WHITE)
            .hoverBackgroundColor(Color.valueOf("#66B1FF"))
            .hoverTextColor(Color.WHITE)
            .borderColor(Color.valueOf("#409EFF"))
            .build();

    public static final TranslationButtonTheme SUCCESS = builder()
            .backgroundColor(Color.valueOf("#67C23A"))
            .textColor(Color.WHITE)
            .hoverBackgroundColor(Color.valueOf("#85CE61"))
            .hoverTextColor(Color.WHITE)
            .borderColor(Color.valueOf("#67C23A"))
            .build();

    public static final TranslationButtonTheme DANGER = builder()
            .backgroundColor(Color.valueOf("#F56C6C"))
            .textColor(Color.WHITE)
            .hoverBackgroundColor(Color.valueOf("#F78989"))
            .hoverTextColor(Color.WHITE)
            .borderColor(Color.valueOf("#F56C6C"))
            .build();

    // ==================== 构造方法 ====================

    private TranslationButtonTheme(Builder builder) {
        this.backgroundColor = builder.backgroundColor;
        this.textColor = builder.textColor;
        this.hoverBackgroundColor = builder.hoverBackgroundColor;
        this.hoverTextColor = builder.hoverTextColor;
        this.borderColor = builder.borderColor;
        this.borderRadius = builder.borderRadius;
        this.fontSize = builder.fontSize;
        this.fontFamily = builder.fontFamily;
        this.animationDuration = builder.animationDuration;
    }

    // ==================== Builder ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Color backgroundColor = Color.valueOf("#ECF5FF");
        private Color textColor = Color.valueOf("#409EFF");
        private Color hoverBackgroundColor = Color.valueOf("#409EFF");
        private Color hoverTextColor = Color.WHITE;
        private Color borderColor = Color.valueOf("#B3D8FF");
        private double borderRadius = 4;
        private double fontSize = 14;
        private String fontFamily = "System";
        private double animationDuration = 130;

        public Builder backgroundColor(Color c) { this.backgroundColor = c; return this; }
        public Builder textColor(Color c) { this.textColor = c; return this; }
        public Builder hoverBackgroundColor(Color c) { this.hoverBackgroundColor = c; return this; }
        public Builder hoverTextColor(Color c) { this.hoverTextColor = c; return this; }
        public Builder borderColor(Color c) { this.borderColor = c; return this; }
        public Builder borderRadius(double r) { this.borderRadius = r; return this; }
        public Builder fontSize(double s) { this.fontSize = s; return this; }
        public Builder fontFamily(String f) { this.fontFamily = f; return this; }
        public Builder animationDuration(double d) { this.animationDuration = d; return this; }

        public TranslationButtonTheme build() {
            return new TranslationButtonTheme(this);
        }
    }

    // ==================== 样式生成 ====================

    /** 生成默认状态容器样式 */
    public String getDefaultPaneStyle() {
        return String.format(
                "-fx-background-color: %s; -fx-background-radius: %.0f; " +
                "-fx-border-color: %s; -fx-border-radius: %.0f;",
                toCssColor(backgroundColor), borderRadius,
                toCssColor(borderColor), borderRadius);
    }

    /** 生成默认文字样式 */
    public String getDefaultTextStyle() {
        return String.format(
                "-fx-text-fill: %s; -fx-font-size: %.0f; -fx-font-family: \"%s\";",
                toCssColor(textColor), fontSize, fontFamily);
    }

    /** 生成悬停文字样式 */
    public String getHoverTextStyle() {
        return String.format(
                "-fx-text-fill: %s; -fx-font-size: %.0f; -fx-font-family: \"%s\";",
                toCssColor(hoverTextColor), fontSize, fontFamily);
    }

    /** 生成悬停状态容器样式 */
    public String getHoverPaneStyle() {
        return String.format(
                "-fx-background-color: %s; -fx-background-radius: %.0f;",
                toCssColor(hoverBackgroundColor), borderRadius);
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

    public Color getBackgroundColor() { return backgroundColor; }
    public Color getTextColor() { return textColor; }
    public Color getHoverBackgroundColor() { return hoverBackgroundColor; }
    public Color getHoverTextColor() { return hoverTextColor; }
    public Color getBorderColor() { return borderColor; }
    public double getBorderRadius() { return borderRadius; }
    public double getFontSize() { return fontSize; }
    public String getFontFamily() { return fontFamily; }
    public double getAnimationDuration() { return animationDuration; }
}
