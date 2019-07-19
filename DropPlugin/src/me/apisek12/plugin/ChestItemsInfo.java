package me.apisek12.plugin;


public class ChestItemsInfo {
    private double chance;
    private int min;
    private int max;

    public ChestItemsInfo(double chance, int min, int max) {
        this.chance = chance;
        this.min = min;
        this.max = max;
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
