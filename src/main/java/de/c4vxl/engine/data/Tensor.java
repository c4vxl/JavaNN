package de.c4vxl.engine.data;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;

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
     * Run an operation element wise
     */
    public Tensor<T> elementWiseIndexed(BiFunction<T, Integer, Object> task) {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = (T) task.apply(this.data[i], i);
        }

        return this;
    }

    /**
     * Element wise addition
     */
    public Tensor<T> add(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            T b = otherData[index];

            return a instanceof Double ? ((Number) a).doubleValue() + ((Number) b).doubleValue()
                            : a instanceof Integer ? ((Number) a).intValue() + ((Number) b).intValue()
                            : a instanceof Float ? ((Number) a).floatValue() + ((Number) b).floatValue()
                            : a instanceof Long ? ((Number) a).longValue() + ((Number) b).longValue()
                    : b;
        });
    }

    /**
     * Element wise subtraction
     */
    public Tensor<T> sub(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            T b = otherData[index];

            return a instanceof Double ? ((Number) a).doubleValue() - ((Number) b).doubleValue()
                    : a instanceof Integer ? ((Number) a).intValue() - ((Number) b).intValue()
                    : a instanceof Float ? ((Number) a).floatValue() - ((Number) b).floatValue()
                    : a instanceof Long ? ((Number) a).longValue() - ((Number) b).longValue()
                    : b;
        });
    }

    /**
     * Element wise division
     */
    public Tensor<T> div(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            T b = otherData[index];

            return a instanceof Double ? ((Number) a).doubleValue() / ((Number) b).doubleValue()
                    : a instanceof Integer ? ((Number) a).intValue() / ((Number) b).intValue()
                    : a instanceof Float ? ((Number) a).floatValue() / ((Number) b).floatValue()
                    : a instanceof Long ? ((Number) a).longValue() / ((Number) b).longValue()
                    : b;
        });
    }

    /**
     * Element wise multiplication
     */
    public Tensor<T> mul(Tensor<T> other) {
        T[] otherData = other.data;
        return elementWiseIndexed((a, index) -> {
            T b = otherData[index];

            return a instanceof Double ? ((Number) a).doubleValue() * ((Number) b).doubleValue()
                    : a instanceof Integer ? ((Number) a).intValue() * ((Number) b).intValue()
                    : a instanceof Float ? ((Number) a).floatValue() * ((Number) b).floatValue()
                    : a instanceof Long ? ((Number) a).longValue() * ((Number) b).longValue()
                    : b;
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
            if (a instanceof Double) {
                a = (Double) a > (Double) max ? max : a;
                return (Double) a < (Double) min ? min : a;
            } else if (a instanceof Float) {
                a = (Float) a > (Float) max ? max : a;
                return (Float) a < (Float) min ? min : a;
            } else if (a instanceof Long) {
                a = (Long) a > (Long) max ? max : a;
                return (Long) a < (Long) min ? min : a;
            } else {
                a = (Integer) a > (Integer) max ? max : a;
                return (Integer) a < (Integer) min ? min : a;
            }
        });
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

        this.shape = newShape;
        this.data = resultData;
        return this;
    }

    /**
     * Reshape the Tensor's data
     */
    public Tensor<T> reshape(int... shape) {
        if (shapeToSize(shape) != this.size)
            throw new IllegalArgumentException("The size of the Tensor can not change!");

        this.shape = shape;
        return this;
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