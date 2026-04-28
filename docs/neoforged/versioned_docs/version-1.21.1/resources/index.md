# Resources

Resources are external files that are used by the game, but are not code. The most prominent kinds of resources are textures, however, many other types of resources exist in the Minecraft ecosystem. Of course, all these resources require a consumer on the code side, so the consuming systems are grouped in this section as well.

Minecraft generally has two kinds of resources: resources for the [logical client][logicalsides], known as assets, and resources for the [logical server][logicalsides], known as data. Assets are mostly display-only information, for example textures, display models, translations, or sounds, while data includes various things that affect gameplay, such as loot tables, recipes, or worldgen information. They are loaded from resource packs and data packs, respectively. NeoForge generates a built-in resource and data pack for every mod.

Both resource and data packs normally require a [`pack.mcmeta` file][packmcmeta]; however, modern NeoForge generates these at runtime for you, so you don't need to worry about it.

If you are confused about the format of something, have a look at the vanilla resources. Your NeoForge development environment not only contains vanilla code, but also vanilla resources. They can be found in the External Resources section (IntelliJ)/Project Libraries section (Eclipse), under the name `ng_dummy_ng.net.minecraft:client:client-extra:<minecraft_version>` (for Minecraft resources) or `ng_dummy_ng.net.neoforged:neoforge:<neoforge_version>` (for NeoForge resources).

## Assets

_See also: [Resource Packs][mcwikiresourcepacks] on the [Minecraft Wiki][mcwiki]_

Assets, or client-side resources, are all resources that are only relevant on the [client][sides]. They are loaded from resource packs, sometimes also known by the old term texture packs (stemming from old versions when they could only affect textures). A resource pack is basically an `assets` folder. The `assets` folder contains subfolders for the various namespaces the resource pack includes; every namespace is one subfolder. For example, a resource pack for a mod with the id `coolmod` will probably contain a `coolmod` namespace, but may additionally include other namespaces, such as `minecraft`.

NeoForge automatically collects all mod resource packs into the `Mod resources` pack, which sits at the bottom of the Selected Packs side in the resource packs menu. It is currently not possible to disable the `Mod resources` pack. However, resource packs that sit above the `Mod resources` pack override resources defined in a resource pack below them. This mechanic allows resource pack makers to override your mod's resources, and also allows mod developers to override Minecraft resources if needed.

Resource packs can contain [models][models], [blockstate files][bsfile], [textures][textures], [sounds][sounds], [particle definitions][particles] and [translation files][translations].

## Data

_See also: [Data Packs][mcwikidatapacks] on the [Minecraft Wiki][mcwiki]_

In contrast to assets, data is the term for all [server][sides] resources. Similar to resource packs, data is loaded through data packs (or datapacks). Like a resource pack, a data pack consists of a [`pack.mcmeta` file][packmcmeta] and a root folder, named `data`. Then, again like with resource packs, that `data` folder contains subfolders for the various namespaces the resource pack includes; every namespace is one subfolder. For example, a data pack for a mod with the id `coolmod` will probably contain a `coolmod` namespace, but may additionally include other namespaces, such as `minecraft`.

NeoForge automatically applies all mod data packs to a new world upon creation. It is currently not possible to disable mod data packs. However, most data files can be overridden (and thus be removed by replacing them with an empty file) by a data pack with a higher priority. Additional data packs can be enabled or disabled by placing them in a world's `datapacks` subfolder and then enabling or disabling them through the [`/datapack`][datapackcmd] command.

:::info
There is currently no built-in way to apply a set of custom data packs to every world. However, there are a number of mods that achieve this.
:::

Data packs may contain folders with files affecting the following things:

| Folder name                                                                       | Contents                     |
|-----------------------------------------------------------------------------------|------------------------------|
| `advancement`                                                                     | [Advancements][advancements] |
| `damage_type`                                                                     | [Damage types][damagetypes]  |
| `loot_table`                                                                      | [Loot tables][loottables]    |
| `recipe`                                                                          | [Recipes][recipes]           |
| `tags`                                                                            | [Tags][tags]                 |
| `neoforge/data_maps`                                                              | [Data maps][datamap]         |
| `neoforge/loot_modifiers`                                                         | [Global loot modifiers][glm] |
| `dimension`, `dimension_type`, `structure`, `worldgen`, `neoforge/biome_modifier` | Worldgen files               |

Additionally, they may also contain subfolders for some systems that integrate with commands. These systems are rarely used in conjunction with mods, but worth mentioning regardless:

| Folder name     | Contents                       |
|-----------------|--------------------------------|
| `chat_type`     | [Chat types][chattype]         |
| `function`      | [Functions][function]          |
| `item_modifier` | [Item modifiers][itemmodifier] |
| `predicate`     | [Predicates][predicate]        |

## `pack.mcmeta`

