package io.data.chain.fx.concurrent;

/**
 * 泛型发布者接口，用于发布异步任务的中间结果。
 *
 * @param <T> 发布的数据类型
 *
 * <p>通常由 {@link PublishingTask} 实现。</p>
 *
 * <pre>{@code
 * Publisher<String> publisher = values -> {
 *     System.out.println("收到: " + Arrays.toString(values));
 * };
 * publisher.publish("A", "B", "C");
 * }</pre>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
@FunctionalInterface
public interface Publisher<T> {
    /**
     * 发布一个或多个中间结果值。
     *
     * @param values 要发布的值
     */
    void publish(final T... values);
}

