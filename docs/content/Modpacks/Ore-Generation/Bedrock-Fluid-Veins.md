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
            vein.dimensions('minecraft:overworld') // (1)
            vein.fluid(() => Fluid.of('gtceu:custom_fluid').fluid) 
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
    pubic class ExampleBedrockFluidVeins {
        public static void init() {}
        
        public static final Set<ResourceKey<Level>> DIM_OVERWORLD = Set.of(Level.OVERWORLD);
        
        public static final BedrockFluidDefinition CUSTOM_BEDROCK_FLUID_VEIN = BedrockFluidDefinition.builder(ExampleMod.id("custom_bedrock_fluid_vein"))
                .dimensions(DIM_OVERWORLD) // (1)
                .fluid(CustomFluid::getFluid) // (2)
                .weight(600)
                .yield(120, 720)
                .depletionAmount(2)
                .depletionChance(1)
                .depletedYield(50)
                .register();
    }
    ```

    1. Dimension where fluid vein will spawn.
    2. Replace `CustomFluid` with any GTMaterial you want to get the fluid from.

    As the last step, you need to actually enable the registration of the veins in your class implementing IGTAddon.  
    
    ```java title="ExampleModGTAddon.java"
    @GTAddon
    public class ExampleModGTAddon implements IGTAddon {
        // ...
        @Override
        public void registerFluidVeins() {
            ExampleBedrockFluidVeins.init();
        }
        // ...
    }
    ```
