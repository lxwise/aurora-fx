package io.aurora.fx.theme;

/**
 * Windows 11 深色主题
 * <p>
 * 基于 Microsoft Fluent Design System 设计语言，呈现 Windows 11 深色模式的现代化 UI 外观。
 * 特点包括圆角边框、柔和阴影、半透明效果以及符合 Windows 11 深色模式的色彩搭配。
 * </p>
 *
 * <h3>设计特征</h3>
 * <ul>
 *     <li>圆角控件边框（4-8px 圆角半径）</li>
 *     <li>柔和阴影与深度层次</li>
 *     <li>Windows 11 深色色彩系统（Accent: #60CDFF）</li>
 *     <li>Segoe UI 字体族</li>
 *     <li>Mica / Acrylic 风格的半透明背景效果</li>
 * </ul>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * Application.setUserAgentStylesheet(new Windows11Dark().getUserAgentStylesheet());
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 * @see Windows11Light
 */
public class Windows11Dark extends AbstractOSTheme {

    /** 主题名称 */
    public static final String NAME = "Windows 11 Dark";

    /** CSS 样式表资源路径 */
    public static final String STYLESHEET = Windows11Dark.class.getResource(
            "windows11-dark.css").toExternalForm();

    /**
     * 创建 Windows 11 深色主题实例
     */
    public Windows11Dark() {
        super(NAME, true, STYLESHEET);
    }
}
