//package io.data.chain.fx.concurrent.process;
//
//import com.alibaba.fastjson.JSON;
//import com.dtflys.forest.Forest;
//import io.data.chain.fx.concurrent.ProcessChain;
//import io.data.chain.fx.concurrent.client.Amap;
//import javafx.application.Application;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextArea;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//public class ProcessChainDemo extends Application {
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//
//        VBox ap = new VBox();
//        ap.setAlignment(Pos.CENTER);
//        ap.setSpacing(30.0);
//        Label label = new Label("This is an javafx data chain dispose test. ");
//        label.setStyle(" -fx-font-size: 14px;-fx-text-fill: #d73e39;");
//
//        TextArea location = new TextArea("");
//        location.setEditable(false);
//        location.setPrefHeight(300);
//        location.setPrefWidth(300);
//
//        Button button = new Button("request api");
//
//        button.setOnAction(event -> {
//            ProcessChain.create().addRunnableInPlatformThread(() -> {
//                        button.setDisable(true);
//                    })
//                    .addSupplierInExecutor(() -> {
//                        Amap client = Forest.client(Amap.class);
//                        return client.getLocation("116.397428", "39.90923");
//                    })
//                    .addConsumerInPlatformThread(r -> {
//
//                        if (r.getStatus() == 1) {
//                            label.setText("数据请求成功...");
//                            label.setStyle(" -fx-font-size: 14px;-fx-text-fill: #95da9e;");
//
//                            location.setText(JSON.toJSONString(r.getData()));
//                        } else {
//                            label.setText("数据请求失败...");
//                            label.setStyle(" -fx-font-size: 14px;-fx-text-fill: #d73e39;");
//                        }
//                    })
//                    .onException(e -> {
//                        label.setText("数据请求失败...失败原因:"+e.getLocalizedMessage());
//                        label.setStyle(" -fx-font-size: 14px;-fx-text-fill: #d73e39;");
//                        e.printStackTrace();
//                    })
//                    .withFinal(() -> {
//                        button.setDisable( false);
//                    })
//                    .run();
//        });
//
//        ap.getChildren().addAll(label,button,location);
//        Scene scene = new Scene(ap);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("jfx-data-chain");
//        primaryStage.setWidth(800);
//        primaryStage.setHeight(800);
//        primaryStage.show();
//
//    }
//}
