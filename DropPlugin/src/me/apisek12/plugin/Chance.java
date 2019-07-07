package me.apisek12.plugin;

public class Chance {

    public static boolean chance(double prec){
        if (Math.random() <= prec) return true;
        return false;
    }

    public static int randBetween(int a, int b){
        return (int)(a + (Math.random()*(b-a) + 1));
    }
}
