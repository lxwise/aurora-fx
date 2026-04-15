package io.aurora.fx.components.upload;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 文件夹上传组件 — 选择文件夹，递归扫描并上传所有文件。
 * <p>
 * 开箱即用的文件夹上传组件，默认配置：
 * <ul>
 *   <li>启用文件夹选择器（directory=true）</li>
 *   <li>支持多选</li>
 *   <li>TEXT 列表模式</li>
 *   <li>递归扁平化文件夹内所有文件</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * FolderUploadNode node = new FolderUploadNode();
 * node.setAction("http://localhost:8080/upload");
 * node.setOnChange((file, list) -> System.out.println("共 " + list.size() + " 个文件"));
 * root.getChildren().add(node);
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class FolderUploadNode extends VBox {

    private final FileUploader uploader;

    public FolderUploadNode() {
        this("");
    }

    public FolderUploadNode(String action) {
        uploader = new FileUploader();
        uploader.setDirectory(true);
        uploader.setMultiple(true);
        uploader.setButtonText("选择文件夹");
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
    public void setButtonText(String text) { uploader.setButtonText(text); }
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
