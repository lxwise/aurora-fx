package io.aurora.fx.theme;

/**
 * macOS 浅色主题
 * <p>
 * 基于 Apple San Francisco / Human Interface Guidelines 设计语言，呈现 macOS 风格的简洁优雅 UI 外观。
 * 特点包括圆润边角、半透明毛玻璃效果、精致阴影以及符合 macOS 视觉规范的色彩搭配。
 * </p>
 *
 * <h3>设计特征</h3>
 * <ul>
 *     <li>圆润边角（6-10px 圆角半径）</li>
 *     <li>精致阴影与深度层次</li>
 *     <li>macOS 标准色彩系统（Accent: #007AFF）</li>
 *     <li>SF Pro / -apple-system 字体族</li>
 *     <li>Vibrancy 风格的半透明背景效果</li>
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * Application.setUserAgentStylesheet(new MacOSLight().getUserAgentStylesheet());
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 * @see MacOSDark
 */
public class MacOSLight extends AbstractOSTheme {

    /** 主题名称 */
    public static final String NAME = "macOS Light";

    /** CSS 样式表资源路径 */
    public static final String STYLESHEET = MacOSLight.class.getResource(
            "macos-light.css").toExternalForm();

    /**
     * 创建 macOS 浅色主题实例
     */
    public MacOSLight() {
        super(NAME, false, STYLESHEET);
    }
}
