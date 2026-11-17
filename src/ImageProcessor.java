import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * 图片转特征：查找笔迹 → 保持比例缩放并居中 → 按区域统计笔画像素。
 * Image-to-feature pipeline: find ink, resize proportionally and center, then count ink per block.
 * 使用黑色笔迹、白色背景；64个特征的排列和取值范围与参考数据对应。
 * Use black ink on white; the 64 features follow the reference data order and value range.
 * 当前居中和缩放是本项目的简化处理，不完全复现 UCI 的原始预处理。
 * Centering and scaling are simplified here and do not fully reproduce UCI preprocessing.
 */
public class ImageProcessor {

    // 返回笔迹范围：左上角 x、y，以及宽度、高度
    // Return ink bounds: top-left x, y, width, and height.
    // 如果没有笔迹，返回 null
    // Return null if no ink is found.
    public static int[] findInkBounds(BufferedImage image) {
        // 先设为图片外侧的极值；发现笔迹后逐步收紧范围。
        // Initialize beyond the image bounds, then tighten the range as ink is found.
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                // RGB 中每种颜色占8位。右移后与255按位与，取出该颜色的0～255值。
                // Each RGB channel uses 8 bits; shift and mask with 255 to extract its value in 0-255.
                int red = (rgb >> 16) & 255;
                int green = (rgb >> 8) & 255;
                int blue = rgb & 255;

                // 简化亮度：黑色为0、白色为255，以128作为笔迹阈值。
                // Simplified brightness: black is 0, white is 255, and the ink threshold is 128.
                int brightness = (red + green + blue) / 3;

                // 较暗的像素算作笔迹
                // Count darker pixels as ink.
                if (brightness < 128) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX == -1) { // 未发现任何暗像素，调用者应提示先书写 / No dark pixels were found; the caller should ask for a drawing.
            return null;
        }

        // 两端都包含在范围内，例如从x=5到x=5也占一个像素，所以加一。
        // Both endpoints are included; x=5 through x=5 still spans one pixel, so add one.
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        return new int[]{minX, minY, width, height};
    }
    // bounds 必须是 findInkBounds 返回的有效范围，不能为 null。
    // bounds must be valid non-null bounds returned by findInkBounds.
    // 将笔迹缩放、居中到32×32图片中，输出仍为黑色笔迹、白色背景。
    // Resize and center into a 32x32 image, retaining black ink on white.
    public static BufferedImage normalize(
            BufferedImage image, int[] bounds) {

        int x = bounds[0];
        int y = bounds[1];
        int width = bounds[2];
        int height = bounds[3];

        // 裁出笔迹区域
        // Crop to the ink bounds.
        BufferedImage cropped = image.getSubimage(
                x, y, width, height
        );

        // 长边缩放到28，给32×32图片留一些边距
        // Scale the longer side to 28, leaving a margin within 32x32.
        double scale = 28.0 / Math.max(width, height); // 小数除法；宽高使用相同比例 / Floating-point division; use the same scale for width and height.

        // 极细笔画缩放并取整后仍至少占一个像素。
        // Keep each dimension at least one pixel after resizing and rounding.
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
        // Fill the background with white.
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 32, 32);

        // 计算居中后的左上角位置
        // Calculate the top-left position for centering.
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
     * Input must be the 32x32 image returned by normalize.
     * 每个4×4区域最多含16个笔画像素，共8×8=64个特征，按行展开。
     * Each 4x4 block has at most 16 ink pixels; flatten 8x8 blocks into 64 row-major features.
     * 返回值只含图像特征，不包含预测标签。
     * Return image features only, without a label.
     */
    public static int[] toFeatures(BufferedImage image) {
        int[] features = new int[64];

        // 外两层循环选择格子，内两层循环检查当前格子中的16个像素。
        // The outer loops select a block; the inner loops inspect its 16 pixels.
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
                // Flatten a block position with row * 8 + col, producing indices 0-63.
                features[row * 8 + col] = inkCount;
            }
        }

        return features;
    }
}
