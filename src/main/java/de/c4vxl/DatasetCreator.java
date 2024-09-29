package de.c4vxl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DatasetCreator extends JFrame {
    private int picturesPerNumber = 10; // let the user draw 10 pictures for each number (0-9)
    private JLabel label = new JLabel("Draw a 0 (1/" + picturesPerNumber + ")");
    private int currentImage = 0;
    private JButton nextButton = new JButton("Next");
    private boolean isDrawing = false;
    private int lastX, lastY;
    private final CanvasPanel canvas = new CanvasPanel();
    private final int width = 28, height = 28, sizeScaling = 3;

    private ArrayList<int[]> dataset = new ArrayList<>();

    public DatasetCreator() {
        setTitle("Dataset creator");
        setSize(300, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        // retry
        JButton retryButton = new JButton("Repaint");
        retryButton.addActionListener(e -> canvas.clear());
        buttonPanel.add(retryButton);

        nextButton.addActionListener(e -> next());
        buttonPanel.add(nextButton);

        add(buttonPanel, BorderLayout.PAGE_END);

        add(canvas);

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                isDrawing = true;
                lastX = e.getX();
                lastY = e.getY();
            }
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
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


    private void next() {
        currentImage++;

        // update label
        int current = currentImage / picturesPerNumber;
        label.setText("Draw a " + current + " (" + (currentImage - picturesPerNumber * current + 1) + "/" + picturesPerNumber + ")");

        // update button label
        if (currentImage == picturesPerNumber * 10 - 1)
            nextButton.setText("Finish");

        // add to dataset
        dataset.add(canvas.getDrawnPixels());

        // reset canvas
        canvas.clear();

        // handle finish
        if (currentImage >= picturesPerNumber * 10)
            finish();
    }

    private void finish() {
        // read lines
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < dataset.size(); i++) {
            int[] pixels = dataset.get(i);
            int label = i / picturesPerNumber;

            String pixelData = String.join(",", Arrays.stream(pixels)
                    .mapToObj(String::valueOf)
                    .toArray(String[]::new));

            lines.add(label + "," + pixelData);
        }

        // shuffle the dataset
        Collections.shuffle(lines);

        // convert dataset to csv format
        String fileContent = "label,pixels\n" + String.join("\n", lines);

        // save dataset
        try {
            File outputFile = new File("dataset/finetune.csv");
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();

            PrintWriter writer = new PrintWriter(new FileWriter(outputFile.getPath()));
            writer.print(fileContent);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // close jFrame
        this.dispose();
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

        public int[] getDrawnPixels() {
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(image, 0, 0, width, height, null);
            g2d.dispose();

            return resizedImage.getRaster().getPixels(0, 0, width, height, (int[]) null);
        }
    }

    public static void main(String[] args) {
        new DatasetCreator();
    }
}