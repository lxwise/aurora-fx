package io.aurora.fx.components.upload;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 文件上传处理器接口。
 * <p>
 * 定义了文件上传的核心行为，支持自定义实现（如 HTTP 上传、FTP 上传、本地拷贝等）。
 * 用户可实现此接口来替换默认的上传逻辑。
 * </p>
 *
 * @author lstar
 * @since 2025
 */
@FunctionalInterface
public interface FileUploadHandler {

    /**
     * 执行文件上传
     *
     * @param request 上传请求参数
     */
    void upload(UploadRequest request);

    /**
     * 上传请求参数封装
     */
    class UploadRequest {

        private final File file;
        private final String action;
        private final String fileName;
        private final Map<String, String> headers;
        private final Map<String, String> data;
        private final boolean withCredentials;
        private final Consumer<Double> onProgress;
        private final Consumer<Object> onSuccess;
        private final Consumer<Throwable> onError;

        private UploadRequest(Builder builder) {
            this.file = builder.file;
            this.action = builder.action;
            this.fileName = builder.fileName;
            this.headers = builder.headers;
            this.data = builder.data;
            this.withCredentials = builder.withCredentials;
            this.onProgress = builder.onProgress;
            this.onSuccess = builder.onSuccess;
            this.onError = builder.onError;
        }

        public File getFile() { return file; }
        public String getAction() { return action; }
        public String getFileName() { return fileName; }
        public Map<String, String> getHeaders() { return headers; }
        public Map<String, String> getData() { return data; }
        public boolean isWithCredentials() { return withCredentials; }
        public Consumer<Double> getOnProgress() { return onProgress; }
        public Consumer<Object> getOnSuccess() { return onSuccess; }
        public Consumer<Throwable> getOnError() { return onError; }

        public static Builder builder(File file, String action) {
            return new Builder(file, action);
        }

        public static class Builder {
            private final File file;
            private final String action;
            private String fileName = "file";
            private Map<String, String> headers = Map.of();
            private Map<String, String> data = Map.of();
            private boolean withCredentials = false;
            private Consumer<Double> onProgress = p -> {};
            private Consumer<Object> onSuccess = r -> {};
            private Consumer<Throwable> onError = e -> {};

            public Builder(File file, String action) {
                this.file = file;
                this.action = action;
            }

            public Builder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public Builder headers(Map<String, String> headers) {
                this.headers = headers != null ? headers : Map.of();
                return this;
            }

            public Builder data(Map<String, String> data) {
                this.data = data != null ? data : Map.of();
                return this;
            }

            public Builder withCredentials(boolean withCredentials) {
                this.withCredentials = withCredentials;
                return this;
            }

            public Builder onProgress(Consumer<Double> onProgress) {
                this.onProgress = onProgress != null ? onProgress : p -> {};
                return this;
            }

            public Builder onSuccess(Consumer<Object> onSuccess) {
                this.onSuccess = onSuccess != null ? onSuccess : r -> {};
                return this;
            }

            public Builder onError(Consumer<Throwable> onError) {
                this.onError = onError != null ? onError : e -> {};
                return this;
            }

            public UploadRequest build() {
                return new UploadRequest(this);
            }
        }
    }
}
