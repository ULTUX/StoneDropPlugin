package me.apisek12.StoneDrop.DataModels;

import org.bukkit.Material;

public class Setting {
    private boolean on;
    private String name;

    @Override
    public String toString() {
        return "Setting{" +
                "on=" + on +
                ", name='" + name + '\'' +
                '}';
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Setting(boolean on, String name) {
        this.on = on;
        this.name = name;
    }
    public boolean toggle(){
        this.on = !this.on;
        return on;
    }

    Material toMaterial(){
        return Material.getMaterial(name);
    }

}
