package io.data.chain.fx.concurrent;
/**
 * 带有任务状态支持的 {@link Runnable} 接口。
 * <p>
 * 与标准 {@code Runnable} 不同，本接口可以反馈任务执行状态。
 * </p>
 *
 * <pre>{@code
 * public class DownloadTask implements DataFxRunnable {
 *     @Override
 *     public void run() {
 *         updateTaskTitle("文件下载中");
 *         for (int i = 1; i <= 100; i++) {
 *             updateTaskProgress(i, 100);
 *             updateTaskMessage("已下载 " + i + "%");
 *             Thread.sleep(100);
 *         }
 *         updateTaskMessage("下载完成！");
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
public interface DataFxRunnable extends Runnable, TaskWithStateHandler {
}

