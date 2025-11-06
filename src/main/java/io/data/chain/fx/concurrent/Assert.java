package io.data.chain.fx.concurrent;

import java.util.Objects;

/**
 * @author lxwise
 * @create 2024-05
 * @description: 非空断言类
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public class Assert {

    public static <T> T requireNonNull(T param, String name) {
        return Objects.requireNonNull(param, "Value " + name + " should not be null!");
    }

}
