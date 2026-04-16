package io.aurora.fx.components.dynamicForm;

import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 表单项容器组件
 * <p>
 * 对标 Element UI 的 el-form-item 和 Ant Design 的 Form.Item，
 * 作为表单输入项的容器，包含标签区域、输入控件区域、描述文本区域和验证错误消息区域。
 * 支持自动绑定常见 JavaFX 控件到 FormModel、标签位置控制、尺寸继承、
 * 可见性控制、描述文本等功能。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 基础用法
 * FormItem item = new FormItem("活动名称", "name", new TextField());
 *
 * // 链式配置
 * FormItem item = new FormItem("活动区域", "region")
 *     .content(comboBox)
 *     .required(true)
 *     .description("请选择活动所在的区域")
 *     .labelPosition(FormLabelPosition.TOP);
 *
 * // 可见性控制
 * FormItem item = new FormItem("详细地址", "address", new TextField())
 *     .visible(false); // 动态隐藏/显示
 *
 * // 添加到表单
 * form.addItem(item);
 * }</pre>
 *
 * @author Form Component
 * @version 2.0
 * @since 1.0
 */
public class FormItem {

    private static final Logger LOGGER = Logger.getLogger(FormItem.class.getName());

    /** 错误状态 CSS 样式类名 */
    private static final String ERROR_STYLE_CLASS = "form-item-has-error";

    // ==================== 属性 ====================

    /** 标签文本 */
    private final StringProperty label = new SimpleStringProperty("");

    /** 模型字段名（用于验证和数据绑定） */
    private final StringProperty prop = new SimpleStringProperty();

    /** 标签位置（null 则继承 Form 设置） */
    private final ObjectProperty<FormLabelPosition> labelPosition = new SimpleObjectProperty<>();

    /** 标签宽度（null 则继承 Form 设置） */
    private final ObjectProperty<Double> labelWidth = new SimpleObjectProperty<>();

    /** 是否必填（显示红色星号） */
    private final BooleanProperty required = new SimpleBooleanProperty(false);

    /** 是否显示错误消息 */
    private final BooleanProperty showMessage = new SimpleBooleanProperty(true);

    /** 当前是否处于错误状态 */
    private final BooleanProperty error = new SimpleBooleanProperty(false);

    /** 当前错误消息 */
    private final StringProperty errorMessage = new SimpleStringProperty();

    /** 尺寸（null 则继承 Form 设置） */
    private final ObjectProperty<FormSize> size = new SimpleObjectProperty<>();

    /** 验证规则（FormItem 级别，与 Form 级规则合并使用） */
    private List<FormValidationRule> rules;

    /** 提示文本（鼠标悬停时显示） */
    private final StringProperty tooltip = new SimpleStringProperty();

    /** 占位文本（自动应用到支持 promptText 的控件） */
    private final StringProperty placeholder = new SimpleStringProperty();

    /** 是否可编辑（可单独控制此 FormItem 的禁用状态） */
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    /** 栅格跨列数（用于响应式布局，1-24，默认 24 表示占满一行） */
    private final IntegerProperty span = new SimpleIntegerProperty(24);

    // ==================== v2.0 新增属性 ====================

    /** 描述文本（显示在控件下方，错误消息上方） */
    private final StringProperty description = new SimpleStringProperty();

    /** 是否可见（支持动态显隐） */
    private final BooleanProperty visible = new SimpleBooleanProperty(true);

    /** 验证状态 */
    private final ObjectProperty<FormValidationResult.FieldStatus> validationStatus =
            new SimpleObjectProperty<>(FormValidationResult.FieldStatus.PENDING);

    /** 自定义字段类型标识（用于 FormFieldFactory 插件） */
    private final StringProperty fieldType = new SimpleStringProperty();

    // ==================== UI 节点 ====================

    private final VBox root = new VBox();
    private HBox horizontalRow;
    private final Label labelNode = new Label();
    private final Label asterisk = new Label("*");
    private final HBox labelContainer = new HBox(2);
    private final StackPane contentWrapper = new StackPane();
    private final Label errorLabel = new Label();
    private final Label descriptionLabel = new Label();
    private Node content;

    /** 父表单引用 */
    private Form parentForm;

