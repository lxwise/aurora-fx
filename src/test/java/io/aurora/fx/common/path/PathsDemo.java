package io.aurora.fx.common.path;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Paths 和 SystemUtils 完整功能演示类。
 * 
 * <p>本 Demo 展示所有重构后的方法，包括：</p>
 * <ul>
 *   <li>SystemUtils 系统属性检测</li>
 *   <li>Paths 应用路径管理</li>
 *   <li>Paths 系统路径访问</li>
 *   <li>Paths 用户目录访问</li>
 *   <li>Paths 路径操作工具</li>
 * </ul>
 * 
 * @author Aurora-FX Team
 * @since 0.0.1
 */
public class PathsDemo {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Aurora-FX Path 模块完整功能演示                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        // 1. SystemUtils 演示
        demoSystemUtils();
        
        // 2. 初始化 Paths
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【初始化 Paths】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        String identifier = "io.aurora.fx.demo";
        System.out.println("设置应用标识符: " + identifier);
        Paths.identifier(identifier);
        System.out.println("当前标识符: " + Paths.getIdentifier());
        
        // 3. 应用路径演示
        demoAppPaths();
        
        // 4. 系统路径演示
        demoSystemPaths();
        
        // 5. 用户目录演示
        demoUserDirectories();
        
        // 6. 路径操作工具演示
        demoPathOperations();
        
        // 7. 实际使用场景演示
        demoRealWorldScenarios();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     演示完成！                                                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * 演示 SystemUtils 的所有功能
     */
    private static void demoSystemUtils() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【SystemUtils - 系统属性与检测】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 1.1 操作系统检测
        System.out.println("\n▶ 操作系统检测:");
        System.out.println("  isWindows(): " + SystemUtils.isWindows());
        System.out.println("  isMac(): " + SystemUtils.isMac());
        System.out.println("  isLinux(): " + SystemUtils.isLinux());
        System.out.println("  isUnix(): " + SystemUtils.isUnix());
        System.out.println("  getOsType(): " + SystemUtils.getOsType());
        
        // 1.2 系统属性常量
        System.out.println("\n▶ 系统属性常量:");
        System.out.println("  USER_HOME: " + SystemUtils.USER_HOME);
        System.out.println("  USER_DIR: " + SystemUtils.USER_DIR);
        System.out.println("  USER_NAME: " + SystemUtils.USER_NAME);
        System.out.println("  OS_NAME: " + SystemUtils.OS_NAME);
        System.out.println("  OS_ARCH: " + SystemUtils.OS_ARCH);
        System.out.println("  OS_VERSION: " + SystemUtils.OS_VERSION);
        System.out.println("  FILE_SEPARATOR: '" + SystemUtils.FILE_SEPARATOR + "'");
        System.out.println("  PATH_SEPARATOR: '" + SystemUtils.PATH_SEPARATOR + "'");
        System.out.println("  LINE_SEPARATOR (转义): " + 
            SystemUtils.LINE_SEPARATOR.replace("\r", "\\r").replace("\n", "\\n"));
        System.out.println("  JAVA_IO_TMPDIR: " + SystemUtils.JAVA_IO_TMPDIR);
        System.out.println("  JAVA_VERSION: " + SystemUtils.JAVA_VERSION);
        System.out.println("  JAVA_VM_NAME: " + SystemUtils.JAVA_VM_NAME);
        System.out.println("  JAVA_VM_VERSION: " + SystemUtils.JAVA_VM_VERSION);
        System.out.println("  JAVA_HOME: " + SystemUtils.JAVA_HOME);
        
        // 1.3 架构检测
        System.out.println("\n▶ 架构检测:");
        System.out.println("  IS_64_BIT_JVM: " + SystemUtils.IS_64_BIT_JVM);
        System.out.println("  IS_ARM_ARCH: " + SystemUtils.IS_ARM_ARCH);
        
        // 1.4 Java 版本检测
        System.out.println("\n▶ Java 版本检测:");
        System.out.println("  getJavaMajorVersion(): " + SystemUtils.getJavaMajorVersion());
        System.out.println("  isJavaVersionAtLeast(17): " + SystemUtils.isJavaVersionAtLeast(17));
        System.out.println("  isJavaVersionAtLeast(21): " + SystemUtils.isJavaVersionAtLeast(21));
        System.out.println("  isJavaVersionAtLeast(25): " + SystemUtils.isJavaVersionAtLeast(25));
        
