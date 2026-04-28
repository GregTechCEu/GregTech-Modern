import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Client Items

Client Items are the in-code representation of how an `ItemStack` should be rendered within the game, specifying what models to use given what state. The client items are located within the `items` subdirectory within the [`assets` folder][assets], specified by the relative location within `DataComponents#ITEM_MODEL`. By default, this is the registry name of the object (e.g. `minecraft:apple` would be located at `assets/minecraft/items/apple.json` by default).

The client items are stored within the `ModelManager`, which can be accessed through `Minecraft.getInstance().modelManager`. Then, you can call `ModelManager#getItemModel` or `getItemProperties` to get the client item information by its [`ResourceLocation`][rl].

:::warning
These are not to be confused with [baked models][bakedmodels], which define the models, along with their quads, that are actually rendered in-game.
:::

## Overview

The JSON of a client item can be broken into two parts: the model, defined by `model`; and the properties, defined by `properties`. The `model` is responsible for defining what model JSONs to use when rendering the `ItemStack` in a given context. The `properties`, on the other hand, is responsible for settings used by the renderer.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    // Defines the model to render
    "model": {
        "type": "minecraft:model",
        // Points to a model JSON relative to the 'models' directory
        // Located at 'assets/examplemod/models/item/example_item.json'
        "model": "examplemod:item/example_item"
    },
    // Defines some settings to use during the rendering process
    "properties": {
        // When false, disables the animation where the item is raised
        // up towards its normal position on item swap
        "hand_animation_on_swap": false
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.register(
        EXAMPLE_ITEM.get(),
        new ClientItem(
            // Defines the model to render
            new BlockModelWrapper.Unbaked(
                // Points to a model JSON relative to the 'models' directory
                // Located at 'assets/examplemod/models/item/example_item.json'
                ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
                Collections.emptyList()
            ),
            // Defines some settings to use during the rendering process
            new ClientItem.Properties(
                // When false, disables the animation where the item is raised
                // up towards its normal position on item swap
                false
            )
        )
    );
}
```

</TabItem>
</Tabs>

More information about how item models are rendered can be found [below][itemmodel].

## A Basic Model

The `type` field within `model` determines how to choose the model to render for the item. The simplest type is handled by `minecraft:model` (or `BlockModelWrapper`), which functionally defines the model JSON to render, relative to the `models` directory (e.g. `assets/<namespace>/models/<path>.json`).

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:model",
        // Points to a model JSON relative to the 'models' directory
        // Located at 'assets/examplemod/models/item/example_item.json'
        "model": "examplemod:item/example_item"
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new BlockModelWrapper.Unbaked(
            // Points to a model JSON relative to the 'models' directory
            // Located at 'assets/examplemod/models/item/example_item.json'
            ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
            Collections.emptyList()
        )
    );
}
```

</TabItem>
</Tabs>

### Tinting

Like most models, client items can change the color of the specified texture based on the properties of the stack. As such, the `minecraft:model` type has the `tints` field to define the opaque colors to apply. These are known as `ItemTintSource`s, which are defined in `ItemTintSources`. They also have a `type` field to define which source to use. The `tintindex` they are applied to is specified by their index within the list.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:model",
        // Points to 'assets/examplemod/models/item/example_item.json'
        "model": "examplemod:item/example_item",
        // A list of tints to apply
        "tints": [
            {
                // For when tintindex: 0
                "type": "minecraft:constant",
                // 0x00FF00 (or pure green)
                "value": 65280
            },
            {
                // For when tintindex: 1
                "type": "minecraft:dye",
                // 0x0000FF (or pure blue)
                // Only is called if `DataComponents#DYED_COLOR` is not set
                "default": 255
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new BlockModelWrapper.Unbaked(
            // Points to 'assets/examplemod/models/item/example_item.json'
            ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
            // A list of tints to apply
            List.of(
                // For when tintindex: 0
                new Constant(
                    // Pure green
                    0x00FF00
                ),
                // For when tintindex: 1
                new Dye(
                    // Pure blue
                    // Only is called if `DataComponents#DYED_COLOR` is not set
                    0x0000FF
                )
            )
        )
    );
}
```

</TabItem>
</Tabs>

Creating your own `ItemTintSource` is similar to any other codec-based registry object. You make a class that implements `ItemTintSource`, create a `MapCodec` to encode and decode the object, and register the codec to its registry via `RegisterColorHandlersEvent.ItemTintSources` on the [mod event bus][modbus]. The `ItemTintSource` only contains one method `calculate`, which takes in the current `ItemStack`, the level the stack is in, and the entity holding the stack to return an opaque color in ARGB format, where the top 8 bits are 0xFF.

```java
public record DamageBar(int defaultColor) implements ItemTintSource {

    // The map codec to register
    public static final MapCodec<DamageBar> MAP_CODEC = ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default")
        .xmap(DamageBar::new, DamageBar::defaultColor);

    public DamageBar(int defaultColor) {
        // Make sure the passed in color is opaque
        this.defaultColor = ARGB.opaque(defaultColor);
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return stack.isDamaged() ? ARGB.opaque(stack.getBarColor()) : defaultColor;
    }

