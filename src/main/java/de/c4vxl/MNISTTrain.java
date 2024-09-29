package de.c4vxl;

import de.c4vxl.engine.data.LossFunction;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MNISTTrain {
    public static void main(String[] args) {
        int N_EPOCHS = 50;
        int LOG_RATE = 2;
        int BACKUP_RATE = 20;
        int VAL_RATE = 5;
        double LEARNING_RATE = 1e-9;
        double WEIGHT_DECAY = 1e-10;

        // load dataset
        System.out.println("Loading dataset...");
        List<ArrayList<Tensor<Double>>> train_split = loadMNIST("train");
        List<ArrayList<Tensor<Double>>> test_split = loadMNIST("test");
        train_split = train_split.subList(0, Math.max(train_split.size() / 50, 0)); // only use the first fifth of the actual dataset for testing
        System.out.println("Train split size: " + train_split.size());
        System.out.println("Test split size: " + test_split.size());

        // load model
        System.out.println("Initializing model...");
        MLP model = (MLP) new MLP(
                784,  // dataset contains 28*28 pixel images (= 784)
                10,         // 10 outputs (0-9)
                2,          // amount of hidden layers
                12          // size of hidden layers
        ).load("models/digitRecognition.mdl"); // load from model file

        for (int epoch = 0; epoch < N_EPOCHS; epoch++) {
            double train_loss = .0;
            for (ArrayList<Tensor<Double>> batch : train_split) {
                Tensor<Double> label = batch.get(1);
                Tensor<Double> features = batch.get(0);

                // forward
                Tensor<Double> prediction = model.forward(features);
                Tensor<Double> loss = LossFunction.crossEntropyLoss(prediction, label);

                train_loss += loss.item();

                // compute gradient and back propagate
                Tensor<Double> gradient = prediction.sub(label).mul(2.0); // p_i - y_i
                model.backward(gradient, LEARNING_RATE, WEIGHT_DECAY);
            }

            // shuffle batches
            Collections.shuffle(train_split);

            train_loss /= train_split.size();

            // logging
            if (epoch % LOG_RATE == 0) {
                if (epoch % VAL_RATE == 0) {
                    double val_loss = .0;
                    for (ArrayList<Tensor<Double>> batch : test_split) {
                        val_loss += LossFunction.crossEntropyLoss(
                                model.forward(batch.get(0)), // y_pred
                                batch.get(1)                 // label
                        ).item();
                    }
                    val_loss /= test_split.size();

                    System.out.println("Epoch: " + epoch + "/" + N_EPOCHS + "; Train loss: " + train_loss + "; Val loss: " + val_loss);
                }
                else
                    System.out.println("Epoch: " + epoch + "/" + N_EPOCHS + "; Train loss: " + train_loss);
            }

            // backup
            if (epoch % BACKUP_RATE == 0)
                model.export("Model-training-" + epoch + ".mdl");
        }

        // save final model
        model.export("models/digitRecognition.mdl");
    }

    public static ArrayList<ArrayList<Tensor<Double>>> loadMNIST(String split) {
        try {
            String[] file = Files.readString(Path.of("dataset/mnist_" + split + ".csv")).split("\n");

            ArrayList<ArrayList<Tensor<Double>>> dataset = new ArrayList<>();

            // start at 1 because we want to skip the first line (with the labels of the rows)
            for (int i = 1; i < file.length; i++) {
                String line = file[i];
                String[] entries = line.split(",");

                // get label
                Tensor<Double> label = Tensor.of(0.0, 1, 10);
                label.data[Integer.parseInt(entries[0])] = 1.0;

                Double[] data = Arrays.stream(Arrays.copyOfRange(entries, 1, entries.length))
                        .map(Double::valueOf)
                        .toArray(Double[]::new);;
                Tensor<Double> features = new Tensor<>(data, 1, data.length);

                dataset.add(new ArrayList<>() {{
                    add(features);
                    add(label);
                }});
            }

            return dataset;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}