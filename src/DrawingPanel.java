import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class DrawingPanel extends JPanel {

    // 用图片保存笔迹，窗口重绘时笔迹不会消失
    private final BufferedImage image =
            new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);

    private int lastX;
    private int lastY;

    // 构造方法：创建画板时自动执行
    public DrawingPanel() {
        setPreferredSize(new Dimension(320, 320));
        clear();

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

                lastX = currentX;
                lastY = currentY;
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    // 在图片上画一段笔迹
    private void drawStroke(int x1, int y1, int x2, int y2) {
        Graphics2D g = image.createGraphics();

        g.setColor(Color.BLACK);

        g.setStroke(new BasicStroke(
                18,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        g.drawLine(x1, y1, x2, y2);
        g.dispose();

        repaint();
    }

    // 用白色覆盖图片，清空笔迹
    public void clear() {
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();

        repaint();
    }
    // 让其他类能够读取画板图片
    public BufferedImage getImage() {
        return image;
    }

    // Swing 需要刷新画板时，显示保存的图片
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}