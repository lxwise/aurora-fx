package io.aurora.fx.components.verifyCode;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 算术验证码组件
 * 用户需要计算并输入正确的数学运算结果
 * <p>
 * 使用示例：
 * <pre>
 * VerifyConfig config = VerifyConfig.arithmetic().numberRange(1, 50);
 * ArithmeticVerifyPane pane = new ArithmeticVerifyPane(config);
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
public class ArithmeticVerifyPane extends VBox implements VerifyPane {

    private static final Logger LOGGER = Logger.getLogger(ArithmeticVerifyPane.class.getName());

    /**
     * 定时任务执行器，用于延迟重置操作
     * 使用守护线程，不阻止JVM退出
     */
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ArithmeticVerifyPane-Scheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * 验证失败后自动重置的延迟时间（毫秒）
     */
    private static final long FAIL_RESET_DELAY_MS = 2000;

    private static final String DEFAULT_STYLE = 
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);";

    private final VerifyConfig config;
    private VerifyImageUtil.ArithmeticVerifyData verifyData;
    
    // UI组件
    private StackPane imageContainer;
    private ImageView backgroundImageView;
    private TextField answerField;
    private Button submitButton;
    private Label statusLabel;
    private Button refreshButton;
    private ProgressIndicator loadingIndicator;
    
    // 状态 - 使用VerifyPane接口中定义的VerifyState
    private final ObjectProperty<VerifyState> state = new SimpleObjectProperty<>(VerifyPane.VerifyState.READY);
    private long startTime;
    
    // 回调
    private Consumer<VerifyResult> onVerifyComplete;
    private Runnable onRefresh;

    /**
     * 创建算术验证码组件（使用默认配置）
     */
    public ArithmeticVerifyPane() {
        this(new VerifyConfig(VerifyType.ARITHMETIC));
    }

    /**
     * 创建算术验证码组件
     * @param config 验证码配置，不能为null
     */
    public ArithmeticVerifyPane(VerifyConfig config) {
        this.config = config != null ? config : new VerifyConfig(VerifyType.ARITHMETIC);
        initializeUI();
        setupEventHandlers();
    }

    /**
     * 初始化UI
     */
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(15));
        setStyle(DEFAULT_STYLE);
        setAlignment(Pos.CENTER);

        // 图片容器
        imageContainer = new StackPane();
        imageContainer.setPrefSize(config.getSrcWidth(), config.getSrcHeight() - 60);
        imageContainer.setMinSize(config.getSrcWidth(), config.getSrcHeight() - 60);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 4;");

        // 背景图片（算式图片）
        backgroundImageView = new ImageView();
        backgroundImageView.setFitWidth(config.getSrcWidth());
        backgroundImageView.setFitHeight(config.getSrcHeight() - 60);
        backgroundImageView.setPreserveRatio(false);

        // 加载指示器
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        loadingIndicator.setVisible(false);

        imageContainer.getChildren().addAll(backgroundImageView, loadingIndicator);

        // 输入区域
        Label promptLabel = new Label("请输入答案：");
        promptLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");

        answerField = new TextField();
        answerField.setPrefWidth(100);
        answerField.setStyle(
                "-fx-font-size: 16; " +
                "-fx-padding: 8; " +
                "-fx-background-radius: 4; " +
                "-fx-border-color: #ddd; " +
                "-fx-border-radius: 4;"
        );
        answerField.setPromptText("答案");

        submitButton = new Button("验证");
        submitButton.setStyle(
                "-fx-background-color: #1e90ff; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14; " +
                "-fx-padding: 8 20; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;"
        );

        HBox inputBox = new HBox(10, promptLabel, answerField, submitButton);
        inputBox.setAlignment(Pos.CENTER);

        // 状态标签
        statusLabel = new Label("请计算并输入结果");
        statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");

        // 刷新按钮
        refreshButton = new Button("换一题");
        refreshButton.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #1e90ff; " +
                "-fx-border-color: #1e90ff; " +
                "-fx-border-radius: 4; " +
                "-fx-cursor: hand;"
        );
        refreshButton.setOnAction(e -> refresh());

        HBox buttonBar = new HBox(20, statusLabel, refreshButton);
        buttonBar.setAlignment(Pos.CENTER);

        getChildren().addAll(imageContainer, inputBox, buttonBar);
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 提交按钮点击事件
        submitButton.setOnAction(e -> verify());

        // 输入框回车事件
        answerField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                verify();
            }
        });

        // 只允许输入数字和负号
        answerField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("-?\\d*")) {
                answerField.setText(oldVal);
            }
        });

        state.addListener((obs, oldVal, newVal) -> updateUI(newVal));
    }

    /**
     * 验证答案
     */
    private void verify() {
        // 防御性检查：verifyData 必须已设置
        if (verifyData == null) {
            LOGGER.warning("验证码数据尚未设置，无法验证");
            statusLabel.setText("请先加载验证码");
            statusLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-size: 12;");
            return;
        }

        String answerText = answerField.getText().trim();
        
        if (answerText.isEmpty()) {
            statusLabel.setText("请输入答案");
            statusLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-size: 12;");
            return;
        }

        state.set(VerifyPane.VerifyState.VERIFYING);

        long duration = System.currentTimeMillis() - startTime;

        try {
            int userAnswer = Integer.parseInt(answerText);
            int correctAnswer = verifyData.getAnswer();
            boolean success = userAnswer == correctAnswer;

            VerifyResult result = new VerifyResult();
            result.setDuration(duration);
            result.setVerifyType(VerifyType.ARITHMETIC);

            if (success) {
                result.setSuccess(true);
                result.setMessage("验证成功");
                state.set(VerifyPane.VerifyState.SUCCESS);
            } else {
                result.setSuccess(false);
                result.setMessage("答案错误，正确答案是: " + correctAnswer);
                result.setErrorCode("WRONG_ANSWER");
                state.set(VerifyPane.VerifyState.FAIL);
            }

            // 播放结果动画
            playResultAnimation(success);

            // 回调（带异常保护）
            if (onVerifyComplete != null) {
                try {
                    onVerifyComplete.accept(result);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "验证回调执行异常", e);
                }
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("请输入有效的数字");
            statusLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-size: 12;");
            state.set(VerifyPane.VerifyState.READY);
        }
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
                answerField.setDisable(false);
                submitButton.setDisable(false);
                statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");
                submitButton.setStyle(
                        "-fx-background-color: #1e90ff; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 4;"
                );
                break;
            case LOADING:
                loadingIndicator.setVisible(true);
                answerField.setDisable(true);
                submitButton.setDisable(true);
                break;
            case VERIFYING:
                answerField.setDisable(true);
                submitButton.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #1e90ff; -fx-font-size: 12;");
                break;
            case SUCCESS:
                answerField.setDisable(true);
                submitButton.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #52c41a; -fx-font-size: 12; -fx-font-weight: bold;");
                submitButton.setStyle(
                        "-fx-background-color: #52c41a; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 4;"
                );
                break;
            case FAIL:
                answerField.setDisable(true);
                submitButton.setDisable(true);
                statusLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-size: 12; -fx-font-weight: bold;");
                submitButton.setStyle(
                        "-fx-background-color: #ff4d4f; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 4;"
                );
                break;
        }
    }

    /**
     * 设置验证码数据
     *
     * @param data 算术验证码数据，不能为null
     */
    public void setVerifyData(VerifyImageUtil.ArithmeticVerifyData data) {
        if (data == null) {
            LOGGER.warning("验证码数据不能为 null");
            return;
        }
        this.verifyData = data;
        startTime = System.currentTimeMillis();

        try {
            BufferedImage srcImage = VerifyImageUtil.base64ToImage(data.getImageBase64());
            if (srcImage != null) {
                Image fxImage = SwingFXUtils.toFXImage(srcImage, null);
                backgroundImageView.setImage(fxImage);
            }

            loadingIndicator.setVisible(false);
            answerField.clear();
            state.set(VerifyPane.VerifyState.READY);
            answerField.requestFocus();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "设置验证码数据失败", e);
            state.set(VerifyPane.VerifyState.FAIL);
        }
    }

    /**
     * 重置验证码状态
     */
    public void reset() {
        answerField.clear();
        state.set(VerifyPane.VerifyState.READY);
    }
    
    /**
     * 获取状态文本
     */
    private String getStateText(VerifyPane.VerifyState state) {
        switch (state) {
            case READY:
                return "请计算并输入结果";
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

    public String getExpression() {
        return verifyData != null ? verifyData.getExpression() : null;
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
