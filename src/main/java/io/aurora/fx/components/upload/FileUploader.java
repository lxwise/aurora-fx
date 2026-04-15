package io.aurora.fx.components.upload;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JavaFX 文件上传组件，对标 Element UI 的 Upload 组件。
 * <p>
 * 提供点击上传、拖拽上传、文件夹上传等多种上传方式，
 * 支持文件数量限制、类型过滤、上传前验证、删除前确认等完整功能。
 * 支持三种文件列表展示模式：文本列表、图片列表、照片墙。
 * </p>
 *
 * <h3>基础用法：</h3>
 * <pre>{@code
 * FileUploader uploader = new FileUploader();
 * uploader.setAction("https://example.com/api/upload");
 * uploader.setListType(ListType.PICTURE_CARD);
 * uploader.setLimit(5);
 * uploader.setAccept("image/*");
 * uploader.setOnSuccess((file, response) -> System.out.println("上传成功: " + file.getName()));
 * }</pre>
 *
 * <h3>手动上传模式：</h3>
 * <pre>{@code
 * FileUploader uploader = new FileUploader();
 * uploader.setAutoUpload(false);
 * // ... 用户选择文件后
 * uploader.submit(); // 手动触发上传
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class FileUploader extends Control {

    private static final String DEFAULT_STYLE_CLASS = "file-uploader";

    /** 上传线程池（守护线程） */
    private final ExecutorService uploadExecutor;

    /** 活跃的上传任务映射（uid -> Thread），用于取消上传。使用 ConcurrentHashMap 确保线程安全。 */
    private final Map<String, Thread> activeUploads = new ConcurrentHashMap<>();

    public FileUploader() {
        this(3);
    }

    /**
     * 构造函数
     *
     * @param maxConcurrentUploads 最大并发上传数
     */
    public FileUploader(int maxConcurrentUploads) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.uploadExecutor = Executors.newFixedThreadPool(
                Math.max(1, maxConcurrentUploads),
                r -> {
                    Thread t = new Thread(r, "aurora-fx-upload-" + System.nanoTime());
                    t.setDaemon(true);
                    return t;
                }
        );
    }

    // ==========================================================================
    //  Properties - 对标 Element UI Upload 的属性
    // ==========================================================================

    // ---- action: 上传地址 ----
    private final StringProperty action = new SimpleStringProperty("");

    public String getAction() { return action.get(); }
    public StringProperty actionProperty() { return action; }
    public void setAction(String action) { this.action.set(action); }

    // ---- headers: 请求头 ----
    private final ObjectProperty<Map<String, String>> headers = new SimpleObjectProperty<>(Map.of());

    public Map<String, String> getHeaders() { return headers.get(); }
    public ObjectProperty<Map<String, String>> headersProperty() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers.set(headers); }

    // ---- multiple: 是否支持多选 ----
    private final BooleanProperty multiple = new SimpleBooleanProperty(false);

    public boolean isMultiple() { return multiple.get(); }
    public BooleanProperty multipleProperty() { return multiple; }
    public void setMultiple(boolean multiple) { this.multiple.set(multiple); }

    // ---- data: 附加的请求参数 ----
    private final ObjectProperty<Map<String, String>> data = new SimpleObjectProperty<>(Map.of());

    public Map<String, String> getData() { return data.get(); }
    public ObjectProperty<Map<String, String>> dataProperty() { return data; }
    public void setData(Map<String, String> data) { this.data.set(data); }

    // ---- name: 上传文件字段名 ----
    private final StringProperty name = new SimpleStringProperty("file");

    public String getFieldName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setFieldName(String name) { this.name.set(name); }

    // ---- withCredentials: 是否携带 Cookie ----
    private final BooleanProperty withCredentials = new SimpleBooleanProperty(false);

    public boolean isWithCredentials() { return withCredentials.get(); }
    public BooleanProperty withCredentialsProperty() { return withCredentials; }
    public void setWithCredentials(boolean withCredentials) { this.withCredentials.set(withCredentials); }

    // ---- showFileList: 是否显示文件列表 ----
    private final BooleanProperty showFileList = new SimpleBooleanProperty(true);

    public boolean isShowFileList() { return showFileList.get(); }
    public BooleanProperty showFileListProperty() { return showFileList; }
    public void setShowFileList(boolean showFileList) { this.showFileList.set(showFileList); }

    // ---- drag: 是否启用拖拽上传 ----
    private final BooleanProperty drag = new SimpleBooleanProperty(false);

    public boolean isDrag() { return drag.get(); }
    public BooleanProperty dragProperty() { return drag; }
    public void setDrag(boolean drag) { this.drag.set(drag); }

    // ---- accept: 接受的文件类型 ----
    private final StringProperty accept = new SimpleStringProperty("");

    public String getAccept() { return accept.get(); }
    public StringProperty acceptProperty() { return accept; }
    public void setAccept(String accept) { this.accept.set(accept); }

    // ---- listType: 文件列表类型 ----
    private final ObjectProperty<ListType> listType = new SimpleObjectProperty<>(ListType.TEXT);

    public ListType getListType() { return listType.get(); }
    public ObjectProperty<ListType> listTypeProperty() { return listType; }
    public void setListType(ListType listType) { this.listType.set(listType); }

    // ---- autoUpload: 是否自动上传 ----
    private final BooleanProperty autoUpload = new SimpleBooleanProperty(true);

    public boolean isAutoUpload() { return autoUpload.get(); }
    public BooleanProperty autoUploadProperty() { return autoUpload; }
    public void setAutoUpload(boolean autoUpload) { this.autoUpload.set(autoUpload); }

    // ---- disabled: 是否禁用 ----
    private final BooleanProperty uploaderDisabled = new SimpleBooleanProperty(false);

    public boolean isUploaderDisabled() { return uploaderDisabled.get(); }
    public BooleanProperty uploaderDisabledProperty() { return uploaderDisabled; }
    public void setUploaderDisabled(boolean disabled) { this.uploaderDisabled.set(disabled); }

    // ---- limit: 文件数量限制（0 表示无限制） ----
    private final IntegerProperty limit = new SimpleIntegerProperty(0);

    public int getLimit() { return limit.get(); }
    public IntegerProperty limitProperty() { return limit; }
    public void setLimit(int limit) { this.limit.set(limit); }

    // ---- fileList: 文件列表 ----
    private final ListProperty<UploadFile> fileList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public ObservableList<UploadFile> getFileList() { return fileList.get(); }
    public ListProperty<UploadFile> fileListProperty() { return fileList; }
    public void setFileList(ObservableList<UploadFile> fileList) { this.fileList.set(fileList); }

    // ---- directory: 是否启用文件夹上传 ----
    private final BooleanProperty directory = new SimpleBooleanProperty(false);

    public boolean isDirectory() { return directory.get(); }
    public BooleanProperty directoryProperty() { return directory; }
    public void setDirectory(boolean directory) { this.directory.set(directory); }

    // ---- tip: 提示文本 ----
    private final StringProperty tip = new SimpleStringProperty("");

    public String getTip() { return tip.get(); }
    public StringProperty tipProperty() { return tip; }
    public void setTip(String tip) { this.tip.set(tip); }

    // ---- buttonText: 按钮文本 ----
    private final StringProperty buttonText = new SimpleStringProperty("点击上传");

    public String getButtonText() { return buttonText.get(); }
    public StringProperty buttonTextProperty() { return buttonText; }
    public void setButtonText(String text) { this.buttonText.set(text); }

    // ---- dragText: 拖拽区域文本 ----
    private final StringProperty dragText = new SimpleStringProperty("将文件拖到此处，或点击上传");

    public String getDragText() { return dragText.get(); }
    public StringProperty dragTextProperty() { return dragText; }
    public void setDragText(String text) { this.dragText.set(text); }

    // ---- thumbnailSize: 缩略图尺寸（用于 PICTURE_CARD 模式） ----
    private final DoubleProperty thumbnailSize = new SimpleDoubleProperty(148);

    public double getThumbnailSize() { return thumbnailSize.get(); }
    public DoubleProperty thumbnailSizeProperty() { return thumbnailSize; }
    public void setThumbnailSize(double size) { this.thumbnailSize.set(size); }

    // ==========================================================================
    //  Callbacks - 对标 Element UI Upload 的事件回调
    // ==========================================================================

    // ---- httpRequest: 自定义上传处理器 ----
    private final ObjectProperty<FileUploadHandler> httpRequest = new SimpleObjectProperty<>(new HttpFileUploadHandler());

    public FileUploadHandler getHttpRequest() { return httpRequest.get(); }
    public ObjectProperty<FileUploadHandler> httpRequestProperty() { return httpRequest; }
    public void setHttpRequest(FileUploadHandler handler) { this.httpRequest.set(handler); }

    // ---- beforeUpload: 上传前校验钩子。返回 false 则阻止上传 ----
    private final ObjectProperty<Function<UploadFile, Boolean>> beforeUpload = new SimpleObjectProperty<>();

    public Function<UploadFile, Boolean> getBeforeUpload() { return beforeUpload.get(); }
    public ObjectProperty<Function<UploadFile, Boolean>> beforeUploadProperty() { return beforeUpload; }
    public void setBeforeUpload(Function<UploadFile, Boolean> fn) { this.beforeUpload.set(fn); }

    // ---- beforeRemove: 删除前校验钩子。返回 false 则阻止删除 ----
    private final ObjectProperty<Function<UploadFile, Boolean>> beforeRemove = new SimpleObjectProperty<>();

    public Function<UploadFile, Boolean> getBeforeRemove() { return beforeRemove.get(); }
    public ObjectProperty<Function<UploadFile, Boolean>> beforeRemoveProperty() { return beforeRemove; }
    public void setBeforeRemove(Function<UploadFile, Boolean> fn) { this.beforeRemove.set(fn); }

    // ---- onPreview: 预览回调 ----
    private final ObjectProperty<Consumer<UploadFile>> onPreview = new SimpleObjectProperty<>();

    public Consumer<UploadFile> getOnPreview() { return onPreview.get(); }
    public ObjectProperty<Consumer<UploadFile>> onPreviewProperty() { return onPreview; }
    public void setOnPreview(Consumer<UploadFile> fn) { this.onPreview.set(fn); }

    // ---- onRemove: 删除回调 ----
    private final ObjectProperty<BiConsumer<UploadFile, ObservableList<UploadFile>>> onRemove = new SimpleObjectProperty<>();

    public BiConsumer<UploadFile, ObservableList<UploadFile>> getOnRemove() { return onRemove.get(); }
    public ObjectProperty<BiConsumer<UploadFile, ObservableList<UploadFile>>> onRemoveProperty() { return onRemove; }
    public void setOnRemove(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { this.onRemove.set(fn); }

    // ---- onSuccess: 上传成功回调 ----
    private final ObjectProperty<BiConsumer<UploadFile, Object>> onSuccess = new SimpleObjectProperty<>();

    public BiConsumer<UploadFile, Object> getOnSuccess() { return onSuccess.get(); }
    public ObjectProperty<BiConsumer<UploadFile, Object>> onSuccessProperty() { return onSuccess; }
    public void setOnSuccess(BiConsumer<UploadFile, Object> fn) { this.onSuccess.set(fn); }

    // ---- onError: 上传失败回调 ----
    private final ObjectProperty<BiConsumer<UploadFile, Throwable>> onError = new SimpleObjectProperty<>();

    public BiConsumer<UploadFile, Throwable> getOnError() { return onError.get(); }
    public ObjectProperty<BiConsumer<UploadFile, Throwable>> onErrorProperty() { return onError; }
    public void setOnError(BiConsumer<UploadFile, Throwable> fn) { this.onError.set(fn); }

    // ---- onProgress: 上传进度回调 ----
    private final ObjectProperty<BiConsumer<UploadFile, Double>> onProgress = new SimpleObjectProperty<>();

    public BiConsumer<UploadFile, Double> getOnProgress() { return onProgress.get(); }
    public ObjectProperty<BiConsumer<UploadFile, Double>> onProgressProperty() { return onProgress; }
    public void setOnProgress(BiConsumer<UploadFile, Double> fn) { this.onProgress.set(fn); }

    // ---- onChange: 文件列表变化回调 ----
    private final ObjectProperty<BiConsumer<UploadFile, ObservableList<UploadFile>>> onChange = new SimpleObjectProperty<>();

    public BiConsumer<UploadFile, ObservableList<UploadFile>> getOnChange() { return onChange.get(); }
    public ObjectProperty<BiConsumer<UploadFile, ObservableList<UploadFile>>> onChangeProperty() { return onChange; }
    public void setOnChange(BiConsumer<UploadFile, ObservableList<UploadFile>> fn) { this.onChange.set(fn); }

    // ---- onExceed: 超出限制回调 ----
    private final ObjectProperty<BiConsumer<List<File>, ObservableList<UploadFile>>> onExceed = new SimpleObjectProperty<>();

    public BiConsumer<List<File>, ObservableList<UploadFile>> getOnExceed() { return onExceed.get(); }
    public ObjectProperty<BiConsumer<List<File>, ObservableList<UploadFile>>> onExceedProperty() { return onExceed; }
    public void setOnExceed(BiConsumer<List<File>, ObservableList<UploadFile>> fn) { this.onExceed.set(fn); }

    // ==========================================================================
    //  Public API Methods - 对标 Element UI Upload 的方法
    // ==========================================================================

    /**
     * 手动触发上传（用于 autoUpload=false 的场景）
     */
    public void submit() {
        getFileList().stream()
                .filter(f -> f.getStatus() == UploadFileStatus.READY)
                .forEach(this::doUpload);
    }

    /**
     * 取消所有上传并清空文件列表
     */
    public void clearFiles() {
        abortAll();
        getFileList().clear();
    }

    /**
     * 取消指定文件的上传
     *
     * @param file 要取消的文件
     */
    public void abort(UploadFile file) {
        if (file != null) {
            Thread thread = activeUploads.remove(file.getUid());
            if (thread != null) {
                thread.interrupt();
            }
        }
    }

    /**
     * 取消所有正在进行的上传
     */
    public void abortAll() {
        activeUploads.values().forEach(Thread::interrupt);
        activeUploads.clear();
    }

    /**
     * 手动添加文件到列表（模拟选择文件）
     *
     * @param file 要添加的文件
     */
    public void handleStart(File file) {
        addFile(file);
    }

    /**
     * 手动移除文件
     *
     * @param uploadFile 要移除的文件
     */
    public void handleRemove(UploadFile uploadFile) {
        removeFile(uploadFile);
    }

    // ==========================================================================
    //  Internal Logic
    // ==========================================================================

    /**
     * 添加文件到文件列表
     *
     * @param rawFile 原始文件
     */
    void addFile(File rawFile) {
        if (rawFile == null || !rawFile.exists()) {
            return;
        }
        UploadFile uploadFile = new UploadFile(rawFile);

        // beforeUpload 校验
        Function<UploadFile, Boolean> beforeFn = getBeforeUpload();
        if (beforeFn != null && !Boolean.TRUE.equals(beforeFn.apply(uploadFile))) {
            return;
        }

        // 生成缩略图（对于图片文件）
        if (uploadFile.isImage()) {
            generateThumbnail(uploadFile);
        }

        getFileList().add(uploadFile);

        // onChange 回调
        fireOnChange(uploadFile);

        // 自动上传
        if (isAutoUpload()) {
            doUpload(uploadFile);
        }
    }

    /**
     * 添加多个文件，处理 limit 检查
     *
     * @param files 文件列表
     */
    void addFiles(List<File> files) {
        if (files == null || files.isEmpty()) return;

        int currentLimit = getLimit();
        if (currentLimit > 0) {
            int currentSize = getFileList().size();
            int available = currentLimit - currentSize;

            if (available <= 0) {
                // 超出限制
                BiConsumer<List<File>, ObservableList<UploadFile>> exceedFn = getOnExceed();
                if (exceedFn != null) {
                    exceedFn.accept(files, getFileList());
                }
                return;
            }

            if (files.size() > available) {
                // 超出限制
                BiConsumer<List<File>, ObservableList<UploadFile>> exceedFn = getOnExceed();
                if (exceedFn != null) {
                    exceedFn.accept(files, getFileList());
                }
                return;
            }
        }

        for (File file : files) {
            addFile(file);
        }
    }

    /**
     * 从文件列表移除文件
     *
     * @param uploadFile 要移除的文件
     */
    void removeFile(UploadFile uploadFile) {
        // beforeRemove 校验
        Function<UploadFile, Boolean> beforeFn = getBeforeRemove();
        if (beforeFn != null && !Boolean.TRUE.equals(beforeFn.apply(uploadFile))) {
            return;
        }

        // 取消上传
        abort(uploadFile);

        getFileList().remove(uploadFile);

        // onRemove 回调
        BiConsumer<UploadFile, ObservableList<UploadFile>> removeFn = getOnRemove();
        if (removeFn != null) {
            removeFn.accept(uploadFile, getFileList());
        }
    }

    /**
     * 执行实际上传
     *
     * @param uploadFile 要上传的文件
     */
    void doUpload(UploadFile uploadFile) {
        if (uploadFile == null || uploadFile.getRawFile() == null) return;

        String actionUrl = getAction();
        FileUploadHandler handler = getHttpRequest();
        if (handler == null || actionUrl == null || actionUrl.isEmpty()) {
            // 无上传目标，标记成功（本地模式）——同步设置确保 UI 立即反映状态
            uploadFile.setStatus(UploadFileStatus.SUCCESS);
            uploadFile.setProgress(1.0);
            BiConsumer<UploadFile, Object> successFn = getOnSuccess();
            if (successFn != null) {
                successFn.accept(uploadFile, null);
            }
            fireOnChange(uploadFile);
            return;
        }

        uploadFile.setStatus(UploadFileStatus.UPLOADING);
        uploadFile.setProgress(0);

        uploadExecutor.submit(() -> {
            activeUploads.put(uploadFile.getUid(), Thread.currentThread());
            try {
                FileUploadHandler.UploadRequest request = FileUploadHandler.UploadRequest
                        .builder(uploadFile.getRawFile(), actionUrl)
                        .fileName(getFieldName())
                        .headers(getHeaders())
                        .data(getData())
                        .withCredentials(isWithCredentials())
                        .onProgress(progress -> javafx.application.Platform.runLater(() -> {
                            uploadFile.setProgress(progress);
                            BiConsumer<UploadFile, Double> progressFn = getOnProgress();
                            if (progressFn != null) {
                                progressFn.accept(uploadFile, progress);
                            }
                        }))
                        .onSuccess(response -> javafx.application.Platform.runLater(() -> {
                            uploadFile.setStatus(UploadFileStatus.SUCCESS);
                            uploadFile.setProgress(1.0);
                            uploadFile.setResponse(response);
                            activeUploads.remove(uploadFile.getUid());
                            BiConsumer<UploadFile, Object> successFn = getOnSuccess();
                            if (successFn != null) {
                                successFn.accept(uploadFile, response);
                            }
                            fireOnChange(uploadFile);
                        }))
                        .onError(error -> javafx.application.Platform.runLater(() -> {
                            uploadFile.setStatus(UploadFileStatus.FAIL);
                            uploadFile.setErrorMessage(error.getMessage());
                            activeUploads.remove(uploadFile.getUid());
                            BiConsumer<UploadFile, Throwable> errorFn = getOnError();
                            if (errorFn != null) {
                                errorFn.accept(uploadFile, error);
                            }
                            fireOnChange(uploadFile);
                        }))
                        .build();

                handler.upload(request);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    uploadFile.setStatus(UploadFileStatus.FAIL);
                    uploadFile.setErrorMessage(e.getMessage());
                    activeUploads.remove(uploadFile.getUid());
                    BiConsumer<UploadFile, Throwable> errorFn = getOnError();
                    if (errorFn != null) {
                        errorFn.accept(uploadFile, e);
                    }
                    fireOnChange(uploadFile);
                });
            }
        });
    }

    /**
     * 生成图片缩略图
     */
    private void generateThumbnail(UploadFile uploadFile) {
        if (uploadFile.getRawFile() != null && uploadFile.getRawFile().exists()) {
            try {
                double size = getThumbnailSize();
                javafx.scene.image.Image thumbnail = new javafx.scene.image.Image(
                        uploadFile.getRawFile().toURI().toString(),
                        size, size, true, true, true
                );
                uploadFile.setThumbnail(thumbnail);
            } catch (Exception e) {
                // 缩略图生成失败不影响上传流程
            }
        }
    }

    /**
     * 触发 onChange 回调
     */
    private void fireOnChange(UploadFile uploadFile) {
        BiConsumer<UploadFile, ObservableList<UploadFile>> changeFn = getOnChange();
        if (changeFn != null) {
            changeFn.accept(uploadFile, getFileList());
        }
    }

    // ==========================================================================
    //  Skin & Stylesheet
    // ==========================================================================

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FileUploaderSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(
                FileUploader.class.getResource("/io/aurora/fx/components/upload/file-uploader.css")
        ).toExternalForm();
    }

    /**
     * 释放资源。在组件不再使用时调用。
     * 将取消所有进行中的上传并关闭线程池。
     */
    public void dispose() {
        abortAll();
        try {
            uploadExecutor.shutdownNow();
        } catch (Exception ignored) {
        }
    }
}
