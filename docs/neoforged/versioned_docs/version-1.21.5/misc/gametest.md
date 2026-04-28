import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Game Tests

Game Tests are a way to run in-game unit tests. The system was designed to be scalable and in parallel to run large numbers of different tests efficiently. Testing object interactions and behaviors are simply a few of the many applications of this framework. As the system can either be implemented fully in-code or via [datapacks], both will be shown below.

## Creating a Game Test

A standard Game Test follows four basic steps:

1. A structure, or template, is loaded holding the scene on which the interaction or behavior is tested.
1. An environment for the test to run in.
1. A registered function to run the logic. If a sucessful state is reached, then the test succeeds. Otherwise, the test fails and the result is stored within a lectern adjacent to the scene.
1. A test instance to link the other three objects together.

## The Test Data

All test instances hold some `TestData` which defines how a game test should be run, from its initial configurations to the environment and structure template to use. As the `TestData` is serialized as a `MapCodec`, the data is stored at the root level of the file along with all the other instance-specific parameters.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some game test examplemod:example_test
// In 'data/examplemod/test_instance/example_test.json'
{
    // `TestData`

    // The environment to run the test in
    // Points to 'data/examplemod/test_environment/example_environment.json'
    "environment": "examplemod:example_environment",

    // The structure used for the game test
    // Points to 'data/examplemod/structure/example_structure.nbt'
    "structure": "examplemod:example_structure",

    // The number of ticks that the game test will run until it automatically fails
    "max_ticks": 400,

    // The number of ticks that are used to setup everying required for the game test
    // This is not counted towards the maximum number of ticks the test can take
    // If not specified, defaults to 0
    "setup_ticks": 50,

    // Whether the test is required to succeed to mark the batch run as successful
    // If not specified, defaults to true
    "required": true,

    // Specifies how the structure and all subsequent helper methods should be rotated for the test
    // If not specified, nothing is rotated
    // Can be 'none', 'clockwise_90', '180', 'counterclockwise_90'
    "rotation": "clockwise_90",

    // When true, the test can only be ran through the `/test` command
    // If not specified, defaults to false
    "manual_only": true,

    // Specifies the maximum number of times that the test can be reran
    // If not specified, defaults to 1
    "max_attempts": 3,

    // Specifies the minimum number of successes that must occur for a test to be marked as successful
    // This must be less than or equal to the maximum number of attempts allowed
    // If not specified, defaults to 1
    "required_successes": 1,

    // Returns whether the structure boundary should keep the top empty
    // This is currently only used in block-based test instances
    // If not specified, defaults to false 
    "sky_access": false

    // ...
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_INSTANCE, bootstrap -> {
            // Use this to get the test environments
            HolderGetter<TestEnvironmentDefinition> environments = bootstrap.lookup(Registries.TEST_ENVIRONMENT);

            // Register a game test
            // Any fields not relevant to the test data are hidden
            bootstrap.register(..., new FunctionGameTestInstance(...,
                new TestData<>(
                    // The environment to run the test in
                    // Points to 'data/examplemod/test_environment/example_environment.json'
                    environments.getOrThrow(EXAMPLE_ENVIRONMENT),

                    // The structure used for the game test
                    // Points to 'data/examplemod/structure/example_structure.nbt'
                    ResourceLocation.fromNamespaceAndPath("examplemod", "example_structure"),

                    // The number of ticks that the game test will run until it automatically fails
                    400,

                    // The number of ticks that are used to setup everying required for the game test
                    // This is not counted towards the maximum number of ticks the test can take
                    // If not specified, defaults to 0
                    50,

                    // Whether the test is required to succeed to mark the batch run as successful
                    // If not specified, defaults to true
                    true,

                    // Specifies how the structure and all subsequent helper methods should be rotated for the test
                    // If not specified, nothing is rotated
                    // Can be 'none', 'clockwise_90', '180', 'counterclockwise_90'
                    Rotation.CLOCKWISE_90,

                    // When true, the test can only be ran through the `/test` command
                    // If not specified, defaults to false
                    true,

                    // Specifies the maximum number of times that the test can be reran
                    // If not specified, defaults to 1
                    3,

                    // Specifies the minimum number of successes that must occur for a test to be marked as successful
                    // This must be less than or equal to the maximum number of attempts allowed
                    // If not specified, defaults to 1
                    1,

                    // Returns whether the structure boundary should keep the top empty
                    // This is currently only used in block-based test instances
                    // If not specified, defaults to false 
                    false
                )
            ));
        })
    );
}
```

</TabItem>
</Tabs>

## Structure Templates

Game Tests are performed within scenes loaded by structures, or templates. All templates define the dimensions of the scene and the initial data (blocks and entities) that will be loaded. The template must be stored as an `.nbt` file within `data/<namespace>/structure`. `TestData#structure` references the NBT file using a relative `ResourceLocation` (e.g., `examplemod:example_structure` points to `data/examplemod/structure/example_structure.nbt`)

