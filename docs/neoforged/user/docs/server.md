---
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Installing a NeoForge Server

_This article assumes that you [have the correct version of Java installed][java]._

## Installing

<Tabs defaultValue="unix">
  <TabItem value="unix" label="UNIX-like (Linux, macOS, FreeBSD, etc.)">
Running a NeoForge server on Linux, macOS, BSD, or any UNIX-like OS assumes your ability to use basic terminal commands.


- Navigate to the folder you'd like to install the server into.
- Download the installer `.jar` from the Maven using the following command (swap out version numbers and `-beta` labels as needed):
<Tabs defaultValue="linux">
  <TabItem value="linux" label="GNU/Linux">
```shell
wget https://maven.neoforged.net/releases/net/neoforged/neoforge/21.4.111-beta/neoforge-21.4.111-beta-installer.jar
```
</TabItem>
<TabItem value="mac" label="macOS">
```shell
curl -LO https://maven.neoforged.net/releases/net/neoforged/neoforge/21.4.111-beta/neoforge-21.4.111-beta-installer.jar
```
</TabItem>
<TabItem value="freebsd" label="FreeBSD">
```shell
fetch https://maven.neoforged.net/releases/net/neoforged/neoforge/21.4.111-beta/neoforge-21.4.111-beta-installer.jar
```
</TabItem>
<TabItem value="openbsd" label="OpenBSD">
```shell
ftp https://maven.neoforged.net/releases/net/neoforged/neoforge/21.4.111-beta/neoforge-21.4.111-beta-installer.jar
```
</TabItem>
</Tabs>
- Run the installer using `java -jar /path/to/neoforge-installer.jar --installServer`.
- (Optional) Modify the amount of RAM provided to the server, and potentially other JVM arguments, in the newly-created `user_jvm_args.txt` file. See the comments in the file for more information.
- Start the server using `./run.sh`. It will shut down on the first run.
- Open the `eula.txt` file and change `eula=false` to `eula=true`. By doing so, you agree to abide by the [Minecraft EULA][eula] while operating the server.
- Start the server again using `./run.sh`.

Your server should now be operational. You may have to do some environment setup, such as opening the Minecraft port (defaults to 25565) in your system's and/or your network's firewall. If you plan to have the server run 24/7, you should also schedule a cronjob or similar for periodic restarts.
  </TabItem>
  <TabItem value="windows" label="Windows">
Running a NeoForge server on Windows assumes your ability to use basic terminal commands.

- Navigate to the folder you'd like to install the server into.
- Download the installer `.jar` from the Maven using `curl` (swap out version numbers and `-beta` labels as needed):
```shell
curl -O https://maven.neoforged.net/releases/net/neoforged/neoforge/21.4.111-beta/neoforge-21.4.111-beta-installer.jar
```
- Run the installer using `java -jar /path/to/neoforge-installer.jar --installServer`.
- (Optional) Modify the amount of RAM provided to the server, and potentially other JVM arguments, in the newly-created `user_jvm_args.txt` file. See the comments in the file for more information.
- Start the server using `.\run.bat`. It will shut down on the first run.
- Open the `eula.txt` file and change `eula=false` to `eula=true`. By doing so, you agree to abide by the [Minecraft EULA][eula] while operating the server.
- Start the server again using `.\run.bat`.

Your server should now be operational. You may have to do some environment setup, such as opening the Minecraft port (defaults to 25565) in your system's and/or your network's firewall. If you plan to have the server run 24/7, you should also schedule a task in the Windows Task Scheduler for periodic restarts.
  </TabItem>
</Tabs>

## Adding Mods

After starting the game once, a `mods` folder will have appeared. This is where you want to put your mod files.

Mod files should only be downloaded from trustworthy sources. We generally recommend you get your mods on [CurseForge][curseforge] or [Modrinth][modrinth].

## Updating

To update the server's NeoForge version, simply download and run the installer for the new version the same way you would with the old one. The installer will automatically replace uses of the old version where needed.

:::danger
Always backup your world before updating NeoForge or mods!
:::

## Installing Modpacks

Installing pre-made modpacks often requires some additional setup on servers. Since modpacks are a [third-party launcher][launchers] feature, one such launcher is required for installing a modpack on a server.

- Install a server, as described above.
- In your launcher, if the modpack offers a "server pack", install a separate instance of the server pack. Otherwise, install a separate instance of the normal pack.
- Move all contents of the newly-installed instance into the server's game folder.
- (Optional) Remove client-side mods from the server.
  - What constitutes as a client-side mod is not always clear, and often requires trial and error. Common things client-side mods do are visual things, e.g. enable shaders or additional resource pack features.
  - If you have installed a server pack, this should have been done for you.
- Start the server using `./run.sh` (Linux) or `.\run.bat` (Windows).

## See Also

- [Setting up a Java Edition server][wiki1] and [Server maintenance][wiki2] on the [Minecraft Wiki][wiki] - note that those articles talk about setting up a vanilla Minecraft server, not a modded one

[curseforge]: https://www.curseforge.com/minecraft/search?class=mc-mods
[eula]: https://www.minecraft.net/en-us/eula
[java]: index.md#java
[launchers]: launchers.md
[modrinth]: https://modrinth.com/mods
[wiki]: https://minecraft.wiki/
[wiki1]: https://minecraft.wiki/w/Tutorial:Setting_up_a_Java_Edition_server
[wiki2]: https://minecraft.wiki/w/Tutorial:Server_maintenance
