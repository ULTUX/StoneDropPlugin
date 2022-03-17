package me.apisek12.StoneDrop.InventorySelectors;

import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.PluginMain;
import me.apisek12.StoneDrop.Utils.ItemUtils;
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
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class InventorySelector implements Listener {
    protected Player player;
    protected LinkedHashMap<String, Setting> settings;
    protected Inventory selector;
    protected static HashMap<Player, InventorySelector> objects = new HashMap<>();
    protected boolean willBeUsed = false;
    protected static ItemStack exit, back;
    protected ItemStack cobble = null;
    protected ArrayList<ItemStack> items = new ArrayList<>();
    protected static ItemStack glassFiller = null;

    public InventorySelector() {
        exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();

        exitMeta.setDisplayName(ChatColor.RED + Message.GUI_EXIT_BUTTON.toString());
        exitMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        exit.setItemMeta(exitMeta);

        back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();

        backMeta.setDisplayName(ChatColor.GREEN + Message.GUI_BACK_BUTTON.toString());
        backMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        back.setItemMeta(backMeta);
    }

    public InventorySelector(Player player, LinkedHashMap<String, Setting> settings) {
        this();
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

        if (!(this instanceof AdminPanel)) this.cobble = new ItemStack(Material.COBBLESTONE);

        if (cobble != null) this.refreshCobbleObject();
        reloadInventory();
        if (PluginMain.versionCompatible(12))
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, (float) PluginMain.volume, 0);
        player.openInventory(selector);
    }

    protected String getTitle() {
        return ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + ChatColor.translateAlternateColorCodes('&', Message.GUI_TITLE.toString());
    }


    protected void setLoreLine(ArrayList<String> lore, double chance, double minAmount, double maxAmount, String enchant) {
        int min = (int) minAmount;
        int max = (int) maxAmount;
        chance *= 100;
        NumberFormat format = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.ENGLISH));
        lore.add(ChatColor.BLUE + ChatColor.BOLD.toString() + enchant + ":");
        lore.add(ChatColor.GRAY + ChatColor.BOLD.toString() + "    \u00BB " + ChatColor.RESET + ChatColor.GRAY + Message.CHANCE + ": " + format.format(chance) + "%");
        if (min == max) {
            lore.add(ChatColor.GRAY + ChatColor.BOLD.toString() + "    \u00BB " + ChatColor.RESET + ChatColor.GRAY + Message.AMOUNT + ": " + min);
            return;
        }
        lore.add(ChatColor.GRAY + ChatColor.BOLD.toString() + "    \u00BB " + ChatColor.RESET + ChatColor.GRAY + Message.AMOUNT + ": " + min + "-" + max);
    }

    protected ArrayList<String> setDropItemLore(DropChance dropData, Setting setting) {
        ArrayList<String> lore = new ArrayList<>();
        String onOff;
        if (setting.isOn()) onOff = ChatColor.GREEN + Message.INFO_ENABLED.toString();
        else onOff = ChatColor.RED + Message.INFO_DISABLED.toString();
        lore.add(ChatColor.GRAY + Message.GUI_ITEM_LEVEL_IN_RANGE.toString() + ": " + ChatColor.GOLD + dropData.getMinLevel() + "-" + dropData.getMaxLevel());
        lore.add(ChatColor.GRAY + Message.GUI_ITEM_DESCRIPTION_THIS_ITEM_DROP_IS.toString() + " " + onOff + ".");
        lore.add("");

        if (dropData.getFortuneChance(0) > 0) {
            setLoreLine(lore, dropData.getFortuneChance(0),
                    dropData.getFortuneItemsAmountMin(0),
                    dropData.getFortuneItemsAmountMax(0),
                    Message.INFO_FORTUNE_0.toString());
        }
        if (dropData.getFortuneChance(1) > 0) {
            setLoreLine(lore, dropData.getFortuneChance(1),
                    dropData.getFortuneItemsAmountMin(1),
                    dropData.getFortuneItemsAmountMax(1),
                    Message.INFO_FORTUNE_1.toString());

        }
        if (dropData.getFortuneChance(2) > 0) {
            setLoreLine(lore, dropData.getFortuneChance(2),
                    dropData.getFortuneItemsAmountMin(2),
                    dropData.getFortuneItemsAmountMax(2),
                    Message.INFO_FORTUNE_2.toString());

        }
        if (dropData.getFortuneChance(3) > 0) {
            setLoreLine(lore, dropData.getFortuneChance(3),
                    dropData.getFortuneItemsAmountMin(3),
                    dropData.getFortuneItemsAmountMax(3),
                    Message.INFO_FORTUNE_3.toString());

        }
        if (dropData.getST() > 0) {
            setLoreLine(lore, dropData.getST(),
                    dropData.getMinST(),
                    dropData.getMaxST(),
                    Message.INFO_SILK_TOUCH.toString());

        }

        lore.add("");
        if (dropData.getAcceptedBiomes() != null && dropData.getAcceptedBiomes().length > 0) {
            lore.add(ChatColor.BLUE + ChatColor.BOLD.toString() + Message.GUI_ALLOWED_BIOMES + ":");
            StringBuilder loreSB = new StringBuilder();
            loreSB.setLength(0);
            int length = dropData.getAcceptedBiomes().length;
            for (int b = 0; b < length; b++) {
                String biomeName =  dropData.getAcceptedBiomes()[b].name();
                String formattedName = biomeName.substring(1).toLowerCase(Locale.ROOT).replace('_', ' ');
                int currenLen = loreSB.length();
                if (currenLen + dropData.getAcceptedBiomes()[b].name().length() > 30) {

                    lore.add(loreSB.toString());
                    loreSB.setLength(0);
                }
                loreSB.append(biomeName.charAt(0)).append(formattedName);
                if (b < length - 1) loreSB.append(", ");
            }
            if (loreSB.length() > 0) {
                lore.add(loreSB.toString());
                loreSB.setLength(0);
            }
        }

        if (!dropData.isEnabled()) {
            lore.add("");
            lore.add(ChatColor.RED + ChatColor.BOLD.toString() + Message.ITEM_DROP_DISABLED_BY_ADMIN);
        }

        return lore;
    }

    protected void refreshSettings() {
        this.refreshCobbleObject();
        items.clear();
        settings.forEach((materialName, setting) -> {
            try {
                Material material;
                ItemStack item;
                if ((item = ItemUtils.getItemStack(materialName, 1)) != null) {
                    DropChance dropData = PluginMain.dropChances.get(materialName);
                    if (dropData != null) {
                        //ItemStack item = new ItemStack(material);
                        ItemUtils.applyCustomName(dropData, item);
                        ItemMeta itemMeta = item.getItemMeta();
                        if (dropData.getEnchant() != null)
                            dropData.getEnchant().forEach((enchantment, integer) -> itemMeta.addEnchant(enchantment, integer, false));

                        ArrayList<String> lore = this.setDropItemLore(dropData, setting);


                        itemMeta.setLore(lore);
                        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        item.setItemMeta(itemMeta);

                        this.putItemToItems(item, item.getType(), dropData);


                    }
                }
            } catch (NullPointerException e) {
                PluginMain.plugin.getLogger().log(Level.WARNING, "Material: " + materialName + " does not exist in this minecraft version, something is probably not properly set in config.yml file.");

            }

        });
    }


    protected void putItemToItems(ItemStack item, Material material, DropChance dropData) {
        items.add(item);
    }


    protected ArrayList<String> setCobbleLore() {
        ArrayList<String> lore = new ArrayList<>();
        String onOff;
        if (!settings.get("COBBLE").isOn()) onOff = ChatColor.GREEN + Message.INFO_ENABLED.toString();
        else onOff = ChatColor.RED + Message.INFO_DISABLED.toString();
        lore.add("");
        lore.add(ChatColor.GRAY + Message.GUI_ITEM_DESCRIPTION_THIS_ITEM_DROP_IS.toString() + " " + onOff + ".");
        return lore;
    }

    protected void refreshCobbleObject() {
        if (cobble == null) return;
        ItemMeta cobbleMeta = cobble.getItemMeta();
        ArrayList<String> lore = setCobbleLore();
        cobbleMeta.setLore(lore);
        cobbleMeta.setDisplayName(ChatColor.RESET.toString() + ChatColor.AQUA + Message.COBBLE_TOGGLE_BUTTON_NAME);
        cobbleMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        cobble.setItemMeta(cobbleMeta);
    }


    protected void reloadInventory() {
        refreshSettings();
        selector.clear();
        AtomicInteger index = new AtomicInteger(9);

        if (cobble != null) selector.setItem(index.getAndIncrement(), cobble);
        items.forEach((itemStack) -> selector.setItem(index.getAndIncrement(), itemStack));
        selector.setItem(selector.getSize() - 5, exit);
        fillWithGlass(selector);
    }

    protected void fillWithGlass(Inventory selector) {
        if (glassFiller != null) {
            ItemStack[] items = selector.getContents();

            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) items[i] = glassFiller;
            }
            selector.setContents(items);
        }
    }


    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (objects.containsKey(player)) {
            if (PluginMain.versionCompatible(14))
                ((Player) event.getPlayer()).playSound(event.getPlayer().getLocation(), Sound.UI_LOOM_TAKE_RESULT, (float) PluginMain.volume, 1);
            if (event.getInventory().equals(objects.get(player).selector)) {
                if (!objects.get(player).willBeUsed) objects.remove(player);
            }
        }
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        if (objects.containsKey(player) && (event.getClickedInventory().equals(objects.get(player).selector) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) {
            }

        }
    }

    protected boolean checkForFuncButtonsPressed(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            if (event.getCurrentItem().equals(exit)) {
                Player player = (Player) event.getWhoClicked();
                objects.get(player).willBeUsed = false;
                event.getWhoClicked().closeInventory();
                return true;
            }

        }
        return false;
    }

}
