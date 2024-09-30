package de.c4vxl.app.page;

import de.c4vxl.app.App;
import de.c4vxl.app.CanvasPanel;
import de.c4vxl.engine.data.Tensor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;

public class Prediction extends JFrame {
    private final JPanel probDisplay = new JPanel();
    private final CanvasPanel canvas = new CanvasPanel();
    private App parent;

    public Prediction(App parentApp) {
        parent = parentApp;

        this.setTitle("Predict");
        this.setSize(260, 500);
        this.getContentPane().setBackground(App.background);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setIconImage(App.icon.getImage());

        // home button
        JButton homeButton = App.createButton("Home");
        homeButton.addActionListener(e -> {
            this.setVisible(false);
            parentApp.setVisible(true);
        });

        // clear button
        JButton clearButton = App.createButton("Clear canvas");
        clearButton.addActionListener(e -> canvas.clear());

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(App.background);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;

        panel.add(homeButton, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(canvas, gbc);

        gbc.insets = new Insets(20, 0, 0, 0);
        gbc.gridy = 2;
        probDisplay.setLayout(new BoxLayout(probDisplay, BoxLayout.Y_AXIS));
        probDisplay.setBackground(App.background);
        panel.add(probDisplay, gbc);

        this.add(panel, BorderLayout.PAGE_START);

        this.add(clearButton, BorderLayout.PAGE_END);

        displayProbabilities(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});

        canvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                onDrawComplete();
            }
        });

        this.repaint();
    }

    private void onDrawComplete() {
        Tensor<Double> pixels = new Tensor<>(canvas.getDrawnPixels(), 1, canvas.width * canvas.height);

        // make prediction
        Tensor<Double> prediction = parent.model.forward(pixels);

        displayProbabilities(prediction.data);
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

        for (Component component : probDisplay.getComponents()) {
            component.setForeground(Color.WHITE);
        }

        probDisplay.updateUI();
    }
}