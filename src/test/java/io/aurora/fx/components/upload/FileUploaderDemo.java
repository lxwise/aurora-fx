package io.aurora.fx.components.upload;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * Aurora-FX FileUploader 组件 —— 全功能综合演示
 * ============================================================================
 * <p>
 * 本 Demo 全面展示了 FileUploader 组件的所有功能特性，
 * 对标 Element UI 的 Upload 组件，涵盖以下场景：
 * </p>
 *
 * <h2>Tab 页索引：</h2>
 * <ol>
 *   <li><b>基础上传</b> — 点击按钮选择文件上传（TEXT 列表模式）</li>
 *   <li><b>照片墙</b> — PICTURE_CARD 卡片模式，支持预览/删除</li>
 *   <li><b>拖拽上传</b> — 拖拽文件到指定区域上传</li>
 *   <li><b>图片列表</b> — PICTURE 缩略图列表模式</li>
 *   <li><b>头像上传</b> — 单文件限制 + 格式/大小校验 + 图像裁切</li>
 *   <li><b>数量限制</b> — 限制最大上传文件数量</li>
 *   <li><b>手动上传</b> — 选择文件后手动触发上传</li>
 *   <li><b>文件夹上传</b> — 选择文件夹，自动扁平化所有文件</li>
 * </ol>
 *
 * <h2>核心 API 速查：</h2>
 * <pre>
 * 属性配置：
 *   setAction(String)           — 上传接口地址（为空时进入本地模式）
 *   setListType(ListType)       — 列表类型：TEXT / PICTURE / PICTURE_CARD
 *   setMultiple(boolean)        — 是否支持多选
 *   setAccept(String)           — 接受的文件类型："image/*", ".jpg,.png"
 *   setLimit(int)               — 最大文件数量（0=无限制）
 *   setDrag(boolean)            — 启用拖拽上传
 *   setDirectory(boolean)       — 启用文件夹上传
 *   setAutoUpload(boolean)      — 是否自动上传（false 时需调用 submit()）
 *   setThumbnailSize(double)    — 卡片/缩略图尺寸（px）
 *   setButtonText(String)       — 上传按钮文本
 *   setDragText(String)         — 拖拽区域提示文本
 *   setTip(String)              — 底部提示文本
 *   setUploaderDisabled(boolean)— 禁用组件
 *   setShowFileList(boolean)    — 是否显示文件列表
 *
 * 回调钩子：
 *   setBeforeUpload(Function)   — 上传前校验，返回 false 阻止上传
 *   setBeforeRemove(Function)   — 删除前校验，返回 false 阻止删除
 *   setOnSuccess(BiConsumer)    — 上传成功回调
 *   setOnError(BiConsumer)      — 上传失败回调
 *   setOnProgress(BiConsumer)   — 上传进度回调
 *   setOnChange(BiConsumer)     — 文件列表变化回调
 *   setOnRemove(BiConsumer)     — 文件删除回调
 *   setOnExceed(BiConsumer)     — 超出数量限制回调
 *   setOnPreview(Consumer)      — 文件预览回调
 *   setHttpRequest(handler)     — 自定义上传处理器
 *
 * 公开方法：
 *   submit()                    — 手动触发上传
 *   clearFiles()                — 清空文件列表
 *   abort(UploadFile)           — 取消指定文件上传
 *   abortAll()                  — 取消全部上传
 *   handleStart(File)           — 手动添加文件
 *   handleRemove(UploadFile)    — 手动移除文件
 *   dispose()                   — 释放资源
 * </pre>
 *
 * @author lstar
 * @since 2025
 */
public class FileUploaderDemo extends Application {

    /** 默认接口地址 */
    private static final String DEFAULT_API_URL = "http://localhost:9999/file/upload";

    /** 是否使用真实 API 上传 */
    private final BooleanProperty useRealApi = new SimpleBooleanProperty(false);

    /** 当前接口地址（运行时可编辑） */
    private final javafx.beans.property.StringProperty apiUrl
            = new javafx.beans.property.SimpleStringProperty(DEFAULT_API_URL);

