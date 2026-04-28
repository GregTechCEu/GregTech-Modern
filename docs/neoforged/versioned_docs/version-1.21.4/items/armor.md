---
sidebar_position: 5
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Armor

Armors are [items][item] whose primary use is to protect a [`LivingEntity`][livingentity] from damage using a variety of resistances and effects. Many mods add new armor sets (for example copper armor).

## Custom Armor Sets

An armor set for a humanoid entity typically consists of four items: a helmet for the head, a chestplate for the chest, leggings for the legs, and boots for the feet. There is also armor for wolves, horses, and llamas that are applied to a 'body' armor slot specifically for animals. All of these items are generally implemented through `ArmorItem` and `AnimalArmorItem`, respectively.

Armors are almost completely implemented through seven [data components][datacomponents]: 

- `DataComponents#MAX_DAMAGE` and `#DAMAGE` for durability
- `#MAX_STACK_SIZE` to set the stack size to `1`
- `#REPAIRABLE` for repairing an armor piece in an anvil
- `#ENCHANTABLE` for the maximum [enchanting][enchantment] value
- `#ATTRIBUTE_MODIFIERS` for armor, armor toughness, and knockback resistance
- `#EQUIPPABLE` for how the entity can equip the item.

`ArmorItem` and `AnimalArmorItem` use `ArmorMaterial` combined with `ArmorType` or `AnimalArmorItem.BodyType` respectively to set up the components. Reference values can be found within `ArmorMaterials`. This example uses a copper armor material, which you can adjust the values as needed.

```java
public static final ArmorMaterial COPPER_ARMOR_MATERIAL = new ArmorMaterial(
    // The durability multiplier of the armor material.
    // ArmorType have different unit durabilities that the multiplier is applied to:
    // - HELMET: 11
    // - CHESTPLATE: 16
    // - LEGGINGS: 15
    // - BOOTS: 13
    // - BODY: 16
    15,
    // Determines the defense value (or the number of half-armors on the bar).
    // Based on ArmorType.
    Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 2);
        map.put(ArmorItem.Type.LEGGINGS, 4);
        map.put(ArmorItem.Type.CHESTPLATE, 6);
        map.put(ArmorItem.Type.HELMET, 2);
        map.put(ArmorItem.Type.BODY, 4);
    }),
    // Determines the enchantability of the armor. This represents how good the enchantments on this armor will be.
    // Gold uses 25; we put copper slightly below that.
    20,
    // Determines the sound played when equipping this armor.
    // This is wrapped with a Holder.
    SoundEvents.ARMOR_EQUIP_GENERIC,
     // Returns the toughness value of the armor. The toughness value is an additional value included in
    // damage calculation, for more information, refer to the Minecraft Wiki's article on armor mechanics:
    // https://minecraft.wiki/w/Armor#Armor_toughness
    // Only diamond and netherite have values greater than 0 here, so we just return 0.
    0,
    // Returns the knockback resistance value of the armor. While wearing this armor, the player is
    // immune to knockback to some degree. If the player has a total knockback resistance value of 1 or greater
    // from all armor pieces combined, they will not take any knockback at all.
    // Only netherite has values greater than 0 here, so we just return 0.
    0,
    // The tag that determines what items can repair this armor.
    Tags.Items.INGOTS_COPPER,
    // The resource key of the EquipmentClientInfo JSON discussed below
    // Points to assets/examplemod/equipment/copper.json
    ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("examplemod", "copper"))
);
```

Now that we have our `ArmorMaterial`, we can use it for [registering] armor. `ArmorItem` takes in the `ArmorMaterial` and the `ArmorType` representing where the item can be equipped. `AnimalArmorItem`, on the other hand takes in the `ArmorMaterial` and the `AnimalArmorItem.BodyType`. It also can optionally take in the an equip sound and whether to apply the durability and repairable components.

```java
// ITEMS is a DeferredRegister.Items
public static final DeferredItem<ArmorItem> COPPER_HELMET = ITEMS.registerItem(
    "copper_helmet",
    props -> new ArmorItem(
        // The material to use.
        COPPER_ARMOR_MATERIAL,
        // The type of armor to create.
        ArmorType.HELMET,
        // The item properties.
        props
    )
);

public static final DeferredItem<ArmorItem> COPPER_CHESTPLATE =
    ITEMS.registerItem("copper_chestplate", props -> new ArmorItem(...));
public static final DeferredItem<ArmorItem> COPPER_LEGGINGS =
    ITEMS.registerItem("copper_chestplate", props -> new ArmorItem(...));
public static final DeferredItem<ArmorItem> COPPER_BOOTS =
    ITEMS.registerItem("copper_chestplate", props -> new ArmorItem(...));

public static final DeferredItem<AnimalArmorItem> COPPER_WOLF_ARMOR = ITEMS.registerItem(
    "copper_wolf_armor",
    props -> new AnimalArmorItem(
        // The material to use.
        COPPER_ARMOR_MATERIAL,
        // The body type the armor can be worn by.
        AnimalArmorItem.BodyType.CANINE,
        // The item properties.
        props
    )
);

public static final DeferredItem<AnimalArmorItem> COPPER_HORSE_ARMOR =
    ITEMS.registerItem("copper_horse_armor", props -> new AnimalArmorItem(...));
```

