package io.aurora.fx.components.dynamicForm;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 表单分组容器
 * <p>
 * 参考 FormsFX 的 Group 概念和 Ant Design 的 Card 分组设计，
 * 将多个相关的 {@link FormItem} 组织在一起，提供可选的标题、描述文本、
 * 折叠功能和验证状态跟踪。Group 是 Form 和 Section 的中间语义层。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 基础分组
 * FormGroup group = FormGroup.of("基本信息",
 *     new FormItem("姓名", "name", nameField),
 *     new FormItem("年龄", "age", ageField)
 * );
 *
 * // 可折叠分组
 * FormGroup group = FormGroup.of("高级选项",
 *     new FormItem("超时", "timeout", spinner)
 * ).collapsible(true).collapsed(true);
 *
 * // 控制可见性
 * FormGroup group = FormGroup.of("配送信息",
 *     new FormItem("地址", "address", addressField)
 * ).visible(false); // 可动态显隐
 *
 * // 添加到表单
 * form.addGroup(group);
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormGroup {

    /** 分组标题 */
    private final StringProperty title = new SimpleStringProperty("");

    /** 分组描述 */
    private final StringProperty description = new SimpleStringProperty("");

    /** 是否支持折叠 */
    private final BooleanProperty collapsible = new SimpleBooleanProperty(false);

    /** 当前是否折叠 */
    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);

    /** 是否可见 */
    private final BooleanProperty visible = new SimpleBooleanProperty(true);

    /** 包含的表单项 */
    private final List<FormItem> items = new ArrayList<>();

    /** 生成的 UI 节点 */
    private Node node;

    // ==================== 工厂方法 ====================

    /**
     * 创建分组（FormsFX 风格的静态工厂方法）
     */
    public static FormGroup of(FormItem... formItems) {
        FormGroup group = new FormGroup();
        if (formItems != null) {
            Collections.addAll(group.items, formItems);
        }
        return group;
    }

    /**
     * 创建带标题的分组
     */
    public static FormGroup of(String title, FormItem... formItems) {
        return of(formItems).title(title);
    }

    // ==================== 链式 API ====================

    public FormGroup title(String title) { this.title.set(title); return this; }
    public FormGroup description(String desc) { this.description.set(desc); return this; }
    public FormGroup collapsible(boolean collapsible) { this.collapsible.set(collapsible); return this; }
    public FormGroup collapsed(boolean collapsed) { this.collapsed.set(collapsed); return this; }
    public FormGroup visible(boolean visible) { this.visible.set(visible); return this; }

    /** 向分组中追加表单项 */
    public FormGroup add(FormItem item) {
        if (item != null) items.add(item);
        return this;
    }

    // ==================== 验证状态 ====================

    /**
     * 检查分组内是否有验证错误
     *
     * @return true 表示有错误
     */
    public boolean hasErrors() {
        for (FormItem item : items) {
            if (item.errorProperty().get()) return true;
        }
        return false;
    }

    /**
     * 获取分组内有错误的表单项数量
     */
    public int getErrorCount() {
        int count = 0;
        for (FormItem item : items) {
            if (item.errorProperty().get()) count++;
        }
        return count;
    }

    // ==================== UI 构建 ====================

    /**
     * 构建并返回分组 UI 节点。
     */
    public Node buildNode(double spacing) {
        VBox content = new VBox(spacing);
        content.setPadding(new Insets(5, 0, 10, 0));
        for (FormItem item : items) {
            content.getChildren().add(item.getNode());
        }

        if (collapsible.get()) {
            TitledPane tp = new TitledPane();
            tp.textProperty().bind(title);
            tp.setContent(content);
            tp.setExpanded(!collapsed.get());
            tp.expandedProperty().addListener((obs, o, n) -> collapsed.set(!n));
            collapsed.addListener((obs, o, n) -> tp.setExpanded(!n));
            tp.setAnimated(true);
            tp.getStyleClass().add("form-group-collapsible");
            // 可见性绑定
            tp.visibleProperty().bind(visible);
            tp.managedProperty().bind(visible);
            node = tp;
        } else {
            VBox wrapper = new VBox(4);
            wrapper.getStyleClass().add("form-group");
            // 可见性绑定
            wrapper.visibleProperty().bind(visible);
            wrapper.managedProperty().bind(visible);

            String titleText = title.get();
            if (titleText != null && !titleText.isEmpty()) {
                Label titleLabel = new Label(titleText);
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                titleLabel.setTextFill(Color.web("#303133"));
                titleLabel.setPadding(new Insets(5, 0, 5, 0));
                title.addListener((obs, o, n) -> titleLabel.setText(n));
                wrapper.getChildren().add(titleLabel);
            }

            String descText = description.get();
            if (descText != null && !descText.isEmpty()) {
                Label descLabel = new Label(descText);
                descLabel.setTextFill(Color.web("#909399"));
                descLabel.setFont(Font.font(12));
                descLabel.setWrapText(true);
                description.addListener((obs, o, n) -> descLabel.setText(n));
                wrapper.getChildren().add(descLabel);
            }

            wrapper.getChildren().add(content);
            node = wrapper;
        }
        return node;
    }

    // ==================== Getters ====================

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public boolean isCollapsible() { return collapsible.get(); }
    public BooleanProperty collapsibleProperty() { return collapsible; }
    public boolean isCollapsed() { return collapsed.get(); }
    public BooleanProperty collapsedProperty() { return collapsed; }
    public boolean isVisible() { return visible.get(); }
    public BooleanProperty visibleProperty() { return visible; }
    public List<FormItem> getItems() { return Collections.unmodifiableList(items); }
    public Node getNode() { return node; }
}
