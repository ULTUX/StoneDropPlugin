package me.apisek12.plugin;

import java.io.Serializable;

public class Setting  {
    boolean ifGold;
    boolean ifCoal;
    boolean ifIron;
    boolean ifDiamond;
    boolean ifLapis;
    boolean ifRedstone;
    boolean ifEmerald;
    boolean ifCobble;
    boolean ifStack;


    public Setting(boolean ifGold, boolean ifCoal, boolean ifIron, boolean ifDiamond, boolean ifLapis, boolean ifRedstone, boolean ifEmerald, boolean ifCobble, boolean ifStack) {
        this.ifGold = ifGold;
        this.ifCoal = ifCoal;
        this.ifIron = ifIron;
        this.ifDiamond = ifDiamond;
        this.ifLapis = ifLapis;
        this.ifRedstone = ifRedstone;
        this.ifEmerald = ifEmerald;
        this.ifCobble = ifCobble;
        this.ifStack = ifStack;
    }

    public Setting (boolean[] options){
        this.ifGold = options[0];
        this.ifCoal = options[1];
        this.ifIron = options[2];
        this.ifDiamond = options[3];
        this.ifLapis = options[4];
        this.ifRedstone = options[5];
        this.ifEmerald = options[6];
        this.ifCobble = options[7];
        this.ifStack = options[8];
    }

    public String isIfStack() {
        if (ifStack) return "tak";
        return "nie";
    }


    @Override
    public String toString() {
        return        ifGold +
                "," + ifCoal +
                "," + ifIron +
                "," + ifDiamond +
                "," + ifLapis +
                "," + ifRedstone +
                "," + ifEmerald +
                "," + ifCobble +
                "," + ifStack;
    }

    public Setting fromString(String string){
        boolean ifGold;
        boolean ifCoal;
        boolean ifIron;
        boolean ifDiamond;
        boolean ifLapis;
        boolean ifRedstone;
        boolean ifEmerald;
        boolean ifCobble;
        boolean ifStack;

        return new Setting();
    }

    public void setIfStack(boolean ifStack) {
        this.ifStack = ifStack;
    }


    public String isIfCobble() {
        if (ifCobble)return "tak";
        return "nie";
    }

    public void setIfCobble(boolean ifCobble) {
        this.ifCobble = ifCobble;
    }

    public Setting() {
        ifGold = true;
        ifCoal = true;
        ifIron = true;
        ifDiamond = true;
        ifLapis = true;
        ifRedstone = true;
        ifEmerald = true;
        ifCobble = true;
        ifStack = false;
    }


    public boolean get (String name){
        if (name.equals("gold")) return ifGold;
        else if (name.equals("coal")) return ifCoal;
        else if (name.equals("iron")) return ifIron;
        else if (name.equals("diamond")) return ifDiamond;
        else if (name.equals("lapis")) return ifLapis;
        else if (name.equals("redstone")) return ifRedstone;
        else if (name.equals("emerald")) return ifEmerald;
        return false;
    }

    public void setIfGold(boolean ifGold) {
        this.ifGold = ifGold;
    }

    public void setIfCoal(boolean ifCoal) {
        this.ifCoal = ifCoal;
    }

    public void setIfIron(boolean ifIron) {
        this.ifIron = ifIron;
    }

    public void setIfDiamond(boolean ifDiamond) {
        this.ifDiamond = ifDiamond;
    }

    public void setIfLapis(boolean ifLapis) {
        this.ifLapis = ifLapis;
    }

    public void setIfRedstone(boolean ifRedstone) {
        this.ifRedstone = ifRedstone;
    }

    public void setIfEmerald(boolean ifEmerald) {
        this.ifEmerald = ifEmerald;
    }

    public String isIfGold() {
        if (ifGold) return "tak";
        else return "nie";
    }

    public String isIfCoal() {
        if (ifCoal) return "tak";
        else return "nie";
    }

    public String isIfIron() {
        if (ifIron) return "tak";
        else return "nie";
    }

    public String isIfDiamond() {
        if (ifDiamond) return "tak";
        else return "nie";
    }

    public String isIfLapis() {
        if (ifLapis) return "tak";
        else return "nie";
    }

    public String isIfRedstone() {
        if (ifRedstone) return "tak";
        else return "nie";
    }

    public String isIfEmerald() {
        if (ifEmerald) return "tak";
        else return "nie";
    }
}
