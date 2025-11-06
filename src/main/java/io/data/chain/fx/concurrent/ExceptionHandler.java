package io.data.chain.fx.concurrent;

import io.data.chain.fx.common.utils.ConcurrentUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DataFX 异常统一处理类。
 * <p>
 * 用于捕获 JavaFX 异步任务（如 {@link javafx.concurrent.Task}、{@link javafx.concurrent.Service}）
 * 中抛出的异常，并统一记录或传递到 UI 层。
 * 支持通过属性绑定监听异常变化。
 * </p>
 *
 * <p><b>功能特性：</b></p>
 * <ul>
 *     <li>支持统一异常捕获与日志输出</li>
 *     <li>支持将后台线程异常安全地抛回 JavaFX 主线程</li>
 *     <li>可与 {@link ObservableExecutor} 联动</li>
 * </ul>
 *
 * <p><b>示例：</b></p>
 * <pre>{@code
 * ExceptionHandler handler = ExceptionHandler.getDefaultInstance();
 * handler.exceptionProperty().addListener((obs, old, ex) -> {
 *     if (ex != null) {
 *         showAlert("任务出错", ex.getMessage());
 *     }
 * });
 * }</pre>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class ExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());

    private static ChangeListener<Throwable> loggerListener;
    private static ExceptionHandler defaultInstance;
    private static boolean logException = false;

    /** 当前任务中的异常对象 */
    private ObjectProperty<Throwable> exception;

    /** 默认构造函数 */
    public ExceptionHandler() {
    }

    /**
     * 获取全局默认的 {@code ExceptionHandler} 实例。
     * 若不存在则自动创建，并根据配置启用日志记录。
     *
     * @return 默认异常处理器实例
     */
    public static synchronized ExceptionHandler getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new ExceptionHandler();
            setExceptionLogging(ThreadPoolConfiguration.getInstance().isExceptionLoggingActive());
        }
        return defaultInstance;
    }

    /**
     * 返回一个用于日志输出的监听器。
     * 当异常变化时，自动记录到日志。
     *
     * @return 异常日志监听器
     */
    public static ChangeListener<Throwable> getLoggerListener() {
        if (loggerListener == null) {
            loggerListener = (ob, o, e) -> {
                if (e != null) {
                    if (e instanceof Exception) {
                        LOGGER.log(Level.SEVERE, "DataFX Exception Handler", e);
                    } else {
                        LOGGER.log(Level.SEVERE, "DataFX Exception Handler: " + e.getMessage());
                    }
                }
            };
        }
        return loggerListener;
    }

    /**
     * 启用或关闭异常日志记录。
     *
     * @param log true 表示启用日志输出
     */
    public static void setExceptionLogging(boolean log) {
        if (log) {
            getDefaultInstance().exceptionProperty().addListener(getLoggerListener());
        } else {
            getDefaultInstance().exceptionProperty().removeListener(getLoggerListener());
        }
        logException = log;
    }

    /**
     * 判断当前是否开启了异常日志输出。
     *
     * @return true 表示启用
     */
    public static boolean isLogException() {
        return logException;
    }

    /**
     * 获取最近一次任务异常。
     *
     * @return 异常对象
     */
    public Throwable getException() {
        return exceptionProperty().get();
    }

    /**
     * 设置当前异常。
     * 若在 JavaFX 主线程以外调用，则会安全地切回主线程执行。
     *
     * @param exception 异常对象
     */
    public void setException(Throwable exception) {
        if (Platform.isFxApplicationThread()) {
            exceptionProperty().set(exception);
        } else {
            try {
                ConcurrentUtils.runAndWait(() -> exceptionProperty().set(exception));
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.log(Level.SEVERE, "无法在 JavaFX 应用线程中处理异常！", e);
                LOGGER.log(Level.SEVERE, "初始异常：", exception);
            }
        }
    }

    /**
     * 监听某个 Worker（任务或服务）的异常属性。
     * 当该任务出现异常时，自动回调 {@link #setException(Throwable)}。
     *
     * @param <T>    Worker 的返回类型
     * @param worker 要监听的 Worker
     */
    public <T> void observeWorker(Worker<T> worker) {
        worker.exceptionProperty().addListener((ob, ol, e) -> setException(e));
    }

    /**
     * 获取异常属性对象，用于绑定或监听。
     *
     * @return 异常属性
     */
    public ObjectProperty<Throwable> exceptionProperty() {
        if (exception == null) {
            exception = new SimpleObjectProperty<>();
        }
        return exception;
    }
}

