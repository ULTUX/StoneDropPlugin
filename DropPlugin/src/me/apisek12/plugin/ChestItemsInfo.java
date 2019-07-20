package me.apisek12.plugin;


import org.bukkit.enchantments.Enchantment;

public class ChestItemsInfo {
    private double chance;
    private int min;
    private int max;
    private Enchantment enchantment = null;
    private int level;

    public int getLevel() {
        return level;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }


    public ChestItemsInfo(double chance, int min, int max) {
        this.chance = chance;
        this.min = min;
        this.max = max;
    }

    public ChestItemsInfo(double chance, int min, int max, Enchantment enchantment, int level) {
        this.chance = chance;
        this.min = min;
        this.max = max;
        this.enchantment = enchantment;
        this.level = level;
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
}
