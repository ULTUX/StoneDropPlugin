package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class PluginMain extends JavaPlugin {
    static Plugin plugin = null;
    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "Plugin wylączony!");
        plugin = null;
    }
    public static Plugin getInstance() {
        return plugin;
    }

    Location location;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
//             if (command.getName().equalsIgnoreCase("kol") && player.isOp()) {
//                 player.getInventory().addItem(new ItemStack(Material.TNT));
//                 player.getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL));
//                 player.sendTitle(ChatColor.RED + "Dodano TNT do ekwipunku!", ChatColor.AQUA + "oraz zapalniczke...", 10, 40, 20);
//                 return true;
//             }
            if (command.getName().equalsIgnoreCase("toon") && args != null){
                Collection<Player> arrayList = (Collection<Player>) Bukkit.getOnlinePlayers();
                Player playerSelected = null;
                boolean jesli = false;
                player.sendMessage(args[0]+" "+args[1]);
                for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++){
                    playerSelected = arrayList.iterator().next();
                    if (playerSelected.getName().equalsIgnoreCase(args[0])){
                        jesli = true;
                        break;
                    }
                }
//                if (jesli == true){
//                    getServer().broadcastMessage(player.getDisplayName()+": "+args[1]);
//                }
            }
//             if (command.getName().equalsIgnoreCase("sethome")) {
//                 location = player.getLocation();
//                 player.sendTitle(ChatColor.RED + "Changed home location.", "", 10, 40, 20);
//             }
//             if (command.getName().equalsIgnoreCase("home")){
//                 player.teleport(location);
//                 player.sendTitle(ChatColor.GREEN + "Teleported!", "", 10, 40, 20);
//                 player.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, location, 2);
//             }
        } else if (sender instanceof ConsoleCommandSender)
            sender.getServer().getConsoleSender().sendMessage("To musi byc gracz a nie konsola!");
        return false;

    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Plugin włączony!");
        plugin = this;
        this.getServer().getPluginManager().registerEvents(new MyEvents(), this);

    }
}
