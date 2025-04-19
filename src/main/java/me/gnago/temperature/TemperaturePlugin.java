package me.gnago.temperature;

import me.clip.placeholderapi.PlaceholderAPI;
import me.gnago.temperature.api.TemperatureExpansion;
import me.gnago.temperature.command.ShowBarCommand;
import me.gnago.temperature.manager.file.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class TemperaturePlugin extends JavaPlugin {

    private static TemperaturePlugin plugin;
    private @Nullable PlaceholderAPI placeholderAPI;

    @Override
    public void onEnable() {
        plugin = this;
        ConfigData configData = new ConfigData();
        saveDefaultConfig();

        this.getCommand("showbaralways").setExecutor(new ShowBarCommand());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new TemperatureExpansion().register();
        }

        getLogger().info("Enabled Temperature");
    }
    @Override
    public void onDisable() {

    }

    public @Nullable PlaceholderAPI getPlaceholderAPI() { return placeholderAPI; }
    public static TemperaturePlugin getInstance() {
        return plugin;
    }
}
