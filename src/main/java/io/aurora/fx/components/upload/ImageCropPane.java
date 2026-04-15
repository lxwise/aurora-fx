package io.aurora.fx.components.upload;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 图像裁切面板组件，用于头像上传前的图片裁剪。
 * <p>
 * 功能特性：
 * <ul>
 *   <li>支持固定比例裁切（默认 1:1 正方形头像）</li>
 *   <li>支持自由比例裁切</li>
 *   <li>支持缩放控制</li>
 *   <li>实时预览裁切效果</li>
 *   <li>裁切结果输出为 File 文件，可直接用于上传</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * ImageCropPane.showCropDialog(sourceFile, croppedFile -> {
 *     // croppedFile 是裁切后的文件
 *     uploader.handleStart(croppedFile);
 * });
 * }</pre>
 *
 * @author lstar
 * @since 2025
 */
public class ImageCropPane extends BorderPane {

    /** 裁切区域的宽高比（默认 1.0 即正方形，0 表示自由比例） */
    private final DoubleProperty aspectRatio = new SimpleDoubleProperty(1.0);

    /** 裁切后输出的宽度（像素，0 表示使用原始分辨率） */
    private final IntegerProperty outputWidth = new SimpleIntegerProperty(0);

    /** 裁切后输出的高度（像素，0 表示使用原始分辨率） */
    private final IntegerProperty outputHeight = new SimpleIntegerProperty(0);

    /** 输出图片格式 */
    private final StringProperty outputFormat = new SimpleStringProperty("png");

    // 内部组件
    private final ImageView sourceView;
    private final Pane overlayPane;
    private final Rectangle cropRect;
    private final Region dimTop, dimBottom, dimLeft, dimRight;
    private final Line[] gridLines = new Line[4]; // 三分线网格
    private final Rectangle[] cornerHandles = new Rectangle[4]; // 四角拖拽手柄
    private final ImageView previewView;
    private final Slider zoomSlider;
    private HBox toolbar;

    // 源图片信息
    private Image sourceImage;
    private File sourceFile;
    private boolean cropInitialized = false;

    // 裁切结果
    private File croppedResultFile;
    private boolean showingResult = false;

    // 裁切框拖拽状态
    private double dragStartX, dragStartY;
    private double rectStartX, rectStartY;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private static final double RESIZE_HANDLE = 12;

    // 回调
    private Consumer<File> onCropComplete;
    private Runnable onCancel;

    /**
     * 默认构造函数
     */
    public ImageCropPane() {
        this(null);
    }

