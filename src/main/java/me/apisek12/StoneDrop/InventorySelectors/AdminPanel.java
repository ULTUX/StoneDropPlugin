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
        return ChatColor.BOLD.toString() + ChatColor.RED + ChatColor.translateAlternateColorCodes('&', Message.GUI_ADMIN_TITLE.toString());
    }

    public AdminPanel() {
    }

    public static AdminPanel createAdminPanel(Player player) {
        LinkedHashMap<String, Setting> settingss = new LinkedHashMap<>();
        PluginMain.playerSettings.get("9999-9999").forEach((s, setting) -> {
            if (PluginMain.dropChances.containsKey(s) || s.equalsIgnoreCase("COBBLE")) settingss.put(s, setting);

        });
        return new AdminPanel(player, settingss);
    }

    public AdminPanel(Player player, LinkedHashMap<String, Setting> settings) {
        super(player, settings);
    }


    @Override
    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;
        Player player1 = (Player) event.getWhoClicked();
        if (objects.containsKey(player1) && (event.getClickedInventory().equals(objects.get(player1).selector) || event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) {
            event.setCancelled(true);
            if (checkForFuncButtonsPressed(event)) return;
            if (!(objects.get(player1) instanceof AdminPanel)) return;
            AdminPanel panel = (AdminPanel) objects.get(player1);
            Player player = panel.player;
            ItemStack clickedItem = event.getCurrentItem();
            if (event.getClickedInventory().equals(panel.selector)) {
                if (panel.settings.containsKey(event.getCurrentItem().getType().toString())) {
                    if (event.isRightClick()) {
                        Setting setting = panel.settings.get(clickedItem.getType().toString());
                        setting.toggle();
                        if (PluginMain.dropChances.containsKey(setting.getName()))
                            PluginMain.dropChances.get(setting.getName()).setEnabled(setting.isOn());
                        panel.reloadInventory();
                        if (PluginMain.versionCompatible(14))
                            player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, (float) PluginMain.volume, 1);
                    }
                    else if (event.isLeftClick()) {
                        if (event.isLeftClick()) if (PluginMain.versionCompatible(12))
                            ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_CHICKEN_EGG, (float) PluginMain.volume, 1);

                    }
                }

            }
        }
    }
}
