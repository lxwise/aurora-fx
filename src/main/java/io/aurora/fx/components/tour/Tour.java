package io.aurora.fx.components.tour;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tour 漫游式引导组件
 * <p>
 * 对标 Element Plus 的 Tour 与 Flutter showcaseview，用于在 JavaFX 任意 {@link Node}
 * 上显示一组逐步引导提示。支持模态/非模态、12 种弹窗定位、自定义遮罩、自定义指示器以及
 * 多种类型的目标参数（Node、坐标、空目标）。
 * </p>
 *
 * <h3>基础用法</h3>
 * <pre>{@code
 * Tour tour = new Tour()
 *     .addStep(new TourStep(button1, "标题1", "描述1").placement(TourPlacement.BOTTOM))
 *     .addStep(new TourStep(button2, "标题2", "描述2").placement(TourPlacement.RIGHT))
 *     .onFinish(() -> System.out.println("引导完成"));
 *
 * tour.show(scene);
 * }</pre>
 *
 * <h3>非模态用法</h3>
 * <pre>{@code
 * Tour tour = new Tour()
 *     .mask(false)
 *     .type(TourType.PRIMARY)
 *     .addStep(...);
 * }</pre>
 *
 * @author Tour Component
 * @version 1.0
 */
public class Tour {

    private static final Logger LOGGER = Logger.getLogger(Tour.class.getName());

    // ==================== 核心属性 ====================

    /** 步骤集合 */
    private final ObservableList<TourStep> steps = FXCollections.observableArrayList();
    /** 当前步骤索引 */
    private final IntegerProperty current = new SimpleIntegerProperty(0);
    /** 是否显示遮罩 */
    private final BooleanProperty mask = new SimpleBooleanProperty(true);
    /** 全局遮罩配置 */
    private final ObjectProperty<TourMaskConfig> maskConfig = new SimpleObjectProperty<>(TourMaskConfig.DEFAULT);
    /** 全局主题 */
    private final ObjectProperty<TourTheme> theme = new SimpleObjectProperty<>(TourTheme.DEFAULT);
    /** 全局弹窗类型 */
    private final ObjectProperty<TourType> type = new SimpleObjectProperty<>(TourType.DEFAULT);
    /** 是否显示关闭按钮 */
    private final BooleanProperty showClose = new SimpleBooleanProperty(true);
    /** 是否显示箭头 */
    private final BooleanProperty showArrow = new SimpleBooleanProperty(true);
    /** 是否显示步骤指示器 */
    private final BooleanProperty showIndicators = new SimpleBooleanProperty(true);
    /** 按 ESC 键是否关闭 */
    private final BooleanProperty closeOnEsc = new SimpleBooleanProperty(true);
    /** 按钮文案 - 上一步 */
    private final StringProperty prevButtonText = new SimpleStringProperty("上一步");
    /** 按钮文案 - 下一步 */
    private final StringProperty nextButtonText = new SimpleStringProperty("下一步");
    /** 按钮文案 - 完成 */
    private final StringProperty finishButtonText = new SimpleStringProperty("完成");

    // ==================== UI ====================

    /** 宿主容器（接收 overlay） */
    private Pane host;
    /** 引导覆盖层 */
    private final Pane overlay = new Pane();
    /** 遮罩 Path */
    private final Path maskPath = new Path();
    /** 弹窗容器 */
    private final VBox popup = new VBox();
    /** 弹窗箭头 */
    private final Polygon arrow = new Polygon();

    // ==================== 状态 ====================

    private boolean active = false;
    private boolean disposed = false;

    private ChangeListener<Number> hostSizeListener;
    private ChangeListener<javafx.geometry.Bounds> targetBoundsListener;
    private ChangeListener<javafx.geometry.Bounds> targetBoundsInParentListener;
    private ChangeListener<Number> targetLayoutXListener;
    private ChangeListener<Number> targetLayoutYListener;
    private ChangeListener<Scene> targetSceneListener;
    /** 目标 Node 累计变换监听器（感知任何祖先 transform/滚动变化） */
    private ChangeListener<javafx.scene.transform.Transform> targetLocalToSceneListener;
    /** 跟踪目标 Node 全部祖先链上的 boundsInParent，监听任何祖先布局变化 */
    private final List<Node> trackedAncestors = new ArrayList<>();
    /** 跟踪的 ScrollPane 集合，监听其 hvalue/vvalue（滚动不会触发 bounds 变化） */
    private final List<javafx.scene.control.ScrollPane> trackedScrollPanes = new ArrayList<>();
    private ChangeListener<javafx.geometry.Bounds> ancestorBoundsListener;
    /** Scene 尺寸变化监听器（窗口缩放时重新定位） */
    private ChangeListener<Number> sceneSizeListener;
    private Node lastTargetNode;
    private Scene currentScene;
    private javafx.event.EventHandler<javafx.scene.input.KeyEvent> escHandler;
    /** 标记：是否已挂载持久 overlay 子节点 */
    private boolean overlayChildrenInitialized = false;
    /** 当前 layout 调度令牌，避免步骤切换时陈旧 runLater 任务覆盖最新结果 */
    private long layoutToken = 0L;

    // ==================== 事件回调 ====================

    private Runnable onOpen;
    private Runnable onClose;
    private Runnable onFinish;
    private Consumer<Integer> onChange;

    // ==================== 构造方法 ====================

    public Tour() {
        initOverlay();
    }

    private void initOverlay() {
        overlay.setManaged(false);
        overlay.setPickOnBounds(false);
        overlay.getStyleClass().add("aurora-tour-overlay");

        maskPath.setFillRule(FillRule.EVEN_ODD);
        maskPath.setMouseTransparent(false);

        popup.setManaged(false);
        popup.setSpacing(8);
        popup.getStyleClass().add("aurora-tour-popup");

        arrow.setManaged(false);
        arrow.getStyleClass().add("aurora-tour-arrow");
    }

