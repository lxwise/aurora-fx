package io.aurora.fx.components.steps;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 主题演示步骤条组件
 * <p>
 * 展示 Steps 组件的多主题支持能力，包含深色、蓝色、绿色等预设主题。
 * 通过 theme 属性设置 StepsTheme 实例。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * ThemeStepsPane pane = new ThemeStepsPane();
 *
 * // 自定义配置
 * ThemeStepsPane pane = new ThemeStepsPane(
 *     "主题展示",
 *     "内置多种预设主题"
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class ThemeStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "主题定制";
    private static final String DEFAULT_DESCRIPTION = 
            "通过 StepsTheme 自定义颜色、字体等样式，内置 DEFAULT/DARK/BLUE/GREEN 预设主题。";

    /**
     * 创建默认配置的主题演示组件
     */
    public ThemeStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    /**
     * 创建自定义配置的主题演示组件
     *
     * @param title       卡片标题
     * @param description 卡片描述
     */
    public ThemeStepsPane(String title, String description) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        // 深色主题
        Label darkLabel = createThemeLabel("深色主题 (DARK)");
        Steps darkSteps = new Steps()
                .theme(StepsTheme.DARK)
                .active(1)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"));
        darkSteps.getNode().setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 8;");

        // 蓝色主题
        Label blueLabel = createThemeLabel("蓝色主题 (BLUE)");
        Steps blueSteps = new Steps()
                .theme(StepsTheme.BLUE)
                .active(2)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"))
                .addStep(new Step("Step 4"));

        // 绿色主题
        Label greenLabel = createThemeLabel("绿色主题 (GREEN)");
        Steps greenSteps = new Steps()
                .theme(StepsTheme.GREEN)
                .active(1)
                .finishStatus(StepStatus.SUCCESS)
                .addStep(new Step("Step 1"))
                .addStep(new Step("Step 2"))
                .addStep(new Step("Step 3"));

        // 保存最后一个作为主引用
        steps = greenSteps;

        getChildren().addAll(
                darkLabel, darkSteps.getNode(),
                blueLabel, blueSteps.getNode(),
                greenLabel, greenSteps.getNode()
        );
    }

    /**
     * 创建主题标签
     */
    private Label createThemeLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        label.setTextFill(Color.valueOf("#606266"));
        return label;
    }
}
