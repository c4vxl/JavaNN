package de.c4vxl.engine.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}