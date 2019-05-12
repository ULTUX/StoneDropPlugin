package me.apisek12.plugin;

public class Setting {
    boolean ifGold;
    boolean ifCoal;
    boolean ifIron;
    boolean ifDiamond;
    boolean ifLapis;
    boolean ifRedstone;
    boolean ifEmerald;
    boolean ifCobble;
    boolean ifStack;

    public String isIfStack() {
        if (ifStack) return "tak";
        return "nie";
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