## Test Environments

All game tests run in some `TestEnvironmentDefinition`, determining how the current `ServerLevel` should be set up. Then, once the test has finished, the environment is tore down, letting the next instance or instances run. All environments are batched, meaning that if multiple test instances have the same environment, they will run at the same time. All test environments are located within `data/<namespace>/test_environment/<path>.json`.

Vanilla provides `minecraft:default`, which does not modify the `ServerLevel`. However, there are other supported definition types that can be used to construct an environment.

### Game Rules

This environment type sets the game rules to use for the test. During teardown, the game rules are reset to their default value.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// examplemod:example_environment
// In 'data/examplemod/test_environment/example_environment.json'
{
    "type": "minecraft:game_rules",

    // A list of game rules with boolean values to set
    "bool_rules": [
        {
            // The name of the rule
            "rule": "doFireTick",
            "value": false
        }
        // ...
    ],

    // A list of game rules with integer values to set
    "int_rules": [
        {
            "rule": "playersSleepingPercentage",
            "value": 50
        }
        // ...
    ]
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_ENVIRONMENT, bootstrap -> {

            // Register the environment
            bootstrap.register(
                EXAMPLE_ENVIRONMENT,
                new TestEnvironmentDefinition.SetGameRules(
                    // A list of game rules with boolean values to set
                    List.of(
                        new TestEnvironmentDefinition.SetGameRules.Entry(
                            // The game rule
                            GameRules.RULE_DOFIRETICK,
                            GameRules.BooleanValue.create(false)
                        )
                        // ...
                    ),
                    // A list of game rules with integer values to set
                    List.of(
                        new TestEnvironmentDefinition.SetGameRules.Entry(
                            // The game rule
                            GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE,
                            GameRules.IntegerValue.create(50)
                        )
                        // ...
                    )
                )
            );
        })
    );
}
```

</TabItem>
</Tabs>

### Time of Day

This environment type sets the time to some non-negative integer, like how the `/time set <number>` command is used.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// examplemod:example_environment
// In 'data/examplemod/test_environment/example_environment.json'
{
    "type": "minecraft:time_of_day",

    // Sets the time of day in the world
    // Common values:
    // - Day      -> 1000
    // - Noon     -> 6000
    // - Night    -> 13000
    // - Midnight -> 18000
    "time": 13000
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_ENVIRONMENT, bootstrap -> {

            // Register the environment
            bootstrap.register(
                EXAMPLE_ENVIRONMENT,
                new TestEnvironmentDefinition.TimeOfDay(
                    // Sets the time of day in the world
                    // Common values:
                    // - Day      -> 1000
                    // - Noon     -> 6000
                    // - Night    -> 13000
                    // - Midnight -> 18000
                    13000
                )
            );
        })
    );
}
```

</TabItem>
</Tabs>

### Weather

