import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * 手写画板：接收鼠标事件，把笔迹保存在图片中，供 ImageProcessor 读取。
 * Drawing canvas: receive mouse events and store strokes in an image for ImageProcessor.
 */
public class DrawingPanel extends JPanel {

    // 用图片保存笔迹，窗口重绘时笔迹不会消失
    // Store strokes in an image so repainting does not erase them.
    private final BufferedImage image =
            new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);

    // 上一个鼠标位置，用来把连续拖动的位置连接成线段。
    // Previous mouse position, used to connect drag positions with line segments.
    // 图片左上角为 (0, 0)，x 向右增加，y 向下增加。
    // The top-left is (0, 0); x increases rightward and y downward.
    private int lastX;
    private int lastY;

    // 构造方法：创建画板时自动执行
    // Constructor: runs automatically when a canvas is created.
    public DrawingPanel() {
        setPreferredSize(new Dimension(320, 320));
        clear();

        // 匿名内部类：只重写需要处理的按下、拖动事件。
        // Anonymous inner class: override only the press and drag events we need.
        MouseAdapter mouseHandler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();

                // 单击时也画一个点
                // A single click also draws a dot.
                drawStroke(lastX, lastY, lastX, lastY);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX = e.getX();
                int currentY = e.getY();

                drawStroke(lastX, lastY, currentX, currentY);

                // 下一段笔迹要从本段终点开始。
                // Start the next segment at the end of this segment.
                lastX = currentX;
                lastY = currentY;
            }
        };

        // 按下与拖动属于不同事件，需要分别注册同一个处理对象。
        // Press and drag are different event types; register the handler for both.
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    // 在图片上画一段笔迹
    // Draw a stroke segment onto the image.
    private void drawStroke(int x1, int y1, int x2, int y2) {
        Graphics2D g = image.createGraphics();

        g.setColor(Color.BLACK);

        g.setStroke(new BasicStroke(
                18, // 画笔宽度（像素）；圆形端点让线段衔接自然 / Brush width in pixels; round caps make segment joins smoother.
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        g.drawLine(x1, y1, x2, y2);
        g.dispose(); // 释放本次创建的绘图资源 / Release the graphics resources created for this operation.

        repaint(); // 请求 Swing 稍后调用 paintComponent 显示更新后的图片 / Ask Swing to call paintComponent later to display the updated image.
    }

    // 用白色覆盖图片，清空笔迹
    // Cover the image with white to erase the drawing.
    public void clear() {
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();

        repaint();
    }
    // 返回原图片而不是副本；调用者只读取它，清空和绘画仍由画板负责。
    // Return the original image, not a copy; callers read it while the canvas owns drawing and clearing.
    public BufferedImage getImage() {
        return image;
    }

    // Swing 需要刷新画板时，显示保存的图片
    // Display the stored image when Swing repaints the canvas.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 先完成 JPanel 的默认背景绘制 / Paint the default JPanel background first.
        g.drawImage(image, 0, 0, null);
    }
}
