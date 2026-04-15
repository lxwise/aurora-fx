package io.aurora.fx.components.verifyCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户行为轨迹数据
 * 用于记录和分析用户交互行为，实现反机器人检测
 * @author JavaFX Team
 */
public class TrajectoryData {

    /**
     * 轨迹点列表
     */
    private List<TrajectoryPoint> points = new ArrayList<>();
    
    /**
     * 开始时间戳
     */
    private long startTime;
    
    /**
     * 结束时间戳
     */
    private long endTime;
    
    /**
     * 总移动距离
     */
    private double totalDistance;
    
    /**
     * 平均速度（像素/毫秒）
     */
    private double averageSpeed;
    
    /**
     * 最大速度
     */
    private double maxSpeed;
    
    /**
     * 方向变化次数
     */
    private int directionChanges;
    
    /**
     * 是否为机器人特征（基于算法判断）
     */
    private boolean robotSuspected;

    /**
     * 轨迹模式：true=连续拖拽模式（滑块），false=离散点击模式（文字点选）
     */
    private boolean continuousMode = true;

    public TrajectoryData() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 添加轨迹点
     * @param x X坐标
     * @param y Y坐标
     * @param timestamp 时间戳
     */
    public void addPoint(double x, double y, long timestamp) {
        TrajectoryPoint point = new TrajectoryPoint(x, y, timestamp);
        points.add(point);
        
        if (points.size() > 1) {
            TrajectoryPoint prev = points.get(points.size() - 2);
            double distance = Math.sqrt(Math.pow(x - prev.x, 2) + Math.pow(y - prev.y, 2));
            long timeDiff = timestamp - prev.timestamp;
            
            if (timeDiff > 0) {
                double speed = distance / timeDiff;
                this.maxSpeed = Math.max(this.maxSpeed, speed);
            }
            
            this.totalDistance += distance;
        }
    }

    /**
     * 结束轨迹记录并计算统计数据
     */
    public void finish() {
        this.endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        if (duration > 0) {
            this.averageSpeed = totalDistance / duration;
        }
        
        this.directionChanges = calculateDirectionChanges();
        this.robotSuspected = continuousMode ? detectRobotBehaviorDrag() : detectRobotBehaviorClick();
    }

    /**
     * 计算方向变化次数
     */
    private int calculateDirectionChanges() {
        if (points.size() < 3) {
            return 0;
        }
        
        int changes = 0;
        int prevDirection = 0; // -1: 左, 0: 静止, 1: 右
        
        for (int i = 1; i < points.size(); i++) {
            TrajectoryPoint curr = points.get(i);
            TrajectoryPoint prev = points.get(i - 1);
            
            int direction = Double.compare(curr.x, prev.x);
            
            if (prevDirection != 0 && direction != 0 && direction != prevDirection) {
                changes++;
            }
            
            if (direction != 0) {
                prevDirection = direction;
            }
        }
        
        return changes;
    }

    /**
     * 检测拖拽模式下的机器人行为特征（用于滑块验证码）
     * @return true表示疑似机器人行为
     */
    private boolean detectRobotBehaviorDrag() {
        // 拖拽模式检测规则（已放宽以避免误判）：
        // 1. 轨迹点太少（少于3个点）
        // 2. 速度过于均匀（标准差过小）
        // 3. 方向变化太多（不稳定）
        // 4. 移动时间过短
        
        if (points.size() < 3) {
            return true;
        }
        
        long duration = endTime - startTime;
        if (duration < 100) { // 少于100毫秒完成
            return true;
        }
        
        // 方向变化超过30次可能是机器人抖动
        if (directionChanges > 30) {
            return true;
        }
        
        // 检测速度标准差
        if (points.size() >= 3) {
            double speedVariance = calculateSpeedVariance();
            if (speedVariance < 0.0005) { // 速度极其均匀
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检测点击模式下的机器人行为特征（用于文字点选验证码）
     * @return true表示疑似机器人行为
     */
    private boolean detectRobotBehaviorClick() {
        // 点击模式检测规则更宽松：
        // 1. 至少需要2个点击点
        // 2. 总时间不能太短（少于150毫秒完成所有点击）
        // 3. 所有点击时间间隔不能完全相同（机器人特征）
        
        if (points.size() < 2) {
            return true;
        }
        
        long duration = endTime - startTime;
        if (duration < 150) {
            return true;
        }
        
        // 检查点击时间间隔是否完全相同（机器人特征）
        // 仅在点击数>=3时才检查
        if (points.size() >= 3) {
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < points.size(); i++) {
                intervals.add(points.get(i).timestamp - points.get(i - 1).timestamp);
            }
            
            // 计算时间间隔的标准差
            if (intervals.size() >= 2) {
                double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0);
                double variance = intervals.stream()
                        .mapToDouble(t -> Math.pow(t - mean, 2))
                        .average().orElse(0);
                double stdDev = Math.sqrt(variance);
                
                // 时间间隔标准差小于2ms，说明点击太均匀（机器人特征）
                if (mean > 0 && stdDev < 2) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 计算速度方差
     */
    private double calculateSpeedVariance() {
        if (points.size() < 2) {
            return 0;
        }
        
        List<Double> speeds = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            TrajectoryPoint curr = points.get(i);
            TrajectoryPoint prev = points.get(i - 1);
            double distance = Math.sqrt(Math.pow(curr.x - prev.x, 2) + Math.pow(curr.y - prev.y, 2));
            long timeDiff = curr.timestamp - prev.timestamp;
            
            if (timeDiff > 0) {
                speeds.add(distance / timeDiff);
            }
        }
        
        if (speeds.isEmpty()) {
            return 0;
        }
        
        double mean = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = speeds.stream()
                .mapToDouble(s -> Math.pow(s - mean, 2))
                .average().orElse(0);
        
        return variance;
    }

    /**
     * 获取轨迹点数量
     */
    public int getPointCount() {
        return points.size();
    }

    /**
     * 获取总时长（毫秒）
     */
    public long getDuration() {
        return endTime - startTime;
    }

    // ==================== Getter/Setter ====================

    public List<TrajectoryPoint> getPoints() {
        return points;
    }

    public void setPoints(List<TrajectoryPoint> points) {
        this.points = points;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getDirectionChanges() {
        return directionChanges;
    }

    public void setDirectionChanges(int directionChanges) {
        this.directionChanges = directionChanges;
    }

    public boolean isRobotSuspected() {
        return robotSuspected;
    }

    public void setRobotSuspected(boolean robotSuspected) {
        this.robotSuspected = robotSuspected;
    }

    public boolean isContinuousMode() {
        return continuousMode;
    }

    /**
     * 设置轨迹模式
     * @param continuousMode true=连续拖拽模式（滑块），false=离散点击模式（文字点选）
     */
    public void setContinuousMode(boolean continuousMode) {
        this.continuousMode = continuousMode;
    }

    /**
     * 轨迹点内部类
     */
    public static class TrajectoryPoint {
        public final double x;
        public final double y;
        public final long timestamp;

        public TrajectoryPoint(double x, double y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("Point(%.2f, %.2f) @ %d", x, y, timestamp);
        }
    }
}
