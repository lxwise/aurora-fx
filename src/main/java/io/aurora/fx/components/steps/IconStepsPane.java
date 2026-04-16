package io.aurora.fx.components.steps;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * 带图标的步骤条组件
 * <p>
 * 演示使用自定义 SVG 图标替代默认编号的步骤条。
 * 通过 Step 的 iconSlot 方法设置自定义图标节点。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 默认配置
 * IconStepsPane pane = new IconStepsPane();
 *
 * // 自定义配置
 * IconStepsPane pane = new IconStepsPane(
 *     "功能导航",
 *     "使用图标展示各功能模块",
 *     1
 * );
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class IconStepsPane extends BaseStepsPane {

    private static final String DEFAULT_TITLE = "带图标的步骤条";
    private static final String DEFAULT_DESCRIPTION = "支持通过 icon 属性或 iconSlot 设置自定义图标。";

    private final int initialActive;

    /**
     * 创建默认配置的带图标步骤条组件
     */
    public IconStepsPane() {
        this(DEFAULT_TITLE, DEFAULT_DESCRIPTION, 1);
    }

    /**
     * 创建自定义配置的带图标步骤条组件
     *
     * @param title       卡片标题
     * @param description 卡片描述
     * @param active      初始激活步骤索引
     */
    public IconStepsPane(String title, String description, int active) {
        super(title != null ? title : DEFAULT_TITLE,
              description != null ? description : DEFAULT_DESCRIPTION);
        this.initialActive = Math.max(0, active);
    }

    @Override
    protected void buildContent() {
        // 创建 SVG 图标
        Node editIcon = createSvgIcon(
                "M 3 17.25 V 21 h 3.75 L 17.81 9.94 l -3.75 -3.75 L 3 17.25 Z " +
                "M 20.71 7.04 c 0.39 -0.39 0.39 -1.02 0 -1.41 l -2.34 -2.34 c -0.39 -0.39 -1.02 -0.39 -1.41 0 " +
                "l -1.83 1.83 l 3.75 3.75 l 1.83 -1.83 Z",
                Color.valueOf("#409EFF"));

        Node uploadIcon = createSvgIcon(
                "M 9 16 h 6 v -6 h 4 l -7 -7 -7 7 h 4 Z M 5 18 h 14 v 2 H 5 Z",
                Color.valueOf("#409EFF"));

        Node pictureIcon = createSvgIcon(
                "M 21 19 V 5 c 0 -1.1 -0.9 -2 -2 -2 H 5 c -1.1 0 -2 0.9 -2 2 v 14 " +
                "c 0 1.1 0.9 2 2 2 h 14 c 1.1 0 2 -0.9 2 -2 Z M 8.5 13.5 l 2.5 3.01 L 14.5 12 l 4.5 6 H 5 l 3.5 -4.5 Z",
                Color.valueOf("#409EFF"));

        steps = createDefaultSteps()
                .active(initialActive)
                .addStep(new Step("Step 1").iconSlot(editIcon))
                .addStep(new Step("Step 2").iconSlot(uploadIcon))
                .addStep(new Step("Step 3").iconSlot(pictureIcon));

        getChildren().add(steps.getNode());
    }

    /**
     * 创建 SVG 图标节点（静态工具方法）
     *
     * @param svgPath SVG 路径
     * @param color   填充颜色
     * @return 图标节点
     */
    public static Node createSvgIcon(String svgPath, Color color) {
        SVGPath path = new SVGPath();
        path.setContent(svgPath);
        path.setFill(color);
        path.setScaleX(0.8);
        path.setScaleY(0.8);

        StackPane pane = new StackPane(path);
        pane.setMinSize(24, 24);
        pane.setPrefSize(24, 24);
        pane.setMaxSize(24, 24);
        pane.setAlignment(Pos.CENTER);
        return pane;
    }
}
