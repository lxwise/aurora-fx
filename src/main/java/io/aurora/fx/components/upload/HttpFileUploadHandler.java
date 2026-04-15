package io.aurora.fx.components.upload;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * 基于 HTTP Multipart 的文件上传处理器。
 * <p>
 * 使用 JDK 内置的 {@link HttpURLConnection} 实现 multipart/form-data 上传，
 * 无需引入额外的 HTTP 客户端库。支持进度回调、自定义 Header 和附加表单数据。
 * </p>
 *
 * @author lstar
 * @since 2025
 */
public class HttpFileUploadHandler implements FileUploadHandler {

    private static final String LINE_FEED = "\r\n";
    private static final int BUFFER_SIZE = 8192;

    /** 连接超时（毫秒），默认 15 秒 */
    private static final int DEFAULT_CONNECT_TIMEOUT = 15_000;

    /** 读取超时（毫秒），默认 60 秒 */
    private static final int DEFAULT_READ_TIMEOUT = 60_000;

    private final int connectTimeout;
    private final int readTimeout;

    /**
     * 使用默认超时配置构造
     */
    public HttpFileUploadHandler() {
        this(DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * 使用自定义超时配置构造
     *
     * @param connectTimeout 连接超时（毫秒）
     * @param readTimeout    读取超时（毫秒）
     */
    public HttpFileUploadHandler(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public void upload(UploadRequest request) {
        String boundary = "----AuroraFxUpload" + UUID.randomUUID().toString().replace("-", "");
        HttpURLConnection connection = null;

        try {
            URL url = URI.create(request.getAction()).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Connection", "Keep-Alive");

            // 设置自定义 Header
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 计算总大小用于进度回调
            File file = request.getFile();
            long totalSize = file.length();

            try (OutputStream outputStream = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

                // 写入附加表单数据
                if (request.getData() != null) {
                    for (Map.Entry<String, String> entry : request.getData().entrySet()) {
                        writer.append("--").append(boundary).append(LINE_FEED);
                        writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(LINE_FEED);
                        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
                        writer.append(LINE_FEED);
                        writer.append(entry.getValue()).append(LINE_FEED);
                        writer.flush();
                    }
                }

                // 写入文件部分
                writer.append("--").append(boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"")
                        .append(request.getFileName())
                        .append("\"; filename=\"")
                        .append(file.getName())
                        .append("\"")
                        .append(LINE_FEED);
                writer.append("Content-Type: application/octet-stream").append(LINE_FEED);
                writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();

                // 分块写入文件内容，并回调进度
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    long bytesWritten = 0;
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        bytesWritten += bytesRead;
                        double progress = (double) bytesWritten / totalSize;
                        request.getOnProgress().accept(Math.min(progress, 1.0));
                    }
                    outputStream.flush();
                }

                writer.append(LINE_FEED);
                writer.append("--").append(boundary).append("--").append(LINE_FEED);
                writer.flush();
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String response = readResponse(connection.getInputStream());
                request.getOnSuccess().accept(response);
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                request.getOnError().accept(new IOException(
                        "Upload failed with HTTP " + responseCode + ": " + errorResponse));
            }

        } catch (Exception e) {
            request.getOnError().accept(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 读取输入流为字符串
     */
    private String readResponse(InputStream inputStream) {
        if (inputStream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