    @Override
    public MapCodec<DamageBar> type() {
        return MAP_CODEC;
    }
}

// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
    event.register(
        // The name to reference as the type
        ResourceLocation.fromNamespaceAndPath("examplemod", "damage_bar"),
        // The map codec
        DamageBar.MAP_CODEC
    )
}
```

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:model",
        // Points to 'assets/examplemod/models/item/example_item.json'
        "model": "examplemod:item/example_item",
        // A list of tints to apply
        "tints": [
            {
                // For when tintindex: 0
                "type": "examplemod:damage_bar",
                // 0x00FF00 (or pure green)
                "default": 65280
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new BlockModelWrapper.Unbaked(
            // Points to 'assets/examplemod/models/item/example_item.json'
            ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
            // A list of tints to apply
            List.of(
                // For when tintindex: 0
                new DamageBar(
                    // Pure green
                    0x00FF00
                )
            )
        )
    );
}
```

</TabItem>
</Tabs>

## Composite Models

Sometimes, you may want to register multiple models for a single item. While this can be done directly with the [composite model loader][composite], for item models, there is a custom `minecraft:composite` type which takes a list of models to render.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:composite",

        // The models to render
        // Will render in the order they appear in the list
        "models": [
            {
                "type": "minecraft:model",
                // Points to 'assets/examplemod/models/item/example_item_1.json'
                "model": "examplemod:item/example_item_1"
            },
            {
                "type": "minecraft:model",
                // Points to 'assets/examplemod/models/item/example_item_2.json'
                "model": "examplemod:item/example_item_2"
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new CompositeModel.Unbaked(
            // The models to render
            // Will render in the order they appear in the list
            List.of(
                new BlockModelWrapper.Unbaked(
                    // Points to 'assets/examplemod/models/item/example_item_1.json'
                    ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_1"),
                    // A list of tints to apply
                    Collections.emptyList()
                ),
                new BlockModelWrapper.Unbaked(
                    // Points to 'assets/examplemod/models/item/example_item_2.json'
                    ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_2"),
                    // A list of tints to apply
                    Collections.emptyList()
                )
            )
        )
    );
}
```

</TabItem>
</Tabs>

## Property Models

Some items change their state depending on the data stored in their stack (e.g., pulling a bow, breaking an elytra, a clock when in a given dimension, etc.). To allow models to change based on state, item models can specify a property to keep track of and select a model based on that condition. There are three different types of property models: range dispatch, select, and conditional. Each of these act as a expression for some float, switch case, and boolean respectively.

### Range Dispatch Models

Range dispatch models have the type define some `RangeSelectItemModelProperty` to get some float to switch the model on. Each entry then has some threshold value which the float must be greater than to render. The model chosen is the one with the closest threshold value that is not over the property value (e.g., if the property values is `4` with thresholds `3` and `5`, then the model associated with `3` will be rendered, and if the value was `6`, then the model associated with `5` would be rendered). The available `RangeSelectItemModelProperty`s to use can be found in `RangeSelectItemModelProperties`.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:range_dispatch",

        // The `RangeSelectItemModelProperty` to use
        "property": "minecraft:count",
        // A scalar to multiply to the computed property value
        // If count was 0.3 and scale was 0.2, then the threshold checked would be 0.3*0.2=0.06
        "scale": 1,
        "fallback": {
            // The fallback model to use if no threshold matches
            // Can be any unbaked model type
            "type": "minecraft:model",
            // Points to 'assets/examplemod/models/item/example_item.json'
            "model": "examplemod:item/example_item"
        },

        // Properties defined by `Count`
        // When true, normalizes the count using its max stack size
        "normalize": true,

        // Entries with threshold information
        "entries": [
            {
                // When the count is a third of its current max stack size
                "threshold": 0.33,
                "model": {
                    // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_1.json'
                    "model": "examplemod:item/example_item_1"
                }
            },
            {
                // When the count is two thirds of its current max stack size
                "threshold": 0.66,
                "model": {
                    // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_2.json'
                    "model": "examplemod:item/example_item_2"
                }
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new RangeSelectItemModel.Unbaked(
            new Count(
                // When true, normalizes the count using its max stack size
                true
            ),
            // A scalar to multiply to the computed property value
            // If count was 0.3 and scale was 0.2, then the threshold checked would be 0.3*0.2=0.06
            1,
            // Entries with threshold information
            List.of(
                new RangeSelectItemModel.Entry(
                    // When the count is a third of its current max stack size
                    0.33,
                    // Can be any unbaked model type
                    new BlockModelWrapper.Unbaked(
                        // Points to 'assets/examplemod/models/item/example_item_1.json'
                        ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_1"),
                        // A list of tints to apply
                        Collections.emptyList()
                    )
                ),
                new RangeSelectItemModel.Entry(
                    // When the count is two thirds of its current max stack size
                    0.66,
                    // Can be any unbaked model type
                    new BlockModelWrapper.Unbaked(
                        // Points to 'assets/examplemod/models/item/example_item_2.json'
                        ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_2"),
                        // A list of tints to apply
                        Collections.emptyList()
                    )
                )
            ),
            // The fallback model to use if no threshold matches
            Optional.of(
                new BlockModelWrapper.Unbaked(
                    // Points to 'assets/examplemod/models/item/example_item.json'
                    ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
                    // A list of tints to apply
                    Collections.emptyList()
                )
            )
        )
    );
}
```

