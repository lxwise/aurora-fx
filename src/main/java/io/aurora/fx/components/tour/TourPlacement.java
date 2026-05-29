package io.aurora.fx.components.tour;

/**
 * Tour 引导弹窗相对于目标的 12 个定位方向
 * <p>
 * 对标 Element Plus Tour 组件 placement 取值：
 * top / top-start / top-end / bottom / bottom-start / bottom-end /
 * left / left-start / left-end / right / right-start / right-end，
 * 另外提供 {@link #CENTER} 用于目标为空时居中显示。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public enum TourPlacement {

    /** 顶部居中 */
    TOP("top"),
    /** 顶部左对齐（top-start） */
    TOP_START("top-start"),
    /** 顶部右对齐（top-end） */
    TOP_END("top-end"),

    /** 底部居中 */
    BOTTOM("bottom"),
    /** 底部左对齐（bottom-start） */
    BOTTOM_START("bottom-start"),
    /** 底部右对齐（bottom-end） */
    BOTTOM_END("bottom-end"),

    /** 左侧居中 */
    LEFT("left"),
    /** 左侧上对齐（left-start） */
    LEFT_START("left-start"),
    /** 左侧下对齐（left-end） */
    LEFT_END("left-end"),

    /** 右侧居中 */
    RIGHT("right"),
    /** 右侧上对齐（right-start） */
    RIGHT_START("right-start"),
    /** 右侧下对齐（right-end） */
    RIGHT_END("right-end"),

    /** 屏幕中央（target 为空时使用） */
    CENTER("center");

    private final String value;

    TourPlacement(String value) {
        this.value = value;
    }

    /**
     * 获取字符串值
     *
     * @return 字符串值
     */
    public String getValue() {
        return value;
    }

    /**
     * 是否为顶部方向
     *
     * @return true 表示弹窗在目标上方
     */
    public boolean isTop() {
        return this == TOP || this == TOP_START || this == TOP_END;
    }

    /**
     * 是否为底部方向
     *
     * @return true 表示弹窗在目标下方
     */
    public boolean isBottom() {
        return this == BOTTOM || this == BOTTOM_START || this == BOTTOM_END;
    }

    /**
     * 是否为左侧方向
     *
     * @return true 表示弹窗在目标左方
     */
    public boolean isLeft() {
        return this == LEFT || this == LEFT_START || this == LEFT_END;
    }

    /**
     * 是否为右侧方向
     *
     * @return true 表示弹窗在目标右方
     */
    public boolean isRight() {
        return this == RIGHT || this == RIGHT_START || this == RIGHT_END;
    }

    /**
     * 是否为水平方向（上/下）
     *
     * @return true 表示水平定位
     */
    public boolean isHorizontalAxis() {
        return isTop() || isBottom();
    }

    /**
     * 是否为垂直方向（左/右）
     *
     * @return true 表示垂直定位
     */
    public boolean isVerticalAxis() {
        return isLeft() || isRight();
    }

    /**
     * 根据字符串解析枚举
     *
     * @param value 字符串
     * @return 对应枚举，未匹配返回 {@link #BOTTOM}
     */
    public static TourPlacement fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return BOTTOM;
        }
        for (TourPlacement p : values()) {
            if (p.value.equalsIgnoreCase(value)) {
                return p;
            }
        }
        return BOTTOM;
    }
}
