package io.aurora.fx.components.tour;

import javafx.beans.property.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;

/**
 * Tour 单步配置
 * <p>
 * 描述漫游引导中的一个步骤，包含目标节点、标题、描述、定位方式、
 * 局部主题/遮罩覆盖、自定义指示器节点、按钮文本以及该步骤的事件回调等。
 * </p>
 *
 * <pre>{@code
 * TourStep step = new TourStep()
 *     .target(myButton)
 *     .title("功能入口")
 *     .description("点击这里可以进入新功能")
 *     .placement(TourPlacement.BOTTOM)
 *     .nextText("下一步")
 *     .onShow(s -> System.out.println("显示步骤: " + s.getTitle()));
 * }</pre>
 *
 * @author Tour Component
 * @version 1.0
 */
public class TourStep {

    // ==================== 属性 ====================

    /** 步骤标题 */
    private final StringProperty title = new SimpleStringProperty("");
    /** 步骤描述 */
    private final StringProperty description = new SimpleStringProperty("");
    /** 弹窗相对目标的定位 */
    private final ObjectProperty<TourPlacement> placement = new SimpleObjectProperty<>(TourPlacement.BOTTOM);
    /** 步骤目标 */
    private final ObjectProperty<TourTarget> target = new SimpleObjectProperty<>(TourTarget.empty());
    /** 步骤局部主题（null 时使用 Tour 全局主题） */
    private final ObjectProperty<TourTheme> theme = new SimpleObjectProperty<>(null);
    /** 步骤局部遮罩配置（null 时使用 Tour 全局遮罩） */
    private final ObjectProperty<TourMaskConfig> maskConfig = new SimpleObjectProperty<>(null);
    /** 步骤局部弹窗类型 */
    private final ObjectProperty<TourType> type = new SimpleObjectProperty<>(null);
    /** 步骤局部 mask 开关，null 表示沿用 Tour 全局设置 */
    private final ObjectProperty<Boolean> showMask = new SimpleObjectProperty<>(null);

    /** 主按钮文本 */
    private final StringProperty nextText = new SimpleStringProperty(null);
    /** 上一步按钮文本 */
    private final StringProperty prevText = new SimpleStringProperty(null);

    /** 自定义指示器节点（覆盖默认圆点+计数） */
    private Node indicatorSlot;
    /** 自定义内容节点（覆盖 description 文本） */
    private Node contentSlot;
    /** 自定义底部按钮区节点（覆盖默认按钮区） */
    private Node footerSlot;

    /** 步骤索引（由 Tour 管理） */
    private int index;

    // ==================== 事件回调 ====================

    /** 当此步骤显示时触发 */
    private java.util.function.Consumer<TourStep> onShow;
    /** 当此步骤隐藏（切换到下一/上一步、关闭、完成）时触发 */
    private java.util.function.Consumer<TourStep> onHide;

    // ==================== 构造方法 ====================

    public TourStep() {
    }

    public TourStep(String title) {
        this.title.set(title);
    }

    public TourStep(String title, String description) {
        this.title.set(title);
        this.description.set(description);
    }

    public TourStep(Node target, String title, String description) {
        this.target.set(TourTarget.of(target));
        this.title.set(title);
        this.description.set(description);
    }

    // ==================== Builder 风格链式调用 ====================

    /**
     * 设置标题
     *
     * @param t 标题文本
     * @return this
     */
    public TourStep title(String t) {
        this.title.set(t);
        return this;
    }

    /**
     * 设置描述
     *
     * @param d 描述文本
     * @return this
     */
    public TourStep description(String d) {
        this.description.set(d);
        return this;
    }

    /**
     * 设置定位
     *
     * @param p 定位
     * @return this
     */
    public TourStep placement(TourPlacement p) {
        this.placement.set(p != null ? p : TourPlacement.BOTTOM);
        return this;
    }

    /**
     * 通过 Node 设置目标
     *
     * @param node 目标节点
     * @return this
     */
    public TourStep target(Node node) {
        this.target.set(TourTarget.of(node));
        return this;
    }

    /**
     * 通过坐标设置目标
     *
     * @param rect 场景坐标矩形
     * @return this
     */
    public TourStep target(Rectangle2D rect) {
        this.target.set(TourTarget.of(rect));
        return this;
    }

    /**
     * 直接设置目标
     *
     * @param t 目标
     * @return this
     */
    public TourStep target(TourTarget t) {
        this.target.set(t != null ? t : TourTarget.empty());
        return this;
    }

