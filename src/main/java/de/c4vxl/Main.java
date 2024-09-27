package de.c4vxl;

import de.c4vxl.engine.data.LossFunction;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

public class Main {
    public static void main(String[] args) {
        // This is an example of how Training could look like
        // when running you can see the loss decreasing as the model learns to produce the wanted shape ($wanted)

        MLP model = new MLP(4, 10, 1, 5);
        model.load("model.mdl");
        Tensor<Double> wanted = Tensor.of(0.0, 1, 10);
        wanted.data[2] = 6.0;

        Tensor<Double> input = Tensor.of(12.0, 1, 4);

        for (int i = 0; i < 5; i++) {
            Tensor<Double> out = model.forward(input);
            Tensor<Double> loss = LossFunction.meanSquaredError(out, wanted);
            System.out.println(loss.item());

            Tensor<Double> gradient = out.sub(wanted).mul(2.0);

            model.backward(gradient, 0.00001, 0.001);
        }

        model.export("model.mdl");
    }
}
