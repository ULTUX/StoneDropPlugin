package me.apisek12.StoneDrop;

import me.apisek12.StoneDrop.Apis.Updater;
import me.apisek12.StoneDrop.DataModels.ChestItemsInfo;
import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.DataModels.ExecuteCommands;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.Apis.Metrics;
import me.apisek12.StoneDrop.EventListeners.BlockBreakEventListener;
import me.apisek12.StoneDrop.InventorySelectors.AdminPanel;
import me.apisek12.StoneDrop.InventorySelectors.InventorySelector;
import me.apisek12.StoneDrop.InventorySelectors.InventorySelectorAdvanced;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Dye;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.util.*;
import static org.bukkit.Bukkit.getPluginManager;

public class PluginMain extends JavaPlugin {
    public static PluginMain plugin = null;
    public static LinkedHashMap<String, LinkedHashMap<String, Setting>> playerSettings; //These are settings set by players
    public static LinkedHashMap<String, DropChance> dropChances; //These are chances set in config file String-material
    public static HashMap<Material, ChestItemsInfo> chestContent;
    public static float experienceToDrop;
    public static double chestSpawnRate = 0;
    public static ArrayList<String> disabledWorlds = null;
    static FileConfiguration playerData = null;
    static FileConfiguration langData = null;
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
    public static boolean realisticDrop = true;
    public static String bukkitVersion;
    private ConfigManager configManager;



    /**
     * Checks if plugin version is compatible with given version number. Value is a second number in version string (in 1.16.6 it is 16).
     *
     * @param val A version to compare with.
     * @return true if plugin verison is greater or equal than given value.
     */
    public static boolean versionCompatible(int val) {
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
                sender.sendMessage(ChatColor.GRAY + "Unregistering all event listeners...");
                HandlerList.unregisterAll(plugin);
                sender.sendMessage(ChatColor.GRAY + "Generating config files...");
                sender.sendMessage(ChatColor.GRAY + "Registering new event listeners");
                registerEvents();
                sender.sendMessage(ChatColor.GRAY + "Reloading config files...");
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

    boolean checkForSpace(Material material, Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (ItemStack content : contents) {
            if (content != null) {
                if (content.getType().equals(material) && content.getAmount() < material.getMaxStackSize())
                    return true;

            }
        }
        return false;
    }


    @Override
    public void onEnable() {
        plugin = this;
        currentPluginVersion = getDescription().getVersion();
        bukkitVersion = Bukkit.getBukkitVersion();

        //Check if version is < 1.8.9
        try {
            if (versionCompatible(16)) {
                isNetherite = true;
            }
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

        registerEvents();

        new Updater(this, 339276, getFile(), Updater.UpdateType.DEFAULT, true);
        new Metrics(this);
    }

    public static void generateNewPlayerData(Player player){
        String uid = player.getUniqueId().toString();
        generateNewPlayerData(uid);
    }
    public static void generateNewPlayerData(String uid){
        Bukkit.getServer().getConsoleSender().sendMessage("[StoneDrop] Creating new player data...");
        if (!playerSettings.containsKey(uid)) {
            LinkedHashMap<String, Setting> settings = new LinkedHashMap<>();
            for (int i = 0; i < BlockBreakEventListener.set.length; i++) {
                settings.put(BlockBreakEventListener.set[i], new Setting(true, BlockBreakEventListener.set[i]));
            }
            settings.put("COBBLE", new Setting(false, "COBBLE"));
            playerSettings.put(uid, settings);
        }
        if (!playerLastVersionPluginVersion.containsKey(uid)) {
            playerLastVersionPluginVersion.put(uid, currentPluginVersion);
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
            if (Material.getMaterial(itemName) == null) return null;
            return new ItemStack(Material.getMaterial(itemName),dropAmount);
    }

    private void registerEvents() {
        getPluginManager().registerEvents(new BlockBreakEventListener(), this);
        getPluginManager().registerEvents(new InventorySelector(), this);
        getPluginManager().registerEvents(new InventorySelectorAdvanced(), this);
        getPluginManager().registerEvents(new AdminPanel(), this);
    }
}
