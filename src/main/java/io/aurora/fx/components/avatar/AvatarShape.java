package io.aurora.fx.components.avatar;

/**
 * 头像形状枚举
 * <p>
 * 定义头像组件支持的裁剪形状类型。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public enum AvatarShape {

    /** 圆形 */
    CIRCLE,

    /** 正方形（支持圆角） */
    SQUARE,

    /** 水平六边形 */
    HEXAGON_H,

    /** 垂直六边形 */
    HEXAGON_V,

    /** 菱形 */
    DIAMOND,

    /** 五边形 */
    PENTAGON,

    /** 五角星 */
    STAR,

    /** 圆角方形（自动圆角） */
    ROUNDED_SQUARE
}
