package io.aurora.fx.components.steps;

import javafx.geometry.Orientation;

/**
 * 垂直的步骤条组件
 * <p>
 * 演示垂直方向排列的步骤条用法。
 * 通过 direction 属性设置为 VERTICAL 启用垂直布局。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * VerticalStepsPane pane = new VerticalStepsPane();
 *
 * // 自定义配置
 * VerticalStepsPane pane = new VerticalStepsPane(
 *     "审批流程",
 *     "审批各阶段状态",
 *     1
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class VerticalStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "垂直的步骤条";
    private static final String DEFAULT_DESCRIPTION = "设置 direction 为 VERTICAL 即可垂直显示。";

    private final int initialActive;

    /**
     * 创建默认配置的垂直步骤条组件
     */
    public VerticalStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 1);
    }

    /**
     * 创建自定义配置的垂直步骤条组件
     *
     * @param title       卡片标题
     * @param description 卡片描述
     * @param active      初始激活步骤索引
     */
    public VerticalStepsPane(String title, String description, int active) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
    }

    @Override
    protected void buildContent() {
        steps = createDefaultSteps()
                .direction(Orientation.VERTICAL)
                .active(initialActive)
                .addStep(new Step("Step 1", "这是第一步的描述信息"))
                .addStep(new Step("Step 2", "这是第二步的描述信息"))
                .addStep(new Step("Step 3", "这是第三步的描述信息"));

        // 设置固定高度
        steps.getNode().setMaxHeight(280);
        steps.getNode().setMinHeight(280);

        getChildren().add(steps.getNode());
    }
}
