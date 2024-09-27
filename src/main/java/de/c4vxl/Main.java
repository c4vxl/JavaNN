package de.c4vxl;

import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

public class Main {
    public static void main(String[] args) {
        MLP mlp = new MLP(4, 2, 1, 5);
        System.out.println(mlp.toJSON());


        for (int i = 0; i < 500; i++) {
            System.out.println(mlp.forward(Tensor.of(2.0, 1, 4)));
        }
    }
}