package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
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
        Material tool = event.getPlayer().getInventory().getItemInMainHand().getType();
        if (block.getBlockData().getMaterial() == Material.STONE && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) &&  (tool == Material.DIAMOND_PICKAXE ||
        tool == Material.GOLDEN_PICKAXE || tool == Material.IRON_PICKAXE || tool == Material.STONE_PICKAXE || tool == Material.WOODEN_PICKAXE)) {
            if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                if (Chance.chance(0.005)) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(1, 2)));
                if (Chance.chance(0.02)) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(1, 2)));
                if (Chance.chance(0.03)) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(1, 2)));
                if (Chance.chance(0.05)) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(1, 2)));
                if (Chance.chance(0.01)) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(1, 2)));
                if (Chance.chance(0.01))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(3, 6)));
                if (Chance.chance(0.025))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(3, 5)));
                event.getPlayer().giveExp(15);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                if (Chance.chance(0.006)) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(2, 3)));
                if (Chance.chance(0.03)) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(2, 3)));
                if (Chance.chance(0.04)) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(2, 3)));
                if (Chance.chance(0.06)) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(2, 3)));
                if (Chance.chance(0.02)) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(2, 3)));
                if (Chance.chance(0.02))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(4, 7)));
                if (Chance.chance(0.5))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(4, 6)));
                event.getPlayer().giveExp(18);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                if (Chance.chance(0.007)) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(3, 4)));
                if (Chance.chance(0.04)) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(3, 4)));
                if (Chance.chance(0.05)) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(3, 4)));
                if (Chance.chance(0.07)) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(3, 4)));
                if (Chance.chance(0.03)) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(3, 4)));
                if (Chance.chance(0.03))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(5, 8)));
                if (Chance.chance(0.75))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(5, 7)));
                event.getPlayer().giveExp(20);
            }
            else  {
                if (Chance.chance(0.005)) world.dropItem(location, new ItemStack(Material.DIAMOND, 1));
                if (Chance.chance(0.02)) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, 1));
                if (Chance.chance(0.03)) world.dropItem(location, new ItemStack(Material.IRON_INGOT, 1));
                if (Chance.chance(0.05)) world.dropItem(location, new ItemStack(Material.COAL, 1));
                if (Chance.chance(0.01)) world.dropItem(location, new ItemStack(Material.EMERALD, 1));
                if (Chance.chance(0.01))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(2, 5)));
                if (Chance.chance(0.025))
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(2, 4)));
                event.getPlayer().giveExp(12);
            }
        }
    }

}
