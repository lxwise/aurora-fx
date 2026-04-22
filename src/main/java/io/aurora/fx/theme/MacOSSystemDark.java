package io.aurora.fx.theme;

/**
 * macOS 系统深色主题（MacOSDark 的别名）
 * <p>
 * 此类是 {@link MacOSDark} 的语义别名，提供与用户需求中对齐的命名方式。
 * 功能和行为与 {@link MacOSDark} 完全一致。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * Application.setUserAgentStylesheet(new MacOSSystemDark().getUserAgentStylesheet());
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 * @see MacOSDark
 */
public class MacOSSystemDark extends MacOSDark {

    /** 主题名称 */
    public static final String NAME = "macOS System Dark";

    /**
     * 创建 macOS 系统深色主题实例
     */
    public MacOSSystemDark() {
        // 委托给父类 MacOSDark 的实现
    }

    @Override
    public String getName() {
        return NAME;
    }
}
