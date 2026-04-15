package io.aurora.fx.concurrent;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 线程池配置
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ThreadPoolConfiguration {

    private static ThreadPoolConfiguration instance;

    private ThreadPoolConfiguration() {}

    public boolean isExceptionLoggingActive() {
        return true;
    }

    public int getDefaultThreadMaxSize() {
        return 32;
    }

    public int getDefaultThreadPoolStartSize() {
        return 2;
    }

    public String getThreadGroupName() {
        return "DataChainFX thread pool";
    }

    public long getDefaultThreadTimeout() {
        return 3000;
    }


    public static synchronized ThreadPoolConfiguration getInstance() {
        if(instance == null) {
            instance = new ThreadPoolConfiguration();
        }
        return instance;
    }
}
