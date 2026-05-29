package io.aurora.fx.components.tour;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * 交互式引导演示
 * <p>
 * 提供完整控制面板：切换主题/类型/遮罩开关、显示步骤进度日志，
 * 用于演示和测试 Tour 组件的全部功能。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class InteractiveTourPane extends BaseTourPane {

    private static final String DEFAULT_TITLE = "交互式控制";
    private static final String DEFAULT_DESCRIPTION =
            "通过控制面板动态切换主题、类型与遮罩开关，并实时观察步骤变化日志。";

    private Label statusInfo;

    public InteractiveTourPane() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        Button btnHome = createDemoButton("首页", "#409EFF");
        Button btnFavor = createDemoButton("收藏", "#67C23A");
        Button btnMessage = createDemoButton("消息", "#E6A23C");
        Button btnProfile = createDemoButton("个人中心", "#F56C6C");

        HBox row = new HBox(15, btnHome, btnFavor, btnMessage, btnProfile);
        row.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("DEFAULT", "DARK", "PRIMARY_BLUE", "PRIMARY_GREEN");
        themeCombo.setValue("DEFAULT");

        ComboBox<TourType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TourType.DEFAULT, TourType.PRIMARY);
        typeCombo.setValue(TourType.DEFAULT);

        CheckBox maskCheck = new CheckBox("显示遮罩");
        maskCheck.setSelected(true);

        CheckBox indicatorCheck = new CheckBox("显示指示器");
        indicatorCheck.setSelected(true);

        CheckBox arrowCheck = new CheckBox("显示箭头");
        arrowCheck.setSelected(true);

        Button startBtn = new Button("启动引导");
        startBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 18;");

        startBtn.setOnAction(e -> {
            TourTheme th = switch (themeCombo.getValue()) {
                case "DARK" -> TourTheme.DARK;
                case "PRIMARY_BLUE" -> TourTheme.PRIMARY_BLUE;
                case "PRIMARY_GREEN" -> TourTheme.PRIMARY_GREEN;
                default -> TourTheme.DEFAULT;
            };

            tour = TourFactory.builder()
                    .theme(th)
                    .type(typeCombo.getValue())
                    .mask(maskCheck.isSelected())
                    .showIndicators(indicatorCheck.isSelected())
                    .showArrow(arrowCheck.isSelected())
                    .step(btnHome, "首页", "应用主入口，展示核心信息。", TourPlacement.BOTTOM_START)
                    .step(btnFavor, "收藏", "收藏你喜欢的内容。", TourPlacement.BOTTOM)
                    .step(btnMessage, "消息", "查看通知与私信。", TourPlacement.BOTTOM)
                    .step(btnProfile, "个人中心", "管理账户与偏好设置。", TourPlacement.BOTTOM_END)
                    .onChange(idx -> updateStatus("步骤变化 → 第 " + (idx + 1) + " 步"))
                    .onOpen(() -> updateStatus("引导已开始"))
                    .onFinish(() -> updateStatus("引导已完成"))
                    .onClose(() -> updateStatus("引导已关闭"))
                    .build();
            tour.show(getScene());
        });

        HBox controls = new HBox(8,
                new Label("主题:"), themeCombo,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                new Label("类型:"), typeCombo,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                maskCheck, indicatorCheck, arrowCheck);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(8, 0, 8, 0));

        statusInfo = new Label("等待启动…");
        statusInfo.setTextFill(Color.valueOf("#909399"));
        statusInfo.setFont(Font.font("Microsoft YaHei", 11));
        VBox.setMargin(statusInfo, new Insets(8, 0, 0, 0));

        getChildren().addAll(row, controls, startBtn, statusInfo);
    }

    private void updateStatus(String text) {
        if (statusInfo != null) {
            statusInfo.setText(text);
        }
    }

    private Button createDemoButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }
}
