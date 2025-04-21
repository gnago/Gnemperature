package me.gnago.gnemperature.command.thermometer;

import me.gnago.gnemperature.command.CommandBase;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ToggleUnits {
    public ToggleUnits() {
        new CommandBase("thermometer-toggleunits", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                return true;
            }

            @Override
            public @NotNull String getUsage() {
                return "/";
            }
        };
    }
}
