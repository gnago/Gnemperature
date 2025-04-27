package me.gnago.gnemperature.api;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GnemperatureExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return GnemperaturePlugin.getInstance().getDescription().getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", GnemperaturePlugin.getInstance().getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return GnemperaturePlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("feels"))
            return String.valueOf(GnemperaturePlugin.getInstance().getPlayerData(player).feelsLike());
        else if (params.equalsIgnoreCase("actual"))
            return String.valueOf(GnemperaturePlugin.getInstance().getPlayerData(player).actuallyIs());

        return null;
    }
}
