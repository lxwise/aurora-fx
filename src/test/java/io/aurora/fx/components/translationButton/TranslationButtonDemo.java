package io.aurora.fx.components.translationButton;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * TranslationButton 平移按钮组件综合演示
 * <p>
 * 展示所有功能特性：基础用法、多种方向、主题定制、图标按钮、交互式控制。
 * 使用 ikonli-antdesignicons-pack 图标库。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class TranslationButtonDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #F5F7FA;");

        Label title = new Label("TranslationButton 平移按钮组件演示");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        title.setTextFill(Color.valueOf("#303133"));

        Label subtitle = new Label("鼠标悬停时通过平移动画切换显示内容的按钮组件");
        subtitle.setFont(Font.font("Microsoft YaHei", 14));
        subtitle.setTextFill(Color.valueOf("#909399"));

        mainContainer.getChildren().addAll(title, subtitle,
                buildBasicDemo(),
                buildDirectionDemo(),
                buildThemeDemo(),
                buildIconDemo(),
                buildInteractiveDemo());

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F7FA;");

        Scene scene = new Scene(scrollPane, 900, 750);
        primaryStage.setTitle("TranslationButton 平移按钮 - 综合演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** 1. 基础用法 */
    private VBox buildBasicDemo() {
        VBox card = createCard("基础用法", "默认从下向上平移，鼠标悬停触发动画。");

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        TranslationButton btn1 = new TranslationButton("悬停我")
                .onAction(e -> System.out.println("按钮被点击！"));
        btn1.setPrefSize(140, 45);

        TranslationButton btn2 = new TranslationButton("了解更多")
                .direction(TranslationDirection.LEFT_TO_RIGHT)
                .onAction(e -> System.out.println("了解更多"));
        btn2.setPrefSize(140, 45);

        row.getChildren().addAll(btn1, btn2);
        card.getChildren().add(row);
        return card;
    }

    /** 2. 方向演示 */
    private VBox buildDirectionDemo() {
        VBox card = createCard("多种方向", "支持 BOTTOM_TO_TOP、TOP_TO_BOTTOM、LEFT_TO_RIGHT、RIGHT_TO_LEFT 四种方向。");

        FlowPane flow = new FlowPane(15, 15);
        flow.setAlignment(Pos.CENTER_LEFT);

        for (TranslationDirection dir : TranslationDirection.values()) {
            // 每个按钮使用对应方向的图标
            FontIcon dirIcon = createDirectionIcon(dir);

            TranslationButton btn = new TranslationButton(dir.name());
            btn.setPrefSize(200, 50);
            btn.direction(dir);
            btn.getHoverLabel().setGraphic(dirIcon);
            btn.getHoverLabel().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            flow.getChildren().add(btn);
        }

        card.getChildren().add(flow);
        return card;
    }

    /** 3. 主题演示 */
    private VBox buildThemeDemo() {
        VBox card = createCard("主题定制", "内置 DEFAULT、DARK、PRIMARY、SUCCESS、DANGER 预设主题，支持 Builder 自定义。");

        FlowPane flow = new FlowPane(12, 12);
        flow.setAlignment(Pos.CENTER_LEFT);

        // 预设主题
        TranslationButton def = new TranslationButton("默认");
        def.setPrefSize(100, 42);
        def.theme(TranslationButtonTheme.DEFAULT);

        TranslationButton dark = new TranslationButton("深色");
        dark.setPrefSize(100, 42);
        dark.theme(TranslationButtonTheme.DARK);

        TranslationButton primary = new TranslationButton("主要");
        primary.setPrefSize(100, 42);
        primary.theme(TranslationButtonTheme.PRIMARY);

        TranslationButton success = new TranslationButton("成功");
        success.setPrefSize(100, 42);
        success.theme(TranslationButtonTheme.SUCCESS);

        TranslationButton danger = new TranslationButton("危险");
        danger.setPrefSize(100, 42);
        danger.theme(TranslationButtonTheme.DANGER);

        // 自定义主题 - 渐变紫
        TranslationButtonTheme purpleTheme = TranslationButtonTheme.builder()
                .backgroundColor(Color.valueOf("#F3E8FF"))
                .textColor(Color.valueOf("#7C3AED"))
                .hoverBackgroundColor(Color.valueOf("#7C3AED"))
                .hoverTextColor(Color.WHITE)
                .borderColor(Color.valueOf("#C4B5FD"))
                .borderRadius(20)
                .build();
        TranslationButton customPurple = new TranslationButton("自定义紫");
        customPurple.setPrefSize(120, 42);
        customPurple.theme(purpleTheme);

        // 自定义主题 - 橙色圆角
        TranslationButtonTheme orangeTheme = TranslationButtonTheme.builder()
                .backgroundColor(Color.valueOf("#FFF7ED"))
                .textColor(Color.valueOf("#EA580C"))
                .hoverBackgroundColor(Color.valueOf("#EA580C"))
                .hoverTextColor(Color.WHITE)
                .borderColor(Color.valueOf("#FDBA74"))
                .borderRadius(8)
                .build();
        TranslationButton customOrange = new TranslationButton("自定义橙");
        customOrange.setPrefSize(120, 42);
        customOrange.theme(orangeTheme);

        // 给每个按钮设置对应的悬停图标
        addHoverIcon(def, AntDesignIconsOutlined.UP);
        addHoverIcon(dark, AntDesignIconsOutlined.UP);
        addHoverIcon(primary, AntDesignIconsOutlined.CHECK);
        addHoverIcon(success, AntDesignIconsOutlined.CHECK_CIRCLE);
        addHoverIcon(danger, AntDesignIconsOutlined.CLOSE_CIRCLE);
        addHoverIcon(customPurple, AntDesignIconsOutlined.STAR);
        addHoverIcon(customOrange, AntDesignIconsOutlined.FIRE);

        flow.getChildren().addAll(def, dark, primary, success, danger, customPurple, customOrange);
        card.getChildren().add(flow);
        return card;
    }

    /** 4. 图标按钮 */
    private VBox buildIconDemo() {
        VBox card = createCard("图标按钮", "悬停时显示图标，通过 getHoverLabel().setGraphic() 设置。使用 ikonli-antdesignicons-pack 图标。");

        FlowPane flow = new FlowPane(15, 15);
        flow.setAlignment(Pos.CENTER_LEFT);

        // 提交按钮 - 悬停显示右箭头
        TranslationButton submitBtn = new TranslationButton("提交");
        submitBtn.setPrefSize(140, 45);
        submitBtn.theme(TranslationButtonTheme.PRIMARY);
        addHoverIcon(submitBtn, AntDesignIconsOutlined.ARROW_RIGHT);

        // 删除按钮 - 悬停显示删除图标
        TranslationButton deleteBtn = new TranslationButton("删除");
        deleteBtn.setPrefSize(140, 45);
        deleteBtn.theme(TranslationButtonTheme.DANGER);
        addHoverIcon(deleteBtn, AntDesignIconsOutlined.DELETE);

        // 搜索按钮 - 悬停显示搜索图标
        TranslationButton searchBtn = new TranslationButton("搜索");
        searchBtn.setPrefSize(140, 45);
        addHoverIcon(searchBtn, AntDesignIconsOutlined.SEARCH);

        // 下载按钮 - 悬停显示下载图标
        TranslationButton downloadBtn = new TranslationButton("下载");
        downloadBtn.setPrefSize(140, 45);
        TranslationButtonTheme successRound = TranslationButtonTheme.builder()
                .backgroundColor(Color.valueOf("#F0F9EB"))
                .textColor(Color.valueOf("#67C23A"))
                .hoverBackgroundColor(Color.valueOf("#67C23A"))
                .hoverTextColor(Color.WHITE)
                .borderColor(Color.valueOf("#C2E7B0"))
                .borderRadius(22)
                .build();
        downloadBtn.theme(successRound);
        addHoverIcon(downloadBtn, AntDesignIconsOutlined.DOWNLOAD);

        // 设置按钮 - 圆形深色
        TranslationButton settingBtn = new TranslationButton("设置");
        settingBtn.setPrefSize(140, 45);
        settingBtn.theme(TranslationButtonTheme.DARK);
        addHoverIcon(settingBtn, AntDesignIconsOutlined.SETTING);

        flow.getChildren().addAll(submitBtn, deleteBtn, searchBtn, downloadBtn, settingBtn);
        card.getChildren().add(flow);
        return card;
    }

    /** 5. 交互式演示 */
    private VBox buildInteractiveDemo() {
        VBox card = createCard("交互式控制", "动态切换方向和主题，实时预览效果。");

        TranslationButton btn = new TranslationButton("交互式按钮");
        btn.setPrefSize(200, 55);

        // 默认悬停图标
        addHoverIcon(btn, AntDesignIconsOutlined.ARROW_UP);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        ComboBox<TranslationDirection> dirCombo = new ComboBox<>();
        dirCombo.getItems().addAll(TranslationDirection.values());
        dirCombo.setValue(TranslationDirection.BOTTOM_TO_TOP);
        dirCombo.setOnAction(e -> {
            btn.setDirection(dirCombo.getValue());
            // 同步更新悬停图标方向
            btn.getHoverLabel().setGraphic(createDirectionIcon(dirCombo.getValue()));
        });

        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("DEFAULT", "DARK", "PRIMARY", "SUCCESS", "DANGER");
        themeCombo.setValue("DEFAULT");
        themeCombo.setOnAction(e -> {
            switch (themeCombo.getValue()) {
                case "DARK": btn.setTheme(TranslationButtonTheme.DARK); break;
                case "PRIMARY": btn.setTheme(TranslationButtonTheme.PRIMARY); break;
                case "SUCCESS": btn.setTheme(TranslationButtonTheme.SUCCESS); break;
                case "DANGER": btn.setTheme(TranslationButtonTheme.DANGER); break;
                default: btn.setTheme(TranslationButtonTheme.DEFAULT); break;
            }
        });

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.valueOf("#909399"));
        statusLabel.setFont(Font.font("Microsoft YaHei", 12));
        btn.onAction(e -> statusLabel.setText("按钮被点击! ✓"));

        controls.getChildren().addAll(new Label("方向:"), dirCombo, new Label("主题:"), themeCombo, statusLabel);
        card.getChildren().addAll(btn, controls);
        return card;
    }

    // ==================== 辅助方法 ====================

    /** 为按钮的 hoverLabel 添加 ikonli 图标 */
    private void addHoverIcon(TranslationButton btn, AntDesignIconsOutlined iconType) {
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(18);
        icon.setIconColor(Color.WHITE);
        btn.getHoverLabel().setGraphic(icon);
        btn.getHoverLabel().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /** 根据方向创建对应图标 */
    private FontIcon createDirectionIcon(TranslationDirection dir) {
        AntDesignIconsOutlined iconCode;
        switch (dir) {
            case TOP_TO_BOTTOM: iconCode = AntDesignIconsOutlined.ARROW_DOWN; break;
            case LEFT_TO_RIGHT: iconCode = AntDesignIconsOutlined.ARROW_RIGHT; break;
            case RIGHT_TO_LEFT: iconCode = AntDesignIconsOutlined.ARROW_LEFT; break;
            case BOTTOM_TO_TOP:
            default: iconCode = AntDesignIconsOutlined.ARROW_UP; break;
        }
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(18);
        icon.setIconColor(Color.WHITE);
        return icon;
    }

    private VBox createCard(String title, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#303133"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Microsoft YaHei", 12));
        descLabel.setTextFill(Color.valueOf("#909399"));
        descLabel.setWrapText(true);

        Separator separator = new Separator();
        VBox.setMargin(separator, new Insets(5, 0, 10, 0));

        card.getChildren().addAll(titleLabel, descLabel, separator);
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
