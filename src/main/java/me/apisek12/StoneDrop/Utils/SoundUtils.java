package me.apisek12.StoneDrop.Utils;

import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtils {
    public static void playSound(Player player, String sound){
        if (PluginMain.versionCompatible(14)) {
            player.playSound(player.getLocation(), Sound.valueOf(sound), (float)PluginMain.volume, 1);
            System.out.println("Playing...");
        }
    }
}