This environment type sets the weather, like to how the `/weather` command is used.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// examplemod:example_environment
// In 'data/examplemod/test_environment/example_environment.json'
{
    "type": "minecraft:weather",

    // Can be one of three values:
    // - clear   (No weather)
    // - rain    (Rain)
    // - thunder (Rain and thunder)
    "weather": "thunder"
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_ENVIRONMENT, bootstrap -> {

            // Register the environment
            bootstrap.register(
                EXAMPLE_ENVIRONMENT,
                new TestEnvironmentDefinition.Weather(
                    // Can be one of three values:
                    // - clear   (No weather)
                    // - rain    (Rain)
                    // - thunder (Rain and thunder)
                    TestEnvironmentDefinition.Weather.Type.THUNDER
                )
            );
        })
    );
}
```

</TabItem>
</Tabs>

### Minecraft Functions

This environment type provides two ResourceLocations to `mcfunction`s to setup and teardown the level, respectively.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// examplemod:example_environment
// In 'data/examplemod/test_environment/example_environment.json'
{
    "type": "minecraft:function",

    // The setup mcfunction to use
    // If not specified, nothing will be ran
    // Points to 'data/examplemod/function/example/setup.mcfunction'
    "setup": "examplemod:example/setup",

    // The teardown mcfunction to use
    // If not specified, nothing will be ran
    // Points to 'data/examplemod/function/example/teardown.mcfunction'
    "teardown": "examplemod:example/teardown"
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_ENVIRONMENT, bootstrap -> {

            // Register the environment
            bootstrap.register(
                EXAMPLE_ENVIRONMENT,
                new TestEnvironmentDefinition.Functions(
                    // The setup mcfunction to use
                    // If not specified, nothing will be ran
                    // Points to 'data/examplemod/function/example/setup.mcfunction'
                    Optional.of(ResourceLocation.fromNamespaceAndPath("examplemod", "example/setup")),

                    // The teardown mcfunction to use
                    // If not specified, nothing will be ran
                    // Points to 'data/examplemod/function/example/teardown.mcfunction'
                    Optional.of(ResourceLocation.fromNamespaceAndPath("examplemod", "example/teardown"))
                )
            );
        })
    );
}
```

</TabItem>
</Tabs>

### Composites

Multiple environments can be merged using the composite environment type. The list of definitions can take in either a reference to an existing definiton, or an inlined definition.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// examplemod:example_environment
// In 'data/examplemod/test_environment/example_environment.json'
{
    "type": "minecraft:all_of",

    // A list of test environments to use
    // Can either specified the registry name or the environment itself
    "definitions": [
        // Points to 'data/minecraft/test_environment/default.json'
        "minecraft:default",
        {
            // A raw environment definition
            "type": "..."
        }
        // ...
    ]
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_ENVIRONMENT, bootstrap -> {
            // Getting existing environments
            HolderGetter<TestEnvironmentDefinition> environments = bootstrap.lookup(Registries.TEST_ENVIRONMENT);

            // Register the environment
            bootstrap.register(
                EXAMPLE_ENVIRONMENT,
                new TestEnvironmentDefinition.AllOf(
                    List.of(
                        // Points to 'data/minecraft/test_environment/default.json'
                        environments.getOrThrow(GameTestEnvironments.DEFAULT_KEY),
                        Holder.direct(
                            // Create a new TestEnvironmentDefinition here
                            ...
                        )
                        // ...
                    )
                )
            );
        })
    );
}
```

</TabItem>
</Tabs>

### Custom Definition Types

A custom `TestEnvironmentDefinition` type provides three methods: `setup` to modify the `ServerLevel`, `teardown` to reset what was modified, and `codec` to provide the `MapCodec` to encode and decode the type:

```java
public record ExampleEnvironmentType(int value1, boolean value2) implements TestEnvironmentDefinition {

