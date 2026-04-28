# Advancements

Advancements are quest-like tasks that can be achieved by the player. Advancements are awarded based on advancement criteria, and can run behavior when completed.

A new advancement can be added by creating a JSON file in your namespace's `advancement` subfolder. So for example, if we want to add an advancement named `example_name` for a mod with the mod id `examplemod`, it will be located at `data/examplemod/advancement/example_name.json`. An advancement's ID will be relative to the `advancement` directory, so for our example, it would be `examplemod:example_name`. Any name can be chosen, and the advancement will automatically be picked up by the game. Java code is only necessary if you want to add new criteria or trigger a certain criterion from code (see below).

## Specification

An advancement JSON file may contain the following entries:

- `parent`: The parent advancement ID of this advancement. Circular references will be detected and cause a loading failure. Optional; if absent, this advancement will be considered a root advancement. Root advancements are advancements that have no parent set. They will be the root of their [advancement tree][tree].
- `display`: The object holding several properties used for display of the advancement in the advancement GUI. Optional; if absent, this advancement will be invisible, but can still be triggered.
    - `icon`: A [JSON representation of an item stack][itemstackjson].
    - `text`: A [text component][text] to use as the advancement's title.
    - `description`: A [text component][text] to use as the advancement's description.
    - `frame`: The frame type of the advancement. Accepts `challenge`, `goal` and `task`. Optional, defaults to `task`.
    - `background`: The texture to use for the tree background. This is not relative to the `textures` directory, i.e. the `textures/` folder prefix must be included. Optional, defaults to the missing texture. Only effective on root advancements.
    - `show_toast`: Whether to show a toast in the top right corner on completion. Optional, defaults to true.
    - `announce_to_chat`: Whether to announce advancement completion in the chat. Optional, defaults to true.
    - `hidden`: Whether to hide this advancement and all children from the advancement GUI until it is completed. Has no effect on root advancements themselves, but still hides all of their children. Optional, defaults to false.
- `criteria`: A map of criteria this advancement should track. Every criterion is identified by its map key. A list of criteria triggers added by Minecraft can be found in the `CriteriaTriggers` class, and the JSON specifications can be found on the [Minecraft Wiki][triggers]. For implementing your own criteria or triggering criteria from code, see below.
- `requirements`: A list of lists that determine what criteria are required. This is a list of OR lists that are ANDed together, or in other words, every sublist must have at least one criterion matching. Optional, defaults to all criteria being required.
- `rewards`: An object representing the rewards to grant when this advancement is completed. Optional, all values of the object are also optional.
    - `experience`: The amount of experience to award to the player.
    - `recipes`: A list of [recipe] IDs to unlock.
    - `loot`: A list of [loot tables][loottable] to roll and give to the player.
    - `function`: A [function] to run. If you want to run multiple functions, create a wrapper function that runs all other functions.
- `sends_telemetry_event`: Determines whether telemetry data should be collected when this advancement is completed or not. Only actually does anything if in the `minecraft` namespace. Optional, defaults to false.
- `neoforge:conditions`: NeoForge-added. A list of [conditions] that must be passed for the advancement to be loaded. Optional.

### Advancement Trees

Advancement files may be grouped in directories, which tells the game to create multiple advancement tabs. One advancement tab may contain one or more advancement trees, depending on the amount of root advancements. Empty advancement tabs will automatically be hidden.

:::tip
Minecraft only ever has one root advancement per tab, and always calls the root advancement `root`. It is suggested to follow this practice.
:::

## Criteria Triggers

To unlock an advancement, the specified criteria must be met. Criteria are tracked through triggers, which are executed from code when the associated action happens (e.g. the `player_killed_entity` trigger executes when the player kills the specified entity). Any time an advancement is loaded into the game, the criteria defined are read and added as listeners to the trigger. When a trigger is executed, all advancements that have a listener for the corresponding criterion are rechecked for completion. If the advancement is completed, the listeners are removed.

Custom criteria triggers are made up of two parts: the trigger, which is activated in code by calling `#trigger`, and the instance which defines the conditions under which the trigger should award the criterion. The trigger extends `SimpleCriterionTrigger<T>` while the instance implements `SimpleCriterionTrigger.SimpleInstance`. The generic value `T` represents the trigger instance type.

### `SimpleCriterionTrigger.SimpleInstance`

A `SimpleCriterionTrigger.SimpleInstance` represents a single criterion defined in the `criteria` object. Trigger instances are responsible for holding the defined conditions, and returning whether the inputs match the condition.

Conditions are usually passed in through the constructor. The `SimpleCriterionTrigger.SimpleInstance` interface requires only one function, called `#player`, which returns the conditions the player must meet as an `Optional<ContextAwarePredicate>`. If the subclass is a record with a `player` parameter of this type (as below), the automatically generated `#player` method will suffice.

```java
public record ExampleTriggerInstance(Optional<ContextAwarePredicate> player/*, other parameters here*/)
        implements SimpleCriterionTrigger.SimpleInstance {}
```

