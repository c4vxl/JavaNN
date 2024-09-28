package de.c4vxl;

import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

import java.util.ArrayList;
import java.util.Arrays;

import static de.c4vxl.MNISTTrain.loadMNIST;

public class Benchmark {
    public static void main(String[] args) {
        // load dataset
        ArrayList<ArrayList<Tensor<Double>>> dataset = loadMNIST("test");

        // initialize model
        MLP model = (MLP) new MLP(784, 10, 3,16)
                .load("models/digitRecognition.mdl"); // load from model file

        int correct = 0;
        for (ArrayList<Tensor<Double>> batch : dataset) {
            // get label and features
            Tensor<Double> label = batch.get(1);
            int wanted = Arrays.stream(label.data).toList().indexOf(label.max());
            Tensor<Double> features = batch.get(0);

            // make prediction
            Tensor<Double> y_pred = model.forward(features);
            int predicted = Arrays.stream(y_pred.data).toList().indexOf(y_pred.max());

            // logging
            System.out.println("Predicted: " + predicted + " \t Wanted: " + wanted);

            if (predicted == wanted)
                correct++;
        }

        // logging
        System.out.println("Guessed " + correct + " correct of " + dataset.size() + " images! (" + ((double) correct / dataset.size()) * 100 + "%)");
    }
}