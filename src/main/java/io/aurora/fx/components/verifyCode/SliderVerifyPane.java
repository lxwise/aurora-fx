package io.aurora.fx.components.verifyCode;

import javafx.animation.FadeTransition;
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
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 滑动拼图验证码组件
 * 提供类似极验的滑块验证功能
 * <p>
 * 使用示例：
 * <pre>
 * VerifyConfig config = VerifyConfig.slider().size(350, 200).tolerance(8);
 * SliderVerifyPane pane = new SliderVerifyPane(config);
 * pane.setVerifyImage(verifyImage);
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
public class SliderVerifyPane extends VBox implements VerifyPane {

    private static final Logger LOGGER = Logger.getLogger(SliderVerifyPane.class.getName());

    /**
     * 定时任务执行器，用于延迟重置操作
     * 使用守护线程，不阻止JVM退出
     */
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SliderVerifyPane-Scheduler");
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
    private VerifyImage verifyImage;
    
    // UI组件
    private Pane imageContainer;
    private ImageView backgroundImageView;
    private ImageView sliderImageView;
    private StackPane sliderThumb;
    private Label statusLabel;
    private Button refreshButton;
    private ProgressIndicator loadingIndicator;
    
    // 状态 - 使用VerifyPane接口中定义的VerifyState
    private final ObjectProperty<VerifyState> state = new SimpleObjectProperty<>(VerifyPane.VerifyState.READY);
    private final BehaviorTracker behaviorTracker = new BehaviorTracker();
    private long startTime;
    
    // 回调
    private Consumer<VerifyResult> onVerifyComplete;
    private Runnable onRefresh;
    
    // 拖拽状态
    private double dragStartX = 0;
    private double sliderStartX = 0;
    private boolean dragging = false;

    /**
     * 创建滑动验证码组件（使用默认配置）
     */
    public SliderVerifyPane() {
        this(new VerifyConfig());
    }

    /**
     * 创建滑动验证码组件
     * @param config 验证码配置，不能为null
     */
    public SliderVerifyPane(VerifyConfig config) {
        this.config = config != null ? config : new VerifyConfig();
        initializeUI();
        setupEventHandlers();
    }

