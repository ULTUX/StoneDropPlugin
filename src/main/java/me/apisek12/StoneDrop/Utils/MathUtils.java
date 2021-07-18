package me.apisek12.StoneDrop.Utils;

public class MathUtils {

    public static boolean chance(double prec){
        return Math.random() <= prec;
    }

    public static int randBetween(int a, int b){
        if (a == b) return a;
        return (int)(a + (Math.round(Math.random()*(b-a))));
    }
}
