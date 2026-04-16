package io.aurora.fx.common.path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 跨平台路径管理类，提供统一的系统路径和应用路径访问接口。
 * 
 * <p>该类提供了三类路径：</p>
 * <ul>
 *   <li><b>系统路径</b>：如用户主目录、临时目录、文档目录等</li>
 *   <li><b>应用路径</b>：基于应用标识符的应用专属目录，用于存储应用数据、配置、缓存等</li>
 *   <li><b>用户目录</b>：如桌面、下载、图片等用户常用目录</li>
 * </ul>
 * 
 * <h2>初始化</h2>
 * <p>使用应用路径前，必须先调用 {@link #identifier(String)} 设置应用标识符：</p>
 * <pre>{@code
 * // 设置应用标识符（通常使用反向域名格式）
 * Paths.identifier("com.example.myapp");
 * 
 * // 然后可以使用应用路径
 * String appData = Paths.app();           // 应用主目录
 * String appCache = Paths.appCache();     // 应用缓存目录
 * String appConfig = Paths.appConfig();   // 应用配置目录
 * }</pre>
 * 
 * <h2>跨平台路径映射</h2>
 * <table border="1">
 *   <tr><th>路径类型</th><th>Windows</th><th>macOS</th><th>Linux</th></tr>
 *   <tr><td>应用主目录</td><td>%LOCALAPPDATA%\{id}</td><td>~/Library/Application Support/{id}</td><td>~/.local/share/{id}</td></tr>
 *   <tr><td>应用缓存</td><td>%LOCALAPPDATA%\{id}\Cache</td><td>~/Library/Caches/{id}</td><td>~/.cache/{id}</td></tr>
 *   <tr><td>应用配置</td><td>%APPDATA%\{id}\Config</td><td>~/Library/Preferences/{id}</td><td>~/.config/{id}</td></tr>
 *   <tr><td>应用数据</td><td>%APPDATA%\{id}\Data</td><td>~/Library/Application Support/{id}</td><td>~/.local/share/{id}</td></tr>
 *   <tr><td>应用日志</td><td>%LOCALAPPDATA%\{id}\Logs</td><td>~/Library/Logs/{id}</td><td>~/.local/share/{id}/logs</td></tr>
 *   <tr><td>临时目录</td><td>%TEMP%</td><td>/tmp</td><td>/tmp</td></tr>
 *   <tr><td>桌面</td><td>~/Desktop</td><td>~/Desktop</td><td>~/Desktop</td></tr>
 *   <tr><td>下载</td><td>~/Downloads</td><td>~/Downloads</td><td>~/Downloads</td></tr>
 *   <tr><td>文档</td><td>~/Documents</td><td>~/Documents</td><td>~/Documents</td></tr>
 *   <tr><td>图片</td><td>~/Pictures</td><td>~/Pictures</td><td>~/Pictures</td></tr>
 *   <tr><td>音乐</td><td>~/Music</td><td>~/Music</td><td>~/Music</td></tr>
 *   <tr><td>视频</td><td>~/Videos</td><td>~/Movies</td><td>~/Videos</td></tr>
 * </table>
 * 
 * <h2>线程安全性</h2>
 * <p>该类是线程安全的。所有路径值使用延迟初始化，并通过 AtomicReference 保证原子性。</p>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 初始化应用标识符
 * Paths.identifier("io.aurora.fx");
 * 
 * // 获取系统路径
 * String home = Paths.home();           // 用户主目录
 * String temp = Paths.temp();           // 临时目录
 * String desktop = Paths.desktop();     // 桌面目录
 * 
 * // 获取应用路径
 * String dataDir = Paths.appData();     // 应用数据目录
 * String logDir = Paths.appLog();       // 应用日志目录
 * 
 * // 创建目录（如果不存在）
 * Paths.createDirectories(Paths.appLog());
 * 
 * // 规范化路径
 * String normalized = Paths.normalize("C:\\Users\\test\\..\\test2");
 * }</pre>
 * 
 * @author Aurora-FX Team
 * @since 0.0.1
 * @see SystemUtils
 */
