# Aurora-FX Path 工具模块 — 完整 API 文档

> 版本: 0.0.1 | 最后更新: 2025 年  
> 提供跨平台的系统路径和应用路径管理功能，支持 Windows、macOS、Linux 三大平台

---

## 目录

1. [模块概览](#1-模块概览)
2. [快速开始](#2-快速开始)
3. [核心类 — SystemUtils](#3-核心类--systemutils)
   - [系统属性常量](#31-系统属性常量)
   - [操作系统检测方法](#32-操作系统检测方法)
   - [Java 版本检测方法](#33-java-版本检测方法)
   - [属性访问方法](#34-属性访问方法)
4. [核心类 — Paths](#4-核心类--paths)
   - [初始化与标识符管理](#41-初始化与标识符管理)
   - [应用路径方法](#42-应用路径方法)
   - [系统路径方法](#43-系统路径方法)
   - [用户目录方法](#44-用户目录方法)
   - [路径操作工具方法](#45-路径操作工具方法)
5. [跨平台路径映射表](#5-跨平台路径映射表)
6. [类间关系图](#6-类间关系图)
7. [使用场景与最佳实践](#7-使用场景与最佳实践)
8. [线程安全性说明](#8-线程安全性说明)
9. [常见问题 FAQ](#9-常见问题-faq)

---

## 1. 模块概览

```
┌─────────────────────────────────────────────────────────────┐
│                    io.aurora.fx.common.path                 │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐        ┌─────────────────────────────┐ │
│  │   SystemUtils   │        │           Paths             │ │
│  │   (系统工具类)   │◄───────│       (路径管理类)           │ │
│  ├─────────────────┤        ├─────────────────────────────┤ │
│  │ - OS 检测       │        │ - 应用路径 (app, appData)   │ │
│  │ - Java 版本检测 │        │ - 系统路径 (home, temp)     │ │
│  │ - 系统属性访问  │        │ - 用户目录 (desktop, docs)  │ │
│  │ - 架构检测      │        │ - 路径操作 (join, normalize)│ │
│  └─────────────────┘        └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 设计原则

- **跨平台兼容**: 所有路径自动适配 Windows、macOS、Linux
- **懒加载**: 路径值延迟初始化，仅在首次访问时计算
- **线程安全**: 使用 `AtomicReference` 保证多线程环境下的安全性
- **不可实例化**: 所有类都是工具类，通过静态方法访问
- **标准规范**: 遵循各操作系统的路径约定（XDG、Apple 规范等）

---

## 2. 快速开始

### 基础使用

```java
import io.aurora.fx.common.path.Paths;
import io.aurora.fx.common.path.SystemUtils;

// 检测操作系统
if (SystemUtils.isWindows()) {
    System.out.println("运行在 Windows 系统");
} else if (SystemUtils.isMac()) {
    System.out.println("运行在 macOS 系统");
} else if (SystemUtils.isLinux()) {
    System.out.println("运行在 Linux 系统");
}

// 获取系统路径（无需初始化）
String home = Paths.home();           // 用户主目录
String temp = Paths.temp();           // 临时目录
String desktop = Paths.desktop();     // 桌面目录
```

### 应用路径使用

```java
// 初始化应用标识符（必须在使用应用路径前调用）
Paths.identifier("io.aurora.fx");

// 获取应用专属目录
String appDir = Paths.app();          // 应用主目录
String appData = Paths.appData();     // 应用数据目录
String appConfig = Paths.appConfig(); // 应用配置目录
String appCache = Paths.appCache();   // 应用缓存目录
String appLog = Paths.appLog();       // 应用日志目录

// 确保目录存在
Paths.ensureAppDirectoriesQuietly();
```

### 路径操作

```java
// 路径拼接
String path = Paths.join("C:", "Users", "test", "Documents");
// Windows: "C:\Users\test\Documents"
// macOS/Linux: "C:/Users/test/Documents"

// 路径规范化
String normalized = Paths.normalize("/home/user/../test/./file.txt");
// 结果: "/home/test/file.txt"

// 检查路径
boolean exists = Paths.exists("/path/to/dir");
boolean isDir = Paths.isDirectory("/path/to/dir");
boolean isFile = Paths.isFile("/path/to/file.txt");

// 创建目录
Paths.createDirectories("/path/to/new/dir");
```

---

## 3. 核心类 — SystemUtils

`io.aurora.fx.common.path.SystemUtils`

系统工具类，提供跨平台的系统属性检测和访问功能。所有属性在类加载时初始化。

### 3.1 系统属性常量

| 常量 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| `USER_HOME` | `String` | 用户主目录 | `C:\Users\john` / `/home/john` |
| `USER_DIR` | `String` | 当前工作目录 | `C:\project` |
| `USER_NAME` | `String` | 当前用户名 | `john` |
| `OS_NAME` | `String` | 操作系统名称 | `Windows 11` / `Mac OS X` / `Linux` |
| `OS_ARCH` | `String` | 操作系统架构 | `amd64` / `aarch64` |
| `OS_VERSION` | `String` | 操作系统版本 | `10.0` / `14.0` |
| `FILE_SEPARATOR` | `String` | 文件分隔符 | `\` (Win) / `/` (Unix) |
| `PATH_SEPARATOR` | `String` | 路径分隔符 | `;` (Win) / `:` (Unix) |
| `LINE_SEPARATOR` | `String` | 行分隔符 | `\r\n` (Win) / `\n` (Unix) |
| `JAVA_IO_TMPDIR` | `String` | 临时目录 | `C:\Users\...\Temp` / `/tmp` |
| `JAVA_VERSION` | `String` | Java 版本 | `21.0.1` |
| `JAVA_HOME` | `String` | Java 主目录 | `C:\Program Files\Java\jdk-21` |

### 3.2 操作系统检测方法

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `isWindows()` | `boolean` | 判断是否为 Windows 系统 |
| `isMac()` | `boolean` | 判断是否为 macOS 系统 |
| `isLinux()` | `boolean` | 判断是否为 Linux 系统 |
| `isUnix()` | `boolean` | 判断是否为 Unix-like 系统（含 macOS、Linux、BSD 等） |
| `getOsType()` | `String` | 获取操作系统类型名：`"Windows"` / `"macOS"` / `"Linux"` / `"Unknown"` |

```java
// 示例：跨平台逻辑
if (SystemUtils.isWindows()) {
    // Windows 特定逻辑
    String programFiles = SystemUtils.getUserHome().substring(0, 2) + "\\Program Files";
} else if (SystemUtils.isMac()) {
    // macOS 特定逻辑
    String appSupport = Paths.join(SystemUtils.USER_HOME, "Library", "Application Support");
} else if (SystemUtils.isLinux()) {
    // Linux 特定逻辑（遵循 XDG 规范）
}
```

### 3.3 Java 版本检测方法

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getJavaMajorVersion()` | `int` | 获取 Java 主版本号（如 21、17、8） |
| `isJavaVersionAtLeast(int)` | `boolean` | 判断 Java 版本是否大于等于指定版本 |

```java
// 示例：版本检测
if (SystemUtils.isJavaVersionAtLeast(21)) {
    // 使用 Java 21+ 特性（如虚拟线程）
    Thread.startVirtualThread(() -> { /* ... */ });
} else {
    // 降级处理
    new Thread(() -> { /* ... */ }).start();
}

System.out.println("Java 主版本: " + SystemUtils.getJavaMajorVersion());
// 输出: Java 主版本: 21
```

### 3.4 属性访问方法

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getUserHome()` | `String` | 获取用户主目录 |
| `getUserDir()` | `String` | 获取当前工作目录 |
| `getUserName()` | `String` | 获取当前用户名 |
| `getOsName()` | `String` | 获取操作系统名称 |
| `getOsArch()` | `String` | 获取操作系统架构 |
| `getOsVersion()` | `String` | 获取操作系统版本 |
| `getJavaVersion()` | `String` | 获取 Java 版本 |
| `getJavaHome()` | `String` | 获取 Java 主目录 |
| `getTempDir()` | `String` | 获取临时目录 |
| `getFileSeparator()` | `String` | 获取文件分隔符 |
| `getPathSeparator()` | `String` | 获取路径分隔符 |
| `getLineSeparator()` | `String` | 获取行分隔符 |
| `getSystemProperty(String key)` | `String` | 获取系统属性 |
| `getSystemProperty(String key, String default)` | `String` | 获取系统属性（带默认值） |
| `getSystemInfo()` | `String` | 获取系统信息摘要 |

---

## 4. 核心类 — Paths

`io.aurora.fx.common.path.Paths`

跨平台路径管理类，提供统一的系统路径和应用路径访问接口。

### 4.1 初始化与标识符管理

#### `identifier(String identifier)`

设置应用标识符并初始化应用路径。

**参数**:
- `identifier`: 应用标识符，通常使用反向域名格式（如 `com.example.myapp`）

**异常**:
- `NullPointerException`: 如果 identifier 为 null
- `IllegalArgumentException`: 如果 identifier 为空字符串

```java
// 设置应用标识符（通常在应用启动时调用）
Paths.identifier("io.aurora.fx");

// 也可以通过 JVM 参数设置
// java -Djavafx.identifier=io.aurora.fx MyApp
```

#### `getIdentifier()`

获取当前应用标识符。

**返回值**: 当前应用标识符，未设置时返回 `null`

#### `reset()`

重置所有路径缓存。主要用于测试场景。

```java
// 测试场景：每个测试用例前重置
@BeforeEach
void setUp() {
    Paths.reset();
    Paths.identifier("com.test.app");
}
```

### 4.2 应用路径方法

使用应用路径方法前，必须先调用 `identifier()` 设置应用标识符。

#### 跨平台应用路径映射

| 方法 | 说明 | Windows | macOS | Linux |
|------|------|---------|-------|-------|
| `app()` | 应用主目录 | `%LOCALAPPDATA%\{id}` | `~/Library/Application Support/{id}` | `~/.local/share/{id}` |
| `appData()` | 应用数据目录 | `%APPDATA%\{id}\Data` | `~/Library/Application Support/{id}` | `~/.local/share/{id}` |
| `appConfig()` | 应用配置目录 | `%APPDATA%\{id}\Config` | `~/Library/Preferences/{id}` | `~/.config/{id}` |
| `appCache()` | 应用缓存目录 | `%LOCALAPPDATA%\{id}\Cache` | `~/Library/Caches/{id}` | `~/.cache/{id}` |
| `appLog()` | 应用日志目录 | `%LOCALAPPDATA%\{id}\Logs` | `~/Library/Logs/{id}` | `~/.local/share/{id}/logs` |
| `appLocalData()` | 本地数据目录 | `%LOCALAPPDATA%\{id}\Data` | `~/Library/Application Support/{id}` | `~/.local/share/{id}` |

**使用示例**:

```java
// 初始化
Paths.identifier("com.example.myapp");

// 获取路径
String appDir = Paths.app();
String dataDir = Paths.appData();
String configDir = Paths.appConfig();
String cacheDir = Paths.appCache();
String logDir = Paths.appLog();

// 确保目录存在
try {
    Paths.ensureAppDirectories();
} catch (IOException e) {
    e.printStackTrace();
}

// 或静默版本（失败不抛异常）
Paths.ensureAppDirectoriesQuietly();
```

### 4.3 系统路径方法

系统路径方法无需初始化标识符即可使用。

| 方法 | 说明 | Windows | macOS | Linux |
|------|------|---------|-------|-------|
| `home()` | 用户主目录 | `C:\Users\{user}` | `/Users/{user}` | `/home/{user}` |
| `cache()` | 系统缓存目录 | `%TEMP%` | `~/Library/Caches` | `~/.cache` |
| `config()` | 系统配置目录 | `%APPDATA%` | `~/Library/Preferences` | `~/.config` |
| `data()` | 系统数据目录 | `%APPDATA%` | `~/Library/Application Support` | `~/.local/share` |
| `documents()` | 文档目录 | `~/Documents` | `~/Documents` | `~/Documents` |
| `executable()` | 可执行文件目录 | `C:\Windows\System32` | `/usr/bin` | `/usr/bin` |
| `localData()` | 本地数据目录 | `%LOCALAPPDATA%` | `~/Library/Application Support` | `~/.local/share` |
| `pub()` | 公共目录 | `C:\Users\Public` | `/Users/Shared` | `/usr/share` |
| `resource()` | 资源目录 | `C:\Windows\Resources` | `/System/Library` | `/usr/share` |
| `runtime()` | 运行时目录 | `C:\Windows\Temp` | `/tmp` | `/run/user/{uid}` |
| `temp()` | 临时目录 | `C:\Windows\Temp` | `/tmp` | `/tmp` |
| `template()` | 模板目录 | `%APPDATA%\Microsoft\Templates` | `~/Templates` | `~/Templates` |

### 4.4 用户目录方法

| 方法 | 说明 | Windows | macOS | Linux |
|------|------|---------|-------|-------|
| `desktop()` | 桌面 | `~/Desktop` | `~/Desktop` | `~/Desktop` |
| `pictures()` | 图片 | `~/Pictures` | `~/Pictures` | `~/Pictures` |
| `downloads()` | 下载 | `~/Downloads` | `~/Downloads` | `~/Downloads` |
| `music()` | 音乐 | `~/Music` | `~/Music` | `~/Music` |
| `videos()` | 视频 | `~/Videos` | `~/Movies` | `~/Videos` |

```java
// 示例：获取用户常用目录
String desktop = Paths.desktop();    // 桌面
String downloads = Paths.downloads(); // 下载目录
String pictures = Paths.pictures();   // 图片目录
```

### 4.5 路径操作工具方法

#### `join(String first, String... more)`

连接多个路径组件，自动使用正确的分隔符。

**参数**:
- `first`: 第一个路径组件（不能为 null）
- `more`: 其他路径组件

**返回值**: 连接后的路径字符串

```java
String path = Paths.join("C:", "Users", "test", "Documents");
// Windows: "C:\Users\test\Documents"
// Unix: "C:/Users/test/Documents"
```

#### `normalize(String path)`

规范化路径，移除 `.` 和 `..` 等冗余部分。

**参数**:
- `path`: 要规范化的路径

**返回值**: 规范化后的路径

```java
String path = Paths.normalize("/home/user/../test/./file.txt");
// 结果: "/home/test/file.txt"
```

#### `toAbsolutePath(String path)`

将路径转换为绝对路径。

**参数**:
- `path`: 要解析的路径

**返回值**: 绝对路径字符串

```java
String abs = Paths.toAbsolutePath("subdir/file.txt");
// 结果: "/current/working/dir/subdir/file.txt"
```

#### `exists(String path)`

判断路径是否存在。

**参数**:
- `path`: 要检查的路径

**返回值**: 路径存在返回 `true`

#### `isDirectory(String path)`

判断路径是否为目录。

#### `isFile(String path)`

判断路径是否为文件。

#### `createDirectories(String path)`

创建目录（包含所有父目录），等同于 `mkdir -p`。

**异常**:
- `IOException`: 如果创建失败

```java
try {
    Paths.createDirectories(Paths.appLog());
} catch (IOException e) {
    System.err.println("创建目录失败: " + e.getMessage());
}
```

#### `createDirectoriesQuietly(String path)`

静默创建目录，失败时返回 `null` 而不抛异常。

#### `ensureAppDirectories()`

确保所有应用目录存在（主目录、数据、配置、缓存、日志）。

**异常**:
- `IOException`: 如果创建失败
- `IllegalStateException`: 如果应用标识符未设置

#### `ensureAppDirectoriesQuietly()`

静默版本，失败时返回 `false`。

#### `getExtension(String path)`

获取文件扩展名（不含点号）。

```java
String ext = Paths.getExtension("/home/user/file.txt");
// 结果: "txt"
```

#### `getBaseName(String path)`

获取文件名（不含扩展名）。

```java
String name = Paths.getBaseName("/home/user/file.txt");
// 结果: "file"
```

#### `getFileName(String path)`

获取文件名（含扩展名）。

```java
String name = Paths.getFileName("/home/user/file.txt");
// 结果: "file.txt"
```

#### `getParent(String path)`

获取父目录路径。

```java
String parent = Paths.getParent("/home/user/file.txt");
// 结果: "/home/user"
```

---

## 5. 跨平台路径映射表

### 应用路径对比

| 路径类型 | Windows | macOS | Linux |
|----------|---------|-------|-------|
| **应用主目录** | `C:\Users\{user}\AppData\Local\{id}` | `/Users/{user}/Library/Application Support/{id}` | `/home/{user}/.local/share/{id}` |
| **应用数据** | `C:\Users\{user}\AppData\Roaming\{id}\Data` | `/Users/{user}/Library/Application Support/{id}` | `/home/{user}/.local/share/{id}` |
| **应用配置** | `C:\Users\{user}\AppData\Roaming\{id}\Config` | `/Users/{user}/Library/Preferences/{id}` | `/home/{user}/.config/{id}` |
| **应用缓存** | `C:\Users\{user}\AppData\Local\{id}\Cache` | `/Users/{user}/Library/Caches/{id}` | `/home/{user}/.cache/{id}` |
| **应用日志** | `C:\Users\{user}\AppData\Local\{id}\Logs` | `/Users/{user}/Library/Logs/{id}` | `/home/{user}/.local/share/{id}/logs` |

### 系统路径对比

| 路径类型 | Windows | macOS | Linux |
|----------|---------|-------|-------|
| **用户主目录** | `C:\Users\{user}` | `/Users/{user}` | `/home/{user}` |
| **临时目录** | `C:\Windows\Temp` | `/tmp` | `/tmp` |
| **系统配置** | `C:\Users\{user}\AppData\Roaming` | `/Users/{user}/Library/Preferences` | `/home/{user}/.config` |
| **系统数据** | `C:\Users\{user}\AppData\Roaming` | `/Users/{user}/Library/Application Support` | `/home/{user}/.local/share` |
| **公共目录** | `C:\Users\Public` | `/Users/Shared` | `/usr/share` |
| **运行时目录** | `C:\Windows\Temp` | `/tmp` | `/run/user/{uid}` |

### 用户目录对比

| 目录类型 | Windows | macOS | Linux |
|----------|---------|-------|-------|
| **桌面** | `C:\Users\{user}\Desktop` | `/Users/{user}/Desktop` | `/home/{user}/Desktop` |
| **文档** | `C:\Users\{user}\Documents` | `/Users/{user}/Documents` | `/home/{user}/Documents` |
| **下载** | `C:\Users\{user}\Downloads` | `/Users/{user}/Downloads` | `/home/{user}/Downloads` |
| **图片** | `C:\Users\{user}\Pictures` | `/Users/{user}/Pictures` | `/home/{user}/Pictures` |
| **音乐** | `C:\Users\{user}\Music` | `/Users/{user}/Music` | `/home/{user}/Music` |
| **视频** | `C:\Users\{user}\Videos` | `/Users/{user}/Movies` | `/home/{user}/Videos` |

---

## 6. 类间关系图

```
┌────────────────────────────────────────────────────────────────────┐
│                         应用程序                                    │
│                            │                                        │
│                            ▼                                        │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                     Paths.identifier("com.app")               │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                            │                                        │
│          ┌─────────────────┼─────────────────┐                     │
│          ▼                 ▼                 ▼                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │   app()      │  │  appData()   │  │  appLog()    │             │
│  │  应用主目录   │  │  应用数据    │  │  应用日志    │             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
│                            │                                        │
│                            ▼                                        │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                      SystemUtils                              │  │
│  │  ┌─────────────────┐  ┌─────────────────┐                    │  │
│  │  │   IS_OS_WINDOWS │  │  IS_OS_MAC      │                    │  │
│  │  │   IS_OS_LINUX   │  │  IS_OS_UNIX     │                    │  │
│  │  └─────────────────┘  └─────────────────┘                    │  │
│  │  ┌─────────────────────────────────────────────────────────┐ │  │
│  │  │  USER_HOME | FILE_SEPARATOR | LINE_SEPARATOR | ...      │ │  │
│  │  └─────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                            │                                        │
│                            ▼                                        │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              操作系统特定路径                                  │  │
│  │  ┌──────────────────────────────────────────────────────┐    │  │
│  │  │ Windows: %LOCALAPPDATA% | %APPDATA% | C:\Windows\... │    │  │
│  │  ├──────────────────────────────────────────────────────┤    │  │
│  │  │ macOS:   ~/Library/Application Support | ~/Library/* │    │  │
│  │  ├──────────────────────────────────────────────────────┤    │  │
│  │  │ Linux:   ~/.local/share | ~/.config | ~/.cache       │    │  │
│  │  └──────────────────────────────────────────────────────┘    │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
```

---

## 7. 使用场景与最佳实践

### 场景一：应用初始化

```java
public class Application {
    public void start() {
        // 1. 设置应用标识符
        Paths.identifier("io.aurora.fx");
        
        // 2. 确保所有必要目录存在
        if (!Paths.ensureAppDirectoriesQuietly()) {
            throw new RuntimeException("无法创建应用目录");
        }
        
        // 3. 加载配置
        loadConfig();
        
        // 4. 初始化日志
        initLogger();
    }
    
    private void loadConfig() {
        String configPath = Paths.join(Paths.appConfig(), "settings.json");
        // 加载配置文件...
    }
    
    private void initLogger() {
        String logPath = Paths.appLog();
        // 配置日志输出到 logPath...
    }
}
```

### 场景二：跨平台文件存储

```java
public class FileStorage {
    private final String storagePath;
    
    public FileStorage(String subdir) {
        // 使用应用缓存目录存储可重建的数据
        this.storagePath = Paths.join(Paths.appCache(), subdir);
        Paths.createDirectoriesQuietly(storagePath);
    }
    
    public void saveCache(String key, byte[] data) {
        String filePath = Paths.join(storagePath, key + ".cache");
        Files.write(Paths.get(filePath), data);
    }
    
    public byte[] loadCache(String key) {
        String filePath = Paths.join(storagePath, key + ".cache");
        if (Paths.exists(filePath)) {
            return Files.readAllBytes(Paths.get(filePath));
        }
        return null;
    }
}
```

### 场景三：导出文件到用户目录

```java
public class ExportService {
    public void exportToDownloads(String filename, byte[] content) {
        String downloadsDir = Paths.downloads();
        String filePath = Paths.join(downloadsDir, filename);
        
        // 确保不覆盖已存在的文件
        if (Paths.exists(filePath)) {
            String baseName = Paths.getBaseName(filename);
            String ext = Paths.getExtension(filename);
            filePath = Paths.join(downloadsDir, 
                baseName + "_" + System.currentTimeMillis() + "." + ext);
        }
        
        Files.write(java.nio.file.Paths.get(filePath), content);
    }
}
```

## 8. 常见问题 FAQ

### Q: 为什么 `appConfig()` 在 Windows 下返回的路径和 `appCache()` 不同？

A: 这是遵循操作系统规范：
- **Windows**: 配置文件应存储在 `%APPDATA%`（Roaming 目录），缓存应存储在 `%LOCALAPPDATA%`
- Roaming 目录的数据会在域环境中漫游到其他计算机，而 Local 目录的数据仅在本机有效

### Q: 如何检测应用标识符是否已设置？

A: 使用 `Paths.getIdentifier()` 检查：

```java
if (Paths.getIdentifier() == null) {
    Paths.identifier("com.example.myapp");
}
```

### Q: 路径不存在时会自动创建吗？

A: 不会自动创建。需要显式调用创建方法：
- `createDirectories(path)` - 创建目录，失败抛异常
- `createDirectoriesQuietly(path)` - 静默创建
- `ensureAppDirectories()` - 创建所有应用目录

### Q: 如何在单元测试中重置路径？

A: 使用 `reset()` 方法：

```java
@BeforeEach
void setUp() {
    Paths.reset();
    Paths.identifier("com.test.app");
}

@AfterEach
void tearDown() {
    Paths.reset();
}
```

### Q: Linux 下 `runtime()` 方法为什么需要调用 `id -u` 命令？

A: Linux 的运行时目录路径为 `/run/user/{uid}`，需要获取当前用户的 UID。结果会被缓存，只会执行一次。

### Q: 如何处理路径中的中文或特殊字符？

A: 路径类内部使用 Java NIO 的 `Paths.get()` 方法，自动处理 Unicode 字符。无需额外处理。
