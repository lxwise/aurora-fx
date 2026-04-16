package io.aurora.fx.components.steps;

/**
 * 步骤状态枚举
 * <p>对标 Element UI Steps 组件的 status 属性，定义步骤在不同阶段的显示状态。</p>
 *
 * @author Steps Component
 * @version 1.0
 */
public enum StepStatus {

    /**
     * 等待状态 - 步骤未开始
     */
    WAIT("wait"),

    /**
     * 进行中状态 - 当前正在处理的步骤
     */
    PROCESS("process"),

    /**
     * 完成状态 - 步骤已完成（默认完成样式）
     */
    FINISH("finish"),

    /**
     * 成功状态 - 步骤已成功完成
     */
    SUCCESS("success"),

    /**
     * 错误状态 - 步骤处理失败
     */
    ERROR("error");

    private final String value;

    StepStatus(String value) {
        this.value = value;
    }

    /**
     * 获取状态值
     *
     * @return 状态字符串值
     */
    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值解析状态枚举
     *
     * @param value 状态字符串
     * @return 对应的 StepStatus 枚举，未匹配时返回 WAIT
     */
    public static StepStatus fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return WAIT;
        }
        for (StepStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return WAIT;
    }
}
