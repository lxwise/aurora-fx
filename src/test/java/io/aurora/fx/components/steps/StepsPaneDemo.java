package io.aurora.fx.components.steps;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Steps 步骤条组件综合演示应用
 * <p>
 * 完整展示所有功能特性，使用封装好的独立演示组件。
 * 所有演示组件继承自 {@link BaseStepsPane}，
 * 可通过 {@link StepsComponentFactory} 快速创建。
 * </p>
 *
 * @author Steps Component
 * @version 1.0
 * @see StepsComponentFactory
 * @see BaseStepsPane
 */
public class StepsPaneDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F7FA;");

        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #F5F7FA;");

        // 标题
        Label titleLabel = new Label("Steps 步骤条组件演示");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.valueOf("#303133"));
        mainContainer.getChildren().add(titleLabel);

        Label subtitleLabel = new Label("对标 Element UI Steps，引导用户按照流程完成任务的分步导航条");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", 14));
        subtitleLabel.setTextFill(Color.valueOf("#909399"));
        mainContainer.getChildren().add(subtitleLabel);

        // 各个演示区域 - 使用工厂类创建
        mainContainer.getChildren().addAll(
                StepsComponentFactory.createBasic(),
                StepsComponentFactory.createStatus(),
                StepsComponentFactory.createCenter(),
                StepsComponentFactory.createDescription(),
                StepsComponentFactory.createIcon(),
                StepsComponentFactory.createVertical(),
                StepsComponentFactory.createSimple(),
                StepsComponentFactory.createTheme(),
                StepsComponentFactory.createInteractive()
        );

        scrollPane.setContent(mainContainer);

        Scene scene = new Scene(scrollPane, 900, 750);
        primaryStage.setTitle("Steps 步骤条组件 - 综合演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
