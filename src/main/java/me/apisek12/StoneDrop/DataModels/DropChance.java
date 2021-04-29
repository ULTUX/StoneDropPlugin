package me.apisek12.StoneDrop.DataModels;


import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DropChance {
    private String name;
    private final int fortunesAmount = 4;
    private double st;
    private final double[] fortuneChances = new double[fortunesAmount];
    private final double[] fortuneMins = new double[fortunesAmount];
    private final double[] fortuneMaxs = new double[fortunesAmount];
    private int min_st, max_st;
    private int minLevel = 0, maxLevel = 256;
    private String customName;
    private HashMap<Enchantment, Integer> enchant = new HashMap<>();
    public HashMap<Enchantment, Integer> getEnchant() {
        return enchant;
    }


    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }


    public DropChance() {
        Arrays.fill(this.fortuneChances, 0);
        Arrays.fill(this.fortuneMaxs, 0);
        Arrays.fill(this.fortuneMins, 0);
    }

    public void setEnchant(HashMap<String, Integer> enchant) {
        enchant.forEach((enchantName, level) ->{
            enchantName = enchantName.toLowerCase();
            if (PluginMain.plugin.versionCompatible(12)) {
                try {
                    this.enchant.put((Enchantment) Enchantment.class.getMethod("getByKey", NamespacedKey.class).invoke(Enchantment.class, NamespacedKey.minecraft(enchantName)), level);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    this.enchant.put((Enchantment) Enchantment.class.getMethod("getByName" , String.class).invoke(Enchantment.class, enchantName), level);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void setSilkCahnce(int level, double val){
        if (level >= 1) this.st = val;
        else this.st = 0;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        DecimalFormat format = new DecimalFormat("##0.0##");
        String toReturn = ChatColor.GOLD+name;
        for(int i=0; i<fortunesAmount;i++){
            toReturn+=": \n   "+ChatColor.GREEN+"fortune " + i+ " : "
                    +ChatColor.GRAY+" chance: "+format.format(fortuneChances[i]*100)
                    +"%, drop amount: " +fortuneMins[i]+"-"+fortuneMaxs[i];
        }
        toReturn +="\n   "+ChatColor.GREEN+"silk touch: "+ChatColor.GRAY+"chance: "+format.format(st*100)+"%, drop amount: "+min_st+"-"+max_st;
        return toReturn;
    }


    public double getST() {
        return st;
    }


    public int getMinST() {
        return min_st;
    }

    public int getMaxST() {
        return max_st;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setSilkMinDrop(int level, int val){
        if (level >= 1) this.min_st = val;
        else this.min_st = 0;

    }
    public void setSilkMaxDrop(int level, int val){
        if (level >= 1) this.max_st = val;
        else this.max_st = 0;
    }

    private double getValueFromFortuneArray(double [] fortuneArray,  int level){
        if(level<0) return fortuneArray[0];
        else if(level>=fortuneArray.length) return fortuneArray[fortuneArray.length-1];
        else return fortuneArray[level];
    }

    public double getFortuneChance(int level){
        return getValueFromFortuneArray(this.fortuneChances,level);
    }

    public double getFortuneItemsAmountMin(int level){
        return getValueFromFortuneArray(this.fortuneMins,level);
    }

    public double getFortuneItemsAmountMax(int level){
        return getValueFromFortuneArray(this.fortuneMaxs,level);
    }


    private void setInFortuneArray(double [] fortuneArray, int level, double amount){
        if(level<0) fortuneArray[0]=amount;
        else if (level>=fortuneArray.length) fortuneArray[fortuneArray.length-1]=amount;
        else fortuneArray[level]=amount;
    }

    public void setFortuneChance(int level, double chance){
        this.setInFortuneArray(this.fortuneChances,level,chance);
    }

    public void setFortuneItemsAmountMin(int level, double minAmount){
        this.setInFortuneArray(this.fortuneMins,level,minAmount);
    }

    public void setFortuneItemsAmountMax(int level, double maxAmount){
        this.setInFortuneArray(this.fortuneMaxs,level,maxAmount);
    }


}
