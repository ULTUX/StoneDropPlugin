package me.apisek12.StoneDrop;

import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.EventListeners.BlockBreakEventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InventorySelector implements Listener {
    private Player player;
    private LinkedHashMap<String, Setting> settings;
    private static String title = ChatColor.DARK_AQUA + Message.GUI_TITLE.toString();
    private Inventory selector;
    private LinkedHashMap<ItemStack, ArrayList<ItemStack>> items = new LinkedHashMap<>();
    private static HashMap<Player, InventorySelector> objects = new HashMap<>();
    private boolean willBeUsed = false;
    private Inventory secondaryWindow;
    private static ItemStack exit, back;
    private ItemStack cobble;

    static {
        exit = new ItemStack(Material.BARRIER);
        back = new ItemStack(Material.ARROW);
        ItemMeta exitMeta = exit.getItemMeta();
        ItemMeta backMeta = back.getItemMeta();

        exitMeta.setDisplayName(ChatColor.RED+Message.GUI_EXIT_BUTTON.toString());
        backMeta.setDisplayName(ChatColor.GREEN+Message.GUI_BACK_BUTTON.toString());
        exitMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        backMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        exit.setItemMeta(exitMeta);
        back.setItemMeta(backMeta);

    }

    public InventorySelector() {}

    public InventorySelector(Player player, LinkedHashMap<String, Setting> settings) {
        this.player = player;
        this.settings = new LinkedHashMap<>();
        this.settings.putAll(settings);
        objects.put(player, this);
        selector = Bukkit.createInventory(null, PluginMain.dropChances.size() + (9 - PluginMain.dropChances.size() % 9) + 2 * 9, title);

        this.cobble = new ItemStack(Material.COBBLESTONE);

        refreshCobbleObject();
        reloadInventory();
        if (PluginMain.plugin.versionCompatible(12)) player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, (float)PluginMain.volume, 0);
        player.openInventory(selector);
    }

    private void refreshSettings(){
        refreshCobbleObject();
        items.clear();
        settings.forEach((materialName, setting) -> {
            Material material;
            if ((material = Material.getMaterial(materialName)) != null) {
                DropChance dropData = PluginMain.dropChances.get(materialName);
                if (dropData != null) {
                    ItemStack item = new ItemStack(material);
                    BlockBreakEventListener.applyCustomName(dropData, item);
                    ItemMeta itemMeta = item.getItemMeta();
                    if (dropData != null && dropData.getEnchant() != null)
                        dropData.getEnchant().forEach((enchantment, integer) -> itemMeta.addEnchant(enchantment, integer, false));
                    ArrayList<String> lore = new ArrayList<>();
                    String onOff;
                    if (setting.isOn()) onOff = ChatColor.GREEN + Message.INFO_ENABLED.toString();
                    else onOff = ChatColor.RED + Message.INFO_DISABLED.toString();
                    lore.add(ChatColor.GRAY+ Message.GUI_ITEM_LEVEL_IN_RANGE.toString()+": "+ChatColor.GOLD+dropData.getMinLevel()+"-"+dropData.getMaxLevel());
                    lore.add(ChatColor.GRAY + Message.GUI_ITEM_DESCRIPTION_THIS_ITEM_DROP_IS.toString()+ " " + onOff + ".");
                    lore.add("");
                    lore.add(ChatColor.DARK_GRAY + "--------------------");
                    lore.add(ChatColor.AQUA + Message.GUI_ITEM_DESCRIPTION_RIGHT_CLICK_TO_TOGGLE.toString());
                    lore.add(ChatColor.AQUA + Message.GUI_ITEM_DESCRIPTION_LEFT_CLICK_TO_SEE_DETAILS.toString());
                    lore.add(ChatColor.DARK_GRAY + "--------------------");

                    itemMeta.setLore(lore);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(itemMeta);
                    ArrayList<ItemStack> dropChances = getItemDetailedData(dropData, material);

                    items.put(item, dropChances);
                }
            }
        });
    }
    private void refreshCobbleObject(){
        ItemMeta cobbleMeta = cobble.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        String onOff;
        if (!settings.get("COBBLE").isOn()) onOff = ChatColor.GREEN + Message.INFO_ENABLED.toString();
        else onOff = ChatColor.RED + Message.INFO_DISABLED.toString();
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "--------------------");
        lore.add(ChatColor.GRAY + Message.GUI_ITEM_DESCRIPTION_THIS_ITEM_DROP_IS.toString()+ " " + onOff + ".");
        lore.add(ChatColor.AQUA + Message.GUI_ITEM_DESCRIPTION_RIGHT_CLICK_TO_TOGGLE.toString());
        lore.add(ChatColor.DARK_GRAY + "--------------------");
        cobbleMeta.setLore(lore);
        cobbleMeta.setDisplayName(ChatColor.RESET.toString()+ChatColor.AQUA.toString()+Message.COBBLE_TOGGLE_BUTTON_NAME);
        cobbleMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        cobble.setItemMeta(cobbleMeta);
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

        if (dropData != null && dropData.getEnchant() != null) {
            f0.addEnchantments(dropData.getEnchant());
            f1.addEnchantments(dropData.getEnchant());
            f2.addEnchantments(dropData.getEnchant());
            f3.addEnchantments(dropData.getEnchant());
        }

        ItemMeta f0Meta = f0.getItemMeta();
        ItemMeta f1Meta = f2.getItemMeta();
        ItemMeta f2Meta = f2.getItemMeta();
        ItemMeta f3Meta = f3.getItemMeta();

        f0Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_0.toString());
        f1Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_1.toString());
        f2Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_2.toString());
        f3Meta.setDisplayName(ChatColor.GREEN + Message.INFO_FORTUNE_3.toString());

        f0Meta.setLore(generateItemLore(dropData, 0));
        f1Meta.setLore(generateItemLore(dropData, 1));
        f2Meta.setLore(generateItemLore(dropData, 2));
        f3Meta.setLore(generateItemLore(dropData, 3));

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
        lore.add(ChatColor.GRAY + Message.INFO_DROP_CHANCE.toString() + " " + ChatColor.GOLD + format.format(chance)+"%");
        lore.add(ChatColor.GRAY + Message.INFO_DROP_AMOUNT.toString() + " " + ChatColor.GOLD + min + "-" + max);

        return lore;
    }

    private void reloadInventory() {
        refreshSettings();
        selector.clear();
        AtomicInteger index = new AtomicInteger(9);

        selector.setItem(index.getAndIncrement(), cobble);
        items.forEach((itemStack, itemStacks) -> selector.setItem(index.getAndIncrement(), itemStack));
        selector.setItem(selector.getSize()-5, exit);
    }
    private void openSecondaryWindow(ArrayList<ItemStack> items){
        willBeUsed = false;
        if (PluginMain.plugin.versionCompatible(14)) player.playSound(player.getLocation(), Sound.UI_LOOM_TAKE_RESULT, (float)PluginMain.volume, 1);
        secondaryWindow = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA+Message.GUI_SECOND_TITLE.toString());
        AtomicInteger i = new AtomicInteger(10);
        items.forEach(item -> secondaryWindow.setItem(i.getAndAdd(2), item));
        secondaryWindow.setItem(secondaryWindow.getSize()-5, exit);
        secondaryWindow.setItem(secondaryWindow.getSize()-6, back);
        player.openInventory(secondaryWindow);
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;
        if (objects.containsKey(event.getWhoClicked()) && (event.getClickedInventory().equals(objects.get(event.getWhoClicked()).secondaryWindow) || event.getClickedInventory().equals(objects.get(event.getWhoClicked()).selector) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) return;
            InventorySelector inventorySelector = objects.get(event.getWhoClicked());
            Player player = inventorySelector.player;
            ItemStack clickedItem = event.getCurrentItem();
            if (event.isRightClick() && !event.getWhoClicked().getOpenInventory().getTopInventory().equals(secondaryWindow)) {
                if (event.getCurrentItem().equals(inventorySelector.cobble)) inventorySelector.settings.get("COBBLE").toggle();
                else if (event.getClickedInventory().equals(inventorySelector.selector)){
                    inventorySelector.settings.get(clickedItem.getType().toString()).toggle();
                }
                inventorySelector.reloadInventory();
                if (PluginMain.plugin.versionCompatible(14)) player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, (float)PluginMain.volume, 1);
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
            if (PluginMain.plugin.versionCompatible(12)) ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_CHICKEN_EGG, (float)PluginMain.volume, 1);
            checkForFuncButtonsPressed(event);
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        if (objects.containsKey(event.getPlayer())) {
            if (event.getInventory().equals(objects.get(event.getPlayer()).selector)){
                if (PluginMain.plugin.versionCompatible(14)) ((Player) event.getPlayer()).playSound(event.getPlayer().getLocation(), Sound.UI_LOOM_TAKE_RESULT, (float)PluginMain.volume, 1);
                if (!objects.get(event.getPlayer()).willBeUsed) objects.remove(event.getPlayer());
            }
            else if (event.getInventory().equals(objects.get(event.getPlayer()).secondaryWindow)){
                if (PluginMain.plugin.versionCompatible(14)) ((Player) event.getPlayer()).playSound(event.getPlayer().getLocation(), Sound.UI_LOOM_TAKE_RESULT, (float)PluginMain.volume, 1);
                if (!objects.get(event.getPlayer()).willBeUsed) objects.remove(event.getPlayer());
            }
        }
    }

    private boolean checkForFuncButtonsPressed(InventoryClickEvent event){
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

}
