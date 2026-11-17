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

/** 展示预测结果以及实际参与投票的参考样本，帮助解释 K近邻的判断过程。 */
public class NeighborResults {

    /**
     * normalized 是32×32的处理结果，features 是它对应的64个特征。
     * k 必须满足 1 <= k <= samples.length；标签在0～9之间。
     * 弹窗关闭后返回预测数字，不是样本下标，也不是票数。
     */
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
                "你的笔迹（处理后）",
                normalized,
                "预测数字：" + prediction
        ));

        for (int i = 0; i < k; i++) {
            // i 是邻居的排名位置，sampleIndex 才是参考数据中的原始下标。
            int sampleIndex = nearestIndices[i];

            BufferedImage sampleImage =
                    sampleToImage(samples[sampleIndex]);

            results.add(createImagePanel(
                    "最近邻 " + (i + 1),
                    sampleImage,
                    "<html><center>"
                            + "真实标签：" + labels[sampleIndex]
                            + "<br>平方距离："
                            + distances[sampleIndex]
                            + "</center></html>"
            ));
        }

        JOptionPane.showMessageDialog(
                window,
                results,
                "预测数字：" + prediction + " ｜ 邻居数 K = " + k,
                JOptionPane.PLAIN_MESSAGE
        );

        return prediction;
    }

    // 把64个0～16的特征还原成8×8灰度示意图，无法恢复原始笔迹的精确位置。
    public static BufferedImage sampleToImage(int[] features) {
        BufferedImage image = new BufferedImage(
                8, 8, BufferedImage.TYPE_INT_RGB
        );

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // 按行展开：第0行占下标0～7，第1行占8～15，最后一个下标为63。
                int value = features[row * 8 + col];

                // 特征0显示为白色，特征16显示为黑色
                int gray = 255 - value * 255 / 16;

                Color color = new Color(gray, gray, gray);

                image.setRGB(col, row, color.getRGB());
            }
        }

        return image;
    }

    // 创建包含标题、图片和说明的小面板；放大仅用于显示，不参与距离计算。
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
        detailLabel.setToolTipText("标签是样本的真实数字；平方距离越小表示特征越接近，不是置信度或正确率。");

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(detailLabel, BorderLayout.SOUTH);

        return panel;
    }
}
