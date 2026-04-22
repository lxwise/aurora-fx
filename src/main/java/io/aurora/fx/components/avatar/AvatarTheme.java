package io.aurora.fx.components.avatar;

import javafx.scene.paint.Color;

/**
 * 头像组件主题配置
 * <p>
 * 通过 Builder 模式提供丰富的主题定制能力，支持边框、背景、阴影等全方位配置。
 * 内置多套预设主题（DEFAULT / DARK / BORDERED / SHADOW），可直接使用或基于预设进行二次定制。
 * </p>
 *
 * <pre>{@code
 * // 使用预设主题
 * AvatarTheme theme = AvatarTheme.DARK;
 *
 * // 自定义主题
 * AvatarTheme custom = AvatarTheme.builder()
 *     .borderColor(Color.valueOf("#409EFF"))
 *     .borderWidth(3)
 *     .shadowRadius(12)
 *     .build();
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class AvatarTheme {

    // ==================== 颜色配置 ====================

    /** 边框颜色 */
    private final Color borderColor;

    /** 背景颜色（图片加载前的占位背景） */
    private final Color backgroundColor;

    /** 占位文字颜色 */
    private final Color placeholderColor;

    /** 悬停边框颜色 */
    private final Color hoverBorderColor;

    // ==================== 尺寸配置 ====================

    /** 边框宽度 */
    private final double borderWidth;

    /** 阴影半径 */
    private final double shadowRadius;

    /** 阴影透明度 */
    private final double shadowOpacity;

    /** 默认圆角（仅 SQUARE 形状生效） */
    private final double arcSize;

    // ==================== 预设主题 ====================

    /** 默认主题 - 无边框 */
    public static final AvatarTheme DEFAULT = builder().build();

    /** 深色主题 */
    public static final AvatarTheme DARK = builder()
            .borderColor(Color.valueOf("#4A4A6A"))
            .backgroundColor(Color.valueOf("#2A2A3A"))
            .placeholderColor(Color.valueOf("#909399"))
            .hoverBorderColor(Color.valueOf("#667EEA"))
            .build();

    /** 带边框主题 */
    public static final AvatarTheme BORDERED = builder()
            .borderColor(Color.valueOf("#409EFF"))
            .borderWidth(2)
            .hoverBorderColor(Color.valueOf("#66B1FF"))
            .build();

    /** 阴影主题 */
    public static final AvatarTheme SHADOW = builder()
            .shadowRadius(10)
            .shadowOpacity(0.3)
            .build();

    // ==================== 构造方法 ====================

    private AvatarTheme(Builder builder) {
        this.borderColor = builder.borderColor;
        this.backgroundColor = builder.backgroundColor;
        this.placeholderColor = builder.placeholderColor;
        this.hoverBorderColor = builder.hoverBorderColor;
        this.borderWidth = builder.borderWidth;
        this.shadowRadius = builder.shadowRadius;
        this.shadowOpacity = builder.shadowOpacity;
        this.arcSize = builder.arcSize;
    }

    // ==================== Builder ====================

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 主题构建器
     */
    public static class Builder {
        private Color borderColor = Color.TRANSPARENT;
        private Color backgroundColor = Color.valueOf("#F0F0F0");
        private Color placeholderColor = Color.valueOf("#C0C4CC");
        private Color hoverBorderColor = Color.TRANSPARENT;
        private double borderWidth = 0;
        private double shadowRadius = 0;
        private double shadowOpacity = 0.15;
        private double arcSize = 0;

        public Builder borderColor(Color c) { this.borderColor = c; return this; }
        public Builder backgroundColor(Color c) { this.backgroundColor = c; return this; }
        public Builder placeholderColor(Color c) { this.placeholderColor = c; return this; }
        public Builder hoverBorderColor(Color c) { this.hoverBorderColor = c; return this; }
        public Builder borderWidth(double w) { this.borderWidth = w; return this; }
        public Builder shadowRadius(double r) { this.shadowRadius = r; return this; }
        public Builder shadowOpacity(double o) { this.shadowOpacity = o; return this; }
        public Builder arcSize(double s) { this.arcSize = s; return this; }

        public AvatarTheme build() {
            return new AvatarTheme(this);
        }
    }

    // ==================== 样式生成 ====================

    /**
     * 生成边框样式 CSS
     */
    public String getBorderStyle() {
        if (borderWidth <= 0) return "";
        return String.format(
                "-fx-stroke: %s; -fx-stroke-width: %.1f;",
                toCssColor(borderColor), borderWidth);
    }

    /**
     * 生成阴影样式 CSS
     */
    public String getShadowStyle() {
        if (shadowRadius <= 0) return "";
        return String.format(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,%.2f), %.0f, 0, 0, 2);",
                shadowOpacity, shadowRadius);
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

    public Color getBorderColor() { return borderColor; }
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getPlaceholderColor() { return placeholderColor; }
    public Color getHoverBorderColor() { return hoverBorderColor; }
    public double getBorderWidth() { return borderWidth; }
    public double getShadowRadius() { return shadowRadius; }
    public double getShadowOpacity() { return shadowOpacity; }
    public double getArcSize() { return arcSize; }
}
