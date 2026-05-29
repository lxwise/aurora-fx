package io.aurora.fx.components.tour;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;

/**
 * Tour 目标包装器
 * <p>
 * 支持以下三种目标参数形式：
 * <ul>
 *   <li>JavaFX {@link Node}：自动跟随节点位置变化</li>
 *   <li>场景坐标矩形 {@link Rectangle2D}：固定位置、固定尺寸</li>
 *   <li>{@code null}：表示无目标，弹窗显示在屏幕中央</li>
 * </ul>
 * </p>
 *
 * <pre>{@code
 * // 1. 直接绑定 Node
 * TourTarget t1 = TourTarget.of(button);
 *
 * // 2. 自定义坐标
 * TourTarget t2 = TourTarget.of(100, 100, 200, 50);
 *
 * // 3. 居中显示
 * TourTarget t3 = TourTarget.empty();
 * }</pre>
 *
 * @author Tour Component
 * @version 1.0
 */
public final class TourTarget {

    private final Node node;
    private final Rectangle2D rect;

    private TourTarget(Node node, Rectangle2D rect) {
        this.node = node;
        this.rect = rect;
    }

    // ==================== 工厂方法 ====================

    /**
     * 通过 Node 创建目标
     *
     * @param node 目标节点（可为 null，等同于 {@link #empty()}）
     * @return 新的 TourTarget
     */
    public static TourTarget of(Node node) {
        return new TourTarget(node, null);
    }

    /**
     * 通过场景坐标创建目标
     *
     * @param rect 场景坐标矩形（可为 null，等同于 {@link #empty()}）
     * @return 新的 TourTarget
     */
    public static TourTarget of(Rectangle2D rect) {
        return new TourTarget(null, rect);
    }

    /**
     * 通过场景坐标分量创建目标
     *
     * @param x      x 坐标
     * @param y      y 坐标
     * @param width  宽度
     * @param height 高度
     * @return 新的 TourTarget
     */
    public static TourTarget of(double x, double y, double width, double height) {
        return new TourTarget(null, new Rectangle2D(x, y, width, height));
    }

    /**
     * 创建空目标，弹窗将显示在屏幕中央
     *
     * @return 空 TourTarget
     */
    public static TourTarget empty() {
        return new TourTarget(null, null);
    }

    // ==================== 状态判断 ====================

    /**
     * 是否为空目标
     *
     * @return true 表示无目标
     */
    public boolean isEmpty() {
        return node == null && rect == null;
    }

    /**
     * 是否绑定到 Node
     *
     * @return true 表示绑定 Node
     */
    public boolean isNodeBased() {
        return node != null;
    }

    // ==================== 计算 ====================

    /**
     * 计算目标在场景坐标系下的边界
     * <p>
     * 当目标为 Node 时，使用 {@link Node#localToScene(Bounds)} 实时计算；
     * 当目标为坐标矩形时直接返回；空目标返回 null。
     * </p>
     *
     * @return 场景坐标边界，无目标时返回 null
     */
    public Rectangle2D resolveSceneBounds() {
        if (node != null) {
            // 节点已脱离场景图时返回 null（视为空目标）
            if (node.getScene() == null) {
                return null;
            }
            Bounds b = node.localToScene(node.getBoundsInLocal());
            if (b == null || b.getWidth() <= 0 || b.getHeight() <= 0) {
                return null;
            }
            return new Rectangle2D(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        }
        return rect;
    }

    /**
     * 计算目标中心点（场景坐标）
     *
     * @return 中心坐标，目标为空时返回 null
     */
    public Point2D resolveCenter() {
        Rectangle2D b = resolveSceneBounds();
        if (b == null) {
            return null;
        }
        return new Point2D(b.getMinX() + b.getWidth() / 2, b.getMinY() + b.getHeight() / 2);
    }

    // ==================== Getters ====================

    public Node getNode() {
        return node;
    }

    public Rectangle2D getRect() {
        return rect;
    }
}
