package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;


public class MyEvents implements Listener {


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();
        Material tool = event.getPlayer().getInventory().getItemInMainHand().getType();

        if (!PluginMain.playerSettings.get(event.getPlayer()).ifCobble && event.getBlock().getType() == Material.STONE) event.setDropItems(false);


        if (block.getBlockData().getMaterial() == Material.STONE && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) &&  (tool == Material.DIAMOND_PICKAXE ||
        tool == Material.GOLDEN_PICKAXE || tool == Material.IRON_PICKAXE || tool == Material.STONE_PICKAXE || tool == Material.WOODEN_PICKAXE)) {
            if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                if (Chance.chance(0.005) && PluginMain.playerSettings.get(event.getPlayer()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(1, 2)));
                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(1, 2)));
                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(1, 2)));
                if (Chance.chance(0.05) && PluginMain.playerSettings.get(event.getPlayer()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(1, 2)));
                if (Chance.chance(0.01) && PluginMain.playerSettings.get(event.getPlayer()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(1, 2)));
                if (Chance.chance(0.01) && PluginMain.playerSettings.get(event.getPlayer()).ifLapis)
                    world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(3, 6)));
                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer()).ifRedstone)
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(3, 5)));
                event.getPlayer().giveExp(15);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                if (Chance.chance(0.006) && PluginMain.playerSettings.get(event.getPlayer()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(2, 3)));
                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(2, 3)));
                if (Chance.chance(0.04) && PluginMain.playerSettings.get(event.getPlayer()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(2, 3)));
                if (Chance.chance(0.06) && PluginMain.playerSettings.get(event.getPlayer()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(2, 3)));
                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(2, 3)));
                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer()).ifLapis)
                    world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(4, 7)));
                if (Chance.chance(0.04) && PluginMain.playerSettings.get(event.getPlayer()).ifRedstone)
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(4, 6)));
                event.getPlayer().giveExp(18);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                if (Chance.chance(0.007) && PluginMain.playerSettings.get(event.getPlayer()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(3, 4)));
                if (Chance.chance(0.04) && PluginMain.playerSettings.get(event.getPlayer()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(3, 4)));
                if (Chance.chance(0.05) && PluginMain.playerSettings.get(event.getPlayer()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(3, 4)));
                if (Chance.chance(0.07) && PluginMain.playerSettings.get(event.getPlayer()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(3, 4)));
                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(3, 4)));
                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer()).ifLapis)
                    world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(5, 8)));
                if (Chance.chance(0.05) && PluginMain.playerSettings.get(event.getPlayer()).ifRedstone)
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(5, 7)));
                event.getPlayer().giveExp(20);
            }
            else  {
                if (Chance.chance(0.005) && PluginMain.playerSettings.get(event.getPlayer()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, 1));
                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, 1));
                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, 1));
                if (Chance.chance(0.05) && PluginMain.playerSettings.get(event.getPlayer()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, 1));
                if (Chance.chance(0.01) && PluginMain.playerSettings.get(event.getPlayer()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, 1));
                if (Chance.chance(0.04) && PluginMain.playerSettings.get(event.getPlayer()).ifLapis)
                    world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(2, 5)));
                if (Chance.chance(0.06) && PluginMain.playerSettings.get(event.getPlayer()).ifRedstone)
                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(2, 4)));
                event.getPlayer().giveExp(12);
            }
//            if (Chance.chance(1)){
//                Player player = event.getPlayer();
//                player.sendMessage(ChatColor.GOLD+"Gratulacje, znalazłeś"+ChatColor.GREEN+ChatColor.GOLD+" skrzynie skarbów"+ChatColor.GOLD+"!");
//                Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.plugin, new Runnable() {
//                    @Override
//                    public void run() {
//                        Block skrzynka = event.getBlock();
//                        skrzynka.setType(Material.CHEST);
//                        Chest chest = (Chest) skrzynka;
//                        ((Chest) skrzynka).getBlockInventory().addItem(new ItemStack(Material.TNT, 13));
//                        chest.update();
//
//                    }
//                }, 2);
//            }
        }
    }


    @EventHandler
    public void PlayerRespawnEvent(PlayerRespawnEvent e) {

        Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.plugin, new Runnable() {
            @Override
            public void run() {
                Player player = e.getPlayer();
                player.setGameMode(GameMode.SURVIVAL);
            }
        }, 200);

    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e){
        PluginMain.playerSettings.put(e.getPlayer(), new Setting());
    }

//    public static ItemStack[] getRandomItems(){
//        ItemStack[] pool = {new ItemStack(Material.SPONGE)};
//
//
//    }

}
