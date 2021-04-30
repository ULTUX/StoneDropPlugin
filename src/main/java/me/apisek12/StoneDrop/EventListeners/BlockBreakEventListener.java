package me.apisek12.StoneDrop.EventListeners;


import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.PluginMain;
import me.apisek12.StoneDrop.Utils.Chance;
import org.bukkit.*;
import org.bukkit.block.Biome;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
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
            if(PluginMain.realisticDrop){
                spawnDropChest(itemStack,player,location);
            }else {
                location.getWorld().dropItem(location, itemStack);
            }
        }
    }

    private static void spawnDropChest(ItemStack itemToChest, Player player,Location location){
        boolean isTrpChstNeighbour = hasTheSameNeighbour(location.getBlock(),Material.TRAPPED_CHEST);

        if (isTrpChstNeighbour){
            location.getWorld().dropItemNaturally(location, itemToChest);
        }
        else{
            location.getBlock().setType(Material.TRAPPED_CHEST);
            Chest chest = (Chest) location.getBlock().getState();
            ItemMeta meta = itemToChest.getItemMeta();
            int [] chestIndexesTable = new int [27];
            int chestSlotAmount = (itemToChest.getAmount()/27);
            Arrays.fill(chestIndexesTable,chestSlotAmount);
            //in these case is not possible to drop more than 27*64 the same item
            //by the way its very huge drop
            int extraSlotAmount = (chestSlotAmount < 64) ?  itemToChest.getAmount() % 27 : 0;
            for(int chestPlaceIndex=0;chestPlaceIndex<extraSlotAmount;chestPlaceIndex++)
                chestIndexesTable[chestPlaceIndex]+=1;
            for(int chestPlaceIndex=0;chestPlaceIndex<27;chestPlaceIndex++){
                if(chestIndexesTable[chestPlaceIndex]<=0) break;
                ItemStack stack = new ItemStack(
                        itemToChest.getType(),
                        chestIndexesTable[chestPlaceIndex],
                        itemToChest.getDurability()
                );
                stack.setItemMeta(meta);
                chest.getInventory().setItem(chestPlaceIndex, stack);
            }

            Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(location.getBlock(),player));
            location.getBlock().setType(Material.AIR);

        }

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


                    if (getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.SILK_TOUCH) >= 1) {
                        for (int i = 0; i < set.length; i++) {
                            if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {

                                DropChance oreSettings = dropChances.get(set[i]);

                                if (oreSettings.getAcceptedBiomes() != null && oreSettings.getAcceptedBiomes().length > 0
                                        && !Arrays.stream(oreSettings.getAcceptedBiomes()).anyMatch(biome -> event.getBlock().getBiome().equals(biome))) continue;

                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (Chance.chance(dropChances.get(set[i]).getST()) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn()) {
                                        try {
                                            int dropAmount = Chance.randBetween(
                                                    dropChances.get(set[i]).getMinST(),
                                                    dropChances.get(set[i]).getMaxST()
                                            );
                                            ItemStack itemToDrop = this.getItemStack(set[i],dropAmount);
                                            applyEnchants(oreSettings, itemToDrop);
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
                        int pickaxeLootLevel = getItemInHand(event.getPlayer()).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                        for (int i = 0; i < set.length; i++) {
                            if (!set[i].equals("COBBLE") && !set[i].equals("STACK")) {

                                DropChance oreSettings = dropChances.get(set[i]);

                                if (oreSettings.getAcceptedBiomes() != null && oreSettings.getAcceptedBiomes().length > 0
                                        && !Arrays.stream(oreSettings.getAcceptedBiomes()).anyMatch(biome -> event.getBlock().getBiome().equals(biome))) continue;

                                if (event.getBlock().getLocation().getBlockY() >= oreSettings.getMinLevel() && event.getBlock().getLocation().getBlockY() <= oreSettings.getMaxLevel()) {
                                    if (Chance.chance(dropChances.get(set[i]).getFortuneChance(pickaxeLootLevel)) && PluginMain.playerSettings.get(event.getPlayer().getUniqueId().toString()).get(set[i]).isOn()) {
                                        try {
                                            int dropAmount = Chance.randBetween(
                                                    (int) dropChances.get(set[i]).getFortuneItemsAmountMin(pickaxeLootLevel),
                                                    (int) dropChances.get(set[i]).getFortuneItemsAmountMax(pickaxeLootLevel));
                                            ItemStack itemToDrop = this.getItemStack(set[i], dropAmount);
                                            applyEnchants(oreSettings, itemToDrop);
                                            applyCustomName(oreSettings, itemToDrop);
                                            dropItems(itemToDrop, event.getPlayer(), event.getBlock().getLocation());
                                        } catch (NullPointerException e){
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

    private ItemStack getItemStack(String itemName, int dropAmount)  {
        if(!PluginMain.plugin.versionCompatible(12)){
            if(itemName.contains("LAPIS_LAZULI")){

                return new Dye(DyeColor.BLUE).toItemStack(dropAmount);
            }
            else if(itemName.contains("LAPIS_ORE")){
                return new ItemStack(PluginMain.lapis_ore,dropAmount);
            }
        }
        return new ItemStack(Material.getMaterial(itemName),dropAmount);
    }

    private static void applyEnchants(DropChance oreSettings, ItemStack itemToDrop) {
        if (oreSettings.getEnchant().size() > 0) {
            oreSettings.getEnchant().forEach(((enchantment, level) -> {
                System.out.println(enchantment.toString());
                System.out.println(level);
                if (enchantment != null && level > 0) itemToDrop.addUnsafeEnchantment(enchantment, level);
            }));
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


    public static boolean hasTheSameNeighbour(Block block, Material material){
        for (int xi = -1; xi < 2; xi++){
            for (int zi = -1; zi < 2; zi++){
                if (xi*xi+zi*zi == 2) continue;
                if (block.getRelative(xi, 0, zi).getType().equals(material)) return true;
            }
        }
        return false;
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
        boolean isChestNeighbour = hasTheSameNeighbour(block,Material.CHEST);

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
                if(openedChests.contains(event.getInventory())) openedChests.remove(event.getInventory());
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
