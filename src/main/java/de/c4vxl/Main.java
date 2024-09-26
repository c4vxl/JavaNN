package de.c4vxl;

import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.module.Linear;

public class Main {
    public static void main(String[] args) {
        Linear layer = new Linear(4, 2);
        Tensor<Double> input = new Tensor<>(1, 4);

        System.out.println(layer.forward(input));
    }
}