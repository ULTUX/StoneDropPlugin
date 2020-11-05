package me.apisek12.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InventorySelector {
    private Player player;
    private HashMap<String, Setting> settings;
    private String title;
    private Inventory selector;
    HashMap<ItemStack, ArrayList<ItemStack>> items = new HashMap<>();

    public InventorySelector(Player player, HashMap<String, Setting> settings, String title) {
        this.player = player;
        this.settings = settings;
        this.title = title;
        settings.forEach((materialName, setting)->{
            Material material;
            if ((material = Material.getMaterial(materialName)) != null){
                DropChance dropData = PluginMain.dropChances.get(materialName);
                ItemStack item = new ItemStack(material);
                ItemMeta itemMeta = item.getItemMeta();
                dropData.getEnchant().forEach((enchantment, integer) -> itemMeta.addEnchant(enchantment, integer, true));
                ArrayList<ItemStack> dropChances = getItemDetailedData(dropData, material);

                items.put(item, dropChances);
            }
        });
        run();
    }

    private ArrayList<ItemStack> getItemDetailedData(DropChance dropData, Material dropMaterial){
        ArrayList<ItemStack> items = new ArrayList<>();
        ItemStack f0, f1, f2, f3;

        f0 = new ItemStack(dropMaterial);
        f1 = new ItemStack(dropMaterial);
        f2 = new ItemStack(dropMaterial);
        f3 = new ItemStack(dropMaterial);

        items.add(f0);
        items.add(f1);
        items.add(f2);
        items.add(f3);

        f1.addEnchantments(dropData.getEnchant());
        f2.addEnchantments(dropData.getEnchant());
        f3.addEnchantments(dropData.getEnchant());

        f0.getItemMeta().setDisplayName(ChatColor.GREEN+"Fortune 0");
        f1.getItemMeta().setDisplayName(ChatColor.GREEN+"Fortune 1");
        f2.getItemMeta().setDisplayName(ChatColor.GREEN+"Fortune 2");
        f3.getItemMeta().setDisplayName(ChatColor.GREEN+"Fortune 3");

        f0.getItemMeta().setLore(generateItemLore(dropData, 0));
        f1.getItemMeta().setLore(generateItemLore(dropData, 1));
        f2.getItemMeta().setLore(generateItemLore(dropData, 2));
        f3.getItemMeta().setLore(generateItemLore(dropData, 3));

        return items;

    }

    private ArrayList<String> generateItemLore(DropChance dropData, int level) {
        ArrayList<String> lore = new ArrayList<>();
        double chance = 0;
        int min = 0,max = 0;
        assert level == 0 || level == 1 || level == 2 || level == 3;
        switch (level){
            case 0:
                chance = dropData.getNof();
                min = dropData.getMinnof();
                max = dropData.getMaxnof();
                break;
            case 1:
                chance = dropData.getF1();
                min = dropData.getMinf1();
                max = dropData.getMaxnof();
                break;
            case 2:
                chance = dropData.getF2();
                min = dropData.getMinf2();
                max = dropData.getMaxf2();
                break;
            case 3:
                chance = dropData.getF3();
                min = dropData.getMinf3();
                max = dropData.getMaxf3();
                break;
        }
        lore.add(ChatColor.GRAY+"Drop chance: "+ChatColor.GOLD+chance);
        lore.add(ChatColor.GRAY+"Drop amount: "+ChatColor.GOLD+min+"-"+max);

        return lore;
    }

    private void run(){
        selector = Bukkit.createInventory(null, settings.size()+(9-settings.size()%9)+2*9, title);
        AtomicInteger index = new AtomicInteger(selector.getSize() - 9 - settings.size());
        items.forEach((itemStack, itemStacks) -> selector.setItem(index.getAndIncrement(), itemStack));
        player.openInventory(selector);
    }

    public void InventoryClickEvent(InventoryClickEvent event){
        if (event.getInventory().equals(selector)){

        }
    }
}
