package me.gnago.gnemperature.manager;

import java.util.Vector;

public class Temperature {

    public enum Type {
        CLIMATE,
        WATER,
        WETNESS,
        ENVIRONMENT,
        CLOTHING,
        TOOL,
        ACTIVITY,
        STATE
    }

    private static Temperature gradualityRates = new Temperature(1,1,1,1,1,1,1,1);
    public static void setGradualityRates(Temperature rates) { gradualityRates = rates; }

    private final Vector<Double> vector;

    public Temperature(double climate, double water, double wetness, double environment, double clothing, double tool, double activity, double state) {
        vector = new Vector<>();
        set(Type.CLIMATE, climate);
        set(Type.WATER, water);
        set(Type.WETNESS, wetness);
        set(Type.ENVIRONMENT, environment);
        set(Type.CLOTHING, clothing);
        set(Type.TOOL, tool);
        set(Type.ACTIVITY, activity);
        set(Type.STATE, state);
    }
    public Temperature() {
        this(0,0,0,0,0,0,0,0);
    }

    public double get(Type type) {
        return vector.get(type.ordinal());
    }
    public void set(Type type, double value) {
        vector.insertElementAt(value, type.ordinal());
    }
    public void set(Temperature target) {
        for (Type type : Type.values())
            set(type, target.get(type));
    }
    public Temperature add(Type type, double value) {
        set(type, get(type) + value);
        return this;
    }
    public Temperature mult(Type type, double value) {
        set(type, get(type) * value);
        return this;
    }

    public Temperature clear() {
        for (Type type : Type.values())
            set(type, 0);
        return this;
    }

    public Temperature copy() {
        return new Temperature(get(Type.CLIMATE), get(Type.WATER), get(Type.WETNESS), get(Type.ENVIRONMENT), get(Type.CLOTHING), get(Type.TOOL), get(Type.ACTIVITY), get(Type.STATE));
    }

    public Temperature approach(Temperature target) {
        for (Type type : Type.values())
            set(type, calcLogGradual(get(type), target.get(type), gradualityRates.get(type)));
        return this;
    }
    private double calcLogGradual(double current, double target, double rate) {
        double change = (target - current) * rate;
        return current + change;
    }

    public Temperature resist(double resistance) {
        for (Type type : Type.values())
            if (type != Type.CLOTHING) // Clothing cannot be resisted
                mult(type, resistance);
        return this;
    }

    public double total() {
        double total = 0;
        for (Type type : Type.values())
            total += get(type);
        return total;
    }
}

