package io.aurora.fx.components.upload;

import javafx.scene.layout.Pane;

import java.util.function.BiConsumer;

/**
 * 文件上传组件工厂类 — 提供统一的组件创建入口。
 * <p>
 * 对标 {@link io.aurora.fx.components.verifyCode.VerifyCodeFactory} 的设计模式，
 * 所有上传组件均可通过本工厂类快速创建。每个组件开箱即用，自带合理默认配置。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 快速创建基础上传组件
 * BasicUploadNode basic = FileUploaderFactory.createBasic();
 * root.getChildren().add(basic);
 *
 * // 创建带接口地址的照片墙组件
 * PictureCardUploadNode card = FileUploaderFactory.createPictureCard("http://localhost:8080/upload");
 * card.setLimit(6);
 * root.getChildren().add(card);
 *
 * // 快速集成到容器
 * FileUploaderFactory.integrateBasic(container, "http://api.example.com/upload",
 *     (file, resp) -> System.out.println("上传成功: " + file.getName()));
 * }</pre>
 *
 * <h3>可用组件一览：</h3>
 * <table>
 *   <tr><th>组件</th><th>工厂方法</th><th>说明</th></tr>
 *   <tr><td>{@link BasicUploadNode}</td><td>{@link #createBasic()}</td><td>基础按钮上传</td></tr>
 *   <tr><td>{@link PictureCardUploadNode}</td><td>{@link #createPictureCard()}</td><td>照片墙卡片</td></tr>
 *   <tr><td>{@link DragUploadNode}</td><td>{@link #createDrag()}</td><td>拖拽上传</td></tr>
 *   <tr><td>{@link PictureListUploadNode}</td><td>{@link #createPictureList()}</td><td>图片缩略图列表</td></tr>
 *   <tr><td>{@link AvatarUploadNode}</td><td>{@link #createAvatar()}</td><td>头像上传+裁切</td></tr>
 *   <tr><td>{@link LimitUploadNode}</td><td>{@link #createLimit()}</td><td>数量限制上传</td></tr>
 *   <tr><td>{@link ManualUploadNode}</td><td>{@link #createManual()}</td><td>手动上传</td></tr>
 *   <tr><td>{@link FolderUploadNode}</td><td>{@link #createFolder()}</td><td>文件夹上传</td></tr>
 * </table>
 *
 * @author lstar
 * @since 2025
 */
public final class FileUploaderFactory {

    private FileUploaderFactory() {
    }

    // ==================== 基础上传 ====================

    /**
     * 创建基础上传组件（本地模式）
     */
    public static BasicUploadNode createBasic() {
        return new BasicUploadNode();
    }

    /**
     * 创建基础上传组件
     * @param action 上传接口地址
     */
    public static BasicUploadNode createBasic(String action) {
        return new BasicUploadNode(action);
    }

    /**
     * 创建基础上传组件并设置成功回调
     */
    public static BasicUploadNode createBasic(String action, BiConsumer<UploadFile, Object> onSuccess) {
        BasicUploadNode node = new BasicUploadNode(action);
        node.setOnSuccess(onSuccess);
        return node;
    }

    // ==================== 照片墙 ====================

    /**
     * 创建照片墙组件（本地模式）
     */
    public static PictureCardUploadNode createPictureCard() {
        return new PictureCardUploadNode();
    }

    /**
     * 创建照片墙组件
     * @param action 上传接口地址
     */
    public static PictureCardUploadNode createPictureCard(String action) {
        return new PictureCardUploadNode(action);
    }

    /**
     * 创建照片墙组件（指定限制数量）
     */
    public static PictureCardUploadNode createPictureCard(String action, int limit) {
        PictureCardUploadNode node = new PictureCardUploadNode(action);
        node.setLimit(limit);
        return node;
    }

    // ==================== 拖拽上传 ====================

    /**
     * 创建拖拽上传组件（本地模式）
     */
    public static DragUploadNode createDrag() {
        return new DragUploadNode();
    }

    /**
     * 创建拖拽上传组件
     * @param action 上传接口地址
     */
    public static DragUploadNode createDrag(String action) {
        return new DragUploadNode(action);
    }

    // ==================== 图片列表 ====================

    /**
     * 创建图片列表组件（本地模式）
     */
    public static PictureListUploadNode createPictureList() {
        return new PictureListUploadNode();
    }

