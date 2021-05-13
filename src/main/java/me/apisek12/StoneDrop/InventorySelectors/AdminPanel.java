package me.apisek12.StoneDrop.InventorySelectors;

import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.LinkedHashMap;

public class AdminPanel extends InventorySelector {

    @Override
    protected String getTitle() {
        return ChatColor.RED+ChatColor.BOLD.toString()+ChatColor.translateAlternateColorCodes('&', Message.GUI_ADMIN_TITLE.toString());
    }

    public AdminPanel() {}

    public static AdminPanel createAdminPanel(Player player) {
        LinkedHashMap<String, Setting> settingss = new LinkedHashMap<>();
        PluginMain.playerSettings.get("9999-9999").forEach((s, setting) -> {
            if (PluginMain.dropChances.containsKey(s) || s.equalsIgnoreCase("COBBLE")) settingss.put(s, setting);

        });
        return new AdminPanel(player, settingss);
    }

    public AdminPanel(Player player, LinkedHashMap<String, Setting> settings){
        super(player, settings);
    }




    @Override
    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;
        if (objects.containsKey(event.getWhoClicked()) && (event.getClickedInventory().equals(objects.get(event.getWhoClicked()).selector) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) return;
            if (!(objects.get(event.getWhoClicked()) instanceof AdminPanel)) return;
            AdminPanel panel = (AdminPanel) objects.get(event.getWhoClicked());
            Player player = panel.player;
            ItemStack clickedItem = event.getCurrentItem();
            if (event.isRightClick()) {
                if (event.getClickedInventory().equals(panel.selector)){
                    Setting setting = panel.settings.get(clickedItem.getType().toString());
                    setting.toggle();
                    if (PluginMain.dropChances.containsKey(setting.getName())) PluginMain.dropChances.get(setting.getName()).setEnabled(setting.isOn());
                }
                panel.reloadInventory();
                if (PluginMain.plugin.versionCompatible(14)) player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, (float)PluginMain.volume, 1);
            }
            }
        if (objects.containsKey(event.getWhoClicked()) && (event.getClickedInventory().equals(objects.get(event.getWhoClicked())) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) return;
            if (PluginMain.plugin.versionCompatible(12)) ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_CHICKEN_EGG, (float)PluginMain.volume, 1);
            checkForFuncButtonsPressed(event);
        }
    }
}
