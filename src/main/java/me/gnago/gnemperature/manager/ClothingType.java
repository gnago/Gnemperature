package me.gnago.gnemperature.manager;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class ClothingType {
    public final MaterialType material;
    public final double warmth;
    public final double resistance;

    public ClothingType(MaterialType material, Double warmth, Double resistance) {
        this.material = material;
        this.warmth = warmth != null ? warmth : DefaultWarmth;
        this.resistance = resistance != null ? resistance : DefaultResistance;
    }

    private static double DefaultWarmth;
    private static double DefaultResistance;
    public static void setDefaults(double warmth, double resistance) {
        DefaultWarmth = warmth;
        DefaultResistance = resistance;
    }

    public enum MaterialType {
        LEATHER(Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS)),
        CHAINMAIL(Arrays.asList(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS)),
        IRON(Arrays.asList(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS)),
        TURTLE(Collections.singletonList(Material.TURTLE_HELMET)),
        GOLDEN(Arrays.asList(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS)),
        DIAMOND(Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS)),
        NETHERITE(Arrays.asList(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS));

        private final Collection<Material> pieces;
        MaterialType(Collection<Material> pieces) {
            this.pieces = pieces;
        }
        public Collection<Material> getPieces() {
            return pieces;
        }
    }
}
