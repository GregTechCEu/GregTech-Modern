# Versioning

This article will break down how versioning works in Minecraft and NeoForge, and will give some recommendations for mod versioning as well.

## Minecraft

Minecraft's versioning scheme has changed over the years.

Versions from 1.0 to 1.21.11 follow a [semver]-inspired format, `major.minor.<patch>`, with the patch version omitted for brevity. Examples of these versions would be 1.21 and 1.21.11.

Versions 26.1 and above follow a [calver]-inspired format, `year.release.<patch>`, with the patch version omitted for brevity. Examples of these versions would be 26.1 and 26.1.1.

### The Early Release Era

Before the game's release, the versioning scheme changed often, and there were versions like `a1.1` (Alpha 1.1), `b1.7.3` (Beta 1.7.3) or even the `infdev` versions, which didn't follow a clear versioning scheme at all.

### The Major Update Era

When the game was officially released as Minecraft 1.0, it adopted a new versioning scheme, using `1` as the major version, a second number to represent a (usually annual) major update, and a third number to represent minor versions and patches. For example, Minecraft 1.20.2 has the major version 1, the minor version 20 and the patch version 2. This format was used from 2011 until 2026, from when Minecraft 1.0 was introduced to the 26.1 update in early 2026. This format worked well until Minecraft began releasing drops, which, being minor updates, only incremented the patch version, leading to the 1.21 update lasting nearly two years.

#### Snapshots

Snapshots for 1.21.11 and below deviated from the standard versioning scheme. They were labeled as `YYwWWa`, where `YY` represents the last two digits of the year (e.g. `23`) and `WW` represents the week of that year (e.g. `01`). So for example, snapshot `23w01a` was the snapshot released in the first week of 2023.

