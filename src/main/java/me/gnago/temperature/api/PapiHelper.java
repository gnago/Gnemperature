package me.gnago.temperature.api;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

public class PapiHelper {
    public boolean placeholderExists(OfflinePlayer player, String placeholder) {
        return !PlaceholderAPI.setPlaceholders(player, placeholder).equals(placeholder);
    }
    public String getPlaceholderString(OfflinePlayer player, String placeholder) {
        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }
    public double getPlaceholderDouble(OfflinePlayer player, String placeholder) throws NumberFormatException {
        return Double.parseDouble(getPlaceholderString(player, placeholder));
    }
}
