package de.c4vxl.app.page;

import de.c4vxl.app.App;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.training.Datasets;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Benchmark extends JFrame {
    public Benchmark(App parentApp) {
        this.setTitle("Benchmark");
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

        this.add(homeButton, BorderLayout.PAGE_START);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(App.background);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.insets = new Insets(20, 0, 0, 0);
        JTextArea output = new JTextArea();
        output.setLayout(new BoxLayout(output, BoxLayout.Y_AXIS));
        App.setSize(output, 250, 300);
        output.setLineWrap(true);
        panel.add(output, gbc);

        JScrollPane scrollPane = new JScrollPane(output);
        App.setSize(scrollPane, 250, 300);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, gbc);

        gbc.gridy = 3;
        JLabel finalOut = new JLabel("");
        finalOut.setForeground(Color.WHITE);
        panel.add(finalOut, gbc);
        this.add(panel, BorderLayout.CENTER);

        JButton startButton = App.createButton("Run benchmark");
        startButton.addActionListener(e -> start(output, parentApp, finalOut));
        this.add(startButton, BorderLayout.PAGE_END);
    }

    private void start(JTextArea output, App parentApp, JLabel finalOut) {
        List<ArrayList<Tensor<Double>>> dataset = Datasets.MNIST("test");

        int correct = 0;
        for (ArrayList<Tensor<Double>> batch : dataset) {
            // get label and features
            Tensor<Double> label = batch.get(1);
            int wanted = Arrays.stream(label.data).toList().indexOf(label.max());
            Tensor<Double> features = batch.get(0);

            // make prediction
            Tensor<Double> y_pred = parentApp.model.forward(features);
            int predicted = Arrays.stream(y_pred.data).toList().indexOf(y_pred.max());

            // logging
            output.append("Predicted: " + predicted + " \t Wanted: " + wanted + "\n");
            output.updateUI();

            if (predicted == wanted)
                correct++;
        }

        double perc = ((double) correct / dataset.size()) * 100;

        output.append("Guessed " + correct + " correct of " + dataset.size() + " images! (" + perc + "%)");
        output.updateUI();

        finalOut.setText("(" + perc + "%)");
        finalOut.updateUI();
    }
}