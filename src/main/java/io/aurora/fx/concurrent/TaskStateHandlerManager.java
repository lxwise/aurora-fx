package io.aurora.fx.concurrent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理任务与 {@link TaskStateHandler} 的映射关系。
 * <p>
 * 该类内部使用弱引用（WeakReference）以避免内存泄漏，
 * 当任务对象被 GC 回收时，映射也会自动失效。
 * </p>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class TaskStateHandlerManager {

    /** 使用弱引用保存任务到状态处理器的映射表 */
    private static Map<WeakReference<Object>, TaskStateHandler> weakMap;

    private TaskStateHandlerManager() {
        // 工具类禁止实例化
    }

    /**
     * 向内部映射表中添加任务及其状态处理器。
     *
     * @param task         任务实例
     * @param stateHandler 对应的状态处理器
     */
    public static synchronized void add(final Object task, final TaskStateHandler stateHandler) {
        if (weakMap == null) {
            weakMap = new HashMap<>();
        }
        weakMap.put(new WeakReference<>(task), stateHandler);
    }

    /**
     * 获取指定任务对应的 {@link TaskStateHandler}。
     *
     * @param task 任务实例
     * @return 若存在则返回对应状态处理器，否则返回 null
     */
    public static synchronized TaskStateHandler get(final Object task) {
        if (weakMap == null) return null;
        for (WeakReference<Object> ref : weakMap.keySet()) {
            Object t = ref.get();
            if (t == null) {
                // 可在这里清理已被回收的引用
                continue;
            } else if (t.equals(task)) {
                return weakMap.get(ref);
            }
        }
        return null;
    }
}
