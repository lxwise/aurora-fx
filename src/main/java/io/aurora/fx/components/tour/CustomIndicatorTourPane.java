package io.aurora.fx.components.tour;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 自定义指示器演示
 * <p>
 * 通过 {@link TourStep#indicatorSlot(Node)} 替换默认圆点指示器，
 * 例如改为 "1 / N" 进度文本或方块进度指示。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class CustomIndicatorTourPane extends BaseTourPane {

    private static final String DEFAULT_TITLE = "自定义指示器";
    private static final String DEFAULT_DESCRIPTION =
            "通过 indicatorSlot 替换默认指示器：可使用文本进度、方块进度等任意 Node。";

    public CustomIndicatorTourPane() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        Button b1 = createDemoButton("步骤 1", "#1890FF");
        Button b2 = createDemoButton("步骤 2", "#1890FF");
        Button b3 = createDemoButton("步骤 3", "#1890FF");
        Button b4 = createDemoButton("步骤 4", "#1890FF");

        HBox row = new HBox(15, b1, b2, b3, b4);
        row.setAlignment(Pos.CENTER_LEFT);

        Button startBtn = new Button("启动自定义指示器引导");
        startBtn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 18;");

        startBtn.setOnAction(e -> {
            int total = 4;
            // 文本指示器
            TourStep s1 = new TourStep(b1, "第一步", "演示文本进度指示")
                    .placement(TourPlacement.BOTTOM)
                    .indicatorSlot(buildTextIndicator(1, total));
            // 方块进度指示器
            TourStep s2 = new TourStep(b2, "第二步", "演示方块进度指示")
                    .placement(TourPlacement.BOTTOM)
                    .indicatorSlot(buildBlockIndicator(2, total));
            TourStep s3 = new TourStep(b3, "第三步", "演示方块进度指示")
                    .placement(TourPlacement.BOTTOM)
                    .indicatorSlot(buildBlockIndicator(3, total));
            TourStep s4 = new TourStep(b4, "第四步", "完成自定义指示器演示")
                    .placement(TourPlacement.BOTTOM_END)
                    .indicatorSlot(buildTextIndicator(4, total));

            tour = TourFactory.builder()
                    .theme(TourTheme.PRIMARY_BLUE)
                    .showIndicators(true)
                    .step(s1).step(s2).step(s3).step(s4)
                    .build();
            tour.show(getScene());
        });

        getChildren().addAll(row, startBtn);
    }

    private Node buildTextIndicator(int current, int total) {
        Label lbl = new Label(current + " / " + total);
        lbl.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#1890FF"));
        lbl.setStyle("-fx-padding: 2 8; -fx-background-color: #E6F4FF; -fx-background-radius: 10;");
        return lbl;
    }

    private Node buildBlockIndicator(int current, int total) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= total; i++) {
            Rectangle r = new Rectangle(14, 4);
            r.setArcWidth(2);
            r.setArcHeight(2);
            r.setFill(i <= current ? Color.web("#1890FF") : Color.web("#DCDFE6"));
            box.getChildren().add(r);
        }
        return box;
    }

    private Button createDemoButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 4; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }
}
