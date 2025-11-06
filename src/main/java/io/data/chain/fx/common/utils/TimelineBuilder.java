package io.data.chain.fx.common.utils;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Timeline 构建器
 */
public class TimelineBuilder {

    private final Timeline timeline;

    private TimelineBuilder(Timeline timeline) {
        this.timeline = Objects.requireNonNullElseGet(timeline, Timeline::new);
    }

    public static TimelineBuilder builder() {
        return new TimelineBuilder(null);
    }

    public static TimelineBuilder builder(Timeline timeline) {
        return new TimelineBuilder(timeline);
    }

    /**
     * 动画中的循环次数
     *
     * @param value
     * @return
     */
    public TimelineBuilder cycleCount(int value) {
        timeline.setCycleCount(value);
        return this;
    }

    /**
     * 动画是否在交替循环时反转方向
     *
     * @param value
     * @return
     */
    public TimelineBuilder autoReverse(boolean value) {
        timeline.setAutoReverse(value);
        return this;
    }

    /**
     * 动画结束时执行的动作
     *
     * @param value
     * @return
     */
    public TimelineBuilder onFinished(EventHandler<ActionEvent> value) {
        timeline.setOnFinished(value);
        return this;
    }

    public TimelineBuilder rate(double rate) {
        timeline.setRate(rate);
        return this;
    }

    /**
     * 延迟动画的开始
     *
     * @param duration
     * @return
     */
    public TimelineBuilder delay(Duration duration) {
        timeline.setDelay(duration);
        return this;
    }

    /**
     * 添加KeyFrame ：targets和values的长度应该保持一致
     *
     * @param targets  目标属性
     * @param values   目标值
     * @param duration 时间
     * @param <T>
     * @return
     */
    public <T> TimelineBuilder addKeyFrame(WritableValue<T>[] targets, T[] values, Duration duration) {
        return addKeyFrame(targets, values, duration, null);
    }

    public <T> TimelineBuilder addKeyFrame(WritableValue<T>[] targets, T[] values, Duration duration, EventHandler<ActionEvent> finished) {
        List<KeyValue> keyValues = new ArrayList<>();
        for (int i = 0; i < targets.length; i++) {
            WritableValue<T> writableValue = targets[i];
            keyValues.add(new KeyValue(writableValue, values[i], Interpolator.LINEAR));
        }
        KeyFrame keyFrame = new KeyFrame(duration, finished, keyValues.toArray(new KeyValue[0]));
        timeline.getKeyFrames().add(keyFrame);
        return this;
    }

    public TimelineBuilder clearKeyFrames() {
        timeline.getKeyFrames().clear();
        return this;
    }

    /**
     * 添加KeyFrame
     *
     * @param target   目标属性
     * @param value    目标值
     * @param duration 时间
     * @param <T>
     * @return
     */
    public <T> TimelineBuilder addKeyFrame(WritableValue<T> target, T value, Duration duration) {
        return addKeyFrame(target, value, duration, null);
    }

    public <T> TimelineBuilder addKeyFrame(WritableValue<T> target, T value, Duration duration, EventHandler<ActionEvent> finished) {
        KeyValue keyValue = new KeyValue(target, value, Interpolator.LINEAR);
        KeyFrame keyFrame = new KeyFrame(duration, finished, keyValue);
        timeline.getKeyFrames().add(keyFrame);
        return this;
    }

    /**
     * 构建 Timeline
     *
     * @return
     */
    public Timeline build() {
        return timeline;
    }

}
