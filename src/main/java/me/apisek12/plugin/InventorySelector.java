package me.apisek12.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;

public class InventorySelector {
    private Player player;
    private HashMap<String, Setting> settings;
    private String title;
    private Inventory selector;
    ArrayList<Material> items = new ArrayList<>();

    public InventorySelector(Player player, HashMap<String, Setting> settings, String title) {
        this.player = player;
        this.settings = settings;
        this.title = title;
        run();
    }

    private void run(){
        selector = Bukkit.createInventory(null, settings.size()+(9-settings.size()%9)+2*9, title);
        settings.forEach((materialName, settings)->{

        });
    }

    public void InventoryClickEvent(InventoryClickEvent event){
        if (event.getInventory().equals(selector)){

        }
    }
}
