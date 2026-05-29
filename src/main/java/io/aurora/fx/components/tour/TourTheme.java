package io.aurora.fx.components.tour;

import javafx.scene.paint.Color;

/**
 * Tour 主题配置
 * <p>
 * 通过 Builder 模式提供完整的主题定制能力，包括弹窗背景、文字颜色、字体、按钮样式等。
 * 内置多套预设主题（DEFAULT / DARK / PRIMARY_BLUE / PRIMARY_GREEN）。
 * </p>
 *
 * <pre>{@code
 * // 使用预设
 * TourTheme theme = TourTheme.DARK;
 *
 * // 自定义
 * TourTheme custom = TourTheme.builder()
 *     .primaryColor(Color.web("#1890ff"))
 *     .popupBackground(Color.WHITE)
 *     .build();
 * }</pre>
 *
 * @author Tour Component
 * @version 1.0
 */
public class TourTheme {

    // 颜色配置
    private final Color primaryColor;
    private final Color popupBackground;
    private final Color popupBorderColor;
    private final Color titleColor;
    private final Color descriptionColor;
    private final Color buttonTextColor;
    private final Color secondaryButtonBg;
    private final Color secondaryButtonText;
    private final Color closeIconColor;
    private final Color indicatorActiveColor;
    private final Color indicatorInactiveColor;
    private final Color arrowFillColor;

    // 字体配置
    private final String fontFamily;
    private final double titleFontSize;
    private final double descriptionFontSize;
    private final double buttonFontSize;
    private final double indicatorFontSize;

    // 尺寸配置
    private final double popupMinWidth;
    private final double popupMaxWidth;
    private final double popupCornerRadius;
    private final double popupPadding;
    private final double popupArrowSize;
    private final double popupOffset;
    private final double indicatorDotSize;
    private final double dropShadowRadius;

    /** 默认主题（白底） */
    public static final TourTheme DEFAULT = builder().build();

    /** 暗色主题 */
    public static final TourTheme DARK = builder()
            .primaryColor(Color.web("#409EFF"))
            .popupBackground(Color.web("#2B2B2B"))
            .popupBorderColor(Color.web("#3F3F3F"))
            .titleColor(Color.web("#F0F0F0"))
            .descriptionColor(Color.web("#BFBFBF"))
            .secondaryButtonBg(Color.web("#3A3A3A"))
            .secondaryButtonText(Color.web("#E0E0E0"))
            .closeIconColor(Color.web("#BFBFBF"))
            .indicatorInactiveColor(Color.web("#5A5A5A"))
            .indicatorActiveColor(Color.web("#409EFF"))
            .arrowFillColor(Color.web("#2B2B2B"))
            .build();

    /** 蓝色主题 */
    public static final TourTheme PRIMARY_BLUE = builder()
            .primaryColor(Color.web("#1890FF"))
            .indicatorActiveColor(Color.web("#1890FF"))
            .build();

    /** 绿色主题 */
    public static final TourTheme PRIMARY_GREEN = builder()
            .primaryColor(Color.web("#52C41A"))
            .indicatorActiveColor(Color.web("#52C41A"))
            .build();

