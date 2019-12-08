package me.apisek12.plugin;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;


public class MyEvents implements Listener {

    private HashMap<String, DropChance> dropChances = PluginMain.dropChances;
    public static String[] set; //Ore names

    private void giveExp(Player player){
        float experienceToGive = PluginMain.experienceToDrop/((float)Math.sqrt((double)player.getLevel()+1));
        if (player.getExp() == 1.0 || player.getExp()+experienceToGive >= 1.0){
            player.setLevel(player.getLevel() + (int)(player.getExp()+experienceToGive));
            player.setExp((player.getExp() + experienceToGive)-((int)player.getExp()+experienceToGive));
        }
        else {
            player.setExp(player.getExp()+ experienceToGive);
        }


    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent event) {

        if (!PluginMain.isIsDisabled() && !event.isCancelled()){
        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();
        Material tool = event.getPlayer().getInventory().getItemInMainHand().getType();





        if (block.getBlockData().getMaterial() == Material.STONE && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) &&  (tool == Material.DIAMOND_PICKAXE ||
        tool == Material.GOLDEN_PICKAXE || tool == Material.IRON_PICKAXE || tool == Material.STONE_PICKAXE || tool == Material.WOODEN_PICKAXE)) {
            if (PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get("COBBLE").isOn()) event.setDropItems(false);
                        if (Chance.chance(PluginMain.chestSpawnRate)) {
                            Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> {
                                block.setType(Material.CHEST);
                                event.getPlayer().sendTitle(ChatColor.GOLD + "You have found a " + ChatColor.GREEN + "treasure " + ChatColor.GOLD + "chest!", ChatColor.AQUA + "Ciekawe co jest w środku...", 20, 20, 15);
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
                                for (Material material : PluginMain.chestContent.keySet()) {
                                    if (Chance.chance(PluginMain.chestContent.get(material).getChance())) {
                                        if (PluginMain.chestContent.get(material).getEnchantment() != null) {
                                            ItemStack item = new ItemStack(material, Chance.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax()));
                                            ItemMeta meta = item.getItemMeta();
                                            PluginMain.chestContent.get(material).getEnchantment().forEach((whatToEnchant, level) -> {
                                                meta.addEnchant(whatToEnchant, level, true);
                                            });
                                            item.setItemMeta(meta);
                                            int freeSlot = getRandomFreeSlot(chest.getBlockInventory());
                                            if (freeSlot >= 0) chest.getBlockInventory().setItem(freeSlot, item);
                                        } else
                                            chest.getBlockInventory().addItem(new ItemStack(material, Chance.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax())));
                                    }
                                }
                            }, 4);


                        }

                        if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                            for (int i = 0; i < set.length; i++) {
                                if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                    if (Chance.chance(dropChances.get(set[i]).getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn())
                                        if (dropChances.get(set[i]).getEnchant().size() > 0) {
                                            ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf1(), dropChances.get(set[i]).getMaxf1()));
                                            ItemMeta itemMeta = itemToDrop.getItemMeta();
                                            dropChances.get(set[i]).getEnchant().forEach(((enchantment, level) -> {
                                                itemMeta.addEnchant(enchantment, level, false);
                                            }));
                                            itemToDrop.setItemMeta(itemMeta);
                                            world.dropItem(location, itemToDrop);

                                        } else {
                                            world.dropItem(location, new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf1(), dropChances.get(set[i]).getMaxf1())));

                                        }
                                }
                            }

                        } else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                            for (int i = 0; i < set.length; i++) {
                                if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                    if (Chance.chance(dropChances.get(set[i]).getF2()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn())
                                        if (dropChances.get(set[i]).getEnchant().size() > 0) {
                                            ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf2(), dropChances.get(set[i]).getMaxf2()));
                                            ItemMeta itemMeta = itemToDrop.getItemMeta();
                                            dropChances.get(set[i]).getEnchant().forEach(((enchantment, level) -> {
                                                itemMeta.addEnchant(enchantment, level, false);
                                            }));
                                            itemToDrop.setItemMeta(itemMeta);
                                            world.dropItem(location, itemToDrop);

                                        } else {
                                            world.dropItem(location, new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf2(), dropChances.get(set[i]).getMaxf2())));

                                        }
                                }
                            }

                        } else if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                            for (int i = 0; i < set.length; i++) {
                                if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                    if (Chance.chance(dropChances.get(set[i]).getF3()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn())
                                        if (dropChances.get(set[i]).getEnchant().size() > 0) {
                                            ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf3(), dropChances.get(set[i]).getMaxf3()));
                                            ItemMeta itemMeta = itemToDrop.getItemMeta();
                                            dropChances.get(set[i]).getEnchant().forEach(((enchantment, level) -> {
                                                itemMeta.addEnchant(enchantment, level, false);
                                            }));
                                            itemToDrop.setItemMeta(itemMeta);
                                            world.dropItem(location, itemToDrop);

                                        } else {
                                            world.dropItem(location, new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf3(), dropChances.get(set[i]).getMaxf3())));

                                        }
                                }
                            }

                        } else {
                            for (int i = 0; i < set.length; i++) {
                                if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                    if (Chance.chance(dropChances.get(set[i]).getNof()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn())
                                        if (dropChances.get(set[i]).getEnchant().size() != 0) {
                                            ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinnof(), dropChances.get(set[i]).getMaxnof()));
                                            ItemMeta itemMeta = itemToDrop.getItemMeta();
                                            dropChances.get(set[i]).getEnchant().forEach(((enchantment, level) -> {
                                                itemMeta.addEnchant(enchantment, level, false);
                                            }));
                                            itemToDrop.setItemMeta(itemMeta);
                                            world.dropItem(location, itemToDrop);

                                        } else {
                                            world.dropItem(location, new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinnof(), dropChances.get(set[i]).getMaxnof())));

                                        }
                                }
                            }
                        }
            giveExp(event.getPlayer());

        }
        }
    }


    @EventHandler
    public void PlayerRespawnEvent(PlayerRespawnEvent e) {

        Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.plugin, () -> {
            Player player = e.getPlayer();
            player.setGameMode(GameMode.SURVIVAL);
        }, 200);

    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e){
        if (!PluginMain.playerSettings.containsKey(e.getPlayer().getUniqueId().toString())) newPlayerJoined(e.getPlayer().getUniqueId().toString());
    }

    private int getRandomFreeSlot(Inventory inv){
        ArrayList<Integer> possibleInv = new ArrayList<>();
        for (int i = 0; i < 27; i++) if (inv.getItem(i) == null) possibleInv.add(i);
        if (possibleInv.size() == 0) return -1;
        int random = Chance.randBetween(0, possibleInv.size()-1);
        return possibleInv.get(random);
    }


    private void newPlayerJoined(String uid){
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] Creating new player data");
        HashMap<String, Setting> settings = new HashMap<>();
        for (int i = 0; i < set.length; i++){
            settings.put(set[i], new Setting(true, set[i]));
        }
        settings.put("COBBLE", new Setting(false, "COBBLE"));
        settings.put("STACK", new Setting(false, "STACK"));
        PluginMain.playerSettings.put(uid, settings);
    }



}
