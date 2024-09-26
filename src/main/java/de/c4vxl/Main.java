package de.c4vxl;

import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.module.Linear;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Linear layer = new Linear(4, 2);
        Tensor<?> input = new Tensor<>(1, 4);

        Tensor<Double> bef = layer.weight.clone();
        System.out.println(Arrays.equals(bef.data, ((Linear) layer.load_state(layer.state())).weight.data));
    }
}