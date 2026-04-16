package io.aurora.fx.components.steps;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Steps 演示组件抽象基类
 * <p>
 * 提供统一的卡片样式容器和基础功能封装，所有演示组件继承此类。
 * 子类只需实现 {@link #buildContent()} 方法来构建具体的步骤条内容。
 * </p>
 *
 * @author Steps Component
 * @version 1.0
 */
public abstract class BaseStepsPane extends VBox {

    protected static final Logger LOGGER = Logger.getLogger(BaseStepsPane.class.getName());

    /** 默认卡片内边距 */
    protected static final double DEFAULT_PADDING = 20;

    /** 默认组件间距 */
    protected static final double DEFAULT_SPACING = 8;

    /** 默认标题字体大小 */
    protected static final double DEFAULT_TITLE_FONT_SIZE = 18;

    /** 默认描述字体大小 */
    protected static final double DEFAULT_DESC_FONT_SIZE = 12;

    // ==================== 核心属性 ====================

    /** 标题 */
    private String title;

    /** 描述文本 */
    private String description;

    /** 内部 Steps 组件实例 */
    protected Steps steps;

    /** 是否已释放资源 */
    private volatile boolean disposed = false;

    // ==================== 构造方法 ====================

    /**
     * 创建演示组件
     *
     * @param title       卡片标题
     * @param description 卡片描述
     */
    protected BaseStepsPane(String title, String description) {
        this.title = title;
        this.description = description;
        initialize();
    }

    // ==================== 初始化 ====================

    private void initialize() {
        setSpacing(DEFAULT_SPACING);
        setPadding(new Insets(DEFAULT_PADDING));
        setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // 构建标题区域
        getChildren().add(buildTitleSection());

        // 分隔线
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #EBEEF5;");
        setMargin(separator, new Insets(5, 0, 10, 0));
        getChildren().add(separator);

        // 构建具体内容（由子类实现）
        try {
            buildContent();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "构建步骤条内容失败: " + getClass().getSimpleName(), e);
            getChildren().add(createErrorLabel("组件加载失败: " + e.getMessage()));
        }
    }

    /**
     * 构建标题区域
     */
    private VBox buildTitleSection() {
        VBox section = new VBox(4);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, DEFAULT_TITLE_FONT_SIZE));
        titleLabel.setTextFill(Color.valueOf("#303133"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Microsoft YaHei", DEFAULT_DESC_FONT_SIZE));
        descLabel.setTextFill(Color.valueOf("#909399"));
        descLabel.setWrapText(true);

        section.getChildren().addAll(titleLabel, descLabel);
        return section;
    }

    /**
     * 构建步骤条内容（子类实现）
     * <p>
     * 子类应在此方法中创建并配置 Steps 组件，添加到 getChildren() 中。
     * </p>
     */
    protected abstract void buildContent();

    // ==================== 工具方法 ====================

    /**
     * 创建错误标签
     */
    protected Label createErrorLabel(String message) {
        Label label = new Label(message);
        label.setTextFill(Color.valueOf("#F56C6C"));
        label.setFont(Font.font("Microsoft YaHei", DEFAULT_DESC_FONT_SIZE));
        return label;
    }

    /**
     * 创建默认的 Steps 实例
     *
     * @return Steps 实例
     */
    protected Steps createDefaultSteps() {
        this.steps = new Steps();
        return this.steps;
    }

    // ==================== 公开方法 ====================

    /**
     * 获取内部的 Steps 组件
     *
     * @return Steps 实例，如果未创建则返回 null
     */
    public Steps getSteps() {
        return steps;
    }

    /**
     * 获取标题
     *
     * @return 卡片标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取描述
     *
     * @return 卡片描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 释放资源
     * <p>
     * 清理内部 Steps 组件和监听器，防止内存泄漏。
     * </p>
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;

        if (steps != null) {
            steps.dispose();
            steps = null;
        }

        getChildren().clear();
        LOGGER.fine(getClass().getSimpleName() + " 资源已释放");
    }

    /**
     * 检查是否已释放资源
     *
     * @return 是否已释放
     */
    public boolean isDisposed() {
        return disposed;
    }
}
