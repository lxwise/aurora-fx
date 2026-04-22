package io.aurora.fx.components.avatar;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 头像组件皮肤
 * <p>
 * 负责头像组件的视觉渲染，包括图片裁剪、形状绘制、占位符显示、
 * 边框/阴影效果等。支持图片异步加载与进度监听。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class AvatarSkin extends SkinBase<Avatar> {

    private final Avatar control;
    private final Group rootPane;
    private final ImageView imageView;
    private final Label placeholderLabel;
    private Shape borderShape;

    // ==================== 监听器 ====================

    private final InvalidationListener updateListener = obs -> clipImageView();
    private final ChangeListener<Number> progressListener = (obs, ov, nv) -> {
        if (nv.doubleValue() >= 1.0) {
            clipImageView();
        }
    };
    private final ChangeListener<Image> imageChangeListener = (obs, ov, nv) -> {
        if (ov != null) {
            ov.progressProperty().removeListener(progressListener);
        }
        if (nv != null) {
            nv.progressProperty().addListener(progressListener);
            if (nv.getProgress() >= 1.0) {
                clipImageView();
            }
        }
        updatePlaceholderVisibility();
    };
    private final InvalidationListener themeListener = obs -> applyTheme();

    // ==================== 构造方法 ====================

    public AvatarSkin(Avatar control) {
        super(control);
        this.control = control;

        rootPane = new Group();
        rootPane.getStyleClass().add("avatar-group");

        imageView = new ImageView();
        imageView.getStyleClass().add("avatar-image");
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);
        imageView.imageProperty().bind(control.imageProperty());

        placeholderLabel = new Label();
        placeholderLabel.getStyleClass().add("avatar-placeholder");
        placeholderLabel.setAlignment(Pos.CENTER);
        placeholderLabel.textProperty().bind(control.placeholderProperty());

        rootPane.getChildren().addAll(placeholderLabel, imageView);
        getChildren().setAll(rootPane);

        // 注册监听
        control.imageProperty().addListener(imageChangeListener);
        control.avatarShapeProperty().addListener(updateListener);
        control.prefWidthProperty().addListener(updateListener);
        control.prefHeightProperty().addListener(updateListener);
        control.arcWidthProperty().addListener(updateListener);
        control.arcHeightProperty().addListener(updateListener);
        control.themeProperty().addListener(themeListener);

        if (control.getImage() != null) {
            control.getImage().progressProperty().addListener(progressListener);
        }

        applyTheme();
        clipImageView();
        updatePlaceholderVisibility();
    }

    // ==================== 核心渲染逻辑 ====================

    /**
     * 根据形状裁剪图片并更新显示
     */
    private void clipImageView() {
        Image img = control.getImage();
        if (img == null || img.getProgress() < 1.0) {
            updatePlaceholderShape();
            return;
        }

        // 重置 fitWidth/fitHeight
        imageView.setFitHeight(0);
        imageView.setFitWidth(0);

        double imgW = img.getWidth();
        double imgH = img.getHeight();
        double conW = control.getPrefWidth();
        double conH = control.getPrefHeight();

        AvatarShape shapeType = control.getAvatarShape();
        Shape clipShape;

        switch (shapeType) {
            case SQUARE:
                clipShape = buildSquareClip(imgW, imgH, conW, conH);
                break;
            case HEXAGON_H:
                clipShape = buildHexagonHClip(imgW, imgH, conW, conH);
                break;
            case HEXAGON_V:
                clipShape = buildHexagonVClip(imgW, imgH, conW, conH);
                break;
            case DIAMOND:
                clipShape = buildDiamondClip(imgW, imgH, conW, conH);
                break;
            case PENTAGON:
                clipShape = buildPentagonClip(imgW, imgH, conW, conH);
                break;
            case STAR:
                clipShape = buildStarClip(imgW, imgH, conW, conH);
                break;
            case ROUNDED_SQUARE:
                clipShape = buildRoundedSquareClip(imgW, imgH, conW, conH);
                break;
            case CIRCLE:
            default:
                clipShape = buildCircleClip(imgW, imgH, conW, conH);
                break;
        }

        imageView.setClip(clipShape);
        updateBorderShape();
        updatePlaceholderVisibility();
    }

    private Shape buildCircleClip(double imgW, double imgH, double conW, double conH) {
        double r = Math.min(conW, conH) / 2;
        fitImageToRadius(imgW, imgH, r);
        double cx = computeCenterX(imgW, imgH, r);
        double cy = computeCenterY(imgW, imgH, r);
        return new Circle(cx, cy, r);
    }

    private Shape buildSquareClip(double imgW, double imgH, double conW, double conH) {
        double r = Math.min(conW, conH) / 2;
        fitImageToRadius(imgW, imgH, r);
        double cx = computeCenterX(imgW, imgH, r);
        double cy = computeCenterY(imgW, imgH, r);
        Rectangle rect = new Rectangle(cx - r, cy - r, r * 2, r * 2);
        double aw = control.getArcWidth() > 0 ? control.getArcWidth() : control.getTheme().getArcSize();
        double ah = control.getArcHeight() > 0 ? control.getArcHeight() : control.getTheme().getArcSize();
        rect.setArcWidth(aw);
        rect.setArcHeight(ah);
        return rect;
    }

    private Shape buildHexagonHClip(double imgW, double imgH, double conW, double conH) {
        double rate = 1.16;
        double rw, rh;
        boolean isWide = imgW / imgH > rate;

        if (conW / conH > rate) {
            rh = conH / 2;
            rw = rh * rate;
            if (isWide) imageView.setFitHeight(conH);
            else imageView.setFitWidth(conH * rate);
        } else {
            rw = conW / 2;
            rh = rw / rate;
            if (isWide) imageView.setFitHeight(conW * rate);
            else imageView.setFitWidth(conW);
        }

        double cx = imageView.prefWidth(-1) / 2;
        double cy = imageView.prefHeight(-1) / 2;
        return new Polygon(
                cx - rw / 2, cy - rh, cx + rw / 2, cy - rh,
                cx + rw, cy,
                cx + rw / 2, cy + rh, cx - rw / 2, cy + rh,
                cx - rw, cy);
    }

    private Shape buildHexagonVClip(double imgW, double imgH, double conW, double conH) {
        double rate = 0.871;
        double rw, rh;
        boolean isWide = imgW / imgH > rate;

        if (conW / conH > rate) {
            rh = conH / 2;
            rw = rh * rate;
            if (isWide) imageView.setFitHeight(rh * 2);
            else imageView.setFitWidth(rw * 2);
        } else {
            rw = conW / 2;
            rh = rw / rate;
            if (isWide) imageView.setFitHeight(rh * 2);
            else imageView.setFitWidth(rw * 2);
        }

        double cx = imageView.prefWidth(-1) / 2;
        double cy = imageView.prefHeight(-1) / 2;
        return new Polygon(
                cx, cy - rh, cx + rw, cy - rh / 2,
                cx + rw, cy + rh / 2,
                cx, cy + rh, cx - rw, cy + rh / 2,
                cx - rw, cy - rh / 2);
    }

    private Shape buildDiamondClip(double imgW, double imgH, double conW, double conH) {
        double r = Math.min(conW, conH) / 2;
        fitImageToRadius(imgW, imgH, r);
        double cx = computeCenterX(imgW, imgH, r);
        double cy = computeCenterY(imgW, imgH, r);
        return new Polygon(
                cx, cy - r,
                cx + r, cy,
                cx, cy + r,
                cx - r, cy);
    }

    private Shape buildPentagonClip(double imgW, double imgH, double conW, double conH) {
        double r = Math.min(conW, conH) / 2;
        fitImageToRadius(imgW, imgH, r);
        double cx = computeCenterX(imgW, imgH, r);
        double cy = computeCenterY(imgW, imgH, r);

        // 五边形：顶点从正上方开始，顺时针排列
        double[] points = new double[10];
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(-90 + i * 72);
            points[i * 2] = cx + r * Math.cos(angle);
            points[i * 2 + 1] = cy + r * Math.sin(angle);
        }
        return new Polygon(points);
    }

    private Shape buildStarClip(double imgW, double imgH, double conW, double conH) {
        double r = Math.min(conW, conH) / 2;
        fitImageToRadius(imgW, imgH, r);
        double cx = computeCenterX(imgW, imgH, r);
        double cy = computeCenterY(imgW, imgH, r);

        // 五角星：外半径 r，内半径 r * 0.382（黄金比例）
        double innerR = r * 0.382;
        double[] points = new double[20];
        for (int i = 0; i < 5; i++) {
            double outerAngle = Math.toRadians(-90 + i * 72);
            double innerAngle = Math.toRadians(-90 + i * 72 + 36);
            points[i * 4] = cx + r * Math.cos(outerAngle);
            points[i * 4 + 1] = cy + r * Math.sin(outerAngle);
            points[i * 4 + 2] = cx + innerR * Math.cos(innerAngle);
            points[i * 4 + 3] = cy + innerR * Math.sin(innerAngle);
        }
        return new Polygon(points);
    }

    private Shape buildRoundedSquareClip(double imgW, double imgH, double conW, double conH) {
        double r = Math.min(conW, conH) / 2;
        fitImageToRadius(imgW, imgH, r);
        double cx = computeCenterX(imgW, imgH, r);
        double cy = computeCenterY(imgW, imgH, r);
        Rectangle rect = new Rectangle(cx - r, cy - r, r * 2, r * 2);
        // 圆角为尺寸的 1/4，形成明显的圆角效果
        double arcSize = r * 0.5;
        rect.setArcWidth(arcSize);
        rect.setArcHeight(arcSize);
        return rect;
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断形状是否为圆形或类圆形（占位符需要圆形背景）
     */
    private boolean isRoundedShape(AvatarShape shape) {
        return shape == AvatarShape.CIRCLE || shape == AvatarShape.DIAMOND
                || shape == AvatarShape.PENTAGON || shape == AvatarShape.STAR;
    }

    private void fitImageToRadius(double imgW, double imgH, double r) {
        if (imgW > imgH) {
            double scale = (r * 2) / imgH;
            imageView.setFitHeight(r * 2);
            imageView.setFitWidth(imgW * scale);
        } else {
            double scale = (r * 2) / imgW;
            imageView.setFitWidth(r * 2);
            imageView.setFitHeight(imgH * scale);
        }
    }

    private double computeCenterX(double imgW, double imgH, double r) {
        if (imgW > imgH) {
            double scale = (r * 2) / imgH;
            return imgW * scale / 2 + imageView.getLayoutX();
        }
        return imageView.getFitWidth() / 2 + imageView.getLayoutX();
    }

    private double computeCenterY(double imgW, double imgH, double r) {
        if (imgW > imgH) {
            return imageView.getFitHeight() / 2 + imageView.getLayoutY();
        }
        double scale = (r * 2) / imgW;
        return imgH * scale / 2 + imageView.getLayoutY();
    }

    private void updatePlaceholderVisibility() {
        boolean showPlaceholder = control.getImage() == null
                || control.getImage().getProgress() < 1.0;
        placeholderLabel.setVisible(showPlaceholder);
        imageView.setVisible(!showPlaceholder);
    }

    private void updatePlaceholderShape() {
        double conW = control.getPrefWidth();
        double conH = control.getPrefHeight();
        double size = Math.min(conW, conH);
        placeholderLabel.setPrefSize(size, size);
        placeholderLabel.setFont(Font.font("System", FontWeight.BOLD, size * 0.4));

        AvatarTheme theme = control.getTheme();
        placeholderLabel.setTextFill(theme.getPlaceholderColor());
        placeholderLabel.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: %s;",
                AvatarTheme.toCssColor(theme.getBackgroundColor()),
                isRoundedShape(control.getAvatarShape()) ? "50%" : "0"));
    }

    private void updateBorderShape() {
        AvatarTheme theme = control.getTheme();
        if (theme.getBorderWidth() <= 0) {
            if (borderShape != null) {
                rootPane.getChildren().remove(borderShape);
                borderShape = null;
            }
            return;
        }

        double conW = control.getPrefWidth();
        double conH = control.getPrefHeight();
        double r = Math.min(conW, conH) / 2;
        double cx = r, cy = r;

        Shape newBorder = buildBorderShape(control.getAvatarShape(), cx, cy, r, theme);

        newBorder.setFill(Color.TRANSPARENT);
        newBorder.setStroke(theme.getBorderColor());
        newBorder.setStrokeWidth(theme.getBorderWidth());
        newBorder.setStrokeType(StrokeType.OUTSIDE);

        if (borderShape != null) {
            rootPane.getChildren().remove(borderShape);
        }
        borderShape = newBorder;
        rootPane.getChildren().add(borderShape);
    }

    /**
     * 根据形状类型构建边框形状
     */
    private Shape buildBorderShape(AvatarShape shapeType, double cx, double cy, double r, AvatarTheme theme) {
        switch (shapeType) {
            case CIRCLE:
                return new Circle(cx, cy, r);
            case DIAMOND:
                return new Polygon(cx, cy - r, cx + r, cy, cx, cy + r, cx - r, cy);
            case PENTAGON: {
                double[] points = new double[10];
                for (int i = 0; i < 5; i++) {
                    double angle = Math.toRadians(-90 + i * 72);
                    points[i * 2] = cx + r * Math.cos(angle);
                    points[i * 2 + 1] = cy + r * Math.sin(angle);
                }
                return new Polygon(points);
            }
            case STAR: {
                double innerR = r * 0.382;
                double[] points = new double[20];
                for (int i = 0; i < 5; i++) {
                    double outerAngle = Math.toRadians(-90 + i * 72);
                    double innerAngle = Math.toRadians(-90 + i * 72 + 36);
                    points[i * 4] = cx + r * Math.cos(outerAngle);
                    points[i * 4 + 1] = cy + r * Math.sin(outerAngle);
                    points[i * 4 + 2] = cx + innerR * Math.cos(innerAngle);
                    points[i * 4 + 3] = cy + innerR * Math.sin(innerAngle);
                }
                return new Polygon(points);
            }
            case ROUNDED_SQUARE: {
                Rectangle rect = new Rectangle(0, 0, r * 2, r * 2);
                double arcSize = r * 0.5;
                rect.setArcWidth(arcSize);
                rect.setArcHeight(arcSize);
                return rect;
            }
            case SQUARE:
            case HEXAGON_H:
            case HEXAGON_V:
            default: {
                Rectangle rect = new Rectangle(0, 0, r * 2, r * 2);
                double aw = control.getArcWidth() > 0 ? control.getArcWidth() : theme.getArcSize();
                double ah = control.getArcHeight() > 0 ? control.getArcHeight() : theme.getArcSize();
                rect.setArcWidth(aw);
                rect.setArcHeight(ah);
                return rect;
            }
        }
    }

    private void applyTheme() {
        AvatarTheme theme = control.getTheme();
        if (theme == null) return;

        // 阴影
        if (theme.getShadowRadius() > 0) {
            DropShadow shadow = new DropShadow();
            shadow.setRadius(theme.getShadowRadius());
            shadow.setColor(Color.color(0, 0, 0, theme.getShadowOpacity()));
            shadow.setOffsetY(2);
            rootPane.setEffect(shadow);
        } else {
            rootPane.setEffect(null);
        }

        updatePlaceholderShape();
        updateBorderShape();
    }

    // ==================== 布局 ====================

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        layoutInArea(rootPane, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    // ==================== 资源释放 ====================

    @Override
    public void dispose() {
        Image img = control.getImage();
        if (img != null) {
            img.progressProperty().removeListener(progressListener);
        }
        imageView.imageProperty().unbind();
        placeholderLabel.textProperty().unbind();

        control.imageProperty().removeListener(imageChangeListener);
        control.avatarShapeProperty().removeListener(updateListener);
        control.prefWidthProperty().removeListener(updateListener);
        control.prefHeightProperty().removeListener(updateListener);
        control.arcWidthProperty().removeListener(updateListener);
        control.arcHeightProperty().removeListener(updateListener);
        control.themeProperty().removeListener(themeListener);

        getChildren().clear();
        super.dispose();
    }
}
