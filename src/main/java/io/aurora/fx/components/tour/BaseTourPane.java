package io.aurora.fx.components.tour;

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
 * Tour 演示组件抽象基类
 * <p>
 * 提供统一的卡片样式容器和基础功能封装，所有 Tour 演示组件继承此类。
 * 子类只需实现 {@link #buildContent()} 构建具体的演示内容。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public abstract class BaseTourPane extends VBox {

    protected static final Logger LOGGER = Logger.getLogger(BaseTourPane.class.getName());

    /** 默认卡片内边距 */
    protected static final double DEFAULT_PADDING = 20;

    /** 默认组件间距 */
    protected static final double DEFAULT_SPACING = 8;

    /** 默认标题字体大小 */
    protected static final double DEFAULT_TITLE_FONT_SIZE = 18;

    /** 默认描述字体大小 */
    protected static final double DEFAULT_DESC_FONT_SIZE = 12;

    /** 卡片标题 */
    private final String title;

    /** 卡片描述 */
    private final String description;

    /** 内部 Tour 实例（子类构建） */
    protected Tour tour;

    /** 是否已释放资源 */
    private volatile boolean disposed = false;

    protected BaseTourPane(String title, String description) {
        this.title = title;
        this.description = description;
        initialize();
    }

    private void initialize() {
        setSpacing(DEFAULT_SPACING);
        setPadding(new Insets(DEFAULT_PADDING));
        setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        getChildren().add(buildTitleSection());

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #EBEEF5;");
        setMargin(separator, new Insets(5, 0, 10, 0));
        getChildren().add(separator);

        try {
            buildContent();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "构建 Tour 演示失败: " + getClass().getSimpleName(), e);
            getChildren().add(createErrorLabel("组件加载失败: " + e.getMessage()));
        }
    }

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
     * 子类实现具体演示内容
     */
    protected abstract void buildContent();

    /**
     * 创建错误提示
     *
     * @param msg 信息
     * @return Label
     */
    protected Label createErrorLabel(String msg) {
        Label label = new Label(msg);
        label.setTextFill(Color.valueOf("#F56C6C"));
        label.setFont(Font.font("Microsoft YaHei", DEFAULT_DESC_FONT_SIZE));
        return label;
    }

    /**
     * 获取内部 Tour 实例
     *
     * @return Tour，可能为 null
     */
    public Tour getTour() {
        return tour;
    }

    /**
     * 获取标题
     *
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 释放资源
     */
    public void dispose() {
        if (disposed) return;
        disposed = true;
        if (tour != null) {
            tour.dispose();
            tour = null;
        }
        getChildren().clear();
    }

    /**
     * 是否已释放
     *
     * @return 是否
     */
    public boolean isDisposed() {
        return disposed;
    }
}
