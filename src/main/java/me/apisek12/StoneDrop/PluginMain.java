package me.apisek12.StoneDrop;

import me.apisek12.StoneDrop.Apis.Updater;
import me.apisek12.StoneDrop.DataModels.ChestItemsInfo;
import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.DataModels.ExecuteCommands;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.Apis.Metrics;
import me.apisek12.StoneDrop.EventListeners.BlockBreakEventListener;
import me.apisek12.StoneDrop.InventorySelectors.InventorySelector;
import me.apisek12.StoneDrop.InventorySelectors.InventorySelectorAdvanced;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Dye;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getPluginManager;

public class PluginMain extends JavaPlugin {
    public static PluginMain plugin = null;
    public static LinkedHashMap<String, LinkedHashMap<String, Setting>> playerSettings; //These are settings set by players
    public static LinkedHashMap<String, DropChance> dropChances; //These are chances set in config file String-material
    public static HashMap<Material, ChestItemsInfo> chestContent;
    public static float experienceToDrop;
    public static double chestSpawnRate = 0;
    private static BukkitTask shutdownThread = null;
    public static ArrayList<String> disabledWorlds = null;
    private static FileConfiguration playerData = null;
    private static FileConfiguration langData = null;
    public static ArrayList<Material> dropBlocks = null;
    static public boolean dropFromOres = true;
    public static ArrayList<Material> dropOresWhiteList = null;
    public static boolean dropIntoInventory = false;
    public static boolean displayUpdateMessage = true;
    public static Material wooden = null, golden = null, lapis_ore = null;
    public static boolean isNetherite = false;
    public static HashMap<String, String> playerLastVersionPluginVersion = new HashMap<>();
    public static String currentPluginVersion;
    public static boolean dropExpOrb = false;
    public static boolean treasureChestBroadcast = true;
    public static double oreDropChance = 1.0f;
    public static double volume = 0.3d;
    public static ArrayList<ExecuteCommands> commands = new ArrayList<>();
    public static boolean dropChestToInv = false;
    public static boolean realisticDrop = true;
    public static String bukkitVersion;



    /**
     * Checks if plugin version is compatible with given version number. Value is a second number in version string (in 1.16.6 it is 16).
     *
     * @param val A version to compare with.
     * @return true if plugin verison is greater or equal than given value.
     */
    public boolean versionCompatible(int val) {
        String[] version = Bukkit.getBukkitVersion().replace(".", ",").replace("-", ",").split(",");
        return Integer.parseInt(version[1]) > val;
    }



    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[StoneDrop] " + ChatColor.GRAY + "Saving getConfig() file...");
        playerSettings.forEach((player, settings) -> settings.forEach((material, setting) -> {
            playerData.set("users." + player + "." + material, setting.isOn());
        }));

        playerLastVersionPluginVersion.forEach((user, version) -> {
            playerData.set("userVersion." + user, version);
        });

