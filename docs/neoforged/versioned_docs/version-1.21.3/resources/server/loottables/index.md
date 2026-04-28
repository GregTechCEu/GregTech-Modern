# Loot Tables

Loot tables are data files that are used to define randomized loot drops. A loot table can be rolled, returning a (potentially empty) list of item stacks. The output of this process depends on (pseudo-)randomness. Loot tables are located at `data/<mod_id>/loot_table/<name>.json`. For example, the loot table `minecraft:blocks/dirt`, used by the dirt block, is located at `data/minecraft/loot_table/blocks/dirt.json`.

Minecraft uses loot tables at various points in the game, including block drops, entity drops, chest loot, fishing loot, and many others. How a loot table is referenced depends on the context:

- Every block will, by default, receive an associated loot table, located at `<block_namespace>:blocks/<block_name>`. This can be disabled by calling `#noLootTable` on the block's `Properties`, resulting in no loot table being created and the block dropping nothing; this is mainly done by air-like or technical blocks.
- Every entity that does not call `EntityType.Builder#noLootTable` (which is typically entities in `MobCategory#MISC`) will, by default, receive an associated loot table, located at `<entity_namespace>:entities/<entity_name>`. This can be changed by overriding `#getLootTable`. For example, sheep use this to roll different loot tables depending on their wool color.
- Chests in structures specify their loot table in their block entity data. Minecraft stores all chest loot tables in `minecraft:chests/<chest_name>`; it is recommended, but not required to follow this practice in mods.
- The loot tables for gift items that villagers may throw at players after a raid are defined in the [`neoforge:raid_hero_gifts` data map][raidherogifts].
- Other loot tables, for example the fishing loot table, are retrieved when needed from `level.getServer().reloadableRegistries().getLootTable(lootTableKey)`. A list of all vanilla loot table locations can be found in `BuiltInLootTables`.

:::warning
Loot tables should generally only be created for stuff that belongs to your mod. For modifying existing loot tables, [global loot modifiers (GLMs)][glm] should be used instead.
:::

Due to the complexity of the loot table system, loot tables are compromised of several sub-systems that each have a different purpose.

## Loot Entry

A loot entry (or loot pool entry), represented in code through the abstract `LootPoolEntryContainer` class, is a singular loot element. It can specify one or multiple items to be dropped.

Vanilla provides a total of 8 different loot entry types. Through the common `LootPoolEntryContainer` superclass, all of them have the following properties:

- `weight`: The weight value. Defaults to 1. This is used for cases where some items should be more common than others. For example, given two loot entries, one with weight 3 and one with weight 1, then there is a 75% chance for the first entry to be chosen, and a 25% chance for the second entry.
- `quality`: The quality value. Defaults to 0. If this is non-zero, then this value is multiplied by the luck value (set in the [loot context][context]) and added to the weight when rolling the loot table.
- `conditions`: A list of [loot conditions][lootcondition] to apply to this loot entry. If one condition fails, the entry is treated as if it weren't present.
- `functions`: A list of [loot functions][lootfunction] to apply to the outputs of this loot entry.

Loot entries are generally split into two groups: singletons (with the common superclass `LootPoolSingletonContainer`) and composites (with the common superclass `CompositeEntryBase`), where composites are made up of multiple singletons. The following singleton types are provided by Minecraft:

- `minecraft:empty`: An empty loot entry, representing no item. Created in code by calling `EmptyLootItem#emptyItem`.
- `minecraft:item`: A singular loot item entry, dropping the specified item when rolled. Created in code by calling `LootItem#lootTableItem` with the desired item.
    - Setting stack size, data components, etc. can be done using loot functions.
