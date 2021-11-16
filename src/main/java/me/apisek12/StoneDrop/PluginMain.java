package me.apisek12.StoneDrop;

import me.apisek12.StoneDrop.Apis.Updater;
import me.apisek12.StoneDrop.DataModels.ChestItemsInfo;
import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.DataModels.ExecuteCommands;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.Apis.Metrics;
import me.apisek12.StoneDrop.EventListeners.BlockListener;
import me.apisek12.StoneDrop.EventListeners.EntityListener;
import me.apisek12.StoneDrop.EventListeners.InventoryListener;
import me.apisek12.StoneDrop.InventorySelectors.AdminPanel;
import me.apisek12.StoneDrop.InventorySelectors.InventorySelector;
import me.apisek12.StoneDrop.InventorySelectors.InventorySelectorAdvanced;
import me.apisek12.StoneDrop.Utils.McMMOUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import static org.bukkit.Bukkit.getPluginManager;

public class PluginMain extends JavaPlugin {
    public static PluginMain plugin = null;
    public static LinkedHashMap<String, LinkedHashMap<String, Setting>> playerSettings;
    public static LinkedHashMap<String, DropChance> dropChances;
    public static HashMap<Material, ChestItemsInfo> chestContent;
    public static float experienceToDrop;
    public static double chestSpawnRate = 0;
    public static ArrayList<String> disabledWorlds = null;
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
    public static ArrayList<ExecuteCommands> commands;
    public static boolean dropChestToInv = false;
    public static boolean realisticDrop = false;
    public static boolean restrictedSilkTouch = false;
    public static boolean mcmmoSupport = false;
    public static String bukkitVersion;
    public static ArrayList<Inventory> openedChests = new ArrayList<>();
    public static ArrayList<Location> chestLocations = new ArrayList<>();
    static FileConfiguration playerData = null;
    static FileConfiguration langData = null;
    private ConfigManager configManager;

    /**
     * Checks if plugin version is compatible with given version number. Value is a second number in version string (in 1.16.6 it is 16).
     *
     * @param val A version to compare with.
     * @return true if plugin version is greater or equal than given value.
     */
    public static boolean versionCompatible(int val) {
        String[] version = Bukkit.getBukkitVersion().replace(".", ",").replace("-", ",").split(",");
        return Integer.parseInt(version[1]) > val;
    }

    @Override
    public void onEnable() {
        plugin = this;
        currentPluginVersion = getDescription().getVersion();
        bukkitVersion = Bukkit.getBukkitVersion();
        configManager = new ConfigManager(this);


        try {
            //Check if version is > 1.12
            if (versionCompatible(12)) {
                golden = Material.getMaterial(Material.class.getField("GOLDEN_PICKAXE").getName());
                wooden = Material.getMaterial(Material.class.getField("WOODEN_PICKAXE").getName());
            } else {
                golden = Material.getMaterial(Material.class.getField("GOLD_PICKAXE").getName());
                wooden = Material.getMaterial(Material.class.getField("WOOD_PICKAXE").getName());
                lapis_ore = Material.getMaterial(Material.class.getField("LAPIS_ORE").getName());

            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        if (versionCompatible(16)) {
            isNetherite = true;
        }

        registerEvents();
        configManager.loadConfig();


        new Updater(this, 339276, getFile(), Updater.UpdateType.DEFAULT, true);
        new Metrics(this);
        getServer().getConsoleSender().sendMessage("[StoneDrop] " + ChatColor.GREEN + "Configuration Loaded, Plugin enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[StoneDrop] " + ChatColor.GRAY + "Saving config data...");
        configManager.unloadConfig();
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] " + ChatColor.DARK_RED + "Plugin disabled!");
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("drop") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("stonedrop.reload")) {
                sender.sendMessage(ChatColor.GRAY + "Starting reload...");
                sender.sendMessage(ChatColor.GRAY + "Unregistering all event listeners...");
                HandlerList.unregisterAll(plugin);
                sender.sendMessage(ChatColor.GRAY + "Registering new event listeners");
                registerEvents();
                BlockListener.initialize();
                sender.sendMessage(ChatColor.GRAY + "Reloading config files...");
                sender.sendMessage(ChatColor.GRAY + "Generating config files...");
                configManager.reloadConfig();
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
                    if (args.length == 0 || args[0].equalsIgnoreCase("manage")) {
                        if(player.hasPermission("stonedrop.drop.advanced")){
                            new InventorySelectorAdvanced(player, setting);
                        } else {
                            new InventorySelector(player,setting);
                        }
                        return true;

                    }
                    else if (args[0].equalsIgnoreCase("basic")){
                        new InventorySelector(player, setting);
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("version")){
                        player.sendMessage(ChatColor.AQUA+"v"+getDescription().getVersion());
                        return true;
                    }
                    else {
                        if (args[0].equalsIgnoreCase("admin")){
                            if ((player.isOp() ||  player.hasPermission("stonedrop.admin"))) {
                                AdminPanel.createAdminPanel(player);
                                return true;
                            }
                            else {
                                player.sendMessage(ChatColor.RED+Message.PERMISSION_MISSING.toString());
                                return false;
                            }
                        }
                    }
                    player.sendMessage(ChatColor.GRAY + Message.COMMAND_ARGUMENT_UNKNOWN.toString());
                } else {
                    player.sendMessage(ChatColor.RED + Message.PERMISSION_MISSING.toString());
                }
            }
        }
        return false;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("drop")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (player.hasPermission("stonedrop.drop")) {
                    if (player.hasPermission("stonedrop.drop.advanced")) {
                        list.add("basic");
                        list.add("manage");
                    }
                }
                if (player.hasPermission("stonedrop.reload")){
                    list.add("reload");

                }
                if (player.hasPermission("admin")){
                    list.add("admin");
                }
            }
        }
        return list;
    }


    private void registerEvents() {
        getPluginManager().registerEvents(new BlockListener(), this);
        getPluginManager().registerEvents(new InventorySelector(), this);
        getPluginManager().registerEvents(new InventorySelectorAdvanced(), this);
        getPluginManager().registerEvents(new AdminPanel(), this);
        getPluginManager().registerEvents(new EntityListener(), this);
        getPluginManager().registerEvents(new InventoryListener(), this);

    }
}