    // ==================== Builder API ====================

    /**
     * 添加单步
     *
     * @param step 步骤
     * @return this
     */
    public Tour addStep(TourStep step) {
        if (step != null) {
            step.setIndex(steps.size());
            steps.add(step);
        }
        return this;
    }

    /**
     * 批量添加步骤
     *
     * @param ss 步骤数组
     * @return this
     */
    public Tour addSteps(TourStep... ss) {
        if (ss != null) {
            for (TourStep s : ss) {
                addStep(s);
            }
        }
        return this;
    }

    /**
     * 是否显示遮罩
     *
     * @param show true=模态/false=非模态
     * @return this
     */
    public Tour mask(boolean show) { mask.set(show); return this; }

    /**
     * 设置遮罩配置
     *
     * @param cfg 遮罩配置
     * @return this
     */
    public Tour maskConfig(TourMaskConfig cfg) {
        maskConfig.set(cfg != null ? cfg : TourMaskConfig.DEFAULT);
        return this;
    }

    /**
     * 设置主题
     *
     * @param t 主题
     * @return this
     */
    public Tour theme(TourTheme t) {
        theme.set(t != null ? t : TourTheme.DEFAULT);
        return this;
    }

    /**
     * 设置弹窗类型
     *
     * @param t 类型
     * @return this
     */
    public Tour type(TourType t) {
        type.set(t != null ? t : TourType.DEFAULT);
        return this;
    }

    /**
     * 设置是否显示关闭按钮
     *
     * @param show 是否显示
     * @return this
     */
    public Tour showClose(boolean show) { showClose.set(show); return this; }

    /**
     * 设置是否显示箭头
     *
     * @param show 是否显示
     * @return this
     */
    public Tour showArrow(boolean show) { showArrow.set(show); return this; }

    /**
     * 设置是否显示指示器
     *
     * @param show 是否显示
     * @return this
     */
    public Tour showIndicators(boolean show) { showIndicators.set(show); return this; }

    /**
     * 设置按 ESC 是否关闭
     *
     * @param close 是否关闭
     * @return this
     */
    public Tour closeOnEsc(boolean close) { closeOnEsc.set(close); return this; }

    /**
     * 设置上一步按钮文案
     *
     * @param text 文案
     * @return this
     */
    public Tour prevButtonText(String text) { prevButtonText.set(text); return this; }

    /**
     * 设置下一步按钮文案
     *
     * @param text 文案
     * @return this
     */
    public Tour nextButtonText(String text) { nextButtonText.set(text); return this; }

    /**
     * 设置完成按钮文案
     *
     * @param text 文案
     * @return this
     */
    public Tour finishButtonText(String text) { finishButtonText.set(text); return this; }

    /**
     * 注册引导开始回调
     *
     * @param r 回调
     * @return this
     */
    public Tour onOpen(Runnable r) { this.onOpen = r; return this; }

    /**
     * 注册引导关闭回调
     *
     * @param r 回调
     * @return this
     */
    public Tour onClose(Runnable r) { this.onClose = r; return this; }

    /**
     * 注册引导完成回调
     *
     * @param r 回调
     * @return this
     */
    public Tour onFinish(Runnable r) { this.onFinish = r; return this; }

    /**
     * 注册步骤变化回调
     *
     * @param c 回调（参数为新索引）
     * @return this
     */
    public Tour onChange(Consumer<Integer> c) { this.onChange = c; return this; }

    // ==================== 启动/关闭 ====================

    /**
     * 在 Scene 中启动引导
     *
     * @param scene 场景
     * @return this
     */
    public Tour show(Scene scene) {
        if (scene == null) {
            LOGGER.warning("Tour.show: scene 为空");
            return this;
        }
        Parent root = scene.getRoot();
        if (!(root instanceof Pane parentPane)) {
            LOGGER.warning("Tour.show: scene 根节点必须为 Pane 子类");
            return this;
        }
        return show(parentPane);
    }

    /**
     * 在指定容器中启动引导
     *
     * @param container 宿主容器
     * @return this
     */
    public Tour show(Pane container) {
        if (disposed) {
            LOGGER.warning("Tour 已释放");
            return this;
        }
        if (active) {
            LOGGER.fine("Tour 已激活，忽略重复 show");
            return this;
        }
        if (container == null || steps.isEmpty()) {
            return this;
        }

        this.host = container;
        this.currentScene = container.getScene();
        active = true;

        // 挂载 overlay
        attachOverlay();

        // 注册 ESC 处理
        registerEscHandler();

        current.set(0);
        try {
            if (onOpen != null) onOpen.run();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "onOpen 回调异常", e);
        }

