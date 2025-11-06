package io.data.chain.fx.concurrent.ui;

import io.data.chain.fx.ui.controls.ImageViewer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ImageViewerTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        //
        ObservableList<Image> images = FXCollections.observableArrayList(
                new Image("https://i0.hdslb.com/bfs/article/4bfb3057864685e8e9ad8c056d07c2874fde1afd.jpg"),
                new Image("https://i2.hdslb.com/bfs/archive/1a0ac15ec89f78ea76a47adff7d22e467a8ae9c8.jpg"),
                new Image("https://i0.hdslb.com/bfs/archive/c5e8d57f5c9962dd6eb7ba49ed6cf271e5231a2a.jpg"),
                new Image("https://pic.rmb.bdstatic.com/85fef0d4bbc654d1031bb0978f55b9fd.jpeg@s_0,w_2000")
        );
        ImageViewer cfImageViewer = new ImageViewer(images);
        //
        StackPane root = new StackPane(cfImageViewer);
        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.show();
    }

}