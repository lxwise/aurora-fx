package io.aurora.fx.components.lineButton;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * 线条按钮皮肤
 * <p>
 * 负责线条按钮的视觉渲染和动画逻辑。包含一个 Label 和一条 Line，
 * 鼠标悬停时根据动画类型（EXTEND / RISE）播放线条动画。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class LineButtonSkin extends SkinBase<LineButton> {

    private final LineButton control;
    private final Label label;
    private final Line line;
    private final Pane linePane;
    private final Rectangle clipRect = new Rectangle();
    private Timeline animEnter = new Timeline();
    private Timeline animExit = new Timeline();

    // ==================== 事件处理器与监听器（在构造方法中初始化） ====================

    private EventHandler<MouseEvent> enterHandler;
    private EventHandler<MouseEvent> exitHandler;
    private EventHandler<MouseEvent> clickHandler;
    private ChangeListener<LineAnimationType> lineTypeListener;
    private InvalidationListener themeListener;
    private ChangeListener<Number> spacingListener;
    private ChangeListener<Bounds> boundsListener;
    private InvalidationListener animParamListener;

    // ==================== 构造方法 ====================

    public LineButtonSkin(LineButton control) {
        super(control);
        this.control = control;

        // 初始化 Label
        label = new Label();
        bindLabelProperties();

        // 初始化 Line
        line = new Line();
        line.getStyleClass().add("line");
        line.setStartX(0);

        // 线条容器（带裁剪）
        linePane = new Pane(line);
        clipRect.widthProperty().bind(linePane.widthProperty());
        clipRect.heightProperty().bind(linePane.heightProperty());
        linePane.setClip(clipRect);

        getChildren().addAll(label, linePane);

        // 初始化事件处理器
        enterHandler = e -> playAnimEnter();
        exitHandler = e -> playAnimExit();
        clickHandler = e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                control.fireEvent(new ActionEvent());
            }
        };

        // 初始化监听器
        lineTypeListener = (obs, ov, nv) -> initAnimation();
        themeListener = obs -> applyTheme();
        spacingListener = (obs, ov, nv) -> {
            Bounds bounds = label.getBoundsInParent();
            double maxY = bounds.getMaxY() + nv.doubleValue();
            line.setStartY(maxY);
            line.setEndY(maxY);
        };
        boundsListener = (obs, ov, nv) -> {
            line.setStartX(nv.getMinX());
            line.setEndX(nv.getMaxX());
            double maxY = nv.getMaxY() + control.getSpacing();
            line.setStartY(maxY);
            line.setEndY(maxY);
        };
        animParamListener = obs -> initAnimation();

        // 注册监听
        control.lineTypeProperty().addListener(lineTypeListener);
        control.themeProperty().addListener(themeListener);
        control.spacingProperty().addListener(spacingListener);
        control.offsetYProperty().addListener(animParamListener);
        control.animationTimeProperty().addListener(animParamListener);
        label.boundsInParentProperty().addListener(boundsListener);

        // 鼠标事件
        control.addEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        control.addEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        control.addEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);

        applyTheme();
        initAnimation();
    }

    // ==================== 动画逻辑 ====================

    private void initAnimation() {
        stopAnimations();

        LineAnimationType type = control.getLineType();
        switch (type) {
            case RISE:
                initRiseAnimation();
                break;
            case EXTEND:
            default:
                initExtendAnimation();
                break;
        }
    }

    private void initExtendAnimation() {
        line.setOpacity(1.0);
        line.setTranslateY(0);

        if (control.isHover()) {
            line.setScaleX(1.0);
        } else {
            line.setScaleX(0.0);
        }

        animEnter.getKeyFrames().setAll(
                new KeyFrame(control.getAnimationTime(),
                        new KeyValue(line.scaleXProperty(), 1.0)));

        animExit.getKeyFrames().setAll(
                new KeyFrame(control.getAnimationTime(),
                        new KeyValue(line.scaleXProperty(), 0.0)));
    }

    private void initRiseAnimation() {
        line.setScaleX(1.0);
        double offsetY = control.getOffsetY();

        if (control.isHover()) {
            line.setTranslateY(0);
            line.setOpacity(1.0);
        } else {
            line.setTranslateY(offsetY);
            line.setOpacity(0.0);
        }

        animEnter.getKeyFrames().setAll(
                new KeyFrame(control.getAnimationTime(),
                        new KeyValue(line.translateYProperty(), 0),
                        new KeyValue(line.opacityProperty(), 1.0)));

        animExit.getKeyFrames().setAll(
                new KeyFrame(control.getAnimationTime(),
                        new KeyValue(line.translateYProperty(), offsetY),
                        new KeyValue(line.opacityProperty(), 0.0)));
    }

    private void playAnimEnter() {
        if (animExit.getStatus() == Animation.Status.RUNNING) {
            animExit.stop();
        }
        animEnter.play();
    }

    private void playAnimExit() {
        if (animEnter.getStatus() == Animation.Status.RUNNING) {
            animEnter.stop();
        }
        animExit.play();
    }

    private void stopAnimations() {
        if (animEnter.getStatus() != Animation.Status.STOPPED) animEnter.stop();
        if (animExit.getStatus() != Animation.Status.STOPPED) animExit.stop();
    }

    // ==================== 主题应用 ====================

    private void applyTheme() {
        LineButtonTheme theme = control.getTheme();
        if (theme == null) return;

        label.setStyle(theme.getTextStyle());
        line.setStroke(theme.getLineColor());
        line.setStrokeWidth(theme.getLineWidth());
    }

    // ==================== 属性绑定 ====================

    private void bindLabelProperties() {
        label.ellipsisStringProperty().bind(control.ellipsisStringProperty());
        label.textFillProperty().bind(control.textFillProperty());
        label.fontProperty().bind(control.fontProperty());
        label.graphicProperty().bind(control.graphicProperty());
        label.contentDisplayProperty().bind(control.contentDisplayProperty());
        label.graphicTextGapProperty().bind(control.graphicTextGapProperty());
        label.alignmentProperty().bind(control.alignmentProperty());
        label.mnemonicParsingProperty().bind(control.mnemonicParsingProperty());
        label.textProperty().bind(control.textProperty());
        label.textAlignmentProperty().bind(control.textAlignmentProperty());
        label.textOverrunProperty().bind(control.textOverrunProperty());
        label.wrapTextProperty().bind(control.wrapTextProperty());
        label.underlineProperty().bind(control.underlineProperty());
        label.lineSpacingProperty().bind(control.lineSpacingProperty());
    }

    private void unbindLabelProperties() {
        label.ellipsisStringProperty().unbind();
        label.textFillProperty().unbind();
        label.fontProperty().unbind();
        label.graphicProperty().unbind();
        label.contentDisplayProperty().unbind();
        label.graphicTextGapProperty().unbind();
        label.alignmentProperty().unbind();
        label.mnemonicParsingProperty().unbind();
        label.textProperty().unbind();
        label.textAlignmentProperty().unbind();
        label.textOverrunProperty().unbind();
        label.wrapTextProperty().unbind();
        label.underlineProperty().unbind();
        label.lineSpacingProperty().unbind();
    }

    // ==================== 布局 ====================

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return label.minWidth(height);
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return label.minHeight(width);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return label.prefWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return label.prefHeight(width) + topInset + bottomInset;
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        layoutInArea(label, contentX, contentY, contentWidth, contentHeight, 0,
                control.getAlignment().getHpos(), control.getAlignment().getVpos());
        layoutInArea(linePane, contentX, contentY, contentWidth, contentHeight, 0,
                control.getAlignment().getHpos(), control.getAlignment().getVpos());
    }

    // ==================== 公开 Getter ====================

    public Line getLine() { return line; }
    public Label getLabel() { return label; }
    public Timeline getAnimEnter() { return animEnter; }
    public Timeline getAnimExit() { return animExit; }

    // ==================== 资源释放 ====================

    @Override
    public void dispose() {
        stopAnimations();
        animEnter = null;
        animExit = null;

        clipRect.widthProperty().unbind();
        clipRect.heightProperty().unbind();
        unbindLabelProperties();

        control.lineTypeProperty().removeListener(lineTypeListener);
        control.themeProperty().removeListener(themeListener);
        control.spacingProperty().removeListener(spacingListener);
        control.offsetYProperty().removeListener(animParamListener);
        control.animationTimeProperty().removeListener(animParamListener);
        label.boundsInParentProperty().removeListener(boundsListener);

        control.removeEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        control.removeEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        control.removeEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);

        getChildren().clear();
        super.dispose();
    }
}