</TabItem>
</Tabs>

Creating your own `RangeSelectItemModelProperty` is similar to any other codec-based registry object. You make a class that implements `RangeSelectItemModelProperty`, create a `MapCodec` to encode and decode the object, and register the codec to its registry via `RegisterRangeSelectItemModelPropertyEvent` on the [mod event bus][modbus]. The `RangeSelectItemModelProperty` only contains one method `get`, which takes in the current `ItemStack`, the level the stack is in, the entity holding the stack, and some seeded value to return an arbitrary float to be interpreted by the ranged dispatch model.

```java
public record AppliedEnchantments() implements RangeSelectItemModelProperty {

    public static final MapCodec<AppliedEnchantments> MAP_CODEC = MapCodec.unit(new AppliedEnchantments());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return (float) stack.getEnchantments().size();
    }

    @Override
    public MapCodec<AppliedEnchantments> type() {
        return MAP_CODEC;
    }
}

// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event) {
    event.register(
        // The name to reference as the type
        ResourceLocation.fromNamespaceAndPath("examplemod", "applied_enchantments"),
        // The map codec
        AppliedEnchantments.MAP_CODEC
    )
}
```

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:range_dispatch",

        // The `RangeSelectItemModelProperty` to use
        "property": "examplemod:applied_enchantments",
        // A scalar to multiply to the computed property value
        // If count was 0.3 and scale was 0.2, then the threshold checked would be 0.3*0.2=0.06
        "scale": 0.5,
        "fallback": {
            // The fallback model to use if no threshold matches
            // Can be any unbaked model type
            "type": "minecraft:model",
            // Points to 'assets/examplemod/models/item/example_item.json'
            "model": "examplemod:item/example_item"
        },

        // Entries with threshold information
        "entries": [
            {
                // When there is at least one enchantment present
                // Since 1 * the scale 0.5 = 0.5
                "threshold": 0.5,
                "model": {
                    // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_1.json'
                    "model": "examplemod:item/example_item_1"
                }
            },
            {
                // When there are at least two enchantments present
                // Since 2 * the scale 0.5 = 1
                "threshold": 1,
                "model": {
                    // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_2.json'
                    "model": "examplemod:item/example_item_2"
                }
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new RangeSelectItemModel.Unbaked(
            new AppliedEnchantments(),
            // A scalar to multiply to the computed property value
            // If count was 0.3 and scale was 0.2, then the threshold checked would be 0.3*0.2=0.06
            0.5,
            // Entries with threshold information
            List.of(
                new RangeSelectItemModel.Entry(
                    // When there is at least one enchantment present
                    0.5,
                    // Can be any unbaked model type
                    new BlockModelWrapper.Unbaked(
                        // Points to 'assets/examplemod/models/item/example_item_1.json'
                        ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_1"),
                        // A list of tints to apply
                        Collections.emptyList()
                    )
                ),
                new RangeSelectItemModel.Entry(
                    // When there are at least two enchantments present
                    1,
                    // Can be any unbaked model type
                    new BlockModelWrapper.Unbaked(
                        // Points to 'assets/examplemod/models/item/example_item_2.json'
                        ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_2"),
                        // A list of tints to apply
                        Collections.emptyList()
                    )
                )
            ),
            // The fallback model to use if no threshold matches
            Optional.of(
                new BlockModelWrapper.Unbaked(
                    // Points to 'assets/examplemod/models/item/example_item.json'
                    ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
                    // A list of tints to apply
                    Collections.emptyList()
                )
            )
        )
    );
}
```

</TabItem>
</Tabs>

### Select Models

Select models are similar to range dispatch models, but they change switch based on some value defined by a `SelectItemModelProperty`, like a switch statement for an enum. The model chosen is the property which exactly matches the value in the switch case. The available `SelectItemModelProperty`s to use can be found in `SelectItemModelProperties`.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:select",

        // The `SelectItemModelProperty` to use
        "property": "minecraft:display_context",
        "fallback": {
            // The fallback model to use if no case matches
            // Can be any unbaked model type
            "type": "minecraft:model",
            "model": "examplemod:item/example_item"
        },

        // Switch cases based on Selectable Property
        "cases": [
            {
                // When the display context is `ItemDisplayContext#GUI`
                "when": "gui",
                "model": {
                    // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_1.json'
                    "model": "examplemod:item/example_item_1"
                }
            },
            {
                // When the display context is `ItemDisplayContext#FIRST_PERSON_RIGHT_HAND`
                "when": "firstperson_righthand",
                "model": {
                     // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_2.json'
                    "model": "examplemod:item/example_item_2"
                }
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new SelectItemModel.Unbaked(
            new SelectItemModel.UnbakedSwitch(
                // The `SelectItemModelProperty` to use
                new DisplayContext(),
                // Switch cases based on selectable property
                List.of(
                    new SelectItemModel.SwitchCase(
                        // The list of cases to match for this model
                        List.of(ItemDisplayContext.GUI),
                        // Can be any unbaked model type
                        new BlockModelWrapper.Unbaked(
                            // Points to 'assets/examplemod/models/item/example_item_1.json'
                            ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_1"),
                            // A list of tints to apply
                            Collections.emptyList()
                        )
                    ),
                    new SelectItemModel.SwitchCase(
                        // The list of cases to match for this model
                        List.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND),
                        // Can be any unbaked model type
                        new BlockModelWrapper.Unbaked(
                            // Points to 'assets/examplemod/models/item/example_item_2.json'
                            ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_2"),
                            // A list of tints to apply
                            Collections.emptyList()
                        )
                    )
                )
            ),
            // The fallback model to use if no case matches
            Optional.of(
                new BlockModelWrapper.Unbaked(
                    // Points to 'assets/examplemod/models/item/example_item.json'
                    ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
                    // A list of tints to apply
                    Collections.emptyList()
                )
            )
        )
    );
}
```

</TabItem>
</Tabs>

Creating your own `SelectItemModelProperty` is similar to a codec-based registry object. You make a class that implements `SelectItemModelProperty<T>`, create a `Codec` to serialize and deserialize the property value, create a `MapCodec` to encode and decode the object, and register the codec to its registry via `RegisterSelectItemModelPropertyEvent` on the [mod event bus][modbus]. The `SelectItemModelProperty` has a generic `T` that represents the value to switch on. It only contains one method `get`, which takes in the current `ItemStack`, the level the stack is in, the entity holding the stack, some seeded value, and the display context of the item to return an arbitrary `T` to be interpreted by the select model.

```java
// The select property class
public record StackRarity() implements SelectItemModelProperty<Rarity> {

