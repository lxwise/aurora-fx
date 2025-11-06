package io.data.chain.fx.concurrent;

import io.data.chain.fx.common.enums.ThreadType;
import io.data.chain.fx.common.utils.ConcurrentUtils;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 任务链（ProcessChain）
 *
 * <p>用于以链式结构定义和执行一系列异步或同步任务的执行器。</p>
 *
 * <p>它可以让开发者将多个逻辑步骤（函数、消费者、任务等）按顺序组织，
 * 并自动在合适的线程中执行（JavaFX 主线程或后台线程）。
 * 支持异常处理、任务发布、循环执行、以及任务完成后的收尾操作等功能。</p>
 *
 * <p>该类是 DataFX 框架中用于统一管理多线程任务执行的核心组件。</p>
 *
 * <p>完整示例1：</p>
 * <pre>{@code
  ProcessChain.create()
       .addRunnableInPlatformThread(() -> {
       // 第一步：在 JavaFX 主线程执行 UI 更新逻辑
              button.setDisable(true);
      })
      .addSupplierInExecutor(() -> {
          // 第二步：后台线程执行耗时操作 例如请求接口
          return "后台任务结果";
      })
      .addConsumerInPlatformThread(result -> {
          // 第三步：拿到接口结果 在 JavaFX 主线程更新 UI
          label.setText(result);
      })
      .onException(e -> {
          // 异常处理
          System.err.println("执行出错：" + e.getMessage());
      })
      .withFinal(() -> System.out.println("任务链结束"))
      .run();
 *

 * <p>完整示例2：执行保存更新删除等操作</p>
    ProcessChain.create()
        .addRunnableInExecutor(() -> System.err.println("执行操作请求"))
        .addRunnableInPlatformThread(() -> {
            //在 JavaFX 主线程执行 UI 更新逻辑/例如关闭弹窗,重新请求加载列表
            dialog.close();
            query();
         }).onException(e -> e.pr intStackTrace())
        .run();
 * }</pre>
 *
 * @param <T> 当前链条上最后一个步骤的返回类型
 */
public class ProcessChain<T> {

    /** 任务链中保存的所有步骤描述 */
    private final List<ProcessDescription<?, ?>> processes;

    /** 用于执行后台任务的线程池执行器 */
    private final Executor executorService;

    /** 异常处理器，用于捕获任务中抛出的异常 */
    private ExceptionHandler exceptionHandler;

    /** 任务链执行完毕后的最终回调（无论是否异常都会执行） */
    private Runnable finalRunnable;

    /** 默认构造函数，使用默认线程池 */
    public ProcessChain() {
        this(ObservableExecutor.getDefaultInstance());
    }

    /**
     * 使用指定执行器创建任务链
     * @param executorService 执行后台任务的线程池
     */
    public ProcessChain(final Executor executorService) {
        this(executorService, null, null, null);
    }

    /** 私有构造函数，用于链式调用时复制上下文 */
    private ProcessChain(final Executor executorService,
                         final List<ProcessDescription<?, ?>> processes,
                         final ExceptionHandler exceptionHandler,
                         final Runnable finalRunnable) {
        this.executorService = Assert.requireNonNull(executorService, "executorService");
        this.processes = new ArrayList<>();
        if (processes != null) {
            this.processes.addAll(processes);
        }
        this.exceptionHandler = exceptionHandler;
        this.finalRunnable = finalRunnable;
    }

    /** 创建默认任务链 */
    public static ProcessChain<Void> create() {
        return new ProcessChain<>();
    }

    /** 创建指定执行器的任务链 */
    public static ProcessChain<Void> create(final Executor executorService) {
        return new ProcessChain<>(executorService);
    }

    /**
     * 向任务链中添加一个函数步骤。
     * 该函数会将上一个步骤的结果作为输入，并返回新的输出结果。
     * @param function 执行函数
     * @param type 执行线程类型（平台线程或后台线程）
     * @return 新的 ProcessChain 实例
     */
    public <V> ProcessChain<V> addFunction(final Function<T, V> function, final ThreadType type) {
        return addProcessDescription(new ProcessDescription<>(function, type));
    }

    /** 添加自定义的 ProcessDescription */
    public <V> ProcessChain<V> addProcessDescription(final ProcessDescription<T, V> processDescription) {
        processes.add(processDescription);
        return new ProcessChain<>(executorService, processes, exceptionHandler, finalRunnable);
    }

    /** 在 JavaFX 主线程执行函数 */
    public <V> ProcessChain<V> addFunctionInPlatformThread(final Function<T, V> function) {
        return addFunction(function, ThreadType.PLATFORM);
    }

    /** 在后台线程执行函数 */
    public <V> ProcessChain<V> addFunctionInExecutor(final Function<T, V> function) {
        return addFunction(function, ThreadType.EXECUTOR);
    }

    /**
     * 向任务链中添加一个 Runnable 步骤。
     * @param runnable 可运行任务
     * @param type 执行线程类型
     */
    public ProcessChain<Void> addRunnable(final Runnable runnable, final ThreadType type) {
        Assert.requireNonNull(runnable, "runnable");
        return addFunction(e -> {
            runnable.run();
            return null;
        }, type);
    }

    /** 在 JavaFX 主线程执行 Runnable */
    public ProcessChain<Void> addRunnableInPlatformThread(final Runnable runnable) {
        return addRunnable(runnable, ThreadType.PLATFORM);
    }

    /** 在后台线程执行 Runnable */
    public ProcessChain<Void> addRunnableInExecutor(final Runnable runnable) {
        return addRunnable(runnable, ThreadType.EXECUTOR);
    }

