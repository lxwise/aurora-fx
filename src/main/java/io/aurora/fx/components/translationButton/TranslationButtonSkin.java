package io.aurora.fx.components.translationButton;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * 平移按钮皮肤
 * <p>
 * 负责平移按钮的视觉渲染和动画逻辑。包含两层 Label（hoverLabel / nonHoverLabel），
 * 鼠标悬停时通过 Timeline 动画实现平移切换效果。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class TranslationButtonSkin extends SkinBase<TranslationButton> {

    private final TranslationButton control;
    private final StackPane rootPane;
    private final Label hoverLabel;
    private final Label nonHoverLabel;
    private final Rectangle rectClip = new Rectangle();
    private Timeline animEnter = new Timeline();
    private Timeline animExit = new Timeline();

    // ==================== 事件处理器 ====================

    private final EventHandler<MouseEvent> enterHandler = e -> playAnimEnter();
    private final EventHandler<MouseEvent> exitHandler = e -> playAnimExit();
    private final EventHandler<MouseEvent> clickHandler = e -> {
        if (e.getButton() == MouseButton.PRIMARY) {
            getSkinnable().fireEvent(new ActionEvent());
        }
    };

    private final InvalidationListener updateAnimListener = obs -> updateAnimation();
    private final InvalidationListener themeChangeListener = obs -> applyTheme();

    /** 监听 control.graphic 变化自动同步到 hoverLabel（在构造方法中初始化） */
    private ChangeListener<javafx.scene.Node> graphicSyncListener;

    // ==================== 构造方法 ====================

    public TranslationButtonSkin(TranslationButton control) {
        super(control);
        this.control = control;

        // 初始化悬停 Label
        hoverLabel = new Label();
        hoverLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        hoverLabel.setAlignment(Pos.CENTER);
        hoverLabel.getStyleClass().add("hover-label");

        // 初始化非悬停 Label
        nonHoverLabel = new Label();
        nonHoverLabel.setAlignment(Pos.CENTER);
        nonHoverLabel.getStyleClass().add("non-hover-label");

        // 根容器
        rootPane = new StackPane();
        rootPane.getStyleClass().add("translation-pane");
        rootPane.getChildren().addAll(hoverLabel, nonHoverLabel);
        getChildren().setAll(rootPane);

        // 裁剪区域
        rectClip.widthProperty().bind(rootPane.widthProperty());
        rectClip.heightProperty().bind(rootPane.heightProperty());
        rootPane.setClip(rectClip);

        // 尺寸绑定
        hoverLabel.prefWidthProperty().bind(rootPane.widthProperty());
        hoverLabel.prefHeightProperty().bind(rootPane.heightProperty());
        nonHoverLabel.prefWidthProperty().bind(rootPane.widthProperty());
        nonHoverLabel.prefHeightProperty().bind(rootPane.heightProperty());

        // 属性绑定 - nonHoverLabel 跟随 control 的文本属性
        bindLabelProperties(nonHoverLabel);
        // hoverLabel 的 graphic 通过监听同步（而非绑定），允许用户通过 getHoverLabel().setGraphic() 覆盖
        graphicSyncListener = (obs, ov, nv) -> hoverLabel.setGraphic(nv);
        hoverLabel.setGraphic(control.getGraphic());
        control.graphicProperty().addListener(graphicSyncListener);

        // 监听方向、尺寸变化 -> 更新动画
        control.directionProperty().addListener(updateAnimListener);
        control.animationTimeProperty().addListener(updateAnimListener);
        rootPane.widthProperty().addListener(updateAnimListener);
        rootPane.heightProperty().addListener(updateAnimListener);

        // 监听主题变化
        control.themeProperty().addListener(themeChangeListener);

        // 鼠标事件
        control.addEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        control.addEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        control.addEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);

        applyTheme();
    }

    // ==================== 动画逻辑 ====================

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

    private void updateAnimation() {
        stopAnimations();
        nonHoverLabel.setTranslateX(0);
        nonHoverLabel.setTranslateY(0);

        TranslationDirection dir = control.getDirection();
        boolean isVertical = (dir == TranslationDirection.BOTTOM_TO_TOP || dir == TranslationDirection.TOP_TO_BOTTOM);

        if (isVertical) {
            boolean upward = (dir == TranslationDirection.BOTTOM_TO_TOP);
            double h = rootPane.getHeight();
            hoverLabel.setTranslateX(0);
            hoverLabel.setTranslateY(upward ? h : -h);

            animEnter.getKeyFrames().setAll(new KeyFrame(control.getAnimationTime(),
                    new KeyValue(nonHoverLabel.translateYProperty(), upward ? -h : h),
                    new KeyValue(hoverLabel.translateYProperty(), 0)));

            animExit.getKeyFrames().setAll(new KeyFrame(control.getAnimationTime(),
                    new KeyValue(nonHoverLabel.translateYProperty(), 0),
                    new KeyValue(hoverLabel.translateYProperty(), upward ? h : -h)));

            if (control.isHover()) {
                nonHoverLabel.setTranslateY(upward ? -h : h);
                hoverLabel.setTranslateY(0);
            }
        } else {
            boolean leftward = (dir == TranslationDirection.RIGHT_TO_LEFT);
            double w = rootPane.getWidth();
            hoverLabel.setTranslateY(0);
            hoverLabel.setTranslateX(leftward ? w : -w);

            animEnter.getKeyFrames().setAll(new KeyFrame(control.getAnimationTime(),
                    new KeyValue(nonHoverLabel.translateXProperty(), leftward ? -w : w),
                    new KeyValue(hoverLabel.translateXProperty(), 0)));

            animExit.getKeyFrames().setAll(new KeyFrame(control.getAnimationTime(),
                    new KeyValue(nonHoverLabel.translateXProperty(), 0),
                    new KeyValue(hoverLabel.translateXProperty(), leftward ? w : -w)));

            if (control.isHover()) {
                nonHoverLabel.setTranslateX(leftward ? -w : w);
                hoverLabel.setTranslateX(0);
            }
        }
    }

    private void stopAnimations() {
        if (animEnter.getStatus() != Animation.Status.STOPPED) animEnter.stop();
        if (animExit.getStatus() != Animation.Status.STOPPED) animExit.stop();
    }

    // ==================== 主题应用 ====================

    private void applyTheme() {
        TranslationButtonTheme theme = control.getTheme();
        if (theme == null) return;
        rootPane.setStyle(theme.getDefaultPaneStyle());
        nonHoverLabel.setStyle(theme.getDefaultTextStyle());
        hoverLabel.setStyle(theme.getHoverTextStyle());
    }

    // ==================== 属性绑定 ====================

    private void bindLabelProperties(Label label) {
        label.ellipsisStringProperty().bind(control.ellipsisStringProperty());
        label.textFillProperty().bind(control.textFillProperty());
        label.fontProperty().bind(control.fontProperty());
        label.textProperty().bind(control.textProperty());
        label.contentDisplayProperty().bind(control.contentDisplayProperty());
        label.graphicTextGapProperty().bind(control.graphicTextGapProperty());
        label.alignmentProperty().bind(control.alignmentProperty());
        label.mnemonicParsingProperty().bind(control.mnemonicParsingProperty());
        label.textAlignmentProperty().bind(control.textAlignmentProperty());
        label.textOverrunProperty().bind(control.textOverrunProperty());
        label.wrapTextProperty().bind(control.wrapTextProperty());
        label.underlineProperty().bind(control.underlineProperty());
        label.lineSpacingProperty().bind(control.lineSpacingProperty());
    }

    private void unbindLabelProperties(Label label) {
        label.ellipsisStringProperty().unbind();
        label.textFillProperty().unbind();
        label.fontProperty().unbind();
        label.textProperty().unbind();
        label.contentDisplayProperty().unbind();
        label.graphicTextGapProperty().unbind();
        label.alignmentProperty().unbind();
        label.mnemonicParsingProperty().unbind();
        label.textAlignmentProperty().unbind();
        label.textOverrunProperty().unbind();
        label.wrapTextProperty().unbind();
        label.underlineProperty().unbind();
        label.lineSpacingProperty().unbind();
    }

    // ==================== 布局 ====================

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        layoutInArea(rootPane, contentX, contentY, contentWidth, contentHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    // ==================== 公开 Getter ====================

    public Label getHoverLabel() { return hoverLabel; }
    public Label getNonHoverLabel() { return nonHoverLabel; }

    // ==================== 资源释放 ====================

    @Override
    public void dispose() {
        stopAnimations();
        animEnter = null;
        animExit = null;

        // 解绑
        rectClip.widthProperty().unbind();
        rectClip.heightProperty().unbind();
        hoverLabel.prefWidthProperty().unbind();
        hoverLabel.prefHeightProperty().unbind();
        nonHoverLabel.prefWidthProperty().unbind();
        nonHoverLabel.prefHeightProperty().unbind();
        unbindLabelProperties(nonHoverLabel);
        control.graphicProperty().removeListener(graphicSyncListener);

        // 移除监听
        control.directionProperty().removeListener(updateAnimListener);
        control.animationTimeProperty().removeListener(updateAnimListener);
        rootPane.widthProperty().removeListener(updateAnimListener);
        rootPane.heightProperty().removeListener(updateAnimListener);
        control.themeProperty().removeListener(themeChangeListener);

        // 移除事件
        control.removeEventFilter(MouseEvent.MOUSE_ENTERED, enterHandler);
        control.removeEventFilter(MouseEvent.MOUSE_EXITED, exitHandler);
        control.removeEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);

        getChildren().clear();
        super.dispose();
    }
}
