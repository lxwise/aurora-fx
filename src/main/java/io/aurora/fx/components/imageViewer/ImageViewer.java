package io.aurora.fx.components.imageViewer;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

/**
 * @author lstar
 * @create 2025-02
 * @description: 仿Viewer.js 图片预览组件不支持gif(动态图)
 */
public class ImageViewer extends Control {

    public ImageViewer(ObservableList<Image> images) {
        setImages(images);
    }

    private final ListProperty<Image> images = new SimpleListProperty<>();

    public ObservableList<Image> getImages() {
        return images.get();
    }

    public ListProperty<Image> imagesProperty() {
        return images;
    }

    public void setImages(ObservableList<Image> images) {
        this.images.set(images);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ImageViewerSkin(this);
    }
}