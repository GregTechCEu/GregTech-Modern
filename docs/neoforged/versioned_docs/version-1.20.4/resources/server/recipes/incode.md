# Non-Datapack Recipes

Not all recipes are simplistic enough or migrated to using data-driven recipes. Some subsystems still need to be patched within the codebase to provide support for adding new recipes.

## Brewing Recipes

Brewing is one of the few recipes that still exist in code. Brewing recipes are added as part of a bootstrap within `PotionBrewing` for their containers, container recipes, and potion mixes. To expand upon the existing system, NeoForge allows brewing recipes to be added by calling `BrewingRecipeRegistry#addRecipe` in `FMLCommonSetupEvent`.

:::caution
`BrewingRecipeRegistry#addRecipe` must be called within the synchronous work queue via `#enqueueWork` as the method is not thread-safe.
:::

The default implementation takes in an input ingredient, a catalyst ingredient, and a stack output for a standard implementation. Additionally, an `IBrewingRecipe` instance can be supplied instead to do the transformations.

### IBrewingRecipe

`IBrewingRecipe` is a pseudo-[`Recipe`][recipe] interface that checks whether the input and catalyst is valid and provides the associated output if so. This is provided through `#isInput`, `#isIngredient`, and `#getOutput` respectively. The output method has access to the input and catalyst stacks to construct the result.

:::caution
When copying data between `ItemStack`s or `CompoundTag`s, make sure to use their respective `#copy` methods to create unique instances.
:::

There is no wrapper for adding additional potion containers or potion mixes similar to vanilla. A new `IBrewingRecipe` implementation will need to be added to replicate this behavior.

## Anvil Recipes

Anvils are responsible for taking a damaged input and given some material or a similar input, remove some of the damage on the input result. As such, its system is not easily data-driven. However, as anvil recipes are an input with some number of materials equals some output when the user has the required experience levels, it can be modified to create a pseudo-recipe system via `AnvilUpdateEvent`. This takes in the input and materials and allows the modder to specify the output, experience level cost, and number of materials to use for the output. The event can also prevent any output by [canceling][cancel] it.

```java
// Checks whether the left and right items are correct
// When true, sets the output, level experience cost, and material amount
@SubscribeEvent // on the game event bus
public static void updateAnvil(AnvilUpdateEvent event) {
  if (event.getLeft().is(...) && event.getRight().is(...)) {
    event.setOutput(...);
    event.setCost(...);
    event.setMaterialCost(...);
  }
}
```

The update event must be [attached] to the NeoForge event bus.

## Loom Recipes

Looms are responsible for applying a dye and pattern (either from the loom or from an item) to a banner. While the banner and the dye must be a `BannerItem` or `DyeItem` respectively, custom patterns can be created and applied in the loom. Banner Patterns can be created by [registering] a `BannerPattern`.

:::caution
`BannerPattern`s which are in the `minecraft:no_item_required` tag appear as an option in the loom. Patterns not in this tag must have an accompanying `BannerPatternItem` to be used along with an associated tag.
:::

```java
private static final DeferredRegister<BannerPattern> REGISTER = DeferredRegister.create(Registries.BANNER_PATTERN, "examplemod");

// Takes in the pattern name to send over the network
public static final BannerPattern EXAMPLE_PATTERN = REGISTER.register("example_pattern", () -> new BannerPattern("examplemod:ep"));
```

[recipe]: ./custom.md#recipe
[cancel]: ../../../concepts/events.md#cancellable-events
[attached]: ../../../concepts/events.md#registering-an-event-handler
[registering]: ../../../concepts/registries.md
