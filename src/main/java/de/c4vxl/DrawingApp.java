package de.c4vxl;

import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

public class DrawingApp extends JFrame {
    private final int width = 28, height = 28, sizeScaling = 3;
    private boolean isDrawing = false;
    private int lastX, lastY;
    private final CanvasPanel canvas = new CanvasPanel();
    private final JPanel probDisplay = new JPanel();
    private final MLP model;

    public DrawingApp(MLP model) {
        this.model = model;

        setTitle("Handwritten Digit Identification");
        setSize(300, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        probDisplay.setLayout(new BoxLayout(probDisplay, BoxLayout.Y_AXIS));

        add(probDisplay, BorderLayout.EAST);
        add(canvas, BorderLayout.CENTER);

        JButton clearButton = new JButton("Clear Canvas");
        clearButton.addActionListener(e -> canvas.clear());
        add(clearButton, BorderLayout.SOUTH);

        displayProbabilities(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}); // initial probs

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                isDrawing = true;
                lastX = e.getX();
                lastY = e.getY();
            }
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
                onDrawComplete();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDrawing) {
                    canvas.drawLine(lastX, lastY, e.getX(), e.getY());
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }
        });

        setVisible(true);
    }

    private void displayProbabilities(Double[] probabilities) {
        int finalPrediction = IntStream.range(0, probabilities.length)
                .reduce((a, b) -> probabilities[a] > probabilities[b] ? a : b)
                .orElse(-1);

        // clear display
        probDisplay.removeAll();

        probDisplay.add(new JLabel("Prediction:"));

        for (int i = 0; i < probabilities.length; i++)
            probDisplay.add(new JLabel(i + ": " + Math.round(probabilities[i] * 100) + "%"));

        probDisplay.add(new JLabel("Final prediction: " + finalPrediction));

        probDisplay.updateUI();
    }

    private void onDrawComplete() {
        Tensor<Double> pixels = new Tensor<>(canvas.getDrawnPixels(), 1, width * height);

        // save pixels as png file for logging
        saveCanvasToFile(pixels.data, "last_prompt.png");

        // make prediction
        Tensor<Double> prediction = this.model.forward(pixels);

        displayProbabilities(prediction.data);
    }

    private void saveCanvasToFile(Double[] pixels, String path) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            image.getRaster().setSample(x, y, 0, (int) (pixels[y * width + x] * 1));

        try {
            File outputFile = new File(path);
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class CanvasPanel extends JPanel {
        private final BufferedImage image;
        private final Graphics2D g2d;

        public CanvasPanel() {
            setPreferredSize(new Dimension(width * sizeScaling, height * sizeScaling));
            image = new BufferedImage(width * sizeScaling, height * sizeScaling, BufferedImage.TYPE_INT_RGB);
            g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            clear();
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

    public static void main(String[] args) {
        new DrawingApp(
                (MLP) new MLP(784, 10, 2, 12)
                        .load("models/digitRecognition.mdl")
        );
    }
}