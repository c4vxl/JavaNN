package de.c4vxl;

import de.c4vxl.engine.data.Tensor;

public class Main {
    public static void main(String[] args) {
        Tensor<Double> x = new Tensor<Double>(10, 10, 1).fill(1.0);

        System.out.println(x.transpose(0, -1, 1));
    }
}