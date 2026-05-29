package io.aurora.fx.components.tour;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tour 组件核心模型单元测试
 * <p>
 * 仅测试纯模型/逻辑层（不依赖 JavaFX 渲染线程）。Tour 显示与布局相关行为
 * 由 {@link TourPaneDemo} 演示验证。
 * </p>
 *
 * @author Tour Component
 * @version 1.0
 */
public class TourTest {

    /**
     * 在执行任何用例前启动一次 JavaFX Toolkit，避免创建 {@link javafx.scene.Node}
     * 时触发 {@code Control.<clinit>} → {@code PlatformImpl.runLater}
     * 的链式初始化导致 {@code Toolkit not initialized} 异常。
     */
    @BeforeAll
    static void initToolkit() throws InterruptedException {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(5, TimeUnit.SECONDS);
        } catch (IllegalStateException ignored) {
            // Toolkit 已经启动
        }
    }

    // ==================== TourPlacement ====================

    @Test
    void placement_axisDetection() {
        assertTrue(TourPlacement.TOP.isTop());
        assertTrue(TourPlacement.TOP_START.isTop());
        assertTrue(TourPlacement.TOP_END.isTop());
        assertTrue(TourPlacement.BOTTOM.isBottom());
        assertTrue(TourPlacement.LEFT.isLeft());
        assertTrue(TourPlacement.RIGHT.isRight());
        assertTrue(TourPlacement.TOP.isHorizontalAxis());
        assertFalse(TourPlacement.LEFT.isHorizontalAxis());
    }

    @Test
    void placement_fromValue() {
        assertEquals(TourPlacement.BOTTOM, TourPlacement.fromValue("bottom"));
        assertEquals(TourPlacement.TOP_START, TourPlacement.fromValue("top-start"));
        assertEquals(TourPlacement.RIGHT_END, TourPlacement.fromValue("right-end"));
        assertEquals(TourPlacement.BOTTOM, TourPlacement.fromValue(null));
        assertEquals(TourPlacement.BOTTOM, TourPlacement.fromValue("unknown"));
    }

    // ==================== TourTarget ====================

    @Test
    void target_emptyAndRect() {
        TourTarget empty = TourTarget.empty();
        assertTrue(empty.isEmpty());
        assertFalse(empty.isNodeBased());
        assertNull(empty.resolveSceneBounds());

        TourTarget rect = TourTarget.of(10, 20, 100, 50);
        assertFalse(rect.isEmpty());
        Rectangle2D b = rect.resolveSceneBounds();
        assertNotNull(b);
        assertEquals(10, b.getMinX());
        assertEquals(50, b.getHeight());
    }

    @Test
    void target_nodeWithoutScene_returnsNull() {
        // 使用 Region 子类 Pane，避免触发 Control 的静态初始化（需 JavaFX Toolkit）
        Pane node = new Pane();
        TourTarget t = TourTarget.of(node);
        assertTrue(t.isNodeBased());
        // 节点未挂载 Scene，应返回 null（视为空目标）
        assertNull(t.resolveSceneBounds());
    }

    // ==================== TourMaskConfig ====================

    @Test
    void maskConfig_defaults() {
        TourMaskConfig cfg = TourMaskConfig.DEFAULT;
        assertEquals(0.5, cfg.getOpacity(), 0.001);
        assertEquals(4, cfg.getPadding(), 0.001);
        assertFalse(cfg.isHighlight());
        assertFalse(cfg.isDismissOnMaskClick());
    }

    @Test
    void maskConfig_builderClampsOpacity() {
        TourMaskConfig over = TourMaskConfig.builder().opacity(2.5).build();
        assertEquals(1.0, over.getOpacity(), 0.001);
        TourMaskConfig under = TourMaskConfig.builder().opacity(-1).build();
        assertEquals(0.0, under.getOpacity(), 0.001);
    }

    @Test
    void maskConfig_builder_chain() {
        TourMaskConfig cfg = TourMaskConfig.builder()
                .color(Color.BLACK)
                .opacity(0.6)
                .padding(8)
                .cornerRadius(10)
                .highlight(true)
                .highlightWidth(3)
                .dismissOnMaskClick(true)
                .build();
        assertEquals(0.6, cfg.getOpacity(), 0.001);
        assertEquals(8, cfg.getPadding(), 0.001);
        assertEquals(10, cfg.getCornerRadius(), 0.001);
        assertTrue(cfg.isHighlight());
        assertTrue(cfg.isDismissOnMaskClick());
    }

    // ==================== TourTheme ====================

    @Test
    void theme_presetsNotNull() {
        assertNotNull(TourTheme.DEFAULT);
        assertNotNull(TourTheme.DARK);
        assertNotNull(TourTheme.PRIMARY_BLUE);
        assertNotNull(TourTheme.PRIMARY_GREEN);
    }

    @Test
    void theme_toCssColor() {
        String css = TourTheme.toCssColor(Color.web("#409EFF"));
        assertTrue(css.startsWith("rgba("));
        assertEquals("transparent", TourTheme.toCssColor(null));
    }

    @Test
    void theme_builder_overrides() {
        TourTheme t = TourTheme.builder()
                .primaryColor(Color.RED)
                .popupBackground(Color.web("#222"))
                .titleFontSize(20)
                .build();
        assertEquals(Color.RED, t.getPrimaryColor());
        assertEquals(20, t.getTitleFontSize(), 0.001);
        // popupBackground 同步更新 arrowFill
        assertEquals(t.getPopupBackground(), t.getArrowFillColor());
    }

    // ==================== TourStep ====================

    @Test
    void step_chainingProperties() {
        TourStep s = new TourStep()
                .title("title")
                .description("desc")
                .placement(TourPlacement.RIGHT)
                .nextText("ok")
                .prevText("back");
        assertEquals("title", s.getTitle());
        assertEquals("desc", s.getDescription());
        assertEquals(TourPlacement.RIGHT, s.getPlacement());
        assertEquals("ok", s.getNextText());
        assertEquals("back", s.getPrevText());
    }

    @Test
    void step_targetVariants() {
        TourStep s1 = new TourStep().target(new Rectangle2D(0, 0, 50, 50));
        assertNotNull(s1.getTarget());
        assertFalse(s1.getTarget().isEmpty());

        TourStep s2 = new TourStep().target((javafx.scene.Node) null);
        // null Node 仍然是 nodeBased，但 resolveSceneBounds 为 null
        assertNotNull(s2.getTarget());
    }

    @Test
    void step_disposeClears() {
        TourStep s = new TourStep("t", "d").placement(TourPlacement.LEFT);
        s.dispose();
        assertEquals("", s.getTitle());
        assertEquals("", s.getDescription());
    }

    // ==================== Tour ====================

    @Test
    void tour_addStepsAndDispose() {
        Tour t = new Tour()
                .addStep(new TourStep("a", "1"))
                .addSteps(new TourStep("b", "2"), new TourStep("c", "3"));
        assertEquals(3, t.getSteps().size());
        assertEquals(0, t.getSteps().get(0).getIndex());
        assertEquals(1, t.getSteps().get(1).getIndex());
        assertEquals(2, t.getSteps().get(2).getIndex());

        t.dispose();
        assertTrue(t.isDisposed());
        assertEquals(0, t.getSteps().size());
    }

    @Test
    void tour_builderConfig() {
        Tour t = TourFactory.builder()
                .mask(false)
                .type(TourType.PRIMARY)
                .showClose(false)
                .showArrow(false)
                .closeOnEsc(false)
                .step(new TourStep("a"))
                .build();
        assertFalse(t.isMask());
        assertEquals(TourType.PRIMARY, t.getType());
        assertFalse(t.isShowClose());
        assertFalse(t.isShowArrow());
        assertFalse(t.isCloseOnEsc());
        assertEquals(1, t.getSteps().size());
    }

    @Test
    void tour_showWithoutSteps_doesNothing() {
        Tour t = new Tour();
        Pane container = new Pane();
        // 无步骤时调用 show 应安全无副作用
        t.show(container);
        assertFalse(t.isActive());
    }
}
