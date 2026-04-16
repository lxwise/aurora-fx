package io.aurora.fx.components.steps;

import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * Steps 演示组件工厂类
 * <p>
 * 提供统一的组件创建入口，支持 Builder 模式配置，开箱即用。
 * 所有演示组件都可以通过此工厂类快速创建和配置。
 * </p>
 *
 * <h3>基础用法</h3>
 * <pre>{@code
 * // 快速创建默认组件
 * BasicStepsPane basic = StepsComponentFactory.createBasic();
 *
 * // 使用 Builder 模式配置
 * BasicStepsPane custom = StepsComponentFactory.builder()
 *     .title("自定义标题")
 *     .active(2)
 *     .finishStatus(StepStatus.SUCCESS)
 *     .buildBasic();
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public final class StepsComponentFactory {

    private StepsComponentFactory() {
        // 私有构造函数，防止实例化
    }

    // ==================== 快速创建方法 ====================

    /**
     * 创建基础用法演示组件（默认配置）
     *
     * @return BasicStepsPane 实例
     */
    public static BasicStepsPane createBasic() {
        return new BasicStepsPane();
    }

    /**
     * 创建含状态的步骤条演示组件（默认配置）
     *
     * @return StatusStepsPane 实例
     */
    public static StatusStepsPane createStatus() {
        return new StatusStepsPane();
    }

    /**
     * 创建居中的步骤条演示组件（默认配置）
     *
     * @return CenterStepsPane 实例
     */
    public static CenterStepsPane createCenter() {
        return new CenterStepsPane();
    }

    /**
     * 创建带描述的步骤栏演示组件（默认配置）
     *
     * @return DescriptionStepsPane 实例
     */
    public static DescriptionStepsPane createDescription() {
        return new DescriptionStepsPane();
    }

    /**
     * 创建带图标的步骤条演示组件（默认配置）
     *
     * @return IconStepsPane 实例
     */
    public static IconStepsPane createIcon() {
        return new IconStepsPane();
    }

    /**
     * 创建垂直的步骤条演示组件（默认配置）
     *
     * @return VerticalStepsPane 实例
     */
    public static VerticalStepsPane createVertical() {
        return new VerticalStepsPane();
    }

    /**
     * 创建简洁风格的步骤条演示组件（默认配置）
     *
     * @return SimpleStepsPane 实例
     */
    public static SimpleStepsPane createSimple() {
        return new SimpleStepsPane();
    }

    /**
     * 创建主题演示组件（默认配置）
     *
     * @return ThemeStepsPane 实例
     */
    public static ThemeStepsPane createTheme() {
        return new ThemeStepsPane();
    }

    /**
     * 创建交互式演示组件（默认配置）
     *
     * @return InteractiveStepsPane 实例
     */
    public static InteractiveStepsPane createInteractive() {
        return new InteractiveStepsPane();
    }

    // ==================== Builder 模式 ====================

    /**
     * 创建 Builder 实例
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Steps 组件配置构建器
     * <p>
     * 使用 Builder 模式配置和创建各类型 Steps 演示组件，支持链式调用。
     * </p>
     */
    public static class Builder {
        // 通用配置
        private String title;
        private String description;
        private int active = 0;
        private StepStatus finishStatus = StepStatus.SUCCESS;
        private StepStatus processStatus = StepStatus.PROCESS;
        private StepsTheme theme = StepsTheme.DEFAULT;
        private Consumer<Integer> onChangeCallback;
        private Consumer<Integer> onStepClickCallback;

        // 特定配置
        private double space = -1; // -1 表示使用默认
        private boolean alignCenter = false;
        private boolean simple = false;

        // 步骤配置
        private Step[] steps;

        /**
         * 设置卡片标题
         *
         * @param title 标题文本
         * @return Builder实例
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * 设置卡片描述
         *
         * @param description 描述文本
         * @return Builder实例
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * 设置当前激活步骤索引
         *
         * @param active 激活步骤索引（从0开始）
         * @return Builder实例
         */
        public Builder active(int active) {
            this.active = active;
            return this;
        }

        /**
         * 设置已完成步骤的显示状态
         *
         * @param status 完成状态（SUCCESS/FINISH等）
         * @return Builder实例
         */
        public Builder finishStatus(StepStatus status) {
            this.finishStatus = status;
            return this;
        }

        /**
         * 设置当前步骤的显示状态
         *
         * @param status 进行中状态（PROCESS等）
         * @return Builder实例
         */
        public Builder processStatus(StepStatus status) {
            this.processStatus = status;
            return this;
        }

        /**
         * 设置主题
         *
         * @param theme 主题配置对象
         * @return Builder实例
         */
        public Builder theme(StepsTheme theme) {
            this.theme = theme;
            return this;
        }

        /**
         * 设置步骤变化监听
         *
         * @param callback 步骤变化时的回调函数
         * @return Builder实例
         */
        public Builder onChange(Consumer<Integer> callback) {
            this.onChangeCallback = callback;
            return this;
        }

        /**
         * 设置步骤点击监听
         *
         * @param callback 步骤被点击时的回调函数
         * @return Builder实例
         */
        public Builder onStepClick(Consumer<Integer> callback) {
            this.onStepClickCallback = callback;
            return this;
        }

        /**
         * 设置固定步距
         *
         * @param space 步间距（像素），-1表示自动
         * @return Builder实例
         */
        public Builder space(double space) {
            this.space = space;
            return this;
        }

        /**
         * 设置是否居中对齐
         *
         * @param alignCenter 是否居中
         * @return Builder实例
         */
        public Builder alignCenter(boolean alignCenter) {
            this.alignCenter = alignCenter;
            return this;
        }

        /**
         * 设置是否简洁模式
         *
         * @param simple 是否简洁模式
         * @return Builder实例
         */
        public Builder simple(boolean simple) {
            this.simple = simple;
            return this;
        }

        /**
         * 设置步骤列表
         *
         * @param steps 步骤数组
         * @return Builder实例
         */
        public Builder steps(Step... steps) {
            this.steps = steps;
            return this;
        }

        // ==================== 构建各类型组件 ====================

        /**
         * 构建基础用法组件
         *
         * @return 配置完成的 BasicStepsPane 实例
         */
        public BasicStepsPane buildBasic() {
            return new BasicStepsPane(title, description, active, finishStatus, onChangeCallback);
        }

        /**
         * 构建含状态的步骤条组件
         *
         * @return 配置完成的 StatusStepsPane 实例
         */
        public StatusStepsPane buildStatus() {
            return new StatusStepsPane(title, description, active, finishStatus, space);
        }

        /**
         * 构建居中的步骤条组件
         *
         * @return 配置完成的 CenterStepsPane 实例
         */
        public CenterStepsPane buildCenter() {
            return new CenterStepsPane(title, description, active, alignCenter);
        }

        /**
         * 构建带描述的步骤栏组件
         *
         * @return 配置完成的 DescriptionStepsPane 实例
         */
        public DescriptionStepsPane buildDescription() {
            return new DescriptionStepsPane(title, description, active);
        }

        /**
         * 构建带图标的步骤条组件
         *
         * @return 配置完成的 IconStepsPane 实例
         */
        public IconStepsPane buildIcon() {
            return new IconStepsPane(title, description, active);
        }

        /**
         * 构建垂直的步骤条组件
         *
         * @return 配置完成的 VerticalStepsPane 实例
         */
        public VerticalStepsPane buildVertical() {
            return new VerticalStepsPane(title, description, active);
        }

        /**
         * 构建简洁风格的步骤条组件
         *
         * @return 配置完成的 SimpleStepsPane 实例
         */
        public SimpleStepsPane buildSimple() {
            return new SimpleStepsPane(title, description, active, finishStatus);
        }

        /**
         * 构建主题演示组件
         *
         * @return 配置完成的 ThemeStepsPane 实例
         */
        public ThemeStepsPane buildTheme() {
            return new ThemeStepsPane(title, description);
        }

        /**
         * 构建交互式演示组件
         *
         * @return 配置完成的 InteractiveStepsPane 实例
         */
        public InteractiveStepsPane buildInteractive() {
            InteractiveStepsPane pane = new InteractiveStepsPane(title, description, active, finishStatus);
            if (onChangeCallback != null) {
                pane.getSteps().onChange(onChangeCallback);
            }
            if (onStepClickCallback != null) {
                pane.getSteps().onStepClick(onStepClickCallback);
            }
            return pane;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 创建 SVG 图标节点
     *
     * @param svgPath SVG 路径
     * @param color   填充颜色
     * @return 图标节点
     */
    public static Node createSvgIcon(String svgPath, Color color) {
        return IconStepsPane.createSvgIcon(svgPath, color);
    }

    /**
     * 创建默认的步骤数组
     *
     * @param count 步骤数量
     * @return 步骤数组
     */
    public static Step[] createDefaultSteps(int count) {
        Step[] steps = new Step[count];
        for (int i = 0; i < count; i++) {
            steps[i] = new Step("Step " + (i + 1));
        }
        return steps;
    }
}
