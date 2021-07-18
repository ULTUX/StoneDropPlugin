package me.apisek12.StoneDrop.Utils;

import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.entity.Player;

public class SoundUtils {
    public static void playSound(Player player, org.bukkit.Sound sound){
        if (PluginMain.versionCompatible(14))
            player.playSound(player.getLocation(), sound, (float)PluginMain.volume, 1);
    }
}
