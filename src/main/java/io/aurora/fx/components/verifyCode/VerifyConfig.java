package io.aurora.fx.components.verifyCode;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 验证码配置类
 * 提供验证码生成的各种可配置参数
 * <p>
 * 使用示例：
 * <pre>
 * // 使用Builder模式
 * VerifyConfig config = VerifyConfig.slider()
 *     .size(400, 250)
 *     .tolerance(10)
 *     .difficulty(2)
 *     .theme(VerifyTheme.BLUE)
 *     .build();
 * 
 * // 快速创建
 * VerifyConfig config = VerifyConfig.createDefault(VerifyType.SLIDER);
 * </pre>
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public class VerifyConfig implements Cloneable {

    // ==================== 通用配置 ====================
    /**
     * 验证码类型
     */
    private VerifyType verifyType = VerifyType.SLIDER;
    
    /**
     * 难度级别 (1-简单, 2-中等, 3-困难)
     */
    private int difficulty = 1;
    
    /**
     * 验证容差值（像素）
     */
    private int tolerance = 8;
    
    /**
     * 是否启用行为轨迹检测
     */
    private boolean enableBehaviorTracking = true;
    
    /**
     * 背景图片路径列表
     */
    private List<String> backgroundImages;

    // ==================== 滑块验证码配置 ====================
    /**
     * 背景图宽度
     */
    private int srcWidth = 350;
    
    /**
     * 背景图高度
     */
    private int srcHeight = 200;
    
    /**
     * 滑块宽度
     */
    private int sliderWidth = 50;
    
    /**
     * 滑块高度
     */
    private int sliderHeight = 50;
    
    /**
     * 滑块凸起圆半径
     */
    private int circleRadius = 5;
    
    /**
     * 滑块内边距
     */
    private int rectanglePadding = 8;

    // ==================== 文字点选验证码配置 ====================
    /**
     * 需要点击的文字数量
     */
    private int clickTextCount = 3;
    
    /**
     * 干扰文字数量
     */
    private int interferenceTextCount = 5;
    
    /**
     * 文字大小范围 [min, max]
     */
    private int[] fontSizeRange = {16, 24};
    
    /**
     * 文字颜色
     */
    private Color textColor = Color.BLACK;
    
    /**
     * 提示文字列表
     */
    private List<String> textPool = Arrays.asList(
            "春", "夏", "秋", "冬", "风", "花", "雪", "月",
            "山", "水", "云", "天", "地", "人", "和", "美",
            "爱", "心", "梦", "想", "希", "望", "信", "念"
    );

    // ==================== 算术验证码配置 ====================
    /**
     * 算术运算符
     */
    private List<String> operators = Arrays.asList("+", "-", "×");
    
    /**
     * 数字范围 [min, max]
     */
    private int[] numberRange = {1, 50};
    
    /**
     * 是否允许负数结果
     */
    private boolean allowNegativeResult = false;

    // ==================== 主题和国际化配置 ====================

    /**
     * 主题配置
     */
    private VerifyTheme theme = VerifyTheme.DEFAULT;

    /**
     * 语言区域
     */
    private Locale locale = Locale.getDefault();

    /**
     * 自定义提示文本
     */
    private VerifyMessages messages = new VerifyMessages();

    // ==================== 缓存配置 ====================

    /**
     * 是否启用图片缓存
     */
    private boolean enableCache = true;

    /**
     * 缓存最大数量
     */
    private int maxCacheSize = 10;

    // ==================== 构造方法 ====================

    public VerifyConfig() {
    }

    public VerifyConfig(VerifyType verifyType) {
        this.verifyType = verifyType;
        // 根据验证码类型设置默认容差值
        switch (verifyType) {
            case TEXT_CLICK:
                // 文字点选需要更大的容差（字体大小+视觉误差）
                this.tolerance = 15;
                break;
            case SLIDER:
                // 滑块验证码容差适中
                this.tolerance = 8;
                break;
            case ARITHMETIC:
                // 算术验证码不需要位置容差
                this.tolerance = 0;
                break;
            default:
                this.tolerance = 10;
        }
    }

    // ==================== 静态工厂方法 ====================
    
    public static VerifyConfig slider() {
        return new VerifyConfig(VerifyType.SLIDER);
    }
    
    public static VerifyConfig textClick() {
        return new VerifyConfig(VerifyType.TEXT_CLICK);
    }
    
    public static VerifyConfig arithmetic() {
        return new VerifyConfig(VerifyType.ARITHMETIC);
    }
    
    public static VerifyConfig mixed() {
        return new VerifyConfig(VerifyType.MIXED);
    }

    // ==================== Builder模式 ====================
    
    public VerifyConfig verifyType(VerifyType verifyType) {
        this.verifyType = verifyType;
        return this;
    }
    
    public VerifyConfig difficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(3, difficulty));
        return this;
    }
    
    public VerifyConfig tolerance(int tolerance) {
        this.tolerance = tolerance;
        return this;
    }
    
    public VerifyConfig enableBehaviorTracking(boolean enable) {
        this.enableBehaviorTracking = enable;
        return this;
    }
    
    public VerifyConfig backgroundImages(List<String> paths) {
        this.backgroundImages = paths;
        return this;
    }
    
    public VerifyConfig size(int width, int height) {
        this.srcWidth = width;
        this.srcHeight = height;
        return this;
    }
    
    public VerifyConfig sliderSize(int width, int height) {
        this.sliderWidth = width;
        this.sliderHeight = height;
        return this;
    }
    
    public VerifyConfig clickTextCount(int count) {
        this.clickTextCount = count;
        return this;
    }
    
    public VerifyConfig interferenceTextCount(int count) {
        this.interferenceTextCount = count;
        return this;
    }
    
    public VerifyConfig textPool(List<String> pool) {
        this.textPool = pool;
        return this;
    }
    
    public VerifyConfig numberRange(int min, int max) {
        this.numberRange = new int[]{min, max};
        return this;
    }
    
    public VerifyConfig operators(List<String> operators) {
        this.operators = operators;
        return this;
    }

    // ==================== 随机获取背景图 ====================
    
    public String getRandomBackgroundImage() {
        if (backgroundImages == null || backgroundImages.isEmpty()) {
            return null;
        }
        return backgroundImages.get(new Random().nextInt(backgroundImages.size()));
    }

    // ==================== 根据难度调整参数 ====================
    
    public void applyDifficulty() {
        switch (difficulty) {
            case 1: // 简单
                if (verifyType == VerifyType.TEXT_CLICK) {
                    this.tolerance = 20; // 文字点选简单模式：大容差
                } else {
                    this.tolerance = 10;
                }
                this.clickTextCount = 2;
                this.interferenceTextCount = 3;
                this.numberRange = new int[]{1, 20};
                break;
            case 2: // 中等
                if (verifyType == VerifyType.TEXT_CLICK) {
                    this.tolerance = 15; // 文字点选中等模式：中等容差
                } else {
                    this.tolerance = 8;
                }
                this.clickTextCount = 3;
                this.interferenceTextCount = 5;
                this.numberRange = new int[]{1, 50};
                break;
            case 3: // 困难
                if (verifyType == VerifyType.TEXT_CLICK) {
                    this.tolerance = 10; // 文字点选困难模式：小容差但仍可接受
                } else {
                    this.tolerance = 5;
                }
                this.clickTextCount = 4;
                this.interferenceTextCount = 8;
                this.numberRange = new int[]{1, 100};
                break;
        }
    }

    // ==================== Getter方法 ====================

    public VerifyType getVerifyType() {
        return verifyType;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getTolerance() {
        return tolerance;
    }

    public boolean isEnableBehaviorTracking() {
        return enableBehaviorTracking;
    }

    public List<String> getBackgroundImages() {
        return backgroundImages;
    }

    public int getSrcWidth() {
        return srcWidth;
    }

    public int getSrcHeight() {
        return srcHeight;
    }

    public int getSliderWidth() {
        return sliderWidth;
    }

    public int getSliderHeight() {
        return sliderHeight;
    }

    public int getCircleRadius() {
        return circleRadius;
    }

    public int getRectanglePadding() {
        return rectanglePadding;
    }

    public int getClickTextCount() {
        return clickTextCount;
    }

    public int getInterferenceTextCount() {
        return interferenceTextCount;
    }

    public int[] getFontSizeRange() {
        return fontSizeRange;
    }

    public Color getTextColor() {
        return textColor;
    }

    public List<String> getTextPool() {
        return textPool;
    }

    public List<String> getOperators() {
        return operators;
    }

    public int[] getNumberRange() {
        return numberRange;
    }

    public boolean isAllowNegativeResult() {
        return allowNegativeResult;
    }

    // ==================== Setter方法 ====================

    public void setVerifyType(VerifyType verifyType) {
        this.verifyType = verifyType;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public void setEnableBehaviorTracking(boolean enableBehaviorTracking) {
        this.enableBehaviorTracking = enableBehaviorTracking;
    }

    public void setBackgroundImages(List<String> backgroundImages) {
        this.backgroundImages = backgroundImages;
    }

    public void setSrcWidth(int srcWidth) {
        this.srcWidth = srcWidth;
    }

    public void setSrcHeight(int srcHeight) {
        this.srcHeight = srcHeight;
    }

    public void setSliderWidth(int sliderWidth) {
        this.sliderWidth = sliderWidth;
    }

    public void setSliderHeight(int sliderHeight) {
        this.sliderHeight = sliderHeight;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    public void setRectanglePadding(int rectanglePadding) {
        this.rectanglePadding = rectanglePadding;
    }

    public void setClickTextCount(int clickTextCount) {
        this.clickTextCount = clickTextCount;
    }

    public void setInterferenceTextCount(int interferenceTextCount) {
        this.interferenceTextCount = interferenceTextCount;
    }

    public void setFontSizeRange(int[] fontSizeRange) {
        this.fontSizeRange = fontSizeRange;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setTextPool(List<String> textPool) {
        this.textPool = textPool;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public void setNumberRange(int[] numberRange) {
        this.numberRange = numberRange;
    }

    public void setAllowNegativeResult(boolean allowNegativeResult) {
        this.allowNegativeResult = allowNegativeResult;
    }

    // ==================== 新增Getter/Setter ====================

    public VerifyTheme getTheme() {
        return theme;
    }

    public void setTheme(VerifyTheme theme) {
        this.theme = theme;
    }

    public VerifyConfig theme(VerifyTheme theme) {
        this.theme = theme;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        this.messages = new VerifyMessages(locale);
    }

    public VerifyConfig locale(Locale locale) {
        setLocale(locale);
        return this;
    }

    public VerifyMessages getMessages() {
        return messages;
    }

    public void setMessages(VerifyMessages messages) {
        this.messages = messages;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public VerifyConfig enableCache(boolean enable) {
        this.enableCache = enable;
        return this;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public VerifyConfig maxCacheSize(int size) {
        this.maxCacheSize = size;
        return this;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建默认配置
     * @param type 验证码类型
     * @return 配置对象
     */
    public static VerifyConfig createDefault(VerifyType type) {
        return new VerifyConfig(type);
    }

    /**
     * 创建滑块验证码配置
     * @return 配置对象
     */
    public static VerifyConfig createSlider() {
        return new VerifyConfig(VerifyType.SLIDER);
    }

    /**
     * 创建文字点选验证码配置
     * @return 配置对象
     */
    public static VerifyConfig createTextClick() {
        return new VerifyConfig(VerifyType.TEXT_CLICK);
    }

    /**
     * 创建算术验证码配置
     * @return 配置对象
     */
    public static VerifyConfig createArithmetic() {
        return new VerifyConfig(VerifyType.ARITHMETIC);
    }

    /**
     * 创建混合验证码配置
     * @return 配置对象
     */
    public static VerifyConfig createMixed() {
        return new VerifyConfig(VerifyType.MIXED);
    }

    // ==================== Clone方法 ====================

    /**
     * 克隆配置对象
     * @return 配置对象副本
     */
    @Override
    public VerifyConfig clone() {
        try {
            VerifyConfig cloned = (VerifyConfig) super.clone();
            if (this.backgroundImages != null) {
                cloned.backgroundImages = new java.util.ArrayList<>(this.backgroundImages);
            }
            if (this.textPool != null) {
                cloned.textPool = new java.util.ArrayList<>(this.textPool);
            }
            if (this.operators != null) {
                cloned.operators = new java.util.ArrayList<>(this.operators);
            }
            if (this.fontSizeRange != null) {
                cloned.fontSizeRange = this.fontSizeRange.clone();
            }
            if (this.numberRange != null) {
                cloned.numberRange = this.numberRange.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new VerifyException("克隆配置失败", e);
        }
    }

    @Override
    public String toString() {
        return "VerifyConfig{" +
                "verifyType=" + verifyType +
                ", difficulty=" + difficulty +
                ", tolerance=" + tolerance +
                ", srcWidth=" + srcWidth +
                ", srcHeight=" + srcHeight +
                ", enableBehaviorTracking=" + enableBehaviorTracking +
                '}';
    }
}
