package io.data.chain.fx.concurrent;
/**
 * 【基于 Runnable 的 DataFxTask 实现】
 * <p>用于执行无返回值的异步任务。</p>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class RunnableBasedDataFxTask extends DataFxTask<Void> {

    private final Runnable runnable;

    public RunnableBasedDataFxTask(final Runnable runnable) {
        this.runnable = Assert.requireNonNull(runnable, "runnable");
        // 如果 Runnable 支持状态注入，则注入当前任务句柄
        if (this.runnable instanceof DataFxRunnable) {
            ((DataFxRunnable) this.runnable).injectStateHandler(this);
        }
    }

    /** 在后台执行任务逻辑 */
    @Override
    public Void call() throws Exception {
        runnable.run();
        return null;
    }
}

