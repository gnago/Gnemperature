package io.github.gnago.manager.file;

import io.github.gnago.TemperaturePlugin;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ConfigData {
    public static int RefreshRate;
    public static List<String> EnabledWorlds;
    //Add thermometer config

    public static double IndoorTemperature;
    public static double PeakTemperatureTime;

    public static int EnvironmentRange;
    public static double InWaterModifier;
    public static double CloudyResistFactor;
    public static double CloudyModifier;

    public static double SprintingModifier;
    public static double SwimmingModifier;
    public static double BurningModifier;
    public static double FreezingModifier;

    public static double WetnessMax;
    public static double WetnessIncrement;
    public static double WetnessDecrement;
    public static double WetnessModifier;
    public static boolean UseWetnessDegradation;

    public static double IdealTemperature;
    public static double ResistTemperatureMin;
    public static double ResistTemperatureMax;

    public static double HungerMidPoint;
    public static double HungerMaxResist;
    public static double HungerMaxVuln;
    public static double ThirstMidPoint;
    public static double ThirstMaxResist;
    public static double ThirstMaxVuln;

    public static HashMap<Material,Double> BlockTemperatures;
    public static HashMap<Material,Double> ItemTemperatures;

    public static HashMap<PotionEffect,Double> ResistanceEffects;

    public static HashMap<Enchantment,Double> ResistanceEnchantments;
    public static HashMap<String,Double> ClothingResistances;
    public static HashMap<String,Double> ClothingWarmth;

    public static int DebuffGracePeriod;
    public static HashMap<Double,List<String>> Debuffs;

    public static HashMap<TemperatureType, Double> GradualityRates;
    public enum TemperatureType {
        SUNLIGHT, WATER, ENVIRONMENT, CLOTHING, TOOL, ACTIVITY, STATE
    }

    private final FileConfiguration configFile;

    public ConfigData() {
        this.configFile = TemperaturePlugin.getInstance().getConfig();

        RefreshRate = configFile.getInt("RefreshRate", 20);
        EnabledWorlds = configFile.getStringList("EnabledWorlds");
        IndoorTemperature = configFile.getDouble("IndoorTemperature", 64);
        PeakTemperatureTime = configFile.getInt("PeakTemperatureTime", 1400);
        EnvironmentRange = configFile.getInt("EnvironmentRange", 5);
        InWaterModifier = configFile.getDouble("InWaterModifier", -24);
        CloudyResistFactor = configFile.getDouble("Weather.WeakensSun", 0.6);
        CloudyModifier = configFile.getDouble("Weather.Modifier", -10);
        SprintingModifier = configFile.getDouble("Activity.Sprinting", 8);
        SwimmingModifier = configFile.getDouble("Activity.Swimming", 0);
        BurningModifier = configFile.getDouble("State.Burning", 50);
        FreezingModifier = configFile.getDouble("State.Freezing", -50);

        GradualityRates = new HashMap<>();
        GradualityRates.put(TemperatureType.SUNLIGHT, configFile.getDouble("Graduality.Climate",0.12));
        GradualityRates.put(TemperatureType.ENVIRONMENT, configFile.getDouble("Graduality.Environment",0.15));
        GradualityRates.put(TemperatureType.WATER, configFile.getDouble("Graduality.Water",0.75));
        GradualityRates.put(TemperatureType.CLOTHING, configFile.getDouble("Graduality.Clothing",0.25));
        GradualityRates.put(TemperatureType.TOOL, configFile.getDouble("Graduality.Tool",0.7));
        GradualityRates.put(TemperatureType.ACTIVITY, configFile.getDouble("Graduality.Activity",0.02));
        GradualityRates.put(TemperatureType.STATE, configFile.getDouble("Graduality.State",0.8));

        WetnessModifier = configFile.getDouble("Wetness.Modifier", -16);
        WetnessMax = configFile.getDouble("Wetness.Max", 60);
        WetnessIncrement = configFile.getDouble("Wetness.Increment", 5);
        WetnessDecrement = configFile.getDouble("Wetness.Decrement", 1);
        UseWetnessDegradation = configFile.getBoolean("Wetness.EnableDegradation", true);

        IdealTemperature = configFile.getDouble("Resistance.Target", 75);
        ResistTemperatureMax = configFile.getDouble("Resistance.PotionEffects.MaxTemperature", 90);
        ResistTemperatureMin = configFile.getDouble("Resistance.PotionEffects.MinTemperature", 10);

        HungerMidPoint = configFile.getDouble("Resistance.Hunger.Mid-point", 3.5);
        HungerMaxResist = configFile.getDouble("Resistance.Hunger.Resistance", 0.15);
        HungerMaxVuln = configFile.getDouble("Resistance.Hunger.Vulnerability", 0.4);
        ThirstMidPoint = configFile.getDouble("Resistance.Thirst.Mid-point", 40);
        ThirstMaxResist = configFile.getDouble("Resistance.Thirst.Resistance", 0.15);
        ThirstMaxVuln = configFile.getDouble("Resistance.Thirst.Vulnerability", 0.4);

        DebuffGracePeriod = configFile.getInt("Debuffs.GracePeriod", 30);
        Debuffs = new HashMap<>();
        configFile.getConfigurationSection("Debuffs").getKeys(false).forEach(
                key -> {
                    Collection<String> effects = configFile.getConfigurationSection("Debuffs."+key).getKeys(false);
                    Debuffs.put(Double.parseDouble(key), new ArrayList<>(effects));
                });

        //todo add clothing stuff
        //then put the item/block temperatures in a different config file, since that will be pretty big
    }
}