        // 1.5 属性访问方法
        System.out.println("\n▶ 属性访问方法:");
        System.out.println("  getUserHome(): " + SystemUtils.getUserHome());
        System.out.println("  getUserDir(): " + SystemUtils.getUserDir());
        System.out.println("  getUserName(): " + SystemUtils.getUserName());
        System.out.println("  getOsName(): " + SystemUtils.getOsName());
        System.out.println("  getOsArch(): " + SystemUtils.getOsArch());
        System.out.println("  getOsVersion(): " + SystemUtils.getOsVersion());
        System.out.println("  getJavaVersion(): " + SystemUtils.getJavaVersion());
        System.out.println("  getJavaHome(): " + SystemUtils.getJavaHome());
        System.out.println("  getTempDir(): " + SystemUtils.getTempDir());
        System.out.println("  getFileSeparator(): '" + SystemUtils.getFileSeparator() + "'");
        System.out.println("  getPathSeparator(): '" + SystemUtils.getPathSeparator() + "'");
        System.out.println("  getLineSeparator(): '" + 
            SystemUtils.getLineSeparator().replace("\r", "\\r").replace("\n", "\\n") + "'");
        
        // 1.6 系统属性获取
        System.out.println("\n▶ 系统属性获取:");
        System.out.println("  getSystemProperty(\"java.specification.version\"): " + 
            SystemUtils.getSystemProperty("java.specification.version"));
        System.out.println("  getSystemProperty(\"nonexistent.property\", \"default\"): " + 
            SystemUtils.getSystemProperty("nonexistent.property", "default"));
        
