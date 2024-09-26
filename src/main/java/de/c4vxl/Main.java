package de.c4vxl;

import de.c4vxl.engine.data.Tensor;

public class Main {
    public static void main(String[] args) {
        Tensor<Boolean> x = new Tensor<>(Boolean.class, 10, 10, 1);

        System.out.println(x.zeroValue());
    }
}