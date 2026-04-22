package io.aurora.fx.components.avatar;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

/**
 * 头像组件
 * <p>
 * 支持圆形、方形、六边形等多种形状的头像展示，提供图片裁剪、主题定制、
 * 占位文字、响应式尺寸等功能。基于 JavaFX Control 架构实现 Skin 分离。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 基础用法
 * Avatar avatar = new Avatar(new Image("avatar.png"));
 *
 * // Builder 链式配置
 * Avatar avatar = new Avatar()
 *     .image(new Image("avatar.png"))
 *     .shape(AvatarShape.CIRCLE)
 *     .size(80)
 *     .arcWidth(10)
 *     .arcHeight(10)
 *     .theme(AvatarTheme.BORDERED)
 *     .placeholder("U");
 *
 * // URL 加载
 * Avatar avatar = new Avatar("https://example.com/avatar.png");
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class Avatar extends Control {

    private static final String DEFAULT_STYLE_CLASS = "aurora-avatar";

    // ==================== 属性定义 ====================

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");
    private final ObjectProperty<AvatarShape> avatarShape = new SimpleObjectProperty<>(this, "avatarShape", AvatarShape.CIRCLE);
    private final ObjectProperty<AvatarTheme> theme = new SimpleObjectProperty<>(this, "theme", AvatarTheme.DEFAULT);
    private final DoubleProperty arcWidth = new SimpleDoubleProperty(this, "arcWidth", 0);
    private final DoubleProperty arcHeight = new SimpleDoubleProperty(this, "arcHeight", 0);
    private final StringProperty placeholder = new SimpleStringProperty(this, "placeholder", "");

    // ==================== 构造方法 ====================

    public Avatar() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setPrefSize(100, 100);
    }

    public Avatar(Image image) {
        this();
        setImage(image);
    }

    public Avatar(String imageUrl) {
        this(new Image(imageUrl, true));
    }

    public Avatar(String imageUrl, boolean backgroundLoading) {
        this(new Image(imageUrl, backgroundLoading));
    }

    // ==================== Skin ====================

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AvatarSkin(this);
    }

    // ==================== 链式 API ====================

    public Avatar image(Image image) {
        setImage(image);
        return this;
    }

    public Avatar avatarShape(AvatarShape avatarShape) {
        setAvatarShape(avatarShape);
        return this;
    }

    public Avatar size(double size) {
        setPrefSize(size, size);
        return this;
    }

    public Avatar arcWidth(double arcWidth) {
        setArcWidth(arcWidth);
        return this;
    }

    public Avatar arcHeight(double arcHeight) {
        setArcHeight(arcHeight);
        return this;
    }

    public Avatar theme(AvatarTheme theme) {
        setTheme(theme);
        return this;
    }

    public Avatar placeholder(String text) {
        setPlaceholder(text);
        return this;
    }

    // ==================== Property Accessors ====================

    public final ObjectProperty<Image> imageProperty() { return image; }
    public final Image getImage() { return image.get(); }
    public final void setImage(Image value) { image.set(value); }

    public final ObjectProperty<AvatarShape> avatarShapeProperty() { return avatarShape; }
    public final AvatarShape getAvatarShape() { return avatarShape.get(); }
    public final void setAvatarShape(AvatarShape value) { avatarShape.set(value); }

    public final ObjectProperty<AvatarTheme> themeProperty() { return theme; }
    public final AvatarTheme getTheme() { return theme.get(); }
    public final void setTheme(AvatarTheme value) { theme.set(value); }

    public final DoubleProperty arcWidthProperty() { return arcWidth; }
    public final double getArcWidth() { return arcWidth.get(); }
    public final void setArcWidth(double value) { arcWidth.set(value); }

    public final DoubleProperty arcHeightProperty() { return arcHeight; }
    public final double getArcHeight() { return arcHeight.get(); }
    public final void setArcHeight(double value) { arcHeight.set(value); }

    public final StringProperty placeholderProperty() { return placeholder; }
    public final String getPlaceholder() { return placeholder.get(); }
    public final void setPlaceholder(String value) { placeholder.set(value); }
}