Now, creating armor or an armor-like item does not need to extend `ArmorItem` or `AnimalArmorItem`. It simply can be implemented using a combination of the following parts:

- Adding a `Equippable` with your own requirements by setting `DataComponents#EQUIPPABLE` via `Item.Properties#component`.
- Adding attributes to the item (e.g. armor, toughness, knockback) via `Item.Properties#attributes`.
- Adding item durability via `Item.Properties#durability`.
- Allowing the item to be repaired via `Item.Properties#repariable`.
- Allowing the item to be enchanted via `Item.Properties#enchantable`.
- Adding your armor to some of the `minecraft:enchantable/*` `ItemTags` so that your item can have certain enchantments applied to it.

### `Equippable`

`Equippable` is a data component that contains how an entity can equip this item and what handles the rendering in game. This allows any item, regardless of whether it is considered 'armor', to be equipped if this component is available (for example carpets on llamas). Each item with this component can only be equipped to a single `EquipmentSlot`.

An `Equippable` can be created either by directly calling the record constructor or via `Equippable#builder`, which sets the defaults for each field, folowed by `build` once finished:

```java
// Assume there is some DeferredRegister.Items ITEMS
public static final DeferredItem<Item> EQUIPPABLE = ITEMS.registerSimpleItem(
    "equippable",
    new Item.Properties().copmonent(
        DataComponents.EQUIPPABLE,
        // Sets the slot that this item can be equipped to.
        Equippable.builder(EquipmentSlot.HELMET)
            // Determines the sound played when equipping this armor.
            // This is wrapped with a Holder.
            // Defaults to SoundEvents#ARMOR_EQUIP_GENERIC.
            .setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC)
            // The resource key of the EquipmentClientInfo JSON discussed below.
            // Points to assets/examplemod/equipment/equippable.json
            // When not set, does not render the equipment.
            .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("examplemod", "equippable")))
            // The relative location of the texture to overlay on the player screen when wearing (e.g., pumpkin blur).
            // Points to assets/examplemod/textures/equippable.png
            // When not set, does not render an overlay.
            .setCameraOverlay(ResourceLocation.withDefaultNamespace("examplemod", "equippable"))
            // A HolderSet of entity types (direct or tag) that can equip this item.
            // When not set, any entity can equip this item.
            .setAllowedEntities(EntityType.ZOMBIE)
            // Whether the item can be equipped when dispensed from a dispenser.
            // Defaults to true.
            .setDispensable(true),
            // Whether the item can be swapped off the player during a quick equip.
            // Defaults to true.
            .setSwappable(false),
            // Whether the item should be damaged when attacked (for equipment typically).
            // Must also be a damageable item.
            // Defaults to true.
            .setDamageOnHurt(false)
            .build()
    )
);
```

## Equipment Assets

Now we have some armor in game, but if we try to wear it, nothing will render since we never specified how to render the equipment. To do so, we need to create an `EquipmentClientInfo` JSON at the location specified by `Equippable#assetId`, relative to the `equipment` folder of the [resource pack][respack] (`assets` folder). The `EquipmentClientInfo` specifies the associated textures to use for each layer to render.

An `EquipmentClientInfo` is functionally a map of `EquipmentClientInfo.LayerType`s to a list of `EquipmentClientInfo.Layer`s to apply.

The `LayerType` can be thought of as a group of textures to render for some instance. For example, `LayerType#HUMANOID` is used by the `HumanoidArmorLayer` to render the head, chest, and feet on humanoid entities; `LayerType#WOLF_BODY` is used by `WolfArmorLayer` to render the body armor. These can be combined into one equipment info JSON if they are for the same type of equippable, like copper armor.

The `LayerType` maps to some list of `Layer`s to apply and render the textures in the order provided. A `Layer` effectively represents a single texture to render. The first parameter represents the location of the texture, relative to `textures/entity/equipment`.