    /**
     * 初始化UI
     */
    private void initializeUI() {
        setSpacing(0);
        setPadding(new Insets(10));
        setStyle(DEFAULT_STYLE);
        setAlignment(Pos.CENTER);
        
        // 图片容器 - 使用Pane而非StackPane，以支持子节点的绝对定位(layoutX/Y)
        imageContainer = new Pane();
        imageContainer.setPrefSize(config.getSrcWidth(), config.getSrcHeight());
        imageContainer.setMinSize(config.getSrcWidth(), config.getSrcHeight());
        imageContainer.setMaxSize(config.getSrcWidth(), config.getSrcHeight());
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 4;");
        // 裁剪超出区域，防止滑块溢出
        imageContainer.setClip(new javafx.scene.shape.Rectangle(config.getSrcWidth(), config.getSrcHeight()));
        
        // 背景图片
        backgroundImageView = new ImageView();
        backgroundImageView.setFitWidth(config.getSrcWidth());
        backgroundImageView.setFitHeight(config.getSrcHeight());
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setLayoutX(0);
        backgroundImageView.setLayoutY(0);
        
        // 滑块图片
        sliderImageView = new ImageView();
        sliderImageView.setFitWidth(config.getSliderWidth());
        sliderImageView.setFitHeight(config.getSliderHeight());
        sliderImageView.setPreserveRatio(false);
        sliderImageView.setSmooth(true);
        
        // 加载指示器
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        loadingIndicator.setVisible(false);
        loadingIndicator.setLayoutX(config.getSrcWidth() / 2.0 - 25);
        loadingIndicator.setLayoutY(config.getSrcHeight() / 2.0 - 25);
        
        imageContainer.getChildren().addAll(backgroundImageView, sliderImageView, loadingIndicator);
        
        // 滑块轨道容器 - 使用Pane允许thumb自由移动
        Pane sliderTrackPane = new Pane();
        double trackWidth = config.getSrcWidth();
        double trackHeight = 40;
        sliderTrackPane.setPrefSize(trackWidth, trackHeight);
        sliderTrackPane.setMinSize(trackWidth, trackHeight);
        sliderTrackPane.setMaxSize(trackWidth, trackHeight);
        sliderTrackPane.setStyle(
                "-fx-background-color: #f0f0f0; " +
                "-fx-background-radius: 20;"
        );
        
        // 状态标签 - 居中
        statusLabel = new Label("向右滑动完成验证");
        statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setPrefWidth(trackWidth);
        statusLabel.setPrefHeight(trackHeight);
        statusLabel.setLayoutX(0);
        statusLabel.setLayoutY(0);
        
        // 滑块按钮
        sliderThumb = new StackPane();
        sliderThumb.setPrefSize(50, 36);
        sliderThumb.setMinSize(50, 36);
        sliderThumb.setMaxSize(50, 36);
        sliderThumb.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f8f8); " +
                "-fx-background-radius: 4; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 4; " +
                "-fx-cursor: hand;"
        );
        sliderThumb.setEffect(new DropShadow(2, Color.rgb(0, 0, 0, 0.2)));
        sliderThumb.setLayoutX(2);
        sliderThumb.setLayoutY((trackHeight - 36) / 2);
        
        // 滑块图标
        Label thumbIcon = new Label("»");
        thumbIcon.setStyle("-fx-font-size: 18; -fx-text-fill: #666;");
        sliderThumb.getChildren().add(thumbIcon);
        
        sliderTrackPane.getChildren().addAll(statusLabel, sliderThumb);
        
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
        
        HBox buttonBar = new HBox(10, refreshButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(5, 0, 0, 0));
        
        getChildren().addAll(imageContainer, sliderTrackPane, buttonBar);
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 鼠标按下 - 在滑块按钮上
        sliderThumb.setOnMousePressed(e -> {
            if (state.get() != VerifyPane.VerifyState.READY) {
                return;
            }
            dragging = true;
            dragStartX = e.getSceneX();
            sliderStartX = sliderThumb.getLayoutX();
            sliderThumb.setCursor(Cursor.CLOSED_HAND);
            
            // 开始行为追踪
            if (config.isEnableBehaviorTracking()) {
                behaviorTracker.startTracking();
                behaviorTracker.trackEvent(e);
            }
            startTime = System.currentTimeMillis();
            
            // 阻止事件传播
            e.consume();
        });
        
        // 鼠标拖动 - 在场景级别监听，确保不会丢失事件
        setOnMouseDragged(e -> {
            if (!dragging) {
                return;
            }
            
            double deltaX = e.getSceneX() - dragStartX;
            double maxX = config.getSrcWidth() - sliderThumb.getPrefWidth() - 4;
            double newX = Math.max(2, Math.min(sliderStartX + deltaX, maxX));
            
            // 移动滑块按钮
            sliderThumb.setLayoutX(newX);
            
            // 同步移动滑块图片（滑块图片X从0开始移动）
            sliderImageView.setLayoutX(newX);
            
            // 追踪行为
            if (config.isEnableBehaviorTracking()) {
                behaviorTracker.trackEvent(e);
            }
            
            e.consume();
        });
        
        // 鼠标释放 - 在场景级别监听
        setOnMouseReleased(e -> {
            if (!dragging) {
                return;
            }
            dragging = false;
            sliderThumb.setCursor(Cursor.HAND);
            
            // 停止行为追踪
            TrajectoryData trajectoryData = null;
            if (config.isEnableBehaviorTracking()) {
                trajectoryData = behaviorTracker.stopTracking();
            }
            
            // 验证 - 使用滑块图片的X位置来检测
            verify((int) sliderImageView.getLayoutX(), trajectoryData);
            
            e.consume();
        });
        
        // 状态变化监听
        state.addListener((obs, oldVal, newVal) -> {
            updateUI(newVal);
        });
    }

    /**
     * 验证滑块位置
     */
    private void verify(int sliderX, TrajectoryData trajectoryData) {
        // 防御性检查：verifyImage 可能尚未设置
        if (verifyImage == null) {
            LOGGER.warning("验证码图片尚未设置，无法验证");
            state.set(VerifyPane.VerifyState.FAIL);
            return;
        }

        state.set(VerifyPane.VerifyState.VERIFYING);
        
        long duration = System.currentTimeMillis() - startTime;
        int tolerance = config.getTolerance();
        boolean success = verifyImage.verify(sliderX, tolerance);
        
        // 检查是否疑似机器人
        boolean robotSuspected = trajectoryData != null && trajectoryData.isRobotSuspected();
        
        VerifyResult result = new VerifyResult();
        result.setDuration(duration);
        result.setTrajectoryData(trajectoryData);
        result.setVerifyType(VerifyType.SLIDER);
        
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
            result.setMessage("验证失败，请重试");
            result.setErrorCode("POSITION_MISMATCH");
            state.set(VerifyPane.VerifyState.FAIL);
        }
        
        // 动画效果
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
     * 播放结果动画
     */
    private void playResultAnimation(boolean success) {
        // 淡入淡出效果
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
                sliderThumb.setDisable(false);
                statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
                sliderThumb.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f8f8); " +
                        "-fx-background-radius: 4; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-radius: 4;"
                );
                break;
            case LOADING:
                loadingIndicator.setVisible(true);
                sliderThumb.setDisable(true);
                break;
            case VERIFYING:
                sliderThumb.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #1e90ff; -fx-font-size: 14;");
                break;
            case SUCCESS:
                sliderThumb.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #52c41a; -fx-font-size: 14; -fx-font-weight: bold;");
                sliderThumb.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #d4edda, #c3e6cb); " +
                        "-fx-background-radius: 4; " +
                        "-fx-border-color: #52c41a; " +
                        "-fx-border-radius: 4;"
                );
                break;
            case FAIL:
                sliderThumb.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-size: 14; -fx-font-weight: bold;");
                sliderThumb.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #f8d7da, #f5c6cb); " +
                        "-fx-background-radius: 4; " +
                        "-fx-border-color: #ff4d4f; " +
                        "-fx-border-radius: 4;"
                );
                break;
        }
    }

    /**
     * 设置验证码图片
     * @param verifyImage 验证码图片数据，不能为null
     */
    public void setVerifyImage(VerifyImage verifyImage) {
        if (verifyImage == null) {
            LOGGER.warning("verifyImage 不能为 null");
            return;
        }
        this.verifyImage = verifyImage;
        
        try {
            // 设置背景图
            BufferedImage srcImage = verifyImage.getSrcBufferedImage();
            if (srcImage != null) {
                Image fxImage = SwingFXUtils.toFXImage(srcImage, null);
                backgroundImageView.setImage(fxImage);
            }
            
            // 设置滑块图
            BufferedImage cutImage = verifyImage.getCutBufferedImage();
            if (cutImage != null) {
                Image sliderImage = SwingFXUtils.toFXImage(cutImage, null);
                sliderImageView.setImage(sliderImage);
            }
            
            // 设置滑块初始位置：X=0，Y=缺口的Y坐标
            sliderImageView.setLayoutX(0);
            sliderImageView.setLayoutY(verifyImage.getYPosition());
            
            // 重置滑块按钮位置
            sliderThumb.setLayoutX(2);
            
            loadingIndicator.setVisible(false);
            state.set(VerifyPane.VerifyState.READY);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "设置验证码图片失败", e);
            state.set(VerifyPane.VerifyState.FAIL);
        }
    }

    /**
     * 重置验证码状态
     */
    public void reset() {
        sliderThumb.setLayoutX(2);
        sliderImageView.setLayoutX(0);
        behaviorTracker.reset();
        state.set(VerifyPane.VerifyState.READY);
    }
    
    /**
     * 获取状态文本
     */
    private String getStateText(VerifyPane.VerifyState state) {
        switch (state) {
            case READY:
                return "向右滑动完成验证";
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

    public VerifyImage getVerifyImage() {
        return verifyImage;
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
