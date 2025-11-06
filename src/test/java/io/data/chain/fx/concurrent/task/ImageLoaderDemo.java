package io.data.chain.fx.concurrent.task;

import io.data.chain.fx.concurrent.ObservableExecutor;
import io.data.chain.fx.concurrent.task.ImageLoader;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 使用 ImageLoader 异步加载图片的 JavaFX 示例。
 * 演示：
 *  - 如何使用 ObservableExecutor 在后台线程加载网络图片
 *  - 加载过程中不阻塞 UI
 *  - 加载完毕自动显示
 */
public class ImageLoaderDemo extends Application {

    @Override
    public void start(Stage stage) {
        // 创建一个 ImageView，初始为空
        ImageView imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);

        // 默认占位图（在加载前显示）
        Image defaultImage = new Image("https://via.placeholder.com/400x300?text=Loading...", false);

        // 创建 ImageLoader，传入默认图像
        ImageLoader loader = new ImageLoader(ObservableExecutor.getDefaultInstance(), defaultImage);
        loader.debug(); // 开启线程监控调试输出

        Label statusLabel = new Label("点击下方按钮加载网络图片");

        // 点击按钮异步加载图片
        Button loadButton = new Button("加载图片");
        loadButton.setOnAction(e -> {
            String imageUrl = "https://picsum.photos/seed/" + System.currentTimeMillis() + "/800/600";
            Worker<Void> worker = loader.updateImageView(imageView, imageUrl);

            // 绑定任务状态到标签
            statusLabel.textProperty().bind(worker.messageProperty());
        });

        // 布局
        VBox root = new VBox(15, imageView, statusLabel, loadButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 30; -fx-background-color: whitesmoke;");

        // 场景
        Scene scene = new Scene(root, 500, 500);
        stage.setTitle("JavaFX 异步图片加载示例");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