    // The object to register that contains the relevant codecs
    public static final SelectItemModelProperty.Type<StackRarity, Rarity> TYPE = SelectItemModelProperty.Type.create(
        // The map codec for this property
        MapCodec.unit(new StackRarity()),
        // The codec for the object being selected
        // Used to serialize the case entries ("when": <property value>)
        Rarity.CODEC
    );

    @Nullable
    @Override
    public Rarity get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        // When null, uses the fallback model
        return stack.get(DataComponents.RARITY);
    }

    @Override
    public SelectItemModelProperty.Type<StackRarity, Rarity> type() {
        return TYPE;
    }
}

// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
    event.register(
        // The name to reference as the type
        ResourceLocation.fromNamespaceAndPath("examplemod", "rarity"),
        // The property type
        StackRarity.TYPE
    )
}
```

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:select",

        // The `SelectItemModelProperty` to use
        "property": "examplemod:rarity",
        "fallback": {
            // The fallback model to use if no case matches
            // Can be any unbaked model type
            "type": "minecraft:model",
            "model": "examplemod:item/example_item"
        },

        // Switch cases based on Selectable Property
        "cases": [
            {
                // When the rarity is `Rarity#UNCOMMON`
                "when": "uncommon",
                "model": {
                    // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_1.json'
                    "model": "examplemod:item/example_item_1"
                }
            },
            {
                // When the rarity is `Rarity#RARE`
                "when": "rare",
                "model": {
                     // Can be any unbaked model type
                    "type": "minecraft:model",
                    // Points to 'assets/examplemod/models/item/example_item_2.json'
                    "model": "examplemod:item/example_item_2"
                }
            }
        ]
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new SelectItemModel.Unbaked(
            new SelectItemModel.UnbakedSwitch(
                // The `SelectItemModelProperty` to use
                new StackRarity(),
                // Switch cases based on selectable property
                List.of(
                    new SelectItemModel.SwitchCase(
                        // The list of cases to match for this model
                        List.of(Rarity.UNCOMMON),
                        // Can be any unbaked model type
                        new BlockModelWrapper.Unbaked(
                            // Points to 'assets/examplemod/models/item/example_item_1.json'
                            ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_1"),
                            // A list of tints to apply
                            Collections.emptyList()
                        )
                    ),
                    new SelectItemModel.SwitchCase(
                        // The list of cases to match for this model
                        List.of(Rarity.RARE),
                        // Can be any unbaked model type
                        new BlockModelWrapper.Unbaked(
                            // Points to 'assets/examplemod/models/item/example_item_2.json'
                            ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_2"),
                            // A list of tints to apply
                            Collections.emptyList()
                        )
                    )
                )
            ),
            // The fallback model to use if no case matches
            Optional.of(
                new BlockModelWrapper.Unbaked(
                    // Points to 'assets/examplemod/models/item/example_item.json'
                    ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
                    // A list of tints to apply
                    Collections.emptyList()
                )
            )
        )
    );
}
```

</TabItem>
</Tabs>

### Conditional Models

Conditional models are the simplest out of the three. The type defines some `ConditionalItemModelProperty` to get a boolean to switch the model on. The model chosen based on whether the returned boolean is true or false. The available `ConditionalItemModelProperty`s to use can be found in `ConditionalItemModelProperties`.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:condition",

        // The `ConditionalItemModelProperty` to use
        "property": "minecraft:damaged",

        // What the boolean outcome is
        "on_true": {
            // Can be any unbaked model type
            "type": "minecraft:model",
            // Points to 'assets/examplemod/models/item/example_item_1.json'
            "model": "examplemod:item/example_item_1"
            
        },
        "on_false": {
            // Can be any unbaked model type
            "type": "minecraft:model",
            // Points to 'assets/examplemod/models/item/example_item_2.json'
            "model": "examplemod:item/example_item_2"
        }
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new ConditionalItemModel.Unbaked(
            // The property to check
            new Damaged(),
            // When the boolean is true
            new BlockModelWrapper.Unbaked(
                // Points to 'assets/examplemod/models/item/example_item_1.json'
                ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_1"),
                // A list of tints to apply
                Collections.emptyList()
            ),
            // When the boolean is false
            new BlockModelWrapper.Unbaked(
                // Points to 'assets/examplemod/models/item/example_item_2.json'
                ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_2"),
                // A list of tints to apply
                Collections.emptyList()
            )
        )
    );
}
```