    /**
     * 绑定监听器注册表：每个元素为 {ObservableValue, ChangeListener, type}。
     * dispose 时统一移除，防止内存泄漏。
     */
    private final List<Object[]> bindingListeners = new ArrayList<>();

    /** 缓存的淡入动画（复用，避免每次 showError 都新建） */
    private FadeTransition errorFadeTransition;

    /** 是否已释放 */
    private volatile boolean disposed = false;

    // ==================== 构造方法 ====================

    public FormItem() {
        initUI();
        bindProperties();
    }

    public FormItem(String label, String prop) {
        this();
        this.label.set(label);
        this.prop.set(prop);
    }

    public FormItem(String label, String prop, Node content) {
        this(label, prop);
        setContent(content);
    }

    // ==================== 初始化 ====================

    private void initUI() {
        root.getStyleClass().add("form-item");
        root.setSpacing(4);

        // 必填星号
        asterisk.setTextFill(Color.web("#F56C6C"));
        asterisk.setFont(Font.font(14));
        asterisk.setVisible(false);
        asterisk.setManaged(false);

        // 标签
        labelNode.setTextFill(Color.web("#606266"));
        labelNode.setFont(Font.font(14));

        // 标签容器
        labelContainer.setAlignment(Pos.CENTER_LEFT);
        labelContainer.getChildren().addAll(asterisk, labelNode);
        labelContainer.setPadding(new Insets(0, 12, 0, 0));

        // 内容区域
        contentWrapper.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(contentWrapper, Priority.ALWAYS);

        // 描述文本
        descriptionLabel.setTextFill(Color.web("#909399"));
        descriptionLabel.setFont(Font.font(12));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setVisible(false);
        descriptionLabel.setManaged(false);
        descriptionLabel.setPadding(new Insets(2, 0, 0, 0));
        descriptionLabel.getStyleClass().add("form-item-description");

        // 错误消息
        errorLabel.setTextFill(Color.web("#F56C6C"));
        errorLabel.setFont(Font.font(12));
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setPadding(new Insets(2, 0, 0, 0));
        errorLabel.getStyleClass().add("form-item-error");

        // 默认水平布局
        buildLayout(FormLabelPosition.RIGHT);
    }

    private void bindProperties() {
        // 标签文本变更
        ChangeListener<String> labelTextListener = (obs, oldVal, newVal) -> updateLabelText();
        label.addListener(labelTextListener);
        trackListener(label, labelTextListener);
        updateLabelText();

        // 必填星号
        ChangeListener<Boolean> requiredListener = (obs, oldVal, newVal) -> {
            asterisk.setVisible(newVal);
            asterisk.setManaged(newVal);
        };
        required.addListener(requiredListener);
        trackListener(required, requiredListener);

        // 错误状态驱动 UI
        ChangeListener<Boolean> errorListener = (obs, oldVal, newVal) -> {
            if (newVal && showMessage.get()) {
                showError(errorMessage.get());
                validationStatus.set(FormValidationResult.FieldStatus.ERROR);
            } else {
                hideError();
                if (!newVal) {
                    validationStatus.set(FormValidationResult.FieldStatus.SUCCESS);
                }
            }
        };
        error.addListener(errorListener);
        trackListener(error, errorListener);

        // 错误消息变更
        ChangeListener<String> errorMsgListener = (obs, oldVal, newVal) -> {
            if (error.get() && showMessage.get()) {
                showError(newVal);
            }
        };
        errorMessage.addListener(errorMsgListener);
        trackListener(errorMessage, errorMsgListener);

        // 标签位置变更
        ChangeListener<FormLabelPosition> labelPosListener = (obs, oldVal, newVal) -> {
            FormLabelPosition pos = newVal != null ? newVal : getEffectiveLabelPosition();
            buildLayout(pos);
        };
        labelPosition.addListener(labelPosListener);
        trackListener(labelPosition, labelPosListener);

        // tooltip 变更
        ChangeListener<String> tooltipListener = (obs, oldVal, newVal) -> applyTooltip(newVal);
        tooltip.addListener(tooltipListener);
        trackListener(tooltip, tooltipListener);

        // placeholder 变更
        ChangeListener<String> placeholderListener = (obs, oldVal, newVal) -> applyPlaceholder(newVal);
        placeholder.addListener(placeholderListener);
        trackListener(placeholder, placeholderListener);

        // editable 变更
        ChangeListener<Boolean> editableListener = (obs, oldVal, newVal) -> {
            if (content != null) content.setDisable(!newVal);
        };
        editable.addListener(editableListener);
        trackListener(editable, editableListener);

        // 描述文本变更
        ChangeListener<String> descListener = (obs, oldVal, newVal) -> {
            boolean hasDesc = newVal != null && !newVal.isEmpty();
            descriptionLabel.setText(hasDesc ? newVal : "");
            descriptionLabel.setVisible(hasDesc);
            descriptionLabel.setManaged(hasDesc);
        };
        description.addListener(descListener);
        trackListener(description, descListener);

        // 可见性变更
        ChangeListener<Boolean> visibleListener = (obs, oldVal, newVal) -> {
            root.setVisible(newVal);
            root.setManaged(newVal);
        };
        visible.addListener(visibleListener);
        trackListener(visible, visibleListener);
    }

