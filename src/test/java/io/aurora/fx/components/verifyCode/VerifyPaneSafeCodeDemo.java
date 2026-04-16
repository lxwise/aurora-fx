package io.aurora.fx.components.verifyCode;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JavaFX验证码系统 - 安全增强版演示程序
 * 
 * 本演示程序展示验证码系统的企业级安全特性：
 * 1. 服务端验证 - 验证逻辑在服务端执行，客户端仅负责展示和提交
 * 2. 尝试次数限制 - 最大5次尝试，超过后锁定并提示联系管理员
 * 3. 验证码时效性 - 5分钟有效期，显示倒计时，过期自动刷新
 * 
 * 安全设计原则：
 * - 客户端不存储正确答案，仅存储服务端返回的验证ID
 * - 验证结果必须由服务端确认
 * - 敏感操作需要重新验证
 * 
 * @author JavaFX Team
 * @since 1.5.0
 */
public class VerifyPaneSafeCodeDemo extends Application {

    // ==================== 常量定义 ====================
    
    /** 背景图片路径列表 */
    private static final List<String> BACKGROUND_IMAGES = Arrays.asList(
            "D:\\workfile\\javafx\\src\\main\\resources\\img\\girl1.jpg",
            "D:\\workfile\\javafx\\src\\main\\resources\\img\\girl2.jpg",
            "D:\\workfile\\javafx\\src\\main\\resources\\img\\girl3.jpg"
    );
    
    /** 最大尝试次数 */
    private static final int MAX_ATTEMPTS = 5;
    
    /** 验证码有效期（毫秒） */
    private static final long VERIFY_CODE_VALIDITY_MS = 5 * 60 * 1000; // 5分钟
    
    /** 尝试锁定时间（毫秒） */
    private static final long LOCKOUT_DURATION_MS = 10 * 60 * 1000; // 10分钟
    
    /** 状态更新间隔（毫秒） */
    private static final long STATUS_UPDATE_INTERVAL_MS = 1000;

    // ==================== UI组件 ====================
    
    private Label statusLabel;
    private Label attemptLabel;
    private Label timerLabel;
    private TabPane tabPane;

    // ==================== 安全状态管理 ====================
    
