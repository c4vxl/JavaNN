package de.c4vxl.engine.data;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("unchecked")
public class Tensor<T> {
    // default data type
    public static Class<?> defaultDType = Float.class;


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
    public static Tensor<?> of(Object obj, int... shape) { return new Tensor<>(obj.getClass(), shape).fill(obj); }

    /**
     * Helper function for converting the shape of a tensor to the amount of data which can be stored in it
     */
    public static Integer shapeToSize(int... shape) { return Arrays.stream(shape).reduce(1, (a, b) -> a * b); }

    /**
     * Fill this Tensor with values
     */
    public Tensor<?> fill(Object obj) {
        Arrays.fill(data, obj);
        return this;
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
