package me.gnago.temperature;

import me.gnago.temperature.api.PapiHelper;
import me.gnago.temperature.api.TemperatureExpansion;
import me.gnago.temperature.command.ShowBarCommand;
import me.gnago.temperature.manager.file.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TemperaturePlugin extends JavaPlugin {

    private static TemperaturePlugin plugin;
    private PapiHelper papiHelper;

    @Override
    public void onEnable() {
        plugin = this;
        ConfigData configData = new ConfigData();
        saveDefaultConfig();

        this.getCommand("showbaralways").setExecutor(new ShowBarCommand());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new TemperatureExpansion().register();
            papiHelper = new PapiHelper();
        }

        getLogger().info("Enabled Temperature");
    }
    @Override
    public void onDisable() {

    }

    public PapiHelper getPapiHelper() { return papiHelper; }
    public static TemperaturePlugin getInstance() {
        return plugin;
    }
}
