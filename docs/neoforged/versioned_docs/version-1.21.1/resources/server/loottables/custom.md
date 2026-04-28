# Custom Loot Objects

Due to the complexity of the loot table system, there are several [registries] at work, all of which can be used by a modder to add more behavior.

All loot table related registries follow a similar pattern. To add a new registry entry, you generally extend some class or implement some interface that holds your functionality. Then, you define a [codec] for serialization, and register that codec to the corresponding registry, using `DeferredRegister` like normal. This goes along with the "one base object, many instances" approach most registries (for example also blocks/blockstates and items/item stacks) use.

## Custom Loot Entry Types

To create a custom loot entry type, extend `LootPoolEntryContainer` or one of its two direct subclasses, `LootPoolSingletonContainer` or `CompositeEntryBase`. For the sake of example, we want to create a loot entry type that returns the drops of a entity - this is purely for example purposes, in practice it would be more ideal to directly reference the other loot table. Let's start by creating our loot entry type class:

```java
// We extend LootPoolSingletonContainer since we have a "finite" set of drops.
// Some of this code is adapted from NestedLootTable.
public class EntityLootEntry extends LootPoolSingletonContainer {
    // A Holder for the entity type we want to roll the other table for.
    private final Holder<EntityType<?>> entity;

    // It is common practice to have a private constructor and have a static factory method.
    // This is because weight, quality, conditions, and functions are supplied by a lambda below.
    private EntityLootEntry(Holder<EntityType<?>> entity, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        // Pass lambda-provided parameters to super.
        super(weight, quality, conditions, functions);
        // Set our values.
        this.entity = entity;
    }

    // Static builder method, accepting our custom parameters and combining them with a lambda that supplies the values common to all entry types.
    public static LootPoolSingletonContainer.Builder<?> entityLoot(Holder<EntityType<?>> entity) {
        // Use the static simpleBuilder() method defined in LootPoolSingletonContainer.
        return simpleBuilder((weight, quality, conditions, functions) -> new EntityLootEntry(entity, weight, quality, conditions, functions));
    }

    // This is where the magic happens. To add an item stack, we generally call #accept on the consumer.
    // However, in this case, we let #getRandomItems do that for us.
    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        // Get the entity's loot table. If it doesn't exist, an empty loot table will be returned, so null-checking is not necessary.
        LootTable table = context.getLevel().reloadableRegistries().getLootTable(entity.value().getDefaultLootTable());
        // Use the raw version here, because vanilla does it too. :P
        // #getRandomItemsRaw calls consumer#accept for us on the results of the roll.
        table.getRandomItemsRaw(context, consumer);
    }
}
```

Next up, we create a `MapCodec` for our loot entry:

```java
// This is placed as a constant in EntityLootEntry.
public static final MapCodec<EntityLootEntry> CODEC = RecordCodecBuilder.mapCodec(inst ->
        // Add our own fields.
        inst.group(
                        // A value referencing an entity type id.
                        BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().fieldOf("entity").forGetter(e -> e.entity)
                )
                // Add common fields: weight, display, conditions, and functions.
                .and(singletonFields(inst))
                .apply(inst, EntityLootEntry::new)
);
```

We then use this codec in registration:

```java
public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES =
        DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, ExampleMod.MOD_ID);

public static final Supplier<LootPoolEntryType> ENTITY_LOOT =
        LOOT_POOL_ENTRY_TYPES.register("entity_loot", () -> new LootPoolEntryType(EntityLootEntry.CODEC));
```

Finally, in our loot entry class, we must override `getType()`:

```java
public class EntityLootEntry extends LootPoolSingletonContainer {
    // other stuff here

    @Override
    public LootPoolEntryType getType() {
        return ENTITY_LOOT.get();
    }
}
```

## Custom Number Providers

To create a custom number provider, implement the `NumberProvider` interface. For the sake of example, let's assume we want to create a number provider that changes the sign of the provided number:

```java
// We accept another number provider as our base.
public record InvertedSignProvider(NumberProvider base) implements NumberProvider {
    public static final MapCodec<InvertedSignProvider> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            NumberProviders.CODEC.fieldOf("base").forGetter(InvertedSignProvider::base)
    ).apply(inst, InvertedSignProvider::new));

    // Return a float value. Use the context and the record parameters as needed.
    @Override
    public float getFloat(LootContext context) {
        return -this.base.getFloat(context);
    }

    // Return an int value. Use the context and the record parameters as needed.
    // Overriding this is optional, the default implementation will round the result of #getFloat.
    @Override
    public int getInt(LootContext context) {
        return -this.base.getInt(context);
    }

    // Return a set of the loot context params used by this provider. See below for more information.
    // Since we have a base value, we just defer to the base.
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.base.getReferencedContextParams();
    }
}
```

Like with custom loot entry types, we then use this codec in registration:

```java
public static final DeferredRegister<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPES =
        DeferredRegister.create(Registries.LOOT_NUMBER_PROVIDER_TYPE, ExampleMod.MOD_ID);

public static final Supplier<LootNumberProviderType> INVERTED_SIGN =
        LOOT_NUMBER_PROVIDER_TYPES.register("inverted_sign", () -> new LootNumberProviderType(InvertedSignProvider.CODEC));
```

And similarly, in our number provider class, we must override `getType()`:

```java
public record InvertedSignProvider(NumberProvider base) implements NumberProvider {
    // other stuff here

    @Override
    public LootNumberProviderType getType() {
        return INVERTED_SIGN.get();
    }
}
```

## Custom Level-Based Values

Custom `LevelBasedValue`s can be created by implementing the `LevelBasedValue` interface in a record. Again, for the sake of example, let's assume that we want to invert the output of another `LevelBasedValue`:

