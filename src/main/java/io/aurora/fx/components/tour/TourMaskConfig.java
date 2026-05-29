package io.aurora.fx.components.tour;

import javafx.scene.paint.Color;

/**
 * Tour 遮罩配置
 * <p>
 * 用于自定义遮罩层的外观。包括遮罩颜色、不透明度、目标周围空白区域内边距、
 * 目标镂空圆角，以及是否在目标周围显示高亮描边等。
 * </p>
 *
 * <pre>{@code
 * // 默认遮罩
 * TourMaskConfig mask = TourMaskConfig.builder().build();
 *
 * // 自定义半透明红色遮罩 + 大圆角
 * TourMaskConfig mask = TourMaskConfig.builder()
 *     .color(Color.web("#ff4d4f"))
 *     .opacity(0.4)
 *     .padding(8)
 *     .cornerRadius(12)
 *     .build();
 * }</pre>
 *
 * @author Tour Component
 * @version 1.0
 */
public class TourMaskConfig {

    /** 遮罩颜色 */
    private final Color color;
    /** 遮罩不透明度 (0~1) */
    private final double opacity;
    /** 目标外扩 padding（像素），形成镂空空白区 */
    private final double padding;
    /** 目标镂空圆角（像素） */
    private final double cornerRadius;
    /** 是否在目标周围绘制高亮描边 */
    private final boolean highlight;
    /** 高亮描边颜色 */
    private final Color highlightColor;
    /** 高亮描边宽度 */
    private final double highlightWidth;
    /** 是否启用遮罩点击关闭功能（点击非目标区域关闭引导） */
    private final boolean dismissOnMaskClick;

    /** 默认遮罩配置 */
    public static final TourMaskConfig DEFAULT = builder().build();

    private TourMaskConfig(Builder b) {
        this.color = b.color;
        this.opacity = b.opacity;
        this.padding = b.padding;
        this.cornerRadius = b.cornerRadius;
        this.highlight = b.highlight;
        this.highlightColor = b.highlightColor;
        this.highlightWidth = b.highlightWidth;
        this.dismissOnMaskClick = b.dismissOnMaskClick;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ==================== Getters ====================

    public Color getColor() { return color; }
    public double getOpacity() { return opacity; }
    public double getPadding() { return padding; }
    public double getCornerRadius() { return cornerRadius; }
    public boolean isHighlight() { return highlight; }
    public Color getHighlightColor() { return highlightColor; }
    public double getHighlightWidth() { return highlightWidth; }
    public boolean isDismissOnMaskClick() { return dismissOnMaskClick; }

    /**
     * 遮罩配置构建器
     */
    public static class Builder {
        private Color color = Color.web("#000000");
        private double opacity = 0.5;
        private double padding = 4;
        private double cornerRadius = 4;
        private boolean highlight = false;
        private Color highlightColor = Color.web("#FFFFFF", 0.85);
        private double highlightWidth = 2;
        private boolean dismissOnMaskClick = false;

        /**
         * 设置遮罩颜色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder color(Color c) { this.color = c; return this; }

        /**
         * 设置遮罩不透明度
         *
         * @param v 0~1 之间的不透明度
         * @return Builder
         */
        public Builder opacity(double v) { this.opacity = clamp(v, 0, 1); return this; }

        /**
         * 设置目标外扩 padding（像素）
         *
         * @param p 内边距
         * @return Builder
         */
        public Builder padding(double p) { this.padding = Math.max(0, p); return this; }

        /**
         * 设置目标镂空圆角（像素）
         *
         * @param r 圆角半径
         * @return Builder
         */
        public Builder cornerRadius(double r) { this.cornerRadius = Math.max(0, r); return this; }

        /**
         * 是否在目标周围绘制高亮描边
         *
         * @param h true 表示绘制
         * @return Builder
         */
        public Builder highlight(boolean h) { this.highlight = h; return this; }

        /**
         * 设置高亮描边颜色
         *
         * @param c 颜色
         * @return Builder
         */
        public Builder highlightColor(Color c) { this.highlightColor = c; return this; }

        /**
         * 设置高亮描边宽度
         *
         * @param w 宽度
         * @return Builder
         */
        public Builder highlightWidth(double w) { this.highlightWidth = Math.max(0, w); return this; }

        /**
         * 设置点击遮罩区域是否关闭引导
         *
         * @param d true 表示点击遮罩关闭
         * @return Builder
         */
        public Builder dismissOnMaskClick(boolean d) { this.dismissOnMaskClick = d; return this; }

        /**
         * 构建遮罩配置实例
         *
         * @return TourMaskConfig
         */
        public TourMaskConfig build() {
            return new TourMaskConfig(this);
        }

        private static double clamp(double v, double lo, double hi) {
            return Math.max(lo, Math.min(hi, v));
        }
    }
}
