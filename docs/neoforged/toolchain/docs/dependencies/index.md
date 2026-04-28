# Dependencies

Dependencies are not only used to develop interoperability between mods or add additional libraries to the game, but it also determines what version of Minecraft to develop for. This will provide a quick overview on how to modify the `repositories` and `dependencies` block to add dependencies to your development environment.

:::note

This will not explain Gradle concepts in depth. It is highly recommended to read the [Gradle Dependency Management guide][guide] before continuing.

:::

## Mod Dependencies

All mod dependencies are added the same way as any other artifacts.

```gradle
dependencies {
    // Assume we have some artifact 'examplemod' that can be obtained from a specified repository
    implementation 'com.example:examplemod:1.0'
}
```

### Local Mod Dependencies

If the mod you are trying to depend on is not available on a maven repository (e.g., [Maven Central][central], [CurseMaven], [Modrinth]), you can add a mod dependency using a [flat directory][flat] instead:

```gradle
repositories {
    // Adds the 'libs' folder in the project directory as a flat directory
    flatDir {
        dir 'libs'
    }
}

dependencies {
    // ...

    // Given some <group>:<name>:<version>:<classifier (default None)>
    //   with an extension <ext (default jar)>
    // Artifacts in flat directories will be resolved in the following order:
    // - <name>-<version>.<ext>
    // - <name>-<version>-<classifier>.<ext>
    // - <name>.<ext>
    // - <name>-<classifier>.<ext>

    // If a classifier is explicitly specified
    //  artifacts with the classifier will take priority:
    // - examplemod-1.0-api.jar
    // - examplemod-api.jar
    // - examplemod-1.0.jar
    // - examplemod.jar
    implementation 'com.example:examplemod:1.0:api'
}
```

:::note
The group name can be anything but must not be empty for flat directory entries as they are not checked when resolving the artifact file.
:::

[guide]: https://docs.gradle.org/8.14.3/userguide/dependency_management.html

[central]: https://central.sonatype.com/
[CurseMaven]: https://cursemaven.com/
[Modrinth]: https://docs.modrinth.com/docs/tutorials/maven/

[flat]: https://docs.gradle.org/8.14.3/userguide/supported_repository_types.html#sec:flat-dir-resolver
