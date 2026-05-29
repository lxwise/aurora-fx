package io.aurora.fx.components.tour;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Tour 漫游引导组件综合演示应用
 * <p>
 * 完整展示所有功能特性，使用封装好的独立演示组件。
 * 所有演示组件继承自 {@link BaseTourPane}，可通过 {@link TourFactory} 快速创建。
 * </p>
 *
 * <h3>设计说明</h3>
 * <p>
 * 本 Demo 不再使用 ScrollPane 作为根容器，而是使用 {@link TabPane} 让每个示例
 * 独占一页，避免 ScrollPane viewport transform 与坐标转换给 Tour overlay
 * 定位带来的不确定性。每个 Tab 加载一个 {@link BaseTourPane} 子类，互不干扰。
 * </p>
 *
 * @author Tour Component
 * @version 2.0
 * @see TourFactory
 * @see BaseTourPane
 */
public class TourPaneDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F7FA;");

        // 顶部标题
        VBox header = new VBox(4);
        header.setPadding(new Insets(20, 30, 16, 30));
        header.setStyle("-fx-background-color: white;"
                + "-fx-border-color: #EBEEF5; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("Tour 漫游引导组件演示");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.valueOf("#303133"));

        Label subtitleLabel = new Label(
                "对标 Element Plus Tour，每个标签页独立展示一项能力，无需滚动即可完整体验。");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", 12));
        subtitleLabel.setTextFill(Color.valueOf("#909399"));
        header.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(header);

        // 中部 TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #F5F7FA;");

        tabPane.getTabs().addAll(
                buildTab("基础用法", TourFactory.createBasic()),
                buildTab("非模态", TourFactory.createNonModal()),
                buildTab("12 种定位", TourFactory.createPlacement()),
                buildTab("自定义遮罩", TourFactory.createCustomMask()),
                buildTab("自定义指示器", TourFactory.createCustomIndicator()),
                buildTab("中央显示", TourFactory.createCenter()),
                buildTab("交互式控制", TourFactory.createInteractive())
        );
        root.setCenter(tabPane);

        // 关键：包一层 StackPane 作为 Scene 根，便于 Tour overlay 直接挂载
        StackPane sceneRoot = new StackPane(root);
        Scene scene = new Scene(sceneRoot, 1100, 720);
        primaryStage.setTitle("Tour 漫游引导组件 - 综合演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab buildTab(String text, BaseTourPane pane) {
        Tab tab = new Tab(text);
        StackPane container = new StackPane(pane);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #F5F7FA;");
        tab.setContent(container);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
