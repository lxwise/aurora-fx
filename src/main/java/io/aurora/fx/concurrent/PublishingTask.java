package io.aurora.fx.concurrent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.Arrays;

/**
 * 支持实时发布中间结果的 JavaFX 任务。
 * <p>
 * 可用于后台持续生成数据（如从网络或数据库加载分页数据）并实时推送到 UI。
 * 任务执行完毕后，其 {@link #getPublishedValues()} 包含所有已发布数据。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * PublishingTask<String> task = new PublishingTask<>() {
 *     @Override
 *     protected void callTask() throws Exception {
 *         for (int i = 1; i <= 5; i++) {
 *             publish("数据项 " + i);
 *             Thread.sleep(500);
 *         }
 *     }
 * };
 *
 * task.getPublishedValues().addListener((ListChangeListener<String>) c -> {
 *     while (c.next()) {
 *         if (c.wasAdded()) {
 *             System.out.println("新增数据: " + c.getAddedSubList());
 *         }
 *     }
 * });
 *
 * new Thread(task).start();
 * }</pre>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public abstract class PublishingTask<T> extends Task<ObservableList<T>> implements Publisher<T> {

    /** 存储已发布数据的可观察列表 */
    private final ObservableList<T> publishedValues;

    /** 使用默认空列表构造 */
    public PublishingTask() {
        this(FXCollections.observableArrayList());
    }

    /** 使用指定列表构造 */
    public PublishingTask(final ObservableList<T> values) {
        this.publishedValues = Assert.requireNonNull(values, "values");
    }

    /** 获取所有已发布的值 */
    public ObservableList<T> getPublishedValues() {
        return publishedValues;
    }

    /** JavaFX Task 的执行入口，内部调用 {@link #callTask()} */
    @Override
    protected final ObservableList<T> call() throws Exception {
        callTask();
        return publishedValues;
    }

    /**
     * 抽象方法：用户应在此实现任务逻辑，并可调用 {@link #publish(Object[])} 推送数据。
     */
    protected abstract void callTask() throws Exception;

    /**
     * 向 UI 线程发布新的数据项。
     * 调用后会自动在 JavaFX 主线程中更新 {@link #publishedValues}。
     *
     * @param values 要发布的数据项
     */
    @Override
    public void publish(final T... values) {
        if (values != null && values.length > 0) {
            Platform.runLater(() -> publishedValues.addAll(Arrays.asList(values)));
        }
    }
}
