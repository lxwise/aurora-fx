package io.aurora.fx.components.verifyCode;

import javafx.scene.layout.Pane;

import java.util.List;
import java.util.function.Consumer;

/**
 * 验证码工厂类
 * 提供统一的验证码组件创建入口
 * <p>
 * 使用示例：
 * <pre>
 * // 快速创建滑块验证码
 * SliderVerifyPane slider = VerifyCodeFactory.createSlider(backgroundImages);
 * slider.setOnVerifyComplete(result -> {
 *     if (result.isSuccess()) {
 *         System.out.println("验证成功！");
 *     }
 * });
 * 
 * // 使用自定义配置创建
 * VerifyConfig config = VerifyConfig.slider()
 *     .size(400, 250)
 *     .tolerance(10)
 *     .theme(VerifyTheme.BLUE);
 * SliderVerifyPane slider = VerifyCodeFactory.createSlider(config);
 * 
 * // 创建随机类型的验证码
 * Pane verifyPane = VerifyCodeFactory.createRandom(backgroundImages);
 * </pre>
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public final class VerifyCodeFactory {

    // 私有构造方法，防止实例化
    private VerifyCodeFactory() {
    }

    // ==================== 滑块验证码创建 ====================

    /**
     * 创建滑块验证码组件（使用默认配置）
     * @param backgroundImages 背景图片路径列表
     * @return 滑块验证码组件
     */
    public static SliderVerifyPane createSlider(List<String> backgroundImages) {
        VerifyConfig config = new VerifyConfig(VerifyType.SLIDER);
        config.setBackgroundImages(backgroundImages);
        return new SliderVerifyPane(config);
    }

    /**
     * 创建滑块验证码组件
     * @param config 验证码配置
     * @return 滑块验证码组件
     */
    public static SliderVerifyPane createSlider(VerifyConfig config) {
        if (config == null) {
            config = new VerifyConfig(VerifyType.SLIDER);
        }
        return new SliderVerifyPane(config);
    }

    /**
     * 创建滑块验证码组件并设置回调
     * @param config 验证码配置
     * @param onVerifyComplete 验证完成回调
     * @return 滑块验证码组件
     */
    public static SliderVerifyPane createSlider(VerifyConfig config, 
                                                 Consumer<VerifyResult> onVerifyComplete) {
        SliderVerifyPane pane = createSlider(config);
        pane.setOnVerifyComplete(onVerifyComplete);
        return pane;
    }

    /**
     * 创建滑块验证码组件（完整参数）
     * @param backgroundImages 背景图片路径列表
     * @param width 宽度
     * @param height 高度
     * @param tolerance 容差值
     * @param onVerifyComplete 验证完成回调
     * @return 滑块验证码组件
     */
    public static SliderVerifyPane createSlider(List<String> backgroundImages, 
                                                 int width, int height,
                                                 int tolerance,
                                                 Consumer<VerifyResult> onVerifyComplete) {
        VerifyConfig config = new VerifyConfig(VerifyType.SLIDER)
                .backgroundImages(backgroundImages)
                .size(width, height)
                .tolerance(tolerance);
        return createSlider(config, onVerifyComplete);
    }

    // ==================== 文字点选验证码创建 ====================

    /**
     * 创建文字点选验证码组件（使用默认配置）
     * @return 文字点选验证码组件
     */
    public static TextClickVerifyPane createTextClick() {
        return new TextClickVerifyPane(new VerifyConfig(VerifyType.TEXT_CLICK));
    }

    /**
     * 创建文字点选验证码组件
     * @param config 验证码配置
     * @return 文字点选验证码组件
     */
    public static TextClickVerifyPane createTextClick(VerifyConfig config) {
        if (config == null) {
            config = new VerifyConfig(VerifyType.TEXT_CLICK);
        }
        return new TextClickVerifyPane(config);
    }

    /**
     * 创建文字点选验证码组件并设置回调
     * @param config 验证码配置
     * @param onVerifyComplete 验证完成回调
     * @return 文字点选验证码组件
     */
    public static TextClickVerifyPane createTextClick(VerifyConfig config,
                                                       Consumer<VerifyResult> onVerifyComplete) {
        TextClickVerifyPane pane = createTextClick(config);
        pane.setOnVerifyComplete(onVerifyComplete);
        return pane;
    }

    // ==================== 算术验证码创建 ====================

    /**
     * 创建算术验证码组件（使用默认配置）
     * @return 算术验证码组件
     */
    public static ArithmeticVerifyPane createArithmetic() {
        return new ArithmeticVerifyPane(new VerifyConfig(VerifyType.ARITHMETIC));
    }

    /**
     * 创建算术验证码组件
     * @param config 验证码配置
     * @return 算术验证码组件
     */
    public static ArithmeticVerifyPane createArithmetic(VerifyConfig config) {
        if (config == null) {
            config = new VerifyConfig(VerifyType.ARITHMETIC);
        }
        return new ArithmeticVerifyPane(config);
    }

    /**
     * 创建算术验证码组件并设置回调
     * @param config 验证码配置
     * @param onVerifyComplete 验证完成回调
     * @return 算术验证码组件
     */
    public static ArithmeticVerifyPane createArithmetic(VerifyConfig config,
                                                         Consumer<VerifyResult> onVerifyComplete) {
        ArithmeticVerifyPane pane = createArithmetic(config);
        pane.setOnVerifyComplete(onVerifyComplete);
        return pane;
    }

    // ==================== 通用创建方法 ====================

    /**
     * 根据类型创建验证码组件
     * @param type 验证码类型
     * @param config 验证码配置
     * @return 验证码组件（作为Pane）
     */
    public static Pane create(VerifyType type, VerifyConfig config) {
        if (config == null) {
            config = new VerifyConfig(type);
        }

        switch (type) {
            case SLIDER:
                return createSlider(config);
            case TEXT_CLICK:
                return createTextClick(config);
            case ARITHMETIC:
                return createArithmetic(config);
            case MIXED:
                // 混合模式随机选择一种
                return createRandom(config);
            default:
                return createSlider(config);
        }
    }

    /**
     * 创建随机类型的验证码组件
     * @param config 验证码配置
     * @return 验证码组件
     */
    public static Pane createRandom(VerifyConfig config) {
        VerifyType[] types = {VerifyType.SLIDER, VerifyType.TEXT_CLICK, VerifyType.ARITHMETIC};
        int index = (int) (Math.random() * types.length);
        return create(types[index], config);
    }

    /**
     * 创建随机类型的验证码组件（使用默认配置）
     * @return 验证码组件
     */
    public static Pane createRandom() {
        return createRandom(null);
    }

    // ==================== 快速集成方法 ====================

    /**
     * 快速集成滑块验证码到现有容器
     * <p>
     * 使用示例：
     * <pre>
     * VBox container = new VBox();
     * VerifyCodeFactory.integrateSlider(container, backgroundImages, result -> {
     *     if (result.isSuccess()) {
     *         // 验证成功，继续业务流程
     *     }
     * });
     * </pre>
     * 
     * @param container 目标容器
     * @param backgroundImages 背景图片路径列表
     * @param onVerifyComplete 验证完成回调
     * @return 创建的验证码组件
     */
    public static SliderVerifyPane integrateSlider(Pane container, 
                                                    List<String> backgroundImages,
                                                    Consumer<VerifyResult> onVerifyComplete) {
        SliderVerifyPane pane = createSlider(backgroundImages);
        pane.setOnVerifyComplete(onVerifyComplete);
        
        // 生成并设置验证码
        try {
            VerifyImage verifyImage = VerifyImageUtil.generateSliderVerifyImage(
                    backgroundImages.get((int) (Math.random() * backgroundImages.size())),
                    pane.getConfig()
            );
            pane.setVerifyImage(verifyImage);
        } catch (Exception e) {
            throw VerifyException.imageGenerationError("生成验证码失败", e);
        }

        // 设置刷新回调
        pane.setOnRefresh(() -> {
            try {
                VerifyImage newImage = VerifyImageUtil.generateSliderVerifyImage(
                        backgroundImages.get((int) (Math.random() * backgroundImages.size())),
                        pane.getConfig()
                );
                pane.setVerifyImage(newImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        container.getChildren().add(pane);
        return pane;
    }

    /**
     * 快速集成文字点选验证码到现有容器
     * @param container 目标容器
     * @param onVerifyComplete 验证完成回调
     * @return 创建的验证码组件
     */
    public static TextClickVerifyPane integrateTextClick(Pane container,
                                                          Consumer<VerifyResult> onVerifyComplete) {
        TextClickVerifyPane pane = createTextClick();
        pane.setOnVerifyComplete(onVerifyComplete);

        // 生成验证码
        VerifyImageUtil.TextClickVerifyData data = VerifyImageUtil.generateTextClickVerify(pane.getConfig());
        pane.setVerifyData(data);

        // 设置刷新回调
        pane.setOnRefresh(() -> {
            VerifyImageUtil.TextClickVerifyData newData = 
                    VerifyImageUtil.generateTextClickVerify(pane.getConfig());
            pane.setVerifyData(newData);
        });

        container.getChildren().add(pane);
        return pane;
    }

    /**
     * 快速集成算术验证码到现有容器
     * @param container 目标容器
     * @param onVerifyComplete 验证完成回调
     * @return 创建的验证码组件
     */
    public static ArithmeticVerifyPane integrateArithmetic(Pane container,
                                                            Consumer<VerifyResult> onVerifyComplete) {
        ArithmeticVerifyPane pane = createArithmetic();
        pane.setOnVerifyComplete(onVerifyComplete);

        // 生成验证码
        VerifyImageUtil.ArithmeticVerifyData data = 
                VerifyImageUtil.generateArithmeticVerify(pane.getConfig());
        pane.setVerifyData(data);

        // 设置刷新回调
        pane.setOnRefresh(() -> {
            VerifyImageUtil.ArithmeticVerifyData newData = 
                    VerifyImageUtil.generateArithmeticVerify(pane.getConfig());
            pane.setVerifyData(newData);
        });

        container.getChildren().add(pane);
        return pane;
    }
}
