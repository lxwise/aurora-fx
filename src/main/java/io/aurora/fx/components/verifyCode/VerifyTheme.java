package io.aurora.fx.components.verifyCode;

import javafx.scene.paint.Color;

/**
 * 验证码主题配置类
 * 支持自定义验证码组件的颜色、字体、样式等
 * <p>
 * 使用示例：
 * <pre>
 * VerifyTheme theme = VerifyTheme.builder()
 *     .primaryColor(Color.valueOf("#1e90ff"))
 *     .successColor(Color.valueOf("#52c41a"))
 *     .backgroundColor(Color.WHITE)
 *     .fontFamily("Microsoft YaHei")
 *     .fontSize(14)
 *     .build();
 * 
 * VerifyConfig config = new VerifyConfig()
 *     .theme(theme);
 * </pre>
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public class VerifyTheme {

    // ==================== 颜色配置 ====================

    /**
     * 主色调
     */
    private final Color primaryColor;

    /**
     * 成功颜色
     */
    private final Color successColor;

    /**
     * 失败颜色
     */
    private final Color errorColor;

    /**
     * 警告颜色
     */
    private final Color warningColor;

    /**
     * 背景颜色
     */
    private final Color backgroundColor;

    /**
     * 卡片背景颜色
     */
    private final Color cardBackgroundColor;

    /**
     * 文字颜色
     */
    private final Color textColor;

    /**
     * 次要文字颜色
     */
    private final Color secondaryTextColor;

    /**
     * 边框颜色
     */
    private final Color borderColor;

    /**
     * 滑块轨道颜色
     */
    private final Color sliderTrackColor;

    /**
     * 滑块按钮颜色
     */
    private final Color sliderThumbColor;

    // ==================== 字体配置 ====================

    /**
     * 字体族
     */
    private final String fontFamily;

    /**
     * 基础字体大小
     */
    private final double baseFontSize;

    /**
     * 标题字体大小
     */
    private final double titleFontSize;

    /**
     * 小字体大小
     */
    private final double smallFontSize;

    // ==================== 尺寸配置 ====================

    /**
     * 圆角大小
     */
    private final double borderRadius;

    /**
     * 内边距
     */
    private final double padding;

    /**
     * 阴影大小
     */
    private final double shadowRadius;

    // ==================== 预设主题 ====================

    /**
     * 默认主题（浅色）
     */
    public static final VerifyTheme DEFAULT = builder().build();

    /**
     * 深色主题
     */
    public static final VerifyTheme DARK = builder()
            .backgroundColor(Color.valueOf("#1a1a2e"))
            .cardBackgroundColor(Color.valueOf("#16213e"))
            .textColor(Color.WHITE)
            .secondaryTextColor(Color.valueOf("#a0a0a0"))
            .borderColor(Color.valueOf("#3a3a5a"))
            .primaryColor(Color.valueOf("#4361ee"))
            .successColor(Color.valueOf("#4ade80"))
            .errorColor(Color.valueOf("#f87171"))
            .sliderTrackColor(Color.valueOf("#2a2a4a"))
            .sliderThumbColor(Color.valueOf("#4a4a6a"))
            .build();

    /**
     * 蓝色主题
     */
    public static final VerifyTheme BLUE = builder()
            .primaryColor(Color.valueOf("#1890ff"))
            .successColor(Color.valueOf("#52c41a"))
            .errorColor(Color.valueOf("#ff4d4f"))
            .backgroundColor(Color.valueOf("#f0f5ff"))
            .cardBackgroundColor(Color.WHITE)
            .build();

    /**
     * 绿色主题
     */
    public static final VerifyTheme GREEN = builder()
            .primaryColor(Color.valueOf("#52c41a"))
            .successColor(Color.valueOf("#73d13d"))
            .errorColor(Color.valueOf("#ff4d4f"))
            .backgroundColor(Color.valueOf("#f6ffed"))
            .cardBackgroundColor(Color.WHITE)
            .build();

    // ==================== 构造方法 ====================

    private VerifyTheme(Builder builder) {
        this.primaryColor = builder.primaryColor;
        this.successColor = builder.successColor;
        this.errorColor = builder.errorColor;
        this.warningColor = builder.warningColor;
        this.backgroundColor = builder.backgroundColor;
        this.cardBackgroundColor = builder.cardBackgroundColor;
        this.textColor = builder.textColor;
        this.secondaryTextColor = builder.secondaryTextColor;
        this.borderColor = builder.borderColor;
        this.sliderTrackColor = builder.sliderTrackColor;
        this.sliderThumbColor = builder.sliderThumbColor;
        this.fontFamily = builder.fontFamily;
        this.baseFontSize = builder.baseFontSize;
        this.titleFontSize = builder.titleFontSize;
        this.smallFontSize = builder.smallFontSize;
        this.borderRadius = builder.borderRadius;
        this.padding = builder.padding;
        this.shadowRadius = builder.shadowRadius;
    }

    // ==================== Builder模式 ====================

    /**
     * 创建Builder
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 主题构建器
     */
    public static class Builder {
        private Color primaryColor = Color.valueOf("#1e90ff");
        private Color successColor = Color.valueOf("#52c41a");
        private Color errorColor = Color.valueOf("#ff4d4f");
        private Color warningColor = Color.valueOf("#faad14");
        private Color backgroundColor = Color.valueOf("#f5f5f5");
        private Color cardBackgroundColor = Color.WHITE;
        private Color textColor = Color.valueOf("#333333");
        private Color secondaryTextColor = Color.valueOf("#999999");
        private Color borderColor = Color.valueOf("#e0e0e0");
        private Color sliderTrackColor = Color.valueOf("#f0f0f0");
        private Color sliderThumbColor = Color.WHITE;
        private String fontFamily = "Microsoft YaHei";
        private double baseFontSize = 14;
        private double titleFontSize = 16;
        private double smallFontSize = 12;
        private double borderRadius = 8;
        private double padding = 10;
        private double shadowRadius = 10;

        public Builder primaryColor(Color color) {
            this.primaryColor = color;
            return this;
        }

        public Builder successColor(Color color) {
            this.successColor = color;
            return this;
        }

        public Builder errorColor(Color color) {
            this.errorColor = color;
            return this;
        }

        public Builder warningColor(Color color) {
            this.warningColor = color;
            return this;
        }

        public Builder backgroundColor(Color color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder cardBackgroundColor(Color color) {
            this.cardBackgroundColor = color;
            return this;
        }

        public Builder textColor(Color color) {
            this.textColor = color;
            return this;
        }

        public Builder secondaryTextColor(Color color) {
            this.secondaryTextColor = color;
            return this;
        }

        public Builder borderColor(Color color) {
            this.borderColor = color;
            return this;
        }

        public Builder sliderTrackColor(Color color) {
            this.sliderTrackColor = color;
            return this;
        }

        public Builder sliderThumbColor(Color color) {
            this.sliderThumbColor = color;
            return this;
        }

        public Builder fontFamily(String family) {
            this.fontFamily = family;
            return this;
        }

        public Builder baseFontSize(double size) {
            this.baseFontSize = size;
            return this;
        }

        public Builder titleFontSize(double size) {
            this.titleFontSize = size;
            return this;
        }

        public Builder smallFontSize(double size) {
            this.smallFontSize = size;
            return this;
        }

        public Builder borderRadius(double radius) {
            this.borderRadius = radius;
            return this;
        }

        public Builder padding(double padding) {
            this.padding = padding;
            return this;
        }

        public Builder shadowRadius(double radius) {
            this.shadowRadius = radius;
            return this;
        }

        /**
         * 构建主题
         * @return VerifyTheme实例
         */
        public VerifyTheme build() {
            return new VerifyTheme(this);
        }
    }

    // ==================== 样式生成方法 ====================

    /**
     * 生成卡片样式
     * @return CSS样式字符串
     */
    public String getCardStyle() {
        return String.format(
                "-fx-background-color: %s; " +
                "-fx-background-radius: %.0f; " +
                "-fx-border-color: %s; " +
                "-fx-border-radius: %.0f; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), %.0f, 0, 0, 2);",
                toCssColor(cardBackgroundColor),
                borderRadius,
                toCssColor(borderColor),
                borderRadius,
                shadowRadius
        );
    }

    /**
     * 生成滑块轨道样式
     * @return CSS样式字符串
     */
    public String getSliderTrackStyle() {
        return String.format(
                "-fx-background-color: %s; " +
                "-fx-background-radius: 20;",
                toCssColor(sliderTrackColor)
        );
    }

    /**
     * 生成滑块按钮样式
     * @param isSuccess 是否成功状态
     * @param isFail 是否失败状态
     * @return CSS样式字符串
     */
    public String getSliderThumbStyle(boolean isSuccess, boolean isFail) {
        String bgColor;
        String borderColor;
        if (isSuccess) {
            bgColor = String.format("linear-gradient(to bottom, %s, %s)",
                    toCssColor(successColor.deriveColor(0, 1, 1.2, 1)),
                    toCssColor(successColor));
            borderColor = toCssColor(successColor);
        } else if (isFail) {
            bgColor = String.format("linear-gradient(to bottom, %s, %s)",
                    toCssColor(errorColor.deriveColor(0, 1, 1.2, 1)),
                    toCssColor(errorColor));
            borderColor = toCssColor(errorColor);
        } else {
            bgColor = String.format("linear-gradient(to bottom, %s, %s)",
                    toCssColor(sliderThumbColor),
                    toCssColor(sliderThumbColor.darker()));
            borderColor = toCssColor(this.borderColor);
        }

        return String.format(
                "-fx-background-color: %s; " +
                "-fx-background-radius: 4; " +
                "-fx-border-color: %s; " +
                "-fx-border-radius: 4; " +
                "-fx-cursor: hand;",
                bgColor, borderColor
        );
    }

    /**
     * 生成刷新按钮样式
     * @return CSS样式字符串
     */
    public String getRefreshButtonStyle() {
        return String.format(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: %s; " +
                "-fx-border-color: %s; " +
                "-fx-border-radius: 4; " +
                "-fx-cursor: hand;",
                toCssColor(primaryColor),
                toCssColor(primaryColor)
        );
    }

    /**
     * 生成状态标签样式
     * @param state 验证状态
     * @return CSS样式字符串
     */
    public String getStatusLabelStyle(String state) {
        String color;
        String fontWeight = "normal";
        switch (state) {
            case "SUCCESS":
                color = toCssColor(successColor);
                fontWeight = "bold";
                break;
            case "FAIL":
                color = toCssColor(errorColor);
                fontWeight = "bold";
                break;
            case "VERIFYING":
                color = toCssColor(primaryColor);
                break;
            default:
                color = toCssColor(secondaryTextColor);
        }
        return String.format("-fx-text-fill: %s; -fx-font-size: %.0f; -fx-font-weight: %s;",
                color, baseFontSize, fontWeight);
    }

    /**
     * 将JavaFX Color转换为CSS颜色字符串
     * @param color JavaFX颜色
     * @return CSS颜色字符串
     */
    public static String toCssColor(Color color) {
        if (color == null) {
            return "transparent";
        }
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }

    // ==================== Getter方法 ====================

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public Color getSuccessColor() {
        return successColor;
    }

    public Color getErrorColor() {
        return errorColor;
    }

    public Color getWarningColor() {
        return warningColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getCardBackgroundColor() {
        return cardBackgroundColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Color getSecondaryTextColor() {
        return secondaryTextColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public Color getSliderTrackColor() {
        return sliderTrackColor;
    }

    public Color getSliderThumbColor() {
        return sliderThumbColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public double getBaseFontSize() {
        return baseFontSize;
    }

    public double getTitleFontSize() {
        return titleFontSize;
    }

    public double getSmallFontSize() {
        return smallFontSize;
    }

    public double getBorderRadius() {
        return borderRadius;
    }

    public double getPadding() {
        return padding;
    }

    public double getShadowRadius() {
        return shadowRadius;
    }
}
