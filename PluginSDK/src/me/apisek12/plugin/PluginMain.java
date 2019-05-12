package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class PluginMain extends JavaPlugin {
    static Plugin plugin = null;

    public static HashMap<Player, Setting> playerSettings = new HashMap<>();

    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "Plugin wylączony!");
        plugin = null;
    }

    Location location;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
//            if (command.getName().equalsIgnoreCase("toon") && args != null){
//                Collection<Player> arrayList = (Collection<Player>) Bukkit.getOnlinePlayers();
//                Player playerSelected = null;
//                boolean jesli = false;
//                player.sendMessage(args[0]+" "+args[1]);
//                for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++){
//                    playerSelected = arrayList.iterator().next();
//                    if (playerSelected.getName().equalsIgnoreCase(args[0])){
//                        jesli = true;
//                        break;
//                    }
//                }
//        }
            if (command.getName().equalsIgnoreCase("drop")){
                Setting setting = playerSettings.get(player);
                if (args.length == 0 || args.length > 1) player.sendMessage(ChatColor.GRAY+"Komenda powinna wyglądać mniej więcej tak:\n"+ChatColor.GOLD+"/drop <info, cobble, zelazo, lapis, redstone, wegiel, diament, emerald, gold>");
                else if (args[0].equalsIgnoreCase("cobble")){
                    setting.ifCobble = !setting.ifCobble;
                    if (setting.ifCobble) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.AQUA+"cobbla"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.AQUA+"cobbla"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("zelazo")) {
                    setting.ifIron =  !setting.ifIron;
                    if (setting.ifIron) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.GRAY+"żelaza"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.GRAY+"żelaza"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("lapis")){
                    setting.ifLapis =  !setting.ifLapis;
                    if (setting.ifLapis) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.BLUE+"lapisu"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.BLUE+"lapisu"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("redstone")){
                    setting.ifRedstone =  !setting.ifRedstone;
                    if (setting.ifRedstone) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.RED+"redstone"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.RED+"redstone"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("wegiel")){
                    setting.ifCoal =  !setting.ifCoal;
                    if (setting.ifCoal) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.DARK_GRAY+"węgla"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.DARK_GRAY+"węgla"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("diament")){
                    setting.ifDiamond =  !setting.ifDiamond;
                    if (setting.ifDiamond) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.DARK_AQUA+"diamentów"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.DARK_AQUA+"diamentów"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("emerald")){
                    setting.ifEmerald =  !setting.ifEmerald;
                    if (setting.ifEmerald) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.GREEN+"szmaragdów"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.GREEN+"szmaragdów"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("gold")){
                    setting.ifGold =  !setting.ifGold;
                    if (setting.ifGold) player.sendMessage(ChatColor.GOLD+"Drop złota jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop złota jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("info")){
                    player.sendMessage(ChatColor.GREEN+"Obowiązują nastepujące ustawienia: \nCobblestone: "+setting.isIfCobble()+"\nWęgiel: "+setting.isIfCoal()+"\nŻelazo: "+setting.isIfIron()+"\nZłoto: "+setting.isIfGold()+"\nLapis: "+setting.isIfLapis()+"\nSzmaragdy: "+
                            setting.isIfEmerald()+"\nRedstone: "+setting.isIfRedstone()+"\nDiamenty: "+setting.isIfDiamond());
                }

                else player.sendMessage(ChatColor.GRAY+"Nieznany argument!\nKomenda powinna wyglądać mniej więcej tak:\n"+ChatColor.GOLD+"/drop <cobble, zelazo, lapis, redstone, wegiel, diament, emerald, gold>");
            }
        } else if (sender instanceof ConsoleCommandSender)
            if (command.getName().equalsIgnoreCase("shutdown") && args != null){
                long time = Long.parseLong(args[0])*1000;
                boolean wylacz = false;
                Runnable thread = new Runnable() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis();
                        long timer = System.currentTimeMillis();
                        while ((System.currentTimeMillis() - startTime) < time) {
                            if (System.currentTimeMillis() > timer+1000){
                                timer = System.currentTimeMillis();
                                long currentTime = System.currentTimeMillis();
                                Object[] players = plugin.getServer().getOnlinePlayers().toArray();

                                for (int i = 0; i < players.length; i++) {
                                    Player player = (Player) players[i];

                                    if ((int)((time/1000) - (currentTime-startTime)/1000) > 60)
                                        player.sendTitle(ChatColor.RED + "Serwer wyłączony za: "+ (int)((time/60000) - (currentTime-startTime)/60000)+" minut", null, 0, 25, 0);
                                    else player.sendTitle(ChatColor.RED + "Serwer wyłączony za: "+ (int)((time/1000) - (currentTime-startTime)/1000)+" sekund", null, 0, 25, 0);
                                }
                            }

                        }


                    }

                };
                Bukkit.getScheduler().runTaskAsynchronously(plugin, thread);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    }
                }, time*20/1000);
            }
            else sender.getServer().getConsoleSender().sendMessage("To musi byc gracz a nie konsola!");
        return false;

    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Plugin włączony!");
        plugin = this;
        this.getServer().getPluginManager().registerEvents(new MyEvents(), this);

    }
}
