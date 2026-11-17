import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

/**
 * 图形界面入口：加载参考数据，将画板、图像处理和 K近邻预测连接起来。
 * 在 IDEA 中运行本类的 main 方法即可打开手写窗口。
 */
public class DigitRecognizerUI {

    public static void main(String[] args)
            throws FileNotFoundException {

        // 相对路径从运行时的工作目录开始查找；在 IDEA 中应将工作目录设为项目根目录。
        // 启动时读取一次，之后点击识别时复用数组，不重复读取文件。
        String filePath =
                "data/optical+recognition+of+handwritten+digits/optdigits.tra";

        int[][] rows = Main.readAllRows(filePath);
        int[][] samples = Main.buildSamples(rows);
        int[] labels = Main.buildLabels(rows);

        System.out.println("已加载参考样本：" + samples.length);

        // Swing 界面在事件线程上创建。() -> ... 表示稍后要执行的任务。
        // 调用 Main 的静态方法不会运行 Main.main，两个 main 是不同的程序入口。
        SwingUtilities.invokeLater(
                () -> createWindow(samples, labels)
        );
    }

    /** samples[i] 是第 i 个参考样本，labels[i] 是同一样本的真实数字标签。 */
    public static void createWindow(
            int[][] samples, int[] labels) {

        JFrame window = new JFrame(
                "手写数字识别 · K近邻"
        );

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        JLabel instruction = new JLabel(
                "请写一个数字（0～9），再点击识别",
                SwingConstants.CENTER
        );

        DrawingPanel drawingPanel = new DrawingPanel();

        JButton recognizeButton = new JButton("识别数字");
        JButton clearButton = new JButton("清空重写");
        drawingPanel.setToolTipText("按住鼠标或触控板拖动书写；每次只写一个数字，尽量写大、写完整。");
        recognizeButton.setToolTipText("识别当前笔迹，并展示参与投票的三个最近邻样本。");
        clearButton.setToolTipText("清除全部笔迹和预测提示，开始下一次书写。");

        recognizeButton.addActionListener(e -> {
            BufferedImage image = drawingPanel.getImage();

            int[] bounds = ImageProcessor.findInkBounds(image);

            if (bounds == null) {
                instruction.setText("画板为空，请先写一个数字（0～9）");
                // return 结束的是本次按钮响应；窗口仍可继续使用。
                return;
            }

            // 1. 缩放并居中
            BufferedImage normalized =
                    ImageProcessor.normalize(image, bounds);

            // 2. 转换成64个特征
            int[] features =
                    ImageProcessor.toFeatures(normalized);

            // 3. 使用相同的最近邻完成投票和展示。
            // 最后的 3 是 K：实际决定参与投票和展示的邻居数量。
            int prediction = NeighborResults.showResults(
                    window,
                    normalized,
                    features,
                    samples,
                    labels,
                    3
            );

            instruction.setText("预测数字：" + prediction + "（可清空后重写）");
        });

        clearButton.addActionListener(e -> {
            drawingPanel.clear();
            instruction.setText("请写一个数字（0～9），再点击识别");
        });

        JPanel buttons = new JPanel();
        buttons.add(recognizeButton);
        buttons.add(clearButton);

        window.add(instruction, BorderLayout.NORTH);
        window.add(drawingPanel, BorderLayout.CENTER);
        window.add(buttons, BorderLayout.SOUTH);

        // pack 根据组件的首选尺寸确定窗口大小。
        // 固定窗口大小，让显示画板与内部 320×320 图片使用同一套鼠标坐标。
        window.pack();
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
