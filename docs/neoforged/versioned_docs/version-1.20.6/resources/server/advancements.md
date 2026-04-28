# Advancements

Advancements are tasks that can be achieved by the player which may advance the progress of the game. Advancements can trigger based on any action the player may be directly involved in.

All advancement implementations within vanilla are data driven via JSON. This means that a mod is not necessary to create a new advancement, only a [data pack][datapack]. A full list on how to create and put these advancements within the mod's `resources` can be found on the [Minecraft Wiki][wiki]. Additionally, advancements can be [loaded conditionally and defaulted][conditional] depending on what information is present (mod loaded, item exists, etc.). As with other data driven features, advancements can be generated via [data generators][datagen].

## Advancement Criteria

To unlock an advancement, the specified criteria must be met. Criteria are tracked through triggers which execute when a certain action is performed: killing an entity, changing an inventory, breading animals, etc. Any time an advancement is loaded into the game, the criteria defined are read and added as listeners to the trigger. Afterwards a trigger function is called (usually named `#trigger`) which checks all listeners as to whether the current state meets the conditions of the advancement criteria. The criteria listeners for the advancement are only removed once the advancement has been obtained by completing all requirements.

Requirements are defined as an array of string arrays representing the name of the criteria specified on the advancement. An advancement is completed once one string array of criteria has been met:

```json5
// In some advancement JSON

// List of defined criteria to meet
"criteria": {
    "example_criterion1": { /*...*/ },
    "example_criterion2": { /*...*/ },
    "example_criterion3": { /*...*/ },
    "example_criterion4": { /*...*/ }
},

// This advancement is only unlocked once
// - Criteria 1 AND 2 have been met
// OR
// - Criteria 3 and 4 have been met
"requirements": [
    [
        "example_criterion1",
        "example_criterion2"
    ],
    [
        "example_criterion3",
        "example_criterion4"
    ]
]
```

A list of criteria triggers defined by vanilla can be found in `CriteriaTriggers`. Additionally, the JSON formats are defined on the [Minecraft Wiki][triggers].

### Custom Criteria Triggers

Custom criteria triggers are made up of two parts: the trigger, which is activated in code at some point you specify by calling `#trigger`, and the instance which defines the conditions under which the trigger should award the criterion. The trigger extends `SimpleCriterionTrigger<T>` while the instance implements `SimpleCriterionTrigger.SimpleInstance`. The generic value `T` represents the trigger instance type.

### The SimpleCriterionTrigger.SimpleInstance Implementation

The `SimpleCriterionTrigger.SimpleInstance` represents a single criteria defined in the `criteria` object. Trigger instances are responsible for holding the defined conditions, and returning whether the inputs match the condition.

Conditions are usually passed in through the constructor. The `SimpleCriterionTrigger.SimpleInstance` interface requires only one function, called `#player`, which returns the conditions the player must meet as an `Optional<ContextAwarePredicate>`. If the subclass is a Java record with a `player` parameter of this type (as below), the automatically generated `#player` method will suffice.

```java
public record ExampleTriggerInstance(Optional<ContextAwarePredicate> player, ItemPredicate item) implements SimpleCriterionTrigger.SimpleInstance {
    // extra methods here
}
```

:::note
Typically, trigger instances have static helper methods which construct the full `Criterion<T>` object from the arguments to the instance. This allows these instances to be easily created during data generation, but are optional.

```java
// In this example, EXAMPLE_TRIGGER is a DeferredHolder<CriterionTrigger<?>, ExampleTrigger>
public static Criterion<ExampleTriggerInstance> instance(ContextAwarePredicate player, ItemPredicate item) {
    return EXAMPLE_TRIGGER.get().createCriterion(new ExampleTriggerInstance(Optional.of(player), item));
}
```
:::

Finally, a method should be added which takes in the current data state and returns whether the user has met the necessary conditions. The conditions of the player are already checked through `SimpleCriterionTrigger#trigger(ServerPlayer, Predicate)`. Most trigger instances call this method `#matches`.

```java
// This method is unique for each instance and is as such not overridden
public boolean matches(ItemStack stack) {
    // Since ItemPredicate matches a stack, a stack is the input
    return this.item.test(stack);
}
```

### SimpleCriterionTrigger

The `SimpleCriterionTrigger<T>` subclass is responsible for specifying a codec to [serialize] the trigger instance `T` and supplying a method to check trigger instances and run attached listeners on success.

The latter is done by defining a method to check all trigger instances and run the listeners if their condition is met. This method takes in the `ServerPlayer` and whatever other data defined by the matching method in the `SimpleCriterionTrigger.SimpleInstance` subclass. This method should internally call `SimpleCriterionTrigger#trigger` to properly handle checking all listeners. Most trigger instances call this method `#trigger`.

```java
// This method is unique for each trigger and is as such not a method to override
public void trigger(ServerPlayer player, ItemStack stack) {
    this.trigger(player,
        // The condition checker method within the SimpleCriterionTrigger.SimpleInstance subclass
        triggerInstance -> triggerInstance.matches(stack)
    );
}
```

Finally, instances must be registered on the `Registries.TRIGGER_TYPE` registry. Techniques for doing so can be found under [Registries][registration].

### Serialization

A [codec] must be defined to serialize and deserialize the trigger instance. Vanilla typically creates this codec as a constant within the instance implementation that is then returned by the trigger's `#codec` method.


```java
class ExampleTrigger extends SimpleCriterionTrigger<ExampleTrigger.ExampleTriggerInstance> {

    @Override
    public Codec<ExampleTriggerInstance> codec() {
        return ExampleTriggerInstance.CODEC;
    }

    // ...

    public class ExampleTriggerInstance implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<ExampleTriggerInstance> CODEC = ...;

        // ...
    }
}
```

For the earlier example of a record with a `ContextAwarePredicate` and an `ItemPredicate`, the codec could be:

```java
RecordCodecBuilder.create(instance -> instance.group(
    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ExampleTriggerInstance::player),
    ItemPredicate.CODEC.fieldOf("item").forGetter(ExampleTriggerInstance::item)
).apply(instance, ExampleTriggerInstance::new));
```

### Calling the Trigger

Whenever the action being checked is performed, the `#trigger` method defined by the `SimpleCriterionTrigger` subclass should be called.

```java
// In some piece of code where the action is being performed
// Again, EXAMPLE_TRIGGER is a supplier for the registered instance of the custom criteria trigger
public void performExampleAction(ServerPlayer player, ItemStack stack) {
    // Run code to perform action
    EXAMPLE_TRIGGER.get().trigger(player, stack);
}
```

## Advancement Rewards

When an advancement is completed, rewards may be given out. These can be a combination of experience points, loot tables, recipes for the recipe book, or a [function] executed as a creative player.

```json5
// In some advancement JSON
"rewards": {
    "experience": 10,
    "loot": [
        "examplemod:example_loot_table",
        "examplemod:example_loot_table2"
        // ...
    ],
    "recipes": [
        "examplemod:example_recipe",
        "examplemod:example_recipe2"
        // ...
    ],
    "function": "examplemod:example_function"
}
```

[datapack]: https://minecraft.wiki/w/Data_pack
[wiki]: https://minecraft.wiki/w/Advancement/JSON_format
[conditional]: ./conditional.md#implementations
[function]: https://minecraft.wiki/w/Function_(Java_Edition)
[triggers]: https://minecraft.wiki/w/Advancement/JSON_format#List_of_triggers
[datagen]: ../../datagen/advancements.md#advancement-generation
[codec]: ../../datastorage/codecs.md
[registration]: ../../concepts/registries.md#methods-for-registering
[serialize]: #serialization
