package de.c4vxl.engine.module;

import de.c4vxl.engine.data.Module;
import de.c4vxl.engine.data.Tensor;

import java.util.Random;

/**
 * Object for linear transformation by multiplying with weights and adding an optional bias
 *
 * @author c4vxl
 */
public class Linear extends Module {
    public int n_inp;
    public int n_out;
    public boolean useBias;

    public Tensor<Double> weight;
    public Tensor<Double> bias;

    // cache the input to use it in the backward pass
    private Tensor<Double> lastInput;

    public Linear(int input_features, int output_features) { this(input_features, output_features, true); }
    public Linear(int input_features, int output_features, boolean bias) {
        this.n_inp = input_features;
        this.n_out = output_features;
        this.useBias = bias;

        this.weight = new Tensor<>(n_inp, n_out);
        this.bias = bias ? Tensor.of(0.0, output_features) : null;

        // Xavier/Glorot initialization
        double limit = Math.sqrt(6.0 / (n_inp + n_out));
        Random rand = new Random();
        for (int i = 0; i < weight.data.length; i++) {
            weight.data[i] = rand.nextDouble() * 2 * limit - limit; // Xavier initialization
        }
    }

    /**
     * Forward pass
     */
    public Tensor<Double> forward(Tensor<Double> x) {
        this.lastInput = x.clone(); // store last input for backprop

        x = x.matmul(this.weight);

        if (useBias && this.bias.shape == x.shape)
            x = x.add(this.bias);

        return x;
    }

    /**
     * Backward pass
     */
    public Tensor<Double> backward(Tensor<Double> grad, double lr, double wd) {
        // compute gradient with respect to weight (lastInp^T @ gradOut)
        Tensor<Double> gradWeight = lastInput.transpose().matmul(grad); // clip gradient;

        System.out.println(weight);

        // update weights
        weight = weight.sub(weight.mul(Tensor.of(wd, weight.shape))); // apply weight decay
        weight = weight.sub(gradWeight.mul(Tensor.of(lr, weight.shape))); // change weight with respect to grad and learning rate

        System.out.println(weight);

        if (useBias) {
            // compute bias gradient
            Tensor<Double> gradBias = grad.sum(0).unsqueeze(0);

            // update bias
            bias = bias.sub(gradBias.mul(Tensor.of(lr, bias.shape)));
        }

        // compute gradient with respect to input to prop the previous layer
        return grad.matmul(weight.transpose());
    }

    @Override
    public String toString() {
        return "Linear{" +
                "n_inp=" + n_inp +
                ", n_out=" + n_out +
                ", useBias=" + useBias +
                ", weight=" + weight +
                ", bias=" + bias +
                '}';
    }
}