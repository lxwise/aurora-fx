package io.aurora.fx.components.verifyCode;

/**
 * 验证结果类
 * @author JavaFX Team
 */
public class VerifyResult {
    
    /**
     * 验证是否成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 验证耗时（毫秒）
     */
    private long duration;
    
    /**
     * 行为轨迹数据
     */
    private TrajectoryData trajectoryData;
    
    /**
     * 验证码类型
     */
    private VerifyType verifyType;
    
    /**
     * 错误代码（验证失败时）
     */
    private String errorCode;

    public VerifyResult() {
    }

    public VerifyResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // ==================== 静态工厂方法 ====================
    
    public static VerifyResult success() {
        return new VerifyResult(true, "验证成功");
    }
    
    public static VerifyResult success(String message) {
        return new VerifyResult(true, message);
    }
    
    public static VerifyResult fail(String message) {
        return new VerifyResult(false, message);
    }
    
    public static VerifyResult fail(String message, String errorCode) {
        VerifyResult result = new VerifyResult(false, message);
        result.setErrorCode(errorCode);
        return result;
    }

    // ==================== Getter/Setter ====================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public TrajectoryData getTrajectoryData() {
        return trajectoryData;
    }

    public void setTrajectoryData(TrajectoryData trajectoryData) {
        this.trajectoryData = trajectoryData;
    }

    public VerifyType getVerifyType() {
        return verifyType;
    }

    public void setVerifyType(VerifyType verifyType) {
        this.verifyType = verifyType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "VerifyResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", duration=" + duration + "ms" +
                ", verifyType=" + verifyType +
                '}';
    }
}
