package me.gnago.gnemperature.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandManager {
    public static void registerCommands() {
        new CommandBase("thermometer-help", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                sender.sendMessage("Works!");
                return true;
            }

            @Override
            public @NotNull String getUsage() {
                return "/thermometer-help";
            }
        };
        new CommandBase("thermometer-toggleunits", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                sender.sendMessage("Heyo");
                return true;
            }

            @Override
            public @NotNull String getUsage() {
                return "/thermometer-toggleunits";
            }
        };
    }
}
