package io.aurora.fx.concurrent.process;

import io.aurora.fx.concurrent.ProcessChain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

// 模拟分页数据加载 + 实时 UI 推送
public class PublishingTaskDemo extends Application {

    private int currentPage = 1;
    private final int totalPages = 5;

    @Override
    public void start(Stage primaryStage) {
        // ✅ 使用 ObservableList 确保 UI 可自动刷新
        ObservableList<String> dataList = FXCollections.observableArrayList();
        ListView<String> listView = new ListView<>(dataList);

        BorderPane root = new BorderPane(listView);
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.setTitle("后台分页加载 + UI 实时更新");
        primaryStage.show();

        // ✅ 使用 ObservableList 作为 addPublishingTask 的容器
        ProcessChain.create()
                .addPublishingTask(
                        () -> dataList, // 直接使用 UI 的 ObservableList
                        publisher -> {
                            // 启动后台线程模拟分页加载
                            new Thread(() -> {
                                try {
                                    while (currentPage <= totalPages) {
                                        List<String> pageData = loadPage(currentPage);
                                        System.out.println("后台加载第 " + currentPage + " 页: " + pageData);

                                        // publisher.publish() 是线程安全的，会在 FX 主线程中执行更新
                                        for (String item : pageData) {
                                            publisher.publish(item);
                                        }

                                        currentPage++;
                                        Thread.sleep(1000);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                )
                .addConsumerInPlatformThread(list -> {
                    // 任务完成后打印总数据量
                    System.out.println("全部数据加载完成，共 " + list.size() + " 条");
                })
                .onException(Throwable::printStackTrace)
                .withFinal(() -> System.out.println("加载任务已结束"))
                .run();

        // ✅ 实时滚动到最新数据
        dataList.addListener((javafx.collections.ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    Platform.runLater(() -> listView.scrollTo(dataList.size() - 1));
                }
            }
        });
    }

    /** 模拟后台分页加载 */
    private List<String> loadPage(int page) {
        List<String> pageData = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            pageData.add("第 " + page + " 页 - 数据项 " + i);
        }
        return pageData;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
