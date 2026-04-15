package io.aurora.fx.concurrent;

import io.aurora.fx.common.utils.ConcurrentUtils;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * 可观察的任务执行器（Observable Executor）。
 * <p>
 * 用于统一管理并发任务的执行，支持：
 * <ul>
 *     <li>监控当前运行中的服务（Service）列表</li>
 *     <li>自动清理完成、取消或失败的任务</li>
 *     <li>自动注册异常监听器（通过 {@link ExceptionHandler}）</li>
 * </ul>
 *
 * <p><b>典型使用场景：</b></p>
 * <pre>{@code
 * ObservableExecutor executor = ObservableExecutor.getDefaultInstance();
 *
 * Task<String> task = new Task<>() {
 *     @Override
 *     protected String call() throws Exception {
 *         Thread.sleep(1000);
 *         return "完成";
 *     }
 * };
 *
 * executor.submit(task);  // 异步执行任务
 * }</pre>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ObservableExecutor implements Executor {

    private static ObservableExecutor defaultInstance;

    private final Executor executor;
    private final ListProperty<Service<?>> currentServices;
    private final ExceptionHandler exceptionHandler;

    /** 使用默认线程池创建一个可观察执行器 */
    public ObservableExecutor() {
        this(ThreadPoolExecutorFactory.getThreadPoolExecutor());
    }

    /** 使用指定执行器创建一个可观察执行器 */
    public ObservableExecutor(final Executor executor) {
        this(executor, ExceptionHandler.getDefaultInstance());
    }

    /** 使用默认线程池和指定异常处理器创建执行器 */
    public ObservableExecutor(final ExceptionHandler exceptionHandler) {
        this(ThreadPoolExecutorFactory.getThreadPoolExecutor(), exceptionHandler);
    }

    /**
     * 使用给定的执行器与异常处理器创建可观察执行器。
     *
     * @param executor         实际用于运行任务的执行器
     * @param exceptionHandler 异常处理器
     */
    public ObservableExecutor(final Executor executor, final ExceptionHandler exceptionHandler) {
        this.executor = Assert.requireNonNull(executor, "executor");
        this.exceptionHandler = Assert.requireNonNull(exceptionHandler, "exceptionHandler");

        currentServices = new SimpleListProperty<>(
                FXCollections.observableArrayList());

        // 自动移除完成、失败、取消的服务
        currentServices.addListener((ListChangeListener<? super Service<?>>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    List<? extends Service<?>> newServices = change.getAddedSubList();
                    for (final Service<?> service : newServices) {
                        service.stateProperty().addListener(
                                (observableValue, oldState, newState) -> {
                                    if (newState != null &&
                                            (newState.equals(State.CANCELLED)
                                                    || newState.equals(State.SUCCEEDED)
                                                    || newState.equals(State.FAILED))) {
                                        currentServices.remove(service);
                                    }
                                });
                    }
                }
            }
        });
    }

    /**
     * 获取当前正在运行或已计划的任务列表属性。
     *
     * @return 当前服务列表属性
     */
    public ReadOnlyListProperty<Service<?>> currentServicesProperty() {
        return currentServices;
    }

    /**
     * 提交并执行一个 Service。
     *
     * @param <T>     Service 的返回类型
     * @param service 要执行的服务
     * @return 可用于监控状态的 Worker
     */
    public <T> Worker<T> submit(final Service<T> service) {
        Assert.requireNonNull(service, "service");
        service.setExecutor(executor);
        currentServices.add(service);
        if (exceptionHandler != null) {
            exceptionHandler.observeWorker(service);
        }
        service.start();
        return service;
    }

    /** 提交一个 Task 并执行 */
    public <T> Worker<T> submit(final Task<T> task) {
        return submit(ConcurrentUtils.createService(task));
    }

    /** 提交一个 Callable 并执行（支持 DataFxCallable） */
    public <T> Worker<T> submit(final Callable<T> callable) {
        return submit(ConcurrentUtils.createService(callable));
    }

    /** 提交一个 Runnable 并执行（支持 DataFxRunnable） */
    public Worker<Void> submit(final Runnable runnable) {
        return submit(ConcurrentUtils.createService(runnable));
    }

    /** 直接执行 Runnable（不返回结果） */
    @Override
    public void execute(final Runnable runnable) {
        submit(runnable);
    }

    /**
     * 创建一个使用此执行器的 {@code ProcessChain}。
     *
     * @return 新的 ProcessChain 实例
     */
    public ProcessChain<Void> createProcessChain() {
        return new ProcessChain<>(this);
    }

    /** 获取默认的可观察执行器实例 */
    public static synchronized ObservableExecutor getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new ObservableExecutor();
        }
        return defaultInstance;
    }
}
