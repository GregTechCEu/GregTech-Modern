# Identifiers

`Identifier`s are one of the most important things in Minecraft. They are used as keys in [registries][registries], as identifiers for data or resource files, as references to models in code, and in a lot of other places. An `Identifier` consists of two parts: a namespace and a path, separated by a `:`.

The namespace denotes what mod, resource pack or datapack the location refers to. For example, a mod with the mod id `examplemod` will use the `examplemod` namespace. Minecraft uses the `minecraft` namespace. Extra namespaces can be defined at will simply by creating a corresponding data folder, this is usually done by datapacks to keep their logic separate from the point where they integrate with vanilla.

The path is a reference to whatever object you want, inside your namespace. For example, `minecraft:cow` is a reference to something named `cow` in the `minecraft` namespace - usually this location would be used to get the cow entity from the entity registry. Another example would be `examplemod:example_item`, which would probably be used to get your mod's `example_item` from the item registry.

`Identifier`s may only contain lowercase letters, digits, underscores, dots and hyphens. Paths may additionally contain forward slashes. Note that due to Java module restrictions, mod ids may not contain hyphens, which by extension means that mod namespaces may not contain hyphens either (they are still permitted in paths).

:::info
An `Identifier` on its own says nothing about what kind of objects we are using it for. Objects named `minecraft:dirt` exist in multiple places, for example. It is up to whatever receives the `Identifier` to associate an object with it.
:::

A new `Identifier` can be created by calling `Identifier.fromNamespaceAndPath("examplemod", "example_item")` or `Identifier.parse("examplemod:example_item")`. If `withDefaultNamespace` is used, the string will be used as the path, and `minecraft` will be used as the namespace. So for example, `Identifier.withDefaultNamespace("example_item")` will result in `minecraft:example_item`.

The namespace and path of an `Identifier` can be retrieved using `Identifier#getNamespace()` and `#getPath()`, respectively, and the combined form can be retrieved through `Identifier#toString`.

`Identifier`s are immutable. All utility methods on `Identifier`, such as `withPrefix` or `withSuffix`, return a new `Identifier`.

## Resolving `Identifier`s

Some places, for example registries, use `Identifier`s directly. Some other places, however, will resolve the `Identifier` as needed. For example:

- `Identifier`s are used as identifiers for GUI backgrounds. For example, the furnace GUI uses the identifier `minecraft:textures/gui/container/furnace.png`. This maps to the file `assets/minecraft/textures/gui/container/furnace.png` on disk. Note that the `.png` suffix is required in this identifier.
- `Identifier`s are used as identifiers for block models. For example, the block model of dirt uses the identifier `minecraft:block/dirt`. This maps to the file `assets/minecraft/models/block/dirt.json` on disk. Note that the `.json` suffix is not required here. Note as well that this identifier automatically maps into the `models` subfolder.
- `Identifiers` are used as identifiers for client items. For example, the client item of the apple uses the identifier `minecraft:apple` (as defined by `DataComponents#ITEM_MODEL`). This maps to the file `assets/minecraft/items/apple.json`. Note that the `.json` suffix is not required here. Note as well that this identifier automatically maps into the `items` subfolder.
- `Identifier`s are used as identifiers for recipes. For example, the iron block crafting recipe uses the identifier `minecraft:iron_block`. This maps to the file `data/minecraft/recipe/iron_block.json` on disk. Note that the `.json` suffix is not required here. Note as well that this identifier automatically maps into the `recipe` subfolder.

Whether the `Identifier` expects a file suffix, or what exactly the identifier resolves to, depends on the use case.

## `ResourceKey`s

`ResourceKey`s combine a registry id with a registry name. An example would be a registry key with the registry id `minecraft:item` and the registry name `minecraft:diamond_sword`. Unlike an `Identifier`, `ResourceKey`s actually refer to a unique element, thus being able to clearly identify an element. They are most commonly used in contexts where many different registries come in contact with one another. A common use case are datapacks, especially worldgen.

A new `ResourceKey` can be created through the static method `ResourceKey#create(ResourceKey<? extends Registry<T>>, Identifier)`. The second parameter here is the registry name, while the first parameter is what is known as a registry key. Registry keys are a special kind of `ResourceKey` whose registry is the root registry (i.e. the registry of all other registries). A registry key can be created via `ResourceKey#createRegistryKey(Identifier)` with the desired registry's id.

`ResourceKey`s are interned at creation. This means that comparing by reference equality (`==`) is possible and encouraged, but their creation is comparatively expensive.

[registries]: ../concepts/registries.md
[sides]: ../concepts/sides.md
