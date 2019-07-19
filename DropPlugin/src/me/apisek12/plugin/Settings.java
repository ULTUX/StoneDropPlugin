package me.apisek12.plugin;

public class Settings {
    boolean ifCobble;
    boolean ifStack;


    public Settings(boolean ifGold, boolean ifCoal, boolean ifIron, boolean ifDiamond, boolean ifLapis, boolean ifRedstone, boolean ifEmerald, boolean ifCobble, boolean ifStack) {

        this.ifCobble = ifCobble;
        this.ifStack = ifStack;
    }

    public Settings(boolean[] options){

        this.ifCobble = options[7];
        this.ifStack = options[8];
    }

    public String isIfStack() {
        if (ifStack) return "tak";
        return "nie";
    }


    @Override
    public String toString() {
        return   ifCobble +
                "," + ifStack;
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

    public Settings() {
        ifCobble = true;
        ifStack = false;
    }

}
