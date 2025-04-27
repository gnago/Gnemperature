package me.gnago.gnemperature.manager.player;

import me.gnago.gnemperature.GnemperaturePlugin;

import java.util.EnumMap;
import java.util.UUID;

public class PlayerSettings {
    public enum Key {
        USE_CELSIUS,
        SHOW_FROM_INVENTORY,
        SHOW_ACTUAL,
        LOCK_THERMOMETER,
        DEBUG_MODE_ON,
        DEBUG_DISABLE_DEBUFFS,
        TEMPERATURE_DISABLED
    }

    private final UUID playerUUID;
    private final EnumMap<Key,Boolean> settings;

    public PlayerSettings(UUID playerUUID, boolean loadFromFile) {
        this.playerUUID = playerUUID;
        if (loadFromFile)
            settings = GnemperaturePlugin.getInstance().getPlayerSettingsFile().loadSettings(playerUUID, true);
        else
            settings = getDefaultSettings();
    }
    public PlayerSettings(UUID playerUUID) {
        this(playerUUID, false);
    }

    public Boolean getSetting(Key key) {
        return settings.get(key);
    }
    public static EnumMap<Key,Boolean> getDefaultSettings() {
        EnumMap<Key,Boolean> defaultSettings = new EnumMap<>(Key.class);
        for (Key key : Key.values())
            defaultSettings.put(key, false);
        return defaultSettings;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean toggleSetting(Key setting, boolean save) {
        settings.put(setting, !settings.getOrDefault(setting,true));
        if (save)
            GnemperaturePlugin.getInstance().getPlayerSettingsFile().prepPlayerSettings(this, true);
        return settings.get(setting);
    }
}
