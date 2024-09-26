package de.c4vxl.engine.data;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;

/**
 * A Tensor can be understood as a multidimensional matrix of different datatypes capable of performing various mathematical operations.
 * These operations include element-wise computation, matrix multiplication, reshaping, transposing, and more.
 *
 * @author c4vxl
 */
@SuppressWarnings("unchecked")
public class Tensor<T> {
    // default data type
    public static Class<?> defaultDType = Double.class;


    public T[] data;
    public Class<T> dtype;
    public int size;
    public int[] shape;

    /**
     * Construct with default dtype
     */
    public Tensor(int... shape) { this((Class<T>) defaultDType, shape); }

    /**
     * Construct with dtype
     */
    public Tensor(Class<T> dtype, int... shape) {
        this((T[]) Array.newInstance(dtype, shapeToSize(shape)), shape);

        for (int i = 0; i < data.length; i++) {
            Random rand = new Random();

            if (dtype == Double.class) data[i] = (T) Double.valueOf(rand.nextDouble());
            else if (dtype == Integer.class) data[i] = (T) Integer.valueOf(rand.nextInt(99));
            else if (dtype == Long.class) data[i] = (T) Long.valueOf(rand.nextLong());
            else if (dtype == Float.class) data[i] = (T) Float.valueOf(rand.nextFloat());
            else if (dtype == Boolean.class) data[i] = (T) Boolean.valueOf(rand.nextBoolean());
            else throw new IllegalArgumentException("Unsupported dtype '" + dtype.getSimpleName() + "'");
        }
    }

    /**
     * Construct from data
     */
    public Tensor(T[] data, int... shape) {
        this.shape = shape;
        this.size = shapeToSize(shape);
        this.dtype = (Class<T>) data.getClass().getComponentType();
        this.data = data;
    }

    /**
     * Construct a tensor filled with a set object
     */
    public static <T> Tensor<T> of(T obj, int... shape) {
        return new Tensor<T>((Class<T>) obj.getClass(), shape).fill(obj);
    }

    /**
     * Helper function for converting the shape of a tensor to the amount of data which can be stored in it
     */
    public static Integer shapeToSize(int... shape) { return Arrays.stream(shape).reduce(1, (a, b) -> a * b); }

    /**
     * Fill this Tensor with values
     */
    public Tensor<T> fill(T obj) {
        Arrays.fill(data, obj);
        return this;
    }

    /**
     * Get the item of the Tensor
     */
    public T item() {
        return data[0];
    }

    /**
     * Get the representation of 0 in the current dtype
     */
    public T zeroValue() {
        try {
            return (T) dtype.getMethod("valueOf", String.class).invoke(null, "0");
        } catch (Exception e) { // should never be triggered as we check the dtype on construction
            return null;
        }
    }

    /**
     * Run an operation element wise
     */
    public Tensor<T> elementWiseIndexed(BiFunction<T, Integer, Object> task) {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = (T) task.apply(this.data[i], i);
        }

