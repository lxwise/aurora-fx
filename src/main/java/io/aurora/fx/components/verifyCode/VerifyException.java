package io.aurora.fx.components.verifyCode;

/**
 * 验证码异常类
 * 用于封装验证码组件中发生的各种异常
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public class VerifyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 创建验证码异常
     * @param message 异常消息
     */
    public VerifyException(String message) {
        super(message);
        this.errorCode = "UNKNOWN_ERROR";
    }

    /**
     * 创建验证码异常
     * @param message 异常消息
     * @param errorCode 错误代码
     */
    public VerifyException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 创建验证码异常
     * @param message 异常消息
     * @param cause 原因异常
     */
    public VerifyException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN_ERROR";
    }

    /**
     * 创建验证码异常
     * @param message 异常消息
     * @param errorCode 错误代码
     * @param cause 原因异常
     */
    public VerifyException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误代码
     * @return 错误代码字符串
     */
    public String getErrorCode() {
        return errorCode;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建配置错误异常
     * @param message 异常消息
     * @return VerifyException实例
     */
    public static VerifyException configError(String message) {
        return new VerifyException(message, "CONFIG_ERROR");
    }

    /**
     * 创建图片生成错误异常
     * @param message 异常消息
     * @param cause 原因异常
     * @return VerifyException实例
     */
    public static VerifyException imageGenerationError(String message, Throwable cause) {
        return new VerifyException(message, "IMAGE_GENERATION_ERROR", cause);
    }

    /**
     * 创建验证失败异常
     * @param message 异常消息
     * @return VerifyException实例
     */
    public static VerifyException verifyFailed(String message) {
        return new VerifyException(message, "VERIFY_FAILED");
    }

    /**
     * 创建机器人检测异常
     * @return VerifyException实例
     */
    public static VerifyException robotDetected() {
        return new VerifyException("检测到机器人行为", "ROBOT_DETECTED");
    }

    /**
     * 创建超时异常
     * @return VerifyException实例
     */
    public static VerifyException timeout() {
        return new VerifyException("验证超时", "TIMEOUT");
    }

    /**
     * 创建状态错误异常
     * @param message 异常消息
     * @return VerifyException实例
     */
    public static VerifyException invalidState(String message) {
        return new VerifyException(message, "INVALID_STATE");
    }

    @Override
    public String toString() {
        return "VerifyException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