    /**
     * 创建图片列表组件
     * @param action 上传接口地址
     */
    public static PictureListUploadNode createPictureList(String action) {
        return new PictureListUploadNode(action);
    }

    // ==================== 头像上传 ====================

    /**
     * 创建头像上传组件（本地模式，默认 1:1 裁切 200×200 输出）
     */
    public static AvatarUploadNode createAvatar() {
        return new AvatarUploadNode();
    }

    /**
     * 创建头像上传组件
     * @param action 上传接口地址
     */
    public static AvatarUploadNode createAvatar(String action) {
        return new AvatarUploadNode(action);
    }

    /**
     * 创建头像上传组件（自定义裁切参数）
     * @param action       上传接口地址
     * @param aspectRatio  裁切宽高比（1.0=正方形，0=自由比例）
     * @param outputWidth  输出宽度（0=不缩放）
     * @param outputHeight 输出高度（0=不缩放）
     */
    public static AvatarUploadNode createAvatar(String action, double aspectRatio,
                                                 int outputWidth, int outputHeight) {
        AvatarUploadNode node = new AvatarUploadNode(action);
        node.setCropAspectRatio(aspectRatio);
        node.setCropOutputWidth(outputWidth);
        node.setCropOutputHeight(outputHeight);
        return node;
    }

    // ==================== 数量限制 ====================

    /**
     * 创建数量限制上传组件（默认限制 3 个文件）
     */
    public static LimitUploadNode createLimit() {
        return new LimitUploadNode();
    }

    /**
     * 创建数量限制上传组件
     * @param limit 最大文件数量
     */
    public static LimitUploadNode createLimit(int limit) {
        return new LimitUploadNode(limit);
    }

    /**
     * 创建数量限制上传组件
     * @param limit  最大文件数量
     * @param action 上传接口地址
     */
    public static LimitUploadNode createLimit(int limit, String action) {
        return new LimitUploadNode(limit, action);
    }

    // ==================== 手动上传 ====================

    /**
     * 创建手动上传组件（本地模式）
     */
    public static ManualUploadNode createManual() {
        return new ManualUploadNode();
    }

    /**
     * 创建手动上传组件
     * @param action 上传接口地址
     */
    public static ManualUploadNode createManual(String action) {
        return new ManualUploadNode(action);
    }

    // ==================== 文件夹上传 ====================

    /**
     * 创建文件夹上传组件（本地模式）
     */
    public static FolderUploadNode createFolder() {
        return new FolderUploadNode();
    }

    /**
     * 创建文件夹上传组件
     * @param action 上传接口地址
     */
    public static FolderUploadNode createFolder(String action) {
        return new FolderUploadNode(action);
    }

    // ==================== 快速集成方法 ====================

    /**
     * 快速将基础上传组件集成到容器
     * @param container 目标容器
     * @param action    上传接口地址
     * @param onSuccess 上传成功回调
     * @return 创建的组件
     */
    public static BasicUploadNode integrateBasic(Pane container, String action,
                                                  BiConsumer<UploadFile, Object> onSuccess) {
        BasicUploadNode node = createBasic(action, onSuccess);
        container.getChildren().add(node);
        return node;
    }

    /**
     * 快速将照片墙组件集成到容器
     * @param container 目标容器
     * @param action    上传接口地址
     * @param limit     最大数量
     * @return 创建的组件
     */
    public static PictureCardUploadNode integratePictureCard(Pane container, String action, int limit) {
        PictureCardUploadNode node = createPictureCard(action, limit);
        container.getChildren().add(node);
        return node;
    }

    /**
     * 快速将拖拽上传组件集成到容器
     * @param container 目标容器
     * @param action    上传接口地址
     * @return 创建的组件
     */
    public static DragUploadNode integrateDrag(Pane container, String action) {
        DragUploadNode node = createDrag(action);
        container.getChildren().add(node);
        return node;
    }

    /**
     * 快速将头像上传组件集成到容器
     * @param container 目标容器
     * @param action    上传接口地址
     * @return 创建的组件
     */
    public static AvatarUploadNode integrateAvatar(Pane container, String action) {
        AvatarUploadNode node = createAvatar(action);
        container.getChildren().add(node);
        return node;
    }
}
