package io.aurora.fx.components.steps;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * 简洁风格的步骤条组件
 * <p>
 * 演示简洁模式的步骤条用法。
 * 简洁模式下 alignCenter、description、direction、space 属性失效，
 * 呈现更紧凑的布局。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * SimpleStepsPane pane = new SimpleStepsPane();
 *
 * // 自定义配置
 * SimpleStepsPane pane = new SimpleStepsPane(
 *     "快速导航",
 *     "简洁模式展示",
 *     1,
 *     StepStatus.SUCCESS
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class SimpleStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "简洁风格的步骤条";
    private static final String DEFAULT_DESCRIPTION = 
            "设置 simple 为 true 启用简洁风格。该模式下 alignCenter/description/direction/space 失效。";

    private final int initialActive;
    private final StepStatus finishStatus;

    /**
     * 创建默认配置的简洁风格步骤条组件
     */
    public SimpleStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 1, StepStatus.SUCCESS);
    }

    /**
     * 创建自定义配置的简洁风格步骤条组件
     *
     * @param title        卡片标题
     * @param description  卡片描述
     * @param active       初始激活步骤索引
     * @param finishStatus 已完成步骤的显示状态
     */
    public SimpleStepsPane(String title, String description, int active, StepStatus finishStatus) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
        this.finishStatus = finishStatus != null ? finishStatus : StepStatus.SUCCESS;
    }

    @Override
    protected void buildContent() {
        // 第一个简洁模式示例 - 带图标
        Steps simple1 = new Steps()
                .simple(true)
                .active(initialActive)
                .addStep(new Step("Step 1").iconSlot(
                        IconStepsPane.createSvgIcon(
                                "M 3 17.25 V 21 h 3.75 L 17.81 9.94 l -3.75 -3.75 L 3 17.25 Z",
                                Color.valueOf("#409EFF"))))
                .addStep(new Step("Step 2").iconSlot(
                        IconStepsPane.createSvgIcon(
                                "M 9 16 h 6 v -6 h 4 l -7 -7 -7 7 h 4 Z M 5 18 h 14 v 2 H 5 Z",
                                Color.valueOf("#409EFF"))))
                .addStep(new Step("Step 3").iconSlot(
                        IconStepsPane.createSvgIcon(
                                "M 21 19 V 5 c 0 -1.1 -0.9 -2 -2 -2 H 5 c -1.1 0 -2 0.9 -2 2 v 14",
                                Color.valueOf("#409EFF"))));

        // 第二个简洁模式示例 - 带 finishStatus
        Steps simple2 = new Steps()
                .simple(true)
                .active(initialActive)
                .finishStatus(finishStatus)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"));

        VBox.setMargin(simple2.getNode(), new Insets(15, 0, 0, 0));

        // 注意：这里只保存第二个 steps 的引用
        steps = simple2;

        getChildren().addAll(simple1.getNode(), simple2.getNode());
    }
}
