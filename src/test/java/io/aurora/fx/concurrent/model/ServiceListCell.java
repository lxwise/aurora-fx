package io.aurora.fx.concurrent.model;

import io.aurora.fx.concurrent.DataFxService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * 一个用于显示 JavaFX Service 执行状态的 ListCell。
 *
 * @param <T> Service 的结果类型
 */
public class ServiceListCell<T> extends ListCell<Service<T>> {

    private final Label taskTitleLabel;
    private final Label taskMessageLabel;
    private final Button killTaskButton;
    private final ProgressBar taskProgress;
    private final AnchorPane anchorPane;

    public ServiceListCell() {

        // ========== 构建 UI 结构 ==========
        anchorPane = new AnchorPane();

        // 外层 VBox：垂直排列
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        AnchorPane.setTopAnchor(vbox, 6.0);
        AnchorPane.setBottomAnchor(vbox, 6.0);
        AnchorPane.setLeftAnchor(vbox, 6.0);
        AnchorPane.setRightAnchor(vbox, 6.0);

        // 任务标题
        taskTitleLabel = new Label("Task name");

        // 中间部分 BorderPane：包含进度条和右侧取消按钮
        BorderPane borderPane = new BorderPane();

        taskProgress = new ProgressBar(0);
        taskProgress.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setAlignment(taskProgress, javafx.geometry.Pos.CENTER_LEFT);

        killTaskButton = new Button("x");
        BorderPane.setAlignment(killTaskButton, javafx.geometry.Pos.CENTER_RIGHT);
        BorderPane.setMargin(killTaskButton, new Insets(0, 0, 0, 6));

        borderPane.setCenter(taskProgress);
        borderPane.setRight(killTaskButton);

        // 任务消息标签
        taskMessageLabel = new Label("Task Message");

        // 添加子节点到 VBox
        vbox.getChildren().addAll(taskTitleLabel, borderPane, taskMessageLabel);

        // 把 VBox 放入 AnchorPane
        anchorPane.getChildren().add(vbox);

        // 把 AnchorPane 设置为单元格图形
        setGraphic(anchorPane);

        // 默认隐藏
        anchorPane.setVisible(false);

        // ========== 交互逻辑 ==========
        // 点击取消按钮时取消任务
        killTaskButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                Service<?> service = getItem();
                if (service != null) {
                    service.cancel();
                }
            }
        });

        // 监听 ListCell 中的 Service 变化
        itemProperty().addListener(new ChangeListener<Service<?>>() {
            @Override
            public void changed(ObservableValue<? extends Service<?>> observableValue, Service<?> oldValue, Service<?> newValue) {
                try {
                    if (oldValue != null) {
                        taskTitleLabel.textProperty().unbind();
                        taskMessageLabel.textProperty().unbind();
                        taskProgress.progressProperty().unbind();
                        killTaskButton.visibleProperty().unbind();
                    }
                    if (newValue != null) {
                        // 绑定任务属性
                        taskTitleLabel.textProperty().bind(newValue.titleProperty());
                        taskMessageLabel.textProperty().bind(newValue.messageProperty());
                        taskProgress.progressProperty().bind(newValue.progressProperty());

                        // 处理可取消性
                        if (newValue instanceof DataFxService<?>) {
                            killTaskButton.visibleProperty().bind(((DataFxService<?>) newValue).cancelableProperty());
                        } else {
                            killTaskButton.visibleProperty().set(true);
                        }
                        anchorPane.setVisible(true);
                    } else {
                        anchorPane.setVisible(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
