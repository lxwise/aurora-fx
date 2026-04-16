package io.aurora.fx.components.steps;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Step 单个步骤组件
 * <p>
 * 对标 Element UI 的 el-step 组件，表示步骤条中的一个步骤。
 * 支持标题、描述、图标、状态等属性配置，并提供自定义插槽机制。
 * </p>
 *
 * <pre>{@code
 * Step step = new Step()
 *     .title("步骤一")
 *     .description("这是第一步的描述")
 *     .status(StepStatus.PROCESS);
 * }</pre>
 *
 * @author Steps Component
 * @version 1.0
 */
public class Step {

    private static final Logger LOGGER = Logger.getLogger(Step.class.getName());

    // ==================== 属性 ====================

    /** 步骤标题 */
    private final StringProperty title = new SimpleStringProperty("");
    /** 步骤描述 */
    private final StringProperty description = new SimpleStringProperty("");
    /** 步骤状态（手动设置时覆盖自动计算） */
    private final ObjectProperty<StepStatus> status = new SimpleObjectProperty<>(null);
    /** 自定义图标节点 */
    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(null);
    /** 步骤索引（由 Steps 容器自动设置） */
    private int index = 0;

    // ==================== 自定义插槽 ====================

    /** 标题插槽 - 自定义标题节点 */
    private Node titleSlot;
    /** 描述插槽 - 自定义描述节点 */
    private Node descriptionSlot;
    /** 图标插槽 - 自定义图标节点 */
    private Node iconSlot;

    // ==================== 构造方法 ====================

    public Step() {
    }

    public Step(String title) {
        this.title.set(title);
    }

    public Step(String title, String description) {
        this.title.set(title);
        this.description.set(description);
    }

    // ==================== Builder风格链式调用 ====================

    public Step title(String title) {
        this.title.set(title);
        return this;
    }

    public Step description(String description) {
        this.description.set(description);
        return this;
    }

    public Step status(StepStatus status) {
        this.status.set(status);
        return this;
    }

    public Step icon(Node icon) {
        this.icon.set(icon);
        return this;
    }

    public Step titleSlot(Node node) {
        this.titleSlot = node;
        return this;
    }

    public Step descriptionSlot(Node node) {
        this.descriptionSlot = node;
        return this;
    }

    public Step iconSlot(Node node) {
        this.iconSlot = node;
        return this;
    }

    /**
     * 清除所有自定义插槽
     */
    public void clearSlots() {
        this.titleSlot = null;
        this.descriptionSlot = null;
        this.iconSlot = null;
    }

    /**
     * 释放资源并清除所有引用
     * <p>
     * 清除标题、描述、图标、插槽等所有引用，防止内存泄漏。
     * 调用此方法后，步骤对象应不再使用。
     * </p>
     */
    public void dispose() {
        // 清除属性值
        title.set("");
        description.set("");
        status.set(null);
        icon.set(null);

        // 清除插槽
        clearSlots();

        // 重置索引
        index = 0;

        LOGGER.fine("Step 资源已释放");
    }

    // ==================== UI 构建方法 ====================