```java
public record InvertedSignLevelBasedValue(LevelBasedValue base) implements LevelBaseValue {
    public static final MapCodec<InvertedLevelBasedValue> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LevelBasedValue.CODEC.fieldOf("base").forGetter(InvertedLevelBasedValue::base)
    ).apply(inst, InvertedLevelBasedValue::new));

    // Perform our operation.
    @Override
    public float calculate(int level) {
        return -this.base.calculate(level);
    }

    // Unlike NumberProviders, we don't return the registered type, instead we return the codec directly.
    @Override
    public MapCodec<InvertedLevelBasedValue> codec() {
        return CODEC;
    }
}
```

And again, we then use the codec in registration, though this time directly:

```java
public static final DeferredRegister<MapCodec<? extends LevelBasedValue>> LEVEL_BASED_VALUES =
        DeferredRegister.create(Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, ExampleMod.MOD_ID);

public static final Supplier<MapCodec<? extends LevelBasedValue>> INVERTED_SIGN =
        LEVEL_BASED_VALUES.register("inverted_sign", () -> InvertedSignLevelBasedValue.CODEC);
```

## Custom Loot Conditions

To get started, we create our loot item condition class that implements `LootItemCondition`. For the sake of example, let's assume we only want the condition to pass if the player killing the mob has a certain xp level:

```java
public record HasXpLevelCondition(int level) implements LootItemCondition {
    // Add the context we need for this condition. In our case, this will be the xp level the player must have.
    public static final MapCodec<HasXpLevelCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.fieldOf("level").forGetter(HasXpLevelCondition::level)
    ).apply(inst, HasXpLevelCondition::new));
    
    // Evaluates the condition here. Get the required loot context parameters from the provided LootContext.
    // In our case, we want the KILLER_ENTITY to have at least our required level.
    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        return entity instanceof Player player && player.experienceLevel >= level; 
    }
    
    // Tell the game what parameters we expect from the loot context. Used in validation.
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
    }
}
```

We can register the condition type to the registry using the condition's codec:

```java
public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES =
        DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ExampleMod.MOD_ID);

public static final Supplier<LootItemConditionType> MIN_XP_LEVEL =
        LOOT_CONDITION_TYPES.register("min_xp_level", () -> new LootItemConditionType(HasXpLevelCondition.CODEC));
```

After we have done that, we need to override `#getType` in our condition and return the registered type:

```java
public record HasXpLevelCondition(int level) implements LootItemCondition {
    // other stuff here

    @Override
    public LootItemConditionType getType() {
        return MIN_XP_LEVEL.get();
    }
}
```

## Custom Loot Functions

To get started, we create our own class extending `LootItemFunction`. `LootItemFunction` extends `BiFunction<ItemStack, LootContext, ItemStack>`, so what we want is to use the existing item stack and the loot context to return a new, modified item stack. However, almost all loot functions don't directly extend `LootItemFunction`, but extend `LootItemConditionalFunction` instead. This class has built-in functionality for applying loot conditions to the function - the function is only applied if the loot conditions apply. For the sake of example, let's apply a random enchantment with a specified level to the item:

```java
// Code adapted from vanilla's EnchantRandomlyFunction class.
// LootItemConditionalFunction is an abstract class, not an interface, so we cannot use a record here.
public class RandomEnchantmentWithLevelFunction extends LootItemConditionalFunction {
    // Our context: an optional list of enchantments, and a level.
    private final Optional<HolderSet<Enchantment>> enchantments;
    private final int level;
    // Our codec.
    public static final MapCodec<RandomEnchantmentWithLevelFunction> CODEC =
            // #commonFields adds the conditions field.
            RecordCodecBuilder.mapCodec(inst -> commonFields(inst).and(inst.group(
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(e -> e.enchantments),
                    Codec.INT.fieldOf("level").forGetter(e -> e.level)
            ).apply(inst, RandomEnchantmentWithLevelFunction::new));
    
    public RandomEnchantmentWithLevelFunction(List<LootItemCondition> conditions, Optional<HolderSet<Enchantment>> enchantments, int level) {
        super(conditions);
        this.enchantments = enchantments;
        this.level = level;
    }
    
    // Run our enchantment application logic. Most of this is copied from EnchantRandomlyFunction#run.
    @Override
    public ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        List<Holder<Enchantment>> stream = this.enchantments
                .map(HolderSet::stream)
                .orElseGet(() -> context.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().map(Function.identity()))
                .filter(e -> e.value().canEnchant(stack))
                .toList();
        Optional<Holder<Enchantment>> optional = Util.getRandomSafe(list, random);
        if (optional.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", stack);
        } else {
            if (stack.is(Items.BOOK)) {
                stack = new ItemStack(Items.ENCHANTED_BOOK);
            }
            stack.enchant(enchantment, Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel()));
        }
        return stack;
    }
}
```

We can then register the function type to the registry using the function's codec:

```java
public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
        DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, ExampleMod.MOD_ID);

public static final Supplier<LootItemFunctionType<RandomEnchantmentWithLevelFunction>> RANDOM_ENCHANTMENT_WITH_LEVEL =
        LOOT_FUNCTION_TYPES.register("random_enchantment_with_level", () -> new LootItemFunctionType(RandomEnchantmentWithLevelFunction.CODEC));
```

After we have done that, we need to override `#getType` in our condition and return the registered type:

```java
public class RandomEnchantmentWithLevelFunction extends LootItemConditionalFunction {
    // other stuff here

    @Override
    public LootItemFunctionType getType() {
        return RANDOM_ENCHANTMENT_WITH_LEVEL.get();
    }
}
```

[codec]: ../../../datastorage/codecs.md
[registries]: ../../../concepts/registries.md
