package io.aurora.fx.components.steps;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Consumer;

/**
 * 交互式步骤条组件
 * <p>
 * 提供完整的交互控制面板，支持动态切换步骤、修改状态、切换布局等。
 * 适合用于演示和测试 Steps 组件的全部功能。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * InteractiveStepsPane pane = new InteractiveStepsPane();
 *
 * // 自定义配置
 * InteractiveStepsPane pane = new InteractiveStepsPane(
 *     "订单流程",
 *     "订单处理各阶段",
 *     0,
 *     StepStatus.SUCCESS
 * );
 * pane.getSteps().onChange(idx -> System.out.println("当前: " + idx));
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class InteractiveStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "交互式控制";
    private static final String DEFAULT_DESCRIPTION = 
            "动态控制步骤条的各项属性，点击步骤可触发 onStepClick 事件。";

    private final int initialActive;
    private final StepStatus finishStatus;

    private Label statusInfo;

    /**
     * 创建默认配置的交互式组件
     */
    public InteractiveStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 0, StepStatus.SUCCESS);
    }

    /**
     * 创建自定义配置的交互式组件
     *
     * @param title        卡片标题
     * @param description  卡片描述
     * @param active       初始激活步骤索引
     * @param finishStatus 已完成步骤的显示状态
     */
    public InteractiveStepsPane(String title, String description, int active, StepStatus finishStatus) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
        this.finishStatus = finishStatus != null ? finishStatus : StepStatus.SUCCESS;
    }

    @Override
    protected void buildContent() {
        // 创建 Steps 组件
        steps = createDefaultSteps()
                .active(initialActive)
                .finishStatus(finishStatus)
                .addStep(new Step("选择产品", "浏览并选择您需要的产品"))
                .addStep(new Step("填写信息", "填写收货地址和联系方式"))
                .addStep(new Step("支付订单", "选择支付方式完成支付"))
                .addStep(new Step("完成", "订单处理完成"));

        // 步骤点击事件
        steps.onStepClick(idx -> {
            steps.setActive(idx);
            updateStatusInfo();
        });

        // 控制面板
        HBox controls = buildControlPanel();

        // 状态信息显示
        statusInfo = new Label();
        statusInfo.setTextFill(Color.valueOf("#909399"));
        statusInfo.setFont(Font.font("Microsoft YaHei", 11));
        VBox.setMargin(statusInfo, new Insets(10, 0, 0, 0));

        updateStatusInfo();

        getChildren().addAll(steps.getNode(), controls, statusInfo);
    }

    /**
     * 构建控制面板
     */
    private HBox buildControlPanel() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        // 上一步按钮
        Button prevBtn = new Button("上一步");
        prevBtn.setStyle("-fx-background-color: #909399; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 6 16;");
        prevBtn.setOnAction(e -> {
            if (!steps.prev()) {
                System.out.println("已在第一步，无法后退");
            }
            updateStatusInfo();
        });

        // 下一步按钮
        Button nextBtn = new Button("下一步");
        nextBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 6 16;");
        nextBtn.setOnAction(e -> {
            if (!steps.next()) {
                System.out.println("已在最后一步，无法前进");
            }
            updateStatusInfo();
        });

        // 重置按钮
        Button resetBtn = new Button("重置");
        resetBtn.setStyle("-fx-background-color: #E6A23C; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 6 16;");
        resetBtn.setOnAction(e -> {
            steps.goToFirst();
            updateStatusInfo();
        });

        // 完成状态选择
        Label statusLabel = new Label("完成状态:");
        statusLabel.setTextFill(Color.valueOf("#606266"));
        ComboBox<StepStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(StepStatus.FINISH, StepStatus.SUCCESS, StepStatus.ERROR);
        statusCombo.setValue(finishStatus);
        statusCombo.setOnAction(e -> steps.setFinishStatus(statusCombo.getValue()));

        // 居中切换
        CheckBox centerCheck = new CheckBox("居中");
        centerCheck.setOnAction(e -> steps.setAlignCenter(centerCheck.isSelected()));

        // 简洁模式
        CheckBox simpleCheck = new CheckBox("简洁模式");
        simpleCheck.setOnAction(e -> steps.setSimple(simpleCheck.isSelected()));

        // 垂直模式
        CheckBox verticalCheck = new CheckBox("垂直");
        verticalCheck.setOnAction(e -> steps.setDirection(
                verticalCheck.isSelected() ? Orientation.VERTICAL : Orientation.HORIZONTAL));

        controls.getChildren().addAll(prevBtn, nextBtn, resetBtn,
                new Separator(Orientation.VERTICAL),
                statusLabel, statusCombo, centerCheck, simpleCheck, verticalCheck);

        return controls;
    }

    /**
     * 更新状态信息显示
     */
    private void updateStatusInfo() {
        if (statusInfo != null && steps != null) {
            statusInfo.setText(String.format("当前: 第%d步 / 共%d步 | %s%s",
                    steps.getActive() + 1, steps.getTotalSteps(),
                    steps.isFirst() ? "[第一步] " : "",
                    steps.isLast() ? "[最后一步]" : ""));
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 设置步骤变化监听
     *
     * @param callback 回调函数
     */
    public void setOnChange(Consumer<Integer> callback) {
        if (steps != null) {
            steps.onChange(callback);
        }
    }

    /**
     * 设置步骤点击监听
     *
     * @param callback 回调函数
     */
    public void setOnStepClick(Consumer<Integer> callback) {
        if (steps != null) {
            steps.onStepClick(callback);
        }
    }
}
