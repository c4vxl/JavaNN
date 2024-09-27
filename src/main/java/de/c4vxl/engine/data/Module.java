package de.c4vxl.engine.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class serves as a base for creating any kind of modules.
 * A module adds functionality for converting all of its weights into a map which can be saved in a file.
 *
 * @author c4vxl
 */
public class Module {
    public Map<String, Object> state() {
        Map<String, Object> state = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            // skip private variables
            if (Modifier.isPrivate(field.getModifiers())) continue;

            try {
                Object value = field.get(this);

                state.put(field.getName(), cast(value));
            } catch (IllegalAccessException e) {
                System.err.println("Error while trying to create the state! " + e);
            }
        }

        return state;
    }

    @SuppressWarnings("unchecked")
    public Module load_state(Map<String, Object> state) {
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Object value = field.get(this); // Get the current value of the field

                if (value instanceof Module) { // handle if value is another module
                    // load the module of the state list
                    ((Module) value).load_state((Map<String, Object>) state.get(field.getName()));
                }

                else if (value instanceof List<?> currentList) { // handle if value is a list of modules
                    List<?> stateList = (List<?>) state.get(field.getName());
                    for (int i = 0; i < currentList.size(); i++) {
                        if (currentList.get(i) instanceof Module) {
                            ((Module) currentList.get(i)).load_state((Map<String, Object>) stateList.get(i));
                        } else {
                            field.set(this, state.get(field.getName()));
                        }
                    }
                }

                else // handle non-Module values
                    field.set(this, state.get(field.getName()));
            } catch (IllegalAccessException e) {
                System.err.println("Error while trying to load the state! " + e);
            }
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    private Object cast(Object value) {
        // if value is a tensor -> return
        if (value instanceof Tensor<?>) return value;

        // if value is a module -> return state
        else if (value instanceof Module) return ((Module) value).state();

        // if value is a list -> cast each element -> return
        else if (value instanceof List<?>) {
            List<Object> list = (List<Object>) value;
            list.replaceAll(this::cast);
            return list;
        }

        // else -> return value
        return value;
    }

    /**
     * Get the state as a JSON String
     */
    public String toJSON() {
        Map<String, Object> state = state();

        state.forEach((key, value) -> {
            if (value instanceof Tensor<?> tensor) { // manually serialize Tensors as gson can not handle <T>
                Map<String, Object> tensorData = new HashMap<>();
                tensorData.put("dtype", tensor.dtype.getSimpleName());
                tensorData.put("data", tensor.data);
                tensorData.put("shape", tensor.shape);
                state.put(key, tensorData);
            }
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(state);
    }

    /**
     * Load the state from a JSON String
     */
    @SuppressWarnings("unchecked")
    public Module fromJSON(String json) {
        try {
            Map<String, Object> extracted = new Gson().fromJson(json, Map.class);

            Field[] fields = this.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                field.setAccessible(true);

                Object value = field.get(this);

                // skip null and Module values
                if (value == null || value instanceof Module) continue;

                String key = field.getName();
                Object other = extracted.get(key);

                if (value instanceof Tensor<?>) {
                    Map<String, Object> tensorData = (Map<String, Object>) other;
                    Class<?> dtype = Class.forName("java.lang." + tensorData.get("dtype").toString());
                    int[] shape = ((ArrayList<?>) tensorData.get("shape"))
                            .stream()
                            .mapToInt(o -> ((Number) o).intValue())
                            .toArray();
                    Tensor<?> output = new Tensor<>(dtype, shape);

                    List<?> data = (List<?>) ((Map<?, ?>) other).get("data");
                    for (int i1 = 0; i1 < output.data.length; i1++) {
                        Array.set(output.data, i1, dtype.cast(data.get(i1)));
                    }

                    extracted.put(key, output.clone());
                }
                else {
                    if (value instanceof Integer && !(other instanceof Integer))
                        other = Integer.valueOf(other.toString().split("\\.")[0]);

                    extracted.put(key, value.getClass().getMethod("valueOf", String.class).invoke(null, other.toString()));
                }
            }

            this.load_state(extracted);
        } catch (Exception ignored) {}

        return this;
    }
}