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
    "pattern": [
        "XXX",
        " # ",
        " # "
    ],
    "key": {
        "#": {
            "item": "minecraft:stick"
        },
        "X": {
            "item": "minecraft:iron_ingot"
        }
    },
    "result": {
        "count": 1,
        "id": "minecraft:iron_pickaxe"
    }
}
```

Let's digest this line for line:

- `type`: This is the id of the shaped recipe serializer, `minecraft:crafting_shaped`.
- `category`: This optional field defines the category in the crafting book.
- `key` and `pattern`: Together, these define how the items must be put into the crafting grid.
    - The pattern defines up to three lines of up to three-wide strings that define the shape. All lines must be the same length, i.e. the pattern must form a rectangular shape. Spaces can be used to denote slots that should stay empty.
    - The key associates the characters used in the pattern with [ingredients][ingredient]. In the above example, all `X`s in the pattern must be iron ingots, and all `#`s must be sticks.
- `result`: The result of the recipe. This is [an item stack's JSON representation][itemjson].
- Not shown in the example is the `group` key. This optional string property creates a group in the recipe book. Recipes in the same group will be displayed as one in the recipe book.

And then, let's have a look at how you'd generate this recipe:

```java
// We use a builder pattern, therefore no variable is created. Create a new builder by calling
// ShapedRecipeBuilder#shaped with the recipe category (found in the RecipeCategory enum)
// and a result item, a result item and count, or a result item stack.
ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
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
        .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
        // Stores the recipe in the passed RecipeOutput, to be written to disk.
        // If you want to add conditions to the recipe, those can be set on the output.
        .save(output);
```

Additionally, you can call `#group` to set the recipe book group.

### Shapeless Crafting

Unlike shaped crafting recipes, shapeless crafting recipes do not care about the order the ingredients are passed in. As such, there is no pattern and key, instead there is just a list of ingredients:

```json5
{
    "type": "minecraft:crafting_shapeless",
    "category": "misc",
    "ingredients": [
        {
            "item": "minecraft:brown_mushroom"
        },
        {
            "item": "minecraft:red_mushroom"
        },
        {
            "item": "minecraft:bowl"
        }
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
- `result`: The result of the recipe. This is [an item stack's JSON representation][itemjson].
- Not shown in the example is the `group` key. This optional string property creates a group in the recipe book. Recipes in the same group will be displayed as one in the recipe book.

And then, let's have a look at how you'd generate this recipe:

```java
// We use a builder pattern, therefore no variable is created. Create a new builder by calling
// ShapelessRecipeBuilder#shapeless with the recipe category (found in the RecipeCategory enum)
// and a result item, a result item and count, or a result item stack.
ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.MUSHROOM_STEW)
        // Add the recipe ingredients. This can either accept Ingredients, TagKey<Item>s or ItemLikes.
        // Overloads also exist that additionally accept a count, adding the same ingredient multiple times.
        .requires(Blocks.BROWN_MUSHROOM)
        .requires(Blocks.RED_MUSHROOM)
        .requires(Items.BOWL)
        // Creates the recipe advancement. While not mandated by the consuming background systems,
        // the recipe builder will crash if you omit this. The first parameter is the advancement name,
        // and the second one is the condition. Normally, you want to use the has() shortcut for the condition.
        // Multiple advancement requirements can be added by calling #unlockedBy multiple times.
        .unlockedBy("has_mushroom_stew", has(Items.MUSHROOM_STEW))
        .unlockedBy("has_bowl", has(Items.BOWL))
        .unlockedBy("has_brown_mushroom", has(Blocks.BROWN_MUSHROOM))
        .unlockedBy("has_red_mushroom", has(Blocks.RED_MUSHROOM))
        // Stores the recipe in the passed RecipeOutput, to be written to disk.
        // If you want to add conditions to the recipe, those can be set on the output.
        .save(output);
```

Additionally, you can call `#group` to set the recipe book group.

:::info
One-item recipes (e.g. storage blocks unpacking) should be shapeless recipes to follow vanilla standards.
:::

### Special Crafting

In some cases, outputs must be created dynamically from inputs. Most of the time, this is to set data components on the output by copying or calculating their values from the input stacks. These recipes usually only specify the type and hardcode everything else. For example:

