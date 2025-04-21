package me.gnago.gnemperature;

import me.gnago.gnemperature.api.PapiHelper;
import me.gnago.gnemperature.api.GnemperatureExpansion;
import me.gnago.gnemperature.command.CommandManager;
import me.gnago.gnemperature.manager.file.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GnemperaturePlugin extends JavaPlugin {

    private static GnemperaturePlugin plugin;
    private PapiHelper papiHelper;

    @Override
    public void onEnable() {
        plugin = this;
        ConfigData configData = new ConfigData();
        saveDefaultConfig();
        CommandManager.registerCommands();

        //this.getCommand("showbaralways").setExecutor(new ShowBarCommand());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new GnemperatureExpansion().register();
            papiHelper = new PapiHelper();
        }

        getLogger().info("Enabled Temperature");
    }
    @Override
    public void onDisable() {

    }

    public PapiHelper getPapiHelper() { return papiHelper; }
    public static GnemperaturePlugin getInstance() {
        return plugin;
    }
}
