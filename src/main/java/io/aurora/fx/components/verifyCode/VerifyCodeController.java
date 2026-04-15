package io.aurora.fx.components.verifyCode;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * 验证码控制器
 * 提供统一的验证码管理接口
 * @author JavaFX Team
 */
public class VerifyCodeController {

    private final VerifyConfig config;
    private Pane currentPane;
    private VerifyType currentType;
    
    // 回调
    private Consumer<VerifyResult> onVerifyComplete;
    
    /**
     * 创建验证码控制器
     */
    public VerifyCodeController() {
        this(new VerifyConfig());
    }

    /**
     * 创建验证码控制器
     * @param config 验证码配置
     */
    public VerifyCodeController(VerifyConfig config) {
        this.config = config;
        this.config.applyDifficulty();
    }

    /**
     * 创建指定类型的验证码面板
     * @param type 验证码类型
     * @return 验证码面板
     */
    public Pane createVerifyPane(VerifyType type) {
        this.currentType = type;
        
        switch (type) {
            case SLIDER:
                return createSliderPane();
            case TEXT_CLICK:
                return createTextClickPane();
            case ARITHMETIC:
                return createArithmeticPane();
            case MIXED:
                return createRandomPane();
            default:
                return createSliderPane();
        }
    }

    /**
     * 创建滑块验证码面板
     */
    public SliderVerifyPane createSliderPane() {
        SliderVerifyPane pane = new SliderVerifyPane(config);
        currentType = VerifyType.SLIDER;
        currentPane = pane;
        
        // 设置刷新回调
        pane.setOnRefresh(() -> {
            try {
                refreshSliderPane(pane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        // 设置验证完成回调
        pane.setOnVerifyComplete(result -> {
            if (onVerifyComplete != null) {
                onVerifyComplete.accept(result);
            }
        });
        
        return pane;
    }

    /**
     * 创建文字点选验证码面板
     */
    public TextClickVerifyPane createTextClickPane() {
        TextClickVerifyPane pane = new TextClickVerifyPane(config);
        currentType = VerifyType.TEXT_CLICK;
        currentPane = pane;
        
        pane.setOnRefresh(() -> {
            try {
                refreshTextClickPane(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        pane.setOnVerifyComplete(result -> {
            if (onVerifyComplete != null) {
                onVerifyComplete.accept(result);
            }
        });
        
        return pane;
    }

    /**
     * 创建算术验证码面板
     */
    public ArithmeticVerifyPane createArithmeticPane() {
        ArithmeticVerifyPane pane = new ArithmeticVerifyPane(config);
        currentType = VerifyType.ARITHMETIC;
        currentPane = pane;
        
        pane.setOnRefresh(() -> {
            try {
                refreshArithmeticPane(pane);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        pane.setOnVerifyComplete(result -> {
            if (onVerifyComplete != null) {
                onVerifyComplete.accept(result);
            }
        });
        
        return pane;
    }

    /**
     * 创建随机类型的验证码面板
     */
    public Pane createRandomPane() {
        VerifyType[] types = {VerifyType.SLIDER, VerifyType.TEXT_CLICK, VerifyType.ARITHMETIC};
        VerifyType randomType = types[ThreadLocalRandom.current().nextInt(types.length)];
        return createVerifyPane(randomType);
    }

    /**
     * 刷新滑块验证码
     */
    public void refreshSliderPane(SliderVerifyPane pane) throws IOException {
        String imagePath = config.getRandomBackgroundImage();
        if (imagePath != null) {
            VerifyImage verifyImage = VerifyImageUtil.generateSliderVerifyImage(imagePath, config);
            pane.setVerifyImage(verifyImage);
        }
    }

    /**
     * 刷新文字点选验证码
     */
    public void refreshTextClickPane(TextClickVerifyPane pane) {
        VerifyImageUtil.TextClickVerifyData data = VerifyImageUtil.generateTextClickVerify(config);
        pane.setVerifyData(data);
    }

    /**
     * 刷新算术验证码
     */
    public void refreshArithmeticPane(ArithmeticVerifyPane pane) {
        VerifyImageUtil.ArithmeticVerifyData data = VerifyImageUtil.generateArithmeticVerify(config);
        pane.setVerifyData(data);
    }

    /**
     * 刷新当前验证码
     */
    public void refresh() throws IOException {
        if (currentPane == null) {
            return;
        }
        
        switch (currentType) {
            case SLIDER:
                refreshSliderPane((SliderVerifyPane) currentPane);
                break;
            case TEXT_CLICK:
                refreshTextClickPane((TextClickVerifyPane) currentPane);
                break;
            case ARITHMETIC:
                refreshArithmeticPane((ArithmeticVerifyPane) currentPane);
                break;
            default:
                break;
        }
    }

    /**
     * 重置当前验证码
     */
    public void reset() {
        if (currentPane == null) {
            return;
        }
        
        if (currentPane instanceof SliderVerifyPane) {
            ((SliderVerifyPane) currentPane).reset();
        } else if (currentPane instanceof TextClickVerifyPane) {
            ((TextClickVerifyPane) currentPane).reset();
        } else if (currentPane instanceof ArithmeticVerifyPane) {
            ((ArithmeticVerifyPane) currentPane).reset();
        }
    }

    /**
     * 创建一个完整的验证码对话框面板（包含类型选择）
     */
    public Pane createFullVerifyPanel() {
        StackPane container = new StackPane();
        container.setPrefSize(config.getSrcWidth() + 40, config.getSrcHeight() + 100);
        
        // 创建初始面板
        Pane verifyPane = createVerifyPane(config.getVerifyType());
        container.getChildren().add(verifyPane);
        
        return container;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 快速创建滑块验证码
     * @param backgroundImages 背景图片路径列表
     * @return 验证码控制器
     */
    public static VerifyCodeController createSlider(List<String> backgroundImages) {
        VerifyConfig config = VerifyConfig.slider()
                .backgroundImages(backgroundImages)
                .verifyType(VerifyType.SLIDER);
        return new VerifyCodeController(config);
    }

    /**
     * 快速创建文字点选验证码
     * @return 验证码控制器
     */
    public static VerifyCodeController createTextClick() {
        VerifyConfig config = VerifyConfig.textClick();
        return new VerifyCodeController(config);
    }

    /**
     * 快速创建算术验证码
     * @return 验证码控制器
     */
    public static VerifyCodeController createArithmetic() {
        VerifyConfig config = VerifyConfig.arithmetic();
        return new VerifyCodeController(config);
    }

    /**
     * 快速创建混合验证码
     * @param backgroundImages 背景图片路径列表（用于滑块验证码）
     * @return 验证码控制器
     */
    public static VerifyCodeController createMixed(List<String> backgroundImages) {
        VerifyConfig config = VerifyConfig.mixed()
                .backgroundImages(backgroundImages)
                .verifyType(VerifyType.MIXED);
        return new VerifyCodeController(config);
    }

    // ==================== Getter/Setter ====================

    public VerifyConfig getConfig() {
        return config;
    }

    public Pane getCurrentPane() {
        return currentPane;
    }

    public VerifyType getCurrentType() {
        return currentType;
    }

    public void setOnVerifyComplete(Consumer<VerifyResult> callback) {
        this.onVerifyComplete = callback;
    }
}
