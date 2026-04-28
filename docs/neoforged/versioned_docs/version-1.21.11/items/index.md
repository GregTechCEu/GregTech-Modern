# Items

Along with blocks, items are a key component of Minecraft. While blocks make up the world around you, items exist within inventories.

## What Even Is an Item?

Before we get further into creating items, it is important to understand what an item actually is, and what distinguishes it from, say, a [block][block]. Let's illustrate this using an example:

- In the world, you encounter a dirt block and want to mine it. This is a **block**, because it is placed in the world. (Actually, it is not a block, but a blockstate. See the [Blockstates article][blockstates] for more detailed information.)
    - Not all blocks drop themselves when breaking (e.g. leaves), see the article on [loot tables][loottables] for more information.
- Once you have [mined the block][breaking], it is removed (= replaced with an air block) and the dirt drops. The dropped dirt is an item **[entity][entity]**. This means that like other entities (pigs, zombies, arrows, etc.), it can inherently be moved by things like water pushing on it, or burned by fire and lava.
- Once you pick up the dirt item entity, it becomes an **item stack** in your inventory. An item stack is, simply put, an instance of an item with some extra information, such as the stack size.
- Item stacks are backed by their corresponding **item** (which is what we're creating). Items hold [data components][datacomponents] that contains the default information all items stacks are initialized to (for example, every iron sword has a max durability of 250), while item stacks can modify those data components, allowing two different stacks for the same item to have different information (for example, one iron sword has 100 uses left, while another iron sword has 200 uses left). For more information on what is done through items and what is done through item stacks, read on.
    - The relationship between items and item stacks is roughly the same as between [blocks][block] and [blockstates][blockstates], in that a blockstate is always backed by a block. It's not a really accurate comparison (item stacks aren't singletons, for example), but it gives a good basic idea about what the concept is here.

## Creating an Item

Now that we understand what an item is, let's create one!

Like with basic blocks, for basic items that need no special functionality (think sticks, sugar, etc.), the `Item` class can be used directly. To do so, during registration, instantiate `Item` with a `Item.Properties` parameter. This `Item.Properties` parameter can be created using `Item.Properties#of`, and it can be customized by calling its methods:

- `setId` - Sets the resource key of the item.
    - This **must** be set on every item; otherwise, an exception will be thrown.
- `overrideDescription` - Sets the translation key of the item. The created `Component` is stored in `DataComponents#ITEM_NAME`.
- `useBlockDescriptionPrefix` - Convenience helper that calls `overrideDescription` with the translation key `block.<modid>.<registry_name>`. This should be called on any `BlockItem`.
- `requiredFeatures` - Sets the required feature flags for this item. This is mainly used for vanilla's feature locking system in minor versions. It is discouraged to use this, unless you're integrating with a system locked behind feature flags by vanilla.
- `stacksTo` - Sets the max stack size (via `DataComponents#MAX_STACK_SIZE`) of this item. Defaults to 64. Used e.g. by ender pearls or other items that only stack to 16.
- `durability` - Sets the durability (via `DataComponents#MAX_DAMAGE`) of this item and the initial damage to 0 (via `DataComponents#DAMAGE`). Defaults to 0, which means "no durability". For example, iron tools use 250 here. Note that setting the durability automatically locks the max stack size to 1.
- `fireResistant` - Makes item entities that use this item immune to fire and lava (via `DataComponents#FIRE_RESISTANT`). Used by various netherite items.
- `rarity` - Sets the rarity of this item (via `DataComponents#RARITY`). Currently, this simply changes the item's color. `Rarity` is an enum consisting of the four values `COMMON` (white, default), `UNCOMMON` (yellow), `RARE` (aqua) and `EPIC` (light purple). Be aware that mods may add more rarity types.
- `setNoCombineRepair` - Disables grindstone and crafting grid repairing for this item. Unused in vanilla.
- `jukeboxPlayable` - Sets the resource key of the datapack `JukeboxSong` to play when inserted into a jukebox.
- `food` - Sets the [`FoodProperties`][food] of this item (via `DataComponents#FOOD`).

For examples, or to look at the various values used by Minecraft, have a look at the `Items` class.

### Remainders and Cooldowns

Items may have additional properties that are applied when being used or prevent the item from being used for a set time:

- `craftRemainder` - Sets the crafting remainder of this item. Vanilla uses this for filled buckets that leave behind empty buckets after crafting.
- `usingConvertsTo` - Sets the item to return after the item is finished being used via `Item#use`, `IItemExtension#finishUsingItem`, or `Item#releaseUsing`. The `ItemStack` is stored on `DataComponents#USE_REMAINDER`.
- `useCooldown` - Sets the number of seconds before the item can be used again (via `DataComponents#USE_COOLDOWN`).

### Tools and Armor

Some items act like [tools] and [armor]. These are constructed via a series of item properties, with only some usage being delegated to their associated classes:

