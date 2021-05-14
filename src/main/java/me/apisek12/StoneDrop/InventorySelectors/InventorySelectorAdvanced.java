package me.apisek12.StoneDrop.InventorySelectors;

import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InventorySelectorAdvanced extends InventorySelector{

    protected Inventory secondaryWindow;
    protected static HashMap<Player, InventorySelectorAdvanced> objects = new HashMap<>();
    protected LinkedHashMap<ItemStack, ArrayList<ItemStack>> items = new LinkedHashMap<>();

    static{
        back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();

        backMeta.setDisplayName(ChatColor.GREEN+Message.GUI_BACK_BUTTON.toString());
        backMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        back.setItemMeta(backMeta);

    }


    public InventorySelectorAdvanced(){
        super();
    }

    public InventorySelectorAdvanced(Player player, LinkedHashMap<String, Setting> settings) {
        if (PluginMain.versionCompatible(13)) {
            glassFiller = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = glassFiller.getItemMeta();
            meta.setDisplayName(" ");
            glassFiller.setItemMeta(meta);
        }
        this.player = player;
        this.settings = new LinkedHashMap<>();
        this.settings.putAll(settings);
        objects.put(player, this);
        selector = Bukkit.createInventory(null, PluginMain.dropChances.size() + (9 - PluginMain.dropChances.size() % 9) + 2 * 9, getTitle());

        this.cobble = new ItemStack(Material.COBBLESTONE);

        this.refreshCobbleObject();
        reloadInventory();
        if (PluginMain.versionCompatible(12)) player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, (float)PluginMain.volume, 0);
        player.openInventory(selector);
    }

    @Override
    protected void refreshSettings(){
        items.clear();
        super.refreshSettings();
    }

    @Override
    protected void reloadInventory() {
        refreshSettings();
        selector.clear();
        AtomicInteger index = new AtomicInteger(9);

        if (player.hasPermission("stonedrop.toggle-cobble")) selector.setItem(index.getAndIncrement(), cobble);
        items.forEach((itemStack, itemStacks) -> selector.setItem(index.getAndIncrement(), itemStack));
        selector.setItem(selector.getSize()-5, exit);
        fillWithGlass(selector);
    }

    @Override
    protected ArrayList<String> setCobbleLore() {
        ArrayList<String> lore = super.setCobbleLore();
        lore.add(ChatColor.DARK_GRAY + "--------------------");
        lore.add(ChatColor.AQUA + Message.GUI_ITEM_DESCRIPTION_RIGHT_CLICK_TO_TOGGLE.toString());
        lore.add(ChatColor.DARK_GRAY + "--------------------");
        return lore;
    }



    protected ArrayList<ItemStack> getItemDetailedData(ItemStack item, DropChance dropChance) {
        ArrayList<ItemStack> items = new ArrayList<>();
        ItemStack f0, f1, f2, f3;

        f0 = item.clone();
        f1 = item.clone();
        f2 = item.clone();
        f3 = item.clone();

        items.add(f0);
        items.add(f1);
        items.add(f2);
        items.add(f3);

        if (dropChance != null && dropChance.getEnchant() != null) {
            f0.addEnchantments(dropChance.getEnchant());
            f1.addEnchantments(dropChance.getEnchant());
            f2.addEnchantments(dropChance.getEnchant());
            f3.addEnchantments(dropChance.getEnchant());
        }

        ItemMeta f0Meta = f0.getItemMeta();
        ItemMeta f1Meta = f2.getItemMeta();
        ItemMeta f2Meta = f2.getItemMeta();
        ItemMeta f3Meta = f3.getItemMeta();

        f0Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_0.toString());
        f1Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_1.toString());
        f2Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_2.toString());
        f3Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_3.toString());

        f0Meta.setLore(generateItemLore(dropChance, 0));
        f1Meta.setLore(generateItemLore(dropChance, 1));
        f2Meta.setLore(generateItemLore(dropChance, 2));
        f3Meta.setLore(generateItemLore(dropChance, 3));

        f0Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        f1Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        f2Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        f3Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        f0.setItemMeta(f0Meta);
        f1.setItemMeta(f1Meta);
        f2.setItemMeta(f2Meta);
        f3.setItemMeta(f3Meta);

        return items;

    }

    protected ArrayList<String> generateItemLore(DropChance dropData, int level) {
        ArrayList<String> lore = new ArrayList<>();
        double chance = 0;
        int min = 0, max = 0;
        assert level == 0 || level == 1 || level == 2 || level == 3;
        DecimalFormat format = new DecimalFormat("##0.0##");


        switch (level) {
            case 0:
                chance = dropData.getFortuneChance(0);
                min = (int)dropData.getFortuneItemsAmountMin(0);
                max = (int)dropData.getFortuneItemsAmountMax(0);
                break;
            case 1:
                chance = dropData.getFortuneChance(1);
                min = (int)dropData.getFortuneItemsAmountMin(1);
                max = (int)dropData.getFortuneItemsAmountMax(1);
                break;
            case 2:
                chance = dropData.getFortuneChance(2);
                min = (int)dropData.getFortuneItemsAmountMin(2);
                max = (int)dropData.getFortuneItemsAmountMax(2);
                break;
            case 3:
                chance = dropData.getFortuneChance(3);
                min = (int)dropData.getFortuneItemsAmountMin(3);
                max = (int)dropData.getFortuneItemsAmountMax(3);
                break;
        }
        chance *= 100;
        lore.add(ChatColor.GRAY + Message.INFO_DROP_CHANCE.toString() + " " + ChatColor.GOLD + format.format(chance)+"%");
        lore.add(ChatColor.GRAY + Message.INFO_DROP_AMOUNT.toString() + " " + ChatColor.GOLD + min + "-" + max);

        return lore;
    }

    @Override
    protected void putItemToItems(ItemStack item, Material material, DropChance dropData){

        ArrayList<ItemStack> dropChances = getItemDetailedData(item, dropData);
        items.put(item, dropChances);
    }



    @Override
    protected ArrayList<String> setDropItemLore(DropChance dropData, Setting setting) {
        ArrayList<String> lore = super.setDropItemLore(dropData, setting);
        lore.add(ChatColor.DARK_GRAY + "--------------------");
        lore.add(ChatColor.AQUA + Message.GUI_ITEM_DESCRIPTION_RIGHT_CLICK_TO_TOGGLE.toString());
        lore.add(ChatColor.AQUA + Message.GUI_ITEM_DESCRIPTION_LEFT_CLICK_TO_SEE_DETAILS.toString());
        lore.add(ChatColor.DARK_GRAY + "--------------------");
        return lore;
    }

    protected void openSecondaryWindow(ArrayList<ItemStack> items){
        willBeUsed = false;
        if (PluginMain.versionCompatible(14)) player.playSound(player.getLocation(), Sound.UI_LOOM_TAKE_RESULT, (float)PluginMain.volume, 1);
        secondaryWindow = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA+Message.GUI_SECOND_TITLE.toString());
        AtomicInteger i = new AtomicInteger(10);
        items.forEach(item -> secondaryWindow.setItem(i.getAndAdd(2), item));
        secondaryWindow.setItem(secondaryWindow.getSize()-5, exit);
        secondaryWindow.setItem(secondaryWindow.getSize()-6, back);
        fillWithGlass(secondaryWindow);
        player.openInventory(secondaryWindow);
    }

    @Override
    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;
        if (objects.containsKey(event.getWhoClicked()) && (event.getClickedInventory().equals(objects.get(event.getWhoClicked()).secondaryWindow) || event.getClickedInventory().equals(objects.get(event.getWhoClicked()).selector) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) return;
            InventorySelectorAdvanced inventorySelector = objects.get(event.getWhoClicked());
            Player player = inventorySelector.player;
            ItemStack clickedItem = event.getCurrentItem();
            if (event.isRightClick() && !event.getWhoClicked().getOpenInventory().getTopInventory().equals(secondaryWindow)) {
                if (event.getCurrentItem().equals(inventorySelector.cobble)) inventorySelector.settings.get("COBBLE").toggle();
                else if (event.getClickedInventory().equals(inventorySelector.selector)){
                    if (PluginMain.versionCompatible(12) ){
                        if (inventorySelector.settings.containsKey(clickedItem.getType().toString())) {
                            inventorySelector.settings.get(clickedItem.getType().toString()).toggle();
                            if (PluginMain.versionCompatible(14)) player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, (float)PluginMain.volume, 1);
                        }
                    }
                    else{
                        ItemStack dye = new Dye(DyeColor.BLUE).toItemStack();
                        if (clickedItem.getType().equals(dye.getType())){
                            Dye dyeColor = (Dye) clickedItem.getData();
                            if (dyeColor != null && dyeColor.getColor() != null && dyeColor.getColor().equals(DyeColor.BLUE)){
                                inventorySelector.settings.get("LAPIS_LAZULI").toggle();
                            }
                        }
                        else {
                            if (inventorySelector.settings.containsKey(clickedItem.getType().toString())) inventorySelector.settings.get(clickedItem.getType().toString()).toggle();
                        }
                    }

                }
                inventorySelector.reloadInventory();
            } else if (event.isLeftClick()) {
                if (event.getCurrentItem() != null && inventorySelector.items.containsKey(clickedItem)){
                    inventorySelector.willBeUsed = true;
                    player.closeInventory();
                    inventorySelector.openSecondaryWindow(inventorySelector.items.get(clickedItem));
                }
            }
        }
        if (objects.containsKey(event.getWhoClicked()) && (event.getClickedInventory().equals(objects.get(event.getWhoClicked()).secondaryWindow) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) return;
            if (PluginMain.versionCompatible(12)) ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_CHICKEN_EGG, (float)PluginMain.volume, 1);
            checkForFuncButtonsPressed(event);
        }
    }


    @Override
    protected boolean checkForFuncButtonsPressed(InventoryClickEvent event){

        if (event.getCurrentItem() != null){
            if (event.getCurrentItem().equals(exit)) {
                objects.get(event.getWhoClicked()).willBeUsed = false;
                event.getWhoClicked().closeInventory();
                return true;
            }
            else if (event.getCurrentItem().equals(back)) {
                objects.get(event.getWhoClicked()).willBeUsed = true;
                event.getWhoClicked().closeInventory();
                objects.get(event.getWhoClicked()).player.openInventory(objects.get(event.getWhoClicked()).selector);
                objects.get(event.getWhoClicked()).willBeUsed = false;
                objects.get(event.getWhoClicked()).reloadInventory();
                return true;
            }

        }
        return false;
    }

    @Override
    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        if (objects.containsKey(event.getPlayer())) {
            if (PluginMain.versionCompatible(14)) ((Player) event.getPlayer()).playSound(event.getPlayer().getLocation(), Sound.UI_LOOM_TAKE_RESULT, (float)PluginMain.volume, 1);
            if (event.getInventory().equals(objects.get(event.getPlayer()).selector)){
                if (!objects.get(event.getPlayer()).willBeUsed) objects.remove(event.getPlayer());
            }
            else if (event.getInventory().equals(objects.get(event.getPlayer()).secondaryWindow)){
                if (!objects.get(event.getPlayer()).willBeUsed) objects.remove(event.getPlayer());
            }
        }
    }
}

