package io.data.chain.fx.concurrent;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Executor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.util.concurrent.Executor;

/**
 * DataFX 异步服务基类。
 * <p>
 * 封装了 {@link javafx.concurrent.Service}，并扩展了任务的可取消性与自定义执行器逻辑。
 * 适用于 JavaFX 应用中在后台线程执行任务（如加载数据、网络请求等），
 * 并在完成后自动回到 UI 线程更新界面。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * public class LoadUserService extends DataFxService<List<User>> {
 *     @Override
 *     protected Task<List<User>> createTask() {
 *         return new Task<>() {
 *             @Override
 *             protected List<User> call() throws Exception {
 *                 updateMessage("正在加载用户数据...");
 *                 List<User> users = userRepository.loadAll();
 *                 updateMessage("加载完成");
 *                 return users;
 *             }
 *         };
 *     }
 * }
 *
 * // 在 Controller 中使用
 * LoadUserService service = new LoadUserService();
 * service.setOnSucceeded(e -> userTable.setItems(FXCollections.observableArrayList(service.getValue())));
 * service.start();
 * }</pre>
 *
 * @author lxwise
 * @create 2024-05
 * @version: 1.0
 * @email: lstart980@gmail.com
 */
public abstract class DataFxService<V> extends Service<V> {

    /** 表示任务是否可被取消的属性 */
    private final BooleanProperty cancelable;

    /** 默认构造函数 */
    public DataFxService() {
        cancelable = new SimpleBooleanProperty(true);
    }

    /**
     * 执行具体任务的方法（覆盖父类的任务执行逻辑）。
     * 若未设置自定义执行器，则使用 DataFX 默认线程池。
     *
     * @param task 要执行的任务
     */
    @Override
    protected void executeTask(final Task<V> task) {
        Assert.requireNonNull(task, "task");

        cancelable.unbind();
        if (task instanceof DataFxTask) {
            cancelable.bind(((DataFxTask<V>) task).cancelableProperty());
        }

        Executor executor = getExecutor();
        if (executor != null) {
            executor.execute(task);
        } else {
            ObservableExecutor.getDefaultInstance().execute(task);
        }
    }

    /**
     * 获取“可取消”属性。
     * 用于 UI 绑定（如按钮是否可点击取消任务）。
     *
     * @return 可取消属性
     */
    public ReadOnlyBooleanProperty cancelableProperty() {
        return cancelable;
    }

    /**
     * 判断当前服务是否可取消。
     *
     * @return true 表示任务可被取消
     */
    public boolean isCancelable() {
        return cancelable.get();
    }
}

