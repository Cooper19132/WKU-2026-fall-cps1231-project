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

/**
 * 展示预测结果以及实际参与投票的参考样本，帮助解释 K近邻的判断过程。
 * Show predictions and the reference samples that actually voted, explaining KNN decisions.
 */
public class NeighborResults {

    /**
     * normalized 是32×32的处理结果，features 是它对应的64个特征。
     * normalized is the processed 32x32 image; features contains its 64 features.
     * k 必须满足 1 <= k <= samples.length；标签在0～9之间。
     * Requires 1 <= k <= samples.length and labels in 0-9.
     * 弹窗关闭后返回预测数字，不是样本下标，也不是票数。
     * After the dialog closes, return the predicted digit, not a sample index or vote count.
     */
    public static int showResults(
            JFrame window,
            BufferedImage normalized,
            int[] features,
            int[][] samples,
            int[] labels,
            int k) {

        // 计算距离，找到最近的 K 个样本
        // Calculate distances and find the K nearest samples.
        int[] distances =
                Main.calculateDistances(features, samples);

        int[] nearestIndices =
                Main.findNearestIndices(distances, k);

        // 直接使用这些邻居投票，保证展示与预测一致
        // Vote with these same neighbors so the display matches the prediction.
        int[] votes =
                Main.countVotes(nearestIndices, labels, k);

        int prediction = Main.findMostVotedLabel(votes);

        // 第一列显示自己的手写图片，其余列显示邻居
        // Show the drawing first, followed by the neighbors.
        JPanel results = new JPanel(
                new GridLayout(1, k + 1, 12, 0)
        );

        results.add(createImagePanel(
                "<html><center>你的笔迹（处理后）<br>Your processed drawing</center></html>",
                normalized,
                "预测 / Prediction: " + prediction
        ));

        for (int i = 0; i < k; i++) {
            // i 是邻居的排名位置，sampleIndex 才是参考数据中的原始下标。
            // i is a neighbor position; sampleIndex is its original reference-data index.
            int sampleIndex = nearestIndices[i];

            BufferedImage sampleImage =
                    sampleToImage(samples[sampleIndex]);

            results.add(createImagePanel(
                    "<html><center>最近邻 / Neighbor " + (i + 1) + "</center></html>",
                    sampleImage,
                    "<html><center>"
                            + "真实标签 / Label: " + labels[sampleIndex]
                            + "<br>平方距离<br>Squared distance: "
                            + distances[sampleIndex]
                            + "</center></html>"
            ));
        }

        JOptionPane.showMessageDialog(
                window,
                results,
                "预测 / Prediction: " + prediction + " | 邻居数 / Neighbors K = " + k,
                JOptionPane.PLAIN_MESSAGE
        );

        return prediction;
    }

    // 把64个0～16的特征还原成8×8灰度示意图，无法恢复原始笔迹的精确位置。
    // Render 64 values in 0-16 as an 8x8 grayscale approximation, not the exact original strokes.
    public static BufferedImage sampleToImage(int[] features) {
        BufferedImage image = new BufferedImage(
                8, 8, BufferedImage.TYPE_INT_RGB
        );

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // 按行展开：第0行占下标0～7，第1行占8～15，最后一个下标为63。
                // Row-major order: row 0 uses 0-7, row 1 uses 8-15, and the final index is 63.
                int value = features[row * 8 + col];

                // 特征0显示为白色，特征16显示为黑色
                // Feature 0 is white; feature 16 is black.
                int gray = 255 - value * 255 / 16;

                Color color = new Color(gray, gray, gray);

                image.setRGB(col, row, color.getRGB());
            }
        }

        return image;
    }

    // 创建包含标题、图片和说明的小面板；放大仅用于显示，不参与距离计算。
    // Build a title/image/detail panel; enlargement is display-only and does not affect distance.
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
        detailLabel.setToolTipText("<html>标签为真实数字；平方距离越小表示特征越接近。<br>Labels are true digits; smaller squared distances mean closer features.<br>距离不是置信度或正确率 / Distance is not confidence or accuracy.</html>");

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(detailLabel, BorderLayout.SOUTH);

        return panel;
    }
}
