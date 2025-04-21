package me.gnago.gnemperature.command;

import me.gnago.gnemperature.GnemperaturePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CommandBase extends BukkitCommand implements CommandExecutor {
    private final int minArgs;
    private final int maxArgs;
    private final boolean playerOnly;
    private int delay = 0;
    private List<UUID> delayedPlayers = null;
    public CommandBase(String command) {
        this(command, 0);
    }
    public CommandBase(String command, boolean playerOnly) {
        this(command, 0, playerOnly);
    }
    public CommandBase(String command, int requiredArgs) {
        this(command, requiredArgs, requiredArgs);
    }
    public CommandBase(String command, int minArgs, int maxArgs) {
        this(command, minArgs, minArgs, false);
    }
    public CommandBase(String command, int requiredArgs, boolean playerOnly) {
        this(command, requiredArgs, requiredArgs, playerOnly);
    }
    public CommandBase(String command, int minArgs, int maxArgs, boolean playerOnly) {
        this(command, minArgs, maxArgs, playerOnly, null, null);
    }
    public CommandBase(String command, int minArgs, int maxArgs, boolean playerOnly, String description, List<String> aliases) {
        super(command);
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.playerOnly = playerOnly;
        if (description != null && !description.isEmpty()) {
            this.setDescription(description);
        }
        if (aliases != null && !aliases.isEmpty())
            this.setAliases(aliases);

        CommandMap commandMap = getCommandMap();
        if (commandMap != null) {
            commandMap.register(GnemperaturePlugin.getInstance().getName(), this);
        }
    }

    public CommandMap getCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);

                return (CommandMap) field.get(Bukkit.getPluginManager());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            GnemperaturePlugin.getInstance().getLogger().severe(e.getMessage());
        }

        return null;
    }

    public CommandBase enableDelay(int delay) {
        this.delay = delay;
        this.delayedPlayers = new ArrayList<>();
        return this;
    }
    public void endDelay(OfflinePlayer player) {
        this.delayedPlayers.remove(player.getUniqueId());
    }

    public void sendUsage(@NotNull CommandSender sender) {
        sender.sendMessage(getUsage());
    }

    @Override
    public boolean execute (@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        if (args.length < minArgs || (args.length < maxArgs && maxArgs != -1)) {
            sendUsage(sender);
            return true;
        }

        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage("&aThis command is player only.");
            return true;
        }

        String permission = getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage("&aYou do not have permission to use this command!");
            return true;
        }

        if (delayedPlayers != null && sender instanceof Player) {
            Player player = (Player) sender;
            if (delayedPlayers.contains(player.getUniqueId())) {
                player.sendMessage("&aPlease wait before using this command again.");
                return true;
            }

            delayedPlayers.add(player.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(GnemperaturePlugin.getInstance(), () ->
                    delayedPlayers.remove(player.getUniqueId()), 20L * delay);
        }

        if (!onCommand(sender, args)) {
            sendUsage(sender);
        }
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return onCommand(sender, args);
    }

    public abstract boolean onCommand(@NotNull CommandSender sender, String[] args);
    public abstract @Override @NotNull String getUsage();


}
