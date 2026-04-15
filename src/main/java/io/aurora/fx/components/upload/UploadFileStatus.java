package io.aurora.fx.components.upload;

/**
 * 上传文件的状态枚举
 *
 * @author lstar
 * @since 2025
 */
public enum UploadFileStatus {

    /**
     * 就绪状态，文件已选中但尚未开始上传
     */
    READY,

    /**
     * 上传中
     */
    UPLOADING,

    /**
     * 上传成功
     */
    SUCCESS,

    /**
     * 上传失败
     */
    FAIL;
}
