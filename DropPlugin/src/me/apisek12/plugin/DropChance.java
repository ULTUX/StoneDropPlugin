package me.apisek12.plugin;


public class DropChance {
    private String name;
    private double nof, f1, f2, f3;
    private int minnof, maxnof, minf1, maxf1, minf2, maxf2, minf3, maxf3;



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
        return "DropChance{" +
                "name='" + name + '\'' +
                ", nof=" + nof +
                ", f1=" + f1 +
                ", f2=" + f2 +
                ", f3=" + f3 +
                ", minnof=" + minnof +
                ", maxnof=" + maxnof +
                ", minf1=" + minf1 +
                ", maxf1=" + maxf1 +
                ", minf2=" + minf2 +
                ", maxf2=" + maxf2 +
                ", minf3=" + minf3 +
                ", maxf3=" + maxf3 +
                '}';
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
