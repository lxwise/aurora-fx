package io.aurora.fx.common.path;

import java.util.Objects;

/**
 * 系统工具类，提供跨平台的系统属性检测和访问功能。
 * 
 * <p>该类是一个不可实例化的工具类，所有方法都是静态方法。
 * 系统属性在类加载时初始化，后续调用不会重新获取。</p>
 * 
 * <h2>平台支持</h2>
 * <ul>
 *   <li><b>Windows</b>：Windows 10/11、Windows Server 等</li>
 *   <li><b>macOS</b>：macOS 10.x - 14.x（Intel 和 Apple Silicon）</li>
 *   <li><b>Linux</b>：Ubuntu、Debian、CentOS、Fedora 等主流发行版</li>
 * </ul>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 检测当前操作系统
 * if (SystemUtils.isWindows()) {
 *     System.out.println("运行在 Windows 系统");
 * } else if (SystemUtils.isMac()) {
 *     System.out.println("运行在 macOS 系统");
 * } else if (SystemUtils.isLinux()) {
 *     System.out.println("运行在 Linux 系统");
 * }
 * 
 * // 获取系统信息
 * System.out.println("用户目录: " + SystemUtils.getUserHome());
 * System.out.println("Java 版本: " + SystemUtils.getJavaVersion());
 * System.out.println("操作系统: " + SystemUtils.getOsName() + " " + SystemUtils.getOsVersion());
 * }</pre>
 * 
 * @author Aurora-FX Team
 * @since 0.0.1
 */
public final class SystemUtils {
    
    // ==================== 系统属性常量 ====================
    
    /**
     * 用户主目录路径。
     * 
     * <p>跨平台路径说明：</p>
     * <ul>
     *   <li><b>Windows</b>：{@code C:\Users\用户名}</li>
     *   <li><b>macOS</b>：{@code /Users/用户名}</li>
     *   <li><b>Linux</b>：{@code /home/用户名}</li>
     * </ul>
     */
    public static final String USER_HOME;
    
    /**
     * 当前工作目录路径。
     * 
     * <p>即 JVM 启动时所在的目录，等同于 {@code new File(".").getAbsolutePath()}。</p>
     */
    public static final String USER_DIR;
    
    /**
     * 当前用户名。
     */
    public static final String USER_NAME;
    
    /**
     * 操作系统名称。
     * 
     * <p>例如：{@code "Windows 11"}、{@code "Mac OS X"}、{@code "Linux"}</p>
     */
    public static final String OS_NAME;
    
    /**
     * 操作系统架构。
     * 
     * <p>例如：{@code "amd64"}、{@code "aarch64"}、{@code "x86"}</p>
     */
    public static final String OS_ARCH;
    
    /**
     * 操作系统版本。
     * 
     * <p>例如：{@code "10.0"}（Windows 10）、{@code "14.0"}（macOS 14）</p>
     */
    public static final String OS_VERSION;
    
    /**
     * 文件分隔符。
     * 
     * <ul>
     *   <li><b>Windows</b>：{@code \}</li>
     *   <li><b>macOS/Linux</b>：{@code /}</li>
     * </ul>
     */
    public static final String FILE_SEPARATOR;
    
    /**
     * 路径分隔符（用于 PATH 环境变量）。
     * 
     * <ul>
     *   <li><b>Windows</b>：{@code ;}</li>
     *   <li><b>macOS/Linux</b>：{@code :}</li>
     * </ul>
     */
    public static final String PATH_SEPARATOR;
    
    /**
     * 行分隔符。
     * 
     * <ul>
     *   <li><b>Windows</b>：{@code \r\n}</li>
     *   <li><b>macOS/Linux</b>：{@code \n}</li>
     * </ul>
     */
    public static final String LINE_SEPARATOR;
    
    /**
     * 临时文件目录路径。
     */
    public static final String JAVA_IO_TMPDIR;
    
    // ==================== Java 属性常量 ====================
    
