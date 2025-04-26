package me.gnago.gnemperature.manager;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.manager.file.ConfigData;
import me.gnago.gnemperature.manager.player.PlayerSettings;
import org.bukkit.Bukkit;

public abstract class TemperatureScheduler {
    private static int taskId = -1;
    public static boolean start(long delay) {
        if (taskId == -1)
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GnemperaturePlugin.getInstance(), () -> {
                GnemperaturePlugin.getInstance().getPlayerData().forEach((player, data) -> {
                    double prevFeels = data.feelsLike();
                    data.calcTemperature();

                    if (!data.getSetting(PlayerSettings.Key.DEBUG_DISABLE_DEBUFFS))
                        data.applyDebuffs(prevFeels, data.feelsLike());

                    data.displayBossBar(ConfigData.AlwaysShowTemperature ||
                            (data.getSetting(PlayerSettings.Key.SHOW_FROM_INVENTORY) && player.getInventory().contains(ConfigData.ThermometerItem)) ||
                            TemperatureMethods.isThermometer(player.getInventory().getItemInMainHand()) || TemperatureMethods.isThermometer(player.getInventory().getItemInOffHand()));

                    data.updateBossBar();
                });
            }, delay, ConfigData.RefreshRate);
        return taskId != -1;
    }
    public static boolean start() {
        return start(0);
    }

    public static void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public static boolean isRunning() {
        return taskId != -1;
    }
}
