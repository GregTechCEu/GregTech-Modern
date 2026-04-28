# NeoForge Update Checker

NeoForge provides a very lightweight, opt-in, update-checking framework. If any mods have an available update, it will show a flashing icon on the 'Mods' button of the main menu and mod list along with the respective changelogs. It *does not* download updates automatically.

## Getting Started

The first thing you want to do is specify the `updateJSONURL` parameter in your `mods.toml` file. The value of this parameter should be a valid URL pointing to an update JSON file. This file can be hosted on your own web server, GitHub, or wherever you want as long as it can be reliably reached by all users of your mod.

## Update JSON format

The JSON itself has a relatively simple format as follows:

```json5
{
  "homepage": "<homepage/download page for your mod>",
  "<mcversion>": {
    "<modversion>": "<changelog for this version>", 
    // List all versions of your mod for the given Minecraft version, along with their changelogs
    // ...
  },
  "promos": {
    "<mcversion>-latest": "<modversion>",
    // Declare the latest "bleeding-edge" version of your mod for the given Minecraft version
    "<mcversion>-recommended": "<modversion>",
    // Declare the latest "stable" version of your mod for the given Minecraft version
    // ...
  }
}
```

This is fairly self-explanatory, but some notes:
 
- The link under `homepage` is the link the user will be shown when the mod is outdated.
- NeoForge uses an internal algorithm to determine whether one version string of your mod is "newer" than another. Most versioning schemes should be compatible, but see the `ComparableVersion` class if you are concerned about whether your scheme is supported. Adherence to [Maven versioning][mvnver] is highly recommended.
- The changelog string can be separated into lines using `\n`. Some prefer to include a abbreviated changelog, then link to an external site that provides a full listing of changes.
- Manually inputting data can be chore. You can configure your `build.gradle` to automatically update this file when building a release as Groovy has native JSON parsing support. Doing this is left as an exercise to the reader.

- Some examples can be found here for [nocubes], [Corail Tombstone][corail] and [Chisels & Bits 2][chisel].

## Retrieving Update Check Results

You can retrieve the results of the NeoForge Update Checker using `VersionChecker#getResult(IModInfo)`. You can obtain your `IModInfo` via `ModContainer#getModInfo`, where `ModContainer` can be added as a parameter to your mod constructor. You can obtain any other mod's `ModContainer` using `ModList.get().getModContainerById(<modId>)`. The returned object has a method `#status` which indicates the status of the version check.

|          Status | Description |
|----------------:|:------------|
|        `FAILED` | The version checker could not connect to the URL provided. |
|    `UP_TO_DATE` | The current version is equal to the recommended version. |
|         `AHEAD` | The current version is newer than the recommended version if there is not latest version. |
|      `OUTDATED` | There is a new recommended or latest version. |
| `BETA_OUTDATED` | There is a new latest version. |
|          `BETA` | The current version is equal to or newer than the latest version. |
|       `PENDING` | The result requested has not finished yet, so you should try again in a little bit. |

The returned object will also have the target version and any changelog lines as specified in `update.json`.

[mvnver]: ../gettingstarted/versioning.md
[nocubes]: https://cadiboo.github.io/projects/nocubes/update.json
[corail]: https://github.com/Corail31/tombstone_lite/blob/master/update.json
[chisel]: https://github.com/Aeltumn/Chisels-and-Bits-2/blob/master/update.json