</TabItem>
</Tabs>

Creating your own `ConditionalItemModelProperty` is similar to any other codec-based registry object. You make a class that implements `ConditionalItemModelProperty`, create a `MapCodec` to encode and decode the object, and register the codec to its registry via `RegisterConditionalItemModelPropertyEvent` on the [mod event bus][modbus]. The `RangeSelectItemModelProperty` only contains one method `get`, which takes in the current `ItemStack`, the level the stack is in, the entity holding the stack, some seeded value, and the display context of the item to return an arbitrary boolean to be interpreted by the conditional model (`on_true` or `on_false`).

```java
public record BarVisible() implements ConditionalItemModelProperty {

    public static final MapCodec<BarVisible> MAP_CODEC =  MapCodec.unit(new BarVisible());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return stack.isBarVisible();
    }

    @Override
    public MapCodec<BarVisible> type() {
        return MAP_CODEC;
    }
}

// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
    event.register(
        // The name to reference as the type
        ResourceLocation.fromNamespaceAndPath("examplemod", "bar_visible"),
        // The map codec
        BarVisible.MAP_CODEC
    )
}
```

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:condition",

        // The `ConditionalItemModelProperty` to use
        "property": "examplemod:bar_visible",

        // What the boolean outcome is
        "on_true": {
            // Can be any unbaked model type
            "type": "minecraft:model",
            // Points to 'assets/examplemod/models/item/example_item_1.json'
            "model": "examplemod:item/example_item_1"
            
        },
        "on_false": {
            // Can be any unbaked model type
            "type": "minecraft:model",
            // Points to 'assets/examplemod/models/item/example_item_2.json'
            "model": "examplemod:item/example_item_2"
        }
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new ConditionalItemModel.Unbaked(
            // The property to check
            new BarVisible(),
            // When the boolean is true
            new BlockModelWrapper.Unbaked(
                // Points to 'assets/examplemod/models/item/example_item_1.json'
                ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_1"),
                // A list of tints to apply
                Collections.emptyList()
            ),
            // When the boolean is false
            new BlockModelWrapper.Unbaked(
                // Points to 'assets/examplemod/models/item/example_item_2.json'
                ResourceLocation.fromNamespaceAndPath("examplemod", "item/example_item_2"),
                // A list of tints to apply
                Collections.emptyList()
            )
        )
    );
}
```

</TabItem>
</Tabs>

## Special Models

Not all models can be rendered using the basic model JSON. Some models can be dynamically rendered, or use existing models created for a [`BlockEntityRenderer`][ber]. In these instances, there is a special model type which allows the user to define their own rendering logic to use. These are known as `SpecialModelRenderer`s, which are defined within `SpecialModelRenderers`.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:special",

        // The parent model to read the particle texture and display transformation from
        // Points to 'assets/minecraft/models/item/template_skull.json'
        "base": "minecraft:item/template_skull",
        "model": {
            // The special model renderer to use
            "type": "minecraft:head",

            // Properties defined by `SkullSpecialRenderer.Unbaked`
            // The type of the skull block
            "kind": "wither_skeleton",
            // The texture to use when rendering the head
            // Points to 'assets/examplemod/textures/entity/heads/skeleton_override.png'
            "texture": "examplemod:heads/skeleton_override",
            // The animation float used to animate the head model
            "animation": 0.5
        }
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new SpecialModelWrapper.Unbaked(
            // The parent model to read the particle texture and display transformation from
            // Points to 'assets/minecraft/models/item/template_skull.json'
            ResourceLocation.fromNamespaceAndPath("minecraft", "item/template_skull"),
            // The special model renderer to use
            new SkullSpecialRenderer.Unbaked(
                // The type of the skull block
                SkullBlock.Types.WITHER_SKELETON,
                // The texture to use when rendering the head
                // Points to 'assets/examplemod/textures/entity/heads/skeleton_override.png'
                Optional.of(
                    ResourceLocation.fromNamespaceAndPath("examplemod", "heads/skeleton_override")
                ),
                // The animation float used to animate the head model
                0.5f
            )
        )
    );
}
```

</TabItem>
</Tabs>

