package me.gnago.gnemperature.manager.file;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.manager.ClothingType;
import me.gnago.gnemperature.manager.Temperature;
import me.gnago.gnemperature.manager.debuff.DebuffRegistry;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;

public class ConfigData {
    public static int RefreshRate;
    public static Collection<String> EnabledWorlds;
    public static boolean AlwaysShowTemperature;

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
    public static EnumMap<Material,Double> BlockTemperatures;
    public static EnumMap<Material,Double> ItemTemperatures;

    public static HashMap<PotionEffect,Double> ResistanceEffects;
    public static boolean ExcludeTurtleHelmetEffect;

    public static HashMap<Enchantment,Double> ResistanceEnchantments;
    public static EnumMap<ClothingType.MaterialType,ClothingType> ClothingTypes;

    public static int DebuffGracePeriod;

    public static ItemStack ThermometerItem;

    private final FileConfiguration config;
    private final FileConfiguration materialsConfig;
    private final File thermometerConfigFile;
    private final FileConfiguration thermometerConfig;

    public ConfigData() {
        this.config = GnemperaturePlugin.getInstance().getConfig();
        this.materialsConfig = createConfig(createFile("materials.yml"));
        this.thermometerConfigFile = createFile("customitems.yml");
        this.thermometerConfig = createConfig(thermometerConfigFile);

        ThermometerItem = thermometerConfig.getItemStack("thermometer", new ItemStack(Material.CLOCK));

        RefreshRate = config.getInt("refresh_rate", 20);
        EnabledWorlds = config.getStringList("enabled_worlds");
        AlwaysShowTemperature = config.getBoolean("always_show_temperature", true);
        IndoorTemperature = config.getDouble("indoor_temperature", 64);
        PeakTemperatureTime = config.getInt("peak_temperature_time", 1400);
        CloudyResistFactor = config.getDouble("weather.sun_protection", 0.6);
        CloudyModifier = config.getDouble("weather.modifier", -10);
        InWaterModifier = config.getDouble("in_water_modifier", -24);
        InLavaModifier = config.getDouble("in_lava_modifier", 1100);
        EnvironmentRange = config.getInt("environment_range", 5);
        SprintingModifier = config.getDouble("activity.sprinting", 8);
        SwimmingModifier = config.getDouble("activity.swimming", 0);
        GlidingModifier = config.getDouble("activity.gliding", 0);
        BurningModifier = config.getDouble("state.burning", 50);
        FreezingModifier = config.getDouble("state.freezing", -50);

        Temperature.setGradualityRates(new Temperature(
                config.getDouble("graduality.climate",0.12),
                1, // Wetness is instant since it has its own custom graduality functionality
                config.getDouble("graduality.water",0.75),
                config.getDouble("graduality.environment",0.15),
                config.getDouble("graduality.clothing",0.25),
                config.getDouble("graduality.tool",0.7),
                config.getDouble("graduality.activity",0.02),
                config.getDouble("graduality.state",0.8)
        ));

        WetnessModifier = config.getDouble("wetness.modifier", -16);
        WetnessMax = config.getDouble("wetness.max", 60);
        WetnessIncrement = config.getDouble("wetness.increment", 5);
        WetnessDecrement = config.getDouble("wetness.decrement", 1);
        UseWetnessDegradation = config.getBoolean("wetness.enable_degradation", true);

        IdealTemperature = config.getDouble("resistance.ideal", 75);
        ResistTemperatureMax = config.getDouble("resistance.potion_effects.max_temperature", 90);
        ResistTemperatureMin = config.getDouble("resistance.potion_effects.min_temperature", 10);

        HungerMidPoint = config.getDouble("resistance.hunger.midpoint", 3.5);
        HungerMaxResist = config.getDouble("resistance.hunger.resistance", 0.15);
        HungerMaxVuln = config.getDouble("resistance.hunger.vulnerability", 0.4);
        ThirstMidPoint = config.getDouble("resistance.thirst.midpoint", 40);
        ThirstMaxResist = config.getDouble("resistance.thirst.resistance", 0.15);
        ThirstMaxVuln = config.getDouble("resistance.thirst.vulnerability", 0.4);

        DebuffGracePeriod = config.getInt("debuff_grace_period", 30);

        // Using this variable to more easily do null checks
        ConfigurationSection readingSection = config.getConfigurationSection("debuffs");
        DebuffRegistry.clearRegistry();
        if (readingSection != null)
            readingSection.getKeys(false).forEach(effect -> {
                Collection<Double> thresholds = config.getDoubleList("debuffs." + effect);
                DebuffRegistry.processDebuff(effect, thresholds, DebuffGracePeriod * 20);
            });

        ClothingTypes = new EnumMap<>(ClothingType.MaterialType.class);
        ClothingType.setDefaults(config.getDouble("clothing.default.warmth"),
                                config.getDouble("clothing.default.resistance"));
        for (ClothingType.MaterialType mat : ClothingType.MaterialType.values()) {
            String matName = mat.name().toLowerCase();
            ClothingTypes.put(mat, new ClothingType(mat,
                    config.getDouble("clothing." + matName + ".warmth"),
                    config.getDouble("clothing." + matName + ".resistance")));
        }

        ExcludeTurtleHelmetEffect = config.getBoolean("resistance.potion_effects.exclude_turtle_helmet_effect", true);
        ResistanceEffects = new HashMap<>();
        readingSection = config.getConfigurationSection("resistance.potion_effects.list");
        if (readingSection != null)
            readingSection.getKeys(false).forEach(key -> {
                PotionEffectType effType = Registry.EFFECT.match(key.toUpperCase());
                if (effType != null)
                    ResistanceEffects.put(
                            new PotionEffect(effType, 8, 0),
                            config.getDouble("resistance.potion_effects.list." + key, 0)
                    );
            });

        ResistanceEnchantments = new HashMap<>();
        readingSection = config.getConfigurationSection("resistance.enchantments.list");
        if (readingSection != null)
            readingSection.getKeys(false).forEach(key -> {
                Enchantment enchant = Registry.ENCHANTMENT.match(key.toUpperCase());
                if (enchant != null)
                    ResistanceEnchantments.put(
                            enchant,
                            config.getDouble("resistance.enchantments.list." + key, 0)
                    );
            });

        MinimumBoilerTemperature = materialsConfig.getDouble("minimum_boiler_temperature", 25);

        BlockTemperatures = new EnumMap<>(Material.class);
        readingSection = materialsConfig.getConfigurationSection("blocks");
        if (readingSection != null)
            readingSection.getKeys(false).forEach(key -> {
                Material mat = Registry.MATERIAL.match(key.toUpperCase());
                if (mat != null)
                    BlockTemperatures.put(
                            mat,
                            materialsConfig.getDouble("blocks." + key, 0)
                    );
            }
        );
        ItemTemperatures = new EnumMap<>(Material.class);
        readingSection = materialsConfig.getConfigurationSection("items");
        if (readingSection != null)
            readingSection.getKeys(false).forEach(key -> {
                Material mat = Registry.MATERIAL.match(key.toUpperCase());
                if (mat != null)
                    ItemTemperatures.put(
                            mat,
                            materialsConfig.getDouble("items." + key, 0)
                    );
            });
    }

