package io.aurora.fx.theme;

import atlantafx.base.theme.Theme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 操作系统主题测试类
 * <p>
 * 验证所有主题类的基本功能，包括主题接口实现、名称、深浅色模式、
 * CSS 样式表路径以及工厂方法的正确性。
 * </p>
 *
 * @author Aurora-FX
 * @version 1.0
 */
@DisplayName("操作系统主题测试")
class OSThemeTest {

    // ==================== Windows 11 主题测试 ====================

    @Nested
    @DisplayName("Windows 11 Light 主题")
    class Windows11LightTest {

        @Test
        @DisplayName("应该正确实现 Theme 接口")
        void shouldImplementThemeInterface() {
            Windows11Light theme = new Windows11Light();
            assertInstanceOf(Theme.class, theme, "Windows11Light 应实现 Theme 接口");
        }

        @Test
        @DisplayName("应该返回正确的主题名称")
        void shouldReturnCorrectName() {
            Windows11Light theme = new Windows11Light();
            assertEquals("Windows 11 Light", theme.getName());
        }

        @Test
        @DisplayName("应该是浅色模式")
        void shouldBeLightMode() {
            Windows11Light theme = new Windows11Light();
            assertFalse(theme.isDarkMode(), "Windows11Light 不应是深色模式");
        }

        @Test
        @DisplayName("应该返回有效的样式表路径")
        void shouldReturnValidStylesheet() {
            Windows11Light theme = new Windows11Light();
            String stylesheet = theme.getUserAgentStylesheet();
            assertNotNull(stylesheet, "样式表路径不应为 null");
            assertFalse(stylesheet.isBlank(), "样式表路径不应为空");
            assertTrue(stylesheet.contains("windows11-light.css"),
                    "样式表路径应包含 windows11-light.css");
        }
    }

    @Nested
    @DisplayName("Windows 11 Dark 主题")
    class Windows11DarkTest {

        @Test
        @DisplayName("应该是深色模式")
        void shouldBeDarkMode() {
            Windows11Dark theme = new Windows11Dark();
            assertTrue(theme.isDarkMode(), "Windows11Dark 应是深色模式");
        }

        @Test
        @DisplayName("应该返回正确的主题名称")
        void shouldReturnCorrectName() {
            Windows11Dark theme = new Windows11Dark();
            assertEquals("Windows 11 Dark", theme.getName());
        }

        @Test
        @DisplayName("应该返回有效的样式表路径")
        void shouldReturnValidStylesheet() {
            Windows11Dark theme = new Windows11Dark();
            String stylesheet = theme.getUserAgentStylesheet();
            assertNotNull(stylesheet, "样式表路径不应为 null");
            assertTrue(stylesheet.contains("windows11-dark.css"),
                    "样式表路径应包含 windows11-dark.css");
        }
    }

    // ==================== macOS 主题测试 ====================

    @Nested
    @DisplayName("macOS Light 主题")
    class MacOSLightTest {

        @Test
        @DisplayName("应该正确实现 Theme 接口")
        void shouldImplementThemeInterface() {
            MacOSLight theme = new MacOSLight();
            assertInstanceOf(Theme.class, theme, "MacOSLight 应实现 Theme 接口");
        }

        @Test
        @DisplayName("应该是浅色模式")
        void shouldBeLightMode() {
            MacOSLight theme = new MacOSLight();
            assertFalse(theme.isDarkMode(), "MacOSLight 不应是深色模式");
        }

        @Test
        @DisplayName("应该返回正确的主题名称")
        void shouldReturnCorrectName() {
            MacOSLight theme = new MacOSLight();
            assertEquals("macOS Light", theme.getName());
        }

        @Test
        @DisplayName("应该返回有效的样式表路径")
        void shouldReturnValidStylesheet() {
            MacOSLight theme = new MacOSLight();
            String stylesheet = theme.getUserAgentStylesheet();
            assertNotNull(stylesheet, "样式表路径不应为 null");
            assertTrue(stylesheet.contains("macos-light.css"),
                    "样式表路径应包含 macos-light.css");
        }
    }

    @Nested
    @DisplayName("macOS Dark 主题")
    class MacOSDarkTest {

        @Test
        @DisplayName("应该是深色模式")
        void shouldBeDarkMode() {
            MacOSDark theme = new MacOSDark();
            assertTrue(theme.isDarkMode(), "MacOSDark 应是深色模式");
        }

