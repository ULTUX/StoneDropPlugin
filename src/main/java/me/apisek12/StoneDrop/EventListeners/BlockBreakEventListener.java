package me.apisek12.StoneDrop.EventListeners;


import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.PluginMain;
import me.apisek12.StoneDrop.Utils.Chance;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class BlockBreakEventListener implements Listener {

<<<<<<< HEAD:src/main/java/me/apisek12/plugin/MainEventListener.java
    private final LinkedHashMap<String, DropChance> dropChances = PluginMain.dropChances;
    static String[] set; //Ore names
=======
    private final HashMap<String, DropChance> dropChances = PluginMain.dropChances;
    public static String[] set; //Ore names
>>>>>>> 67fcf035a83b02e7cc1c7b303a2115e87b362614:src/main/java/me/apisek12/StoneDrop/EventListeners/BlockBreakEventListener.java
    static final Map<Player, Long> messageTimestamp = new HashMap<>();
    private static final long ORE_MESSAGE_DELAY = 10000;
    private ArrayList<Inventory> openedChests = new ArrayList<>();
    private ArrayList<Location> chestLocations = new ArrayList<>();
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
        if (!PluginMain.dropExpOrb){
            float experienceToGive = PluginMain.experienceToDrop/((float)Math.sqrt((double)player.getLevel()+1));
            if (player.getExp() == 1.0 || player.getExp()+experienceToGive >= 1.0){
                player.setLevel(player.getLevel() + (int)(player.getExp()+experienceToGive));
                player.setExp((player.getExp() + experienceToGive)-((int)(player.getExp()+experienceToGive)));
            }
            else {
                player.setExp(player.getExp()+ experienceToGive);
            }
        } else {
            ExperienceOrb experienceOrb = (ExperienceOrb) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.EXPERIENCE_ORB);
            experienceOrb.setExperience((int) PluginMain.experienceToDrop);
        }


    }
    ItemStack getItemInHand(Player player){
        ItemStack tool = null;
        if (PluginMain.plugin.isVersionNew()){
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

                if (event.getBlock().getType().toString().contains("ORE"))
                {
                    if (PluginMain.dropFromOres && Chance.chance(PluginMain.oreDropChance)) return;
                    if (!PluginMain.dropFromOres) {
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

                if (PluginMain.dropBlocks.contains(block.getType()) && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) &&  (tool == Material.DIAMOND_PICKAXE ||
                        tool == PluginMain.golden || tool == Material.IRON_PICKAXE || tool == Material.STONE_PICKAXE || tool == PluginMain.wooden || (PluginMain.isNetherite && tool == Material.NETHERITE_PICKAXE))) {
                    if (PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get("COBBLE").isOn()) event.setDropItems(false);
                    if (Chance.chance(PluginMain.chestSpawnRate)) {
                        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> {
                            spawnChest(event.getBlock().getLocation(), event.getPlayer());
                        }, 4);


                    }

                    if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                        for (int i = 0; i < set.length; i++) {
                            DropChance oreSettings = dropChances.get(set[i]);
                            if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {

                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (Chance.chance(dropChances.get(set[i]).getF1()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn()) {
                                        ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf1(), dropChances.get(set[i]).getMaxf1()));
                                        applyEnchants(event, oreSettings, itemToDrop);
                                        applyCustomName(oreSettings, itemToDrop);
                                        dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                    }
                                }
                            }
                        }

                    } else if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                        for (int i = 0; i < set.length; i++) {
                            if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                DropChance oreSettings = dropChances.get(set[i]);
                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (Chance.chance(dropChances.get(set[i]).getF2()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn()){
                                        ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf2(), dropChances.get(set[i]).getMaxf2()));
                                        applyEnchants(event, oreSettings, itemToDrop);
                                        applyCustomName(oreSettings, itemToDrop);
                                        dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                    }
                                }
                            }
                        }

                    } else if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                        for (int i = 0; i < set.length; i++) {
                            DropChance oreSettings = dropChances.get(set[i]);
                            if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (Chance.chance(dropChances.get(set[i]).getF3()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn()){
                                        ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinf3(), dropChances.get(set[i]).getMaxf3()));
                                        applyEnchants(event, oreSettings, itemToDrop);
                                        applyCustomName(oreSettings, itemToDrop);
                                        dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                    }
                                }
                            }
                        }

                    } else {
                        for (int i = 0; i < set.length; i++) {
                            if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                DropChance oreSettings = dropChances.get(set[i]);
                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (Chance.chance(dropChances.get(set[i]).getNof()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn()) {
                                        ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinnof(), dropChances.get(set[i]).getMaxnof()));
                                        applyEnchants(event, oreSettings, itemToDrop);
                                        applyCustomName(oreSettings, itemToDrop);
                                        dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
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

    private void applyEnchants(BlockBreakEvent event, DropChance oreSettings, ItemStack itemToDrop) {
        if (oreSettings.getEnchant().size() > 0) {
            ItemMeta itemMeta = itemToDrop.getItemMeta();
            oreSettings.getEnchant().forEach(((enchantment, level) -> {
                itemMeta.addEnchant(enchantment, level, false);
            }));
            itemToDrop.setItemMeta(itemMeta);

        }
    }
    public static void applyCustomName(DropChance oreSettings, ItemStack itemToDrop){
        if (oreSettings.getCustomName() != null) {
            ItemMeta meta = itemToDrop.getItemMeta();
            meta.setDisplayName(oreSettings.getCustomName());
            itemToDrop.setItemMeta(meta);
        }
    }


    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e){
        if (!PluginMain.playerSettings.containsKey(e.getPlayer().getUniqueId().toString())
                || !PluginMain.playerLastVersionPluginVersion.containsKey(e.getPlayer().getUniqueId().toString())) {
            newPlayerJoined(e.getPlayer());

        }
        if (e.getPlayer().isOp() && isNewToUpdate(e.getPlayer().getUniqueId().toString())){
            displayUpdateMessage(e.getPlayer());
            PluginMain.playerLastVersionPluginVersion.remove(e.getPlayer().getUniqueId().toString());
            PluginMain.playerLastVersionPluginVersion.put(e.getPlayer().getUniqueId().toString(), PluginMain.currentPluginVersion);
        }

    }

    @EventHandler
    public void InventoryOpenEvent(InventoryOpenEvent event){
        if (event.getInventory().getHolder() instanceof Chest && chestLocations.contains(((Chest)(event.getInventory().getHolder())).getLocation())) {
            openedChests.add(event.getInventory());
        }
    }
    public void spawnChest(Location location, Player player){
        Block block = location.getBlock();
        block.setType(Material.CHEST);
        chestLocations.add(block.getLocation());
        if (PluginMain.treasureChestBroadcast) player.getServer().broadcastMessage(ChatColor.GOLD+ChatColor.translateAlternateColorCodes('&', Message.TREASURE_CHEST_BROADCAST.toString().replace("@name", player.getName())));
        if (PluginMain.plugin.isVersionNew()) player.sendTitle(ChatColor.GOLD + Message.TREASURE_CHEST_PRIMARY.toString(), ChatColor.AQUA + Message.TREASURE_CHEST_SECONDARY.toString(), 20, 20, 15);
        if (PluginMain.plugin.isVersionNew()) player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.3f, 1f);
        Chest chest = (Chest) block.getState();

        if (PluginMain.plugin.isVersionNew()) Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.TOTEM, location, 100, 0, 0, 0);

        for (Material material : PluginMain.chestContent.keySet()) {
            if (Chance.chance(PluginMain.chestContent.get(material).getChance())) {
                if (PluginMain.chestContent.get(material).getEnchantment() != null) {
                    ItemStack item = new ItemStack(material, Chance.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax()));
                    ItemMeta meta = item.getItemMeta();
                    PluginMain.chestContent.get(material).getEnchantment().forEach((whatToEnchant, level) -> {
                        if (whatToEnchant != null) meta.addEnchant(whatToEnchant, level, true);
                    });
                    item.setItemMeta(meta);
                    int freeSlot = getRandomFreeSlot(chest.getBlockInventory());
                    if (freeSlot >= 0) chest.getBlockInventory().setItem(freeSlot, item);
                } else
                    chest.getBlockInventory().addItem(new ItemStack(material, Chance.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax())));
            }
        }
    }


    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event){
        if (openedChests.contains(event.getInventory())){
            Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.plugin, () -> {
                if (PluginMain.plugin.isVersionNew()) ((Player) event.getPlayer()).playSound(Objects.requireNonNull(event.getInventory().getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, 0.4f, 0.1f);
                event.getInventory().clear();
                chestLocations.remove(((Chest)event.getInventory().getHolder()).getLocation());
                openedChests.remove(event.getInventory());
                if (event.getInventory().getHolder() instanceof Chest) ((Chest)event.getInventory().getHolder()).getLocation().getBlock().setType(Material.AIR);
                if (PluginMain.plugin.isVersionNew()) event.getInventory().getLocation().getWorld().spawnParticle(Particle.CLOUD, event.getInventory().getLocation(), 500, 0, 0, 0);
            }, 20);
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
        return playerVersionArray.length < serverVersionArray.length;
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
            LinkedHashMap<String, Setting> settings = new LinkedHashMap<>();
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
        Scanner reader;
        InputStream inputStream= PluginMain.plugin.getResource("update.txt");
        reader = new Scanner(inputStream, "utf-8");
        while (reader.hasNextLine()){
            String message = ChatColor.translateAlternateColorCodes('&', reader.nextLine());
            player.sendMessage(message);

        }


    }


}
