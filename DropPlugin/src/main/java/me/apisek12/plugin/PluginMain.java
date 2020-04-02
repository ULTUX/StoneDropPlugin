package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class PluginMain extends JavaPlugin {
    static Plugin plugin = null;

    static HashMap<String, HashMap<String, Setting>> playerSettings = new HashMap<>(); //These are settings set by players
    static HashMap<String, DropChance> dropChances = new HashMap<>(); //These are chances set in config file String-material
    static HashMap<Material, ChestItemsInfo> chestContent = new HashMap<>();
    static float experienceToDrop;
    static double chestSpawnRate = 0;
    private static boolean isDisabled = false;
    private static BukkitTask shutdownThread = null;
    static ArrayList<String> disabledWorlds = null;
    private static FileConfiguration playerData = null;
    static ArrayList<Material> dropBlocks = null;

    static boolean isVersionNew(){
        String[] version = Bukkit.getBukkitVersion().replace(".", ",").replace("-", ",").split(",");
        if (Integer.parseInt(version[1]) > 12) return true;
        return false;
    }


    static boolean isIsDisabled() {
        return isDisabled;
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[StoneDrop] "+ChatColor.GRAY+"Saving getConfig() file...");
        playerSettings.forEach((player, settings) -> settings.forEach((material, setting)->{
            playerData.set("users."+player+"."+material, setting.isOn());
        }));
        try {
            playerData.save(new File(getDataFolder(), "playerData.yml"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY+"[StoneDrop] Config file saved!");

        } catch (IOException e) {
            getServer().getConsoleSender().sendMessage("[StoneDrop] Player data file not found, creating a new one...");
            try {
                playerData.save(new File(getDataFolder(), "playerData.yml"));
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY+"[StoneDrop] Config file saved!");
            } catch (IOException ex) {
                getServer().getConsoleSender().sendMessage("[StoneDrop] Could not create player data file!");

            }
        }
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] "+ChatColor.DARK_RED + "Plugin disabled!");
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isDisabled){
            if (command.getName().equalsIgnoreCase("whatdrops")){
                dropChances.forEach((ore, oreOptions) -> sender.sendMessage(oreOptions.toString()));
            }
        }

        if (sender instanceof Player && !isDisabled) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("drop")){
                HashMap<String, Setting> setting = playerSettings.get(player.getUniqueId().toString());
                boolean wasOk = false;
                if (args.length == 0){
                    playerSettings.get(player.getUniqueId().toString()).forEach((material, preferences)-> player.sendMessage(ChatColor.GOLD+material+": "+preferences.isOn()));
                    wasOk = true;
                }
                else if (args.length > 1) player.sendMessage(ChatColor.GRAY+"Command should look like that:\n"+ChatColor.GOLD+"/drop <info, stack, cobble, zelazo, lapis, redstone, wegiel, diament, emerald, gold>");
                else {
                    if (args[0].equalsIgnoreCase("cobble")) {
                        wasOk = true;
                            setting.get("COBBLE").setOn(!setting.get("COBBLE").isOn());
                        if (!setting.get("COBBLE").isOn())
                            player.sendMessage(ChatColor.GOLD + "Drop " + ChatColor.AQUA + "of cobble" + ChatColor.GOLD + " is now "+ChatColor.GREEN+"enabled");
                        else
                            player.sendMessage(ChatColor.GOLD + "Drop " + ChatColor.AQUA + "of cobble" + ChatColor.GOLD + " is now "+ChatColor.RED+"disabled");
                    } else if (args[0].equalsIgnoreCase("stack")) {
                        wasOk = true;
                        setting.get("STACK").setOn(!setting.get("STACK").isOn());
                        if (setting.get("STACK").isOn())
                            player.sendMessage(ChatColor.RED + "Stacking" + ChatColor.GOLD + " is now "+ChatColor.GREEN+"enabled");
                        else
                            player.sendMessage(ChatColor.RED + "Stacking" + ChatColor.GOLD +" is now "+ChatColor.RED+"disabled");

                    } else {
                        for (int i = 0; i < MyEvents.set.length; i++) {
                            if (!MyEvents.set[i].equals("STACK") && !MyEvents.set[i].equals("COBBLE")){
                            if (args[0].equalsIgnoreCase(MyEvents.set[i])) {
                                setting.get(MyEvents.set[i]).setOn(!setting.get(MyEvents.set[i]).isOn());
                                if (setting.get(MyEvents.set[i]).isOn()) {
                                    player.sendMessage(ChatColor.GOLD + "Drop of " + ChatColor.AQUA + MyEvents.set[i] + ChatColor.GOLD + " is now "+ChatColor.GREEN+"enabled");
                                } else {
                                    player.sendMessage(ChatColor.GOLD + "Drop of " + ChatColor.AQUA + MyEvents.set[i] + ChatColor.GOLD + " is now "+ChatColor.RED+"disabled");
                                }
                                wasOk = true;
                            }
                        }}
                    }
                }
                if (!wasOk){
                    player.sendMessage(ChatColor.GRAY+"Unknown argument!\nCommand should look like:\n"+ChatColor.GOLD+"/drop <info, stack, DROPPABLE_NAME>");

                }

            }
        }
        if (sender instanceof ConsoleCommandSender || sender.isOp()) {
            if (command.getName().equalsIgnoreCase("emergencyDisable")) {
                isDisabled = !isDisabled;
                sender.sendMessage("PluginDisabled: " + isDisabled);
            }
            else if (command.getName().equalsIgnoreCase("shutdown") && args.length == 1) {
                long time = Long.parseLong(args[0]) * 1000;
                long timeToStop = System.currentTimeMillis() + time;
                final long[] lastDisplayedTime = {System.currentTimeMillis() - 60000}; // Last time when remaining minutes were displayed
                long startTime = System.currentTimeMillis(); //Moment in time of starting this command
                final long[] timer = {System.currentTimeMillis()}; //This is a timer that will count seconds untill 10 then reset

                Runnable thread = () -> {
                    if (System.currentTimeMillis() > timer[0] + 1000) {
                        timer[0] = System.currentTimeMillis();
                        Object[] players = plugin.getServer().getOnlinePlayers().toArray();

                        if (System.currentTimeMillis() >= timeToStop) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                        } else if (timeToStop - System.currentTimeMillis() >= 60000) {
                            if (System.currentTimeMillis() - lastDisplayedTime[0] >= 60000) {

                                for (Object o : players) {
                                    Player player = (Player) o;
                                    player.sendTitle(ChatColor.RED + "Server shut down in " + (int) ((time / 60000) - (System.currentTimeMillis() - startTime) / 60000) + " minutes", null, 10, 80, 10);
                                }
                                    getServer().getConsoleSender().sendMessage(ChatColor.RED + "Server shut down in " + (int) ((time / 60000) - (System.currentTimeMillis() - startTime) / 60000) + " minutes");
                                    timer[0] = System.currentTimeMillis();
                                    lastDisplayedTime[0] = System.currentTimeMillis();
                            }
                        } else {
                            for (Object o : players) {
                                Player player = (Player) o;
                                player.sendTitle(ChatColor.RED + "Server shut down in: " + (int) ((time / 1000) - (System.currentTimeMillis() - startTime) / 1000) + " seconds", null, 0, 40, 0);
                            }
                                getServer().getConsoleSender().sendMessage(ChatColor.RED + "Server shut down in " + (int) ((time / 1000) - (System.currentTimeMillis() - startTime) / 1000) + " seconds");
                        }
                    }

                };
                shutdownThread = Bukkit.getScheduler().runTaskTimer(plugin, thread, 0, 1);
                return true;
            }
            else if (command.getName().equalsIgnoreCase("cancelShutdown")) {
                    if (shutdownThread != null && !shutdownThread.isCancelled()) {
                            shutdownThread.cancel();
                            sender.sendMessage(ChatColor.GREEN + "Server shut down cancelled.");
                            getServer().getOnlinePlayers().forEach((player) -> player.sendTitle(ChatColor.GREEN + "Server shut down cancelled.", null, 10, 80, 10));
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "Server shut down has not been initialized yet. To initialize use command /shutdown <time_in_seconds>");
                        return false;
                    }
                }
        }

        return false;

    }
    boolean checkForSpace (Material material, Inventory inventory){
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++){
            if (contents[i] != null) {
                if (contents[i].getType().equals(material) && contents[i].getAmount() < material.getMaxStackSize()) return true;
            }
        }
        return false;
    }
    @Override
    public void onEnable() {
        playerData = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "playerData.yml"));
        Updater updater = new Updater(this, 339276, getFile(), Updater.UpdateType.DEFAULT, true);
        Metrics metrics = new Metrics(this);

        saveDefaultConfig();
        saveConfig();
        experienceToDrop = (float) ((double)getConfig().get("experience"));
        disabledWorlds = new ArrayList<>(getConfig().getStringList("disabled-worlds"));
        dropBlocks = new ArrayList<>();
        getConfig().getStringList("dropBlocks").forEach(material -> {
            dropBlocks.add(Material.getMaterial(material));
        });
        ConfigurationSection cs = playerData.getConfigurationSection("users");
        if (cs != null) {
            Set<String> keyList = cs.getKeys(false);
            keyList.forEach((user) -> {
                ConfigurationSection materialsSection = cs.getConfigurationSection(user);
                HashMap<String, Setting> settings = new HashMap<>();
                for (int i = 0; i < Objects.requireNonNull(materialsSection).getKeys(false).toArray().length; i++) {
                    String materialName = (String) materialsSection.getKeys(false).toArray()[i];
                    boolean setting = (boolean) materialsSection.get(materialName);
                    settings.put(materialName, new Setting(setting, materialName));
                }
                playerSettings.put(user, settings);

            });
        }

        loadChances();
        loadChestChances();
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] "+ChatColor.GREEN + "Confing Loaded, Plugin enabled!");
        plugin = this;
        this.getServer().getPluginManager().registerEvents(new MyEvents(), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (Bukkit.getServer().getOnlinePlayers().size() > 0){
                for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().toArray().length; i++){
                    Player player = (Player) Bukkit.getServer().getOnlinePlayers().toArray()[i];
                    if (playerSettings.get(player.getUniqueId().toString()).get("STACK").isOn()){
                        boolean tak = true;
                        while (tak){
                            if ((player.getInventory().containsAtLeast(new ItemStack(Material.REDSTONE), 9) && player.getInventory().firstEmpty() != -1) || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.REDSTONE_BLOCK, player.getInventory()))){
                                player.getInventory().removeItem(new ItemStack(Material.REDSTONE, 9));
                                player.getInventory().addItem(new ItemStack(Material.REDSTONE_BLOCK));
                            }
                            else tak = false;
                        }
                        if (isVersionNew()){
                            tak = true;
                            while (tak){
                                try {
                                    if ((player.getInventory().containsAtLeast(new ItemStack(Objects.requireNonNull(Material.getMaterial(Material.class.getField("LAPIS_LAZULI").getName()))), 9) && player.getInventory().firstEmpty() != -1)  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.LAPIS_BLOCK, player.getInventory()))){
                                        player.getInventory().removeItem(new ItemStack(Objects.requireNonNull(Material.getMaterial(Material.class.getField("LAPIS_LAZULI").getName())), 9));
                                        player.getInventory().addItem(new ItemStack(Material.LAPIS_BLOCK));

                                    }
                                    else tak = false;
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        tak = true;
                        while (tak){
                            if ((player.getInventory().containsAtLeast(new ItemStack(Material.COAL), 9) && player.getInventory().firstEmpty() != -1)  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.COAL_BLOCK, player.getInventory()))){
                                player.getInventory().removeItem(new ItemStack(Material.COAL, 9));
                                player.getInventory().addItem(new ItemStack(Material.COAL_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if ((player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 9) && player.getInventory().firstEmpty() != -1)  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.IRON_BLOCK, player.getInventory()))){
                                player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, 9));
                                player.getInventory().addItem(new ItemStack(Material.IRON_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if ((player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 9) && player.getInventory().firstEmpty() != -1) || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.DIAMOND_BLOCK, player.getInventory()))){
                                player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 9));
                                player.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if ((player.getInventory().containsAtLeast(new ItemStack(Material.GOLD_INGOT), 9) && player.getInventory().firstEmpty() != -1)  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.GOLD_BLOCK, player.getInventory()))){
                                player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, 9));
                                player.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if ((player.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), 9) && player.getInventory().firstEmpty() != -1) || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.EMERALD_BLOCK, player.getInventory()))){
                                player.getInventory().removeItem(new ItemStack(Material.EMERALD, 9));
                                player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK));

                            }
                            else tak = false;
                        }
                    }
                }
            }
    }, 40L, 80L);

        MyEvents.set = new String[dropChances.keySet().toArray().length];
        for (int i = 0; i < dropChances.keySet().toArray().length; i++) {
            MyEvents.set[i] = (String) dropChances.keySet().toArray()[i];
        }

        playerSettings.forEach((name, settings) -> {
            for (int i = 0; i < MyEvents.set.length; i++) {
                if (!settings.containsKey(MyEvents.set[i])) settings.put(MyEvents.set[i], new Setting(true, MyEvents.set[i]));
            }
        });
    }

    private void loadChestChances(){
        chestSpawnRate = (Double) getConfig().get("chest-spawn-chance");
        Set<String> config =  getConfig().getConfigurationSection("chest").getKeys(false);
        for (String k: config){
            Material material = Material.getMaterial(k);
            if (material != null){
                try {
                    HashMap<String, Integer> enchants = (HashMap) getConfig().getConfigurationSection("chest."+k+".enchant").getValues(false);
                   if (enchants != null) {
                       chestContent.put(material, new ChestItemsInfo((Double) getConfig().getConfigurationSection("chest."+k).get("chance"), (Integer) getConfig().getConfigurationSection("chest."+k).get("min"), (Integer) getConfig().getConfigurationSection("chest."+k).get("max"), enchants));
                   }
                }catch (NullPointerException e){
                    chestContent.put(material, new ChestItemsInfo((Double) getConfig().getConfigurationSection("chest."+k).get("chance"), (Integer) getConfig().getConfigurationSection("chest."+k).get("min"), (Integer) getConfig().getConfigurationSection("chest."+k).get("max")));
                }
            }
        }
    }

    private void loadChances() {

        for (String key : getConfig().getConfigurationSection("chances").getKeys(false)) {
            ConfigurationSection oreObject = getConfig().getConfigurationSection("chances."+key);
             DropChance oreObjectOptions = new DropChance();
             oreObjectOptions.setName(key);
            for (String fortuneLevel : Objects.requireNonNull(oreObject).getKeys(false)){
                if (!fortuneLevel.equals("enchant")){
                int level = Integer.parseInt(fortuneLevel.split(("-"))[1]);
                double chance = (double) oreObject.getConfigurationSection(fortuneLevel).get("chance");
                int min = (int) oreObject.getConfigurationSection(fortuneLevel).get("min-amount");
                int max = (int) oreObject.getConfigurationSection(fortuneLevel).get("max-amount");
                oreObjectOptions.setChance(level, chance);
                oreObjectOptions.setMinDrop(level, min);
                oreObjectOptions.setMaxDrop(level, max);
                try {
                    HashMap<String, Integer> enchants = (HashMap) getConfig().getConfigurationSection("chances").getConfigurationSection(key + ".enchant").getValues(false);
                    if (enchants != null) {
                        oreObjectOptions.setEnchant(enchants);
                    }
                }
                catch (NullPointerException eA){
                }
            }
            }
            dropChances.put(oreObjectOptions.getName(), oreObjectOptions);
        }

    }
}



