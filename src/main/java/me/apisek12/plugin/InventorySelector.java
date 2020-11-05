package me.apisek12.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InventorySelector implements Listener {
    private Player player;
    private HashMap<String, Setting> settings;
    private static String title = ChatColor.DARK_AQUA + "Item Drop Chances";
    private Inventory selector;
    private HashMap<ItemStack, ArrayList<ItemStack>> items = new HashMap<>();
    private static HashMap<Player, InventorySelector> objects = new HashMap<>();

    public InventorySelector() {
    }

    public InventorySelector(Player player, HashMap<String, Setting> settings) {
        this.player = player;
        this.settings = settings;
        objects.put(player, this);
        selector = Bukkit.createInventory(null, PluginMain.dropChances.size() + (9 - PluginMain.dropChances.size() % 9) + 2 * 9, title);
        reloadInventory();
        player.openInventory(selector);
    }

    private void refreshSettings(){
        items.clear();
        settings.forEach((materialName, setting) -> {
            Material material;
            if ((material = Material.getMaterial(materialName)) != null) {
                DropChance dropData = PluginMain.dropChances.get(materialName);
                ItemStack item = new ItemStack(material);
                ItemMeta itemMeta = item.getItemMeta();
                dropData.getEnchant().forEach((enchantment, integer) -> itemMeta.addEnchant(enchantment, integer, false));
                ArrayList<String> lore = new ArrayList<>();
                String onOff = "";
                if (setting.isOn()) onOff = ChatColor.GREEN + "enabled";
                else onOff = ChatColor.RED + "disabled";
                lore.add("");
                lore.add(ChatColor.DARK_GRAY + "--------------------");
                lore.add(ChatColor.GRAY + "This item drop is " + onOff + ".");
                lore.add(ChatColor.AQUA + "Right click to toggle.");
                lore.add(ChatColor.AQUA + "Left click to see details.");
                lore.add(ChatColor.DARK_GRAY + "--------------------");

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
                ArrayList<ItemStack> dropChances = getItemDetailedData(dropData, material);

                items.put(item, dropChances);
            }
        });
    }

    private ArrayList<ItemStack> getItemDetailedData(DropChance dropData, Material dropMaterial) {
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

        f0.addEnchantments(dropData.getEnchant());
        f1.addEnchantments(dropData.getEnchant());
        f2.addEnchantments(dropData.getEnchant());
        f3.addEnchantments(dropData.getEnchant());

        ItemMeta f0Meta = f0.getItemMeta();
        ItemMeta f1Meta = f2.getItemMeta();
        ItemMeta f2Meta = f2.getItemMeta();
        ItemMeta f3Meta = f3.getItemMeta();

        f0Meta.setDisplayName(ChatColor.GREEN + "Fortune 0");
        f1Meta.setDisplayName(ChatColor.GREEN + "Fortune 1");
        f2Meta.setDisplayName(ChatColor.GREEN + "Fortune 2");
        f3Meta.setDisplayName(ChatColor.GREEN + "Fortune 3");

        f0Meta.setLore(generateItemLore(dropData, 0));
        f1Meta.setLore(generateItemLore(dropData, 1));
        f2Meta.setLore(generateItemLore(dropData, 2));
        f3Meta.setLore(generateItemLore(dropData, 3));

        f0.setItemMeta(f0Meta);
        f1.setItemMeta(f1Meta);
        f2.setItemMeta(f2Meta);
        f3.setItemMeta(f3Meta);

        return items;

    }

    private ArrayList<String> generateItemLore(DropChance dropData, int level) {
        ArrayList<String> lore = new ArrayList<>();
        double chance = 0;
        int min = 0, max = 0;
        assert level == 0 || level == 1 || level == 2 || level == 3;
        DecimalFormat format = new DecimalFormat("##0.0##");
        switch (level) {
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
        chance *= 100;
        lore.add(ChatColor.GRAY + "Drop chance: " + ChatColor.GOLD + format.format(chance)+"%");
        lore.add(ChatColor.GRAY + "Drop amount: " + ChatColor.GOLD + min + "-" + max);

        return lore;
    }

    private void reloadInventory() {
        refreshSettings();
        AtomicInteger index = new AtomicInteger(selector.getSize() - 10 - PluginMain.dropChances.size());
        items.forEach((itemStack, itemStacks) -> selector.setItem(index.getAndIncrement(), itemStack));
    }
    private void openSecondaryWindow(ArrayList<ItemStack> items){
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA+"Drop information");
        AtomicInteger i = new AtomicInteger(10);
        items.forEach(item -> {
            inventory.setItem(i.getAndAdd(2), item);
        });
        player.openInventory(inventory);
    }
    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (objects.containsKey(event.getWhoClicked()) && event.getClickedInventory().equals(objects.get(event.getWhoClicked()).selector)) {
            event.setCancelled(true);
            InventorySelector inventorySelector = objects.get(event.getWhoClicked());
            Player player = inventorySelector.player;
            ItemStack clickedItem = event.getCurrentItem();
            if (event.isRightClick()) {
                inventorySelector.settings.get(clickedItem.getType().toString()).toggle();
                inventorySelector.reloadInventory();
            } else if (event.isLeftClick()) {
                player.closeInventory();
                inventorySelector.openSecondaryWindow(inventorySelector.items.get(clickedItem));
            }
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {

        if (objects.containsKey(event.getPlayer()) && event.getInventory().equals(objects.get(event.getPlayer()).selector)) {
            objects.remove(event.getPlayer());
        }
    }
}
