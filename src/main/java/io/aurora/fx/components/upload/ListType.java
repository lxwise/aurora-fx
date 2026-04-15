package io.aurora.fx.components.upload;

/**
 * 文件列表展示类型枚举，对标 Element UI upload 的 list-type 属性
 *
 * @author lstar
 * @since 2025
 */
public enum ListType {

    /**
     * 文本列表：以文件名 + 状态图标的形式展示
     */
    TEXT,

    /**
     * 图片列表：在文本列表左侧增加缩略图
     */
    PICTURE,

    /**
     * 照片墙：以卡片网格形式展示图片缩略图
     */
    PICTURE_CARD;
}
