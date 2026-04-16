package io.aurora.fx.components.dynamicForm;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 表单状态管理器
 * <p>
 * 参考 FormsFX 的 persist/reset 机制以及 TornadoFX 的 ViewModel 理念，
 * 为 {@link FormModel} 提供脏数据检测、快照、撤销/重做、表单比较、自动保存等高级功能。
 * </p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li><b>脏数据检测</b> - 追踪哪些字段被修改，提供 {@link #isDirty()} / {@link #getDirtyFields()}</li>
 *   <li><b>持久化/回滚</b> - {@link #persist()} 提交当前值，{@link #rollback()} 恢复到上次持久化状态</li>
 *   <li><b>撤销/重做</b> - 基于快照栈的 {@link #undo()} / {@link #redo()} 操作</li>
 *   <li><b>表单比较</b> - {@link #diff(FormModel)} 比较两个模型的差异</li>
 *   <li><b>自动保存</b> - {@link #enableAutoSave(long, Consumer)} 定时自动保存</li>
 *   <li><b>变更历史</b> - 获取完整的操作历史记录</li>
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * FormModel model = new FormModel().field("name", "").field("age", 0);
 * FormStateManager state = new FormStateManager(model);
 *
 * model.setFieldValue("name", "Alice");
 * state.isDirty();           // true
 * state.getDirtyFields();    // ["name"]
 *
 * state.persist();           // 提交
 * state.isDirty();           // false
 *
 * model.setFieldValue("name", "Bob");
 * state.rollback();          // 恢复到 "Alice"
 *
 * // 自动保存
 * state.enableAutoSave(5000, data -> saveToServer(data));
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormStateManager {

    private static final Logger LOGGER = Logger.getLogger(FormStateManager.class.getName());

    /** 最大撤销栈深度 */
    private static final int DEFAULT_MAX_UNDO_DEPTH = 50;

    /** 绑定的数据模型 */
    private final FormModel model;

    /** 持久化值快照（上次 persist 时的值） */
    private Map<String, Object> persistedValues = new LinkedHashMap<>();

    /** 撤销栈 */
    private final Deque<Map<String, Object>> undoStack = new ArrayDeque<>();

    /** 重做栈 */
    private final Deque<Map<String, Object>> redoStack = new ArrayDeque<>();

    /** 脏状态属性（可绑定到 UI） */
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    /** 是否可以撤销 */
    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);

    /** 是否可以重做 */
    private final BooleanProperty canRedo = new SimpleBooleanProperty(false);

    /** 最大撤销栈深度（可配置） */
    private int maxUndoDepth = DEFAULT_MAX_UNDO_DEPTH;

    /** 自动保存调度器 */
    private ScheduledExecutorService autoSaveScheduler;

    /** 自动保存任务 */
    private ScheduledFuture<?> autoSaveTask;

    /** 持久化监听器列表 */
    private final List<Consumer<Map<String, Object>>> persistListeners = new ArrayList<>();

    /** 变更历史记录 */
    private final List<HistoryEntry> changeHistory = new ArrayList<>();

    /** 最大历史记录数 */
    private static final int MAX_HISTORY_SIZE = 200;

    // ==================== 构造方法 ====================

    /**
     * 创建状态管理器并绑定到模型
     *
     * @param model 表单数据模型
     */
    public FormStateManager(FormModel model) {
        this.model = Objects.requireNonNull(model, "FormModel 不能为空");
        this.persistedValues = snapshotModel();
        installFieldListeners();
    }

    /**
     * 创建状态管理器（自定义撤销栈深度）
     *
     * @param model        表单数据模型
     * @param maxUndoDepth 最大撤销栈深度
     */
    public FormStateManager(FormModel model, int maxUndoDepth) {
        this(model);
        this.maxUndoDepth = Math.max(1, maxUndoDepth);
    }

    // ==================== 脏数据检测 ====================

    /**
     * 检查表单是否有脏数据（任意字段与持久化值不同）
     */
    public boolean isDirty() {
        return dirty.get();
    }

    /** 脏状态属性（可绑定到保存按钮的 disable 等） */
    public ReadOnlyBooleanProperty dirtyProperty() {
        return dirty;
    }

    /**
     * 获取所有脏字段名
     *
     * @return 已修改的字段名集合
     */
    public Set<String> getDirtyFields() {
        Set<String> dirtyFields = new LinkedHashSet<>();
        for (String field : model.getFieldNames()) {
            Object current = model.getFieldValue(field);
            Object persisted = persistedValues.get(field);
            if (!Objects.equals(current, persisted)) {
                dirtyFields.add(field);
            }
        }
        return dirtyFields;
    }

    /**
     * 检查指定字段是否脏
     *
     * @param fieldName 字段名
     * @return true 表示该字段已被修改
     */
    public boolean isFieldDirty(String fieldName) {
        Object current = model.getFieldValue(fieldName);
        Object persisted = persistedValues.get(fieldName);
        return !Objects.equals(current, persisted);
    }

    /**
     * 获取脏字段数量
     *
     * @return 已修改的字段数
     */
    public int getDirtyFieldCount() {
        return getDirtyFields().size();
    }

    // ==================== 持久化/回滚 ====================

    /**
     * 持久化当前值 — 将当前模型值作为新的持久化基准
     */
    public void persist() {
        persistedValues = snapshotModel();
        updateDirtyState();
        addHistoryEntry("persist", null);
        // 通知持久化监听器
        Map<String, Object> data = new LinkedHashMap<>(persistedValues);
        for (Consumer<Map<String, Object>> listener : persistListeners) {
            try {
                listener.accept(data);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "持久化监听器异常", e);
            }
        }
        LOGGER.fine("表单状态已持久化");
    }

    /**
     * 回滚到上次持久化的值
     */
    public void rollback() {
        pushUndoSnapshot();
        restoreSnapshot(persistedValues);
        updateDirtyState();
        addHistoryEntry("rollback", null);
        LOGGER.fine("表单已回滚到持久化状态");
    }

    /**
     * 添加持久化完成监听器
     *
     * @param listener 监听器（参数为持久化后的数据快照）
     */
    public void onPersist(Consumer<Map<String, Object>> listener) {
        if (listener != null) {
            persistListeners.add(listener);
        }
    }

    // ==================== 撤销/重做 ====================

    /**
     * 保存当前状态快照到撤销栈（在重要操作前手动调用）
     */
    public void pushUndoSnapshot() {
        undoStack.push(snapshotModel());
        // 限制撤销栈深度，移除最早的快照
        while (undoStack.size() > maxUndoDepth) {
            undoStack.removeLast();
        }
        redoStack.clear();
        updateUndoRedoState();
    }

    /**
     * 撤销：恢复到上一个快照
     *
     * @return true 表示撤销成功
     */
    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        redoStack.push(snapshotModel());
        Map<String, Object> snapshot = undoStack.pop();
        restoreSnapshot(snapshot);
        updateUndoRedoState();
        updateDirtyState();
        addHistoryEntry("undo", null);
        return true;
    }

    /**
     * 重做：恢复到撤销前的状态
     *
     * @return true 表示重做成功
     */
    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        undoStack.push(snapshotModel());
        Map<String, Object> snapshot = redoStack.pop();
        restoreSnapshot(snapshot);
        updateUndoRedoState();
        updateDirtyState();
        addHistoryEntry("redo", null);
        return true;
    }

    public boolean canUndo() { return canUndo.get(); }
    public ReadOnlyBooleanProperty canUndoProperty() { return canUndo; }
    public boolean canRedo() { return canRedo.get(); }
    public ReadOnlyBooleanProperty canRedoProperty() { return canRedo; }

    /**
     * 获取撤销栈深度
     */
    public int getUndoStackSize() { return undoStack.size(); }

    /**
     * 获取重做栈深度
     */
    public int getRedoStackSize() { return redoStack.size(); }

    // ==================== 自动保存 ====================

    /**
     * 启用自动保存
     * <p>
     * 当表单有脏数据时，每隔指定毫秒自动调用 persist() 并通知监听器。
     * </p>
     *
     * @param intervalMs 自动保存间隔（毫秒）
     * @param onSave     保存回调（参数为当前数据快照）
     */
    public void enableAutoSave(long intervalMs, Consumer<Map<String, Object>> onSave) {
        disableAutoSave();
        if (onSave != null) {
            persistListeners.add(onSave);
        }
        autoSaveScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "form-auto-save");
            t.setDaemon(true);
            return t;
        });
        autoSaveTask = autoSaveScheduler.scheduleAtFixedRate(() -> {
            if (isDirty()) {
                javafx.application.Platform.runLater(this::persist);
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        LOGGER.fine("自动保存已启用，间隔: " + intervalMs + "ms");
    }

    /**
     * 禁用自动保存
     */
    public void disableAutoSave() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel(false);
            autoSaveTask = null;
        }
        if (autoSaveScheduler != null) {
            autoSaveScheduler.shutdown();
            autoSaveScheduler = null;
        }
        LOGGER.fine("自动保存已禁用");
    }

    // ==================== 表单比较 ====================

    /**
     * 比较当前模型与另一个模型的差异
     *
     * @param other 另一个模型
     * @return 差异映射：字段名 -> [当前值, 对方值]
     */
    public Map<String, Object[]> diff(FormModel other) {
        Map<String, Object[]> differences = new LinkedHashMap<>();
        if (other == null) return differences;

        Set<String> allFields = new LinkedHashSet<>(model.getFieldNames());
        allFields.addAll(other.getFieldNames());

        for (String field : allFields) {
            Object myVal = model.getFieldValue(field);
            Object otherVal = other.getFieldValue(field);
            if (!Objects.equals(myVal, otherVal)) {
                differences.put(field, new Object[]{myVal, otherVal});
            }
        }
        return differences;
    }

    /**
     * 比较当前值与持久化值的差异
     *
     * @return 差异映射：字段名 -> [当前值, 持久化值]
     */
    public Map<String, Object[]> diffWithPersisted() {
        Map<String, Object[]> differences = new LinkedHashMap<>();
        for (String field : model.getFieldNames()) {
            Object current = model.getFieldValue(field);
            Object persisted = persistedValues.get(field);
            if (!Objects.equals(current, persisted)) {
                differences.put(field, new Object[]{current, persisted});
            }
        }
        return differences;
    }

    // ==================== 变更历史 ====================

    /**
     * 获取变更历史记录
     *
     * @return 不可修改的历史记录列表
     */
    public List<HistoryEntry> getChangeHistory() {
        return Collections.unmodifiableList(changeHistory);
    }

    /**
     * 获取最近的 N 条历史记录
     *
     * @param count 数量
     * @return 历史记录列表
     */
    public List<HistoryEntry> getRecentHistory(int count) {
        int size = changeHistory.size();
        int from = Math.max(0, size - count);
        return Collections.unmodifiableList(changeHistory.subList(from, size));
    }

    /**
     * 清除历史记录
     */
    public void clearChangeHistory() {
        changeHistory.clear();
    }

    /**
     * 变更历史条目
     */
    public static class HistoryEntry {
        private final long timestamp;
        private final String action;
        private final String fieldName;

        HistoryEntry(String action, String fieldName) {
            this.timestamp = System.currentTimeMillis();
            this.action = action;
            this.fieldName = fieldName;
        }

        public long getTimestamp() { return timestamp; }
        public String getAction() { return action; }
        public String getFieldName() { return fieldName; }

        @Override
        public String toString() {
            return String.format("[%tT] %s%s", new Date(timestamp), action,
                    fieldName != null ? " (" + fieldName + ")" : "");
        }
    }

    private void addHistoryEntry(String action, String fieldName) {
        changeHistory.add(new HistoryEntry(action, fieldName));
        if (changeHistory.size() > MAX_HISTORY_SIZE) {
            changeHistory.remove(0);
        }
    }

    // ==================== 内部方法 ====================

    /** 对模型当前值做快照 */
    private Map<String, Object> snapshotModel() {
        return new LinkedHashMap<>(model.toMap());
    }

    /** 从快照恢复模型值 */
    private void restoreSnapshot(Map<String, Object> snapshot) {
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            model.setFieldValue(entry.getKey(), entry.getValue());
        }
    }

    /** 更新脏状态属性 */
    private void updateDirtyState() {
        dirty.set(!getDirtyFields().isEmpty());
    }

    /** 更新撤销/重做状态 */
    private void updateUndoRedoState() {
        canUndo.set(!undoStack.isEmpty());
        canRedo.set(!redoStack.isEmpty());
    }

    /** 为模型所有字段安装变更监听器 */
    private void installFieldListeners() {
        for (String field : model.getFieldNames()) {
            model.fieldProperty(field).addListener((obs, oldV, newV) -> {
                updateDirtyState();
                addHistoryEntry("fieldChange", field);
            });
        }
    }

    /**
     * 清除所有状态（撤销栈、重做栈），重新以当前值作为持久化基准
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        persistedValues = snapshotModel();
        updateDirtyState();
        updateUndoRedoState();
        changeHistory.clear();
    }

    /**
     * 释放资源（停止自动保存等）
     */
    public void dispose() {
        disableAutoSave();
        persistListeners.clear();
        changeHistory.clear();
        undoStack.clear();
        redoStack.clear();
        persistedValues.clear();
    }
}
