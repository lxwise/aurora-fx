package io.aurora.fx.components.translationButton;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;
import javafx.util.Duration;

/**
 * 平移按钮组件
 * <p>
 * 具有两层 Label 的按钮组件，鼠标悬停时通过平移动画显示 hoverLabel，
 * 移出时还原显示 nonHoverLabel。支持多种平移方向、主题定制和事件回调。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 基础用法
 * TranslationButton btn = new TranslationButton("Click Me");
 *
 * // 链式配置
 * TranslationButton btn = new TranslationButton("Click Me")
 *     .direction(TranslationDirection.LEFT_TO_RIGHT)
 *     .animationTime(Duration.millis(200))
 *     .theme(TranslationButtonTheme.PRIMARY)
 *     .onAction(e -> System.out.println("Clicked!"));
 *
 * // 悬停显示图标
 * btn.getHoverLabel().setGraphic(new FontIcon("fas-arrow-right"));
 * btn.getHoverLabel().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class TranslationButton extends Labeled {

    private static final String DEFAULT_STYLE_CLASS = "aurora-translation-button";
    private TranslationButtonSkin skin;

    // ==================== 属性定义 ====================

    private final ObjectProperty<TranslationDirection> direction =
            new SimpleObjectProperty<>(this, "direction", TranslationDirection.BOTTOM_TO_TOP);

    private final ObjectProperty<Duration> animationTime =
            new SimpleObjectProperty<>(this, "animationTime", Duration.millis(130));

    private final ObjectProperty<TranslationButtonTheme> theme =
            new SimpleObjectProperty<>(this, "theme", TranslationButtonTheme.DEFAULT);

    private ObjectProperty<EventHandler<ActionEvent>> onAction;

    // ==================== 构造方法 ====================

    public TranslationButton() {
        this("Translation");
    }

    public TranslationButton(String text) {
        super(text);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setPrefSize(160, 60);
    }

    public TranslationButton(String text, Node graphic) {
        super(text, graphic);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setPrefSize(160, 60);
    }

    // ==================== Skin ====================

    @Override
    protected Skin<?> createDefaultSkin() {
        skin = new TranslationButtonSkin(this);
        return skin;
    }

    // ==================== 公开 API ====================

    /** 获取悬停层 Label（可自定义图标和文字） */
    public Label getHoverLabel() {
        ensureSkin();
        return skin.getHoverLabel();
    }

    /** 获取非悬停层 Label */
    public Label getNonHoverLabel() {
        ensureSkin();
        return skin.getNonHoverLabel();
    }

    /** 确保 Skin 已创建 */
    private void ensureSkin() {
        if (skin == null) {
            // getSkin() 不会自动创建，需通过 setSkin 触发 createDefaultSkin
            Skin<?> s = createDefaultSkin();
            if (s != null) {
                setSkin(s);
            }
        }
    }

    // ==================== 链式 API ====================

    public TranslationButton direction(TranslationDirection dir) {
        setDirection(dir);
        return this;
    }

    public TranslationButton animationTime(Duration duration) {
        setAnimationTime(duration);
        return this;
    }

    public TranslationButton theme(TranslationButtonTheme theme) {
        setTheme(theme);
        return this;
    }

    public TranslationButton onAction(EventHandler<ActionEvent> handler) {
        setOnAction(handler);
        return this;
    }

    // ==================== Property Accessors ====================

    public final ObjectProperty<TranslationDirection> directionProperty() { return direction; }
    public final TranslationDirection getDirection() { return direction.get(); }
    public final void setDirection(TranslationDirection value) { direction.set(value); }

    public final ObjectProperty<Duration> animationTimeProperty() { return animationTime; }
    public final Duration getAnimationTime() { return animationTime.get(); }
    public final void setAnimationTime(Duration value) { animationTime.set(value); }

    public final ObjectProperty<TranslationButtonTheme> themeProperty() { return theme; }
    public final TranslationButtonTheme getTheme() { return theme.get(); }
    public final void setTheme(TranslationButtonTheme value) { theme.set(value); }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        if (onAction == null) {
            onAction = new ObjectPropertyBase<>() {
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
                @Override
                public Object getBean() { return TranslationButton.this; }
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
