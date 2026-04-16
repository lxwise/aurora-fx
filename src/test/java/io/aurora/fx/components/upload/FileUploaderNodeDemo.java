package io.aurora.fx.components.upload;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ============================================================================
 * Aurora-FX FileUploader 独立组件演示
 * ============================================================================
 * <p>
 * 本 Demo 展示了 8 个独立封装的上传组件节点，每个组件都可以在任意 JavaFX 项目中
 * 独立引入使用，实现开箱即用的效果。
 * </p>
 *
 * <h2>组件一览：</h2>
 * <ol>
 *   <li>{@link BasicUploadNode} — 基础按钮上传</li>
 *   <li>{@link PictureCardUploadNode} — 照片墙卡片</li>
 *   <li>{@link DragUploadNode} — 拖拽上传</li>
 *   <li>{@link PictureListUploadNode} — 图片缩略图列表</li>
 *   <li>{@link AvatarUploadNode} — 头像上传+裁切</li>
 *   <li>{@link LimitUploadNode} — 数量限制上传</li>
 *   <li>{@link ManualUploadNode} — 手动上传</li>
 *   <li>{@link FolderUploadNode} — 文件夹上传</li>
 * </ol>
 *
 * <h2>使用方式：</h2>
 * <pre>{@code
 * // 方式一：直接 new
 * BasicUploadNode node = new BasicUploadNode();
 *
 * // 方式二：通过工厂类
 * BasicUploadNode node = FileUploaderFactory.createBasic();
 *
 * // 方式三：快速集成到容器
 * FileUploaderFactory.integrateBasic(container, "http://api.example.com/upload",
 *     (file, resp) -> System.out.println("上传成功"));
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class FileUploaderNodeDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                createTab("基础上传", createBasicDemo()),
                createTab("照片墙", createPictureCardDemo()),
                createTab("拖拽上传", createDragDemo()),
                createTab("图片列表", createPictureListDemo()),
                createTab("头像上传", createAvatarDemo()),
                createTab("数量限制", createLimitDemo()),
                createTab("手动上传", createManualDemo()),
                createTab("文件夹上传", createFolderDemo())
        );

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(tabPane, 1000, 700);
        primaryStage.setTitle("Aurora-FX 独立上传组件演示（Node 模式）");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // =========================================================================
    //  1. 基础上传
    // =========================================================================

    private VBox createBasicDemo() {
        VBox box = demoBox("BasicUploadNode — 基础上传",
                "零配置即可使用。直接 new BasicUploadNode() 或 FileUploaderFactory.createBasic()。",
                "BasicUploadNode node = new BasicUploadNode();\n"
                        + "node.setOnSuccess((f, r) -> System.out.println(\"成功: \" + f.getName()));");

        // 方式一：直接 new
        BasicUploadNode node = new BasicUploadNode();
        node.setOnSuccess((file, response) ->
                System.out.println("[基础上传] 成功: " + file.getName()));
        node.setOnRemove((file, files) ->
                System.out.println("[基础上传] 删除: " + file.getName()));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  2. 照片墙
    // =========================================================================

    private VBox createPictureCardDemo() {
        VBox box = demoBox("PictureCardUploadNode — 照片墙",
                "图片以卡片网格展示，悬浮显示预览/删除。默认最多 8 张图片，卡片 148×148 px。",
                "PictureCardUploadNode node = FileUploaderFactory.createPictureCard();\n"
                        + "node.setLimit(6);\n"
                        + "node.setThumbnailSize(160);");

        // 方式二：通过工厂类
        PictureCardUploadNode node = FileUploaderFactory.createPictureCard();
        node.setOnSuccess((file, response) ->
                System.out.println("[照片墙] 成功: " + file.getName()));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  3. 拖拽上传
    // =========================================================================

    private VBox createDragDemo() {
        VBox box = demoBox("DragUploadNode — 拖拽上传",
                "拖拽文件到虚线区域或点击选择文件。默认支持多选、仅图片、TEXT 列表模式。",
                "DragUploadNode node = new DragUploadNode();\n"
                        + "node.setDragText(\"拖入文件即可上传\");");

        DragUploadNode node = new DragUploadNode();
        node.setOnSuccess((file, response) ->
                System.out.println("[拖拽上传] 成功: " + file.getName()));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  4. 图片列表
    // =========================================================================

    private VBox createPictureListDemo() {
        VBox box = demoBox("PictureListUploadNode — 图片列表",
                "文件列表左侧显示缩略图，右侧显示文件名和大小。悬浮显示预览/删除按钮。",
                "PictureListUploadNode node = FileUploaderFactory.createPictureList();");

        PictureListUploadNode node = FileUploaderFactory.createPictureList();
        node.setOnSuccess((file, response) ->
                System.out.println("[图片列表] 成功: " + file.getName()));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  5. 头像上传
    // =========================================================================

    private VBox createAvatarDemo() {
        VBox box = demoBox("AvatarUploadNode — 头像上传（含裁切）",
                "单文件上传，限 JPG/PNG，最大 2MB。超出限制自动替换并弹出裁切对话框，1:1 正方形裁切，200×200 输出。",
                "AvatarUploadNode node = new AvatarUploadNode();\n"
                        + "// 自定义裁切参数\n"
                        + "node.setCropAspectRatio(16.0 / 9.0);\n"
                        + "node.setCropOutputWidth(400);");

        AvatarUploadNode node = FileUploaderFactory.createAvatar();
        node.setOnSuccess((file, response) ->
                System.out.println("[头像上传] 成功: " + file.getName()));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  6. 数量限制
    // =========================================================================

    private VBox createLimitDemo() {
        VBox box = demoBox("LimitUploadNode — 数量限制",
                "通过 limit 属性限制最大上传数量，超出时自动弹窗提示。默认限制 3 个文件。",
                "LimitUploadNode node = new LimitUploadNode(5);\n"
                        + "// 或者\n"
                        + "LimitUploadNode node = FileUploaderFactory.createLimit(5);");

        LimitUploadNode node = FileUploaderFactory.createLimit(3);
        node.setOnSuccess((file, response) ->
                System.out.println("[数量限制] 成功: " + file.getName()));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  7. 手动上传
    // =========================================================================

    private VBox createManualDemo() {
        VBox box = demoBox("ManualUploadNode — 手动上传",
                "选择文件后不自动上传，文件保持 READY 状态。点击'上传到服务器'按钮手动触发。也可通过 node.submit() 代码触发。",
                "ManualUploadNode node = new ManualUploadNode();\n"
                        + "node.setAction(\"http://localhost:8080/upload\");\n"
                        + "// 代码触发：node.submit();");

        ManualUploadNode node = FileUploaderFactory.createManual();
        node.setOnSuccess((file, response) ->
                System.out.println("[手动上传] 成功: " + file.getName()));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  8. 文件夹上传
    // =========================================================================

    private VBox createFolderDemo() {
        VBox box = demoBox("FolderUploadNode — 文件夹上传",
                "点击按钮选择文件夹，内部所有文件递归扫描后添加到上传列表。",
                "FolderUploadNode node = new FolderUploadNode();\n"
                        + "node.setOnChange((f, list) -> System.out.println(\"共 \" + list.size()));");

        FolderUploadNode node = FileUploaderFactory.createFolder();
        node.setOnChange((file, fileList) ->
                System.out.println("[文件夹上传] 当前共 " + fileList.size() + " 个文件"));

        box.getChildren().add(node);
        return box;
    }

    // =========================================================================
    //  辅助方法
    // =========================================================================

    private Tab createTab(String title, VBox content) {
        Tab tab = new Tab(title);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * 创建带标题、描述和代码示例的演示容器
     */
    private VBox demoBox(String title, String description, String codeExample) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #303133;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #909399; -fx-line-spacing: 4;");
        descLabel.setWrapText(true);

        // 代码示例区域
        TextArea codeArea = new TextArea(codeExample);
        codeArea.setEditable(false);
        codeArea.setPrefRowCount(Math.min(codeExample.split("\n").length + 1, 6));
        codeArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px; "
                + "-fx-control-inner-background: #f8f9fa; -fx-border-color: #e4e7ed; -fx-border-radius: 4;");
        codeArea.setMaxHeight(140);

        Separator separator = new Separator();

        box.getChildren().addAll(titleLabel, descLabel, codeArea, separator);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

