package io.aurora.fx.theme;

import atlantafx.base.theme.Theme;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.List;

/**
 * 关于窗口演示 — 使用 Aurora-FX 双主题系统
 * <p>
 * 演示如何使用 Aurora-FX 的 Windows 11 Fluent Design 主题和 macOS Vibrancy 主题
 * 来构建一个精美的"关于"窗口。所有样式完全依赖主题系统提供的语义化 CSS 变量，
 * 无需硬编码颜色值。
 * </p>
 * <p>
 * 功能特性：
 * <ul>
 *   <li>动态切换 Windows 11 / macOS 双主题</li>
 *   <li>Light / Dark 模式切换</li>
 *   <li>透明无装饰窗口 + 自定义拖拽</li>
 *   <li>弹出动画（缩放 + 淡入）</li>
 *   <li>作者信息卡片 + 链接交互</li>
 *   <li>完全使用语义化 CSS 变量驱动样式</li>
 * </ul>
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class AboutWindowDemo extends Application {

    // ==================== 动画常量 ====================

    /** 卡片 hover 动画持续时间 */
    private static final Duration HOVER_DURATION = Duration.millis(180);
    /** 卡片默认阴影 */
    private static final String CARD_SHADOW_NORMAL =
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);";
    /** 卡片 hover 阴影 */
    private static final String CARD_SHADOW_HOVER =
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.14), 14, 0.08, 0, 4);";
    /** 卡片基础样式（不含 effect） */
    private static final String CARD_BASE_STYLE =
            "-fx-background-color: -color-bg-default; "
                    + "-fx-background-radius: 10; "
                    + "-fx-border-color: -color-border-muted; "
                    + "-fx-border-radius: 10; "
                    + "-fx-border-width: 0.5; ";

    /** 当前活跃的主题 */
    private Theme currentTheme;

    /** 主场景引用（用于动态切换主题） */
    private Scene mainScene;

    /** 关于窗口的 Stage（用于关闭） */
    private Stage aboutStage;

    @Override
    public void start(Stage primaryStage) {
        // 默认使用当前操作系统推荐的浅色主题
        currentTheme = OSThemeFactory.recommendedLightTheme();
        Application.setUserAgentStylesheet(currentTheme.getUserAgentStylesheet());

        // 构建主控制面板
        VBox root = new VBox(24);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: -color-bg-default;");

        // 标题
        Label title = new Label("Aurora-FX 主题系统演示");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        Label subtitle = new Label("选择主题后点击「打开关于窗口」查看效果");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");

        // 主题选择区
        VBox themeSelector = createThemeSelector();

        // 打开关于窗口按钮
        Button openAboutBtn = new Button("打开关于窗口");
        openAboutBtn.setDefaultButton(true);
        openAboutBtn.setStyle("-fx-font-size: 15px; -fx-padding: 12 36;");
        openAboutBtn.setOnAction(e -> showAboutWindow(primaryStage));

        // 直接打开独立透明关于窗口按钮
        Button openTransparentBtn = new Button("打开透明关于窗口（无装饰）");
        openTransparentBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8 24;");
        openTransparentBtn.setOnAction(e -> showTransparentAboutWindow());

        root.getChildren().addAll(title, subtitle, new Separator(), themeSelector, new Separator(), openAboutBtn, openTransparentBtn);

        mainScene = new Scene(root, 560, 520);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Aurora-FX About Window Demo");
        primaryStage.show();
    }

    // ==================== 主题选择器 ====================

    /**
     * 创建主题选择面板
     */
    private VBox createThemeSelector() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);

        Label sectionTitle = new Label("选择主题");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        // 主题按钮网格
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        List<Theme> themes = OSThemeFactory.allThemes();
        int col = 0;
        int row = 0;
        for (Theme theme : themes) {
            Button btn = new Button(theme.getName());
            btn.setPrefWidth(180);
            btn.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
            if (theme.isDarkMode()) {
                btn.getStyleClass().add("accent");
            }
            btn.setOnAction(e -> switchTheme(theme));
            grid.add(btn, col, row);
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        // 当前主题显示
        Label currentLabel = new Label("当前主题: " + currentTheme.getName());
        currentLabel.setId("current-theme-label");
        currentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-accent-fg;");

        box.getChildren().addAll(sectionTitle, grid, currentLabel);
        return box;
    }

    /**
     * 切换主题
     */
    private void switchTheme(Theme theme) {
        currentTheme = theme;
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());

        // 更新当前主题标签
        Label label = (Label) mainScene.lookup("#current-theme-label");
        if (label != null) {
            label.setText("当前主题: " + theme.getName());
        }
    }

    // ==================== 关于窗口（普通模式） ====================

    /**
     * 在普通窗口中打开关于页面
     */
    private void showAboutWindow(Stage owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);

        ScrollPane scrollPane = new ScrollPane(buildAboutContent(stage));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: -color-bg-default;");

        Scene scene = new Scene(scrollPane, 520, 640);
        stage.setScene(scene);
        stage.setTitle("关于 Aurora-FX");
        stage.setResizable(false);
        stage.show();
    }

    // ==================== 关于窗口（透明无装饰模式） ====================

    /**
     * 以透明无装饰窗口模式打开关于页面（类似原版 AboutWindow 的效果）
     */
    private void showTransparentAboutWindow() {
        aboutStage = new Stage();
        aboutStage.initStyle(StageStyle.TRANSPARENT);

        VBox card = buildAboutContent(aboutStage);
        card.setPrefWidth(480);
        card.setStyle(card.getStyle() + buildCardShadowStyle());

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        // 应用当前主题的 UserAgent 样式表
        Application.setUserAgentStylesheet(currentTheme.getUserAgentStylesheet());

        aboutStage.setScene(scene);
        enableDrag(aboutStage, card);
        playShowAnimation(card);
        aboutStage.show();
    }

    // ==================== 关于窗口内容构建 ====================

    /**
     * 构建关于窗口的完整内容
     * <p>
     * 所有样式均使用主题系统的语义化 CSS 变量，自动适配不同主题。
     * 每个内容卡片均具有平滑的 hover 悬浮效果（缩放 + 阴影提升 + 边框高亮）。
     * </p>
     */
    private VBox buildAboutContent(Stage stage) {
        // === Header ===
        Label titleLabel = new Label("🎨 Aurora-FX 组件库");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: -color-fg-default;");

        Label subtitleLabel = new Label("现代化 JavaFX 组件库 — 双主题系统 · 开箱即用");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");

        Label versionTag = new Label("v1.0.0");
        versionTag.setStyle(
                "-fx-background-color: -color-success-subtle; "
                        + "-fx-text-fill: -color-success-fg; "
                        + "-fx-padding: 2 8; "
                        + "-fx-background-radius: 4; "
                        + "-fx-font-size: 11px;");

        HBox titleRow = new HBox(10, titleLabel, versionTag);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(6, titleRow, subtitleLabel);
        header.setPadding(new Insets(4, 0, 10, 0));

        // === 功能介绍 ===
        VBox featureSection = createSection("✨ 核心特性",
                "Aurora-FX 提供：\n\n"
                        + "• 🎨 Windows 11 Fluent Design 主题\n"
                        + "• 🍎 macOS Vibrancy 主题\n"
                        + "• 🌗 Light / Dark 双模式\n"
                        + "• 🧩 丰富的自定义组件\n"
                        + "• ⚡ 异步任务管理框架\n"
                        + "• 📦 开箱即用，零配置\n\n"
                        + "👉 一行代码即可切换主题风格");

        // === 主题信息 ===
        VBox themeInfoSection = createSection("🎯 当前主题信息",
                "• 主题名称: " + currentTheme.getName() + "\n"
                        + "• 深色模式: " + (currentTheme.isDarkMode() ? "是" : "否") + "\n"
                        + "• 设计语言: " + getDesignLanguage() + "\n"
                        + "• 字体系统: " + getFontFamily());

        // === 技术栈 ===
        VBox techSection = createSection("🔧 技术栈",
                "• ☕ Java 25 / JavaFX 25\n"
                        + "• 🎨 AtlantaFX 2.1.0\n"
                        + "• 🔤 Ikonli 图标库\n"
                        + "• 📐 CSS 语义化变量系统\n"
                        + "• 🏗 Skin 架构分离模式");

        // === 作者信息 ===
        VBox authorSection = createAuthorSection();

        // === 版本信息 ===
        VBox versionSection = createSection("📋 版本信息",
                "• 版本: 1.0.0-SNAPSHOT\n"
                        + "• 许可证: MIT License\n"
                        + "• 运行环境: JDK 25+ / JavaFX 25+");

        // === 内容容器 ===
        VBox content = new VBox(12, featureSection, themeInfoSection, techSection, authorSection, versionSection);

        // === 底部按钮栏 ===
        Button okBtn = new Button("知道了");
        okBtn.setDefaultButton(true);
        okBtn.setStyle("-fx-font-size: 13px; -fx-padding: 10 28;");
        okBtn.setOnAction(e -> stage.close());
        installButtonHover(okBtn);

        Label themeBadge = new Label(currentTheme.getName());
        themeBadge.setStyle(
                "-fx-background-color: -color-accent-emphasis; "
                        + "-fx-text-fill: -color-fg-emphasis; "
                        + "-fx-padding: 4 14; "
                        + "-fx-background-radius: 12; "
                        + "-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(12, themeBadge, spacer, okBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12, 0, 0, 0));

        // === 外层卡片容器 ===
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 4 0 0 0;");

        VBox card = new VBox(16, header, content, sep, buttonBar);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: -color-bg-overlay; "
                        + "-fx-background-radius: 14;");

        return card;
    }

    /**
     * 创建带 hover 悬浮效果的内容分区
     * <p>
     * hover 时卡片会轻微上浮（translateY）+ 阴影加深 + 边框高亮，
     * 使用 JavaFX Timeline 动画实现平滑过渡。
     * </p>
     */
    private VBox createSection(String title, String text) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 15px; "
                        + "-fx-font-weight: 600; "
                        + "-fx-text-fill: -color-fg-default;");

        Label descLabel = new Label(text);
        descLabel.setWrapText(true);
        descLabel.setStyle(
                "-fx-font-size: 13px; "
                        + "-fx-text-fill: -color-fg-muted; "
                        + "-fx-line-spacing: 2;");

        VBox box = new VBox(8, titleLabel, descLabel);
        box.setPadding(new Insets(16));
        box.setCursor(Cursor.HAND);
        box.setStyle(CARD_BASE_STYLE + CARD_SHADOW_NORMAL);

        // 安装 hover 悬浮动画
        installCardHover(box);

        return box;
    }

    /**
     * 创建带 hover 悬浮效果的作者信息区
     */
    private VBox createAuthorSection() {
        Label title = new Label("👤 关于作者");
        title.setStyle(
                "-fx-font-size: 15px; "
                        + "-fx-font-weight: 600; "
                        + "-fx-text-fill: -color-fg-default;");

        // 头像占位（圆形 + hover 光晕）
        Label avatarPlaceholder = new Label("A");
        avatarPlaceholder.setAlignment(Pos.CENTER);
        avatarPlaceholder.setPrefSize(64, 64);
        avatarPlaceholder.setMinSize(64, 64);
        avatarPlaceholder.setMaxSize(64, 64);
        avatarPlaceholder.setStyle(
                "-fx-background-color: -color-accent-emphasis; "
                        + "-fx-text-fill: -color-fg-emphasis; "
                        + "-fx-font-size: 28px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-background-radius: 32; "
                        + "-fx-effect: dropshadow(three-pass-box, -color-accent-muted, 8, 0.3, 0, 2);");

        Label nameLabel = new Label("Aurora-FX");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        Label descLabel = new Label("现代化 JavaFX 组件库 · 双主题系统");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-subtle;");

        VBox infoBox = new VBox(4, nameLabel, descLabel);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        HBox topRow = new HBox(14, avatarPlaceholder, infoBox);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // 分隔线
        Separator linkSep = new Separator();
        linkSep.setStyle("-fx-padding: 2 0;");

        // 链接（带 hover 效果）
        Hyperlink github = createLink("🔗 GitHub", "https://github.com/aurora-fx");
        Hyperlink docs = createLink("📖 文档", "https://aurora-fx.io/docs");
        Hyperlink issues = createLink("💬 反馈", "https://github.com/aurora-fx/issues");
        Hyperlink copyEmail = createCopyLink("📧 Email", "aurora-fx@example.com");

        FlowPane links = new FlowPane(10, 8, github, docs, issues, copyEmail);
        links.setPrefWrapLength(360);

        VBox box = new VBox(10, title, topRow, linkSep, links);
        box.setPadding(new Insets(16));
        box.setCursor(Cursor.HAND);
        box.setStyle(CARD_BASE_STYLE + CARD_SHADOW_NORMAL);

        // 安装 hover 悬浮动画
        installCardHover(box);

        return box;
    }

    // ==================== 链接交互 ====================

    /**
     * 创建可点击的超链接（带 hover 底色）
     */
    private Hyperlink createLink(String text, String url) {
        Hyperlink link = new Hyperlink(text);
        link.setStyle(
                "-fx-text-fill: -color-accent-fg; "
                        + "-fx-padding: 4 10; "
                        + "-fx-background-radius: 6;");
        // hover 底色反馈
        link.setOnMouseEntered(e -> link.setStyle(
                "-fx-text-fill: -color-accent-fg; "
                        + "-fx-padding: 4 10; "
                        + "-fx-background-radius: 6; "
                        + "-fx-background-color: -color-neutral-muted;"));
        link.setOnMouseExited(e -> link.setStyle(
                "-fx-text-fill: -color-accent-fg; "
                        + "-fx-padding: 4 10; "
                        + "-fx-background-radius: 6;"));
        link.setOnAction(e -> {
            try {
                getHostServices().showDocument(url);
            } catch (Exception ex) {
                System.out.println("打开链接: " + url);
            }
        });
        return link;
    }

    /**
     * 创建复制到剪贴板的链接（使用 PauseTransition 替代 Thread.sleep）
     */
    private Hyperlink createCopyLink(String text, String value) {
        Hyperlink link = new Hyperlink(text);
        link.setStyle(
                "-fx-text-fill: -color-accent-fg; "
                        + "-fx-padding: 4 10; "
                        + "-fx-background-radius: 6;");
        link.setOnMouseEntered(e -> {
            if (!link.getText().contains("✓")) {
                link.setStyle(
                        "-fx-text-fill: -color-accent-fg; "
                                + "-fx-padding: 4 10; "
                                + "-fx-background-radius: 6; "
                                + "-fx-background-color: -color-neutral-muted;");
            }
        });
        link.setOnMouseExited(e -> {
            if (!link.getText().contains("✓")) {
                link.setStyle(
                        "-fx-text-fill: -color-accent-fg; "
                                + "-fx-padding: 4 10; "
                                + "-fx-background-radius: 6;");
            }
        });
        link.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(value);
            Clipboard.getSystemClipboard().setContent(content);

            // 即时视觉反馈
            String original = link.getText();
            link.setText("✅ 已复制");
            link.setStyle(
                    "-fx-text-fill: -color-success-fg; "
                            + "-fx-padding: 4 10; "
                            + "-fx-background-radius: 6; "
                            + "-fx-background-color: -color-success-subtle;");

            // 1.5秒后恢复（使用 PauseTransition，不阻塞任何线程）
            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(ev -> {
                link.setText(original);
                link.setStyle(
                        "-fx-text-fill: -color-accent-fg; "
                                + "-fx-padding: 4 10; "
                                + "-fx-background-radius: 6;");
            });
            pause.play();
        });
        return link;
    }

    // ==================== 动画效果 ====================

    /**
     * 为内容卡片安装 hover 悬浮动画
     * <p>
     * 鼠标进入时：卡片向上微浮（translateY -3px）+ 阴影加深 + 缩放 1.012x<br>
     * 鼠标离开时：平滑恢复原状。
     * 使用 Timeline + KeyFrame 实现流畅的 CSS 动画。
     * </p>
     */
    private void installCardHover(VBox card) {
        // hover enter 动画
        Timeline enterAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleYProperty(), 1.0, Interpolator.EASE_BOTH)),
                new KeyFrame(HOVER_DURATION,
                        new KeyValue(card.translateYProperty(), -3, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleXProperty(), 1.012, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleYProperty(), 1.012, Interpolator.EASE_BOTH))
        );

        // hover exit 动画
        Timeline exitAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(card.translateYProperty(), -3, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleXProperty(), 1.012, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleYProperty(), 1.012, Interpolator.EASE_BOTH)),
                new KeyFrame(HOVER_DURATION,
                        new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(card.scaleYProperty(), 1.0, Interpolator.EASE_BOTH))
        );

        card.setOnMouseEntered(e -> {
            exitAnim.stop();
            card.setStyle(CARD_BASE_STYLE + CARD_SHADOW_HOVER
                    + " -fx-border-color: -color-accent-muted;");
            enterAnim.playFromStart();
        });

        card.setOnMouseExited(e -> {
            enterAnim.stop();
            card.setStyle(CARD_BASE_STYLE + CARD_SHADOW_NORMAL);
            exitAnim.playFromStart();
        });
    }

    /**
     * 为按钮安装微妙的 hover 缩放动画
     */
    private void installButtonHover(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), button);
            st.setToX(1.04);
            st.setToY(1.04);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    /**
     * 播放窗口弹出动画（缩放 + 淡入 + 上滑）
     */
    private void playShowAnimation(Node node) {
        node.setOpacity(0);
        node.setScaleX(0.92);
        node.setScaleY(0.92);
        node.setTranslateY(16);

        Timeline anim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.opacityProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleXProperty(), 0.92, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleYProperty(), 0.92, Interpolator.EASE_BOTH),
                        new KeyValue(node.translateYProperty(), 16, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(280),
                        new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleYProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(node.translateYProperty(), 0, Interpolator.EASE_BOTH))
        );
        anim.play();
    }

    /**
     * 启用窗口拖拽
     */
    private void enableDrag(Stage stage, Node dragNode) {
        final double[] offset = new double[2];
        dragNode.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });
        dragNode.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - offset[0]);
            stage.setY(e.getScreenY() - offset[1]);
        });
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建卡片阴影样式（透明窗口模式下使用）
     */
    private String buildCardShadowStyle() {
        return " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 24, 0.15, 0, 8);";
    }

    /**
     * 获取当前主题的设计语言描述
     */
    private String getDesignLanguage() {
        String name = currentTheme.getName().toLowerCase();
        if (name.contains("windows")) {
            return "Fluent Design System（毛玻璃 · 柔和阴影 · 大圆角）";
        } else if (name.contains("macos") || name.contains("mac os")) {
            return "Vibrancy Design（玻璃质感 · 极窄阴影 · 小圆角 · 无边框）";
        }
        return "Unknown";
    }

    /**
     * 获取当前主题的字体系列描述
     */
    private String getFontFamily() {
        String name = currentTheme.getName().toLowerCase();
        if (name.contains("windows")) {
            return "Segoe UI Variable";
        } else if (name.contains("macos") || name.contains("mac os")) {
            return "SF Pro Text / San Francisco";
        }
        return "System Default";
    }

    // ==================== 入口 ====================

    public static void main(String[] args) {
        launch(args);
    }
}
