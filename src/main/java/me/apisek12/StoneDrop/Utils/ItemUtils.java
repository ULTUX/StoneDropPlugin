package me.apisek12.StoneDrop.Utils;

import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ItemUtils {

    private static final float SPREAD_RADIUS = 0.3F;

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
            }
            if (Material.getMaterial(itemName) == null) return null;
            return new ItemStack(Objects.requireNonNull(Material.getMaterial(itemName)),dropAmount);
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


    public static void dropItems(ItemStack itStckToDrop, Player player, Location location) {

        if(PluginMain.mcmmoSupport){
            int amountToDrop = itStckToDrop.getAmount();
            amountToDrop += McMMOUtils.increasePlayerDrop(player,amountToDrop);
            itStckToDrop.setAmount(amountToDrop);
        }

        if (PluginMain.dropIntoInventory) {
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(itStckToDrop);
            for (Map.Entry<Integer, ItemStack> entry : remainingItems.entrySet()) {
                Objects.requireNonNull(location.getWorld()).dropItem(location, entry.getValue());
            }
        } else if (PluginMain.realisticDrop) {
            dropMultiDirection(itStckToDrop,location);
        } else {
            Objects.requireNonNull(location.getWorld()).dropItem(location, itStckToDrop);
        }

    }


    private static void dropMultiDirection(ItemStack itemToDrop, Location location) {
        Random r = ThreadLocalRandom.current();
        for(int i=0; i<itemToDrop.getAmount();i++){
            Vector velocity = new Vector(r.nextGaussian()*SPREAD_RADIUS,r.nextFloat() + 0.5F, r.nextGaussian()*SPREAD_RADIUS);

            Objects.requireNonNull(location.getWorld()).dropItemNaturally(location.setDirection(velocity), new ItemStack(itemToDrop.getType(),1));
        }
    }
}
