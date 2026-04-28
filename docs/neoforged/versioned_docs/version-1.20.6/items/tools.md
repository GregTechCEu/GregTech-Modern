# Tools & Armor

Tools are [items][item] whose primary use is to break [blocks][block]. Many mods add new tool sets (for example copper tools) or new tool types (for example hammers).

## Custom Tool Sets

A tool set typically consists of five items: a pickaxe, an axe, a shovel, a hoe and a sword. (Swords aren't tools in the classical sense, but are included here for consistency as well.) All of those items have their corresponding class: `PickaxeItem`, `AxeItem`, `ShovelItem`, `HoeItem` and `SwordItem`, respectively. The class hierarchy of tools looks as follows:

```text
Item
- TieredItem
  - DiggerItem
    - AxeItem
    - HoeItem
    - PickaxeItem
    - ShovelItem
  - SwordItem
```

`TieredItem` is a class that contains helpers for items with a certain `Tier` (read on). `DiggerItem` contains helpers for items that are designed to break blocks. Note that other items usually considered tools, such as shears, are not included in this hierarchy. Instead, they directly extend `Item` and hold the breaking logic themselves.

To create a standard set of tools, you must first define a `Tier`. For reference values, see Minecraft's `Tiers` enum. This example uses copper tools, you can use your own material here and adjust the values as needed.

```java
// We place copper somewhere between stone and iron.
public static final Tier COPPER_TIER = new SimpleTier(
        // The tag that determines what blocks this tool cannot break. See below for more information.
        MyBlockTags.INCORRECT_FOR_COPPER_TOOL,
        // Determines the level of this tool. Since this is an int, there is no good way to place our tool between stone and iron.
        // NeoForge introduces the TierSortingRegistry to solve this problem, see below for more information. Use a best-effort approximation here.
        // Stone is 1, iron is 2.
        1,
        // Determines the durability of the tier.
        // Stone is 131, iron is 250.
        200,
        // Determines the mining speed of the tier. Unused by swords.
        // Stone uses 4, iron uses 6.
        5f,
        // Determines the attack damage bonus. Different tools use this differently. For example, swords do (getAttackDamageBonus() + 4) damage.
        // Stone uses 1, iron uses 2, corresponding to 5 and 6 attack damage for swords, respectively; our sword does 5.5 damage now.
        1.5f,
        // Determines the enchantability of the tier. This represents how good the enchantments on this tool will be.
        // Gold uses 22, we put copper slightly below that.
        20,
        // Determines the repair ingredient of the tier. Use a supplier for lazy initializing.
        () -> Ingredient.of(Tags.Items.INGOTS_COPPER)
);
```

Now that we have our `Tier`, we can use it for registering tools. All tool constructors have the same four parameters.

```java
//ITEMS is a DeferredRegister<Item>
public static final Supplier<SwordItem> COPPER_SWORD = ITEMS.register("copper_sword", () -> new SwordItem(
        // The tier to use.
        COPPER_TIER,
        // The item properties. We don't need to set the durability here because TieredItem handles that for us.
        new Item.Properties().attributes(
            // There are `createAttributes` methods in either the class or subclass of each DiggerItem
            SwordItem.createAttributes(
                // The tier to use.
                COPPER_TIER,
                // The type-specific attack damage bonus. 3 for swords, 1.5 for shovels, 1 for pickaxes, varying for axes and hoes.
                3,
                // The type-specific attack speed modifier. The player has a default attack speed of 4, so to get to the desired
                // value of 1.6f, we use -2.4f. -2.4f for swords, -3f for shovels, -2.8f for pickaxes, varying for axes and hoes.
                -2.4f,
            )
        )
));
public static final Supplier<AxeItem> COPPER_AXE = ITEMS.register("copper_axe", () -> new AxeItem(...));
public static final Supplier<PickaxeItem> COPPER_PICKAXE = ITEMS.register("copper_pickaxe", () -> new PickaxeItem(...));
public static final Supplier<ShovelItem> COPPER_SHOVEL = ITEMS.register("copper_shovel", () -> new ShovelItem(...));
public static final Supplier<HoeItem> COPPER_HOE = ITEMS.register("copper_hoe", () -> new HoeItem(...));
```

### Tags

When creating a `Tier`, it is assigned a block [tag][tags] containing blocks that will not drop anything if broken with this tool. For example, the `minecraft:incorrect_for_stone_tool` tag contains blocks like Diamond Ore, and the `minecraft:incorrect_for_iron_tool` tag contains blocks like Obsidian and Ancient Debris. To make it easier to assign blocks to their incorrect mining levels, a tag also exists for blocks that need this tool to be mined. For example, the `minecraft:needs_iron_tool` tag containslike Diamond Ore, and the `minecraft:needs_diamond_tool` tag contains blocks like Obsidian and Ancient Debris.

You can reuse one of the incorrect tags for your tool if you're fine with that. For example, if we wanted our copper tools to just be more durable stone tools, we'd pass in `BlockTags#INCORRECT_FOR_STONE_TOOL`.

Alternatively, we can create our own tag, like so:

```java
// This tag will allow us to add these blocks to the incorrect tags that cannot mine them
public static final TagKey<Block> NEEDS_COPPER_TOOL = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(MOD_ID, "needs_copper_tool"));

// This tag will be passed into our tier
public static final TagKey<Block> INCORRECT_FOR_COPPER_TOOL = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(MOD_ID, "incorrect_for_cooper_tool"));
```

And then, we populate our tag. For example, let's make copper able to mine gold ores, gold blocks and redstone ore, but not diamonds or emeralds. (Redstone blocks are already mineable by stone tools.) The tag file is located at `src/main/resources/data/mod_id/tags/blocks/needs_copper_tool.json` (where `mod_id` is your mod id):

```json5
{
  "values": [
    "minecraft:gold_block",
    "minecraft:raw_gold_block",
    "minecraft:gold_ore",
    "minecraft:deepslate_gold_ore",
    "minecraft:redstone_ore",
    "minecraft:deepslate_redstone_ore"
  ]
}
```

Then, for our tag to pass into the tier, we can provide a negative constraint for any tools that are incorrect for stone tools but within our copper tools tag. The tag file is located at `src/main/resources/data/mod_id/tags/blocks/incorrect_for_cooper_tool.json`:

```json5
{
    "values": [
        "#minecraft:incorrect_for_stone_tool"
    ],
    "remove": [
        "#mod_id:needs_copper_tool"
    ]
}
```

Finally, we can pass our tag into our tier creation, as seen above.

If you want to check if a tool can make a block state drop its blocks, call `Tool#isCorrectForDrops`. The `Tool` can be obtained by calling `ItemStack#get` with `DataComponents#TOOL`.

## Custom Tools

Custom tools can be created by adding a `Tool` [data component][datacomponents] (via `DataComponents#TOOL`) to the list of default components on your item via `Item.Properties#component`. `DiggerItem` is an implementation which takes in a `Tier`, as explained above, to construct the `Tool`. `DiggerItem` also provides a convience method called `#createAttributes` to supply to `Item.Properties#attributes` for your tool, such as the modified attack damage and attack speed.

A `Tool` contains a list of `Tool.Rule`s, the default mining speed when holding the tool (`1` by default), and the amount of damage the tool should take when mining a block (`1` by default). A `Tool.Rule` contains three pieces of information: a `HolderSet` of blocks to apply the rule to, an optional speed at which to mine the blocks in the set, and an optional boolean at which to determine whether these blocks can drop from this tool. If the optional are not set, then the other rules will be checked. The default behavior if all rules fail is the default mining speed and that the block cannot be dropped.

:::note
A `HolderSet` can be created from a `TagKey` via `Registry#getOrCreateTag`.
:::

Creating a multitool-like item (i.e. an item that combines two or more tools into one, e.g. an axe and a pickaxe as one item) or any tool-like does not need to extend any of the existing `TieredItem`s. It simply can be implemented using a combination of the following parts:

- Adding a `Tool` with your own rules by setting `DataComponents#TOOL` via `Item.Properties#component`.
- Adding attributes to the item (e.g. attack damage, attack speed) via `Item.Properties#attributes`.
- Overriding `IItemExtension#canPerformAction` to determine what [`ToolAction`s][toolaction] the item can perform.
- Calling `IBlockExtension#getToolModifiedState` if you want your item to modify the block state on right click based on the `ToolAction`s.
- Adding your tool to some of the `minecraft:*_enchantable` tags so that your item can have certain enchantments applied to it, or `IItemExtension#canApplyAtEnchantingTable` to check if the enchantment can be applied at all.

## `ToolAction`s

`ToolAction`s are an abstraction over what a tool can and cannot do. This includes both left-click and right-click behavior. NeoForge provides default `ToolAction`s in the `ToolActions` class:

- Digging actions. These exist for all four `DiggerItem` types as mentioned above, as well as sword and shears digging.
- Axe right-click actions for stripping (logs), scraping (oxidized copper) and unwaxing (waxed copper).
- Shear actions for harvesting (honeycombs), carving (pumpkins) and disarming (tripwires).
- Actions for shovel flattening (dirt paths), sword sweeping, hoe tilling, shield blocking, and fishing rod casting.

To create your own `ToolAction`s, use `ToolAction#get` - it will create a new `ToolAction` if needed. Then, in a custom tool type, override `IItemExtension#canPerformAction` as needed.

To query if an `ItemStack` can perform a certain `ToolAction`, call `IItemStackExtension#canPerformAction`. Note that this works on any `Item`, not just tools.

## Armor

Similar to tools, armor uses a tier system (although a different one). What is called `Tier` for tools is called `ArmorMaterial` for armors. Like above, this example shows how to add copper armor; this can be adapted as needed. However, unlike `Tier`s, `ArmorMaterial`s need to be [registered]. For the vanilla values, see the `ArmorMaterials` class.

```java
// We place copper somewhere between chainmail and iron.
public static final ArmorMaterial COPPER_ARMOR_MATERIAL = new ArmorMaterial(
    // Determines the defense value of this armor material, depending on what armor piece it is.
    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 2);
        map.put(ArmorItem.Type.LEGGINGS, 4);
        map.put(ArmorItem.Type.CHESTPLATE, 6);
        map.put(ArmorItem.Type.HELMET, 2);
        map.put(ArmorItem.Type.BODY, 4);
    }),
    // Determines the enchantability of the tier. This represents how good the enchantments on this armor will be.
    // Gold uses 25, we put copper slightly below that.
    20,
    // Determines the sound played when equipping this armor.
    // This is wrapped with a Holder.
    SoundEvents.ARMOR_EQUIP_GENERIC,
    // Determines the repair item for this armor.
    () -> Ingredient.of(Tags.Items.INGOTS_COPPER),
    // Determines the texture locations of the armor to apply when rendering
    // This can also be specified by overriding 'IItemExtension#getArmorTexture' on your item if the armor texture needs to be more dynamic
    List.of(
        // Creates a new armor texture that will be located at:
        // - 'assets/mod_id/textures/models/armor/copper_layer_1.png' for the outer texture
        // - 'assets/mod_id/textures/models/armor/copper_layer_2.png' for the inner texture (only legs)
        new ArmorMaterial.Layer(
            new ResourceLocation(MOD_ID, "copper")
        ),
        // Creates a new armor texture that will be rendered on top of the previous at:
        // - 'assets/mod_id/textures/models/armor/copper_layer_1_overlay.png' for the outer texture
        // - 'assets/mod_id/textures/models/armor/copper_layer_2_overlay.png' for the inner texture (only legs)
        // 'true' means that the armor material is dyeable; however, the item must also be added to the 'minecraft:dyeable' tag
        new ArmorMaterial.Layer(
            new ResourceLocation(MOD_ID, "copper"), "_overlay", true
        )
    ),
    // Returns the toughness value of the armor. The toughness value is an additional value included in
    // damage calculation, for more information, refer to the Minecraft Wiki's article on armor mechanics:
    // https://minecraft.wiki/w/Armor#Armor_toughness
    // Only diamond and netherite have values greater than 0 here, so we just return 0.
    0,
    // Returns the knockback resistance value of the armor. While wearing this armor, the player is
    // immune to knockback to some degree. If the player has a total knockback resistance value of 1 or greater
    // from all armor pieces combined, they will not take any knockback at all.
    // Only netherite has values greater than 0 here, so we just return 0.
    0
);
```

And then, we use that armor material in item registration.

```java
//ITEMS is a DeferredRegister<Item>
public static final Supplier<ArmorItem> COPPER_HELMET = ITEMS.register("copper_helmet", () -> new ArmorItem(
        // The armor material to use.
        COPPER_ARMOR_MATERIAL,
        // The armor type to use.
        ArmorItem.Type.HELMET,
        // The item properties where we set the durability.
        // ArmorItem.Type is an enum of five values: HELMET, CHESTPLATE, LEGGINGS, BOOTS, and BODY.
        // BODY is used for non-player entities like wolves or horses.
        // Vanilla armor materials determine this by using a base value and multiplying it with a type-specific constant.
        // The constants are 13 for BOOTS, 15 for LEGGINGS, 16 for CHESTPLATE, 11 for HELMET, and 16 for BODY.
        // If we don't want to use these ratios, we can set the durability normally.
        new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15))
));
public static final Supplier<ArmorItem> COPPER_CHESTPLATE = ITEMS.register("copper_chestplate", () -> new ArmorItem(...));
public static final Supplier<ArmorItem> COPPER_LEGGINGS = ITEMS.register("copper_leggings", () -> new ArmorItem(...));
public static final Supplier<ArmorItem> COPPER_BOOTS = ITEMS.register("copper_boots", () -> new ArmorItem(...));
```

When creating your armor texture, it is a good idea to work on top of the vanilla armor texture to see which part goes where.

[block]: ../blocks/index.md
[datacomponents]: ./datacomponents.md
[item]: index.md
[toolaction]: #toolactions
[tags]: ../resources/server/tags.md
[registered]: ../concepts/registries.md#methods-for-registering