    // Construct the map codec to register
    public static final MapCodec<ExampleEnvironmentType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("value1").forGetter(ExampleEnvironmentType::value1),
            Codec.BOOL.fieldOf("value2").forGetter(ExampleEnvironmentType::value2)
        ).apply(instance, ExampleEnvironmentType::new)
    );

    @Override
    public void setup(ServerLevel level) {
        // Setup whatever is necessary here
    }

    @Override
    public void teardown(ServerLevel level) {
        // Undo whatever was changed within the setup method
        // This should either return to default or the previous value
    }

    @Override
    public MapCodec<ExampleEnvironmentType> codec() {
        return EXAMPLE_ENVIRONMENT_CODEC.get();
    }
}
```

Then, the `MapCodec` can be [registered]:


```java
public static final DeferredRegister<MapCodec<? extends TestEnvironmentDefinition>> TEST_ENVIRONMENT_DEFINITION_TYPES = DeferredRegister.create(
        BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE,
        "examplemod"
);

public static final Supplier<MapCodec<ExampleEnvironmentType>> EXAMPLE_ENVIRONMENT_CODEC = TEST_ENVIRONMENT_DEFINITION_TYPES.register(
    "example_environment_type",
    () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("value1").forGetter(ExampleEnvironmentType::value1),
            Codec.BOOL.fieldOf("value2").forGetter(ExampleEnvironmentType::value2)
        ).apply(instance, ExampleEnvironmentType::new)
    )
);
```

Finally, the type can then be used in your environment definition:

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// examplemod:example_environment
// In 'data/examplemod/test_environment/example_environment.json'
{
    "type": "examplemod:example_environment_type",

    "value1": 0,
    "value2": true
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_ENVIRONMENT, bootstrap -> {

            // Register the environment
            bootstrap.register(
                EXAMPLE_ENVIRONMENT,
                new ExampleEnvironmentType(
                    0, true
                )
            );
        })
    );
}
```

</TabItem>
</Tabs>

## The Test Function

The basic concept of game tests are structured around running some method that takes in a `GameTestHelper` and returning nothing. Calling the methods within the `GameTestHelper` determines whether the test suceeds or fails. Each test function is [registered], allowing it to be referenced in a test instance:

```java
public class ExampleFunctions {

    // Here is our example function
    public static void exampleTest(GameTestHelper helper) {
        // Do Stuff
    }
}

// Register our function for use
public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTION = DeferredRegister.create(
        BuiltInRegistries.TEST_FUNCTION,
        "examplemod"
);

public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXAMPLE_FUNCTION = TEST_FUNCTION.register(
    "example_function",
    () -> ExampleFunctions::exampleTest
);
```

### Relative Positioning

All test functions translate relative coordinates within the structure template scene to its absolute coordinates using the structure block's current location. To allow for easy conversion between relative and absolute positioning, `GameTestHelper#absolutePos` and `GameTestHelper#relativePos` can be used respectively.

The relative position of a structure template can be obtained in-game by loading the structure via the [test command][test], placing the player at the wanted location, and finally running the `/test pos` command. This will grab the coordinates of the player relative to the closest structure within 200 blocks of the player. The command will export the relative position as a copyable text component in the chat to be used as a final local variable.

:::tip
The local variable generated by `/test pos` can specify its reference name by appending it to the end of the command:

```bash
/test pos <var> # Exports 'final BlockPos <var> = new BlockPos(...);'
```
:::

### Successful Completion

A test function is responsible for one thing: marking the test was successful on a valid completion. If no success state was achieved before the timeout is reached (as defined by `TestData#maxTicks`), then the test automatically fails.

There are many abstracted methods within `GameTestHelper` which can be used to define a successful state; however, four are extremely important to be aware of.

