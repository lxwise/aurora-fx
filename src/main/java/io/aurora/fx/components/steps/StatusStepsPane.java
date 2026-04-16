package io.aurora.fx.components.steps;

/**
 * 含状态的步骤条组件
 * <p>
 * 演示每个步骤显示不同状态（完成、进行中、等待）的用法。
 * 通过 space 属性设置固定步距，finishStatus 设置完成状态样式。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * StatusStepsPane pane = new StatusStepsPane();
 *
 * // 自定义配置
 * StatusStepsPane pane = new StatusStepsPane(
 *     "流程状态",
 *     "当前流程执行状态",
 *     1,
 *     StepStatus.SUCCESS,
 *     200
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class StatusStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "含状态的步骤条";
    private static final String DEFAULT_DESCRIPTION = "每一步骤显示出该步骤的状态。可以使用 title 属性设置标题。";

    private final int initialActive;
    private final StepStatus finishStatus;
    private final double space;

    /**
     * 创建默认配置的含状态步骤条组件
     */
    public StatusStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 1, StepStatus.SUCCESS, 200);
    }

    /**
     * 创建自定义配置的含状态步骤条组件
     *
     * @param title        卡片标题
     * @param description  卡片描述
     * @param active       初始激活步骤索引
     * @param finishStatus 已完成步骤的显示状态
     * @param space        固定步距（像素），<= 0 表示自适应
     */
    public StatusStepsPane(String title, String description, int active,
                           StepStatus finishStatus, double space) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
        this.finishStatus = finishStatus != null ? finishStatus : StepStatus.SUCCESS;
        this.space = space;
    }

    @Override
    protected void buildContent() {
        steps = createDefaultSteps()
                .space(space > 0 ? space : null)
                .active(initialActive)
                .finishStatus(finishStatus)
                .addStep(new Step("Done"))
                .addStep(new Step("Processing"))
                .addStep(new Step("Step 3"));

        getChildren().add(steps.getNode());
    }
}
