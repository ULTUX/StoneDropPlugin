package me.apisek12.StoneDrop.EventListeners;


import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.PluginMain;
import me.apisek12.StoneDrop.Utils.BlockUtils;
import me.apisek12.StoneDrop.Utils.MathUtils;
import me.apisek12.StoneDrop.Utils.ItemUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;


public class BlockListener implements Listener {

    static final Map<Player, Long> messageTimestamp = new HashMap<>();
    private static final long ORE_MESSAGE_DELAY = 10000;
    public static String[] set; //Ore names
    private static LinkedHashMap<String, DropChance> dropChances;

    public static void initialize() {
        dropChances = PluginMain.dropChances;
        Bukkit.getServer().getConsoleSender().sendMessage("[" + PluginMain.plugin.getName() + "] Starting internal scheduler...");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PluginMain.plugin, new Runnable() {
            @Override
            public void run() {
                synchronized (messageTimestamp) {
                    Iterator iterator = messageTimestamp.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        Player player = (Player) entry.getKey();
                        Long timeStamp = (Long) entry.getValue();
                        if (System.currentTimeMillis() > timeStamp + 10000) {
                            iterator.remove();
                        }
                    }
                }
            }
        }, 20, 20 * ORE_MESSAGE_DELAY / 1000 - 1);
    }

    private void giveExp(Player player) {
        if (!PluginMain.dropExpOrb) {
            float experienceToGive = PluginMain.experienceToDrop / ((float) Math.sqrt((double) player.getLevel() + 1));
            if (player.getExp() == 1.0 || player.getExp() + experienceToGive >= 1.0) {
                player.setLevel(player.getLevel() + (int) (player.getExp() + experienceToGive));
                player.setExp((player.getExp() + experienceToGive) - ((int) (player.getExp() + experienceToGive)));
            } else {
                player.setExp(player.getExp() + experienceToGive);
            }
        } else {
            ExperienceOrb experienceOrb = (ExperienceOrb) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.EXPERIENCE_ORB);
            experienceOrb.setExperience((int) PluginMain.experienceToDrop);
        }


    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent event) {

        if (!PluginMain.disabledWorlds.contains(event.getPlayer().getWorld().getName())) {

            if (!event.isCancelled()) {
                Block block = event.getBlock();
                Location location = block.getLocation();
                World world = block.getWorld();
                Material tool = ItemUtils.getItemInHand(event.getPlayer()).getType();

                Material breakBlockName = event.getBlock().getType();
                if (breakBlockName.toString().contains("ORE") || event.getBlock().getType().toString().equalsIgnoreCase("ancient_debris")) {
                    if (PluginMain.dropFromOres && MathUtils.chance(PluginMain.oreDropChance)) return;
                    if (!PluginMain.dropFromOres) {
                        if (PluginMain.dropOresWhiteList != null &&
                                PluginMain.dropOresWhiteList.contains(breakBlockName)) {
                            return;
                        }
                        if (!messageTimestamp.containsKey(event.getPlayer())) {
                            event.getPlayer().sendMessage(ChatColor.RED + Message.INFO_DROP_DISABLED.toString());
                            synchronized (messageTimestamp) {
                                messageTimestamp.put(event.getPlayer(), System.currentTimeMillis());
                            }
                        }
                    }
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    return;
                }

                if (PluginMain.dropBlocks.contains(block.getType())
                    && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                    && (tool.toString().contains("PICKAXE") || tool.toString().contains("SHOVEL") || tool.toString().contains("AXE") || tool.toString().contains("HOE") || (PluginMain.isNetherite && tool == Material.NETHERITE_PICKAXE))) {

                    if (PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get("COBBLE").isOn()) {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> event.getBlock().setType(Material.AIR), 1L);
                        Player player = event.getPlayer();
                        if (PluginMain.versionCompatible(12)) {
                            Damageable itemMeta = (Damageable) player.getInventory().getItemInMainHand().getItemMeta();
                            if (((ItemMeta) itemMeta).hasEnchant(Enchantment.DURABILITY)) {
                                if (MathUtils.chance(1f / (((ItemMeta) itemMeta).getEnchantLevel(Enchantment.DURABILITY) + 1))) {
                                    itemMeta.setDamage(itemMeta.getDamage() + 1);
                                }
                            } else {
                                itemMeta.setDamage(itemMeta.getDamage() + 1);
                            }
                            if (itemMeta.getDamage() > player.getInventory().getItemInMainHand().getType().getMaxDurability())
                                player.getInventory().setItemInMainHand(null);
                            else player.getInventory().getItemInMainHand().setItemMeta((ItemMeta) itemMeta);
                        } else {
                            if (player.getItemInHand().containsEnchantment(Enchantment.DURABILITY)) {
                                if (MathUtils.chance(1f / (player.getItemInHand().getEnchantmentLevel(Enchantment.DURABILITY) + 1))) {
                                    player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
                                }
                            } else {
                                player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
                            }
                            if (player.getItemInHand().getDurability() > player.getItemInHand().getType().getMaxDurability())
                                player.setItemInHand(null);
                        }
                        player.updateInventory();
                    }
                    if (MathUtils.chance(PluginMain.chestSpawnRate)) {
                        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> {
                            spawnChest(event.getBlock().getLocation(), event.getPlayer());
                        }, 4);
                    }


                    PluginMain.commands.forEach((executeCommands) -> {
                        if (!executeCommands.isRequiredPermission() || event.getPlayer().hasPermission("stonedrop.exec-commands"))
                            if (MathUtils.chance(executeCommands.getChance())) {
                                String[] commands = executeCommands.getCommands().toArray(new String[0]);
                                Arrays.stream(commands).map(command -> command.replaceAll("@player", event.getPlayer().getName())).forEach(command -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command));
                            }
                    });


                    if (ItemUtils.getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.SILK_TOUCH) >= 1) {
                        for (String s : set) {
                            if (!s.equals("COBBLE") && !s.equals("STACK")) {

                                DropChance oreSettings = dropChances.get(s);

                                if (oreSettings.getAcceptedBiomes() != null && oreSettings.getAcceptedBiomes().length > 0
                                        && !Arrays.stream(oreSettings.getAcceptedBiomes()).anyMatch(biome -> event.getBlock().getBiome().equals(biome)))
                                    continue;

                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (MathUtils.chance(dropChances.get(s).getST()) && dropChances.get(s).isEnabled() && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(s).isOn()) {
                                        try {
                                            int dropAmount = MathUtils.randBetween(
                                                    dropChances.get(s).getMinST(),
                                                    dropChances.get(s).getMaxST()
                                            );
                                            ItemStack itemToDrop = ItemUtils.getItemStack(s, dropAmount);
                                            ItemUtils.applyEnchants(oreSettings, itemToDrop);
                                            ItemUtils.applyCustomName(oreSettings, itemToDrop);
                                            ItemUtils.dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                        } catch (NullPointerException e) {
                                            PluginMain.plugin.getLogger().log(Level.WARNING, "Material: " + s + " does not exist in this minecraft version, something is probably not properly set in config.yml file.");
                                        }

                                    }
                                }
                            }
                        }
                    }
                    int pickaxeLootLevel = ItemUtils.getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                    for (String s : set) {
                        if (!s.equals("COBBLE")) {
                            DropChance oreSettings = dropChances.get(s);

                            if (oreSettings.getAcceptedBiomes() != null && oreSettings.getAcceptedBiomes().length > 0
                                    && !Arrays.stream(oreSettings.getAcceptedBiomes()).anyMatch(biome -> event.getBlock().getBiome().equals(biome)))
                                continue;

                            if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                if (MathUtils.chance(dropChances.get(s).getFortuneChance(pickaxeLootLevel)) && dropChances.get(s).isEnabled() && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(s).isOn()) {
                                    try {
                                        int dropAmount = MathUtils.randBetween(
                                                (int) dropChances.get(s).getFortuneItemsAmountMin(pickaxeLootLevel),
                                                (int) dropChances.get(s).getFortuneItemsAmountMax(pickaxeLootLevel));
                                        ItemStack itemToDrop = ItemUtils.getItemStack(s, dropAmount);
                                        ItemUtils.applyEnchants(oreSettings, itemToDrop);
                                        ItemUtils.applyCustomName(oreSettings, itemToDrop);
                                        ItemUtils.dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                    } catch (NullPointerException e) {
                                        PluginMain.plugin.getLogger().log(Level.WARNING, "Material: " + s + " does not exist in this minecraft version, something is probably not properly set in config.yml file.");
                                    }
                                }
                            }
                        }

                    }

                    giveExp(event.getPlayer());

                }
            }
        }
    }

    public void spawnChest(Location location, Player player) {
        Block block = location.getBlock();
        if (PluginMain.treasureChestBroadcast)
            player.getServer().broadcastMessage(ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', Message.TREASURE_CHEST_BROADCAST.toString().replace("@name", player.getName())));
        if (PluginMain.plugin.versionCompatible(12))
            player.sendTitle(ChatColor.GOLD + Message.TREASURE_CHEST_PRIMARY.toString(), ChatColor.AQUA + Message.TREASURE_CHEST_SECONDARY.toString(), 20, 20, 15);
        if (PluginMain.plugin.versionCompatible(12))
            player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, (float) PluginMain.volume, 1f);


        ArrayList<ItemStack> chestInv = new ArrayList<>();
        for (Material material : PluginMain.chestContent.keySet()) {
            if (MathUtils.chance(PluginMain.chestContent.get(material).getChance())) {
                if (PluginMain.chestContent.get(material).getEnchantment() != null) {
                    ItemStack item = new ItemStack(material, MathUtils.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax()));
                    ItemMeta meta = item.getItemMeta();
                    PluginMain.chestContent.get(material).getEnchantment().forEach((whatToEnchant, level) -> {
                        if (whatToEnchant != null) meta.addEnchant(whatToEnchant, level, true);
                    });
                    item.setItemMeta(meta);
                    chestInv.add(item);
                } else
                    chestInv.add(new ItemStack(material, MathUtils.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax())));
            }
        }
        boolean isChestNeighbour = BlockUtils.hasTheSameNeighbour(block, Material.CHEST);

        if (!isChestNeighbour && !PluginMain.dropChestToInv || player.hasPermission("stonedrop.chest.to-inventory")) {
            block.setType(Material.CHEST);
            PluginMain.chestLocations.add(block.getLocation());

            Chest chest = (Chest) block.getState();

            if (PluginMain.plugin.versionCompatible(12))
                Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.TOTEM, location, 100, 0, 0, 0);

            chestInv.forEach(itemStack -> {
                int freeSlot = ItemUtils.getRandomFreeSlot(chest.getBlockInventory());
                if (freeSlot >= 0) chest.getBlockInventory().setItem(freeSlot, itemStack);
            });
        } else {
            if (isChestNeighbour) player.sendMessage(ChatColor.RED + Message.CHEST_CANT_BE_SPAWNED.toString());
            HashMap<Integer, ItemStack> items = player.getInventory().addItem(chestInv.toArray(new ItemStack[0]));
            items.forEach((integer, itemStack1) -> location.getWorld().dropItemNaturally(location, itemStack1));
        }
    }

}
