package io.aurora.fx.components.steps;

import javafx.scene.control.Button;

import java.util.function.Consumer;

/**
 * 基础用法步骤条组件
 * <p>
 * 演示 Steps 组件的最基本用法，包含一个"下一步"按钮用于切换活动步骤。
 * 适合用于快速入门和简单场景展示。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * BasicStepsPane pane = new BasicStepsPane();
 * root.getChildren().add(pane);
 *
 * // 自定义配置
 * BasicStepsPane pane = new BasicStepsPane(
 *     "安装向导",
 *     "跟随引导完成安装",
 *     1,
 *     StepStatus.SUCCESS,
 *     idx -> System.out.println("当前步骤: " + idx)
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class BasicStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "基础用法";
    private static final String DEFAULT_DESCRIPTION = "简单的步骤条。设置 active 属性表明步骤的 index，从 0 开始。";

    private final int initialActive;
    private final StepStatus finishStatus;
    private final Consumer<Integer> onChangeCallback;

    /**
     * 创建默认配置的基础用法组件
     */
    public BasicStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 1, StepStatus.SUCCESS, null);
    }

    /**
     * 创建自定义配置的基础用法组件
     *
     * @param title           卡片标题
     * @param description     卡片描述
     * @param active          初始激活步骤索引
     * @param finishStatus    已完成步骤的显示状态
     * @param onChangeCallback 步骤变化回调（可为 null）
     */
    public BasicStepsPane(String title, String description, int active,
                          StepStatus finishStatus, Consumer<Integer> onChangeCallback) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
        this.finishStatus = finishStatus != null ? finishStatus : StepStatus.SUCCESS;
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    protected void buildContent() {
        // 创建 Steps 组件
        steps = createDefaultSteps()
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"))
                .active(initialActive)
                .finishStatus(finishStatus);

        // 设置变化监听
        if (onChangeCallback != null) {
            steps.onChange(onChangeCallback);
        }

        // 下一步按钮
        Button nextBtn = createNextButton();

        getChildren().addAll(steps.getNode(), nextBtn);
    }

    /**
     * 创建"下一步"按钮
     */
    private Button createNextButton() {
        Button button = new Button("Next Step");
        button.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 4; -fx-padding: 8 20;");
        button.setOnAction(e -> {
            int next = steps.getActive() + 1;
            if (next > 2) {
                next = 0;
            }
            steps.setActive(next);
        });
        return button;
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取当前步骤索引
     *
     * @return 当前激活步骤索引（从 0 开始）
     */
    public int getCurrentStep() {
        return steps != null ? steps.getActive() : initialActive;
    }

    /**
     * 设置当前步骤
     *
     * @param index 步骤索引
     */
    public void setCurrentStep(int index) {
        if (steps != null) {
            steps.setActive(index);
        }
    }

    /**
     * 前进到下一步
     *
     * @return 是否成功
     */
    public boolean next() {
        return steps != null && steps.next();
    }

    /**
     * 后退到上一步
     *
     * @return 是否成功
     */
    public boolean prev() {
        return steps != null && steps.prev();
    }
}