- `minecraft:tag`: A tag entry, dropping all items in the specified tag when rolling. Has two variants, depending on the value of the boolean `expand` property. If `expand` is true, a separate entry for each item in the tag is generated, otherwise one entry is used to drop all items. Created by calling `TagEntry#tagContents` (for `expand=false`) or `TagEntry#expandTag` (for `expand=true`), each with an item [tag key][tags] parameter.
    - For example, if `expand` is true and the tag is `#minecraft:planks`, one entry is generated for each planks type (so 11 entries for the 11 vanilla planks + one entry per modded planks), each with the specified weight, quality and functions; whereas if `expand` is false, one single entry dropping all planks is used.
- `minecraft:dynamic`: A loot entry referencing a dynamic drop. Dynamic drops are a system to add entries to a loot table that cannot be specified beforehand, instead adding them in code. A dynamic drops entry consists of an id and a `Consumer<ItemStack>` that actually adds the items. To add a dynamic drops entry, specify a `minecraft:dynamic` entry with the desired id and then add a corresponding consumer in the [loot context][context]. Created using `DynamicLoot#dynamicEntry`.
- `minecraft:loot_table`: A loot entry that rolls another loot table, adding the result of that loot table as a single entry. The other loot table can either be specified by id or be inlined as a whole. Created in code by calling `NestedLootTable#lootTableReference` with a `ResourceLocation` parameter, or `NestedLootTable#inlineLootTable` with a `LootTable` object parameter for an inline loot table.

The following composite types are provided by Minecraft:

- `minecraft:group`: A loot entry containing a list of other loot entries, which are run in order. Created in code by calling `EntryGroup#list`, or by calling `#append` on another `LootPoolSingletonContainer.Builder`, each with other loot entry builders.
- `minecraft:sequence`: Like `minecraft:group`, but the loot entry stops running as soon as one sub-entry fails, discarding all entries after that. Created in code by calling `SequentialEntry#sequential`, or by calling `#then` on another `LootPoolSingletonContainer.Builder`, each with other loot entry builders.
- `minecraft:alternatives`: Sort of an opposite to `minecraft:sequence`, but the loot entry stops running as soon as one sub-entry succeeds (instead of as soon as one fails), discarding all entries after that. Created in code by calling `AlternativesEntry#alternatives`, or by calling `#otherwise` on another `LootPoolSingletonContainer.Builder`, each with other loot entry builders.

For modders, it is also possible to define [custom loot entry types][customentry].

## Loot Pool

A loot pool is, in essence, a list of loot entries. Loot tables can contain multiple loot pools, each loot pool will be rolled independently of the others.

Loot pools may contain the following contents:

- `entries`: A list of loot entries.
- `conditions`: A list of [loot conditions][lootcondition] to apply to this loot pool. If one condition fails, none of the loot pool's entries will be rolled.
- `functions`: A list of [loot functions][lootfunction] to apply to all loot entry outputs of this loot pool.
- `rolls` and `bonus_rolls`: Two number providers (read on) that together determine the amount of times this loot pool will be rolled. The formula is rolls + bonus_rolls * luck, where the luck value is set in the [loot parameters][parameters].
- `name`: A name for the loot pool. NeoForge-added. This can be used by [GLMs][glm]. If unspecified, this is the hash code of the loot pool, prefixed by `custom#`.

## Number Provider

Number providers are a way to get (pseudo-)randomized numbers in a datapack context. Primarily used by loot tables, they are also used in other contexts, for example in worldgen. Vanilla provides the following six number providers:

- `minecraft:constant`: A constant float value, rounding to integer where needed. Created through `ConstantValue#exactly`.
- `minecraft:uniform`: Uniformly-distributed random integer or float values, with min and max values set. All values between min and max have the same chance to appear. Created through `UniformGenerator#between`.
- `minecraft:binomial`: Binomially-distributed random integer values, with n and p values set. See [Binomial Distribution][binomial] for more information on what these values mean. Created through `BinomialDistributionGenerator#binomial`.
- `minecraft:score`: Given an entity target, a score name and (optionally) a scale value, retrieves the given scoreboard value for the entity target, multiplying it with the given scale value (if available). Created through `ScoreboardValue#fromScoreboard`.
- `minecraft:storage`: A value from the command storage at a given nbt path. Created through `new StorageValue`.
- `minecraft:enchantment_level`: A provider of values for each enchantment level. Created through `EnchantmentLevelProvider#forEnchantmentLevel`, providing a `LevelBasedValue`. Valid `LevelBasedValue`s are:
    - Simply a constant value, without a specified type. Created through `LevelBasedValue#constant`.
    - `minecraft:linear`: A linearly-increasing value per enchantment level, plus an optional constant base value. Created through `LevelBasedValue#perLevel`.
    - `minecraft:levels_squared`: Squares the enchantment value, and then adds an optional base value to it. Created through `new LevelBasedValue.LevelsSquared`.
    - `minecraft:fraction`: Accepts two other `LevelBasedValue`s, using them to create a fraction. Created through `new LevelBasedValue.Fraction`.
    - `minecraft:clamped`: Accepts another `LevelBasedValue`, alongside min and max values. Calculates the value using the other `LevelBasedValue` and clamps the result. Created through `new LevelBasedValue.Clamped`.
    - `minecraft:lookup`: Accepts a `List<Float>` and a fallback `LevelBasedValue`. Looks up the value to use in the list (level 1 is the first element in the list, level 2 is the second element, etc.), and uses the fallback value if the value for a level is missing. Created through `LevelBasedValue#lookup`.

Modders can also register [custom number providers][customnumber] and [custom level-based values][customlevelbased] if needed.

## Loot Parameters

A loot parameter, known internally as a `ContextKey<T>`, is a parameter provided to a loot table when rolled, where `T` is the type of the provided parameter, for example `BlockPos` or `Entity`. They can be used by [loot conditions][lootcondition] and [loot functions][lootfunction]. For example, the `minecraft:killed_by_player` loot condition checks for the presence of the `minecraft:player` parameter.

Minecraft provides the following loot parameters:

- `minecraft:origin`: A location associated with the loot table, e.g. the location of a loot chest. Access via `LootContextParams.ORIGIN`.
- `minecraft:tool`: An item stack associated with the loot table, e.g. the item used to break a block. This is not necessarily a tool. Access via `LootContextParams.TOOL`.
- `minecraft:enchantment_level`: An enchantment level, used by enchantment logic. Access via `LootContextParams.ENCHANTMENT_LEVEL`.
- `minecraft:enchantment_active`: Whether the used item has an enchantment or not, used e.g. by silk touch checks. Access via `LootContextParams.ENCHANTMENT_ACTIVE`.
- `minecraft:block_state`: A block state associated with the loot table, e.g. the broken block state. Access via `LootContextParams.BLOCK_STATE`.
- `minecraft:block_entity`: A block entity associated with the loot table, e.g. the block entity associated with the broken block. Used e.g. by shulker boxes to save their inventory to the dropped item. Access via `LootContextParams.BLOCK_ENTITY`.
- `minecraft:explosion_radius`: An explosion radius in the current context. Used primarily to apply explosion decay to drops. Access via `LootContextParams.EXPLOSION_RADIUS`.
- `minecraft:this_entity`: An entity associated with the loot table, typically the killed entity. Access via `LootContextParams.THIS_ENTITY`.
- `minecraft:damage_source`: A [damage source][damagesource] associated with the loot table, typically the damage source that killed the entity. Access via `LootContextParams.DAMAGE_SOURCE`.
- `minecraft:attacking_entity`: An attacking entity associated with the loot table, typically the killer of the entity. Access via `LootContextParams.ATTACKING_ENTITY`.
- `minecraft:direct_attacking_entity`: A direct attacking entity associated with the loot table. For example, if the attacking entity were a skeleton, the direct attacking entity would be the arrow. Access via `LootContextParams.DIRECT_ATTACKING_ENTITY`.
- `minecraft:last_damage_player`: A player associated with the loot table, typically the player that last attacked the killed entity, even if the player kill was indirect (for example: the player tapped the entity, and it was then killed by spikes). Used e.g. for player-kill-only drops. Access via `LootContextParams.LAST_DAMAGE_PLAYER`.