    /**
     * 设置该步骤局部主题
     *
     * @param t 主题
     * @return this
     */
    public TourStep theme(TourTheme t) {
        this.theme.set(t);
        return this;
    }

    /**
     * 设置该步骤局部遮罩
     *
     * @param m 遮罩
     * @return this
     */
    public TourStep maskConfig(TourMaskConfig m) {
        this.maskConfig.set(m);
        return this;
    }

    /**
     * 设置该步骤的弹窗类型
     *
     * @param t 类型
     * @return this
     */
    public TourStep type(TourType t) {
        this.type.set(t);
        return this;
    }

    /**
     * 单独控制此步骤是否显示遮罩
     *
     * @param show true / false / null（继承全局）
     * @return this
     */
    public TourStep showMask(Boolean show) {
        this.showMask.set(show);
        return this;
    }

    /**
     * 设置主按钮文字
     *
     * @param text 文本
     * @return this
     */
    public TourStep nextText(String text) {
        this.nextText.set(text);
        return this;
    }

    /**
     * 设置上一步按钮文字
     *
     * @param text 文本
     * @return this
     */
    public TourStep prevText(String text) {
        this.prevText.set(text);
        return this;
    }

    /**
     * 设置自定义指示器节点
     *
     * @param node 节点
     * @return this
     */
    public TourStep indicatorSlot(Node node) {
        this.indicatorSlot = node;
        return this;
    }

    /**
     * 设置自定义内容节点
     *
     * @param node 节点
     * @return this
     */
    public TourStep contentSlot(Node node) {
        this.contentSlot = node;
        return this;
    }

    /**
     * 设置自定义底部按钮区
     *
     * @param node 节点
     * @return this
     */
    public TourStep footerSlot(Node node) {
        this.footerSlot = node;
        return this;
    }

    /**
     * 设置步骤显示回调
     *
     * @param callback 回调
     * @return this
     */
    public TourStep onShow(java.util.function.Consumer<TourStep> callback) {
        this.onShow = callback;
        return this;
    }

    /**
     * 设置步骤隐藏回调
     *
     * @param callback 回调
     * @return this
     */
    public TourStep onHide(java.util.function.Consumer<TourStep> callback) {
        this.onHide = callback;
        return this;
    }

    /**
     * 释放资源
     */
    public void dispose() {
        title.set("");
        description.set("");
        target.set(TourTarget.empty());
        theme.set(null);
        maskConfig.set(null);
        type.set(null);
        showMask.set(null);
        indicatorSlot = null;
        contentSlot = null;
        footerSlot = null;
        onShow = null;
        onHide = null;
    }

    // ==================== Getters ====================

    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<TourPlacement> placementProperty() { return placement; }
    public ObjectProperty<TourTarget> targetProperty() { return target; }
    public ObjectProperty<TourTheme> themeProperty() { return theme; }
    public ObjectProperty<TourMaskConfig> maskConfigProperty() { return maskConfig; }
    public ObjectProperty<TourType> typeProperty() { return type; }
    public ObjectProperty<Boolean> showMaskProperty() { return showMask; }
    public StringProperty nextTextProperty() { return nextText; }
    public StringProperty prevTextProperty() { return prevText; }

    public String getTitle() { return title.get(); }
    public void setTitle(String t) { title.set(t); }
    public String getDescription() { return description.get(); }
    public void setDescription(String d) { description.set(d); }
    public TourPlacement getPlacement() { return placement.get(); }
    public void setPlacement(TourPlacement p) { placement.set(p); }
    public TourTarget getTarget() { return target.get(); }
    public void setTarget(TourTarget t) { target.set(t); }
    public TourTheme getTheme() { return theme.get(); }
    public void setTheme(TourTheme t) { theme.set(t); }
    public TourMaskConfig getMaskConfig() { return maskConfig.get(); }
    public void setMaskConfig(TourMaskConfig m) { maskConfig.set(m); }
    public TourType getType() { return type.get(); }
    public void setType(TourType t) { type.set(t); }
    public Boolean getShowMask() { return showMask.get(); }
    public void setShowMask(Boolean s) { showMask.set(s); }
    public String getNextText() { return nextText.get(); }
    public void setNextText(String t) { nextText.set(t); }
    public String getPrevText() { return prevText.get(); }
    public void setPrevText(String t) { prevText.set(t); }

    public Node getIndicatorSlot() { return indicatorSlot; }
    public Node getContentSlot() { return contentSlot; }
    public Node getFooterSlot() { return footerSlot; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public java.util.function.Consumer<TourStep> getOnShow() { return onShow; }
    public java.util.function.Consumer<TourStep> getOnHide() { return onHide; }
}
