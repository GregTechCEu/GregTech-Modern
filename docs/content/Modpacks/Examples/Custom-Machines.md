---
title: Custom Machines
---


# Custom Machines


## Creating Custom Steam Machine

=== "Java"
    ```java title="AddonMachines.java"
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_COMPRESSOR = GTMachineUtils.registerSimpleSteamMachines(
        ADDON_REGISTRATE, "test_simple_steam_machine", AddonRecipeTypes.TEST_RECIPE_TYPE);
    ```
=== "JavaScript"
    ```js title="test_steam_machine.js"
    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create('test_simple_steam_machine', 'steam', true) // (1)
    })
    ```

1. Machine ID, Machine Type, Has High Pressure Varient


## Creating Custom Electric Machine

=== "Java"
    ```java title="AddonMachines.java"
    public static final MachineDefinition[] TEST_ELECTRIC = new SimpleMachineBuilder(ADDON_REGISTRATE, "extractor",
            AddonRecipeTypes.TEST_RECIPE_TYPE)
            .tiers(GTValues.LV, GTValues.MV, GTValues.HV)
            .tankScalingFunction(tier -> tier * 3200)
            .register();
    ```
=== "JavaScript"
    ```js title="test_electric_machine.js"
    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create('test_electric', 'simple', 0, GTValues.LV, GTValues.MV, GTValues.HV) // (1)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType('test_recipe_type')
            .tankScalingFunction(tier => tier * 3200)
    })
    ```


1. Machine ID, Machine Type, Pollution Produced, Voltage Tiers

## Creating Custom Generator

=== "Java"
    ```java title="AddonMachines.java"
    public static final MachineDefinition[] TEST_GENERATOR = GTMachineUtils.registerSimpleGenerator(ADDON_REGISTRATE, "test_generator",
        AddonRecipeTypes.TEST_GENERATOR_RECIPE_TYPE, tier -> tier * 3200, 0.1f, GTValues.LV, GTValues.MV,
        GTValues.HV);
    ```
=== "JavaScript"
    ```js title="test_generator.js"
    GTCEuStartupEvents.registry('gtceu:machine', event => {
        event.create('test_generator', 'generator', GTValues.LV, GTValues.MV, GTValues.HV) // (1)
            .recipeType('test_generator_recipe_type')
            .tankScalingFunction(tier => tier * 3200)
    })
    ```
