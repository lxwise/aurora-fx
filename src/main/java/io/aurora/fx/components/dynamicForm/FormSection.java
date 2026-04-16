package io.aurora.fx.components.dynamicForm;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 表单区域（Section）
 * <p>
 * 参考 FormsFX 的 Section 概念，在表单中创建带标题分隔线的视觉区域。
 * Section 包含一个或多个 {@link FormGroup}，用于组织复杂表单的布局层次。
 * 支持可见性控制和验证摘要。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * FormSection section = FormSection.of("个人信息",
 *     FormGroup.of("基本信息",
 *         new FormItem("姓名", "name", nameField),
 *         new FormItem("年龄", "age", ageField)
 *     ),
 *     FormGroup.of("联系方式",
 *         new FormItem("邮箱", "email", emailField),
 *         new FormItem("电话", "phone", phoneField)
 *     )
 * );
 *
 * form.addSection(section);
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormSection {

    /** 区域标题 */
    private final StringProperty title = new SimpleStringProperty("");

    /** 区域描述 */
    private final StringProperty description = new SimpleStringProperty("");

    /** 是否可见 */
    private final BooleanProperty visible = new SimpleBooleanProperty(true);

    /** 包含的分组 */
    private final List<FormGroup> groups = new ArrayList<>();

    /** 生成的 UI 节点 */
    private Node node;

    // ==================== 工厂方法 ====================

    public static FormSection of(FormGroup... formGroups) {
        FormSection section = new FormSection();
        if (formGroups != null) {
            Collections.addAll(section.groups, formGroups);
        }
        return section;
    }

    public static FormSection of(String title, FormGroup... formGroups) {
        return of(formGroups).title(title);
    }

    // ==================== 链式 API ====================

    public FormSection title(String title) { this.title.set(title); return this; }
    public FormSection description(String desc) { this.description.set(desc); return this; }
    public FormSection visible(boolean visible) { this.visible.set(visible); return this; }

    /** 向区域中追加分组 */
    public FormSection add(FormGroup group) {
        if (group != null) groups.add(group);
        return this;
    }

    // ==================== 验证摘要 ====================

    /**
     * 检查区域内是否有验证错误
     */
    public boolean hasErrors() {
        for (FormGroup group : groups) {
            if (group.hasErrors()) return true;
        }
        return false;
    }

    /**
     * 获取区域内的错误总数
     */
    public int getErrorCount() {
        int count = 0;
        for (FormGroup group : groups) {
            count += group.getErrorCount();
        }
        return count;
    }

    // ==================== UI 构建 ====================

    public Node buildNode(double groupSpacing, double itemSpacing) {
        VBox wrapper = new VBox(groupSpacing);
        wrapper.getStyleClass().add("form-section");
        wrapper.setPadding(new Insets(5, 0, 10, 0));
        // 可见性绑定
        wrapper.visibleProperty().bind(visible);
        wrapper.managedProperty().bind(visible);

        String titleText = title.get();
        if (titleText != null && !titleText.isEmpty()) {
            Label titleLabel = new Label(titleText);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            titleLabel.setTextFill(Color.web("#303133"));
            titleLabel.setPadding(new Insets(0, 0, 2, 0));
            title.addListener((obs, o, n) -> titleLabel.setText(n));

            Separator separator = new Separator();
            separator.setPadding(new Insets(0, 0, 5, 0));

            wrapper.getChildren().addAll(titleLabel, separator);
        }

        String descText = description.get();
        if (descText != null && !descText.isEmpty()) {
            Label descLabel = new Label(descText);
            descLabel.setTextFill(Color.web("#909399"));
            descLabel.setFont(Font.font(12));
            descLabel.setWrapText(true);
            descLabel.setPadding(new Insets(0, 0, 8, 0));
            description.addListener((obs, o, n) -> descLabel.setText(n));
            wrapper.getChildren().add(descLabel);
        }

        for (FormGroup group : groups) {
            wrapper.getChildren().add(group.buildNode(itemSpacing));
        }

        node = wrapper;
        return node;
    }

    // ==================== Getters ====================

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public boolean isVisible() { return visible.get(); }
    public BooleanProperty visibleProperty() { return visible; }
    public List<FormGroup> getGroups() { return Collections.unmodifiableList(groups); }
    public Node getNode() { return node; }

    /**
     * 获取此 Section 下所有 Group 中的全部 FormItem
     */
    public List<FormItem> getAllItems() {
        List<FormItem> all = new ArrayList<>();
        for (FormGroup group : groups) {
            all.addAll(group.getItems());
        }
        return Collections.unmodifiableList(all);
    }
}
