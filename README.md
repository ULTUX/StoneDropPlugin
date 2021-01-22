![Stone Drop Plugin Logo (Image by Ponanoix)](.github/banner.png)

![](https://img.shields.io/github/v/release/ULTUX/StoneDropPlugin)
![](https://img.shields.io/github/issues/ULTUX/StoneDropPlugin)
![](https://img.shields.io/github/license/ULTUX/StoneDropPlugin)
![](https://img.shields.io/github/release-date/ULTUX/StoneDropPlugin)
![Build](https://github.com/ULTUX/StoneDropPlugin/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master)

## What is Stone Drop?


This plugin gives a player the ability to drop some additional items while mining stone.\
What is more, it also features mystery chests, which can appear in front of player while mining.\
Everything can be configured and changed to meet server requirements.

## Need help, have some ideas or have found a bug? Visit our Discord Server.
[ ![](https://i.imgur.com/lUUtxLdl.jpg) ](https://discord.gg/4gsa9wCCST)

## Requirements


-   **Stone Drop** requires Java version 8 or higher, most likely already installed.
-   **Stone Drop** has been tested on Minecraft version **1.16.4** although it may work with previous versions (such as **1.12**)
-   And you probably know that because this is a plugin, **Stone Drop** requires *Bukkit, Spigot or Paper* as a server engine

## Features


-   The plugin makes mining stone way more fun.
-   All the items that are specified can be dropped with specified enchantment.
-   Player can get a treasure chest spawned while mining stone.
-   Treasure chest contents and chances of being spawned are specified in the config file.
-   Player can prevent specified items from dropping using the command.
-   Players settings are saved into the config file.
-   Every player manage their drop preferences quickly and easily by right-clicking on an item in the menu
-   Left-clicking on the items shows drop chances for every fortune level
## Recommendations

-   Server running **PaperMC**
-   *Java* version *8*
-   *Minecraft* version **1.16.4**

## How to install


1.  **Download** plugin from **[here.](https://github.com/ULTUX/StoneDropPlugin/releases)**
2.  Place the file in **plugins** folder inside your server directory.
3.  Start the server *(if it was already running reload it with **/reload** command).*
4.  You have successfully **downloaded and installed** the plugin!

## Commands


/drop (or /d) - this is a base command - shows a menu\
/drop stack - toggle (for command sender) automatic crafting of diamonds, gold, etc. into their block equivalents.\
/drop cobble - toggle (for command sender) drop of cobblestone from stone.\
/drop - toggle (for command sender) drop of specific material from stone.\
/whatdrops - displays a list of items that can drop, their chances and amounts.\
/shutdown - op/console-only command. It schedules a server shut down and informs all players about it.\
/cancelshutdown - op/console-only command. Cancels shut down if it's been initialized.

## How to edit config.yml file?


### It is all explained on my github page: **[StoneDrop Wiki](https://github.com/ULTUX/StoneDropPlugin/wiki/Config-file)**

## Example Images:


![](https://camo.githubusercontent.com/946825fe14ce81ad8e796658cfc952be26adfa7238e8bbc3b9c23d85486cd8c4/68747470733a2f2f692e696d6775722e636f6d2f41624d737736752e706e67) \
![](https://camo.githubusercontent.com/638a63c374a7b461bae422892c248bca7872ae6aadb9c653519f653d0c2107bf/68747470733a2f2f692e696d6775722e636f6d2f454b4d6c38384d2e706e67)

## Permissions


-   `stonedrop.drop` - Allow players to use `/drop` command.
-   `stonedrop.whatdrops` - Allow players to use `/whatdrops` command.

## Latest releases: [Releases](https://github.com/ULTUX/minecraft-stone-drop-plugin/releases/)
