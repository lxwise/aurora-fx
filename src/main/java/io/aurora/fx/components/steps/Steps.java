package io.aurora.fx.components.steps;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.*;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Steps 步骤条容器组件
 * <p>
 * 对标 Element UI 的 el-steps 组件，引导用户按照流程完成任务的分步导航条。
 * 支持水平/垂直布局、简洁模式、居中对齐、自定义主题等丰富功能。
 * </p>
 *
 * <h3>基础用法</h3>
 * <pre>{@code
 * Steps steps = new Steps()
 *     .addStep(new Step("步骤一"))
 *     .addStep(new Step("步骤二"))
 *     .addStep(new Step("步骤三"))
 *     .active(1)
 *     .finishStatus(StepStatus.SUCCESS);
 *
 * // 添加到场景
 * root.getChildren().add(steps.getNode());
 * }</pre>
 *
 * <h3>垂直步骤条</h3>
 * <pre>{@code
 * Steps steps = new Steps()
 *     .direction(Orientation.VERTICAL)
 *     .addStep(new Step("步骤一", "描述信息"))
 *     .addStep(new Step("步骤二", "描述信息"))
 *     .active(0);
 * }</pre>
 *
 * <h3>简洁风格</h3>
 * <pre>{@code
 * Steps steps = new Steps()
 *     .simple(true)
 *     .addStep(new Step("步骤一"))
 *     .addStep(new Step("步骤二"))
 *     .active(1);
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class Steps {

    private static final Logger LOGGER = Logger.getLogger(Steps.class.getName());

    // ==================== 核心属性 ====================

    /** 当前激活步骤索引（从0开始） */
    private final IntegerProperty active = new SimpleIntegerProperty(0);

    /** 布局方向 */
    private final ObjectProperty<Orientation> direction = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    /** 已完成步骤的状态（默认 FINISH） */
    private final ObjectProperty<StepStatus> finishStatus = new SimpleObjectProperty<>(StepStatus.FINISH);

    /** 当前步骤的状态（默认 PROCESS） */
    private final ObjectProperty<StepStatus> processStatus = new SimpleObjectProperty<>(StepStatus.PROCESS);

    /** 是否居中对齐 */
    private final BooleanProperty alignCenter = new SimpleBooleanProperty(false);

    /** 是否简洁模式 */
    private final BooleanProperty simple = new SimpleBooleanProperty(false);

    /** 步距（固定宽度，null 或 <= 0 表示自适应） */
    private final ObjectProperty<Number> space = new SimpleObjectProperty<>(null);

    /** 主题 */
    private final ObjectProperty<StepsTheme> theme = new SimpleObjectProperty<>(StepsTheme.DEFAULT);

    // ==================== 步骤列表 ====================

    /** 步骤集合 */
    private final ObservableList<Step> steps = FXCollections.observableArrayList();

    // ==================== UI ====================

    /** 根节点容器 */
    private final StackPane rootPane = new StackPane();

    /** 实际布局容器 */
    private Pane layoutPane;

    // ==================== 事件回调 ====================

    /** 活动步骤变化监听器 */
    private Consumer<Integer> onChangeCallback;

    /** 步骤点击回调 */
    private Consumer<Integer> onStepClickCallback;

    // ==================== 内部状态 ====================

    /** 是否正在刷新布局（防止重复刷新） */
    private volatile boolean refreshing = false;

    /** 是否已释放资源 */
    private volatile boolean disposed = false;

    /** active属性监听器引用（用于清理） */
    private ChangeListener<? super Number> activeListener;

    /** 其他属性监听器引用（用于清理） */
    private ChangeListener<? super Object> refreshListener;

    // ==================== 构造方法 ====================

    public Steps() {
        initRoot();
        bindListeners();
    }

    // ==================== 初始化 ====================

    private void initRoot() {
        rootPane.getStyleClass().add("steps-container");
        rootPane.setPadding(new Insets(10));
    }

    /**
     * 绑定所有属性监听器，属性变化时自动刷新布局
     */
    private void bindListeners() {
        // active 变化监听器
        activeListener = (obs, oldVal, newVal) -> {
            if (disposed) return;
            int newActive = newVal != null ? newVal.intValue() : 0;
            // 边界检查：空列表时只允许设置为0
            if (steps.isEmpty()) {
                if (newActive != 0) {
                    Platform.runLater(() -> active.set(0));
                }
                return;
            }
            // 边界检查：索引不能为负
            if (newActive < 0) {
                Platform.runLater(() -> active.set(0));
                return;
            }
            // 边界检查：索引不能超出范围
            int maxIndex = steps.size() - 1;
            if (newActive > maxIndex) {
                final int correctedIndex = maxIndex;
                Platform.runLater(() -> active.set(correctedIndex));
                return;
            }
            scheduleRefresh();
            fireChangeEvent(newActive);
        };
        active.addListener(activeListener);

        // 其他属性变化监听器（统一处理）
        refreshListener = (obs, oldVal, newVal) -> {
            if (!disposed) {
                scheduleRefresh();
            }
        };

        direction.addListener(refreshListener);
        finishStatus.addListener(refreshListener);
        processStatus.addListener(refreshListener);
        alignCenter.addListener(refreshListener);
        simple.addListener(refreshListener);
        space.addListener(refreshListener);
        theme.addListener(refreshListener);

        // 步骤列表变化
        steps.addListener((ListChangeListener<Step>) change -> {
            if (disposed) return;
            reindexSteps();
            // 如果当前active超出新范围，自动调整
            int currentActive = active.get();
            int maxIndex = Math.max(0, steps.size() - 1);
            if (currentActive > maxIndex) {
                active.set(maxIndex);
            }
            // 为新添加的步骤绑定状态监听器
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Step step : change.getAddedSubList()) {
                        bindStepStatusListener(step);
                    }
                }
            }
            scheduleRefresh();
        });
    }

    /**
     * 绑定步骤状态监听器，当步骤状态变化时自动刷新布局
     */
    private void bindStepStatusListener(Step step) {
        if (step == null) return;
        step.statusProperty().addListener((obs, oldVal, newVal) -> {
            if (!disposed) {
                scheduleRefresh();
            }
        });
    }

    /**
     * 在 JavaFX 应用线程中安排一次布局刷新
     */
    private void scheduleRefresh() {
        if (refreshing) return;
        refreshing = true;
        Platform.runLater(() -> {
            try {
                rebuildLayout();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "刷新布局失败", e);
            } finally {
                refreshing = false;
            }
        });
    }

    /**
     * 重新索引步骤
     */
    private void reindexSteps() {
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setIndex(i);
        }
    }

    /**
     * 触发 change 事件（确保在 JavaFX 应用线程中执行）
     */
    private void fireChangeEvent(int newActive) {
        if (onChangeCallback != null) {
            if (Platform.isFxApplicationThread()) {
                try {
                    onChangeCallback.accept(newActive);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "onChange 回调执行异常", e);
                }
            } else {
                Platform.runLater(() -> {
                    try {
                        onChangeCallback.accept(newActive);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "onChange 回调执行异常", e);
                    }
                });
            }
        }
    }

    // ==================== 布局构建 ====================

    /**
     * 重建整个步骤条布局
     */
    private void rebuildLayout() {
        rootPane.getChildren().clear();

        if (steps.isEmpty()) {
            return;
        }

        boolean isSimple = simple.get();
        Orientation dir = isSimple ? Orientation.HORIZONTAL : direction.get();
        boolean center = !isSimple && alignCenter.get();
        Number spaceVal = isSimple ? null : space.get();

        if (dir == Orientation.VERTICAL) {
            layoutPane = buildVerticalLayout(center, spaceVal);
        } else if (isSimple) {
            layoutPane = buildSimpleLayout();
        } else {
            layoutPane = buildHorizontalLayout(center, spaceVal);
        }

        rootPane.getChildren().add(layoutPane);
    }

    /**
     * 构建水平布局
     */
    private Pane buildHorizontalLayout(boolean center, Number spaceVal) {
        HBox hbox = new HBox();
        hbox.setAlignment(center ? Pos.TOP_CENTER : Pos.TOP_LEFT);
        hbox.setFillHeight(false);

        int activeIdx = active.get();

        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            final int stepIndex = i;
            StepStatus effectiveStatus = computeEffectiveStatus(step, i, activeIdx);
            boolean isLast = (i == steps.size() - 1);

            Region node = step.buildHorizontal(effectiveStatus, theme.get(), isLast, center, spaceVal);
            
            // 添加点击事件
            node.setStyle(node.getStyle() + "; -fx-cursor: hand;");
            node.setOnMouseClicked(event -> handleStepClick(stepIndex));
            
            hbox.getChildren().add(node);

            if (spaceVal == null || spaceVal.doubleValue() <= 0) {
                HBox.setHgrow(node, isLast ? Priority.NEVER : Priority.ALWAYS);
            }
        }

        return hbox;
    }

    /**
     * 构建垂直布局
     */
    private Pane buildVerticalLayout(boolean center, Number spaceVal) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_LEFT);

        int activeIdx = active.get();

        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            final int stepIndex = i;
            StepStatus effectiveStatus = computeEffectiveStatus(step, i, activeIdx);
            boolean isLast = (i == steps.size() - 1);

            Region node = step.buildVertical(effectiveStatus, theme.get(), isLast);
            
            // 添加点击事件
            node.setStyle(node.getStyle() + "; -fx-cursor: hand;");
            node.setOnMouseClicked(event -> handleStepClick(stepIndex));
            
            vbox.getChildren().add(node);

            if (spaceVal != null && spaceVal.doubleValue() > 0) {
                node.setPrefHeight(spaceVal.doubleValue());
            } else if (!isLast) {
                VBox.setVgrow(node, Priority.ALWAYS);
            }
        }

        return vbox;
    }

    /**
     * 构建简洁模式布局
     */
    private Pane buildSimpleLayout() {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(5, 15, 5, 15));
        hbox.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 4; " +
                "-fx-border-color: %s; -fx-border-radius: 4;",
                StepsTheme.toCssColor(theme.get().getBackgroundColor()),
                StepsTheme.toCssColor(theme.get().getLineColor())));

        int activeIdx = active.get();

        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            final int stepIndex = i;
            StepStatus effectiveStatus = computeEffectiveStatus(step, i, activeIdx);
            boolean isLast = (i == steps.size() - 1);

            Region node = step.buildSimple(effectiveStatus, theme.get(), isLast, i + 1, steps.size());
            
            // 添加点击事件
            node.setStyle(node.getStyle() + "; -fx-cursor: hand;");
            node.setOnMouseClicked(event -> handleStepClick(stepIndex));
            
            hbox.getChildren().add(node);
        }

        return hbox;
    }

    /**
     * 处理步骤点击事件
     */
    private void handleStepClick(int stepIndex) {
        if (disposed) {
            return;
        }
        if (onStepClickCallback != null) {
            try {
                onStepClickCallback.accept(stepIndex);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "onStepClick 回调执行异常", e);
            }
        }
    }

    // ==================== 状态计算 ====================

    /**
     * 计算步骤的有效状态
     * <p>优先使用步骤自身设置的 status，否则根据 active 索引和 finishStatus/processStatus 自动计算。</p>
     *
     * @param step      步骤对象
     * @param stepIndex 步骤索引
     * @param activeIdx 当前活动步骤索引
     * @return 有效的步骤状态
     */
    private StepStatus computeEffectiveStatus(Step step, int stepIndex, int activeIdx) {
        // 如果步骤自身设置了 status，优先使用
        StepStatus manualStatus = step.getStatus();
        if (manualStatus != null) {
            return manualStatus;
        }

        // 自动计算
        if (stepIndex < activeIdx) {
            return finishStatus.get();
        } else if (stepIndex == activeIdx) {
            return processStatus.get();
        } else {
            return StepStatus.WAIT;
        }
    }

    // ==================== Builder风格链式API ====================

    /**
     * 设置当前激活步骤索引
     *
     * @param index 索引（从0开始）
     * @return this
     */
    public Steps active(int index) {
        this.active.set(index);
        return this;
    }

    /**
     * 设置布局方向
     *
     * @param orientation 方向（HORIZONTAL / VERTICAL）
     * @return this
     */
    public Steps direction(Orientation orientation) {
        this.direction.set(orientation);
        return this;
    }

    /**
     * 设置已完成步骤的显示状态
     *
     * @param status 完成状态
     * @return this
     */
    public Steps finishStatus(StepStatus status) {
        this.finishStatus.set(status);
        return this;
    }

    /**
     * 设置当前步骤的显示状态
     *
     * @param status 进行中状态
     * @return this
     */
    public Steps processStatus(StepStatus status) {
        this.processStatus.set(status);
        return this;
    }

    /**
     * 设置是否居中对齐
     *
     * @param center 是否居中
     * @return this
     */
    public Steps alignCenter(boolean center) {
        this.alignCenter.set(center);
        return this;
    }

    /**
     * 设置是否启用简洁模式
     * <p>
     * 简洁模式下，以下属性将失效：alignCenter、description、direction、space。
     * 组件会自动忽略这些设置，但建议开发者了解此行为。
     * </p>
     *
     * @param simple 是否简洁模式
     * @return this
     */
    public Steps simple(boolean simple) {
        if (simple && !this.simple.get()) {
            // 进入简洁模式时发出提示日志
            LOGGER.fine("启用简洁模式：alignCenter/description/direction/space 属性将被忽略");
        }
        this.simple.set(simple);
        return this;
    }

    /**
     * 设置固定步距
     *
     * @param space 步距（px），null或<=0为自适应
     * @return this
     */
    public Steps space(Number space) {
        this.space.set(space);
        return this;
    }

    /**
     * 设置主题
     *
     * @param theme 主题配置
     * @return this
     */
    public Steps theme(StepsTheme theme) {
        this.theme.set(theme != null ? theme : StepsTheme.DEFAULT);
        return this;
    }

    /**
     * 添加一个步骤
     *
     * @param step 步骤对象
     * @return this
     */
    public Steps addStep(Step step) {
        if (step != null) {
            step.setIndex(steps.size());
            bindStepStatusListener(step);
            steps.add(step);
        }
        return this;
    }

    /**
     * 批量添加步骤
     *
     * @param stepsToAdd 步骤数组
     * @return this
     */
    public Steps addSteps(Step... stepsToAdd) {
        if (stepsToAdd != null) {
            for (Step s : stepsToAdd) {
                addStep(s);
            }
        }
        return this;
    }

    /**
     * 设置步骤变化监听
     *
     * @param callback 回调函数，参数为新的 active 索引
     * @return this
     */
    public Steps onChange(Consumer<Integer> callback) {
        this.onChangeCallback = callback;
        return this;
    }

    /**
     * 设置步骤点击监听
     *
     * @param callback 回调函数，参数为被点击的步骤索引
     * @return this
     */
    public Steps onStepClick(Consumer<Integer> callback) {
        this.onStepClickCallback = callback;
        return this;
    }

    // ==================== 公开操作方法 ====================

    /**
     * 前进一步
     *
     * @return 是否成功前进（已在最后一步时返回 false）
     */
    public boolean next() {
        if (disposed) {
            LOGGER.warning("Steps 组件已释放，无法执行 next()");
            return false;
        }
        int current = active.get();
        if (current < steps.size() - 1) {
            active.set(current + 1);
            return true;
        }
        return false;
    }

    /**
     * 后退一步
     *
     * @return 是否成功后退（已在第一步时返回 false）
     */
    public boolean prev() {
        if (disposed) {
            LOGGER.warning("Steps 组件已释放，无法执行 prev()");
            return false;
        }
        int current = active.get();
        if (current > 0) {
            active.set(current - 1);
            return true;
        }
        return false;
    }

    /**
     * 获取总步骤数
     *
     * @return 步骤总数
     */
    public int getTotalSteps() {
        return steps.size();
    }

    /**
     * 是否为第一步
     *
     * @return 当前是否在第一步
     */
    public boolean isFirst() {
        return active.get() == 0;
    }

    /**
     * 是否为最后一步
     *
     * @return 当前是否在最后一步
     */
    public boolean isLast() {
        return active.get() == steps.size() - 1;
    }

    /**
     * 跳转到第一步
     */
    public void goToFirst() {
        setActive(0);
    }

    /**
     * 跳转到最后一步
     */
    public void goToLast() {
        if (!steps.isEmpty()) {
            setActive(steps.size() - 1);
        }
    }

    /**
     * 获取根节点（用于添加到场景图）
     *
     * @return 步骤条的根节点
     */
    public StackPane getNode() {
        // 确保首次构建
        if (rootPane.getChildren().isEmpty() && !steps.isEmpty()) {
            rebuildLayout();
        }
        return rootPane;
    }

    /**
     * 移除指定索引的步骤
     *
     * @param index 步骤索引
     */
    public void removeStep(int index) {
        if (index >= 0 && index < steps.size()) {
            steps.remove(index);
        }
    }

    /**
     * 清除所有步骤
     */
    public void clearSteps() {
        steps.clear();
    }

    /**
     * 手动刷新布局
     */
    public void refresh() {
        scheduleRefresh();
    }

    /**
     * 释放资源
     * <p>
     * 移除所有属性监听器，清理步骤列表，防止内存泄漏。
     * 调用此方法后，组件将不再可用。
     * </p>
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;

        // 移除属性监听器
        try {
            if (activeListener != null) {
                active.removeListener(activeListener);
                activeListener = null;
            }
            if (refreshListener != null) {
                direction.removeListener(refreshListener);
                finishStatus.removeListener(refreshListener);
                processStatus.removeListener(refreshListener);
                alignCenter.removeListener(refreshListener);
                simple.removeListener(refreshListener);
                space.removeListener(refreshListener);
                theme.removeListener(refreshListener);
                refreshListener = null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "移除监听器时发生异常", e);
        }

        // 清理步骤和UI
        for (Step step : steps) {
            try {
                step.dispose();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "清理步骤时发生异常", e);
            }
        }
        steps.clear();
        rootPane.getChildren().clear();
        onChangeCallback = null;
        onStepClickCallback = null;
        layoutPane = null;

        LOGGER.fine("Steps 组件资源已释放");
    }

    /**
     * 检查组件是否已释放
     *
     * @return 是否已释放
     */
    public boolean isDisposed() {
        return disposed;
    }

    // ==================== Property Getters ====================

    public IntegerProperty activeProperty() { return active; }
    public ObjectProperty<Orientation> directionProperty() { return direction; }
    public ObjectProperty<StepStatus> finishStatusProperty() { return finishStatus; }
    public ObjectProperty<StepStatus> processStatusProperty() { return processStatus; }
    public BooleanProperty alignCenterProperty() { return alignCenter; }
    public BooleanProperty simpleProperty() { return simple; }
    public ObjectProperty<Number> spaceProperty() { return space; }
    public ObjectProperty<StepsTheme> themeProperty() { return theme; }
    public ObservableList<Step> getSteps() { return steps; }

    public int getActive() { return active.get(); }
    public void setActive(int idx) { active.set(idx); }
    public Orientation getDirection() { return direction.get(); }
    public void setDirection(Orientation d) { direction.set(d); }
    public StepStatus getFinishStatus() { return finishStatus.get(); }
    public void setFinishStatus(StepStatus s) { finishStatus.set(s); }
    public StepStatus getProcessStatus() { return processStatus.get(); }
    public void setProcessStatus(StepStatus s) { processStatus.set(s); }
    public boolean isAlignCenter() { return alignCenter.get(); }
    public void setAlignCenter(boolean c) { alignCenter.set(c); }
    public boolean isSimple() { return simple.get(); }
    public void setSimple(boolean s) { simple.set(s); }
    public Number getSpace() { return space.get(); }
    public void setSpace(Number s) { space.set(s); }
    public StepsTheme getTheme() { return theme.get(); }
    public void setTheme(StepsTheme t) { theme.set(t); }
}