public final class Paths {
    
    // ==================== 应用标识符 ====================
    
    /**
     * 应用标识符，用于构建应用专属目录路径。
     * 通常使用反向域名格式，如 "com.example.myapp"。
     */
    private static final AtomicReference<String> BUNDLE_IDENTIFIER = new AtomicReference<>();
    
    // ==================== 应用路径缓存 ====================
    
    /** 应用主目录 */
    private static final AtomicReference<String> APP = new AtomicReference<>();
    /** 应用缓存目录 */
    private static final AtomicReference<String> APP_CACHE = new AtomicReference<>();
    /** 应用配置目录 */
    private static final AtomicReference<String> APP_CONFIG = new AtomicReference<>();
    /** 应用数据目录 */
    private static final AtomicReference<String> APP_DATA = new AtomicReference<>();
    /** 应用本地数据目录 */
    private static final AtomicReference<String> APP_LOCAL_DATA = new AtomicReference<>();
    /** 应用日志目录 */
    private static final AtomicReference<String> APP_LOG = new AtomicReference<>();
    
    // ==================== 系统路径缓存 ====================
    
    /** 用户主目录 */
    private static final AtomicReference<String> HOME = new AtomicReference<>();
    /** 系统缓存目录 */
    private static final AtomicReference<String> CACHE = new AtomicReference<>();
    /** 系统配置目录 */
    private static final AtomicReference<String> CONFIG = new AtomicReference<>();
    /** 系统数据目录 */
    private static final AtomicReference<String> DATA = new AtomicReference<>();
    /** 系统文档目录 */
    private static final AtomicReference<String> DOCUMENTS = new AtomicReference<>();
    /** 系统可执行文件目录 */
    private static final AtomicReference<String> EXECUTABLE = new AtomicReference<>();
    /** 系统本地数据目录 */
    private static final AtomicReference<String> LOCAL_DATA = new AtomicReference<>();
    /** 系统公共目录 */
    private static final AtomicReference<String> PUBLIC = new AtomicReference<>();
    /** 系统资源目录 */
    private static final AtomicReference<String> RESOURCE = new AtomicReference<>();
    /** 系统运行时目录 */
    private static final AtomicReference<String> RUNTIME = new AtomicReference<>();
    /** 系统临时目录 */
    private static final AtomicReference<String> TEMP = new AtomicReference<>();
    /** 系统模板目录 */
    private static final AtomicReference<String> TEMPLATE = new AtomicReference<>();
    
    // ==================== 用户目录缓存 ====================
    
    /** 桌面目录 */
    private static final AtomicReference<String> DESKTOP = new AtomicReference<>();
    /** 图片目录 */
    private static final AtomicReference<String> PICTURES = new AtomicReference<>();
    /** 下载目录 */
    private static final AtomicReference<String> DOWNLOADS = new AtomicReference<>();
    /** 音乐目录 */
    private static final AtomicReference<String> MUSIC = new AtomicReference<>();
    /** 视频目录 */
    private static final AtomicReference<String> VIDEOS = new AtomicReference<>();
    
    // ==================== Linux UID 缓存 ====================
    
    /** Linux 用户 ID（延迟初始化） */
    private static final AtomicReference<String> LINUX_UID = new AtomicReference<>();
    
    /**
     * 私有构造函数，防止实例化。
     */
    private Paths() {
        throw new AssertionError("Paths 是工具类，不应被实例化");
    }
    
    // ==================== 初始化方法 ====================
    
