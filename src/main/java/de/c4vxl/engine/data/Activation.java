package de.c4vxl.engine.data;

public class Activation {
    /**
     * Rectified linear unit
     */
    public static <T> Tensor<T> ReLU(Tensor<T> input) {
        return input.clone().clip(input.zeroValue(), input.max());
    }
}