    /**
     * 使用指定图片文件构造裁切面板
     *
     * @param imageFile 要裁切的图片文件
     */
    public ImageCropPane(File imageFile) {
        getStyleClass().add("image-crop-pane");

        // === 图片显示区 ===
        sourceView = new ImageView();
        sourceView.setPreserveRatio(true);
        sourceView.setSmooth(true);

        // === 裁切框 ===
        cropRect = new Rectangle();
        cropRect.setFill(Color.TRANSPARENT);
        cropRect.setStroke(Color.WHITE);
        cropRect.setStrokeWidth(2);

        // === 裁切区域三分线网格（提升操作手感） ===
        for (int i = 0; i < 4; i++) {
            gridLines[i] = new Line();
            gridLines[i].setStroke(Color.rgb(255, 255, 255, 0.4));
            gridLines[i].setStrokeWidth(0.8);
            gridLines[i].setMouseTransparent(true);
        }

        // === 四角拖拽手柄（视觉指示） ===
        for (int i = 0; i < 4; i++) {
            cornerHandles[i] = new Rectangle(10, 10);
            cornerHandles[i].setFill(Color.WHITE);
            cornerHandles[i].setStroke(Color.web("#409eff"));
            cornerHandles[i].setStrokeWidth(1.5);
            cornerHandles[i].setMouseTransparent(true);
        }

        // === 暗化遮罩（裁切框外的区域） ===
        dimTop = createDimRegion();
        dimBottom = createDimRegion();
        dimLeft = createDimRegion();
        dimRight = createDimRegion();

        overlayPane = new Pane(sourceView, dimTop, dimBottom, dimLeft, dimRight, cropRect,
                gridLines[0], gridLines[1], gridLines[2], gridLines[3],
                cornerHandles[0], cornerHandles[1], cornerHandles[2], cornerHandles[3]);
        overlayPane.setStyle("-fx-background-color: #2c2c2c;");
        overlayPane.setPrefSize(500, 400);

        // 裁切框拖拽与调整大小
        setupCropInteractions();

        // === 预览区 ===
        previewView = new ImageView();
        previewView.setPreserveRatio(true);
        previewView.setSmooth(true);
        previewView.setFitWidth(120);
        previewView.setFitHeight(120);

        StackPane previewBox = new StackPane(previewView);
        previewBox.setStyle("-fx-border-color: #dcdfe6; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-color: white; -fx-background-radius: 4;");
        previewBox.setPrefSize(130, 130);
        previewBox.setMinSize(130, 130);
        previewBox.setMaxSize(130, 130);

        Label previewLabel = new Label("预览");
        previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #909399;");

        VBox previewPanel = new VBox(8, previewLabel, previewBox);
        previewPanel.setAlignment(Pos.TOP_CENTER);
        previewPanel.setPadding(new Insets(10));

        // === 缩放滑块 ===
        zoomSlider = new Slider(0.1, 3.0, 1.0);
        zoomSlider.setPrefWidth(200);
        zoomSlider.valueProperty().addListener((obs, o, n) -> {
            if (sourceImage != null) {
                applyZoom(n.doubleValue());
            }
        });

        Label zoomLabel = new Label("缩放:");
        zoomLabel.setStyle("-fx-text-fill: #606266; -fx-font-size: 13px;");

        // === 底部工具栏 ===
        Button confirmBtn = new Button("确认裁切");
        confirmBtn.getStyleClass().add("button");
        confirmBtn.setOnAction(e -> doCrop());

        Button cancelBtn = new Button("取消");
        cancelBtn.getStyleClass().addAll("button", "cancel-button");
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar = new HBox(10, zoomLabel, zoomSlider, spacer, cancelBtn, confirmBtn);
        toolbar.getStyleClass().add("image-crop-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 16, 10, 16));

        // === 布局 ===
        setCenter(overlayPane);
        setRight(previewPanel);
        setBottom(toolbar);

        // 加载图片
        if (imageFile != null) {
            setImage(imageFile);
        }

        // 监听尺寸变化
        overlayPane.widthProperty().addListener((obs, o, n) -> layoutCrop());
        overlayPane.heightProperty().addListener((obs, o, n) -> layoutCrop());
    }

    // =========================================================================
    //  公共 API
    // =========================================================================

    /**
     * 设置要裁切的图片
     *
     * @param file 图片文件
     * @throws IllegalArgumentException 如果文件为 null 或不存在
     */
    public void setImage(File file) {
        if (file == null || !file.exists()) {
            System.err.println("ImageCropPane.setImage: 文件为 null 或不存在");
            return;
        }
        this.sourceFile = file;
        try {
            this.sourceImage = new Image(file.toURI().toString());
            if (sourceImage.isError()) {
                System.err.println("ImageCropPane.setImage: 图片加载失败 - " + file.getName());
                return;
            }
        } catch (Exception e) {
            System.err.println("ImageCropPane.setImage: 图片加载异常 - " + e.getMessage());
            return;
        }
        sourceView.setImage(sourceImage);
        zoomSlider.setValue(1.0);
        cropInitialized = false;

        // 等待布局完成后初始化裁切框
        Platform.runLater(() -> {
            applyZoom(1.0);
            Platform.runLater(() -> {
                initCropRect();
                cropInitialized = true;
            });
        });
    }

    /**
     * 设置裁切完成回调
     *
     * @param callback 回调函数，参数为裁切后的文件
     */
    public void setOnCropComplete(Consumer<File> callback) {
        this.onCropComplete = callback;
    }

    /**
     * 设置取消回调
     *
     * @param callback 取消回调
     */
    public void setOnCancel(Runnable callback) {
        this.onCancel = callback;
    }

    // =========================================================================
    //  Properties
    // =========================================================================

    public double getAspectRatio() { return aspectRatio.get(); }
    public DoubleProperty aspectRatioProperty() { return aspectRatio; }
    public void setAspectRatio(double ratio) { this.aspectRatio.set(ratio); }

    public int getOutputWidth() { return outputWidth.get(); }
    public IntegerProperty outputWidthProperty() { return outputWidth; }
    public void setOutputWidth(int width) { this.outputWidth.set(width); }

    public int getOutputHeight() { return outputHeight.get(); }
    public IntegerProperty outputHeightProperty() { return outputHeight; }
    public void setOutputHeight(int height) { this.outputHeight.set(height); }

    public String getOutputFormat() { return outputFormat.get(); }
    public StringProperty outputFormatProperty() { return outputFormat; }
    public void setOutputFormat(String format) { this.outputFormat.set(format); }

    // =========================================================================
    //  内部逻辑
    // =========================================================================

    /**
     * 初始化裁切框（居中、占图片区域 70%）
     */
    private void initCropRect() {
        double viewW = sourceView.getBoundsInParent().getWidth();
        double viewH = sourceView.getBoundsInParent().getHeight();
        if (viewW <= 0 || viewH <= 0) return;

        double ratio = getAspectRatio();
        double cropSize;

        if (ratio > 0) {
            // 固定比例
            cropSize = Math.min(viewW, viewH) * 0.7;
            double cropW = cropSize;
            double cropH = cropSize / ratio;
            if (cropH > viewH * 0.7) {
                cropH = viewH * 0.7;
                cropW = cropH * ratio;
            }
            cropRect.setWidth(cropW);
            cropRect.setHeight(cropH);
        } else {
            // 自由比例
            cropRect.setWidth(viewW * 0.7);
            cropRect.setHeight(viewH * 0.7);
        }

        // 居中
        cropRect.setX(sourceView.getLayoutX() + (viewW - cropRect.getWidth()) / 2);
        cropRect.setY(sourceView.getLayoutY() + (viewH - cropRect.getHeight()) / 2);

        updateDimRegions();
        updatePreview();
    }

    /**
     * 应用缩放
     */
    private void applyZoom(double zoom) {
        if (sourceImage == null) return;

        double paneW = overlayPane.getWidth();
        double paneH = overlayPane.getHeight();
        if (paneW <= 0 || paneH <= 0) return;

        double imgW = sourceImage.getWidth() * zoom;
        double imgH = sourceImage.getHeight() * zoom;

        // 限制图片不超过面板
        double fitW = Math.min(imgW, paneW - 20);
        double fitH = Math.min(imgH, paneH - 20);
        double fitRatio = Math.min(fitW / imgW, fitH / imgH);

        sourceView.setFitWidth(imgW * fitRatio);
        sourceView.setFitHeight(imgH * fitRatio);

        layoutCrop();
    }

    /**
     * 重新布局图片和裁切框
     */
    private void layoutCrop() {
        double paneW = overlayPane.getWidth();
        double paneH = overlayPane.getHeight();
        if (paneW <= 0 || paneH <= 0) return;

        double viewW = sourceView.getBoundsInLocal().getWidth();
        double viewH = sourceView.getBoundsInLocal().getHeight();

        // 居中图片
        sourceView.setLayoutX((paneW - viewW) / 2);
        sourceView.setLayoutY((paneH - viewH) / 2);

        // 确保裁切框在图片范围内
        constrainCropRect();
        updateDimRegions();
        updatePreview();
    }

    /**
     * 约束裁切框不超出图片范围
     */
    private void constrainCropRect() {
        double imgX = sourceView.getLayoutX();
        double imgY = sourceView.getLayoutY();
        double imgW = sourceView.getBoundsInLocal().getWidth();
        double imgH = sourceView.getBoundsInLocal().getHeight();

        double rw = Math.min(cropRect.getWidth(), imgW);
        double rh = Math.min(cropRect.getHeight(), imgH);
        cropRect.setWidth(rw);
        cropRect.setHeight(rh);

        double rx = Math.max(imgX, Math.min(cropRect.getX(), imgX + imgW - rw));
        double ry = Math.max(imgY, Math.min(cropRect.getY(), imgY + imgH - rh));
        cropRect.setX(rx);
        cropRect.setY(ry);
    }

    /**
     * 更新暗化遮罩区域
     */
    private void updateDimRegions() {
        double paneW = overlayPane.getWidth();
        double paneH = overlayPane.getHeight();
        double rx = cropRect.getX();
        double ry = cropRect.getY();
        double rw = cropRect.getWidth();
        double rh = cropRect.getHeight();

        // 上方
        dimTop.setLayoutX(0);
        dimTop.setLayoutY(0);
        dimTop.setPrefSize(paneW, ry);

        // 下方
        dimBottom.setLayoutX(0);
        dimBottom.setLayoutY(ry + rh);
        dimBottom.setPrefSize(paneW, paneH - ry - rh);

        // 左方
        dimLeft.setLayoutX(0);
        dimLeft.setLayoutY(ry);
        dimLeft.setPrefSize(rx, rh);

        // 右方
        dimRight.setLayoutX(rx + rw);
        dimRight.setLayoutY(ry);
        dimRight.setPrefSize(paneW - rx - rw, rh);

        // 同步更新网格线和角标
        updateGridAndHandles();
    }

    /**
     * 更新预览图（使用 viewport 直接裁切，避免每次创建快照，大幅提升拖拽流畅度）
     */
    private void updatePreview() {
        if (sourceImage == null) return;

        double imgX = sourceView.getLayoutX();
        double imgY = sourceView.getLayoutY();
        double viewW = sourceView.getBoundsInLocal().getWidth();
        double viewH = sourceView.getBoundsInLocal().getHeight();

        if (viewW <= 0 || viewH <= 0) return;

        // 计算裁切框相对于图片的比例
        double relX = Math.max(0, Math.min((cropRect.getX() - imgX) / viewW, 1));
        double relY = Math.max(0, Math.min((cropRect.getY() - imgY) / viewH, 1));
        double relW = Math.min(cropRect.getWidth() / viewW, 1 - relX);
        double relH = Math.min(cropRect.getHeight() / viewH, 1 - relY);

        // 映射到源图片像素坐标
        double srcX = relX * sourceImage.getWidth();
        double srcY = relY * sourceImage.getHeight();
        double srcW = relW * sourceImage.getWidth();
        double srcH = relH * sourceImage.getHeight();

        if (srcW > 0 && srcH > 0) {
            // 直接设置 viewport，比 snapshot 方法高效得多
            previewView.setImage(sourceImage);
            previewView.setViewport(new Rectangle2D(srcX, srcY, srcW, srcH));
        }
    }

    /**
     * 更新裁切区域三分线网格和四角手柄位置
     */
    private void updateGridAndHandles() {
        double rx = cropRect.getX();
        double ry = cropRect.getY();
        double rw = cropRect.getWidth();
        double rh = cropRect.getHeight();

        // 三分线网格（水平两条 + 垂直两条）
        gridLines[0].setStartX(rx); gridLines[0].setEndX(rx + rw);
        gridLines[0].setStartY(ry + rh / 3); gridLines[0].setEndY(ry + rh / 3);

        gridLines[1].setStartX(rx); gridLines[1].setEndX(rx + rw);
        gridLines[1].setStartY(ry + 2 * rh / 3); gridLines[1].setEndY(ry + 2 * rh / 3);

        gridLines[2].setStartX(rx + rw / 3); gridLines[2].setEndX(rx + rw / 3);
        gridLines[2].setStartY(ry); gridLines[2].setEndY(ry + rh);

        gridLines[3].setStartX(rx + 2 * rw / 3); gridLines[3].setEndX(rx + 2 * rw / 3);
        gridLines[3].setStartY(ry); gridLines[3].setEndY(ry + rh);

        // 四角拖拽手柄
        double hs = 5; // 半尺寸
        cornerHandles[0].setX(rx - hs); cornerHandles[0].setY(ry - hs);
        cornerHandles[1].setX(rx + rw - hs); cornerHandles[1].setY(ry - hs);
        cornerHandles[2].setX(rx - hs); cornerHandles[2].setY(ry + rh - hs);
        cornerHandles[3].setX(rx + rw - hs); cornerHandles[3].setY(ry + rh - hs);
    }

    /**
     * 设置裁切框的拖拽和调整大小交互
     */
    private void setupCropInteractions() {
        overlayPane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            double mx = e.getX();
            double my = e.getY();

            double rx = cropRect.getX();
            double ry = cropRect.getY();
            double rw = cropRect.getWidth();
            double rh = cropRect.getHeight();

            // 检测是否在右下角调整手柄区域
            if (mx >= rx + rw - RESIZE_HANDLE && mx <= rx + rw + RESIZE_HANDLE
                    && my >= ry + rh - RESIZE_HANDLE && my <= ry + rh + RESIZE_HANDLE) {
                isResizing = true;
                isDragging = false;
            }
            // 检测是否在裁切框内部（拖拽移动）
            else if (mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh) {
                isDragging = true;
                isResizing = false;
            } else {
                isDragging = false;
                isResizing = false;
            }

            dragStartX = mx;
            dragStartY = my;
            rectStartX = rx;
            rectStartY = ry;
            e.consume();
        });

        overlayPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            double mx = e.getX();
            double my = e.getY();

            if (isDragging) {
                double dx = mx - dragStartX;
                double dy = my - dragStartY;
                cropRect.setX(rectStartX + dx);
                cropRect.setY(rectStartY + dy);
                constrainCropRect();
                updateDimRegions();
                updatePreview();
            } else if (isResizing) {
                double newW = Math.max(40, mx - cropRect.getX());
                double newH;
                double ratio = getAspectRatio();
                if (ratio > 0) {
                    newH = newW / ratio;
                } else {
                    newH = Math.max(40, my - cropRect.getY());
                }
                cropRect.setWidth(newW);
                cropRect.setHeight(newH);
                constrainCropRect();
                updateDimRegions();
                updatePreview();
            }
            e.consume();
        });

        overlayPane.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            isDragging = false;
            isResizing = false;
            e.consume();
        });

        // 鼠标样式
        overlayPane.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            double mx = e.getX();
            double my = e.getY();
            double rx = cropRect.getX();
            double ry = cropRect.getY();
            double rw = cropRect.getWidth();
            double rh = cropRect.getHeight();

            if (mx >= rx + rw - RESIZE_HANDLE && mx <= rx + rw + RESIZE_HANDLE
                    && my >= ry + rh - RESIZE_HANDLE && my <= ry + rh + RESIZE_HANDLE) {
                overlayPane.setCursor(Cursor.SE_RESIZE);
            } else if (mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh) {
                overlayPane.setCursor(Cursor.MOVE);
            } else {
                overlayPane.setCursor(Cursor.DEFAULT);
            }
        });
    }

    /**
     * 执行裁切操作
     */
    private void doCrop() {
        if (sourceImage == null || sourceFile == null) {
            System.err.println("ImageCropPane.doCrop: 源图片未设置");
            return;
        }

        double imgX = sourceView.getLayoutX();
        double imgY = sourceView.getLayoutY();
        double viewW = sourceView.getBoundsInLocal().getWidth();
        double viewH = sourceView.getBoundsInLocal().getHeight();

        if (viewW <= 0 || viewH <= 0) return;

        // 计算源图片上的裁切区域
        double relX = Math.max(0, (cropRect.getX() - imgX) / viewW);
        double relY = Math.max(0, (cropRect.getY() - imgY) / viewH);
        double relW = Math.min(cropRect.getWidth() / viewW, 1 - relX);
        double relH = Math.min(cropRect.getHeight() / viewH, 1 - relY);

        int srcX = (int) (relX * sourceImage.getWidth());
        int srcY = (int) (relY * sourceImage.getHeight());
        int srcW = (int) (relW * sourceImage.getWidth());
        int srcH = (int) (relH * sourceImage.getHeight());

        srcW = Math.max(1, Math.min(srcW, (int) sourceImage.getWidth() - srcX));
        srcH = Math.max(1, Math.min(srcH, (int) sourceImage.getHeight() - srcY));

        try {
            // 从源图片中裁切
            BufferedImage buffered = SwingFXUtils.fromFXImage(sourceImage, null);
            BufferedImage cropped = buffered.getSubimage(srcX, srcY, srcW, srcH);

            // 如果指定了输出尺寸，进行缩放
            int outW = getOutputWidth() > 0 ? getOutputWidth() : srcW;
            int outH = getOutputHeight() > 0 ? getOutputHeight() : srcH;

            BufferedImage output;
            if (outW != srcW || outH != srcH) {
                output = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = output.createGraphics();
                g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                        java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(cropped, 0, 0, outW, outH, null);
                g.dispose();
            } else {
                output = cropped;
            }

            // 写入临时文件
            String format = getOutputFormat();
            String suffix = "." + format;
            File tempFile = File.createTempFile("aurora_crop_", suffix);
            tempFile.deleteOnExit();
            ImageIO.write(output, format, tempFile);

            // 显示裁切结果预览，而不是直接完成
            showCropResult(tempFile);

        } catch (IOException e) {
            System.err.println("裁切图片失败: " + e.getMessage());
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("裁切失败");
                alert.setHeaderText(null);
                alert.setContentText("图片裁切处理失败，请重试。\n错误信息: " + e.getMessage());
                alert.showAndWait();
            });
        } catch (Exception e) {
            System.err.println("裁切图片意外异常: " + e.getMessage());
        }
    }

    /**
     * 显示裁切结果预览，用户可确认使用或重新裁切
     */
    private void showCropResult(File resultFile) {
        this.croppedResultFile = resultFile;
        this.showingResult = true;

        Image resultImage = new Image(resultFile.toURI().toString());

        // 更新中心区域为结果预览
        ImageView resultView = new ImageView(resultImage);
        resultView.setPreserveRatio(true);
        resultView.setFitWidth(Math.min(overlayPane.getWidth() - 40, 380));
        resultView.setFitHeight(Math.min(overlayPane.getHeight() - 40, 380));
        resultView.setSmooth(true);

        // 裁切结果边框
        StackPane imageFrame = new StackPane(resultView);
        imageFrame.setStyle("-fx-border-color: white; -fx-border-width: 3; -fx-border-radius: 4; -fx-background-color: white; -fx-background-radius: 4; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        imageFrame.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Label resultLabel = new Label("✅ 裁切完成");
        resultLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label sizeLabel = new Label(String.format("%d × %d px", (int) resultImage.getWidth(), (int) resultImage.getHeight()));
        sizeLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 13px;");

        VBox resultBox = new VBox(12, resultLabel, imageFrame, sizeLabel);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setStyle("-fx-background-color: #2c2c2c;");
        resultBox.setPadding(new Insets(20));

        setCenter(resultBox);

        // 更新预览区
        previewView.setImage(resultImage);
        previewView.setViewport(null);

        // 更新工具栏为结果操作按钮
        Button useBtn = new Button("使用此图片");
        useBtn.getStyleClass().add("button");
        useBtn.setStyle("-fx-background-color: #67c23a; -fx-text-fill: white; -fx-padding: 8 24; -fx-background-radius: 4; -fx-font-size: 14px;");
        useBtn.setOnAction(e -> {
            if (onCropComplete != null) {
                onCropComplete.accept(croppedResultFile);
            }
        });

        Button recropBtn = new Button("重新裁切");
        recropBtn.getStyleClass().addAll("button", "cancel-button");
        recropBtn.setOnAction(e -> restoreCropMode());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().clear();
        toolbar.getChildren().addAll(spacer, recropBtn, useBtn);
    }

    /**
     * 恢复到裁切模式
     */
    private void restoreCropMode() {
        this.showingResult = false;
        this.croppedResultFile = null;

        // 恢复中心区域
        setCenter(overlayPane);

        // 恢复工具栏
        Label zoomLabel = new Label("缩放:");
        zoomLabel.setStyle("-fx-text-fill: #606266; -fx-font-size: 13px;");

        Button confirmBtn = new Button("确认裁切");
        confirmBtn.getStyleClass().add("button");
        confirmBtn.setOnAction(e -> doCrop());

        Button cancelBtn = new Button("取消");
        cancelBtn.getStyleClass().addAll("button", "cancel-button");
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().clear();
        toolbar.getChildren().addAll(zoomLabel, zoomSlider, spacer, cancelBtn, confirmBtn);

        // 重新初始化预览
        Platform.runLater(() -> {
            layoutCrop();
            updatePreview();
        });
    }

    /**
     * 创建暗化遮罩区域
     */
    private Region createDimRegion() {
        Region region = new Region();
        region.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        region.setMouseTransparent(true);
        return region;
    }

    // =========================================================================
    //  静态便捷方法
    // =========================================================================

    /**
     * 显示图片裁切对话框（便捷方法）
     * <p>
     * 弹出模态对话框，用户完成裁切后通过回调返回裁切后的文件。
     *
     * @param imageFile   要裁切的原始图片文件
     * @param onComplete  裁切完成回调，参数为裁切后的临时文件
     */
    public static void showCropDialog(File imageFile, Consumer<File> onComplete) {
        showCropDialog(imageFile, 1.0, 0, 0, onComplete);
    }

    /**
     * 显示图片裁切对话框（完整参数版本）
     *
     * @param imageFile    要裁切的原始图片文件
     * @param aspectRatio  宽高比（1.0 为正方形，0 为自由比例）
     * @param outputWidth  输出宽度（0 为不缩放）
     * @param outputHeight 输出高度（0 为不缩放）
     * @param onComplete   裁切完成回调
     */
    public static void showCropDialog(File imageFile, double aspectRatio,
                                       int outputWidth, int outputHeight,
                                       Consumer<File> onComplete) {
        if (imageFile == null || !imageFile.exists()) {
            System.err.println("ImageCropPane.showCropDialog: 文件为 null 或不存在");
            return;
        }

        ImageCropPane cropPane = new ImageCropPane(imageFile);
        cropPane.setAspectRatio(aspectRatio);
        cropPane.setOutputWidth(outputWidth);
        cropPane.setOutputHeight(outputHeight);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("裁切图片");
        dialog.setResizable(true);

        cropPane.setOnCropComplete(file -> {
            // 关键：必须在 dialog.close() 之前同步调用回调！
            // 因为 AvatarUploadNode.beforeUpload 依赖 showAndWait() 阻塞后
            // 同步读取回调设置的裁切结果。如果使用 Platform.runLater，
            // 回调会在 showAndWait() 返回之后才执行，导致裁切结果丢失。
            if (onComplete != null) {
                onComplete.accept(file);
            }
            dialog.close();
        });

        cropPane.setOnCancel(dialog::close);

        Scene scene = new Scene(cropPane, 750, 520);

        // 尝试加载组件样式表
        try {
            String css = ImageCropPane.class.getResource("/io/aurora/fx/components/upload/file-uploader.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {
        }

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
