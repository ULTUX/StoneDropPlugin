package me.apisek12.StoneDrop.DataModels;


import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashMap;

public class DropChance {
    private String name;

    private final int FORTUNES_AMOUNT = 4;
    private final double[] fortuneChances = new double[FORTUNES_AMOUNT];
    private final double[] fortuneMins = new double[FORTUNES_AMOUNT];
    private final double[] fortuneMaxs = new double[FORTUNES_AMOUNT];

    private double st;
    private int min_st, max_st;

    private int minLevel = 0, maxLevel = 256;

    private String customName = null;

    private Biome[] acceptedBiomes = null;

    private HashMap<Enchantment, Integer> enchant = new HashMap<>();

    private boolean isEnabled = true;

    public HashMap<Enchantment, Integer> getEnchant() {
        return enchant;
    }

    public DropChance(String name, double nof, double f1, double f2, double f3, double st, int minnof, int maxnof, int minf1, int maxf1, int minf2, int maxf2, int minf3, int maxf3, int min_st, int max_st, HashMap<Enchantment, Integer> enchant) {
        this.name = name;
        this.st = st;
        this.min_st = min_st;
        this.max_st = max_st;
        this.enchant = enchant;
        this.setFortuneChance(0,nof);
        this.setFortuneChance(1,f1);
        this.setFortuneChance(2,f2);
        this.setFortuneChance(3,f3);
        this.setFortuneItemsAmountMax(0,maxnof);
        this.setFortuneItemsAmountMax(1,maxf1);
        this.setFortuneItemsAmountMax(2,maxf2);
        this.setFortuneItemsAmountMax(3,maxf3);
        this.setFortuneItemsAmountMin(0,minnof);
        this.setFortuneItemsAmountMin(1,minf1);
        this.setFortuneItemsAmountMin(2,minf2);
        this.setFortuneItemsAmountMin(3,minf3);

    }

    public void setEnabled(boolean enable) {
        this.isEnabled = enable;
    }

    public boolean isEnabled() {return this.isEnabled;}

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

    public void setAcceptedBiomes(Collection<String> biomes) {
        ArrayList<Biome> acceptedBiomes1 = new ArrayList<>();
        biomes.forEach(s -> {
            try {
                Biome biome;
                 biome = Biome.valueOf(s.toUpperCase());
                acceptedBiomes1.add(biome);
            } catch (IllegalArgumentException ignored){}
        });
        acceptedBiomes = acceptedBiomes1.toArray(new Biome[0]);
    }

    public Biome[] getAcceptedBiomes() {
        return acceptedBiomes;
    }

    public void setEnchant(HashMap<String, Integer> enchant) {
        enchant.forEach((enchantName, level) ->{
            enchantName = enchantName.toLowerCase();
            if (PluginMain.versionCompatible(12)) {
                try {
                    Enchantment ench = (Enchantment) Enchantment.class.getMethod("getByKey", NamespacedKey.class).invoke(Enchantment.class, NamespacedKey.minecraft(enchantName));
                    if (ench != null) this.enchant.put(ench, level);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    Enchantment ench = (Enchantment) Enchantment.class.getMethod("getByName" , String.class).invoke(Enchantment.class, enchantName);
                    if (ench != null) this.enchant.put(ench, level);
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
        if(fortuneChances[0]>0){
            toReturn+=": \n   "+ChatColor.GREEN+ Message.INFO_FORTUNE_0+ " : "
                    +ChatColor.GRAY+" chance: "+format.format(fortuneChances[0]*100)
                    +"%, drop amount: " +fortuneMins[0]+"-"+fortuneMaxs[0];
        }
        if(fortuneChances[1]>0){
            toReturn+=": \n   "+ChatColor.GREEN+ Message.INFO_FORTUNE_1+ " : "
                    +ChatColor.GRAY+" chance: "+format.format(fortuneChances[1]*100)
                    +"%, drop amount: " +fortuneMins[1]+"-"+fortuneMaxs[1];
        }
        if(fortuneChances[2]>0){
            toReturn+=": \n   "+ChatColor.GREEN+ Message.INFO_FORTUNE_2+ " : "
                    +ChatColor.GRAY+" chance: "+format.format(fortuneChances[2]*100)
                    +"%, drop amount: " +fortuneMins[2]+"-"+fortuneMaxs[2];
        }
        if(fortuneChances[3]>0){
            toReturn+=": \n   "+ChatColor.GREEN+ Message.INFO_FORTUNE_3+ " : "
                    +ChatColor.GRAY+" chance: "+format.format(fortuneChances[3]*100)
                    +"%, drop amount: " +fortuneMins[0]+"-"+fortuneMaxs[3];
        }
        if(getST()>0){
            toReturn +="\n   "+ChatColor.GREEN+Message.INFO_SILK_TOUCH+ " :"
                    +ChatColor.GRAY+"chance: "+format.format(st*100)
                    +"%, drop amount: "+min_st+"-"+max_st;

        }

        /*for(int i=0; i<fortunesAmount;i++){
            if(fortuneChances[i]<)
            toReturn+=": \n   "+ChatColor.GREEN+"fortune " + i+ " : "
                    +ChatColor.GRAY+" chance: "+format.format(fortuneChances[i]*100)
                    +"%, drop amount: " +fortuneMins[i]+"-"+fortuneMaxs[i];
        }*/
        //toReturn +="\n   "+ChatColor.GREEN+"silk touch: "+ChatColor.GRAY+"chance: "+format.format(st*100)+"%, drop amount: "+min_st+"-"+max_st;
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
