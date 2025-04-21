package me.gnago.gnemperature.manager.debuff;

import me.gnago.gnemperature.manager.file.ConfigData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class DebuffRegistry {
    private static final HashMap<String, Debuff> DebuffRegistry = new HashMap<>();

    public static Debuff processDebuff(String name, Collection<Double> thresholds, int delay) {
        if (name.equalsIgnoreCase("burning")) {
            return ((FunctionDebuff)Register(name, Debuff.New(name, thresholds, delay))).setFunctions(200,
                ent -> {
                    ent.setFireTicks(60); // Every 10 seconds, catch on fire for 3 seconds
                },
                ent -> {
                    ent.setFireTicks(0);
                });
        } else if (name.equalsIgnoreCase("freezing")) {
            return ((FunctionDebuff)Register(name, Debuff.New(name, thresholds, delay))).setFunctions(200,
                    ent -> {
                        ent.setFreezeTicks(ent.getMaxFreezeTicks() + 210); // Stay frozen, reapply every 10 seconds
                    },
                    ent -> { /* do nothing, freezing will go away on its own */ });
        } else {
            return Register(name, Debuff.New(name, thresholds, delay));
        }
    }

    public static Debuff Register(String name, Debuff debuff) {
        name = name.toUpperCase();
        if (!isRegistered(name))
            DebuffRegistry.put(name, debuff);
        return DebuffRegistry.get(name);
    }
    public static boolean isRegistered(String name) {
        return DebuffRegistry.containsKey(name.toUpperCase());
    }
    public static void clearRegistry() {
        DebuffRegistry.clear();
    }
    public static ArrayList<Debuff> getDebuffsInRange(double temp) {
        ArrayList<Debuff> debuffs = new ArrayList<>();
        if (temp > ConfigData.IdealTemperature) {
            DebuffRegistry.forEach((key, debuff) -> {
                if (debuff.thresholds.stream().anyMatch(threshold ->
                        threshold > ConfigData.IdealTemperature && threshold <= temp)) {
                    debuffs.add(debuff);
                }
            });
        } else if (temp < ConfigData.IdealTemperature) {
            DebuffRegistry.forEach((key, debuff) -> {
                if (debuff.thresholds.stream().anyMatch(threshold ->
                        threshold < ConfigData.IdealTemperature && threshold >= temp)) {
                    debuffs.add(debuff);
                }
            });
        }
        return debuffs;
    }
    public static ArrayList<Debuff> getDebuffsWithThresholdsCrossed(double prevTemp, double currTemp) {
        ArrayList<Debuff> debuffs = new ArrayList<>();
        if (currTemp > ConfigData.IdealTemperature && prevTemp < currTemp) { // check if hot AND got hotter
            DebuffRegistry.forEach((key, debuff) -> {
                if (debuff.thresholds.stream().anyMatch(threshold ->
                        threshold > ConfigData.IdealTemperature && threshold <= currTemp)) {
                    debuffs.add(debuff);
                }
            });
        } else if (currTemp < ConfigData.IdealTemperature && prevTemp > currTemp) {
            DebuffRegistry.forEach((key, debuff) -> {
                if (debuff.thresholds.stream().anyMatch(threshold ->
                        threshold < ConfigData.IdealTemperature && threshold >= currTemp)) {
                    debuffs.add(debuff);
                }
            });
        }
        return debuffs;
    }
    public static ArrayList<Debuff> getDebuffsWithThresholdsUncrossed(double prevTemp, double currTemp) {
        ArrayList<Debuff> debuffs = new ArrayList<>();
        if (prevTemp > ConfigData.IdealTemperature && prevTemp > currTemp) { // check if was hot AND got cooler
            DebuffRegistry.forEach((key, debuff) -> {
                if (debuff.thresholds.stream().anyMatch(threshold ->
                        threshold > ConfigData.IdealTemperature && threshold > currTemp)) { // check that currTemp went below
                    debuffs.add(debuff);
                }
            });
        } else if (prevTemp < ConfigData.IdealTemperature && prevTemp < currTemp) { // check if was cool AND got hotter
            DebuffRegistry.forEach((key, debuff) -> {
                if (debuff.thresholds.stream().anyMatch(threshold ->
                        threshold < ConfigData.IdealTemperature && threshold < currTemp)) { // check that currTemp went above
                    debuffs.add(debuff);
                }
            });
        }
        return debuffs;
    }
}
