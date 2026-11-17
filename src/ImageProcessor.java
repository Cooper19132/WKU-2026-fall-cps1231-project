import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;

public class ImageProcessor {

    // 返回笔迹范围：左上角 x、y，以及宽度、高度
    // 如果没有笔迹，返回 null
    public static int[] findInkBounds(BufferedImage image) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> 16) & 255;
                int green = (rgb >> 8) & 255;
                int blue = rgb & 255;

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

        if (maxX == -1) {
            return null;
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        return new int[]{minX, minY, width, height};
    }
    // 将笔迹缩放、居中到32×32图片中
// bounds 必须是已经找到的有效笔迹范围
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
        double scale = 28.0 / Math.max(width, height);

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
    // 将32×32图片转换为64个特征
    public static int[] toFeatures(BufferedImage image) {
        int[] features = new int[64];

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

                features[row * 8 + col] = inkCount;
            }
        }

        return features;
    }
}
