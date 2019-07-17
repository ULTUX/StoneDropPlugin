package me.apisek12.plugin;

import javafx.scene.media.MediaException;
import jdk.nashorn.internal.codegen.types.Type;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import sun.plugin2.main.server.Plugin;

import java.util.HashMap;
import java.util.Set;


public class MyEvents implements Listener {

    private HashMap<String, DropChance> drop = PluginMain.dropChances;
    public static String[] set;


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {

        if (!PluginMain.isIsDisabled()){
        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();
        Material tool = event.getPlayer().getInventory().getItemInMainHand().getType();




        if (!PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).ifCobble && event.getBlock().getType() == Material.STONE) event.setDropItems(false);


        if (block.getBlockData().getMaterial() == Material.STONE && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) &&  (tool == Material.DIAMOND_PICKAXE ||
        tool == Material.GOLDEN_PICKAXE || tool == Material.IRON_PICKAXE || tool == Material.STONE_PICKAXE || tool == Material.WOODEN_PICKAXE)) {

            if (Chance.chance(PluginMain.chestSpawnRate)){
                Bukkit.getScheduler().runTaskLater(PluginMain.plugin, new Runnable() {
                    @Override
                    public void run() {
                        block.setType(Material.CHEST);
//                        Location fireworkToSpawn = event.getBlock().getLocation();
//                        fireworkToSpawn.setY(fireworkToSpawn.getBlock().getLocation().getY()+20);
//                        Firework firework = (Firework) block.getLocation().getWorld().spawnEntity(fireworkToSpawn, EntityType.FIREWORK);
//                        FireworkMeta fireworkMeta = firework.getFireworkMeta();
//                        fireworkMeta.setPower(10);
//                        fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.LIME).with(FireworkEffect.Type.BALL_LARGE).flicker(true).withColor(Color.RED).withFade(Color.BLUE).build());
//                        firework.setFireworkMeta(fireworkMeta);
//                        firework.detonate();
                        event.getPlayer().sendTitle(ChatColor.GOLD+"Znalazłeś "+ ChatColor.GREEN+ "skrzynię "+ ChatColor.GOLD+"skarbów!", ChatColor.AQUA+"Ciekawe co jest w środku...", 20, 20, 15);
                        event.getPlayer().playSound(event.getBlock().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1f);
                        Chest chest = (Chest) block.getState();
                        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, new Runnable() {
                            @Override
                            public void run() {
                                chest.getBlockInventory().clear();
                                block.setType(Material.AIR);
                                Firework firework = (Firework) block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.FIREWORK);
                                FireworkMeta fireworkMeta = firework.getFireworkMeta();
                                fireworkMeta.setPower(3);
                                fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).withColor(Color.GRAY).withFade(Color.AQUA).build());
                                firework.setFireworkMeta(fireworkMeta);
                                firework.detonate();
                            }
                        }, 200);
                        for (Material material : PluginMain.chestContent.keySet()){
                            if (Chance.chance(PluginMain.chestContent.get(material).getChance())){
                                chest.getBlockInventory().addItem(new ItemStack(material, Chance.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax())));
                            }
                        }
                    }
                }, 4);


            }

            if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(getOre(set[i]), Chance.randBetween(drop.get(set[i]).getMinf1(), drop.get(set[i]).getMaxf1())));

                }

                event.getPlayer().giveExp(15);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getF2()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(getOre(set[i]), Chance.randBetween(drop.get(set[i]).getMinf2(), drop.get(set[i]).getMaxf2())));
                }

                event.getPlayer().giveExp(18);
            }
            else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getF3()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(getOre(set[i]), Chance.randBetween(drop.get(set[i]).getMinf3(), drop.get(set[i]).getMaxf3())));
                }

                event.getPlayer().giveExp(20);
            }
            else  {
                for (int i = 0; i < set.length; i++){
                    if (Chance.chance(drop.get(set[i]).getNof()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i])) world.dropItem(location, new ItemStack(getOre(set[i]), Chance.randBetween(drop.get(set[i]).getMinnof(), drop.get(set[i]).getMaxnof())));
                }

        }
    }}}

    private Material getOre(String name){
        switch (name){
            case "diamond":
                return Material.DIAMOND;
            case "iron":
                return Material.IRON_INGOT;
            case "gold":
                return Material.GOLD_INGOT;
            case "coal":
                return Material.COAL;
            case "emerald":
                return Material.EMERALD;
            case "lapis":
                return Material.LAPIS_LAZULI;
            case "redstone":
                return Material.REDSTONE;
        }
        return null;
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
        e.getPlayer().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
    }




}
