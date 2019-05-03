package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;



public class MyEvents implements Listener {
    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();
        if (block.getBlockData().getMaterial() == Material.STONE && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            if (Chance.chance(0.005)) world.dropItem(location, new ItemStack(Material.DIAMOND, 1));
            if (Chance.chance(0.02)) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, 1));
            if (Chance.chance(0.03)) world.dropItem(location, new ItemStack(Material.IRON_INGOT, 1));
            if (Chance.chance(0.05)) world.dropItem(location, new ItemStack(Material.COAL, 1));
            if (Chance.chance(0.01)) world.dropItem(location, new ItemStack(Material.EMERALD, 1));
            if (Chance.chance(0.01)) world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(2, 5)));
            if (Chance.chance(0.025)) world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(2, 4)));
            event.getPlayer().giveExp(12);
        }
    }

}
