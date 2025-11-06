package io.data.chain.fx.concurrent;

/**
 * 【任务状态处理接口】
 * <p>定义了任务在执行过程中的状态更新行为。</p>
 * 实现该接口的类可以：
 * <ul>
 *   <li>动态更新任务标题与提示消息</li>
 *   <li>更新任务进度</li>
 *   <li>设置任务是否允许取消</li>
 * </ul>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public interface TaskStateHandler {

    /** 更新任务标题 */
    void updateTaskTitle(String title);

    /** 更新任务提示信息 */
    void updateTaskMessage(String message);

    /** 更新任务进度（浮点型） */
    void updateTaskProgress(double workDone, double max);

    /** 更新任务进度（整型） */
    void updateTaskProgress(long workDone, long max);

    /** 设置任务是否可取消 */
    void setCancelable(boolean cancelable);
}