        // 1.7 系统信息摘要
        System.out.println("\n▶ 系统信息摘要:");
        System.out.println(SystemUtils.getSystemInfo());
    }
    
    /**
     * 演示应用路径方法
     */
    private static void demoAppPaths() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【Paths - 应用路径】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        System.out.println("\n▶ 应用路径（基于标识符: " + Paths.getIdentifier() + "）:");
        System.out.println("  app(): " + Paths.app());
        System.out.println("  appData(): " + Paths.appData());
        System.out.println("  appConfig(): " + Paths.appConfig());
        System.out.println("  appCache(): " + Paths.appCache());
        System.out.println("  appLog(): " + Paths.appLog());
        System.out.println("  appLocalData(): " + Paths.appLocalData());
        
        // 演示目录创建
        System.out.println("\n▶ 目录创建演示:");
        boolean created = Paths.ensureAppDirectoriesQuietly();
        System.out.println("  ensureAppDirectoriesQuietly(): " + (created ? "成功" : "失败"));
        
        // 验证目录是否存在
        System.out.println("\n▶ 验证目录是否存在:");
        System.out.println("  exists(app()): " + Paths.exists(Paths.app()));
        System.out.println("  exists(appData()): " + Paths.exists(Paths.appData()));
        System.out.println("  exists(appConfig()): " + Paths.exists(Paths.appConfig()));
        System.out.println("  exists(appCache()): " + Paths.exists(Paths.appCache()));
        System.out.println("  exists(appLog()): " + Paths.exists(Paths.appLog()));
    }
    
    /**
     * 演示系统路径方法
     */
    private static void demoSystemPaths() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【Paths - 系统路径】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        System.out.println("\n▶ 系统路径:");
        System.out.println("  home(): " + Paths.home());
        System.out.println("  cache(): " + Paths.cache());
        System.out.println("  config(): " + Paths.config());
        System.out.println("  data(): " + Paths.data());
        System.out.println("  documents(): " + Paths.documents());
        System.out.println("  executable(): " + Paths.executable());
        System.out.println("  localData(): " + Paths.localData());
        System.out.println("  pub(): " + Paths.pub());
        System.out.println("  resource(): " + Paths.resource());
        System.out.println("  runtime(): " + Paths.runtime());
        System.out.println("  temp(): " + Paths.temp());
        System.out.println("  template(): " + Paths.template());
        
        // 验证系统路径
        System.out.println("\n▶ 验证系统路径是否存在:");
        System.out.println("  exists(home()): " + Paths.exists(Paths.home()));
        System.out.println("  exists(documents()): " + Paths.exists(Paths.documents()));
        System.out.println("  isDirectory(home()): " + Paths.isDirectory(Paths.home()));
        System.out.println("  isDirectory(documents()): " + Paths.isDirectory(Paths.documents()));
    }
    
    /**
     * 演示用户目录方法
     */
    private static void demoUserDirectories() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【Paths - 用户目录】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        System.out.println("\n▶ 用户目录:");
        System.out.println("  desktop(): " + Paths.desktop());
        System.out.println("  pictures(): " + Paths.pictures());
        System.out.println("  downloads(): " + Paths.downloads());
        System.out.println("  music(): " + Paths.music());
        System.out.println("  videos(): " + Paths.videos());
        
        // 验证用户目录
        System.out.println("\n▶ 验证用户目录是否存在:");
        System.out.println("  exists(desktop()): " + Paths.exists(Paths.desktop()));
        System.out.println("  exists(downloads()): " + Paths.exists(Paths.downloads()));
        System.out.println("  isDirectory(desktop()): " + Paths.isDirectory(Paths.desktop()));
    }
    
    /**
     * 演示路径操作工具方法
     */
    private static void demoPathOperations() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【Paths - 路径操作工具】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // join 方法
        System.out.println("\n▶ join() 路径拼接:");
        String joined1 = Paths.join("home", "user", "documents", "file.txt");
        System.out.println("  join(\"home\", \"user\", \"documents\", \"file.txt\"): " + joined1);
        
        String joined2 = Paths.join("C:", "Users", "test", "Desktop");
        System.out.println("  join(\"C:\", \"Users\", \"test\", \"Desktop\"): " + joined2);
        
        // normalize 方法
        System.out.println("\n▶ normalize() 路径规范化:");
        String normalized1 = Paths.normalize("/home/user/../test/./file.txt");
        System.out.println("  normalize(\"/home/user/../test/./file.txt\"): " + normalized1);
        
        String normalized2 = Paths.normalize("C:\\Users\\test\\..\\test2");
        System.out.println("  normalize(\"C:\\Users\\test\\..\\test2\"): " + normalized2);
        
        String normalized3 = Paths.normalize("./relative/path/../file.txt");
        System.out.println("  normalize(\"./relative/path/../file.txt\"): " + normalized3);
        
        // toAbsolutePath 方法
        System.out.println("\n▶ toAbsolutePath() 转换为绝对路径:");
        String absPath = Paths.toAbsolutePath("relative/path/file.txt");
        System.out.println("  toAbsolutePath(\"relative/path/file.txt\"): " + absPath);
        
        // 文件名操作
        System.out.println("\n▶ 文件名操作:");
        String testPath = "/home/user/documents/report.pdf";
        System.out.println("  测试路径: " + testPath);
        System.out.println("  getFileName(): " + Paths.getFileName(testPath));
        System.out.println("  getBaseName(): " + Paths.getBaseName(testPath));
        System.out.println("  getExtension(): " + Paths.getExtension(testPath));
        System.out.println("  getParent(): " + Paths.getParent(testPath));
        
        // 更多文件名测试
        System.out.println("\n  更多测试:");
        String[] testPaths = {
            "file.txt",
            "/path/to/archive.tar.gz",
            "no_extension",
            ".hidden",
            "C:\\Windows\\System32\\notepad.exe"
        };
        for (String path : testPaths) {
            System.out.println("    路径: " + path);
            System.out.println("      getFileName: " + Paths.getFileName(path));
            System.out.println("      getBaseName: " + Paths.getBaseName(path));
            System.out.println("      getExtension: " + Paths.getExtension(path));
        }
        
        // createDirectories 演示
        System.out.println("\n▶ createDirectories() 创建目录:");
        String testDir = Paths.join(Paths.temp(), "aurora-fx-demo", "test", "nested");
        System.out.println("  创建目录: " + testDir);
        try {
            java.nio.file.Path created = Paths.createDirectories(testDir);
            System.out.println("  创建成功: " + created);
            System.out.println("  exists(): " + Paths.exists(testDir));
            System.out.println("  isDirectory(): " + Paths.isDirectory(testDir));
            
            // 清理测试目录
            Files.deleteIfExists(created);
            Files.deleteIfExists(created.getParent());
            Files.deleteIfExists(created.getParent().getParent());
            System.out.println("  测试目录已清理");
        } catch (IOException e) {
            System.out.println("  创建失败: " + e.getMessage());
        }
        
        // createDirectoriesQuietly 演示
        System.out.println("\n▶ createDirectoriesQuietly() 静默创建:");
        String quietDir = Paths.join(Paths.temp(), "aurora-fx-demo-quiet", "subdir");
        String result = Paths.createDirectoriesQuietly(quietDir);
        System.out.println("  创建目录: " + quietDir);
        System.out.println("  结果: " + (result != null ? "成功" : "失败"));
        if (result != null) {
            try {
                Files.deleteIfExists(java.nio.file.Paths.get(quietDir));
                Files.deleteIfExists(java.nio.file.Paths.get(quietDir).getParent());
            } catch (IOException e) {
                // 忽略清理错误
            }
        }
    }
    
    /**
     * 演示实际使用场景
     */
    private static void demoRealWorldScenarios() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【实际使用场景演示】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 场景 1: 应用初始化
        System.out.println("\n▶ 场景 1: 应用初始化");
        System.out.println("  代码示例:");
        System.out.println("    Paths.identifier(\"com.example.myapp\");");
        System.out.println("    Paths.ensureAppDirectoriesQuietly();");
        System.out.println("  实际路径:");
        System.out.println("    数据目录: " + Paths.appData());
        System.out.println("    配置目录: " + Paths.appConfig());
        System.out.println("    日志目录: " + Paths.appLog());
        
        // 场景 2: 跨平台配置文件路径
        System.out.println("\n▶ 场景 2: 跨平台配置文件路径");
        String configFile = Paths.join(Paths.appConfig(), "settings.json");
        System.out.println("  配置文件路径: " + configFile);
        
        // 场景 3: 导出到下载目录
        System.out.println("\n▶ 场景 3: 导出文件到下载目录");
        String exportFile = Paths.join(Paths.downloads(), "export_" + System.currentTimeMillis() + ".txt");
        System.out.println("  导出文件路径: " + exportFile);
        
        // 场景 4: 缓存文件管理
        System.out.println("\n▶ 场景 4: 缓存文件管理");
        String cacheFile = Paths.join(Paths.appCache(), "images", "thumbnail.png");
        System.out.println("  缓存文件路径: " + cacheFile);
        System.out.println("  规范化后: " + Paths.normalize(cacheFile));
        
        // 场景 5: 根据操作系统选择不同策略
        System.out.println("\n▶ 场景 5: 根据操作系统选择策略");
        if (SystemUtils.isWindows()) {
            System.out.println("  Windows 系统: 使用注册表存储额外配置");
        } else if (SystemUtils.isMac()) {
            System.out.println("  macOS 系统: 使用 plist 文件存储配置");
        } else if (SystemUtils.isLinux()) {
            System.out.println("  Linux 系统: 使用 XDG 规范存储配置");
        }
        
        // 场景 6: Java 版本特性检测
        System.out.println("\n▶ 场景 6: Java 版本特性检测");
        if (SystemUtils.isJavaVersionAtLeast(21)) {
            System.out.println("  Java 21+: 可以使用虚拟线程 (Virtual Threads)");
        } else if (SystemUtils.isJavaVersionAtLeast(17)) {
            System.out.println("  Java 17+: 可以使用密封类 (Sealed Classes)");
        } else {
            System.out.println("  Java 8-16: 使用传统线程模型");
        }
        
        // 场景 7: 路径存在性检查
        System.out.println("\n▶ 场景 7: 路径存在性检查");
        String[] checkPaths = {
            Paths.home(),
            Paths.desktop(),
            "/nonexistent/path/xyz",
            Paths.join(Paths.temp(), "probably_not_exists_12345")
        };
        for (String path : checkPaths) {
            System.out.println("  " + path + ": " + 
                (Paths.exists(path) ? "存在" : "不存在") + 
                (Paths.isDirectory(path) ? " [目录]" : "") +
                (Paths.isFile(path) ? " [文件]" : ""));
        }
    }
}