Method               | Description
:---:                | :---
`#succeed`           | The test is marked as successful.
`#succeedIf`         | The supplied `Runnable` is tested immediately and succeeds if no `GameTestAssertException` is thrown. If the test does not succeed on the immediate tick, then it is marked as a failure.
`#succeedWhen`       | The supplied `Runnable` is tested every tick until timeout and succeeds if the check on one of the ticks does not throw a `GameTestAssertException`.
`#succeedOnTickWhen` | The supplied `Runnable` is tested on the specified tick and will succeed if no `GameTestAssertException` is thrown. If the `Runnable` succeeds on any other tick, then it is marked as a failure.

:::caution
Game Tests are executed every tick until the test is marked as a success. As such, methods which schedule success on a given tick must be careful to always fail on any previous tick.
:::

### Scheduling Actions

Not all actions will occur when a test begins. Actions can be scheduled to occur at specific times or intervals:

Method           | Description
:---:            | :---
`#runAtTickTime` | The action is ran on the specified tick.
`#runAfterDelay` | The action is ran `x` ticks after the current tick.
`#onEachTick`    | The action is ran every tick.

### Assertions

At any time during a Game Test, an assertion can be made to check if a given condition is true. There are numerous assertion methods within `GameTestHelper`; however, it simplifies to throwing a `GameTestAssertException` whenever the appropriate state is not met.

## Registering The Test Instance

With the `TestData`, `TestEnvironmentDefinition`, and test function in hand, we can now link everything together through a `GameTestInstance`. Each test instance is what represents a single game test to run. All test instances are located within `data/<namespace>/test_instance/<path>.json`.

### Function-Based Tests

`FunctionGameTestInstance` links a `TestData` to some registered test function. The test instance will run the test function when called.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some game test examplemod:example_test
// In 'data/examplemod/test_instance/example_test.json'
{
    // `TestData`

    "environment": "examplemod:example_environment",
    "structure": "examplemod:example_structure",
    "max_ticks": 400,
    "setup_ticks": 50,
    "required": true,
    "rotation": "clockwise_90",
    "manual_only": true,
    "max_attempts": 3,
    "required_successes": 1,
    "sky_access": false,

    // `FunctionGameTestInstance`
    "type": "minecraft:function",

    // Points to a 'Consumer<GameTestHelper>' in the test function registry
    "function": "examplemod:example_function"
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// The test instance key
public static final ResourceKey<GameTestInstance> EXAMPLE_TEST_INSTANCE = ResourceKey.create(
    Registries.TEST_INSTANCE,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_test")
);

// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_INSTANCE, bootstrap -> {
            // Use this to get the test environments
            HolderGetter<TestEnvironmentDefinition> environments = bootstrap.lookup(Registries.TEST_ENVIRONMENT);

            // Register a game test
            // Any fields not relevant to the test data are hidden
            bootstrap.register(EXAMPLE_TEST_INSTANCE,
                new FunctionGameTestInstance(
                    // Points to a 'Consumer<GameTestHelper>' in the test function registry
                    EXAMPLE_FUNCTION.getKey()
                    new TestData<>(
                        environments.getOrThrow(EXAMPLE_ENVIRONMENT),
                        ResourceLocation.fromNamespaceAndPath("examplemod", "example_structure"),
                        400,
                        50,
                        true,
                        Rotation.CLOCKWISE_90,
                        true,
                        3,
                        1,
                        false
                    )
            ));
        })
    );
}
```

</TabItem>
</Tabs>

### Block-Based Tests

`BlockBasedTestInstance` is a special kind of test instance that relies on redstone signals sent and received by `Blocks#TEST_BLOCK`s. For this test to work, the structure template must contain at least two test blocks: one and only one set to `TestBlockMode#START` and one set to `TestBlockMode#ACCEPT`. When the test starts, the starting test block is triggered, sending a fifteen signal pulse for one tick. It is expected that this signal eventually triggers other test blocks in either `LOG`, `FAIL`, or `ACCEPT` states. `LOG` test blocks also send a fifteen signal pulse when activated. `ACCEPT` and `FAIL` test blocks either cause the test instance to succeed or fail, respectively. `ACCEPT` always takes precedence over `FAIL` on a given tick.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some game test examplemod:example_test
// In 'data/examplemod/test_instance/example_test.json'
{
    // `TestData`

    "environment": "examplemod:example_environment",
    "structure": "examplemod:example_structure",
    "max_ticks": 400,
    "setup_ticks": 50,
    "required": true,
    "rotation": "clockwise_90",
    "manual_only": true,
    "max_attempts": 3,
    "required_successes": 1,
    "sky_access": false,

    // `BlockBasedTestInstance`
    "type": "minecraft:block_based"
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// The test instance key
public static final ResourceKey<GameTestInstance> EXAMPLE_TEST_INSTANCE = ResourceKey.create(
    Registries.TEST_INSTANCE,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_test")
);

// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_INSTANCE, bootstrap -> {
            // Use this to get the test environments
            HolderGetter<TestEnvironmentDefinition> environments = bootstrap.lookup(Registries.TEST_ENVIRONMENT);

            // Register a game test
            // Any fields not relevant to the test data are hidden
            bootstrap.register(EXAMPLE_TEST_INSTANCE,
                new BlockBasedTestInstance(
                    new TestData<>(
                        environments.getOrThrow(EXAMPLE_ENVIRONMENT),
                        ResourceLocation.fromNamespaceAndPath("examplemod", "example_structure"),
                        400,
                        50,
                        true,
                        Rotation.CLOCKWISE_90,
                        true,
                        3,
                        1,
                        false
                    )
            ));
        })
    );
}
```

</TabItem>
</Tabs>

### Custom Test Instances

If you need to implement your own test-based logic for whatever reason, `GameTestInstance` can be extended. Two methods must be implemented: `run`, which represents the test function; and `typeDescription`, which provides a description of the test instance. If the test instance should be used in datagen, it must have a `MapCodec` to be [registered].

```java
public class ExampleTestInstance extends GameTestInstance {

