# Built-In Recipe Types

Minecraft provides a variety of recipe types and serializers out of the box for you to use. This article will explain each recipe type, as well as how to generate them.

## Crafting

Crafting recipes are typically made in crafting tables, crafters, or in modded crafting tables or machines. Their recipe type is `minecraft:crafting`.

### Shaped Crafting

Some of the most important recipes - such as the crafting table, sticks, or most tools - are created through shaped recipes. These recipes are defined by a crafting pattern or shape (hence "shaped") in which the items must be inserted. Let's have a look at what an example looks like:

```json5
{
    "type": "minecraft:crafting_shaped",
    "category": "equipment",
    "key": {
        "#": "minecraft:stick",
        "X": "minecraft:iron_ingot"
    },
    "pattern": [
        "XXX",
        " # ",
        " # "
    ],
    "result": {
        "count": 1,
        "id": "minecraft:iron_pickaxe"
    }
}
```

Let's digest this line for line:

- `type`: This is the id of the shaped recipe serializer, `minecraft:crafting_shaped`.
- `category`: This optional field defines the `CraftingBookCategory` in the crafting book.
- `key` and `pattern`: Together, these define how the items must be put into the crafting grid.
    - The pattern defines up to three lines of up to three-wide strings that define the shape. All lines must be the same length, i.e. the pattern must form a rectangular shape. Spaces can be used to denote slots that should stay empty.
    - The key associates the characters used in the pattern with [ingredients][ingredient]. In the above example, all `X`s in the pattern must be iron ingots, and all `#`s must be sticks.