        @Test
        @DisplayName("应该返回正确的主题名称")
        void shouldReturnCorrectName() {
            MacOSDark theme = new MacOSDark();
            assertEquals("macOS Dark", theme.getName());
        }

        @Test
        @DisplayName("应该返回有效的样式表路径")
        void shouldReturnValidStylesheet() {
            MacOSDark theme = new MacOSDark();
            String stylesheet = theme.getUserAgentStylesheet();
            assertNotNull(stylesheet, "样式表路径不应为 null");
            assertTrue(stylesheet.contains("macos-dark.css"),
                    "样式表路径应包含 macos-dark.css");
        }
    }

    @Nested
    @DisplayName("MacOSSystemDark 主题")
    class MacOSSystemDarkTest {

        @Test
        @DisplayName("应该是深色模式")
        void shouldBeDarkMode() {
            MacOSSystemDark theme = new MacOSSystemDark();
            assertTrue(theme.isDarkMode(), "MacOSSystemDark 应是深色模式");
        }

        @Test
        @DisplayName("应该返回正确的主题名称")
        void shouldReturnCorrectName() {
            MacOSSystemDark theme = new MacOSSystemDark();
            assertEquals("macOS System Dark", theme.getName());
        }

        @Test
        @DisplayName("应该使用与 MacOSDark 相同的样式表")
        void shouldUseSameStylesheetAsMacOSDark() {
            MacOSSystemDark systemDark = new MacOSSystemDark();
            MacOSDark macOSDark = new MacOSDark();
            assertEquals(macOSDark.getUserAgentStylesheet(),
                    systemDark.getUserAgentStylesheet(),
                    "MacOSSystemDark 应使用与 MacOSDark 相同的样式表");
        }
    }

    // ==================== 抽象基类测试 ====================

    @Nested
    @DisplayName("AbstractOSTheme 基类")
    class AbstractOSThemeTest {

