package io.aurora.fx.components.upload;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.File;
import java.util.UUID;

/**
 * 上传文件模型类，封装文件的基本信息、上传状态和进度。
 * <p>
 * 所有属性均为 JavaFX 可观察属性，支持绑定与监听。
 * 对标 Element UI 的 UploadFile 类型。
 * </p>
 *
 * @author lstar
 * @since 2025
 */
public class UploadFile {

    /** 文件唯一标识 */
    private final String uid;

    /** 原始文件对象 */
    private final File rawFile;

    // ====================== Observable Properties ======================

    private final StringProperty name = new SimpleStringProperty();
    private final LongProperty size = new SimpleLongProperty();
    private final ObjectProperty<UploadFileStatus> status = new SimpleObjectProperty<>(UploadFileStatus.READY);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty url = new SimpleStringProperty();
    private final ObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();
    private final StringProperty errorMessage = new SimpleStringProperty();
    private final ObjectProperty<Object> response = new SimpleObjectProperty<>();

    /**
     * 构造函数
     *
     * @param rawFile 原始文件
     */
    public UploadFile(File rawFile) {
        this.uid = UUID.randomUUID().toString();
        this.rawFile = rawFile;
        this.name.set(rawFile.getName());
        this.size.set(rawFile.length());
    }

    /**
     * 构造函数（通过 URL 创建，用于回显已上传文件）
     *
     * @param name 文件名
     * @param url  文件URL
     */
    public UploadFile(String name, String url) {
        this.uid = UUID.randomUUID().toString();
        this.rawFile = null;
        this.name.set(name);
        this.url.set(url);
        this.status.set(UploadFileStatus.SUCCESS);
    }

    // ====================== UID ======================

    public String getUid() {
        return uid;
    }

    // ====================== Raw File ======================

    public File getRawFile() {
        return rawFile;
    }

    // ====================== Name ======================

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    // ====================== Size ======================

    public long getSize() {
        return size.get();
    }

    public LongProperty sizeProperty() {
        return size;
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    // ====================== Status ======================

    public UploadFileStatus getStatus() {
        return status.get();
    }

    public ObjectProperty<UploadFileStatus> statusProperty() {
        return status;
    }

    public void setStatus(UploadFileStatus status) {
        this.status.set(status);
    }

    // ====================== Progress ======================

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    // ====================== URL ======================

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    // ====================== Thumbnail ======================

    public Image getThumbnail() {
        return thumbnail.get();
    }

    public ObjectProperty<Image> thumbnailProperty() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail.set(thumbnail);
    }

    // ====================== Error Message ======================

    public String getErrorMessage() {
        return errorMessage.get();
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage.set(errorMessage);
    }

    // ====================== Response ======================

    public Object getResponse() {
        return response.get();
    }

    public ObjectProperty<Object> responseProperty() {
        return response;
    }

    public void setResponse(Object response) {
        this.response.set(response);
    }

    // ====================== Utility Methods ======================

    /**
     * 获取文件扩展名（小写）
     *
     * @return 文件扩展名，若无则返回空字符串
     */
    public String getExtension() {
        String fileName = getName();
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1).toLowerCase() : "";
    }

    /**
     * 判断文件是否为图片类型
     *
     * @return 是否为图片
     */
    public boolean isImage() {
        return UploadUtils.isImageExtension(getExtension());
    }

    /**
     * 获取人类可读的文件大小字符串
     *
     * @return 文件大小描述，如 "1.5 MB"
     */
    public String getReadableSize() {
        return UploadUtils.formatFileSize(getSize());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadFile that = (UploadFile) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "UploadFile{" +
                "uid='" + uid + '\'' +
                ", name='" + getName() + '\'' +
                ", size=" + getSize() +
                ", status=" + getStatus() +
                '}';
    }
}
