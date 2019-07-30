package me.apisek12.plugin;


import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;

public class DropChance {
    private String name;
    private double nof, f1, f2, f3;
    private int minnof, maxnof, minf1, maxf1, minf2, maxf2, minf3, maxf3;
    private HashMap<Enchantment, Integer> enchant = new HashMap<>();

    public HashMap<Enchantment, Integer> getEnchant() {
        return enchant;
    }

    public DropChance(String name, double nof, double f1, double f2, double f3, int minnof, int maxnof, int minf1, int maxf1, int minf2, int maxf2, int minf3, int maxf3, HashMap<Enchantment, Integer> enchant) {
        this.name = name;
        this.nof = nof;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.minnof = minnof;
        this.maxnof = maxnof;
        this.minf1 = minf1;
        this.maxf1 = maxf1;
        this.minf2 = minf2;
        this.maxf2 = maxf2;
        this.minf3 = minf3;
        this.maxf3 = maxf3;
        this.enchant = enchant;
    }
    public DropChance() {
    }

    public void setEnchant(HashMap<String, Integer> enchant) {
        enchant.forEach((enchantName, level) ->{
            this.enchant.put(Enchantment.getByKey(NamespacedKey.minecraft(enchantName)), level);
        });
    }

    public void setChance(int level, double val){
        if (level == 0) this.nof = val;
        else if (level == 1) this.f1 = val;
        else if (level == 2) this.f2 = val;
        else if (level == 3) this.f3 = val;

    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String toReturn = ChatColor.GOLD+name
                +": \n   "+ChatColor.DARK_RED+"no fortune: "+ChatColor.GREEN+" chance: "+nof*100+"%, drop amount: "+minnof+"-"+maxnof
                +"\n   "+ChatColor.DARK_RED+"fortune 1: "+ChatColor.GREEN+" chance: "+f1*100+"%, drop amount: "+minf1+"-"+maxf1
                +"\n   "+ChatColor.DARK_RED+"fortune 2: "+ChatColor.GREEN+"chance: "+f2*100+"%, drop amount: "+minf2+"-"+maxf2
                +"\n   "+ChatColor.DARK_RED+"fortune 3: "+ChatColor.GREEN+"chance: "+f3*100+"%, drop amount: "+minf3+"-"+maxf3;
        return toReturn;
    }

    public double getNof() {
        return nof;
    }

    public double getF1() {
        return f1;
    }

    public double getF2() {
        return f2;
    }

    public double getF3() {
        return f3;
    }

    public int getMinnof() {
        return minnof;
    }

    public int getMaxnof() {
        return maxnof;
    }

    public int getMinf1() {
        return minf1;
    }

    public int getMaxf1() {
        return maxf1;
    }

    public int getMinf2() {
        return minf2;
    }


    public int getMaxf2() {
        return maxf2;
    }

    public int getMinf3() {
        return minf3;
    }

    public int getMaxf3() {
        return maxf3;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinDrop(int level, int val){
        if (level == 0) this.minnof = val;
        else if (level == 1) this.minf1 = val;
        else if (level == 2) this.minf2 = val;
        else if (level == 3) this.minf3 = val;

    }
    public void setMaxDrop(int level, int val){
        if (level == 0) this.maxnof = val;
        else if (level == 1) this.maxf1 = val;
        else if (level == 2) this.maxf2 = val;
        else if (level == 3) this.maxf3 = val;
    }


}