    /** 所有 uploader 实例，用于统一切换上传模式 */
    private final List<FileUploader> allUploaders = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                createTab("基础上传", createBasicUploadDemo()),
                createTab("照片墙", createPictureCardDemo()),
                createTab("拖拽上传", createDragUploadDemo()),
                createTab("图片列表", createPictureListDemo()),
                createTab("头像上传", createAvatarUploadDemo()),
                createTab("数量限制", createLimitDemo()),
                createTab("手动上传", createManualUploadDemo()),
                createTab("文件夹上传", createFolderUploadDemo())
        );

        // === 上传模式切换工具栏 ===
        HBox modeBar = createUploadModeBar();

        VBox mainLayout = new VBox(modeBar, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 1000, 750);
        primaryStage.setTitle("Aurora-FX FileUploader 组件演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 创建上传模式切换工具栏
     * 支持在"模拟上传（本地模式）"和"真实接口上传"之间切换
     */
    private HBox createUploadModeBar() {
        Label modeLabel = new Label("上传模式：");
        modeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #303133;");

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton localRadio = new RadioButton("模拟上传（本地模式）");
        RadioButton realRadio = new RadioButton("真实接口上传");
        localRadio.setToggleGroup(modeGroup);
        realRadio.setToggleGroup(modeGroup);
        localRadio.setSelected(true);
        localRadio.setStyle("-fx-font-size: 13px;");
        realRadio.setStyle("-fx-font-size: 13px;");

        // 可编辑的接口地址输入框
        TextField urlField = new TextField(apiUrl.get());
        urlField.setPromptText("请输入上传接口地址");
        urlField.setPrefWidth(320);
        urlField.setStyle("-fx-font-size: 12px; -fx-font-family: monospace;");
        urlField.visibleProperty().bind(realRadio.selectedProperty());
        urlField.managedProperty().bind(realRadio.selectedProperty());
        // URL 双向绑定
        urlField.textProperty().bindBidirectional(apiUrl);

        Label modeHint = new Label("本地模式：文件选择后直接标记为成功");
        modeHint.setStyle("-fx-text-fill: #67c23a; -fx-font-size: 12px;");

        // 绑定模式切换
        realRadio.selectedProperty().addListener((obs, o, isReal) -> {
            useRealApi.set(isReal);
            String action = isReal ? apiUrl.get() : "";
            for (FileUploader uploader : allUploaders) {
                uploader.setAction(action);
            }
            if (isReal) {
                modeHint.setText("真实模式：文件将上传到 " + apiUrl.get());
                modeHint.setStyle("-fx-text-fill: #e6a23c; -fx-font-size: 12px;");
            } else {
                modeHint.setText("本地模式：文件选择后直接标记为成功");
                modeHint.setStyle("-fx-text-fill: #67c23a; -fx-font-size: 12px;");
            }
        });

        // URL 变化时自动同步到所有 uploader（仅在真实模式下）
        apiUrl.addListener((obs, oldUrl, newUrl) -> {
            if (useRealApi.get() && newUrl != null && !newUrl.isEmpty()) {
                for (FileUploader uploader : allUploaders) {
                    uploader.setAction(newUrl);
                }
                modeHint.setText("真实模式：文件将上传到 " + newUrl);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox modeBar = new HBox(12, modeLabel, localRadio, realRadio, urlField, spacer, modeHint);
        modeBar.setAlignment(Pos.CENTER_LEFT);
        modeBar.setPadding(new Insets(10, 20, 10, 20));
        modeBar.setStyle("-fx-background-color: #f5f7fa; -fx-border-color: #e4e7ed; -fx-border-width: 0 0 1 0;");

        return modeBar;
    }

    /**
     * 注册 uploader 到全局列表，支持上传模式统一切换
     */
    private void registerUploader(FileUploader uploader) {
        allUploaders.add(uploader);
    }

    // =========================================================================
    //  1. 基础上传 — 点击按钮，选择文件上传
    // =========================================================================

    /**
     * 演示最基本的上传功能：
     * - 点击按钮弹出文件选择器
     * - 支持多选
     * - TEXT 列表模式展示已上传文件
     * - 无 action 时进入"本地模式"，文件直接标记为成功
     */
    private VBox createBasicUploadDemo() {
        VBox box = createDemoBox("基础上传",
                "点击按钮选择文件上传，支持多选。未设置 action 时自动进入本地模式，文件选择后立即标记为成功状态。");

        // 创建上传组件实例
        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setMultiple(true);               // 允许多选文件
        uploader.setAccept("image/*");             // 只接受图片文件
        uploader.setTip("仅支持 jpg/png 格式，单个文件不超过 500KB"); // 底部提示文本
        uploader.setButtonText("点击上传");          // 按钮文字
        uploader.setListType(ListType.TEXT);        // 文件列表样式：纯文本

        // ===== 事件回调 =====
        // 上传成功回调（本地模式下文件选择后即触发）
        uploader.setOnSuccess((file, response) ->
                System.out.println("[成功] " + file.getName())
        );

        // 文件删除回调
        uploader.setOnRemove((file, files) ->
                System.out.println("[删除] " + file.getName() + "，剩余 " + files.size() + " 个文件")
        );

        // 删除前确认（返回 true 允许删除，false 阻止）
        uploader.setBeforeRemove(file -> {
            System.out.println("[确认删除] " + file.getName());
            return true;
        });

        registerUploader(uploader);
        box.getChildren().add(uploader);
        return box;
    }

    // =========================================================================
    //  2. 照片墙 — PICTURE_CARD 卡片模式
    // =========================================================================

    /**
     * 演示照片墙模式：
     * - 图片以卡片网格形式展示
     * - 鼠标悬浮显示预览/删除操作按钮
     * - 集成 ImageViewer 图片预览组件
     * - 支持文件数量上限
     */
    private VBox createPictureCardDemo() {
        VBox box = createDemoBox("照片墙（picture-card）",
                "以卡片形式展示上传的图片，支持悬浮预览和删除。上传成功后右下角显示绿色对勾。");

        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setListType(ListType.PICTURE_CARD); // 照片墙模式
        uploader.setMultiple(true);                   // 多选
        uploader.setAccept("image/*");                // 仅图片
        uploader.setLimit(8);                         // 最多 8 张
        uploader.setThumbnailSize(148);               // 卡片尺寸 148x148px

        // 超出数量限制时的处理
        uploader.setOnExceed((files, fileList) ->
                showAlert("超出限制", "最多只能上传 8 张图片，当前已有 " + fileList.size() + " 张")
        );

        registerUploader(uploader);
        box.getChildren().add(uploader);
        return box;
    }

    // =========================================================================
    //  3. 拖拽上传 — 拖拽文件到指定区域
    // =========================================================================

    /**
     * 演示拖拽上传模式：
     * - 显示拖拽区域，支持将文件直接拖入
     * - 点击区域也可打开文件选择器
     * - 拖拽悬浮时区域高亮
     */
    private VBox createDragUploadDemo() {
        VBox box = createDemoBox("拖拽上传",
                "将文件拖拽到虚线区域进行上传，也可以点击区域选择文件。");

        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setDrag(true);                        // 启用拖拽模式
        uploader.setMultiple(true);                    // 多选
        uploader.setAccept("image/*");                 // 仅图片
        uploader.setDragText("将文件拖到此处，或点击上传");  // 拖拽区域提示文本
        uploader.setTip("仅支持图片格式文件");            // 底部提示
        uploader.setListType(ListType.TEXT);            // 已上传文件以列表形式展示

        registerUploader(uploader);
        box.getChildren().add(uploader);
        return box;
    }

    // =========================================================================
    //  4. 图片列表 — PICTURE 缩略图列表模式
    // =========================================================================

    /**
     * 演示图片缩略图列表模式：
     * - 文件列表左侧显示缩略图
     * - 右侧显示文件名和大小
     * - 鼠标悬浮显示预览和删除按钮
     */
    private VBox createPictureListDemo() {
        VBox box = createDemoBox("图片列表缩略图",
                "文件列表左侧显示缩略图预览，右侧展示文件名和大小信息。悬浮可查看/删除。");

        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setListType(ListType.PICTURE);        // 图片列表模式
        uploader.setMultiple(true);                    // 多选
        uploader.setAccept("image/*");                 // 仅图片
        uploader.setButtonText("点击上传");              // 按钮文字
        uploader.setTip("仅支持 jpg/png 格式，单个文件不超过 500KB");

        registerUploader(uploader);
        box.getChildren().add(uploader);
        return box;
    }

    // =========================================================================
    //  5. 头像上传 — 单文件 + 格式/大小校验 + 图像裁切
    // =========================================================================

    /**
     * 演示头像上传场景，包含以下特性：
     * - 单文件上传（limit=1）
     * - beforeUpload 钩子校验文件格式和大小
     * - 超出限制时自动替换旧文件
     * - 集成 ImageCropPane 图像裁切功能
     * - 裁切后的文件自动上传到组件
     */
    private VBox createAvatarUploadDemo() {
        VBox box = createDemoBox("头像上传（含裁切）",
                "单文件上传，限制 JPG/PNG 格式、大小不超过 2MB。选择图片后弹出裁切对话框，"
                        + "支持 1:1 正方形裁切和实时预览。裁切完成后的头像自动添加到上传组件。");

        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setListType(ListType.PICTURE_CARD);   // 照片墙模式展示头像
        uploader.setAccept(".jpg,.jpeg,.png");          // 指定扩展名
        uploader.setLimit(1);                          // 限制 1 个文件
        uploader.setShowFileList(true);                // 显示文件列表
        uploader.setThumbnailSize(178);                // 头像卡片尺寸 178x178px
        uploader.setTip("仅支持 JPG/PNG 格式，大小不超过 2MB");

        // ===== 上传前校验：格式和大小 =====
        uploader.setBeforeUpload(file -> {
            String ext = file.getExtension();
            if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
                showAlert("格式错误", "头像图片必须是 JPG 或 PNG 格式！");
                return false;
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                showAlert("大小超限", "头像图片大小不能超过 2MB！");
                return false;
            }
            return true;
        });

        // ===== 超出限制处理：自动替换旧文件 =====
        // 当 limit=1 且已有文件时，清空旧文件并添加新文件
        uploader.setOnExceed((files, fileList) -> {
            uploader.clearFiles();
            if (!files.isEmpty()) {
                // 弹出裁切对话框
                ImageCropPane.showCropDialog(
                        files.get(0),     // 原始图片
                        1.0,              // 宽高比 1:1（正方形）
                        200,              // 输出宽度 200px
                        200,              // 输出高度 200px
                        croppedFile -> {
                            // 裁切完成，将裁切后的文件添加到上传组件
                            uploader.handleStart(croppedFile);
                        }
                );
            }
        });

        // ===== 说明：使用裁切对话框的快捷方式 =====
        // 也可以使用更简洁的重载方法（默认 1:1 比例，不缩放）：
        // ImageCropPane.showCropDialog(file, croppedFile -> uploader.handleStart(croppedFile));

        registerUploader(uploader);
        box.getChildren().add(uploader);

        // 额外添加"选择并裁切头像"独立按钮示例
        Button cropButton = new Button("独立裁切示例（无需通过上传组件）");
        cropButton.setStyle("-fx-background-color: #e6a23c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        cropButton.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("选择要裁切的图片");
            fc.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png"));
            java.io.File file = fc.showOpenDialog(primaryStage(cropButton));
            if (file != null) {
                ImageCropPane.showCropDialog(file, croppedFile ->
                        System.out.println("[裁切完成] 输出文件: " + croppedFile.getAbsolutePath())
                );
            }
        });

        box.getChildren().add(new Separator());
        box.getChildren().add(cropButton);

        return box;
    }

    // =========================================================================
    //  6. 数量限制 — 限制最大上传文件数量
    // =========================================================================

    /**
     * 演示文件数量限制功能：
     * - limit 属性限制最大上传数量
     * - 超出限制时触发 onExceed 回调
     * - 与 beforeRemove 配合控制文件列表
     */
    private VBox createLimitDemo() {
        VBox box = createDemoBox("文件数量限制",
                "通过 limit 属性限制最多上传 3 个文件，超出时弹出提示。");

        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setMultiple(true);                    // 多选
        uploader.setLimit(3);                          // 最多 3 个文件
        uploader.setAccept("image/*");                 // 仅图片
        uploader.setButtonText("点击上传");
        uploader.setTip("最多上传 3 个文件");
        uploader.setListType(ListType.TEXT);

        // ===== 超出限制回调 =====
        uploader.setOnExceed((files, fileList) ->
                showAlert("超出限制",
                        String.format("最多上传 3 个文件，本次选择了 %d 个文件，当前已有 %d 个文件。",
                                files.size(), fileList.size()))
        );

        // ===== 删除前校验 =====
        uploader.setBeforeRemove(file -> {
            System.out.println("[删除前确认] " + file.getName());
            return true; // 返回 true 允许删除
        });

        registerUploader(uploader);
        box.getChildren().add(uploader);
        return box;
    }

    // =========================================================================
    //  7. 手动上传 — autoUpload=false，选择后不自动上传
    // =========================================================================

    /**
     * 演示手动上传模式：
     * - autoUpload=false：选择文件后不自动上传
     * - 文件保持 READY 状态，等待调用 submit() 触发上传
     * - 按钮区域会自动出现"上传到服务器"按钮
     */
    private VBox createManualUploadDemo() {
        VBox box = createDemoBox("手动上传",
                "关闭自动上传，选择文件后不立即上传。点击'上传到服务器'按钮手动触发上传流程。");

        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setAutoUpload(false);                 // 关闭自动上传
        uploader.setMultiple(true);                    // 多选
        uploader.setLimit(3);                          // 最多 3 个
        uploader.setButtonText("选择文件");              // 按钮文字
        uploader.setTip("最多 3 个文件，点击'上传到服务器'开始上传");
        uploader.setListType(ListType.TEXT);

        // 超出限制时的覆盖模式
        uploader.setOnExceed((files, fileList) -> {
            uploader.clearFiles();
            if (!files.isEmpty()) {
                uploader.handleStart(files.get(0));
            }
        });

        registerUploader(uploader);
        box.getChildren().add(uploader);
        return box;
    }

    // =========================================================================
    //  8. 文件夹上传 — directory=true
    // =========================================================================

    /**
     * 演示文件夹上传模式：
     * - directory=true：点击后弹出文件夹选择器
     * - 选中文件夹后，内部所有文件会被递归扁平化处理
     * - 所有文件添加到上传列表中
     */
    private VBox createFolderUploadDemo() {
        VBox box = createDemoBox("文件夹上传",
                "启用 directory 属性后，点击按钮选择文件夹，文件夹内所有文件将被递归扫描并添加到上传列表中。");

        FileUploader uploader = new FileUploader();

        // ===== 属性配置 =====
        uploader.setDirectory(true);                   // 启用文件夹上传
        uploader.setMultiple(true);
        uploader.setButtonText("选择文件夹");
        uploader.setListType(ListType.TEXT);

        // ===== 文件列表变化回调 =====
        uploader.setOnChange((file, fileList) ->
                System.out.println("[列表变化] 当前共 " + fileList.size() + " 个文件")
        );

        registerUploader(uploader);
        box.getChildren().add(uploader);
        return box;
    }

    // =========================================================================
    //  辅助方法
    // =========================================================================

    /**
     * 创建 Tab 页
     */
    private Tab createTab(String title, VBox content) {
        Tab tab = new Tab(title);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * 创建演示区域标题和描述
     */
    private VBox createDemoBox(String title, String description) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #303133;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #909399; -fx-line-spacing: 4;");
        descLabel.setWrapText(true);

        Separator separator = new Separator();

        box.getChildren().addAll(titleLabel, descLabel, separator);
        return box;
    }

    /**
     * 显示警告弹窗
     */
    private void showAlert(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * 从节点获取所属 Stage
     */
    private Stage primaryStage(javafx.scene.Node node) {
        if (node.getScene() != null && node.getScene().getWindow() instanceof Stage s) {
            return s;
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