Creating your own `SpecialModelRenderer` is broken into three parts: the `SpecialModelRenderer` instance used to render the item, the `SpecialModelRenderer.Unbaked` instance used to read and write to JSON, and the registration to use the renderer when as an item or, if necessary, when as a block.

First, there is the `SpecialModelRenderer`. This works similarly to any other renderer class (e.g. block entity renderers, entity renderers). It should take in the static data used during the rendering process (e.g., the `Model` instance, the `Material` of the texture, etc.). There are two methods to be aware of. First, there is `extractArgument`. This is used to limit the amount of data available to the `render` method by only supplying what is necessary from the `ItemStack`.

:::note
If you don't know what data you may need, you can just have this return the `ItemStack` in question. If you need no data from the stack, you can instead use `NoDataSpecialModelRenderer`, which implements this method for you.
:::

Next is the `render` method. This takes in value returned from `extractArgument`, the display context of the item, the pose stack to render in, the buffer sources available to use, the packed light, the overlay texture, and a boolean if the stack is foiled (e.g. enchanted). All rendering should happen in this method.

```java
public record ExampleSpecialRenderer(Model model, Material material) implements SpecialModelRenderer<Boolean> {

    @Nullable
    public Boolean extractArgument(ItemStack stack) {
        // Extract the data to be used
        return stack.isBarVisible();
    }

    // Render the model
    @Override
    public void render(@Nullable Boolean barVisible, ItemDisplayContext displayContext, PoseStack pose, MultiBufferSource bufferSource, int light, int overlay, boolean hasFoil) {
        this.model.renderToBuffer(pose, this.material.buffer(bufferSource, barVisible ? RenderType::entityCutout : RenderType::entitySolid), light, overlay);
    }
}
```

Next is the `SpecialModelRenderer.Unbaked` instance. This should contain data that can be read from a file to determine what to pass into the special renderer. This also contains two methods: `bake`, which is used to construct the special renderer instance; and `type`, which defines the `MapCodec` to use for encoding/decoding to file.

```java
public record ExampleSpecialRenderer(Model model, Material material) implements SpecialModelRenderer<Boolean> {

    // ...

    public record Unbaked(ResourceLocation texture) implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<ExampleSpecialRenderer.Unbaked> MAP_CODEC = ResourceLocation.CODEC.fieldOf("texture")
            .xmap(ExampleSpecialRenderer.Unbaked::new, ExampleSpecialRenderer.Unbaked::texture);

        @Override
        public MapCodec<ExampleSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            // Resolve resource location to absolute path
            ResourceLocation textureLoc = this.texture.withPath(path -> "textures/entity/" + path + ".png");

            // Get the model and the material to render
            return new ExampleSpecialRenderer(...);
        }
    }
}
```

Finally, we register the objects to their necessary locations. For the client items, this is done via `RegisterSpecialModelRendererEvent` on the [mod event bus][modbus]. If the special renderer should also be used as part of a `BlockEntityRenderer`, such as when rendering in some item-like context (e.g., enderman holding the block), then an `Unbaked` version for the block should be registered via `RegisterSpecialBlockModelRendererEvent` on the [mod event bus][modbus].

