import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class NeighborResults {

    // 显示结果窗口，并返回预测数字
    public static int showResults(
            JFrame window,
            BufferedImage normalized,
            int[] features,
            int[][] samples,
            int[] labels,
            int k) {

        // 计算距离，找到最近的 K 个样本
        int[] distances =
                Main.calculateDistances(features, samples);

        int[] nearestIndices =
                Main.findNearestIndices(distances, k);

        // 直接使用这些邻居投票，保证展示与预测一致
        int[] votes =
                Main.countVotes(nearestIndices, labels, k);

        int prediction = Main.findMostVotedLabel(votes);

        // 第一列显示自己的手写图片，其余列显示邻居
        JPanel results = new JPanel(
                new GridLayout(1, k + 1, 12, 0)
        );

        results.add(createImagePanel(
                "Your drawing",
                normalized,
                "Prediction: " + prediction
        ));

        for (int i = 0; i < k; i++) {
            int sampleIndex = nearestIndices[i];

            BufferedImage sampleImage =
                    sampleToImage(samples[sampleIndex]);

            results.add(createImagePanel(
                    "Neighbor " + (i + 1),
                    sampleImage,
                    "<html><center>"
                            + "Label: " + labels[sampleIndex]
                            + "<br>Squared distance: "
                            + distances[sampleIndex]
                            + "</center></html>"
            ));
        }

        JOptionPane.showMessageDialog(
                window,
                results,
                "Prediction: " + prediction + " | K = " + k,
                JOptionPane.PLAIN_MESSAGE
        );

        return prediction;
    }

    // 把一个参考样本的64个特征转换成8×8灰度图片
    public static BufferedImage sampleToImage(int[] features) {
        BufferedImage image = new BufferedImage(
                8, 8, BufferedImage.TYPE_INT_RGB
        );

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int value = features[row * 8 + col];

                // 特征0显示为白色，特征16显示为黑色
                int gray = 255 - value * 255 / 16;

                Color color = new Color(gray, gray, gray);

                image.setRGB(col, row, color.getRGB());
            }
        }

        return image;
    }

    // 创建包含标题、图片和说明的小面板
    public static JPanel createImagePanel(
            String title,
            BufferedImage image,
            String detail) {

        JPanel panel = new JPanel(new BorderLayout(0, 8));

        JLabel titleLabel = new JLabel(
                title, SwingConstants.CENTER
        );

        Image enlarged = image.getScaledInstance(
                96, 96, Image.SCALE_REPLICATE
        );

        JLabel imageLabel = new JLabel(new ImageIcon(enlarged));

        JLabel detailLabel = new JLabel(
                detail, SwingConstants.CENTER
        );

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(detailLabel, BorderLayout.SOUTH);

        return panel;
    }
}