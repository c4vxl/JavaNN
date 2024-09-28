package de.c4vxl;

import de.c4vxl.engine.data.LossFunction;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

public class Main {
    public static void main(String[] args) {
        // This is an example of how Training could look like
        // when running you can see the loss decreasing as the model learns to produce the wanted shape ($wanted)

        MLP model = new MLP(4, 2, 4, 12);

        Tensor<Double> label = new Tensor<>(new Double[]{0.0, 1.0}, 1, 2); // example label (desired output)
        Tensor<Double> input = new Tensor<>(1, 4); // example input

        double lastLoss = Double.MAX_VALUE;
        for (int epoch = 0; epoch < 5; epoch++) {
            Tensor<Double> output = model.forward(input);
            double loss = LossFunction.crossEntropyLoss(output, label).item();

            if (lastLoss > loss) {
                System.out.println("Loss has decreased: " + loss + " (by " + (lastLoss - loss) + ")");
            } else {
                System.out.println("Loss has increased: " + loss + " (by " + (loss - lastLoss) + ")");
            }
            lastLoss = loss;

            model.backward(
                    output.sub(label).mul(2.0), // calculate gradient
                    1e-1,                              // learning rate
                    1e-3                               // weight decay
            );
        }
    }
}
