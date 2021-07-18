package me.apisek12.StoneDrop.Utils;

import me.apisek12.StoneDrop.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.util.Scanner;

public class MessageUtils {
    public static void displayUpdateMessage(Player player) {
        Scanner reader;
        InputStream inputStream= PluginMain.plugin.getResource("update.txt");
        reader = new Scanner(inputStream, "utf-8");
        while (reader.hasNextLine()){
            String message = ChatColor.translateAlternateColorCodes('&', reader.nextLine());
            player.sendMessage(message);

        }
    }
}
