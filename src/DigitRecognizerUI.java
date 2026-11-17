import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

public class DigitRecognizerUI {

    public static void main(String[] args)
            throws FileNotFoundException {

        // 启动时读取一次参考数据
        String filePath =
                "data/optical+recognition+of+handwritten+digits/optdigits.tra";

        int[][] rows = Main.readAllRows(filePath);
        int[][] samples = Main.buildSamples(rows);
        int[] labels = Main.buildLabels(rows);

        System.out.println("Loaded samples: " + samples.length);

        // 把参考样本和标签交给窗口使用
        SwingUtilities.invokeLater(
                () -> createWindow(samples, labels)
        );
    }

    public static void createWindow(
            int[][] samples, int[] labels) {

        JFrame window = new JFrame(
                "Handwritten Digit Recognizer"
        );

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        JLabel instruction = new JLabel(
                "Write one digit (0-9)",
                SwingConstants.CENTER
        );

        DrawingPanel drawingPanel = new DrawingPanel();

        JButton recognizeButton = new JButton("Recognize");
        JButton clearButton = new JButton("Clear");

        recognizeButton.addActionListener(e -> {
            BufferedImage image = drawingPanel.getImage();

            int[] bounds = ImageProcessor.findInkBounds(image);

            if (bounds == null) {
                instruction.setText("Please write a digit first.");
                return;
            }

            // 1. 缩放并居中
            BufferedImage normalized =
                    ImageProcessor.normalize(image, bounds);

            // 2. 转换成64个特征
            int[] features =
                    ImageProcessor.toFeatures(normalized);

            // 3. 调用已有的 K近邻算法
            // 展示最近邻，并返回它们投票得到的预测数字
            int prediction = NeighborResults.showResults(
                    window,
                    normalized,
                    features,
                    samples,
                    labels,
                    3
            );

            instruction.setText("Prediction: " + prediction);
        });

        clearButton.addActionListener(e -> {
            drawingPanel.clear();
            instruction.setText("Write one digit (0-9)");
        });

        JPanel buttons = new JPanel();
        buttons.add(recognizeButton);
        buttons.add(clearButton);

        window.add(instruction, BorderLayout.NORTH);
        window.add(drawingPanel, BorderLayout.CENTER);
        window.add(buttons, BorderLayout.SOUTH);

        window.pack();
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}