    private static FileConfiguration createConfig(@NotNull File file) {
        try {
            new YamlConfiguration().load(file);
            return YamlConfiguration.loadConfiguration(file);
        } catch (IOException | InvalidConfigurationException | YAMLException e) {
            String filename = file.getName();
            GnemperaturePlugin.getInstance().getLogger().severe(filename + " cannot be read! Archiving and creating new file...");
            file.renameTo(new File(GnemperaturePlugin.getInstance().getDataFolder(), file.getName() + ".old." + new Date().getTime()));
            file = createFile(filename);
            return YamlConfiguration.loadConfiguration(file);
        }
    }
    private static File createFile(String filename){
        File file = new File(GnemperaturePlugin.getInstance().getDataFolder(), filename);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            GnemperaturePlugin.getInstance().saveResource(filename, false);
        }
        return file;
    }

    public boolean SetThermometerItem(@NotNull ItemStack newThermometer) {
        thermometerConfig.set("thermometer", newThermometer);
        try {
            if (newThermometer.getType() != Material.AIR) {
                thermometerConfig.save(thermometerConfigFile);
                ThermometerItem = newThermometer;
                return true;
            }
        } catch (IOException e) {
            GnemperaturePlugin.getInstance().getLogger().warning("Failed to save thermometer file.");
        }
        return false;
    }
}