    private TourTheme(Builder b) {
        this.primaryColor = b.primaryColor;
        this.popupBackground = b.popupBackground;
        this.popupBorderColor = b.popupBorderColor;
        this.titleColor = b.titleColor;
        this.descriptionColor = b.descriptionColor;
        this.buttonTextColor = b.buttonTextColor;
        this.secondaryButtonBg = b.secondaryButtonBg;
        this.secondaryButtonText = b.secondaryButtonText;
        this.closeIconColor = b.closeIconColor;
        this.indicatorActiveColor = b.indicatorActiveColor;
        this.indicatorInactiveColor = b.indicatorInactiveColor;
        this.arrowFillColor = b.arrowFillColor;
        this.fontFamily = b.fontFamily;
        this.titleFontSize = b.titleFontSize;
        this.descriptionFontSize = b.descriptionFontSize;
        this.buttonFontSize = b.buttonFontSize;
        this.indicatorFontSize = b.indicatorFontSize;
        this.popupMinWidth = b.popupMinWidth;
        this.popupMaxWidth = b.popupMaxWidth;
        this.popupCornerRadius = b.popupCornerRadius;
        this.popupPadding = b.popupPadding;
        this.popupArrowSize = b.popupArrowSize;
        this.popupOffset = b.popupOffset;
        this.indicatorDotSize = b.indicatorDotSize;
        this.dropShadowRadius = b.dropShadowRadius;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ==================== 工具方法 ====================

    /**
     * 将 JavaFX Color 转为 CSS 颜色字符串
     *
     * @param color 颜色
     * @return CSS rgba 字符串
     */
    public static String toCssColor(Color color) {
        if (color == null) {
            return "transparent";
        }
        return String.format("rgba(%d,%d,%d,%.3f)",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255),
                color.getOpacity());
    }

    // ==================== Getters ====================

    public Color getPrimaryColor() { return primaryColor; }
    public Color getPopupBackground() { return popupBackground; }
    public Color getPopupBorderColor() { return popupBorderColor; }
    public Color getTitleColor() { return titleColor; }
    public Color getDescriptionColor() { return descriptionColor; }
    public Color getButtonTextColor() { return buttonTextColor; }
    public Color getSecondaryButtonBg() { return secondaryButtonBg; }
    public Color getSecondaryButtonText() { return secondaryButtonText; }
    public Color getCloseIconColor() { return closeIconColor; }
    public Color getIndicatorActiveColor() { return indicatorActiveColor; }
    public Color getIndicatorInactiveColor() { return indicatorInactiveColor; }
    public Color getArrowFillColor() { return arrowFillColor; }

    public String getFontFamily() { return fontFamily; }
    public double getTitleFontSize() { return titleFontSize; }
    public double getDescriptionFontSize() { return descriptionFontSize; }
    public double getButtonFontSize() { return buttonFontSize; }
    public double getIndicatorFontSize() { return indicatorFontSize; }

    public double getPopupMinWidth() { return popupMinWidth; }
    public double getPopupMaxWidth() { return popupMaxWidth; }
    public double getPopupCornerRadius() { return popupCornerRadius; }
    public double getPopupPadding() { return popupPadding; }
    public double getPopupArrowSize() { return popupArrowSize; }
    public double getPopupOffset() { return popupOffset; }
    public double getIndicatorDotSize() { return indicatorDotSize; }
    public double getDropShadowRadius() { return dropShadowRadius; }

    /**
     * 主题构建器
     */
    public static class Builder {
        private Color primaryColor = Color.web("#409EFF");
        private Color popupBackground = Color.WHITE;
        private Color popupBorderColor = Color.web("#EBEEF5");
        private Color titleColor = Color.web("#303133");
        private Color descriptionColor = Color.web("#606266");
        private Color buttonTextColor = Color.WHITE;
        private Color secondaryButtonBg = Color.web("#F4F4F5");
        private Color secondaryButtonText = Color.web("#606266");
        private Color closeIconColor = Color.web("#909399");
        private Color indicatorActiveColor = Color.web("#409EFF");
        private Color indicatorInactiveColor = Color.web("#DCDFE6");
        private Color arrowFillColor = Color.WHITE;

        private String fontFamily = "Microsoft YaHei";
        private double titleFontSize = 15;
        private double descriptionFontSize = 13;
        private double buttonFontSize = 12;
        private double indicatorFontSize = 12;

        private double popupMinWidth = 260;
        private double popupMaxWidth = 360;
        private double popupCornerRadius = 8;
        private double popupPadding = 16;
        private double popupArrowSize = 8;
        private double popupOffset = 12;
        private double indicatorDotSize = 6;
        private double dropShadowRadius = 12;