Typically, trigger instances have static helper methods which construct the full `Criterion<T>` object from the arguments to the instance. This allows these instances to be easily created during data generation, but are optional.

```java
// In this example, EXAMPLE_TRIGGER is a DeferredHolder<CriterionTrigger<?>, ExampleTrigger>.
// See below for how to register triggers.
public static Criterion<ExampleTriggerInstance> instance(ContextAwarePredicate player, ItemPredicate item) {
    return EXAMPLE_TRIGGER.get().createCriterion(new ExampleTriggerInstance(Optional.of(player), item));
}
```

Finally, a method should be added which takes in the current data state and returns whether the user has met the necessary conditions. The conditions of the player are already checked through `SimpleCriterionTrigger#trigger(ServerPlayer, Predicate)`. Most trigger instances call this method `#matches`.

```java
// Let's assume we have an additional ItemPredicate parameter. This can be whatever you need.
// For example, this could also be a Predicate<LivingEntity>.
public record ExampleTriggerInstance(Optional<ContextAwarePredicate> player, ItemPredicate predicate)
        implements SimpleCriterionTrigger.SimpleInstance {
    // This method is unique for each instance and is as such not overridden.
    // The parameter may be whatever you need to properly match, for example, this could also be a LivingEntity.
    // If you need no context other than the player, this may also take no parameters at all.
    public boolean matches(ItemStack stack) {
        // Since ItemPredicate matches a stack, we use a stack as the input here.
        return this.predicate.test(stack);
    }
}
```

### `SimpleCriterionTrigger`

The `SimpleCriterionTrigger<T>` implementation has two purposes: supplying a method to check trigger instances and run attached listeners on success, and specifying a [codec] to serialize the trigger instance (`T`).

First, we want to add a method that takes the inputs we need and calls `SimpleCriterionTrigger#trigger` to properly handle checking all listeners. Most trigger instances also name this method `#trigger`. Reusing our example trigger instance from above, our trigger would look something like this:

```java
public class ExampleCriterionTrigger extends SimpleCriterionTrigger<ExampleTriggerInstance> {
    // This method is unique for each trigger and is as such not a method to override
    public void trigger(ServerPlayer player, ItemStack stack) {
        this.trigger(player,
                // The condition checker method within the SimpleCriterionTrigger.SimpleInstance subclass
                triggerInstance -> triggerInstance.matches(stack)
        );
    }
}
```

Triggers must be registered to the `Registries.TRIGGER_TYPE` [registry][registration]:

```java
public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
        DeferredRegister.create(Registries.TRIGGER_TYPE, ExampleMod.MOD_ID);

public static final Supplier<ExampleCriterionTrigger> EXAMPLE_TRIGGER =
        TRIGGER_TYPES.register("example", ExampleCriterionTrigger::new);
```

And then, triggers must define a [codec] to serialize and deserialize the trigger instance by overriding `#codec`. This codec is typically created as a constant within the instance implementation.

```java
public record ExampleTriggerInstance(Optional<ContextAwarePredicate> player/*, other parameters here*/)
        implements SimpleCriterionTrigger.SimpleInstance {
    public static final Codec<ExampleTriggerInstance> CODEC = ...;

    // ...
}

public class ExampleTrigger extends SimpleCriterionTrigger<ExampleTriggerInstance> {
    @Override
    public Codec<ExampleTriggerInstance> codec() {
        return ExampleTriggerInstance.CODEC;
    }

    // ...
}
```

For the earlier example of a record with a `ContextAwarePredicate` and an `ItemPredicate`, the codec could be:

```java
public static final Codec<ExampleTriggerInstace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ExampleTriggerInstance::player),
        ItemPredicate.CODEC.fieldOf("item").forGetter(ExampleTriggerInstance::item)
).apply(instance, ExampleTriggerInstance::new));
```

### Calling Criterion Triggers

Whenever the action being checked is performed, the `#trigger` method defined by our `SimpleCriterionTrigger` subclass should be called. Of course, you can also call on vanilla triggers, which are found in `CriteriaTriggers`.

```java
// In some piece of code where the action is being performed
// Again, EXAMPLE_TRIGGER is a supplier for the registered instance of the custom criterion trigger
public void performExampleAction(ServerPlayer player, additionalContextParametersHere) {
    // Run code to perform action here
    EXAMPLE_TRIGGER.get().trigger(player, additionalContextParametersHere);
}
```

## Data Generation

Advancements can be [datagenned][datagen] using an `AdvancementProvider`. An `AdvancementProvider` accepts a list of `AdvancementGenerator`s, which actually generate the advancements using `Advancement.Builder`.

:::warning
Both Minecraft and NeoForge provide a class named `AdvancementProvider`, located at `net.minecraft.data.advancements.AdvancementProvider` and `net.neoforged.neoforge.common.data.AdvancementProvider`, respectively. The NeoForge class is an improvement on the one Minecraft provides, and should always be used in favor of the Minecraft one. The following documentation always assumes usage of the NeoForge `AdvancementProvider` class.
:::

