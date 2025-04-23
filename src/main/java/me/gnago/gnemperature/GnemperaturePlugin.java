package me.gnago.gnemperature;

import me.gnago.gnemperature.api.PapiHelper;
import me.gnago.gnemperature.api.GnemperatureExpansion;
import me.gnago.gnemperature.command.CommandManager;
import me.gnago.gnemperature.listener.TemperatureListener;
import me.gnago.gnemperature.manager.TemperatureScheduler;
import me.gnago.gnemperature.manager.file.ConfigData;
import me.gnago.gnemperature.manager.file.PlayerSettingsFile;
import me.gnago.gnemperature.manager.player.PlayerSettings;
import me.gnago.gnemperature.manager.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class GnemperaturePlugin extends JavaPlugin {

    private static GnemperaturePlugin plugin;
    private PapiHelper papiHelper;

    private ConfigData configData;
    private PlayerSettingsFile playerSettingsFile;
    private HashMap<Player, PlayerData> playerData;

    @Override
    public void onEnable() {
        plugin = this;
        configData = new ConfigData();
        saveDefaultConfig();
        CommandManager.registerCommands();

        playerSettingsFile = new PlayerSettingsFile("playersettings.db");
        playerData = new HashMap<>();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new GnemperatureExpansion().register();
            papiHelper = new PapiHelper();
        }

        getServer().getPluginManager().registerEvents(new TemperatureListener(), this);
        //TemperatureScheduler.start();
        getLogger().info("Enabled Gnemperature");
    }
    @Override
    public void onDisable() {

        getLogger().info("Disabled Gnemperature");
    }

    public PlayerSettingsFile getPlayerSettingsFile() {
        return playerSettingsFile;
    }
    public HashMap<Player, PlayerData> getPlayerData() {
        return playerData;
    }
    public PlayerData getPlayerData(Player player) {
        return playerData.get(player);
    }
    public Boolean getPlayerSetting(Player player, PlayerSettings.Key key) {
        return playerData.get(player).getSetting(key);
    }
    public void loadPlayerData(Player player) {
        if (!playerData.containsKey(player))
            playerData.put(player, new PlayerData(player, true));
    }
    public void savePlayerData(Player player) {
        if (playerData.containsKey(player))
            playerSettingsFile.prepPlayerSettings(playerData.get(player), true);
    }

    public boolean setThermometerItem(@NotNull ItemStack newThermometer) {
        return configData.SetThermometerItem(newThermometer);
    }

    public PapiHelper getPapiHelper() { return papiHelper; }
    public static GnemperaturePlugin getInstance() {
        return plugin;
    }
}
