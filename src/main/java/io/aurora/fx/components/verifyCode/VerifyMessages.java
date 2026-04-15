package io.aurora.fx.components.verifyCode;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 验证码消息配置类
 * 支持国际化提示文本
 * <p>
 * 使用示例：
 * <pre>
 * VerifyMessages messages = new VerifyMessages()
 *     .sliderHint("滑动滑块完成验证")
 *     .successMessage("验证成功！")
 *     .failMessage("验证失败，请重试");
 * 
 * // 或使用国际化
 * VerifyMessages messages = new VerifyMessages(Locale.ENGLISH);
 * </pre>
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public class VerifyMessages {

    // ==================== 消息键常量 ====================

    /**
     * 滑块提示消息键
     */
    public static final String KEY_SLIDER_HINT = "slider.hint";

    /**
     * 文字点选提示消息键
     */
    public static final String KEY_TEXT_CLICK_HINT = "textClick.hint";

    /**
     * 算术验证提示消息键
     */
    public static final String KEY_ARITHMETIC_HINT = "arithmetic.hint";

    /**
     * 成功消息键
     */
    public static final String KEY_SUCCESS = "result.success";

    /**
     * 失败消息键
     */
    public static final String KEY_FAIL = "result.fail";

    /**
     * 机器人检测消息键
     */
    public static final String KEY_ROBOT_DETECTED = "result.robotDetected";

    /**
     * 刷新按钮文本键
     */
    public static final String KEY_REFRESH = "button.refresh";

    /**
     * 验证中消息键
     */
    public static final String KEY_VERIFYING = "result.verifying";

    /**
     * 加载中消息键
     */
    public static final String KEY_LOADING = "result.loading";

    // ==================== 默认消息 ====================

    private String sliderHint = "向右滑动完成验证";
    private String textClickHint = "请依次点击图中文字";
    private String arithmeticHint = "请输入计算结果";
    private String successMessage = "验证成功";
    private String failMessage = "验证失败，请重试";
    private String robotDetectedMessage = "验证失败，请手动操作";
    private String positionMismatchMessage = "位置不正确，请重试";
    private String refreshButtonText = "刷新";
    private String verifyingMessage = "验证中...";
    private String loadingMessage = "加载中...";

    // ==================== 构造方法 ====================

    /**
     * 创建默认消息配置（中文）
     */
    public VerifyMessages() {
    }

    /**
     * 创建指定语言的消息配置
     * @param locale 语言区域
     */
    public VerifyMessages(Locale locale) {
        loadMessages(locale);
    }

    // ==================== 消息加载 ====================

    /**
     * 从资源文件加载消息
     * @param locale 语言区域
     */
    private void loadMessages(Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    "com.javafx.test.VerifyImage.messages", locale);

            if (bundle.containsKey(KEY_SLIDER_HINT)) {
                sliderHint = bundle.getString(KEY_SLIDER_HINT);
            }
            if (bundle.containsKey(KEY_TEXT_CLICK_HINT)) {
                textClickHint = bundle.getString(KEY_TEXT_CLICK_HINT);
            }
            if (bundle.containsKey(KEY_ARITHMETIC_HINT)) {
                arithmeticHint = bundle.getString(KEY_ARITHMETIC_HINT);
            }
            if (bundle.containsKey(KEY_SUCCESS)) {
                successMessage = bundle.getString(KEY_SUCCESS);
            }
            if (bundle.containsKey(KEY_FAIL)) {
                failMessage = bundle.getString(KEY_FAIL);
            }
            if (bundle.containsKey(KEY_ROBOT_DETECTED)) {
                robotDetectedMessage = bundle.getString(KEY_ROBOT_DETECTED);
            }
            if (bundle.containsKey(KEY_REFRESH)) {
                refreshButtonText = bundle.getString(KEY_REFRESH);
            }
            if (bundle.containsKey(KEY_VERIFYING)) {
                verifyingMessage = bundle.getString(KEY_VERIFYING);
            }
            if (bundle.containsKey(KEY_LOADING)) {
                loadingMessage = bundle.getString(KEY_LOADING);
            }
        } catch (Exception e) {
            // 使用默认中文消息
        }
    }

    // ==================== Builder模式 ====================

    /**
     * 设置滑块提示
     * @param hint 提示文本
     * @return this
     */
    public VerifyMessages sliderHint(String hint) {
        this.sliderHint = hint;
        return this;
    }

    /**
     * 设置文字点选提示
     * @param hint 提示文本
     * @return this
     */
    public VerifyMessages textClickHint(String hint) {
        this.textClickHint = hint;
        return this;
    }

    /**
     * 设置算术验证提示
     * @param hint 提示文本
     * @return this
     */
    public VerifyMessages arithmeticHint(String hint) {
        this.arithmeticHint = hint;
        return this;
    }

    /**
     * 设置成功消息
     * @param message 消息文本
     * @return this
     */
    public VerifyMessages successMessage(String message) {
        this.successMessage = message;
        return this;
    }

    /**
     * 设置失败消息
     * @param message 消息文本
     * @return this
     */
    public VerifyMessages failMessage(String message) {
        this.failMessage = message;
        return this;
    }

    /**
     * 设置机器人检测消息
     * @param message 消息文本
     * @return this
     */
    public VerifyMessages robotDetectedMessage(String message) {
        this.robotDetectedMessage = message;
        return this;
    }

    /**
     * 设置位置不匹配消息
     * @param message 消息文本
     * @return this
     */
    public VerifyMessages positionMismatchMessage(String message) {
        this.positionMismatchMessage = message;
        return this;
    }

    /**
     * 设置刷新按钮文本
     * @param text 文本
     * @return this
     */
    public VerifyMessages refreshButtonText(String text) {
        this.refreshButtonText = text;
        return this;
    }

    /**
     * 设置验证中消息
     * @param message 消息文本
     * @return this
     */
    public VerifyMessages verifyingMessage(String message) {
        this.verifyingMessage = message;
        return this;
    }

    /**
     * 设置加载中消息
     * @param message 消息文本
     * @return this
     */
    public VerifyMessages loadingMessage(String message) {
        this.loadingMessage = message;
        return this;
    }

    // ==================== Getter方法 ====================

    public String getSliderHint() {
        return sliderHint;
    }

    public String getTextClickHint() {
        return textClickHint;
    }

    public String getArithmeticHint() {
        return arithmeticHint;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public String getRobotDetectedMessage() {
        return robotDetectedMessage;
    }

    public String getPositionMismatchMessage() {
        return positionMismatchMessage;
    }

    public String getRefreshButtonText() {
        return refreshButtonText;
    }

    public String getVerifyingMessage() {
        return verifyingMessage;
    }

    public String getLoadingMessage() {
        return loadingMessage;
    }

    // ==================== Setter方法 ====================

    public void setSliderHint(String sliderHint) {
        this.sliderHint = sliderHint;
    }

    public void setTextClickHint(String textClickHint) {
        this.textClickHint = textClickHint;
    }

    public void setArithmeticHint(String arithmeticHint) {
        this.arithmeticHint = arithmeticHint;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public void setRobotDetectedMessage(String robotDetectedMessage) {
        this.robotDetectedMessage = robotDetectedMessage;
    }

    public void setPositionMismatchMessage(String positionMismatchMessage) {
        this.positionMismatchMessage = positionMismatchMessage;
    }

    public void setRefreshButtonText(String refreshButtonText) {
        this.refreshButtonText = refreshButtonText;
    }

    public void setVerifyingMessage(String verifyingMessage) {
        this.verifyingMessage = verifyingMessage;
    }

    public void setLoadingMessage(String loadingMessage) {
        this.loadingMessage = loadingMessage;
    }
}
