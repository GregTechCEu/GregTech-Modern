---
title: "Tag Prefix Recipe Generation"
---

# Generating recipes based on TagPrefixes

Most recipes that turn some form of a material into another form, like iron ingots to iron plates, or tin bolts into tin screws, are done through tag prefix based recipe generation.

Gregtech will iterate through all materials and all tag prefixes possible for that material to generate recipes. You can do the same in your addon by mirroring the following:

```java title="TagPrefixRecipes.java"

public static void recipeAddition(Consumer<FinishedRecipe> consumer) {

    for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasFlag(MaterialFlags.NO_UNIFICATION)) {
                continue;
            }
        MaterialRecipeHandler.run(provider, material)
    }

} 

```

```java title="MaterialRecipeHandler.java"

public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        processFrame(provider, material);
}

private static void processFrame(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(frameGt) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        } // (1)

        if (material.hasFlag(GENERATE_FRAME)) {
            boolean isWoodenFrame = material.hasProperty(PropertyKey.WOOD);
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("frame_%s", material.getName()),
                    ChemicalHelper.get(frameGt, material, 2),
                    "SSS", isWoodenFrame ? "SsS" : "SwS", "SSS",
                    'S', new UnificationEntry(rod, material));

            ASSEMBLER_RECIPES.recipeBuilder("assemble_" + material.getName() + "_frame")
                    .inputItems(rod, material, 4)
                    .circuitMeta(4)
                    .outputItems(frameGt, material)
                    .EUt(VA[ULV]).duration(64)
                    .save(provider);
        }
    }

```
1. Checks that the material has a valid item with that specific tag prefix and can generate the recipe.