To start, create an instance of `AdvancementProvider` within `GatherDataEvent`:

```java
@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput output = generator.getPackOutput();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

    generator.addProvider(
            event.includeServer(),
            new AdvancementProvider(
                output, lookupProvider, existingFileHelper,
                // Add generators here
                List.of(...)
            )
    );

     // Other providers
}
```

Now, the next step is to fill the list with our generators. To do so, we can either add generators as classes or lambdas, and then add an instance of each of them to the currently empty list in the constructor parameter.

```java
// Class example
public class MyAdvancementGenerator extends AdvancementProvider.AdvancementGenerator {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        // Generate your advancements here.
    }
}

// Method Example
public class ExampleClass {

    // Matches the parameters provided by AdvancementProvider.AdvancementGenerator#generate
    public static void generateExampleAdvancements(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        // Generate your advancements here.
    }
}

// In GatherDataEvent
generator.addProvider(
    event.includeServer(),
    new AdvancementProvider(
        output, lookupProvider, existingFileHelper,
        // Add generators here
        List.of(
            // Add an instance of our generator to the list parameter. This can be done as many times as you want.
            // Having multiple generators is purely for organization, all functionality can be achieved with a single generator.
            new MyAdvancementGenerator(),
            ExampleClass::generateExampleAdvancements
        )
    )
);
```

To generate an advancement, you want to use an `Advancement.Builder`:

```java
// All methods follow the builder pattern, meaning that chaining is possible and encouraged.
// For better readability of the explanations, chaining will not be done here.

// Create an advancement builder using the static #advancement() method.
// Using #advancement() automatically enables telemetry events. If you do not want this,
// #recipeAdvancement() can be used instead, there are no other functional differences.
Advancement.Builder builder = Advancement.Builder.advancement();

// Sets the parent of the advancement. You can use another advancement you have already generated,
// or create a placeholder advancement using the static AdvancementSubProvider#createPlaceholder method.
builder.parent(AdvancementSubProvider.createPlaceholder("minecraft:story/root"));

// Sets the display properties of the advancement. This can either be a DisplayInfo object,
// or pass in the values directly. If values are passed in directly, a DisplayInfo object will be created for you.
builder.display(
        // The advancement icon. Can be an ItemStack or an ItemLike.
        new ItemStack(Items.GRASS_BLOCK),
        // The advancement title and description. Don't forget to add translations for these!
        Component.translatable("advancements.examplemod.example_advancement.title"),
        Component.translatable("advancements.examplemod.example_advancement.description"),
        // The background texture. Use null if you don't want a background texture (for non-root advancements).
        null,
        // The frame type. Valid values are AdvancementType.TASK, CHALLENGE, or GOAL.
        AdvancementType.GOAL,
        // Whether to show the advancement toast or not.
        true,
        // Whether to announce the advancement into chat or not.
        true,
        // Whether the advancement should be hidden or not.
        false
);

// An advancement reward builder. Can be created with any of the four reward types, and further rewards
// can be added using the methods prefixed with add. This can also be built beforehand,
// and the resulting AdvancementRewards can then be reused across multiple advancement builders.
builder.rewards(
    // Alternatively, use addExperience() to add to an existing builder.
    AdvancementRewards.Builder.experience(100)
    // Alternatively, use loot() to create a new builder.
    .addLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("minecraft", "chests/igloo")))
    // Alternatively, use recipe() to create a new builder.
    .addRecipe(ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ingot")))
    // Alternatively, use function() to create a new builder.
    .runs(ResourceLocation.fromNamespaceAndPath("examplemod", "example_function"))
);

// Adds a criterion with the given name to the advancement. Use the corresponding trigger instance's static method.
builder.addCriterion("pickup_dirt", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIRT));

// Adds a requirements handler. Minecraft natively provides allOf() and anyOf(), more complex requirements
// must be implemented manually. Only has an effect with two or more criteria.
builder.requirements(AdvancementRequirements.allOf(List.of("pickup_dirt")));

// Save the advancement to disk, using the given resource location. This returns an AdvancementHolder,
// which may be stored in a variable and used as a parent by other advancement builders.
builder.save(saver, ResourceLocation.fromNamespaceAndPath("examplemod", "example_advancement"), existingFileHelper);
```

[codec]: ../../datastorage/codecs.md
[conditions]: conditions.md
[datagen]: ../index.md#data-generation
[function]: https://minecraft.wiki/w/Function_(Java_Edition)
[itemstackjson]: ../../items/index.md#json-representation
[loottable]: loottables/index.md
[recipe]: recipes/index.md
[registration]: ../../concepts/registries.md#methods-for-registering
[root]: #root-advancements
[text]: ../client/i18n.md#components
[tree]: #advancement-trees
[triggers]: https://minecraft.wiki/w/Advancement/JSON_format#List_of_triggers
