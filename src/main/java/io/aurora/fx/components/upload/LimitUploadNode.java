package io.aurora.fx.components.upload;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 数量限制上传组件 — 限制最大上传文件数量。
 * <p>
 * 开箱即用的限制上传组件，默认配置：
 * <ul>
 *   <li>最多上传 3 个文件</li>
 *   <li>支持多选</li>
 *   <li>仅接受图片文件</li>
 *   <li>TEXT 列表模式</li>
 *   <li>超出限制时弹窗提示</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * LimitUploadNode node = new LimitUploadNode();
 * node.setLimit(5);
 * node.setAction("http://localhost:8080/upload");
 * root.getChildren().add(node);
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class LimitUploadNode extends VBox {

    private final FileUploader uploader;

    public LimitUploadNode() {
        this(3);
    }

    /**
     * @param limit 最大文件数量
     */
    public LimitUploadNode(int limit) {
        this(limit, "");
    }

    public LimitUploadNode(int limit, String action) {
        uploader = new FileUploader();
        uploader.setMultiple(true);
        uploader.setLimit(limit);
        uploader.setAccept("image/*");
        uploader.setButtonText("点击上传");
        uploader.setTip("最多上传 " + limit + " 个文件");
        uploader.setListType(ListType.TEXT);
        if (action != null && !action.isEmpty()) {
            uploader.setAction(action);
        }

        // 默认超出限制提示
        uploader.setOnExceed((files, fileList) -> {
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("超出限制");
                alert.setHeaderText(null);
                alert.setContentText(String.format(
                        "最多上传 %d 个文件，本次选择了 %d 个，当前已有 %d 个。",
                        uploader.getLimit(), files.size(), fileList.size()));
                alert.showAndWait();
            });
        });

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
    public void setLimit(int limit) {
        uploader.setLimit(limit);
        uploader.setTip("最多上传 " + limit + " 个文件");
    }
    public void setButtonText(String text) { uploader.setButtonText(text); }
    public void setTip(String tip) { uploader.setTip(tip); }
    public void setListType(ListType type) { uploader.setListType(type); }

    // ===== 回调代理 =====

    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { uploader.setOnSuccess(fn); }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { uploader.setOnError(fn); }
    public void setOnExceed(BiConsumer<List<File>, ObservableList<UploadFile>> fn) { uploader.setOnExceed(fn); }
    public void setOnRemove(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnRemove(fn); }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnChange(fn); }
    public void setBeforeUpload(Function<UploadFile, Boolean> fn) { uploader.setBeforeUpload(fn); }
    public void setBeforeRemove(Function<UploadFile, Boolean> fn) { uploader.setBeforeRemove(fn); }

    public void dispose() { uploader.dispose(); }
}
