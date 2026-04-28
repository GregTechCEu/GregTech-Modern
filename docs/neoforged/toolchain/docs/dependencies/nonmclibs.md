---
sidebar_position: 1
---
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Non-Minecraft Dependencies

Non-Minecraft dependencies are artifacts that are neither a mod nor a dependency Minecraft or NeoForge itself relies on. By default, NeoForge does not load non-Minecraft dependencies when loading a mod. For development environments, they must be added as a runtime dependencies, while production environments should make use of the [jar-in-jar system][jij].

## 1.21.9 and Above

Running NeoForge on 1.21.9 and above will load anything available on the classpath in development, including non-minecraft dependencies. This means adding the library is as simple as adding any other gradle dependency:

```gradle
// This adds the library at compile and runtime
// In practice, this should be wrapped with 'jarJar'
// to include the library in your jar
implementation 'com.example:example:1.0'
```

## 1.21.8 and Below

Running NeoForge on 1.21.8 and below additionally require the library to be added to the runtime classpath:

<Tabs defaultValue="mdg">
<TabItem value="mdg" label="ModDevGradle">

```gradle
dependencies {
    // This is required to add the library at compile time
    implementation 'com.example:example:1.0'
    // This adds the library to all the runs
    additionalRuntimeClasspath 'com.example:example:1.0'
}
```

</TabItem>
<TabItem value="ng" label="NeoGradle">

```gradle
dependencies {
    implementation 'com.example:example:1.0'
}

runs {
    configureEach {
        dependencies {
            // highlight-next-line
            runtime 'com.example:example:1.0'
        }
    }
}
```

Or, you can use a configuration:

```gradle
configurations {
    libraries
    // This will make sure that all dependencies that you add to the libraries configuration will also be added to the implementation configuration
    // This way, you only need one dependency declaration for both runtime and compile dependencies
    implementation.extendsFrom libraries
}

dependencies {
    libraries 'com.example:example:1.0'
}

runs {
    configureEach {
        dependencies {
            // highlight-next-line
            runtime project.configurations.libraries
        }
    }
}
```

</TabItem>
</Tabs>

[jij]: jarinjar.md
