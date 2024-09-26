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