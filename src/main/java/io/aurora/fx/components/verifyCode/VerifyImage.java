package io.aurora.fx.components.verifyCode;

import java.awt.image.BufferedImage;

/**
 * 滑块验证码图片数据模型
 * 存储验证码图片的相关信息
 * @author JavaFX Team
 */
public class VerifyImage {
    
    /**
     * 原图Base64编码
     */
    private String srcImage;
    
    /**
     * 滑块图片Base64编码
     */
    private String cutImage;
    
    /**
     * 滑块正确X坐标位置
     */
    private Integer xPosition;
    
    /**
     * 滑块正确Y坐标位置
     */
    private Integer yPosition;
    
    /**
     * 背景图宽度
     */
    private Integer srcImageWidth;
    
    /**
     * 背景图高度
     */
    private Integer srcImageHeight;
    
    /**
     * 滑块宽度
     */
    private Integer sliderWidth;
    
    /**
     * 滑块高度
     */
    private Integer sliderHeight;

    // ==================== 构造方法 ====================

    public VerifyImage() {
    }

    public VerifyImage(String srcImage, String cutImage, Integer xPosition, Integer yPosition, 
                       Integer srcImageWidth, Integer srcImageHeight) {
        this.srcImage = srcImage;
        this.cutImage = cutImage;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.srcImageWidth = srcImageWidth;
        this.srcImageHeight = srcImageHeight;
    }

    public VerifyImage(String srcImage, String cutImage, Integer xPosition, Integer yPosition, 
                       Integer srcImageWidth, Integer srcImageHeight, Integer sliderWidth, Integer sliderHeight) {
        this(srcImage, cutImage, xPosition, yPosition, srcImageWidth, srcImageHeight);
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
    }

    // ==================== 验证方法 ====================

    /**
     * 验证滑块位置是否正确
     * @param userX 用户拖拽的X坐标
     * @param tolerance 允许的误差范围
     * @return 验证是否通过
     */
    public boolean verify(int userX, int tolerance) {
        return Math.abs(userX - xPosition) <= tolerance;
    }

    /**
     * 验证滑块位置是否正确（使用默认容差）
     * @param userX 用户拖拽的X坐标
     * @return 验证是否通过
     */
    public boolean verify(int userX) {
        return verify(userX, 8);
    }

    // ==================== 缓存BufferedImage对象 ====================
    
    private transient BufferedImage srcBufferedImage;
    private transient BufferedImage cutBufferedImage;

    /**
     * 获取原图BufferedImage对象（带缓存）
     */
    public BufferedImage getSrcBufferedImage() {
        if (srcBufferedImage == null && srcImage != null) {
            srcBufferedImage = VerifyImageUtil.base64ToImage(srcImage);
        }
        return srcBufferedImage;
    }

    /**
     * 获取滑块BufferedImage对象（带缓存）
     */
    public BufferedImage getCutBufferedImage() {
        if (cutBufferedImage == null && cutImage != null) {
            cutBufferedImage = VerifyImageUtil.base64ToImage(cutImage);
        }
        return cutBufferedImage;
    }

    // ==================== Getter/Setter ====================

    public String getSrcImage() {
        return srcImage;
    }

    public void setSrcImage(String srcImage) {
        this.srcImage = srcImage;
        this.srcBufferedImage = null; // 清除缓存
    }

    public String getCutImage() {
        return cutImage;
    }

    public void setCutImage(String cutImage) {
        this.cutImage = cutImage;
        this.cutBufferedImage = null; // 清除缓存
    }

    public Integer getXPosition() {
        return xPosition;
    }

    public void setXPosition(Integer xPosition) {
        this.xPosition = xPosition;
    }

    public Integer getYPosition() {
        return yPosition;
    }

    public void setYPosition(Integer yPosition) {
        this.yPosition = yPosition;
    }

    public Integer getSrcImageWidth() {
        return srcImageWidth;
    }

    public void setSrcImageWidth(Integer srcImageWidth) {
        this.srcImageWidth = srcImageWidth;
    }

    public Integer getSrcImageHeight() {
        return srcImageHeight;
    }

    public void setSrcImageHeight(Integer srcImageHeight) {
        this.srcImageHeight = srcImageHeight;
    }

    public Integer getSliderWidth() {
        return sliderWidth;
    }

    public void setSliderWidth(Integer sliderWidth) {
        this.sliderWidth = sliderWidth;
    }

    public Integer getSliderHeight() {
        return sliderHeight;
    }

    public void setSliderHeight(Integer sliderHeight) {
        this.sliderHeight = sliderHeight;
    }

    @Override
    public String toString() {
        return "VerifyImage{" +
                "xPosition=" + xPosition +
                ", yPosition=" + yPosition +
                ", srcImageWidth=" + srcImageWidth +
                ", srcImageHeight=" + srcImageHeight +
                ", sliderWidth=" + sliderWidth +
                ", sliderHeight=" + sliderHeight +
                '}';
    }
}