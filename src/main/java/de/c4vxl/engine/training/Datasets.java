package de.c4vxl.engine.training;

import de.c4vxl.engine.data.Tensor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Datasets {
    private static String readDatasetFile(String path) {
        try {
            File file = new File(path);
            // remove first line as it only contains the labels
            String[] lines = Files.readString(file.toPath()).split("\n");
            return String.join("\n", Arrays.copyOfRange(lines, 1, lines.length));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<ArrayList<Tensor<Double>>> loadClassificationDataset(String csvPath) {
        // Read dataset
        String csv = readDatasetFile(csvPath);
        String[] lines = csv.split("\n");

        Stream<ArrayList<Tensor<Double>>> dataset = Arrays.stream(lines)
                .map(value -> {
                    String[] parts = value.split(","); // Split by comma
                    String labelStr = parts[0];
                    Double[] featuresData = Arrays.stream(parts, 1, parts.length) // Get features starting from index 1
                            .map(Double::valueOf)
                            .toArray(Double[]::new);

                    // Create label
                    Tensor<Double> label = Tensor.of(0.0, 1, 10); // Assuming you have 10 classes
                    label.data[Integer.parseInt(labelStr)] = 1.0;

                    // Create features tensor
                    Tensor<Double> features = new Tensor<>(featuresData, 1, featuresData.length);

                    // Return a new ArrayList containing features and label
                    return new ArrayList<Tensor<Double>>() {{
                        add(features);
                        add(label);
                    }};
                });

        return new ArrayList<>(dataset.toList());
    }

    public static ArrayList<ArrayList<Tensor<Double>>> MNIST(String split) {
        return loadClassificationDataset("dataset/mnist_" + split + ".csv");
    }

    public static <T> List<List<T>> TrainTestSplit(List<T> dataset) { return TrainTestSplit(dataset, .8); }
    public static <T> List<List<T>> TrainTestSplit(List<T> dataset, double splitPercentage) {
        int splitIndex = (int) (dataset.size() * splitPercentage);
        return List.of(dataset.subList(0, splitIndex), dataset.subList(splitIndex, dataset.size()));
    }
}