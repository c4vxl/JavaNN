package de.c4vxl.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class CanvasPanel extends JPanel {
    public final BufferedImage image;
    public final Graphics2D g2d;
    public final int width = 28, height = 28, sizeScaling = 3;
    public boolean isDrawing = false;
    public int lastX, lastY;

    public CanvasPanel() {
        App.setSize(this, width * sizeScaling, height * sizeScaling);
        image = new BufferedImage(width * sizeScaling, height * sizeScaling, BufferedImage.TYPE_INT_RGB);
        g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        clear();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                isDrawing = true;
                lastX = e.getX();
                lastY = e.getY();
            }
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDrawing) {
                    drawLine(lastX, lastY, e.getX(), e.getY());
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(x1, y1, x2, y2);
        repaint();
    }

    public void clear() {
        g2d.setPaint(Color.black);
        g2d.fillRect(0, 0, width * sizeScaling, height * sizeScaling);
        g2d.setPaint(Color.white);
        repaint();
    }

    public Double[] getDrawnPixels() {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();

        int[] pixels = resizedImage.getRaster().getPixels(0, 0, width, height, (int[]) null);
        return Arrays.stream(pixels).mapToObj(p -> (double) p).toArray(Double[]::new);
    }
}