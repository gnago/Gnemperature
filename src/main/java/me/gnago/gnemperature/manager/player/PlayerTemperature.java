package me.gnago.gnemperature.manager.player;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.api.PapiHelper;
import me.gnago.gnemperature.manager.ClothingType;
import me.gnago.gnemperature.manager.Temperature;
import me.gnago.gnemperature.manager.TemperatureMethods;
import me.gnago.gnemperature.manager.debuff.Debuff;
import me.gnago.gnemperature.manager.debuff.DebuffRegistry;
import me.gnago.gnemperature.manager.file.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerTemperature implements PlayerMethods, PlayerSettings {
    private final Temperature feelsLike;
    private final Temperature actuallyIs;
    private double wetness;
    private final HashMap<Integer,Debuff> scheduledDebuffs;
    private final ArrayList<Debuff> activeDebuffs;

    private final Player player;

    public PlayerTemperature(Player player) {
        this.player = player;
        this.actuallyIs = new Temperature();
        this.feelsLike = this.actuallyIs.copy();
        this.scheduledDebuffs = new HashMap<>();
        this.activeDebuffs = new ArrayList<>();
    }

    @Override
    public void calcTemperature() {
        actuallyIs.set(new Temperature(calcClimateTemp(), calcWaterTemp(), calcWetnessTemp(), calcEnvironmentTemp(), calcClothingWarmth(), calcToolTemp(), calcActivityTemp(), calcStateTemp()));
        feelsLike.approach(actuallyIs);
        feelsLike.resist(applyEffectResistance(applyClothingResistance(applyCareResistance(1))));
    }

    @Override
    public double calcClimateTemp() {
        // 15 = maximum possible light from sky
        double climateExposure = Math.pow(player.getLocation().getBlock().getLightFromSky(),2)/Math.pow(15,2);
        double temp = calcWeatherTemp(calcTimeHeat());

        return temp * climateExposure + ConfigData.IndoorTemperature * (1 - climateExposure);
    }

    private double calcTimeHeat() {
        double humidityFactor = 1 - player.getLocation().getBlock().getHumidity()/2;
        double biomeTemp = TemperatureMethods.getBiomeTemp(player.getLocation().getBlock());
        double biomeFactor = Math.abs(biomeTemp);

        return 10 * Math.cos(
                Math.PI * (player.getWorld().getTime() - ConfigData.PeakTemperatureTime)/12000) *
                humidityFactor * (biomeFactor + 1) * (biomeTemp + 1) +
                32 * biomeTemp * biomeFactor / humidityFactor +
                32 * (biomeTemp + 1 - biomeFactor);
    }
    private double calcWeatherTemp(double temp) {
        if (!player.getWorld().isClearWeather()) {
            if (player.getLocation().getBlock().getHumidity() == 0) { // no downfall
                temp = TemperatureMethods.calcResist(temp, ConfigData.CloudyResistFactor);
            }
            else {
                if (player.getLocation().getBlock().getTemperature() >= 0.15) // If it's snowing
                    temp += ConfigData.CloudyModifier * 0.5;
                else
                    temp += ConfigData.CloudyModifier * 0.2;
            }
            temp += ConfigData.CloudyModifier;
        }
        return temp;
    }

    @Override
    public double calcWaterTemp() {
        double temp = 0;
        double skyExposure = (double) player.getLocation().getBlock().getLightFromSky()/15;
        if (player.isInWater()) {
            if (player.getLocation().getBlock().getType() == Material.BUBBLE_COLUMN)
                temp += ConfigData.BlockTemperatures.getOrDefault(Material.BUBBLE_COLUMN, 0.0) * 0.5;
            else
                temp += ConfigData.InWaterModifier * skyExposure;
        }
        else if (player.getLocation().getBlock().getType() == Material.WATER_CAULDRON) {
            Location loc = player.getLocation();
            loc.setY(loc.getBlockY() - 1);
            double boilerTemp = ConfigData.BlockTemperatures.getOrDefault(loc.getBlock().getType(), 0.0);
            if (boilerTemp >= ConfigData.MinimumBoilerTemperature)
                temp += ConfigData.BlockTemperatures.getOrDefault(loc.getBlock().getType(), 0.0) * 2;
            else
                temp += ConfigData.InWaterModifier * skyExposure;
        }
        return temp;
    }

    @Override
    public double calcWetnessTemp() {
        if (player.isInWater() || isInRain() || player.getLocation().getBlock().getType() == Material.WATER_CAULDRON) {
            if (wetness < ConfigData.WetnessMax)
                wetness += ConfigData.WetnessIncrement;
        } else {
            if (wetness > 0) {
                wetness -= ConfigData.WetnessDecrement;
                if (wetness > 0) {
                    double temp = ConfigData.WetnessModifier * player.getLocation().getBlock().getLightFromSky()/15;
                    if (ConfigData.UseWetnessDegradation)
                        temp *= wetness / ConfigData.WetnessMax;
                    return temp;
                }
            }
        }
        return 0;
    }
    private boolean isInRain() {
        return !player.getWorld().isClearWeather() && // is raining
                player.getLocation().getBlock().getLightFromSky() == 15 && // no blocks above
                player.getLocation().getBlock().getTemperature() > 0.15 && // not snowing
                player.getLocation().getBlock().getHumidity() > 0; // not a dry biome
    }

    @Override
    public double calcEnvironmentTemp() {
        // Need to use atomic as wrappers since local variables in lambdas need to be effectively final
        AtomicReference<Double> aTemp = new AtomicReference<>(0.0);
        AtomicReference<Double> aMax = new AtomicReference<>(0.0);
        AtomicReference<Double> aMin = new AtomicReference<>(0.0);
        if (player.getLocation().getBlock().getType() == Material.LAVA || player.getLocation().getBlock().getType() == Material.LAVA_CAULDRON) {
            aTemp.set(ConfigData.InLavaModifier);
            aMax.set(aTemp.get());
        } else {
            TemperatureMethods.forEachBlockInRadius(player.getLocation(), ConfigData.EnvironmentRange, true, block -> {
                if (block.getType() != Material.AIR) {
                    // Check if block has temperature and, if applicable, is powered/lit
                    if (ConfigData.BlockTemperatures.containsKey(block.getType()) &&
                        !block.getBlockData().getAsString().contains("lit=false") &&
                        !block.getBlockData().getAsString().contains("powered=false")) {
                        double blockTemp = ConfigData.BlockTemperatures.getOrDefault(block.getType(), 0.0);
                        if (block.getBlockData().getAsString().contains("candles=")) {
                            int candleDataInd = block.getBlockData().getAsString().indexOf("candles=");
                            String numCandlesStr = block.getBlockData().getAsString().substring(candleDataInd + "candles=".length(), candleDataInd + "candles=".length() + 1);
                            try {
                                blockTemp *= Integer.parseInt(numCandlesStr);
                            } catch (NumberFormatException e) { /* do nothing */ }
                        }

                        // Now check distance
                        double dist = player.getLocation().distance(block.getLocation());
                        double distTemp = blockTemp * (ConfigData.EnvironmentRange - dist)/ConfigData.EnvironmentRange;

                        // Set min and max temps.
                        // This is to ensure that things don't get too intense if there's a lot of temperature blocks in the area.
                        // So set max/min temps to be only a multiple of the strength of the hottest/coldest block's base temperature.
                        double blockTempLimit = distTemp * 2;
                        if (blockTempLimit > aMax.get())
                            aMax.set(blockTempLimit);
                        if (blockTempLimit < aMin.get())
                            aMin.set(blockTempLimit);

                        // Check for obstructions
                        AtomicReference<Double> aInverseDiminishRatio = new AtomicReference<>(1.0);
                        TemperatureMethods.forEachBlockBetween(player.getLocation().add(0, 1, 0), block.getLocation(), obstructingBlock -> {
                            // Blocks can block temperature only if they are solid and don't have air gaps
                            if (obstructingBlock != block &&
                                obstructingBlock.getType().isSolid() &&
                                TemperatureMethods.isFullBlock(obstructingBlock)) {
                                aInverseDiminishRatio.set(aInverseDiminishRatio.get() * 0.5);
                            }
                            return false;
                        });
                        aTemp.set(aTemp.get() + distTemp * aInverseDiminishRatio.get());
                    }
                }
                return false;
            });
        }

        aTemp.set(Math.max(Math.min(aTemp.get(), aMax.get()), aMin.get()));
        return aTemp.get();
    }

    @Override
    public double calcActivityTemp() {
        double temp = 0;
        if (player.isSprinting() && player.getVehicle() == null)
            temp += ConfigData.SprintingModifier;
        if (player.isSwimming())
            temp += ConfigData.SwimmingModifier;
        if (player.isGliding())
            temp += ConfigData.GlidingModifier;
        return temp;
    }

    @Override
    public double calcStateTemp() {
        double temp = 0;
        if (player.getFireTicks() > 0)
            temp += ConfigData.BurningModifier;
        if (player.isFrozen())
            temp += ConfigData.FreezingModifier;
        return temp;
    }

    @Override
    public double calcToolTemp() {
        return ConfigData.ItemTemperatures.getOrDefault(player.getInventory().getItemInMainHand().getType(),0.0) +
            ConfigData.ItemTemperatures.getOrDefault(player.getInventory().getItemInOffHand().getType(),0.0);
    }

    @Override
    public double calcClothingWarmth() {
        double temp = 0;

        // Can probably optimize this...
        for (ItemStack armour : player.getInventory().getArmorContents()) {
                for (ClothingType.MaterialType mat : ClothingType.MaterialType.values()) {
                    if (mat.getPieces().contains(armour.getType())) {
                        double addTemp = ConfigData.ClothingTypes.get(mat).warmth;
                        if (armour.getType().getEquipmentSlot() == EquipmentSlot.CHEST)
                            addTemp *= 1.6;
                        else if (armour.getType().getEquipmentSlot() == EquipmentSlot.LEGS)
                            addTemp *= 1.2;

                        temp += addTemp;
                        break;
                    }
                }
        }
        return temp;
    }

    @Override
    public double applyClothingResistance(double temp) {
        AtomicReference<Double> aTemp = new AtomicReference<>(temp);
        for (ItemStack armour : player.getInventory().getArmorContents()) {
            for (ClothingType.MaterialType mat : ClothingType.MaterialType.values()) {
                if (mat.getPieces().contains(armour.getType())) {
                    aTemp.set(TemperatureMethods.calcResist(temp, ConfigData.ClothingTypes.get(mat).resistance));
                    break;
                }
            }
            ConfigData.ResistanceEnchantments.forEach((enchant, res) -> {
                if (armour.containsEnchantment(enchant)) {
                    aTemp.set(TemperatureMethods.calcResist(aTemp.get(), res, armour.getEnchantmentLevel(enchant)));
                }
            });
        }
        return aTemp.get();
    }

    @Override
    public double applyCareResistance(double temp) {
        // Hunger
        temp = TemperatureMethods.calcResistBasic(temp, calcCareResistance(ConfigData.HungerMidPoint, 20,
                player.getFoodLevel(), ConfigData.HungerMaxResist, ConfigData.HungerMaxVuln));

        PapiHelper papi = GnemperaturePlugin.getInstance().getPapiHelper();
        if (papi != null) { // If PlaceholderAPI was enabled on startup
            if (papi.placeholderExists(player,"%thirstbar_isDisabled%")) { // This soft-checks that the server has the ThirstBar plugin
                if (!Boolean.parseBoolean(papi.getPlaceholderString(player, "%thirstbar_isDisabled%"))) { // Check if enabled
                    try {
                        double thirstLevel = papi.getPlaceholderDouble(player, "%thirstbar_current_int%");
                        double thirstMax = papi.getPlaceholderDouble(player, "%thirstbar_max_int%");
                        temp = TemperatureMethods.calcResistBasic(temp, calcCareResistance(ConfigData.ThirstMidPoint, thirstMax,
                                thirstLevel, ConfigData.ThirstMaxResist, ConfigData.ThirstMaxVuln));
                    } catch (NumberFormatException e) {
                        GnemperaturePlugin.getInstance().getLogger().warning("Failed to retrieve ThirstBar Placeholders");
                    }
                }
            }
        }

        return temp;
    }
    private double calcCareResistance(double midpoint, double max, double currVal, double res, double vuln) {
        if (currVal > midpoint)
            return 1 - (res * (currVal - midpoint)/(max - midpoint));
        else
            return 1 + (vuln * (midpoint - currVal)/midpoint);
    }

    @Override
    public double applyEffectResistance(double temp) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (!ConfigData.ExcludeTurtleHelmetEffect ||
                effect.getType() != PotionEffectType.WATER_BREATHING || effect.getDuration() > 200) {
                temp = TemperatureMethods.calcResist(temp, ConfigData.ResistanceEffects.get(effect), effect.getAmplifier());
            }
        }
        return Math.max(Math.min(temp, ConfigData.ResistTemperatureMax), ConfigData.ResistTemperatureMin);
    }

    @Override
    public void applyDebuffs(double prevTemp, double currTemp) {

        ArrayList<Debuff> addDebuffs = DebuffRegistry.getDebuffsWithThresholdsCrossed(prevTemp, currTemp);
        ArrayList<Debuff> acknowledgedDebuffs = new ArrayList<>(activeDebuffs);
        acknowledgedDebuffs.addAll(scheduledDebuffs.values());

        addDebuffs.forEach(debuff -> {
            if (!acknowledgedDebuffs.contains(debuff)) {
                int id = new BukkitRunnable() {
                    @Override
                    public void run() {
                        debuff.apply(player);
                        scheduledDebuffs.remove(this.getTaskId());
                        activeDebuffs.add(debuff);
                    }
                }.runTaskLater(GnemperaturePlugin.getInstance(), debuff.getDelay()).getTaskId();

                if (id != -1)
                    scheduledDebuffs.put(id,debuff);
                else
                    GnemperaturePlugin.getInstance().getLogger().warning("Failed to scheduled a task");
            }
        });

        ArrayList<Debuff> removeDebuffs = DebuffRegistry.getDebuffsWithThresholdsUncrossed(prevTemp, currTemp);
        activeDebuffs.forEach(debuff -> {
            if (removeDebuffs.contains(debuff)) {
                activeDebuffs.remove(debuff);
                debuff.clear(player);
            }
        });
        scheduledDebuffs.forEach((id, debuff) -> {
            if (removeDebuffs.contains(debuff)) {
                scheduledDebuffs.remove(id);
                if (Bukkit.getScheduler().isQueued(id))
                    Bukkit.getScheduler().cancelTask(id);
                // Might need to add this in case the task ran anyway or something
                // debuff.cleat(player);
            }
        });
    }
}
