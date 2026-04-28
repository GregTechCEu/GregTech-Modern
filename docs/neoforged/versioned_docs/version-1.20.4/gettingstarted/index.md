# Getting Started with NeoForge

This section includes information about how to set up a NeoForge workspace, and how to run and test your mod.

## Prerequisites

- Familiarity with the Java programming language, specifically its object-oriented, polymorphic, generic, and functional features.
- An installation of the Java 17 Development Kit (JDK) and 64-bit Java Virtual Machine (JVM). NeoForge recommends and officially supports the [Microsoft builds of OpenJDK][jdk], but any other JDK should work as well.

:::caution
Make sure you are using a 64-bit JVM. One way of checking is to run `java -version` in a terminal. Issues may arise when using a 32-bit JVM, due to 32-bit JVMs running out of support for a lot of things.
:::

- Familiarity with an Integrated Development Environment (IDE) of your choice.
   - NeoForge officially supports [IntelliJ IDEA][intellij] and [Eclipse][eclipse], both of which have integrated Gradle support. However, any IDE can be used, ranging from Netbeans or Visual Studio Code to Vim or Emacs.
- Familiarity with [Git][git] and [GitHub][github]. This is technically not required, but it will make your life a lot easier.

## Setting Up the Workspace

- Open the [Mod Developer Kit (MDK)][mdk] GitHub repository, click "Use this template" and clone the newly-created repository to your local machine.
   - If you do not want to use GitHub, or if you want to get the template for an older commit, you can also download the ZIP of the repository (under Code -> Download ZIP) and extract it.
- Open your IDE and import the Gradle project. Eclipse and IntelliJ IDEA will do this automatically for you. If you have an IDE that does not do this, you can also do it via the `gradlew` terminal command.
   - When doing this for the first time, Gradle will download all dependencies of NeoForge, including Minecraft itself, and decompile them. This can take a fair amount of time (up to an hour, depending on your hardware and network strength).
   - Whenever you make a change to the Gradle files, the Gradle changes will need to be reloaded, either through the "Reload Gradle" button in your IDE, or again through the `gradlew` terminal command.

## Customizing Your Mod Information

Many of the basic properties of your mod can be changed in the `gradle.properties` file. This includes basic things like the mod name or the mod version. For more information, see the comments in the `gradle.properties` file, or see [the documentation of the `gradle.properties` file][properties].

If you want to modify the build process beyond that, you can edit the `build.gradle` file. NeoGradle, the Gradle plugin for NeoForge, provides several configuration options, a few of which are explained by comments in the `build.gradle` files. For full documentation, see the [NeoGradle documentation][neogradle].

:::caution
Only edit the `build.gradle` and `settings.gradle` files if you know what you are doing. All basic properties can be set via `gradle.properties`.
:::

## Building and Testing Your Mod

To build your mod, run `gradlew build`. This will output a file in `build/libs` with the name `<archivesBaseName>-<version>.jar`. `<archivesBaseName>` and `<version>` are properties set by the `build.gradle` and default to the `mod_id` and `mod_version` values in the `gradle.properties` file, respectively; this can be changed in the `build.gradle` if desired. The resulting JAR file can then be placed in the `mods` folder of a NeoForge-enabled Minecraft setup, or uploaded to a mod distribution platform.

To run your mod in a test environment, you can either use the generated run configurations or use the associated tasks (e.g. `gradlew runClient`). This will launch Minecraft from the corresponding runs directory (e.g. `runs/client` or `runs/server`), along with any source sets specified. The default MDK includes the `main` source set, so any code written in `src/main/java` will be applied.

### Server Testing

If you are running a dedicated server, whether through the run configuration or `gradlew runServer`, the server will shut down immediately. You will need to accept the Minecraft EULA by editing the `eula.txt` file in the run directory.

Once accepted, the server will load and become available under `localhost` (or `127.0.0.1` by default). However, you will still not able to join, because the server will be put into online mode by default, which requires authentication (that the Dev player does not have). To fix this, stop your server again and set the `online-mode` property in the `server.properties` file to `false`. Now, start your server, and you should be able to connect.

:::tip
You should always test your mod in a dedicated server environment. This includes [client-only mods][client], as these should not do anything when loaded on the server.
:::

[client]: ../concepts/sides.md
[eclipse]: https://www.eclipse.org/downloads/
[git]: https://www.git-scm.com/
[github]: https://github.com/
[intellij]: https://www.jetbrains.com/idea/
[jdk]: https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-17
[mdk]: https://github.com/NeoForgeMDKs/MDK-1.20.4-NeoGradle
[neogradle]: https://docs.neoforged.net/neogradle/docs/
[properties]: modfiles.md#gradleproperties