        @Test
        @DisplayName("构造器应该拒绝空名称")
        void shouldRejectBlankName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TestTheme("", false, "test.css"),
                    "应拒绝空名称");
            assertThrows(IllegalArgumentException.class,
                    () -> new TestTheme(null, false, "test.css"),
                    "应拒绝 null 名称");
        }

        @Test
        @DisplayName("构造器应该拒绝空样式表路径")
        void shouldRejectBlankStylesheet() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TestTheme("Test", false, ""),
                    "应拒绝空样式表路径");
            assertThrows(IllegalArgumentException.class,
                    () -> new TestTheme("Test", false, null),
                    "应拒绝 null 样式表路径");
        }

        @Test
        @DisplayName("toString 应包含主题信息")
        void toStringShouldContainThemeInfo() {
            Windows11Light theme = new Windows11Light();
            String str = theme.toString();
            assertTrue(str.contains("Windows 11 Light"), "toString 应包含主题名称");
            assertTrue(str.contains("false"), "toString 应包含深浅色模式信息");
        }

        /**
         * 用于测试抽象基类的具体实现
         */
        private static class TestTheme extends AbstractOSTheme {
            TestTheme(String name, boolean darkMode, String stylesheet) {
                super(name, darkMode, stylesheet);
            }
        }
    }

    // ==================== 工厂类测试 ====================

    @Nested
    @DisplayName("OSThemeFactory 工厂类")
    class OSThemeFactoryTest {

        @Test
        @DisplayName("应该返回 5 个主题")
        void shouldReturnFiveThemes() {
            assertEquals(5, OSThemeFactory.allThemes().size(),
                    "应有 5 个主题可用");
        }

        @Test
        @DisplayName("应该返回 2 个浅色主题")
        void shouldReturnTwoLightThemes() {
            assertEquals(2, OSThemeFactory.lightThemes().size(),
                    "应有 2 个浅色主题");
        }

        @Test
        @DisplayName("应该返回 3 个深色主题")
        void shouldReturnThreeDarkThemes() {
            assertEquals(3, OSThemeFactory.darkThemes().size(),
                    "应有 3 个深色主题");
        }

        @Test
        @DisplayName("应该按名称查找主题")
        void shouldFindThemeByName() {
            Theme theme = OSThemeFactory.forName("Windows 11 Light");
            assertNotNull(theme, "应能找到 Windows 11 Light 主题");
            assertEquals("Windows 11 Light", theme.getName());
        }

        @Test
        @DisplayName("未找到名称时应返回 null")
        void shouldReturnNullForUnknownName() {
            Theme theme = OSThemeFactory.forName("不存在的主题");
            assertNull(theme, "不存在的主题名称应返回 null");
        }

        @Test
        @DisplayName("forName 不应接受 null")
        void shouldRejectNullName() {
            assertThrows(NullPointerException.class,
                    () -> OSThemeFactory.forName(null),
                    "forName 不应接受 null");
        }

        @Test
        @DisplayName("工厂方法应返回正确的主题实例")
        void shouldReturnCorrectThemeInstances() {
            assertInstanceOf(Windows11Light.class, OSThemeFactory.windows11Light());
            assertInstanceOf(Windows11Dark.class, OSThemeFactory.windows11Dark());
            assertInstanceOf(MacOSLight.class, OSThemeFactory.macOSLight());
            assertInstanceOf(MacOSDark.class, OSThemeFactory.macOSDark());
        }

        @Test
        @DisplayName("推荐浅色主题不应为 null")
        void recommendedLightThemeShouldNotBeNull() {
            assertNotNull(OSThemeFactory.recommendedLightTheme(),
                    "推荐的浅色主题不应为 null");
        }

        @Test
        @DisplayName("推荐深色主题不应为 null")
        void recommendedDarkThemeShouldNotBeNull() {
            assertNotNull(OSThemeFactory.recommendedDarkTheme(),
                    "推荐的深色主题不应为 null");
        }

        @Test
        @DisplayName("自动推荐主题不应为 null")
        void recommendedThemeShouldNotBeNull() {
            assertNotNull(OSThemeFactory.recommendedTheme(),
                    "自动推荐主题不应为 null");
        }

        @Test
        @DisplayName("浅色主题列表应全部为浅色")
        void lightThemesShouldAllBeLight() {
            assertTrue(OSThemeFactory.lightThemes().stream()
                    .noneMatch(Theme::isDarkMode), "浅色主题列表中不应有深色主题");
        }

        @Test
        @DisplayName("深色主题列表应全部为深色")
        void darkThemesShouldAllBeDark() {
            assertTrue(OSThemeFactory.darkThemes().stream()
                    .allMatch(Theme::isDarkMode), "深色主题列表中应全部为深色主题");
        }

        @Test
        @DisplayName("所有主题的样式表路径应非空且唯一")
        void allStylesheetsShouldBeNonEmptyAndUnique() {
            var stylesheets = OSThemeFactory.allThemes().stream()
                    .map(Theme::getUserAgentStylesheet)
                    .toList();
            assertTrue(stylesheets.stream().noneMatch(s -> s == null || s.isBlank()),
                    "所有主题的样式表路径应非空");
            assertEquals(stylesheets.size(),
                    stylesheets.stream().distinct().count() + 1,
                    "样式表路径应基本唯一（MacOSSystemDark 与 MacOSDark 共享）");
        }
    }

    // ==================== 主题切换一致性测试 ====================

    @Nested
    @DisplayName("主题一致性")
    class ThemeConsistencyTest {

        @Test
        @DisplayName("Windows 11 浅色与深色主题样式表应不同")
        void win11LightAndDarkShouldDiffer() {
            Windows11Light light = new Windows11Light();
            Windows11Dark dark = new Windows11Dark();
            assertNotEquals(light.getUserAgentStylesheet(), dark.getUserAgentStylesheet(),
                    "Windows 11 浅色与深色应使用不同样式表");
        }

        @Test
        @DisplayName("macOS 浅色与深色主题样式表应不同")
        void macOSLightAndDarkShouldDiffer() {
            MacOSLight light = new MacOSLight();
            MacOSDark dark = new MacOSDark();
            assertNotEquals(light.getUserAgentStylesheet(), dark.getUserAgentStylesheet(),
                    "macOS 浅色与深色应使用不同样式表");
        }

        @Test
        @DisplayName("Windows 11 与 macOS 浅色主题样式表应不同")
        void win11AndMacOSLightShouldDiffer() {
            Windows11Light win = new Windows11Light();
            MacOSLight mac = new MacOSLight();
            assertNotEquals(win.getUserAgentStylesheet(), mac.getUserAgentStylesheet(),
                    "Windows 11 与 macOS 浅色主题应使用不同样式表");
        }
    }
}
