import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * 图片转特征：查找笔迹 → 保持比例缩放并居中 → 按区域统计笔画像素。
 * 使用黑色笔迹、白色背景；64个特征的排列和取值范围与参考数据对应。
 * 当前居中和缩放是本项目的简化处理，不完全复现 UCI 的原始预处理。
 */
public class ImageProcessor {

    // 返回笔迹范围：左上角 x、y，以及宽度、高度
    // 如果没有笔迹，返回 null
    public static int[] findInkBounds(BufferedImage image) {
        // 先设为图片外侧的极值；发现笔迹后逐步收紧范围。
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                // RGB 中每种颜色占8位。右移后与255按位与，取出该颜色的0～255值。
                int red = (rgb >> 16) & 255;
                int green = (rgb >> 8) & 255;
                int blue = rgb & 255;

                // 简化亮度：黑色为0、白色为255，以128作为笔迹阈值。
                int brightness = (red + green + blue) / 3;

                // 较暗的像素算作笔迹
                if (brightness < 128) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX == -1) { // 未发现任何暗像素，调用者应提示先书写
            return null;
        }

        // 两端都包含在范围内，例如从x=5到x=5也占一个像素，所以加一。
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        return new int[]{minX, minY, width, height};
    }
    // bounds 必须是 findInkBounds 返回的有效范围，不能为 null。
    // 将笔迹缩放、居中到32×32图片中，输出仍为黑色笔迹、白色背景。
    public static BufferedImage normalize(
            BufferedImage image, int[] bounds) {

        int x = bounds[0];
        int y = bounds[1];
        int width = bounds[2];
        int height = bounds[3];

        // 裁出笔迹区域
        BufferedImage cropped = image.getSubimage(
                x, y, width, height
        );

        // 长边缩放到28，给32×32图片留一些边距
        double scale = 28.0 / Math.max(width, height); // 小数除法；宽高使用相同比例

        // 极细笔画缩放并取整后仍至少占一个像素。
        int newWidth = Math.max(
                1, (int) Math.round(width * scale)
        );

        int newHeight = Math.max(
                1, (int) Math.round(height * scale)
        );

        BufferedImage normalized = new BufferedImage(
                32, 32, BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = normalized.createGraphics();

        // 背景填白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 32, 32);

        // 计算居中后的左上角位置
        int offsetX = (32 - newWidth) / 2;
        int offsetY = (32 - newHeight) / 2;

        g.drawImage(
                cropped,
                offsetX, offsetY,
                newWidth, newHeight,
                null
        );

        g.dispose();

        return normalized;
    }
    /**
     * 输入必须为 normalize 返回的32×32图片。
     * 每个4×4区域最多含16个笔画像素，共8×8=64个特征，按行展开。
     * 返回值只含图像特征，不包含预测标签。
     */
    public static int[] toFeatures(BufferedImage image) {
        int[] features = new int[64];

        // 外两层循环选择格子，内两层循环检查当前格子中的16个像素。
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int inkCount = 0;

                for (int dy = 0; dy < 4; dy++) {
                    for (int dx = 0; dx < 4; dx++) {
                        int x = col * 4 + dx;
                        int y = row * 4 + dy;

                        int rgb = image.getRGB(x, y);

                        int red = (rgb >> 16) & 255;
                        int green = (rgb >> 8) & 255;
                        int blue = rgb & 255;

                        int brightness = (red + green + blue) / 3;

                        if (brightness < 128) {
                            inkCount++;
                        }
                    }
                }

                // 二维格子转一维下标：行号×8+列号，结果范围为0～63。
                features[row * 8 + col] = inkCount;
            }
        }

        return features;
    }
}
