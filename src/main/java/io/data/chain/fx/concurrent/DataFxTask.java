package io.data.chain.fx.concurrent;

import io.data.chain.fx.common.utils.ConcurrentUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

import java.util.function.Consumer;

/**
 * 【DataFxTask】
 * <p>
 * JavaFX 异步任务的抽象基类，继承自 {@link Task} 并实现 {@link TaskStateHandler}。
 * 可用于后台执行逻辑、更新进度、控制取消行为。
 * </p>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public abstract class DataFxTask<V> extends Task<V> implements TaskStateHandler {

    /** 标记任务是否可被取消 */
    private final BooleanProperty cancelable;

    public DataFxTask() {
        cancelable = new SimpleBooleanProperty(true);
        updateTitle("Unknown task");
    }

    @Override
    public void updateTaskTitle(final String title) {
        updateTitle(title);
    }

    @Override
    public void updateTaskMessage(final String message) {
        updateMessage(message);
    }

    @Override
    public void updateTaskProgress(final double workDone, final double max) {
        updateProgress(workDone, max);
    }

    @Override
    public void updateTaskProgress(final long workDone, final long max) {
        updateProgress(workDone, max);
    }

    /** @return 当前任务是否可取消的属性 */
    public BooleanProperty cancelableProperty() {
        return cancelable;
    }

    /** 检查任务是否可取消 */
    public boolean isCancelable() {
        return cancelable.get();
    }

    /** 设置任务是否可取消 */
    public void setCancelable(final boolean cancelable) {
        this.cancelable.set(cancelable);
    }

    /**
     * 取消任务
     * <p>如果不可取消，会抛出 {@link RuntimeException}</p>
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (cancelable.get()) {
            return super.cancel(mayInterruptIfRunning);
        } else {
            throw new RuntimeException("Task is not cancelable!");
        }
    }

    /**
     * 注册任务完成回调
     * @param consumer 当任务执行成功后，将结果传递给 consumer
     */
    public void then(final Consumer<V> consumer) {
        ConcurrentUtils.then(this, consumer);
    }
}
