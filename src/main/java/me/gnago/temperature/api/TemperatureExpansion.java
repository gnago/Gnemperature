package me.gnago.temperature.api;

import me.gnago.temperature.TemperaturePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TemperatureExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "temperature";
    }

    @Override
    public @NotNull String getAuthor() {
        return "gnago";
    }

    @Override
    public @NotNull String getVersion() {
        return TemperaturePlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("feels")) {
            return "0"; //todo: calculate player's feels temp and return
        }

        return null;
    }
}
