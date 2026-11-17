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
 * GUI entry point: load reference data and connect drawing, image processing, and KNN prediction.
 * 在 IDEA 中运行本类的 main 方法即可打开手写窗口。
 * Run this main method in IDEA to open the drawing window.
 */
public class DigitRecognizerUI {

    public static void main(String[] args)
            throws FileNotFoundException {

        // 相对路径从运行时的工作目录开始查找；在 IDEA 中应将工作目录设为项目根目录。
        // Relative paths start at the working directory; use the project root in IDEA.
        // 启动时读取一次，之后点击识别时复用数组，不重复读取文件。
        // Load once at startup and reuse the arrays for each recognition request.
        String filePath =
                "data/optical+recognition+of+handwritten+digits/optdigits.tra";

        int[][] rows = Main.readAllRows(filePath);
        int[][] samples = Main.buildSamples(rows);
        int[] labels = Main.buildLabels(rows);

        System.out.println("已加载参考样本 / Loaded reference samples: " + samples.length);

        // Swing 界面在事件线程上创建。() -> ... 表示稍后要执行的任务。
        // Create Swing components on the event thread. () -> ... represents a task to run later.
        // 调用 Main 的静态方法不会运行 Main.main，两个 main 是不同的程序入口。
        // Calling static methods in Main does not run Main.main; the two main methods are separate entries.
        SwingUtilities.invokeLater(
                () -> createWindow(samples, labels)
        );
    }

    /**
     * samples[i] 是第 i 个参考样本，labels[i] 是同一样本的真实数字标签。
     * samples[i] is reference sample i; labels[i] is its true digit label.
     */
    public static void createWindow(
            int[][] samples, int[] labels) {

        JFrame window = new JFrame(
                "手写数字识别 / Digit Recognizer · KNN"
        );

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        JLabel instruction = new JLabel(
                "<html><center>请写一个数字（0～9），再点击识别<br>Write one digit (0-9), then click Recognize</center></html>",
                SwingConstants.CENTER
        );

        DrawingPanel drawingPanel = new DrawingPanel();

        JButton recognizeButton = new JButton("<html><center>识别数字<br>Recognize</center></html>");
        JButton clearButton = new JButton("<html><center>清空重写<br>Clear</center></html>");
        drawingPanel.setToolTipText("<html>按住鼠标或触控板拖动，每次写一个完整数字。<br>Drag to write one large, complete digit at a time.</html>");
        recognizeButton.setToolTipText("<html>识别笔迹，展示参与投票的三个最近邻。<br>Recognize and show the three neighbors that vote.</html>");
        clearButton.setToolTipText("<html>清除笔迹与结果，重新书写。<br>Clear the drawing and result to start again.</html>");

        recognizeButton.addActionListener(e -> {
            BufferedImage image = drawingPanel.getImage();

            int[] bounds = ImageProcessor.findInkBounds(image);

            if (bounds == null) {
                instruction.setText("<html><center>画板为空，请先写一个数字（0～9）<br>Canvas is empty. Please write a digit first.</center></html>");
                // return 结束的是本次按钮响应；窗口仍可继续使用。
                // return ends this button callback; the window remains usable.
                return;
            }

            // 1. 缩放并居中
            // 1. Resize and center the drawing.
            BufferedImage normalized =
                    ImageProcessor.normalize(image, bounds);

            // 2. 转换成64个特征
            // 2. Convert to 64 features.
            int[] features =
                    ImageProcessor.toFeatures(normalized);

            // 3. 使用相同的最近邻完成投票和展示。
            // 3. Use the same neighbors for voting and display.
            // 最后的 3 是 K：实际决定参与投票和展示的邻居数量。
            // The final argument, 3, is K: it controls how many neighbors vote and appear.
            int prediction = NeighborResults.showResults(
                    window,
                    normalized,
                    features,
                    samples,
                    labels,
                    3
            );

            instruction.setText("<html><center>预测数字 / Prediction: " + prediction + "<br>可清空后重写 / Clear to write again</center></html>");
        });

        clearButton.addActionListener(e -> {
            drawingPanel.clear();
            instruction.setText("<html><center>请写一个数字（0～9），再点击识别<br>Write one digit (0-9), then click Recognize</center></html>");
        });

        JPanel buttons = new JPanel();
        buttons.add(recognizeButton);
        buttons.add(clearButton);

        window.add(instruction, BorderLayout.NORTH);
        window.add(drawingPanel, BorderLayout.CENTER);
        window.add(buttons, BorderLayout.SOUTH);

        // pack 根据组件的首选尺寸确定窗口大小。
        // pack sizes the window using component preferred sizes.
        // 固定窗口大小，让显示画板与内部 320×320 图片使用同一套鼠标坐标。
        // Fix the window size to keep canvas mouse coordinates aligned with the 320x320 image.
        window.pack();
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