- `result`: The result of the recipe. This is [an item stack template's JSON representation][itemjson].
- Not shown in the example is the `group` key. This optional string property creates a group in the recipe book. Recipes in the same group will be displayed as one in the recipe book.
- Not shown in the example is `show_notification`. This optional boolean, when false, disables the toast shown on the top right hand corner on first use or unlock.

And then, let's have a look at how you'd generate this recipe within `RecipeProvider#buildRecipes`:

```java
// We use a builder pattern, therefore no variable is created. Create a new builder by calling
// ShapedRecipeBuilder#shaped with the recipe category (found in the RecipeCategory enum)
// and a result item, a result item and count, or a result item stack template.
ShapedRecipeBuilder.shaped(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.TOOLS, Items.IRON_PICKAXE)
        // Create the lines of your pattern. Each call to #pattern adds a new line.
        // Patterns will be validated, i.e. their shape will be checked.
        .pattern("XXX")
        .pattern(" # ")
        .pattern(" # ")
        // Create the keys for the pattern. All non-space characters used in the pattern must be defined.
        // This can either accept Ingredients, TagKey<Item>s or ItemLikes, i.e. items or blocks.
        .define('X', Items.IRON_INGOT)
        .define('#', Items.STICK)
        // Creates the recipe advancement. While not mandated by the consuming background systems,
        // the recipe builder will crash if you omit this. The first parameter is the advancement name,
        // and the second one is the condition. Normally, you want to use the has() shortcut for the condition.
        // Multiple advancement requirements can be added by calling #unlockedBy multiple times.
        .unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
        // Stores the recipe in the passed RecipeOutput, to be written to disk.
        // If you want to add conditions to the recipe, those can be set on the output.
        .save(this.output);
```

Additionally, you can call `#group` and `#showNotification` to set the recipe book group and toggle the toast pop-up, respectively.

### Shapeless Crafting

Unlike shaped crafting recipes, shapeless crafting recipes do not care about the order the ingredients are passed in. As such, there is no pattern and key, instead there is just a list of ingredients:

```json5
{
    "type": "minecraft:crafting_shapeless",
    "category": "misc",
    "ingredients": [
        "minecraft:brown_mushroom",
        "minecraft:red_mushroom",
        "minecraft:bowl"
    ],
    "result": {
        "count": 1,
        "id": "minecraft:mushroom_stew"
    }
}
```

Like before, let's digest this line for line:

- `type`: This is the id of the shapeless recipe serializer, `minecraft:crafting_shapeless`.
- `category`: This optional field defines the category in the crafting book.
- `ingredients`: A list of [ingredients][ingredient]. The list order is preserved in code for recipe viewing purposes, but the recipe itself accepts the ingredients in any order.
- `result`: The result of the recipe. This is [an item stack template's JSON representation][itemjson].
- Not shown in the example is the `group` key. This optional string property creates a group in the recipe book. Recipes in the same group will be displayed as one in the recipe book.

And then, let's have a look at how you'd generate this recipe in `RecipeProvider#buildRecipes`:

```java
// We use a builder pattern, therefore no variable is created. Create a new builder by calling
// ShapelessRecipeBuilder#shapeless with the recipe category (found in the RecipeCategory enum)
// and a result item, a result item and count, or a result item stack template.
ShapelessRecipeBuilder.shapeless(this.registries.lookupOrThrow(Registries.ITEM), RecipeCategory.MISC, Items.MUSHROOM_STEW)
        // Add the recipe ingredients. This can either accept Ingredients, TagKey<Item>s or ItemLikes.
        // Overloads also exist that additionally accept a count, adding the same ingredient multiple times.
        .requires(Blocks.BROWN_MUSHROOM)
        .requires(Blocks.RED_MUSHROOM)
        .requires(Items.BOWL)
        // Creates the recipe advancement. While not mandated by the consuming background systems,
        // the recipe builder will crash if you omit this. The first parameter is the advancement name,
        // and the second one is the condition. Normally, you want to use the has() shortcut for the condition.
        // Multiple advancement requirements can be added by calling #unlockedBy multiple times.
        .unlockedBy("has_mushroom_stew", this.has(Items.MUSHROOM_STEW))
        .unlockedBy("has_bowl", this.has(Items.BOWL))
        .unlockedBy("has_brown_mushroom", this.has(Blocks.BROWN_MUSHROOM))
        .unlockedBy("has_red_mushroom", this.has(Blocks.RED_MUSHROOM))
        // Stores the recipe in the passed RecipeOutput, to be written to disk.
        // If you want to add conditions to the recipe, those can be set on the output.
        .save(this.output);
```

Additionally, you can call `#group` to set the recipe book group.

:::info
One-item recipes (e.g. storage blocks unpacking) should be shapeless recipes to follow vanilla standards.
:::

### Imbuing Items

Imbue recipes are a special type of single item crafting recipes where the potion contents of the material stack are copied to the resulting stack. For example:

```json5
{
    "type": "minecraft:crafting_imbue",
    "category": "misc",
    "material": "minecraft:arrow",
    "result": {
        "count": 8,
        "id": "minecraft:tipped_arrow"
    },
    "source": "minecraft:lingering_potion"
}
```

Like before, let's digest this line for line:

- `type`: This is the id of the recipe serializer, `minecraft:crafting_imbue`.
- `category`: This optional field defines the category in the crafting book.
- `group`: This optional string property creates a group in the recipe book. Recipes in the same group will be displayed as one in the recipe book, which typically makes sense for transmuted recipes.
- `source`: The [ingredient] that contains the potion contents to imbue.
- `material`: The [ingredient] used to imbue the source.
- `result`: The result of the recipe. This is [an item stack template's JSON representation][itemjson].

And then, let's have a look at how you'd generate this recipe in `RecipeProvider#buildRecipes`:

```java
// We use a builder pattern, therefore no variable is created. Create a new builder by calling
// CustomCraftingRecipeBuilder#customCrafting with the recipe category (found in the RecipeCategory enum)
// and a factory function that takes in the `Recipe.CommonInfo` and `CraftingRecipe.CraftingBookInfo`
// to return the `Recipe` instance.
CustomCraftingRecipeBuilder.customCrafting(
    RecipeCategory.MISC,
    // The function used to construct the recipe instance.
    (commonInfo, bookInfo) -> new ImbueRecipe(
        commonInfo, bookInfo,
        // The source that contains the potion contents.
        Ingredient.of(Items.LINGERING_POTION),
        // The material used to imbue the source.
        Ingredient.of(Items.ARROW),
        // The resulting template with the potion contents.
        new ItemStackTemplate(Items.TIPPED_ARROW, 8)
    )
)
    // Creates the recipe advancement. While not mandated by the consuming background systems,
    // the recipe builder will crash if you omit this. The first parameter is the advancement name,
    // and the second one is the condition. Normally, you want to use the has() shortcut for the condition.
    // Multiple advancement requirements can be added by calling #unlockedBy multiple times.
    .unlockedBy("has_lingering_potion", this.has(Items.LINGERING_POTION))
    // Stores the recipe in the passed RecipeOutput, to be written to disk.
    // If you want to add conditions to the recipe, those can be set on the output.
    .save(this.output, "tipped_arrow");
```

Additionally, you can call `#group` to set the recipe book group.

### Transmute Crafting

Transmute recipes are a special type of single item crafting recipes where the input stack's data components are completely copied to the resulting stack. Transmutations usually occur between two different items where one is the dyed version of another. For example:

```json5
{
    "type": "minecraft:crafting_transmute",
    "category": "misc",
    "group": "shulker_box_dye",
    "input": "#minecraft:shulker_boxes",
    "material": "minecraft:blue_dye",
    "material_count": 1,
    "add_material_count_to_result": false,
    "result": {
        "id": "minecraft:blue_shulker_box"
    }
}
```

Like before, let's digest this line for line:

- `type`: This is the id of the recipe serializer, `minecraft:crafting_transmute`.
- `category`: This optional field defines the category in the crafting book.
- `group`: This optional string property creates a group in the recipe book. Recipes in the same group will be displayed as one in the recipe book, which typically makes sense for transmuted recipes.
- `input`: The [ingredient] to transmute.
- `material`: The [ingredient] that transforms the stack into its result.
- `material_count`: The number of `material`s required to transform the stack into its result.
- `result`: The result of the recipe. This is [an item stack template's JSON representation][itemjson].
- `add_material_count_to_result`: Whether the number of materials used should be added to the number of items returned as a result. This is commonly used for cloning item data, like maps.

And then, let's have a look at how you'd generate this recipe in `RecipeProvider#buildRecipes`:

```java
// We use a builder pattern, therefore no variable is created. Create a new builder by calling
// TransmuteRecipeBuilder#transmute with the recipe category (found in the RecipeCategory enum),
// the ingredient input, the ingredient material, and the resulting item.
TransmuteRecipeBuilder.transmute(RecipeCategory.MISC, this.tag(ItemTags.SHULKER_BOXES),
    Ingredient.of(DyeItem.byColor(DyeColor.BLUE)), ShulkerBoxBlock.getBlockByColor(DyeColor.BLUE).asItem())
        // Sets the group of the recipe to display in the recipe book.
        .group("shulker_box_dye")
        // Sets the number of materials required to transmute the stack.
        .setMaterialCount(TransmuteRecipe.DEFAULT_MATERIAL_COUNT)
        // Creates the recipe advancement. While not mandated by the consuming background systems,
        // the recipe builder will crash if you omit this. The first parameter is the advancement name,
        // and the second one is the condition. Normally, you want to use the has() shortcut for the condition.
        // Multiple advancement requirements can be added by calling #unlockedBy multiple times.
        .unlockedBy("has_shulker_box", this.has(ItemTags.SHULKER_BOXES))
        // Stores the recipe in the passed RecipeOutput, to be written to disk.
        // If you want to add conditions to the recipe, those can be set on the output.
        .save(this.output);
```

Additionally, `#addMaterialCountToOutput` can be used to add the number of materials used to the result count.

### Dying Items

Dye recipes are a special type of single item crafting recipes where the dye stacks' `DataComponents#DYE` are combined into a single `DataComponents#DYED_COLOR` and applied to the target stack to construct the result stack. All other components from the target stack are copied to the result stack, similar to [transmute crafting][transmute]. For example:

```json5
{
    "type": "minecraft:crafting_dye",
    "category": "misc",
    "dye": "#minecraft:dyes",
    "group": "dyed_armor",
    "result": {
        "id": "minecraft:leather_boots"
    },
    "target": "minecraft:leather_boots"
}
```

Like before, let's digest this line for line:

- `type`: This is the id of the recipe serializer, `minecraft:crafting_transmute`.
- `category`: This optional field defines the category in the crafting book.
- `group`: This optional string property creates a group in the recipe book. Recipes in the same group will be displayed as one in the recipe book, which typically makes sense for dying recipes.
- `target`: The [ingredient] to apply the dyes to.
- `dye`: The [ingredient] dyes that are used to dye the stack. They must all have the `DataComponents#DYE` [data component][datacomponent].
- `result`: The result of the recipe. This is [an item stack template's JSON representation][itemjson].

And then, let's have a look at how you'd generate this recipe in `RecipeProvider#buildRecipes`:

```java
// We use a builder pattern, therefore no variable is created. Create a new builder by calling
// CustomCraftingRecipeBuilder#customCrafting with the recipe category (found in the RecipeCategory enum)
// and a factory function that takes in the `Recipe.CommonInfo` and `CraftingRecipe.CraftingBookInfo`
// to return the `Recipe` instance.
CustomCraftingRecipeBuilder.customCrafting(
    RecipeCategory.MISC,
    // The function used to construct the recipe instance.
    (commonInfo, bookInfo) -> new DyeRecipe(
        commonInfo, bookInfo,
        // The target to apply the dyes to.
        Ingredient.of(Items.LEATHER_BOOTS),
        // The dyes that can be applied to the target.
        this.tag(ItemTags.DYES),
        // The resulting template with the applied dye color.
        new ItemStackTemplate(Items.LEATHER_BOOTS)
    )
)
    // Sets the group of the recipe to display in the recipe book.
    .group("dyed_armor")
    // Creates the recipe advancement. While not mandated by the consuming background systems,
    // the recipe builder will crash if you omit this. The first parameter is the advancement name,
    // and the second one is the condition. Normally, you want to use the has() shortcut for the condition.
    // Multiple advancement requirements can be added by calling #unlockedBy multiple times.
    .unlockedBy("has_leather_boots", this.has(Items.LEATHER_BOOTS))
    // Stores the recipe in the passed RecipeOutput, to be written to disk.
    // If you want to add conditions to the recipe, those can be set on the output.
    .save(this.output, "dyed_leather_boots");
```

### Special Recipes

There are many other crafting recipes that are specifically made for a single purpose (e.g. cloning books, creating fireworks) rather than be applied in a generic manner. Most of the time, this is to set data components on the output by calculating their values from the input stacks. These recipes are still configurable, usually taking in the input ingredients and the template result. For example:

```json5
{
    "type": "minecraft:crafting_special_firework_rocket",
    "fuel": "minecraft:gunpowder",
    "result": {
        "count": 3,
        "id": "minecraft:firework_rocket"
    },
    "shell": "minecraft:paper",
    "star": "minecraft:firework_star"
}
```

This recipe, which is for creating a firework rocket, specifies the fuel, shell, and star ingredients used to create the result. However, it assumes that the star ingredient has the `DataComponents#FIREWORK_EXPLOSION` component, as otherwise no explosion is added.

Minecraft prefixes most special crafting recipes with `crafting_special_`, however this practice is not necessary to follow.

Generating this recipe looks as follows in `RecipeProvider#buildRecipes`:

```java
// The parameter of #special is a Supplier<Recipe<?>>.
SpecialRecipeBuilder.special(
    () -> new FireworkRocketRecipe(
        Ingredient.of(Items.PAPER),
        Ingredient.of(Items.GUNPOWDER),
        Ingredient.of(Items.FIREWORK_STAR),
        new ItemStackTemplate(Items.FIREWORK_ROCKET, 3)
    )
)
    // This overload of #save allows us to specify a name. It can also be used on other recipe builders.
    .save(this.output, "firework_rocket");
```

Vanilla provides the following special crafting serializers (mods may add more):

- `minecraft:crafting_special_bannerduplicate`: For duplicating banners.
- `minecraft:crafting_special_bookcloning`: For copying written books. This increases the resulting book's generation property by one.
- `minecraft:crafting_special_firework_rocket`: For crafting firework rockets.
- `minecraft:crafting_special_firework_star`: For crafting firework stars.
- `minecraft:crafting_special_firework_star_fade`: For applying a fade to a firework star.
- `minecraft:crafting_special_mapextending`: For extending filled maps.
- `minecraft:crafting_special_repairitem`: For repairing two broken items into one.
- `minecraft:crafting_special_shielddecoration`: For applying a banner to a shield.
- `minecraft:crafting_decorated_pot`: For crafting decorated pots from sherds.

## Furnace-like Recipes

The second most important group of recipes are the ones made through smelting or a similar process. All recipes made in furnaces (type `minecraft:smelting`), smokers (`minecraft:smoking`), blast furnaces (`minecraft:blasting`) and campfires (`minecraft:campfire_cooking`) use the same format:

```json5
{
    "type": "minecraft:smelting",
    "category": "food",
    "cookingtime": 200,
    "experience": 0.1,
    "ingredient": {
        "item": "minecraft:kelp"
    },
    "result": {
        "id": "minecraft:dried_kelp"
    }
}
```

Let's digest this line by line:

- `type`: This is the id of the recipe serializer, `minecraft:smelting`. This may be different depending on what kind of furnace-like recipe you're making.
- `category`: This optional field defines the category in the cooking recipe book.
- `cookingtime`: This field determines how long the recipes needs to be processed, in ticks. All vanilla furnace recipes use 200, smokers and blast furnaces use 100, and campfires use 600. However, this can be any value you want.
- `experience`: Determines the amount of experience rewarded when making this recipe. This field is optional, and no experience will be awarded if it is omitted.
- `ingredient`: The input [ingredient] of the recipe.
- `result`: The result of the recipe. This is [an item stack template's JSON representation][itemjson].

Datagen for these recipes looks like this in `RecipeProvider#buildRecipes`:

```java
// Use #smoking for smoking recipes, #blasting for blasting recipes, and #campfireCooking for campfire recipes.
// All of these builders work the same otherwise.
SimpleCookingRecipeBuilder.smelting(
        // Our input ingredient.
        Ingredient.of(Items.KELP),
        // Our recipe category.
        RecipeCategory.FOOD,
        CookingBookCategory.FOOD
        // Our result item. May also be an ItemStackTemplate.
        Items.DRIED_KELP,
        // Our experience reward
        0.1f,
        // Our cooking time.
        200
)
        // The recipe advancement, like with the crafting recipes above.
        .unlockedBy("has_kelp", this.has(Blocks.KELP))
        // This overload of #save allows us to specify a name.
        .save(this.output, "dried_kelp_smelting");
```

:::info
The recipe type for these recipes is the same as their recipe serializer, i.e. furnaces use `minecraft:smelting`, smokers use `minecraft:smoking`, and so on.
:::

## Stonecutting

Stonecutter recipes use the `minecraft:stonecutting` recipe type. They are about as simple as it gets, with only a type, an input and an output:

```json5
{
    "type": "minecraft:stonecutting",
    "ingredient": "minecraft:andesite",
    "result": {
        "count": 2,
        "id": "minecraft:andesite_slab"
    }
}
```

The `type` defines the recipe serializer (`minecraft:stonecutting`). The ingredient is an [ingredient], and the result is a basic [item stack template JSON][itemjson]. Like crafting recipes, they can also optionally specify a `group` for grouping in the recipe book.

Datagen is also simple in `RecipeProvider#buildRecipes`:

```java
SingleItemRecipeBuilder.stonecutting(Ingredient.of(Items.ANDESITE), RecipeCategory.BUILDING_BLOCKS, Items.ANDESITE_SLAB, 2)
        .unlockedBy("has_andesite", this.has(Items.ANDESITE))
        .save(this.output, "andesite_slab_from_andesite_stonecutting");
```

Note that the single item recipe builder does not support actual ItemStack results, and as such, no results with data components. The recipe codec, however, does support them, so a custom builder would need to be implemented if this functionality was desired.

## Smithing

The smithing table supports two different recipe serializers. One is for transforming inputs into outputs, copying over the components of the input (such as enchantments), and the other is for applying components to the input. Both use the `minecraft:smithing` recipe type, and require three inputs, named the base, the template, and the addition item.

### Transform Smithing

This recipe serializer is for transforming two input items into one, preserving the data components of the first input. Vanilla uses this mainly for netherite equipment, however any items can be used here:

```json5
{
    "type": "minecraft:smithing_transform",
    "addition": "#minecraft:netherite_tool_materials",
    "base": "minecraft:diamond_axe",
    "result": {
        "id": "minecraft:netherite_axe"
    },
    "template": "minecraft:netherite_upgrade_smithing_template"
}
```

Let's break this down line by line:

- `type`: This is the id of the recipe serializer, `minecraft:smithing_transform`.
- `base`: The base [ingredient] of the recipe. Usually, this is some piece of equipment.
- `template`: The template [ingredient] of the recipe. Usually, this is a smithing template.
- `addition`: The addition [ingredient] of the recipe. Usually, this is some sort of material, for example a netherite ingot.
- `result`: The result of the recipe. This is [an item stack template's JSON representation][itemjson].

During datagen, call on `SmithingTransformRecipeBuilder#smithing` to add your recipe in `RecipeProvider#buildRecipes`:

```java
SmithingTransformRecipeBuilder.smithing(
        // The template ingredient.
        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        // The base ingredient.
        Ingredient.of(Items.DIAMOND_AXE),
        // The addition ingredient.
        this.tag(ItemTags.NETHERITE_TOOL_MATERIALS),
        // The recipe book category.
        RecipeCategory.TOOLS,
        // The result item. Note that while the recipe codec accepts an item stack template here, the builder does not.
        // If you need an item stack template output, you need to use your own builder.
        Items.NETHERITE_AXE
)
        // The recipe advancement, like with the other recipes above.
        .unlocks("has_netherite_ingot", this.has(ItemTags.NETHERITE_TOOL_MATERIALS))
        // This overload of #save allows us to specify a name.
        .save(this.output, "netherite_axe_smithing");
```

### Trim Smithing

Trim smithing is the process of applying armor trims to armor:

```json5
{
    "type": "minecraft:smithing_trim",
    "addition": "#minecraft:trim_materials",
    "base": "#minecraft:trimmable_armor",
    "pattern": "minecraft:spire",
    "template": "minecraft:bolt_armor_trim_smithing_template"
}
```

Again, let's break this down into its bits:

- `type`: This is the id of the recipe serializer, `minecraft:smithing_trim`.
- `base`: The base [ingredient] of the recipe. All vanilla use cases use the `minecraft:trimmable_armor` tag here.
- `template`: The template [ingredient] of the recipe. All vanilla use cases use a smithing trim template here.
- `addition`: The addition [ingredient] of the recipe. All vanilla use cases use the `minecraft:trim_materials` tag here.
- `pattern`: The trim pattern applied to the base ingredient.

This recipe serializer is notably missing a result field. This is because it uses the base input and "applies" the template and addition items on it, i.e., it sets the base's components based on the other inputs and uses the result of that operation as the recipe's result.

During datagen, call on `SmithingTrimRecipeBuilder#smithingTrim` to add your recipe in `RecipeProvider#buildRecipes`:

```java
SmithingTrimRecipeBuilder.smithingTrim(
        // The template ingredient.
        Ingredient.of(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE),
        // The base ingredient.
        this.tag(ItemTags.TRIMMABLE_ARMOR),
        // The addition ingredient.
        this.tag(ItemTags.TRIM_MATERIALS),
        // The trim pattern to apply to the base.
        this.registries.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.SPIRE),
        // The recipe book category.
        RecipeCategory.MISC
)
        // The recipe advancement, like with the other recipes above.
        .unlocks("has_smithing_trim_template", this.has(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE))
        // This overload of #save allows us to specify a name. Yes, this name is copied from vanilla.
        .save(this.output, "bolt_armor_trim_smithing_template_smithing_trim");
```

[datacomponent]: ../../../items/datacomponents.md
[ingredient]: ingredients.md
[itemjson]: ../../../items/index.md#json-representation
[transmute]: #transmute-crafting
