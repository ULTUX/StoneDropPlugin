package me.apisek12.StoneDrop.EventListeners;

import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Objects;

public class InventoryListener implements Listener {



    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        if (PluginMain.openedChests.contains(event.getInventory())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.plugin, () -> {
                if (PluginMain.plugin.versionCompatible(12))
                    ((Player) event.getPlayer()).playSound(Objects.requireNonNull(event.getInventory().getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, (float) PluginMain.volume, 0.1f);
                event.getInventory().clear();
                PluginMain.chestLocations.remove(((Chest) event.getInventory().getHolder()).getLocation());
                if (PluginMain.openedChests.contains(event.getInventory())) PluginMain.openedChests.remove(event.getInventory());
                if (event.getInventory().getHolder() instanceof Chest)
                    ((Chest) event.getInventory().getHolder()).getLocation().getBlock().setType(Material.AIR);
                if (PluginMain.plugin.versionCompatible(12))
                    event.getInventory().getLocation().getWorld().spawnParticle(Particle.CLOUD, event.getInventory().getLocation(), 500, 0, 0, 0);
            }, 20);
        }
    }

    @EventHandler
    public void InventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Chest && PluginMain.chestLocations.contains(((Chest) (event.getInventory().getHolder())).getLocation())) {
            PluginMain.openedChests.add(event.getInventory());
        }
    }
}
