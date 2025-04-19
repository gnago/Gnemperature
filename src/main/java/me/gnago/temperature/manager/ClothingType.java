package me.gnago.temperature.manager;

public class ClothingType {
    public final MaterialType Material;
    public final double Warmth;
    public final double Resistance;

    public ClothingType(MaterialType material, Double warmth, Double resistance) {
        Material = material;
        Warmth = warmth != null ? warmth : DefaultWarmth;
        Resistance = resistance != null ? resistance : DefaultResistance;
    }

    public static double DefaultWarmth;
    public static double DefaultResistance;

    public enum MaterialType {
        LEATHER, CHAINMAIL, IRON, TURTLE, GOLDEN, DIAMOND, NETHERITE
    }
}
