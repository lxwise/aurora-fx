package io.aurora.fx.components.upload;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 图片列表上传组件 — PICTURE 缩略图列表模式。
 * <p>
 * 开箱即用的图片列表组件，默认配置：
 * <ul>
 *   <li>PICTURE 缩略图列表模式</li>
 *   <li>左侧缩略图 + 右侧文件名/大小</li>
 *   <li>支持多选</li>
 *   <li>仅接受图片文件</li>
 *   <li>悬浮显示预览/删除按钮</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * PictureListUploadNode node = new PictureListUploadNode();
 * node.setAction("http://localhost:8080/upload");
 * root.getChildren().add(node);
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class PictureListUploadNode extends VBox {

    private final FileUploader uploader;

    public PictureListUploadNode() {
        this("");
    }

    public PictureListUploadNode(String action) {
        uploader = new FileUploader();
        uploader.setListType(ListType.PICTURE);
        uploader.setMultiple(true);
        uploader.setAccept("image/*");
        uploader.setButtonText("点击上传");
        uploader.setTip("仅支持 jpg/png 格式，单个文件不超过 500KB");
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
    public void setButtonText(String text) { uploader.setButtonText(text); }
    public void setTip(String tip) { uploader.setTip(tip); }
    public void setLimit(int limit) { uploader.setLimit(limit); }

    // ===== 回调代理 =====

    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { uploader.setOnSuccess(fn); }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { uploader.setOnError(fn); }
    public void setOnPreview(Consumer<UploadFile> fn) { uploader.setOnPreview(fn); }
    public void setOnRemove(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnRemove(fn); }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnChange(fn); }
    public void setBeforeUpload(Function<UploadFile, Boolean> fn) { uploader.setBeforeUpload(fn); }
    public void setBeforeRemove(Function<UploadFile, Boolean> fn) { uploader.setBeforeRemove(fn); }

    public void dispose() { uploader.dispose(); }
}
