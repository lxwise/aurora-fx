package io.aurora.fx.theme;

import atlantafx.base.theme.Theme;

/**
 * 操作系统风格主题的抽象基类
 * <p>
 * 提供了 AtlantaFX {@link Theme} 接口的基础实现，所有操作系统风格主题
 * （如 Windows 11、macOS）均继承此抽象类。子类只需指定主题名称、CSS 样式表路径
 * 以及是否为深色模式即可完成主题定义。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 使用 Windows 11 浅色主题
 * Application.setUserAgentStylesheet(new Windows11Light().getUserAgentStylesheet());
 *
 * // 使用 macOS 深色主题
 * Application.setUserAgentStylesheet(new MacOSDark().getUserAgentStylesheet());
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 * @see Theme
 * @see Windows11Light
 * @see Windows11Dark
 * @see MacOSLight
 * @see MacOSDark
 */
public abstract class AbstractOSTheme implements Theme {

    /** 主题名称 */
    private final String name;

    /** 是否为深色模式 */
    private final boolean darkMode;

    /** CSS 样式表资源路径 */
    private final String stylesheet;

    /**
     * 构造一个操作系统风格主题
     *
     * @param name       主题名称，不能为 null 或空字符串
     * @param darkMode   是否为深色模式
     * @param stylesheet CSS 样式表资源路径，不能为 null 或空字符串
     * @throws IllegalArgumentException 如果 name 或 stylesheet 为 null 或空字符串
     */
    protected AbstractOSTheme(String name, boolean darkMode, String stylesheet) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("主题名称不能为空");
        }
        if (stylesheet == null || stylesheet.isBlank()) {
            throw new IllegalArgumentException("样式表路径不能为空");
        }
        this.name = name;
        this.darkMode = darkMode;
        this.stylesheet = stylesheet;
    }

    /**
     * 返回主题的用户代理样式表路径
     * <p>
     * 该路径指向类路径中的 CSS 资源文件，用于通过
     * {@code Application.setUserAgentStylesheet()} 设置 JavaFX 全局主题。
     * </p>
     *
     * @return CSS 样式表路径字符串
     */
    @Override
    public String getUserAgentStylesheet() {
        return stylesheet;
    }

    /**
     * 返回主题名称
     *
     * @return 主题名称字符串
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 判断是否为深色模式主题
     *
     * @return 如果是深色模式则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean isDarkMode() {
        return darkMode;
    }

    /**
     * 返回主题的二进制样式表（BSS 格式）路径
     * <p>
     * BSS 是 JavaFX 的二进制 CSS 格式，加载速度比纯 CSS 更快。
     * 当前实现返回 {@code null}，表示不提供预编译的 BSS 文件，
     * JavaFX 将自动使用 {@link #getUserAgentStylesheet()} 返回的 CSS 文件。
     * </p>
     * <p>
     * 如需启用 BSS 格式以获得更快的加载速度，可以使用 AtlantaFX 提供的
     * {@link atlantafx.base.theme.ThemeCompiler} 工具将 CSS 文件编译为 BSS 格式，
     * 并重写此方法返回 BSS 文件路径。
     * </p>
     *
     * @return BSS 样式表路径字符串，当前返回 {@code null}
     */
    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public String toString() {
        return "OSTheme{" +
                "name='" + name + '\'' +
                ", darkMode=" + darkMode +
                ", stylesheet='" + stylesheet + '\'' +
                '}';
    }
}
