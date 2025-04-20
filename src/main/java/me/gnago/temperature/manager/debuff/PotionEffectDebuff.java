package me.gnago.temperature.manager.debuff;

import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class PotionEffectDebuff extends Debuff {
    private PotionEffect potionEffect;

    public PotionEffectDebuff(PotionEffectType effectType, Collection<Double> thresholds, int delay, int duration, int amplifier) {
        super(thresholds, delay, duration);
        if (effectType != null) {
            potionEffect = new PotionEffect(effectType, -1, amplifier, true, false, true);
        }
    }
    public PotionEffectDebuff(String effectName, Collection<Double> thresholds, int delay, int duration, int amplifier) {
        this(Registry.EFFECT.match(effectName.toUpperCase()), thresholds, delay, duration, amplifier);
    }

    public PotionEffect getPotionEffect() {
        return potionEffect;
    }

    @Override
    public void apply(LivingEntity entity) {
        PotionEffect activeEffect = entity.getPotionEffect(potionEffect.getType());
        if (activeEffect == null) {
            potionEffect.apply(entity);
        } else {
            if (this.duration == -1 || activeEffect.getDuration() <= this.duration)
                potionEffect.apply(entity);
        }
    }

    @Override
    public void clear(LivingEntity entity) {
        PotionEffect activeEffect = entity.getPotionEffect(potionEffect.getType());
        if (activeEffect != null && activeEffect.getDuration() == -1) {
            entity.removePotionEffect(potionEffect.getType());
        }
    }
}