Custom loot parameters can be created by calling `new ContextKey<T>` with the desired id. Since they are merely resource location wrappers, they do not need to be registered.

### Entity Targets

Entity targets are a type used in loot conditions and functions, represented by the `LootContext.EntityTarget` enum in code. They are used to specify the entity loot parameter to query in a condition or function context. Valid values are:

- `"this"` or `LootContext.EntityTarget.THIS`: Represents the `"minecraft:this_entity"` parameter.
- `"attacker"` or `LootContext.EntityTarget.ATTACKER`: Represents the `"minecraft:attacking_entity"` parameter.
- `"direct_attacker"` or `LootContext.EntityTarget.DIRECT_ATTACKER`: Represents the `"minecraft:direct_attacking_entity"` parameter.
- `"attacking_player"` or `LootContext.EntityTarget.ATTACKING_PLAYER`: Represents the `"minecraft:last_damage_player"` parameter.

For example, the `minecraft:entity_properties` loot condition accepts an entity target to allow all four loot parameters to be checked, if that is what you (as the loot table author) need.

### Loot Parameter Sets

Loot parameter sets, also known as loot table types and known as `ContextKeySet`s in code, are a collection of required and optional loot parameters. Despite their name, they are not `Set`s (not even `Collection`s). Rather, they are a wrapper around two `Set<ContextKey<?>>`s, one holding the required parameters (`#required`) and one holding the optional parameters (`#allowed`). They are used to validate that users of loot parameters only use the parameters that can be expected to be available, and to verify that the required parameters are present when rolling a table. Besides that, they are also used in advancement and enchantment logic.

Vanilla provides the following loot parameter sets (required parameters are **bold**, optional parameters are _in italics_; the in-code names are constants in `LootContextParamSets`):

