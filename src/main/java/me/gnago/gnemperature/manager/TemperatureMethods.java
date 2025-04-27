package me.gnago.gnemperature.manager;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.manager.file.ConfigData;
import me.gnago.gnemperature.manager.player.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Function;

public class TemperatureMethods {
    public static double fahrToCel(double fahr) {
        return (fahr-32)*5/9;
    }
    public static double getBiomeTemp(Block block) {
        // Dry biomes (desert, savannah, badlands) have a base temp of 2.
        // This is a weird jump from the base temp of 1 of jungles and stony peaks.
        // So just reset dry biomes to just above 1
        return Math.min(block.getTemperature(), 1.1);
    }

    public enum ResistType { BOTH, COLD, HOT }
    public static double calcResistPercent(double initTemp, double resistFactor, double amplifier, EnumSet<ResistType> resistSet) {
        // Only check for hot/cold resist if temperature warrants resistance
        double resistPercent = 1; // the percent to reduce the temp to. If it remains 1, it maintains 100% of its value

        if (resistFactor > 1 && resistFactor <= 2) { //If modifier is ]1,2] then resist all
            resistPercent = Math.pow(2 - resistFactor, amplifier);
            resistSet.add(ResistType.BOTH);
        }
        else if (resistFactor >= -1 && resistFactor < 0) { // If factor is [-1,0[ then resist cold
            if (initTemp >= ConfigData.IdealTemperature) { // Don't attempt resisting cold if the player is not cold
                resistPercent = Math.pow(1 - Math.abs(resistFactor), amplifier);
                resistSet.add(ResistType.COLD);
            }
        }
        else if (resistFactor > 0 && resistFactor <= 1) { // If factor is ]0,1] then resist hot
            if (initTemp <= ConfigData.IdealTemperature) { // Don't attempt resisting heat if the player is not hot
                resistPercent = Math.pow(1 - resistFactor, amplifier);
                resistSet.add(ResistType.HOT);
            }
        }
        return resistPercent;
    }
    public static double calcResist(double initTemp, double resistFactor, double amplifier, EnumSet<ResistType> resistSet) {
        return calcResistBasic(initTemp, calcResistPercent(initTemp, resistFactor, amplifier, resistSet));
    }
    public static double calcResist(double initTemp, double resistFactor, double amplifier) {
        return calcResist(initTemp, resistFactor, amplifier, EnumSet.noneOf(ResistType.class));
    }
    public static double calcResist(double initTemp, double resistFactor) {
        return calcResist(initTemp, resistFactor, 1);
    }
    public static double calcResistBasic(double initTemp, double resistPercent) {
        return (initTemp - ConfigData.IdealTemperature) * resistPercent + ConfigData.IdealTemperature;
    }

    public static boolean isThermometer(ItemStack item) {
        return item.isSimilar(ConfigData.ThermometerItem);
        /*
        if (item.getType() != ConfigData.ThermometerItem.getType())
            return false;

        //ItemStack itemClone = item.clone();
        ItemStack itemClone = new ItemStack(item.getType()) {{
            setItemMeta(item.getItemMeta());
        }};

        // Exclude durability
        if (itemClone.getItemMeta() instanceof Damageable)
            ((Damageable)itemClone.getItemMeta()).setDamage(0);

        // Exclude Display name (in case of renamed by anvil)
        if (ConfigData.RecognizeRenamedThermometer && itemClone.getItemMeta() != null) {
            GnemperaturePlugin.getInstance().getLogger().info("Before:" + itemClone.getItemMeta().getDisplayName());
            itemClone.getItemMeta().setDisplayName("");
            GnemperaturePlugin.getInstance().getLogger().info("Now: " + itemClone.getItemMeta().getDisplayName());
        }
        return itemClone.isSimilar(ConfigData.ThermometerItem);
        */
    }

    /**
     *
     * @param loc Center of checked area
     * @param radius Distance to check
     * @param inSphere Whether the checked area is a sphere or a cube
     * @param fn Return false to continue the loop. Return true to stop it.
     */
    public static void forEachBlockInRadius(@NotNull Location loc, int radius, boolean inSphere, Function<Block,Boolean> fn) {
        boolean stopLoop = false;
        if (loc.getWorld() != null)
            for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius && !stopLoop; x++) {
                for (int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius && !stopLoop; y++) {
                    for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius && !stopLoop; z++) {
                        Block block = loc.getWorld().getBlockAt(x, y, z);
                        if (!inSphere || loc.distance(block.getLocation()) <= radius)
                            stopLoop = fn.apply(block);
                    }
                }
            }
    }
    public static void forEachBlockBetween(@NotNull Location begin, @NotNull Location end, Function<Block,Boolean> fn) {
        boolean stopLoop = false;
        if (begin.getWorld() != null && end.getWorld() != null) {
            BlockIterator blockIterator = new BlockIterator(begin.getWorld(),
                    begin.toVector(), end.toVector().subtract(begin.toVector()),
                    0, (int)begin.distance(end));
            while (blockIterator.hasNext() && !stopLoop) {
                stopLoop = fn.apply(blockIterator.next());
            }
        }
    }
    public static boolean isFullBlock(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        BoundingBox boundingBox = block.getBoundingBox();
        return (voxelShape.getBoundingBoxes().size() == 1
                && boundingBox.getWidthX() == 1.0
                && boundingBox.getHeight() == 1.0
                && boundingBox.getWidthZ() == 1.0
        );
    }

    public static boolean toggleUnits(Player p) {
        return GnemperaturePlugin.getInstance().getPlayerData(p).toggleSetting(PlayerSettings.Key.USE_CELSIUS,
                ChatColor.YELLOW+"Now using °C", ChatColor.YELLOW+"Now using °F");
    }
    public static boolean toggleShowFromInventory(Player p) {
        return GnemperaturePlugin.getInstance().getPlayerData(p).toggleSetting(PlayerSettings.Key.SHOW_FROM_INVENTORY,
                ChatColor.YELLOW+"Temperature is now always displayed if you have a thermometer in your inventory",
                ChatColor.YELLOW+"Temperature is now only displayed when holding a thermometer");
    }
    public static boolean toggleShowActual(Player p) {
        return GnemperaturePlugin.getInstance().getPlayerData(p).toggleSetting(PlayerSettings.Key.SHOW_ACTUAL,
                ChatColor.YELLOW+"Now additionally showing \"Actual Temperature\" (Feels/Actual)",
                ChatColor.YELLOW+"Hiding \"Actual Temperature\"");
    }
}
