package io.data.chain.fx.common.utils;

import io.data.chain.fx.concurrent.*;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 【ConcurrentUtils】
 * <p>
 * JavaFX 并发任务的辅助工具类，提供线程切换、任务执行、
 * 结果监听、任务同步等待等常用功能。
 * </p>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ConcurrentUtils {

    private ConcurrentUtils() {
    }

    /**
     * 在 JavaFX 应用程序线程上运行给定的可运行。该方法将阻塞，直到 Runnable完全执行。
     * 应将 {@link ProcessChain} 用于并发任务和后台任务
     * @param runnable 将在 JavaFX 应用程序线程上执行的可运行对象
     */
    public static void runAndWait(Runnable runnable)
            throws InterruptedException, ExecutionException {
        FutureTask<Void> future = new FutureTask<>(runnable, null);
        Platform.runLater(future);
        future.get();
    }

    /**
     * 在 JavaFX 应用程序线程上运行给定的可调用对象。该方法将一直阻塞，直到完全执行 Callable
     * 将返回可调用对象的 call（） 方法的返回值
     * @param callable 将在 JavaFX 应用程序线程上执行的可调用对象
     * @return 可调用对象已执行的 call（） 方法的返回值
     */
    public static <T> T runCallableAndWait(Callable<T> callable)
            throws InterruptedException, ExecutionException {
        FutureTask<T> future = new FutureTask<T>(callable);
        Platform.runLater(future);
        return future.get();
    }

    /** 创建执行 Runnable 的 Service */
    public static DataFxService<Void> createService(Runnable runnable) {
        return createService(new RunnableBasedDataFxTask(runnable));
    }

    /** 创建执行 Callable 的 Service */
    public static <T> DataFxService<T> createService(Callable<T> callable) {
        return createService(new CallableBasedDataFxTask<T>(callable));
    }

    /** 创建执行 javafx异步任务 的 Service */
    public static <T> DataFxService<T> createService(Task<T> task) {
        return new DataFxService<T>() {
            @Override
            protected Task<T> createTask() {
                return task;
            }
        };
    }

    /** 线程池方式 执行 Service 并返回 Worker（可监听状态） */
    public static <T> Worker<T> executeService(Executor executor, Service<T> service) {
        if (executor != null && executor instanceof ObservableExecutor) {
            return ((ObservableExecutor) executor).submit(service);
        } else {
            if (executor != null) {
                service.setExecutor(executor);
            }
            service.start();
            return service;
        }
    }
    /**
     * 返回一个 {@link BooleanBinding} 属性，当给定的 {@link Worker}（如 {@link javafx.concurrent.Task} 或 {@link javafx.concurrent.Service}）
     * 状态为 {@code CANCELLED}、{@code FAILED} 或 {@code SUCCEEDED} 时，其值为 {@code true}。
     * <p>
     * 此属性可用于在 JavaFX 界面中通过绑定来检测任务是否已完成（无论成功或失败），
     * 通常用于禁用按钮、隐藏加载动画等场景。
     *
     * <h3>示例：</h3>
     * <pre>{@code
     * Task<Void> task = new Task<>() {
     *     @Override
     *     protected Void call() throws Exception {
     *         Thread.sleep(2000);
     *         return null;
     *     }
     * };
     *
     * Button startBtn = new Button("开始任务");
     * startBtn.disableProperty().bind(ConcurrentUtils.isFinishedProperty(task).not());
     *
     * new Thread(task).start();
     * }</pre>
     *
     * @param worker 要监控的 JavaFX {@link Worker} 实例（例如 Task 或 Service）
     * @param <V>    worker 任务返回值的类型
     * @return 当 worker 状态为 CANCELLED、FAILED 或 SUCCEEDED 时为 true 的 {@link BooleanBinding}
     */
    public static <V> BooleanBinding isFinishedProperty(Worker<V> worker) {
        return worker.stateProperty().isEqualTo(Worker.State.CANCELLED).or(worker.stateProperty().isEqualTo(Worker.State.FAILED).or(worker.stateProperty().isEqualTo(Worker.State.SUCCEEDED)));
    }

    /**
     *一旦工作线程完成，将调用给定的使用者。工作线程的结果将传递给消费者。
     * @param worker the worker
     * @param consumer the consumer
     * @param <T> the resukt type of the worker
     */
    public static <T> void then(Worker<T> worker, Consumer<T> consumer) {
        ReadOnlyBooleanProperty doneProperty = createIsDoneProperty(worker);
        ChangeListener<Boolean> listener = (o, oldValue, newValue) -> {
            if (newValue) {
                consumer.accept(worker.getValue());
            }
        };
        doneProperty.addListener(listener);
    }

    /**
     * 创建并返回一个只读的布尔属性，用于标识给定 {@link Worker} 是否已经完成。
     * <p>
     * 当 worker 的状态变为 {@code CANCELLED}、{@code FAILED} 或 {@code SUCCEEDED} 时，
     * 属性值会自动更新为 {@code true}；当 worker 正在运行（如 {@code READY} 或 {@code RUNNING}）时，属性值为 {@code false}。
     * <p>
     * 与 {@link #isFinishedProperty(Worker)} 不同的是，
     * 本方法返回的是一个可监听的 {@link ReadOnlyBooleanProperty}，
     * 适合在代码逻辑中通过监听事件来执行某个操作，而非用于 UI 绑定。
     *
     * <h3>示例：</h3>
     * <pre>{@code
     * Task<String> task = new Task<>() {
     *     @Override
     *     protected String call() throws Exception {
     *         Thread.sleep(3000);
     *         return "任务完成";
     *     }
     * };
     *
     * ReadOnlyBooleanProperty doneProperty = ConcurrentUtils.createIsDoneProperty(task);
     * doneProperty.addListener((obs, oldVal, newVal) -> {
     *     if (newVal) {
     *         System.out.println("Worker 已完成，结果：" + task.getValue());
     *     }
     * });
     *
     * new Thread(task).start();
     * }</pre>
     *
     * @param worker 要监听的 JavaFX {@link Worker} 实例（如 Task 或 Service）
     * @return 一个只读布尔属性，当 worker 完成（包括取消、失败或成功）时变为 true
     */
    public static ReadOnlyBooleanProperty createIsDoneProperty(Worker<?> worker) {
        final BooleanProperty property = new SimpleBooleanProperty();
        Consumer<Worker.State> stateChecker = (s) -> {
            if (s.equals(Worker.State.CANCELLED) || s.equals(Worker.State.FAILED) || s.equals(Worker.State.SUCCEEDED)) {
                property.setValue(true);
            } else {
                property.setValue(false);
            }
        };
        worker.stateProperty().addListener((o, oldValue, newValue) -> stateChecker.accept(newValue));
        stateChecker.accept(worker.getState());
        return property;

    }

    /**
     * 此方法将阻止，直到工作线程完成并返回工作线程的结果值。如果工作人员被取消，将引发异常。
     * @param worker The worker
     * @param <T> result type of the worker
     * @return the result
     * @throws InterruptedException if the worker was canceled
     */
    public static <T> T waitFor(Worker<T> worker) throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        lock.lock();
        try {
            ReadOnlyBooleanProperty doneProperty = createIsDoneProperty(worker);
            if (doneProperty.get()) {
                return worker.getValue();
            } else {
                doneProperty.addListener(e -> {
                    boolean locked = lock.tryLock();
                    if (locked) {
                        try {
                            condition.signal();
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        throw new RuntimeException("Concurreny Error");
                    }
                });
                condition.await();
                return worker.getValue();
            }
        } finally {
            lock.unlock();
        }
    }
}