    /**
     * 设置应用标识符并初始化应用路径。
     * 
     * <p>应用标识符用于构建应用专属目录路径。通常使用反向域名格式，
     * 如 "com.example.myapp"、"io.aurora.fx" 等。</p>
     * 
     * <p>调用此方法后，应用路径缓存将被清空并重新初始化。</p>
     * 
     * <h3>平台路径示例</h3>
     * <table border="1">
     *   <tr><th>平台</th><th>应用主目录</th></tr>
     *   <tr><td>Windows</td><td>C:\Users\{user}\AppData\Local\com.example.myapp</td></tr>
     *   <tr><td>macOS</td><td>/Users/{user}/Library/Application Support/com.example.myapp</td></tr>
     *   <tr><td>Linux</td><td>/home/{user}/.local/share/com.example.myapp</td></tr>
     * </table>
     * 
     * @param identifier 应用标识符，不能为 null 或空字符串
     * @throws IllegalArgumentException 如果 identifier 为 null 或空
     */
    public static void identifier(String identifier) {
        Objects.requireNonNull(identifier, "应用标识符不能为 null");
        String trimmed = identifier.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("应用标识符不能为空字符串");
        }
        
        // 如果标识符发生变化，清空应用路径缓存
        String oldIdentifier = BUNDLE_IDENTIFIER.get();
        if (!trimmed.equals(oldIdentifier)) {
            BUNDLE_IDENTIFIER.set(trimmed);
            clearAppPaths();
        }
    }
    
    /**
     * 获取当前应用标识符。
     *
     * @return 当前应用标识符，未设置时返回 null
     */
    public static String getIdentifier() {
        return BUNDLE_IDENTIFIER.get();
    }
    
    /**
     * 清空应用路径缓存。
     * 
     * <p>当应用标识符变更时调用，确保路径重新生成。</p>
     */
    private static void clearAppPaths() {
        APP.set(null);
        APP_CACHE.set(null);
        APP_CONFIG.set(null);
        APP_DATA.set(null);
        APP_LOCAL_DATA.set(null);
        APP_LOG.set(null);
    }
    
    /**
     * 重置所有路径缓存。
     * 
     * <p>主要用于测试场景，重置后所有路径将重新初始化。</p>
     */
    public static void reset() {
        BUNDLE_IDENTIFIER.set(null);
        clearAppPaths();
        HOME.set(null);
        CACHE.set(null);
        CONFIG.set(null);
        DATA.set(null);
        DOCUMENTS.set(null);
        EXECUTABLE.set(null);
        LOCAL_DATA.set(null);
        PUBLIC.set(null);
        RESOURCE.set(null);
        RUNTIME.set(null);
        TEMP.set(null);
        TEMPLATE.set(null);
        DESKTOP.set(null);
        PICTURES.set(null);
        DOWNLOADS.set(null);
        MUSIC.set(null);
        VIDEOS.set(null);
        LINUX_UID.set(null);
    }
    
    // ==================== 断言与标识符处理 ====================
    
    /**
     * 断言应用标识符已设置。
     *
     * @throws IllegalStateException 如果应用标识符未设置
     */
    private static void assertIdentifier() {
        String identifier = BUNDLE_IDENTIFIER.get();
        if (identifier == null || identifier.trim().isEmpty()) {
            // 尝试从系统属性获取
            identifier = System.getProperty("javafx.identifier");
            if (identifier == null || identifier.trim().isEmpty()) {
                throw new IllegalStateException(
                    "应用标识符未设置。请先调用 Paths.identifier(\"com.example.myapp\") 进行设置，" +
                    "或设置系统属性 -Djavafx.identifier=com.example.myapp"
                );
            }
            BUNDLE_IDENTIFIER.set(identifier);
        }
    }
    
    // ==================== 应用路径方法 ====================
    
    /**
     * 获取应用主目录路径。
     * 
     * <p>应用主目录是应用存储所有数据、配置、缓存等的根目录。
     * 不同的操作系统有不同的标准路径：</p>
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th><th>说明</th></tr>
     *   <tr><td>Windows</td><td>%LOCALAPPDATA%\{identifier}</td><td>C:\Users\{user}\AppData\Local\{id}</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Application Support/{identifier}</td><td>符合 Apple 规范</td></tr>
     *   <tr><td>Linux</td><td>~/.local/share/{identifier}</td><td>符合 XDG 规范</td></tr>
     * </table>
     * 
     * @return 应用主目录路径
     * @throws IllegalStateException 如果应用标识符未设置
     */
    public static String app() {
        assertIdentifier();
        return getOrCompute(APP, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Local", getIdentifier());
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Application Support", getIdentifier());
            } else {
                return join(SystemUtils.USER_HOME, ".local", "share", getIdentifier());
            }
        });
    }
    
    /**
     * 获取应用缓存目录路径。
     * 
     * <p>缓存目录用于存储可以重新生成的临时数据，如：</p>
     * <ul>
     *   <li>图片/视频缩略图缓存</li>
     *   <li>网络请求缓存</li>
     *   <li>临时下载文件</li>
     * </ul>
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%LOCALAPPDATA%\{identifier}\Cache</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Caches/{identifier}</td></tr>
     *   <tr><td>Linux</td><td>~/.cache/{identifier}</td></tr>
     * </table>
     * 
     * @return 应用缓存目录路径
     * @throws IllegalStateException 如果应用标识符未设置
     */
    public static String appCache() {
        assertIdentifier();
        return getOrCompute(APP_CACHE, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Local", getIdentifier(), "Cache");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Caches", getIdentifier());
            } else {
                return join(SystemUtils.USER_HOME, ".cache", getIdentifier());
            }
        });
    }
    
    /**
     * 获取应用配置目录路径。
     * 
     * <p>配置目录用于存储应用配置文件，如：</p>
     * <ul>
     *   <li>用户偏好设置</li>
     *   <li>应用配置文件（JSON/YAML/Properties）</li>
     *   <li>主题设置</li>
     * </ul>
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%APPDATA%\{identifier}\Config</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Preferences/{identifier}</td></tr>
     *   <tr><td>Linux</td><td>~/.config/{identifier}</td></tr>
     * </table>
     * 
     * @return 应用配置目录路径
     * @throws IllegalStateException 如果应用标识符未设置
     */
    public static String appConfig() {
        assertIdentifier();
        return getOrCompute(APP_CONFIG, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Roaming", getIdentifier(), "Config");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Preferences", getIdentifier());
            } else {
                return join(SystemUtils.USER_HOME, ".config", getIdentifier());
            }
        });
    }
    
    /**
     * 获取应用数据目录路径。
     * 
     * <p>数据目录用于存储应用的重要持久化数据，如：</p>
     * <ul>
     *   <li>用户数据库</li>
     *   <li>用户文档</li>
     *   <li>重要业务数据</li>
     * </ul>
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%APPDATA%\{identifier}\Data</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Application Support/{identifier}</td></tr>
     *   <tr><td>Linux</td><td>~/.local/share/{identifier}</td></tr>
     * </table>
     * 
     * @return 应用数据目录路径
     * @throws IllegalStateException 如果应用标识符未设置
     */
    public static String appData() {
        assertIdentifier();
        return getOrCompute(APP_DATA, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Roaming", getIdentifier(), "Data");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Application Support", getIdentifier());
            } else {
                return join(SystemUtils.USER_HOME, ".local", "share", getIdentifier());
            }
        });
    }
    
    /**
     * 获取应用本地数据目录路径。
     * 
     * <p>本地数据目录用于存储特定于本机的数据，如：</p>
     * <ul>
     *   <li>本机特定的配置</li>
     *   <li>本机缓存数据</li>
     *   <li>不需要在设备间同步的数据</li>
     * </ul>
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%LOCALAPPDATA%\{identifier}\Data</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Application Support/{identifier}</td></tr>
     *   <tr><td>Linux</td><td>~/.local/share/{identifier}</td></tr>
     * </table>
     * 
     * @return 应用本地数据目录路径
     * @throws IllegalStateException 如果应用标识符未设置
     */
    public static String appLocalData() {
        assertIdentifier();
        return getOrCompute(APP_LOCAL_DATA, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Local", getIdentifier(), "Data");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Application Support", getIdentifier());
            } else {
                return join(SystemUtils.USER_HOME, ".local", "share", getIdentifier());
            }
        });
    }
    
    /**
     * 获取应用日志目录路径。
     * 
     * <p>日志目录用于存储应用的运行时日志文件，如：</p>
     * <ul>
     *   <li>错误日志</li>
     *   <li>访问日志</li>
     *   <li>调试日志</li>
     * </ul>
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%LOCALAPPDATA%\{identifier}\Logs</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Logs/{identifier}</td></tr>
     *   <tr><td>Linux</td><td>~/.local/share/{identifier}/logs</td></tr>
     * </table>
     * 
     * @return 应用日志目录路径
     * @throws IllegalStateException 如果应用标识符未设置
     */
    public static String appLog() {
        assertIdentifier();
        return getOrCompute(APP_LOG, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Local", getIdentifier(), "Logs");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Logs", getIdentifier());
            } else {
                return join(SystemUtils.USER_HOME, ".local", "share", getIdentifier(), "logs");
            }
        });
    }
    
    // ==================== 系统路径方法 ====================
    
    /**
     * 获取用户主目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径示例</th></tr>
     *   <tr><td>Windows</td><td>C:\Users\{username}</td></tr>
     *   <tr><td>macOS</td><td>/Users/{username}</td></tr>
     *   <tr><td>Linux</td><td>/home/{username}</td></tr>
     * </table>
     *
     * @return 用户主目录路径
     */
    public static String home() {
        return getOrCompute(HOME, () -> SystemUtils.USER_HOME);
    }
    
    /**
     * 获取系统缓存目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%TEMP%</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Caches</td></tr>
     *   <tr><td>Linux</td><td>~/.cache</td></tr>
     * </table>
     *
     * @return 系统缓存目录路径
     */
    public static String cache() {
        return getOrCompute(CACHE, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Local", "Temp");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Caches");
            } else {
                return join(SystemUtils.USER_HOME, ".cache");
            }
        });
    }
    
    /**
     * 获取系统配置目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%APPDATA%</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Preferences</td></tr>
     *   <tr><td>Linux</td><td>~/.config</td></tr>
     * </table>
     *
     * @return 系统配置目录路径
     */
    public static String config() {
        return getOrCompute(CONFIG, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Roaming");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Preferences");
            } else {
                return join(SystemUtils.USER_HOME, ".config");
            }
        });
    }
    
    /**
     * 获取系统数据目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%APPDATA%</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Application Support</td></tr>
     *   <tr><td>Linux</td><td>~/.local/share</td></tr>
     * </table>
     *
     * @return 系统数据目录路径
     */
    public static String data() {
        return getOrCompute(DATA, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Roaming");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Application Support");
            } else {
                return join(SystemUtils.USER_HOME, ".local", "share");
            }
        });
    }
    
    /**
     * 获取用户文档目录路径。
     * 
     * <p>所有平台都使用 ~/Documents 路径。</p>
     *
     * @return 文档目录路径
     */
    public static String documents() {
        return getOrCompute(DOCUMENTS, () -> join(SystemUtils.USER_HOME, "Documents"));
    }
    
    /**
     * 获取系统可执行文件目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>C:\Windows\System32</td></tr>
     *   <tr><td>macOS</td><td>/usr/bin</td></tr>
     *   <tr><td>Linux</td><td>/usr/bin</td></tr>
     * </table>
     *
     * @return 系统可执行文件目录路径
     */
    public static String executable() {
        return getOrCompute(EXECUTABLE, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                String systemDrive = SystemUtils.USER_HOME.substring(0, 2);
                return join(systemDrive, "Windows", "System32");
            } else {
                return "/usr/bin";
            }
        });
    }
    
    /**
     * 获取系统本地数据目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%LOCALAPPDATA%</td></tr>
     *   <tr><td>macOS</td><td>~/Library/Application Support</td></tr>
     *   <tr><td>Linux</td><td>~/.local/share</td></tr>
     * </table>
     *
     * @return 系统本地数据目录路径
     */
    public static String localData() {
        return getOrCompute(LOCAL_DATA, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Local");
            } else if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Library", "Application Support");
            } else {
                return join(SystemUtils.USER_HOME, ".local", "share");
            }
        });
    }
    
    /**
     * 获取系统公共目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>C:\Users\Public</td></tr>
     *   <tr><td>macOS</td><td>/Users/Shared</td></tr>
     *   <tr><td>Linux</td><td>/usr/share</td></tr>
     * </table>
     *
     * @return 系统公共目录路径
     */
    public static String pub() {
        return getOrCompute(PUBLIC, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                String systemDrive = SystemUtils.USER_HOME.substring(0, 2);
                return join(systemDrive, "Users", "Public");
            } else if (SystemUtils.IS_OS_MAC) {
                return "/Users/Shared";
            } else {
                return "/usr/share";
            }
        });
    }
    
    /**
     * 获取系统资源目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>C:\Windows\Resources</td></tr>
     *   <tr><td>macOS</td><td>/System/Library</td></tr>
     *   <tr><td>Linux</td><td>/usr/share</td></tr>
     * </table>
     *
     * @return 系统资源目录路径
     */
    public static String resource() {
        return getOrCompute(RESOURCE, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                String systemDrive = SystemUtils.USER_HOME.substring(0, 2);
                return join(systemDrive, "Windows", "Resources");
            } else if (SystemUtils.IS_OS_MAC) {
                return "/System/Library";
            } else {
                return "/usr/share";
            }
        });
    }
    
    /**
     * 获取系统运行时目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>C:\Windows\Temp</td></tr>
     *   <tr><td>macOS</td><td>/tmp</td></tr>
     *   <tr><td>Linux</td><td>/run/user/{uid}</td></tr>
     * </table>
     *
     * @return 系统运行时目录路径
     */
    public static String runtime() {
        return getOrCompute(RUNTIME, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                String systemDrive = SystemUtils.USER_HOME.substring(0, 2);
                return join(systemDrive, "Windows", "Temp");
            } else if (SystemUtils.IS_OS_MAC) {
                return "/tmp";
            } else {
                return join("/run", "user", getLinuxUid());
            }
        });
    }
    
    /**
     * 获取系统临时目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>C:\Windows\Temp 或 %TEMP%</td></tr>
     *   <tr><td>macOS</td><td>/tmp</td></tr>
     *   <tr><td>Linux</td><td>/tmp</td></tr>
     * </table>
     *
     * @return 系统临时目录路径
     */
    public static String temp() {
        return getOrCompute(TEMP, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                String systemDrive = SystemUtils.USER_HOME.substring(0, 2);
                return join(systemDrive, "Windows", "Temp");
            } else {
                return "/tmp";
            }
        });
    }
    
    /**
     * 获取系统模板目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>%APPDATA%\Microsoft\Templates</td></tr>
     *   <tr><td>macOS</td><td>~/Templates</td></tr>
     *   <tr><td>Linux</td><td>~/Templates</td></tr>
     * </table>
     *
     * @return 系统模板目录路径
     */
    public static String template() {
        return getOrCompute(TEMPLATE, () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                return join(SystemUtils.USER_HOME, "AppData", "Roaming", "Microsoft", "Templates");
            } else {
                return join(SystemUtils.USER_HOME, "Templates");
            }
        });
    }
    
    // ==================== 用户目录方法 ====================
    
    /**
     * 获取用户桌面目录路径。
     *
     * @return 桌面目录路径
     */
    public static String desktop() {
        return getOrCompute(DESKTOP, () -> join(SystemUtils.USER_HOME, "Desktop"));
    }
    
    /**
     * 获取用户图片目录路径。
     *
     * @return 图片目录路径
     */
    public static String pictures() {
        return getOrCompute(PICTURES, () -> join(SystemUtils.USER_HOME, "Pictures"));
    }
    
    /**
     * 获取用户下载目录路径。
     *
     * @return 下载目录路径
     */
    public static String downloads() {
        return getOrCompute(DOWNLOADS, () -> join(SystemUtils.USER_HOME, "Downloads"));
    }
    
    /**
     * 获取用户音乐目录路径。
     *
     * @return 音乐目录路径
     */
    public static String music() {
        return getOrCompute(MUSIC, () -> join(SystemUtils.USER_HOME, "Music"));
    }
    
    /**
     * 获取用户视频目录路径。
     * 
     * <table border="1">
     *   <tr><th>平台</th><th>路径</th></tr>
     *   <tr><td>Windows</td><td>~/Videos</td></tr>
     *   <tr><td>macOS</td><td>~/Movies</td></tr>
     *   <tr><td>Linux</td><td>~/Videos</td></tr>
     * </table>
     *
     * @return 视频目录路径
     */
    public static String videos() {
        return getOrCompute(VIDEOS, () -> {
            if (SystemUtils.IS_OS_MAC) {
                return join(SystemUtils.USER_HOME, "Movies");
            } else {
                return join(SystemUtils.USER_HOME, "Videos");
            }
        });
    }
    
    // ==================== 路径操作工具方法 ====================
    
    /**
     * 连接多个路径组件。
     * 
     * <p>使用系统默认的文件分隔符连接路径组件，自动处理跨平台兼容性。</p>
     * 
     * <h3>使用示例</h3>
     * <pre>{@code
     * // Windows
     * String path = Paths.join("C:", "Users", "test", "Documents");
     * // 结果: "C:\Users\test\Documents"
     * 
     * // macOS/Linux
     * String path = Paths.join("/home", "user", "Documents");
     * // 结果: "/home/user/Documents"
     * }</pre>
     *
     * @param first 第一个路径组件（不能为 null）
     * @param more  其他路径组件
     * @return 连接后的路径字符串
     * @throws NullPointerException 如果 first 为 null
     */
    public static String join(String first, String... more) {
        Objects.requireNonNull(first, "第一个路径组件不能为 null");
        if (more == null || more.length == 0) {
            return first;
        }
        return first + SystemUtils.FILE_SEPARATOR + String.join(SystemUtils.FILE_SEPARATOR, more);
    }
    
    /**
     * 规范化路径。
     * 
     * <p>移除路径中的冗余部分，如 {@code .}（当前目录）和 {@code ..}（上级目录），
     * 并统一使用系统默认的分隔符。</p>
     * 
     * <h3>使用示例</h3>
     * <pre>{@code
     * String path = Paths.normalize("/home/user/../test/./file.txt");
     * // 结果: "/home/test/file.txt"
     * 
     * String path2 = Paths.normalize("C:\\Users\\test\\..\\test2");
     * // 结果: "C:\Users\test2"
     * }</pre>
     *
     * @param path 要规范化的路径
     * @return 规范化后的路径，如果 path 为 null 则返回 null
     */
    public static String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        try {
            return java.nio.file.Paths.get(path).normalize().toString();
        } catch (Exception e) {
            return path;
        }
    }
    
    /**
     * 解析路径，返回绝对路径。
     * 
     * <p>如果路径是相对路径，则基于当前工作目录解析为绝对路径。</p>
     *
     * @param path 要解析的路径
     * @return 绝对路径字符串
     * @throws NullPointerException 如果 path 为 null
     */
    public static String toAbsolutePath(String path) {
        Objects.requireNonNull(path, "路径不能为 null");
        return java.nio.file.Paths.get(path).toAbsolutePath().toString();
    }
    
    /**
     * 判断路径是否存在。
     *
     * @param path 要检查的路径
     * @return 如果路径存在返回 {@code true}，否则返回 {@code false}
     */
    public static boolean exists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return Files.exists(java.nio.file.Paths.get(path));
    }
    
    /**
     * 判断路径是否为目录。
     *
     * @param path 要检查的路径
     * @return 如果是目录返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return Files.isDirectory(java.nio.file.Paths.get(path));
    }
    
    /**
     * 判断路径是否为文件。
     *
     * @param path 要检查的路径
     * @return 如果是文件返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return Files.isRegularFile(java.nio.file.Paths.get(path));
    }
    
    /**
     * 创建目录（如果不存在）。
     * 
     * <p>如果父目录不存在，会一并创建。等同于 {@code mkdir -p}。</p>
     *
     * @param path 要创建的目录路径
     * @return 创建的目录路径（Path 对象）
     * @throws IOException 如果创建目录失败
     * @throws NullPointerException 如果 path 为 null
     */
    public static Path createDirectories(String path) throws IOException {
        Objects.requireNonNull(path, "路径不能为 null");
        return Files.createDirectories(java.nio.file.Paths.get(path));
    }
    
    /**
     * 创建目录（如果不存在），不抛出检查异常。
     * 
     * <p>等同于 {@link #createDirectories(String)}，但失败时返回 null 而不是抛出异常。</p>
     *
     * @param path 要创建的目录路径
     * @return 创建的目录路径，失败时返回 null
     */
    public static String createDirectoriesQuietly(String path) {
        try {
            createDirectories(path);
            return path;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 确保应用目录存在。
     * 
     * <p>创建应用相关的所有必要目录（如果不存在）：</p>
     * <ul>
     *   <li>应用主目录</li>
     *   <li>应用数据目录</li>
     *   <li>应用配置目录</li>
     *   <li>应用缓存目录</li>
     *   <li>应用日志目录</li>
     * </ul>
     *
     * @throws IOException 如果创建目录失败
     * @throws IllegalStateException 如果应用标识符未设置
     */
    public static void ensureAppDirectories() throws IOException {
        createDirectories(app());
        createDirectories(appData());
        createDirectories(appConfig());
        createDirectories(appCache());
        createDirectories(appLog());
    }
    
    /**
     * 确保应用目录存在（静默版本）。
     * 
     * <p>等同于 {@link #ensureAppDirectories()}，但失败时返回 false 而不是抛出异常。</p>
     *
     * @return 如果所有目录创建成功返回 {@code true}，否则返回 {@code false}
     */
    public static boolean ensureAppDirectoriesQuietly() {
        try {
            ensureAppDirectories();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取文件扩展名。
     * 
     * <p>返回文件扩展名（不含点号），如 "txt"、"jpg"、"pdf"。
     * 如果文件没有扩展名，返回空字符串。</p>
     *
     * @param path 文件路径或文件名
     * @return 扩展名（小写），无扩展名时返回空字符串
     */
    public static String getExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastDot = path.lastIndexOf('.');
        int lastSep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastDot > lastSep + 1 && lastDot < path.length() - 1) {
            return path.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 获取文件名（不含扩展名）。
     *
     * @param path 文件路径
     * @return 文件名（不含扩展名）
     */
    public static String getBaseName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String fileName = getFileName(path);
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
    
    /**
     * 获取文件名（含扩展名）。
     *
     * @param path 文件路径
     * @return 文件名（含扩展名）
     */
    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSep >= 0 ? path.substring(lastSep + 1) : path;
    }
    
    /**
     * 获取父目录路径。
     *
     * @param path 文件路径
     * @return 父目录路径，如果没有父目录返回 null
     */
    public static String getParent(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        Path parent = java.nio.file.Paths.get(path).getParent();
        return parent != null ? parent.toString() : null;
    }
    
    // ==================== 内部辅助方法 ====================
    
    /**
     * 获取或计算值（线程安全的懒加载）。
     */
    private static String getOrCompute(AtomicReference<String> ref, java.util.function.Supplier<String> supplier) {
        String value = ref.get();
        if (value == null) {
            value = supplier.get();
            ref.compareAndSet(null, value);
            value = ref.get();
        }
        return value;
    }
    
    /**
     * 获取 Linux 用户 ID。
     * 
     * <p>仅用于 Linux 系统，通过执行 {@code id -u} 命令获取。</p>
     *
     * @return Linux 用户 ID
     */
    private static String getLinuxUid() {
        return getOrCompute(LINUX_UID, () -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("id", "-u");
                pb.redirectErrorStream(true);
                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String uid = reader.readLine();
                    process.waitFor();
                    return uid != null ? uid.trim() : "0";
                }
            } catch (Exception e) {
                return "0";
            }
        });
    }
}
