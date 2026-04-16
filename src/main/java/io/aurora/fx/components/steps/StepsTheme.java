package io.aurora.fx.components.steps;

import javafx.scene.paint.Color;

/**
 * Steps 步骤条主题配置
 * <p>
 * 通过 Builder 模式提供丰富的主题定制能力，支持颜色、字体、间距等全方位配置。
 * 内置多套预设主题（DEFAULT / DARK / BLUE / GREEN），可直接使用或基于预设进行二次定制。
 * </p>
 *
 * <pre>{@code
 * // 使用预设主题
 * StepsTheme theme = StepsTheme.DARK;
 *
 * // 自定义主题
 * StepsTheme custom = StepsTheme.builder()
 *     .primaryColor(Color.valueOf("#409EFF"))
 *     .successColor(Color.valueOf("#67C23A"))
 *     .build();
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class StepsTheme {

    // ==================== 颜色配置 ====================

    /** 主色调 - 用于进行中步骤 */
    private final Color primaryColor;
    /** 成功色 */
    private final Color successColor;
    /** 错误色 */
    private final Color errorColor;
    /** 警告色 */
    private final Color warningColor;
    /** 等待状态文字颜色 */
    private final Color waitColor;
    /** 完成状态文字颜色 */
    private final Color finishColor;
    /** 主文字颜色 */
    private final Color textColor;
    /** 描述文字颜色 */
    private final Color descriptionColor;
    /** 连接线颜色 */
    private final Color lineColor;
    /** 完成步骤的连接线颜色 */
    private final Color finishLineColor;
    /** 背景颜色 */
    private final Color backgroundColor;

    // ==================== 字体配置 ====================

    /** 字体族 */
    private final String fontFamily;
    /** 标题字体大小 */
    private final double titleFontSize;
    /** 描述字体大小 */
    private final double descriptionFontSize;
    /** 图标字体大小 */
    private final double iconFontSize;

    // ==================== 尺寸配置 ====================

    /** 步骤图标尺寸 */
    private final double iconSize;
    /** 连接线高度（水平模式）/宽度（垂直模式） */
    private final double lineHeight;
    /** 简洁模式箭头大小 */
    private final double simpleArrowSize;
    /** 步骤间内边距 */
    private final double stepPadding;

    // ==================== 预设主题 ====================

    /** 默认主题 - Element UI 风格 */
    public static final StepsTheme DEFAULT = builder().build();

    /** 深色主题 */
    public static final StepsTheme DARK = builder()
            .primaryColor(Color.valueOf("#409EFF"))
            .successColor(Color.valueOf("#67C23A"))
            .errorColor(Color.valueOf("#F56C6C"))
            .warningColor(Color.valueOf("#E6A23C"))
            .waitColor(Color.valueOf("#909399"))
            .finishColor(Color.valueOf("#409EFF"))
            .textColor(Color.valueOf("#E0E0E0"))
            .descriptionColor(Color.valueOf("#A0A0A0"))
            .lineColor(Color.valueOf("#4A4A4A"))
            .finishLineColor(Color.valueOf("#409EFF"))
            .backgroundColor(Color.valueOf("#1E1E1E"))
            .build();

    /** 蓝色主题 */
    public static final StepsTheme BLUE = builder()
            .primaryColor(Color.valueOf("#1890FF"))
            .successColor(Color.valueOf("#52C41A"))
            .finishColor(Color.valueOf("#1890FF"))
            .finishLineColor(Color.valueOf("#1890FF"))
            .build();

    /** 绿色主题 */
    public static final StepsTheme GREEN = builder()
            .primaryColor(Color.valueOf("#52C41A"))
            .successColor(Color.valueOf("#73D13D"))
            .finishColor(Color.valueOf("#52C41A"))
            .finishLineColor(Color.valueOf("#52C41A"))
            .build();

    // ==================== 构造方法 ====================

    private StepsTheme(Builder builder) {
        this.primaryColor = builder.primaryColor;
        this.successColor = builder.successColor;
        this.errorColor = builder.errorColor;
        this.warningColor = builder.warningColor;
        this.waitColor = builder.waitColor;
        this.finishColor = builder.finishColor;
        this.textColor = builder.textColor;
        this.descriptionColor = builder.descriptionColor;
        this.lineColor = builder.lineColor;
        this.finishLineColor = builder.finishLineColor;
        this.backgroundColor = builder.backgroundColor;
        this.fontFamily = builder.fontFamily;
        this.titleFontSize = builder.titleFontSize;
        this.descriptionFontSize = builder.descriptionFontSize;
        this.iconFontSize = builder.iconFontSize;
        this.iconSize = builder.iconSize;
        this.lineHeight = builder.lineHeight;
        this.simpleArrowSize = builder.simpleArrowSize;
        this.stepPadding = builder.stepPadding;
    }

    // ==================== Builder ====================

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 主题构建器
     * <p>
     * 使用 Builder 模式创建 StepsTheme 实例，支持链式调用配置各项主题属性。
     * </p>
     */
    public static class Builder {
        private Color primaryColor = Color.valueOf("#409EFF");
        private Color successColor = Color.valueOf("#67C23A");
        private Color errorColor = Color.valueOf("#F56C6C");
        private Color warningColor = Color.valueOf("#E6A23C");
        private Color waitColor = Color.valueOf("#C0C4CC");
        private Color finishColor = Color.valueOf("#303133");
        private Color textColor = Color.valueOf("#303133");
        private Color descriptionColor = Color.valueOf("#909399");
        private Color lineColor = Color.valueOf("#C0C4CC");
        private Color finishLineColor = Color.valueOf("#409EFF");
        private Color backgroundColor = Color.TRANSPARENT;
        private String fontFamily = "System";
        private double titleFontSize = 14;
        private double descriptionFontSize = 12;
        private double iconFontSize = 14;
        private double iconSize = 24;
        private double lineHeight = 2;
        private double simpleArrowSize = 12;
        private double stepPadding = 10;

        /**
         * 设置主色调（用于进行中步骤）
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder primaryColor(Color c) { this.primaryColor = c; return this; }

        /**
         * 设置成功色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder successColor(Color c) { this.successColor = c; return this; }

        /**
         * 设置错误色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder errorColor(Color c) { this.errorColor = c; return this; }

        /**
         * 设置警告色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder warningColor(Color c) { this.warningColor = c; return this; }

        /**
         * 设置等待状态颜色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder waitColor(Color c) { this.waitColor = c; return this; }

        /**
         * 设置完成状态颜色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder finishColor(Color c) { this.finishColor = c; return this; }

        /**
         * 设置主文字颜色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder textColor(Color c) { this.textColor = c; return this; }

        /**
         * 设置描述文字颜色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder descriptionColor(Color c) { this.descriptionColor = c; return this; }

        /**
         * 设置连接线颜色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder lineColor(Color c) { this.lineColor = c; return this; }

        /**
         * 设置完成步骤的连接线颜色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder finishLineColor(Color c) { this.finishLineColor = c; return this; }

        /**
         * 设置背景颜色
         *
         * @param c 颜色值
         * @return Builder实例
         */
        public Builder backgroundColor(Color c) { this.backgroundColor = c; return this; }

        /**
         * 设置字体族
         *
         * @param f 字体名称
         * @return Builder实例
         */
        public Builder fontFamily(String f) { this.fontFamily = f; return this; }

        /**
         * 设置标题字体大小
         *
         * @param s 字体大小（像素）
         * @return Builder实例
         */
        public Builder titleFontSize(double s) { this.titleFontSize = s; return this; }

        /**
         * 设置描述字体大小
         *
         * @param s 字体大小（像素）
         * @return Builder实例
         */
        public Builder descriptionFontSize(double s) { this.descriptionFontSize = s; return this; }

        /**
         * 设置图标字体大小
         *
         * @param s 字体大小（像素）
         * @return Builder实例
         */
        public Builder iconFontSize(double s) { this.iconFontSize = s; return this; }

        /**
         * 设置步骤图标尺寸
         *
         * @param s 图标尺寸（像素）
         * @return Builder实例
         */
        public Builder iconSize(double s) { this.iconSize = s; return this; }

        /**
         * 设置连接线高度/宽度
         *
         * @param h 线条粗细（像素）
         * @return Builder实例
         */
        public Builder lineHeight(double h) { this.lineHeight = h; return this; }

        /**
         * 设置简洁模式箭头大小
         *
         * @param s 箭头大小（像素）
         * @return Builder实例
         */
        public Builder simpleArrowSize(double s) { this.simpleArrowSize = s; return this; }

        /**
         * 设置步骤间内边距
         *
         * @param p 内边距（像素）
         * @return Builder实例
         */
        public Builder stepPadding(double p) { this.stepPadding = p; return this; }

        /**
         * 构建 StepsTheme 实例
         *
         * @return 配置完成的 StepsTheme 实例
         */
        public StepsTheme build() {
            return new StepsTheme(this);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 根据步骤状态获取对应颜色
     *
     * @param status 步骤状态
     * @return 对应的颜色
     */
    public Color getColorForStatus(StepStatus status) {
        if (status == null) {
            return waitColor;
        }
        switch (status) {
            case PROCESS: return primaryColor;
            case SUCCESS: return successColor;
            case ERROR: return errorColor;
            case FINISH: return finishColor;
            case WAIT:
            default: return waitColor;
        }
    }

    /**
     * 将 JavaFX Color 转为 CSS 颜色字符串
     */
    public static String toCssColor(Color color) {
        if (color == null) return "transparent";
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }

    // ==================== Getters ====================

    public Color getPrimaryColor() { return primaryColor; }
    public Color getSuccessColor() { return successColor; }
    public Color getErrorColor() { return errorColor; }
    public Color getWarningColor() { return warningColor; }
    public Color getWaitColor() { return waitColor; }
    public Color getFinishColor() { return finishColor; }
    public Color getTextColor() { return textColor; }
    public Color getDescriptionColor() { return descriptionColor; }
    public Color getLineColor() { return lineColor; }
    public Color getFinishLineColor() { return finishLineColor; }
    public Color getBackgroundColor() { return backgroundColor; }
    public String getFontFamily() { return fontFamily; }
    public double getTitleFontSize() { return titleFontSize; }
    public double getDescriptionFontSize() { return descriptionFontSize; }
    public double getIconFontSize() { return iconFontSize; }
    public double getIconSize() { return iconSize; }
    public double getLineHeight() { return lineHeight; }
    public double getSimpleArrowSize() { return simpleArrowSize; }
    public double getStepPadding() { return stepPadding; }
}
