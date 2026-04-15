package io.aurora.fx.components.upload;

import io.aurora.fx.components.imageViewer.ImageViewer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * FileUploader 组件的皮肤实现，负责全部 UI 渲染和交互逻辑。
 * <p>
 * 根据 {@link ListType} 属性切换三种展示模式：
 * <ul>
 *   <li>TEXT - 纯文本文件列表</li>
 *   <li>PICTURE - 带缩略图的文件列表</li>
 *   <li>PICTURE_CARD - 照片墙卡片模式</li>
 * </ul>
 *
 * @author lstar
 * @since 2025
 */
public class FileUploaderSkin extends SkinBase<FileUploader> {

    // SVG 图标路径常量（基于 24x24 视口）
    private static final String SVG_UPLOAD_CLOUD = "M19.35 10.04A7.49 7.49 0 0 0 12 4C9.11 4 6.6 5.64 5.35 8.04A5.994 5.994 0 0 0 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z";
    private static final String SVG_PLUS = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z";
    private static final String SVG_CLOSE = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";
    private static final String SVG_FILE = "M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm4 18H6V4h7v5h5v11z";
    private static final String SVG_EYE = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private static final String SVG_DELETE = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    private static final String SVG_SUCCESS_CHECK = "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z";

    private final VBox rootContainer;
    private final FlowPane cardContainer;
    private final VBox listContainer;
    private Label tipLabel;

    private final ListChangeListener<UploadFile> fileListListener;

    /**
     * 跟踪每个 UploadFile 上注册的监听器，以便在文件移除或 dispose 时清理，避免内存泄漏。
     * key = UploadFile, value = 该文件上注册的所有 ChangeListener
     */
    private final Map<UploadFile, List<javafx.beans.value.ChangeListener<?>>> perFileListeners = new HashMap<>();

    /**
     * UI 重建节流标志：当多个快速连续的状态/进度变化触发时，
     * 只在第一次变化时通过 Platform.runLater 提交重建，
     * 后续变化在同一帧内复用已提交的重建请求。
     */
    private boolean rebuildScheduled = false;

    public FileUploaderSkin(FileUploader control) {
        super(control);

        rootContainer = new VBox();
        rootContainer.getStyleClass().add("file-uploader-root");
        rootContainer.setSpacing(8);

        cardContainer = new FlowPane();
        cardContainer.getStyleClass().add("file-uploader-card-container");
        cardContainer.setHgap(8);
        cardContainer.setVgap(8);

        listContainer = new VBox();
        listContainer.getStyleClass().add("file-uploader-list-container");
        listContainer.setSpacing(4);

        // 构建UI
        buildUI();

        // 监听文件列表变化，同时为新添加的文件注册状态监听（确保所有模式下状态变化都能触发 UI 刷新）
        fileListListener = change -> {
            while (change.next()) {
                // 清理已移除文件的监听器，防止内存泄漏
                for (UploadFile removed : change.getRemoved()) {
                    detachFileListeners(removed);
                }
                // 为新添加的文件注册状态/进度监听器
                for (UploadFile added : change.getAddedSubList()) {
                    attachFileListeners(added);
                }
            }
            scheduleRebuild();
        };
        control.getFileList().addListener(fileListListener);

        // 监听属性变化重建UI
        registerChangeListener(control.listTypeProperty(), e -> buildUI());
        registerChangeListener(control.dragProperty(), e -> buildUI());
        registerChangeListener(control.showFileListProperty(), e -> buildUI());
        registerChangeListener(control.uploaderDisabledProperty(), e -> buildUI());
        registerChangeListener(control.tipProperty(), e -> updateTip());
        registerChangeListener(control.buttonTextProperty(), e -> buildUI());
        registerChangeListener(control.dragTextProperty(), e -> buildUI());
        registerChangeListener(control.thumbnailSizeProperty(), e -> buildUI());

        getChildren().add(rootContainer);
    }

    /**
     * 为 UploadFile 注册状态/进度监听器，并跟踪以便后续清理
     */
    private void attachFileListeners(UploadFile file) {
        List<javafx.beans.value.ChangeListener<?>> listeners = new ArrayList<>();

        javafx.beans.value.ChangeListener<UploadFileStatus> statusListener = (obs, o, n) -> scheduleRebuild();
        file.statusProperty().addListener(statusListener);
        listeners.add(statusListener);

        javafx.beans.value.ChangeListener<Number> progressListener = (obs, o, n) -> {
            if (file.getStatus() == UploadFileStatus.UPLOADING) {
                scheduleRebuild();
            }
        };
        file.progressProperty().addListener(progressListener);
        listeners.add(progressListener);

        perFileListeners.put(file, listeners);
    }

