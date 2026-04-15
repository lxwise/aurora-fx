package io.aurora.fx.components.upload;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 拖拽上传组件 — 拖拽文件到指定区域或点击选择文件上传。
 * <p>
 * 开箱即用的拖拽上传组件，默认配置：
 * <ul>
 *   <li>启用拖拽模式（显示拖拽区域而非按钮）</li>
 *   <li>支持多选</li>
 *   <li>接受所有图片类型</li>
 *   <li>TEXT 列表模式展示已上传文件</li>
 *   <li>拖拽悬浮时区域自动高亮</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * DragUploadNode node = new DragUploadNode();
 * node.setAction("http://localhost:8080/upload");
 * node.setDragText("拖入文件即可上传");
 * root.getChildren().add(node);
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class DragUploadNode extends VBox {

    private final FileUploader uploader;

    public DragUploadNode() {
        this("");
    }

    public DragUploadNode(String action) {
        uploader = new FileUploader();
        uploader.setDrag(true);
        uploader.setMultiple(true);
        uploader.setAccept("image/*");
        uploader.setDragText("将文件拖到此处，或点击上传");
        uploader.setTip("仅支持图片格式文件");
        uploader.setListType(ListType.TEXT);
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
    public void setDragText(String text) { uploader.setDragText(text); }
    public void setTip(String tip) { uploader.setTip(tip); }
    public void setListType(ListType type) { uploader.setListType(type); }
    public void setLimit(int limit) { uploader.setLimit(limit); }

    // ===== 回调代理 =====

    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { uploader.setOnSuccess(fn); }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { uploader.setOnError(fn); }
    public void setOnRemove(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnRemove(fn); }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnChange(fn); }
    public void setBeforeUpload(Function<UploadFile, Boolean> fn) { uploader.setBeforeUpload(fn); }

    public void dispose() { uploader.dispose(); }
}
