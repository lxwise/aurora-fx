package io.aurora.fx.components.avatar;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Avatar 头像组件综合演示
 * <p>
 * 展示所有功能特性：多种形状、主题定制、尺寸配置、占位符、动态切换等。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public class AvatarDemo extends Application {

    private static final String SAMPLE_IMG = "https://avatars.githubusercontent.com/u/1?v=4";
    private static final String SAMPLE_IMG2 = "https://avatars.githubusercontent.com/u/2?v=4";

    @Override
    public void start(Stage primaryStage) {
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #F5F7FA;");

        Label title = new Label("Avatar 头像组件演示");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        title.setTextFill(Color.valueOf("#303133"));

        Label subtitle = new Label("支持圆形、方形、六边形等多种形状的头像展示组件");
        subtitle.setFont(Font.font("Microsoft YaHei", 14));
        subtitle.setTextFill(Color.valueOf("#909399"));

        mainContainer.getChildren().addAll(title, subtitle,
                buildShapeDemo(),
                buildSizeDemo(),
                buildThemeDemo(),
                buildPlaceholderDemo(),
                buildInteractiveDemo());

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F7FA;");

        Scene scene = new Scene(scrollPane, 900, 750);
        primaryStage.setTitle("Avatar 头像组件 - 综合演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** 1. 形状演示 */
    private VBox buildShapeDemo() {
        VBox card = createCard("多种形状", "支持 CIRCLE、SQUARE、HEXAGON_H、HEXAGON_V、DIAMOND、PENTAGON、STAR、ROUNDED_SQUARE 八种形状。");

        FlowPane flow = new FlowPane(20, 20);
        flow.setAlignment(Pos.CENTER_LEFT);

        Avatar circle = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.CIRCLE).size(80);
        Avatar square = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.SQUARE).size(80);
        Avatar squareRound = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.SQUARE).size(80).arcWidth(16).arcHeight(16);
        Avatar hexH = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.HEXAGON_H).size(80);
        Avatar hexV = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.HEXAGON_V).size(80);
        Avatar diamond = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.DIAMOND).size(80);
        Avatar pentagon = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.PENTAGON).size(80);
        Avatar star = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.STAR).size(80);
        Avatar roundedSq = new Avatar(SAMPLE_IMG).avatarShape(AvatarShape.ROUNDED_SQUARE).size(80);

        flow.getChildren().addAll(
                wrapWithLabel(circle, "圆形"),
                wrapWithLabel(square, "方形"),
                wrapWithLabel(squareRound, "圆角方形"),
                wrapWithLabel(hexH, "水平六边形"),
                wrapWithLabel(hexV, "垂直六边形"),
                wrapWithLabel(diamond, "菱形"),
                wrapWithLabel(pentagon, "五边形"),
                wrapWithLabel(star, "五角星"),
                wrapWithLabel(roundedSq, "自动圆角"));

        card.getChildren().add(flow);
        return card;
    }

    /** 2. 尺寸演示 */
    private VBox buildSizeDemo() {
        VBox card = createCard("不同尺寸", "通过 size() 方法设置头像大小。");

        FlowPane flow = new FlowPane(15, 15);
        flow.setAlignment(Pos.CENTER_LEFT);

        int[] sizes = {32, 48, 64, 80, 100, 120};
        for (int s : sizes) {
            Avatar a = new Avatar(SAMPLE_IMG).size(s);
            flow.getChildren().add(wrapWithLabel(a, s + "px"));
        }

        card.getChildren().add(flow);
        return card;
    }

    /** 3. 主题演示 */
    private VBox buildThemeDemo() {
        VBox card = createCard("主题定制", "内置 DEFAULT、DARK、BORDERED、SHADOW 预设主题。");

        FlowPane flow = new FlowPane(20, 20);
        flow.setAlignment(Pos.CENTER_LEFT);

        Avatar def = new Avatar(SAMPLE_IMG).size(80).theme(AvatarTheme.DEFAULT);
        Avatar bordered = new Avatar(SAMPLE_IMG).size(80).theme(AvatarTheme.BORDERED);
        Avatar shadow = new Avatar(SAMPLE_IMG).size(80).theme(AvatarTheme.SHADOW);

        AvatarTheme custom = AvatarTheme.builder()
                .borderColor(Color.valueOf("#F56C6C"))
                .borderWidth(3)
                .shadowRadius(8)
                .shadowOpacity(0.25)
                .build();
        Avatar customAvatar = new Avatar(SAMPLE_IMG).size(80).theme(custom);

        flow.getChildren().addAll(
                wrapWithLabel(def, "默认"),
                wrapWithLabel(bordered, "边框"),
                wrapWithLabel(shadow, "阴影"),
                wrapWithLabel(customAvatar, "自定义"));

        card.getChildren().add(flow);
        return card;
    }

    /** 4. 占位符演示 */
    private VBox buildPlaceholderDemo() {
        VBox card = createCard("占位符", "图片未加载时显示占位文字，支持不同形状和主题。", 900);

        FlowPane flow = new FlowPane(15, 15);
        flow.setAlignment(Pos.CENTER_LEFT);

        // 不同占位符文字
        Avatar a1 = new Avatar().size(64).placeholder("A");
        Avatar a2 = new Avatar().size(64).placeholder("U");
        Avatar a3 = new Avatar().size(64).placeholder("?");

        // 不同形状的占位符
        Avatar a4 = new Avatar().size(64).avatarShape(AvatarShape.SQUARE).arcWidth(12).arcHeight(12).placeholder("S");
        Avatar a5 = new Avatar().size(64).avatarShape(AvatarShape.HEXAGON_H).placeholder("H");
        Avatar a6 = new Avatar().size(64).avatarShape(AvatarShape.DIAMOND).placeholder("D");

        // 深色主题占位符
        Avatar a7 = new Avatar().size(64).theme(AvatarTheme.DARK).placeholder("J");
        Avatar a8 = new Avatar().size(64).theme(AvatarTheme.BORDERED).placeholder("B");

        flow.getChildren().addAll(
                wrapWithLabel(a1, "A"),
                wrapWithLabel(a2, "U"),
                wrapWithLabel(a3, "?"),
                wrapWithLabel(a4, "方形S"),
                wrapWithLabel(a5, "六边形H"),
                wrapWithLabel(a6, "菱形D"),
                wrapWithLabel(a7, "深色J"),
                wrapWithLabel(a8, "边框B"));

        card.getChildren().add(flow);
        return card;
    }

    /** 5. 交互式演示 */
    private VBox buildInteractiveDemo() {
        VBox card = createCard("交互式控制", "动态切换形状、尺寸和图片。");

        Avatar avatar = new Avatar(SAMPLE_IMG).size(100);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        ComboBox<AvatarShape> shapeCombo = new ComboBox<>();
        shapeCombo.getItems().addAll(AvatarShape.values());
        shapeCombo.setValue(AvatarShape.CIRCLE);
        shapeCombo.setOnAction(e -> avatar.setAvatarShape(shapeCombo.getValue()));

        Button switchImgBtn = new Button("切换图片");
        switchImgBtn.setStyle("-fx-background-color: #409EFF; -fx-text-fill: white; -fx-background-radius: 4;");
        final boolean[] toggle = {false};
        switchImgBtn.setOnAction(e -> {
            toggle[0] = !toggle[0];
            avatar.setImage(new Image(toggle[0] ? SAMPLE_IMG2 : SAMPLE_IMG, true));
        });

        controls.getChildren().addAll(new Label("形状:"), shapeCombo, switchImgBtn);
        card.getChildren().addAll(avatar, controls);
        return card;
    }

    // ==================== 辅助方法 ====================

    private VBox wrapWithLabel(Avatar avatar, String text) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(text);
        label.setTextFill(Color.valueOf("#909399"));
        label.setFont(Font.font("Microsoft YaHei", 11));
        box.getChildren().addAll(avatar, label);
        return box;
    }

    private VBox createCard(String title, String description) {
        return createCard(title, description, 800);
    }

    private VBox createCard(String title, String description, double minWidth) {
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
