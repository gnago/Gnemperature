package me.gnago.temperature.command;

import me.gnago.temperature.manager.TemperatureMethods;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowBarCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location target = player.getTargetBlock(null, 8).getLocation();
            TemperatureMethods.forEachBlockInRadius(player.getLocation(), 8, true, block -> {
                block.setType(Material.GLASS);
                return false;
            });
            TemperatureMethods.forEachBlockBetween(player.getLocation(), target, block -> {
                block.setType(Material.ACACIA_LOG);
                return false;
            });
            player.sendMessage("Hello!");
        }

        return true;
    }
}
