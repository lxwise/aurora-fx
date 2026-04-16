package io.aurora.fx.components.verifyCode;

import javafx.scene.input.MouseEvent;

/**
 * 用户行为追踪器
 * 用于记录和分析用户交互行为
 * @author JavaFX Team
 */
public class BehaviorTracker {

    private volatile TrajectoryData trajectoryData;
    private volatile boolean tracking = false;
    private double lastX = 0;
    private double lastY = 0;
    private long lastTime = 0;
    
    /**
     * 最小采样间隔（毫秒）
     */
    private long minSampleInterval = 10;

    /**
     * 轨迹模式：true=连续拖拽，false=离散点击
     */
    private boolean continuousMode = true;

    public BehaviorTracker() {
    }

    /**
     * 开始追踪
     */
    public void startTracking() {
        trajectoryData = new TrajectoryData();
        trajectoryData.setContinuousMode(continuousMode);
        tracking = true;
        lastTime = 0; // 重置时间，确保第一个点不被过滤
    }

    /**
     * 记录鼠标事件
     * @param event 鼠标事件
     */
    public void trackEvent(MouseEvent event) {
        if (!tracking) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // 采样间隔控制，避免过于密集的采样
        if (currentTime - lastTime < minSampleInterval) {
            return;
        }
        
        double x = event.getSceneX();
        double y = event.getSceneY();
        
        trajectoryData.addPoint(x, y, currentTime);
        
        lastX = x;
        lastY = y;
        lastTime = currentTime;
    }

    /**
     * 记录坐标点（用于点击模式，不应用采样间隔过滤）
     * @param x X坐标
     * @param y Y坐标
     */
    public void trackPoint(double x, double y) {
        if (!tracking) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // 点击模式不应用采样间隔过滤，每次点击都应该记录
        if (continuousMode && currentTime - lastTime < minSampleInterval) {
            return;
        }
        
        trajectoryData.addPoint(x, y, currentTime);
        
        lastX = x;
        lastY = y;
        lastTime = currentTime;
    }

    /**
     * 停止追踪并获取轨迹数据
     * @return 轨迹数据
     */
    public TrajectoryData stopTracking() {
        if (trajectoryData != null) {
            trajectoryData.finish();
        }
        tracking = false;
        return trajectoryData;
    }

    /**
     * 获取当前轨迹数据
     */
    public TrajectoryData getTrajectoryData() {
        return trajectoryData;
    }

    /**
     * 是否正在追踪
     */
    public boolean isTracking() {
        return tracking;
    }

    /**
     * 重置追踪器
     */
    public void reset() {
        trajectoryData = null;
        tracking = false;
        lastX = 0;
        lastY = 0;
        lastTime = 0;
    }

    /**
     * 获取分析报告
     */
    public String getAnalysisReport() {
        if (trajectoryData == null) {
            return "无轨迹数据";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("===== 行为轨迹分析报告 =====\n");
        sb.append(String.format("轨迹点数量: %d\n", trajectoryData.getPointCount()));
        sb.append(String.format("总时长: %d ms\n", trajectoryData.getDuration()));
        sb.append(String.format("总移动距离: %.2f px\n", trajectoryData.getTotalDistance()));
        sb.append(String.format("平均速度: %.4f px/ms\n", trajectoryData.getAverageSpeed()));
        sb.append(String.format("最大速度: %.4f px/ms\n", trajectoryData.getMaxSpeed()));
        sb.append(String.format("方向变化次数: %d\n", trajectoryData.getDirectionChanges()));
        sb.append(String.format("疑似机器人: %s\n", trajectoryData.isRobotSuspected() ? "是" : "否"));
        sb.append("===========================");
        
        return sb.toString();
    }

    public long getMinSampleInterval() {
        return minSampleInterval;
    }

    public void setMinSampleInterval(long minSampleInterval) {
        this.minSampleInterval = minSampleInterval;
    }

    public boolean isContinuousMode() {
        return continuousMode;
    }

    /**
     * 设置轨迹模式
     * @param continuousMode true=连续拖拽模式，false=离散点击模式
     */
    public void setContinuousMode(boolean continuousMode) {
        this.continuousMode = continuousMode;
    }
}
