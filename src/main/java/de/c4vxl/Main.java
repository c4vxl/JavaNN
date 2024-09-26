package de.c4vxl;

import de.c4vxl.engine.data.Tensor;

public class Main {
    public static void main(String[] args) {
        Tensor<Double> x = new Tensor<Double>(10, 10).fill(1.0);

        System.out.println(Tensor.of(10, 5, 5));
    }
}