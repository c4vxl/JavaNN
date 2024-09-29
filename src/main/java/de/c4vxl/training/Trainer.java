package de.c4vxl.training;

import de.c4vxl.engine.data.LossFunction;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.nn.MLP;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trainer {
    @FunctionalInterface
    public interface LoggingFunction<T, U, V> {
        void apply(T t, U u, V v);
    }

    @SuppressWarnings("unchecked")
    private static <T> T lossFunction(Method lossFunction, T a, T b) {
        try {
            return (T) lossFunction.invoke(null, a, b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MLP start_training(
            MLP model,
            List<ArrayList<Tensor<Double>>> train_split,
            List<ArrayList<Tensor<Double>>> test_split,
            int N_EPOCHS,
            int LOG_RATE,
            int VAL_RATE,
            double LEARNING_RATE,
            double WEIGHT_DECAY,
            boolean shuffle_batches,
            String lossFunction
    ) {
        return start_training(model, train_split, test_split, N_EPOCHS, LOG_RATE, VAL_RATE, LEARNING_RATE, WEIGHT_DECAY, shuffle_batches, lossFunction,
                (epoch, train_loss, val_loss) -> {
                    if (val_loss != null)
                        System.out.println("Epoch: " + epoch + "/" + N_EPOCHS + "; Train loss: " + train_loss + "; Val loss: " + val_loss);
                    else
                        System.out.println("Epoch: " + epoch + "/" + N_EPOCHS + "; Train loss: " + train_loss);
                });
    }

    public static MLP start_training(
            MLP model,
            List<ArrayList<Tensor<Double>>> train_split,
            List<ArrayList<Tensor<Double>>> test_split,
            int N_EPOCHS,
            int LOG_RATE,
            int VAL_RATE,
            double LEARNING_RATE,
            double WEIGHT_DECAY,
            boolean shuffle_batches,
            String lossFunction,
            LoggingFunction<Integer, Double, Double> onLogging) {
        // log
        System.out.println("Starting training... \n  - Train split size: " + train_split.size() + "\n  - Test split size: " + test_split.size());

        for (int epoch = 0; epoch < N_EPOCHS; epoch++) {
            // load loss function
            Method lossCalculator;
            try {
                lossCalculator = LossFunction.class.getMethod(lossFunction, Tensor.class, Tensor.class);
            } catch (NoSuchMethodException e) {
                System.err.println("Invalid loss function provided. Exiting!");
                return model;
            }

            double train_loss = .0;
            for (ArrayList<Tensor<Double>> batch : train_split) {
                Tensor<Double> label = batch.get(1);
                Tensor<Double> features = batch.get(0);

                // forward
                Tensor<Double> prediction = model.forward(features);
                Tensor<Double> loss = lossFunction(lossCalculator, prediction, label);

                train_loss += loss.item();

                // compute gradient and back propagate
                Tensor<Double> gradient = prediction.sub(label).mul(2.0); // p_i - y_i
                model.backward(gradient, LEARNING_RATE, WEIGHT_DECAY);
            }
            train_loss /= train_split.size();

            // shuffle batches
            if (shuffle_batches) Collections.shuffle(train_split);

            // logging
            if (epoch % LOG_RATE == 0) {
                if (epoch % VAL_RATE == 0) {
                    double val_loss = .0;
                    for (ArrayList<Tensor<Double>> batch : test_split) {
                        val_loss += lossFunction(lossCalculator,
                                model.forward(batch.get(0)), // y_pred
                                batch.get(1)                 // label
                        ).item();
                    }
                    val_loss /= test_split.size();

                    onLogging.apply(epoch, train_loss, val_loss);
                }
                else
                    onLogging.apply(epoch, train_loss, null);
            }
        }

        return model;
    }
}