    /**
     * Java 运行时版本。
     * 
     * <p>例如：{@code "21.0.1"}、{@code "17.0.9"}</p>
     */
    public static final String JAVA_VERSION;
    
    /**
     * Java 虚拟机名称。
     * 
     * <p>例如：{@code "OpenJDK 64-Bit Server VM"}</p>
     */
    public static final String JAVA_VM_NAME;
    
    /**
     * Java 虚拟机版本。
     */
    public static final String JAVA_VM_VERSION;
    
    /**
     * Java 主目录（JRE/JDK 安装路径）。
     */
    public static final String JAVA_HOME;
    
    // ==================== 操作系统检测标志 ====================
    
    /**
     * 是否为 macOS 系统。
     */
    public static final boolean IS_OS_MAC;
    
    /**
     * 是否为 Windows 系统。
     */
    public static final boolean IS_OS_WINDOWS;
    
    /**
     * 是否为 Linux 系统。
     */
    public static final boolean IS_OS_LINUX;
    
    /**
     * 是否为 Unix-like 系统（包括 macOS、Linux、BSD 等）。
     */
    public static final boolean IS_OS_UNIX;
    
    // ==================== 架构检测标志 ====================
    
    /**
     * 是否为 64 位 JVM。
     */
    public static final boolean IS_64_BIT_JVM;
    
    /**
     * 是否为 ARM 架构（Apple Silicon、ARM 服务器）。
     */
    public static final boolean IS_ARM_ARCH;
    
    // 静态初始化块：初始化所有系统属性
    static {
        // 基础用户属性
        USER_HOME = getSystemProperty("user.home", "");
        USER_DIR = getSystemProperty("user.dir", "");
        USER_NAME = getSystemProperty("user.name", "unknown");
        
        // 操作系统属性
        OS_NAME = getSystemProperty("os.name", "unknown");
        OS_ARCH = getSystemProperty("os.arch", "unknown");
        OS_VERSION = getSystemProperty("os.version", "unknown");
        
        // 分隔符
        FILE_SEPARATOR = getSystemProperty("file.separator", "/");
        PATH_SEPARATOR = getSystemProperty("path.separator", ":");
        LINE_SEPARATOR = getSystemProperty("line.separator", "\n");
        
        // 临时目录
        JAVA_IO_TMPDIR = getSystemProperty("java.io.tmpdir", "/tmp");
        
        // Java 属性
        JAVA_VERSION = getSystemProperty("java.version", "unknown");
        JAVA_VM_NAME = getSystemProperty("java.vm.name", "unknown");
        JAVA_VM_VERSION = getSystemProperty("java.vm.version", "unknown");
        JAVA_HOME = getSystemProperty("java.home", "");
        
        // 操作系统检测
        IS_OS_MAC = isOsNameStartsWith("Mac");
        IS_OS_WINDOWS = isOsNameStartsWith("Windows");
        IS_OS_LINUX = isOsNameStartsWith("Linux");
        IS_OS_UNIX = IS_OS_MAC || IS_OS_LINUX || 
                     isOsNameStartsWith("AIX") || 
                     isOsNameStartsWith("HP-UX") || 
                     isOsNameStartsWith("Irix") || 
                     isOsNameStartsWith("Solaris") || 
                     isOsNameStartsWith("SunOS") ||
                     isOsNameStartsWith("FreeBSD") ||
                     isOsNameStartsWith("OpenBSD");
        
        // 架构检测
        IS_64_BIT_JVM = isArchMatch("amd64") || isArchMatch("x86_64") || 
                        isArchMatch("aarch64") || isArchMatch("ppc64") ||
                        isArchMatch("sparc64") || isArchMatch("ia64");
        IS_ARM_ARCH = isArchMatch("aarch64") || isArchMatch("arm");
    }
    
    /**
     * 私有构造函数，防止实例化。
     */
    private SystemUtils() {
        throw new AssertionError("SystemUtils 是工具类，不应被实例化");
    }
    
