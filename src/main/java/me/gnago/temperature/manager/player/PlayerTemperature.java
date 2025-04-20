package me.gnago.temperature.manager.player;

import me.gnago.temperature.manager.ClothingType;
import me.gnago.temperature.manager.TemperatureMethods;
import me.gnago.temperature.manager.file.ConfigData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerTemperature implements PlayerMethods {
    private double feelsLike;
    private double actuallyIs;
    private double wetness;
    private final Player player;

    public PlayerTemperature(Player player) {
        this.player = player;
    }

    @Override
    public void calcTemperature() {

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
        // You cannot reassign external variables from within a lambda. Use this class to hold them.
        class EnvValues {
            public double temp = 0;
            public double max = 0;
            public double min = 0;
            public double inverseDiminishRatio = 0;
        }
        EnvValues envValues = new EnvValues();

        if (player.getLocation().getBlock().getType() == Material.LAVA || player.getLocation().getBlock().getType() == Material.LAVA_CAULDRON) {
            envValues.temp = ConfigData.InLavaModifier;
            envValues.max = envValues.temp;
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
                        if (blockTempLimit > envValues.max)
                            envValues.max = blockTempLimit;
                        if (blockTempLimit < envValues.min)
                            envValues.min = blockTempLimit;

                        // Check for obstructions
                        envValues.inverseDiminishRatio = 1;
                        TemperatureMethods.forEachBlockBetween(player.getLocation().add(0, 1, 0), block.getLocation(), obstructingBlock -> {
                            // Blocks can block temperature only if they are solid and don't have air gaps
                            if (obstructingBlock != block &&
                                obstructingBlock.getType().isSolid() &&
                                TemperatureMethods.isFullBlock(obstructingBlock)) {
                                envValues.inverseDiminishRatio *= 0.5;
                            }
                            return false;
                        });
                        envValues.temp += distTemp * envValues.inverseDiminishRatio;
                    }
                }
                return false;
            });
        }

        envValues.temp = Math.max(Math.min(envValues.temp, envValues.max), envValues.min);
        return envValues.temp;
    }

    @Override
    public double calcActivityTemp() {
        double temp = 0;
        if (player.isSprinting() && player.getVehicle() == null)
            temp += ConfigData.SprintingModifier;
        if (player.isSwimming())
            temp += ConfigData.SwimmingModifier;
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
                    if (ClothingType.ArmourMaterials.get(mat).contains(armour.getType())) {
                        double addTemp = ConfigData.ClothingTypes.get(mat).warmth;
                        if (armour.getType().getEquipmentSlot() == EquipmentSlot.CHEST)
                            addTemp *= 1.6;
                        else if (armour.getType().getEquipmentSlot() == EquipmentSlot.LEGS)
                            addTemp *= 1.2;

                        temp += addTemp;
                    }
                }
        }
        return temp;
    }

    @Override
    public double applyClothingResistance(double temp) {
        return 0;
    }

    @Override
    public double applyCareResistance(double temp) {
        return 0;
    }

    @Override
    public double applyEffectResistance(double temp) {
        return 0;
    }

    @Override
    public void applyDebuffs(double temp) {

    }
}
