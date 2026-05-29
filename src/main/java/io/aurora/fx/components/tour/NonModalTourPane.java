package io.aurora.fx.components.tour;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * 非模态用法演示（mask=false + PRIMARY 类型）
 * <p>
 * 不显示遮罩，目标节点在引导期间仍可正常交互。
 * 推荐与 {@link TourType#PRIMARY} 搭配，提升非模态弹窗辨识度。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class NonModalTourPane extends BaseTourPane {

    private static final String DEFAULT_TITLE = "非模态用法";
    private static final String DEFAULT_DESCRIPTION =
            "通过 mask(false) 关闭遮罩。建议与 type(PRIMARY) 组合，使弹窗在没有遮罩时更醒目。";

    public NonModalTourPane() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        Label hint = new Label("非模态状态下，仍可正常点击页面其他元素。");
        hint.setStyle("-fx-text-fill: #606266; -fx-font-size: 12;");

        Button btnSearch = createDemoButton("搜索", "#909399");
        Button btnFavor = createDemoButton("收藏", "#909399");
        Button btnSetting = createDemoButton("设置", "#909399");

        HBox row = new HBox(15, btnSearch, btnFavor, btnSetting);
        row.setAlignment(Pos.CENTER_LEFT);

        Button startBtn = new Button("启动非模态引导");
        startBtn.setStyle("-fx-background-color: #67C23A; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 18;");
        startBtn.setOnAction(e -> {
            tour = TourFactory.builder()
                    .mask(false)
                    .type(TourType.PRIMARY)
                    .step(btnSearch, "搜索", "支持模糊匹配与正则搜索。", TourPlacement.BOTTOM)
                    .step(btnFavor, "收藏", "把常用项加入收藏夹。", TourPlacement.BOTTOM)
                    .step(btnSetting, "设置", "在这里调整应用偏好。", TourPlacement.BOTTOM_END)
                    .build();
            tour.show(getScene());
        });

        getChildren().addAll(hint, row, startBtn);
    }

    private Button createDemoButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }
}
