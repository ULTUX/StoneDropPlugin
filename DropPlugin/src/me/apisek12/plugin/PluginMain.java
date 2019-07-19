package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class PluginMain extends JavaPlugin {
    static Plugin plugin = null;

    public static HashMap<String, Setting> playerSettings = new HashMap<>();
    public static HashMap<String, DropChance> dropChances = new HashMap<>();
    public static HashMap<Material, ChestItemsInfo> chestContent = new HashMap<>();
    public static double chestSpawnRate = 0;
    private static boolean isDisabled = false;

    public static boolean isIsDisabled() {
        return isDisabled;
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY+"Saving getConfig() file...");
        playerSettings.forEach((player, setting) -> { getConfig().set("users."+player, setting.toString());});
        saveConfig();
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY+"Config file saved!");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "Plugin disabled!");
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player && !isDisabled) {
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
            if (command.getName().equalsIgnoreCase("codropi")){
                player.sendMessage(ChatColor.GOLD+dropChances.toString());
            }
            if (command.getName().equalsIgnoreCase("drop")){
                Setting setting = playerSettings.get(player.getUniqueId().toString());
                if (args.length == 0 || args.length > 1) player.sendMessage(ChatColor.GRAY+"Komenda powinna wyglądać mniej więcej tak:\n"+ChatColor.GOLD+"/drop <info, stack, cobble, zelazo, lapis, redstone, wegiel, diament, emerald, gold>");
                else if (args[0].equalsIgnoreCase("cobble")){
                    setting.ifCobble = !setting.ifCobble;
                    if (setting.ifCobble) player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.AQUA+"cobbla"+ChatColor.GOLD+" jest teraz włączony");
                    else player.sendMessage(ChatColor.GOLD+"Drop "+ChatColor.AQUA+"cobbla"+ChatColor.GOLD+" jest teraz wyłączony");
                }
                else if (args[0].equalsIgnoreCase("stack")) {
                    setting.ifStack =  !setting.ifStack;
                    if (setting.ifStack) player.sendMessage(ChatColor.RED+"Stackowanie"+ChatColor.GOLD+" jest teraz włączone");
                    else player.sendMessage(ChatColor.RED+"Stackowanie"+ChatColor.GOLD+" jest teraz wyłączone");
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
                    player.sendMessage(ChatColor.GREEN+"Obowiązują nastepujące ustawienia: \nStackowanie: "+setting.isIfStack()+"\nCobblestone: "+setting.isIfCobble()+"\nWęgiel: "+setting.isIfCoal()+"\nŻelazo: "+setting.isIfIron()+"\nZłoto: "+setting.isIfGold()+"\nLapis: "+setting.isIfLapis()+"\nSzmaragdy: "+
                            setting.isIfEmerald()+"\nRedstone: "+setting.isIfRedstone()+"\nDiamenty: "+setting.isIfDiamond());
                }

                else player.sendMessage(ChatColor.GRAY+"Nieznany argument!\nKomenda powinna wyglądać mniej więcej tak:\n"+ChatColor.GOLD+"/drop <info, stack, cobble, zelazo, lapis, redstone, wegiel, diament, emerald, gold>");
            }
        } else if (sender instanceof ConsoleCommandSender)
            if (command.getName().equalsIgnoreCase("emergencyDisable")) {
                isDisabled = !isDisabled;
                sender.sendMessage("PluginDisabled: " + isDisabled);
            }
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
                                long timer2 = System.currentTimeMillis();
                                for (int i = 0; i < players.length; i++) {
                                    Player player = (Player) players[i];
                                        if (System.currentTimeMillis() > timer2 + 3000) {
                                            player.sendTitle(ChatColor.RED + "Serwer wyłączony za: " + (int) ((time / 60000) - (currentTime - startTime) / 60000) + " minut", null, 10, 80, 10);
                                            timer = System.currentTimeMillis();
                                    }
                                    else player.sendTitle(ChatColor.RED + "Serwer wyłączony za: "+ (int)((time/1000) - (currentTime-startTime)/1000)+" sekund", null, 0, 40, 0);
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

        saveDefaultConfig();
        saveConfig();

        ConfigurationSection cs =  getConfig().getConfigurationSection("users");
            if (cs != null) {
                Set<String> keyList = cs.getKeys(false);
                for (String key : keyList) {
                    String setting = (String) getConfig().get("users." + key);
                    String[] options = Objects.requireNonNull(setting).split(",");
                    boolean[] boolopt = new boolean[options.length];
                    for (int i = 0; i < options.length; i++) boolopt[i] = Boolean.parseBoolean(options[i]);

                    Setting finalSetting = new Setting(boolopt);
                    playerSettings.put(key, finalSetting);
                }
            }

        loadChances();
        loadChestChances();
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded getConfig()!\nPlugin enabled!");
        plugin = this;
        this.getServer().getPluginManager().registerEvents(new MyEvents(), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getOnlinePlayers().size() > 0){
                    for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().toArray().length; i++){
                        Player player = (Player) Bukkit.getServer().getOnlinePlayers().toArray()[i];
                        if (playerSettings.get(player.getUniqueId().toString()).ifStack){
                            boolean tak = true;
                            while (tak){
                                if (player.getInventory().containsAtLeast(new ItemStack(Material.REDSTONE), 9)){
                                    player.getInventory().removeItem(new ItemStack(Material.REDSTONE, 9));
                                    player.getInventory().addItem(new ItemStack(Material.REDSTONE_BLOCK));

                                }
                                else tak = false;
                            }
                            tak = true;
                            while (tak){
                                if (player.getInventory().containsAtLeast(new ItemStack(Material.LAPIS_LAZULI), 9)){
                                    player.getInventory().removeItem(new ItemStack(Material.LAPIS_LAZULI, 9));
                                    player.getInventory().addItem(new ItemStack(Material.LAPIS_BLOCK));

                                }
                                else tak = false;
                            }
                            tak = true;
                            while (tak){
                                if (player.getInventory().containsAtLeast(new ItemStack(Material.COAL), 9)){
                                    player.getInventory().removeItem(new ItemStack(Material.COAL, 9));
                                    player.getInventory().addItem(new ItemStack(Material.COAL_BLOCK));

                                }
                                else tak = false;
                            }
                            tak = true;
                            while (tak){
                                if (player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 9)){
                                    player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, 9));
                                    player.getInventory().addItem(new ItemStack(Material.IRON_BLOCK));

                                }
                                else tak = false;
                            }
                            tak = true;
                            while (tak){
                                if (player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 9)){
                                    player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 9));
                                    player.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK));

                                }
                                else tak = false;
                            }
                            tak = true;
                            while (tak){
                                if (player.getInventory().containsAtLeast(new ItemStack(Material.GOLD_INGOT), 9)){
                                    player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, 9));
                                    player.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK));

                                }
                                else tak = false;
                            }
                            tak = true;
                            while (tak){
                                if (player.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), 9)){
                                    player.getInventory().removeItem(new ItemStack(Material.EMERALD, 9));
                                    player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK));

                                }
                                else tak = false;
                            }
                        }
                    }
                }
        }}, 40L, 80L);

        MyEvents.set = new String[dropChances.keySet().toArray().length];
    for (int i = 0; i < dropChances.keySet().toArray().length; i++){
        MyEvents.set[i] = (String) dropChances.keySet().toArray()[i];
    }

    }

    private void loadChestChances(){
        chestSpawnRate = (Double) getConfig().get("chest-spawn-chance");
        Set<String> config =  getConfig().getConfigurationSection("chest").getKeys(false);
        for (String k: config){
            Material material = Material.getMaterial(k);
            if (material != null){
                chestContent.put(material, new ChestItemsInfo((Double) getConfig().getConfigurationSection("chest."+k).get("chance"), (Integer) getConfig().getConfigurationSection("chest."+k).get("min"), (Integer) getConfig().getConfigurationSection("chest."+k).get("max")));
            }
        }
    }

    private void loadChances() {

        for (String key : getConfig().getConfigurationSection("chances").getKeys(false)) {
            ConfigurationSection oreObject = getConfig().getConfigurationSection("chances."+key);
             DropChance oreObjectOptions = new DropChance();
             oreObjectOptions.setName(key);
            for (String fortuneLevel : Objects.requireNonNull(oreObject).getKeys(false)){
                int level = Integer.parseInt(fortuneLevel.split(("-"))[1]);
                double chance = (double) oreObject.getConfigurationSection(fortuneLevel).get("chance");
                int min = (int) oreObject.getConfigurationSection(fortuneLevel).get("min-amount");
                int max = (int) oreObject.getConfigurationSection(fortuneLevel).get("max-amount");
                oreObjectOptions.setChance(level, chance);
                oreObjectOptions.setMinDrop(level, min);
                oreObjectOptions.setMaxDrop(level, max);
            }
            dropChances.put(oreObjectOptions.getName(), oreObjectOptions);
        }

    }
}