        try {
            playerData.save(new File(getDataFolder(), "playerData.yml"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[StoneDrop] Config file saved!");

        } catch (IOException e) {
            getServer().getConsoleSender().sendMessage("[StoneDrop] Player data file not found, creating a new one...");
            try {
                playerData.save(new File(getDataFolder(), "playerData.yml"));
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[StoneDrop] Config file saved!");
            } catch (IOException ex) {
                getServer().getConsoleSender().sendMessage("[StoneDrop] Could not create player data file!");

            }
        }
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] " + ChatColor.DARK_RED + "Plugin disabled!");
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("drop") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("stonedrop.reload")) {
                sender.sendMessage(ChatColor.GRAY + "Starting reload...");
                reloadConfig();
                sender.sendMessage(ChatColor.GRAY + "Unregistering all event listeners...");
                HandlerList.unregisterAll(plugin);
                sender.sendMessage(ChatColor.GRAY + "Generating config files...");
                generateConfig();
                generateLang();
                sender.sendMessage(ChatColor.GRAY + "Registering new event listeners");
                getPluginManager().registerEvents(new BlockBreakEventListener(), this);
                getPluginManager().registerEvents(new InventorySelector(), this);
                getPluginManager().registerEvents(new InventorySelectorAdvanced(), this);
                sender.sendMessage(ChatColor.GRAY + "Loading all config files...");
                loadConfig();
                loadPlayerData();
                loadChances();
                loadChestChances();
                BlockBreakEventListener.initialize();
                sender.sendMessage(ChatColor.GREEN + Message.RELOADED_SUCCESSFULLY.toString());
                return true;
            } else sender.sendMessage(ChatColor.RED + Message.PERMISSION_MISSING.toString());
        }
        if (command.getName().equalsIgnoreCase("whatdrops")) {
            if (sender instanceof ConsoleCommandSender || (sender instanceof Player && sender.hasPermission("stonedrop.whatdrops"))) {
                dropChances.forEach((ore, oreOptions) -> sender.sendMessage(oreOptions.toString()));
            } else {
                sender.sendMessage(ChatColor.RED + Message.PERMISSION_MISSING.toString());
            }
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("drop")) {
                if (player.hasPermission("stonedrop.drop")) {
                    if (args.length > 1) {
                        player.sendMessage(ChatColor.RED + Message.COMMAND_ARGUMENT_UNKNOWN.toString());
                        return false;
                    }
                    LinkedHashMap<String, Setting> setting = playerSettings.get(player.getUniqueId().toString());
                    if (args.length == 0 || (args.length == 1 && args[0] == "info")) {
                        if(player.hasPermission("stonedrop.drop.advanced")){
                            new InventorySelectorAdvanced(player, setting);
                            return true;
                        } else {
                            new InventorySelector(player,setting);
                            return true;
                        }

                    } else {
                        if (args[0].equalsIgnoreCase("cobble")) {
                            setting.get("COBBLE").setOn(!setting.get("COBBLE").isOn());
                            if (!setting.get("COBBLE").isOn())
                                player.sendMessage(ChatColor.GOLD + "Drop " + ChatColor.AQUA + "of cobble" + ChatColor.GOLD + " is now " + ChatColor.GREEN + "enabled");
                            else
                                player.sendMessage(ChatColor.GOLD + "Drop " + ChatColor.AQUA + "of cobble" + ChatColor.GOLD + " is now " + ChatColor.RED + "disabled");
                            return true;
                        } else if (args[0].equalsIgnoreCase("stack")) {
                            setting.get("STACK").setOn(!setting.get("STACK").isOn());
                            if (setting.get("STACK").isOn())
                                player.sendMessage(ChatColor.RED + "Stacking" + ChatColor.GOLD + " is now " + ChatColor.GREEN + "enabled");
                            else
                                player.sendMessage(ChatColor.RED + "Stacking" + ChatColor.GOLD + " is now " + ChatColor.RED + "disabled");
                            return true;
                        } else {
                            for (int i = 0; i < BlockBreakEventListener.set.length; i++) {
                                if (!BlockBreakEventListener.set[i].equals("STACK") && !BlockBreakEventListener.set[i].equals("COBBLE")) {
                                    if (args[0].equalsIgnoreCase(BlockBreakEventListener.set[i])) {
                                        setting.get(BlockBreakEventListener.set[i]).setOn(!setting.get(BlockBreakEventListener.set[i]).isOn());
                                        if (setting.get(BlockBreakEventListener.set[i]).isOn()) {
                                            player.sendMessage(ChatColor.GOLD + "Drop of " + ChatColor.AQUA + BlockBreakEventListener.set[i] + ChatColor.GOLD + " is now " + ChatColor.GREEN + "enabled");
                                        } else {
                                            player.sendMessage(ChatColor.GOLD + "Drop of " + ChatColor.AQUA + BlockBreakEventListener.set[i] + ChatColor.GOLD + " is now " + ChatColor.RED + "disabled");
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    player.sendMessage(ChatColor.GRAY + Message.COMMAND_ARGUMENT_UNKNOWN.toString());
                } else {
                    player.sendMessage(ChatColor.RED + Message.PERMISSION_MISSING.toString());
                }
            }
        }
        if (sender instanceof ConsoleCommandSender || sender.isOp()) {
            if (command.getName().equalsIgnoreCase("shutdown") && args.length == 1) {
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
            } else if (command.getName().equalsIgnoreCase("cancelShutdown")) {
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

    boolean checkForSpace(Material material, Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                if (contents[i].getType().equals(material) && contents[i].getAmount() < material.getMaxStackSize())
                    return true;

            }
        }
        return false;
    }

    static private void generateConfig() {
        File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
        File folderFile = new File(plugin.getDataFolder().toString());
        if (!file.exists()) {
            if (!folderFile.isDirectory()) folderFile.mkdir();
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

    static private void generateLang() {
        File folderFile = new File(plugin.getDataFolder().toString());
        File file = new File(plugin.getDataFolder() + File.separator + "lang.yml");
        if (!file.exists()) {
            if (!folderFile.isDirectory()) folderFile.mkdir();
            getConsoleSender().sendMessage("lang.yml file has not been found, generating a new one...");
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            for (Message message : Message.values()) {
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
        bukkitVersion = Bukkit.getBukkitVersion();
        generateConfig();
        generateLang();
        loadConfig();
        if (!dropFromOres)
            getServer().getConsoleSender().sendMessage("[" + this.getName() + "] Drop from ores is now disabled");

        //Check if version is < 1.8.9
        try {
            if (versionCompatible(16)) {
                isNetherite = true;
            }
            if (this.versionCompatible(12)) {
                golden = Material.getMaterial(Material.class.getField("GOLDEN_PICKAXE").getName());
                wooden = Material.getMaterial(Material.class.getField("WOODEN_PICKAXE").getName());
            } else {
                golden = Material.getMaterial(Material.class.getField("GOLD_PICKAXE").getName());
                wooden = Material.getMaterial(Material.class.getField("WOOD_PICKAXE").getName());
                lapis_ore  = Material.getMaterial(Material.class.getField("LAPIS_ORE").getName());

            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        getPluginManager().registerEvents(new BlockBreakEventListener(), this);
        getPluginManager().registerEvents(new InventorySelectorAdvanced(), this);
        getPluginManager().registerEvents(new InventorySelector(), this);
        new Updater(this, 339276, getFile(), Updater.UpdateType.DEFAULT, true);
        new Metrics(this);
        reloadConfig();

        loadPlayerData();
        loadChances();
        loadChestChances();
        BlockBreakEventListener.initialize();

        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] " + ChatColor.GREEN + "Configuration Loaded, Plugin enabled!");
        startStackScheduler();
        fixPlayerData();
        BlockBreakEventListener.initialize();
    }

    private void fixPlayerData() {
        getServer().getOnlinePlayers().forEach(PluginMain::newPlayerJoined);
        playerSettings.forEach((name, settings) -> {
            for (int i = 0; i < BlockBreakEventListener.set.length; i++) {
                if (!settings.containsKey(BlockBreakEventListener.set[i]))
                    settings.put(BlockBreakEventListener.set[i], new Setting(true, BlockBreakEventListener.set[i]));
            }
        });
    }

    private void startStackScheduler() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
                for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().toArray().length; i++) {
                    Player player = (Player) Bukkit.getServer().getOnlinePlayers().toArray()[i];
                    if (playerSettings.get(player.getUniqueId().toString()) != null && playerSettings.get(player.getUniqueId().toString()).get("STACK").isOn()) {
                        boolean tak = true;
                        while (tak) {
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.REDSTONE), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.REDSTONE_BLOCK, player.getInventory())))) {
                                player.getInventory().removeItem(new ItemStack(Material.REDSTONE, 9));
                                player.getInventory().addItem(new ItemStack(Material.REDSTONE_BLOCK));
                            } else tak = false;
                        }
                        if (versionCompatible(12)) {
                            tak = true;
                            while (tak) {
                                try {
                                    if (player.getInventory().containsAtLeast(new ItemStack(Objects.requireNonNull(Material.getMaterial(Material.class.getField("LAPIS_LAZULI").getName()))), 9)
                                            && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.LAPIS_BLOCK, player.getInventory())))) {
                                        player.getInventory().removeItem(new ItemStack(Objects.requireNonNull(Material.getMaterial(Material.class.getField("LAPIS_LAZULI").getName())), 9));
                                        player.getInventory().addItem(new ItemStack(Material.LAPIS_BLOCK));

                                    } else tak = false;
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        tak = true;
                        while (tak) {
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.COAL), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.COAL_BLOCK, player.getInventory())))) {
                                player.getInventory().removeItem(new ItemStack(Material.COAL, 9));
                                player.getInventory().addItem(new ItemStack(Material.COAL_BLOCK));

                            } else tak = false;
                        }
                        tak = true;
                        while (tak) {
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.IRON_BLOCK, player.getInventory())))) {
                                player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, 9));
                                player.getInventory().addItem(new ItemStack(Material.IRON_BLOCK));

                            } else tak = false;
                        }
                        tak = true;
                        while (tak) {
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.DIAMOND_BLOCK, player.getInventory())))) {
                                player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 9));
                                player.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK));

                            } else tak = false;
                        }
                        tak = true;
                        while (tak) {
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.GOLD_INGOT), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.GOLD_BLOCK, player.getInventory())))) {
                                player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, 9));
                                player.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK));

                            } else tak = false;
                        }
                        tak = true;
                        while (tak) {
                            if (player.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), 9)
                                    && (player.getInventory().firstEmpty() != -1 || (player.getInventory().firstEmpty() == -1 && checkForSpace(Material.EMERALD_BLOCK, player.getInventory())))) {
                                player.getInventory().removeItem(new ItemStack(Material.EMERALD, 9));
                                player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK));

                            } else tak = false;
                        }
                    }
                }
            }
        }, 40L, 80L);
    }

    private void loadPlayerData() {
        playerSettings = new LinkedHashMap<>();
        playerData = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "playerData.yml"));
        final ConfigurationSection cs = playerData.getConfigurationSection("users");
        if (cs != null) {
            Set<String> keyList = cs.getKeys(false);
            keyList.forEach((user) -> {
                ConfigurationSection materialsSection = cs.getConfigurationSection(user);
                LinkedHashMap<String, Setting> settings = new LinkedHashMap<>();
                for (int i = 0; i < Objects.requireNonNull(materialsSection).getKeys(false).toArray().length; i++) {
                    String materialName = (String) materialsSection.getKeys(false).toArray()[i];
                    boolean setting = (boolean) materialsSection.get(materialName);
                    settings.put(materialName, new Setting(setting, materialName));
                }
                playerSettings.put(user, settings);

            });
        }
        final ConfigurationSection cs2 = playerData.getConfigurationSection("userVersion");
        if (cs2 != null) {
            Set<String> keyList = cs2.getKeys(false);
            keyList.forEach((user) -> {
                String version = cs2.get(user).toString();
                if (version != null) playerLastVersionPluginVersion.put(user, version);

            });
        }
    }

    private void loadConfig() {
        getLogger().info("Loading config...");
        langData = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang.yml"));
        langData.getKeys(false).forEach(key -> {
            Message.valueOf(key).setDefaultMessage((String) langData.get(key));
        });
        ConfigurationSection cs = getConfig().getConfigurationSection("executeCommands");
        Set<String> keys = null;
        if (cs != null) {
            keys = cs.getKeys(false);
            keys.forEach(s -> {
                ConfigurationSection commandSection = cs.getConfigurationSection(s);
                if (commandSection != null){
                    float chance = (float) commandSection.getDouble("chance");
                    boolean reqPer = commandSection.getBoolean("requires-permission");
                    ExecuteCommands commands1 = new ExecuteCommands(s, chance, reqPer);
                    commands.add(commands1);
                    getLogger().log(Level.INFO, commands1.toString());
                }
                else {
                    getLogger().log(Level.WARNING, "You've typed something wrong in config.yml (executeCommands section).");
                }
            });
        }
        //Check if plugin should drop items into inventory
        dropIntoInventory = getConfig().getBoolean("drop-to-inventory");
        //Check if plugin should block item dropping from ores
        dropFromOres = getConfig().getBoolean("ore-drop");
        if(dropFromOres==false){
            dropOresWhiteList = new ArrayList<>();
            getConfig().getStringList("ores-whitelist").forEach(white_ore -> {
                dropOresWhiteList.add(Material.getMaterial(white_ore));
            });
        }
        displayUpdateMessage = getConfig().getBoolean("display-update-message");
        dropExpOrb = getConfig().getBoolean("drop-exp-orb");
        treasureChestBroadcast = getConfig().getBoolean("treasure-broadcast");
        oreDropChance = getConfig().getDouble("ore-drop-chance");
        volume = getConfig().getDouble("volume");
        experienceToDrop = (float) ((double) getConfig().get("experience"));
        disabledWorlds = new ArrayList<>(getConfig().getStringList("disabled-worlds"));
        dropChestToInv = getConfig().getBoolean("drop-chest-to-inventory-global");
        realisticDrop = getConfig().getBoolean("realistic-drop");
    }

    private void loadChestChances() {
        chestContent = new HashMap<>();
        chestSpawnRate = Double.parseDouble(getConfig().get("chest-spawn-chance").toString());
        Set<String> config = getConfig().getConfigurationSection("chest").getKeys(false);
        for (String k : config) {
            Material material = Material.getMaterial(k);
            if (material != null) {
                try {
                    HashMap<String, Integer> enchants = (HashMap) getConfig().getConfigurationSection("chest." + k + ".enchant").getValues(false);
                    if (enchants != null) {
                        chestContent.put(material, new ChestItemsInfo(getConfig().getConfigurationSection("chest." + k).getDouble("chance"), getConfig().getConfigurationSection("chest." + k).getInt("min"), getConfig().getConfigurationSection("chest." + k).getInt("max"), enchants));
                    }
                } catch (NullPointerException e) {
                    chestContent.put(material, new ChestItemsInfo(getConfig().getConfigurationSection("chest." + k).getDouble("chance"), getConfig().getConfigurationSection("chest." + k).getInt("min"), getConfig().getConfigurationSection("chest." + k).getInt("max")));
                }
            }
        }
    }

    private void loadChances() {
        getLogger().info("Loading chances...");
        dropChances = new LinkedHashMap<>();
        dropBlocks = new ArrayList<>();
        getConfig().getStringList("dropBlocks").forEach(material -> {
            dropBlocks.add(Material.getMaterial(material));
        });
        for (String key : getConfig().getConfigurationSection("chances").getKeys(false)) {
            ConfigurationSection oreObject = getConfig().getConfigurationSection("chances." + key);
            DropChance oreObjectOptions = new DropChance();
            oreObjectOptions.setName(key);
            if (oreObject == null) continue;
            if (oreObject.contains("biomes")){
                List<String> biomes = oreObject.getStringList("biomes");
                oreObjectOptions.setAcceptedBiomes(biomes);
            }
            for (String fortuneLevel : Objects.requireNonNull(oreObject).getKeys(false)) {
                if (fortuneLevel.split("-")[0].equals("fortune")) {
                    int level = Integer.parseInt(fortuneLevel.split(("-"))[1]);
                    double chance = oreObject.getConfigurationSection(fortuneLevel).getDouble("chance");
                    int min = oreObject.getConfigurationSection(fortuneLevel).getInt("min-amount");
                    int max = oreObject.getConfigurationSection(fortuneLevel).getInt("max-amount");
                    oreObjectOptions.setFortuneChance(level,chance);
                    oreObjectOptions.setFortuneItemsAmountMin(level,min);
                    oreObjectOptions.setFortuneItemsAmountMax(level,max);
                } else if (fortuneLevel.split("-")[0].equals("silk_touch")) {
                    int level = Integer.parseInt(fortuneLevel.split(("-"))[1]);
                    double chance = oreObject.getConfigurationSection(fortuneLevel).getDouble("chance");
                    int min = oreObject.getConfigurationSection(fortuneLevel).getInt("min-amount");
                    int max = oreObject.getConfigurationSection(fortuneLevel).getInt("max-amount");
                    oreObjectOptions.setSilkCahnce(level, chance);
                    oreObjectOptions.setSilkMinDrop(level, min);
                    oreObjectOptions.setSilkMaxDrop(level, max);
                }

            }
            try {
                HashMap<String, Integer> enchants = (HashMap) getConfig().getConfigurationSection("chances").getConfigurationSection(key + ".enchant").getValues(false);
                if (enchants.size() != 0) {
                    oreObjectOptions.setEnchant(enchants);
                }
            } catch (NullPointerException ignored) {
            }
            try {
                int minLevel = -1, maxLevel = -1;
                if (getConfig().getConfigurationSection("chances").getConfigurationSection(key).isSet("minLevel"))
                    minLevel = getConfig().getConfigurationSection("chances").getConfigurationSection(key).getInt("minLevel");
                if (getConfig().getConfigurationSection("chances").getConfigurationSection(key).isSet("maxLevel"))
                    maxLevel = getConfig().getConfigurationSection("chances").getConfigurationSection(key).getInt("maxLevel");
                if (minLevel == maxLevel && minLevel == -1) throw new NullPointerException();
                oreObjectOptions.setMinLevel(minLevel);
                oreObjectOptions.setMaxLevel(maxLevel);
            } catch (NullPointerException ignored) {
            }
            try {
                String text;
                if (getConfig().getConfigurationSection("chances").getConfigurationSection(key).isSet("customName")) {
                    text = getConfig().getConfigurationSection("chances").getConfigurationSection(key).getString("customName");
                    String customName = ChatColor.translateAlternateColorCodes('&', text);
                    oreObjectOptions.setCustomName(customName);
                }

            } catch (NullPointerException ignored) {
            }
            dropChances.put(oreObjectOptions.getName(), oreObjectOptions);
        }
        BlockBreakEventListener.set = new String[dropChances.keySet().toArray().length];

        for (int i = 0; i < dropChances.keySet().toArray().length; i++) {
            BlockBreakEventListener.set[i] = (String) dropChances.keySet().toArray()[i];
        }
    }


    public static void newPlayerJoined(Player player){
        String uid = player.getUniqueId().toString();
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] Creating new player data...");
        if (!playerSettings.containsKey(uid)) {
            LinkedHashMap<String, Setting> settings = new LinkedHashMap<>();
            for (int i = 0; i < BlockBreakEventListener.set.length; i++) {
                settings.put(BlockBreakEventListener.set[i], new Setting(true, BlockBreakEventListener.set[i]));
            }
            settings.put("COBBLE", new Setting(false, "COBBLE"));
            settings.put("STACK", new Setting(false, "STACK"));
            playerSettings.put(uid, settings);
        }
        if (!playerLastVersionPluginVersion.containsKey(uid)) {
            playerLastVersionPluginVersion.put(uid, currentPluginVersion);
            if (displayUpdateMessage){
                displayUpdateMessage(player);
            }
        }

    }

    public static void displayUpdateMessage(Player player) {
        Scanner reader;
        InputStream inputStream= plugin.getResource("update.txt");
        reader = new Scanner(inputStream, "utf-8");
        while (reader.hasNextLine()){
            String message = ChatColor.translateAlternateColorCodes('&', reader.nextLine());
            player.sendMessage(message);

        }

    }
    public ItemStack getItemStack(String itemName, int dropAmount)  {
            if(!this.versionCompatible(12)){
                if(itemName.contains("LAPIS_LAZULI")){

                    return new Dye(DyeColor.BLUE).toItemStack(dropAmount);
                }
                else if(itemName.contains("LAPIS_ORE")){
                    return new ItemStack(PluginMain.lapis_ore,dropAmount);
                }
                else if(itemName.contains("COBBLE")){
                    return new ItemStack(Material.COBBLESTONE,1);
                }
                else if(itemName.contains("STACK")){
                    return new ItemStack(Material.BOOK,1);
                }

            }
            return new ItemStack(Material.getMaterial(itemName),dropAmount);

    }
}