- `enchantable` - Sets the maximum [enchantment] value of the stack, allowing the item to be enchanted (via `DataComponents#ENCHANTABLE`).
- `repairable` - Sets the item or tag that can be used to repair the durability of this item (via `DataComponents#REPAIRABLE`). Must have durability components and not `DataComponents#UNBREAKABLE`.
- `equippable` - Sets the slot the item can be equipped to (via `DataComponents#EQUIPPABLE`).
- `equippableUnswappable` - Same as `equippable`, but disables quick swapping via the use item button (default right-click).

More information can be found on their relevant pages.

### More Functionality

Directly using `Item` only allows for very basic items. If you want to add functionality, for example right-click interactions, a custom class that extends `Item` is required. The `Item` class has many methods that can be overridden to do different things; see the classes `Item` and `IItemExtension` for more information.

The two most common use cases for items are left-clicking and right-clicking. Due to their complexity and their reaching into other systems, they are explained in a separate [Interaction article][interactions].

### `DeferredRegister.Items`

All registries use `DeferredRegister` to register their contents, and items are no exceptions. However, due to the fact that adding new items is such an essential feature of an overwhelming amount of mods, NeoForge provides the `DeferredRegister.Items` helper class that extends `DeferredRegister<Item>` and provides some item-specific helpers:

```java
public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleMod.MOD_ID);

public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerItem(
    "example_item",
    Item::new, // The factory that the properties will be passed into.
    props -> props // A unary operator of the properties to use.
);
```

Internally, this will simply call `ITEMS.register("example_item", registryName -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))))` by applying the properties parameter to the provided item factory (which is commonly the constructor). The id is set on the properties.

If you want to use `Item::new`, you can leave out the factory entirely and use the `simple` method variant:

```java
public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem(
    "example_item",
    props -> props // A unary operator of the properties to use.
);
```

This does the exact same as the previous example, but is slightly shorter. Of course, if you want to use a subclass of `Item` and not `Item` itself, you will have to use the previous method instead.

Both of these methods also have overloads that omit the `new Item.Properties()` parameter:

```java
public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerItem("example_item", Item::new);

// Variant that also omits the Item::new parameter
public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item");
```

Finally, there's also shortcuts for block items. Along with `setId`, these also call `useBlockDescriptionPrefix` to set the translation key to the one used for a block:

```java
public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
    "example_block",
    ExampleBlocksClass.EXAMPLE_BLOCK,
    props -> props
);

// Variant that omits the properties parameter:
public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
    "example_block",
    ExampleBlocksClass.EXAMPLE_BLOCK
);

// Variant that omits the name parameter, instead using the block's registry name:
public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
    // Must be an instance of `Holder<Block>`
    // DeferredBlock<T> also works
    ExampleBlocksClass.EXAMPLE_BLOCK,
    props -> props
);

// Variant that omits both the name and the properties:
public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
    // Must be an instance of `Holder<Block>`
    // DeferredBlock<T> also works
    ExampleBlocksClass.EXAMPLE_BLOCK
);
```

:::note
If you keep your registered blocks in a separate class, you should classload your blocks class before your items class.
:::

### Resources

If you register your item and get your item (via `/give` or through a [creative tab][creativetabs]), you will find it to be missing a proper model and texture. This is because textures and models are handled by Minecraft's resource system.

For every item, you will want to add - or [generate][datagen] - JSON files for the following:

- A [client item][citems] with an associated [texture]
- A [translation][i18n]
- A [recipe][recipes] (optional)
- Some item [tags] (optional)

For all of the above, also reference the files and data generators of similar vanilla blocks.

## `ItemStack`s

Like with blocks and blockstates, most places where you'd expect an `Item` actually use an `ItemStack` instead. `ItemStack`s represent a stack of one or multiple items in a container, e.g. an inventory. Again like with blocks and blockstates, methods should be overridden by the `Item` and called on the `ItemStack`, and many methods in `Item` get an `ItemStack` instance passed in.

An `ItemStack` consists of three major parts:

- The `Item` it represents, obtainable through `ItemStack#getItem`, or `getItemHolder` for `Holder<Item>`.
- The stack size, typically between 1 and 64, obtainable through `getCount` and changeable through `setCount` or `shrink`.
- The [data components][datacomponents] map, where stack-specific data is stored. Obtainable through `getComponents`. The components values are typically accessed and mutated via `has`, `get`, `set`, `update`, and `remove`.

To create a new `ItemStack`, call `new ItemStack(Item)`, passing in the backing item. By default, this uses a count of 1 and no NBT data; there are constructor overloads that accept a count and NBT data as well if needed.

`ItemStack`s are mutable objects (see below), however it is sometimes required to treat them as immutables. If you need to modify an `ItemStack` that is to be treated immutable, you can clone the stack using `#copy` or `#copyWithCount` if a specific stack size should be used.

If you want to represent that a stack has no item, use `ItemStack.EMPTY`. If you want to check whether an `ItemStack` is empty, call `#isEmpty`.

### Mutability of `ItemStack`s