```java
// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
    event.register(
        // The name to reference as the type
        ResourceLocation.fromNamespaceAndPath("examplemod", "example_special"),
        // The map codec
        ExampleSpecialRenderer.Unbaked.MAP_CODEC
    )
}

// For rendering a block in an item-like context
// Assume some DeferredBlock<ExampleBlock> EXAMPLE_BLOCK
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerSpecialBlockRenderers(RegisterSpecialBlockModelRendererEvent event) {
    event.register(
        // The block to render for
        EXAMPLE_BLOCK.get()
        // The unbaked instance to use
        new ExampleSpecialRenderer.Unbaked(ResourceLocation.fromNamespaceAndPath("examplemod", "entity/example_special"))
    )
}
```

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "minecraft:special",

        // The parent model to read the particle texture and display transformation from
        // Points to 'assets/minecraft/models/item/template_skull.json'
        "base": "minecraft:item/template_skull",
        "model": {
            // The special model renderer to use
            "type": "examplemod:example_special",

            // Properties defined by `ExampleSpecialRenderer.Unbaked`
            // The texture to use when rendering
            // Points to 'assets/examplemod/textures/entity/example/example_texture.png'
            "texture": "examplemod:example/example_texture"
        }
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new SpecialModelWrapper.Unbaked(
            // The parent model to read the particle texture and display transformation from
            // Points to 'assets/minecraft/models/item/template_skull.json'
            ResourceLocation.fromNamespaceAndPath("minecraft", "item/template_skull"),
            // The special model renderer to use
            new ExampleSpecialRenderer.Unbaked(
                // The texture to use when rendering
                // Points to 'assets/examplemod/textures/entity/example/example_texture.png'
                ResourceLocation.fromNamespaceAndPath("examplemod", "example/example_texture")
            )
        )
    );
}
```

</TabItem>
</Tabs>

## Dynamic Fluid Container

NeoForge adds an item model that constructs a dynamic fluid container, capable of re-texturing itself at runtime to match the contained fluid.

:::note
For the fluid tint to apply to the fluid texture, the item in question must have a `Capabilities.FluidHandler.ITEM` attached. If your item does not directly use `BucketItem` (not a subtype either), then you need to [register the capability to your item][capability].
:::

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "neoforge:fluid_container",

        // The textures used to construct the container
        // These are in reference to the block atlas, so they are relative to the `textures` directory
        "textures": {
            // Sets the model particle sprite
            // If not set, uses the first texture that is not null:
            // - Fluid still texture
            // - Container base texture
            // - Container cover texture, if not used as a mask
            // Points to 'assets/minecraft/textures/item/bucket.png'
            "particle": "minecraft:item/bucket",
            // Sets the texture to use on the first layer, generally the container of the fluid
            // If not set, the layer will not be added
            // Points to 'assets/minecraft/textures/item/bucket.png'
            "base": "minecraft:item/bucket",
            // Sets the texture to use as the mask for the still fluid texture
            // Areas where the fluid is seen should be pure white
            // If not set or the fluid is empty, then the layer is not rendered
            // Points to 'assets/neoforge/textures/item/mask/bucket_fluid.png'
            "fluid": "neoforge:item/mask/bucket_fluid",
            // Sets the texture to use as either
            // - The overlay texture when 'cover_is_mask' is false
            // - The mask to apply to the base texture (should be pure white to see) when 'cover_is_mask' is true
            // If not set or no base texture is set when 'cover_is_mask' is true, then the layer is not rendered
            // Points to 'assets/neoforge/textures/item/mask/bucket_fluid_cover.png'
            "cover": "neoforge:item/mask/bucket_fluid_cover",
        },

        // When true, rotates the model 180 degrees for fluids whose density is negative or zero
        // Defaults to false
        "flip_gas": true,
        // When true, uses the cover texture as a mask for the base texture
        // Defaults to true
        "cover_is_mask": true,
        // When true, sets the lightmap of the fluid texture layer to its max value
        // for fluids whose light level is greater than zero
        // Defaults to true
        "apply_fluid_luminosity": false
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<ExampleFluidContainerItem> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new DynamicFluidContainerModel.Unbaked(
            // The textures used to construct the container
            // These are in reference to the block atlas, so they are relative to the `textures` directory
            new DynamicFluidContainerModel.Textures(
                // Sets the model particle sprite
                // If not set, uses the first texture that is not null:
                // - Fluid still texture
                // - Container base texture
                // - Container cover texture, if not used as a mask
                // Points to 'assets/minecraft/textures/item/bucket.png'
                Optional.of(ResourceLocation.withDefaultNamespace("item/bucket")),
                // Sets the texture to use on the first layer, generally the container of the fluid
                // If not set, the layer will not be added
                // Points to 'assets/minecraft/textures/item/bucket.png'
                Optional.of(ResourceLocation.withDefaultNamespace("item/bucket")),
                // Sets the texture to use as the mask for the still fluid texture
                // Areas where the fluid is seen should be pure white
                // If not set or the fluid is empty, then the layer is not rendered
                // Points to 'assets/neoforge/textures/item/mask/bucket_fluid.png'
                Optional.of(ResourceLocation.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid")),
                // Sets the texture to use as either
                // - The overlay texture when 'cover_is_mask' is false
                // - The mask to apply to the base texture (should be pure white to see) when 'cover_is_mask' is true
                // If not set or no base texture is set when 'cover_is_mask' is true, then the layer is not rendered
                // Points to 'assets/neoforge/textures/item/mask/bucket_fluid_cover.png'
                Optional.of(ResourceLocation.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid_cover"))
            ),
            // When true, rotates the model 180 degrees
            // Defaults to false
            true,
            // When true, uses the cover texture as a mask for the base texture
            // Defaults to true
            true,
            // When true, sets the lightmap of the fluid texture layer to its max value
            // Defaults to true
            false
        )
    );
}
```

</TabItem>
</Tabs>

## Manually Rendering an Item

If you need to render an item yourself, such as in some `BlockEntityRenderer` or `EntityRenderer`, it can be achieved through three steps. First, the renderer in question creates an `ItemStackRenderState` to hold the rendering information of the stack. Then, the `ItemModelResolver` updates the `ItemStackRenderState` using one of its methods to update the state to the current item to render. Finally, the item is rendered using the render state's `render` method.

The `ItemStackRenderState` keeps track of the data used to render. Each 'model' is given its own `ItemStackRenderState.LayerRenderState`, which contains the `BakedModel` to render, along with its render type, foil status, tint information, and any special renderers used. Layers are created using the `newLayer` method, and cleared for rendering using the `clear` method. If a predefined number of layers is used, then `ensureCapacity` is used to make sure there are the necessary number of `LayerRenderStates` to render properly. The `ItemStackRenderState` also contains some methods to get the `BakedModel` properties for the first layer except for `pickParticleIcon`, which gets the particle texture for a random layer.

`ItemModelResolver` is responsible for updating the `ItemStackRenderState`. This is done through either `updateForLiving` for items held by living entities, `updateForNonLiving` for items held by other kinds of entities, and `updateForTopItem` for all other cases. These methods take in the render state, stack to render, and current display context. The other parameters update information about the held hand, level, entity, and seeded value. Each method calls `ItemStackRenderState#clear` before calling `update` on the `ItemModel` obtained from  `DataComponents#ITEM_MODEL`. The `ItemModelResolver` can always be obtained via `Minecraft#getItemModelResolver` if you are not within some renderer context (e.g., `BlockEntityRenderer`, `EntityRenderer`).

