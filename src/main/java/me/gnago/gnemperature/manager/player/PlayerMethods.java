package me.gnago.gnemperature.manager.player;

public interface PlayerMethods {
    void calcTemperature();
    double calcClimateTemp();
    double calcWaterTemp();
    double calcWetnessTemp();
    double calcEnvironmentTemp();
    double calcActivityTemp();
    double calcStateTemp();
    double calcToolTemp();
    double calcClothingWarmth();
    double applyClothingResistance(double temp);
    double applyCareResistance(double temp);
    double applyEffectResistance(double temp);
    void applyDebuffs(double prevTemp, double currTemp);
}
