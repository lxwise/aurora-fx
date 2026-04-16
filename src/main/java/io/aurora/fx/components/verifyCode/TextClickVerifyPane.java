package io.aurora.fx.components.verifyCode;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 文字点选验证码组件
 * 用户需要按顺序点击指定的文字
 * <p>
 * 使用示例：
 * <pre>
 * VerifyConfig config = VerifyConfig.textClick().clickTextCount(3);
 * TextClickVerifyPane pane = new TextClickVerifyPane(config);
 * pane.setVerifyData(verifyData);
 * pane.setOnVerifyComplete(result -> {
 *     if (result.isSuccess()) {
 *         System.out.println("验证成功");
 *     }
 * });
 * </pre>
 *
 * @author JavaFX Team
 * @since 1.0.0
 */
public class TextClickVerifyPane extends VBox implements VerifyPane {

    private static final Logger LOGGER = Logger.getLogger(TextClickVerifyPane.class.getName());

    /**
     * 定时任务执行器，用于延迟重置操作
     * 使用守护线程，不阻止JVM退出
     */
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "TextClickVerifyPane-Scheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * 验证失败后自动重置的延迟时间（毫秒）
     */
    private static final long FAIL_RESET_DELAY_MS = 1500;

    private static final String DEFAULT_STYLE =
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);";

    private final VerifyConfig config;
    private VerifyImageUtil.TextClickVerifyData verifyData;

    // UI组件
    private StackPane imageContainer;
    private ImageView backgroundImageView;
    private Label hintLabel;
    private Label statusLabel;
    private Button refreshButton;
    private ProgressIndicator loadingIndicator;
    private Pane clickPane;

    // 状态 - 使用VerifyPane接口中定义的VerifyState
    private final ObjectProperty<VerifyState> state = new SimpleObjectProperty<>(VerifyPane.VerifyState.READY);
    private final BehaviorTracker behaviorTracker;
    private long startTime;

    // 点击记录
    private final List<Point> userClickPositions = new ArrayList<>();
    private final List<Circle> clickMarkers = new ArrayList<>();
    private int currentClickIndex = 0;

    // 回调
    private Consumer<VerifyResult> onVerifyComplete;
    private Runnable onRefresh;

    /**
     * 创建文字点选验证码组件（使用默认配置）
     */
    public TextClickVerifyPane() {
        this(new VerifyConfig(VerifyType.TEXT_CLICK));
    }

    /**
     * 创建文字点选验证码组件
     * @param config 验证码配置，不能为null
     */
    public TextClickVerifyPane(VerifyConfig config) {
        this.config = config != null ? config : new VerifyConfig(VerifyType.TEXT_CLICK);
        this.behaviorTracker = new BehaviorTracker();
        this.behaviorTracker.setContinuousMode(false); // 点击模式
        initializeUI();
        setupEventHandlers();
    }

    /**
     * 初始化UI
     */
    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle(DEFAULT_STYLE);
        setAlignment(Pos.CENTER);

        // 提示标签
        hintLabel = new Label("请依次点击图中文字");
        hintLabel.setStyle(
                "-fx-font-size: 14; " +
                "-fx-text-fill: #333; " +
                "-fx-padding: 5 10; " +
                "-fx-background-color: #f0f7ff; " +
                "-fx-background-radius: 4;"
        );
        hintLabel.setWrapText(true);
        hintLabel.setMaxWidth(config.getSrcWidth() - 20);

        // 图片容器
        imageContainer = new StackPane();
        imageContainer.setPrefSize(config.getSrcWidth(), config.getSrcHeight());
        imageContainer.setMinSize(config.getSrcWidth(), config.getSrcHeight());
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 4;");

        // 背景图片
        backgroundImageView = new ImageView();
        backgroundImageView.setFitWidth(config.getSrcWidth());
        backgroundImageView.setFitHeight(config.getSrcHeight());
        backgroundImageView.setPreserveRatio(false);

        // 点击层
        clickPane = new Pane();
        clickPane.setPrefSize(config.getSrcWidth(), config.getSrcHeight());
        clickPane.setMinSize(config.getSrcWidth(), config.getSrcHeight());
        clickPane.setCursor(Cursor.CROSSHAIR);

        // 加载指示器
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        loadingIndicator.setVisible(false);

        imageContainer.getChildren().addAll(backgroundImageView, clickPane, loadingIndicator);

        // 状态标签
        statusLabel = new Label("请点击图中文字");
        statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");

        // 刷新按钮
        refreshButton = new Button("刷新");
        refreshButton.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #1e90ff; " +
                "-fx-border-color: #1e90ff; " +
                "-fx-border-radius: 4; " +
                "-fx-cursor: hand;"
        );
        refreshButton.setOnAction(e -> refresh());

        HBox buttonBar = new HBox(10, statusLabel, refreshButton);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(5, 0, 0, 0));

        getChildren().addAll(hintLabel, imageContainer, buttonBar);
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        clickPane.setOnMouseClicked(e -> {
            if (state.get() != VerifyPane.VerifyState.READY) {
                return;
            }

            // 防御性检查：verifyData 必须已设置
            if (verifyData == null || verifyData.getTargetTexts() == null) {
                LOGGER.warning("验证码数据尚未设置，忽略点击事件");
                return;
            }

            // 获取点击坐标，使用Math.round确保精度
            double rawX = e.getX();
            double rawY = e.getY();

            // 确保坐标在有效范围内
            int x = (int) Math.round(Math.max(0, Math.min(rawX, config.getSrcWidth())));
            int y = (int) Math.round(Math.max(0, Math.min(rawY, config.getSrcHeight())));

            // 开始行为追踪
            if (userClickPositions.isEmpty() && config.isEnableBehaviorTracking()) {
                behaviorTracker.startTracking();
                startTime = System.currentTimeMillis();
            }

            // 记录点击位置（使用精确坐标）
            userClickPositions.add(new Point(x, y));

            // 追踪行为
            if (config.isEnableBehaviorTracking()) {
                behaviorTracker.trackPoint(x, y);
            }

            // 显示点击标记
            showClickMarker(x, y, currentClickIndex + 1);

            currentClickIndex++;

            // 检查是否点击完成
            if (currentClickIndex >= verifyData.getTargetTexts().size()) {
                verify();
            }
        });

        state.addListener((obs, oldVal, newVal) -> updateUI(newVal));
    }

    /**
     * 显示点击标记
     * 圆圈中心精确对齐用户点击位置
     */
    private void showClickMarker(double x, double y, int index) {
        // 创建圆圈标记，半径12像素
        Circle marker = new Circle(12);
        marker.setFill(Color.TRANSPARENT);
        marker.setStroke(Color.valueOf("#1e90ff"));
        marker.setStrokeWidth(2);
        // Circle的centerX/centerY直接设置圆心坐标
        marker.setCenterX(x);
        marker.setCenterY(y);
        marker.setEffect(new DropShadow(2, Color.rgb(30, 144, 255, 0.5)));

        // 创建序号标签，居中显示在圆圈内
        Label indexLabel = new Label(String.valueOf(index));
        indexLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #1e90ff; -fx-font-weight: bold;");
        // 计算标签位置使其居中（标签宽高约8x10像素）
        indexLabel.setLayoutX(x - 3);  // 水平居中偏移
        indexLabel.setLayoutY(y - 6);  // 垂直居中偏移

        clickPane.getChildren().addAll(marker, indexLabel);
        clickMarkers.add(marker);

        // 播放动画
        ScaleTransition st = new ScaleTransition(Duration.millis(200), marker);
        st.setFromX(0.5);
        st.setFromY(0.5);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    /**
     * 验证点击结果
     */
    private void verify() {
        state.set(VerifyPane.VerifyState.VERIFYING);

        long duration = System.currentTimeMillis() - startTime;

        // 停止行为追踪
        TrajectoryData trajectoryData = null;
        if (config.isEnableBehaviorTracking()) {
            trajectoryData = behaviorTracker.stopTracking();
        }

        // 检查点击位置是否正确
        boolean success = verifyClickPositions();
        boolean robotSuspected = trajectoryData != null && trajectoryData.isRobotSuspected();

        VerifyResult result = new VerifyResult();
        result.setDuration(duration);
        result.setTrajectoryData(trajectoryData);
        result.setVerifyType(VerifyType.TEXT_CLICK);

        if (robotSuspected) {
            result.setSuccess(false);
            result.setMessage("验证失败，请手动操作");
            result.setErrorCode("ROBOT_DETECTED");
            state.set(VerifyPane.VerifyState.FAIL);
        } else if (success) {
            result.setSuccess(true);
            result.setMessage("验证成功");
            state.set(VerifyPane.VerifyState.SUCCESS);
        } else {
            result.setSuccess(false);
            result.setMessage("点击位置不正确，请重试");
            result.setErrorCode("POSITION_MISMATCH");
            state.set(VerifyPane.VerifyState.FAIL);
        }

        // 显示结果动画
        playResultAnimation(success);

        // 回调（带异常保护）
        if (onVerifyComplete != null) {
            try {
                onVerifyComplete.accept(result);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "验证回调执行异常", e);
            }
        }
    }

    /**
     * 验证点击位置
     * 使用更宽松的容差范围，确保用户能够正常通过验证
     */
    private boolean verifyClickPositions() {
        List<Point> targetPositions = verifyData.getTargetPositions();
        int configTolerance = config.getTolerance();

        // 使用更大的基础容差值（文字验证码需要更大的容差）
        // 考虑到字体大小范围 16-24，加上可能的视觉误差
        int baseTolerance = Math.max(configTolerance, 15);
        int tolerance = baseTolerance + 25; // 总容差 = 基础容差 + 字体相关容差

        if (userClickPositions.size() != targetPositions.size()) {
            LOGGER.fine("点击数量不匹配: 用户=" + userClickPositions.size() +
                             ", 目标=" + targetPositions.size());
            return false;
        }

        for (int i = 0; i < targetPositions.size(); i++) {
            Point target = targetPositions.get(i);
            Point user = userClickPositions.get(i);

            // 计算欧几里得距离
            double dx = target.x - user.x;
            double dy = target.y - user.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            LOGGER.fine("点击 " + (i + 1) + ": 目标=(" + target.x + "," + target.y +
                             "), 用户=(" + user.x + "," + user.y +
                             "), 距离=" + String.format("%.2f", distance) +
                             ", 容差=" + tolerance);

            if (distance > tolerance) {
                LOGGER.fine("点击 " + (i + 1) + " 超出容差范围");
                return false;
            }
        }

        LOGGER.fine("所有点击位置验证通过");
        return true;
    }

    /**
     * 播放结果动画
     */
    private void playResultAnimation(boolean success) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), imageContainer);
        ft.setFromValue(1.0);
        ft.setToValue(0.7);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);
        ft.play();

        // 失败后自动重置（使用scheduler替代new Thread）
        if (!success) {
            scheduler.schedule(() -> {
                Platform.runLater(() -> {
                    if (state.get() == VerifyPane.VerifyState.FAIL) {
                        reset();
                    }
                });
            }, FAIL_RESET_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 更新UI状态
     */
    private void updateUI(VerifyPane.VerifyState state) {
        statusLabel.setText(getStateText(state));

        switch (state) {
            case READY:
                clickPane.setDisable(false);
                statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");
                break;
            case LOADING:
                loadingIndicator.setVisible(true);
                clickPane.setDisable(true);
                break;
            case VERIFYING:
                clickPane.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #1e90ff; -fx-font-size: 12;");
                break;
            case SUCCESS:
                clickPane.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #52c41a; -fx-font-size: 12; -fx-font-weight: bold;");
                break;
            case FAIL:
                clickPane.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-size: 12; -fx-font-weight: bold;");
                break;
        }
    }

    /**
     * 设置验证码数据
     * 根据实际图片尺寸调整UI组件大小，确保坐标映射准确
     *
     * @param data 文字点选验证码数据，不能为null
     */
    public void setVerifyData(VerifyImageUtil.TextClickVerifyData data) {
        if (data == null) {
            LOGGER.warning("验证码数据不能为 null");
            return;
        }
        this.verifyData = data;

        try {
            BufferedImage srcImage = VerifyImageUtil.base64ToImage(data.getImageBase64());
            if (srcImage != null) {
                Image fxImage = SwingFXUtils.toFXImage(srcImage, null);
                backgroundImageView.setImage(fxImage);
                
                // 使用验证码图片的实际尺寸，确保坐标映射准确
                int actualWidth = data.getWidth();
                int actualHeight = data.getHeight();
                
                // 调整图片视图尺寸
                backgroundImageView.setFitWidth(actualWidth);
                backgroundImageView.setFitHeight(actualHeight);
                
                // 调整点击层尺寸（关键修复：确保clickPane与图片尺寸一致）
                clickPane.setPrefSize(actualWidth, actualHeight);
                clickPane.setMinSize(actualWidth, actualHeight);
                clickPane.setMaxSize(actualWidth, actualHeight);
                
                // 调整容器尺寸
                imageContainer.setPrefSize(actualWidth, actualHeight);
                imageContainer.setMinSize(actualWidth, actualHeight);
                imageContainer.setMaxSize(actualWidth, actualHeight);
                
                // 调整提示标签宽度
                hintLabel.setMaxWidth(actualWidth - 20);
            }

            hintLabel.setText(data.getHint());
            loadingIndicator.setVisible(false);
            state.set(VerifyPane.VerifyState.READY);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "设置验证码数据失败", e);
            state.set(VerifyPane.VerifyState.FAIL);
        }
    }

    /**
     * 重置验证码状态
     */
    public void reset() {
        userClickPositions.clear();
        currentClickIndex = 0;
        clickPane.getChildren().clear();
        clickMarkers.clear();
        behaviorTracker.reset();
        state.set(VerifyPane.VerifyState.READY);
    }

    /**
     * 获取状态文本
     */
    private String getStateText(VerifyPane.VerifyState state) {
        switch (state) {
            case READY:
                return "请点击图中文字";
            case LOADING:
                return "加载中...";
            case VERIFYING:
                return "验证中...";
            case SUCCESS:
                return "验证成功";
            case FAIL:
                return "验证失败";
            default:
                return "";
        }
    }

    // ==================== Getter/Setter ====================

    @Override
    public VerifyPane.VerifyState getState() {
        return state.get();
    }

    @Override
    public ObjectProperty<VerifyState> stateProperty() {
        return state;
    }

    public void setOnVerifyComplete(Consumer<VerifyResult> callback) {
        this.onVerifyComplete = callback;
    }

    @Override
    public Consumer<VerifyResult> getOnVerifyComplete() {
        return onVerifyComplete;
    }

    public void setOnRefresh(Runnable callback) {
        this.onRefresh = callback;
    }

    /**
     * 获取验证码配置
     * @return 配置对象
     */
    public VerifyConfig getConfig() {
        return config;
    }

    /**
     * 获取根容器（VerifyPane接口实现）
     * @return 当前组件实例，因为VBox继承自Pane
     */
    @Override
    public Pane getRoot() {
        return this;
    }

    /**
     * 刷新验证码（VerifyPane接口实现）
     * 调用刷新回调函数
     */
    @Override
    public void refresh() {
        if (onRefresh != null) {
            onRefresh.run();
        }
        reset();
    }
}
