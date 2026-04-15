package io.aurora.fx.components.upload;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 头像上传组件 — 单文件上传 + 格式/大小校验 + 图像裁切。
 * <p>
 * 开箱即用的头像上传组件，默认配置：
 * <ul>
 *   <li>PICTURE_CARD 照片墙模式展示头像</li>
 *   <li>限制 1 个文件</li>
 *   <li>仅接受 JPG/PNG 格式</li>
 *   <li>文件大小上限 2MB</li>
 *   <li>选择图片后自动弹出裁切对话框</li>
 *   <li>超出限制时自动替换旧头像并重新裁切</li>
 *   <li>集成 ImageCropPane 裁切（默认 1:1、200×200 输出）</li>
 *   <li>卡片尺寸 178×178 px</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 默认裁切
 * AvatarUploadNode node = new AvatarUploadNode();
 * node.setAction("http://localhost:8080/upload");
 * root.getChildren().add(node);
 *
 * // 自定义裁切参数
 * AvatarUploadNode node = new AvatarUploadNode();
 * node.setCropAspectRatio(16.0 / 9.0);
 * node.setCropOutputWidth(400);
 * node.setCropOutputHeight(225);
 *
 * // 禁用裁切
 * node.setCropEnabled(false);
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class AvatarUploadNode extends VBox {

    private final FileUploader uploader;

    /** 裁切宽高比，默认 1.0（正方形） */
    private double cropAspectRatio = 1.0;
    /** 裁切输出宽度，默认 200 px */
    private int cropOutputWidth = 200;
    /** 裁切输出高度，默认 200 px */
    private int cropOutputHeight = 200;
    /** 文件大小上限（字节），默认 2MB */
    private long maxFileSize = 2L * 1024 * 1024;
    /** 是否启用裁切功能 */
    private boolean cropEnabled = true;

    /**
     * 内部标志：当裁切完成后添加裁切文件时，跳过 beforeUpload 中的裁切拦截，
     * 避免裁切后的文件再次触发裁切对话框导致无限递归。
     */
    private boolean skipCropIntercept = false;

    public AvatarUploadNode() {
        this("");
    }

    public AvatarUploadNode(String action) {
        uploader = new FileUploader();
        uploader.setListType(ListType.PICTURE_CARD);
        uploader.setAccept(".jpg,.jpeg,.png");
        uploader.setLimit(1);
        uploader.setShowFileList(true);
        uploader.setThumbnailSize(178);
        uploader.setTip("仅支持 JPG/PNG 格式，大小不超过 2MB");
        if (action != null && !action.isEmpty()) {
            uploader.setAction(action);
        }

        // ========== beforeUpload 钩子：格式校验 + 大小校验 + 裁切拦截 ==========
        // 关键设计：
        //   1. 首次选择文件 → beforeUpload 拦截 → 弹出裁切对话框（showAndWait 阻塞）
        //   2. 裁切完成 → 设置 skipCropIntercept=true → handleStart(croppedFile)
        //   3. handleStart → addFile → beforeUpload 再次调用，但此时跳过裁切
        //   4. 裁切后的文件正常添加和上传
        uploader.setBeforeUpload(file -> {
            // 格式校验
            String ext = file.getExtension();
            if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
                showWarning("格式错误", "头像图片必须是 JPG 或 PNG 格式！");
                return false;
            }
            // 大小校验
            if (file.getSize() > maxFileSize) {
                showWarning("大小超限", "头像图片大小不能超过 " + UploadUtils.formatFileSize(maxFileSize) + "！");
                return false;
            }

            // 裁切拦截：如果启用裁切且非裁切后的文件
            if (cropEnabled && !skipCropIntercept) {
                File rawFile = file.getRawFile();
                if (rawFile != null && rawFile.exists()) {
                    // showCropDialog 使用 showAndWait() 阻塞 FX 线程直到用户完成裁切或取消
                    final File[] croppedResult = {null};
                    ImageCropPane.showCropDialog(
                            rawFile,
                            cropAspectRatio,
                            cropOutputWidth,
                            cropOutputHeight,
                            croppedFile -> croppedResult[0] = croppedFile
                    );

                    if (croppedResult[0] != null) {
                        // 裁切成功：清除已有头像，用裁切后的文件替换
                        uploader.clearFiles();
                        skipCropIntercept = true;
                        try {
                            uploader.handleStart(croppedResult[0]);
                        } finally {
                            skipCropIntercept = false;
                        }
                    }
                    // 无论裁切成功还是取消，都阻止原始文件添加
                    return false;
                }
            }
            return true;
        });

        // ========== onExceed 钩子：超出限制时替换旧头像 ==========
        // 当已有头像时用户再次选择文件，触发 onExceed
        uploader.setOnExceed((files, fileList) -> {
            if (files == null || files.isEmpty()) return;

            File rawFile = files.get(0);
            // 前置校验（因为 onExceed 绕过了 beforeUpload 的校验流程）
            String ext = UploadUtils.getExtension(rawFile.getName());
            if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
                showWarning("格式错误", "头像图片必须是 JPG 或 PNG 格式！");
                return;
            }
            if (rawFile.length() > maxFileSize) {
                showWarning("大小超限", "头像图片大小不能超过 " + UploadUtils.formatFileSize(maxFileSize) + "！");
                return;
            }

            if (cropEnabled) {
                final File[] croppedResult = {null};
                ImageCropPane.showCropDialog(
                        rawFile,
                        cropAspectRatio,
                        cropOutputWidth,
                        cropOutputHeight,
                        croppedFile -> croppedResult[0] = croppedFile
                );
                if (croppedResult[0] != null) {
                    uploader.clearFiles();
                    skipCropIntercept = true;
                    try {
                        uploader.handleStart(croppedResult[0]);
                    } finally {
                        skipCropIntercept = false;
                    }
                }
            } else {
                uploader.clearFiles();
                uploader.handleStart(rawFile);
            }
        });

        getChildren().add(uploader);
    }

    public FileUploader getUploader() { return uploader; }

    /** 清空头像 */
    public void clearFiles() { uploader.clearFiles(); }

    // ===== 裁切配置 =====

    public double getCropAspectRatio() { return cropAspectRatio; }
    public void setCropAspectRatio(double ratio) { this.cropAspectRatio = ratio; }

    public int getCropOutputWidth() { return cropOutputWidth; }
    public void setCropOutputWidth(int width) { this.cropOutputWidth = width; }

    public int getCropOutputHeight() { return cropOutputHeight; }
    public void setCropOutputHeight(int height) { this.cropOutputHeight = height; }

    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long bytes) { this.maxFileSize = bytes; }

    public boolean isCropEnabled() { return cropEnabled; }
    public void setCropEnabled(boolean enabled) { this.cropEnabled = enabled; }

    // ===== 常用属性代理 =====

    public void setAction(String action) { uploader.setAction(action); }
    public String getAction() { return uploader.getAction(); }
    public StringProperty actionProperty() { return uploader.actionProperty(); }

    public void setAccept(String accept) { uploader.setAccept(accept); }
    public void setTip(String tip) { uploader.setTip(tip); }
    public void setThumbnailSize(double size) { uploader.setThumbnailSize(size); }

    // ===== 回调代理 =====

    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { uploader.setOnSuccess(fn); }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { uploader.setOnError(fn); }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnChange(fn); }

    public void dispose() { uploader.dispose(); }

    private void showWarning(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
