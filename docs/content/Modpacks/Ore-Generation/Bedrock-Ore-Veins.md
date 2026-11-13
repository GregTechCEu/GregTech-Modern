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
                .addSpawnDimension('minecraft:overworld')
        })
    })
    ```

    1. The diameter of the bedrock vein in chunks
    2. The second parameter defines the chance of each material being mined on each cycle
=== "Java"
    Bedrock ore veins are stored in a datapack registry, so in Java they are created through datagen.  
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
    public class ExampleBedrockOreVeins {
        
        public static final Set<ResourceKey<Level>> DIM_OVERWORLD = Set.of(Level.OVERWORLD);

        public static final ResourceKey<BedrockOreDefinition> OVERWORLD_BEDROCK_ORE_VEIN_IRON = ResourceKey.create(
                GTRegistries.Keys.BEDROCK_ORE, ExampleMod.id("overworld_bedrock_ore_vein_iron"));
        
        public static void bootstrap(BootstrapContext<BedrockOreDefinition> context) {
            context.register(OVERWORLD_BEDROCK_ORE_VEIN_IRON, BedrockOreDefinition.builder(context.lookup(Registries.BIOME))
                    .weight(100)
                    .size(3) // (1)
                    .yield(10, 20)
                    .material(Goethite.get(), 5) // (2)
                    .material(Limonite.get(), 2)
                    .material(Hematite.get(), 2)
                    .material(Gold.get(), 1)
                    .dimensions(DIM_OVERWORLD)
                    .build());
        }
    }
    ```
    
    1. The diameter of the bedrock vein in chunks
    2. The second parameter defines the chance of each material being mined on each cycle

    As the last step, you need to add the bootstrap method to your datagen, and run datagen.  
    
    ```java title="ExampleModDatagen.java"
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // ...
        event.getGenerator().addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(), event.getLookupProvider(),
                new RegistrySetBuilder().add(GTRegistries.Keys.BEDROCK_ORE, ExampleBedrockOreVeins::bootstrap),
                Set.of(ExampleMod.MOD_ID)));
    }
    ```

## Modifying a Bedrock Ore Vein

=== "JavaScript"
    ```js title="ore_veins.js"
    // In server events
    GTCEuServerEvents.bedrockOreVeins(event => {
        event.modify('gtceu:example_ore', vein => {
            vein.weight(9999)
        });
    });
    ```

=== "JSON"
    Bedrock ore veins are loaded from datapacks, so you can modify a vein by overriding its file (with the same ID) in your datapack, at `data/<namespace>/gtceu/bedrock_ore/<vein id>.json`.


## Removing a Bedrock Ore Vein

=== "JavaScript"
    ```js title="ore_veins.js"
    // In server events
    GTCEuServerEvents.bedrockOreVeins(event => {
        event.remove('gtceu:example_ore');
    });
    ```

=== "JSON"
    ```json title="resources/data/gtceu/gtceu/bedrock_ore/example_ore.json"
        {"neoforge:conditions":[{"type":"neoforge:false"}]}
    ```