    /**
     * 构建水平模式下的步骤节点
     *
     * @param effectiveStatus 最终有效状态（由 Steps 容器计算）
     * @param theme           主题配置
     * @param isLast          是否为最后一个步骤
     * @param alignCenter     是否居中对齐
     * @param space           固定步距（null 或 <= 0 表示自适应）
     * @return 水平布局的步骤节点
     */
    public Region buildHorizontal(StepStatus effectiveStatus, StepsTheme theme,
                                  boolean isLast, boolean alignCenter, Number space) {
        try {
            HBox container = new HBox();
            container.setAlignment(Pos.TOP_LEFT);
            container.getStyleClass().add("step-item");

            // 步骤头部（图标 + 连接线）
            HBox headBox = new HBox();
            headBox.setAlignment(Pos.CENTER);

            Node iconNode = buildIconNode(effectiveStatus, theme);
            headBox.getChildren().add(iconNode);

            // 连接线（非最后一步添加）
            if (!isLast) {
                Region lineRegion = buildHorizontalLine(effectiveStatus, theme);
                HBox.setHgrow(lineRegion, Priority.ALWAYS);
                headBox.getChildren().add(lineRegion);
            }

            // 文本区域
            VBox textBox = new VBox(2);
            textBox.setPadding(new Insets(8, 0, 0, 0));

            // 标题
            Node titleNode = buildTitleNode(effectiveStatus, theme);
            textBox.getChildren().add(titleNode);

            // 描述
            String desc = description.get();
            if ((desc != null && !desc.isEmpty()) || descriptionSlot != null) {
                Node descNode = buildDescriptionNode(effectiveStatus, theme);
                textBox.getChildren().add(descNode);
            }

            // 居中对齐处理
            if (alignCenter) {
                textBox.setAlignment(Pos.TOP_CENTER);
            }

            VBox stepColumn = new VBox();
            stepColumn.getChildren().addAll(headBox, textBox);

            if (alignCenter) {
                stepColumn.setAlignment(Pos.TOP_CENTER);
            }

            container.getChildren().add(stepColumn);

            // 空间分配
            if (space != null && space.doubleValue() > 0) {
                container.setPrefWidth(space.doubleValue());
                container.setMinWidth(space.doubleValue());
                container.setMaxWidth(space.doubleValue());
            } else {
                HBox.setHgrow(container, Priority.ALWAYS);
                if (!isLast) {
                    HBox.setHgrow(stepColumn, Priority.ALWAYS);
                    HBox.setHgrow(headBox, Priority.ALWAYS);
                }
            }

            return container;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "构建水平步骤节点失败: index=" + index, e);
            return new HBox(new Label("Error"));
        }
    }

    /**
     * 构建垂直模式下的步骤节点
     *
     * @param effectiveStatus 最终有效状态
     * @param theme           主题配置
     * @param isLast          是否为最后一个步骤
     * @return 垂直布局的步骤节点
     */
    public Region buildVertical(StepStatus effectiveStatus, StepsTheme theme, boolean isLast) {
        try {
            HBox container = new HBox(10);
            container.getStyleClass().add("step-item-vertical");
            container.setAlignment(Pos.TOP_LEFT);

            // 左侧：图标 + 垂直连接线
            VBox iconColumn = new VBox();
            iconColumn.setAlignment(Pos.TOP_CENTER);
            iconColumn.setMinWidth(theme.getIconSize() + 8);

            Node iconNode = buildIconNode(effectiveStatus, theme);
            iconColumn.getChildren().add(iconNode);

            if (!isLast) {
                Region vertLine = buildVerticalLine(effectiveStatus, theme);
                VBox.setVgrow(vertLine, Priority.ALWAYS);
                iconColumn.getChildren().add(vertLine);
            }

            // 右侧：标题 + 描述
            VBox textBox = new VBox(4);
            textBox.setPadding(new Insets(0, 0, isLast ? 0 : 20, 0));

            Node titleNode = buildTitleNode(effectiveStatus, theme);
            textBox.getChildren().add(titleNode);

            String desc = description.get();
            if ((desc != null && !desc.isEmpty()) || descriptionSlot != null) {
                Node descNode = buildDescriptionNode(effectiveStatus, theme);
                textBox.getChildren().add(descNode);
            }

            container.getChildren().addAll(iconColumn, textBox);
            return container;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "构建垂直步骤节点失败: index=" + index, e);
            return new HBox(new Label("Error"));
        }
    }

    /**
     * 构建简洁模式下的步骤节点
     *
     * @param effectiveStatus 最终有效状态
     * @param theme           主题配置
     * @param isLast          是否为最后一个步骤
     * @param stepNumber      步骤编号（从1开始）
     * @param totalSteps      总步骤数
     * @return 简洁模式的步骤节点
     */
    public Region buildSimple(StepStatus effectiveStatus, StepsTheme theme,
                              boolean isLast, int stepNumber, int totalSteps) {
        try {
            HBox container = new HBox(8);
            container.setAlignment(Pos.CENTER);
            container.getStyleClass().add("step-item-simple");

            // 图标/编号
            Node iconNode = buildSimpleIconNode(effectiveStatus, theme, stepNumber);
            container.getChildren().add(iconNode);

            // 标题
            Node titleNode;
            if (titleSlot != null) {
                titleNode = titleSlot;
            } else {
                Label titleLabel = new Label(title.get());
                Color titleColor = theme.getColorForStatus(effectiveStatus);
                titleLabel.setTextFill(titleColor);
                titleLabel.setFont(Font.font(theme.getFontFamily(), FontWeight.NORMAL, theme.getTitleFontSize()));
                titleNode = titleLabel;
            }
            container.getChildren().add(titleNode);

            // 箭头分隔符（非最后一步）
            if (!isLast) {
                SVGPath arrow = new SVGPath();
                arrow.setContent("M 0 0 L 5 5 L 0 10");
                arrow.setFill(Color.TRANSPARENT);
                arrow.setStroke(theme.getWaitColor());
                arrow.setStrokeWidth(1.5);
                HBox.setMargin(arrow, new Insets(0, 8, 0, 8));
                container.getChildren().add(arrow);
            }

            HBox.setHgrow(container, Priority.ALWAYS);
            return container;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "构建简洁步骤节点失败: index=" + index, e);
            return new HBox(new Label("Error"));
        }
    }

    // ==================== 内部构建方法 ====================

    /**
     * 构建图标节点（圆形带编号或自定义图标）
     */
    private Node buildIconNode(StepStatus effectiveStatus, StepsTheme theme) {
        // 优先使用自定义插槽
        if (iconSlot != null) {
            return iconSlot;
        }
        if (icon.get() != null) {
            return icon.get();
        }

        Color statusColor = theme.getColorForStatus(effectiveStatus);
        double iconSize = theme.getIconSize();

        StackPane iconPane = new StackPane();
        iconPane.setMinSize(iconSize, iconSize);
        iconPane.setPrefSize(iconSize, iconSize);
        iconPane.setMaxSize(iconSize, iconSize);

        Circle circle = new Circle(iconSize / 2);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(statusColor);
        circle.setStrokeWidth(2);

        // 根据状态显示不同内容
        Node centerContent;
        if (effectiveStatus == StepStatus.SUCCESS || effectiveStatus == StepStatus.FINISH) {
            // 成功/完成状态显示对勾
            SVGPath checkIcon = buildCheckIcon(statusColor, iconSize * 0.4);
            circle.setFill(statusColor);
            checkIcon.setStroke(Color.WHITE);
            centerContent = checkIcon;
        } else if (effectiveStatus == StepStatus.ERROR) {
            // 错误状态显示叉号
            SVGPath closeIcon = buildCloseIcon(statusColor, iconSize * 0.35);
            circle.setFill(statusColor);
            closeIcon.setStroke(Color.WHITE);
            centerContent = closeIcon;
        } else if (effectiveStatus == StepStatus.PROCESS) {
            // 进行中 - 实心背景 + 白色数字
            circle.setFill(statusColor);
            Text text = new Text(String.valueOf(index + 1));
            text.setFill(Color.WHITE);
            text.setFont(Font.font(theme.getFontFamily(), FontWeight.BOLD, theme.getIconFontSize()));
            centerContent = text;
        } else {
            // 等待状态显示数字
            Text text = new Text(String.valueOf(index + 1));
            text.setFill(statusColor);
            text.setFont(Font.font(theme.getFontFamily(), FontWeight.NORMAL, theme.getIconFontSize()));
            centerContent = text;
        }

        iconPane.getChildren().addAll(circle, centerContent);
        return iconPane;
    }

    /**
     * 构建简洁模式的图标节点
     */
    private Node buildSimpleIconNode(StepStatus effectiveStatus, StepsTheme theme, int stepNumber) {
        if (iconSlot != null) {
            return iconSlot;
        }
        if (icon.get() != null) {
            return icon.get();
        }

        Color statusColor = theme.getColorForStatus(effectiveStatus);

        if (effectiveStatus == StepStatus.SUCCESS || effectiveStatus == StepStatus.FINISH) {
            return buildCheckIcon(statusColor, 10);
        } else if (effectiveStatus == StepStatus.ERROR) {
            return buildCloseIcon(statusColor, 8);
        } else {
            Text numText = new Text(String.valueOf(stepNumber));
            numText.setFill(statusColor);
            numText.setFont(Font.font(theme.getFontFamily(), FontWeight.BOLD, theme.getTitleFontSize()));
            return numText;
        }
    }

    /**
     * 构建对勾图标 (SVG)
     */
    private SVGPath buildCheckIcon(Color color, double size) {
        SVGPath check = new SVGPath();
        check.setContent("M 2 6 L 5 9 L 10 2");
        check.setFill(Color.TRANSPARENT);
        check.setStroke(color);
        check.setStrokeWidth(2);
        double scale = size / 10.0;
        check.setScaleX(scale);
        check.setScaleY(scale);
        return check;
    }

    /**
     * 构建叉号图标 (SVG)
     */
    private SVGPath buildCloseIcon(Color color, double size) {
        SVGPath close = new SVGPath();
        close.setContent("M 2 2 L 8 8 M 8 2 L 2 8");
        close.setFill(Color.TRANSPARENT);
        close.setStroke(color);
        close.setStrokeWidth(2);
        double scale = size / 10.0;
        close.setScaleX(scale);
        close.setScaleY(scale);
        return close;
    }

    /**
     * 构建标题节点
     */
    private Node buildTitleNode(StepStatus effectiveStatus, StepsTheme theme) {
        if (titleSlot != null) {
            return titleSlot;
        }
        Label label = new Label(title.get());
        Color titleColor;
        FontWeight fontWeight;

        if (effectiveStatus == StepStatus.PROCESS) {
            titleColor = theme.getTextColor();
            fontWeight = FontWeight.BOLD;
        } else if (effectiveStatus == StepStatus.WAIT) {
            titleColor = theme.getWaitColor();
            fontWeight = FontWeight.NORMAL;
        } else if (effectiveStatus == StepStatus.ERROR) {
            titleColor = theme.getErrorColor();
            fontWeight = FontWeight.NORMAL;
        } else {
            titleColor = theme.getTextColor();
            fontWeight = FontWeight.NORMAL;
        }

        label.setTextFill(titleColor);
        label.setFont(Font.font(theme.getFontFamily(), fontWeight, theme.getTitleFontSize()));
        label.setWrapText(true);
        return label;
    }

    /**
     * 构建描述节点
     */
    private Node buildDescriptionNode(StepStatus effectiveStatus, StepsTheme theme) {
        if (descriptionSlot != null) {
            return descriptionSlot;
        }
        Label label = new Label(description.get());
        label.setTextFill(theme.getDescriptionColor());
        label.setFont(Font.font(theme.getFontFamily(), theme.getDescriptionFontSize()));
        label.setWrapText(true);
        label.setMaxWidth(200);
        return label;
    }

    /**
     * 构建水平连接线
     */
    private Region buildHorizontalLine(StepStatus effectiveStatus, StepsTheme theme) {
        Region line = new Region();
        Color lineColor = (effectiveStatus == StepStatus.FINISH || effectiveStatus == StepStatus.SUCCESS)
                ? theme.getFinishLineColor() : theme.getLineColor();

        line.setStyle(String.format(
                "-fx-background-color: %s; -fx-min-height: %.0fpx; -fx-max-height: %.0fpx; -fx-pref-height: %.0fpx;",
                StepsTheme.toCssColor(lineColor),
                theme.getLineHeight(), theme.getLineHeight(), theme.getLineHeight()));
        line.setMinWidth(20);

        // 垂直居中连接线到图标中心
        HBox wrapper = new HBox(line);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(theme.getIconSize() / 2 - theme.getLineHeight() / 2, 6, 0, 6));
        HBox.setHgrow(line, Priority.ALWAYS);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    /**
     * 构建垂直连接线
     */
    private Region buildVerticalLine(StepStatus effectiveStatus, StepsTheme theme) {
        Region line = new Region();
        Color lineColor = (effectiveStatus == StepStatus.FINISH || effectiveStatus == StepStatus.SUCCESS)
                ? theme.getFinishLineColor() : theme.getLineColor();

        line.setStyle(String.format(
                "-fx-background-color: %s; -fx-min-width: %.0fpx; -fx-max-width: %.0fpx; -fx-pref-width: %.0fpx;",
                StepsTheme.toCssColor(lineColor),
                theme.getLineHeight(), theme.getLineHeight(), theme.getLineHeight()));
        line.setMinHeight(20);
        VBox.setVgrow(line, Priority.ALWAYS);
        return line;
    }

    // ==================== Property Getters ====================

    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<StepStatus> statusProperty() { return status; }
    public ObjectProperty<Node> iconProperty() { return icon; }

    public String getTitle() { return title.get(); }
    public void setTitle(String t) { title.set(t); }
    public String getDescription() { return description.get(); }
    public void setDescription(String d) { description.set(d); }
    public StepStatus getStatus() { return status.get(); }
    public void setStatus(StepStatus s) { status.set(s); }
    public Node getIcon() { return icon.get(); }
    public void setIcon(Node n) { icon.set(n); }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public Node getTitleSlot() { return titleSlot; }
    public Node getDescriptionSlot() { return descriptionSlot; }
    public Node getIconSlot() { return iconSlot; }
}
