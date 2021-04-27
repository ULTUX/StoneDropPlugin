package me.apisek12.StoneDrop.InventorySelectors;

import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.EventListeners.BlockBreakEventListener;
import me.apisek12.StoneDrop.PluginMain;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InventorySelector implements Listener {
    protected Player player;
    protected LinkedHashMap<String, Setting> settings;
    protected static String title = ChatColor.DARK_AQUA + Message.GUI_TITLE.toString();
    protected Inventory selector;
    protected static HashMap<Player, InventorySelector> objects = new HashMap<>();
    protected boolean willBeUsed = false;
    protected static ItemStack exit, back;
    protected ItemStack cobble;
    protected ArrayList<ItemStack> items = new ArrayList<>();
    static {
        exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();

        exitMeta.setDisplayName(ChatColor.RED+Message.GUI_EXIT_BUTTON.toString());
        exitMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        exit.setItemMeta(exitMeta);
    }

    public InventorySelector() {}

    public InventorySelector(Player player, LinkedHashMap<String, Setting> settings) {
        this.player = player;
        this.settings = new LinkedHashMap<>();
        this.settings.putAll(settings);
        objects.put(player, this);
        selector = Bukkit.createInventory(null, PluginMain.dropChances.size() + (9 - PluginMain.dropChances.size() % 9) + 2 * 9, title);
        
        this.cobble = new ItemStack(Material.COBBLESTONE);

        this.refreshCobbleObject();
        reloadInventory();
        if (PluginMain.plugin.versionCompatible(12)) player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, (float)PluginMain.volume, 0);
        player.openInventory(selector);
    }

    protected ArrayList<String> setDropItemLore(DropChance dropData, Setting setting){
        ArrayList<String> lore = new ArrayList<>();
        String onOff;
        if (setting.isOn()) onOff = ChatColor.GREEN + Message.INFO_ENABLED.toString();
        else onOff = ChatColor.RED + Message.INFO_DISABLED.toString();
        lore.add(ChatColor.GRAY+ Message.GUI_ITEM_LEVEL_IN_RANGE.toString()+": "+ChatColor.GOLD+dropData.getMinLevel()+"-"+dropData.getMaxLevel());
        lore.add(ChatColor.GRAY + Message.GUI_ITEM_DESCRIPTION_THIS_ITEM_DROP_IS.toString()+ " " + onOff + ".");
        lore.add("");
        for(int f_num=0;f_num<=3;f_num++){
            double chance = dropData.getFortuneChance(f_num);
            if(chance>0){
                String amountStr = String.valueOf((int)dropData.getFortuneItemsAmountMin(f_num))+ " - "+
                        String.valueOf((int)dropData.getFortuneItemsAmountMax(f_num));
                lore.add(ChatColor.GOLD+ "fortune "+ f_num+ ": "
                        + ChatColor.GRAY+ String.format("%1$5s",String.valueOf(chance*100))+
                         "%   "+ String.format("%1$8s",amountStr) );
            }
        }
        double silkChance = dropData.getST();
        if(silkChance>0){
            String amountStr = String.valueOf((int)dropData.getMinST())+
                    " - "+
                    String.valueOf((int)dropData.getMaxST());

            lore.add(ChatColor.GOLD+  "silk touch"+ ":  " + ChatColor.GRAY+
                    String.format("%1$4s",String.valueOf(silkChance*100))+
                    "%   "+ String.format("%1$8s",String.valueOf(amountStr))
            );
        }
        lore.add("");
        return lore;
    }

    protected void refreshSettings(){
        this.refreshCobbleObject();
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

                    ArrayList<String> lore = this.setDropItemLore(dropData,setting);


                    itemMeta.setLore(lore);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(itemMeta);

                    this.putItemToItems(item,material,dropData);


                }
            }
        });
    }


    protected void putItemToItems(ItemStack item, Material material, DropChance dropData){


        items.add(item);
    }


    protected ArrayList<String> setCobbleLore(){
        ArrayList<String> lore = new ArrayList<>();
        String onOff;
        if (!settings.get("COBBLE").isOn()) onOff = ChatColor.GREEN + Message.INFO_ENABLED.toString();
        else onOff = ChatColor.RED + Message.INFO_DISABLED.toString();
        lore.add("");
        lore.add(ChatColor.GRAY + Message.GUI_ITEM_DESCRIPTION_THIS_ITEM_DROP_IS.toString()+ " " + onOff + ".");
        return lore;
    }

    protected void refreshCobbleObject(){
        ItemMeta cobbleMeta = cobble.getItemMeta();
        ArrayList<String> lore = setCobbleLore();
        cobbleMeta.setLore(lore);
        cobbleMeta.setDisplayName(ChatColor.RESET.toString()+ChatColor.AQUA.toString()+Message.COBBLE_TOGGLE_BUTTON_NAME);
        cobbleMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        cobble.setItemMeta(cobbleMeta);
    }



    protected void reloadInventory() {
        refreshSettings();
        selector.clear();
        AtomicInteger index = new AtomicInteger(9);

        selector.setItem(index.getAndIncrement(), cobble);
        items.forEach((itemStack) -> selector.setItem(index.getAndIncrement(), itemStack));
        selector.setItem(selector.getSize()-5, exit);
    }


    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        if (objects.containsKey(event.getPlayer())) {
            if (PluginMain.plugin.versionCompatible(14)) ((Player) event.getPlayer()).playSound(event.getPlayer().getLocation(), Sound.UI_LOOM_TAKE_RESULT, (float)PluginMain.volume, 1);
            if (event.getInventory().equals(objects.get(event.getPlayer()).selector)){
                if (!objects.get(event.getPlayer()).willBeUsed) objects.remove(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;
        if (objects.containsKey(event.getWhoClicked()) && ( event.getClickedInventory().equals(objects.get(event.getWhoClicked()).selector) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) return;

        }
    }

    protected boolean checkForFuncButtonsPressed(InventoryClickEvent event){

        if (event.getCurrentItem() != null){
            if (event.getCurrentItem().equals(exit)) {
                objects.get(event.getWhoClicked()).willBeUsed = false;
                event.getWhoClicked().closeInventory();
                return true;
            }
            /*else if (event.getCurrentItem().equals(back)) {
                objects.get(event.getWhoClicked()).willBeUsed = true;
                event.getWhoClicked().closeInventory();
                objects.get(event.getWhoClicked()).player.openInventory(objects.get(event.getWhoClicked()).selector);
                objects.get(event.getWhoClicked()).willBeUsed = false;
                objects.get(event.getWhoClicked()).reloadInventory();
                return true;
            }*/

        }
        return false;
    }

}
