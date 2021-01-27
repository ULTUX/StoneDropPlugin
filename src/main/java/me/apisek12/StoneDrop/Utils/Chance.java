package me.apisek12.StoneDrop.Utils;

public class Chance {

    public static boolean chance(double prec){
        if (Math.random() <= prec) return true;
        return false;
    }

    public static int randBetween(int a, int b){
        if (a == b) return a;
        return (int)(a + (Math.round(Math.random()*(b-a))));
    }
}
