package me.apisek12.StoneDrop.Utils;

import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.EventListeners.BlockListener;
import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ItemUtils {

    public static ItemStack getItemStack(String itemName, int dropAmount)  {
            if(!PluginMain.versionCompatible(12)){
                if(itemName.contains("LAPIS_LAZULI")){
                    return new Dye(DyeColor.BLUE).toItemStack(dropAmount);
                }
                else if(itemName.contains("LAPIS_ORE")){
                    return new ItemStack(PluginMain.lapis_ore,dropAmount);
                }
                else if(itemName.contains("COBBLE")){
                    return new ItemStack(Material.COBBLESTONE,1);
                }
                else if(itemName.contains("STACK")){
                    return new ItemStack(Material.BOOK,1);
                }
            }
            if (Material.getMaterial(itemName) == null) return null;
            return new ItemStack(Material.getMaterial(itemName),dropAmount);
    }

    public static void applyEnchants(DropChance oreSettings, ItemStack itemToDrop) {
        if (oreSettings.getEnchant().size() > 0) {
            oreSettings.getEnchant().forEach(((enchantment, level) -> {
                if (enchantment != null && level > 0) itemToDrop.addUnsafeEnchantment(enchantment, level);
            }));
        }
    }

    public static void applyCustomName(DropChance oreSettings, ItemStack itemToDrop) {
        if (oreSettings.getCustomName() != null) {
            ItemMeta meta = itemToDrop.getItemMeta();
            meta.setDisplayName(oreSettings.getCustomName());
            itemToDrop.setItemMeta(meta);
        }
    }

    public static int getRandomFreeSlot(Inventory inv) {
        ArrayList<Integer> possibleInv = new ArrayList<>();
        for (int i = 0; i < 27; i++) if (inv.getItem(i) == null) possibleInv.add(i);
        if (possibleInv.size() == 0) return -1;
        int random = MathUtils.randBetween(0, possibleInv.size() - 1);
        return possibleInv.get(random);
    }

    public static ItemStack getItemInHand(Player player) {
        ItemStack tool = null;
        if (PluginMain.plugin.versionCompatible(12)) {
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

    public static void dropItems(ItemStack itemStack, Player player, Location location) {
        if (PluginMain.dropIntoInventory) {
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(itemStack);
            for (Map.Entry<Integer, ItemStack> entry : remainingItems.entrySet()) {
                location.getWorld().dropItem(location, entry.getValue());
            }
        } else {
            if (PluginMain.realisticDrop) {
                spawnDropChest(itemStack, player, location);
            } else {
                location.getWorld().dropItem(location, itemStack);
            }
        }
    }

    private static void spawnDropChest(ItemStack itemToChest, Player player, Location location) {
        boolean isTrpChstNeighbour = BlockUtils.hasTheSameNeighbour(location.getBlock(), Material.TRAPPED_CHEST);

        if (isTrpChstNeighbour) {
            location.getWorld().dropItemNaturally(location, itemToChest);
        } else {
            location.getBlock().setType(Material.TRAPPED_CHEST);
            Chest chest = (Chest) location.getBlock().getState();
            ItemMeta meta = itemToChest.getItemMeta();
            int[] chestIndexesTable = new int[27];
            int chestSlotAmount = (itemToChest.getAmount() / 27);
            Arrays.fill(chestIndexesTable, chestSlotAmount);
            //in this case it is not possible to drop more than 27*64 the same item
            int extraSlotAmount = (chestSlotAmount < 64) ? itemToChest.getAmount() % 27 : 0;
            for (int chestPlaceIndex = 0; chestPlaceIndex < extraSlotAmount; chestPlaceIndex++)
                chestIndexesTable[chestPlaceIndex] += 1;
            for (int chestPlaceIndex = 0; chestPlaceIndex < 27; chestPlaceIndex++) {
                if (chestIndexesTable[chestPlaceIndex] <= 0) break;
                ItemStack stack = new ItemStack(
                        itemToChest.getType(),
                        chestIndexesTable[chestPlaceIndex],
                        itemToChest.getDurability()
                );
                stack.setItemMeta(meta);
                chest.getInventory().setItem(chestPlaceIndex, stack);
            }

            Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(location.getBlock(), player));
            location.getBlock().setType(Material.AIR);

        }

    }
}
