package de.c4vxl.engine.training;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.c4vxl.engine.data.Tensor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
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



    public static class HuggingFaceAPI {
        public static List<List<String>> getDatasetList(String query) {
            try {
                // read the json from the api
                String content = new String(
                        new URL("https://huggingface.co/api/datasets?search=" + query + "%20csv&limit=500&full=true")
                                .openStream().readAllBytes()
                );


                return JsonParser.parseString(content).getAsJsonArray().asList() // parse api response
                        .stream().map((dataset) -> {
                            JsonObject ds = dataset.getAsJsonObject();
                            return List.of(
                                    String.valueOf(ds.get("id")),
                                    String.valueOf(ds.get("author")),
                                    String.valueOf(ds.get("sha")),
                                    String.valueOf(ds.get("description")),
                                    String.valueOf(ds.get("createdAt"))
                            );
                        }).toList();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static File downloadDataset(String dataset, String file, String outputFile) {
            try {
                String content = new String(new URL("https://huggingface.co/datasets/" + dataset + "/resolve/main/" + file)
                        .openStream().readAllBytes());

                // create file
                File output = new File(outputFile);
                if (output.getParentFile() != null) output.getParentFile().mkdirs();
                output.createNewFile();

                // export
                PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
                writer.print(content);
                writer.close();

                return output;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static JsonObject getDataset(String name) {
            try {
                String content = new String(new URL("https://huggingface.co/api/datasets/" + name + "?full=true")
                        .openStream().readAllBytes());

                return JsonParser.parseString(content).getAsJsonObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public static List<String> getDatasetFiles(String dataset) {
            List<String> files = new ArrayList<>(getDataset(dataset).get("siblings").getAsJsonArray().asList()
                    .stream().map((x) -> x.getAsJsonObject().get("rfilename").getAsString()).toList());

            files.remove(".gitattributes"); // ignore .gitattributes

            return files;
        }
    }
}