package io.aurora.fx.components.tour;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * 屏幕中央显示演示
 * <p>
 * 当目标为 {@link TourTarget#empty()} 或 placement 为 {@link TourPlacement#CENTER} 时，
 * 引导弹窗会显示在当前 Scene 的中央位置。常用于欢迎页或全局公告类引导。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class CenterTourPane extends BaseTourPane {

    private static final String DEFAULT_TITLE = "屏幕中央显示";
    private static final String DEFAULT_DESCRIPTION =
            "当 target 为空（TourTarget.empty()）或 placement 为 CENTER 时，弹窗显示在屏幕中央。";

    public CenterTourPane() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        Label hint = new Label("无目标场景常用于欢迎引导、版本公告或全局介绍。");
        hint.setStyle("-fx-text-fill: #606266; -fx-font-size: 12;");

        Button btnEntry = createDemoButton("查看新功能", "#722ED1");

        Button startBtn = new Button("启动中央引导");
        startBtn.setStyle("-fx-background-color: #722ED1; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 18;");

        startBtn.setOnAction(e -> {
            tour = TourFactory.builder()
                    .step(new TourStep("欢迎使用", "感谢你升级到最新版，跟随引导了解全新功能。")
                            .target(TourTarget.empty())
                            .placement(TourPlacement.CENTER))
                    .step(new TourStep("功能 1", "新增多线程任务链与跨线程调度能力。")
                            .target(TourTarget.empty())
                            .placement(TourPlacement.CENTER))
                    .step(new TourStep(btnEntry, "新功能入口", "想详细了解，可点击该按钮。")
                            .placement(TourPlacement.RIGHT))
                    .build();
            tour.show(getScene());
        });

        HBox row = new HBox(15, btnEntry, startBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(hint, row);
    }

    private Button createDemoButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }
}
