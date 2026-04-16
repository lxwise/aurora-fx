package io.aurora.fx.components.steps;

/**
 * 带描述的步骤栏组件
 * <p>
 * 演示每个步骤带有详细描述文本的用法。
 * 通过 Step 的 description 属性设置描述内容。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * DescriptionStepsPane pane = new DescriptionStepsPane();
 *
 * // 自定义配置
 * DescriptionStepsPane pane = new DescriptionStepsPane(
 *     "订单流程",
 *     "订单处理各阶段说明",
 *     1
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class DescriptionStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "带描述的步骤栏";
    private static final String DEFAULT_DESCRIPTION = "每一步都有描述文本。";

    private final int initialActive;

    /**
     * 创建默认配置的带描述步骤栏组件
     */
    public DescriptionStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 1);
    }

    /**
     * 创建自定义配置的带描述步骤栏组件
     *
     * @param title       卡片标题
     * @param description 卡片描述
     * @param active      初始激活步骤索引
     */
    public DescriptionStepsPane(String title, String description, int active) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
    }

    @Override
    protected void buildContent() {
        steps = createDefaultSteps()
                .active(initialActive)
                .addStep(new Step("Step 1", "This is a long description text for step one"))
                .addStep(new Step("Step 2", "This is a description"))
                .addStep(new Step("Step 3", "Some description"));

        getChildren().add(steps.getNode());
    }
}
