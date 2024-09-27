package de.c4vxl;

import de.c4vxl.engine.module.Linear;

public class Main {
    public static void main(String[] args) {
        Linear linear = new Linear(4, 4);
        String oldJSON = linear.toJSON();

        linear.fromJSON(oldJSON);

        System.out.println(linear.toJSON().equals(oldJSON));
    }
}