package io.aurora.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class JavaFXDemoLauncher extends Application {

    private static final String BASE_PACKAGE = "io.aurora.fx";
    private static final String FAVORITE_FILE = "fx_favorites.txt";
    private static final String RECENT_FILE = "fx_recent.txt";

    private TreeView<String> treeView;
    private TextField searchField;

    private List<String> allClasses;
    private Set<String> favorites = new HashSet<>();
    private LinkedList<String> recent = new LinkedList<>();

    @Override
    public void start(Stage stage) {

        loadLocalData();

        allClasses = scanJavaFXApplications(BASE_PACKAGE);

        searchField = new TextField();
        searchField.setPromptText("🔍 搜索类或包...");
        searchField.textProperty().addListener((obs, oldV, newV) -> refreshTree());

        treeView = new TreeView<>();
        treeView.setShowRoot(false);

        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);

                if (empty || val == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                TreeItem<String> item = getTreeItem();

                boolean isLeaf = item != null && item.isLeaf();

                setText((isLeaf ? "☕ " : "📁 ") + val);
            }
        });

        // 双击运行
        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
                if (item != null && item.isLeaf()) {
                    runJavaFXApp(item.getValue());
                }
            }
        });

        // 右键菜单
        treeView.setContextMenu(createContextMenu());

        refreshTree();

        Button sourceBtn = new Button("源码");
        sourceBtn.setOnAction(e -> openSource());

        HBox top = new HBox(10, searchField, sourceBtn);
        top.setPadding(new Insets(10));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        BorderPane root = new BorderPane(treeView);
        root.setTop(top);

        stage.setScene(new Scene(root, 800, 900));
        stage.setTitle("JavaFX Demo Manager 🚀");
        stage.show();
    }

    // ======================
    // 树刷新（核心）
    // ======================
    private void refreshTree() {

        String keyword = searchField.getText().toLowerCase();

        List<String> filtered = allClasses.stream()
                .filter(c -> c.toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        TreeItem<String> root = new TreeItem<>("ROOT");

        // ⭐ 收藏
        if (!favorites.isEmpty()) {
            TreeItem<String> favNode = new TreeItem<>("Favorites");
            favorites.stream().sorted().forEach(f -> {
                if (filtered.contains(f)) {
                    favNode.getChildren().add(new TreeItem<>(f));
                }
            });
            root.getChildren().add(favNode);
        }

        // 🕘 最近
        if (!recent.isEmpty()) {
            TreeItem<String> recNode = new TreeItem<>("Recent");
            recent.forEach(r -> {
                if (filtered.contains(r)) {
                    recNode.getChildren().add(new TreeItem<>(r));
                }
            });
            root.getChildren().add(recNode);
        }

        // 📁 包结构
        Map<String, List<String>> group = filtered.stream()
                .collect(Collectors.groupingBy(c -> c.substring(0, c.lastIndexOf("."))));

        group.keySet().stream().sorted().forEach(pkg -> {
            TreeItem<String> pkgNode = new TreeItem<>(pkg);
            group.get(pkg).stream().sorted()
                    .forEach(cls -> pkgNode.getChildren().add(new TreeItem<>(cls)));
            root.getChildren().add(pkgNode);
        });

        treeView.setRoot(root);
    }

    // ======================
    // 右键菜单
    // ======================
    private ContextMenu createContextMenu() {

        MenuItem fav = new MenuItem("⭐ 收藏/取消收藏");
        fav.setOnAction(e -> {
            TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
            if (item != null && item.isLeaf()) {
                String cls = item.getValue();
                if (favorites.contains(cls)) {
                    favorites.remove(cls);
                } else {
                    favorites.add(cls);
                }
                save(favorites, FAVORITE_FILE);
                refreshTree();
            }
        });

        return new ContextMenu(fav);
    }

    // ======================
    // 运行
    // ======================
    private void runJavaFXApp(String cls) {
        try {
            Class<?> clazz = Class.forName(cls);

            Platform.runLater(() -> {
                try {
                    Application app = (Application) clazz.getDeclaredConstructor().newInstance();
                    app.start(new Stage());

                    recent.remove(cls);
                    recent.addFirst(cls);
                    if (recent.size() > 10) recent.removeLast();

                    save(recent, RECENT_FILE);

                    refreshTree();

                } catch (Exception e) {
                    showAlert("运行失败", e.getMessage());
                }
            });

        } catch (Exception e) {
            showAlert("错误", e.getMessage());
        }
    }

    // ======================
    // 源码
    // ======================
    private void openSource() {

        TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();

        if (item == null || !item.isLeaf()) {
            showAlert("提示", "请选择类");
            return;
        }

        try {
            String cls = item.getValue();
            String path = cls.replace('.', '/') + ".java";
            File file = new File(System.getProperty("user.dir"), "src/main/java/" + path);

            String code = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            TextArea area = new TextArea(code);
            area.setStyle("-fx-font-family: Consolas;");

            Stage s = new Stage();
            s.setScene(new Scene(new BorderPane(area), 900, 800));
            s.setTitle(cls);
            s.show();

        } catch (Exception e) {
            showAlert("错误", e.getMessage());
        }
    }

    // ======================
    // 扫描（不变）
    // ======================
    private List<String> scanJavaFXApplications(String basePackage) {
        try {
            String path = basePackage.replace('.', '/');
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

            List<String> list = new ArrayList<>();

            while (resources.hasMoreElements()) {
                File dir = new File(URLDecoder.decode(resources.nextElement().getFile(), "UTF-8"));
                list.addAll(find(dir, basePackage));
            }

            return list.stream()
                    .filter(c -> {
                        try {
                            return Application.class.isAssignableFrom(Class.forName(c));
                        } catch (Exception e) {
                            return false;
                        }
                    }).collect(Collectors.toList());

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<String> find(File dir, String pkg) {
        List<String> list = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return list;

        for (File f : files) {
            if (f.isDirectory()) {
                list.addAll(find(f, pkg + "." + f.getName()));
            } else if (f.getName().endsWith(".class")) {
                list.add(pkg + "." + f.getName().replace(".class", ""));
            }
        }
        return list;
    }

    // ======================
    // 本地存储
    // ======================
    private void loadLocalData() {
        favorites.addAll(load(FAVORITE_FILE));
        recent.addAll(load(RECENT_FILE));
    }

    private List<String> load(String file) {
        try {
            File f = new File(file);
            if (!f.exists()) return new ArrayList<>();
            return java.nio.file.Files.readAllLines(f.toPath());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void save(Collection<String> data, String file) {
        try {
            java.nio.file.Files.write(new File(file).toPath(), data);
        } catch (Exception ignored) {}
    }

    private void showAlert(String t, String m) {
        new Alert(Alert.AlertType.ERROR, m).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}