    /** 更新标签显示文本（附加 labelSuffix） */
    void updateLabelText() {
        String text = label.get() != null ? label.get() : "";
        String suffix = (parentForm != null) ? parentForm.getLabelSuffix() : "";
        if (suffix != null && !suffix.isEmpty() && !text.isEmpty()) {
            labelNode.setText(text + suffix);
        } else {
            labelNode.setText(text);
        }
    }

    // ==================== 布局构建 ====================

    /**
     * 根据标签位置重建内部布局
     */
    void buildLayout(FormLabelPosition position) {
        root.getChildren().clear();

        if (position == FormLabelPosition.TOP) {
            labelContainer.setAlignment(Pos.CENTER_LEFT);
            labelContainer.setMinWidth(Region.USE_COMPUTED_SIZE);
            labelContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
            labelContainer.setMaxWidth(Double.MAX_VALUE);
            root.getChildren().addAll(labelContainer, contentWrapper, descriptionLabel, errorLabel);
        } else {
            horizontalRow = new HBox();
            horizontalRow.setAlignment(Pos.CENTER_LEFT);

            if (position == FormLabelPosition.RIGHT) {
                labelContainer.setAlignment(Pos.CENTER_RIGHT);
            } else {
                labelContainer.setAlignment(Pos.CENTER_LEFT);
            }

            double lw = getEffectiveLabelWidth();
            labelContainer.setMinWidth(lw);
            labelContainer.setPrefWidth(lw);
            labelContainer.setMaxWidth(lw);

            horizontalRow.getChildren().addAll(labelContainer, contentWrapper);
            root.getChildren().addAll(horizontalRow, descriptionLabel, errorLabel);
        }
    }

    // ==================== 错误显示/隐藏 ====================

