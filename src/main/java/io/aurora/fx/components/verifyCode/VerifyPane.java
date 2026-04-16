package io.aurora.fx.components.verifyCode;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

/**
 * 验证码组件统一接口
 * 所有验证码组件都必须实现此接口
 * <p>
 * 使用示例：
 * <pre>
 * VerifyPane sliderPane = new SliderVerifyPane(config);
 * sliderPane.setOnVerifyComplete(result -> {
 *     if (result.isSuccess()) {
 *         System.out.println("验证成功！");
 *     }
 * });
 * </pre>
 * 
 * @author JavaFX Team
 * @since 1.0.0
 */
public interface VerifyPane {

    /**
     * 获取验证码组件的根容器
     * @return JavaFX Pane对象，可直接添加到场景中
     */
    Pane getRoot();

    /**
     * 获取当前验证状态
     * @return 验证状态枚举值
     */
    VerifyState getState();

    /**
     * 获取验证状态属性（用于绑定）
     * @return 验证状态的ObjectProperty
     */
    ObjectProperty<VerifyState> stateProperty();

    /**
     * 设置验证完成回调
     * @param callback 验证完成时的回调函数
     */
    void setOnVerifyComplete(Consumer<VerifyResult> callback);

    /**
     * 获取验证完成回调
     * @return 当前的验证完成回调，如果未设置则返回null
     */
    default Consumer<VerifyResult> getOnVerifyComplete() {
        return null;
    }

    /**
     * 设置刷新回调
     * @param callback 刷新时的回调函数
     */
    void setOnRefresh(Runnable callback);

    /**
     * 刷新验证码
     */
    void refresh();

    /**
     * 重置验证码状态
     */
    void reset();

    /**
     * 获取验证码配置
     * @return 当前配置对象
     */
    VerifyConfig getConfig();

    /**
     * 显示验证码窗口
     * 创建新窗口显示验证码组件，验证成功后自动关闭
     * 
     * @return 显示的Stage窗口
     */
    default javafx.stage.Stage show() {
        return VerifyStageManager.show(this);
    }

    /**
     * 显示验证码窗口（指定尺寸）
     * 
     * @param width 窗口宽度
     * @param height 窗口高度
     * @return 显示的Stage窗口
     */
    default javafx.stage.Stage show(double width, double height) {
        return VerifyStageManager.show(this, width, height);
    }

    /**
     * 显示验证码窗口（指定尺寸和标题）
     * 
     * @param width 窗口宽度
     * @param height 窗口高度
     * @param title 窗口标题
     * @return 显示的Stage窗口
     */
    default javafx.stage.Stage show(double width, double height, String title) {
        return VerifyStageManager.show(this, width, height, title);
    }

    /**
     * 显示模态验证码窗口（阻塞父窗口）
     * 
     * @param parentStage 父窗口
     * @return 显示的Stage窗口
     */
    default javafx.stage.Stage showModal(javafx.stage.Stage parentStage) {
        return VerifyStageManager.showModal(this, parentStage);
    }

    /**
 * 显示模态验证码窗口（阻塞父窗口，指定尺寸）
     * 
     * @param parentStage 父窗口
     * @param width 窗口宽度
     * @param height 窗口高度
     * @return 显示的Stage窗口
     */
    default javafx.stage.Stage showModal(javafx.stage.Stage parentStage, double width, double height) {
        return VerifyStageManager.showModal(this, parentStage, width, height);
    }

    /**
     * 关闭验证码窗口
     */
    default void close() {
        VerifyStageManager.close(this);
    }

    /**
     * 检查验证码窗口是否正在显示
     * 
     * @return 是否正在显示
     */
    default boolean isShowing() {
        return VerifyStageManager.isShowing(this);
    }

    /**
     * 获取验证码组件对应的窗口
     * 
     * @return 对应的Stage，如果不存在返回null
     */
    default javafx.stage.Stage getStage() {
        return VerifyStageManager.getStage(this);
    }

    /**
     * 验证状态枚举
     */
    enum VerifyState {
        /**
         * 准备就绪，等待用户操作
         */
        READY("ready"),

        /**
         * 加载中
         */
        LOADING("loading"),

        /**
         * 验证中
         */
        VERIFYING("verifying"),

        /**
         * 验证成功
         */
        SUCCESS("success"),

        /**
         * 验证失败
         */
        FAIL("fail");

        private final String code;

        VerifyState(String code) {
            this.code = code;
        }

        /**
         * 获取状态代码
         * @return 状态代码字符串
         */
        public String getCode() {
            return code;
        }

        /**
         * 根据代码获取状态
         * @param code 状态代码
         * @return 对应的验证状态，如果没有匹配则返回READY
         */
        public static VerifyState fromCode(String code) {
            if (code == null) {
                return READY;
            }
            for (VerifyState state : values()) {
                if (state.code.equalsIgnoreCase(code)) {
                    return state;
                }
            }
            return READY;
        }
    }
}