    // ==================== 系统属性获取方法 ====================
    
    /**
     * 获取系统属性，如果属性不存在则返回默认值。
     *
     * @param key          系统属性键名
     * @param defaultValue 默认值
     * @return 系统属性值，不存在时返回默认值
     */
    public static String getSystemProperty(String key, String defaultValue) {
        Objects.requireNonNull(key, "系统属性键名不能为 null");
        try {
            String value = System.getProperty(key);
            return value != null ? value : defaultValue;
        } catch (SecurityException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取系统属性，如果属性不存在则返回 null。
     *
     * @param key 系统属性键名
     * @return 系统属性值，不存在或无权限访问时返回 null
     */
    public static String getSystemProperty(String key) {
        return getSystemProperty(key, null);
    }
    
    // ==================== 操作系统检测方法 ====================
    
    /**
     * 判断是否为 Windows 系统。
     *
     * @return 如果是 Windows 系统返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isWindows() {
        return IS_OS_WINDOWS;
    }
    
    /**
     * 判断是否为 macOS 系统。
     *
     * @return 如果是 macOS 系统返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isMac() {
        return IS_OS_MAC;
    }
    
    /**
     * 判断是否为 Linux 系统。
     *
     * @return 如果是 Linux 系统返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isLinux() {
        return IS_OS_LINUX;
    }
    
    /**
     * 判断是否为 Unix-like 系统（包括 macOS、Linux、BSD 等）。
     *
     * @return 如果是 Unix-like 系统返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isUnix() {
        return IS_OS_UNIX;
    }
    
    /**
     * 获取操作系统类型名称。
     * 
     * <p>返回值可能为：{@code "Windows"}、{@code "macOS"}、{@code "Linux"}、{@code "Unknown"}</p>
     *
     * @return 操作系统类型名称
     */
    public static String getOsType() {
        if (IS_OS_WINDOWS) return "Windows";
        if (IS_OS_MAC) return "macOS";
        if (IS_OS_LINUX) return "Linux";
        return "Unknown";
    }
    
    // ==================== 属性访问方法 ====================
    
    /**
     * 获取用户主目录路径。
     *
     * @return 用户主目录路径
     */
    public static String getUserHome() {
        return USER_HOME;
    }
    
    /**
     * 获取当前工作目录路径。
     *
     * @return 当前工作目录路径
     */
    public static String getUserDir() {
        return USER_DIR;
    }
    
    /**
     * 获取当前用户名。
     *
     * @return 当前用户名
     */
    public static String getUserName() {
        return USER_NAME;
    }
    
    /**
     * 获取操作系统名称。
     *
     * @return 操作系统名称
     */
    public static String getOsName() {
        return OS_NAME;
    }
    
    /**
     * 获取操作系统架构。
     *
     * @return 操作系统架构
     */
    public static String getOsArch() {
        return OS_ARCH;
    }
    
    /**
     * 获取操作系统版本。
     *
     * @return 操作系统版本
     */
    public static String getOsVersion() {
        return OS_VERSION;
    }
    
    /**
     * 获取 Java 版本。
     *
     * @return Java 版本
     */
    public static String getJavaVersion() {
        return JAVA_VERSION;
    }
    
    /**
     * 获取 Java 主目录。
     *
     * @return Java 主目录路径
     */
    public static String getJavaHome() {
        return JAVA_HOME;
    }
    
    /**
     * 获取临时文件目录。
     *
     * @return 临时文件目录路径
     */
    public static String getTempDir() {
        return JAVA_IO_TMPDIR;
    }
    
    /**
     * 获取文件分隔符。
     *
     * @return 文件分隔符
     */
    public static String getFileSeparator() {
        return FILE_SEPARATOR;
    }
    
    /**
     * 获取路径分隔符。
     *
     * @return 路径分隔符
     */
    public static String getPathSeparator() {
        return PATH_SEPARATOR;
    }
    
    /**
     * 获取行分隔符。
     *
     * @return 行分隔符
     */
    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }
    
    // ==================== Java 版本解析方法 ====================
    
    /**
     * 获取 Java 主版本号。
     * 
     * <p>对于 Java 9+，版本号直接从版本字符串解析；
     * 对于 Java 8 及以下，从 "1.8.0_xxx" 格式中提取主版本号。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>{@code "21.0.1"} → 21</li>
     *   <li>{@code "17.0.9"} → 17</li>
     *   <li>{@code "1.8.0_392"} → 8</li>
     * </ul>
     *
     * @return Java 主版本号，解析失败返回 0
     */
    public static int getJavaMajorVersion() {
        String version = JAVA_VERSION;
        if (version == null || version.isEmpty()) {
            return 0;
        }
        
        try {
            // Java 9+ 格式：直接是版本号，如 "21.0.1"
            if (!version.startsWith("1.")) {
                int dotIndex = version.indexOf('.');
                if (dotIndex > 0) {
                    return Integer.parseInt(version.substring(0, dotIndex));
                }
                return Integer.parseInt(version);
            }
            
            // Java 8 及以下格式：如 "1.8.0_392"
            int firstDot = version.indexOf('.');
            int secondDot = version.indexOf('.', firstDot + 1);
            if (secondDot > firstDot) {
                return Integer.parseInt(version.substring(firstDot + 1, secondDot));
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        return 0;
    }
    
    /**
     * 检查当前 Java 版本是否大于等于指定版本。
     *
     * @param majorVersion 要比较的主版本号
     * @return 如果当前 Java 版本大于等于指定版本返回 {@code true}
     */
    public static boolean isJavaVersionAtLeast(int majorVersion) {
        return getJavaMajorVersion() >= majorVersion;
    }
    
    // ==================== 内部辅助方法 ====================
    
    /**
     * 检查操作系统名称是否以指定前缀开头（忽略大小写）。
     *
     * @param prefix 前缀字符串
     * @return 如果匹配返回 {@code true}
     */
    private static boolean isOsNameStartsWith(String prefix) {
        return OS_NAME != null && 
               OS_NAME.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    /**
     * 检查系统架构是否匹配指定架构名称。
     *
     * @param arch 架构名称
     * @return 如果匹配返回 {@code true}
     */
    private static boolean isArchMatch(String arch) {
        return OS_ARCH != null && 
               OS_ARCH.toLowerCase().contains(arch.toLowerCase());
    }
    
    // ==================== 调试信息方法 ====================
    
    /**
     * 获取系统信息摘要字符串。
     * 
     * <p>包含操作系统、Java 版本、用户信息等关键信息。</p>
     *
     * @return 系统信息摘要字符串
     */
    public static String getSystemInfo() {
        return String.format(
            "操作系统: %s %s (%s)%n" +
            "Java 版本: %s (%s)%n" +
            "Java 主目录: %s%n" +
            "用户目录: %s%n" +
            "用户名: %s%n" +
            "临时目录: %s%n" +
            "工作目录: %s%n" +
            "文件分隔符: %s%n" +
            "路径分隔符: %s%n" +
            "行分隔符: %s (长度: %d)%n" +
            "64位 JVM: %s%n" +
            "ARM 架构: %s",
            OS_NAME, OS_VERSION, OS_ARCH,
            JAVA_VERSION, JAVA_VM_NAME,
            JAVA_HOME,
            USER_HOME,
            USER_NAME,
            JAVA_IO_TMPDIR,
            USER_DIR,
            FILE_SEPARATOR,
            PATH_SEPARATOR,
            LINE_SEPARATOR.replace("\r", "\\r").replace("\n", "\\n"),
            LINE_SEPARATOR.length(),
            IS_64_BIT_JVM,
            IS_ARM_ARCH
        );
    }
}