| ID                               | In-code name           | Specified Loot Parameters                                                                                                                                                                                                                                                                                            | Usage                                                     |
|----------------------------------|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| `minecraft:empty`                | `EMPTY`                | n/a                                                                                                                                                                                                                                                                                                                  | Fallback purposes.                                        |
| `minecraft:generic`              | `ALL_PARAMS`           | **`minecraft:origin`**, **`minecraft:tool`**, **`minecraft:block_state`**, **`minecraft:block_entity`**, **`minecraft:explosion_radius`**, **`minecraft:this_entity`**, **`minecraft:damage_source`**, **`minecraft:attacking_entity`**, **`minecraft:direct_attacking_entity`**, **`minecraft:last_damage_player`** | Validation.                                               |
| `minecraft:command`              | `COMMAND`              | **`minecraft:origin`**, _`minecraft:this_entity`_                                                                                                                                                                                                                                                                    | Commands.                                                 |
| `minecraft:selector`             | `SELECTOR`             | **`minecraft:origin`**, _`minecraft:this_entity`_                                                                                                                                                                                                                                                                    | Entity selectors in commands.                             |
| `minecraft:block`                | `BLOCK`                | **`minecraft:origin`**, **`minecraft:tool`**, **`minecraft:block_state`**, _`minecraft:block_entity`_, _`minecraft:explosion_radius`_, _`minecraft:this_entity`_                                                                                                                                                     | Block breaking.                                           |
| `minecraft:block_use`            | `BLOCK_USE`            | **`minecraft:origin`**, **`minecraft:block_state`**, **`minecraft:this_entity`**                                                                                                                                                                                                                                     | No vanilla uses.                                          |
| `minecraft:hit_block`            | `HIT_BLOCK`            | **`minecraft:origin`**, **`minecraft:enchantment_level`**, **`minecraft:block_state`**, **`minecraft:this_entity`**                                                                                                                                                                                                  | The channeling enchantment.                               |
| `minecraft:chest`                | `CHEST`                | **`minecraft:origin`**, _`minecraft:this_entity`_, _`minecraft:attacking_entity`_                                                                                                                                                                                                                                    | Loot chests and similar containers, loot chest minecarts. |
| `minecraft:archaeology`          | `ARCHAEOLOGY`          | **`minecraft:origin`**, **`minecraft:this_entity`**, **`minecraft:tool`**                                                                                                                                                                                                                                                                    | Archaeology.                                              |
| `minecraft:vault`                | `VAULT`                | **`minecraft:origin`**, _`minecraft:this_entity`_, _`minecraft:tool`_                                                                                                                                                                                                                                                                    | Trial chamber vault rewards.                              |
| `minecraft:entity`               | `ENTITY`               | **`minecraft:origin`**, **`minecraft:this_entity`**, **`minecraft:damage_source`**, _`minecraft:attacking_entity`_, _`minecraft:direct_attacking_entity`_, _`minecraft:last_damage_player`_                                                                                                                          | Entity kills.                                             |
| `minecraft:shearing`             | `SHEARING`             | **`minecraft:origin`**, **`minecraft:this_entity`**, **`minecraft:tool`**                                                                                                                                                                                                                                                                    | Shearing entities, e.g. sheep.                            |
| `minecraft:equipment`            | `EQUIPMENT`            | **`minecraft:origin`**, **`minecraft:this_entity`**                                                                                                                                                                                                                                                                  | Entity equipment for e.g. zombies.                        |
| `minecraft:gift`                 | `GIFT`                 | **`minecraft:origin`**, **`minecraft:this_entity`**                                                                                                                                                                                                                                                                  | Raid hero gifts.                                          |
| `minecraft:barter`               | `PIGLIN_BARTER`        | **`minecraft:this_entity`**                                                                                                                                                                                                                                                                                          | Piglin bartering.                                         |
| `minecraft:fishing`              | `FISHING`              | **`minecraft:origin`**, **`minecraft:tool`**, _`minecraft:this_entity`_, _`minecraft:attacking_entity`_                                                                                                                                                                                                              | Fishing.                                                  |
| `minecraft:enchanted_item`       | `ENCHANTED_ITEM`       | **`minecraft:tool`**, **`minecraft:enchantment_level`**                                                                                                                                                                                                                                                              | Several enchantments.                                     |
| `minecraft:enchanted_entity`     | `ENCHANTED_ENTITY`     | **`minecraft:origin`**, **`minecraft:enchantment_level`**, **`minecraft:this_entity`**                                                                                                                                                                                                                               | Several enchantments.                                     |
| `minecraft:enchanted_damage`     | `ENCHANTED_DAMAGE`     | **`minecraft:origin`**, **`minecraft:enchantment_level`**, **`minecraft:this_entity`**, **`minecraft:damage_source`**, _`minecraft:attacking_entity`_, _`minecraft:direct_attacking_entity`_                                                                                                                         | Damage and protection enchantments.                       |
| `minecraft:enchanted_location`   | `ENCHANTED_LOCATION`   | **`minecraft:origin`**, **`minecraft:enchantment_level`**, **`minecraft:enchantment_active`**, **`minecraft:this_entity`**                                                                                                                                                                                           | Frost walker and soul speed enchantments.                 |
| `minecraft:advancement_entity`   | `ADVANCEMENT_ENTITY`   | **`minecraft:origin`**, **`minecraft:this_entity`**                                                                                                                                                                                                                                                                  | Several [advancement criteria][advancement].              |
| `minecraft:advancement_location` | `ADVANCEMENT_LOCATION` | **`minecraft:origin`**, **`minecraft:tool`**, **`minecraft:block_state`**, **`minecraft:this_entity`**                                                                                                                                                                                                               | Several [advancement triggers][advancement].              |
| `minecraft:advancement_reward`   | `ADVANCEMENT_REWARD`   | **`minecraft:origin`**, **`minecraft:this_entity`**                                                                                                                                                                                                                                                                  | [Advancement rewards][advancement].                       |

### Loot Context