        return this;
    }

    public T operate(T a, T b, BiFunction<Double, Double, Double> operation) {
        if (dtype == Boolean.class)
            throw new RuntimeException("Operation can not be performed on dtype 'Boolean'");

        double result = operation.apply(((Number) a).doubleValue(), ((Number) b).doubleValue());
        return dtype == Float.class ? (T) Float.valueOf((float) result)
                : dtype == Long.class ? (T) Long.valueOf((long) result)
                : dtype == Integer.class ? (T) Integer.valueOf((int) result)
                : (T) Double.valueOf(result);
    }

    /**
     * Element wise addition
     */
    public Tensor<T> add(T other) { return add(of(other, this.shape)); }
    public Tensor<T> add(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            return operate(a, otherData[index], Double::sum);
        });
    }

    /**
     * Element wise subtraction
     */
    public Tensor<T> sub(T other) { return sub(of(other, this.shape)); }
    public Tensor<T> sub(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            return operate(a, otherData[index], (t, o) -> t - o);
        });
    }

    /**
     * Element wise division
     */
    public Tensor<T> div(T other) { return div(of(other, this.shape)); }
    public Tensor<T> div(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            return operate(a, otherData[index], (t, o) -> t / o);
        });
    }

    /**
     * Element wise multiplication
     */
    public Tensor<T> mul(T other) { return mul(of(other, this.shape)); }
    public Tensor<T> mul(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            return operate(a, otherData[index], (t, o) -> t * o);
        });
    }

    /**
     * Element wise square root
     */
    public Tensor<T> sqrt() {
        if (dtype != Double.class)
            throw new RuntimeException("Operation can only be performed on dtype 'Double'");

        return this.elementWiseIndexed((a, index) -> {
            return Math.sqrt((Double) a);
        });
    }

    /**
     * Element wise raise to power
     */
    public Tensor<T> pow(double pow) {
        if (dtype != Double.class)
            throw new RuntimeException("Operation can only be performed on dtype 'Double'");

        return this.elementWiseIndexed((a, index) -> {
            return Math.pow((double) a, pow);
        });
    }

    /**
     * Element wise logarithm
     */
    public Tensor<T> log() {
        if (dtype != Double.class)
            throw new RuntimeException("Operation can only be performed on dtype 'Double'");

        return this.elementWiseIndexed((a, index) -> {
            return Math.log((double) a);
        });
    }

    /**
     * Element wise clip
     */
    public Tensor<T> clip(T min, T max) {
        if (dtype == Boolean.class)
            throw new RuntimeException("Operation can not be performed on dtype 'Boolean'");

        return elementWiseIndexed((a, index) -> {
            a = operate(a, max, (s, m) -> s > m ? m : s); // clip maximum
            return operate(a, min, (s, m) -> s < m ? m : s); // clip minimum
        });
    }

    /**
     * Sum across one dimension
     */
    public Tensor<T> sum(int dim) {
        if (dim < 0 || dim >= this.shape.length)
            throw new IllegalArgumentException("Invalid dimension specified.");
        if (dtype == Boolean.class)
            throw new RuntimeException("Operation can not be performed on dtype 'Boolean'");

        // Calculate the new shape after summing over the specified dimension
        int[] newShape = Arrays.copyOf(this.shape, this.shape.length - 1);
        for (int i = dim; i < newShape.length; i++) {
            newShape[i] = this.shape[i + 1];
        }

        // Prepare the result data array
        T[] resultData = (T[]) Array.newInstance(dtype, shapeToSize(newShape));

        // Sum elements along the specified dimension
        int[] indices = new int[this.shape.length];
        for (int i = 0; i < resultData.length; i++) {
            T sum = zeroValue();
            for (indices[dim] = 0; indices[dim] < this.shape[dim]; indices[dim]++) {
                int index = 0;
                for (int j = 0; j < this.shape.length; j++) {
                    index += (j < dim) ? indices[j] : (j == dim) ? 0 : indices[j - 1];
                    index *= this.shape[j];
                }
                sum = operate(sum, data[index], Double::sum);
            }
            resultData[i] = sum;

            // Increment indices
            for (int j = newShape.length - 1; j >= 0; j--) {
                if (++indices[j] < newShape[j]) break;
                indices[j] = 0;
            }
        }

        return new Tensor<>(resultData, newShape);
    }

    public Tensor<T> matmul(Tensor<T> b) {
        if (this.shape.length != 2 || b.shape.length != 2) throw new IllegalArgumentException("Matrix multiplication can only be done with 2D tensors.");
        if (this.shape[1] != b.shape[0]) throw new IllegalArgumentException("Number of columns of the first matrix must equal the number of rows of the second matrix.");

        int m = this.shape[0];
        int n = this.shape[1];
        int p = b.shape[1];
        T[] resultData = (T[]) Array.newInstance(dtype, m * p);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                T sum = zeroValue();
                for (int k = 0; k < n; k++) {
                    T a = this.data[i * n + k];
                    T o = b.data[k * p + j];

                    sum = operate(sum, operate(a, o, (t, c) -> t * c), Double::sum);
                }
                resultData[i * p + j] = sum;
            }
        }

        return new Tensor<>(resultData, m, p);
    }

    /**
     * Transpose the Tensor's data over different dimensions
     */
    public Tensor<T> transpose(int... dims) {
        // just flip if it's a 2d Tensor and no dims are specified
        if (dims.length == 0 && shape.length == 2) dims = new int[]{1, 0};
        else if (dims.length == 0 || dims.length != shape.length)
            throw new IllegalArgumentException("Invalid dimensions specified!");

        // handle negative dims
        for (int i = 0; i < dims.length; i++)
            dims[i] = dims[i] < 0 ? shape.length + dims[i] : dims[i];

        // calculate new shape and new data array
        int[] newShape = Arrays.stream(dims).map(d -> shape[d]).toArray();
        T[] resultData = (T[]) Array.newInstance(data.getClass().getComponentType(),
                Arrays.stream(newShape).reduce(1, (a, b) -> a * b));

        int[] strides = new int[shape.length];
        strides[shape.length - 1] = 1;
        for (int i = shape.length - 2; i >= 0; i--)
            strides[i] = strides[i + 1] * shape[i + 1];

        for (int i = 0; i < resultData.length; i++) {
            int[] newIndex = new int[newShape.length];
            int temp = i;
            for (int j = newShape.length - 1; j >= 0; j--) {
                newIndex[j] = temp % newShape[j];
                temp /= newShape[j];
            }

            int originalIndex = 0;
            for (int j = 0; j < dims.length; j++)
                originalIndex += newIndex[j] * strides[dims[j]];

            resultData[i] = data[originalIndex];
        }

        return new Tensor<>(resultData, newShape);
    }

    /**
     * Reshape the Tensor's data
     */
    public Tensor<T> reshape(int... shape) {
        if (shapeToSize(shape) != this.size)
            throw new IllegalArgumentException("The size of the Tensor can not change!");

        Tensor<T> t = this.clone();
        t.shape = shape;
        return t;
    }

    /**
     * Insert a Dimension at a given position
     */
    public Tensor<T> unsqueeze(int pos) {
        if (pos < 0) pos = this.shape.length + pos; // handle negative indexes

        if (this.shape.length <= pos) throw new IllegalArgumentException("Invalid position!");

        // add dim
        ArrayList<Integer> shape = new ArrayList<>(Arrays.stream(this.shape).boxed().toList());

        shape.add(pos, 1);

        // set
        this.shape = shape.stream().mapToInt(Integer::intValue).toArray();;

        return this;
    }

    /**
     * Remove a Dimension at a given position
     */
    public Tensor<T> squeeze(int pos) {
        if (pos < 0) pos = this.shape.length + pos; // handle negative indexes

        if (this.shape.length <= pos) throw new IllegalArgumentException("Invalid position!");

        // add dim
        ArrayList<Integer> shape = new ArrayList<>(Arrays.stream(this.shape).boxed().toList());
        shape.remove(pos);

        // set
        this.shape = shape.stream().mapToInt(Integer::intValue).toArray();;

        return this;
    }

    @Override
    public Tensor<T> clone() {
        return new Tensor<>(data.clone(), shape.clone());
    }

    @Override
    public String toString() {
        return "Tensor{" +
                "dtype=" + dtype.getSimpleName() +
                ", size=" + size +
                ", shape=" + Arrays.toString(shape) +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}