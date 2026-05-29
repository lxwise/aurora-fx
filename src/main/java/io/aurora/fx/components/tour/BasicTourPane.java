package io.aurora.fx.components.tour;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * 基础用法演示
 * <p>
 * 演示在三个普通按钮上依次显示引导提示，使用默认主题与模态遮罩。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * BasicTourPane pane = new BasicTourPane();
 * root.getChildren().add(pane);
 * }</pre>
 *
 * @author Tour Component
 * @version 1.0
 */
public class BasicTourPane extends BaseTourPane {

    private static final String DEFAULT_TITLE = "基础用法";
    private static final String DEFAULT_DESCRIPTION =
            "在任意 JavaFX Node 上显示漫游引导。点击下方按钮启动引导，每步可点击下一步前进。";

    public BasicTourPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    public BasicTourPane(String title, String description) {
        super(title != null ? title : DEFAULT_TITLE,
                description != null ? description : DEFAULT_DESCRIPTION);
    }

    @Override
    protected void buildContent() {
        Button btn1 = createDemoButton("功能 1", "#409EFF");
        Button btn2 = createDemoButton("功能 2", "#67C23A");
        Button btn3 = createDemoButton("功能 3", "#E6A23C");

        HBox row = new HBox(15, btn1, btn2, btn3);
        row.setAlignment(Pos.CENTER_LEFT);

        Button startBtn = new Button("启动引导");
        startBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 18;");

        startBtn.setOnAction(e -> {
            tour = TourFactory.builder()
                    .step(btn1, "功能入口 1", "这是第一步：点击它进入第一项功能。", TourPlacement.BOTTOM)
                    .step(btn2, "功能入口 2", "这是第二步：体验第二项功能。", TourPlacement.BOTTOM)
                    .step(btn3, "功能入口 3", "这是第三步：完成全部引导。", TourPlacement.BOTTOM_END)
                    .onFinish(() -> System.out.println("[BasicTourPane] 引导完成"))
                    .onClose(() -> System.out.println("[BasicTourPane] 引导关闭"))
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