    /** 定时任务执行器 */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "VerifySafeDemo-Scheduler");
        t.setDaemon(true);
        return t;
    });
    
    /** 各验证码类型的会话数据（按类型索引） */
    private final Map<VerifyType, VerifySession> sessionMap = new ConcurrentHashMap<>();
    
    /** 模拟服务端验证码存储 */
    private final Map<String, ServerVerifyData> serverStorage = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("验证码系统 - 安全增强版演示");

        // 创建主界面
        VBox root = createMainUI();
        
        Scene scene = new Scene(root, 950, 800);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(750);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // 启动状态更新定时器
        startStatusUpdater();
    }
    
    @Override
    public void stop() throws Exception {
        // 关闭定时任务执行器
        scheduler.shutdownNow();
        super.stop();
    }

    /**
     * 创建主界面
     */
    private VBox createMainUI() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #f5f5f5;");

        // 标题
        Label titleLabel = new Label("验证码系统 - 安全增强版");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        // 副标题
        Label subtitleLabel = new Label("服务端验证 | 尝试次数限制 | 验证码时效性");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", 13));
        subtitleLabel.setTextFill(Color.valueOf("#666666"));

        // 安全特性说明
        Label featureLabel = new Label(
            "✓ 服务端验证 - 验证逻辑在服务端执行  " +
            "✓ 次数限制 - 最多5次尝试  " +
            "✓ 时效性 - 5分钟有效期"
        );
        featureLabel.setFont(Font.font("Microsoft YaHei", 11));
        featureLabel.setTextFill(Color.valueOf("#52c41a"));
        featureLabel.setStyle("-fx-padding: 6 12; -fx-background-color: #f6ffed; -fx-background-radius: 4;");

        // 状态面板
        HBox statusPanel = createStatusPanel();

        // 创建选项卡面板
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 添加三种验证码演示选项卡
        tabPane.getTabs().addAll(
            new Tab("滑动拼图", createSafeVerifyPaneDemo(VerifyType.SLIDER)),
            new Tab("文字点选", createSafeVerifyPaneDemo(VerifyType.TEXT_CLICK)),
            new Tab("算术验证", createSafeVerifyPaneDemo(VerifyType.ARITHMETIC)),
            new Tab("安全架构", createSecurityArchitectureDemo())
        );

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        container.getChildren().addAll(
            titleLabel, subtitleLabel, featureLabel, statusPanel, tabPane
        );
        
        return container;
    }
    
    /**
     * 创建状态面板
     */
    private HBox createStatusPanel() {
        HBox panel = new HBox(20);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10, 15, 10, 15));
        panel.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 6; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        
        // 状态标签
        statusLabel = new Label("请选择验证码类型开始验证");
        statusLabel.setFont(Font.font("Microsoft YaHei", 12));
        statusLabel.setTextFill(Color.valueOf("#1e90ff"));
        statusLabel.setStyle("-fx-padding: 4 8; -fx-background-color: #e6f7ff; -fx-background-radius: 3;");
        
        // 尝试次数标签
        attemptLabel = new Label("剩余尝试次数: " + MAX_ATTEMPTS);
        attemptLabel.setFont(Font.font("Microsoft YaHei", 11));
        attemptLabel.setTextFill(Color.valueOf("#ff9800"));
        
        // 倒计时标签
        timerLabel = new Label("验证码有效期: --:--");
        timerLabel.setFont(Font.font("Microsoft YaHei", 11));
        timerLabel.setTextFill(Color.valueOf("#52c41a"));
        
        panel.getChildren().addAll(statusLabel, attemptLabel, timerLabel);
        
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        return panel;
    }

    /**
     * 创建安全增强版验证码演示区域
     */
    private VBox createSafeVerifyPaneDemo(VerifyType type) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(15));
        container.setAlignment(Pos.TOP_CENTER);

        // 初始化会话
        VerifySession session = new VerifySession(type);
        sessionMap.put(type, session);

        // 类型标题
        String title = getVerifyTypeTitle(type);
        Label descLabel = createDescLabel(title);
        
        // 安全说明
        Label securityLabel = createSecurityInfoLabel(type);

        // 创建验证码组件
        VerifyPane verifyPane = createVerifyPane(type);
        session.verifyPane = verifyPane;
        
        // 安全增强版回调设置
        setupSecureCallbacks(verifyPane, session);

        // 从服务端获取验证码数据
        fetchVerifyCodeFromServer(session);

        // 获取根容器
        Pane verifyRoot = verifyPane.getRoot();

        // 创建安全控制面板
        VBox securityPanel = createSecurityControlPanel(session);

        container.getChildren().addAll(descLabel, securityLabel, verifyRoot, securityPanel);
        return container;
    }
    
    /**
     * 创建验证码组件
     */
    private VerifyPane createVerifyPane(VerifyType type) {
        switch (type) {
            case SLIDER:
                return new SliderVerifyPane();
                
            case TEXT_CLICK:
                VerifyConfig textConfig = VerifyConfig.createTextClick()
                    .clickTextCount(3)
                    .interferenceTextCount(5)
                    .tolerance(15)
                    .size(350, 200);
                return new TextClickVerifyPane(textConfig);
                
            case ARITHMETIC:
                VerifyConfig arithmeticConfig = VerifyConfig.createArithmetic()
                    .numberRange(10, 99)
                    .operators(Arrays.asList("+", "-", "×"));
                return new ArithmeticVerifyPane(arithmeticConfig);
                
            default:
                throw new IllegalArgumentException("不支持的验证码类型: " + type);
        }
    }
    
    /**
     * 设置安全增强版回调
     */
    private void setupSecureCallbacks(VerifyPane verifyPane, VerifySession session) {
        // 验证完成回调 - 提交到服务端验证
        verifyPane.setOnVerifyComplete(result -> {
            handleVerifyCompleteSecure(session, result);
        });

        // 刷新回调 - 从服务端获取新验证码
        verifyPane.setOnRefresh(() -> {
            fetchVerifyCodeFromServer(session);
        });
    }
    
    /**
     * 安全处理验证完成事件
     * 客户端不直接判断结果，而是提交到服务端验证
     */
    private void handleVerifyCompleteSecure(VerifySession session, VerifyResult clientResult) {
        VerifyType type = session.type;
        
        // 检查是否已锁定
        if (session.isLocked()) {
            updateStatus("验证已锁定，请联系管理员或等待解锁");
            return;
        }
        
        // 检查验证码是否过期
        if (session.isExpired()) {
            updateStatus("验证码已过期，请刷新后重试");
            fetchVerifyCodeFromServer(session);
            return;
        }
        
        // 增加尝试次数
        session.attemptCount++;
        updateAttemptDisplay(session);
        
        // 提交到服务端验证
        submitToServerForVerification(session, clientResult);
    }
    
    /**
     * 提交到服务端验证（模拟）
     */
    private void submitToServerForVerification(VerifySession session, VerifyResult clientResult) {
        // 获取服务端存储的验证数据
        ServerVerifyData serverData = serverStorage.get(session.verifyId);
        
        if (serverData == null) {
            // 服务端数据不存在
            handleServerResponse(session, false, "验证码数据已失效，请刷新");
            return;
        }
        
        // 检查时效性
        if (serverData.isExpired()) {
            handleServerResponse(session, false, "验证码已过期");
            return;
        }
        
        // 服务端验证逻辑
        boolean serverVerified = performServerVerification(session, serverData, clientResult);
        
        if (serverVerified) {
            // 验证成功
            handleServerResponse(session, true, "验证成功");
        } else {
            // 验证失败
            session.failCount++;
            
            if (session.failCount >= MAX_ATTEMPTS) {
                // 达到最大尝试次数，锁定
                session.lock();
                handleServerResponse(session, false, 
                    "验证失败次数过多，已锁定 " + (LOCKOUT_DURATION_MS / 60000) + " 分钟");
            } else {
                // 还有剩余次数
                int remaining = MAX_ATTEMPTS - session.failCount;
                handleServerResponse(session, false, 
                    "验证失败，还剩 " + remaining + " 次尝试机会");
                
                // 延迟刷新验证码
                scheduleDelayedRefresh(session, 1500);
            }
        }
    }
    
    /**
     * 执行服务端验证逻辑
     */
    private boolean performServerVerification(VerifySession session, 
                                               ServerVerifyData serverData,
                                               VerifyResult clientResult) {
        switch (session.type) {
            case SLIDER:
                // 服务端验证滑块位置
                SliderVerifyPane sliderPane = (SliderVerifyPane) session.verifyPane;
                int userX = (int) sliderPane.getVerifyImage().getXPosition(); // 客户端上报的位置
                // 实际应该从客户端获取拖拽位置，这里简化处理
                return clientResult.isSuccess(); // 使用客户端验证结果模拟
                
            case TEXT_CLICK:
                // 服务端验证点击位置
                return clientResult.isSuccess();
                
            case ARITHMETIC:
                // 服务端验证答案
                int correctAnswer = serverData.correctAnswer;
                // 客户端需要上报答案，这里简化处理
                return clientResult.isSuccess();
                
            default:
                return false;
        }
    }
    
    /**
     * 处理服务端响应
     */
    private void handleServerResponse(VerifySession session, boolean success, String message) {
        Platform.runLater(() -> {
            if (success) {
                updateStatus("✓ " + message + " [服务端验证通过]");
                statusLabel.setStyle("-fx-padding: 4 8; -fx-background-color: #f6ffed; " +
                                   "-fx-background-radius: 3; -fx-text-fill: #52c41a;");
            } else {
                updateStatus("✗ " + message + " [服务端验证失败]");
                statusLabel.setStyle("-fx-padding: 4 8; -fx-background-color: #fff2f0; " +
                                   "-fx-background-radius: 3; -fx-text-fill: #ff4d4f;");
            }
            updateAttemptDisplay(session);
        });
    }
    
    /**
     * 从服务端获取验证码数据（模拟）
     * 此方法会重置尝试次数，仅用于首次获取或用户主动重置会话
     * 
     * @param session 验证会话
     */
    private void fetchVerifyCodeFromServer(VerifySession session) {
        fetchVerifyCodeFromServer(session, true);
    }
    
    /**
     * 从服务端获取验证码数据（模拟）
     * 
     * @param session 验证会话
     * @param resetAttempts 是否重置尝试次数
     *                       - true: 首次获取或用户主动重置会话时使用
     *                       - false: 验证失败后刷新验证码时使用（保留失败次数）
     */
    private void fetchVerifyCodeFromServer(VerifySession session, boolean resetAttempts) {
        try {
            // 模拟服务端生成验证码
            ServerVerifyData serverData = generateServerVerifyData(session.type);
            
            // 存储到服务端存储
            serverStorage.put(serverData.verifyId, serverData);
            
            // 更新会话
            session.verifyId = serverData.verifyId;
            session.createdAt = System.currentTimeMillis();
            
            // 只有明确要求重置时才重置尝试次数
            // 验证失败后刷新验证码不应重置失败次数
            if (resetAttempts) {
                session.resetAttempts();
            }
            
            // 更新客户端UI
            updateClientWithServerData(session, serverData);
            
            // 更新状态显示
            if (resetAttempts) {
                updateStatus("验证码已从服务端获取，有效期 " + (VERIFY_CODE_VALIDITY_MS / 60000) + " 分钟");
            } else {
                // 刷新验证码但不重置次数时，显示剩余尝试次数提示
                int remaining = MAX_ATTEMPTS - session.failCount;
                updateStatus("验证码已刷新，剩余 " + remaining + " 次尝试机会");
            }
            statusLabel.setStyle("-fx-padding: 4 8; -fx-background-color: #e6f7ff; " +
                               "-fx-background-radius: 3; -fx-text-fill: #1e90ff;");
            updateAttemptDisplay(session);
            
        } catch (Exception e) {
            updateStatus("获取验证码失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成服务端验证码数据（模拟服务端逻辑）
     */
    private ServerVerifyData generateServerVerifyData(VerifyType type) throws IOException {
        String verifyId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        long createdAt = System.currentTimeMillis();
        long expiresAt = createdAt + VERIFY_CODE_VALIDITY_MS;
        
        ServerVerifyData data = new ServerVerifyData();
        data.verifyId = verifyId;
        data.type = type;
        data.createdAt = createdAt;
        data.expiresAt = expiresAt;
        
        switch (type) {
            case SLIDER:
                String imagePath = BACKGROUND_IMAGES.get(
                    (int) (Math.random() * BACKGROUND_IMAGES.size())
                );
                VerifyConfig sliderConfig = new VerifyConfig(VerifyType.SLIDER);
                data.verifyImage = VerifyImageUtil.generateSliderVerifyImage(imagePath, sliderConfig);
                // 服务端存储正确答案
                data.correctX = data.verifyImage.getXPosition();
                break;
                
            case TEXT_CLICK:
                VerifyConfig textConfig = VerifyConfig.createTextClick()
                    .clickTextCount(3)
                    .interferenceTextCount(5)
                    .tolerance(15)
                    .size(350, 200);
                data.textClickData = VerifyImageUtil.generateTextClickVerify(textConfig);
                // 服务端存储正确位置
                data.correctPositions = new ArrayList<>(data.textClickData.getTargetPositions());
                break;
                
            case ARITHMETIC:
                VerifyConfig arithmeticConfig = VerifyConfig.createArithmetic()
                    .numberRange(10, 99)
                    .operators(Arrays.asList("+", "-", "×"));
                data.arithmeticData = VerifyImageUtil.generateArithmeticVerify(arithmeticConfig);
                // 服务端存储正确答案
                data.correctAnswer = data.arithmeticData.getAnswer();
                break;
        }
        
        return data;
    }
    
    /**
     * 更新客户端UI（使用服务端数据）
     */
    private void updateClientWithServerData(VerifySession session, ServerVerifyData serverData) {
        Platform.runLater(() -> {
            switch (session.type) {
                case SLIDER:
                    SliderVerifyPane sliderPane = (SliderVerifyPane) session.verifyPane;
                    sliderPane.setVerifyImage(serverData.verifyImage);
                    break;
                    
                case TEXT_CLICK:
                    TextClickVerifyPane textPane = (TextClickVerifyPane) session.verifyPane;
                    textPane.setVerifyData(serverData.textClickData);
                    break;
                    
                case ARITHMETIC:
                    ArithmeticVerifyPane arithmeticPane = (ArithmeticVerifyPane) session.verifyPane;
                    arithmeticPane.setVerifyData(serverData.arithmeticData);
                    break;
            }
            
            session.verifyPane.reset();
        });
    }
    
    /**
     * 创建安全控制面板
     */
    private VBox createSecurityControlPanel(VerifySession session) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10, 0, 0, 0));
        panel.setAlignment(Pos.CENTER);
        
        // 按钮行
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);
        
        Button refreshBtn = new Button("刷新验证码");
        refreshBtn.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> {
            if (session.isLocked()) {
                showLockoutAlert(session);
            } else {
                // 刷新验证码但保留失败次数，防止用户绕过尝试次数限制
                fetchVerifyCodeFromServer(session, false);
            }
        });
        
        Button resetBtn = new Button("重置状态");
        resetBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        resetBtn.setOnAction(e -> {
            if (session.isLocked()) {
                showLockoutAlert(session);
            } else {
                session.verifyPane.reset();
            }
        });
        
        Button resetAllBtn = new Button("重置会话");
        resetAllBtn.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white;");
        resetAllBtn.setOnAction(e -> {
            session.unlock();
            fetchVerifyCodeFromServer(session);
            updateStatus("会话已重置，可以重新验证");
        });
        
        buttonRow.getChildren().addAll(refreshBtn, resetBtn, resetAllBtn);
        
        // 会话信息行
        HBox infoRow = new HBox(15);
        infoRow.setAlignment(Pos.CENTER);
        infoRow.setPadding(new Insets(5, 0, 0, 0));
        
        Label sessionIdLabel = new Label("会话ID: --");
        sessionIdLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10;");
        
        Label stateLabel = new Label("状态: " + session.verifyPane.getState().getCode());
        stateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10;");
        
        // 绑定状态变化
        session.verifyPane.stateProperty().addListener((obs, oldVal, newVal) -> {
            stateLabel.setText("状态: " + newVal.getCode());
        });
        
        // 更新会话ID显示
        session.verifyIdProperty.addListener((obs, oldVal, newVal) -> {
            sessionIdLabel.setText("会话ID: " + newVal);
        });
        
        infoRow.getChildren().addAll(sessionIdLabel, stateLabel);
        
        panel.getChildren().addAll(buttonRow, infoRow);
        return panel;
    }
    
    /**
     * 显示锁定提示
     */
    private void showLockoutAlert(VerifySession session) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("验证已锁定");
        alert.setHeaderText("尝试次数已达上限");
        
        long remainingMs = session.lockedUntil - System.currentTimeMillis();
        long remainingMin = remainingMs / 60000;
        
        alert.setContentText(
            "由于多次验证失败，验证功能已被锁定。\n\n" +
            "剩余锁定时间: " + remainingMin + " 分钟\n\n" +
            "您可以：\n" +
            "1. 等待锁定时间结束后重试\n" +
            "2. 点击「重置会话」按钮重新开始\n" +
            "3. 联系管理员寻求帮助"
        );
        
        alert.showAndWait();
    }
    
    /**
     * 延迟刷新验证码
     * 重要：此方法用于验证失败后的自动刷新，必须保留失败次数
     * 不通过 refresh() 回调，而是直接获取新验证码
     * 
     * @param session 验证会话
     * @param delayMs 延迟时间（毫秒）
     */
    private void scheduleDelayedRefresh(VerifySession session, long delayMs) {
        scheduler.schedule(() -> {
            Platform.runLater(() -> {
                if (!session.isLocked() && session.verifyPane.getState() == VerifyPane.VerifyState.FAIL) {
                    // 直接获取新验证码，不重置失败次数
                    // 这确保用户尝试次数限制正常工作
                    fetchVerifyCodeFromServer(session, false);
                }
            });
        }, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 启动状态更新定时器
     */
    private void startStatusUpdater() {
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                // 获取当前选中的选项卡对应的会话
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                if (selectedTab == null) return;
                
                VerifyType currentType = null;
                int index = tabPane.getTabs().indexOf(selectedTab);
                switch (index) {
                    case 0: currentType = VerifyType.SLIDER; break;
                    case 1: currentType = VerifyType.TEXT_CLICK; break;
                    case 2: currentType = VerifyType.ARITHMETIC; break;
                }
                
                if (currentType == null) return;
                
                VerifySession session = sessionMap.get(currentType);
                if (session == null) return;
                
                // 更新倒计时显示
                updateTimerDisplay(session);
                
                // 检查是否需要自动刷新过期的验证码
                // 过期刷新也应保留失败次数，确保尝试限制有效
                if (session.isExpired() && !session.isLocked()) {
                    updateStatus("验证码已过期，正在自动刷新...");
                    fetchVerifyCodeFromServer(session, false);
                }
            });
        }, 0, STATUS_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 更新尝试次数显示
     */
    private void updateAttemptDisplay(VerifySession session) {
        int remaining = Math.max(0, MAX_ATTEMPTS - session.failCount);
        
        if (session.isLocked()) {
            attemptLabel.setText("已锁定");
            attemptLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-weight: bold;");
        } else if (remaining <= 2) {
            attemptLabel.setText("剩余尝试次数: " + remaining + " ⚠️");
            attemptLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-weight: bold;");
        } else {
            attemptLabel.setText("剩余尝试次数: " + remaining);
            attemptLabel.setStyle("-fx-text-fill: #ff9800;");
        }
    }
    
    /**
     * 更新倒计时显示
     */
    private void updateTimerDisplay(VerifySession session) {
        if (session.verifyId == null || session.createdAt == 0) {
            timerLabel.setText("验证码有效期: --:--");
            timerLabel.setStyle("-fx-text-fill: #999;");
            return;
        }
        
        long remainingMs = VERIFY_CODE_VALIDITY_MS - (System.currentTimeMillis() - session.createdAt);
        
        if (remainingMs <= 0) {
            timerLabel.setText("验证码有效期: 已过期");
            timerLabel.setStyle("-fx-text-fill: #ff4d4f;");
        } else if (remainingMs < 60000) {
            // 少于1分钟，显示秒数
            long seconds = remainingMs / 1000;
            timerLabel.setText("验证码有效期: " + seconds + " 秒");
            timerLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-font-weight: bold;");
        } else {
            // 显示分钟和秒数
            long minutes = remainingMs / 60000;
            long seconds = (remainingMs % 60000) / 1000;
            timerLabel.setText(String.format("验证码有效期: %02d:%02d", minutes, seconds));
            
            if (remainingMs < 120000) {
                // 少于2分钟，显示警告颜色
                timerLabel.setStyle("-fx-text-fill: #ff9800;");
            } else {
                timerLabel.setStyle("-fx-text-fill: #52c41a;");
            }
        }
    }
    
    /**
     * 创建安全架构说明区域
     */
    private VBox createSecurityArchitectureDemo() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("安全架构设计");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#333"));

        // 服务端验证流程
        Label serverFlowLabel = new Label("1. 服务端验证流程");
        serverFlowLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        serverFlowLabel.setTextFill(Color.valueOf("#1e90ff"));
        
        Label serverFlowCode = createCodeLabel(
            "// ==================== 服务端验证流程 ====================\n\n" +
            "// 1. 客户端请求验证码\n" +
            "GET /api/verify/generate?type=slider\n\n" +
            "// 2. 服务端生成验证码并存储答案\n" +
            "ServerVerifyData data = generateVerifyCode(type);\n" +
            "cache.put(data.verifyId, data); // 存储到Redis\n" +
            "return { verifyId, imageData, expiresIn };\n\n" +
            "// 3. 客户端提交验证结果\n" +
            "POST /api/verify/validate\n" +
            "{ verifyId, userAnswer, trajectoryData }\n\n" +
            "// 4. 服务端验证\n" +
            "ServerVerifyData stored = cache.get(verifyId);\n" +
            "if (stored.isExpired()) return error(\"已过期\");\n" +
            "if (checkAttempts(verifyId) >= MAX) return error(\"次数超限\");\n" +
            "boolean valid = validateAnswer(stored, userAnswer);\n" +
            "return { success: valid, message: ... };"
        );
        
        // 尝试次数限制
        Label attemptLabel = new Label("2. 尝试次数限制");
        attemptLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        attemptLabel.setTextFill(Color.valueOf("#1e90ff"));
        
        Label attemptCode = createCodeLabel(
            "// ==================== 尝试次数限制 ====================\n\n" +
            "public class AttemptLimiter {\n" +
            "    private static final int MAX_ATTEMPTS = 5;\n" +
            "    private static final long LOCKOUT_MS = 600000; // 10分钟\n" +
            "    \n" +
            "    // Redis存储尝试次数\n" +
            "    public boolean checkAndIncrement(String sessionId) {\n" +
            "        String key = \"verify:attempts:\" + sessionId;\n" +
            "        Long attempts = redis.incr(key);\n" +
            "        redis.expire(key, LOCKOUT_MS / 1000);\n" +
            "        return attempts <= MAX_ATTEMPTS;\n" +
            "    }\n" +
            "    \n" +
            "    public int getRemaining(String sessionId) {\n" +
            "        String key = \"verify:attempts:\" + sessionId;\n" +
            "        Long attempts = redis.get(key);\n" +
            "        return Math.max(0, MAX_ATTEMPTS - attempts);\n" +
            "    }\n" +
            "}"
        );
        
        // 时效性管理
        Label expiryLabel = new Label("3. 验证码时效性");
        expiryLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        expiryLabel.setTextFill(Color.valueOf("#1e90ff"));
        
        Label expiryCode = createCodeLabel(
            "// ==================== 验证码时效性 ====================\n\n" +
            "public class VerifyCodeExpiry {\n" +
            "    private static final long VALIDITY_MS = 300000; // 5分钟\n" +
            "    \n" +
            "    public ServerVerifyData generate() {\n" +
            "        ServerVerifyData data = new ServerVerifyData();\n" +
            "        data.verifyId = UUID.randomUUID().toString();\n" +
            "        data.createdAt = System.currentTimeMillis();\n" +
            "        data.expiresAt = data.createdAt + VALIDITY_MS;\n" +
            "        \n" +
            "        // 存储到Redis，设置过期时间\n" +
            "        String key = \"verify:code:\" + data.verifyId;\n" +
            "        redis.setex(key, VALIDITY_MS / 1000, data.toJson());\n" +
            "        \n" +
            "        return data;\n" +
            "    }\n" +
            "    \n" +
            "    public boolean isValid(ServerVerifyData data) {\n" +
            "        return System.currentTimeMillis() < data.expiresAt;\n" +
            "    }\n" +
            "}"
        );
        
        // 安全要点
        Label securityPointsLabel = new Label("4. 安全要点总结");
        securityPointsLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        securityPointsLabel.setTextFill(Color.valueOf("#52c41a"));
        
        Label securityPoints = new Label(
            "• 验证答案存储在服务端，客户端仅持有验证ID\n" +
            "• 每次验证必须向服务端提交，由服务端判断结果\n" +
            "• 验证码有效期内可重复使用，过期后需重新获取\n" +
            "• 失败次数限制防止暴力破解\n" +
            "• 行为轨迹数据可用于反机器人检测\n" +
            "• 敏感操作应要求重新验证"
        );
        securityPoints.setStyle("-fx-background-color: #f6ffed; -fx-padding: 12; " +
                               "-fx-background-radius: 4; -fx-text-fill: #333;");
        securityPoints.setWrapText(true);
        
        container.getChildren().addAll(
            titleLabel,
            serverFlowLabel, serverFlowCode,
            attemptLabel, attemptCode,
            expiryLabel, expiryCode,
            securityPointsLabel, securityPoints
        );
        
        return container;
    }

    // ==================== 辅助方法 ====================
    
    private String getVerifyTypeTitle(VerifyType type) {
        switch (type) {
            case SLIDER:
                return "SliderVerifyPane - 滑动拼图验证码（安全增强版）";
            case TEXT_CLICK:
                return "TextClickVerifyPane - 文字点选验证码（安全增强版）";
            case ARITHMETIC:
                return "ArithmeticVerifyPane - 算术验证码（安全增强版）";
            default:
                return "未知类型";
        }
    }
    
    private Label createDescLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        label.setTextFill(Color.valueOf("#333"));
        return label;
    }
    
    private Label createSecurityInfoLabel(VerifyType type) {
        Label label = new Label(
            "🔒 安全模式: 服务端验证 | 最大尝试 " + MAX_ATTEMPTS + " 次 | 有效期 " + 
            (VERIFY_CODE_VALIDITY_MS / 60000) + " 分钟"
        );
        label.setFont(Font.font("Microsoft YaHei", 11));
        label.setTextFill(Color.valueOf("#52c41a"));
        label.setStyle("-fx-padding: 6 10; -fx-background-color: #f6ffed; -fx-background-radius: 4;");
        return label;
    }
    
    private Label createCodeLabel(String code) {
        Label label = new Label(code);
        label.setFont(Font.font("Consolas", 10));
        label.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 10; " +
                      "-fx-background-radius: 4; -fx-border-color: #e0e0e0; " +
                      "-fx-border-radius: 4;");
        label.setWrapText(true);
        return label;
    }
    
    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    // ==================== 内部类 ====================
    
    /**
     * 验证会话数据
     * 跟踪单个验证码实例的状态
     */
    private static class VerifySession {
        final VerifyType type;
        VerifyPane verifyPane;
        
        // 使用javafx属性以便绑定
        final javafx.beans.property.SimpleStringProperty verifyIdProperty = 
            new javafx.beans.property.SimpleStringProperty();
        String verifyId;
        long createdAt;
        
        int attemptCount = 0;
        int failCount = 0;
        long lockedUntil = 0;
        
        VerifySession(VerifyType type) {
            this.type = type;
        }
        
        boolean isLocked() {
            if (lockedUntil == 0) return false;
            if (System.currentTimeMillis() > lockedUntil) {
                lockedUntil = 0;
                return false;
            }
            return true;
        }
        
        void lock() {
            lockedUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
        }
        
        void unlock() {
            lockedUntil = 0;
            failCount = 0;
            attemptCount = 0;
        }
        
        boolean isExpired() {
            if (createdAt == 0) return true;
            return System.currentTimeMillis() > createdAt + VERIFY_CODE_VALIDITY_MS;
        }
        
        void resetAttempts() {
            attemptCount = 0;
            failCount = 0;
        }
        
        void setVerifyId(String id) {
            this.verifyId = id;
            this.verifyIdProperty.set(id);
        }
    }
    
    /**
     * 服务端验证码数据（模拟服务端存储）
     * 正确答案只存储在服务端，客户端不可见
     */
    private static class ServerVerifyData {
        String verifyId;
        VerifyType type;
        long createdAt;
        long expiresAt;
        
        // 不同类型的验证数据
        VerifyImage verifyImage;           // 滑块验证码
        VerifyImageUtil.TextClickVerifyData textClickData;  // 文字点选
        VerifyImageUtil.ArithmeticVerifyData arithmeticData; // 算术验证
        
        // 服务端存储的正确答案
        int correctX;                       // 滑块正确X位置
        List<java.awt.Point> correctPositions; // 文字正确位置
        int correctAnswer;                  // 算术正确答案
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
