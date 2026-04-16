package io.aurora.fx.components.verifyCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码图片工具类
 * 提供验证码图片生成、裁剪、编码等功能
 * @author JavaFX Team
 */
public class VerifyImageUtil {

    /**
     * 默认源文件宽度
     */
    private static final int DEFAULT_SRC_WIDTH = 350;
    
    /**
     * 默认源文件高度
     */
    private static final int DEFAULT_SRC_HEIGHT = 200;
    
    /**
     * 默认滑块宽度
     */
    private static final int DEFAULT_SLIDER_WIDTH = 50;
    
    /**
     * 默认滑块高度
     */
    private static final int DEFAULT_SLIDER_HEIGHT = 50;
    
    /**
     * 默认凸起圆心半径
     */
    private static final int DEFAULT_CIRCLE_RADIUS = 5;
    
    /**
     * 默认内边距
     */
    private static final int DEFAULT_RECTANGLE_PADDING = 8;
    
    /**
     * 默认边框宽度
     */
    private static final int DEFAULT_OUT_PADDING = 1;

    // ==================== 滑块验证码生成 ====================

    /**
     * 根据配置生成滑动验证码
     * @param config 验证码配置
     * @return VerifyImage 验证码图片数据
     * @throws IOException 文件读取异常
     */
    public static VerifyImage generateSliderVerifyImage(VerifyConfig config) throws IOException {
        String imagePath = config.getRandomBackgroundImage();
        if (imagePath == null) {
            throw new IllegalArgumentException("未配置背景图片路径");
        }
        return generateSliderVerifyImage(imagePath, config);
    }

    /**
     * 根据文件路径生成滑动验证码
     * @param filePath 图片文件路径
     * @param config 验证码配置
     * @return VerifyImage 验证码图片数据
     * @throws IOException 文件读取异常
     */
    public static VerifyImage generateSliderVerifyImage(String filePath, VerifyConfig config) throws IOException {
        BufferedImage srcImage;

        // 支持 URL / classpath / file
        if (filePath.startsWith("http") || filePath.startsWith("file:") || filePath.startsWith("jar:")) {
            srcImage = ImageIO.read(new URL(filePath));
        } else {
            // 尝试从 classpath 读取（使用try-with-resources确保InputStream关闭）
            InputStream is = VerifyImageUtil.class.getClassLoader().getResourceAsStream(filePath);
            if (is != null) {
                try (InputStream closableIs = is) {
                    srcImage = ImageIO.read(closableIs);
                }
            } else {
                // fallback：当作本地文件
                srcImage = ImageIO.read(new File(filePath));
            }
        }

        if (srcImage == null) {
            throw new IOException("无法读取图片: " + filePath);
        }

        int srcWidth = config.getSrcWidth();
        int srcHeight = config.getSrcHeight();
        int sliderWidth = config.getSliderWidth();
        int sliderHeight = config.getSliderHeight();

        // 调整图片尺寸
        BufferedImage resizedImage = resizeImage(srcImage, srcWidth, srcHeight);

        // 计算滑块位置，确保在有效范围内
        int maxX = srcWidth - sliderWidth * 2;
        int maxY = srcHeight - sliderHeight - 10;
        int minY = 10;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int locationX = sliderWidth + random.nextInt(Math.max(1, maxX));
        int locationY = minY + random.nextInt(Math.max(1, maxY - minY));

        // 创建滑块图片
        BufferedImage sliderImage = new BufferedImage(sliderWidth, sliderHeight, BufferedImage.TYPE_4BYTE_ABGR);
        
        // 生成滑块形状数据
        int[][] blockData = generateBlockData(sliderWidth, sliderHeight, 
                config.getCircleRadius(), config.getRectanglePadding());

        // 根据形状裁剪图片
        cutImageByTemplate(resizedImage, sliderImage, blockData, locationX, locationY);

        // 创建VerifyImage对象
        return new VerifyImage(
                imageToBase64(resizedImage),
                imageToBase64(sliderImage),
                locationX,
                locationY,
                srcWidth,
                srcHeight
        );
    }

    /**
     * 根据文件路径生成滑动验证码（使用默认配置）
     * @param filePath 图片文件路径
     * @return VerifyImage 验证码图片数据
     * @throws IOException 文件读取异常
     */
    public static VerifyImage generateSliderVerifyImage(String filePath) throws IOException {
        return generateSliderVerifyImage(filePath, new VerifyConfig());
    }

