package me.apisek12.plugin;

import org.bukkit.*;
import org.bukkit.Material;
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
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getPluginManager;

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
    private static FileConfiguration langData = null;
    static ArrayList<Material> dropBlocks = null;
    static public boolean dropFromOres = true;
    public static boolean dropIntoInventory = false;
    public static boolean displayUpdateMessage = true;
    public static Material wooden = null, golden = null;
    public static boolean isNetherite = false;
    static HashMap<String, String> playerLastVersionPluginVersion = new HashMap<>();
    public static String currentPluginVersion;
    public static boolean dropExpOrb = false;
    public static boolean treasureChestBroadcast = true;
    public static double oreDropChance = 1.0f;

    static boolean isVersionNew(){
        String[] version = Bukkit.getBukkitVersion().replace(".", ",").replace("-", ",").split(",");
        return Integer.parseInt(version[1]) > 12;
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

        playerLastVersionPluginVersion.forEach((user, version) -> {
            playerData.set("userVersion."+user, version);
        });

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
                if (sender instanceof ConsoleCommandSender || (sender instanceof Player && sender.hasPermission("stonedrop.whatdrops"))){
                    dropChances.forEach((ore, oreOptions) -> sender.sendMessage(oreOptions.toString()));
                }
                else {
                    sender.sendMessage(ChatColor.RED+"You don't have permission to use that command!");
                }
            }
        }

        if (sender instanceof Player && !isDisabled) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("drop")){
                if (player.hasPermission("stonedrop.drop")){
                    HashMap<String, Setting> setting = playerSettings.get(player.getUniqueId().toString());
                    boolean wasOk = false;
                    if (args.length == 0 || (args.length == 1 && args[0] == "info")){
//                        playerSettings.get(player.getUniqueId().toString()).forEach((material, preferences)-> {
//                            String stringMaterial = material;
//                            stringMaterial = stringMaterial.replace("_", " ");
//                            stringMaterial = stringMaterial.toLowerCase();
//                            String toSend = ChatColor.GREEN+stringMaterial+": "+ChatColor.BLUE+preferences.isOn();
//                            toSend = toSend.replace("true", "on").replace("false", "off");
//                            player.sendMessage(toSend);
//                        });
                        new InventorySelector(player, setting);
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
                            for (int i = 0; i < MainEventListener.set.length; i++) {
                                if (!MainEventListener.set[i].equals("STACK") && !MainEventListener.set[i].equals("COBBLE")){
                                    if (args[0].equalsIgnoreCase(MainEventListener.set[i])) {
                                        setting.get(MainEventListener.set[i]).setOn(!setting.get(MainEventListener.set[i]).isOn());
                                        if (setting.get(MainEventListener.set[i]).isOn()) {
                                            player.sendMessage(ChatColor.GOLD + "Drop of " + ChatColor.AQUA + MainEventListener.set[i] + ChatColor.GOLD + " is now "+ChatColor.GREEN+"enabled");
                                        } else {
                                            player.sendMessage(ChatColor.GOLD + "Drop of " + ChatColor.AQUA + MainEventListener.set[i] + ChatColor.GOLD + " is now "+ChatColor.RED+"disabled");
                                        }
                                        wasOk = true;
                                    }
                                }}
                        }
                    }
                if (!wasOk){
                    player.sendMessage(ChatColor.GRAY+Message.COMMAND_ARGUMENT_UNKNOWN.toString());

                }

            } else {
                    player.sendMessage(ChatColor.RED+Message.PERMISSION_MISSING.toString());
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

    static private void generateConfig(){
        File file = new File(plugin.getDataFolder()+File.separator+"config.yml");
        if (!file.exists()) {
            try (OutputStream outputStream = new FileOutputStream(file.toPath().toString())) {
                InputStream is = plugin.getResource("config.yml");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    static private void generateLang(){
        File file = new File(plugin.getDataFolder()+File.separator+"lang.yml");
        if (!file.exists()) {
            getConsoleSender().sendMessage("lang.yml file has not been found, generating a new one...");
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            for (Message message: Message.values()){
                    configuration.set(message.name(), message.toString());
            }
            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        currentPluginVersion = getDescription().getVersion();
        generateConfig();
        generateLang();
        MainEventListener.initialize();
        //Check if plugin should drop items into inventory
        dropIntoInventory = getConfig().getBoolean("drop-to-inventory");
        //Check if plugin should block item dropping from ores
        dropFromOres = getConfig().getBoolean("ore-drop");
        displayUpdateMessage = getConfig().getBoolean("display-update-message");
        dropExpOrb = getConfig().getBoolean("drop-exp-orb");
        treasureChestBroadcast = getConfig().getBoolean("treasure-broadcast");
        oreDropChance = getConfig().getDouble("ore-drop-chance");
        if (!dropFromOres) getServer().getConsoleSender().sendMessage("["+this.getName()+"] Drop from ores is now disabled");

        //Check if version is < 1.8.9
        try {
            if (PluginMain.isVersionNew()) {
                golden = Material.getMaterial(Material.class.getField("GOLDEN_PICKAXE").getName());
                wooden = Material.getMaterial(Material.class.getField("WOODEN_PICKAXE").getName());
                isNetherite = true;
            } else {
                golden = Material.getMaterial(Material.class.getField("GOLD_PICKAXE").getName());
                wooden = Material.getMaterial(Material.class.getField("WOOD_PICKAXE").getName());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        playerData = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "playerData.yml"));
        langData = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang.yml"));
        langData.getKeys(false).forEach(key -> {
            Message.valueOf(key).setDefaultMessage((String) langData.get(key));
        });
        Updater updater = new Updater(this, 339276, getFile(), Updater.UpdateType.DEFAULT, true);
        Metrics metrics = new Metrics(this);

        experienceToDrop = (float) ((double)getConfig().get("experience"));
        disabledWorlds = new ArrayList<>(getConfig().getStringList("disabled-worlds"));
        dropBlocks = new ArrayList<>();
        getConfig().getStringList("dropBlocks").forEach(material -> {
            dropBlocks.add(Material.getMaterial(material));
        });
        final ConfigurationSection cs = playerData.getConfigurationSection("users");
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
        final ConfigurationSection cs2 = playerData.getConfigurationSection("userVersion");
        if (cs2 != null){
            Set<String> keyList = cs2.getKeys(false);
            keyList.forEach((user) -> {
                String version = cs2.get(user).toString();
                if (version != null) playerLastVersionPluginVersion.put(user, version);

            });
        }

        loadChances();
        loadChestChances();
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] "+ChatColor.GREEN + "Configuration Loaded, Plugin enabled!");
        this.getServer().getPluginManager().registerEvents(new MainEventListener(), this);
        getPluginManager().registerEvents(new InventorySelector(), this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (Bukkit.getServer().getOnlinePlayers().size() > 0){
                for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().toArray().length; i++){
                    Player player = (Player) Bukkit.getServer().getOnlinePlayers().toArray()[i];
                    if (playerSettings.get(player.getUniqueId().toString()) != null && playerSettings.get(player.getUniqueId().toString()).get("STACK").isOn()){
                        boolean tak = true;
                        while (tak){
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.REDSTONE), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.REDSTONE_BLOCK, player.getInventory())))){
                                player.getInventory().removeItem(new ItemStack(Material.REDSTONE, 9));
                                player.getInventory().addItem(new ItemStack(Material.REDSTONE_BLOCK));
                            }
                            else tak = false;
                        }
                        if (isVersionNew()){
                            tak = true;
                            while (tak){
                                try {
                                    if (player.getInventory().containsAtLeast(new ItemStack(Objects.requireNonNull(Material.getMaterial(Material.class.getField("LAPIS_LAZULI").getName()))), 9)
                                            && (player.getInventory().firstEmpty() != -1  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.LAPIS_BLOCK, player.getInventory())))){
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
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.COAL), 9)
                                    && (player.getInventory().firstEmpty() != -1  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.COAL_BLOCK, player.getInventory())))){
                                player.getInventory().removeItem(new ItemStack(Material.COAL, 9));
                                player.getInventory().addItem(new ItemStack(Material.COAL_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 9)
                                    && (player.getInventory().firstEmpty() != -1  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.IRON_BLOCK, player.getInventory())))){
                                player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, 9));
                                player.getInventory().addItem(new ItemStack(Material.IRON_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.DIAMOND_BLOCK, player.getInventory())))){
                                player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 9));
                                player.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.GOLD_INGOT), 9)
                                    && (player.getInventory().firstEmpty() != -1  || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.GOLD_BLOCK, player.getInventory())))){
                                player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, 9));
                                player.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK));

                            }
                            else tak = false;
                        }
                        tak = true;
                        while (tak){
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.EMERALD_BLOCK, player.getInventory())))){
                                player.getInventory().removeItem(new ItemStack(Material.EMERALD, 9));
                                player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK));

                            }
                            else tak = false;
                        }
                    }
                }
            }
    }, 40L, 80L);

        MainEventListener.set = new String[dropChances.keySet().toArray().length];
        for (int i = 0; i < dropChances.keySet().toArray().length; i++) {
            MainEventListener.set[i] = (String) dropChances.keySet().toArray()[i];
        }

        playerSettings.forEach((name, settings) -> {
            for (int i = 0; i < MainEventListener.set.length; i++) {
                if (!settings.containsKey(MainEventListener.set[i])) settings.put(MainEventListener.set[i], new Setting(true, MainEventListener.set[i]));
            }
        });
    }

    private void loadChestChances(){
        chestSpawnRate = Double.parseDouble(getConfig().get("chest-spawn-chance").toString());
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
                if (fortuneLevel.split("-")[0].equals("fortune")) {
                    int level = Integer.parseInt(fortuneLevel.split(("-"))[1]);
                    double chance = (double) oreObject.getConfigurationSection(fortuneLevel).get("chance");
                    int min = (int) oreObject.getConfigurationSection(fortuneLevel).get("min-amount");
                    int max = (int) oreObject.getConfigurationSection(fortuneLevel).get("max-amount");
                    oreObjectOptions.setChance(level, chance);
                    oreObjectOptions.setMinDrop(level, min);
                    oreObjectOptions.setMaxDrop(level, max);
                }
            }
            try {
                HashMap<String, Integer> enchants = (HashMap) getConfig().getConfigurationSection("chances").getConfigurationSection(key + ".enchant").getValues(false);
                if (enchants.size() != 0) {
                    oreObjectOptions.setEnchant(enchants);
                }
            }
            catch (NullPointerException ignored){
            }
            try {
                int minLevel = -1, maxLevel = -1;
                if (getConfig().getConfigurationSection("chances").getConfigurationSection(key).contains("minLevel", true)) minLevel = getConfig().getConfigurationSection("chances").getConfigurationSection(key).getInt("minLevel");
                if (getConfig().getConfigurationSection("chances").getConfigurationSection(key).contains("maxLevel", true)) maxLevel = getConfig().getConfigurationSection("chances").getConfigurationSection(key).getInt("maxLevel");
                if (minLevel == maxLevel && minLevel == -1) throw new NullPointerException();
                oreObjectOptions.setMinLevel(minLevel);
                oreObjectOptions.setMaxLevel(maxLevel);
            } catch (NullPointerException ignored){}
            try {
                String text;
                if (getConfig().getConfigurationSection("chances").getConfigurationSection(key).contains("customName", true)) {
                    text = getConfig().getConfigurationSection("chances").getConfigurationSection(key).getString("customName");
                    String customName = ChatColor.translateAlternateColorCodes('&', text);
                    oreObjectOptions.setCustomName(customName);
                }

            } catch (NullPointerException ignored){}
            dropChances.put(oreObjectOptions.getName(), oreObjectOptions);
        }

    }
}