The `a` suffix existed for occasions where two snapshots get released in the same week (where the second snapshot would then be named something like `23w01b`). Mojang has occasionally used this in the past. The alternative suffix has also been used for snapshots like `20w14infinite`, which was the [2020 infinite dimensions April Fool's joke][infinite].

#### Pre-releases and Release Candidates

When a snapshot cycle is nearing completion, Mojang starts releasing so-called pre-releases. Pre-releases are deemed feature-complete for a version and focus solely on bugfixes. They use the semver notation for the version it is for, suffixed by `-preX`. So for example, the first pre-release for 1.20.2 was named `1.20.2-pre1`. There can be and usually are multiple pre-releases, which are accordingly suffixed with `-pre2`, `-pre3`, etc.

Similarly, when the pre-release cycle completes, Mojang releases Release Candidate 1 (suffixing the version with `-rc1`, for example `1.20.2-rc1`). Mojang aims to have one release candidate that they can release if no further bugs occur. However, if an unexpected bug occurs, then there can also be an `-rc2`, `-rc3`, etc. version, similar to pre-releases.

### The Game Drop Era

Due to the decision to change from annual major updates with patches throughout the year to quarterly game drops, Mojang made the decision to change how Minecraft is versioned to a new format, `year.release.<patch>`. As an example, 26.1 will be the first release to adopt this new versioning, representing the first drop of 2026. If a hotfix needs to be released, it will be 26.1.1, and the second drop of 2026 will be 26.2.

#### Snapshots, Pre-Releases, and Release Candidates

As the next update is being developed, Mojang releases early versions called snapshots. Snapshots rapidly change aspects of the game and are quite unstable, but can be a good way to see how the next update is shaping out. Snapshots for 26.1 and above use the calver notation for the version it is for, suffixed by `-snapshot-X`. For example 26.1-snapshot-1 is the first snapshot for the upcoming 26.1 update, with following snapshots labeled as `snapshot-2` or `snapshot-3`.

When a snapshot cycle is nearing completion, Mojang starts releasing so-called pre-releases. Pre-releases are deemed feature-complete for a version and focus solely on bugfixes. Similarly, these versions are suffixed by `-pre-X`. So for example, the first pre-release for 26.1 was named `26.1-pre-1`. There can be and usually are multiple pre-releases, which are accordingly suffixed with `-pre-2`, `-pre-3`, etc.

Similarly, when the pre-release cycle completes, Mojang releases Release Candidate 1 (suffixing the version with `-rc1`, for example `26.1-rc-1`). Mojang aims to have one release candidate that they can release if no further bugs occur. However, if an unexpected bug occurs, then there can also be an `-rc-2`, `-rc-3`, etc. version, similar to pre-releases.

## NeoForge

NeoForge uses an adapted semver system: The major version is Minecraft's minor version, the minor version is Minecraft's patch version, and the patch version is the "actual" NeoForge version. So for example, NeoForge 20.2.59 is the 60th version (we start at 0) for Minecraft 1.20.2. Versions prefixed by `1` omitted the `1` as it is was believed that this would not change. When this turned out not to be the case, NeoForge's versioning was changed to include the full Minecraft version number, with a `0` in the place of the patch number if needed. For example, NeoForge 26.1.0.5-beta is the 6th version for Minecraft 26.1.

A few places in NeoForge also use [Maven version ranges][mvr], for example the Minecraft and NeoForge version ranges in the [`neoforge.mods.toml`][neoforgemodstoml] file. These are mostly, but not fully compatible with semver (the `pre`-tag is not considered by it, for example).

## Mods

There is no definitive best versioning system. Different styles of development, scopes of projects, etc. all influence the decision of what versioning system to use. Sometimes, versioning system can also be combined. This section attempts to give an overview over some commonly used versioning systems, with real-life examples.

Usually, a mod's file name looks like `modid-<version>.jar`. So if our mod id is `examplemod` and our version is `1.2.3`, our mod file would be named `examplemod-1.2.3.jar`.

:::note
Versioning systems are suggestions, rather than strictly enforced rules. This is especially true with regard to when the version is changed ("bumped"), and in what way. If you want to use a different versioning system, nobody is going to stop you.
:::

### Semantic Versioning

Semantic versioning ("semver") consists of three parts: `major.minor.patch`. The major version is bumped when major changes are made to the codebase, which usually correlates with major new features and bugfixes. The minor version is bumped when minor features are introduced, and patch bumps happen when an update only includes bug-fixes.

It is generally agreed upon that any version `0.x.x` is a development version, and with the first (full) release, the version should be bumped to `1.0.0`.

The "minor for features, patch for bugfixes" rule is often disregarded in practice. A popular example for this is Minecraft itself, which does major features through the minor version number, minor features through the patch number, and bugfixes in snapshots (see above).

Depending on how often a mod is updated, these numbers can be smaller or larger. For example, [Supplementaries][supplementaries] is on version `2.6.31` (at the time of writing). Triple- or even quadruple-digit numbers, especially in the `patch`, are absolutely possible.

### "Reduced" and "Expanded" Semver

Sometimes, semver can be seen with only two numbers. This is a sort of "reduced" semver, or "2-part" semver. Their version numbers only have a `major.minor` scheme. This is commonly used by small mods that only add a few simple objects and thus rarely need updates (except Minecraft version updates), often staying at version `1.0` forever.

"Expanded" semver, or "4-part" semver, has four numbers (so something like `1.0.0.0`). Depending on the mod, the format can be `major.api.minor.patch`, or `major.minor.patch.hotfix`, or something different entirely - there is no standard way to do it.

For `major.api.minor.patch`, the `major` version is decoupled from the `api` version. This means that the `major` (feature) bit and the `api` bit can be bumped independently. This is commonly used by mods that expose an API for other modders to use. For example, [Mekanism][mekanism] is currently on version 10.4.5.19 (at the time of writing).

For `major.minor.patch.hotfix`, the patch level is split into two. This is the approach used by the [Create][create] mod, which is currently on version 0.5.1f (at the time of writing). Note that Create denotes the hotfix as a letter instead of a fourth number, in order to stay compatible with regular semver.

:::info
Reduced semver, expanded semver, 2-part semver and 4-part semver are not official terms or standardized formats in any way.
:::

### Alpha, Beta, Release

Like Minecraft itself, modding is often done in the classical `alpha`/`beta`/`release` stages known from software engineering, where `alpha` denotes an unstable/experimental version (sometimes also called `experimental` or `snapshot`), `beta` denotes a semi-stable version, and `release` denotes a stable version (sometimes called `stable` instead of `release`).

Some mods use their major version to denote a Minecraft version bump. An example of this is [JEI][jei], which uses `13.x.x.x` for Minecraft 1.19.2, `14.x.x.x` for 1.19.4, and `15.x.x.x` for 1.20.1 (there are no versions for 1.19.3 and 1.20.0). Others append the tag to the mod name, for example the [Minecolonies][minecolonies] mod, which is on `1.1.328-BETA` at the time of writing.

### Including the Minecraft Version

It is common to include the Minecraft version a mod is for in the filename. This makes it easier for end users to easily find out what Minecraft version a mod is for. A common place for this is either before or after the mod version, with the former being more widespread than the latter. For example, JEI version `16.0.0.28` (latest at the time of writing) for 1.20.2 would become `jei-1.20.2-16.0.0.28` or `jei-16.0.0.28-1.20.2`.

### Including the Mod Loader

As you probably know, NeoForge is not the only mod loader out there, and many mod developers develop on multiple platforms. As a result, a way to distinguish between two files of the same mod of the same version, but for different mod loaders is needed.

Usually, this is done by including the mod loader somewhere in the name. `jei-neoforge-1.20.2-16.0.0.28`, `jei-1.20.2-neoforge-16.0.0.28` or `jei-1.20.2-16.0.0.28-neoforge` are all valid ways to do it. For other mod loaders, the `neoforge` bit would be replaced with `forge`, `fabric`, `quilt` or whatever different mod loader you might be developing on alongside NeoForge.

### A Note on Maven

Maven, the system used for dependency hosting, uses a versioning system that differs from semver in some details (though the general `major.minor.patch` pattern remains the same). The related [Maven Versioning Range (MVR)][mvr] system is used in some places in NeoForge (see [above][neoforge]). When choosing your versioning scheme, you should make sure it is compatible with MVR, as otherwise, mods will not be able to depend on specific versions of your mod!

[create]: https://www.curseforge.com/minecraft/mc-mods/create
[infinite]: https://minecraft.wiki/w/Java_Edition_20w14∞
[jei]: https://www.curseforge.com/minecraft/mc-mods/jei
[mekanism]: https://www.curseforge.com/minecraft/mc-mods/mekanism
[minecolonies]: https://www.curseforge.com/minecraft/mc-mods/minecolonies
[minecraft]: #minecraft
[neoforgemodstoml]: modfiles.md#neoforgemodstoml
[mvr]: https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html
[mvr]: https://maven.apache.org/ref/3.9.9/maven-artifact/apidocs/org/apache/maven/artifact/versioning/ComparableVersion.html
[neoforge]: #neoforge
[pre]: #pre-releases
[rc]: #release-candidates
[semver]: https://semver.org/
[calver]: https://calver.org/
[supplementaries]: https://www.curseforge.com/minecraft/mc-mods/supplementaries
