package io.aurora.fx.components.tour;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * 自定义遮罩样式演示
 * <p>
 * 通过 {@link TourMaskConfig.Builder} 自定义遮罩颜色、不透明度、目标外扩 padding、
 * 镂空圆角与高亮描边效果。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class CustomMaskTourPane extends BaseTourPane {

    private static final String DEFAULT_TITLE = "自定义遮罩样式";
    private static final String DEFAULT_DESCRIPTION =
            "可自定义遮罩颜色、透明度、目标外扩 padding、圆角及目标周围的高亮描边。";

    public CustomMaskTourPane() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        Button btn1 = createDemoButton("数据", "#409EFF");
        Button btn2 = createDemoButton("分析", "#F56C6C");

        HBox row = new HBox(15, btn1, btn2);
        row.setAlignment(Pos.CENTER_LEFT);

        Button startBtn = new Button("启动自定义遮罩引导");
        startBtn.setStyle("-fx-background-color: #F56C6C; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 18;");

        startBtn.setOnAction(e -> {
            TourMaskConfig mask = TourMaskConfig.builder()
                    .color(Color.web("#1F1F1F"))
                    .opacity(0.7)
                    .padding(10)
                    .cornerRadius(12)
                    .highlight(true)
                    .highlightColor(Color.web("#FFD666"))
                    .highlightWidth(2.5)
                    .dismissOnMaskClick(true)
                    .build();

            tour = TourFactory.builder()
                    .maskConfig(mask)
                    .step(btn1, "数据中心", "查看与管理你的全部数据资产。", TourPlacement.BOTTOM)
                    .step(btn2, "智能分析", "AI 智能分析功能在此入口。", TourPlacement.BOTTOM)
                    .build();
            tour.show(getScene());
        });

        getChildren().addAll(row, startBtn);
    }

    private Button createDemoButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }
}
