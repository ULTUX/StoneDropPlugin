package me.apisek12.StoneDrop.EventListeners;

import me.apisek12.StoneDrop.ConfigManager;
import me.apisek12.StoneDrop.PluginMain;
import me.apisek12.StoneDrop.Utils.MathUtils;
import me.apisek12.StoneDrop.Utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EntityListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (PluginMain.dropFromOres && MathUtils.chance(PluginMain.oreDropChance)) return;
        for (Block b : e.blockList()) {
            if (b.getType().toString().contains("ORE")) {
                if (PluginMain.dropOresWhiteList != null &&
                        PluginMain.dropOresWhiteList.contains(b.getType())) {
                    continue;
                }

                b.setType(Material.AIR); // Stop item drops from spawning
            }

        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        if (!PluginMain.playerSettings.containsKey(e.getPlayer().getUniqueId().toString())
                || !PluginMain.playerLastVersionPluginVersion.containsKey(e.getPlayer().getUniqueId().toString())) {
            ConfigManager.generateNewPlayerData(e.getPlayer());

        }
        if (e.getPlayer().hasPermission("stonedrop.display-message") && isNewToUpdate(e.getPlayer().getUniqueId().toString())) {
            MessageUtils.displayUpdateMessage(e.getPlayer());
            PluginMain.playerLastVersionPluginVersion.remove(e.getPlayer().getUniqueId().toString());
            PluginMain.playerLastVersionPluginVersion.put(e.getPlayer().getUniqueId().toString(), PluginMain.currentPluginVersion);
        }

    }

    private boolean isNewToUpdate(String uid) {
        if (PluginMain.playerLastVersionPluginVersion.get(uid) == null) {
            return true;
        }
        String playerVersion = PluginMain.playerLastVersionPluginVersion.get(uid);
        String serverVersion = PluginMain.currentPluginVersion;
        String[] playerVersionArray = playerVersion.replace(".", ",").split(",");
        String[] serverVersionArray = serverVersion.replace(".", ",").split(",");
        int min = Math.min(playerVersionArray.length, serverVersionArray.length);
        for (int i = 0; i < min; i++) {
            int serverVersionPart = Integer.parseInt(serverVersionArray[i]);
            int playerVersionPart = Integer.parseInt(playerVersionArray[i]);
            if (serverVersionPart > playerVersionPart) return true;
        }
        return playerVersionArray.length < serverVersionArray.length;
    }
}
