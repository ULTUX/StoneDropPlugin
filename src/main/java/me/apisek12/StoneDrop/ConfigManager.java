package me.apisek12.StoneDrop;

import me.apisek12.StoneDrop.DataModels.ChestItemsInfo;
import me.apisek12.StoneDrop.DataModels.DropChance;
import me.apisek12.StoneDrop.DataModels.ExecuteCommands;
import me.apisek12.StoneDrop.DataModels.Setting;
import me.apisek12.StoneDrop.Enums.Message;
import me.apisek12.StoneDrop.EventListeners.BlockListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import static org.bukkit.Bukkit.*;

public class ConfigManager {
    PluginMain parentPlugin;

    public ConfigManager(PluginMain parentPlugin) {
        this.parentPlugin = parentPlugin;
    }

    public void loadConfig() {
        generateConfig();
        generateLang();
        if (!PluginMain.dropFromOres)
            getServer().getConsoleSender().sendMessage("[" + parentPlugin.getName() + "] Drop from ores is now disabled");
        loadFromConfig();
        loadPlayerData();
        loadChances();
        loadChestChances();
        BlockListener.initialize();

        fixPlayerData();
        setGlobalSettings();
        BlockListener.initialize();
    }

    public void unloadConfig() {
        PluginMain.playerSettings.forEach((player, settings) -> settings.forEach((material, setting) -> {
            PluginMain.playerData.set("users." + player + "." + material, setting.isOn());
        }));

        PluginMain.playerLastVersionPluginVersion.forEach((user, version) -> {
            PluginMain.playerData.set("userVersion." + user, version);
        });

        try {
            PluginMain.playerData.save(new File(parentPlugin.getDataFolder(), "playerData.yml"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[StoneDrop] Config file saved!");

        } catch (IOException e) {
            getServer().getConsoleSender().sendMessage("[StoneDrop] Player data file not found, creating a new one...");
            try {
                PluginMain.playerData.save(new File(parentPlugin.getDataFolder(), "playerData.yml"));
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[StoneDrop] Config file saved!");
            } catch (IOException ex) {
                getServer().getConsoleSender().sendMessage("[StoneDrop] Could not create player data file!");

            }
        }
    }

    public void reloadConfig() {
        parentPlugin.reloadConfig();
        generateConfig();
        generateLang();
        loadFromConfig();
        loadChances();
        loadChestChances();
        loadPlayerData();
        fixPlayerData();
    }

    private void setGlobalSettings() {
        PluginMain.playerSettings.get("9999-9999").forEach((s, setting) -> {
            if (PluginMain.dropChances.containsKey(s)) PluginMain.dropChances.get(s).setEnabled(setting.isOn());
        });
    }

    private void generateConfig() {
        File file = new File(parentPlugin.getDataFolder() + File.separator + "config.yml");
        File folderFile = new File(parentPlugin.getDataFolder().toString());
        if (!file.exists()) {
            if (!folderFile.isDirectory()) folderFile.mkdir();
            try (OutputStream outputStream = new FileOutputStream(file.toPath().toString())) {
                InputStream is = parentPlugin.getResource("config.yml");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateLang() {
        File folderFile = new File(parentPlugin.getDataFolder().toString());
        File file = new File(parentPlugin.getDataFolder() + File.separator + "lang.yml");
        if (!file.exists()) {
            if (!folderFile.isDirectory()) folderFile.mkdir();
            getConsoleSender().sendMessage("lang.yml file has not been found, generating a new one...");

            // Copy all comments from template lang.yml
            try (OutputStream outputStream = new FileOutputStream(file.toPath().toString())) {
                InputStream is = parentPlugin.getResource("lang.yml");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Save messages to yml file
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

    private void loadPlayerData() {
        PluginMain.playerSettings = new LinkedHashMap<>();
        parentPlugin.playerData = YamlConfiguration.loadConfiguration(new File(parentPlugin.getDataFolder(), "playerData.yml"));
        final ConfigurationSection cs = PluginMain.playerData.getConfigurationSection("users");
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
                PluginMain.playerSettings.put(user, settings);

            });
        }
        final ConfigurationSection cs2 = PluginMain.playerData.getConfigurationSection("userVersion");
        if (cs2 != null) {
            Set<String> keyList = cs2.getKeys(false);
            keyList.forEach((user) -> {
                String version = cs2.get(user).toString();
                if (version != null) PluginMain.playerLastVersionPluginVersion.put(user, version);

            });
        }
    }

    private void loadChances() {
        getLogger().info("Loading chances...");
        PluginMain.dropChances = new LinkedHashMap<>();
        PluginMain.dropBlocks = new ArrayList<>();
        parentPlugin.getConfig().getStringList("dropBlocks").forEach(material -> {
            PluginMain.dropBlocks.add(Material.getMaterial(material));
        });
        for (String key : parentPlugin.getConfig().getConfigurationSection("chances").getKeys(false)) {
            ConfigurationSection oreObject = parentPlugin.getConfig().getConfigurationSection("chances." + key);
            DropChance oreObjectOptions = new DropChance();
            if (Material.getMaterial(key) == null) {
                getLogger().warning("Possible mistype of material "+key+" in config file. Please check config file for errors.");
                continue;
            }
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
                HashMap<String, Integer> enchants = (HashMap) parentPlugin.getConfig().getConfigurationSection("chances").getConfigurationSection(key + ".enchant").getValues(false);
                if (enchants.size() != 0) {
                    oreObjectOptions.setEnchant(enchants);
                }
            } catch (NullPointerException ignored) {
            }
            try {
                int minLevel = -1, maxLevel = -1;
                if (parentPlugin.getConfig().getConfigurationSection("chances").getConfigurationSection(key).isSet("minLevel"))
                    minLevel = parentPlugin.getConfig().getConfigurationSection("chances").getConfigurationSection(key).getInt("minLevel");
                if (parentPlugin.getConfig().getConfigurationSection("chances").getConfigurationSection(key).isSet("maxLevel"))
                    maxLevel = parentPlugin.getConfig().getConfigurationSection("chances").getConfigurationSection(key).getInt("maxLevel");
                if (minLevel == maxLevel && minLevel == -1) throw new NullPointerException();
                oreObjectOptions.setMinLevel(minLevel);
                oreObjectOptions.setMaxLevel(maxLevel);
            } catch (NullPointerException ignored) {
            }
            try {
                String text;
                if (parentPlugin.getConfig().getConfigurationSection("chances").getConfigurationSection(key).isSet("customName")) {
                    text = parentPlugin.getConfig().getConfigurationSection("chances").getConfigurationSection(key).getString("customName");
                    String customName = ChatColor.translateAlternateColorCodes('&', text);
                    oreObjectOptions.setCustomName(customName);
                }

            } catch (NullPointerException ignored) {
            }
            PluginMain.dropChances.put(oreObjectOptions.getName(), oreObjectOptions);
        }
        BlockListener.set = new String[PluginMain.dropChances.keySet().toArray().length];

        for (int i = 0; i < PluginMain.dropChances.keySet().toArray().length; i++) {
            BlockListener.set[i] = (String) PluginMain.dropChances.keySet().toArray()[i];
        }
    }

    private void fixPlayerData() {
        getServer().getOnlinePlayers().forEach(ConfigManager::generateNewPlayerData);
        PluginMain.playerSettings.forEach((name, settings) -> {
            for (int i = 0; i < BlockListener.set.length; i++) {
                if (!settings.containsKey(BlockListener.set[i])) {
                    settings.put(BlockListener.set[i], new Setting(true, BlockListener.set[i]));
                }
            }
        });
        if (!PluginMain.playerSettings.containsKey("9999-9999")) { //Global config
            generateNewPlayerData("9999-9999");
        }
    }
    private void loadChestChances() {
        PluginMain.chestContent = new HashMap<>();
        PluginMain.chestSpawnRate = Double.parseDouble(parentPlugin.getConfig().get("chest-spawn-chance").toString());
        Set<String> config = parentPlugin.getConfig().getConfigurationSection("chest").getKeys(false);
        for (String k : config) {
            Material material = Material.getMaterial(k);
            if (material != null) {
                try {
                    HashMap<String, Integer> enchants = (HashMap) parentPlugin.getConfig().getConfigurationSection("chest." + k + ".enchant").getValues(false);
                    if (enchants != null) {
                        PluginMain.chestContent.put(material, new ChestItemsInfo(parentPlugin.getConfig().getConfigurationSection("chest." + k).getDouble("chance"),
                                parentPlugin.getConfig().getConfigurationSection("chest." + k).getInt("min"),
                                parentPlugin.getConfig().getConfigurationSection("chest." + k).getInt("max"), enchants));
                    }
                } catch (NullPointerException e) {
                    PluginMain.chestContent.put(material, new ChestItemsInfo(parentPlugin.getConfig().getConfigurationSection("chest." + k).getDouble("chance"),
                            parentPlugin.getConfig().getConfigurationSection("chest." + k).getInt("min"),
                            parentPlugin.getConfig().getConfigurationSection("chest." + k).getInt("max")));
                }
            }
        }
    }
    private void loadFromConfig() {
        getLogger().info("Loading config...");
        PluginMain.langData = YamlConfiguration.loadConfiguration(new File(parentPlugin.getDataFolder(), "lang.yml"));
        PluginMain.langData.getKeys(false).forEach(key -> {
            Message.valueOf(key).setDefaultMessage((String) PluginMain.langData.get(key));
        });
        ConfigurationSection cs = parentPlugin.getConfig().getConfigurationSection("executeCommands");
        Set<String> keys = null;
        PluginMain.commands = new ArrayList<>();
        if (cs != null) {
            keys = cs.getKeys(false);
            keys.forEach(s -> {
                ConfigurationSection commandSection = cs.getConfigurationSection(s);
                if (commandSection != null){
                    float chance = (float) commandSection.getDouble("chance");
                    boolean reqPer = commandSection.getBoolean("requires-permission");
                    ExecuteCommands commands1 = new ExecuteCommands(s, chance, reqPer);
                    PluginMain.commands.add(commands1);
                }
                else {
                    getLogger().log(Level.WARNING, "You've typed something wrong in config.yml (executeCommands section).");
                }
            });
        }
        //Check if plugin should drop items into inventory
        PluginMain.dropIntoInventory = parentPlugin.getConfig().getBoolean("drop-to-inventory");
        //Check if plugin should block item dropping from ores
        PluginMain.dropFromOres = parentPlugin.getConfig().getBoolean("ore-drop");
        if(!PluginMain.dropFromOres){
            PluginMain.dropOresWhiteList = new ArrayList<>();
            parentPlugin.getConfig().getStringList("ores-whitelist").forEach(white_ore -> {
                PluginMain.dropOresWhiteList.add(Material.getMaterial(white_ore));
            });
        }
        PluginMain.displayUpdateMessage = parentPlugin.getConfig().getBoolean("display-update-message");
        PluginMain.dropExpOrb = parentPlugin.getConfig().getBoolean("drop-exp-orb");
        PluginMain.treasureChestBroadcast = parentPlugin.getConfig().getBoolean("treasure-broadcast");
        PluginMain.oreDropChance = parentPlugin.getConfig().getDouble("ore-drop-chance");
        PluginMain.volume = parentPlugin.getConfig().getDouble("volume");
        PluginMain.experienceToDrop = (float) ((double) parentPlugin.getConfig().get("experience"));
        PluginMain.disabledWorlds = new ArrayList<>(parentPlugin.getConfig().getStringList("disabled-worlds"));
        PluginMain.dropChestToInv = parentPlugin.getConfig().getBoolean("drop-chest-to-inventory-global");
        PluginMain.realisticDrop = parentPlugin.getConfig().getBoolean("realistic-drop");
    }

    public static void generateNewPlayerData(String uid){
        getServer().getConsoleSender().sendMessage("[StoneDrop] Creating new player data...");
        if (!PluginMain.playerSettings.containsKey(uid)) {
            LinkedHashMap<String, Setting> settings = new LinkedHashMap<>();
            for (int i = 0; i < BlockListener.set.length; i++) {
                settings.put(BlockListener.set[i], new Setting(true, BlockListener.set[i]));
            }
            settings.put("COBBLE", new Setting(false, "COBBLE"));
            PluginMain.playerSettings.put(uid, settings);
        }
        if (!PluginMain.playerLastVersionPluginVersion.containsKey(uid)) {
            PluginMain.playerLastVersionPluginVersion.put(uid, PluginMain.currentPluginVersion);
        }
    }

    public static void generateNewPlayerData(Player player){
        String uid = player.getUniqueId().toString();
        generateNewPlayerData(uid);
    }
}