_See also: [`pack.mcmeta` (Resource Pack)][packmcmetaresourcepack] and [`pack.mcmeta` (Data Pack)][packmcmetadatapack] on the [Minecraft Wiki][mcwiki]_

`pack.mcmeta` files hold the metadata of a resource or data pack. For mods, NeoForge makes this file obsolete, as the `pack.mcmeta` is generated synthetically. In case you still need a `pack.mcmeta` file, the full specification can be found in the linked Minecraft Wiki articles.

## Data Generation

Data generation, colloquially known as datagen, is a way to programmatically generate JSON resource files, in order to avoid the tedious and error-prone process of writing them by hand. The name is a bit misleading, as it works for assets as well as data.

Datagen is run through the Data run configuration, which is generated for you alongside the Client and Server run configurations. The data run configuration follows the [mod lifecycle][lifecycle] until after the registry events are fired. It then fires the [`GatherDataEvent`][event], in which you can register your to-be-generated objects in the form of data providers, writes said objects to disk, and ends the process.

All data providers extend the `DataProvider` interface and usually require one method to be overridden. The following is a list of noteworthy data generators Minecraft and NeoForge offer (the linked articles add further information, such as helper methods):

| Class                                                | Method                           | Generates                                                               | Side   | Notes                                                                                                           |
|------------------------------------------------------|----------------------------------|-------------------------------------------------------------------------|--------|-----------------------------------------------------------------------------------------------------------------|
| [`BlockStateProvider`][blockstateprovider]           | `registerStatesAndModels()`      | Blockstate files, block models                                          | Client |                                                                                                                 |
| [`ItemModelProvider`][itemmodelprovider]             | `registerModels()`               | Item models                                                             | Client |                                                                                                                 |
| [`LanguageProvider`][langprovider]                   | `addTranslations()`              | Translations                                                            | Client | Also requires passing the language in the constructor.                                                          |
| [`ParticleDescriptionProvider`][particleprovider]    | `addDescriptions()`              | Particle definitions                                                    | Client |                                                                                                                 |
| [`SoundDefinitionsProvider`][soundprovider]          | `registerSounds()`               | Sound definitions                                                       | Client |                                                                                                                 |
| `SpriteSourceProvider`                               | `gather()`                       | Sprite sources / atlases                                                | Client |                                                                                                                 |
| [`AdvancementProvider`][advancementprovider]         | `generate()`                     | Advancements                                                            | Server | Make sure to use the NeoForge variant, not the Minecraft one.                                                   |
| [`LootTableProvider`][loottableprovider]             | `generate()`                     | Loot tables                                                             | Server | Requires extra methods and classes to work properly, see linked article for details.                            |
| [`RecipeProvider`][recipeprovider]                   | `buildRecipes(RecipeOutput)`     | Recipes                                                                 | Server |                                                                                                                 |
| [Various subclasses of `TagsProvider`][tagsprovider] | `addTags(HolderLookup.Provider)` | Tags                                                                    | Server | Several specialized subclasses exist, see linked article for details.                                           |
| [`DataMapProvider`][datamapprovider]                 | `gather()`                       | Data map entries                                                        | Server |                                                                                                                 |
| [`GlobalLootModifierProvider`][glmprovider]          | `start()`                        | Global loot modifiers                                                   | Server |                                                                                                                 |
| [`DatapackBuiltinEntriesProvider`][datapackprovider] | N/A                              | Datapack builtin entries, e.g. worldgen and [damage types][damagetypes] | Server | No method overriding, instead entries are added in a lambda in the constructor. See linked article for details. |
| `JsonCodecProvider` (abstract class)                 | `gather()`                       | Objects with a codec                                                    | Both   | This can be extended for use with any object that has a [codec] to encode data to.                              |

All of these providers follow the same pattern. First, you create a subclass and add your own resources to be generated. Then, you add the provider to the event in an [event handler][eventhandler]. An example using a `RecipeProvider`:

```java
public class MyRecipeProvider extends RecipeProvider {
    public MyRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Register your recipes here.
    }
}

// In some event handler class
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    // Data generators may require some of these as constructor parameters.
    // See below for more details on each of these.
    DataGenerator generator = event.getGenerator();
    PackOutput output = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

    // Register the provider.
    generator.addProvider(
            // A boolean that determines whether the data should actually be generated.
            // The event provides methods that determine this:
            // event.includeClient(), event.includeServer(),
            // event.includeDev() and event.includeReports().
            // Since recipes are server data, we only run them in a server datagen.
            event.includeServer(),
            // Our provider.
            new MyRecipeProvider(output, lookupProvider)
    );
    // Other data providers here.
}
```

The event offers some context for you to use:

