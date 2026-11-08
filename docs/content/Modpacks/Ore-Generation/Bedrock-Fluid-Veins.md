---
title: Bedrock Fluid Veins
---


# Bedrock Fluid Veins

Bedrock Fluid Veins are invisible veins that exist under the bedrock, to find Fluid Veins you must have at least a HV tier Prospector. A Fluid Drilling Rig must be used to obtain the fluids out of the vein.

## Creating a Bedrock Fluid Vein

=== "JavaScript"
    ```js title="fluid_veins.js"
    // In server events
    GTCEuServerEvents.fluidVeins(event => {
        event.add('gtceu:custom_bedrock_fluid_vein', vein => {
            vein.addSpawnDimension('minecraft:overworld') // (1)
            vein.fluid('gtceu:custom_fluid') 
            vein.weight(600)
            vein.minimumYield(120)
            vein.maximumYield(720)
            vein.depletionAmount(2)
            vein.depletionChance(1)
            vein.depletedYield(50)
        });
    });
    ```
    
    1. Dimension where fluid vein will spawn.

=== "Java"
    Bedrock fluid veins are stored in a datapack registry, so in Java they are created through datagen.  
    In Java, `.dimensions()` only accepts a `Set<ResourceKey<Level>>`. For example, this is how you would assign the overworld a variable:
    ```java
    public static final Set<ResourceKey<Level>> DIM_OVERWORLD = Set.of(Level.OVERWORLD);
    ```
    To have multiple dimensions in the same set, simply add more `ResourceKey<Level>` instances into the set itself. Here's an example for all vanilla dimensions:
    ```java
    public static final Set<ResourceKey<Level>> DIM_ALL = Set.of(Level.OVERWORLD, Level.NETHER, Level.END);
    ```
    You can define this in any class you want (including the one with the bedrock fluid vein definitions themselves), as long as you're able to reference it in the class where you're defining the bedrock fluid veins.  
    With this done, you can reference said variables in the `BedrockFluidDefinition` builder. Let's use `DIM_OVERWORLD` in this example.  
    
    ```java title="ExampleBedrockFluidVeins.java"
    public class ExampleBedrockFluidVeins {
        
        public static final Set<ResourceKey<Level>> DIM_OVERWORLD = Set.of(Level.OVERWORLD);

        public static final ResourceKey<BedrockFluidDefinition> CUSTOM_BEDROCK_FLUID_VEIN = ResourceKey.create(
                GTRegistries.Keys.BEDROCK_FLUID, ExampleMod.id("custom_bedrock_fluid_vein"));
        
        public static void bootstrap(BootstrapContext<BedrockFluidDefinition> context) {
            context.register(CUSTOM_BEDROCK_FLUID_VEIN, BedrockFluidDefinition.builder(context.lookup(Registries.BIOME))
                    .dimensions(DIM_OVERWORLD) // (1)
                    .fluid(CustomFluid.getFluid()) // (2)
                    .weight(600)
                    .yield(120, 720)
                    .depletionAmount(2)
                    .depletionChance(1)
                    .depletedYield(50)
                    .build());
        }
    }
    ```

    1. Dimension where fluid vein will spawn.
    2. Replace `CustomFluid` with any GTMaterial you want to get the fluid from.

    As the last step, you need to add the bootstrap method to your datagen, and run datagen.  
    
    ```java title="ExampleModDatagen.java"
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // ...
        event.getGenerator().addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(), event.getLookupProvider(),
                new RegistrySetBuilder().add(GTRegistries.Keys.BEDROCK_FLUID, ExampleBedrockFluidVeins::bootstrap),
                Set.of(ExampleMod.MOD_ID)));
    }
    ```


## Modifying a Bedrock Fluid Vein

=== "JavaScript"
    ```js title="fluid_veins.js"
    // In server events
    GTCEuServerEvents.fluidVeins(event => {
        event.modify('gtceu:heavy_oil_deposit', vein => {
            vein.weight(9999);
        });
    });
    ```

=== "JSON"
    Bedrock fluid veins are loaded from datapacks, so you can override a vein by placing a file with the same ID in your datapack:
    ```json title="resources/data/gtceu/gtceu/bedrock_fluid/heavy_oil_deposit.json"
    {
      "depleted_yield": 20,
      "depletion_amount": 1,
      "depletion_chance": 100,
      "dimension_filter": [
        "minecraft:overworld"
      ],
      "fluid": "gtceu:heavy_oil",
      "weight": 9999,
      "yield": {
        "max": 200,
        "min": 100
      }
    }
    ```

## Removing a Bedrock Fluid Vein

=== "JavaScript"
    ```js title="fluid_veins.js"
    // In server events
    GTCEuServerEvents.fluidVeins(event => {
        event.remove('gtceu:heavy_oil_deposit');
    });
    ```

=== "JSON"
    ```json title="resources/data/gtceu/gtceu/bedrock_fluid/heavy_oil_deposit.json"
        {"neoforge:conditions":[{"type":"neoforge:false"}]}
    ```
