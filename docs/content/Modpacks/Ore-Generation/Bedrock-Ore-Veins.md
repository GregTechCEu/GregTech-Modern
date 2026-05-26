---
title: Bedrock Ore Veins
---


# Bedrock Ore Veins

While not enabled by default, GTCEu Modern contains bedrock ore veins and bedrock ore miners.

To enable this feature, you need to enable the config option **Machines -> doBedrockOres** and restart your game.

!!! warning "No recipes by default"
    The various tiers of bedrock ore miners don't have any recipes by default. It is up to modpack developers to create
    crafting recipes for these machines.


## Adding Bedrock Veins

By default, the mod doesn't include any bedrock ore veins. You can add them in either KubeJS or Java using one of the following:

=== "JavaScript"
    Using the `bedrockOreVeins` server event:
    ```js
    GTCEuServerEvents.bedrockOreVeins(event => {
        event.add('kubejs:overworld_bedrock_ore_vein_iron', vein => {
            vein.weight(100)
                .size(3) // (1)
                .yield(10, 20)
                .material(GTMaterials.Goethite, 5) // (2)
                .material(GTMaterials.Limonite, 2)
                .material(GTMaterials.Hematite, 2)
                .material(GTMaterials.Malachite, 1)
                .dimensions('minecraft:overworld')
        })
    })
    ```

    1. The diameter of the bedrock vein in chunks
    2. The second parameter defines the chance of each material being mined on each cycle
=== "Java"
    In Java, `.dimensions()` only accepts a `Set<ResourceKey<Level>>`. For example, this is how you would assign the overworld a variable:
    ```java
    public static final Set<ResourceKey<Level>> DIM_OVERWORLD = Set.of(Level.OVERWORLD);
    ```
    To have multiple dimensions in the same set, simply add more `ResourceKey<Level>` instances into the set itself. Here's an example for all vanilla dimensions:
    ```java
    public static final Set<ResourceKey<Level>> DIM_ALL = Set.of(Level.OVERWORLD, Level.NETHER, Level.END);
    ```
    You can define this in any class you want (including the one with the bedrock ore vein definitions themselves), as long as you're able to reference it in the class where you're defining the bedrock ore veins.  
    With this done, you can reference said variables in the `BedrockOreDefinition` builder. Let's use `DIM_OVERWORLD` in this example.  
    
    ```java title="ExampleBedrockOreVeins.java"
    pubic class ExampleBedrockOreVeins {
        public static void init() {}
        
        public static final Set<ResourceKey<Level>> DIM_OVERWORLD = Set.of(Level.OVERWORLD);
        
        public static final BedrockOreDefinition OVERWORLD_BEDROCK_ORE_VEIN_IRON = BedrockOreDefinition.builder(ExampleMod.id("overworld_bedrock_ore_vein_iron"))
                .weight(100)
                .size(3) // (1)
                .yield(10, 20)
                .materials(List.of(
                    new WeightedMaterial(Goethite, 5), // (2)
                    new WeightedMaterial(Limonite, 2),
                    new WeightedMaterial(Hematite, 2),
                    new WeightedMaterial(Gold, 1)
                ))
                .dimensions(DIM_OVERWORLD)
                .register();
    }
    ```
    
    1. The diameter of the bedrock vein in chunks
    2. The second parameter defines the chance of each material being mined on each cycle

    As the last step, you need to actually enable the registration of the veins in your class implementing IGTAddon.  
    
    ```java title="ExampleModGTAddon.java"
    @GTAddon
    public class ExampleModGTAddon implements IGTAddon {
        // ...
        @Override
        public void registerBedrockOreVeins() {
            ExampleBedrockOreVeins.init();
        }
        // ...
    }
    ```
