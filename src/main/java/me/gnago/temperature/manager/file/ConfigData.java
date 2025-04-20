package me.gnago.temperature.manager.file;

import me.gnago.temperature.TemperaturePlugin;
import me.gnago.temperature.manager.ClothingType;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public class ConfigData {
    public static int RefreshRate;
    public static List<String> EnabledWorlds;
    //Add thermometer config

    public static double IndoorTemperature;
    public static double PeakTemperatureTime;

    public static double CloudyResistFactor;
    public static double CloudyModifier;
    public static double InWaterModifier;
    public static double InLavaModifier;
    public static int EnvironmentRange;

    public static double SprintingModifier;
    public static double SwimmingModifier;
    public static double GlidingModifier;
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

    public static double MinimumBoilerTemperature;
    public static HashMap<Material,Double> BlockTemperatures;
    public static HashMap<Material,Double> ItemTemperatures;

    public static HashMap<PotionEffect,Double> ResistanceEffects;
    public static boolean ExcludeTurtleHelmetEffect;

    public static HashMap<Enchantment,Double> ResistanceEnchantments;
    public static HashMap<ClothingType.MaterialType,ClothingType> ClothingTypes;

    public static int DebuffGracePeriod;
    public static HashMap<Double,List<String>> Debuffs;

    public static HashMap<TemperatureType, Double> GradualityRates;
    public enum TemperatureType {
        CLIMATE, WATER, ENVIRONMENT, CLOTHING, TOOL, ACTIVITY, STATE
    }

    private final FileConfiguration configFile;
    private final FileConfiguration materialsFile;

    public ConfigData() {
        this.configFile = TemperaturePlugin.getInstance().getConfig();
        this.materialsFile = createMaterialsConfig();

        RefreshRate = configFile.getInt("refresh_rate", 20);
        EnabledWorlds = configFile.getStringList("enabled_worlds");
        IndoorTemperature = configFile.getDouble("indoor_temperature", 64);
        PeakTemperatureTime = configFile.getInt("peak_temperature_time", 1400);
        CloudyResistFactor = configFile.getDouble("weather.sun_protection", 0.6);
        CloudyModifier = configFile.getDouble("weather.modifier", -10);
        InWaterModifier = configFile.getDouble("in_water_modifier", -24);
        InLavaModifier = configFile.getDouble("in_lava_modifier", 1100);
        EnvironmentRange = configFile.getInt("environment_range", 5);
        SprintingModifier = configFile.getDouble("activity.sprinting", 8);
        SwimmingModifier = configFile.getDouble("activity.swimming", 0);
        GlidingModifier = configFile.getDouble("activity.gliding", 0);
        BurningModifier = configFile.getDouble("state.burning", 50);
        FreezingModifier = configFile.getDouble("state.freezing", -50);

        GradualityRates = new HashMap<>();
        GradualityRates.put(TemperatureType.CLIMATE, configFile.getDouble("graduality.climate",0.12));
        GradualityRates.put(TemperatureType.ENVIRONMENT, configFile.getDouble("graduality.environment",0.15));
        GradualityRates.put(TemperatureType.WATER, configFile.getDouble("graduality.water",0.75));
        GradualityRates.put(TemperatureType.CLOTHING, configFile.getDouble("graduality.clothing",0.25));
        GradualityRates.put(TemperatureType.TOOL, configFile.getDouble("graduality.tool",0.7));
        GradualityRates.put(TemperatureType.ACTIVITY, configFile.getDouble("graduality.activity",0.02));
        GradualityRates.put(TemperatureType.STATE, configFile.getDouble("graduality.state",0.8));

        WetnessModifier = configFile.getDouble("wetness.modifier", -16);
        WetnessMax = configFile.getDouble("wetness.max", 60);
        WetnessIncrement = configFile.getDouble("wetness.increment", 5);
        WetnessDecrement = configFile.getDouble("wetness.decrement", 1);
        UseWetnessDegradation = configFile.getBoolean("wetness.enable_degradation", true);

        IdealTemperature = configFile.getDouble("resistance.ideal", 75);
        ResistTemperatureMax = configFile.getDouble("resistance.potion_effects.max_temperature", 90);
        ResistTemperatureMin = configFile.getDouble("resistance.potion_effects.min_temperature", 10);

        HungerMidPoint = configFile.getDouble("resistance.hunger.midpoint", 3.5);
        HungerMaxResist = configFile.getDouble("resistance.hunger.resistance", 0.15);
        HungerMaxVuln = configFile.getDouble("resistance.hunger.vulnerability", 0.4);
        ThirstMidPoint = configFile.getDouble("resistance.thirst.midpoint", 40);
        ThirstMaxResist = configFile.getDouble("resistance.thirst.resistance", 0.15);
        ThirstMaxVuln = configFile.getDouble("resistance.thirst.vulnerability", 0.4);

        DebuffGracePeriod = configFile.getInt("debuffs.grace_period", 30);
        Debuffs = new HashMap<>();
        configFile.getConfigurationSection("debuffs").getKeys(false).forEach(
                key -> {
                    Collection<String> effects = configFile.getConfigurationSection("debuffs."+key).getKeys(false);
                    Debuffs.put(Double.parseDouble(key), new ArrayList<>(effects));
                });

        ClothingTypes = new HashMap<>();
        ClothingType.setDefaults(configFile.getDouble("clothing.default.warmth"),
                                configFile.getDouble("clothing.default.resistance"));
        for (ClothingType.MaterialType mat : ClothingType.MaterialType.values()) {
            String matName = mat.name().toLowerCase();
            ClothingTypes.put(mat, new ClothingType(mat,
                    configFile.getDouble("clothing." + matName + ".warmth"),
                    configFile.getDouble("clothing." + matName + ".resistance")));
        }

        ExcludeTurtleHelmetEffect = configFile.getBoolean("resistance.potion_effects.exclude_turtle_helmet_effect", true);
        ResistanceEffects = new HashMap<>();
        configFile.getConfigurationSection("resistance.potion_effects.list").getKeys(false).forEach(
                key -> {
                    PotionEffectType effType = Registry.EFFECT.match(key.toUpperCase());
                    if (effType != null)
                        ResistanceEffects.put(
                                new PotionEffect(effType, 8, 0),
                                configFile.getDouble("resistance.potion_effects.list." + key, 0)
                        );
                });
        ResistanceEnchantments = new HashMap<>();
        configFile.getConfigurationSection("resistance.enchantments.list").getKeys(false).forEach(
                key -> {
                    Enchantment enchant = Registry.ENCHANTMENT.match(key.toUpperCase());
                    if (enchant != null)
                        ResistanceEnchantments.put(
                                enchant,
                                configFile.getDouble("resistance.enchantments.list." + key, 0)
                        );
                });

        MinimumBoilerTemperature = materialsFile.getDouble("minimum_boiler_temperature", 25);

        BlockTemperatures = new HashMap<>();
        materialsFile.getConfigurationSection("blocks").getKeys(false).forEach(
                key -> {
                    Material mat = Registry.MATERIAL.match(key.toUpperCase());
                    if (mat != null)
                        BlockTemperatures.put(
                                mat,
                                materialsFile.getDouble("blocks." + key, 0)
                        );
                }
        );
        ItemTemperatures = new HashMap<>();
        materialsFile.getConfigurationSection("items").getKeys(false).forEach(
                key -> {
                    Material mat = Registry.MATERIAL.match(key.toUpperCase());
                    if (mat != null)
                        ItemTemperatures.put(
                                mat,
                                materialsFile.getDouble("items." + key, 0)
                        );
                }
        );
    }

    private static FileConfiguration createMaterialsConfig() {
        File file = new File(TemperaturePlugin.getInstance().getDataFolder(), "materials.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            TemperaturePlugin.getInstance().saveResource("materials.yml", false);
        }

        return YamlConfiguration.loadConfiguration(file);
    }
}
