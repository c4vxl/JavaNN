package de.c4vxl;

import de.c4vxl.engine.data.Activation;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static de.c4vxl.MNISTTrain.loadMNIST;

public class Testing extends JFrame {
    private final int width = 28, height = 28;
    private final JPanel probDisplay = new JPanel();
    private MLP model;
    private final ArrayList<ArrayList<Tensor<Double>>> dataset = loadMNIST("test");
    private final JLabel imageLabel = new JLabel();

    public Testing(MLP model) {
        this.model = model;

        this.setTitle("Manual benchmark");
        this.setSize(300, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        probDisplay.setLayout(new BoxLayout(probDisplay, BoxLayout.Y_AXIS));
        add(probDisplay, BorderLayout.EAST);

        imageLabel.setPreferredSize(new Dimension(28*2, 28*2));
        add(imageLabel, BorderLayout.CENTER);

        onNext();

        JButton next = new JButton("Next");
        next.addActionListener(e -> onNext());
        this.add(next, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private void displayProbabilities(Double[] probabilities, int correct) {
        int finalPrediction = IntStream.range(0, probabilities.length)
                .reduce((a, b) -> probabilities[a] > probabilities[b] ? a : b)
                .orElse(-1);

        // clear display
        probDisplay.removeAll();

        probDisplay.add(new JLabel("Prediction:"));

        for (int i = 0; i < probabilities.length; i++)
            probDisplay.add(new JLabel(i + ": " + Math.round(probabilities[i] * 100) + "%"));

        probDisplay.add(new JLabel("Final prediction: " + finalPrediction));
        probDisplay.add(new JLabel("Correct: " + correct));

        probDisplay.updateUI();
    }

    private void onNext() {
        ArrayList<Tensor<Double>> batch = dataset.get(new Random().nextInt(dataset.size())); // get random batch
        Tensor<Double> features = batch.get(0);
        Tensor<Double> label = batch.get(1);
        int correct = Arrays.stream(label.data).toList().indexOf(label.max());

        displayProbabilities(Activation.Softmax(model.forward(features)).data, correct);


        // show image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            image.getRaster().setSample(x, y, 0, (int) (features.data[y * width + x] * 1));
        imageLabel.setIcon(new ImageIcon(image));
    }

    public static void main(String[] args) {
        new Testing(
                (MLP) new MLP(784, 10, 3, 16)
                        .load("models/digitRecognition.mdl")
        );
    }
}