## Custom Item Model Defintions

Creating your own `ItemModel` is broken into three parts: the `ItemModel` instance used update the render state, the `ItemModel.Unbaked` instance used to read and write to JSON, and the registration to use the `ItemModel`.

:::warning
Please make sure to check that your required item model can not be created with the existing systems above. In most cases, it is not necessary to create a custom `ItemModel`.
:::

First, there is the `ItemModel`. This is responsible for updating the `ItemStackRenderState` such that the item is rendered correctly. It should take in the static data used during the rendering process (e.g., the `BakedModel` instance, property information, etc.). The only method is `update`, which takes in the render state, stack, model resolver, display context, level, holding entity, and some seeded value to update the `ItemStackRenderState`. `ItemStackRenderState` should be the only parameter modified, with the rest treated as read-only data.

```java
public record RenderTypeModelWrapper(BakedModel model, RenderType type) implements ItemModel {

    // Update the render state
    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        // Create a new layer to render the model in
        ItemStackRenderState.LayerRenderState layerState = state.newLayer();
        if (stack.hasFoil()) {
            layerState.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        }
        layerState.setupBlockModel(this.model, this.type);
    }
}
```

Next is the `ItemModel.Unbaked` instance. This should contain data that can be read from a file to determine what to pass into the item model. This also contains two methods: `bake`, which is used to construct the `ItemModel` instance; and `type`, which defines the `MapCodec` to use for encoding/decoding to file.

```java
public record RenderTypeModelWrapper(BakedModel model, RenderType type) implements ItemModel {

    // ...

     public record Unbaked(ResourceLocation model, RenderType type) implements ItemModel.Unbaked {
        // Create a render type map for the codec
        private static final BiMap<String, RenderType> RENDER_TYPES = Util.make(HashBiMap.create(), map -> {
            map.put("translucent_item", Sheets.translucentItemSheet());
            map.put("cutout_block", Sheets.cutoutBlockSheet());
        });
        private static final Codec<RenderType> RENDER_TYPE_CODEC = ExtraCodecs.idResolverCodec(Codec.STRING, RENDER_TYPES::get, RENDER_TYPES.inverse()::get);

        // The map codec to register
        public static final MapCodec<RenderTypeModelWrapper.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(RenderTypeModelWrapper.Unbaked::model),
                RENDER_TYPE_CODEC.fieldOf("render_type").forGetter(RenderTypeModelWrapper.Unbaked::type)
            )
            .apply(instance, RenderTypeModelWrapper.Unbaked::new)
        );

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            // Resolve model dependencies, so pass in all known resource locations
            resolver.resolve(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            // Get the baked model and return
            BakedModel baked = context.bake(this.model);
            return new RenderTypeModelWrapper(baked, this.type);
        }

        @Override
        public MapCodec<RenderTypeModelWrapper.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
```

Then, we register the map codec via `RegisterItemModelsEvent` on the [mod event bus][modbus].

```java
// In some event handler class
@SubscribeEvent // on the mod event bus only on the physical client
public static void registerItemModels(RegisterItemModelsEvent event) {
    event.register(
        // The name to reference as the type
        ResourceLocation.fromNamespaceAndPath("examplemod", "render_type"),
        // The map codec
        RenderTypeModelWrapper.Unbaked.MAP_CODEC
    )
}
```

Finally, we can use the `ItemModel` in our JSON or as part of the datagen process.

<Tabs>
<TabItem value="json" label="JSON" default>

```json5
// For some item 'examplemod:example_item'
// JSON at 'assets/examplemod/items/example_item.json'
{
    "model": {
        "type": "examplemod:render_type",
        // Points to 'assets/examplemod/models/item/example_item.json'
        "model": "examplemod:item/example_item",
        // Set the render type to use when rendering
        "render_type": "cutout_block"
    }
}
```

</TabItem>

<TabItem value="datagen" label="Datagen">

```java
// Assume there is some DeferredItem<Item> EXAMPLE_ITEM
// Within an extended ModelProvider
@Override
protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
    itemModels.itemModelOutput.accept(
        EXAMPLE_ITEM.get(),
        new RenderTypeModelWrapper.Unbaked(
            // Points to 'assets/examplemod/models/item/example_item.json'
            ModelLocationUtils.getModelLocation(EXAMPLE_ITEM.get()),
            // Set the render type to use when rendering
            Sheets.cutoutBlockSheet()
        )
    );
}
```

</TabItem>
</Tabs>

[assets]: ../../index.md#assets
[bakedmodels]: ../models/bakedmodel.md
[ber]: ../../../blockentities/ber.md
[capability]: ../../../inventories/capabilities.md#registering-capabilities
[composite]: modelloaders.md#composite-model
[itemmodel]: #manually-rendering-an-item
[modbus]: ../../../concepts/events.md#event-buses
[rl]: ../../../misc/resourcelocation.md
