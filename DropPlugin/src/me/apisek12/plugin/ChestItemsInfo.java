package me.apisek12.plugin;


import org.bukkit.enchantments.Enchantment;

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

    public ChestItemsInfo(double chance, int min, int max, HashMap<Enchantment, Integer> enchantment) {
        this.chance = chance;
        this.min = min;
        this.max = max;
        this.enchantment = enchantment;
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
