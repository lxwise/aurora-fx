package io.aurora.fx.common.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.shape.Rectangle;

import java.util.concurrent.Callable;

/**
 * @author lstar
 * @create 2025-02
 * @description: 剪贴板工具类
 */
public class ClipboardUtils {
    /**
     * 复制文本到剪贴板
     * @param text 要复制的文本
     */
    public static void copy(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     * 从剪贴板获取文本内容
     * @return 剪贴板中的文本内容，若没有内容则返回空字符串
     */
    public static String paste() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            return clipboard.getString();
        }
        return "";  // 如果剪贴板没有内容，则返回空字符串
    }


    /**
     * 矩形裁剪
     *
     * @param node    ：裁剪区域
     * @param bindArc ：裁剪圆角
     * @return Rectangle
     */
    public static Rectangle clipRect(Node node, DoubleProperty bindArc) {
        Rectangle rectangle = new Rectangle();
        //rectangle.setSmooth(false);
        rectangle.widthProperty().bind(Bindings
                .createObjectBinding((Callable<Number>) () ->
                        node.getLayoutBounds().getWidth(), node.layoutBoundsProperty()));
        rectangle.heightProperty().bind(Bindings
                .createObjectBinding((Callable<Number>) () ->
                        node.getLayoutBounds().getHeight(), node.layoutBoundsProperty()));
        rectangle.arcWidthProperty().bind(bindArc);
        rectangle.arcHeightProperty().bind(bindArc);
        node.setClip(rectangle);
        return rectangle;
    }
}
