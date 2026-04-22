package io.aurora.fx.theme;

import atlantafx.base.theme.Theme;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作系统主题工厂
 * <p>
 * 提供便捷的工厂方法来创建和查询所有可用的操作系统风格主题。
 * 支持按名称查找主题、获取浅色/深色主题列表以及自动检测当前操作系统的推荐主题。
 * </p>
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 获取所有可用主题
 * List<Theme> themes = OSThemeFactory.allThemes();
 *
 * // 获取当前操作系统的推荐浅色主题
 * Theme recommended = OSThemeFactory.recommendedLightTheme();
 *
 * // 按名称查找主题
 * Theme theme = OSThemeFactory.forName("Windows 11 Light");
 *
 * // 应用推荐主题
 * Application.setUserAgentStylesheet(
 *     OSThemeFactory.recommendedTheme().getUserAgentStylesheet()
 * );
 * }</pre>
 *
 * @author Aurora-FX
 * @version 1.0
 */
public final class OSThemeFactory {

    private OSThemeFactory() {
        // 工具类，禁止实例化
    }

    // ==================== 主题实例缓存 ====================

    private static final Windows11Light WINDOWS_11_LIGHT = new Windows11Light();
    private static final Windows11Dark WINDOWS_11_DARK = new Windows11Dark();
    private static final MacOSLight MAC_OS_LIGHT = new MacOSLight();
    private static final MacOSDark MAC_OS_DARK = new MacOSDark();
    private static final MacOSSystemDark MAC_OS_SYSTEM_DARK = new MacOSSystemDark();

    // ==================== 主题列表 ====================

    /** 所有可用的操作系统风格主题 */
    private static final List<Theme> ALL_THEMES = List.of(
            WINDOWS_11_LIGHT,
            WINDOWS_11_DARK,
            MAC_OS_LIGHT,
            MAC_OS_DARK,
            MAC_OS_SYSTEM_DARK
    );

    /** 所有浅色主题 */
    private static final List<Theme> LIGHT_THEMES = ALL_THEMES.stream()
            .filter(t -> !t.isDarkMode())
            .collect(Collectors.toUnmodifiableList());

    /** 所有深色主题 */
    private static final List<Theme> DARK_THEMES = ALL_THEMES.stream()
            .filter(Theme::isDarkMode)
            .collect(Collectors.toUnmodifiableList());

    // ==================== 工厂方法 ====================

    /**
     * 获取所有可用的操作系统风格主题
     *
     * @return 不可变的主题列表
     */
    public static List<Theme> allThemes() {
        return ALL_THEMES;
    }

    /**
     * 获取所有浅色主题
     *
     * @return 不可变的浅色主题列表
     */
    public static List<Theme> lightThemes() {
        return LIGHT_THEMES;
    }

    /**
     * 获取所有深色主题
     *
     * @return 不可变的深色主题列表
     */
    public static List<Theme> darkThemes() {
        return DARK_THEMES;
    }

    /**
     * 根据主题名称查找主题
     *
     * @param name 主题名称，不能为 null
     * @return 匹配的主题，如果未找到则返回 null
     * @throws NullPointerException 如果 name 为 null
     */
    public static Theme forName(String name) {
        Objects.requireNonNull(name, "主题名称不能为 null");
        return ALL_THEMES.stream()
                .filter(t -> name.equals(t.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取 Windows 11 浅色主题实例
     *
     * @return {@link Windows11Light} 实例
     */
    public static Windows11Light windows11Light() {
        return WINDOWS_11_LIGHT;
    }

    /**
     * 获取 Windows 11 深色主题实例
     *
     * @return {@link Windows11Dark} 实例
     */
    public static Windows11Dark windows11Dark() {
        return WINDOWS_11_DARK;
    }

    /**
     * 获取 macOS 浅色主题实例
     *
     * @return {@link MacOSLight} 实例
     */
    public static MacOSLight macOSLight() {
        return MAC_OS_LIGHT;
    }

    /**
     * 获取 macOS 深色主题实例
     *
     * @return {@link MacOSDark} 实例
     */
    public static MacOSDark macOSDark() {
        return MAC_OS_DARK;
    }

    // ==================== 自动检测 ====================

    /**
     * 根据当前操作系统自动推荐浅色主题
     * <p>
     * 在 Windows 系统上返回 Windows 11 浅色主题，
     * 在 macOS 系统上返回 macOS 浅色主题，
     * 在其他系统上默认返回 Windows 11 浅色主题。
     * </p>
     *
     * @return 推荐的浅色主题
     */
    public static Theme recommendedLightTheme() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            return MAC_OS_LIGHT;
        }
        return WINDOWS_11_LIGHT;
    }

    /**
     * 根据当前操作系统自动推荐深色主题
     * <p>
     * 在 Windows 系统上返回 Windows 11 深色主题，
     * 在 macOS 系统上返回 macOS 深色主题，
     * 在其他系统上默认返回 Windows 11 深色主题。
     * </p>
     *
     * @return 推荐的深色主题
     */
    public static Theme recommendedDarkTheme() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            return MAC_OS_DARK;
        }
        return WINDOWS_11_DARK;
    }

    /**
     * 根据当前操作系统和系统外观设置自动推荐主题
     * <p>
     * 自动检测操作系统类型，并尝试检测系统是否处于深色模式。
     * 如果无法检测，默认返回浅色主题。
     * </p>
     *
     * @return 推荐的主题
     */
    public static Theme recommendedTheme() {
        // 尝试检测系统深色模式偏好
        boolean darkPreferred = isSystemDarkMode();
        return darkPreferred ? recommendedDarkTheme() : recommendedLightTheme();
    }

    /**
     * 检测系统是否处于深色模式
     * <p>
     * 此方法尝试通过 JavaFX Platform Preferences API 检测系统深色模式偏好。
     * 如果无法检测，默认返回 {@code false}。
     * </p>
     *
     * @return 如果系统偏好深色模式则返回 {@code true}
     */
    private static boolean isSystemDarkMode() {
        try {
            // 尝试使用 JavaFX Platform Preferences API（JavaFX 17+）
            var platformPrefs = Class.forName("javafx.application.Platform$Preferences");
            var getUserPrefs = platformPrefs.getMethod("getUserPreferences");
            var prefs = getUserPrefs.invoke(null);
            if (prefs != null) {
                var getBoolean = prefs.getClass().getMethod("getBoolean", String.class, boolean.class);
                return (boolean) getBoolean.invoke(prefs, "colorScheme.dark", false);
            }
        } catch (Exception ignored) {
            // 回退方案：不支持自动检测
        }
        return false;
    }
}
