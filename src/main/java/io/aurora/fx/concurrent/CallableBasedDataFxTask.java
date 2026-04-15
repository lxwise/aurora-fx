package io.aurora.fx.concurrent;

import java.util.concurrent.Callable;

/**
 * 【基于 Callable 的 DataFxTask 实现】
 * <p>用于封装返回结果的异步任务。</p>
 *
 * @param <V> 任务返回值类型
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class CallableBasedDataFxTask<V> extends DataFxTask<V> {

    private final Callable<V> callable;

    public CallableBasedDataFxTask(final Callable<V> callable) {
        this.callable = Assert.requireNonNull(callable, "callable");
        // 如果 Callable 支持状态注入，则注入当前任务句柄
        if (this.callable instanceof DataFxCallable) {
            ((DataFxCallable<V>) this.callable).injectStateHandler(this);
        }
    }

    /** 在后台执行任务逻辑 */
    @Override
    public V call() throws Exception {
        return callable.call();
    }
}