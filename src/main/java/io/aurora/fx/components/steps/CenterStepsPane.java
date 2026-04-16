package io.aurora.fx.components.steps;

/**
 * 居中的步骤条组件
 * <p>
 * 演示步骤标题和描述居中对齐的用法。
 * 通过 alignCenter 属性启用居中模式。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * CenterStepsPane pane = new CenterStepsPane();
 *
 * // 自定义配置
 * CenterStepsPane pane = new CenterStepsPane(
 *     "安装进度",
 *     "当前安装进度如下",
 *     2,
 *     true
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class CenterStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "居中的步骤条";
    private static final String DEFAULT_DESCRIPTION = "标题和描述可以居中。设置 alignCenter 为 true。";

    private final int initialActive;
    private final boolean alignCenter;

    /**
     * 创建默认配置的居中步骤条组件
     */
    public CenterStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 2, true);
    }

    /**
     * 创建自定义配置的居中步骤条组件
     *
     * @param title       卡片标题
     * @param description 卡片描述
     * @param active      初始激活步骤索引
     * @param alignCenter 是否居中对齐
     */
    public CenterStepsPane(String title, String description, int active, boolean alignCenter) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
        this.alignCenter = alignCenter;
    }

    @Override
    protected void buildContent() {
        steps = createDefaultSteps()
                .active(initialActive)
                .alignCenter(alignCenter)
                .addStep(new Step("Step 1", "Some description"))
                .addStep(new Step("Step 2", "Some description"))
                .addStep(new Step("Step 3", "Some description"))
                .addStep(new Step("Step 4", "Some description"));

        getChildren().add(steps.getNode());
    }
}
