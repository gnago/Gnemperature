package io.github.gnago;

import io.github.gnago.command.ShowBarCommand;
import io.github.gnago.manager.file.ConfigData;
import org.bukkit.plugin.java.JavaPlugin;

public final class TemperaturePlugin extends JavaPlugin {

    private static TemperaturePlugin plugin;
    @Override
    public void onEnable() {
        plugin = this;
        ConfigData configData = new ConfigData();
        saveDefaultConfig();

        this.getCommand("showbaralways").setExecutor(new ShowBarCommand());

        getLogger().info("Enabled Temperature");
    }
    @Override
    public void onDisable() {

    }

    public static TemperaturePlugin getInstance() {
        return plugin;
    }
}