    /**
     * 移除 UploadFile 上的所有监听器
     */
    @SuppressWarnings("unchecked")
    private void detachFileListeners(UploadFile file) {
        List<javafx.beans.value.ChangeListener<?>> listeners = perFileListeners.remove(file);
        if (listeners != null) {
            for (javafx.beans.value.ChangeListener<?> listener : listeners) {
                // 安全移除：尝试从两个属性上移除
                try { file.statusProperty().removeListener((javafx.beans.value.ChangeListener<? super UploadFileStatus>) listener); } catch (Exception ignored) {}
                try { file.progressProperty().removeListener((javafx.beans.value.ChangeListener<? super Number>) listener); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 节流 UI 重建：合并同一帧内的多次重建请求为一次执行
     */
    private void scheduleRebuild() {
        if (!rebuildScheduled) {
            rebuildScheduled = true;
            Platform.runLater(() -> {
                rebuildScheduled = false;
                rebuildFileList();
            });
        }
    }

    /**
     * 构建完整 UI（根据 listType 分发）
     */
    private void buildUI() {
        rootContainer.getChildren().clear();
        tipLabel = null;

        FileUploader control = getSkinnable();
        ListType type = control.getListType();

        if (type == ListType.PICTURE_CARD) {
            buildPictureCardUI();
        } else {
            buildListUI(type);
        }
    }

    // =========================================================================
    //  PICTURE_CARD 照片墙模式
    // =========================================================================

    private void buildPictureCardUI() {
        FileUploader control = getSkinnable();
        cardContainer.getChildren().clear();
        double size = control.getThumbnailSize();

        for (UploadFile file : control.getFileList()) {
            cardContainer.getChildren().add(createPictureCard(file, size));
        }

        // 未达上限时显示上传触发卡片
        if (!isLimitReached()) {
            cardContainer.getChildren().add(createUploadCard(size));
        }

        rootContainer.getChildren().add(cardContainer);
        addTipLabel();
    }

    /**
     * 创建照片墙中的图片卡片
     */
    private StackPane createPictureCard(UploadFile file, double size) {
        StackPane card = new StackPane();
        card.getStyleClass().add("upload-card");
        card.setPrefSize(size, size);
        card.setMaxSize(size, size);
        card.setMinSize(size, size);

        // === 图片内容或文件图标 ===
        if (file.isImage() && file.getThumbnail() != null) {
            ImageView imageView = new ImageView(file.getThumbnail());
            imageView.setFitWidth(size - 4);
            imageView.setFitHeight(size - 4);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            file.thumbnailProperty().addListener((obs, o, n) -> {
                if (n != null) imageView.setImage(n);
            });
            card.getChildren().add(imageView);
        } else {
            card.getChildren().add(createFileIconBox(file.getName()));
        }

        // === 上传中：进度条叠加 ===
        if (file.getStatus() == UploadFileStatus.UPLOADING) {
            ProgressBar progressBar = new ProgressBar();
            progressBar.getStyleClass().add("upload-card-progress");
            progressBar.progressProperty().bind(file.progressProperty());
            progressBar.setPrefWidth(size - 20);
            progressBar.setMaxHeight(6);
            StackPane.setAlignment(progressBar, Pos.BOTTOM_CENTER);
            StackPane.setMargin(progressBar, new Insets(0, 10, 10, 10));
            card.getChildren().add(progressBar);
        }

        // === 上传成功：右下角对勾标记 ===
        if (file.getStatus() == UploadFileStatus.SUCCESS) {
            StackPane successBadge = new StackPane();
            successBadge.getStyleClass().add("upload-card-success-badge");
            successBadge.setPrefSize(24, 24);
            successBadge.setMaxSize(24, 24);
            successBadge.setMinSize(24, 24);

            SVGPath checkSvg = new SVGPath();
            checkSvg.setContent(SVG_SUCCESS_CHECK);
            checkSvg.setFill(Color.WHITE);
            double checkScale = 12.0 / 24.0;
            checkSvg.setScaleX(checkScale);
            checkSvg.setScaleY(checkScale);
            successBadge.getChildren().add(checkSvg);

            StackPane.setAlignment(successBadge, Pos.BOTTOM_RIGHT);
            card.getChildren().add(successBadge);
        }

        // === 上传失败：红色边框 ===
        if (file.getStatus() == UploadFileStatus.FAIL) {
            card.getStyleClass().add("upload-card-error");
        }

        // === 悬浮遮罩 + 操作按钮层 ===
        Region mask = new Region();
        mask.getStyleClass().add("upload-card-mask");
        mask.setVisible(false);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("upload-card-actions");
        actions.setVisible(false);
        actions.setPickOnBounds(false);

        // 预览按钮（仅图片）
        if (file.isImage()) {
            StackPane previewBtn = createIconButton(SVG_EYE, "white", 22, 36);
            previewBtn.setOnMouseClicked(e -> {
                e.consume();
                onPreviewFile(file);
            });
            actions.getChildren().add(previewBtn);
        }

        // 删除按钮
        if (!getSkinnable().isUploaderDisabled()) {
            StackPane deleteBtn = createIconButton(SVG_DELETE, "white", 22, 36);
            deleteBtn.setOnMouseClicked(e -> {
                e.consume();
                onRemoveFile(file);
            });
            actions.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(mask, actions);

        // 悬浮显示/隐藏操作层
        card.setOnMouseEntered(e -> {
            mask.setVisible(true);
            actions.setVisible(true);
        });
        card.setOnMouseExited(e -> {
            mask.setVisible(false);
            actions.setVisible(false);
        });

        return card;
    }

    /**
     * 创建上传触发卡片（+号按钮）
     */
    private StackPane createUploadCard(double size) {
        StackPane card = new StackPane();
        card.getStyleClass().add("upload-card-trigger");
        card.setPrefSize(size, size);
        card.setMaxSize(size, size);
        card.setMinSize(size, size);
        card.setCursor(Cursor.HAND);

        SVGPath plusSvg = new SVGPath();
        plusSvg.setContent(SVG_PLUS);
        plusSvg.setFill(Color.web("#8c939d"));
        double plusScale = 32.0 / 24.0;
        plusSvg.setScaleX(plusScale);
        plusSvg.setScaleY(plusScale);
        card.getChildren().add(plusSvg);

        if (getSkinnable().isUploaderDisabled()) {
            card.setDisable(true);
            card.setOpacity(0.6);
        } else {
            card.setOnMouseClicked(e -> openFileChooser());
            setupDragDrop(card);
        }

        return card;
    }

    // =========================================================================
    //  TEXT / PICTURE 列表模式
    // =========================================================================

    private void buildListUI(ListType type) {
        FileUploader control = getSkinnable();

        // 触发区域
        Node triggerNode;
        if (control.isDrag()) {
            triggerNode = createDragZone();
        } else {
            triggerNode = createUploadButton();
        }
        rootContainer.getChildren().add(triggerNode);
        addTipLabel();

        // 文件列表
        if (control.isShowFileList()) {
            listContainer.getChildren().clear();
            for (UploadFile file : control.getFileList()) {
                listContainer.getChildren().add(
                        type == ListType.PICTURE ? createPictureListItem(file) : createTextListItem(file)
                );
            }
            rootContainer.getChildren().add(listContainer);
        }
    }

    /**
     * 创建上传按钮
     */
    private Node createUploadButton() {
        Button button = new Button(getSkinnable().getButtonText());
        button.getStyleClass().add("upload-button");

        SVGPath uploadSvg = new SVGPath();
        uploadSvg.setContent(SVG_UPLOAD_CLOUD);
        uploadSvg.setFill(Color.WHITE);
        double iconScale = 16.0 / 24.0;
        uploadSvg.setScaleX(iconScale);
        uploadSvg.setScaleY(iconScale);
        button.setGraphic(uploadSvg);

        if (getSkinnable().isUploaderDisabled()) {
            button.setDisable(true);
        } else {
            button.setOnAction(e -> openFileChooser());
        }

        // 手动上传模式：增加"上传到服务器"按钮
        if (!getSkinnable().isAutoUpload()) {
            Button submitBtn = new Button("上传到服务器");
            submitBtn.getStyleClass().add("upload-submit-button");
            submitBtn.setOnAction(e -> getSkinnable().submit());
            HBox box = new HBox(10, button, submitBtn);
            box.setAlignment(Pos.CENTER_LEFT);
            return box;
        }

        return button;
    }

    /**
     * 创建拖拽上传区域
     */
    private Node createDragZone() {
        VBox zone = new VBox(10);
        zone.getStyleClass().add("upload-drag-zone");
        zone.setAlignment(Pos.CENTER);
        zone.setPadding(new Insets(40, 20, 40, 20));
        zone.setCursor(Cursor.HAND);

        SVGPath uploadSvg = new SVGPath();
        uploadSvg.setContent(SVG_UPLOAD_CLOUD);
        uploadSvg.setFill(Color.web("#c0c4cc"));
        double dragIconScale = 48.0 / 24.0;
        uploadSvg.setScaleX(dragIconScale);
        uploadSvg.setScaleY(dragIconScale);

        // 为放大后的SVG预留布局空间
        StackPane iconHolder = new StackPane(uploadSvg);
        iconHolder.setPrefSize(50, 50);
        iconHolder.setMaxSize(50, 50);

        Label text = new Label(getSkinnable().getDragText());
        text.getStyleClass().add("upload-drag-text");

        zone.getChildren().addAll(iconHolder, text);

        if (getSkinnable().isUploaderDisabled()) {
            zone.setDisable(true);
            zone.setOpacity(0.6);
        } else {
            zone.setOnMouseClicked(e -> openFileChooser());
            setupDragDrop(zone);
        }

        return zone;
    }

    /**
     * 创建文本模式文件项（TEXT）
     */
    private Node createTextListItem(UploadFile file) {
        HBox item = new HBox(8);
        item.getStyleClass().add("upload-list-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(6, 10, 6, 10));
        item.setMinHeight(36);

        // 文件图标
        StackPane fileIconWrap = createIconDisplay(SVG_FILE, "#909399", 14, 20);

        // 文件名
        Label nameLabel = new Label(file.getName());
        nameLabel.getStyleClass().add("upload-list-item-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        // 状态指示
        Node statusNode = createStatusNode(file);

        // 删除按钮（包裹在足够大的点击区域中）
        StackPane closeBtn = createIconButton(SVG_CLOSE, "#909399", 14, 24);
        closeBtn.getStyleClass().add("upload-list-item-close");
        closeBtn.setOnMouseClicked(e -> {
            e.consume();
            onRemoveFile(file);
        });
        closeBtn.setVisible(false);

        item.getChildren().addAll(fileIconWrap, nameLabel, statusNode, closeBtn);

        // 悬浮显示删除按钮
        item.setOnMouseEntered(e -> closeBtn.setVisible(true));
        item.setOnMouseExited(e -> closeBtn.setVisible(false));

        // 上传中：底部进度条
        if (file.getStatus() == UploadFileStatus.UPLOADING) {
            ProgressBar progressBar = new ProgressBar();
            progressBar.getStyleClass().add("upload-list-progress");
            progressBar.progressProperty().bind(file.progressProperty());
            progressBar.setPrefHeight(2);
            progressBar.setMaxWidth(Double.MAX_VALUE);

            VBox wrapper = new VBox(0, item, progressBar);
            wrapper.getStyleClass().add("upload-list-item-wrapper");
            applyStatusStyle(wrapper, file);
            return wrapper;
        }

        applyStatusStyle(item, file);
        return item;
    }

    /**
     * 创建图片模式文件项（PICTURE）
     */
    private Node createPictureListItem(UploadFile file) {
        HBox item = new HBox(10);
        item.getStyleClass().add("upload-list-item");
        item.getStyleClass().add("upload-list-item-picture");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8));

        // 缩略图
        StackPane thumbBox = new StackPane();
        thumbBox.getStyleClass().add("upload-list-thumb");
        thumbBox.setPrefSize(70, 70);
        thumbBox.setMinSize(70, 70);
        thumbBox.setMaxSize(70, 70);

        if (file.isImage() && file.getThumbnail() != null) {
            ImageView thumbView = new ImageView(file.getThumbnail());
            thumbView.setFitWidth(66);
            thumbView.setFitHeight(66);
            thumbView.setPreserveRatio(true);
            thumbView.setSmooth(true);
            thumbBox.getChildren().add(thumbView);
        } else {
            thumbBox.getChildren().add(createIconDisplay(SVG_FILE, "#c0c4cc", 28, 36));
        }

        // 文件信息
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(file.getName());
        nameLabel.getStyleClass().add("upload-list-item-name");

        if (file.getStatus() == UploadFileStatus.UPLOADING) {
            ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(file.progressProperty());
            progressBar.setPrefHeight(4);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            infoBox.getChildren().addAll(nameLabel, progressBar);
        } else {
            Label sizeLabel = new Label(file.getReadableSize());
            sizeLabel.getStyleClass().add("upload-list-item-size");

            // 状态标识
            HBox statusLine = new HBox(6);
            statusLine.setAlignment(Pos.CENTER_LEFT);
            statusLine.getChildren().add(sizeLabel);

            if (file.getStatus() == UploadFileStatus.SUCCESS) {
                statusLine.getChildren().add(createIconDisplay(SVG_SUCCESS_CHECK, "#67c23a", 14, 18));
            } else if (file.getStatus() == UploadFileStatus.FAIL) {
                Label errLabel = new Label(file.getErrorMessage() != null ? file.getErrorMessage() : "上传失败");
                errLabel.setStyle("-fx-text-fill: #f56c6c; -fx-font-size: 12px;");
                statusLine.getChildren().add(errLabel);
            }

            infoBox.getChildren().addAll(nameLabel, statusLine);
        }

        // 操作按钮组
        VBox actionBox = new VBox(6);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setVisible(false);

        if (file.isImage()) {
            StackPane eyeBtn = createIconButton(SVG_EYE, "#409eff", 16, 28);
            eyeBtn.setOnMouseClicked(e -> {
                e.consume();
                onPreviewFile(file);
            });
            actionBox.getChildren().add(eyeBtn);
        }

        StackPane closeBtn = createIconButton(SVG_CLOSE, "#f56c6c", 16, 28);
        closeBtn.setOnMouseClicked(e -> {
            e.consume();
            onRemoveFile(file);
        });
        actionBox.getChildren().add(closeBtn);

        item.getChildren().addAll(thumbBox, infoBox, actionBox);

        item.setOnMouseEntered(e -> actionBox.setVisible(true));
        item.setOnMouseExited(e -> actionBox.setVisible(false));

        applyStatusStyle(item, file);
        return item;
    }

    // =========================================================================
    //  图标工具方法
    // =========================================================================

    /**
     * 创建可点击的图标按钮（带足够大的点击热区）
     *
     * @param svgPath    SVG 路径
     * @param color      填充颜色
     * @param iconSize   图标视觉大小（px）
     * @param hitSize    点击热区大小（px）
     * @return 包裹了 SVG 的 StackPane
     */
    private StackPane createIconButton(String svgPath, String color, double iconSize, double hitSize) {
        SVGPath svg = new SVGPath();
        svg.setContent(svgPath);
        svg.setFill(Color.web(color));
        double factor = iconSize / 24.0;
        svg.setScaleX(factor);
        svg.setScaleY(factor);

        StackPane wrapper = new StackPane(svg);
        wrapper.setPrefSize(hitSize, hitSize);
        wrapper.setMinSize(hitSize, hitSize);
        wrapper.setMaxSize(hitSize, hitSize);
        wrapper.setCursor(Cursor.HAND);
        wrapper.setPickOnBounds(true); // 确保整个区域可点击
        return wrapper;
    }

    /**
     * 创建纯展示用图标（不可点击）
     *
     * @param svgPath    SVG 路径
     * @param color      填充颜色
     * @param iconSize   图标视觉大小（px）
     * @param boxSize    容器大小（px）
     * @return 包裹了 SVG 的 StackPane
     */
    private StackPane createIconDisplay(String svgPath, String color, double iconSize, double boxSize) {
        SVGPath svg = new SVGPath();
        svg.setContent(svgPath);
        svg.setFill(Color.web(color));
        double factor = iconSize / 24.0;
        svg.setScaleX(factor);
        svg.setScaleY(factor);

        StackPane wrapper = new StackPane(svg);
        wrapper.setPrefSize(boxSize, boxSize);
        wrapper.setMinSize(boxSize, boxSize);
        wrapper.setMaxSize(boxSize, boxSize);
        return wrapper;
    }

    // =========================================================================
    //  公共逻辑
    // =========================================================================

    /**
     * 重建文件列表 UI
     */
    private void rebuildFileList() {
        buildUI();
    }

    /**
     * 打开文件选择器
     */
    private void openFileChooser() {
        FileUploader control = getSkinnable();
        if (control.isUploaderDisabled()) return;

        // 文件夹上传模式
        if (control.isDirectory()) {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("选择文件夹");
            Stage stage = getStage();
            File dir = dc.showDialog(stage);
            if (dir != null) {
                List<File> files = flattenDirectory(dir);
                control.addFiles(files);
            }
            return;
        }

        // 普通文件选择
        FileChooser fc = new FileChooser();
        fc.setTitle("选择文件");

        String accept = control.getAccept();
        if (accept != null && !accept.isEmpty()) {
            String[] extensions = UploadUtils.parseAcceptToExtensions(accept);
            String description = UploadUtils.getAcceptDescription(accept);
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extensions));
        }

        Stage stage = getStage();

        if (control.isMultiple()) {
            List<File> files = fc.showOpenMultipleDialog(stage);
            if (files != null) {
                control.addFiles(files);
            }
        } else {
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                control.addFiles(List.of(file));
            }
        }
    }

    /**
     * 递归扁平化文件夹中的所有文件
     */
    private List<File> flattenDirectory(File dir) {
        List<File> result = new ArrayList<>();
        if (dir == null || !dir.isDirectory()) return result;
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            paths.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(result::add);
        } catch (IOException e) {
            // 忽略遍历错误
        }
        return result;
    }

    /**
     * 设置拖拽处理
     */
    private void setupDragDrop(Node node) {
        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                if (!node.getStyleClass().contains("upload-drag-over")) {
                    node.getStyleClass().add("upload-drag-over");
                }
            }
            event.consume();
        });

        node.setOnDragExited(event -> {
            node.getStyleClass().remove("upload-drag-over");
            event.consume();
        });

        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                getSkinnable().addFiles(db.getFiles());
            }
            node.getStyleClass().remove("upload-drag-over");
            event.setDropCompleted(true);
            event.consume();
        });
    }

    /**
     * 预览文件（优先使用用户自定义回调，否则用内置 ImageViewer）
     */
    private void onPreviewFile(UploadFile file) {
        var previewFn = getSkinnable().getOnPreview();
        if (previewFn != null) {
            previewFn.accept(file);
            return;
        }
        if (file.isImage()) {
            showImagePreview(file);
        }
    }

    /**
     * 使用 ImageViewer 组件进行沉浸式图片预览（无标题栏，类似 Element UI image-viewer）
     */
    private void showImagePreview(UploadFile targetFile) {
        ObservableList<Image> images = FXCollections.observableArrayList();
        int targetIndex = 0;

        List<UploadFile> imageFiles = getSkinnable().getFileList().stream()
                .filter(UploadFile::isImage)
                .toList();

        for (int i = 0; i < imageFiles.size(); i++) {
            UploadFile f = imageFiles.get(i);
            if (f.equals(targetFile)) targetIndex = i;

            Image img;
            if (f.getRawFile() != null && f.getRawFile().exists()) {
                img = new Image(f.getRawFile().toURI().toString());
            } else if (f.getUrl() != null && !f.getUrl().isEmpty()) {
                img = new Image(f.getUrl(), true);
            } else if (f.getThumbnail() != null) {
                img = f.getThumbnail();
            } else {
                continue;
            }
            images.add(img);
        }

        if (images.isEmpty()) return;

        // 按目标索引重排列表，使目标图片排在第一位
        if (targetIndex > 0 && targetIndex < images.size()) {
            ObservableList<Image> reordered = FXCollections.observableArrayList();
            for (int i = targetIndex; i < images.size(); i++) {
                reordered.add(images.get(i));
            }
            for (int i = 0; i < targetIndex; i++) {
                reordered.add(images.get(i));
            }
            images = reordered;
        }

        ImageViewer viewer = new ImageViewer(images);

        // === 沉浸式无边框全屏预览 ===
        Stage previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.initStyle(StageStyle.TRANSPARENT); // 移除窗口标题栏和边框

        // 关闭按钮（右上角 X）
        SVGPath closeSvg = new SVGPath();
        closeSvg.setContent(SVG_CLOSE);
        closeSvg.setFill(Color.WHITE);
        double closeFactor = 18.0 / 24.0;
        closeSvg.setScaleX(closeFactor);
        closeSvg.setScaleY(closeFactor);

        StackPane closeBtn = new StackPane(closeSvg);
        closeBtn.setPrefSize(44, 44);
        closeBtn.setMaxSize(44, 44);
        closeBtn.setMinSize(44, 44);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 22;");
        closeBtn.setOnMouseClicked(e -> {
            e.consume();
            previewStage.close();
        });
        // 悬浮高亮效果
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 22;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 22;"));
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(20, 20, 0, 0));

        StackPane root = new StackPane(viewer, closeBtn);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.85);");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // ESC 键关闭
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                previewStage.close();
            }
        });

        previewStage.setScene(scene);
        previewStage.setMaximized(true); // 全屏沉浸式展示
        previewStage.show();

        // 确保获得键盘焦点
        viewer.requestFocus();
    }

    /**
     * 移除文件
     */
    private void onRemoveFile(UploadFile file) {
        getSkinnable().removeFile(file);
    }

    /**
     * 创建文本模式下的状态指示节点
     */
    private Node createStatusNode(UploadFile file) {
        return switch (file.getStatus()) {
            case UPLOADING -> {
                ProgressIndicator indicator = new ProgressIndicator();
                indicator.progressProperty().bind(file.progressProperty());
                indicator.setPrefSize(18, 18);
                indicator.setMaxSize(18, 18);
                yield indicator;
            }
            case SUCCESS -> createIconDisplay(SVG_SUCCESS_CHECK, "#67c23a", 16, 22);
            case FAIL -> {
                Label errorLabel = new Label("!");
                errorLabel.setStyle(
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;"
                                + "-fx-background-color: #f56c6c; -fx-background-radius: 50%;"
                                + "-fx-min-width: 18; -fx-min-height: 18; -fx-max-width: 18; -fx-max-height: 18;"
                                + "-fx-alignment: center;");
                if (file.getErrorMessage() != null) {
                    Tooltip.install(errorLabel, new Tooltip(file.getErrorMessage()));
                }
                yield errorLabel;
            }
            default -> {
                Region spacer = new Region();
                spacer.setPrefSize(18, 18);
                yield spacer;
            }
        };
    }

    /**
     * 应用状态样式
     */
    private void applyStatusStyle(Node node, UploadFile file) {
        switch (file.getStatus()) {
            case SUCCESS -> node.getStyleClass().add("upload-list-item-success");
            case FAIL -> node.getStyleClass().add("upload-list-item-error");
            case UPLOADING -> node.getStyleClass().add("upload-list-item-uploading");
            default -> {}
        }
    }

    /**
     * 添加提示文本
     */
    private void addTipLabel() {
        String tip = getSkinnable().getTip();
        if (tip != null && !tip.isEmpty()) {
            tipLabel = new Label(tip);
            tipLabel.getStyleClass().add("upload-tip");
            tipLabel.setWrapText(true);
            rootContainer.getChildren().add(tipLabel);
        }
    }

    /**
     * 更新提示文本
     */
    private void updateTip() {
        if (tipLabel != null) {
            tipLabel.setText(getSkinnable().getTip());
        } else {
            // tipLabel 不存在时重建
            buildUI();
        }
    }

    /**
     * 是否已达到文件数量限制
     */
    private boolean isLimitReached() {
        int limit = getSkinnable().getLimit();
        return limit > 0 && getSkinnable().getFileList().size() >= limit;
    }

    /**
     * 创建文件图标盒子
     */
    private VBox createFileIconBox(String fileName) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(createIconDisplay(SVG_FILE, "#c0c4cc", 32, 40));

        Label nameLabel = new Label(fileName);
        nameLabel.getStyleClass().add("upload-card-filename");
        nameLabel.setMaxWidth(120);
        nameLabel.setEllipsisString("...");
        box.getChildren().add(nameLabel);
        return box;
    }

    /**
     * 获取当前窗口 Stage
     */
    private Stage getStage() {
        Scene scene = getSkinnable().getScene();
        if (scene != null && scene.getWindow() instanceof Stage s) {
            return s;
        }
        return null;
    }

    @Override
    public void dispose() {
        // 清理所有文件的监听器，防止内存泄漏
        for (UploadFile file : new ArrayList<>(perFileListeners.keySet())) {
            detachFileListeners(file);
        }
        perFileListeners.clear();
        getSkinnable().getFileList().removeListener(fileListListener);
        super.dispose();
    }
}