    public ExampleTestInstance(int value1, boolean value2, TestData<Holder<TestEnvironmentDefinition>> info) {
        super(info);
    }

    @Override
    public void run(GameTestHelper helper) {
        // Run whatever game test commands you want
        helper.assertBlockPresent(...);

        // Make sure you have some way to succeed
        helper.succeedIf(() -> ...);
    }

    @Override
    public MapCodec<ExampleTestInstance> codec() {
        return EXAMPLE_INSTANCE_CODEC.get();
    }

    @Override
    protected MutableComponent typeDescription() {
        // Provides a description about what this test is supposed to be
        // Should use a translatable component
        return Component.literal("Example Test Instance");
    }
}

// Register our test instance for use
public static final DeferredRegister<MapCodec<? extends GameTestInstance>> TEST_INSTANCE = DeferredRegister.create(
        BuiltInRegistries.TEST_INSTANCE_TYPE,
        "examplemod"
);

public static final Supplier<MapCodec<? extends GameTestInstance>> EXAMPLE_INSTANCE_CODEC = TEST_INSTANCE.register(
    "example_test_instance",
    () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("value1").forGetter(test -> test.value1),
            Codec.BOOL.fieldOf("value2").forGetter(test -> test.value2),
            TestData.CODEC.forGetter(ExampleTestInstance::info)
        ).apply(instance, ExampleTestInstance::new)
    )
);
```

Then, the test instance can be used in a datapack:

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some game test examplemod:example_test
// In 'data/examplemod/test_instance/example_test.json'
{
    // `TestData`

    "environment": "examplemod:example_environment",
    "structure": "examplemod:example_structure",
    "max_ticks": 400,
    "setup_ticks": 50,
    "required": true,
    "rotation": "clockwise_90",
    "manual_only": true,
    "max_attempts": 3,
    "required_successes": 1,
    "sky_access": false,

    // `ExampleTestInstance`
    "type": "examplemod:example_test_instance",

    "value1": 0,
    "value2": true
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// The test instance key
public static final ResourceKey<GameTestInstance> EXAMPLE_TEST_INSTANCE = ResourceKey.create(
    Registries.TEST_INSTANCE,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_test")
);

// Let's assume we have some test environment
public static final ResourceKey<TestEnvironmentDefinition> EXAMPLE_ENVIRONMENT = ResourceKey.create(
    Registries.TEST_ENVIRONMENT,
    ResourceLocation.fromNamespaceAndPath("examplemod", "example_environment")
);

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createDatapackRegistryObjects(
        new RegistrySetBuilder().add(Registries.TEST_INSTANCE, bootstrap -> {
            // Use this to get the test environments
            HolderGetter<TestEnvironmentDefinition> environments = bootstrap.lookup(Registries.TEST_ENVIRONMENT);

            // Register a game test
            // Any fields not relevant to the test data are hidden
            bootstrap.register(EXAMPLE_TEST_INSTANCE,
                new ExampleTestInstance(
                    0,
                    true,
                    new TestData<>(
                        environments.getOrThrow(EXAMPLE_ENVIRONMENT),
                        ResourceLocation.fromNamespaceAndPath("examplemod", "example_structure"),
                        400,
                        50,
                        true,
                        Rotation.CLOCKWISE_90,
                        true,
                        3,
                        1,
                        false
                    )
            ));
        })
    );
}
```

