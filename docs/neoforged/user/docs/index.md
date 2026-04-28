---
sidebar_position: 0
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# NeoForge User Guide

Whether you are a regular player, pack developer or server administrator, this guide is intended to help you through the process of preparing your computer to run NeoForge, as well as answer some frequently asked questions.

The guide mainly aims to help you install NeoForge using the official Minecraft launcher provided by Mojang. There are also a number of third-party launchers that mostly automate this process for you, some of which are mentioned in the article on [third-party launchers][launchers].

## Java

In order to run NeoForge, you will first need to have Java installed on your computer. Java is the programming language Minecraft and NeoForge are written in. While Minecraft uses the launcher to download the required Java version, NeoForge requires you to install Java yourself.

The required Java version differs depending on what Minecraft version you want to run:

| Minecraft version | Java version |
|:-----------------:|:------------:|
|   1.20.2-1.20.4   |      17      |
|   1.20.5-latest   |      21      |

:::info
While NeoForge exists for Minecraft **1.20.1**, we recommend using Forge on that version instead, since it had longer support for Minecraft 1.20.1. We only recommend using NeoForge on Minecraft 1.20.2 and newer.
:::

### Testing For Java

_If you are sure that you do not have Java installed, you can skip to [Installing Java][installingjava]._

In many cases, you may already have Java installed on your system. As such, you need to verify if the version is correct.

- Open a terminal. The way to do this depends on the operating system you are running:

<Tabs defaultValue="windows">
  <TabItem value="windows" label="Windows">
In the Start Menu at the bottom left, search for `Command Prompt` and press Enter.
  </TabItem>
  <TabItem value="macos" label="MacOS">
Open Finder. In Finder, open the Applications/Utilities folder and double-click Terminal.
  </TabItem>
  <TabItem value="linux" label="Linux">
Open the terminal for your Linux distribution. Common names would be `GNOME Terminal` or `Konsole`, however it may vary depending on your exact setup.
  </TabItem>
</Tabs>

- Type the following command: `java -version` and press Enter.
- If an error is displayed, then Java is not installed, and you can skip to the [Installing Java][installingjava] section.
- If Java is installed, you should see the following output (or something similar):
```
openjdk version "21.0.4" 2024-07-16 LTS
OpenJDK Runtime Environment Temurin-21.0.4+7 (build 21.0.4+7-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.4+7 (build 21.0.4+7-LTS, mixed mode, sharing)
```
- Verify that the first number after the `version` bit matches the Java version needed for the desired Minecraft version.
  - E.g., `openjdk version "21.0.4" 2024-07-16 LTS` is a Java 21 version and as such fit for Minecraft 1.20.5 and newer.
  - If the version does not match, then you will have to [install the correct Java version][installingjava].
- If everything went smoothly, continue to [Installing NeoForge][installingneoforge].

### Installing Java

The way to install Java depends on your operating system. Always make sure you're grabbing the correct version of Java, and make sure you grab the 64-bit version, as modern versions of Minecraft do no longer support 32-bit versions of Java.

<Tabs defaultValue="windows">
  <TabItem value="windows" label="Windows">
Download the JDK `.msi` from [the Adoptium project](https://adoptium.net/temurin/releases/?version=21&os=windows). Open the `.msi` file you just downloaded in your file system, double-click it, and run through the installer.
  </TabItem>
  <TabItem value="windows_server" label="Windows (Server)">
Download the JDK using the following `winget` command (change the version number if necessary):

```
winget install -e --id=Microsoft.OpenJDK.21
```
  </TabItem>
  <TabItem value="macos" label="MacOS">
Download the JDK `.pkg` from [the Adoptium project](https://adoptium.net/temurin/releases/?version=21&os=mac). Open the `.pkg` file you just downloaded in your file system, double-click it, and run through the installer.
  </TabItem>
  <TabItem value="linux" label="Linux">
Open the terminal for your Linux distribution. Common names would be `GNOME Terminal` or `Konsole`, however it may vary depending on your exact setup.

Then, use your system's package manager (e.g. `apt` on Ubuntu and Debian, `yum` on CentOS, `dnf` on Fedora, or `pacman` on Arch) to install Java. The package's exact name may vary, but something like `openjdk-21` (swap out version number if needed) is usually a good shot.

Some distributions also provide documentation and/or additional tools for installing Java:

<ul>
  <li>[Installing Java on Arch][arch]</li>
  <li>[Installing Java on Debian][debian]</li>
  <li>[Installing Java on Fedora][fedora]</li>
  <li>[Installing Java on Ubuntu][ubuntu]</li>
</ul>

  </TabItem>
</Tabs>

After installing, it is recommended to [test for Java][testingforjava] again, just to make sure that everything went well.

## Installing NeoForge

Once you have successfully installed Java, you can now install NeoForge itself.

- If you want to play with NeoForge in singleplayer or to join a NeoForge server, please read [Installing a NeoForge Client][client].
- If you want to run a server using NeoForge, please read [Installing a NeoForge Server][server].

## Further Help

This guide covers most of the issues you may face as a user of NeoForge, however it is not intended to be exhaustive, and you may run into a problem which is not covered here.

Common problems can be found in the [Troubleshooting & FAQ][faq] article. For further support, see the FAQ's [Getting Support][support] section.

[arch]: https://wiki.archlinux.org/title/Java
[client]: client.md
[debian]: https://wiki.debian.org/Java
[faq]: faq.md
[fedora]: https://docs.fedoraproject.org/en-US/quick-docs/installing-java
[installingjava]: #installing-java
[installingneoforge]: #installing-neoforge
[launchers]: launchers.md
[server]: server.md
[support]: faq.md#getting-support
[testingforjava]: #testing-for-java
[ubuntu]: https://ubuntu.com/tutorials/install-jre
