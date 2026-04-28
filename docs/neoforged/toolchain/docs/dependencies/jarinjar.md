# Jar-in-Jar

Jar-in-Jar is a way to load dependencies for mods from within the jars of the mods. To accomplish this, Jar-in-Jar generates a metadata json within `META-INF/jarjar/metadata.json` on build containing the artifacts to load from within the jar.

Jar-in-Jar is a completely optional system. This will include all dependencies from the `jarJar` configuration into the `jarJar` task.

## Adding Dependencies

You can add dependencies to be included inside your jar using the `jarJar` configuration. Jar-in-Jar is a negotiation system, and by default, will create and include the highest version from the `prefer` version.

```gradle
// In build.gradle
dependencies {
    // Compiles against and includes the supported version of examplelib
    //   from 2.0 (inclusive)
    jarJar(implementation(group: 'com.example', name: 'examplelib')) {
        version {
            // Version used in your workspace and bundled in mod jar
            prefer '2.0'
        }
    }
}
```

If your library should only work between an exact version range rather than the preferred version to whatever the highest version there is, you can configure `strictly` to a range your mod is compatible with:

```gradle
// In build.gradle
dependencies {
    // Compiles against the highest supported version of examplelib
    //   between 2.0 (inclusive) and 3.0 (exclusive)
    jarJar(implementation(group: 'com.example', name: 'examplelib') {
        version {
            // Sets the supported version of examplelib
            //   between 2.0 (inclusive) and 3.0 (exclusive)
            strictly '[2.0,3.0)'
            // Version used in your workspace and bundled in mod jar
            prefer '2.8.0'
        }
    }
}
```
