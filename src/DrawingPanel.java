import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** 手写画板：接收鼠标事件，把笔迹保存在图片中，供 ImageProcessor 读取。 */
public class DrawingPanel extends JPanel {

    // 用图片保存笔迹，窗口重绘时笔迹不会消失
    private final BufferedImage image =
            new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);

    // 上一个鼠标位置，用来把连续拖动的位置连接成线段。
    // 图片左上角为 (0, 0)，x 向右增加，y 向下增加。
    private int lastX;
    private int lastY;

    // 构造方法：创建画板时自动执行
    public DrawingPanel() {
        setPreferredSize(new Dimension(320, 320));
        clear();

        // 匿名内部类：只重写需要处理的按下、拖动事件。
        MouseAdapter mouseHandler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();

                // 单击时也画一个点
                drawStroke(lastX, lastY, lastX, lastY);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX = e.getX();
                int currentY = e.getY();

                drawStroke(lastX, lastY, currentX, currentY);

                // 下一段笔迹要从本段终点开始。
                lastX = currentX;
                lastY = currentY;
            }
        };

        // 按下与拖动属于不同事件，需要分别注册同一个处理对象。
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    // 在图片上画一段笔迹
    private void drawStroke(int x1, int y1, int x2, int y2) {
        Graphics2D g = image.createGraphics();

        g.setColor(Color.BLACK);

        g.setStroke(new BasicStroke(
                18, // 画笔宽度（像素）；圆形端点让线段衔接自然
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        g.drawLine(x1, y1, x2, y2);
        g.dispose(); // 释放本次创建的绘图资源

        repaint(); // 请求 Swing 稍后调用 paintComponent 显示更新后的图片
    }

    // 用白色覆盖图片，清空笔迹
    public void clear() {
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();

        repaint();
    }
    // 返回原图片而不是副本；调用者只读取它，清空和绘画仍由画板负责。
    public BufferedImage getImage() {
        return image;
    }

    // Swing 需要刷新画板时，显示保存的图片
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 先完成 JPanel 的默认背景绘制
        g.drawImage(image, 0, 0, null);
    }
}
