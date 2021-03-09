package me.apisek12.StoneDrop.DataModels;


import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ChestItemsInfo {
    private double chance;
    private int min;
    private int max;
    private HashMap<Enchantment, Integer> enchantment = new HashMap<>();


    public ChestItemsInfo(double chance, int min, int max) {
        this.chance = chance;
        this.min = min;
        this.max = max;
    }

    public ChestItemsInfo(double chance, int min, int max, HashMap<String, Integer> enchantment) {
        this.chance = chance;
        this.min = min;
        this.max = max;
        enchantment.forEach((name, level) ->{
            if (PluginMain.plugin.versionCompatible(12)){
                try {
                    this.enchantment.put((Enchantment) Enchantment.class.getMethod("getByKey", NamespacedKey.class).invoke(Enchantment.class, NamespacedKey.minecraft(name)), level);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    this.enchantment.put((Enchantment) Enchantment.class.getMethod("getByName", String.class).invoke(Enchantment.class, name), level);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public double getChance() {
        return chance;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public HashMap<Enchantment, Integer> getEnchantment() {
        return enchantment;
    }
}
