![Stone Drop Plugin Logo (Image by Ponanoix)](https://cdn.discordapp.com/attachments/252074890453188608/606969243585478666/logo_pluginu.png)
 ![](https://img.shields.io/github/v/release/ULTUX/StoneDropPlugin) ![](https://img.shields.io/github/downloads/ULTUX/StoneDropPlugin/total) ![](https://img.shields.io/github/issues/ULTUX/StoneDropPlugin) ![](https://img.shields.io/github/commit-activity/master/ULTUX/StoneDropPlugin) ![](https://img.shields.io/github/license/ULTUX/StoneDropPlugin)
 
# What is Stone Drop?
This plugin gives player ability to drop some additional items while mining stone.  
What is more, it also features mystery chests, which can appear in front of player while mining.  
Everything can be configured and changed to meet server requirements.

## Requirements
  * **Stone Drop** requires Java version 8 or higher, most likely already installed.
  * **Stone Drop** has been tested on Minecraft version **1.14.4** although it may work with previous verisons (such as **1.12**)
  * And you probably know that because this is a plugin, **Stone Drop** requires _Bukkit, Spigot or Paper_ as server engine
  
## Recomendations (**_it's just been tested on this specifications_**)
  * Server running **PaperMC**
  * _Java_ version *8*
  * _Minecraft_ version **1.14.4**

## How to install
  1. **Download** plugin from **[here](https://github.com/ULTUX/StoneDropPlugin/releases)**
  2. Place the file in **plugins** folder inside your server directory
  3. Start the server _(if it was already running reload it with **/reload** command)_
  4. You have successfully **downloaded and installed** the plugin!

## Commands
/drop (or /d) - this is a base command  
/drop stack - toggle (for command sender) automatic crafting of diamonds, gold, etc. into their block equivalents.  
/drop cobble - toggle (for command sender) drop of cobblestone from stone.  
/drop <name of material> - toggle (for command sender) drop of specific material from stone.  
/whatdrops - displays list of items that can drop, their chances and ammounts.  
/shutdown <time in seconds> - this is a console-only command. It schedules a server closage and informs all players about it.  
/cancelshutdown - cancels shutdown if it has been initialized.  
 
 
## Latest releases: [Releases](https://github.com/ULTUX/minecraft-stone-drop-plugin/releases/)

### Features
  * The plugin makes mining stone way more fun
  * All the items that are specified can be dropped with specified enchantment
  * Player can get a treasure chest spawned while mining stone
  * Treasure chest contents and chances of being spawned are specified in config file
  * Player can prevent specified items from dropping using command
  * Players settings are saved into config file
