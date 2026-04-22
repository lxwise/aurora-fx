package io.aurora.fx.theme;

/**
 * macOS 深色主题
 * <p>
 * 基于 Apple San Francisco / Human Interface Guidelines 设计语言，呈现 macOS 深色模式的简洁优雅 UI 外观。
 * 特点包括圆润边角、半透明毛玻璃效果、精致阴影以及符合 macOS 深色模式的色彩搭配。
 * </p>
 *
 * <h3>设计特征</h3>
 * <ul>
 *     <li>圆润边角（6-10px 圆角半径）</li>
 *     <li>精致阴影与深度层次</li>
 *     <li>macOS 深色色彩系统（Accent: #0A84FF）</li>
 *     <li>SF Pro / -apple-system 字体族</li>
 *     <li>Vibrancy 风格的半透明背景效果</li>
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * Application.setUserAgentStylesheet(new MacOSDark().getUserAgentStylesheet());
 * // 或者使用别名
 * Application.setUserAgentStylesheet(new MacOSSystemDark().getUserAgentStylesheet());
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 * @see MacOSLight
 */
public class MacOSDark extends AbstractOSTheme {

    /** 主题名称 */
    public static final String NAME = "macOS Dark";

    /** CSS 样式表资源路径 */
    public static final String STYLESHEET = MacOSDark.class.getResource(
            "macos-dark.css").toExternalForm();

    /**
     * 创建 macOS 深色主题实例
     */
    public MacOSDark() {
        super(NAME, true, STYLESHEET);
    }
}
