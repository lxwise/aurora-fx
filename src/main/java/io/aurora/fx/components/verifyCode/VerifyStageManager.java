package io.aurora.fx.components.verifyCode;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 验证码窗口管理器
 * 提供验证码组件的显示和关闭功能，支持多窗口管理和线程安全操作
 * <p>
 * 使用示例：
 * <pre>
 * SliderVerifyPane sliderPane = new SliderVerifyPane(config);
 * sliderPane.show(); // 显示验证码窗口
 * 
 * // 或者使用静态方法
 * VerifyStageManager.show(sliderPane);
 * 
 * // 模态窗口
 * VerifyStageManager.showModal(sliderPane, parentStage);
 * 
 * // 应用退出时释放资源
 * VerifyStageManager.shutdown();
 * </pre>
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public class VerifyStageManager {

    private static final Logger LOGGER = Logger.getLogger(VerifyStageManager.class.getName());

    /**
     * 窗口映射表，用于管理多个验证码窗口
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private static final ConcurrentHashMap<VerifyPane, Stage> stageMap = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器，用于延迟关闭窗口
     * 使用守护线程，不阻止 JVM 退出
     */
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "VerifyStageManager-Scheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * 默认窗口宽度
     */
    private static final double DEFAULT_WIDTH = 420;

    /**
     * 默认窗口高度
     */
    private static final double DEFAULT_HEIGHT = 320;

    /**
     * 窗口标题
     */
    private static final String DEFAULT_TITLE = "验证码验证";

    /**
     * 验证成功后延迟关闭时间（毫秒）
     */
    private static final long CLOSE_DELAY_MS = 800;

    // 私有构造，禁止实例化
    private VerifyStageManager() {
    }

    /**
     * 显示验证码组件
     * 创建新窗口显示验证码组件
     * 
     * @param verifyPane 验证码组件，不能为null
     * @return 创建的Stage窗口
     * @throws IllegalArgumentException 如果verifyPane为null
     */
    public static Stage show(VerifyPane verifyPane) {
        return show(verifyPane, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 显示验证码组件（指定窗口尺寸）
     * 
     * @param verifyPane 验证码组件，不能为null
     * @param width 窗口宽度
     * @param height 窗口高度
     * @return 创建的Stage窗口
     * @throws IllegalArgumentException 如果verifyPane为null
     */
    public static Stage show(VerifyPane verifyPane, double width, double height) {
        return show(verifyPane, width, height, DEFAULT_TITLE);
    }

    /**
     * 显示验证码组件（指定窗口尺寸和标题）
     * 
     * @param verifyPane 验证码组件，不能为null
     * @param width 窗口宽度
     * @param height 窗口高度
     * @param title 窗口标题
     * @return 创建的Stage窗口
     * @throws IllegalArgumentException 如果verifyPane为null
     */
    public static Stage show(VerifyPane verifyPane, double width, double height, String title) {
        if (verifyPane == null) {
            throw new IllegalArgumentException("verifyPane 不能为 null");
        }

        // 如果已经显示，先关闭旧窗口
        if (stageMap.containsKey(verifyPane)) {
            close(verifyPane);
        }

        // 创建新窗口
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(title != null ? title : DEFAULT_TITLE);
        stage.setResizable(false);

        // 获取验证码组件的根容器
        Pane root = verifyPane.getRoot();

        // 创建场景
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);

        // 设置为模态窗口
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(null);

        // 窗口关闭时移除映射
        stage.setOnCloseRequest(event -> {
            stageMap.remove(verifyPane);
        });

        // 保存映射关系
        stageMap.put(verifyPane, stage);

        // 设置验证成功后自动关闭的回调
        setupAutoCloseCallback(verifyPane);

        // 显示窗口
        stage.show();

        return stage;
    }

    /**
     * 关闭验证码窗口
     * 
     * @param verifyPane 验证码组件，如果为null则忽略
     */
    public static void close(VerifyPane verifyPane) {
        if (verifyPane == null) {
            return;
        }
        Stage stage = stageMap.remove(verifyPane);
        if (stage != null) {
            if (Platform.isFxApplicationThread()) {
                closeStageQuietly(stage);
            } else {
                Platform.runLater(() -> closeStageQuietly(stage));
            }
        }
    }

    /**
     * 检查验证码窗口是否正在显示
     * 
     * @param verifyPane 验证码组件
     * @return 是否正在显示
     */
    public static boolean isShowing(VerifyPane verifyPane) {
        if (verifyPane == null) {
            return false;
        }
        Stage stage = stageMap.get(verifyPane);
        return stage != null && stage.isShowing();
    }

    /**
     * 获取验证码组件对应的窗口
     * 
     * @param verifyPane 验证码组件
     * @return 对应的Stage，如果不存在返回null
     */
    public static Stage getStage(VerifyPane verifyPane) {
        if (verifyPane == null) {
            return null;
        }
        return stageMap.get(verifyPane);
    }

    /**
     * 关闭所有验证码窗口
     */
    public static void closeAll() {
        // 使用ConcurrentHashMap的安全迭代
        stageMap.forEach((pane, stage) -> {
            if (stage != null) {
                Platform.runLater(() -> closeStageQuietly(stage));
            }
        });
        stageMap.clear();
    }

    /**
     * 关闭调度器，释放资源
     * 应在应用退出时调用
     */
    public static void shutdown() {
        closeAll();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("VerifyStageManager 已关闭");
    }

    /**
     * 在父窗口上显示验证码（模态）
     * 
     * @param verifyPane 验证码组件，不能为null
     * @param parentStage 父窗口，可以为null
     * @return 创建的Stage窗口
     * @throws IllegalArgumentException 如果verifyPane为null
     */
    public static Stage showModal(VerifyPane verifyPane, Stage parentStage) {
        return showModal(verifyPane, parentStage, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 在父窗口上显示验证码（模态，指定尺寸）
     * 
     * @param verifyPane 验证码组件，不能为null
     * @param parentStage 父窗口，可以为null
     * @param width 窗口宽度
     * @param height 窗口高度
     * @return 创建的Stage窗口
     * @throws IllegalArgumentException 如果verifyPane为null
     */
    public static Stage showModal(VerifyPane verifyPane, Stage parentStage, double width, double height) {
        if (verifyPane == null) {
            throw new IllegalArgumentException("verifyPane 不能为 null");
        }

        // 如果已经显示，先关闭旧窗口
        if (stageMap.containsKey(verifyPane)) {
            close(verifyPane);
        }

        // 创建新窗口
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(DEFAULT_TITLE);
        stage.setResizable(false);

        // 获取验证码组件的根容器
        Pane root = verifyPane.getRoot();

        // 创建场景
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);

        // 设置为模态窗口，阻塞父窗口
        stage.initModality(Modality.WINDOW_MODAL);
        if (parentStage != null) {
            stage.initOwner(parentStage);
        }

        // 窗口关闭时移除映射
        stage.setOnCloseRequest(event -> {
            stageMap.remove(verifyPane);
        });

        // 保存映射关系
        stageMap.put(verifyPane, stage);

        // 设置验证成功后自动关闭的回调
        setupAutoCloseCallback(verifyPane);

        // 显示窗口（阻塞直到关闭）
        stage.showAndWait();

        return stage;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 设置验证成功后自动关闭的回调
     * 包装原有回调，添加异常保护
     */
    private static void setupAutoCloseCallback(VerifyPane verifyPane) {
        Consumer<VerifyResult> originalCallback = verifyPane.getOnVerifyComplete();
        verifyPane.setOnVerifyComplete(result -> {
            // 先调用原有回调（带异常保护）
            if (originalCallback != null) {
                try {
                    originalCallback.accept(result);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "验证回调执行异常", e);
                }
            }
            // 验证成功后延迟关闭，让用户看到成功提示
            if (result != null && result.isSuccess()) {
                scheduler.schedule(() -> {
                    Platform.runLater(() -> close(verifyPane));
                }, CLOSE_DELAY_MS, TimeUnit.MILLISECONDS);
            }
        });
    }

    /**
     * 安全关闭Stage，捕获所有异常
     */
    private static void closeStageQuietly(Stage stage) {
        try {
            if (stage.isShowing()) {
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "关闭验证码窗口异常", e);
        }
    }
}