</TabItem>
</Tabs>

### Skipping the Datapack

If you don't want to use a datapack to construct your game tests, you can instead listen to the `RegisterGameTestsEvent` on the [mod event bus][event] and register your environments and test instances via `registerEnvironment` and `registerTest`, respectively.

```java
@SubscribeEvent // on the mod event bus
public static void registerTests(RegisterGameTestsEvent event) {
    Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
        // The name of the test environment
        EXAMPLE_ENVIRONMENT.location(),
        // A varargs of test environment definitions
        new ExampleEnvironmentType(
            0, true
        )
    );

    event.registerTest(
        // The name of the test instance
        EXAMPLE_TEST_INSTANCE.location(),
        new ExampleTestInstance(
            0,
            true,
            new TestData<>(
                environments.getOrThrow(EXAMPLE_ENVIRONMENT),
                ResourceLocation.fromNamespaceAndPath("examplemod", "example_structure"),
                400,
                50,
                true,
                Rotation.CLOCKWISE_90,
                true,
                3,
                1,
                false
            )
        )
    );
}
```

## Running Game Tests

Game Tests can be run using the `/test` command. The `test` command is highly configurable; however, only a few are of importance to running tests:

| Subcommand  | Description                                           |
|:-----------:|:------------------------------------------------------|
| `run`       | Runs the specified test: `run <test_name>`.           |
| `runall`    | Runs all available tests.                             |
| `runclosest`| Runs the nearest test to the player within 15 blocks. |
| `runthese`  | Runs tests within 200 blocks of the player.           |
| `runfailed` | Runs all tests that failed in the previous run.       |

:::note
Subcommands follow the test command: `/test <subcommand>`.
:::

## Buildscript Configurations

Game Tests provide additional configuration settings within a buildscript (the `build.gradle` file) to run and integrate into different settings.

### Game Test Server Run Configuration

The Game Test Server is a special configuration which runs a build server. The build server returns an exit code of the number of required, failed Game Tests. All failed tests, whether required or optional, are logged. This server can be run using `gradlew runGameTestServer`.

### Enabling Game Tests in Other Run Configurations

By default, only the `client` and `gameTestServer` run configurations have Game Tests enabled. If another run configuration should run Game Tests, then the `neoforge.enableGameTest` property must be set to `true`.

```gradle
// Inside a run configuration
property 'neoforge.enableGameTest', 'true'
```

[datapacks]: ../resources/index.md#data
[registered]: ../concepts/registries.md#methods-for-registering
[test]: #running-game-tests
[event]: ../concepts/events.md#registering-an-event-handler
