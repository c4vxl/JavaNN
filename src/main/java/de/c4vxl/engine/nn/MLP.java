package de.c4vxl.engine.nn;

import de.c4vxl.engine.data.Module;
import de.c4vxl.engine.data.Tensor;
import de.c4vxl.engine.module.Linear;

import java.util.ArrayList;
import java.util.Collections;

public class MLP extends Module {
    public Linear inp_proj;
    public Linear out_proj;
    public ArrayList<Linear> hiddenLayers = new ArrayList<>();

    public MLP(int n_inp, int n_out, int n_hid, int hidden_size) {
        // init input and output projection
        this.inp_proj = new Linear(n_inp, hidden_size);
        this.out_proj = new Linear(hidden_size, n_out);

        // init hidden layers
        for (int i = 0; i < n_hid; i++)
            hiddenLayers.add(new Linear(hidden_size, hidden_size));
    }

    /**
     * Forward pass
     */
    public Tensor<Double> forward(Tensor<Double> x) {
        x = inp_proj.forward(x);

        for (Linear layer : hiddenLayers)
            x = layer.forward(x);

        return out_proj.forward(x);
    }

    /**
     * Backward pass
     */
    @SuppressWarnings("unchecked")
    public void backward(Tensor<Double> grad, double lr, double wd) {
        grad = out_proj.backward(grad, lr, wd);

        ArrayList<Linear> layersCopy = (ArrayList<Linear>) hiddenLayers.clone();
        Collections.reverse(layersCopy);
        for (Linear layer : layersCopy) {
            grad = layer.backward(grad, lr, wd);
        }

        inp_proj.backward(grad, lr, wd);
    }
}