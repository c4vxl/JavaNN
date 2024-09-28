package de.c4vxl.engine.data;

import java.util.Arrays;

public class Activation {
    /**
     * Rectified linear unit
     */
    public static <T> Tensor<T> ReLU(Tensor<T> input) {
        return input.clone().clip(input.zeroValue(), input.max());
    }

    /**
     * Compute a softmax on the data in the Tensor
     */
    public static Tensor<Double> Softmax(Tensor<Double> tensor) {
        Double[] data = tensor.data;
        double maxLogit = tensor.max();
        double sumExp = Arrays.stream(data)
                .mapToDouble(x -> Math.exp(x - maxLogit))
                .sum();
        Double[] softmax = Arrays.stream(data)
                .map(x -> Math.exp(x - maxLogit) / sumExp)
                .toArray(Double[]::new);
        return new Tensor<>(softmax, tensor.shape);
    }
}