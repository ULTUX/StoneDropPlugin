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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class MainEventListener implements Listener {

    private final HashMap<String, DropChance> dropChances = PluginMain.dropChances;
    static String[] set; //Ore names
    static final Map<Player, Long> messageTimestamp = new HashMap<>();
    private static final long ORE_MESSAGE_DELAY = 10000;
    public static void initialize(){
        Bukkit.getServer().getConsoleSender().sendMessage("["+PluginMain.plugin.getName()+"] Starting internal scheduler...");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PluginMain.plugin, new Runnable() {
            @Override
            public void run() {
                synchronized (messageTimestamp){
                    Iterator iterator = messageTimestamp.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry entry = (Map.Entry) iterator.next();
                        Player player = (Player) entry.getKey();
                        Long timeStamp = (Long) entry.getValue();
                        if (System.currentTimeMillis() > timeStamp+10000){
                            iterator.remove();
                        }
                    }
                }
            }
        }, 20, 20*ORE_MESSAGE_DELAY/1000-1);
    }

    private void giveExp(Player player){
        float experienceToGive = PluginMain.experienceToDrop/((float)Math.sqrt((double)player.getLevel()+1));
        if (player.getExp() == 1.0 || player.getExp()+experienceToGive >= 1.0){
            player.setLevel(player.getLevel() + (int)(player.getExp()+experienceToGive));
            player.setExp((player.getExp() + experienceToGive)-((int)(player.getExp()+experienceToGive)));
        }
        else {
            player.setExp(player.getExp()+ experienceToGive);
        }


    }
    ItemStack getItemInHand(Player player){
        ItemStack tool = null;
        if (PluginMain.isVersionNew()){
            try {
                tool = ((ItemStack) PlayerInventory.class.getMethod("getItemInMainHand").invoke(player.getInventory()));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            try {
                tool = ((ItemStack) PlayerInventory.class.getMethod("getItemInHand").invoke(player.getInventory()));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return tool;
    }

    private static void dropItems(ItemStack itemStack, Player player,  Location location){
        if (PluginMain.dropIntoInventory){
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(itemStack);
            for (Map.Entry<Integer, ItemStack> entry: remainingItems.entrySet()){
                location.getWorld().dropItem(location, entry.getValue());
            }
        }
        else {
            location.getWorld().dropItem(location, itemStack);
        }
    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent event) {
        if (!PluginMain.disabledWorlds.contains(event.getPlayer().getWorld().getName())){

            if (!PluginMain.isIsDisabled() && !event.isCancelled()) {
                Block block = event.getBlock();
                Location location = block.getLocation();
                World world = block.getWorld();
                Material tool = getItemInHand(event.getPlayer()).getType();

                if (!PluginMain.dropFromOres && !messageTimestamp.containsKey(event.getPlayer()) && event.getBlock().getType().toString().contains("ORE"))
                {
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    event.getPlayer().sendMessage(ChatColor.RED+Message.INFO_DROP_DISABLED.toString());
                   synchronized (messageTimestamp){
                       messageTimestamp.put(event.getPlayer(), System.currentTimeMillis());
                   }
                    return;
                }

                if (PluginMain.dropBlocks.contains(block.getType()) && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) &&  (tool == Material.DIAMOND_PICKAXE ||
                        tool == PluginMain.golden || tool == Material.IRON_PICKAXE || tool == Material.STONE_PICKAXE || tool == PluginMain.wooden || (PluginMain.isNetherite && tool == Material.NETHERITE_PICKAXE))) {
                    if (PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get("COBBLE").isOn()) event.setDropItems(false);
                    if (Chance.chance(PluginMain.chestSpawnRate)) {
                        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> {
                            block.setType(Material.CHEST);
                            event.getPlayer().sendTitle(ChatColor.GOLD + Message.TREASURE_CHEST_PRIMARY.toString(), ChatColor.AQUA + Message.TREASURE_CHEST_SECONDARY.toString(), 20, 20, 15);
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

                    if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
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
                                        dropItems(itemToDrop, event.getPlayer(), location);

                                    } else {
                                        dropItems(new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf1(), dropChances.get(set[i]).getMaxf1())), event.getPlayer(), location);

                                    }
                            }
                        }

                    } else if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
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
                                        dropItems(itemToDrop, event.getPlayer(), location);

                                    } else {
                                        dropItems(new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf2(), dropChances.get(set[i]).getMaxf2())), event.getPlayer(), location);

                                    }
                            }
                        }

                    } else if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
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
                                        dropItems(itemToDrop, event.getPlayer(), location);

                                    } else {
                                        dropItems(new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf3(), dropChances.get(set[i]).getMaxf3())), event.getPlayer(), location);

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
                                        dropItems(itemToDrop, event.getPlayer(), location);

                                    } else {
                                        dropItems(new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinnof(), dropChances.get(set[i]).getMaxnof())), event.getPlayer(), location);

                                    }
                            }
                        }
                    }
                    giveExp(event.getPlayer());

                }
            }
        }
    }


    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e){
        if (!PluginMain.playerSettings.containsKey(e.getPlayer().getUniqueId().toString())
                || !PluginMain.playerLastVersionPluginVersion.containsKey(e.getPlayer().getUniqueId().toString())) {
            newPlayerJoined(e.getPlayer());

        }
        if (isNewToUpdate(e.getPlayer().getUniqueId().toString())){
            displayUpdateMessage(e.getPlayer());
            PluginMain.playerLastVersionPluginVersion.remove(e.getPlayer().getUniqueId().toString());
            PluginMain.playerLastVersionPluginVersion.put(e.getPlayer().getUniqueId().toString(), PluginMain.currentPluginVersion);
        }

    }

    private boolean isNewToUpdate(String uid){
        if (PluginMain.playerLastVersionPluginVersion.get(uid) == null) {
            return true;
        }
        String playerVersion = PluginMain.playerLastVersionPluginVersion.get(uid);
        String serverVersion = PluginMain.currentPluginVersion;
        String[] playerVersionArray = playerVersion.replace(".", ",").split(",");
        String[] serverVersionArray = serverVersion.replace(".", ",").split(",");
        int min = Math.min(playerVersionArray.length, serverVersionArray.length);
        for (int i = 0; i < min; i++){
            int serverVersionPart = Integer.parseInt(serverVersionArray[i]);
            int playerVersionPart = Integer.parseInt(playerVersionArray[i]);
            if (serverVersionPart > playerVersionPart) return true;
        }
        return false;
    }


    private int getRandomFreeSlot(Inventory inv){
        ArrayList<Integer> possibleInv = new ArrayList<>();
        for (int i = 0; i < 27; i++) if (inv.getItem(i) == null) possibleInv.add(i);
        if (possibleInv.size() == 0) return -1;
        int random = Chance.randBetween(0, possibleInv.size()-1);
        return possibleInv.get(random);
    }


    private void newPlayerJoined(Player player){
        String uid = player.getUniqueId().toString();
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] Creating new player data");
        if (!PluginMain.playerSettings.containsKey(uid)) {
            HashMap<String, Setting> settings = new HashMap<>();
            for (int i = 0; i < set.length; i++) {
                settings.put(set[i], new Setting(true, set[i]));
            }
            settings.put("COBBLE", new Setting(false, "COBBLE"));
            settings.put("STACK", new Setting(false, "STACK"));
            PluginMain.playerSettings.put(uid, settings);
        }
        if (!PluginMain.playerLastVersionPluginVersion.containsKey(uid)) {
            PluginMain.playerLastVersionPluginVersion.put(uid, PluginMain.currentPluginVersion);
            if (PluginMain.displayUpdateMessage){
                displayUpdateMessage(player);
            }
        }

    }

    private void displayUpdateMessage(Player player) {
        Scanner reader = null;
        InputStream inputStream= PluginMain.plugin.getResource("update.txt");
        reader = new Scanner(inputStream, "utf-8");
        while (reader.hasNextLine()){
            String message = ChatColor.translateAlternateColorCodes('&', reader.nextLine());
            player.sendMessage(message);

        }


    }


}
