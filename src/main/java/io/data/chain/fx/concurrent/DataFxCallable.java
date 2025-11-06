package io.data.chain.fx.concurrent;

import java.util.concurrent.Callable;

/**
 * 带有任务状态支持的 {@link Callable} 接口。
 * <p>
 * 与标准 {@code Callable<V>} 不同，本接口继承自 {@link TaskWithStateHandler}，
 * 可在执行过程中通过状态处理器更新任务的进度、消息、标题等。
 * </p>
 *
 * <pre>{@code
 * public class LoadDataTask implements DataFxCallable<List<String>> {
 *     @Override
 *     public List<String> call() throws Exception {
 *         updateTaskTitle("加载数据中...");
 *         List<String> result = new ArrayList<>();
 *         for (int i = 1; i <= 10; i++) {
 *             updateTaskMessage("正在加载第 " + i + " 项");
 *             updateTaskProgress(i, 10);
 *             Thread.sleep(200);
 *         }
 *         return result;
 *     }
 * }
 * }</pre>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
@FunctionalInterface
public interface DataFxCallable<V> extends Callable<V>, TaskWithStateHandler {
}

