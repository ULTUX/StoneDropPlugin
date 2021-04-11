package me.apisek12.StoneDrop.EventListeners;


import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.PluginMain;
import me.apisek12.StoneDrop.Utils.Chance;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class BlockBreakEventListener implements Listener {

    private static LinkedHashMap<String, DropChance> dropChances;
    public static String[] set; //Ore names
    static final Map<Player, Long> messageTimestamp = new HashMap<>();
    private static final long ORE_MESSAGE_DELAY = 10000;
    private ArrayList<Inventory> openedChests = new ArrayList<>();
    private ArrayList<Location> chestLocations = new ArrayList<>();
    public static void initialize(){
        dropChances = PluginMain.dropChances;
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
        if (PluginMain.plugin.versionCompatible(12)){
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
            if(PluginMain.dropSpreadMultiDir){
                chestDropItems(itemStack,player,location);
            }else {
                location.getWorld().dropItem(location, itemStack);
            }
        }
    }

    private static void chestDropItems(ItemStack itemStack, Player player,  Location location){
        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> {
            spawnDropChest(itemStack,player,location);
        }, 4);
    }
    private static  void spawnDropChest(ItemStack itemStack, Player player,Location location){
        location.getBlock().setType(Material.CHEST);

        Chest chest = (Chest) location.getBlock().getState();
        int chestSlotAmount = (int) (itemStack.getAmount()/27);
        int extraSlotAmount = (chestSlotAmount < 64) ?  itemStack.getAmount() % 27 : 0;
        for (int ci=0,extraAdd=0;ci<27;ci++,extraAdd++){
            int finalSlotItemAmount = (extraAdd < extraSlotAmount ) ? chestSlotAmount + 1 : chestSlotAmount;
            chest.getInventory().setItem(ci,new ItemStack(itemStack.getType(), finalSlotItemAmount));

        }

        Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(location.getBlock(),player));
        location.getBlock().setType(Material.AIR);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent event) {

        if (!PluginMain.disabledWorlds.contains(event.getPlayer().getWorld().getName())){

            if (!event.isCancelled()) {
                Block block = event.getBlock();
                Location location = block.getLocation();
                World world = block.getWorld();
                Material tool = getItemInHand(event.getPlayer()).getType();

                Material breakBlockName = event.getBlock().getType();
                if (breakBlockName.toString().contains("ORE") || event.getBlock().getType().toString().equalsIgnoreCase("ancient_debris"))
                {
                    if (PluginMain.dropFromOres && Chance.chance(PluginMain.oreDropChance)) return;
                    if (!PluginMain.dropFromOres) {
                        if(PluginMain.dropOresWhiteList!=null &&
                                PluginMain.dropOresWhiteList.contains(breakBlockName)){
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

                if (PluginMain.dropBlocks.contains(block.getType()) && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL) && (tool.toString().contains("PICKAXE") || tool.toString().contains("SHOVEL") || tool.toString().contains("AXE") || tool.toString().contains("HOE")) || (PluginMain.isNetherite && tool == Material.NETHERITE_PICKAXE)) {
                    if (PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get("COBBLE").isOn()) {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> event.getBlock().setType(Material.AIR), 1L);
                        Player player = event.getPlayer();
                        if (PluginMain.plugin.versionCompatible(12)){
                            Damageable itemMeta = (Damageable) player.getInventory().getItemInMainHand().getItemMeta();
                            if (((ItemMeta)itemMeta).hasEnchant(Enchantment.DURABILITY)){
                                if (Chance.chance(1f/(((ItemMeta)itemMeta).getEnchantLevel(Enchantment.DURABILITY)+1))){
                                    itemMeta.setDamage(itemMeta.getDamage()+1);
                                }
                            }
                            else {
                                itemMeta.setDamage(itemMeta.getDamage()+1);
                            }
                            if (itemMeta.getDamage() > player.getInventory().getItemInMainHand().getType().getMaxDurability()) player.getInventory().setItemInMainHand(null);
                            else player.getInventory().getItemInMainHand().setItemMeta((ItemMeta) itemMeta);
                        }
                        else {
                            if (player.getItemInHand().containsEnchantment(Enchantment.DURABILITY)){
                                if (Chance.chance(1f/(player.getItemInHand().getEnchantmentLevel(Enchantment.DURABILITY)+1))){
                                    player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability()+1));
                                }
                            }
                            else {
                                player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability()+1));
                            }
                            if (player.getItemInHand().getDurability() > player.getItemInHand().getType().getMaxDurability()) player.setItemInHand(null);
                        }
                        player.updateInventory();
                    }
                    if (Chance.chance(PluginMain.chestSpawnRate)) {
                        Bukkit.getScheduler().runTaskLater(PluginMain.plugin, () -> {
                            spawnChest(event.getBlock().getLocation(), event.getPlayer());
                        }, 4);
                    }
                    PluginMain.commands.forEach((s, aDouble) -> {
                        if (Chance.chance(aDouble)) {
                            String command = s.replace("@player", event.getPlayer().getName());
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
                        }
                    });

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

                    } else if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.SILK_TOUCH) >= 1) {
                        for (int i = 0; i < set.length; i++) {
                            if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {
                                DropChance oreSettings = dropChances.get(set[i]);
                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (Chance.chance(dropChances.get(set[i]).getST()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn()) {
                                        try {
                                            ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinST(), dropChances.get(set[i]).getMaxST()));
                                            applyEnchants(event, oreSettings, itemToDrop);
                                            applyCustomName(oreSettings, itemToDrop);
                                            dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                        }
                                        catch (NullPointerException e){
                                            PluginMain.plugin.getLogger().log(Level.WARNING, "Material: "+set[i]+" does not exist in this minecraft version, something is probably not properly set in config.yml file.");
                                        }

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
                                        try {
                                            ItemStack itemToDrop = new ItemStack(Material.getMaterial(set[i]), Chance.randBetween(dropChances.get(set[i]).getMinnof(), dropChances.get(set[i]).getMaxnof()));
                                            applyEnchants(event, oreSettings, itemToDrop);
                                            applyCustomName(oreSettings, itemToDrop);
                                            dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                        }
                                        catch (NullPointerException e){
                                            PluginMain.plugin.getLogger().log(Level.WARNING, "Material: "+set[i]+" does not exist in this minecraft version, something is probably not properly set in config.yml file.");
                                        }

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
            PluginMain.newPlayerJoined(e.getPlayer());

        }
        if (e.getPlayer().isOp() && isNewToUpdate(e.getPlayer().getUniqueId().toString())){
            PluginMain.displayUpdateMessage(e.getPlayer());
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
        if (PluginMain.treasureChestBroadcast) player.getServer().broadcastMessage(ChatColor.GOLD+ChatColor.translateAlternateColorCodes('&', Message.TREASURE_CHEST_BROADCAST.toString().replace("@name", player.getName())));
        if (PluginMain.plugin.versionCompatible(12)) player.sendTitle(ChatColor.GOLD + Message.TREASURE_CHEST_PRIMARY.toString(), ChatColor.AQUA + Message.TREASURE_CHEST_SECONDARY.toString(), 20, 20, 15);
        if (PluginMain.plugin.versionCompatible(12)) player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, (float)PluginMain.volume, 1f);


        ArrayList<ItemStack> chestInv = new ArrayList<>();
        for (Material material : PluginMain.chestContent.keySet()) {
            if (Chance.chance(PluginMain.chestContent.get(material).getChance())) {
                if (PluginMain.chestContent.get(material).getEnchantment() != null) {
                    ItemStack item = new ItemStack(material, Chance.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax()));
                    ItemMeta meta = item.getItemMeta();
                    PluginMain.chestContent.get(material).getEnchantment().forEach((whatToEnchant, level) -> {
                        if (whatToEnchant != null) meta.addEnchant(whatToEnchant, level, true);
                    });
                    item.setItemMeta(meta);
                    chestInv.add(item);
                } else
                    chestInv.add(new ItemStack(material, Chance.randBetween(PluginMain.chestContent.get(material).getMin(), PluginMain.chestContent.get(material).getMax())));
            }
        }
        boolean isChestNeighbour = false;

        for (int xi = -1; xi < 2; xi++){
            for (int zi = -1; zi < 2; zi++){
                if (xi == 0 && zi == 0 || xi*xi+zi*zi == 2) continue;
                if (block.getRelative(xi, 0, zi).getType().equals(Material.CHEST)) isChestNeighbour = true;
            }
        }
        if (!isChestNeighbour && !PluginMain.dropChestToInv) {
            block.setType(Material.CHEST);
            chestLocations.add(block.getLocation());

            Chest chest = (Chest) block.getState();

            if (PluginMain.plugin.versionCompatible(12))
                Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.TOTEM, location, 100, 0, 0, 0);

            chestInv.forEach(itemStack -> {
                int freeSlot = getRandomFreeSlot(chest.getBlockInventory());
                if (freeSlot >= 0) chest.getBlockInventory().setItem(freeSlot, itemStack);
            });
        }
        else {
            if (isChestNeighbour) player.sendMessage(ChatColor.RED+Message.CHEST_CANT_BE_SPAWNED.toString());
            HashMap<Integer, ItemStack> items = player.getInventory().addItem(chestInv.toArray(new ItemStack[0]));
            items.forEach((integer, itemStack1) -> location.getWorld().dropItemNaturally(location, itemStack1));
        }
    }


    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event){
        if (openedChests.contains(event.getInventory())){
            Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.plugin, () -> {
                if (PluginMain.plugin.versionCompatible(12)) ((Player) event.getPlayer()).playSound(Objects.requireNonNull(event.getInventory().getLocation()), Sound.ENTITY_ENDERMAN_TELEPORT, (float)PluginMain.volume, 0.1f);
                event.getInventory().clear();
                chestLocations.remove(((Chest)event.getInventory().getHolder()).getLocation());
                openedChests.remove(event.getInventory());
                if (event.getInventory().getHolder() instanceof Chest) ((Chest)event.getInventory().getHolder()).getLocation().getBlock().setType(Material.AIR);
                if (PluginMain.plugin.versionCompatible(12)) event.getInventory().getLocation().getWorld().spawnParticle(Particle.CLOUD, event.getInventory().getLocation(), 500, 0, 0, 0);
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


    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (PluginMain.dropFromOres && Chance.chance(PluginMain.oreDropChance)) return;
        for (Block b : e.blockList()) {
            if(b.getType().toString().contains("ORE")) {
                if (PluginMain.dropOresWhiteList != null &&
                        PluginMain.dropOresWhiteList.contains(b.getType())) {
                    continue;
                }

                b.setType(Material.AIR); // Stop item drops from spawning
            }

        }
    }

}
