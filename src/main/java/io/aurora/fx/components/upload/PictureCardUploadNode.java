package io.aurora.fx.components.upload;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 照片墙上传组件 — PICTURE_CARD 卡片模式。
 * <p>
 * 开箱即用的照片墙组件，默认配置：
 * <ul>
 *   <li>PICTURE_CARD 照片墙模式</li>
 *   <li>支持多选</li>
 *   <li>仅接受图片文件</li>
 *   <li>最多上传 8 张</li>
 *   <li>卡片尺寸 148×148 px</li>
 *   <li>悬浮显示预览/删除按钮</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * PictureCardUploadNode node = new PictureCardUploadNode();
 * node.setAction("http://localhost:8080/upload");
 * node.setLimit(6);
 * root.getChildren().add(node);
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class PictureCardUploadNode extends VBox {

    private final FileUploader uploader;

    public PictureCardUploadNode() {
        this("");
    }

    public PictureCardUploadNode(String action) {
        uploader = new FileUploader();
        uploader.setListType(ListType.PICTURE_CARD);
        uploader.setMultiple(true);
        uploader.setAccept("image/*");
        uploader.setLimit(8);
        uploader.setThumbnailSize(148);
        if (action != null && !action.isEmpty()) {
            uploader.setAction(action);
        }
        getChildren().add(uploader);
    }

    public FileUploader getUploader() { return uploader; }

    /** 清空文件列表 */
    public void clearFiles() { uploader.clearFiles(); }

    // ===== 常用属性代理 =====

    public void setAction(String action) { uploader.setAction(action); }
    public String getAction() { return uploader.getAction(); }
    public StringProperty actionProperty() { return uploader.actionProperty(); }

    public void setAccept(String accept) { uploader.setAccept(accept); }
    public void setMultiple(boolean v) { uploader.setMultiple(v); }
    public void setLimit(int limit) { uploader.setLimit(limit); }
    public void setThumbnailSize(double size) { uploader.setThumbnailSize(size); }
    public void setTip(String tip) { uploader.setTip(tip); }

    // ===== 回调代理 =====

    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { uploader.setOnSuccess(fn); }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { uploader.setOnError(fn); }
    public void setOnExceed(BiConsumer<List<File>, ObservableList<UploadFile>> fn) { uploader.setOnExceed(fn); }
    public void setOnPreview(Consumer<UploadFile> fn) { uploader.setOnPreview(fn); }
    public void setOnRemove(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnRemove(fn); }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnChange(fn); }
    public void setBeforeUpload(Function<UploadFile, Boolean> fn) { uploader.setBeforeUpload(fn); }
    public void setBeforeRemove(Function<UploadFile, Boolean> fn) { uploader.setBeforeRemove(fn); }

    public void dispose() { uploader.dispose(); }
}