The second parameter is an optional that indicates whether the [texture can be tinted][tinting] as an `EquipmentClientInfo.Dyeable`. The `Dyeable` object holds an integer that, when present, indicates the default RGB color to tint the texture with. If this optional is not present, then pure white is used.

:::warning
For a tint other than the undyed color to be applied to the item, the item must be in the [`ItemTags#DYEABLE`][tag] and have the `DataComponents#DYED_COLOR` component set to some RGB value.
:::

The third parameter is a boolean that indicates whether the texture provided during rendering should be used instead of the one defined within the `Layer`. An example of this is a custom cape or custom elytra texture for the player.

Let's create an equipment info for the copper armor material. We'll also assume that for each layer there are two textures: one for the actual armor and one that is overlayed and tinted. For the animal armor, we'll say that there is some dynamic texture to be used that can be passed in.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// In assets/examplemod/equipment/copper.json
{
    // The layer map
    "layers": {
        // The serialized name of the EquipmentClientInfo.LayerType to apply.
        // For humanoid head, chest, and feet
        "humanoid": [
            // A list of layers to render in the order provided
            {
                // The relative texture of the armor
                // Points to assets/examplemod/textures/entity/equipment/copper/outer.png
                "texture": "examplemod:copper/outer"
            },
            {
                // The overlay texture
                // Points to assets/examplemod/textures/entity/equipment/copper/outer_overlay.png
                "texture": "examplemod:copper/outer_overlay",
                // When specified, allows the texture to be tinted the color in DataComponents#DYED_COLOR
                // Otherwise, cannot be tinted
                "dyeable": {
                    // An RGB value (always opaque color)
                    // 0x7683DE as decimal
                    // When not specified, set to 0 (meaning transparent or invisible)
                    "color_when_undyed": 7767006
                }
            }
        ],
        // For humanoid legs
        "humanoid_leggings": [
            {
                // Points to assets/examplemod/textures/entity/equipment/copper/inner.png
                "texture": "examplemod:copper/inner"
            },
            {
                // Points to assets/examplemod/textures/entity/equipment/copper/inner_overlay.png
                "texture": "examplemod:copper/inner_overlay",
                "dyeable": {
                    "color_when_undyed": 7767006
                }
            }
        ],
        // For wolf armor
        "wolf_body": [
            {
                // Points to assets/examplemod/textures/entity/equipment/copper/wolf.png
                "texture": "examplemod:copper/wolf",
                // When true, uses the texture passed into the layer renderer instead
                "use_player_texture": true
            }
        ],
        // For horse armor
        "horse_body": [
            {
                // Points to assets/examplemod/textures/entity/equipment/copper/horse.png
                "texture": "examplemod:copper/horse",
                "use_player_texture": true
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
public class MyEquipmentInfoProvider implements DataProvider {

    private final PackOutput.PathProvider path;

    public MyEquipmentInfoProvider(PackOutput output) {
        this.path = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "equipment");
    }

    private void add(BiConsumer<ResourceLocation, EquipmentClientInfo> registrar) {
        registrar.accept(
            // Must match Equippable#assetId
            ResourceLocation.fromNamespaceAndPath("examplemod", "copper"),
            EquipmentClientInfo.builder()
                // For humanoid head, chest, and feet
                .addLayers(
                    EquipmentClientInfo.LayerType.HUMANOID,
                    // Base texture
                    new EquipmentClientInfo.Layer(
                        // The relative texture of the armor
                        // Points to assets/examplemod/textures/entity/equipment/copper/outer.png
                        ResourceLocation.fromNamespaceAndPath("examplemod", "copper/outer"),
                        Optional.empty(),
                        false
                    ),
                    // Overlay texture
                    new EquipmentClientInfo.Layer(
                        // The overlay texture
                        // Points to assets/examplemod/textures/entity/equipment/copper/outer_overlay.png
                        ResourceLocation.fromNamespaceAndPath("examplemod", "copper/outer_overlay"),
                        // An RGB value (always opaque color)
                        // When not specified, set to 0 (meaning transparent or invisible)
                        Optional.of(new EquipmentClientInfo.Dyeable(Optional.of(0x7683DE))),
                        false
                    )
                )
                // For humanoid legs
                .addLayers(
                    EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS,
                    new EquipmentClientInfo.Layer(
                        // Points to assets/examplemod/textures/entity/equipment/copper/inner.png
                        ResourceLocation.fromNamespaceAndPath("examplemod", "copper/inner"),
                        Optional.empty(),
                        false
                    ),
                    new EquipmentClientInfo.Layer(
                        // Points to assets/examplemod/textures/entity/equipment/copper/inner_overlay.png
                        ResourceLocation.fromNamespaceAndPath("examplemod", "copper/inner_overlay"),
                        Optional.of(new EquipmentClientInfo.Dyeable(Optional.of(0x7683DE))),
                        false
                    )
                )
                // For wolf armor
                .addLayers(
                    EquipmentClientInfo.LayerType.WOLF_BODY,
                    // Base texture
                    new EquipmentClientInfo.Layer(
                        // Points to assets/examplemod/textures/entity/equipment/copper/wolf.png
                        ResourceLocation.fromNamespaceAndPath("examplemod", "copper/wolf"),
                        Optional.empty(),
                        // When true, uses the texture passed into the layer renderer instead
                        true
                    )
                )
                // For horse armor
                .addLayers(
                    EquipmentClientInfo.LayerType.HORSE_BODY,
                    // Base texture
                    new EquipmentClientInfo.Layer(
                        // Points to assets/examplemod/textures/entity/equipment/copper/horse.png
                        ResourceLocation.fromNamespaceAndPath("examplemod", "copper/horse"),
                        Optional.empty(),
                        true
                    )
                )
                .build()
        );
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<ResourceLocation, EquipmentClientInfo> map = new HashMap<>();
        this.add((name, info) -> {
            if (map.putIfAbsent(name, info) != null) {
                throw new IllegalStateException("Tried to register equipment client info twice for id: " + name);
            }
        });
        return DataProvider.saveAll(cache, EquipmentClientInfo.CODEC, this.pathProvider, map);
    }

    @Override
    public String getName() {
        return "Equipment Client Infos: " + MOD_ID;
    }
}

@SubscribeEvent // on the mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createProvider(MyEquipmentInfoProvider::new);
}
```

</TabItem>
</Tabs>

## Equipment Rendering

The equipment infos are rendered via the `EquipmentLayerRenderer` in the render function of an `EntityRenderer` or one of its `RenderLayer`s. `EquipmentLayerRenderer` is obtained as part of the render context via `EntityRendererProvider.Context#getEquipmentRenderer`. If the `EquipmentClientInfo`s are required, they are also available via `EntityRendererProvider.Context#getEquipmentAssets`.

By default, the following layers render the associated `EquipmentClientInfo.LayerType`:

| `LayerType`         | `RenderLayer`        | Used by                                                        |
|:-------------------:|:--------------------:|:---------------------------------------------------------------|
| `HUMANOID`          | `HumanoidArmorLayer` | Player, humanoid mobs (e.g., zombies, skeletons), armor stands |
| `HUMANOID_LEGGINGS` | `HumanoidArmorLayer` | Player, humanoid mobs (e.g., zombies, skeletons), armor stands |
| `WINGS`             | `WingsLayer`         | Player, humanoid mobs (e.g., zombies, skeletons), armor stands |
| `WOLF_BODY`         | `WolfArmorLayer`     | Wolf                                                           |
| `HORSE_BODY`        | `HorseArmorLayer`    | Horse                                                          |
| `LLAMA_BODY`        | `LlamaDecorLayer`    | Llama, trader llama                                            |

`EquipmentLayerRenderer` has only one method to render the equipment layers, aptly named `renderLayers`:

```java
// In some render method where EquipmentLayerRenderer equipmentLayerRenderer is a field
this.equipmentLayerRenderer.renderLayers(
    // The layer type to render
    EquipmentClientInfo.LayerType.HUMANOID,
    // The resource key representing the EquipmentClientInfo JSON
    // This would be set in the `EQUIPPABLE` data component via `assetId`
    stack.get(DataComponents.EQUIPPABLE).assetId().orElseThrow(),
    // The model to apply the equipment info to
    // These are usually separate models from the entity model
    // and are separate ModelLayers linking to a LayerDefinition
    model,
    // The item stack representing the item being rendered as a model
    // This is only used to get the dyeable, foil, and armor trim information
    stack,
    // The pose stack used to render the model in the correct location
    poseStack,
    // The source of the buffers to get the vertex consumer of the render type
    bufferSource,
    // The packed light texture
    lighting,
    // An absolute path of the texture to render when use_player_texture is true for one of the layer if not null
    // Represents an absolute location within the assets folder
    ResourceLocation.fromNamespaceAndPath("examplemod", "textures/other_texture.png")
);
```

[item]: index.md
[datacomponents]: datacomponents.md
[enchantment]: ../resources/server/enchantments/index.md#enchantment-costs-and-levels
[livingentity]: ../entities/livingentity.md
[registering]: ../concepts/registries.md#methods-for-registering
[rendering]: #equipment-rendering
[respack]: ../resources/index.md#assets
[tag]: ../resources/server/tags.md
[tinting]: ../resources/client/models/index.md#tinting
