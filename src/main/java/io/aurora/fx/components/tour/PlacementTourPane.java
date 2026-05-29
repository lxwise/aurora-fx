package io.aurora.fx.components.tour;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

/**
 * 12 种位置定位演示
 * <p>
 * 在网格中放置 12 个目标按钮，分别演示 TOP/TOP_START/TOP_END、
 * BOTTOM/BOTTOM_START/BOTTOM_END、LEFT/LEFT_START/LEFT_END、RIGHT/RIGHT_START/RIGHT_END
 * 12 种弹窗定位效果。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class PlacementTourPane extends BaseTourPane {

    private static final String DEFAULT_TITLE = "12 种位置定位";
    private static final String DEFAULT_DESCRIPTION =
            "支持 top/top-start/top-end、bottom/bottom-start/bottom-end、left/left-start/left-end、right/right-start/right-end 共 12 种定位。";

    public PlacementTourPane() {
        super(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(40));
        grid.setAlignment(Pos.CENTER);

        TourPlacement[] placements = {
                TourPlacement.TOP_START, TourPlacement.TOP, TourPlacement.TOP_END,
                TourPlacement.LEFT_START, TourPlacement.CENTER, TourPlacement.RIGHT_START,
                TourPlacement.LEFT, null, TourPlacement.RIGHT,
                TourPlacement.LEFT_END, null, TourPlacement.RIGHT_END,
                TourPlacement.BOTTOM_START, TourPlacement.BOTTOM, TourPlacement.BOTTOM_END
        };

        // 用 5 行 3 列网格布置示意按钮
        Button[] buttons = new Button[12];
        int btnIdx = 0;
        TourPlacement[] order = {
                TourPlacement.TOP_START, TourPlacement.TOP, TourPlacement.TOP_END,
                TourPlacement.LEFT_START, TourPlacement.RIGHT_START,
                TourPlacement.LEFT, TourPlacement.RIGHT,
                TourPlacement.LEFT_END, TourPlacement.RIGHT_END,
                TourPlacement.BOTTOM_START, TourPlacement.BOTTOM, TourPlacement.BOTTOM_END
        };

        // 首行
        buttons[btnIdx++] = placeButton(grid, "TOP_START", 0, 0);
        buttons[btnIdx++] = placeButton(grid, "TOP", 1, 0);
        buttons[btnIdx++] = placeButton(grid, "TOP_END", 2, 0);
        // 第 2 行
        buttons[btnIdx++] = placeButton(grid, "LEFT_START", 0, 1);
        buttons[btnIdx++] = placeButton(grid, "RIGHT_START", 2, 1);
        // 第 3 行
        buttons[btnIdx++] = placeButton(grid, "LEFT", 0, 2);
        buttons[btnIdx++] = placeButton(grid, "RIGHT", 2, 2);
        // 第 4 行
        buttons[btnIdx++] = placeButton(grid, "LEFT_END", 0, 3);
        buttons[btnIdx++] = placeButton(grid, "RIGHT_END", 2, 3);
        // 第 5 行
        buttons[btnIdx++] = placeButton(grid, "BOTTOM_START", 0, 4);
        buttons[btnIdx++] = placeButton(grid, "BOTTOM", 1, 4);
        buttons[btnIdx++] = placeButton(grid, "BOTTOM_END", 2, 4);

        // 启动按钮
        Button startBtn = new Button("启动 12 步定位演示");
        startBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 18;");

        final Button[] btns = buttons;
        final TourPlacement[] orderRef = order;
        startBtn.setOnAction(e -> {
            TourFactory.Builder b = TourFactory.builder();
            for (int i = 0; i < orderRef.length; i++) {
                b.step(btns[i],
                        "Placement: " + orderRef[i].name(),
                        "弹窗将出现在目标按钮的 " + orderRef[i].getValue() + " 方向。",
                        orderRef[i]);
            }
            tour = b.build();
            tour.show(getScene());
        });

        getChildren().addAll(grid, startBtn);
    }

    private Button placeButton(GridPane grid, String text, int col, int row) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #ECF5FF; -fx-text-fill: #409EFF; " +
                "-fx-background-radius: 4; -fx-padding: 8 14; -fx-border-color: #B3D8FF; -fx-border-radius: 4;");
        grid.add(b, col, row);
        return b;
    }
}
