package me.gnago.gnemperature.manager.player;

import fr.mrmicky.fastboard.FastBoard;
import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.api.PapiHelper;
import me.gnago.gnemperature.manager.ClothingType;
import me.gnago.gnemperature.manager.Temperature;
import me.gnago.gnemperature.manager.TemperatureMethods;
import me.gnago.gnemperature.manager.debuff.Debuff;
import me.gnago.gnemperature.manager.debuff.DebuffRegistry;
import me.gnago.gnemperature.manager.file.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerData extends PlayerSettings implements PlayerMethods, PlayerTemperatureDisplay {
    private final Temperature feelsLike;
    private final Temperature actuallyIs;
    private double wetness;
    private final HashMap<Integer,Debuff> scheduledDebuffs;
    private final ArrayList<Debuff> activeDebuffs;

    private BossBar bossBar;
    private FastBoard board;
    private final String[] debugLines;
    private StringBuilder debugClothingResistance;
    private StringBuilder debugEffectResistance;
    private StringBuilder debugCareResistance;
    private static final DecimalFormat df = new DecimalFormat("0.##");

    private final Player player;

    public PlayerData(Player player, boolean loadSettingsFromFile) {
        super(player.getUniqueId(), loadSettingsFromFile);
        this.player = player;
        this.actuallyIs = new Temperature();
        this.feelsLike = this.actuallyIs.copy();
        this.scheduledDebuffs = new HashMap<>();
        this.activeDebuffs = new ArrayList<>();
        if (bossBar == null)
            bossBar = Bukkit.createBossBar("Measuring...", BarColor.GREEN, BarStyle.SEGMENTED_20);

        debugLines = new String[15];
        if (getSetting(Key.DEBUG_MODE_ON))
            displayScoreboard(true);
    }

    private double feelsLikeTotal = 0;
    public double feelsLike() {
        return feelsLikeTotal;
    }
    public void applyResistances() {
        feelsLikeTotal = feelsLike.total(true, Temperature.Type.CLOTHING); // exclude clothing. Clothing cannot be resisted
        feelsLikeTotal = applyEffectResistance(applyClothingResistance(applyCareResistance(feelsLikeTotal)));
        feelsLikeTotal += feelsLike.total(false, Temperature.Type.CLOTHING);
    }
    public double actuallyIs() {
        return actuallyIs.total();
    }

    public void resetFeelsLike() {
        this.feelsLike.set(this.actuallyIs);
        feelsLikeTotal = feelsLike.total();
    }

    public boolean toggleSetting(Key setting, String onMessage, String offMessage) {
        boolean on = toggleSetting(setting, true);
        if (on)
            player.sendMessage(onMessage);
        else
            player.sendMessage(offMessage);
        return on;
    }

    @Override
    public void calcTemperature() {
        debugClothingResistance = new StringBuilder();
        debugEffectResistance = new StringBuilder();
        debugCareResistance = new StringBuilder();

        actuallyIs.set(calcClimateTemp(), calcWetnessTemp(), calcWaterTemp(), calcEnvironmentTemp(), calcClothingWarmth(), calcToolTemp(), calcActivityTemp(), calcStateTemp());
        feelsLike.approach(actuallyIs);
        applyResistances();


        setDebugLine(2, "Sun/Clm", String.format("%d: %s/%s", player.getLocation().getBlock().getLightFromSky(), df.format(feelsLike.get(Temperature.Type.CLIMATE)), df.format(actuallyIs.get(Temperature.Type.CLIMATE))));
        setDebugLine(3, "Wet", String.format("%s/%s: %s/%s", df.format(wetness), df.format(ConfigData.WetnessMax), df.format(feelsLike.get(Temperature.Type.WETNESS)), df.format(actuallyIs.get(Temperature.Type.WETNESS))));
        setDebugLine(4, "Water", String.format("%s/%s", df.format(feelsLike.get(Temperature.Type.WATER)), df.format(actuallyIs.get(Temperature.Type.WATER))));
        setDebugLine(5, "Env", String.format("%s/%s", df.format(feelsLike.get(Temperature.Type.ENVIRONMENT)), df.format(actuallyIs.get(Temperature.Type.ENVIRONMENT))));
        setDebugLine(6, "Activity", String.format("%s/%s", df.format(feelsLike.get(Temperature.Type.ACTIVITY)), df.format(actuallyIs.get(Temperature.Type.ACTIVITY))));
        setDebugLine(7, "State", String.format("%s/%s", df.format(feelsLike.get(Temperature.Type.STATE)), df.format(actuallyIs.get(Temperature.Type.STATE))));
        setDebugLine(8, "Tool", String.format("%s/%s", df.format(feelsLike.get(Temperature.Type.TOOL)), df.format(actuallyIs.get(Temperature.Type.TOOL))));
        setDebugLine(9, "Cloth", String.format("%s/%s %s", df.format(feelsLike.get(Temperature.Type.CLOTHING)), df.format(actuallyIs.get(Temperature.Type.CLOTHING)), debugClothingResistance));
        setDebugLine(10, "Resist", String.format("%s %s", debugEffectResistance, debugCareResistance));
        setDebugLine(11, "Total", String.format("%s/%s", df.format(feelsLike()), df.format(actuallyIs())));
    }

    @Override
    public double calcClimateTemp() {
        // 15 = maximum possible light from sky
        double climateExposure = Math.pow(player.getLocation().getBlock().getLightFromSky(),2)/Math.pow(15,2);
        double timeHeat = calcTimeHeat();
        double temp = calcWeatherTemp(timeHeat);

        setDebugLine(1, "Time Temp", String.format("%d: %s", getTime(), df.format(timeHeat)));

        return temp * climateExposure + ConfigData.IndoorTemperature * (1 - climateExposure);
    }

    private double calcTimeHeat() {
        double humidityFactor = 1 - player.getLocation().getBlock().getHumidity()/2;
        double biomeTemp = TemperatureMethods.getBiomeTemp(player.getLocation().getBlock());
        double biomeFactor = Math.abs(biomeTemp);

        setDebugLine(0, "Biome Temp/Hum", String.format("%s/%s", df.format(biomeTemp), df.format(humidityFactor)));

        // 1200 = noon
        // Use this graph to help visualize this formula https://www.desmos.com/calculator/xlzpwluttr
        return 10 * Math.cos(
                Math.PI * (getTime() - ConfigData.PeakTemperatureTime)/1200) *
                humidityFactor * (biomeFactor + 1) * (biomeTemp + 1) +
                32 * biomeTemp * biomeFactor / humidityFactor +
                32 * (biomeTemp + 1 - biomeFactor);
    }

    /**
     * @return time formated as HHMM. e.g. 19000, an hour past midnight, will return as 100
     */
    private long getTime() {
        long time = player.getWorld().getTime()/10;
        time += 600;
        if (time >= 2400)
            time -= 2400;
        return time;
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
        for (ItemStack armour : getArmour()) {
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
        AtomicReference<Double> totalRes = new AtomicReference<>(1.0);
        EnumSet<TemperatureMethods.ResistType> resistSet = EnumSet.noneOf(TemperatureMethods.ResistType.class);
        for (ItemStack armour : getArmour()) {
            for (ClothingType.MaterialType mat : ClothingType.MaterialType.values()) {
                if (mat.getPieces().contains(armour.getType())) {
                    totalRes.updateAndGet(v -> v * TemperatureMethods.calcResistPercent(aTemp.get(), ConfigData.ClothingTypes.get(mat).resistance, 1, resistSet));
                    break; // Armour can only be of one type
                }
            }
            ConfigData.ResistanceEnchantments.forEach((enchant, res) -> {
                if (armour.containsEnchantment(enchant)) {
                    totalRes.updateAndGet(v -> v * TemperatureMethods.calcResistPercent(aTemp.get(), res, armour.getEnchantmentLevel(enchant), resistSet));
                }
            });
        }
        temp = TemperatureMethods.calcResistBasic(temp, totalRes.get());
        debugClothingResistance.append(ChatColor.LIGHT_PURPLE).append("Res ").append(ChatColor.RESET)
                .append(df.format((1-totalRes.get())*100)).append("%");
        if (resistSet.contains(TemperatureMethods.ResistType.BOTH) || resistSet.contains(TemperatureMethods.ResistType.COLD))
            debugClothingResistance.append(ChatColor.AQUA).append("C");
        else
            debugClothingResistance.append(" ");
        if (resistSet.contains(TemperatureMethods.ResistType.BOTH) || resistSet.contains(TemperatureMethods.ResistType.HOT))
            debugClothingResistance.append(ChatColor.RED).append("H");
        else
            debugClothingResistance.append(" ");
        return temp;
    }

    private ItemStack[] getArmour() {
        return Arrays.stream(player.getInventory().getArmorContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);
    }

    @Override
    public double applyCareResistance(double temp) {
        // Hunger
        double resistPercent = calcCareResistance(ConfigData.HungerMidPoint, 20,
                player.getFoodLevel(), ConfigData.HungerMaxResist, ConfigData.HungerMaxVuln);
        temp = TemperatureMethods.calcResistBasic(temp, resistPercent);

        debugCareResistance.append(ChatColor.GOLD).append("H").append(ChatColor.RESET).append(df.format(0 - (resistPercent - 1) * 100)).append("%");

        PapiHelper papi = GnemperaturePlugin.getInstance().getPapiHelper();
        if (papi != null) { // If PlaceholderAPI was enabled on startup
            if (papi.placeholderExists(player,"%thirstbar_isDisabled%")) { // This soft-checks that the server has the ThirstBar plugin
                if (!Boolean.parseBoolean(papi.getPlaceholderString(player, "%thirstbar_isDisabled%"))) { // Check if enabled
                    try {
                        double thirstLevel = papi.getPlaceholderDouble(player, "%thirstbar_current_int%");
                        double thirstMax = papi.getPlaceholderDouble(player, "%thirstbar_max_int%");
                        resistPercent = calcCareResistance(ConfigData.ThirstMidPoint, thirstMax,
                                thirstLevel, ConfigData.ThirstMaxResist, ConfigData.ThirstMaxVuln);
                        temp = TemperatureMethods.calcResistBasic(temp, resistPercent);

                        debugCareResistance.append(ChatColor.AQUA).append("T").append(ChatColor.RESET).append(df.format(0-(resistPercent-1)*100)).append("%");
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
        double resistPercent = 1;
        EnumSet<TemperatureMethods.ResistType> resistSet = EnumSet.noneOf(TemperatureMethods.ResistType.class);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect != null && ConfigData.ResistanceEffects.containsKey(effect.getType())) {
                if (!ConfigData.ExcludeTurtleHelmetEffect ||
                    effect.getType() != PotionEffectType.WATER_BREATHING || effect.getDuration() > 200) {
                    resistPercent *= TemperatureMethods.calcResistPercent(temp, ConfigData.ResistanceEffects.get(effect.getType()), effect.getAmplifier(), resistSet);
                }
            }
        }
        debugEffectResistance.append(ChatColor.LIGHT_PURPLE).append("E ").append(ChatColor.RESET).append(df.format((1-resistPercent)*100.0)).append("%");
        if (!resistSet.isEmpty()) {
            if (resistSet.contains(TemperatureMethods.ResistType.BOTH) || resistSet.contains(TemperatureMethods.ResistType.COLD))
                debugEffectResistance.append(ChatColor.AQUA).append("C");
            else
                debugEffectResistance.append(" ");
            if (resistSet.contains(TemperatureMethods.ResistType.BOTH) || resistSet.contains(TemperatureMethods.ResistType.HOT))
                debugEffectResistance.append(ChatColor.RED).append("H");
            else
                debugEffectResistance.append(" ");

            temp = TemperatureMethods.calcResistBasic(temp, resistPercent);
            temp = Math.max(Math.min(temp, ConfigData.ResistTemperatureMax), ConfigData.ResistTemperatureMin);
        } else {
            debugEffectResistance.append("  ");
        }
        return temp;
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

        // clear active debuffs
        ArrayList<Debuff> removeDebuffs = DebuffRegistry.getDebuffsWithThresholdsUncrossed(prevTemp, currTemp);
        removeDebuffs.forEach(debuff -> debuff.clear(player));
        activeDebuffs.removeAll(removeDebuffs);

        // cancel any incoming debuffs
        ArrayList<Integer> removeIds = new ArrayList<>();
        scheduledDebuffs.forEach((id, debuff) -> {
            if (removeDebuffs.contains(debuff))
                removeIds.add(id);
        });
        removeIds.forEach(id -> {
            scheduledDebuffs.remove(id);
            if (Bukkit.getScheduler().isQueued(id))
                Bukkit.getScheduler().cancelTask(id);
            // Might need to add this in case the task ran anyway or something
            // debuff.clear(player);
        });
    }
    public void removeAllDebuffs() {
        activeDebuffs.forEach(d -> d.clear(player));
        activeDebuffs.clear();
        scheduledDebuffs.forEach((id, debuff) -> {
                if (Bukkit.getScheduler().isQueued(id))
                    Bukkit.getScheduler().cancelTask(id);
        });
        scheduledDebuffs.clear();
    }

    @Override
    public void displayBossBar(boolean show) {
        boolean hasBossBar = bossBar.getPlayers().contains(player);
        if (show && !hasBossBar)
            bossBar.addPlayer(player);
        else if (!show && hasBossBar)
            bossBar.removePlayer(player);
    }

    @Override
    public void updateBossBar() {
        double feelsLike = feelsLike();
        StringBuilder title;
        if (getSetting(Key.USE_CELSIUS)) {
            title = new StringBuilder(df.format(TemperatureMethods.fahrToCel(feelsLike))).append(" °C");
            if (getSetting(Key.SHOW_ACTUAL)) {
                title.append(" / ").append(df.format(TemperatureMethods.fahrToCel(actuallyIs()))).append(" °C");
            }
        } else {
            title = new StringBuilder(df.format(feelsLike)).append(" °F");
            if (getSetting(Key.SHOW_ACTUAL)) {
                title.append(" / ").append(df.format(actuallyIs())).append(" °F");
            }
        }
        bossBar.setTitle(title.toString());

             if (feelsLike > 180) bossBar.setColor(BarColor.PINK);
        else if (feelsLike > 120) bossBar.setColor(BarColor.RED);
        else if (feelsLike >  80) bossBar.setColor(BarColor.YELLOW);
        else if (feelsLike >  55) bossBar.setColor(BarColor.GREEN);
        else if (feelsLike >  32) bossBar.setColor(BarColor.BLUE);
        else if (feelsLike > -40) bossBar.setColor(BarColor.WHITE);
        else                      bossBar.setColor(BarColor.PURPLE);
    }

    @Override
    public void displayScoreboard(boolean show) {
        if (show) {
            if (board == null || board.isDeleted()) {
                board = new FastBoard(player);
                board.updateTitle(ChatColor.DARK_AQUA+"Temperature Factors"+ChatColor.RESET+" (f/a)");
            }
            updateScoreboard();
        } else {
            if (board != null && !board.isDeleted())
                board.delete();
        }
    }

    @Override
    public void updateScoreboard() {
        if (board != null) {
            for (int i = 0; i < debugLines.length; i++) {
                if (debugLines[i] != null && !debugLines[i].isEmpty())
                    board.updateLine(i, debugLines[i]);
            }
        }
    }
    private void setDebugLine(int index, String name, String value) {
        if (index >= 0 && index < debugLines.length) {
            debugLines[index] = ChatColor.GRAY+name+": "+ChatColor.RESET+value;
        }
    }
}
