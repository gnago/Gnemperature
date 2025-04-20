package me.gnago.temperature.manager;

import org.bukkit.Material;

import java.util.*;

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

    public static final HashMap<MaterialType, Collection<Material>> ArmourMaterials = new HashMap<MaterialType, Collection<Material>>() {{
        put(MaterialType.LEATHER, Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS));
        put(MaterialType.CHAINMAIL, Arrays.asList(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS));
        put(MaterialType.IRON, Arrays.asList(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS));
        put(MaterialType.TURTLE, Collections.singletonList(Material.TURTLE_HELMET));
        put(MaterialType.GOLDEN, Arrays.asList(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS));
        put(MaterialType.DIAMOND, Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS));
        put(MaterialType.NETHERITE, Arrays.asList(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS));
    }};

    public enum MaterialType {
        LEATHER, CHAINMAIL, IRON, TURTLE, GOLDEN, DIAMOND, NETHERITE
    }
}
