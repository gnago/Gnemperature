package me.gnago.gnemperature.listener;

import me.gnago.gnemperature.GnemperaturePlugin;
import me.gnago.gnemperature.manager.TemperatureMethods;
import me.gnago.gnemperature.manager.file.ConfigData;
import me.gnago.gnemperature.manager.player.PlayerSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

public class TemperatureListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        GnemperaturePlugin.getInstance().loadPlayerData(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        GnemperaturePlugin.getInstance().savePlayerData(e.getPlayer());
    }

    @EventHandler
    public void OnChangeWorld(PlayerChangedWorldEvent e) {
        if (!ConfigData.EnabledWorlds.contains(e.getPlayer().getWorld().getName()))
            e.getPlayer();//delete bossbar if not headed to enabled world
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        GnemperaturePlugin.getInstance().getPlayerData(e.getPlayer()).resetFeelsLike();
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) &&
            !GnemperaturePlugin.getInstance().getPlayerSetting(player, PlayerSettings.Key.LOCK_THERMOMETER)) {
            boolean mainWield = TemperatureMethods.isThermometer(player.getInventory().getItemInMainHand());
            boolean offWield = TemperatureMethods.isThermometer(player.getInventory().getItemInOffHand());
            if ((e.getHand() == EquipmentSlot.HAND && mainWield) || (e.getHand() == EquipmentSlot.OFF_HAND && offWield)) {
                // Prevent dual wielding thermometers from firing twice
                if (!(e.getHand() == EquipmentSlot.OFF_HAND && mainWield && offWield))
                {
                    player.sendMessage("Used thermometer!");
                }
            }
        }
    }
}
