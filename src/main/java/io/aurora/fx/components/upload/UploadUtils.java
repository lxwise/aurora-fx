package io.aurora.fx.components.upload;

import java.util.Set;

/**
 * 上传组件工具类，提供文件类型判断、大小格式化等实用方法。
 *
 * @author lstar
 * @since 2025
 */
public final class UploadUtils {

    private UploadUtils() {
    }

    /** 常见图片扩展名集合 */
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif"
    );

    /**
     * 判断扩展名是否为图片类型
     *
     * @param extension 文件扩展名（不含 '.'，小写）
     * @return 是否为图片扩展名
     */
    public static boolean isImageExtension(String extension) {
        return extension != null && IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 判断文件名是否为图片文件
     *
     * @param fileName 文件名
     * @return 是否为图片
     */
    public static boolean isImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) return false;
        return isImageExtension(fileName.substring(dotIndex + 1));
    }

    /**
     * 获取文件扩展名（小写）
     *
     * @param fileName 文件名
     * @return 扩展名，若无返回空字符串
     */
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1).toLowerCase() : "";
    }

    /**
     * 格式化文件大小为人类可读字符串
     *
     * @param bytes 文件字节数
     * @return 格式化后的文件大小字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 根据 MIME 类型或扩展名推断 accept 过滤器描述
     *
     * @param accept accept 字符串，如 "image/*", ".jpg,.png"
     * @return 描述字符串
     */
    public static String getAcceptDescription(String accept) {
        if (accept == null || accept.isEmpty()) return "All Files";
        if (accept.startsWith("image/")) return "Image Files";
        if (accept.startsWith("video/")) return "Video Files";
        if (accept.startsWith("audio/")) return "Audio Files";
        return "Files (" + accept + ")";
    }

    /**
     * 根据 accept 字符串生成 FileChooser 的扩展名过滤列表
     * <p>
     * 支持格式：
     * <ul>
     *   <li>"image/*" → 所有常见图片格式</li>
     *   <li>".jpg,.png,.gif" → 指定扩展名</li>
     *   <li>"image/jpeg,image/png" → MIME 类型</li>
     * </ul>
     *
     * @param accept accept 过滤字符串
     * @return 扩展名数组，如 ["*.jpg", "*.png"]
     */
    public static String[] parseAcceptToExtensions(String accept) {
        if (accept == null || accept.isEmpty()) {
            return new String[]{"*.*"};
        }

        // image/* -> 所有图片格式
        if ("image/*".equals(accept.trim())) {
            return IMAGE_EXTENSIONS.stream()
                    .map(ext -> "*." + ext)
                    .toArray(String[]::new);
        }

        // 逗号分隔的扩展名或 MIME 类型
        String[] parts = accept.split(",");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .map(part -> {
                    if (part.startsWith(".")) {
                        return "*" + part;
                    } else if (part.contains("/")) {
                        // MIME type: image/jpeg -> *.jpeg
                        String ext = part.substring(part.lastIndexOf('/') + 1);
                        if ("*".equals(ext)) return "*.*";
                        if ("jpeg".equals(ext)) return "*.jpg";
                        return "*." + ext;
                    }
                    return "*." + part;
                })
                .toArray(String[]::new);
    }
}