    /** 添加一个 Consumer（消费上一步结果） */
    public ProcessChain<Void> addConsumer(final Consumer<T> consumer, final ThreadType type) {
        Assert.requireNonNull(consumer, "consumer");
        return addFunction(e -> {
            consumer.accept(e);
            return null;
        }, type);
    }

    public ProcessChain<Void> addConsumerInPlatformThread(final Consumer<T> consumer) {
        return addConsumer(consumer, ThreadType.PLATFORM);
    }

    public ProcessChain<Void> addConsumerInExecutor(final Consumer<T> consumer) {
        return addConsumer(consumer, ThreadType.EXECUTOR);
    }

    /** 添加一个 Supplier（提供新数据） */
    public <V> ProcessChain<V> addSupplier(final Supplier<V> supplier, final ThreadType type) {
        Assert.requireNonNull(supplier, "supplier");
        return addFunction(e -> supplier.get(), type);
    }

    public <V> ProcessChain<V> addSupplierInPlatformThread(final Supplier<V> supplier) {
        return addSupplier(supplier, ThreadType.PLATFORM);
    }

    public <V> ProcessChain<V> addSupplierInExecutor(final Supplier<V> supplier) {
        return addSupplier(supplier, ThreadType.EXECUTOR);
    }

    /**
     * 添加一个带发布功能的任务。
     * <p>该任务允许在执行过程中向外发布中间结果（如列表内容）。</p>
     */
    public <V> ProcessChain<List<V>> addPublishingTask(final Supplier<List<V>> supplier,
                                                       final Consumer<Publisher<V>> consumer) {
        Assert.requireNonNull(supplier, "supplier");
        Assert.requireNonNull(consumer, "consumer");
        return addFunction(e -> {
            List<V> list = supplier.get();
            Publisher<V> publisher = p -> {
                try {
                    ConcurrentUtils.runAndWait(() -> list.addAll(Arrays.asList(p)));
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            };
            consumer.accept(publisher);
            return list;
        }, ThreadType.EXECUTOR);
    }

    public <V> ProcessChain<List<V>> addPublishingTask(final List<V> list, final Consumer<Publisher<V>> consumer) {
        return addPublishingTask(() -> list, consumer);
    }

    public <V> ProcessChain<List<V>> addPublishingTask(final Consumer<Publisher<V>> consumer) {
        return addPublishingTask(() -> FXCollections.observableArrayList(), consumer);
    }

    /**
     * 设置异常处理逻辑
     * @param consumer 异常回调
     */
    public ProcessChain<T> onException(final Consumer<Throwable> consumer) {
        Assert.requireNonNull(consumer, "consumer");
        this.exceptionHandler = new ExceptionHandler();
        exceptionHandler.exceptionProperty().addListener(e -> consumer.accept(exceptionHandler.getException()));
        return this;
    }

    /** 设置自定义异常处理器 */
    public ProcessChain<T> onException(final ExceptionHandler handler) {
        this.exceptionHandler = handler;
        return this;
    }

    /** 设置任务完成后的最终回调（无论成功或失败都会执行） */
    public ProcessChain<T> withFinal(final Runnable finalRunnable) {
        this.finalRunnable = finalRunnable;
        return this;
    }

    /** 等待另一个 Worker 执行完成并返回结果 */
    public <V> ProcessChain<V> waitFor(final Worker<V> worker) {
        return addSupplierInExecutor(() -> {
            try {
                return ConcurrentUtils.waitFor(worker);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** 内部方法：根据线程类型执行指定任务 */
    private <U, V> V execute(final U inputParameter, final ProcessDescription<U, V> processDescription)
            throws InterruptedException, ExecutionException {
        Assert.requireNonNull(processDescription, "processDescription");
        if (processDescription.getThreadType().equals(ThreadType.EXECUTOR)) {
            return processDescription.getFunction().apply(inputParameter);
        } else {
            return ConcurrentUtils.runCallableAndWait(() -> processDescription.getFunction().apply(inputParameter));
        }
    }

    /** 无限循环执行任务链 */
    public Task<T> repeatInfinite() {
        return repeat(Integer.MAX_VALUE);
    }

    /** 无限循环执行任务链，并在每次之间暂停指定时间 */
    public Task<T> repeatInfinite(final Duration pauseTime) {
        return repeat(Integer.MAX_VALUE, pauseTime);
    }

    /** 重复执行任务链指定次数 */
    public Task<T> repeat(final int count) {
        return repeat(count, Duration.ZERO);
    }

    /**
     * 重复执行任务链指定次数，并在每次之间暂停一段时间
     * @param count 重复次数
     * @param pauseTime 每次循环之间的暂停时间
     */
    public Task<T> repeat(final int count, final Duration pauseTime) {
        Assert.requireNonNull(pauseTime, "pauseTime");
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                try {
                    Object lastResult = null;
                    for (int i = 0; i < count || count == Integer.MAX_VALUE; i++) {
                        lastResult = null;
                        for (ProcessDescription<?, ?> processDescription : processes) {
                            lastResult = execute(lastResult, (ProcessDescription<Object, ?>) processDescription);
                        }
                        Thread.sleep((long) pauseTime.toMillis());
                        if (count == Integer.MAX_VALUE) i = 0; // 无限循环
                    }
                    return (T) lastResult;
                } catch (Exception e) {
                    if (exceptionHandler != null) {
                        ConcurrentUtils.runAndWait(() -> exceptionHandler.setException(e));
                    }
                    throw e;
                } finally {
                    if (finalRunnable != null) {
                        ConcurrentUtils.runAndWait(() -> finalRunnable.run());
                    }
                }
            }
        };
        executorService.execute(task);
        return task;
    }

    /** 执行任务链一次 */
    public Task<T> run() {
        return repeat(1);
    }
}

