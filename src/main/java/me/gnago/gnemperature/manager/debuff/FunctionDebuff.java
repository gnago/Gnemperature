package me.gnago.gnemperature.manager.debuff;

import me.gnago.gnemperature.GnemperaturePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

public class FunctionDebuff extends Debuff {
    protected int applicationFrequency;
    protected Consumer<LivingEntity> applyFn;
    protected Consumer<LivingEntity> clearFn;
    protected HashMap<LivingEntity,Integer> repeatingDebuffIds;
    public FunctionDebuff(Collection<Double> thresholds, int delay) {
        super(thresholds, delay);
    }

    public FunctionDebuff setFunctions(int applicationFrequency, Consumer<LivingEntity> applyFn, Consumer<LivingEntity> clearFn) {
        this.applicationFrequency = applicationFrequency;
        this.applyFn = applyFn;
        this.clearFn = clearFn;
        return this;
    }

    @Override
    public void apply(LivingEntity entity) {
        if (applicationFrequency <= 0) // Less than 0 means only apply once after crossing threshold
            applyFn.accept(entity);
        else {
            int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(GnemperaturePlugin.getInstance(),
                    () -> applyFn.accept(entity), 0, applicationFrequency);

            if (id != -1)
                repeatingDebuffIds.put(entity, id);
        }
    }

    @Override
    public void clear(LivingEntity entity) {
        if (applicationFrequency <= 0)
            clearFn.accept(entity);
        else {
            Integer id = repeatingDebuffIds.get(entity);
            if (id != null && (Bukkit.getScheduler().isQueued(id) || Bukkit.getScheduler().isCurrentlyRunning(id))) {
                Bukkit.getScheduler().cancelTask(id);
            }
        }
    }
}
