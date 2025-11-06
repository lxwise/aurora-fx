package io.data.chain.fx.concurrent;

/**
 * DataFX 任务接口：为支持状态反馈的任务（可用于进度条、消息提示等）。
 * <p>
 * 实现该接口的任务（例如 DataFxCallable、DataFxRunnable）
 * 可以在运行时被 DataFX 框架自动注入 {@link TaskStateHandler}，
 * 以便更新任务的执行状态（如标题、进度、消息、是否可取消等）。
 * </p>
 *
 * <p><b>典型用途：</b></p>
 * 当你的后台任务希望动态反馈状态到 UI（例如显示“正在加载第 n 项...”），
 * 可在任务代码中调用 {@link #updateTaskMessage(String)}、
 * {@link #updateTaskProgress(double, double)} 等方法。
 *
 * <pre>{@code
 * public class MyTask implements DataFxRunnable {
 *     @Override
 *     public void run() {
 *         updateTaskTitle("加载数据中...");
 *         for (int i = 0; i < 100; i++) {
 *             updateTaskMessage("正在处理第 " + i + " 项");
 *             updateTaskProgress(i, 100);
 *             Thread.sleep(50);
 *         }
 *         updateTaskMessage("加载完成！");
 *     }
 * }
 * }</pre>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public interface TaskWithStateHandler {

    /**
     * 由 DataFX 框架调用，用于向任务中注入 {@link TaskStateHandler}。
     * 用户代码无需手动调用。
     *
     * @param stateHandler 状态处理器实例
     */
    default void injectStateHandler(TaskStateHandler stateHandler) {
        TaskStateHandlerManager.add(this, stateHandler);
    }

    /**
     * 获取当前任务对应的 {@link TaskStateHandler} 实例。
     *
     * @return 状态处理器实例，若未注入则返回 null
     */
    default TaskStateHandler getStateHandler() {
        return TaskStateHandlerManager.get(this);
    }

    /** 更新任务标题（例如显示“正在加载...”） */
    default void updateTaskTitle(String title) {
        getStateHandler().updateTaskTitle(title);
    }

    /** 更新任务消息（例如显示“加载第 n 项数据”） */
    default void updateTaskMessage(String message) {
        getStateHandler().updateTaskMessage(message);
    }

    /** 更新任务进度（浮点版本） */
    default void updateTaskProgress(double workDone, double max) {
        getStateHandler().updateTaskProgress(workDone, max);
    }

    /** 更新任务进度（长整型版本） */
    default void updateTaskProgress(long workDone, long max) {
        getStateHandler().updateTaskProgress(workDone, max);
    }

    /** 设置任务是否可取消 */
    default void setCancelable(boolean cancelable) {
        getStateHandler().setCancelable(cancelable);
    }
}

