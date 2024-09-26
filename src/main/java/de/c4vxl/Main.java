package de.c4vxl;

import de.c4vxl.engine.data.Tensor;

public class Main {
    public static void main(String[] args) {
        Tensor<Integer> x = new Tensor<>(Integer.class, 10, 10);
        Tensor<Integer> y = new Tensor<>(Integer.class, 10, 10);

        System.out.println(x.matmul(y));
    }
}