    void showError(String message) {
        if (message == null || message.isEmpty()) {
            hideError();
            return;
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        if (content != null && !content.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            content.getStyleClass().add(ERROR_STYLE_CLASS);
            // 先清除旧的错误样式再添加，避免多次累加
            String style = content.getStyle();
            if (style == null) style = "";
            style = style.replaceAll(";?\\s*-fx-border-color:\\s*#F56C6C;?", "")
                         .replaceAll(";?\\s*-fx-border-radius:\\s*4;?", "")
                         .trim();
            content.setStyle(style + ";-fx-border-color: #F56C6C; -fx-border-radius: 4;");
        }

        if (errorFadeTransition == null) {
            errorFadeTransition = new FadeTransition(Duration.millis(200), errorLabel);
            errorFadeTransition.setFromValue(0);
            errorFadeTransition.setToValue(1);
        } else {
            errorFadeTransition.stop();
        }
        errorFadeTransition.playFromStart();
    }

    void hideError() {
        if (errorFadeTransition != null) {
            errorFadeTransition.stop();
        }
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");

        if (content != null && content.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            content.getStyleClass().remove(ERROR_STYLE_CLASS);
            String style = content.getStyle();
            if (style != null) {
                style = style.replaceAll(";?-fx-border-color:\\s*#F56C6C;?", "")
                             .replaceAll(";?-fx-border-radius:\\s*4;?", "")
                             .trim();
                content.setStyle(style);
            }
        }
    }

    // ==================== 内容控件管理 ====================

    /**
     * 设置内容控件。若之前已有绑定，先清理旧绑定再建立新绑定。
     */
    public void setContent(Node node) {
        if (this.content != null) {
            clearControlBindings();
        }
        this.content = node;
        contentWrapper.getChildren().clear();
        if (node != null) {
            contentWrapper.getChildren().add(node);
            autoBindControl(node);
            applyTooltip(tooltip.get());
            applyPlaceholder(placeholder.get());
            if (!editable.get()) node.setDisable(true);
        }
    }

    /**
     * 清理当前控件的所有绑定监听器
     */
    @SuppressWarnings("unchecked")
    private void clearControlBindings() {
        List<Object[]> toRemove = new ArrayList<>(bindingListeners);
        for (Object[] binding : toRemove) {
            try {
                if (binding.length >= 3 && "control".equals(binding[2])) {
                    ((ObservableValue<Object>) binding[0]).removeListener((ChangeListener<Object>) binding[1]);
                    bindingListeners.remove(binding);
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "移除控件绑定异常", e);
            }
        }
    }

    /**
     * 自动检测并绑定常见 JavaFX 控件到 FormModel
     */
    @SuppressWarnings("unchecked")
    private void autoBindControl(Node node) {
        if (prop.get() == null || parentForm == null || parentForm.getModel() == null) {
            return;
        }
        String fieldName = prop.get();
        FormModel model = parentForm.getModel();

        try {
            if (node instanceof PasswordField) {
                bindTextInput((TextInputControl) node, fieldName, model);
            } else if (node instanceof TextArea) {
                bindTextInput((TextInputControl) node, fieldName, model);
            } else if (node instanceof TextField) {
                bindTextInput((TextInputControl) node, fieldName, model);
            } else if (node instanceof ComboBox) {
                bindComboBox((ComboBox<Object>) node, fieldName, model);
            } else if (node instanceof ChoiceBox) {
                bindChoiceBox((ChoiceBox<Object>) node, fieldName, model);
            } else if (node instanceof CheckBox) {
                bindCheckBox((CheckBox) node, fieldName, model);
            } else if (node instanceof DatePicker) {
                bindDatePicker((DatePicker) node, fieldName, model);
            } else if (node instanceof Spinner) {
                bindSpinner((Spinner<?>) node, fieldName, model);
            } else if (node instanceof ToggleButton) {
                bindToggleButton((ToggleButton) node, fieldName, model);
            } else if (node instanceof Slider) {
                bindSlider((Slider) node, fieldName, model);
            } else if (node instanceof ColorPicker) {
                bindColorPicker((ColorPicker) node, fieldName, model);
            } else if (node instanceof ListView) {
                bindListView((ListView<Object>) node, fieldName, model);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "自动绑定控件失败: " + fieldName, e);
        }
    }

    private void bindTextInput(TextInputControl field, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val != null) field.setText(String.valueOf(val));

        ChangeListener<String> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        field.textProperty().addListener(controlListener);
        trackControlListener(field.textProperty(), controlListener);

        ChangeListener<Object> modelListener = (obs, oldV, newV) -> {
            String text = newV != null ? String.valueOf(newV) : "";
            if (!text.equals(field.getText())) {
                field.setText(text);
            }
        };
        model.fieldProperty(fieldName).addListener(modelListener);
        trackControlListener(model.fieldProperty(fieldName), modelListener);

        ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
            if (!newV) triggerValidation("blur");
        };
        field.focusedProperty().addListener(focusListener);
        trackControlListener(field.focusedProperty(), focusListener);
    }

    @SuppressWarnings("unchecked")
    private void bindComboBox(ComboBox<Object> box, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val != null) box.setValue(val);

        ChangeListener<Object> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        box.valueProperty().addListener(controlListener);
        trackControlListener(box.valueProperty(), controlListener);

        ChangeListener<Object> modelListener = (obs, oldV, newV) -> {
            if (newV != box.getValue()) box.setValue(newV);
        };
        model.fieldProperty(fieldName).addListener(modelListener);
        trackControlListener(model.fieldProperty(fieldName), modelListener);

        ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
            if (!newV) triggerValidation("blur");
        };
        box.focusedProperty().addListener(focusListener);
        trackControlListener(box.focusedProperty(), focusListener);
    }

    @SuppressWarnings("unchecked")
    private void bindChoiceBox(ChoiceBox<Object> box, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val != null) box.setValue(val);

        ChangeListener<Object> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        box.valueProperty().addListener(controlListener);
        trackControlListener(box.valueProperty(), controlListener);

        ChangeListener<Object> modelListener = (obs, oldV, newV) -> {
            if (!Objects.equals(newV, box.getValue())) box.setValue(newV);
        };
        model.fieldProperty(fieldName).addListener(modelListener);
        trackControlListener(model.fieldProperty(fieldName), modelListener);
    }

    private void bindCheckBox(CheckBox box, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val instanceof Boolean) box.setSelected((Boolean) val);

        ChangeListener<Boolean> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        box.selectedProperty().addListener(controlListener);
        trackControlListener(box.selectedProperty(), controlListener);

        ChangeListener<Object> modelListener = (obs, oldV, newV) -> {
            if (newV instanceof Boolean && (Boolean) newV != box.isSelected()) {
                box.setSelected((Boolean) newV);
            }
        };
        model.fieldProperty(fieldName).addListener(modelListener);
        trackControlListener(model.fieldProperty(fieldName), modelListener);
    }

    private void bindToggleButton(ToggleButton btn, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val instanceof Boolean) btn.setSelected((Boolean) val);

        ChangeListener<Boolean> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        btn.selectedProperty().addListener(controlListener);
        trackControlListener(btn.selectedProperty(), controlListener);
    }

    private void bindDatePicker(DatePicker picker, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val instanceof LocalDate) picker.setValue((LocalDate) val);

        ChangeListener<LocalDate> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        picker.valueProperty().addListener(controlListener);
        trackControlListener(picker.valueProperty(), controlListener);

        ChangeListener<Boolean> focusListener = (obs, oldV, newV) -> {
            if (!newV) triggerValidation("blur");
        };
        picker.focusedProperty().addListener(focusListener);
        trackControlListener(picker.focusedProperty(), focusListener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void bindSpinner(Spinner<?> spinner, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val instanceof Number && spinner.getValueFactory() != null) {
            try {
                ((Spinner) spinner).getValueFactory().setValue(val);
            } catch (Exception ignored) {}
        }

        ChangeListener controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        spinner.valueProperty().addListener(controlListener);
        trackControlListener(spinner.valueProperty(), controlListener);
    }

    private void bindSlider(Slider slider, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val instanceof Number) {
            slider.setValue(((Number) val).doubleValue());
        }

        ChangeListener<Number> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV.doubleValue());
            triggerValidation("change");
        };
        slider.valueProperty().addListener(controlListener);
        trackControlListener(slider.valueProperty(), controlListener);

        ChangeListener<Object> modelListener = (obs, oldV, newV) -> {
            if (newV instanceof Number) {
                double v = ((Number) newV).doubleValue();
                if (Math.abs(v - slider.getValue()) > 0.001) {
                    slider.setValue(v);
                }
            }
        };
        model.fieldProperty(fieldName).addListener(modelListener);
        trackControlListener(model.fieldProperty(fieldName), modelListener);
    }

    private void bindColorPicker(ColorPicker picker, String fieldName, FormModel model) {
        Object val = model.getFieldValue(fieldName);
        if (val instanceof Color) {
            picker.setValue((Color) val);
        } else if (val instanceof String) {
            try {
                picker.setValue(Color.web((String) val));
            } catch (Exception ignored) {}
        }

        ChangeListener<Color> controlListener = (obs, oldV, newV) -> {
            model.setFieldValue(fieldName, newV);
            triggerValidation("change");
        };
        picker.valueProperty().addListener(controlListener);
        trackControlListener(picker.valueProperty(), controlListener);

        ChangeListener<Object> modelListener = (obs, oldV, newV) -> {
            if (newV instanceof Color && !Objects.equals(newV, picker.getValue())) {
                picker.setValue((Color) newV);
            }
        };
        model.fieldProperty(fieldName).addListener(modelListener);
        trackControlListener(model.fieldProperty(fieldName), modelListener);
    }

    /**
     * 绑定 ListView 多选到模型字段
     */
    @SuppressWarnings("unchecked")
    private void bindListView(ListView<Object> listView, String fieldName, FormModel model) {
        if (listView.getSelectionModel().getSelectionMode() == SelectionMode.MULTIPLE) {
            Object val = model.getFieldValue(fieldName);
            if (val instanceof List) {
                for (Object item : (List<Object>) val) {
                    int idx = listView.getItems().indexOf(item);
                    if (idx >= 0) listView.getSelectionModel().select(idx);
                }
            }

            listView.getSelectionModel().getSelectedItems().addListener(
                    (javafx.collections.ListChangeListener<Object>) change -> {
                        List<Object> selected = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
                        Object currentVal = model.getFieldValue(fieldName);
                        if (currentVal instanceof ObservableList) {
                            ((ObservableList<Object>) currentVal).setAll(selected);
                        } else {
                            model.setFieldValue(fieldName, FXCollections.observableArrayList(selected));
                        }
                        triggerValidation("change");
                    });
        } else {
            Object val = model.getFieldValue(fieldName);
            if (val != null) {
                listView.getSelectionModel().select(val);
            }

            ChangeListener<Object> controlListener = (obs, oldV, newV) -> {
                model.setFieldValue(fieldName, newV);
                triggerValidation("change");
            };
            listView.getSelectionModel().selectedItemProperty().addListener(controlListener);
            trackControlListener(listView.getSelectionModel().selectedItemProperty(), controlListener);
        }
    }

    // ==================== 手动绑定方法 ====================

    /**
     * 手动绑定 RadioGroup 到模型字段
     */
    public void bindRadioGroup(ToggleGroup group, String fieldName) {
        if (parentForm == null || parentForm.getModel() == null) return;
        FormModel model = parentForm.getModel();

        Object val = model.getFieldValue(fieldName);
        if (val != null) {
            for (Toggle toggle : group.getToggles()) {
                if (toggle instanceof RadioButton) {
                    Object toggleVal = toggle.getUserData() != null
                            ? toggle.getUserData() : ((RadioButton) toggle).getText();
                    if (val.equals(toggleVal)) {
                        group.selectToggle(toggle);
                        break;
                    }
                }
            }
        }

        ChangeListener<Toggle> toggleListener = (obs, oldV, newV) -> {
            if (newV instanceof RadioButton) {
                Object data = newV.getUserData() != null
                        ? newV.getUserData() : ((RadioButton) newV).getText();
                model.setFieldValue(fieldName, data);
                triggerValidation("change");
            }
        };
        group.selectedToggleProperty().addListener(toggleListener);
        trackControlListener(group.selectedToggleProperty(), toggleListener);
    }

    /**
     * 手动绑定 CheckBox 组到模型字段（多选列表）
     */
    @SuppressWarnings("unchecked")
    public void bindCheckBoxGroup(List<CheckBox> checkBoxes, String fieldName) {
        if (parentForm == null || parentForm.getModel() == null) return;
        FormModel model = parentForm.getModel();

        Object val = model.getFieldValue(fieldName);
        if (val instanceof List) {
            List<String> selected = (List<String>) val;
            for (CheckBox cb : checkBoxes) {
                cb.setSelected(selected.contains(cb.getText()));
            }
        }

        for (CheckBox cb : checkBoxes) {
            ChangeListener<Boolean> cbListener = (obs, oldV, newV) -> {
                List<String> selected = new ArrayList<>();
                for (CheckBox box : checkBoxes) {
                    if (box.isSelected()) selected.add(box.getText());
                }
                Object currentVal = model.getFieldValue(fieldName);
                if (currentVal instanceof ObservableList) {
                    ((ObservableList<String>) currentVal).setAll(selected);
                } else {
                    model.setFieldValue(fieldName, FXCollections.observableArrayList(selected));
                }
                triggerValidation("change");
            };
            cb.selectedProperty().addListener(cbListener);
            trackControlListener(cb.selectedProperty(), cbListener);
        }
    }

    // ==================== 监听器跟踪 ====================

    @SuppressWarnings("unchecked")
    private <T> void trackListener(ObservableValue<T> observable, ChangeListener<? super T> listener) {
        bindingListeners.add(new Object[]{observable, listener, "property"});
    }

    @SuppressWarnings("unchecked")
    private <T> void trackControlListener(ObservableValue<T> observable, ChangeListener<? super T> listener) {
        bindingListeners.add(new Object[]{observable, listener, "control"});
    }

    // ==================== 验证触发 ====================

    void triggerValidation(String trigger) {
        if (disposed) return;
        if (parentForm != null && prop.get() != null) {
            validationStatus.set(FormValidationResult.FieldStatus.VALIDATING);
            parentForm.validateField(prop.get(), trigger);
        }
    }

    // ==================== 样式应用 ====================

    void applySize(FormSize formSize) {
        if (formSize == null) formSize = FormSize.DEFAULT;
        labelNode.setFont(Font.font(formSize.getFontSize()));
        errorLabel.setFont(Font.font(Math.max(formSize.getFontSize() - 2, 10)));
        descriptionLabel.setFont(Font.font(Math.max(formSize.getFontSize() - 2, 10)));
        applySizeToNode(content, formSize);
    }

    private void applySizeToNode(Node node, FormSize formSize) {
        if (node == null) return;

        if (node instanceof TextInputControl) {
            TextInputControl tic = (TextInputControl) node;
            tic.setFont(Font.font(formSize.getFontSize()));
            tic.setPrefHeight(formSize.getControlHeight());
            tic.setMinHeight(formSize.getControlHeight());
        } else if (node instanceof ComboBoxBase) {
            Region region = (Region) node;
            region.setPrefHeight(formSize.getControlHeight());
            region.setMinHeight(formSize.getControlHeight());
        } else if (node instanceof Labeled) {
            ((Labeled) node).setFont(Font.font(formSize.getFontSize()));
        }

        if (node instanceof Pane) {
            for (Node child : ((Pane) node).getChildren()) {
                applySizeToNode(child, formSize);
            }
        }
    }

    void applyTheme(FormTheme theme) {
        if (theme == null) return;
        labelNode.setTextFill(theme.getLabelColor());
        errorLabel.setTextFill(theme.getErrorColor());
        asterisk.setTextFill(theme.getErrorColor());
        if (theme.getInfoColor() != null) {
            descriptionLabel.setTextFill(theme.getInfoColor());
        }
    }

    void applyDisabled(boolean disabled) {
        applyDisabledToNode(content, disabled);
    }

    private void applyDisabledToNode(Node node, boolean disabled) {
        if (node == null) return;
        node.setDisable(disabled);
    }

    // ==================== 辅助方法 ====================

    private void applyTooltip(String text) {
        if (content == null) return;
        if (text != null && !text.isEmpty()) {
            Tooltip tp = new Tooltip(text);
            if (content instanceof Control) {
                ((Control) content).setTooltip(tp);
            } else {
                Tooltip.install(content, tp);
            }
        } else {
            if (content instanceof Control) {
                ((Control) content).setTooltip(null);
            }
            // Tooltip.uninstall 需要传入之前安装的 tooltip 实例，
            // 由于未跟踪安装的 tooltip 引用，此处不调用 uninstall 避免异常
        }
    }

    private void applyPlaceholder(String text) {
        if (content == null) return;
        if (content instanceof TextInputControl) {
            ((TextInputControl) content).setPromptText(text);
        } else if (content instanceof ComboBoxBase) {
            ((ComboBoxBase<?>) content).setPromptText(text);
        }
    }

    FormLabelPosition getEffectiveLabelPosition() {
        if (labelPosition.get() != null) return labelPosition.get();
        if (parentForm != null) return parentForm.getLabelPosition();
        return FormLabelPosition.RIGHT;
    }

    double getEffectiveLabelWidth() {
        if (labelWidth.get() != null) return labelWidth.get();
        if (parentForm != null) return parentForm.getLabelWidth();
        return 80;
    }

    FormSize getEffectiveSize() {
        if (size.get() != null) return size.get();
        if (parentForm != null) return parentForm.getSize();
        return FormSize.DEFAULT;
    }

    // ==================== 链式 API ====================

    public FormItem label(String label) { this.label.set(label); return this; }
    public FormItem prop(String prop) { this.prop.set(prop); return this; }
    public FormItem labelPosition(FormLabelPosition pos) { this.labelPosition.set(pos); return this; }
    public FormItem labelWidth(double width) { this.labelWidth.set(width); return this; }
    public FormItem required(boolean required) { this.required.set(required); return this; }
    public FormItem size(FormSize size) { this.size.set(size); return this; }
    public FormItem showMessage(boolean show) { this.showMessage.set(show); return this; }
    public FormItem content(Node content) { setContent(content); return this; }
    public FormItem tooltip(String tooltip) { this.tooltip.set(tooltip); return this; }
    public FormItem placeholder(String placeholder) { this.placeholder.set(placeholder); return this; }
    public FormItem editable(boolean editable) { this.editable.set(editable); return this; }
    public FormItem span(int span) { this.span.set(span); return this; }
    public FormItem description(String desc) { this.description.set(desc); return this; }
    public FormItem visible(boolean visible) { this.visible.set(visible); return this; }
    public FormItem fieldType(String type) { this.fieldType.set(type); return this; }

    public FormItem rules(List<FormValidationRule> rules) {
        this.rules = rules;
        boolean hasRequired = false;
        if (rules != null) {
            for (FormValidationRule rule : rules) {
                if (rule.isRequired()) {
                    hasRequired = true;
                    break;
                }
            }
        }
        required.set(hasRequired);
        return this;
    }

    // ==================== Getters ====================

    public Node getNode() { return root; }
    public String getLabel() { return label.get(); }
    public StringProperty labelProperty() { return label; }
    public String getProp() { return prop.get(); }
    public StringProperty propProperty() { return prop; }
    public ObjectProperty<FormLabelPosition> labelPositionProperty() { return labelPosition; }
    public BooleanProperty requiredProperty() { return required; }
    public BooleanProperty errorProperty() { return error; }
    public StringProperty errorMessageProperty() { return errorMessage; }
    public BooleanProperty showMessageProperty() { return showMessage; }
    public ObjectProperty<FormSize> sizeProperty() { return size; }
    public List<FormValidationRule> getRules() { return rules; }
    public Node getContent() { return content; }
    public String getTooltip() { return tooltip.get(); }
    public StringProperty tooltipProperty() { return tooltip; }
    public String getPlaceholder() { return placeholder.get(); }
    public StringProperty placeholderProperty() { return placeholder; }
    public boolean isEditable() { return editable.get(); }
    public BooleanProperty editableProperty() { return editable; }
    public int getSpan() { return span.get(); }
    public IntegerProperty spanProperty() { return span; }
    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public boolean isVisible() { return visible.get(); }
    public BooleanProperty visibleProperty() { return visible; }
    public FormValidationResult.FieldStatus getValidationStatus() { return validationStatus.get(); }
    public ObjectProperty<FormValidationResult.FieldStatus> validationStatusProperty() { return validationStatus; }
    public String getFieldType() { return fieldType.get(); }
    public StringProperty fieldTypeProperty() { return fieldType; }

    void setParentForm(Form form) {
        this.parentForm = form;
        if (content != null) autoBindControl(content);
        applySize(getEffectiveSize());
        applyTheme(form != null ? form.getTheme() : FormTheme.DEFAULT);
        if (form != null) applyDisabled(form.isDisabled());
        buildLayout(getEffectiveLabelPosition());
        updateLabelText();
    }

    // ==================== 资源释放 ====================

    /**
     * 释放所有资源：移除全部监听器、清空节点、断开引用。
     */
    @SuppressWarnings("unchecked")
    public void dispose() {
        if (disposed) return;
        disposed = true;

        if (errorFadeTransition != null) {
            errorFadeTransition.stop();
            errorFadeTransition = null;
        }

        for (Object[] binding : bindingListeners) {
            try {
                ((ObservableValue<Object>) binding[0]).removeListener((ChangeListener<Object>) binding[1]);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "移除监听器异常", e);
            }
        }
        bindingListeners.clear();

        root.getChildren().clear();
        contentWrapper.getChildren().clear();
        content = null;
        parentForm = null;
    }
}
