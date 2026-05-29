package io.aurora.fx.components.tour;

import javafx.scene.Node;

import java.util.function.Consumer;

/**
 * Tour 组件工厂类
 * <p>
 * 提供统一的 Tour 与演示组件创建入口，支持 Builder 模式快速配置。
 * </p>
 *
 * <h3>基础用法</h3>
 * <pre>{@code
 * // 1. 直接创建 Tour
 * Tour tour = TourFactory.createTour();
 *
 * // 2. Builder 模式
 * Tour tour = TourFactory.builder()
 *     .mask(true)
 *     .type(TourType.DEFAULT)
 *     .theme(TourTheme.DEFAULT)
 *     .step(button1, "标题1", "描述1", TourPlacement.BOTTOM)
 *     .step(button2, "标题2", "描述2", TourPlacement.RIGHT)
 *     .onFinish(() -> System.out.println("done"))
 *     .build();
 *
 * // 3. 演示 Pane
 * BasicTourPane basic = TourFactory.createBasic();
 * }</pre>
 *
 * @author Tour Component
 * @version 1.0
 */
public final class TourFactory {

    private TourFactory() {}

    // ==================== 快速创建 Tour ====================

    /**
     * 创建空 Tour
     *
     * @return Tour 实例
     */
    public static Tour createTour() {
        return new Tour();
    }

    /**
     * 创建一个 Builder
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== 快速创建演示组件 ====================

    /**
     * 创建基础用法演示
     *
     * @return BasicTourPane
     */
    public static BasicTourPane createBasic() {
        return new BasicTourPane();
    }

    /**
     * 创建非模态演示
     *
     * @return NonModalTourPane
     */
    public static NonModalTourPane createNonModal() {
        return new NonModalTourPane();
    }

    /**
     * 创建定位演示
     *
     * @return PlacementTourPane
     */
    public static PlacementTourPane createPlacement() {
        return new PlacementTourPane();
    }

    /**
     * 创建自定义遮罩演示
     *
     * @return CustomMaskTourPane
     */
    public static CustomMaskTourPane createCustomMask() {
        return new CustomMaskTourPane();
    }

    /**
     * 创建自定义指示器演示
     *
     * @return CustomIndicatorTourPane
     */
    public static CustomIndicatorTourPane createCustomIndicator() {
        return new CustomIndicatorTourPane();
    }

    /**
     * 创建中央显示演示（target 为空）
     *
     * @return CenterTourPane
     */
    public static CenterTourPane createCenter() {
        return new CenterTourPane();
    }

    /**
     * 创建交互式演示
     *
     * @return InteractiveTourPane
     */
    public static InteractiveTourPane createInteractive() {
        return new InteractiveTourPane();
    }

    // ==================== Builder ====================

    /**
     * Tour 构建器
     */
    public static class Builder {
        private final Tour tour = new Tour();

        /**
         * 设置遮罩开关
         *
         * @param show true=模态 / false=非模态
         * @return Builder
         */
        public Builder mask(boolean show) { tour.mask(show); return this; }

        /**
         * 设置遮罩配置
         *
         * @param cfg 遮罩配置
         * @return Builder
         */
        public Builder maskConfig(TourMaskConfig cfg) { tour.maskConfig(cfg); return this; }

        /**
         * 设置主题
         *
         * @param th 主题
         * @return Builder
         */
        public Builder theme(TourTheme th) { tour.theme(th); return this; }

        /**
         * 设置弹窗类型
         *
         * @param t 类型
         * @return Builder
         */
        public Builder type(TourType t) { tour.type(t); return this; }

        /**
         * 设置是否显示关闭按钮
         *
         * @param show 是否显示
         * @return Builder
         */
        public Builder showClose(boolean show) { tour.showClose(show); return this; }

        /**
         * 设置是否显示箭头
         *
         * @param show 是否显示
         * @return Builder
         */
        public Builder showArrow(boolean show) { tour.showArrow(show); return this; }

        /**
         * 设置是否显示指示器
         *
         * @param show 是否显示
         * @return Builder
         */
        public Builder showIndicators(boolean show) { tour.showIndicators(show); return this; }

        /**
         * 设置 ESC 是否关闭
         *
         * @param close 是否关闭
         * @return Builder
         */
        public Builder closeOnEsc(boolean close) { tour.closeOnEsc(close); return this; }

        /**
         * 设置上一步按钮文案
         *
         * @param text 文案
         * @return Builder
         */
        public Builder prevButtonText(String text) { tour.prevButtonText(text); return this; }

        /**
         * 设置下一步按钮文案
         *
         * @param text 文案
         * @return Builder
         */
        public Builder nextButtonText(String text) { tour.nextButtonText(text); return this; }

        /**
         * 设置完成按钮文案
         *
         * @param text 文案
         * @return Builder
         */
        public Builder finishButtonText(String text) { tour.finishButtonText(text); return this; }

        /**
         * 添加一个步骤对象
         *
         * @param step 步骤
         * @return Builder
         */
        public Builder step(TourStep step) { tour.addStep(step); return this; }

        /**
         * 快捷添加步骤（基于 Node + 默认底部定位）
         *
         * @param target      目标节点
         * @param title       标题
         * @param description 描述
         * @return Builder
         */
        public Builder step(Node target, String title, String description) {
            return step(new TourStep(target, title, description));
        }

        /**
         * 快捷添加步骤（基于 Node + 指定定位）
         *
         * @param target      目标节点
         * @param title       标题
         * @param description 描述
         * @param placement   定位
         * @return Builder
         */
        public Builder step(Node target, String title, String description, TourPlacement placement) {
            return step(new TourStep(target, title, description).placement(placement));
        }

        /**
         * 注册引导开始回调
         *
         * @param r 回调
         * @return Builder
         */
        public Builder onOpen(Runnable r) { tour.onOpen(r); return this; }

        /**
         * 注册引导关闭回调
         *
         * @param r 回调
         * @return Builder
         */
        public Builder onClose(Runnable r) { tour.onClose(r); return this; }

        /**
         * 注册引导完成回调
         *
         * @param r 回调
         * @return Builder
         */
        public Builder onFinish(Runnable r) { tour.onFinish(r); return this; }

        /**
         * 注册步骤变化回调
         *
         * @param c 回调
         * @return Builder
         */
        public Builder onChange(Consumer<Integer> c) { tour.onChange(c); return this; }

        /**
         * 构建 Tour 实例
         *
         * @return Tour
         */
        public Tour build() {
            return tour;
        }
    }
}
