package io.aurora.fx.common.enums;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 定义线程的类型。这支持 JavaFX 应用程序线程和任何其他线程
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public enum ThreadType {
    /**
     * JavaFX 应用程序线程
     */
    PLATFORM,

    /**
     * 不是 JavaFX 应用程序线程的后台线程
     */
    EXECUTOR;
}
