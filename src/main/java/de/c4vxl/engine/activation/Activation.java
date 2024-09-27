package de.c4vxl.engine.activation;

import de.c4vxl.engine.data.Tensor;

public class Activation {
    /**
     * Rectified linear unit
     */
    public static <T> Tensor<T> ReLU(Tensor<T> input) {
        return input.clone().clip(input.zeroValue(), input.max());
    }
}