import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Parchment

[Parchment] is a community-sourced set of mappings of **parameter names** and **javadocs** that augment the official names released by Mojang. By using parchment, you can get parameter names for most Minecraft methods, instead of the non-descriptive `p_` names.

## Configuring parchment

The most basic configuration is using the following properties in `gradle.properties`:

<Tabs defaultValue="mdg">
<TabItem value="mdg" label="ModDevGradle">

```properties
# The Minecraft version the Parchment version is for
neoForge.parchment.minecraftVersion=1.20.2
# The version of the Parchment mappings
neoForge.parchment.mappingsVersion=2023.12.10
```

</TabItem>
<TabItem value="ng" label="NeoGradle">

```properties
# The Minecraft version the Parchment version is for
neogradle.subsystems.parchment.minecraftVersion=1.20.2
# The version of the Parchment mappings
neogradle.subsystems.parchment.mappingsVersion=2023.12.10
```

</TabItem>
</Tabs>

The subsystem also has a Gradle DSL and supports more parameters, explained in the following Gradle snippet:

<Tabs defaultValue="mdg">
<TabItem value="mdg" label="ModDevGradle">

```gradle
neoForge {
    parchment {
        // The Minecraft version for which the Parchment mappings were created.
        // This does not necessarily need to match the Minecraft version your mod targets
        // Defaults to the value of Gradle property neoForge.parchment.minecraftVersion
        minecraftVersion = "1.20.2"
        
        // The version of Parchment mappings to apply.
        // See https://parchmentmc.org/docs/getting-started for a list.
        // Defaults to the value of Gradle property neoForge.parchment.mappingsVersion
        mappingsVersion = "2023.12.10"
        
        // Overrides the full Maven coordinate of the Parchment artifact to use
        // This is computed from the minecraftVersion and mappingsVersion properties by default.
        // If you set this property explicitly, minecraftVersion and mappingsVersion will be ignored.
        // parchmentArtifact = "org.parchmentmc.data:parchment-$minecraftVersion:$mappingsVersion:checked@zip"
        
        // The string that parameters are prefixed with when they conflict with other names inside the method.
        // Defaults to `p_`. You can set this property to an empty string to disable conflict resolution,
        // for example, when you use the checked version of parchment, which already includes prefixes.
        // conflictResolutionPrefix = ''

        // Can be used to explicitly disable this subsystem. By default, it will be enabled automatically as soon
        // as parchmentArtifact or minecraftVersion and mappingsVersion are set.
        // enabled = true
    }
}
```

</TabItem>
<TabItem value="ng" label="NeoGradle">

```gradle
subsystems {
    parchment {
        // The Minecraft version for which the Parchment mappings were created.
        // This does not necessarily need to match the Minecraft version your mod targets
        // Defaults to the value of Gradle property neogradle.subsystems.parchment.minecraftVersion
        minecraftVersion = "1.20.2"
        
        // The version of Parchment mappings to apply.
        // See https://parchmentmc.org/docs/getting-started for a list.
        // Defaults to the value of Gradle property neogradle.subsystems.parchment.mappingsVersion
        mappingsVersion = "2023.12.10"
        
        // Overrides the full Maven coordinate of the Parchment artifact to use
        // This is computed from the minecraftVersion and mappingsVersion properties by default.
        // If you set this property explicitly, minecraftVersion and mappingsVersion will be ignored.
        // The built-in default value can also be overridden using the Gradle property neogradle.subsystems.parchment.parchmentArtifact
        // parchmentArtifact = "org.parchmentmc.data:parchment-$minecraftVersion:$mappingsVersion:checked@zip"
        
        // Overrides the full Maven coordinate of the tool used to apply the Parchment mappings
        // See https://github.com/neoforged/JavaSourceTransformer
        // The built-in default value can also be overridden using the Gradle property neogradle.subsystems.parchment.toolArtifact
        // toolArtifact = "net.neoforged.jst:jst-cli-bundle:1.0.30"
        
        // The string that parameters are prefixed with when they conflict with other names inside the method.
        // Defaults to `p_`. You can set this property to an empty string to disable conflict resolution,
        // for example, when you use the checked version of parchment, which already includes prefixes.
        // conflictPrefix = ''

        // Set this to false if you don't want the https://maven.parchmentmc.org/ repository to be added automatically when
        // applying Parchment mappings is enabled
        // The built-in default value can also be overridden using the Gradle property neogradle.subsystems.parchment.addRepository
        // addRepository = true
        
        // Can be used to explicitly disable this subsystem. By default, it will be enabled automatically as soon
        // as parchmentArtifact or minecraftVersion and mappingsVersion are set.
        // The built-in default value can also be overridden using the Gradle property neogradle.subsystems.parchment.enabled
        // enabled = true
    }
}
```

</TabItem>
</Tabs>

:::tip
You can find the latest Parchment versions on their [documentation](https://parchmentmc.org/docs/getting-started).
:::

[Parchment]: https://parchmentmc.org/
