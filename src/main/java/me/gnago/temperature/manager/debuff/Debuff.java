package me.gnago.temperature.manager.debuff;

import me.gnago.temperature.TemperaturePlugin;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public abstract class Debuff {
    protected Collection<Double> thresholds;
    protected int delay;

    public static Debuff New(String debuffName, Collection<Double> thresholds, int delay) {
        debuffName = debuffName.toUpperCase();

        String[] potionData = debuffName.split(":"); //Config format is Effect_name:amplifier
        if (potionData.length > 1)
            try {
                return new PotionEffectDebuff(potionData[0], thresholds, delay, Integer.parseInt(potionData[1]));
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                TemperaturePlugin.getInstance().getLogger().warning("Could not decipher Debuff as potion effect: " + debuffName);
            }
        else {
            return new FunctionDebuff(thresholds, delay);
        }
        return null;
    }

    public Debuff(Collection<Double> thresholds, int delay) {
        this.thresholds = thresholds;
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public abstract void apply(LivingEntity entity);
    public abstract void clear(LivingEntity entity);
}
