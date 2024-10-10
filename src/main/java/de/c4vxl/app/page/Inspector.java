package de.c4vxl.app.page;

import de.c4vxl.app.App;
import de.c4vxl.engine.training.Datasets;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Inspector extends JFrame {
    private int currentImage = 0;
    private JLabel label = new JLabel("Current position: -1/-1");
    private List<ArrayList<Object>> dataset;
    private JLabel labelName = new JLabel("Label: -1");
    private JPanel canvas = new JPanel();
    private App parent;

    @SuppressWarnings("unchecked")
    public Inspector(App parentApp) {
        parent = parentApp;

        this.setTitle("Dataset inspector");
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


        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(App.background);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;

        JButton datasetBtn = App.createButton("Select dataset");
        datasetBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(App.getRunPath());
            chooser.setFileFilter(new FileNameExtensionFilter(".csv", "csv"));
            chooser.showOpenDialog(this);

            File datasetPath = chooser.getSelectedFile();

            if (datasetPath != null) {
                datasetBtn.setText(datasetPath.getName());
                dataset = new ArrayList<>(Datasets.loadClassificationDataset(datasetPath.getPath())
                         .stream().map(x ->
                                 new ArrayList<Object>(List.of(
                                         x.get(0).data,                                                // features
                                         Arrays.stream(x.get(1).data).toList().indexOf(x.get(1).max()) // label
                                 ))
                         ).toList());

                update();
            } else
                datasetBtn.setText("Select dataset");
        });

        panel.add(homeButton, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 0, 0, 0);
        panel.add(datasetBtn, gbc);

        gbc.insets = new Insets(15, 0, 0, 0);

        gbc.gridy = 3;
        label.setForeground(Color.WHITE);
        panel.add(label, gbc);

        gbc.insets = new Insets(30, 0, 0, 0);
        gbc.gridy = 4;
        App.setSize(canvas, 28 * 3, 28 * 3);
        canvas.setBackground(Color.BLACK);
        panel.add(canvas, gbc);

        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridy = 5;
        labelName.setForeground(Color.WHITE);
        panel.add(labelName, gbc);

        this.add(panel, BorderLayout.PAGE_START);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        buttons.setBackground(App.background);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;


        JButton jumpBtn = App.createButton("Jump to image");
        jumpBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Select a value (integer)", currentImage);
            try {
                int n = Integer.parseInt(input);
                if (n < 0) return;
                currentImage = n; // update currentImage Position
                update(); // update display
            } catch (Exception ignored) {}
        });
        buttons.add(jumpBtn, gbc);
        gbc.insets = new Insets(2, 0, 0, 0);
        gbc.gridy = 1;

        JButton nextBtn = App.createButton("Next");
        nextBtn.addActionListener(e -> {
            currentImage++;
            update();
        });
        buttons.add(nextBtn, gbc);

        this.add(buttons, BorderLayout.PAGE_END);
    }

    public void update() {
        if (dataset == null) {
            JOptionPane.showMessageDialog(this, "No dataset selected!");
            return;
        }

        // prevent overflow
        if (currentImage >= dataset.size())
            currentImage = 0;

        label.setText("Current position: " + currentImage + "/" + dataset.size());

        Double[] pixels = (Double[]) dataset.get(currentImage).get(0);
        Integer label = ((Number) dataset.get(currentImage).get(1)).intValue();

        // logging
        System.out.println(label + ": " + Arrays.toString(pixels));

        labelName.setText("Label: " + label);

        BufferedImage image = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int value = Math.max(0, Math.min(255, (int) Math.round(pixels[y * 28 + x])));
                int rgbValue = (value << 16) | (value << 8) | value; // Grays
                image.setRGB(x, y, rgbValue);
            }
        }

        canvas.getGraphics().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
    }
}