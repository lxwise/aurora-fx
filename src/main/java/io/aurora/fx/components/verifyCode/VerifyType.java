package io.aurora.fx.components.verifyCode;

/**
 * 验证码类型枚举
 * @author JavaFX Team
 */
public enum VerifyType {
    /**
     * 滑动拼图验证码
     */
    SLIDER("滑动拼图验证", "slider"),
    
    /**
     * 文字点选验证码
     */
    TEXT_CLICK("文字点选验证", "text_click"),
    
    /**
     * 算术验证码
     */
    ARITHMETIC("算术验证码", "arithmetic"),
    
    /**
     * 混合验证码（随机选择类型）
     */
    MIXED("混合验证", "mixed");

    private final String displayName;
    private final String code;

    VerifyType(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }
}
