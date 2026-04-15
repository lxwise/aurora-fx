package io.aurora.fx.concurrent;

import io.aurora.fx.common.enums.ThreadType;

import java.util.function.Function;

/**
 * ProcessDescription（过程描述）
 *
 * <p>封装任务链中的单个步骤，包括执行逻辑（函数）和执行线程类型。</p>
 *
 * @param <V> 输入参数类型
 * @param <T> 输出返回类型
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ProcessDescription<V, T> {

    /** 该步骤需要执行的函数 */
    private final Function<V, T> function;

    /** 该步骤执行的线程类型（平台线程或后台线程） */
    private final ThreadType threadType;

    /**
     * 创建新的进程描述。
     *
     * @param function 执行逻辑（函数）
     * @param threadType 执行线程类型（ThreadType.PLATFORM 或 ThreadType.EXECUTOR）
     */
    public ProcessDescription(final Function<V, T> function, final ThreadType threadType) {
        this.function = function;
        this.threadType = threadType;
    }

    /** 获取该步骤的执行逻辑函数 */
    public Function<V, T> getFunction() {
        return function;
    }

    /** 获取该步骤的线程类型 */
    public ThreadType getThreadType() {
        return threadType;
    }
}
