package io.aurora.fx.components.lineButton;

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
 * LineButton 线条按钮组件综合演示
 * <p>
 * 使用 ikonli-antdesignicons-pack 图标库，展示更接近生产使用的导航栏效果。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class LineButtonDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #F5F7FA;");

        Label title = new Label("LineButton 线条按钮组件演示");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        title.setTextFill(Color.valueOf("#303133"));

        Label subtitle = new Label("鼠标悬停时在文字下方显示线条动画的按钮组件");
        subtitle.setFont(Font.font("Microsoft YaHei", 14));
        subtitle.setTextFill(Color.valueOf("#909399"));

        mainContainer.getChildren().addAll(title, subtitle,
                buildBasicDemo(),
                buildAnimationTypeDemo(),
                buildThemeDemo(),
                buildNavBarDemo(),
                buildInteractiveDemo());

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F7FA;");

        Scene scene = new Scene(scrollPane, 900, 750);
        primaryStage.setTitle("LineButton 线条按钮 - 综合演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** 1. 基础用法 */
    private VBox buildBasicDemo() {
        VBox card = createCard("基础用法", "默认 EXTEND 类型，线条从中心向两边延伸。");

        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);

        LineButton btn1 = new LineButton("悬停我")
                .onAction(e -> System.out.println("LineButton clicked!"));

        LineButton btn2 = new LineButton("了解详情")
                .lineType(LineAnimationType.RISE)
                .theme(LineButtonTheme.PRIMARY)
                .onAction(e -> System.out.println("了解详情"));

        row.getChildren().addAll(btn1, btn2);
        card.getChildren().add(row);
        return card;
    }

    /** 2. 动画类型演示 */
    private VBox buildAnimationTypeDemo() {
        VBox card = createCard("动画类型", "支持 EXTEND（延伸）和 RISE（上升）两种动画类型。");

        FlowPane flow = new FlowPane(20, 15);
        flow.setAlignment(Pos.CENTER_LEFT);

        LineButton extend = new LineButton("EXTEND 延伸")
                .lineType(LineAnimationType.EXTEND);

        LineButton rise = new LineButton("RISE 上升")
                .lineType(LineAnimationType.RISE);

        // 带图标的 EXTEND
        FontIcon homeIcon = new FontIcon(AntDesignIconsOutlined.HOME);
        homeIcon.setIconSize(14);
        LineButton extendWithIcon = new LineButton("首页", homeIcon);
        extendWithIcon.lineType(LineAnimationType.EXTEND);
        extendWithIcon.setContentDisplay(ContentDisplay.LEFT);
        extendWithIcon.setGraphicTextGap(6);

        flow.getChildren().addAll(extend, rise, extendWithIcon);
        card.getChildren().add(flow);
        return card;
    }

    /** 3. 主题演示 */
    private VBox buildThemeDemo() {
        VBox card = createCard("主题定制", "内置 DEFAULT、DARK、PRIMARY、DANGER 预设主题，支持 Builder 自定义。");

        FlowPane flow = new FlowPane(15, 15);
        flow.setAlignment(Pos.CENTER_LEFT);

        LineButton def = new LineButton("Default").theme(LineButtonTheme.DEFAULT);
        LineButton primary = new LineButton("Primary").theme(LineButtonTheme.PRIMARY);
        LineButton danger = new LineButton("Danger").theme(LineButtonTheme.DANGER);

        LineButtonTheme successTheme = LineButtonTheme.builder()
                .textColor(Color.valueOf("#67C23A"))
                .lineColor(Color.valueOf("#67C23A"))
                .hoverTextColor(Color.valueOf("#85CE61"))
                .lineWidth(2)
                .build();
        LineButton success = new LineButton("Success").theme(successTheme);

        LineButtonTheme warningTheme = LineButtonTheme.builder()
                .textColor(Color.valueOf("#E6A23C"))
                .lineColor(Color.valueOf("#E6A23C"))
                .hoverTextColor(Color.valueOf("#EBB563"))
                .lineWidth(2)
                .build();
        LineButton warning = new LineButton("Warning").theme(warningTheme);

        flow.getChildren().addAll(def, primary, danger, success, warning);
        card.getChildren().add(flow);
        return card;
    }

    /** 4. 导航栏模拟 */
    private VBox buildNavBarDemo() {
        VBox card = createCard("导航栏模拟", "模拟网站顶部导航栏效果，使用 ikonli 图标。");

        // 顶部导航栏
        HBox navBar = new HBox(0);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setStyle("-fx-background-color: white; -fx-padding: 0 24; " +
                "-fx-border-color: #E4E7ED; -fx-border-width: 0 0 1 0;");

        // Logo
        FontIcon logoIcon = new FontIcon(AntDesignIconsOutlined.APPSTORE);
        logoIcon.setIconSize(20);
        logoIcon.setIconColor(Color.valueOf("#409EFF"));
        Label logoLabel = new Label("Aurora-FX");
        logoLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        logoLabel.setTextFill(Color.valueOf("#303133"));
        HBox logoBox = new HBox(8, logoIcon, logoLabel);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(0, 30, 0, 0));

        // 导航项
        String[][] navItems = {
                {"首页", AntDesignIconsOutlined.HOME.name()},
                {"产品", AntDesignIconsOutlined.APPSTORE.name()},
                {"文档", AntDesignIconsOutlined.BOOK.name()},
                {"关于", AntDesignIconsOutlined.INFO_CIRCLE.name()},
                {"联系", AntDesignIconsOutlined.MAIL.name()}
        };

        Label selectedLabel = new Label("当前选中: 首页");
        selectedLabel.setTextFill(Color.valueOf("#909399"));
        selectedLabel.setFont(Font.font("Microsoft YaHei", 12));
        selectedLabel.setPadding(new Insets(10, 0, 0, 0));

        for (String[] item : navItems) {
            FontIcon navIcon = new FontIcon(AntDesignIconsOutlined.valueOf(item[1]));
            navIcon.setIconSize(14);

            LineButton btn = new LineButton(item[0], navIcon);
            btn.setLineType(LineAnimationType.EXTEND);
            btn.setSpacing(2);
            btn.setTheme(LineButtonTheme.PRIMARY);
            btn.setPrefSize(100, 45);
            btn.setContentDisplay(ContentDisplay.TOP);
            btn.setGraphicTextGap(4);
            btn.onAction(e -> selectedLabel.setText("当前选中: " + item[0]));
            navBar.getChildren().add(btn);
        }

        navBar.getChildren().add(0, logoBox);
        card.getChildren().addAll(navBar, selectedLabel);
        return card;
    }

    /** 5. 交互式演示 */
    private VBox buildInteractiveDemo() {
        VBox card = createCard("交互式控制", "动态切换动画类型和主题。");

        LineButton btn = new LineButton("交互式 LineButton");
        btn.setPrefSize(200, 60);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        ComboBox<LineAnimationType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(LineAnimationType.values());
        typeCombo.setValue(LineAnimationType.EXTEND);
        typeCombo.setOnAction(e -> btn.setLineType(typeCombo.getValue()));

        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("DEFAULT", "PRIMARY", "DANGER");
        themeCombo.setValue("DEFAULT");
        themeCombo.setOnAction(e -> {
            switch (themeCombo.getValue()) {
                case "PRIMARY": btn.setTheme(LineButtonTheme.PRIMARY); break;
                case "DANGER": btn.setTheme(LineButtonTheme.DANGER); break;
                default: btn.setTheme(LineButtonTheme.DEFAULT); break;
            }
        });

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.valueOf("#909399"));
        btn.onAction(e -> statusLabel.setText("按钮被点击! ✓"));

        controls.getChildren().addAll(new Label("动画:"), typeCombo, new Label("主题:"), themeCombo, statusLabel);
        card.getChildren().addAll(btn, controls);
        return card;
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
