package me.gnago.gnemperature.command;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.manager.player.PlayerSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    public static void registerCommands() {

        new CommandBase("save-settings", 1, true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                PlayerSettings settings = new PlayerSettings(((Player)sender).getUniqueId(), false);
                GnemperaturePlugin.getInstance().getPlayerSettingsFile().prepPlayerSettings(settings, true);
                sender.sendMessage("Go check!");
                return true;
            }

            public @Override String initUsage() {
                return "just testing";
            }
        };

        //Thermometer commands
        new CommandBase("thermometer-help", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                sender.sendMessage("Works!");
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-help";
            }
        };

        new CommandBase("thermometer-locksettings", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                sender.sendMessage("Works!");
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-locksettings";
            }

            public @Override List<String> initAliases() {
                List<String> aliases =  new ArrayList<>();
                aliases.add("thermometer-lock");
                return aliases;
            }
        };

        new CommandBase("thermometer-toggleunits", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                sender.sendMessage("Heyo");
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-toggleunits";
            }
        };

        new CommandBase("thermometer-setItem", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                if (GnemperaturePlugin.getInstance().setThermometerItem(((Player)sender).getInventory().getItemInMainHand()))
                    sender.sendMessage("&aThermometer was successfully set!");
                else
                    sender.sendMessage("&cFailed to set thermometer.");
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-setItem: Assigns the thermometer item to the item in your hand.";
            }
        }.setPermission("server.admin");
    }
}
