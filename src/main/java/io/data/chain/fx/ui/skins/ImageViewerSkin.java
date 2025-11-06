package io.data.chain.fx.ui.skins;

import io.data.chain.fx.common.utils.ClipboardUtils;
import io.data.chain.fx.common.utils.TimelineBuilder;
import io.data.chain.fx.ui.controls.ImageViewer;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

public class ImageViewerSkin extends SkinBase<ImageViewer> {

    private final Viewer viewer;

    public ImageViewerSkin(ImageViewer control) {
        super(control);
        viewer = new Viewer();
        getChildren().add(viewer);
        // 剪切
        ClipboardUtils.clipRect(viewer, new SimpleDoubleProperty(0));
        // 鼠标滚动放大缩小
        control.addEventHandler(ScrollEvent.SCROLL, event -> viewer.imgScale(event.getDeltaY() > 0));
        // 上一个，下一个
        control.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN -> viewer.imgScale(false);
                case UP -> viewer.imgScale(true);
                case LEFT -> viewer.toPrevious();
                case RIGHT -> viewer.toNext();
            }
        });
        // init image
        viewer.setImage(control.getImages().getFirst());
    }

    /**
     * 图片预览容器
     */
    public class Viewer extends Region {

        private final ImageView imageView;
        private final HBox buttons;
        private final Label information;

        private final Rotate rotate_z = new Rotate(0, Rotate.Z_AXIS); // Z轴旋转
        private final Rotate rotate_y = new Rotate(0, Rotate.Y_AXIS); // Y轴旋转
        private final Rotate rotate_x = new Rotate(0, Rotate.X_AXIS); // X轴旋转

        public Viewer() {
            imageView = new ImageView();
            imageView.getTransforms().addAll(rotate_z, rotate_x, rotate_y); // 旋转；翻转
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageViewMove();// 图片拖动
            buttons = new HBox();
            initButtons();// 初始化按钮
            information = new Label();
            information.setStyle("""
                    -fx-text-fill: white;
                    -fx-background-color: rgba(0,0,0,0.3);
                    -fx-background-radius: 0;
                    -fx-padding: 3px 5px;
                    """);
            getChildren().addAll(imageView, buttons, information);
            // 图片变化监听
            image.addListener((observable, v1, v2) -> {
                imageView.setImage(v2);
                setInformation(v2);
            });
            // 监听布局变化
            layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
                resetLayout();
                selfAdaption();
            });
            imageView.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> resetLayout());
        }

        private void setInformation(Image image) {
            ObservableList<Image> images = getSkinnable().getImages();
            int indexOf = images.indexOf(image);
            information.setText("%s*%s  %s/%s".formatted((int) image.getWidth(), (int) image.getHeight(), indexOf + 1, images.size()));
        }

        /**
         * 初始化按钮
         */
        private void initButtons() {
            // 操作按钮
            FontIcon zoomIn = getIcon(BoxiconsRegular.ZOOM_IN);
            zoomIn.setOnMouseClicked(event -> imgScale(true));
            FontIcon zoomOut = getIcon(BoxiconsRegular.ZOOM_OUT);
            zoomOut.setOnMouseClicked(event -> imgScale(false));
            FontIcon oneToOne = getIcon(AntDesignIconsOutlined.ONE_TO_ONE);
            oneToOne.setOnMouseClicked(event -> oneToOne());
            FontIcon reload = getIcon(BoxiconsRegular.REVISION);
            reload.setOnMouseClicked(event -> restore());
            FontIcon previous = getIcon(BoxiconsRegular.SKIP_PREVIOUS);
            previous.setOnMouseClicked(event -> toPrevious());
            FontIcon play = getIcon(BoxiconsRegular.PLAY);
            // 播放
            play.setOnMouseClicked(event -> loopPlayAnimation());
            play.setIconSize(30);
            FontIcon next = getIcon(BoxiconsRegular.SKIP_NEXT);
            next.setOnMouseClicked(event -> toNext());
            FontIcon rotateLeft = getIcon(BoxiconsRegular.ROTATE_LEFT);
            rotateLeft.setOnMouseClicked(event -> imgRotate(-90));
            FontIcon rotateRight = getIcon(BoxiconsRegular.ROTATE_RIGHT);
            rotateRight.setOnMouseClicked(event -> imgRotate(90));
            FontIcon moveHorizontal = getIcon(BoxiconsRegular.MOVE_HORIZONTAL);
            moveHorizontal.setOnMouseClicked(event -> imgFlip(false, true));
            FontIcon moveVertical = getIcon(BoxiconsRegular.MOVE_VERTICAL);
            moveVertical.setOnMouseClicked(event -> imgFlip(true, false));
            buttons.getChildren().addAll(zoomIn, zoomOut, oneToOne, reload, previous, play, next, rotateLeft, rotateRight, moveHorizontal, moveVertical);
            StackPane.setAlignment(buttons, Pos.BOTTOM_CENTER);
            buttons.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            buttons.setStyle("""
                    -fx-padding: 0 10px;
                    -fx-alignment: center;
                    -fx-spacing: 10px;
                    -fx-pref-height: 36px;
                    -fx-background-radius: 18px;
                    -fx-background-color: rgb(0,0,0,0.3);
                    """);
            // 操作按钮布局
            buttons.layoutXProperty().bind(widthProperty().subtract(buttons.widthProperty()).divide(2));
            buttons.layoutYProperty().bind(heightProperty().subtract(buttons.heightProperty()).subtract(20));
        }

        /**********************************************************************
         *                              图片
         **********************************************************************/

        private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

        public void setImage(Image image) {
            this.image.set(image);
            restore();
        }

        public Image getImage() {
            return image.get();
        }

        // 重置布局
        public void resetLayout() {
            Bounds imgBounds = imageView.getLayoutBounds();
            // 调整pane位置
            imageView.setTranslateX((getWidth() - imgBounds.getWidth()) / 2);
            imageView.setTranslateY((getHeight() - imgBounds.getHeight()) / 2);
        }

        /**
         * 下一个
         */
        public void toNext() {
            ObservableList<Image> images = getSkinnable().getImages();
            int size = images.size();
            if (size > 1) {
                switchAnimation();
                int i = images.indexOf(viewer.getImage());
                if (i >= images.size() - 1) {
                    viewer.setImage(images.getFirst());
                } else {
                    viewer.setImage(images.get(i + 1));
                }
            }
        }

        /**
         * 上一个
         */
        public void toPrevious() {
            ObservableList<Image> images = getSkinnable().getImages();
            int size = images.size();
            if (size > 1) {
                switchAnimation();
                int i = images.indexOf(viewer.getImage());
                if (i <= 0) {
                    viewer.setImage(images.get(size - 1));
                } else {
                    viewer.setImage(images.get(i - 1));
                }
            }
        }

        private Timeline switchTimeLine;

        /**
         * 切换动画
         */
        private void switchAnimation() {
            if (switchTimeLine == null) {
                switchTimeLine = TimelineBuilder.builder()
                        .addKeyFrame(imageView.opacityProperty(), 0, Duration.ZERO)
                        .addKeyFrame(imageView.opacityProperty(), 1, Duration.seconds(1))
                        .build();
            }
            switchTimeLine.play();
        }

        private Timeline loopPlayTimeline;

        /**
         * 循环播放
         */
        private void loopPlayAnimation() {
            if (loopPlayTimeline == null) {
                loopPlayTimeline = TimelineBuilder.builder()
                        .addKeyFrame(opacityProperty(), 1, Duration.ZERO)
                        .addKeyFrame(opacityProperty(), 1, Duration.seconds(5), e -> toNext())
                        .cycleCount(Timeline.INDEFINITE)
                        .build();
                loopPlayTimeline.statusProperty().addListener((observable, oldValue, newValue) -> {
                    if (Animation.Status.RUNNING.equals(newValue)) {
                        getChildren().remove(buttons);
                    } else {
                        getChildren().add(buttons);
                    }
                });
                setOnMouseClicked(event -> { // 点击停止动画播放
                    loopPlayTimeline.stop();
                });
            }
            loopPlayTimeline.play();
        }


        /**
         * 还原
         */
        public void restore() {
            rotate_x.setAngle(0);
            rotate_y.setAngle(0);
            rotate_z.setAngle(0);
            imageView.setLayoutX(0);
            imageView.setLayoutY(0);
            selfAdaption();
        }

        /**
         * 自适应
         */
        public void selfAdaption() {
            Image image = imageView.getImage();
            int i = (int) rotate_z.getAngle() % 180;
            if ((i == 0 && image.getWidth() < getWidth() && image.getHeight() < getHeight())
                    || (i == 90 && image.getWidth() < getHeight() && image.getHeight() < getWidth())
            ) {
                imageView.setScaleX(1);
                imageView.setScaleY(1);
            } else {
                double w, h;
                if (i == 0) {
                    w = getWidth() / image.getWidth();
                    h = getHeight() / image.getHeight();
                } else {
                    w = getWidth() / image.getHeight();
                    h = getHeight() / image.getWidth();
                }
                double min = Math.min(w, h);
                imageView.setScaleX(min);
                imageView.setScaleY(min);
            }
        }

        /**
         * 一比一显示
         */
        public void oneToOne() {
            int i = (int) rotate_z.getAngle() % 180;
            Image image = imageView.getImage();
            Bounds bounds = imageView.getBoundsInParent();
            if ((i == 0 && bounds.getWidth() == image.getWidth())
                    || (i == 90 && bounds.getHeight() == image.getWidth())) {
                selfAdaption();
            } else {
                imageView.setScaleX(1);
                imageView.setScaleY(1);
            }
            imageView.setLayoutX(0);
            imageView.setLayoutY(0);
        }

        // =============================================================================
        //  图片缩放
        // =============================================================================

        private ScaleTransition st;

        public void imgScale(boolean val) {
            if (st == null) {
                st = new ScaleTransition(Duration.millis(250), imageView);
                st.setInterpolator(Interpolator.EASE_BOTH);
                st.fromXProperty().bind(imageView.scaleXProperty());
                st.fromYProperty().bind(imageView.scaleYProperty());
            }
            double v = imageView.getScaleX() + (val ? 0.05 : -0.05);
            if (v <= 0.01 || Animation.Status.RUNNING.equals(st.getStatus())) {
                return;
            }
            st.setToX(v);
            st.setToY(v);
            st.play();
        }

        // =============================================================================
        //  图片旋转
        // =============================================================================

        private Timeline timeline_z, timeline_y, timeline_x;

        /**
         * 图片旋转
         *
         * @param angle：旋转角度
         */
        public void imgRotate(double angle) {
            if (timeline_z == null) {
                timeline_z = TimelineBuilder.builder()
                        .build();
            }
            if (Animation.Status.RUNNING.equals(timeline_z.getStatus())) {
                return;
            }
            // 设置旋转参数
            Bounds bounds = imageView.getLayoutBounds();
            rotate_z.setPivotX(bounds.getCenterX());
            rotate_z.setPivotY(bounds.getCenterY());
            TimelineBuilder.builder(timeline_z)
                    .clearKeyFrames()
                    .addKeyFrame(rotate_z.angleProperty(), rotate_z.getAngle(), Duration.ZERO)
                    .addKeyFrame(rotate_z.angleProperty(), rotate_z.getAngle() + angle, Duration.millis(250));
            timeline_z.play();
        }

        // =============================================================================
        //  图片镜像翻转
        // =============================================================================

        /**
         * 图片镜像翻转
         *
         * @param x : 是否垂直翻转
         * @param y : 是否水平旋转
         */
        public void imgFlip(boolean x, boolean y) {
            // 设置翻转参数
            Bounds bounds = imageView.getLayoutBounds();
            if (x) {
                if (timeline_x == null) {
                    timeline_x = TimelineBuilder.builder()
                            .build();
                }
                if (Animation.Status.RUNNING.equals(timeline_x.getStatus())) {
                    return;
                }
                rotate_x.setPivotX(bounds.getCenterX());
                rotate_x.setPivotY(bounds.getCenterY());
                TimelineBuilder.builder(timeline_x)
                        .clearKeyFrames()
                        .addKeyFrame(rotate_x.angleProperty(), rotate_x.getAngle(), Duration.ZERO)
                        .addKeyFrame(rotate_x.angleProperty(), rotate_x.getAngle() == 0 ? 180 : 0, Duration.millis(250));
                timeline_x.play();
            }
            if (y) {
                if (timeline_y == null) {
                    timeline_y = TimelineBuilder.builder()
                            .build();
                }
                if (Animation.Status.RUNNING.equals(timeline_y.getStatus())) {
                    return;
                }
                rotate_y.setPivotX(bounds.getCenterX());
                rotate_y.setPivotY(bounds.getCenterY());
                TimelineBuilder.builder(timeline_y)
                        .clearKeyFrames()
                        .addKeyFrame(rotate_y.angleProperty(), rotate_y.getAngle(), Duration.ZERO)
                        .addKeyFrame(rotate_y.angleProperty(), rotate_y.getAngle() == 0 ? 180 : 0, Duration.millis(250));
                timeline_y.play();
            }
        }

        private FontIcon getIcon(Ikon ikon) {
            FontIcon fontIcon = FontIcon.of(ikon, 20, Color.WHITE);
            fontIcon.setCursor(Cursor.HAND);
            return fontIcon;
        }


        /**********************************************************************
         *                              拖拽图片位置
         **********************************************************************/

        private double xOffset = 0, yOffset = 0;

        private void imageViewMove() {
            // 拖拽图标
            this.imageView.cursorProperty()
                    .bind(Bindings.createObjectBinding(() ->
                            imageView.isPressed() ? Cursor.MOVE : Cursor.DEFAULT, this.imageView.pressedProperty()));
            // 按下
            this.imageView.setOnMousePressed(event -> {
                event.consume();
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            this.imageView.setOnMouseDragged(event -> {
                event.consume();
                // 计算鼠标偏移量
                double deltaX = event.getSceneX() - xOffset;
                double deltaY = event.getSceneY() - yOffset;
                imageView.setLayoutX(imageView.getLayoutX() + deltaX);
                imageView.setLayoutY(imageView.getLayoutY() + deltaY);
                // 更新偏移量
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
        }
    }

}
