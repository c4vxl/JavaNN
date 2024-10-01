package de.c4vxl.app.page;

import de.c4vxl.app.App;
import de.c4vxl.app.CanvasPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CreateDataset extends JFrame {
    private final CanvasPanel canvas = new CanvasPanel();
    private int picturesPerNumber = 15; // let the user draw 15 pictures for each number (0-9)
    private JLabel label = new JLabel("Draw a 0 (1/" + picturesPerNumber + ")");
    private int currentImage = 0;
    private JButton nextButton = App.createButton("Next");
    private ArrayList<int[]> dataset = new ArrayList<>();

    private App parent;

    public CreateDataset(App parentApp) {
        parent = parentApp;

        this.setTitle("Create Dataset");
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

        // redraw button
        JButton redrawButton = App.createButton("Redraw");
        redrawButton.addActionListener(e -> {
            canvas.clear();
        });

        // next button
        nextButton.addActionListener(e -> next());

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(App.background);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;

        panel.add(homeButton, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        label.setForeground(Color.WHITE);
        panel.add(label, gbc);

        gbc.insets = new Insets(20, 0, 0, 0);
        gbc.gridy = 2;
        panel.add(canvas, gbc);

        this.add(panel, BorderLayout.PAGE_START);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        buttons.setBackground(App.background);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;

        buttons.add(redrawButton, gbc);

        gbc.insets = new Insets(2, 0, 0, 0);
        gbc.gridy = 1;
        buttons.add(nextButton, gbc);

        this.add(buttons, BorderLayout.PAGE_END);
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
        dataset.add(Arrays.stream(canvas.getDrawnPixels()).mapToInt(Double::intValue).toArray());

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
            File outputFile = new File(App.getRunPath(), JOptionPane.showInputDialog("Path to save:", "finetune.csv"));
            outputFile.createNewFile();

            PrintWriter writer = new PrintWriter(new FileWriter(outputFile.getPath()));
            writer.print(fileContent);
            writer.close();

            // open file-explorer
            Desktop.getDesktop().open(outputFile.getParentFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // close jFrame
        this.setVisible(false);
        parent.setVisible(true);

        JOptionPane.showMessageDialog(this, "Your dataset has been saved!");
    }
}