```java
{
    "type": "minecraft:crafting_special_armordye"
}
```

This recipe, which is for leather armor dyeing, just specifies the type and hardcodes everything else - most notably the color calculation, which would be hard to express in JSON. Minecraft prefixes all special crafting recipes with `crafting_special_`, however this practice is not necessary to follow.

Generating this recipe looks as follows:

```java
// The parameter of #special is a Function<CraftingBookCategory, Recipe<?>>.
// All vanilla special recipes use a constructor with one CraftingBookCategory parameter for this.
SpecialRecipeBuilder.special(ArmorDyeRecipe::new)
        // This overload of #save allows us to specify a name. It can also be used on shaped or shapeless builders.
        .save(output, "armor_dye");
```

Vanilla provides the following special crafting serializers (mods may add more):

- `minecraft:crafting_special_armordye`: For dyeing leather armor and other dyeable items.
- `minecraft:crafting_special_bannerduplicate`: For duplicating banners.
- `minecraft:crafting_special_bookcloning`: For copying written books. This increases the resulting book's generation property by one.
- `minecraft:crafting_special_firework_rocket`: For crafting firework rockets.
- `minecraft:crafting_special_firework_star`: For crafting firework stars.
- `minecraft:crafting_special_firework_star_fade`: For applying a fade to a firework star.
- `minecraft:crafting_special_mapcloning`: For copying filled maps. Also works for treasure maps.
- `minecraft:crafting_special_mapextending`: For extending filled maps.
- `minecraft:crafting_special_repairitem`: For repairing two broken items into one.
- `minecraft:crafting_special_shielddecoration`: For applying a banner to a shield.
- `minecraft:crafting_special_shulkerboxcoloring`: For coloring a shulker box while preserving its contents.
- `minecraft:crafting_special_suspiciousstew`: For crafting suspicious stews depending on the input flower.
- `minecraft:crafting_special_tippedarrow`: For crafting tipped arrows depending on the input potion.
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
- `category`: This optional field defines the category in the crafting book.
- `cookingtime`: This field determines how long the recipes needs to be processed, in ticks. All vanilla furnace recipes use 200, smokers and blast furnaces use 100, and campfires use 600. However, this can be any value you want.
- `experience`: Determines the amount of experience rewarded when making this recipe. This field is optional, and no experience will be awarded if it is omitted.
- `ingredient`: The input [ingredient] of the recipe.
- `result`: The result of the recipe. This is [an item stack's JSON representation][itemjson].

Datagen for these recipes looks like this:

```java
// Use #smoking for smoking recipes, #blasting for blasting recipes, and #campfireCooking for campfire recipes.
// All of these builders work the same otherwise.
SimpleCookingRecipeBuilder.smelting(
        // Our input ingredient.
        Ingredient.of(Items.KELP),
        // Our recipe category.
        RecipeCategory.FOOD,
        // Our result item. May also be an ItemStack.
        Items.DRIED_KELP,
        // Our experience reward
        0.1f,
        // Our cooking time.
        200
)
        // The recipe advancement, like with the crafting recipes above.
        .unlockedBy("has_kelp", has(Blocks.KELP))
        // This overload of #save allows us to specify a name.
        .save(p_301191_, "dried_kelp_smelting");
```

:::info
The recipe type for these recipes is the same as their recipe serializer, i.e. furnaces use `minecraft:smelting`, smokers use `minecraft:smoking`, and so on.
:::

## Stonecutting

Stonecutter recipes use the `minecraft:stonecutting` recipe type. They are about as simple as it gets, with only a type, an input and an output:

```json5
{
    "type": "minecraft:stonecutting",
    "ingredient": {
        "item": "minecraft:andesite"
    },
    "result": {
        "count": 2,
        "id": "minecraft:andesite_slab"
    }
}
```

The `type` defines the recipe serializer (`minecraft:stonecutting`). The ingredient is an [ingredient], and the result is a basic [item stack JSON][itemjson]. Like crafting recipes, they can also optionally specify a `group` for grouping in the recipe book.

Datagen is also simple:

```java
SingleItemRecipeBuilder.stonecutting(Ingredient.of(Items.ANDESITE), RecipeCategory.BUILDING_BLOCKS, Items.ANDESITE_SLAB, 2)
        .unlockedBy("has_andesite", has(Items.ANDESITE))
        .save(output, "andesite_slab_from_andesite_stonecutting");
```

Note that the single item recipe builder does not support actual ItemStack results, and as such, no results with data components. The recipe codec, however, does support them, so a custom builder would need to be implemented if this functionality was desired.

## Smithing

The smithing table supports two different recipe serializers. One is for transforming inputs into outputs, copying over the components of the input (such as enchantments), and the other is for applying components to the input. Both use the `minecraft:smithing` recipe type, and require three inputs, named the base, the template, and the addition item.

### Transform Smithing

This recipe serializer is for transforming two input items into one, preserving the data components of the first input. Vanilla uses this mainly for netherite equipment, however any items can be used here:

```json5
{
    "type": "minecraft:smithing_transform",
    "base": {
        "item": "minecraft:diamond_axe"
    },
    "template": {
        "item": "minecraft:netherite_upgrade_smithing_template"
    },
    "addition": {
        "item": "minecraft:netherite_ingot"
    },
    "result": {
        "count": 1,
        "id": "minecraft:netherite_axe"
    }
}
```

Let's break this down line by line:

- `type`: This is the id of the recipe serializer, `minecraft:smithing_transform`.
- `base`: The base [ingredient] of the recipe. Usually, this is some piece of equipment.
- `template`: The template [ingredient] of the recipe. Usually, this is a smithing template.
- `addition`: The addition [ingredient] of the recipe. Usually, this is some sort of material, for example a netherite ingot.
- `result`: The result of the recipe. This is [an item stack's JSON representation][itemjson].

During datagen, call on `SmithingTransformRecipeBuilder#smithing` to add your recipe:

```java
SmithingTransformRecipeBuilder.smithing(
        // The template ingredient.
        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
        // The base ingredient.
        Ingredient.of(Items.DIAMOND_AXE),
        // The addition ingredient.
        Ingredient.of(Items.NETHERITE_INGOT),
        // The recipe book category.
        RecipeCategory.TOOLS,
        // The result item. Note that while the recipe codec accepts an item stack here, the builder does not.
        // If you need an item stack output, you need to use your own builder.
        Items.NETHERITE_AXE
)
        // The recipe advancement, like with the other recipes above.
        .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
        // This overload of #save allows us to specify a name.
        .save(output, "netherite_axe_smithing");
```

### Trim Smithing

Trim smithing is the process of applying armor trims to armor:

```json5
{
    "type": "minecraft:smithing_trim",
    "addition": {
        "tag": "minecraft:trim_materials"
    },
    "base": {
        "tag": "minecraft:trimmable_armor"
    },
    "template": {
        "item": "minecraft:bolt_armor_trim_smithing_template"
    }
}
```

Again, let's break this down into its bits:

- `type`: This is the id of the recipe serializer, `minecraft:smithing_trim`.
- `base`: The base [ingredient] of the recipe. All vanilla use cases use the `minecraft:trimmable_armor` tag here.
- `template`: The template [ingredient] of the recipe. All vanilla use cases use a smithing trim template here.
- `addition`: The addition [ingredient] of the recipe. All vanilla use cases use the `minecraft:trim_materials` tag here.

This recipe serializer is notably missing a result field. This is because it uses the base input and "applies" the template and addition items on it, i.e., it sets the base's components based on the other inputs and uses the result of that operation as the recipe's result.

During datagen, call on `SmithingTrimRecipeBuilder#smithingTrim` to add your recipe:

```java
SmithingTrimRecipeBuilder.smithingTrim(
        // The base ingredient.
        Ingredient.of(ItemTags.TRIMMABLE_ARMOR),
        // The template ingredient.
        Ingredient.of(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE),
        // The addition ingredient.
        Ingredient.of(ItemTags.TRIM_MATERIALS),
        // The recipe book category.
        RecipeCategory.MISC
)
        // The recipe advancement, like with the other recipes above.
        .unlocks("has_smithing_trim_template", has(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE))
        // This overload of #save allows us to specify a name. Yes, this name is copied from vanilla.
        .save(output, "bolt_armor_trim_smithing_template_smithing_trim");
```

[ingredient]: ingredients.md
[itemjson]: ../../../items/index.md#json-representation