    /**
     * 根据文件路径和场景尺寸生成滑动验证码
     * @param filePath 图片文件路径
     * @param sceneWidth 场景宽度
     * @param sceneHeight 场景高度
     * @return VerifyImage 验证码图片数据
     * @throws IOException 文件读取异常
     */
    public static VerifyImage generateSliderVerifyImage(String filePath, double sceneWidth, double sceneHeight) throws IOException {
        VerifyConfig config = new VerifyConfig();
        config.setSrcWidth((int) sceneWidth);
        config.setSrcHeight((int) sceneHeight);
        return generateSliderVerifyImage(filePath, config);
    }

    // ==================== 滑块形状生成 ====================

    /**
     * 生成滑块形状数据
     * <p>
     * 数据含义：
     * 0 - 透明像素
     * 1 - 滑块像素
     * 2 - 边框阴影像素
     *
     * @param width 滑块宽度
     * @param height 滑块高度
     * @param circleRadius 凸起圆半径
     * @param padding 内边距
     * @return 形状数据二维数组
     */
    public static int[][] generateBlockData(int width, int height, int circleRadius, int padding) {
        int[][] data = new int[width][height];
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 凸起圆心位置随机化
        // 顶部或底部的凸起
        double x1 = padding + (width - 2 * padding) / 2.0 - 5 + random.nextInt(10);
        double y1Top = padding - random.nextInt(3);
        double y1Bottom = height - padding + random.nextInt(3);
        double y1 = random.nextBoolean() ? y1Top : y1Bottom;

        // 左侧或右侧的凹槽
        double x2Right = width - padding - circleRadius + random.nextInt(2 * circleRadius - 4);
        double x2Left = padding + circleRadius - 2 - random.nextInt(2 * circleRadius - 4);
        double x2 = random.nextBoolean() ? x2Right : x2Left;
        double y2 = padding + (height - 2 * padding) / 2.0 - 4 + random.nextInt(10);

        double radiusSquared = Math.pow(circleRadius, 2);

        // 填充基本形状
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 矩形区域
                if ((i >= padding && i < width - padding) 
                        && (j >= padding && j < height - padding)) {
                    data[i][j] = 1;
                } else {
                    data[i][j] = 0;
                }

                // 凸起区域（顶部或底部）
                double distance1 = Math.pow(i - x1, 2) + Math.pow(j - y1, 2);
                if (distance1 < radiusSquared) {
                    data[i][j] = 1;
                }

                // 凹槽区域（左侧或右侧）
                double distance2 = Math.pow(i - x2, 2) + Math.pow(j - y2, 2);
                if (distance2 < radiusSquared) {
                    data[i][j] = 0;
                }
            }
        }

        // 添加边框阴影
        addBorderShadow(data, width, height, padding);

        return data;
    }

    /**
     * 添加边框阴影效果
     */
    private static void addBorderShadow(int[][] data, int width, int height, int padding) {
        int shadowWidth = DEFAULT_OUT_PADDING;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 四个角落处理
                for (int k = 1; k <= shadowWidth; k++) {
                    // 左上角和右上角
                    if (i >= padding - k && i < padding) {
                        if ((j >= padding - k && j < padding) 
                                || (j >= height - padding - k && j < height - padding + 1)) {
                            data[i][j] = 2;
                        }
                    }

                    // 左下角和右下角
                    if (i >= width - padding + k - 1 && i < width - padding + 1) {
                        if ((j >= padding - k && j < padding) 
                                || (j >= height - padding - k && j <= height - padding)) {
                            data[i][j] = 2;
                        }
                    }
                }

                // 边缘阴影
                if (data[i][j] == 1) {
                    if (j - shadowWidth > 0 && data[i][j - shadowWidth] == 0) {
                        data[i][j - shadowWidth] = 2;
                    }
                    if (j + shadowWidth < height && data[i][j + shadowWidth] == 0) {
                        data[i][j + shadowWidth] = 2;
                    }
                    if (i - shadowWidth > 0 && data[i - shadowWidth][j] == 0) {
                        data[i - shadowWidth][j] = 2;
                    }
                    if (i + shadowWidth < width && data[i + shadowWidth][j] == 0) {
                        data[i + shadowWidth][j] = 2;
                    }
                }
            }
        }
    }

    /**
     * 根据模板裁剪图片
     * @param srcImage 原图
     * @param sliderImage 滑块图
     * @param blockData 形状数据
     * @param x 裁剪起始X坐标
     * @param y 裁剪起始Y坐标
     */
    public static void cutImageByTemplate(BufferedImage srcImage, BufferedImage sliderImage, 
                                          int[][] blockData, int x, int y) {
        int width = sliderImage.getWidth();
        int height = sliderImage.getHeight();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int srcX = x + i;
                int srcY = y + j;
                
                // 边界检查
                if (srcX < 0 || srcX >= srcImage.getWidth() 
                        || srcY < 0 || srcY >= srcImage.getHeight()) {
                    continue;
                }

                int pixelType = blockData[i][j];
                int srcRgb = srcImage.getRGB(srcX, srcY);

                switch (pixelType) {
                    case 1: // 滑块像素
                        sliderImage.setRGB(i, j, srcRgb);
                        // 原图对应位置变灰
                        srcImage.setRGB(srcX, srcY, Color.LIGHT_GRAY.getRGB());
                        break;
                    case 2: // 边框阴影
                        sliderImage.setRGB(i, j, Color.WHITE.getRGB());
                        srcImage.setRGB(srcX, srcY, Color.GRAY.getRGB());
                        break;
                    case 0: // 透明像素
                    default:
                        // 设置为透明
                        sliderImage.setRGB(i, j, srcRgb & 0x00FFFFFF);
                        break;
                }
            }
        }
    }

    // ==================== 文字点选验证码生成 ====================

    /**
     * 生成文字点选验证码
     * @param config 验证码配置
     * @return 文字点选验证码数据
     */
    public static TextClickVerifyData generateTextClickVerify(VerifyConfig config) {
        int width = config.getSrcWidth();
        int height = config.getSrcHeight();
        int clickCount = config.getClickTextCount();
        int interferenceCount = config.getInterferenceTextCount();
        List<String> textPool = config.getTextPool();
        int[] fontSizeRange = config.getFontSizeRange();
        Color textColor = config.getTextColor();

        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 绘制背景
        GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 240, 250), 
                width, height, new Color(220, 230, 240));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        // 添加干扰线
        drawInterferenceLines(g2d, width, height);

        // 随机选择需要点击的文字
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<String> availableTexts = new ArrayList<>(textPool);
        Collections.shuffle(availableTexts);
        
        List<String> targetTexts = availableTexts.subList(0, Math.min(clickCount, availableTexts.size()));
        List<Point> targetPositions = new ArrayList<>();
        
        // 已使用的位置区域，用于防止重叠
        List<Rectangle2D> usedAreas = new ArrayList<>();

        // 绘制目标文字并记录位置
        // 为每个文字确定固定的字体大小，确保位置计算和绘制使用相同的字体
        for (String text : targetTexts) {
            // 为当前文字选择一个固定的随机字体大小
            int fontSize = fontSizeRange[0] + random.nextInt(fontSizeRange[1] - fontSizeRange[0] + 1);
            Font font = new Font("微软雅黑", Font.BOLD, fontSize);
            
            Point position = findNonOverlappingPosition(g2d, text, width, height, usedAreas, font);
            if (position != null) {
                // 使用相同的字体绘制文字
                drawTextWithEffect(g2d, text, position.x, position.y, textColor, font);
                targetPositions.add(position);
                
                // 记录使用区域（使用实际使用的字体）
                FontRenderContext frc = g2d.getFontRenderContext();
                Rectangle2D bounds = font.getStringBounds(text, frc);
                usedAreas.add(new Rectangle2D.Double(
                        position.x - bounds.getWidth() / 2,
                        position.y - bounds.getHeight() / 2,
                        bounds.getWidth(),
                        bounds.getHeight()
                ));
            }
        }

        // 绘制干扰文字
        List<String> interferenceTexts = availableTexts.subList(
                clickCount, 
                Math.min(clickCount + interferenceCount, availableTexts.size())
        );
        
        for (String text : interferenceTexts) {
            // 干扰文字也使用固定字体大小
            int fontSize = fontSizeRange[0] + random.nextInt(fontSizeRange[1] - fontSizeRange[0] + 1);
            Font font = new Font("微软雅黑", Font.BOLD, fontSize);
            
            Point position = findNonOverlappingPosition(g2d, text, width, height, usedAreas, font);
            if (position != null) {
                Color interferenceColor = new Color(
                        100 + random.nextInt(100),
                        100 + random.nextInt(100),
                        100 + random.nextInt(100)
                );
                drawTextWithEffect(g2d, text, position.x, position.y, interferenceColor, font);
                
                FontRenderContext frc = g2d.getFontRenderContext();
                Rectangle2D bounds = font.getStringBounds(text, frc);
                usedAreas.add(new Rectangle2D.Double(
                        position.x - bounds.getWidth() / 2,
                        position.y - bounds.getHeight() / 2,
                        bounds.getWidth(),
                        bounds.getHeight()
                ));
            }
        }

        g2d.dispose();

        // 生成提示信息
        String hint = "请依次点击：" + String.join("、", targetTexts);

        try {
            return new TextClickVerifyData(
                    imageToBase64(image),
                    targetTexts,
                    targetPositions,
                    width,
                    height,
                    hint
            );
        } catch (IOException e) {
            throw new RuntimeException("生成文字点选验证码失败", e);
        }
    }

    /**
     * 绘制干扰线
     */
    private static void drawInterferenceLines(Graphics2D g2d, int width, int height) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        g2d.setStroke(new BasicStroke(1.5f));

        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);

            Color lineColor = new Color(
                    150 + random.nextInt(100),
                    150 + random.nextInt(100),
                    150 + random.nextInt(100),
                    100
            );
            g2d.setColor(lineColor);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 查找不重叠的位置
     * @param g2d Graphics2D对象
     * @param text 文字内容
     * @param width 图片宽度
     * @param height 图片高度
     * @param usedAreas 已使用的区域列表
     * @param font 字体对象（确保位置计算和绘制使用相同的字体）
     * @return 找到的位置，如果找不到则返回null
     */
    private static Point findNonOverlappingPosition(Graphics2D g2d, String text, 
                                                    int width, int height, 
                                                    List<Rectangle2D> usedAreas, Font font) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(text, frc);

        int margin = 30; // 增加边距，确保文字不会贴边
        int maxAttempts = 50;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // 确保文字完全在图片内
            int x = margin + random.nextInt(Math.max(1, width - 2 * margin));
            int y = margin + random.nextInt(Math.max(1, height - 2 * margin));

            Rectangle2D testArea = new Rectangle2D.Double(
                    x - bounds.getWidth() / 2 - 5,
                    y - bounds.getHeight() / 2 - 5,
                    bounds.getWidth() + 10,
                    bounds.getHeight() + 10
            );

            boolean overlaps = false;
            for (Rectangle2D used : usedAreas) {
                if (testArea.intersects(used)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                return new Point(x, y);
            }
        }

        return null;
    }

    /**
     * 绘制带效果的文字
     * @param g2d Graphics2D对象
     * @param text 文字内容
     * @param x 中心X坐标
     * @param y 中心Y坐标
     * @param color 文字颜色
     * @param font 字体对象
     */
    private static void drawTextWithEffect(Graphics2D g2d, String text, int x, int y, 
                                           Color color, Font font) {
        g2d.setFont(font);
        
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(text, frc);
        
        // 计算绘制位置：使文字中心与指定位置对齐
        // drawString的y坐标是文字的基线位置，需要调整
        int drawX = (int) (x - bounds.getWidth() / 2);
        int drawY = (int) (y + bounds.getHeight() / 2 - bounds.getMaxY() / 2);

        // 绘制阴影
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.drawString(text, drawX + 2, drawY + 2);

        // 绘制文字
        g2d.setColor(color);
        g2d.drawString(text, drawX, drawY);
    }

    // ==================== 算术验证码生成 ====================

    /**
     * 生成算术验证码
     * @param config 验证码配置
     * @return 算术验证码数据
     */
    public static ArithmeticVerifyData generateArithmeticVerify(VerifyConfig config) {
        int width = config.getSrcWidth();
        int height = config.getSrcHeight();
        List<String> operators = config.getOperators();
        int[] numberRange = config.getNumberRange();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // 生成两个随机数
        int num1 = numberRange[0] + random.nextInt(numberRange[1] - numberRange[0] + 1);
        int num2 = numberRange[0] + random.nextInt(numberRange[1] - numberRange[0] + 1);
        
        // 随机选择运算符
        String operator = operators.get(random.nextInt(operators.size()));
        
        // 计算答案
        int answer;
        String expression;
        
        switch (operator) {
            case "+":
                answer = num1 + num2;
                expression = num1 + " + " + num2 + " = ?";
                break;
            case "-":
                // 确保结果非负
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                answer = num1 - num2;
                expression = num1 + " - " + num2 + " = ?";
                break;
            case "×":
                // 乘法使用较小的数
                num1 = numberRange[0] + random.nextInt(20);
                num2 = numberRange[0] + random.nextInt(10);
                answer = num1 * num2;
                expression = num1 + " × " + num2 + " = ?";
                break;
            case "÷":
                // 除法确保能整除
                num2 = numberRange[0] + random.nextInt(10) + 1;
                answer = numberRange[0] + random.nextInt(20);
                num1 = answer * num2;
                expression = num1 + " ÷ " + num2 + " = ?";
                break;
            default:
                answer = num1 + num2;
                expression = num1 + " + " + num2 + " = ?";
        }

        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 绘制背景
        GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 245, 255), 
                width, height, new Color(230, 240, 250));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        // 添加干扰元素
        drawInterferenceDots(g2d, width, height);

        // 绘制算式
        Font font = new Font("Arial", Font.BOLD, 36);
        g2d.setFont(font);
        g2d.setColor(new Color(50, 50, 80));

        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(expression, frc);
        int textX = (int) ((width - bounds.getWidth()) / 2);
        int textY = (int) ((height + bounds.getHeight() / 2) / 2);

        // 绘制阴影
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.drawString(expression, textX + 3, textY + 3);

        // 绘制文字
        g2d.setColor(new Color(50, 50, 100));
        g2d.drawString(expression, textX, textY);

        g2d.dispose();

        try {
            return new ArithmeticVerifyData(
                    imageToBase64(image),
                    expression,
                    answer,
                    width,
                    height
            );
        } catch (IOException e) {
            throw new RuntimeException("生成算术验证码失败", e);
        }
    }

    /**
     * 绘制干扰点
     */
    private static void drawInterferenceDots(Graphics2D g2d, int width, int height) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = 1 + random.nextInt(3);

            Color dotColor = new Color(
                    150 + random.nextInt(100),
                    150 + random.nextInt(100),
                    150 + random.nextInt(100),
                    50 + random.nextInt(100)
            );
            g2d.setColor(dotColor);
            g2d.fillOval(x, y, size, size);
        }
    }

    // ==================== 图片处理工具方法 ====================

    /**
     * 调整图片尺寸
     * @param srcImage 原图
     * @param width 目标宽度
     * @param height 目标高度
     * @return 调整后的图片
     */
    public static BufferedImage resizeImage(BufferedImage srcImage, int width, int height) {
        if (srcImage.getWidth() == width && srcImage.getHeight() == height) {
            return srcImage;
        }

        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(srcImage, 0, 0, width, height, null);
        g2d.dispose();
        
        return resizedImage;
    }

    /**
     * 图片转Base64字符串
     * @param image 图片对象
     * @return Base64编码字符串
     * @throws IOException 编码异常
     */
    public static String imageToBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            byte[] bytes = out.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    /**
     * Base64字符串转图片
     * @param base64String Base64编码字符串
     * @return 图片对象
     */
    public static BufferedImage base64ToImage(String base64String) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Base64转图片失败", e);
        }
    }

    /**
     * 随机获取一张图片
     * @param directoryPath 图片目录路径
     * @return BufferedImage 图片对象
     * @throws IOException 文件读取异常
     */
    public static BufferedImage getRandomImage(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("路径不是有效的目录: " + directoryPath);
        }

        File[] files = directory.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".jpg") 
                || name.toLowerCase().endsWith(".jpeg") 
                || name.toLowerCase().endsWith(".png")
        );

        if (files == null || files.length == 0) {
            throw new IOException("目录中没有找到图片文件: " + directoryPath);
        }

        File selectedFile = files[ThreadLocalRandom.current().nextInt(files.length)];
        return ImageIO.read(selectedFile);
    }

    /**
     * 将图片保存到文件
     * @param image 图片对象
     * @param filePath 目标文件路径
     * @throws IOException 文件写入异常
     */
    public static void saveImage(BufferedImage image, String filePath) throws IOException {
        String format = filePath.substring(filePath.lastIndexOf('.') + 1);
        ImageIO.write(image, format, new File(filePath));
    }

    // ==================== 数据类 ====================

    /**
     * 文字点选验证码数据
     */
    public static class TextClickVerifyData {
        private final String imageBase64;
        private final List<String> targetTexts;
        private final List<Point> targetPositions;
        private final int width;
        private final int height;
        private final String hint;

        public TextClickVerifyData(String imageBase64, List<String> targetTexts, 
                                   List<Point> targetPositions, int width, int height, String hint) {
            this.imageBase64 = imageBase64;
            this.targetTexts = targetTexts;
            this.targetPositions = targetPositions;
            this.width = width;
            this.height = height;
            this.hint = hint;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public List<String> getTargetTexts() {
            return targetTexts;
        }

        public List<Point> getTargetPositions() {
            return targetPositions;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getHint() {
            return hint;
        }
    }

    /**
     * 算术验证码数据
     */
    public static class ArithmeticVerifyData {
        private final String imageBase64;
        private final String expression;
        private final int answer;
        private final int width;
        private final int height;

        public ArithmeticVerifyData(String imageBase64, String expression, int answer, int width, int height) {
            this.imageBase64 = imageBase64;
            this.expression = expression;
            this.answer = answer;
            this.width = width;
            this.height = height;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public String getExpression() {
            return expression;
        }

        public int getAnswer() {
            return answer;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
