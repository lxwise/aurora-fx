package io.aurora.fx.components.lineButton;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;
import javafx.scene.shape.Line;
import javafx.util.Duration;

/**
 * 线条按钮组件
 * <p>
 * 鼠标悬停时在文字下方显示线条动画的按钮。支持两种动画类型：
 * EXTEND（从中心向两侧延伸）和 RISE（从下方上升并渐显）。
 * 支持主题定制、间距配置和事件回调。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 基础用法
 * LineButton btn = new LineButton("Home");
 *
 * // 链式配置
 * LineButton btn = new LineButton("Settings")
 *     .lineType(LineAnimationType.RISE)
 *     .spacing(4)
 *     .animationTime(Duration.millis(200))
 *     .theme(LineButtonTheme.PRIMARY)
 *     .onAction(e -> System.out.println("Clicked!"));
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class LineButton extends Labeled {

    private static final String DEFAULT_STYLE_CLASS = "aurora-line-button";
    private LineButtonSkin skin;

    // ==================== 属性定义 ====================

    private final ObjectProperty<LineAnimationType> lineType =
            new SimpleObjectProperty<>(this, "lineType", LineAnimationType.EXTEND);

    private final ObjectProperty<Duration> animationTime =
            new SimpleObjectProperty<>(this, "animationTime", Duration.millis(130));

    private final ObjectProperty<LineButtonTheme> theme =
            new SimpleObjectProperty<>(this, "theme", LineButtonTheme.DEFAULT);

    private final DoubleProperty spacing = new SimpleDoubleProperty(this, "spacing", 0);
    private final DoubleProperty offsetY = new SimpleDoubleProperty(this, "offsetY", 15);

    private ObjectProperty<EventHandler<ActionEvent>> onAction;

    // ==================== 构造方法 ====================

    public LineButton() {
        this("LineButton");
    }

    public LineButton(String text) {
        super(text);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setPrefSize(150, 60);
        setAlignment(Pos.CENTER);
    }

    public LineButton(String text, Node graphic) {
        super(text, graphic);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setPrefSize(150, 60);
        setAlignment(Pos.CENTER);
    }

    // ==================== Skin ====================

    @Override
    protected Skin<?> createDefaultSkin() {
        skin = new LineButtonSkin(this);
        return skin;
    }

    // ==================== 公开 API ====================

    /** 获取线条节点（可直接设置样式） */
    public Line getLine() {
        ensureSkin();
        return skin.getLine();
    }

    /** 获取内部 Label 节点 */
    public javafx.scene.control.Label getLabel() {
        ensureSkin();
        return skin.getLabel();
    }

    /** 确保 Skin 已创建 */
    private void ensureSkin() {
        if (skin == null) {
            Skin<?> s = createDefaultSkin();
            if (s != null) {
                setSkin(s);
            }
        }
    }

    // ==================== 链式 API ====================

    public LineButton lineType(LineAnimationType type) {
        setLineType(type);
        return this;
    }

    public LineButton animationTime(Duration duration) {
        setAnimationTime(duration);
        return this;
    }

    public LineButton theme(LineButtonTheme theme) {
        setTheme(theme);
        return this;
    }

    public LineButton spacing(double spacing) {
        setSpacing(spacing);
        return this;
    }

    public LineButton offsetY(double offsetY) {
        setOffsetY(offsetY);
        return this;
    }

    public LineButton onAction(EventHandler<ActionEvent> handler) {
        setOnAction(handler);
        return this;
    }

    // ==================== Property Accessors ====================

    public final ObjectProperty<LineAnimationType> lineTypeProperty() { return lineType; }
    public final LineAnimationType getLineType() { return lineType.get(); }
    public final void setLineType(LineAnimationType value) { lineType.set(value); }

    public final ObjectProperty<Duration> animationTimeProperty() { return animationTime; }
    public final Duration getAnimationTime() { return animationTime.get(); }
    public final void setAnimationTime(Duration value) { animationTime.set(value); }

    public final ObjectProperty<LineButtonTheme> themeProperty() { return theme; }
    public final LineButtonTheme getTheme() { return theme.get(); }
    public final void setTheme(LineButtonTheme value) { theme.set(value); }

    public final DoubleProperty spacingProperty() { return spacing; }
    public final double getSpacing() { return spacing.get(); }
    public final void setSpacing(double value) { spacing.set(value); }

    public final DoubleProperty offsetYProperty() { return offsetY; }
    public final double getOffsetY() { return offsetY.get(); }
    public final void setOffsetY(double value) { offsetY.set(value); }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        if (onAction == null) {
            onAction = new ObjectPropertyBase<>() {
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
                @Override
                public Object getBean() { return LineButton.this; }
                @Override
                public String getName() { return "onAction"; }
            };
        }
        return onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onAction == null ? null : onAction.get();
    }

    public final void setOnAction(EventHandler<ActionEvent> handler) {
        onActionProperty().set(handler);
    }
}
