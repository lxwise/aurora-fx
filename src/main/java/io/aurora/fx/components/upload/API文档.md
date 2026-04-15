# Aurora-FX Upload 组件库 — 完整 API 文档

> 版本: 0.0.1 | 最后更新: 2025 年  
> 对标 Element UI Upload 组件，提供 JavaFX 原生的文件上传解决方案

---

## 目录

1. [架构概览](#1-架构概览)
2. [快速开始](#2-快速开始)
3. [核心组件 — FileUploader](#3-核心组件--fileuploader)
4. [独立 Node 组件](#4-独立-node-组件)
   - [BasicUploadNode — 基础上传](#41-basicuploadnode)
   - [PictureCardUploadNode — 照片墙](#42-picturecarduploadnode)
   - [DragUploadNode — 拖拽上传](#43-draguploadnode)
   - [PictureListUploadNode — 图片列表](#44-picturelistuploadnode)
   - [AvatarUploadNode — 头像上传+裁切](#45-avataruploadnode)
   - [LimitUploadNode — 数量限制上传](#46-limituploadnode)
   - [ManualUploadNode — 手动上传](#47-manualuploadnode)
   - [FolderUploadNode — 文件夹上传](#48-folderuploadnode)
5. [工厂类 — FileUploaderFactory](#5-工厂类--fileuploaderfactory)
6. [数据模型](#6-数据模型)
   - [UploadFile — 文件模型](#61-uploadfile)
   - [UploadFileStatus — 状态枚举](#62-uploadfilestatus)
   - [ListType — 列表类型枚举](#63-listtype)
7. [上传处理器](#7-上传处理器)
   - [FileUploadHandler — 接口定义](#71-fileuploadhandler)
   - [HttpFileUploadHandler — HTTP 实现](#72-httpfileuploadhandler)
8. [图像裁切 — ImageCropPane](#8-图像裁切--imagecroppane)
9. [工具类 — UploadUtils](#9-工具类--uploadutils)
10. [错误处理与异常](#10-错误处理与异常)
11. [性能优化与最佳实践](#11-性能优化与最佳实践)
12. [样式定制参考](#12-样式定制参考)
    - [FileUploader 上传组件样式](#121-fileuploader-上传组件样式)
    - [ImageCropPane 裁切组件样式](#122-imagecroppane-裁切组件样式)
    - [ImageViewer 图片查看器样式](#123-imageviewer-图片查看器样式)
    - [VerifyCode 验证码组件样式](#124-verifycode-验证码组件样式)
    - [全局样式覆盖最佳实践](#125-全局样式覆盖最佳实践)
13. [组件间关系图](#13-组件间关系图)
14. [常见问题 FAQ](#14-常见问题-faq)

---

## 1. 架构概览

```
┌─────────────────────────────────────────────┐
│         独立 Node 组件（开箱即用）             │
│  BasicUploadNode / PictureCardUploadNode    │
│  DragUploadNode  / PictureListUploadNode    │
│  AvatarUploadNode / LimitUploadNode         │
│  ManualUploadNode / FolderUploadNode        │
├────────────────┬────────────────────────────┤
│                │  FileUploaderFactory        │
│                │  （统一创建入口）              │
├────────────────┴────────────────────────────┤
│           FileUploader (Control)            │
│           FileUploaderSkin (SkinBase)        │
├─────────────────────────────────────────────┤
│  UploadFile │ FileUploadHandler│ImageCropPane│
│  (数据模型)  │ (上传处理器接口) │ (图像裁切)    │
└─────────────────────────────────────────────┘
```

### 设计原则

- **Control/Skin 分离**: `FileUploader` 继承 `Control`，UI 渲染由 `FileUploaderSkin` 负责
- **代理模式**: 每个 Node 组件内部组合 `FileUploader`，代理常用属性和回调
- **工厂模式**: `FileUploaderFactory` 提供统一创建入口
- **线程安全**: 上传线程池 + `ConcurrentHashMap` 管理活跃任务
- **内存安全**: Skin 层跟踪并清理所有 per-file 监听器
- **节流优化**: 同一帧内多次状态变化合并为一次 UI 重建

---

## 2. 快速开始

### 方式一：直接使用 Node 组件（推荐）

```java
// 零配置 — 本地模式（无需服务器）
BasicUploadNode node = new BasicUploadNode();
root.getChildren().add(node);

// 设置上传地址
node.setAction("http://localhost:8080/upload");
node.setOnSuccess((file, resp) -> System.out.println("成功: " + file.getName()));
```

### 方式二：通过工厂类

```java
BasicUploadNode node = FileUploaderFactory.createBasic("http://localhost:8080/upload");
root.getChildren().add(node);
```

### 方式三：快速集成到容器

```java
FileUploaderFactory.integrateBasic(container, "http://api.example.com/upload",
    (file, resp) -> System.out.println("上传成功"));
```

### 方式四：直接使用核心 FileUploader

```java
FileUploader uploader = new FileUploader();
uploader.setAction("http://localhost:8080/upload");
uploader.setListType(ListType.PICTURE_CARD);
uploader.setLimit(5);
uploader.setAccept("image/*");
uploader.setOnSuccess((file, response) -> System.out.println("成功"));
root.getChildren().add(uploader);
```

---

## 3. 核心组件 — FileUploader

`io.aurora.fx.components.upload.FileUploader extends Control`

### 3.1 构造函数

| 构造函数 | 说明 |
|---------|------|
| `FileUploader()` | 默认构造，最大并发上传数 = 3 |
| `FileUploader(int maxConcurrentUploads)` | 自定义并发数（最小为 1） |

### 3.2 属性一览

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `action` | `StringProperty` | `""` | 上传接口地址。为空时进入本地模式 |
| `headers` | `ObjectProperty<Map<String,String>>` | `Map.of()` | HTTP 请求头 |
| `multiple` | `BooleanProperty` | `false` | 是否允许多选文件 |
| `data` | `ObjectProperty<Map<String,String>>` | `Map.of()` | 附加表单参数 |
| `name` | `StringProperty` | `"file"` | 上传文件的字段名 |
| `withCredentials` | `BooleanProperty` | `false` | 是否携带 Cookie |
| `showFileList` | `BooleanProperty` | `true` | 是否显示文件列表 |
| `drag` | `BooleanProperty` | `false` | 是否启用拖拽上传模式 |
| `accept` | `StringProperty` | `""` | 文件类型过滤。支持格式：`"image/*"`, `".jpg,.png"`, `"image/jpeg"` |
| `listType` | `ObjectProperty<ListType>` | `TEXT` | 文件列表展示模式 |
| `autoUpload` | `BooleanProperty` | `true` | 选择文件后是否自动上传 |
| `uploaderDisabled` | `BooleanProperty` | `false` | 是否禁用组件 |
| `limit` | `IntegerProperty` | `0` | 文件数量限制（0=无限制） |
| `fileList` | `ListProperty<UploadFile>` | 空列表 | 当前文件列表（可观察） |
| `directory` | `BooleanProperty` | `false` | 是否启用文件夹选择模式 |
| `tip` | `StringProperty` | `""` | 提示文本 |
| `buttonText` | `StringProperty` | `"点击上传"` | 上传按钮文本 |
| `dragText` | `StringProperty` | `"将文件拖到此处，或点击上传"` | 拖拽区域提示文本 |
| `thumbnailSize` | `DoubleProperty` | `148` | 缩略图/卡片尺寸（px） |

### 3.3 回调函数

| 回调 | 类型 | 说明 |
|------|------|------|
| `httpRequest` | `ObjectProperty<FileUploadHandler>` | 自定义上传处理器（默认 `HttpFileUploadHandler`） |
| `beforeUpload` | `Function<UploadFile, Boolean>` | 上传前校验，返回 `false` 阻止上传 |
| `beforeRemove` | `Function<UploadFile, Boolean>` | 删除前校验，返回 `false` 阻止删除 |
| `onPreview` | `Consumer<UploadFile>` | 预览回调（默认用内置 ImageViewer） |
| `onRemove` | `BiConsumer<UploadFile, ObservableList<UploadFile>>` | 删除完成回调 |
| `onSuccess` | `BiConsumer<UploadFile, Object>` | 上传成功回调。第二个参数为服务器响应 |
| `onError` | `BiConsumer<UploadFile, Throwable>` | 上传失败回调 |
| `onProgress` | `BiConsumer<UploadFile, Double>` | 上传进度回调（0.0~1.0） |
| `onChange` | `BiConsumer<UploadFile, ObservableList<UploadFile>>` | 文件列表变化回调 |
| `onExceed` | `BiConsumer<List<File>, ObservableList<UploadFile>>` | 超出数量限制回调 |

### 3.4 公共方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `submit()` | `void submit()` | 手动触发上传（用于 `autoUpload=false`） |
| `clearFiles()` | `void clearFiles()` | 取消所有上传并清空文件列表 |
| `abort(UploadFile)` | `void abort(UploadFile file)` | 取消指定文件的上传 |
| `abortAll()` | `void abortAll()` | 取消所有正在进行的上传 |
| `handleStart(File)` | `void handleStart(File file)` | 手动添加文件（模拟选择） |
| `handleRemove(UploadFile)` | `void handleRemove(UploadFile file)` | 手动移除文件 |
| `dispose()` | `void dispose()` | 释放资源（取消上传 + 关闭线程池） |

### 3.5 完整使用示例

```java
FileUploader uploader = new FileUploader(5); // 最大5个并发上传
uploader.setAction("http://api.example.com/upload");
uploader.setListType(ListType.PICTURE_CARD);
uploader.setMultiple(true);
uploader.setLimit(9);
uploader.setAccept("image/*");
uploader.setThumbnailSize(120);
uploader.setTip("最多上传 9 张图片");
uploader.setHeaders(Map.of("Authorization", "Bearer xxx"));
uploader.setData(Map.of("category", "avatar"));

uploader.setBeforeUpload(file -> {
    if (file.getSize() > 5 * 1024 * 1024) {
        System.out.println("文件太大: " + file.getName());
        return false;
    }
    return true;
});

uploader.setOnSuccess((file, response) ->
    System.out.println("上传成功: " + file.getName() + " → " + response));

uploader.setOnError((file, error) ->
    System.err.println("上传失败: " + file.getName() + " → " + error.getMessage()));

uploader.setOnProgress((file, progress) ->
    System.out.printf("进度: %s %.0f%%\n", file.getName(), progress * 100));

root.getChildren().add(uploader);
```

---

## 4. 独立 Node 组件

所有 Node 组件都继承 `VBox`，内部组合 `FileUploader`，可在任意 JavaFX 项目中独立使用。

### 通用特性

- **开箱即用**: 零配置即可工作（本地模式）
- **代理 API**: 常用属性和回调通过代理方法暴露
- **高级访问**: `getUploader()` 可获取内部 `FileUploader` 进行完全控制
- **资源管理**: 所有组件提供 `dispose()` 方法释放资源
- **清空操作**: 所有组件提供 `clearFiles()` 方法

---

### 4.1 BasicUploadNode

基础按钮上传组件。

**默认配置**: TEXT 列表 | 多选 | 接受所有图片 | 按钮文字"点击上传"

```java
// 零配置
BasicUploadNode node = new BasicUploadNode();

// 带上传地址
BasicUploadNode node = new BasicUploadNode("http://localhost:8080/upload");

// 工厂方式
BasicUploadNode node = FileUploaderFactory.createBasic();
```

**代理方法一览**:

| 方法 | 说明 |
|------|------|
| `setAction(String)` / `getAction()` / `actionProperty()` | 上传地址 |
| `setAccept(String)` | 文件类型过滤 |
| `setMultiple(boolean)` | 是否多选 |
| `setTip(String)` | 提示文本 |
| `setButtonText(String)` | 按钮文字 |
| `setListType(ListType)` | 列表展示模式 |
| `setLimit(int)` | 数量限制 |
| `setOnSuccess(BiConsumer<UploadFile, Object>)` | 成功回调 |
| `setOnError(BiConsumer<UploadFile, Throwable>)` | 失败回调 |
| `setOnRemove(...)` | 删除回调 |
| `setOnProgress(...)` | 进度回调 |
| `setOnChange(...)` | 变化回调 |
| `setBeforeUpload(Function<UploadFile, Boolean>)` | 上传前校验 |
| `setBeforeRemove(Function<UploadFile, Boolean>)` | 删除前校验 |
| `clearFiles()` | 清空文件列表 |
| `dispose()` | 释放资源 |
| `getUploader()` | 获取内部 FileUploader |

---

### 4.2 PictureCardUploadNode

照片墙卡片模式组件。

**默认配置**: PICTURE_CARD 模式 | 多选 | 仅图片 | 最多 8 张 | 卡片 148×148 px

```java
PictureCardUploadNode node = new PictureCardUploadNode();
node.setLimit(6);
node.setThumbnailSize(160);
node.setAction("http://localhost:8080/upload");
```

**额外代理方法**:

| 方法 | 说明 |
|------|------|
| `setThumbnailSize(double)` | 卡片尺寸 |
| `setOnExceed(BiConsumer<List<File>, ObservableList<UploadFile>>)` | 超出限制回调 |
| `setOnPreview(Consumer<UploadFile>)` | 自定义预览回调 |

---

### 4.3 DragUploadNode

拖拽上传组件。

**默认配置**: 拖拽模式 | 多选 | 仅图片 | TEXT 列表

```java
DragUploadNode node = new DragUploadNode();
node.setDragText("拖入文件即可上传");
node.setAccept("*/*"); // 接受所有文件类型
```

**额外代理方法**:

| 方法 | 说明 |
|------|------|
| `setDragText(String)` | 拖拽区域提示文字 |

---

### 4.4 PictureListUploadNode

图片缩略图列表组件。

**默认配置**: PICTURE 列表模式 | 多选 | 仅图片 | 左侧缩略图+右侧信息

```java
PictureListUploadNode node = new PictureListUploadNode();
node.setOnPreview(file -> {
    // 自定义预览逻辑
    System.out.println("预览: " + file.getName());
});
```

---

### 4.5 AvatarUploadNode

头像上传+图像裁切组件。**这是最复杂的独立组件**。

**默认配置**: PICTURE_CARD 模式 | 限制 1 个 | 仅 JPG/PNG | 最大 2MB | 自动裁切 1:1 | 输出 200×200 | 卡片 178×178 px

**核心机制**:
1. 用户选择图片 → 格式/大小校验
2. 校验通过 → 自动弹出裁切对话框（`showAndWait` 模态阻塞）
3. 用户完成裁切 → 裁切后的文件自动添加到上传列表
4. 如果已有头像再次选择 → 通过 `onExceed` 替换旧头像并重新裁切
5. 用户取消裁切 → 不添加任何文件

```java
// 默认裁切（1:1 正方形，200×200 输出）
AvatarUploadNode node = new AvatarUploadNode();
node.setAction("http://localhost:8080/upload");

// 自定义裁切参数
AvatarUploadNode node = new AvatarUploadNode();
node.setCropAspectRatio(16.0 / 9.0);  // 16:9 宽屏比例
node.setCropOutputWidth(800);          // 输出宽度 800px
node.setCropOutputHeight(450);         // 输出高度 450px

// 禁用裁切
node.setCropEnabled(false);

// 自定义文件大小限制
node.setMaxFileSize(5 * 1024 * 1024); // 5MB

// 工厂方式
AvatarUploadNode node = FileUploaderFactory.createAvatar(
    "http://localhost:8080/upload", 1.0, 300, 300);
```

**裁切配置属性**:

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `cropAspectRatio` | `double` | `1.0` | 裁切宽高比（1.0=正方形，0=自由比例） |
| `cropOutputWidth` | `int` | `200` | 裁切输出宽度（px，0=不缩放） |
| `cropOutputHeight` | `int` | `200` | 裁切输出高度（px，0=不缩放） |
| `maxFileSize` | `long` | `2097152` (2MB) | 文件大小上限（字节） |
| `cropEnabled` | `boolean` | `true` | 是否启用裁切功能 |

**内部实现细节**:
- 使用 `skipCropIntercept` 标志避免裁切后的文件再次触发裁切对话框
- `beforeUpload` 中拦截原始文件并启动裁切，裁切完成后通过 `handleStart` 添加裁切文件
- `onExceed` 中对新文件进行独立的格式/大小校验（因为绕过了 `beforeUpload`）

---

### 4.6 LimitUploadNode

数量限制上传组件，超出限制时弹窗提示。

**默认配置**: TEXT 列表 | 多选 | 仅图片 | 限制 3 个 | 超出弹窗提示

```java
// 默认限制 3 个
LimitUploadNode node = new LimitUploadNode();

// 自定义限制
LimitUploadNode node = new LimitUploadNode(5);

// 带上传地址
LimitUploadNode node = new LimitUploadNode(5, "http://localhost:8080/upload");

// 自定义超出提示
node.setOnExceed((files, fileList) -> {
    System.out.println("超出限制！");
});
```

**额外方法**:

| 方法 | 说明 |
|------|------|
| `setLimit(int)` | 设置限制数量（同时更新提示文本） |

---

### 4.7 ManualUploadNode

手动上传组件。选择文件后不自动上传，需手动触发。

**默认配置**: autoUpload=false | 多选 | 限制 3 个 | TEXT 列表 | 显示"上传到服务器"按钮

```java
ManualUploadNode node = new ManualUploadNode();
node.setAction("http://localhost:8080/upload");

// 代码触发上传
node.submit();

// 清空已选文件
node.clearFiles();
```

**额外方法**:

| 方法 | 说明 |
|------|------|
| `submit()` | 手动触发所有 READY 状态文件的上传 |
| `clearFiles()` | 清空文件列表 |

---

### 4.8 FolderUploadNode

文件夹上传组件。选择文件夹后递归扫描所有文件。

**默认配置**: directory=true | 多选 | TEXT 列表 | 按钮"选择文件夹"

```java
FolderUploadNode node = new FolderUploadNode();
node.setAction("http://localhost:8080/upload");
node.setOnChange((file, list) -> System.out.println("共 " + list.size() + " 个文件"));
```

---

## 5. 工厂类 — FileUploaderFactory

`io.aurora.fx.components.upload.FileUploaderFactory`

所有上传组件的统一创建入口，提供三类方法：

### 5.1 create* 方法 — 创建组件实例

| 方法 | 参数 | 返回类型 |
|------|------|---------|
| `createBasic()` | 无 | `BasicUploadNode` |
| `createBasic(String action)` | 上传地址 | `BasicUploadNode` |
| `createBasic(String action, BiConsumer onSuccess)` | 上传地址+成功回调 | `BasicUploadNode` |
| `createPictureCard()` | 无 | `PictureCardUploadNode` |
| `createPictureCard(String action)` | 上传地址 | `PictureCardUploadNode` |
| `createPictureCard(String action, int limit)` | 上传地址+数量限制 | `PictureCardUploadNode` |
| `createDrag()` | 无 | `DragUploadNode` |
| `createDrag(String action)` | 上传地址 | `DragUploadNode` |
| `createPictureList()` | 无 | `PictureListUploadNode` |
| `createPictureList(String action)` | 上传地址 | `PictureListUploadNode` |
| `createAvatar()` | 无 | `AvatarUploadNode` |
| `createAvatar(String action)` | 上传地址 | `AvatarUploadNode` |
| `createAvatar(String action, double ratio, int w, int h)` | 完整裁切参数 | `AvatarUploadNode` |
| `createLimit()` | 无（默认限制3） | `LimitUploadNode` |
| `createLimit(int limit)` | 数量限制 | `LimitUploadNode` |
| `createLimit(int limit, String action)` | 限制+地址 | `LimitUploadNode` |
| `createManual()` | 无 | `ManualUploadNode` |
| `createManual(String action)` | 上传地址 | `ManualUploadNode` |
| `createFolder()` | 无 | `FolderUploadNode` |
| `createFolder(String action)` | 上传地址 | `FolderUploadNode` |

### 5.2 integrate* 方法 — 创建并添加到容器

| 方法 | 说明 |
|------|------|
| `integrateBasic(Pane, String, BiConsumer)` | 基础上传 → 添加到容器 |
| `integratePictureCard(Pane, String, int)` | 照片墙 → 添加到容器 |
| `integrateDrag(Pane, String)` | 拖拽上传 → 添加到容器 |
| `integrateAvatar(Pane, String)` | 头像上传 → 添加到容器 |

---

## 6. 数据模型

### 6.1 UploadFile

`io.aurora.fx.components.upload.UploadFile`

文件模型类，所有属性均为 JavaFX 可观察属性。

**构造函数**:

| 构造函数 | 说明 |
|---------|------|
| `UploadFile(File rawFile)` | 通过文件创建（自动设置 name/size） |
| `UploadFile(String name, String url)` | 通过 URL 创建（用于回显已上传文件，状态=SUCCESS） |

**属性一览**:

| 属性 | 类型 | 说明 |
|------|------|------|
| `uid` | `String` (只读) | UUID 唯一标识 |
| `rawFile` | `File` (只读) | 原始文件对象（URL 模式为 null） |
| `name` | `StringProperty` | 文件名 |
| `size` | `LongProperty` | 文件大小（字节） |
| `status` | `ObjectProperty<UploadFileStatus>` | 上传状态 |
| `progress` | `DoubleProperty` | 上传进度（0.0~1.0） |
| `url` | `StringProperty` | 文件 URL |
| `thumbnail` | `ObjectProperty<Image>` | 缩略图 |
| `errorMessage` | `StringProperty` | 错误信息 |
| `response` | `ObjectProperty<Object>` | 服务器响应 |

**工具方法**:

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getExtension()` | `String` | 文件扩展名（小写，不含 `.`） |
| `isImage()` | `boolean` | 是否为图片文件 |
| `getReadableSize()` | `String` | 人类可读的文件大小（如 "1.5 MB"） |

### 6.2 UploadFileStatus

```java
public enum UploadFileStatus {
    READY,      // 就绪（等待上传）
    UPLOADING,  // 上传中
    SUCCESS,    // 上传成功
    FAIL        // 上传失败
}
```

### 6.3 ListType

```java
public enum ListType {
    TEXT,          // 纯文本文件列表
    PICTURE,       // 带缩略图的文件列表
    PICTURE_CARD   // 照片墙卡片模式
}
```

---

## 7. 上传处理器

### 7.1 FileUploadHandler

`@FunctionalInterface` 上传处理器接口。

```java
public interface FileUploadHandler {
    void upload(UploadRequest request);
}
```

**UploadRequest 参数**:

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getFile()` | `File` | 待上传的文件 |
| `getAction()` | `String` | 上传 URL |
| `getFileName()` | `String` | 表单字段名（默认 "file"） |
| `getHeaders()` | `Map<String, String>` | HTTP 请求头 |
| `getData()` | `Map<String, String>` | 附加表单数据 |
| `isWithCredentials()` | `boolean` | 是否携带 Cookie |
| `getOnProgress()` | `Consumer<Double>` | 进度回调 |
| `getOnSuccess()` | `Consumer<Object>` | 成功回调 |
| `getOnError()` | `Consumer<Throwable>` | 失败回调 |

**Builder 模式创建请求**:

```java
UploadRequest request = UploadRequest.builder(file, url)
    .fileName("upload_file")
    .headers(Map.of("Auth", "token"))
    .data(Map.of("type", "avatar"))
    .onProgress(p -> System.out.println(p * 100 + "%"))
    .onSuccess(r -> System.out.println("OK"))
    .onError(e -> System.err.println(e.getMessage()))
    .build();
```

**自定义上传处理器示例**:

```java
uploader.setHttpRequest(request -> {
    // 自定义 OkHttp / Apache HttpClient 实现
    // 或本地文件拷贝逻辑
    try {
        Files.copy(request.getFile().toPath(), 
            Path.of("/uploads/" + request.getFile().getName()));
        request.getOnProgress().accept(1.0);
        request.getOnSuccess().accept("local-copy-done");
    } catch (Exception e) {
        request.getOnError().accept(e);
    }
});
```

### 7.2 HttpFileUploadHandler

基于 JDK `HttpURLConnection` 的 multipart/form-data 上传实现。

**构造函数**:

| 构造函数 | 说明 |
|---------|------|
| `HttpFileUploadHandler()` | 默认超时（连接 15s，读取 60s） |
| `HttpFileUploadHandler(int connectTimeout, int readTimeout)` | 自定义超时（毫秒） |

**超时配置**:

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `connectTimeout` | `15000` (15s) | TCP 连接超时 |
| `readTimeout` | `60000` (60s) | 响应读取超时 |

**自定义超时示例**:

```java
// 设置 30s 连接超时 + 120s 读取超时（适合大文件）
uploader.setHttpRequest(new HttpFileUploadHandler(30_000, 120_000));
```

---

## 8. 图像裁切 — ImageCropPane

`io.aurora.fx.components.upload.ImageCropPane extends BorderPane`

### 8.1 功能特性

- 固定比例裁切（默认 1:1 正方形）
- 自由比例裁切（aspectRatio=0）
- 缩放控制滑块（0.1x ~ 3.0x）
- 实时预览（使用 viewport 而非 snapshot，高性能）
- 三分线网格辅助构图
- 四角拖拽手柄调整裁切区域
- 裁切结果预览确认 → "使用此图片" / "重新裁切"
- 输出指定尺寸（自动缩放）
- 输出为临时文件，调用 `deleteOnExit()` 自动清理

### 8.2 静态便捷方法

```java
// 简易版（1:1 裁切，原始分辨率输出）
ImageCropPane.showCropDialog(imageFile, croppedFile -> {
    // croppedFile 是裁切后的临时文件
});

// 完整参数版
ImageCropPane.showCropDialog(
    imageFile,       // 源文件
    1.0,             // 宽高比（1.0=正方形，0=自由）
    200,             // 输出宽度（0=不缩放）
    200,             // 输出高度（0=不缩放）
    croppedFile -> { // 裁切完成回调
        uploader.handleStart(croppedFile);
    }
);
```

### 8.3 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `aspectRatio` | `DoubleProperty` | `1.0` | 裁切宽高比 |
| `outputWidth` | `IntegerProperty` | `0` | 输出宽度（0=原始） |
| `outputHeight` | `IntegerProperty` | `0` | 输出高度（0=原始） |
| `outputFormat` | `StringProperty` | `"png"` | 输出格式 |

### 8.4 实例方法

| 方法 | 说明 |
|------|------|
| `setImage(File)` | 设置要裁切的图片（带 null/存在性检查） |
| `setOnCropComplete(Consumer<File>)` | 裁切完成回调 |
| `setOnCancel(Runnable)` | 取消回调 |

---

## 9. 工具类 — UploadUtils

`io.aurora.fx.components.upload.UploadUtils`

| 方法 | 签名 | 说明 |
|------|------|------|
| `isImageExtension(String)` | `boolean` | 判断扩展名是否为图片 |
| `isImageFile(String)` | `boolean` | 判断文件名是否为图片 |
| `getExtension(String)` | `String` | 提取文件扩展名（小写） |
| `formatFileSize(long)` | `String` | 格式化字节数（如 "1.5 MB"） |
| `getAcceptDescription(String)` | `String` | accept 字符串转描述 |
| `parseAcceptToExtensions(String)` | `String[]` | accept 转 FileChooser 扩展名数组 |

**支持的图片扩展名**: png, jpg, jpeg, gif, bmp, webp, svg, ico, tiff, tif

---

## 10. 错误处理与异常

### 10.1 上传失败处理

```java
uploader.setOnError((file, error) -> {
    System.err.println("上传失败: " + error.getMessage());
    // file.getStatus() == UploadFileStatus.FAIL
    // file.getErrorMessage() 包含错误详情
});
```

### 10.2 FileUploader 的错误防护机制

1. **HTTP 超时**: `HttpFileUploadHandler` 默认连接超时 15s、读取超时 60s，防止线程池耗尽
2. **外层 catch**: `doUpload()` 方法对整个上传流程包裹 try-catch，任何未捕获的异常都会：
   - 将文件状态设为 `FAIL`
   - 设置错误信息
   - 触发 `onError` 回调
   - 触发 `onChange` 回调
   - 从活跃任务映射中移除
3. **空值防护**: `addFile()` 检查文件是否为 null 或不存在
4. **线程安全**: `activeUploads` 使用 `ConcurrentHashMap`

### 10.3 ImageCropPane 的错误防护

- `setImage()`: 检查文件 null、存在性、图片加载错误
- `showCropDialog()`: 检查文件有效性
- `doCrop()`: 捕获 IOException 并显示用户友好的错误对话框
- 额外捕获 RuntimeException 防止意外崩溃

### 10.4 AvatarUploadNode 校验流程

```
用户选择文件
    ↓
beforeUpload: 格式校验（jpg/jpeg/png）
    ↓ 通过
beforeUpload: 大小校验（≤ maxFileSize）
    ↓ 通过
beforeUpload: 裁切拦截 → 弹出裁切对话框
    ↓ 裁切完成
skipCropIntercept=true → handleStart(croppedFile)
    ↓
beforeUpload 再次调用（跳过裁切）→ 正常添加
```

---

## 11. 性能优化与最佳实践

### 11.1 内存管理

- **必须调用 dispose()**: 当组件不再使用时，调用 `dispose()` 释放线程池和取消上传
- **监听器自动清理**: `FileUploaderSkin` 会在文件移除时自动清理 per-file 监听器
- **缩略图异步加载**: 使用 `Image(url, w, h, true, true, true)` 后台加载
- **裁切临时文件**: 使用 `deleteOnExit()` 在 JVM 退出时自动清理

```java
// 窗口关闭时释放资源
stage.setOnCloseRequest(e -> {
    node.dispose();
});
```

### 11.2 UI 性能

- **节流重建**: `FileUploaderSkin` 使用 `scheduleRebuild()` 合并同一帧内的多次重建请求
- **Viewport 预览**: `ImageCropPane` 使用 `ImageView.setViewport()` 实现裁切预览，避免创建快照
- **进度条过滤**: 仅在 `UPLOADING` 状态下响应进度变化

### 11.3 并发配置建议

| 场景 | 推荐并发数 | 理由 |
|------|-----------|------|
| 一般用途 | 3（默认） | 平衡性能与资源占用 |
| 大量小文件 | 5~8 | 提高吞吐量 |
| 少量大文件 | 1~2 | 避免带宽争抢 |
| 文件夹上传 | 3~5 | 文件可能较多 |

```java
FileUploader uploader = new FileUploader(5); // 5 个并发上传
```

### 11.4 大文件上传建议

```java
// 增加超时时间
uploader.setHttpRequest(new HttpFileUploadHandler(30_000, 300_000)); // 连接30s, 读取5分钟

// 监控进度
uploader.setOnProgress((file, progress) -> {
    System.out.printf("%s: %.1f%%\n", file.getName(), progress * 100);
});
```

### 11.5 最佳实践清单

1. ✅ 始终设置 `onError` 回调处理上传失败
2. ✅ 使用 `beforeUpload` 进行客户端校验（减少无效请求）
3. ✅ 窗口关闭时调用 `dispose()` 释放资源
4. ✅ 设置合理的 `limit` 避免用户选择过多文件
5. ✅ 使用 `accept` 过滤文件类型（在选择器层面过滤）
6. ✅ 大文件场景增加 `HttpFileUploadHandler` 超时时间
7. ✅ 使用 `headers` 传递认证信息
8. ❌ 避免在回调中执行耗时操作（会阻塞 FX 线程）
9. ❌ 避免频繁调用 `buildUI`（已由节流机制优化）

---

## 12. 样式定制参考

Aurora-FX 组件库提供了完整的 CSS 样式类体系，开发者可以通过覆盖这些样式类来自定义组件外观。

### 如何加载自定义样式

```java
// 方式一：在 Scene 级别加载自定义 CSS
scene.getStylesheets().add(getClass().getResource("/my-custom-upload.css").toExternalForm());

// 方式二：针对单个组件加载
uploader.getStylesheets().add(getClass().getResource("/my-custom-upload.css").toExternalForm());

// 方式三：直接在代码中设置内联样式
node.setStyle("-fx-background-color: #f0f0f0;");
```

> **重要提示**: 自定义 CSS 文件应在内置样式表之后加载，以确保覆盖生效。JavaFX CSS 遵循“后加载优先”的规则。

---

### 12.1 FileUploader 上传组件样式

上传组件的所有样式均定义在内置样式表 `file-uploader.css` 中，以下是完整的样式类名参考。

#### 12.1.1 根容器

| CSS 类名 | 应用对象 | 可覆盖属性 |
|----------|----------|------------|
| `.file-uploader` | FileUploader 控件根节点 | 所有 Control 属性 |
| `.file-uploader-root` | 内部 VBox 根容器 | `-fx-spacing`, `-fx-padding` |
| `.file-uploader-card-container` | 照片墙模式 FlowPane 容器 | `-fx-hgap`, `-fx-vgap` |
| `.file-uploader-list-container` | 文件列表 VBox 容器 | `-fx-spacing`, `-fx-padding` |

```css
/* 示例：调整卡片间距 */
.file-uploader-card-container {
    -fx-hgap: 16px;
    -fx-vgap: 16px;
}
```

#### 12.1.2 上传按钮

| CSS 类名 | 伪类 | 可覆盖属性 |
|----------|-------|------------|
| `.upload-button` | 无 | `-fx-background-color`, `-fx-text-fill`, `-fx-padding`, `-fx-background-radius`, `-fx-font-size`, `-fx-graphic-text-gap` |
| `.upload-button` | `:hover` | `-fx-background-color` |
| `.upload-button` | `:pressed` | `-fx-background-color` |
| `.upload-button` | `:disabled` | `-fx-background-color`, `-fx-cursor` |
| `.upload-submit-button` | 无 | `-fx-background-color`, `-fx-text-fill`, `-fx-padding`, `-fx-background-radius`, `-fx-font-size` |
| `.upload-submit-button` | `:hover` | `-fx-background-color` |
| `.upload-submit-button` | `:pressed` | `-fx-background-color` |

```css
/* 示例：自定义上传按钮主题色 */
.upload-button {
    -fx-background-color: #722ed1;
    -fx-background-radius: 20px;
    -fx-padding: 10px 28px;
    -fx-font-size: 15px;
}
.upload-button:hover {
    -fx-background-color: #9254de;
}
.upload-button:pressed {
    -fx-background-color: #531dab;
}

/* 示例：自定义手动上传按钮 */
.upload-submit-button {
    -fx-background-color: #fa8c16;
    -fx-text-fill: white;
}
```

#### 12.1.3 拖拽上传区域

| CSS 类名 | 伪类 | 可覆盖属性 |
|----------|-------|------------|
| `.upload-drag-zone` | 无 | `-fx-background-color`, `-fx-border-color`, `-fx-border-style`, `-fx-border-width`, `-fx-border-radius`, `-fx-background-radius` |
| `.upload-drag-zone` | `:hover` | `-fx-border-color` |
| `.upload-drag-over` | 无（拖入时动态添加） | `-fx-border-color`, `-fx-background-color` |
| `.upload-drag-text` | 无 | `-fx-font-size`, `-fx-text-fill` |

```css
/* 示例：自定义拖拽区域外观 */
.upload-drag-zone {
    -fx-background-color: #f6ffed;
    -fx-border-color: #b7eb8f;
    -fx-border-style: dashed;
    -fx-border-width: 2px;
    -fx-border-radius: 12px;
    -fx-background-radius: 12px;
}
.upload-drag-zone:hover {
    -fx-border-color: #52c41a;
}
.upload-drag-over {
    -fx-border-color: #52c41a;
    -fx-background-color: #d9f7be;
}
```

#### 12.1.4 提示文本

| CSS 类名 | 可覆盖属性 |
|----------|------------|
| `.upload-tip` | `-fx-font-size`, `-fx-text-fill`, `-fx-padding` |

```css
.upload-tip {
    -fx-font-size: 13px;
    -fx-text-fill: #faad14;
}
```

#### 12.1.5 照片墙模式（PICTURE_CARD）

| CSS 类名 | 伪类 | 应用对象 | 可覆盖属性 |
|----------|-------|----------|------------|
| `.upload-card` | 无 | 图片卡片 | `-fx-background-color`, `-fx-border-color`, `-fx-border-width`, `-fx-border-radius`, `-fx-background-radius` |
| `.upload-card` | `:hover` | 悬浮状态 | `-fx-border-color` |
| `.upload-card-error` | 无 | 失败状态卡片 | `-fx-border-color`, `-fx-border-width` |
| `.upload-card-trigger` | 无 | 上传触发卡片（+号） | `-fx-background-color`, `-fx-border-color`, `-fx-border-style`, `-fx-border-radius`, `-fx-background-radius` |
| `.upload-card-trigger` | `:hover` | 触发卡片悬浮 | `-fx-border-color`, `-fx-background-color` |
| `.upload-card-mask` | 无 | 悬浮遗罩层 | `-fx-background-color`, `-fx-background-radius` |
| `.upload-card-actions` | 无 | 操作按钮容器 | `-fx-alignment`, `-fx-spacing` |
| `.upload-card-progress` | 无 | 卡片进度条 | `-fx-accent` |
| `.upload-card-success-badge` | 无 | 成功徽章 | `-fx-background-color`, `-fx-background-radius` |
| `.upload-card-filename` | 无 | 卡片文件名 | `-fx-font-size`, `-fx-text-fill` |

```css
/* 示例：自定义照片墙卡片样式（圆角+阴影） */
.upload-card {
    -fx-background-color: white;
    -fx-border-color: #e8e8e8;
    -fx-border-width: 1px;
    -fx-border-radius: 12px;
    -fx-background-radius: 12px;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);
}
.upload-card:hover {
    -fx-border-color: #722ed1;
    -fx-effect: dropshadow(three-pass-box, rgba(114,46,209,0.15), 12, 0, 0, 3);
}

/* 自定义触发卡片 */
.upload-card-trigger {
    -fx-background-color: #fafafa;
    -fx-border-color: #d9d9d9;
    -fx-border-radius: 12px;
    -fx-background-radius: 12px;
}
.upload-card-trigger:hover {
    -fx-border-color: #722ed1;
    -fx-background-color: #f9f0ff;
}

/* 自定义遗罩层透明度 */
.upload-card-mask {
    -fx-background-color: rgba(0, 0, 0, 0.65);
    -fx-background-radius: 11px;
}

/* 自定义成功徽章颜色 */
.upload-card-success-badge {
    -fx-background-color: #722ed1;
}
```

#### 12.1.6 文件列表模式（TEXT / PICTURE）

| CSS 类名 | 伪类 | 应用对象 | 可覆盖属性 |
|----------|-------|----------|------------|
| `.upload-list-item` | 无 | 文件列表项 | `-fx-background-color`, `-fx-padding`, `-fx-background-radius` |
| `.upload-list-item` | `:hover` | 悬浮状态 | `-fx-background-color` |
| `.upload-list-item-wrapper` | 无 | 上传中项包裹器 | `-fx-background-radius` |
| `.upload-list-item-success` | 无 | 成功项 | 可自定义背景 |
| `.upload-list-item-success` | `:hover` | 成功项悬浮 | `-fx-background-color` |
| `.upload-list-item-error` | 无 | 失败项 | `-fx-background-color` |
| `.upload-list-item-error` | `:hover` | 失败项悬浮 | `-fx-background-color` |
| `.upload-list-item-uploading` | `:hover` | 上传中项悬浮 | `-fx-background-color` |
| `.upload-list-item-name` | 无 | 文件名标签 | `-fx-font-size`, `-fx-text-fill` |
| `.upload-list-item-size` | 无 | 文件大小标签 | `-fx-font-size`, `-fx-text-fill` |
| `.upload-list-item-close` | 无 | 删除按钮 | `-fx-cursor` |
| `.upload-list-item-picture` | 无 | 图片列表项（PICTURE模式） | `-fx-padding`, `-fx-border-color`, `-fx-border-width`, `-fx-border-radius` |
| `.upload-list-thumb` | 无 | 缩略图容器 | `-fx-background-color`, `-fx-border-color`, `-fx-border-radius`, `-fx-background-radius` |
| `.upload-list-progress` | 无 | 列表进度条 | `-fx-accent` |

**复合选择器**（文件名随状态变色）:

| CSS 选择器 | 说明 |
|------------|------|
| `.upload-list-item-error .upload-list-item-name` | 失败状态的文件名变红 |
| `.upload-list-item-success .upload-list-item-name` | 成功状态的文件名变绿 |
| `.upload-list-item-picture.upload-list-item-success` | 图片模式成功项边框 |
| `.upload-list-item-picture.upload-list-item-error` | 图片模式失败项边框 |

```css
/* 示例：自定义文件列表样式 */
.upload-list-item:hover {
    -fx-background-color: #f9f0ff;
}
.upload-list-item-success:hover {
    -fx-background-color: #f6ffed;
}
.upload-list-item-error {
    -fx-background-color: #fff2f0;
}
.upload-list-item-error .upload-list-item-name {
    -fx-text-fill: #ff4d4f;
}
.upload-list-item-success .upload-list-item-name {
    -fx-text-fill: #52c41a;
}
```

#### 12.1.7 进度条

| CSS 选择器 | 可覆盖属性 |
|------------|------------|
| `.progress-bar > .track` | `-fx-background-color`, `-fx-background-radius` |
| `.progress-bar > .bar` | `-fx-background-color`, `-fx-background-radius`, `-fx-background-insets` |

```css
/* 示例：自定义进度条颜色 */
.progress-bar > .track {
    -fx-background-color: #f0f0f0;
    -fx-background-radius: 5px;
}
.progress-bar > .bar {
    -fx-background-color: linear-gradient(to right, #722ed1, #b37feb);
    -fx-background-radius: 5px;
}
```

#### 12.1.8 FileUploader 完整样式类名汇总表

| # | CSS 类名 | 节点类型 | 所属模式 | 说明 |
|---|----------|----------|----------|------|
| 1 | `.file-uploader` | Control | 全部 | 组件根节点 |
| 2 | `.file-uploader-root` | VBox | 全部 | 内部布局根容器 |
| 3 | `.file-uploader-card-container` | FlowPane | PICTURE_CARD | 卡片流式容器 |
| 4 | `.file-uploader-list-container` | VBox | TEXT / PICTURE | 列表容器 |
| 5 | `.upload-button` | Button | TEXT / PICTURE | 上传按钮 |
| 6 | `.upload-submit-button` | Button | 手动上传 | “上传到服务器”按钮 |
| 7 | `.upload-drag-zone` | VBox | 拖拽模式 | 拖拽上传区域 |
| 8 | `.upload-drag-over` | 动态添加 | 拖拽模式 | 拖入时临时样式 |
| 9 | `.upload-drag-text` | Label | 拖拽模式 | 拖拽区域提示文本 |
| 10 | `.upload-tip` | Label | 全部 | 提示信息标签 |
| 11 | `.upload-card` | StackPane | PICTURE_CARD | 图片卡片 |
| 12 | `.upload-card-error` | StackPane | PICTURE_CARD | 失败状态卡片 |
| 13 | `.upload-card-trigger` | StackPane | PICTURE_CARD | +号触发卡片 |
| 14 | `.upload-card-mask` | Region | PICTURE_CARD | 悬浮暗色遗罩 |
| 15 | `.upload-card-actions` | HBox | PICTURE_CARD | 悬浮操作按钮组 |
| 16 | `.upload-card-progress` | ProgressBar | PICTURE_CARD | 卡片内进度条 |
| 17 | `.upload-card-success-badge` | StackPane | PICTURE_CARD | 右下角成功徽章 |
| 18 | `.upload-card-filename` | Label | PICTURE_CARD | 卡片内文件名 |
| 19 | `.upload-list-item` | HBox | TEXT / PICTURE | 文件列表行 |
| 20 | `.upload-list-item-wrapper` | VBox | TEXT | 上传中项包裹 |
| 21 | `.upload-list-item-success` | HBox/VBox | TEXT / PICTURE | 成功状态行 |
| 22 | `.upload-list-item-error` | HBox/VBox | TEXT / PICTURE | 失败状态行 |
| 23 | `.upload-list-item-uploading` | HBox/VBox | TEXT / PICTURE | 上传中状态行 |
| 24 | `.upload-list-item-name` | Label | TEXT / PICTURE | 文件名标签 |
| 25 | `.upload-list-item-size` | Label | PICTURE | 文件大小标签 |
| 26 | `.upload-list-item-close` | StackPane | TEXT | 删除按钮 |
| 27 | `.upload-list-item-picture` | HBox | PICTURE | 图片列表行额外样式 |
| 28 | `.upload-list-thumb` | StackPane | PICTURE | 缩略图容器 |
| 29 | `.upload-list-progress` | ProgressBar | TEXT / PICTURE | 列表进度条 |

---

### 12.2 ImageCropPane 裁切组件样式

`ImageCropPane` 的样式定义在同一个 `file-uploader.css` 文件中。

| CSS 类名/选择器 | 应用对象 | 可覆盖属性 |
|------------------|----------|------------|
| `.image-crop-pane` | 裁切面板根节点 | `-fx-background-color`, `-fx-border-color`, `-fx-border-width`, `-fx-border-radius`, `-fx-background-radius` |
| `.image-crop-toolbar` | 底部工具栏 | `-fx-background-color`, `-fx-border-color`, `-fx-border-width`, `-fx-padding`, `-fx-spacing`, `-fx-alignment` |
| `.image-crop-toolbar .button` | 工具栏按钮（确认/使用） | `-fx-background-color`, `-fx-text-fill`, `-fx-padding`, `-fx-background-radius`, `-fx-font-size` |
| `.image-crop-toolbar .button:hover` | 按钮悬浮 | `-fx-background-color` |
| `.image-crop-toolbar .button.cancel-button` | 取消/重新裁切按钮 | `-fx-background-color`, `-fx-text-fill`, `-fx-border-color`, `-fx-border-width`, `-fx-border-radius` |
| `.image-crop-toolbar .button.cancel-button:hover` | 取消按钮悬浮 | `-fx-background-color`, `-fx-border-color` |

```css
/* 示例：自定义裁切对话框按钮风格 */
.image-crop-pane {
    -fx-background-color: #1a1a2e;
    -fx-border-color: #16213e;
}
.image-crop-toolbar {
    -fx-background-color: #1a1a2e;
    -fx-border-color: #16213e;
}
.image-crop-toolbar .button {
    -fx-background-color: #e94560;
    -fx-text-fill: white;
    -fx-background-radius: 20px;
}
.image-crop-toolbar .button:hover {
    -fx-background-color: #ff6b6b;
}
.image-crop-toolbar .button.cancel-button {
    -fx-background-color: transparent;
    -fx-text-fill: #cccccc;
    -fx-border-color: #cccccc;
    -fx-border-radius: 20px;
}
```

---

### 12.3 ImageViewer 图片查看器样式

`ImageViewer` 组件使用内联样式（`setStyle()`）而非 CSS 类名。可通过以下方式自定义外观：

#### 可覆盖的内联样式区域

| 组件 | 节点类型 | 默认内联样式 | 说明 |
|--------|----------|--------------|------|
| 操作按钮栏 | HBox | `-fx-background-color: rgb(0,0,0,0.3)`, `-fx-background-radius: 18px`, `-fx-padding: 0 10px`, `-fx-pref-height: 36px`, `-fx-spacing: 10px` | 底部工具栏 |
| 信息标签 | Label | `-fx-text-fill: white`, `-fx-background-color: rgba(0,0,0,0.3)`, `-fx-padding: 3px 5px` | 图片尺寸+序号信息 |

#### 通过代码自定义 ImageViewer

```java
// 获取 ImageViewer 的 Skin 层进行样式定制
ImageViewer viewer = new ImageViewer(images);
viewer.setStyle("-fx-background-color: #1a1a2e;");

// 在预览窗口中自定义背景色
StackPane root = new StackPane(viewer);
root.setStyle("-fx-background-color: rgba(0,0,0,0.95);");
```

#### 预览窗口定制

当通过 `FileUploaderSkin` 的内置预览打开时，预览窗口是无框透明全屏 Stage。如需定制预览行为，可通过 `FileUploader.setOnPreview()` 完全接管：

```java
uploader.setOnPreview(file -> {
    // 完全自定义的预览窗口
    Stage stage = new Stage();
    stage.initStyle(StageStyle.UNDECORATED);
    ImageViewer viewer = new ImageViewer(FXCollections.observableArrayList(
        new Image(file.getRawFile().toURI().toString())
    ));
    StackPane root = new StackPane(viewer);
    root.setStyle("-fx-background-color: rgba(0,0,50,0.9);");
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.show();
});
```

---

### 12.4 VerifyCode 验证码组件样式

验证码组件（`SliderVerifyPane`、`ArithmeticVerifyPane`、`TextClickVerifyPane`）使用内联样式系统。每个组件继承 `VBox`，可直接通过 `setStyle()` 覆盖外观。

#### 12.4.1 三个验证码组件的通用外层样式

所有验证码组件共享相同的默认外层样式：

```css
/* 默认外层样式（应用在组件根 VBox 上） */
-fx-background-color: #ffffff;
-fx-background-radius: 8;
-fx-border-color: #e0e0e0;
-fx-border-radius: 8;
-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);
```

```java
// 自定义组件外观
SliderVerifyPane slider = new SliderVerifyPane();
slider.setStyle(
    "-fx-background-color: #1a1a2e; " +
    "-fx-background-radius: 12; " +
    "-fx-border-color: #16213e; " +
    "-fx-border-radius: 12; " +
    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 4);"
);
```

#### 12.4.2 SliderVerifyPane 内部样式映射

| 内部组件 | 类型 | 默认样式属性 |
|----------|------|------------|
| 图片容器 | Pane | `-fx-background-color: #f5f5f5`, `-fx-background-radius: 4` |
| 滑块轨道 | Pane | `-fx-background-color: #f0f0f0`, `-fx-background-radius: 20` |
| 滑块按钮 | StackPane | `-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f8f8)`, `-fx-background-radius: 4`, `-fx-border-color: #cccccc`, `-fx-border-radius: 4` |
| 滑块图标 | Label | `-fx-font-size: 18`, `-fx-text-fill: #666` |
| 状态标签 | Label | `-fx-text-fill: #999`, `-fx-font-size: 14` |
| 刷新按钮 | Button | `-fx-background-color: transparent`, `-fx-text-fill: #1e90ff`, `-fx-border-color: #1e90ff`, `-fx-border-radius: 4` |

**状态变化时的样式变更**：

| 状态 | 滑块按钮样式 | 状态标签颜色 |
|------|------------|------------|
| `READY` | 白色渐变 + `#cccccc` 边框 | `#999` |
| `VERIFYING` | 保持不变 | `#1e90ff` |
| `SUCCESS` | 绿色渐变 + `#52c41a` 边框 | `#52c41a` 加粗 |
| `FAIL` | 红色渐变 + `#ff4d4f` 边框 | `#ff4d4f` 加粗 |

#### 12.4.3 ArithmeticVerifyPane 内部样式映射

| 内部组件 | 类型 | 默认样式属性 |
|----------|------|------------|
| 图片容器 | StackPane | `-fx-background-color: #f5f5f5`, `-fx-background-radius: 4` |
| 提示标签 | Label | `-fx-font-size: 14`, `-fx-text-fill: #333` |
| 答案输入框 | TextField | `-fx-font-size: 16`, `-fx-padding: 8`, `-fx-background-radius: 4`, `-fx-border-color: #ddd`, `-fx-border-radius: 4` |
| 验证按钮 | Button | `-fx-background-color: #1e90ff`, `-fx-text-fill: white`, `-fx-font-size: 14`, `-fx-padding: 8 20`, `-fx-background-radius: 4` |
| 状态标签 | Label | `-fx-text-fill: #999`, `-fx-font-size: 12` |
| 刷新按钮 | Button | `-fx-background-color: transparent`, `-fx-text-fill: #1e90ff`, `-fx-border-color: #1e90ff`, `-fx-border-radius: 4` |

**状态变化时的样式变更**：

| 状态 | 验证按钮背景 | 状态标签颜色 |
|------|------------|------------|
| `READY` | `#1e90ff` | `#999` |
| `VERIFYING` | 保持不变 | `#1e90ff` |
| `SUCCESS` | `#52c41a` | `#52c41a` 加粗 |
| `FAIL` | `#ff4d4f` | `#ff4d4f` 加粗 |

#### 12.4.4 TextClickVerifyPane 内部样式映射

| 内部组件 | 类型 | 默认样式属性 |
|----------|------|------------|
| 提示标签 | Label | `-fx-font-size: 14`, `-fx-text-fill: #333`, `-fx-padding: 5 10`, `-fx-background-color: #f0f7ff`, `-fx-background-radius: 4` |
| 图片容器 | StackPane | `-fx-background-color: #f5f5f5`, `-fx-background-radius: 4` |
| 点击标记 | Circle | 描边色 `#1e90ff`，描边宽度 2px |
| 序号标签 | Label | `-fx-font-size: 10`, `-fx-text-fill: #1e90ff`, `-fx-font-weight: bold` |
| 状态标签 | Label | `-fx-text-fill: #999`, `-fx-font-size: 12` |
| 刷新按钮 | Button | 与 SliderVerifyPane 相同 |

#### 12.4.5 验证码组件样式自定义示例

```java
// 方式一：通过 setStyle 覆盖根节点样式
SliderVerifyPane slider = new SliderVerifyPane(config);
slider.setStyle(
    "-fx-background-color: #2d2d2d; " +
    "-fx-background-radius: 12; " +
    "-fx-border-color: #404040; " +
    "-fx-border-radius: 12;"
);

// 方式二：通过 lookup 获取内部节点并修改样式
Platform.runLater(() -> {
    // 覆盖内部 TextField 样式
    arithmeticPane.lookupAll(".text-field").forEach(node ->
        node.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #b0b0b0;")
    );
    // 覆盖内部 Button 样式
    arithmeticPane.lookupAll(".button").forEach(node ->
        node.setStyle("-fx-background-color: #722ed1; -fx-text-fill: white;")
    );
});

// 方式三：通过 CSS 文件覆盖样式
// 因为验证码组件的内部节点使用了 setStyle() 内联样式，
// CSS 文件无法直接覆盖 setStyle() 设置的属性。
// 推荐使用方式一或方式二。
```

> **注意**: 验证码组件的内部子节点使用 `setStyle()` 内联样式，
> 在 JavaFX 中 **内联样式优先级高于 CSS 文件**。
> 因此要覆盖内部子节点样式，必须使用 `setStyle()` 或 `lookup()` + `setStyle()` 方式。
> 组件根节点（VBox）的样式可以通过 `setStyle()` 直接覆盖。

---

### 12.5 全局样式覆盖最佳实践

#### 12.5.1 统一主题色覆盖

创建一个 `custom-upload-theme.css` 文件，在应用启动时加载：

```css
/* custom-upload-theme.css —— 统一紫色主题示例 */

/* 主按钮 */
.upload-button {
    -fx-background-color: #722ed1;
}
.upload-button:hover {
    -fx-background-color: #9254de;
}
.upload-button:pressed {
    -fx-background-color: #531dab;
}

/* 照片墙交互色 */
.upload-card:hover {
    -fx-border-color: #722ed1;
}
.upload-card-trigger:hover {
    -fx-border-color: #722ed1;
    -fx-background-color: #f9f0ff;
}

/* 拖拽区域交互色 */
.upload-drag-zone:hover {
    -fx-border-color: #722ed1;
}
.upload-drag-over {
    -fx-border-color: #722ed1;
    -fx-background-color: #f9f0ff;
}

/* 成功徽章 */
.upload-card-success-badge {
    -fx-background-color: #722ed1;
}

/* 进度条 */
.progress-bar > .bar {
    -fx-background-color: #722ed1;
}

/* 卡片进度条 */
.upload-card-progress {
    -fx-accent: #722ed1;
}

/* 列表进度条 */
.upload-list-progress {
    -fx-accent: #722ed1;
}

/* 裁切工具栏按钮 */
.image-crop-toolbar .button {
    -fx-background-color: #722ed1;
}
.image-crop-toolbar .button:hover {
    -fx-background-color: #9254de;
}
```

```java
// 应用主题
scene.getStylesheets().add(
    getClass().getResource("/custom-upload-theme.css").toExternalForm()
);
```

#### 12.5.2 深色主题示例

```css
/* dark-upload-theme.css */

.file-uploader-root {
    -fx-background-color: #1e1e1e;
}

.upload-button {
    -fx-background-color: #177ddc;
    -fx-text-fill: #e8e8e8;
}
.upload-button:hover {
    -fx-background-color: #3c9ae8;
}

.upload-drag-zone {
    -fx-background-color: #2a2a2a;
    -fx-border-color: #434343;
}
.upload-drag-zone:hover {
    -fx-border-color: #177ddc;
}
.upload-drag-text {
    -fx-text-fill: #a0a0a0;
}

.upload-card {
    -fx-background-color: #2a2a2a;
    -fx-border-color: #434343;
}
.upload-card:hover {
    -fx-border-color: #177ddc;
}
.upload-card-trigger {
    -fx-background-color: #2a2a2a;
    -fx-border-color: #434343;
}
.upload-card-trigger:hover {
    -fx-border-color: #177ddc;
    -fx-background-color: #1a3a5c;
}

.upload-list-item:hover {
    -fx-background-color: #2a2a2a;
}
.upload-list-item-name {
    -fx-text-fill: #e8e8e8;
}
.upload-list-item-size {
    -fx-text-fill: #7a7a7a;
}

.upload-tip {
    -fx-text-fill: #7a7a7a;
}

.upload-card-filename {
    -fx-text-fill: #a0a0a0;
}

.progress-bar > .track {
    -fx-background-color: #3a3a3a;
}
.progress-bar > .bar {
    -fx-background-color: #177ddc;
}

.image-crop-pane {
    -fx-background-color: #1e1e1e;
    -fx-border-color: #434343;
}
.image-crop-toolbar {
    -fx-background-color: #2a2a2a;
    -fx-border-color: #434343;
}
.image-crop-toolbar .button {
    -fx-background-color: #177ddc;
}
.image-crop-toolbar .button.cancel-button {
    -fx-background-color: transparent;
    -fx-text-fill: #a0a0a0;
    -fx-border-color: #434343;
}
```

#### 12.5.3 样式优先级说明

JavaFX CSS 样式优先级（从高到低）：

1. **`setStyle()` 内联样式**（最高优先级，无法被 CSS 文件覆盖）
2. **用户自定义 CSS 文件**（后加载的覆盖先加载的）
3. **组件内置 CSS 文件**（`file-uploader.css`）
4. **JavaFX 默认样式**（Modena/Caspian）

> **实践建议**:
> - FileUploader 组件的样式使用 CSS 类名，可直接通过 CSS 文件覆盖 ✅
> - ImageCropPane 的工具栏按钮使用 CSS 类名，可通过 CSS 文件覆盖 ✅
> - VerifyCode 组件使用 `setStyle()` 内联样式，需用代码 `setStyle()` 或 `lookup()` 覆盖 ⚠️
> - ImageViewer 使用 `setStyle()` 内联样式，需用代码覆盖 ⚠️
> - 对于使用 `setStyle()` 的组件，建议使用 `Platform.runLater()` 确保 UI 已初始化

---

### 12.6 组件样式架构图

```
FileUploader (.file-uploader)
  └─ FileUploaderSkin
       └─ .file-uploader-root (VBox)
            ├─ PICTURE_CARD 模式:
            │    ├─ .file-uploader-card-container (FlowPane)
            │    │    ├─ .upload-card (StackPane)           ← 图片卡片
            │    │    │    ├─ ImageView                       ← 缩略图
            │    │    │    ├─ .upload-card-progress            ← 进度条
            │    │    │    ├─ .upload-card-success-badge       ← 成功徽章
            │    │    │    ├─ .upload-card-error               ← 失败边框
            │    │    │    ├─ .upload-card-mask (Region)       ← 遗罩层
            │    │    │    └─ .upload-card-actions (HBox)      ← 操作按钮
            │    │    └─ .upload-card-trigger (StackPane)  ← +号卡片
            │    └─ .upload-tip (Label)                   ← 提示文本
            │
            ├─ TEXT 模式:
            │    ├─ .upload-button (Button)               ← 上传按钮
            │    ├─ .upload-submit-button (Button)        ← 手动上传按钮
            │    ├─ .upload-tip (Label)                   ← 提示文本
            │    └─ .file-uploader-list-container (VBox)
            │         └─ .upload-list-item (HBox)          ← 文件行
            │              ├─ .upload-list-item-name (Label)
            │              ├─ .upload-list-item-close
            │              └─ .upload-list-progress
            │
            └─ DRAG 模式:
                 ├─ .upload-drag-zone (VBox)              ← 拖拽区域
                 │    └─ .upload-drag-text (Label)
                 └─ .file-uploader-list-container

ImageCropPane (.image-crop-pane)
  ├─ 裁切区域（内联样式）
  └─ .image-crop-toolbar (HBox)
       ├─ .button                       ← 确认/使用按钮
       └─ .button.cancel-button         ← 取消/重新裁切按钮
```

---

## 13. 组件间关系图

```
FileUploaderFactory ─── create*/integrate* ──→ XxxUploadNode
                                                    │
                                                    │ 内部组合
                                                    ▼
                                               FileUploader (Control)
                                                    │
                            ┌───────────────────────┼───────────────────┐
                            │                       │                   │
                      FileUploaderSkin          UploadFile[]       FileUploadHandler
                      (UI 渲染+交互)           (文件模型列表)        (上传处理器)
                            │                       │                   │
                   ┌────────┼────────┐              │           HttpFileUploadHandler
                   │        │        │              │           (HTTP multipart 实现)
              TEXT模式  PICTURE模式 CARD模式    UploadFileStatus
                                                (READY/UPLOADING/SUCCESS/FAIL)

AvatarUploadNode ── 特殊集成 ──→ ImageCropPane (裁切对话框)
```

---

## 14. 常见问题 FAQ

### Q: 没有服务器，能测试上传功能吗？

A: 可以。不设置 `action` 属性（或设为空字符串）时，组件进入**本地模式**：文件选择后直接标记为成功，不发送网络请求。

### Q: 头像上传第一次选择文件就能弹出裁切吗？

A: 是的。AvatarUploadNode 在 `beforeUpload` 中拦截所有文件选择，自动弹出裁切对话框。无论是第一次还是后续替换，都会触发裁切。

### Q: 如何自定义上传逻辑（如使用 OkHttp）？

A: 实现 `FileUploadHandler` 接口，然后 `uploader.setHttpRequest(myHandler)`。

### Q: 文件夹上传时某个文件失败会影响其他文件吗？

A: 不会。每个文件独立上传，失败的文件显示错误状态，其他文件正常上传。`HttpFileUploadHandler` 有超时机制防止线程池耗尽。

### Q: 如何回显已上传的文件？

A: 使用 `UploadFile(name, url)` 构造函数创建，然后添加到 `getFileList()`：
```java
UploadFile existing = new UploadFile("avatar.jpg", "https://cdn.example.com/avatar.jpg");
uploader.getFileList().add(existing);
```

### Q: Node 组件和直接使用 FileUploader 有什么区别？

A: Node 组件是 FileUploader 的**预配置封装**，继承 VBox，自带合理默认配置，通过代理方法暴露常用 API。适合快速集成。需要完全控制时，使用 `getUploader()` 获取内部实例。

### Q: 如何处理上传后的服务器响应？

A: `onSuccess` 回调的第二个参数就是服务器响应（String 类型）：
```java
node.setOnSuccess((file, response) -> {
    // response 通常是 JSON 字符串
    System.out.println("服务器返回: " + response);
    // 可以解析 JSON 提取文件 URL
});
```

---

> **文档版本**: v2.0  
> **适用版本**: Aurora-FX 0.0.1+  
> **技术栈**: Java 21 + JavaFX 23.0.1
