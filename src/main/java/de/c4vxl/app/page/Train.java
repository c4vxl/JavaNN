package de.c4vxl.app.page;

import de.c4vxl.app.App;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.training.Datasets;
import de.c4vxl.engine.training.Trainer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Train extends JFrame {
    private App parent;
    private File datasetPath = new File(App.getRunPath(), "finetune.csv");
    private int n_epochs = 100;
    private int n_logging = 3;
    private int n_val = 5;
    private double learning_rate = 1e-7;
    private double weight_decay = 1e-9;
    private boolean shuffle_batches = true;
    private String loss_function = "crossEntropyLoss";
    private String exportPath = "model.mdl";

    private JButton startButton = App.createButton("Start training");

    private JTextArea consoleOutput;

    private boolean isRunning = false;

    public Train(App parentApp) {
        this.parent = parentApp;

        this.setTitle("Training");
        this.setSize(600, 500);
        this.getContentPane().setBackground(App.background);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setIconImage(App.icon.getImage());

        // home button
        JButton homeButton = App.createButton("Home");
        App.setSize(homeButton, 600, 50);
        homeButton.addActionListener(e -> {
            this.setVisible(false);
            parentApp.setVisible(true);
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(App.background);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;

        panel.add(homeButton, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 0, 0);
        JButton selectDatasetButton = App.createButton("Change configuration");
        App.setSize(selectDatasetButton, 600, 50);
        selectDatasetButton.addActionListener(e -> openConfig());
        panel.add(selectDatasetButton, gbc);

        gbc.gridy = 2;

        gbc.insets = new Insets(20, 0, 0, 0);
        consoleOutput = new JTextArea();
        consoleOutput.setBackground(App.background);
        consoleOutput.setForeground(Color.WHITE);
        consoleOutput.setLayout(new BoxLayout(consoleOutput, BoxLayout.Y_AXIS));

        consoleOutput.setLineWrap(true);
        panel.add(consoleOutput, gbc);

        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        App.setSize(scrollPane, 570, 200);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, gbc);

        gbc.gridy = 3;


        this.add(panel, BorderLayout.PAGE_START);

        App.setSize(startButton, 600, 50);
        startButton.addActionListener(e -> startTraining());
        this.add(startButton, BorderLayout.PAGE_END);
    }

    public void openConfig() {
        JFrame frame = new JFrame();
        frame.setTitle("Training configuration");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        frame.getContentPane().setBackground(App.background);
        frame.setResizable(false);
        frame.setIconImage(App.icon.getImage());
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(2, 0, 0, 0);

        JButton datasetBtn = App.createButton("Select dataset");
        App.setSize(datasetBtn, 340, 40);
        datasetBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(App.getRunPath());
            chooser.setFileFilter(new FileNameExtensionFilter(".csv", "csv"));
            chooser.showOpenDialog(frame);

            datasetPath = chooser.getSelectedFile();

            if (datasetPath != null)
                datasetBtn.setText(datasetPath.getName());
            else
                datasetBtn.setText("Select dataset");
        });
        frame.add(datasetBtn, gbc);

        gbc.gridy = 1;

        JButton useShuffleBtn = App.createButton("Shuffle batches: " + shuffle_batches);
        App.setSize(useShuffleBtn, 340, 40);
        useShuffleBtn.addActionListener(e -> {
            shuffle_batches = !shuffle_batches;
            useShuffleBtn.setText("Shuffle batches: " + shuffle_batches);
        });
        frame.add(useShuffleBtn, gbc);

        gbc.gridy = 2;

        JButton epochsBtn = App.createButton("Number of epochs: " + n_epochs);
        App.setSize(epochsBtn, 340, 40);
        epochsBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Select a value (integer)", n_epochs);
            try {
                n_epochs = Integer.parseInt(input);
            } catch (Exception ignored) {return;}
            epochsBtn.setText("Number of epochs: " + n_epochs);
        });
        frame.add(epochsBtn, gbc);

        gbc.gridy = 3;

        JButton logBtn = App.createButton("Logging rate: " + n_logging);
        App.setSize(logBtn, 340, 40);
        logBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Select a value (integer)", n_logging);
            try {
                n_logging = Integer.parseInt(input);
            } catch (Exception ignored) {return;}
            logBtn.setText("Logging rate: " + n_logging);
        });
        frame.add(logBtn, gbc);

        gbc.gridy = 4;

        JButton valBtn = App.createButton("Validation rate: " + n_val);
        App.setSize(valBtn, 340, 40);
        valBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Select a value (integer)", n_val);
            try {
                n_val = Integer.parseInt(input);
            } catch (Exception ignored) {return;}
            valBtn.setText("Validation rate: " + n_val);
        });
        frame.add(valBtn, gbc);

        gbc.gridy = 5;

        JButton lrBtn = App.createButton("Learning rate: " + learning_rate);
        App.setSize(lrBtn, 340, 40);
        lrBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Select a value (double)", learning_rate);
            try {
                learning_rate = Double.parseDouble(input);
            } catch (Exception ignored) {return;}
            lrBtn.setText("Learning rate: " + learning_rate);
        });
        frame.add(lrBtn, gbc);

        gbc.gridy = 6;

        JButton wdBtn = App.createButton("Weight decay: " + weight_decay);
        App.setSize(wdBtn, 340, 40);
        wdBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Select a value (double)", weight_decay);
            try {
                weight_decay = Double.parseDouble(input);
            } catch (Exception ignored) {return;}
            wdBtn.setText("Weight decay: " + weight_decay);
        });
        frame.add(wdBtn, gbc);

        gbc.gridy = 7;

        JButton lossBtn = App.createButton("Loss function: " + loss_function);
        App.setSize(lossBtn, 340, 40);
        lossBtn.addActionListener(e -> {
            Object output = JOptionPane.showInputDialog(frame, "Select a loss function", "Loss function", JOptionPane.PLAIN_MESSAGE, null,
                    List.of("crossEntropyLoss", "meanSquaredError").toArray(), loss_function);
            output = output == null ? loss_function : output.toString();
            loss_function = output.toString();
            lossBtn.setText("Loss function: " + loss_function);
        });
        frame.add(lossBtn, gbc);

        gbc.gridy = 8;

        gbc.insets = new Insets(5, 0, 0, 0);
        JButton exportBtn = App.createButton("Export: " + exportPath);
        App.setSize(exportBtn, 340, 40);
        exportBtn.addActionListener(e -> {
            String output = JOptionPane.showInputDialog(frame, "Path to export to:", exportPath);
            output = output == null ? exportPath : output;
            exportPath = output;
            exportBtn.setText("Export: " + exportPath);
        });
        frame.add(exportBtn, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(40, 0, 0, 0);
        JButton close = App.createButton("Save");
        App.setSize(close, 340, 40);
        close.addActionListener(e -> frame.dispose());
        frame.add(close, gbc);

        frame.setVisible(true);
    }

    public void startTraining() {
        // stop logic
        if (isRunning) {
            isRunning = false;
            startButton.setText("Start training");
            return;
        }
        startButton.setText("Stop training");
        isRunning = true;

        // check if dataset is valid
        if ((datasetPath == null || !datasetPath.isFile())) {
            JOptionPane.showMessageDialog(this, "Invalid dataset!");
            return;
        }

        // split dataset
        List<List<ArrayList<Tensor<Double>>>> splits = Datasets.TrainTestSplit(Datasets.loadClassificationDataset(datasetPath.getPath()));

        consoleOutput.append("Starting training...\n");

        // start training
        new Thread(() -> Trainer.start_training(
                parent.model,
                splits.get(0),
                splits.get(1),
                n_epochs,
                n_logging,
                n_val,
                learning_rate,
                weight_decay,
                shuffle_batches,
                loss_function,
                (epoch, train_loss, val_loss) -> {
                    if (val_loss != null)
                        consoleOutput.append("Epoch: " + epoch + "/" + n_epochs + "; Train loss: " + train_loss + "; Val loss: " + val_loss + "\n");
                    else
                        consoleOutput.append("Epoch: " + epoch + "/" + n_epochs + "; Train loss: " + train_loss + "\n");

                    // clear console when stopping
                    if (!isRunning) {
                        consoleOutput.setText("");
                        startButton.setText("Start training");
                    }

                    return isRunning;
                }
        ).export(new File(App.getRunPath(), exportPath).getPath())).start();
    }
}
