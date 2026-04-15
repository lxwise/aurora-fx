package io.aurora.fx.components.upload;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 基础上传组件 — 点击按钮选择文件上传。
 * <p>
 * 开箱即用的文件上传组件，默认配置：
 * <ul>
 *   <li>TEXT 列表模式展示已选文件</li>
 *   <li>支持多选</li>
 *   <li>接受所有图片类型</li>
 *   <li>无 action 时自动进入本地模式（文件选择后直接标记成功）</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 最简用法 — 零配置
 * BasicUploadNode node = new BasicUploadNode();
 * root.getChildren().add(node);
 *
 * // 设置上传地址
 * BasicUploadNode node = new BasicUploadNode();
 * node.setAction("http://localhost:8080/upload");
 * node.setOnSuccess((file, resp) -> System.out.println("成功: " + file.getName()));
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class BasicUploadNode extends VBox {

    private final FileUploader uploader;

    public BasicUploadNode() {
        this("");
    }

    /**
     * @param action 上传接口地址（为空时进入本地模式）
     */
    public BasicUploadNode(String action) {
        uploader = new FileUploader();
        uploader.setMultiple(true);
        uploader.setAccept("image/*");
        uploader.setTip("仅支持 jpg/png 格式，单个文件不超过 500KB");
        uploader.setButtonText("点击上传");
        uploader.setListType(ListType.TEXT);
        if (action != null && !action.isEmpty()) {
            uploader.setAction(action);
        }
        getChildren().add(uploader);
    }

    /** 获取内部 FileUploader 实例（用于高级配置） */
    public FileUploader getUploader() { return uploader; }

    /** 清空文件列表 */
    public void clearFiles() { uploader.clearFiles(); }

    // ===== 常用属性代理 =====

    public void setAction(String action) { uploader.setAction(action); }
    public String getAction() { return uploader.getAction(); }
    public StringProperty actionProperty() { return uploader.actionProperty(); }

    public void setAccept(String accept) { uploader.setAccept(accept); }
    public void setMultiple(boolean v) { uploader.setMultiple(v); }
    public void setTip(String tip) { uploader.setTip(tip); }
    public void setButtonText(String text) { uploader.setButtonText(text); }
    public void setListType(ListType type) { uploader.setListType(type); }
    public void setLimit(int limit) { uploader.setLimit(limit); }

    // ===== 回调代理 =====

    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { uploader.setOnSuccess(fn); }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { uploader.setOnError(fn); }
    public void setOnRemove(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnRemove(fn); }
    public void setOnProgress(BiConsumer<UploadFile, Double> fn) { uploader.setOnProgress(fn); }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { uploader.setOnChange(fn); }
    public void setBeforeUpload(Function<UploadFile, Boolean> fn) { uploader.setBeforeUpload(fn); }
    public void setBeforeRemove(Function<UploadFile, Boolean> fn) { uploader.setBeforeRemove(fn); }

    /** 释放资源 */
    public void dispose() { uploader.dispose(); }
}
