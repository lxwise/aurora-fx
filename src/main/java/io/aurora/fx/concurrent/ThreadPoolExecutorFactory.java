package io.aurora.fx.concurrent;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>线程池工厂类，用于统一创建和管理默认线程池。</p>
 *
 * <p>该类提供了一个带有自定义 {@link ThreadFactory} 的 {@link ThreadPoolExecutor}，
 * 支持根据 {@link ThreadPoolConfiguration} 自动调整核心线程数、最大线程数、超时时间等参数。</p>
 *
 * <p>兼容 JDK 21，无需使用过时的 {@code AccessController.doPrivileged}。</p>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ThreadPoolExecutorFactory {

    private static final Logger LOGGER = Logger.getLogger(ThreadPoolExecutorFactory.class.getName());

    /**
     * 默认的线程池实例。单例模式懒加载。
     */
    private static ThreadPoolExecutor defaultExecutor;

    /**
     * 当线程中抛出未捕获异常时的统一处理逻辑。
     *
     * @param thread    发生异常的线程
     * @param throwable 异常对象
     */
    private static void onUncaughtException(final Thread thread, final Throwable throwable) {
        Assert.requireNonNull(thread, "thread");
        if (!ExceptionHandler.isLogException()) {
            LOGGER.log(Level.SEVERE, "Uncaught throwable in " + thread.getName(), throwable);
        }
        ExceptionHandler.getDefaultInstance().setException(throwable);
    }

    /**
     * 获取默认的 {@link ThreadPoolExecutor} 实例（懒加载 + 单例模式）。
     * <p>
     * 内部线程池特性：
     * <ul>
     *     <li>线程名以 {@link ThreadPoolConfiguration#getThreadGroupName()} 为前缀</li>
     *     <li>未捕获异常会交由 {@link ExceptionHandler} 处理</li>
     *     <li>核心线程数与最大线程数由配置类控制</li>
     *     <li>允许核心线程超时退出（节省资源）</li>
     * </ul>
     *
     * @return {@link ThreadPoolExecutor} 默认线程池实例
     */
    public static synchronized ThreadPoolExecutor getThreadPoolExecutor() {
        if (defaultExecutor == null) {

            // 自定义阻塞队列：当线程数未达最大值时，不进入队列，促使线程池先扩容
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>() {
                @Override
                public boolean offer(Runnable runnable) {
                    if (defaultExecutor.getPoolSize() < ThreadPoolConfiguration.getInstance().getDefaultThreadMaxSize()) {
                        return false;
                    }
                    return super.offer(runnable);
                }
            };

            // 定制线程工厂（不使用 AccessController）
            ThreadFactory threadFactory = runnable -> {
                ThreadGroup threadGroup = new ThreadGroup(ThreadPoolConfiguration.getInstance().getThreadGroupName());
                Thread thread = new Thread(threadGroup, runnable);
                thread.setUncaughtExceptionHandler(ThreadPoolExecutorFactory::onUncaughtException);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            };

            // 构造线程池
            defaultExecutor = new ThreadPoolExecutor(
                    ThreadPoolConfiguration.getInstance().getDefaultThreadPoolStartSize(),
                    ThreadPoolConfiguration.getInstance().getDefaultThreadMaxSize(),
                    ThreadPoolConfiguration.getInstance().getDefaultThreadTimeout(),
                    TimeUnit.MILLISECONDS,
                    queue,
                    threadFactory,
                    new ThreadPoolExecutor.AbortPolicy()
            );

            // 允许核心线程空闲超时关闭
            defaultExecutor.allowCoreThreadTimeOut(true);
        }
        return defaultExecutor;
    }
}