`ItemStack`s are mutable objects. This means that if you call for example `#setCount` or any data component map methods, the `ItemStack` itself will be modified. Vanilla uses the mutability of `ItemStack`s extensively, and several methods rely on it. For example, `#split` splits the given amount off the stack it is called on, both modifying the caller and returning a new `ItemStack` in the process.

However, this can sometimes lead to issues when dealing with multiple `ItemStack`s at once. The most common instance where this arises is when handling inventory slots, since you have to consider both the `ItemStack` currently selected by the cursor, as well as the `ItemStack` you are trying to insert to/extract from.

:::tip
When in doubt, better be safe than sorry and `#copy` the stack.
:::

### JSON Representation

In many situations, for example [recipes], item stacks need to be represented as JSON objects. An item stack's JSON representation looks the following way:

```json5
{
    // The item ID. Required.
    "id": "minecraft:dirt",
    // The item stack count [1, 99]. Optional, defaults to 1.
    "count": 4,
    // A map of data components. Optional, defaults to an empty map.
    "components": {
        "minecraft:enchantment_glint_override": true
    }
}
```

## Creative Tabs

By default, your item will only be available through `/give` and not appear in the creative inventory. Let's change that!

The way you get your item into the creative menu depends on what tab you want to add it to.

### Existing Creative Tabs

:::note
This method is for adding your items to Minecraft's tabs, or to other mods' tabs. To add items to your own tabs, see below.
:::

An item can be added to an existing `CreativeModeTab` via the `BuildCreativeModeTabContentsEvent`, which is fired on the [mod event bus][modbus], only on the [logical client][sides]. Add items by calling `event#accept`.

```java
//MyItemsClass.MY_ITEM is a Supplier<? extends Item>, MyBlocksClass.MY_BLOCK is a Supplier<? extends Block>
@SubscribeEvent // on the mod event bus
public static void buildContents(BuildCreativeModeTabContentsEvent event) {
    // Is this the tab we want to add to?
    if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
        event.accept(MyItemsClass.MY_ITEM.get());
        // Accepts an ItemLike. This assumes that MY_BLOCK has a corresponding item.
        event.accept(MyBlocksClass.MY_BLOCK.get());
    }
}
```

The event also provides some extra information, such as `getFlags` to get the list of enabled feature flags, or `hasPermissions` to check if the player has permissions to view the operator items tab.

### Custom Creative Tabs

`CreativeModeTab`s are a registry, meaning custom `CreativeModeTab`s must be [registered][registering]. Creating a creative tab uses a builder system, the builder is obtainable through `CreativeModeTab#builder`. The builder provides options to set the title, icon, default items, and a number of other properties. In addition, NeoForge provides additional methods to customize the tab's image, label and slot colors, where the tab should be ordered, etc.

```java
//CREATIVE_MODE_TABS is a DeferredRegister<CreativeModeTab>
public static final Supplier<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example", () -> CreativeModeTab.builder()
    //Set the title of the tab. Don't forget to add a translation!
    .title(Component.translatable("itemGroup." + MOD_ID + ".example"))
    //Set the icon of the tab.
    .icon(() -> new ItemStack(MyItemsClass.EXAMPLE_ITEM.get()))
    //Add your items to the tab.
    .displayItems((params, output) -> {
        output.accept(MyItemsClass.MY_ITEM.get());
        // Accepts an ItemLike. This assumes that MY_BLOCK has a corresponding item.
        output.accept(MyBlocksClass.MY_BLOCK.get());
    })
    .build()
);
```

## `ItemLike`

`ItemLike` is an interface implemented by `Item`s and [`Block`s][block] in vanilla. It defines the method `#asItem`, which returns an item representation of whatever the object actually is: `Item`s just return themselves, while `Block`s return their associated `BlockItem` if available, and `Blocks.AIR` otherwise. `ItemLike`s are used in various contexts where the "origin" of the item isn't important, for example in many [data generators][datagen].

It is also possible to implement `ItemLike` on your custom objects. Simply override `#asItem` and you're good to go.

[armor]: armor.md
[block]: ../blocks/index.md
[blockstates]: ../blocks/states.md
[breaking]: ../blocks/index.md#breaking-a-block
[citems]: ../resources/client/models/items.md
[creativetabs]: #creative-tabs
[datacomponents]: datacomponents.md
[datagen]: ../resources/index.md#data-generation
[enchantment]: ../resources/server/enchantments/index.md#enchantment-costs-and-levels
[entity]: ../entities/index.md
[food]: consumables.md#food
[hunger]: https://minecraft.wiki/w/Hunger#Mechanics
[interactions]: interactions.md
[loottables]: ../resources/server/loottables/index.md
[modbus]: ../concepts/events.md#event-buses
[recipes]: ../resources/server/recipes/index.md
[registering]: ../concepts/registries.md#methods-for-registering
[sides]: ../concepts/sides.md
[tools]: tools.md
[datagen]: ../resources/index.md#data-generation
[i18n]: ../resources/client/i18n.md
[texture]: ../resources/client/textures.md
[tags]: ../resources/server/tags.md
