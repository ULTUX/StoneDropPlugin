package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;


public class MyEvents implements Listener {

    private HashMap<String, DropChance> drop = PluginMain.dropChances;
    public static String[] set;


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();
        Material tool = event.getPlayer().getInventory().getItemInMainHand().getType();


        if (!PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifCobble && event.getBlock().getType() == Material.STONE) event.setDropItems(false);


        if (block.getBlockData().getMaterial() == Material.STONE && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) &&  (tool == Material.DIAMOND_PICKAXE ||
        tool == Material.GOLDEN_PICKAXE || tool == Material.IRON_PICKAXE || tool == Material.STONE_PICKAXE || tool == Material.WOODEN_PICKAXE)) {
            if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(drop.get(set[i]).getMinf1(), drop.get(set[i]).getMaxf1())));
                }
                //                if (Chance.chance(drop.get("diamond").getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(drop.get(), 2)));
//                if (Chance.chance(drop.get("gold").getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(1, 2)));
//                if (Chance.chance(drop.get("iron").getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(1, 2)));
//                if (Chance.chance(drop.get("coal").getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(1, 2)));
//                if (Chance.chance(drop.get("emerald").getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(1, 2)));
//                if (Chance.chance(drop.get("lapis").getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifLapis)
//                    world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(3, 6)));
//                if (Chance.chance(drop.get("redstone").getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifRedstone)
//                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(3, 5)));
                event.getPlayer().giveExp(15);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getF2()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(drop.get(set[i]).getMinf2(), drop.get(set[i]).getMaxf2())));
                }
//                if (Chance.chance(0.006) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(2, 3)));
//                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(2, 3)));
//                if (Chance.chance(0.04) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(2, 3)));
//                if (Chance.chance(0.06) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(2, 3)));
//                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(2, 3)));
//                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifLapis) world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(4, 7)));
//                if (Chance.chance(0.04) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifRedstone)
//                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(4, 6)));
                event.getPlayer().giveExp(18);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getF3()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(drop.get(set[i]).getMinf3(), drop.get(set[i]).getMaxf3())));
                }
//                if (Chance.chance(0.007) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(3, 4)));
//                if (Chance.chance(0.04) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, Chance.randBetween(3, 4)));
//                if (Chance.chance(0.05) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, Chance.randBetween(3, 4)));
//                if (Chance.chance(0.07) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, Chance.randBetween(3, 4)));
//                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, Chance.randBetween(3, 4)));
//                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifLapis) world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(5, 8)));
//                if (Chance.chance(0.05) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifRedstone) world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(5, 7)));
                event.getPlayer().giveExp(20);
            }
            else  {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getNof()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(Material.DIAMOND, Chance.randBetween(drop.get(set[i]).getMinnof(), drop.get(set[i]).getMaxnof())));
                }
//                if (Chance.chance(0.005) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifDiamond) world.dropItem(location, new ItemStack(Material.DIAMOND, 1));
//                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifGold) world.dropItem(location, new ItemStack(Material.GOLD_INGOT, 1));
//                if (Chance.chance(0.03) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifIron) world.dropItem(location, new ItemStack(Material.IRON_INGOT, 1));
//                if (Chance.chance(0.05) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifCoal) world.dropItem(location, new ItemStack(Material.COAL, 1));
//                if (Chance.chance(0.01) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifEmerald) world.dropItem(location, new ItemStack(Material.EMERALD, 1));
//                if (Chance.chance(0.01) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifLapis)
//                    world.dropItem(location, new ItemStack(Material.LAPIS_LAZULI, Chance.randBetween(2, 3)));
//                if (Chance.chance(0.02) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifRedstone)
//                    world.dropItem(location, new ItemStack(Material.REDSTONE, Chance.randBetween(2, 3)));
                event.getPlayer().giveExp(6);
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
        if (!PluginMain.playerSettings.containsKey(e.getPlayer().getUniqueId().toString())) PluginMain.playerSettings.put(e.getPlayer().getUniqueId().toString(), new Setting());
    }


}
