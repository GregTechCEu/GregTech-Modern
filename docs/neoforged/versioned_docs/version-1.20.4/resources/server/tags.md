# Tags

Tags are generalized sets of objects in the game used for grouping related things together and providing fast membership checks.

## Declaring Your Own Groupings

Tags are declared in your mod's [datapack][datapack]. For example, a `TagKey<Block>` with a given identifier of  `modid:foo/tagname` will reference a tag at `/data/<modid>/tags/blocks/foo/tagname.json`. Tags for `Block`s, `Item`s, `EntityType`s, `Fluid`s, and `GameEvent`s use the plural forms for their folder location while all other registries use the singular version (`EntityType` uses the folder `entity_types` while `Potion` would use the folder `potion`).
Similarly, you may append to or override tags declared in other domains, such as Vanilla, by declaring your own JSONs.
For example, to add your own mod's saplings to the Vanilla sapling tag, you would specify it in `/data/minecraft/tags/blocks/saplings.json`, and Vanilla will merge everything into one tag at reload, if the `replace` option is false.
If `replace` is true, then all entries before the json specifying `replace` will be removed.
Values listed that are not present will cause the tag to error unless the value is listed using an `id` string and `required` boolean set to false, as in the following example:

```js
{
  "replace": false,
  "values": [
    "minecraft:gold_ingot",
    "mymod:my_ingot",
    {
      "id": "othermod:ingot_other",
      "required": false
    }
  ]
}
```

See the [Vanilla wiki][tags] for a description of the base syntax.

There is also a NeoForge extension on the Vanilla syntax.
You may declare a `remove` array of the same format as the `values` array. Any values listed here will be removed from the tag. This acts as a finer grained version of the Vanilla `replace` option.


## Using Tags In Code

Tags for all registries are automatically sent from the server to any remote clients on login and reload. `Block`s, `Item`s, `EntityType`s, `Fluid`s, and `GameEvent`s are special cased as they have `Holder`s allowing for available tags to be accessible through the object itself.

:::note
Intrusive `Holder`s may be removed in a future version of Minecraft. If they are, the below methods can be used instead to query the associated `Holder`s.
:::

### ITagManager

NeoForge wrapped registries provide an additional helper for creating and managing tags through `ITagManager` which can be obtained via `IForgeRegistry#tags`. Tags can be created using using `#createTagKey` or `#createOptionalTagKey`. Tags or registry objects can also be checked for either or using `#getTag` or `#getReverseTag` respectively.

#### Custom Registries

Custom registries can create tags when constructing their `DeferredRegister` via `#createTagKey` or `#createOptionalTagKey` respectively. Their tags or registry objects can then checked for either using the `IForgeRegistry` obtained by calling `DeferredRegister#makeRegistry`.

### Referencing Tags

There are four methods of creating a tag wrapper:

Method                          | For
:---:                           | :---
`*Tags#create`                  | `BannerPattern`, `Biome`, `Block`, `CatVariant`, `DamageType`, `EntityType`, `FlatLevelGeneratorPreset`, `Fluid`, `GameEvent`, `Instrument`, `Item`, `PaintingVariant`, `PoiType`, `Structure`, and `WorldPreset` where `*` represents one of these types.
`ITagManager#createTagKey`      | NeoForge wrapped vanilla registries, registries can be obtained from `ForgeRegistries`.
`DeferredRegister#createTagKey` | Custom NeoForge registries.
`TagKey#create`                 | Vanilla registries without NeoForge wrappers, registries can be obtained from `Registry`.

Registry objects can check their tags or registry objects either through their `Holder` or through `ITag`/`IReverseTag` for vanilla or NeoForge registry objects respectively.

Vanilla registry objects can grab their associated holder using either `Registry#getHolder` or `Registry#getHolderOrThrow` and then compare if the registry object has a tag using `Holder#is`.

NeoForge registry objects can grab their tag definition using either `ITagManager#getTag` or `ITagManager#getReverseTag` and then compare if a registry object has a tag using `ITag#contains` or `IReverseTag#containsTag` respectively.

Tag-holding registry objects contain a method called `#is` in either their registry object or state-aware class to check whether the object belongs to a certain tag.

As an example:
```java
public static final TagKey<Item> myItemTag = ItemTags.create(new ResourceLocation("mymod", "myitemgroup"));

public static final TagKey<Potion> myPotionTag = ForgeRegistries.POTIONS.tags().createTagKey(new ResourceLocation("mymod", "mypotiongroup"));

public static final TagKey<VillagerType> myVillagerTypeTag = TagKey.create(Registries.VILLAGER_TYPE, new ResourceLocation("mymod", "myvillagertypegroup"));

// In some method:

ItemStack stack = /*...*/;
boolean isInItemGroup = stack.is(myItemTag);

Potion potion = /*...*/;
boolean isInPotionGroup  = ForgeRegistries.POTIONS.tags().getTag(myPotionTag).contains(potion);

ResourceKey<VillagerType> villagerTypeKey = /*...*/;
boolean isInVillagerTypeGroup = BuiltInRegistries.VILLAGER_TYPE.getHolder(villagerTypeKey).map(holder -> holder.is(myVillagerTypeTag)).orElse(false);
```

## Conventions

There are several conventions that will help facilitate compatibility in the ecosystem:

- If there is a Vanilla tag that fits your block or item, add it to that tag. See the [list of Vanilla tags][taglist].
- If there is a NeoForge tag that fits your block or item, add it to that tag. The list of tags declared by NeoForge can be seen on [GitHub][forgetags].
- If there is a group of something you feel should be shared by the community, use the `forge` namespace instead of your mod id.
- Tag naming conventions should follow Vanilla conventions. In particular, item and block groupings are plural instead of singular (e.g. `minecraft:logs`, `minecraft:saplings`).
- Item tags should be sorted into subdirectories according to their type (e.g. `forge:ingots/iron`, `forge:nuggets/brass`, etc.).


## Migration from OreDictionary

- For recipes, tags can be used directly in the vanilla recipe format (see below).
- For matching items in code, see the section above.
- If you are declaring a new type of item grouping, follow a couple naming conventions:
  - Use `domain:type/material`. When the name is a common one that all modders should adopt, use the `forge` domain.
  - For example, brass ingots should be registered under the `forge:ingots/brass` tag and cobalt nuggets under the `forge:nuggets/cobalt` tag.


## Using Tags in Recipes and Advancements

Tags are directly supported by Vanilla. See the respective Vanilla wiki pages for [recipes] and [advancements] for usage details.

[datapack]: ./index.md
[tags]: https://minecraft.wiki/w/Tag#JSON_format
[taglist]: https://minecraft.wiki/w/Tag#List_of_tags
[forgetags]: https://github.com/neoforged/NeoForge/tree/1.20.x/src/generated/resources/data/forge/tags
[recipes]: https://minecraft.wiki/w/Recipe#JSON_format
[advancements]: https://minecraft.wiki/w/Advancement