- `event.getGenerator()` returns the `DataGenerator` that you register the providers to.
- `event.getPackOutput()` returns a `PackOutput` that is used by some providers to determine their file output location.
- `event.getExistingFileHelper()` returns an `ExistingFileHelper` that is used by providers for things that can reference other files (for example block models, which can specify a parent file).
- `event.getLookupProvider()` returns a `CompletableFuture<HolderLookup.Provider>` that is mainly used by tags and datagen registries to reference other, potentially not yet existing elements.
- `event.includeClient()`, `event.includeServer()`, `event.includeDev()` and `event.includeReports()` are `boolean` methods that allow you to check whether specific command line arguments (see below) are enabled.

### Command Line Arguments

The data generator can accept several command line arguments:

- `--mod examplemod`: Tells the data generator to run datagen for this mod. Automatically added by NeoGradle for the owning mod id, add this if you e.g. have multiple mods in one project.
- `--output path/to/folder`: Tells the data generator to output into the given folder. It is recommended to use Gradle's `file(...).getAbsolutePath()` to generate an absolute path for you (with a path relative to the project root directory). Defaults to `file('src/generated/resources').getAbsolutePath()`.
- `--existing path/to/folder`: Tells the data generator to consider the given folder when checking for existing files. Like with the output, it is recommended to use Gradle's `file(...).getAbsolutePath()`.
- `--existing-mod examplemod`: Tells the data generator to consider the resources in the given mod's JAR file when checking for existing files.
- Generator modes (all of these are boolean arguments and do not need any additional arguments):
    - `--includeClient`: Whether to generate client resources (assets). Check at runtime with `GatherDataEvent#includeClient()`.
    - `--includeServer`: Whether to generate server resources (data). Check at runtime with `GatherDataEvent#includeServer()`.
    - `--includeDev`: Whether to run dev tools. Generally shouldn't be used by mods. Check at runtime with `GatherDataEvent#includeDev()`.
    - `--includeReports`: Whether to dump a list of registered objects. Check at runtime with `GatherDataEvent#includeReports()`.
    - `--all`: Enable all generator modes.

All arguments can be added to the run configurations by adding the following to your `build.gradle`:

```groovy
runs {
    // other run configurations here

    data {
        programArguments.addAll '--arg1', 'value1', '--arg2', 'value2', '--all' // boolean args have no value
    }
}
```

For example, to replicate the default arguments, you could specify the following:

```groovy
runs {
    // other run configurations here

    data {
        programArguments.addAll '--mod', 'examplemod', // insert your own mod id
                '--output', file('src/generated/resources').getAbsolutePath(),
                '--includeClient',
                '--includeServer'
    }
}
```

[advancementprovider]: server/advancements.md#data-generation
[advancements]: server/advancements.md
[blockstateprovider]: client/models/datagen.md#block-model-datagen
[bsfile]: client/models/index.md#blockstate-files
[chattype]: https://minecraft.wiki/w/Chat_type
[codec]: ../datastorage/codecs.md
[damagetypes]: server/damagetypes.md
[datamap]: server/datamaps/index.md
[datamapprovider]: server/datamaps/index.md#data-generation
[datapackcmd]: https://minecraft.wiki/w/Commands/datapack
[datapackprovider]: ../concepts/registries.md#data-generation-for-datapack-registries
[event]: ../concepts/events.md
[eventhandler]: ../concepts/events.md#registering-an-event-handler
[function]: https://minecraft.wiki/w/Function_(Java_Edition)
[glm]: server/loottables/glm.md
[glmprovider]: server/loottables/glm.md#datagen
[itemmodelprovider]: client/models/datagen.md#item-model-datagen
[itemmodifier]: https://minecraft.wiki/w/Item_modifier
[langprovider]: client/i18n.md#datagen
[lifecycle]: ../concepts/events.md#the-mod-lifecycle
[logicalsides]: ../concepts/sides.md#the-logical-side
[loottableprovider]: server/loottables/index.md#datagen
[loottables]: server/loottables/index.md
[mcwiki]: https://minecraft.wiki
[mcwikidatapacks]: https://minecraft.wiki/w/Data_pack
[mcwikiresourcepacks]: https://minecraft.wiki/w/Resource_pack
[models]: client/models/index.md
[packmcmeta]: #packmcmeta
[packmcmetadatapack]: https://minecraft.wiki/w/Data_pack#pack.mcmeta
[packmcmetaresourcepack]: https://minecraft.wiki/w/Resource_pack#Contents
[particleprovider]: client/particles.md#datagen
[particles]: client/particles.md
[predicate]: https://minecraft.wiki/w/Predicate
[recipeprovider]: server/recipes/index.md#data-generation
[recipes]: server/recipes/index.md
[sides]: ../concepts/sides.md
[soundprovider]: client/sounds.md#datagen
[sounds]: client/sounds.md
[tags]: server/tags.md
[tagsprovider]: server/tags.md#datagen
[textures]: client/textures.md
[translations]: client/i18n.md#language-files