The loot context is an object containing situational information for rolling loot tables. The information includes:

- The `ServerLevel` the loot table is rolled in. Get via `#getLevel`.
- The `RandomSource` used to roll the loot table. Get via `#getRandom`.
- The loot parameters. Check presence using `#hasParameter`, and get single parameters using `#getParameter`.
- The luck value, used for calculating bonus rolls and quality values. Usually populated via the entity's luck attribute. Get via `#getLuck`.
- The dynamic drops consumers. See [above][entry] for more information. Set via `#addDynamicDrops`. No getter available.

## Loot Table

Combining all the previous elements, we finally get a loot table. Loot table JSONs can specify the following values:

- `pools`: A list of loot pools.
- `neoforge:conditions`: A list of [data load conditions][conditions]. **Warning: These are data load conditions, not [loot conditions][lootcondition]!**
- `functions`: A list of [loot functions][lootfunction] to apply to all loot entry outputs of this loot table.
- `type`: A loot parameter set, used to validate proper usage of loot parameters. Optional; if absent, validation will be skipped.
- `random_sequence`: A random sequence for this loot table, in the form of a resource location. Random sequences are provided by the `Level` and used for consistent loot table rolls under identical conditions. This commonly uses the loot table's location.

An example loot table could have the following format:

```json5
{
    "type": "chest", // loot parameter set
    "neoforge:conditions": [
        // data load conditions
    ],
    "functions": [
        // table-wide loot functions
    ],
    "pools": [ // list of loot pools
        {
            "rolls": 1, // amount of rolls of the loot table, using 5 here will yield 5 results from the pool
            "bonus_rolls": 0.5, // amount of bonus rolls
            "name": "my_pool",
            "conditions": [
                // pool-wide loot conditions
            ],
            "functions": [
                // pool-wide loot functions
            ],
            "entries": [ // list of loot table entries
                {
                    "type": "minecraft:item", // loot entry type
                    "name": "minecraft:dirt", // type-specific properties, for example the name of the item
                    "weight": 3, // weight of an entry
                    "quality": 1, // quality of an entry
                    "conditions": [
                        // entry-wide loot conditions
                    ],
                    "functions": [
                        // entry-wide loot functions
                    ]
                }
            ]
        }
    ]
}
```

## Rolling a Loot Table

To roll a loot table, we need two things: the loot table itself, and a loot context.

Let's start with getting the loot table itself. We can obtain a loot table using `level.getServer().reloadableRegistries().getLootTable(lootTableId)`. As the loot data is only available through the server, this logic must run on a [logical server][sides], not a logical client.

:::tip
Minecraft's built-in loot table IDs can be found in the `BuiltInLootTables` class. Block loot tables can be obtained through `BlockBehaviour#getLootTable`, and entity loot tables can be obtained through `EntityType#getDefaultLootTable` or `Entity#getLootTable`.
:::

Now that we have a loot table, let's build our parameter set. We begin by creating an instance of `LootParams.Builder`:

```java
// Make sure that you are on a server, otherwise the cast will fail.
LootParams.Builder builder = new LootParams.Builder((ServerLevel) level);
```

We can then add loot context parameters, like so:

```java
// Use whatever context parameters and values you need. Vanilla parameters can be found in LootContextParams.
builder.withParameter(LootContextParams.ORIGIN, position);
// This variant can accept null as the value, in which case an existing value for that parameter will be removed.
builder.withOptionalParameter(LootContextParams.ORIGIN, null);
// Add a dynamic drop.
builder.withDynamicDrop(ResourceLocation.fromNamespaceAndPath("examplemod", "example_dynamic_drop"), stackAcceptor -> {
    // some logic here
});
// Set our luck value. Assumes that a player is available. Contexts without a player should use 0 here.
builder.withLuck(player.getLuck());
```

Finally, we can create the `LootParams` from the builder and use them to roll the loot table:

```java
// Specify a loot context param set here if you want.
LootParams params = builder.create(LootContextParamSets.EMPTY);
// Get the loot table.
LootTable table = level.getServer().reloadableRegistries().getLootTable(location);
// Actually roll the loot table.
List<ItemStack> list = table.getRandomItems(params);
// Use this instead if you are rolling the loot table for container contents, e.g. loot chests.
// This method takes care of properly splitting the loot items across the container.
List<ItemStack> containerList = table.fill(container, params, someSeed);
```

:::danger
`LootTable` additionally exposes a method named `#getRandomItemsRaw`. Unlike the various `#getRandomItems` variants, `#getRandomItemsRaw` method will not apply [global loot modifiers][glm]. Use this method only if you know what you are doing.
:::

## Datagen

Loot tables can be [datagenned][datagen] by registering a `LootTableProvider` and providing a list of `LootTableSubProvider` in the constructor:

```java
@SubscribeEvent // on the mod event bus
public static void onGatherData(GatherDataEvent event) {
    event.getGenerator().addProvider(
            event.includeServer(),
            output -> new LootTableProvider(
                    output,
                    // A set of required table resource locations. These are later verified to be present.
                    // It is generally not recommended for mods to validate existence,
                    // therefore we pass in an empty set.
                    Set.of(),
                    // A list of sub provider entries. See below for what values to use here.
                    List.of(...),
                    // The registry access
                    event.getLookupProvider()
            )
    );
}
```

### `LootTableSubProvider`s

`LootTableSubProvider`s are where the actual generation happens. To get started, we implement `LootTableSubProvider` and override `#generate`:

```java
public class MyLootTableSubProvider implements LootTableSubProvider {
    // The parameter is provided by the lambda (see below). It can be stored and used to lookup other registry entries.
    public MyLootTableSubProvider(HolderLookup.Provider lookupProvider) {
        // Store the lookupProvider in a field
    }

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        // LootTable.lootTable() returns a loot table builder we can add loot tables to.
        consumer.accept(ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "example_loot_table"), LootTable.lootTable()
                // Add a loot table-level loot function. This example uses a number provider (see below).
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(5)))
                // Add a loot pool.
                .withPool(LootPool.lootPool()
                        // Add a loot pool-level function, similar to above.
                        .apply(...)
                        // Add a loot pool-level condition. This example only rolls the pool if it is raining.
                        .when(WeatherCheck.weather().setRaining(true))
                        // Set the amount of rolls and bonus rolls, respectively.
                        // Both of these methods utilize a number provider.
                        .setRolls(UniformGenerator.between(5, 9))
                        .setBonusRolls(ConstantValue.exactly(1))
                        // Add a loot entry. This example returns an item loot entry. See below for more loot entries.
                        .add(LootItem.lootTableItem(Items.DIRT))
                )
        );
    }
}
```

Once we have our loot table sub provider, we add it to the constructor of our loot provider, like so:

```java
new LootTableProvider(output, Set.of(), List.of(
        new SubProviderEntry(
                // A reference to the sub provider's constructor.
                // This is a Function<HolderLookup.Provider, ? extends LootTableSubProvider>.
                MyLootTableSubProvider::new,
                // An associated loot context set. If you're unsure what to use, use empty.
                LootContextParamSets.EMPTY
        ),
        // other sub providers here (if applicable)
    ), lookupProvider
);
```

### `BlockLootSubProvider`

`BlockLootSubProvider` is an abstract helper class containing many helpers for creating common block loot tables, e.g. single item drops (`#createSingleItemTable`), dropping the block the table is created for (`#dropSelf`), silk touch-only drops (`#createSilkTouchOnlyTable`), drops for slab-like blocks (`#createSlabItemTable`), and many more. Unfortunately, setting up a `BlockLootSubProvider` for modded usage involves more boilerplate:

```java
public class MyBlockLootSubProvider extends BlockLootSubProvider {
    // The constructor can be private if this class is an inner class of your loot table provider.
    // The parameter is provided by the lambda in the LootTableProvider's constructor.
    public MyBlockLootSubProvider(HolderLookup.Provider lookupProvider) {
        // The first parameter is a set of blocks we are creating loot tables for. Instead of hardcoding,
        // we use our block registry and just pass an empty set here.
        // The second parameter is the feature flag set, this will be the default flags
        // unless you are adding custom flags (which is beyond the scope of this article).
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    // The contents of this Iterable are used for validation.
    // We return an Iterable over our block registry's values here.
    @Override
    protected Iterable<Block> getKnownBlocks() {
        // The contents of our DeferredRegister.
        return MyRegistries.BLOCK_REGISTRY.getEntries()
                .stream()
                // Cast to Block here, otherwise it will be a ? extends Block and Java will complain.
                .map(e -> (Block) e.value())
                .toList();
    }

    // Actually add our loot tables.
    @Override
    protected void generate() {
        // Equivalent to calling add(MyBlocks.EXAMPLE_BLOCK.get(), createSingleItemTable(MyBlocks.EXAMPLE_BLOCK.get()));
        this.dropSelf(MyBlocks.EXAMPLE_BLOCK.get());
        // Add a table with a silk touch only loot table.
        this.add(MyBlocks.EXAMPLE_SILK_TOUCHABLE_BLOCK.get(),
                this.createSilkTouchOnlyTable(MyBlocks.EXAMPLE_SILK_TOUCHABLE_BLOCK.get()));
        // other loot table additions here
    }
}
```

We then add our sub provider to the loot table provider's constructor like any other sub provider:

```java
new LootTableProvider(output, Set.of(), List.of(new SubProviderEntry(
        MyBlockLootTableSubProvider::new,
        LootContextParamSets.BLOCK // it makes sense to use BLOCK here
    )), lookupProvider
);
```

### `EntityLootSubProvider`

Similar to `BlockLootSubProvider`, `EntityLootSubProvider` provides many helpers for entity loot table generation. Also similar to `BlockLootSubProvider`, we must provide a `Stream<EntityType<?>>` of entities known to the provider (instead of the `Iterable<Block>` used before). Overall, our implementation looks very similar to our `BlockLootSubProvider`, but with every mentioned of blocks swapped out for entity types:

```java
public class MyEntityLootSubProvider extends EntityLootSubProvider {
    public MyEntityLootSubProvider(HolderLookup.Provider lookupProvider) {
        // Unlike with blocks, we do not provide a set of known entity types. Vanilla instead uses custom checks here.
        super(FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    // This class uses a Stream instead of an Iterable, so we need to adjust this slightly.
    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return MyRegistries.ENTITY_TYPES.getEntries()
                .stream()
                .map(e -> (EntityType<?>) e.value());
    }

    @Override
    protected void generate() {
        this.add(MyEntities.EXAMPLE_ENTITY.get(), LootTable.lootTable());
        // other loot table additions here
    }
}
```

And again, we then add our sub provider to the loot table provider's constructor:

```java
new LootTableProvider(output, Set.of(), List.of(new SubProviderEntry(
        MyEntityLootTableSubProvider::new,
        LootContextParamSets.ENTITY
    )), lookupProvider
);
```

[advancement]: ../advancements.md
[binomial]: https://en.wikipedia.org/wiki/Binomial_distribution
[conditions]: ../conditions.md
[context]: #loot-context
[customentry]: custom.md#custom-loot-entry-types
[customlevelbased]: custom.md#custom-level-based-values
[customnumber]: custom.md#custom-number-providers
[damagesource]: ../damagetypes.md#creating-and-using-damage-sources
[datagen]: ../../index.md#data-generation
[entry]: #loot-entry
[glm]: glm.md
[lootcondition]: lootconditions
[lootfunction]: lootfunctions
[parameters]: #loot-parameters
[raidherogifts]: ../datamaps/builtin.md#neoforgeraid_hero_gifts
[sides]: ../../../concepts/sides.md
[tags]: ../tags.md
