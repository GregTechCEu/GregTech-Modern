# Conditionally-Loaded Data

There are times when modders may want to include data-driven objects using information from another mod without having to explicitly make that mod a dependency. Other cases may be to swap out certain objects with other modded entries when they are present. This can be done through the conditional subsystem.

## Implementations

Conditions are loaded from a top-level `neoforge:conditions` array of objects that represent the conditions to check. If all conditions specified are met, the JSON file will be loaded, otherwise it will be discarded.

```js
{
  "neoforge:conditions": [
    // Condition 1
    {
    
    },
    // Condition 2
    {

    }
  ],

  // The rest of the object
  ...
}
```

Conditionally-loaded recipes additionally have wrappers for [data generation][datagen] through `RecipeOutput#withConditions`. Other generators (like the data map one) will usually have a vararg `ICondition...` array in their methods for adding conditions to objects.

Currently, conditions are supported by the following Vanilla objects:
- recipes
- advancements
- dynamic registries (i.e. biomes)
- loot tables - individual pools can also have their own conditions

:::note
Loot tables that do not meet their loading conditions will not be ignored, but rather replaced with an empty loot table.
:::

```js title="Example recipe that will only be loaded if the examplemod mod is loaded"
{
  // highlight-start
  "neoforge:conditions": [
    {
      "type": "neoforge:mod_loaded",
      "modid": "examplemod"
    }
  ],
  // highlight-end

  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "key": {
    "#": {
      "item": "examplemod:example_planks"
    }
  },
  "pattern": [
    "##",
    "##",
    "##"
  ],
  "result": {
    "count": 3,
    "item": "mymod:compat_door"
  }
}
```

## Conditions

### True and False

Boolean conditions consist of no data and return the expected value of the condition. They are represented by `neoforge:true` and `neoforge:false`.

```js
// For some condition
{
  // Will always return true (or false for 'neoforge:false')
  "type": "neoforge:true"
}
```

### Not, And, and Or

Boolean operator conditions consist of the condition(s) being operated upon and apply the following logic. They are represented by `neoforge:not`, `neoforge:and`, and `neoforge:or`.


```js
// For some condition
{
  // Inverts the result of the stored condition
  "type": "neoforge:not",
  "value": {
    // A condition
  }
}
```

```js
// For some condition
{
  // ANDs the stored conditions together (or ORs for 'neoforge:or')
  "type": "neoforge:and",
  "values": [
    {
      // First condition
    },
    {
      // Second condition to be ANDed (or ORed for 'neoforge:or')
    }
  ]
}
```

### Mod Loaded

`ModLoadedCondition` returns true whenever the specified mod with the given id is loaded in the current application. This is represented by `neoforge:mod_loaded`.

```js
// For some condition
{
  "type": "neoforge:mod_loaded",
   // Returns true if 'examplemod' is loaded
  "modid": "examplemod"
}
```

### Item Exists

`ItemExistsCondition` returns true whenever the given item has been registered in the current application. This is represented by `neoforge:item_exists`.

```js
// For some condition
{
  "type": "neoforge:item_exists",
   // Returns true if 'examplemod:example_item' has been registered
  "item": "examplemod:example_item"
}
```

### Tag Empty

`TagEmptyCondition` returns true whenever the given item tag has no items within it. This is represented by `neoforge:tag_empty`.

```js
// For some condition
{
  "type": "neoforge:tag_empty",
   // Returns true if 'examplemod:example_tag' is an item tag with no entries
  "tag": "examplemod:example_tag"
}
```

## Creating Custom Conditions

Custom conditions can be created by implementing `ICondition` and creating a [Codec] for it.

### ICondition

A condition needs to implement the `ICondition#test(IContext)` method. This method will return `true` if the object should be loaded, and `false` otherwise.

:::note
Every `#test` has access to some `IContext` representing the state of the game. Currently, this only allows obtaining tags from a registry.
:::

:::info
Some objects may be loaded earlier than tags. In those cases, the condition context will be `IContext.EMPTY`.
:::

The `ICondition#codec` method should return the codec used to encode and decode the condition. This codec **must** be [registered] to the `NeoForgeRegistries#CONDITION_SERIALIZERS` registry. The name the codec is registered under will be the name used to refer to that condition in the `type` field.


[datagen]: ../../datagen/recipes.md
[condition]: #icondition
[Codec]: ../../datastorage/codecs
[registered]: ../../concepts/registries