        renderCurrentStep();
        return this;
    }

    /**
     * 关闭引导
     */
    public void close() {
        if (!active) {
            return;
        }
        try {
            // 触发当前步骤的 onHide
            fireStepHide(getCurrentStep());

            unregisterEscHandler();
            unbindTarget();
            detachOverlay();

            if (onClose != null) onClose.run();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Tour 关闭异常", e);
        } finally {
            active = false;
            host = null;
            currentScene = null;
        }
    }

    /**
     * 跳转到下一步（最后一步时调用 finish）
     */
    public void next() {
        if (!active) return;
        int idx = current.get();
        if (idx >= steps.size() - 1) {
            finish();
            return;
        }
        goTo(idx + 1);
    }

    /**
     * 跳转到上一步
     */
    public void prev() {
        if (!active) return;
        int idx = current.get();
        if (idx > 0) {
            goTo(idx - 1);
        }
    }

    /**
     * 跳转到指定步骤
     *
     * @param index 步骤索引
     */
    public void goTo(int index) {
        if (!active) return;
        if (index < 0 || index >= steps.size()) {
            LOGGER.warning("goTo 越界: " + index);
            return;
        }
        // 旧步骤 hide 回调
        fireStepHide(getCurrentStep());

        current.set(index);
        if (onChange != null) {
            try {
                onChange.accept(index);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "onChange 回调异常", e);
            }
        }
        renderCurrentStep();
    }

    /**
     * 完成引导（关闭并触发 onFinish）
     */
    public void finish() {
        if (!active) return;
        try {
            fireStepHide(getCurrentStep());
            unregisterEscHandler();
            unbindTarget();
            detachOverlay();
            if (onFinish != null) onFinish.run();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Tour 完成异常", e);
        } finally {
            active = false;
            host = null;
            currentScene = null;
        }
    }

    // ==================== Overlay 挂载 ====================

    private void attachOverlay() {
        host.getChildren().add(overlay);
        overlay.toFront();
        // 同步尺寸
        hostSizeListener = (o, ov, nv) -> overlay.resize(host.getWidth(), host.getHeight());
        host.widthProperty().addListener(hostSizeListener);
        host.heightProperty().addListener(hostSizeListener);
        overlay.resize(host.getWidth(), host.getHeight());
        // 重置持久子节点标记，以便本次 show 中重新初始化
        overlayChildrenInitialized = false;
    }

    private void detachOverlay() {
        try {
            if (hostSizeListener != null && host != null) {
                host.widthProperty().removeListener(hostSizeListener);
                host.heightProperty().removeListener(hostSizeListener);
                hostSizeListener = null;
            }
            if (host != null) {
                host.getChildren().remove(overlay);
            }
            overlay.getChildren().clear();
            overlayChildrenInitialized = false;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "卸载 overlay 异常", e);
        }
    }

    private void registerEscHandler() {
        if (currentScene == null) return;
        escHandler = e -> {
            if (closeOnEsc.get() && e.getCode() == KeyCode.ESCAPE) {
                close();
                e.consume();
            }
        };
        currentScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, escHandler);
    }

    private void unregisterEscHandler() {
        if (escHandler != null && currentScene != null) {
            currentScene.removeEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, escHandler);
            escHandler = null;
        }
    }

    // ==================== 渲染 ====================
    
    private void renderCurrentStep() {
        if (!active) return;
        TourStep step = getCurrentStep();
        if (step == null) return;
    
        // 重新绑定目标 bounds 监听（步骤切换的关键）
        rebindTarget(step);
    
        // 触发 onShow
        try {
            if (step.getOnShow() != null) step.getOnShow().accept(step);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "onShow 回调异常", e);
        }
    
        // 使用令牌机制避免陈旧任务覆盖
        final long token = ++layoutToken;
        // 首次同步渲染，让 popup/mask 如期出现
        renderStepContent(step);
        // 下一帧进行一次修正性定位，此时 popup 的 prefWidth/Height 已稳定
        Platform.runLater(() -> {
            if (token != layoutToken || !active) return;
            layoutOverlay();
        });
    }
    
    /**
     * 首次同步重建 overlay 内容与初步定位。
     * <p>其中 popup/arrow/maskPath 始终作为 overlay 的永久子节点，避免反复 clear/add
     * 造成 layout 失同步。</p>
     */
    private void renderStepContent(TourStep step) {
        if (host == null) return;
    
        TourTheme th = step.getTheme() != null ? step.getTheme() : theme.get();
        TourMaskConfig mc = step.getMaskConfig() != null ? step.getMaskConfig() : maskConfig.get();
        TourType tp = step.getType() != null ? step.getType() : type.get();
        boolean showMaskEffective = step.getShowMask() != null ? step.getShowMask() : mask.get();
    
        // 持久化子节点初始化（仅首次起作用）
        ensureOverlayChildren();
    
        // 1. 重建 popup 内容
        rebuildPopup(step, th, tp);
    
        // 2. 控制隔罩与高亮框可见性
        maskPath.setVisible(showMaskEffective);
        maskPath.setManaged(false);
    
        // 3. 初步布局一次（首次调用时尽量贴近最终位置）
        layoutOverlay();
    }
    
    /**
     * 确保 overlay 中 popup/maskPath/arrow 子节点始终存在。
     * <p>不用 clear()+add()，避免节点被重复从 scene graph 中移除导致的
     * CSS / layout 不同步。仅依赖 visible 属性控制是否显示。</p>
     */
    private void ensureOverlayChildren() {
        if (overlayChildrenInitialized) return;
        // 按顺序：maskPath -> highlightRect(动态) -> popup -> arrow
        overlay.getChildren().clear();
        overlay.getChildren().add(maskPath);
        overlay.getChildren().add(popup);
        overlay.getChildren().add(arrow);
        overlayChildrenInitialized = true;
    }
    
    private void layoutOverlay() {
        if (!active || host == null) return;
    
        TourStep step = getCurrentStep();
        if (step == null) return;
    
        // 只在目标 Node 的祖先链上触发 layout，避免对 host 全局 applyCss
        // 产生副作用（会递归到 overlay/popup，调整 popup 子节点状态）。
        Node targetNode = step.getTarget() != null && step.getTarget().isNodeBased() ? step.getTarget().getNode() : null;
        if (targetNode != null && targetNode.getScene() != null) {
            try {
                Parent tp = targetNode.getParent();
                if (tp != null) {
                    tp.applyCss();
                    tp.layout();
                }
            } catch (Exception ignored) {
            }
        }
    
        // host 尚未完成 layout 时延后重试，防止零尺寸下定位全靠上角
        if (host.getWidth() <= 0 || host.getHeight() <= 0) {
            Platform.runLater(this::layoutOverlay);
            return;
        }
    
        TourTheme th = step.getTheme() != null ? step.getTheme() : theme.get();
        TourMaskConfig mc = step.getMaskConfig() != null ? step.getMaskConfig() : maskConfig.get();
        TourType tp = step.getType() != null ? step.getType() : type.get();
        boolean showMaskEffective = step.getShowMask() != null ? step.getShowMask() : mask.get();
    
        // 关键：每次 layout 都重新计算目标在 scene 中的实时 bounds
        Rectangle2D targetBounds = step.getTarget() != null ? step.getTarget().resolveSceneBounds() : null;
        Rectangle2D targetInHost = sceneRectToHost(targetBounds);
    
        // 调整子节点可见性与内容
        ensureOverlayChildren();
    
        // overlay 必须顺同 host 尺寸，避免 host 缩放后 overlay 依然按旧尺寸裁剪
        overlay.resize(host.getWidth(), host.getHeight());
    
        // 1. 隔罩
        if (showMaskEffective) {
            buildMaskPath(targetInHost, mc);
            maskPath.setVisible(true);
        } else {
            maskPath.setVisible(false);
        }
    
        // 2. 高亮描边（动态节点，需要时存在）
        // 移除之前的高亮描边，后根据需要重新插入
        overlay.getChildren().removeIf(n -> n.getProperties().get("_tour_highlight") == Boolean.TRUE);
        if (showMaskEffective && mc.isHighlight() && targetInHost != null) {
            Rectangle hl = buildHighlightRect(targetInHost, mc);
            hl.getProperties().put("_tour_highlight", Boolean.TRUE);
            // 插入到 popup 之前，位于 mask 上方
            int popupIdx = overlay.getChildren().indexOf(popup);
            if (popupIdx < 0) popupIdx = overlay.getChildren().size();
            overlay.getChildren().add(popupIdx, hl);
        }
    
        // 3. 箭头形状与颜色
        boolean arrowVisible = showArrow.get() && targetInHost != null && step.getPlacement() != TourPlacement.CENTER;
        if (arrowVisible) {
            arrow.getPoints().setAll(0.0, 0.0, th.getPopupArrowSize() * 2, 0.0, th.getPopupArrowSize(), th.getPopupArrowSize());
            arrow.setFill(th.getArrowFillColor());
            arrow.setStroke(th.getPopupBorderColor());
            arrow.setStrokeWidth(0.5);
        }
        arrow.setVisible(arrowVisible);
    
        // 4. 强制 popup 完成 CSS 与 layout，此时 prefWidth/Height 才反映当前内容
        popup.applyCss();
        popup.autosize();
        popup.layout();
        // 避免高亮描边 / mask 被插入后 popup 被错误压到下层，始终让 popup 位于顶层
        popup.toFront();
        if (arrow.isVisible()) arrow.toFront();
    
        // 5. 计算定位
        positionPopup(step, targetInHost, th);
    }

    private void buildMaskPath(Rectangle2D targetInHost, TourMaskConfig mc) {
        maskPath.getElements().clear();
        double w = host.getWidth();
        double h = host.getHeight();

        // 外矩形
        maskPath.getElements().add(new MoveTo(0, 0));
        maskPath.getElements().add(new LineTo(w, 0));
        maskPath.getElements().add(new LineTo(w, h));
        maskPath.getElements().add(new LineTo(0, h));
        maskPath.getElements().add(new ClosePath());

        if (targetInHost != null) {
            double pad = mc.getPadding();
            double r = mc.getCornerRadius();
            double x = Math.max(0, targetInHost.getMinX() - pad);
            double y = Math.max(0, targetInHost.getMinY() - pad);
            double tw = Math.min(w - x, targetInHost.getWidth() + pad * 2);
            double th = Math.min(h - y, targetInHost.getHeight() + pad * 2);
            r = Math.min(r, Math.min(tw, th) / 2);

            // 圆角矩形（顺时针）
            maskPath.getElements().add(new MoveTo(x + r, y));
            maskPath.getElements().add(new LineTo(x + tw - r, y));
            maskPath.getElements().add(new ArcTo(r, r, 0, x + tw, y + r, false, true));
            maskPath.getElements().add(new LineTo(x + tw, y + th - r));
            maskPath.getElements().add(new ArcTo(r, r, 0, x + tw - r, y + th, false, true));
            maskPath.getElements().add(new LineTo(x + r, y + th));
            maskPath.getElements().add(new ArcTo(r, r, 0, x, y + th - r, false, true));
            maskPath.getElements().add(new LineTo(x, y + r));
            maskPath.getElements().add(new ArcTo(r, r, 0, x + r, y, false, true));
            maskPath.getElements().add(new ClosePath());
        }

        Color base = mc.getColor();
        Color withAlpha = new Color(base.getRed(), base.getGreen(), base.getBlue(), mc.getOpacity());
        maskPath.setFill(withAlpha);
        maskPath.setStroke(Color.TRANSPARENT);
        maskPath.setOnMouseClicked(e -> {
            if (mc.isDismissOnMaskClick()) {
                close();
                e.consume();
            }
        });
    }

    private Rectangle buildHighlightRect(Rectangle2D targetInHost, TourMaskConfig mc) {
        double pad = mc.getPadding();
        double r = mc.getCornerRadius();
        Rectangle rect = new Rectangle(
                targetInHost.getMinX() - pad,
                targetInHost.getMinY() - pad,
                targetInHost.getWidth() + pad * 2,
                targetInHost.getHeight() + pad * 2);
        rect.setArcWidth(r * 2);
        rect.setArcHeight(r * 2);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(mc.getHighlightColor());
        rect.setStrokeWidth(mc.getHighlightWidth());
        rect.setMouseTransparent(true);
        return rect;
    }

    private void rebuildPopup(TourStep step, TourTheme th, TourType tp) {
        popup.getChildren().clear();
        popup.setSpacing(10);
        popup.setPadding(new Insets(th.getPopupPadding()));
        popup.setMinWidth(th.getPopupMinWidth());
        popup.setMaxWidth(th.getPopupMaxWidth());

        boolean primary = tp == TourType.PRIMARY;
        Color bgColor = primary ? th.getPrimaryColor() : th.getPopupBackground();
        Color titleColor = primary ? Color.WHITE : th.getTitleColor();
        Color descColor = primary ? Color.web("#FFFFFF", 0.85) : th.getDescriptionColor();

        popup.setStyle(String.format(
                "-fx-background-color: %s;" +
                "-fx-background-radius: %.0f;" +
                "-fx-border-color: %s;" +
                "-fx-border-radius: %.0f;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), %.0f, 0, 0, 4);",
                TourTheme.toCssColor(bgColor),
                th.getPopupCornerRadius(),
                TourTheme.toCssColor(primary ? bgColor : th.getPopupBorderColor()),
                th.getPopupCornerRadius(),
                th.getDropShadowRadius()));

        // 头部：标题 + 关闭按钮
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(step.getTitle() == null ? "" : step.getTitle());
        titleLabel.setFont(Font.font(th.getFontFamily(), FontWeight.BOLD, th.getTitleFontSize()));
        titleLabel.setTextFill(titleColor);
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().add(titleLabel);

        if (showClose.get()) {
            Button closeBtn = buildCloseButton(primary ? Color.WHITE : th.getCloseIconColor());
            closeBtn.setOnAction(e -> close());
            header.getChildren().add(closeBtn);
        }
        popup.getChildren().add(header);

        // 内容区
        if (step.getContentSlot() != null) {
            popup.getChildren().add(step.getContentSlot());
        } else if (step.getDescription() != null && !step.getDescription().isEmpty()) {
            Label descLabel = new Label(step.getDescription());
            descLabel.setFont(Font.font(th.getFontFamily(), th.getDescriptionFontSize()));
            descLabel.setTextFill(descColor);
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(th.getPopupMaxWidth() - th.getPopupPadding() * 2);
            popup.getChildren().add(descLabel);
        }

        // 底部：指示器 + 按钮
        if (step.getFooterSlot() != null) {
            popup.getChildren().add(step.getFooterSlot());
        } else {
            HBox footer = new HBox(8);
            footer.setAlignment(Pos.CENTER_LEFT);

            // 指示器
            if (showIndicators.get()) {
                Node indicator = step.getIndicatorSlot() != null
                        ? step.getIndicatorSlot()
                        : buildDefaultIndicator(th, primary);
                footer.getChildren().add(indicator);
            }

            // 弹性占位
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            footer.getChildren().add(spacer);

            // 按钮区
            int idx = current.get();
            int total = steps.size();
            // 上一步
            if (idx > 0) {
                String prevTxt = step.getPrevText() != null ? step.getPrevText() : prevButtonText.get();
                Button prevBtn = buildSecondaryButton(prevTxt, th, primary);
                prevBtn.setOnAction(e -> prev());
                footer.getChildren().add(prevBtn);
            }
            // 下一步 / 完成
            String nextTxt = step.getNextText();
            if (nextTxt == null) {
                nextTxt = (idx == total - 1) ? finishButtonText.get() : nextButtonText.get();
            }
            Button nextBtn = buildPrimaryButton(nextTxt, th, primary);
            nextBtn.setOnAction(e -> next());
            footer.getChildren().add(nextBtn);

            popup.getChildren().add(footer);
        }
    }

    private Node buildDefaultIndicator(TourTheme th, boolean primary) {
        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER_LEFT);
        int activeIdx = current.get();
        for (int i = 0; i < steps.size(); i++) {
            Circle c = new Circle(th.getIndicatorDotSize() / 2);
            if (i == activeIdx) {
                c.setFill(primary ? Color.WHITE : th.getIndicatorActiveColor());
            } else {
                c.setFill(primary ? Color.web("#FFFFFF", 0.4) : th.getIndicatorInactiveColor());
            }
            dots.getChildren().add(c);
        }
        return dots;
    }

    private Button buildPrimaryButton(String text, TourTheme th, boolean primary) {
        Button btn = new Button(text);
        Color bg = primary ? Color.WHITE : th.getPrimaryColor();
        Color fg = primary ? th.getPrimaryColor() : Color.WHITE;
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                "-fx-background-radius: 4; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: %.0f;",
                TourTheme.toCssColor(bg),
                TourTheme.toCssColor(fg),
                th.getButtonFontSize()));
        return btn;
    }

    private Button buildSecondaryButton(String text, TourTheme th, boolean primary) {
        Button btn = new Button(text);
        Color bg = primary ? Color.web("#FFFFFF", 0.2) : th.getSecondaryButtonBg();
        Color fg = primary ? Color.WHITE : th.getSecondaryButtonText();
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                "-fx-background-radius: 4; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: %.0f;",
                TourTheme.toCssColor(bg),
                TourTheme.toCssColor(fg),
                th.getButtonFontSize()));
        return btn;
    }

    private Button buildCloseButton(Color color) {
        Button btn = new Button();
        SVGPath x = new SVGPath();
        x.setContent("M 2 2 L 12 12 M 12 2 L 2 12");
        x.setStroke(color);
        x.setStrokeWidth(1.5);
        x.setFill(Color.TRANSPARENT);
        btn.setGraphic(x);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 2 4; -fx-cursor: hand;");
        return btn;
    }

    // ==================== 定位 ====================

    private void positionPopup(TourStep step, Rectangle2D targetInHost, TourTheme th) {
        // 使用 prefWidth/Height 作为起点，限制在 [min, max] 范围，
        // 并同步 resize popup 以使 getWidth/getHeight 与计算一致
        double prefW = popup.prefWidth(-1);
        double minW = popup.getMinWidth() > 0 ? popup.getMinWidth() : prefW;
        double maxW = popup.getMaxWidth() > 0 ? popup.getMaxWidth() : prefW;
        double pwActual = clamp(prefW, minW, maxW);
        double phActual = popup.prefHeight(pwActual);
        // 同步 popup 实际尺寸（setManaged(false) 下需手动 resize）
        popup.resize(pwActual, phActual);

        double hostW = host.getWidth();
        double hostH = host.getHeight();
        double offset = th.getPopupOffset();

        TourPlacement placement = step.getPlacement();
        if (targetInHost == null || placement == TourPlacement.CENTER) {
            // 居中显示：popup 比 host 大时使用负坐标以让中心对齐，
            // 避免 Math.max(0, ...) 将 popup 拖到顶部都贝对齐。
            popup.setLayoutX((hostW - pwActual) / 2);
            popup.setLayoutY((hostH - phActual) / 2);
            arrow.setVisible(false);
            return;
        }

        // 自适应翻转：当原始 placement 方向空间不足时翻到对侧
        TourPlacement effective = flipPlacementIfNeeded(placement, targetInHost, pwActual, phActual, hostW, hostH, offset);

        double tx = targetInHost.getMinX();
        double ty = targetInHost.getMinY();
        double tw = targetInHost.getWidth();
        double th0 = targetInHost.getHeight();
        double cx = tx + tw / 2;
        double cy = ty + th0 / 2;

        double px = 0, py = 0;
        double arrowSize = th.getPopupArrowSize();

        switch (effective) {
            case TOP -> { px = cx - pwActual / 2; py = ty - phActual - offset; }
            case TOP_START -> { px = tx; py = ty - phActual - offset; }
            case TOP_END -> { px = tx + tw - pwActual; py = ty - phActual - offset; }
            case BOTTOM -> { px = cx - pwActual / 2; py = ty + th0 + offset; }
            case BOTTOM_START -> { px = tx; py = ty + th0 + offset; }
            case BOTTOM_END -> { px = tx + tw - pwActual; py = ty + th0 + offset; }
            case LEFT -> { px = tx - pwActual - offset; py = cy - phActual / 2; }
            case LEFT_START -> { px = tx - pwActual - offset; py = ty; }
            case LEFT_END -> { px = tx - pwActual - offset; py = ty + th0 - phActual; }
            case RIGHT -> { px = tx + tw + offset; py = cy - phActual / 2; }
            case RIGHT_START -> { px = tx + tw + offset; py = ty; }
            case RIGHT_END -> { px = tx + tw + offset; py = ty + th0 - phActual; }
            default -> { px = cx - pwActual / 2; py = ty + th0 + offset; }
        }

        // 轴向软约束：仅在与目标垂直的轴上允许轻微滑动避免取到 host 边界外，
        // 不在指向轴上 clamp，避免 popup 被拽离目标导致“偏移到上方”故障
        if (effective.isHorizontalAxis()) {
            // popup 在目标上/下方。水平方向可以 clamp，垂直方向不 clamp
            px = softClampHorizontal(px, pwActual, hostW);
        } else if (effective.isVerticalAxis()) {
            // popup 在目标左/右侧。垂直方向可以 clamp，水平方向不 clamp
            py = softClampVertical(py, phActual, hostH);
        }

        popup.setLayoutX(px);
        popup.setLayoutY(py);

        // 箭头位置与方向
        positionArrow(effective, arrow, px, py, pwActual, phActual, cx, cy, arrowSize);
    }

    /**
     * 当原始 placement 方向空间不足时翻转到对侧。
     * <p>例如目标贴近 host 底部时 BOTTOM 位置放不下 popup，自动翻转为 TOP。</p>
     */
    private TourPlacement flipPlacementIfNeeded(TourPlacement p, Rectangle2D target,
                                                 double pw, double ph,
                                                 double hostW, double hostH, double offset) {
        double ty = target.getMinY();
        double tBottom = target.getMaxY();
        double tx = target.getMinX();
        double tRight = target.getMaxX();

        if (p.isBottom()) {
            // 下方放不下且上方能放下
            boolean noBottomSpace = tBottom + offset + ph > hostH;
            boolean hasTopSpace = ty - offset - ph >= 0;
            if (noBottomSpace && hasTopSpace) {
                return switch (p) {
                    case BOTTOM -> TourPlacement.TOP;
                    case BOTTOM_START -> TourPlacement.TOP_START;
                    case BOTTOM_END -> TourPlacement.TOP_END;
                    default -> p;
                };
            }
        } else if (p.isTop()) {
            boolean noTopSpace = ty - offset - ph < 0;
            boolean hasBottomSpace = tBottom + offset + ph <= hostH;
            if (noTopSpace && hasBottomSpace) {
                return switch (p) {
                    case TOP -> TourPlacement.BOTTOM;
                    case TOP_START -> TourPlacement.BOTTOM_START;
                    case TOP_END -> TourPlacement.BOTTOM_END;
                    default -> p;
                };
            }
        } else if (p.isRight()) {
            boolean noRightSpace = tRight + offset + pw > hostW;
            boolean hasLeftSpace = tx - offset - pw >= 0;
            if (noRightSpace && hasLeftSpace) {
                return switch (p) {
                    case RIGHT -> TourPlacement.LEFT;
                    case RIGHT_START -> TourPlacement.LEFT_START;
                    case RIGHT_END -> TourPlacement.LEFT_END;
                    default -> p;
                };
            }
        } else if (p.isLeft()) {
            boolean noLeftSpace = tx - offset - pw < 0;
            boolean hasRightSpace = tRight + offset + pw <= hostW;
            if (noLeftSpace && hasRightSpace) {
                return switch (p) {
                    case LEFT -> TourPlacement.RIGHT;
                    case LEFT_START -> TourPlacement.RIGHT_START;
                    case LEFT_END -> TourPlacement.RIGHT_END;
                    default -> p;
                };
            }
        }
        return p;
    }

    /**
     * 软约束水平位置：仅在 popup 超出 host 边界时轻微滑回，避免完全不可见。
     * <p>popup 宽 > host 宽时保持原位置不动（贴着目标），避免 popup 被拽离目标。</p>
     */
    private double softClampHorizontal(double x, double pw, double hostW) {
        if (pw >= hostW) return x;                          // popup 过宽不拖动，保留原始与目标的相对位置
        if (x < 0) return Math.max(x, -pw + 24);            // 允许部分超出左，但保留 24px 可见
        if (x + pw > hostW) return Math.min(x, hostW - 24); // 同理右边
        return x;
    }

    /**
     * 软约束垂直位置。popup 高 > host 高时保持原位置不动。
     */
    private double softClampVertical(double y, double ph, double hostH) {
        if (ph >= hostH) return y;
        if (y < 0) return Math.max(y, -ph + 24);
        if (y + ph > hostH) return Math.min(y, hostH - 24);
        return y;
    }

    private void positionArrow(TourPlacement placement, Polygon arrow,
                               double px, double py, double pw, double ph,
                               double cx, double cy, double s) {
        double ax, ay;
        double rotate = 0;
        if (placement.isBottom()) {
            ax = clamp(cx - s, px + 8, px + pw - 8 - 2 * s);
            ay = py - s + 1;
            rotate = 180; // 三角朝上
        } else if (placement.isTop()) {
            ax = clamp(cx - s, px + 8, px + pw - 8 - 2 * s);
            ay = py + ph - 1;
            rotate = 0; // 三角朝下
        } else if (placement.isRight()) {
            ax = px - s + 1;
            ay = clamp(cy - s, py + 8, py + ph - 8 - 2 * s);
            rotate = 90; // 朝左
        } else if (placement.isLeft()) {
            ax = px + pw - 1;
            ay = clamp(cy - s, py + 8, py + ph - 8 - 2 * s);
            rotate = -90; // 朝右
        } else {
            arrow.setVisible(false);
            return;
        }
        arrow.setLayoutX(ax);
        arrow.setLayoutY(ay);
        arrow.setRotate(rotate);
    }

    // ==================== 目标 bounds 监听 ====================

    private void rebindTarget(TourStep step) {
        // 先完全解绑上一步的所有监听器
        unbindTarget();
        if (step.getTarget() == null || !step.getTarget().isNodeBased()) {
            // 非 Node 目标也需要响应窗口缩放
            bindSceneSizeListener();
            return;
        }
        Node node = step.getTarget().getNode();
        if (node == null) return;
        lastTargetNode = node;

        // 统一的 layout 触发器：使用令牌机制去重同一帧的多次触发
        final ChangeListener<javafx.geometry.Bounds> boundsTrigger = (o, ov, nv) -> scheduleLayout();
        final ChangeListener<Number> numberTrigger = (o, ov, nv) -> scheduleLayout();
        final ChangeListener<javafx.scene.transform.Transform> transformTrigger = (o, ov, nv) -> scheduleLayout();

        targetBoundsListener = boundsTrigger;
        targetBoundsInParentListener = boundsTrigger;
        targetLayoutXListener = numberTrigger;
        targetLayoutYListener = numberTrigger;
        targetSceneListener = (o, ov, nv) -> scheduleLayout();
        targetLocalToSceneListener = transformTrigger;

        node.boundsInLocalProperty().addListener(targetBoundsListener);
        node.boundsInParentProperty().addListener(targetBoundsInParentListener);
        node.layoutXProperty().addListener(targetLayoutXListener);
        node.layoutYProperty().addListener(targetLayoutYListener);
        node.sceneProperty().addListener(targetSceneListener);
        // 关键：localToSceneTransformProperty 会反映任何祖先 transform/滚动的变化
        node.localToSceneTransformProperty().addListener(targetLocalToSceneListener);

        // 监听祖先链上所有节点的 boundsInParent / layoutX / layoutY
        ancestorBoundsListener = boundsTrigger;
        Node cur = node.getParent();
        while (cur != null) {
            cur.boundsInParentProperty().addListener(ancestorBoundsListener);
            cur.layoutXProperty().addListener(targetLayoutXListener);
            cur.layoutYProperty().addListener(targetLayoutYListener);
            trackedAncestors.add(cur);

            // 关键：遇到 ScrollPane 额外监听 hvalue/vvalue（滚动不会触发 bounds）
            if (cur instanceof javafx.scene.control.ScrollPane sp) {
                sp.hvalueProperty().addListener(targetLayoutXListener);
                sp.vvalueProperty().addListener(targetLayoutYListener);
                trackedScrollPanes.add(sp);
            }

            cur = cur.getParent();
        }

        // 监听 Scene 尺寸变化（窗口缩放时需重新定位）
        bindSceneSizeListener();
    }

    /**
     * 绑定 Scene 尺寸监听器。
     */
    private void bindSceneSizeListener() {
        if (sceneSizeListener != null || currentScene == null) return;
        sceneSizeListener = (o, ov, nv) -> scheduleLayout();
        currentScene.widthProperty().addListener(sceneSizeListener);
        currentScene.heightProperty().addListener(sceneSizeListener);
    }

    /**
     * 调度一次 layout。使用令牌+runLater 合并同一帧内的多次触发。
     */
    private void scheduleLayout() {
        if (!active) return;
        final long token = ++layoutToken;
        Platform.runLater(() -> {
            if (token != layoutToken || !active) return;
            layoutOverlay();
        });
    }

    private void unbindTarget() {
        if (lastTargetNode != null) {
            try {
                if (targetBoundsListener != null) {
                    lastTargetNode.boundsInLocalProperty().removeListener(targetBoundsListener);
                }
                if (targetBoundsInParentListener != null) {
                    lastTargetNode.boundsInParentProperty().removeListener(targetBoundsInParentListener);
                }
                if (targetLayoutXListener != null) {
                    lastTargetNode.layoutXProperty().removeListener(targetLayoutXListener);
                }
                if (targetLayoutYListener != null) {
                    lastTargetNode.layoutYProperty().removeListener(targetLayoutYListener);
                }
                if (targetSceneListener != null) {
                    lastTargetNode.sceneProperty().removeListener(targetSceneListener);
                }
                if (targetLocalToSceneListener != null) {
                    lastTargetNode.localToSceneTransformProperty().removeListener(targetLocalToSceneListener);
                }
            } catch (Exception ignored) {
            }
        }
        // 解绑祖先链监听器
        if (ancestorBoundsListener != null) {
            for (Node anc : trackedAncestors) {
                try {
                    anc.boundsInParentProperty().removeListener(ancestorBoundsListener);
                    if (targetLayoutXListener != null) anc.layoutXProperty().removeListener(targetLayoutXListener);
                    if (targetLayoutYListener != null) anc.layoutYProperty().removeListener(targetLayoutYListener);
                } catch (Exception ignored) {
                }
            }
        }
        // 解绑 ScrollPane 滚动监听
        for (javafx.scene.control.ScrollPane sp : trackedScrollPanes) {
            try {
                if (targetLayoutXListener != null) sp.hvalueProperty().removeListener(targetLayoutXListener);
                if (targetLayoutYListener != null) sp.vvalueProperty().removeListener(targetLayoutYListener);
            } catch (Exception ignored) {
            }
        }
        // 解绑 Scene 尺寸监听
        if (sceneSizeListener != null && currentScene != null) {
            try {
                currentScene.widthProperty().removeListener(sceneSizeListener);
                currentScene.heightProperty().removeListener(sceneSizeListener);
            } catch (Exception ignored) {
            }
        }
        trackedAncestors.clear();
        trackedScrollPanes.clear();
        targetBoundsListener = null;
        targetBoundsInParentListener = null;
        targetLayoutXListener = null;
        targetLayoutYListener = null;
        targetSceneListener = null;
        targetLocalToSceneListener = null;
        ancestorBoundsListener = null;
        sceneSizeListener = null;
        lastTargetNode = null;
    }

    private void fireStepHide(TourStep step) {
        if (step != null && step.getOnHide() != null) {
            try {
                step.getOnHide().accept(step);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "onHide 回调异常", e);
            }
        }
    }

    // ==================== 工具方法 ====================

    private Rectangle2D sceneRectToHost(Rectangle2D sceneRect) {
        if (sceneRect == null || host == null) return null;
        // 使用 4 个角点让 sceneToLocal 处理任何 transform/旋转/缩放，
        // 避免 minXY/maxXY 两点调用在 host 存在旋转时产生负宽/高。
        javafx.geometry.Bounds sceneB = new javafx.geometry.BoundingBox(
                sceneRect.getMinX(), sceneRect.getMinY(),
                sceneRect.getWidth(), sceneRect.getHeight());
        javafx.geometry.Bounds local = host.sceneToLocal(sceneB);
        if (local == null) return null;
        return new Rectangle2D(local.getMinX(), local.getMinY(),
                Math.max(0, local.getWidth()), Math.max(0, local.getHeight()));
    }

    private static double clamp(double v, double lo, double hi) {
        if (hi < lo) return lo;
        return Math.max(lo, Math.min(hi, v));
    }

    private TourStep getCurrentStep() {
        int idx = current.get();
        return (idx >= 0 && idx < steps.size()) ? steps.get(idx) : null;
    }

    /**
     * 释放资源
     */
    public void dispose() {
        if (disposed) return;
        disposed = true;
        if (active) close();
        for (TourStep s : new ArrayList<>(steps)) {
            try { s.dispose(); } catch (Exception ignored) {}
        }
        steps.clear();
        onOpen = null;
        onClose = null;
        onFinish = null;
        onChange = null;
    }

    // ==================== Property Getters ====================

    public ObservableList<TourStep> getSteps() { return steps; }
    public IntegerProperty currentProperty() { return current; }
    public BooleanProperty maskProperty() { return mask; }
    public ObjectProperty<TourMaskConfig> maskConfigProperty() { return maskConfig; }
    public ObjectProperty<TourTheme> themeProperty() { return theme; }
    public ObjectProperty<TourType> typeProperty() { return type; }
    public BooleanProperty showCloseProperty() { return showClose; }
    public BooleanProperty showArrowProperty() { return showArrow; }
    public BooleanProperty showIndicatorsProperty() { return showIndicators; }
    public BooleanProperty closeOnEscProperty() { return closeOnEsc; }
    public StringProperty prevButtonTextProperty() { return prevButtonText; }
    public StringProperty nextButtonTextProperty() { return nextButtonText; }
    public StringProperty finishButtonTextProperty() { return finishButtonText; }

    public int getCurrent() { return current.get(); }
    public boolean isMask() { return mask.get(); }
    public TourMaskConfig getMaskConfig() { return maskConfig.get(); }
    public TourTheme getTheme() { return theme.get(); }
    public TourType getType() { return type.get(); }
    public boolean isShowClose() { return showClose.get(); }
    public boolean isShowArrow() { return showArrow.get(); }
    public boolean isShowIndicators() { return showIndicators.get(); }
    public boolean isCloseOnEsc() { return closeOnEsc.get(); }
    public boolean isActive() { return active; }
    public boolean isDisposed() { return disposed; }

    public List<TourStep> getStepList() { return new ArrayList<>(steps); }
}
