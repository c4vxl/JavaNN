package de.c4vxl;

import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.module.Linear;

public class Main {
    public static void main(String[] args) {
        Linear layer = new Linear(4, 2);
        Tensor<Double> input = new Tensor<>(1, 4);
        Tensor<Double> wanted = new Tensor<>(new Double[]{0.0, 5.0});

        for (int i = 0; i < 40; i++) {
            // do forward pass
            Tensor<Double> out = layer.forward(input.clone());

            // compute loss
            double loss = out.sub(wanted).pow(2.0).sum(0).item();
            System.out.println("Loss: " + loss);

            // calculate gradient (2*(out - wanted))
            Tensor<Double> gradient = out.sub(wanted).mul(2.0);

            // do backward pass
            layer.backward(gradient, 0.0001, 0.001);
        }
    }
}