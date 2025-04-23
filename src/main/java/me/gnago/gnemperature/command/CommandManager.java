package me.gnago.gnemperature.command;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.manager.TemperatureMethods;
import me.gnago.gnemperature.manager.TemperatureScheduler;
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
                ((Player)sender).sendRawMessage("&7/thermometer-toggleunits:&f Toggle between Celsius and Fahrenheit. &eRight click while holding a thermometer to toggle.");
                ((Player)sender).sendRawMessage("&7/thermometer-showonlywhenholding:&f Toggle between whether the temperature shows only when you are holding a thermometer, or anytime it's anywhere in your inventory. &eRight click while crouching and holding a thermometer to toggle.");
                ((Player)sender).sendRawMessage("&7/thermometer-showactual:&f By default you only see the temperature you are currently \"feeling\". For balance reasons, this doesn't match with the \"actual\" temperature of your surroundings. This setting toggles whether the \"actual\" temperature shows along side your \"feels\" temperature.");
                ((Player)sender).sendRawMessage("&7/thermometer-locksettings:&f Locks whether right click changes thermometer settings while holding the thermometer.");
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-help";
            }
        };

        new CommandBase("thermometer-locksettings", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                GnemperaturePlugin.getInstance().getPlayerData((Player)sender).toggleSetting(PlayerSettings.Key.LOCK_THERMOMETER,
                        "&eThermometer will no longer switch modes on right-click. You can still use /thermometer-toggleunits and /thermometer-showonlywhenholding.",
                        "&eThermometer will now change units on right-click, and toggle actual temperature on right-click while sneaking.");
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
                TemperatureMethods.toggleUnits((Player)sender);
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-toggleunits";
            }
        };

        new CommandBase("thermometer-showonlywhenholding", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                TemperatureMethods.toggleShowFromInventory((Player)sender);
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-showonlywhenholding";
            }
        };

        new CommandBase("thermometer-showactual", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                TemperatureMethods.toggleShowActual((Player)sender);
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-showactual";
            }
        };

        new CommandBase("thermometer-setItem", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                if (GnemperaturePlugin.getInstance().setThermometerItem(((Player)sender).getInventory().getItemInMainHand()))
                    ((Player)sender).sendRawMessage("&aThermometer was successfully set!");
                else
                    ((Player)sender).sendRawMessage("&cFailed to set thermometer.");
                return true;
            }

            public @Override String initUsage() {
                return "/thermometer-setItem: Assigns the thermometer item to the item in your hand.";
            }
        }.setPermission("server.admin");

        new CommandBase("gnemperature-debugboard", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                if (GnemperaturePlugin.getInstance().getPlayerData((Player)sender).toggleSetting(PlayerSettings.Key.DEBUG_MODE_ON)) {
                    //todo turn on scoreboard if necessary
                }
                return true;
            }

            public @Override String initUsage() {
                return "/gnemperature-debugboard";
            }
        }.setPermission("server.admin");

        new CommandBase("gnemperature-debugdebuffs", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
                GnemperaturePlugin.getInstance().getPlayerData((Player)sender).toggleSetting(PlayerSettings.Key.DEBUG_DISABLE_DEBUFFS,
                        "Debuffs are &cdisabled", "Debuffs are &2enabled");
                return true;
            }

            public @Override String initUsage() {
                return "/gnemperature-debugdebuffs";
            }
        }.setPermission("server.admin");

        new CommandBase("gnemperature-disable", true) {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, String[] args) {
//                GnemperaturePlugin.getInstance().getPlayerData((Player)sender).toggleSetting(PlayerSettings.Key.TEMPERATURE_DISABLED,
//                        "disabled temperature", "enabled temperature");
                if (TemperatureScheduler.isRunning())
                    TemperatureScheduler.stop();
                else
                    TemperatureScheduler.start();
                return true;
            }

            public @Override String initUsage() {
                return "/gnemperature-disable";
            }
        }.setPermission("server.admin");
    }
}
