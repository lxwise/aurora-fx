package io.aurora.fx.components.upload;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 手动上传组件 — 选择文件后不自动上传，需手动触发。
 * <p>
 * 开箱即用的手动上传组件，默认配置：
 * <ul>
 *   <li>autoUpload=false：选择文件后保持 READY 状态</li>
 *   <li>自动显示"上传到服务器"按钮</li>
 *   <li>支持多选</li>
 *   <li>最多 3 个文件</li>
 *   <li>TEXT 列表模式</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * ManualUploadNode node = new ManualUploadNode();
 * node.setAction("http://localhost:8080/upload");
 * root.getChildren().add(node);
 *
 * // 也可以通过代码手动触发上传
 * node.submit();
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class ManualUploadNode extends VBox {

    private final FileUploader uploader;

    public ManualUploadNode() {
        this("");
    }

    public ManualUploadNode(String action) {
        uploader = new FileUploader();
        uploader.setAutoUpload(false);
        uploader.setMultiple(true);
        uploader.setLimit(3);
        uploader.setButtonText("选择文件");
        uploader.setTip("最多 3 个文件，点击'上传到服务器'开始上传");
        uploader.setListType(ListType.TEXT);
        if (action != null && !action.isEmpty()) {
            uploader.setAction(action);
        }
        getChildren().add(uploader);
    }

    public FileUploader getUploader() { return uploader; }

    /** 手动触发上传 */
    public void submit() { uploader.submit(); }

    /** 清空文件列表 */
    public void clearFiles() { uploader.clearFiles(); }

    // ===== 常用属性代理 =====

    public void setAction(String action) { uploader.setAction(action); }
    public String getAction() { return uploader.getAction(); }
    public StringProperty actionProperty() { return uploader.actionProperty(); }

    public void setAccept(String accept) { uploader.setAccept(accept); }
    public void setMultiple(boolean v) { uploader.setMultiple(v); }
    public void setLimit(int limit) { uploader.setLimit(limit); }
    public void setButtonText(String text) { uploader.setButtonText(text); }
    public void setTip(String tip) { uploader.setTip(tip); }
    public void setListType(ListType type) { uploader.setListType(type); }

    // ===== 回调代理 =====

    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { uploader.setOnSuccess(fn); }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { uploader.setOnError(fn); }
    public void setOnProgress(BiConsumer<UploadFile, Double> fn) { uploader.setOnProgress(fn); }
    public void setOnExceed(BiConsumer<List<File>, ObservableList<UploadFile>> fn) { uploader.setOnExceed(fn); }
    public void setOnRemove(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnRemove(fn); }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnChange(fn); }
    public void setBeforeUpload(Function<UploadFile, Boolean> fn) { uploader.setBeforeUpload(fn); }

    public void dispose() { uploader.dispose(); }
}
