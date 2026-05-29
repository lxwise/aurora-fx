package io.aurora.fx.components.tour;

/**
 * Tour 引导弹窗类型
 * <p>
 * 对标 Element Plus 的 type 属性，DEFAULT 为常规白色样式，
 * PRIMARY 为主色填充样式（建议在非模态场景下使用）。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public enum TourType {

    /** 默认风格 - 白底深色文字 */
    DEFAULT,

    /** 主色风格 - 主色填充，白色文字（推荐与 mask=false 搭配使用） */
    PRIMARY
}