        /**
         * 设置主色调
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder primaryColor(Color c) { this.primaryColor = c; return this; }

        /**
         * 设置弹窗背景色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder popupBackground(Color c) {
            this.popupBackground = c;
            this.arrowFillColor = c;
            return this;
        }

        /**
         * 设置弹窗边框色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder popupBorderColor(Color c) { this.popupBorderColor = c; return this; }

        /**
         * 设置标题色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder titleColor(Color c) { this.titleColor = c; return this; }

        /**
         * 设置描述色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder descriptionColor(Color c) { this.descriptionColor = c; return this; }

        /**
         * 设置主按钮文字色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder buttonTextColor(Color c) { this.buttonTextColor = c; return this; }

        /**
         * 设置次按钮背景色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder secondaryButtonBg(Color c) { this.secondaryButtonBg = c; return this; }

        /**
         * 设置次按钮文字色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder secondaryButtonText(Color c) { this.secondaryButtonText = c; return this; }

        /**
         * 设置关闭按钮颜色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder closeIconColor(Color c) { this.closeIconColor = c; return this; }

        /**
         * 设置指示器激活色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder indicatorActiveColor(Color c) { this.indicatorActiveColor = c; return this; }

        /**
         * 设置指示器非激活色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder indicatorInactiveColor(Color c) { this.indicatorInactiveColor = c; return this; }

        /**
         * 设置箭头填充色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder arrowFillColor(Color c) { this.arrowFillColor = c; return this; }

        /**
         * 设置字体族
         *
         * @param f 字体名
         * @return Builder
         */
        public Builder fontFamily(String f) { this.fontFamily = f; return this; }

        /**
         * 设置标题字号
         *
         * @param s 字号
         * @return Builder
         */
        public Builder titleFontSize(double s) { this.titleFontSize = s; return this; }

        /**
         * 设置描述字号
         *
         * @param s 字号
         * @return Builder
         */
        public Builder descriptionFontSize(double s) { this.descriptionFontSize = s; return this; }

        /**
         * 设置按钮字号
         *
         * @param s 字号
         * @return Builder
         */
        public Builder buttonFontSize(double s) { this.buttonFontSize = s; return this; }

        /**
         * 设置指示器字号
         *
         * @param s 字号
         * @return Builder
         */
        public Builder indicatorFontSize(double s) { this.indicatorFontSize = s; return this; }

        /**
         * 设置弹窗最小宽度
         *
         * @param w 宽度
         * @return Builder
         */
        public Builder popupMinWidth(double w) { this.popupMinWidth = w; return this; }

        /**
         * 设置弹窗最大宽度
         *
         * @param w 宽度
         * @return Builder
         */
        public Builder popupMaxWidth(double w) { this.popupMaxWidth = w; return this; }

        /**
         * 设置弹窗圆角
         *
         * @param r 圆角
         * @return Builder
         */
        public Builder popupCornerRadius(double r) { this.popupCornerRadius = r; return this; }

        /**
         * 设置弹窗内边距
         *
         * @param p 内边距
         * @return Builder
         */
        public Builder popupPadding(double p) { this.popupPadding = p; return this; }

        /**
         * 设置箭头大小
         *
         * @param s 大小
         * @return Builder
         */
        public Builder popupArrowSize(double s) { this.popupArrowSize = s; return this; }

        /**
         * 设置弹窗与目标的距离偏移
         *
         * @param o 偏移
         * @return Builder
         */
        public Builder popupOffset(double o) { this.popupOffset = o; return this; }

        /**
         * 设置指示器圆点大小
         *
         * @param s 大小
         * @return Builder
         */
        public Builder indicatorDotSize(double s) { this.indicatorDotSize = s; return this; }

        /**
         * 设置投影模糊半径
         *
         * @param r 模糊半径
         * @return Builder
         */
        public Builder dropShadowRadius(double r) { this.dropShadowRadius = r; return this; }

        /**
         * 构建主题
         *
         * @return TourTheme
         */
        public TourTheme build() {
            return new TourTheme(this);
        }
    }
}
