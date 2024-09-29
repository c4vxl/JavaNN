package de.c4vxl;

import de.c4vxl.engine.nn.MLP;
import de.c4vxl.training.Datasets;
import de.c4vxl.training.Trainer;

public class MNISTTrain {
    public static void main(String[] args) {
        Trainer.start_training(
                (MLP) new MLP(784, 10, 2, 12).load("models/digitRecognition.mdl"),
                Datasets.MNIST("train").subList(0, 500),
                Datasets.MNIST("test"),
                50,
                5,
                10,
                1e-9,
                1e-10,
                true,
                "crossEntropyLoss"
        ).export("models/digitRecognition.mdl"); // export model after training
    }
}