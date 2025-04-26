package me.gnago.gnemperature.manager.file;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.manager.player.PlayerSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class PlayerSettingsFile {
    private File file;
    private FileConfiguration fileConfig;

    public PlayerSettingsFile(String filename) {
        this.file = createFile(filename);
        try {
            fileConfig = new YamlConfiguration();
            fileConfig.load(file);
            this.fileConfig = YamlConfiguration.loadConfiguration(file);
        } catch (IOException | InvalidConfigurationException | YAMLException e) {
            GnemperaturePlugin.getInstance().getLogger().severe(filename + " cannot be read! Archiving and creating new file...");
            file.renameTo(new File(GnemperaturePlugin.getInstance().getDataFolder(), filename + ".old." + new Date().getTime()));
            file = createFile(filename);
            this.fileConfig = YamlConfiguration.loadConfiguration(file);
        }
    }

    public File createFile(String filename) {
        File file = new File(GnemperaturePlugin.getInstance().getDataFolder(), filename);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            GnemperaturePlugin.getInstance().saveResource(filename, false);
        }
        return file;
    }

    public void prepPlayerSettings(PlayerSettings settings, boolean save) {
        // Convert enum keys to strings
        HashMap<String,Boolean> stringSettings = new HashMap<>();
        for (PlayerSettings.Key key : PlayerSettings.Key.values())
            stringSettings.put(key.name(),settings.getSetting(key));

        fileConfig.set(settings.getPlayerUUID().toString(), stringSettings);
        if (save)
            save();
    }

    public boolean save() {
        try {
            fileConfig.save(file);
            return true;
        } catch (IOException e) {
            GnemperaturePlugin.getInstance().getLogger().severe("Could not save player settings! " + e.getMessage());
            return false;
        }
    }
    public HashMap<PlayerSettings.Key,Boolean> loadSettings(UUID playerUUID, boolean createIfNotFound) {
        ConfigurationSection settingsSection = fileConfig.getConfigurationSection(playerUUID.toString());
        if (settingsSection != null) {
            HashMap<PlayerSettings.Key,Boolean> settings = new HashMap<>();
            for (PlayerSettings.Key key : PlayerSettings.Key.values())
                settings.put(key, settingsSection.getBoolean(key.name(), false));
            return settings;
        } else {
            if (createIfNotFound)
                prepPlayerSettings(new PlayerSettings(playerUUID), true);
            return PlayerSettings.getDefaultSettings();
        }
    }
}
