---
title: "NBT Predicate Ingredients"
---

For some use-cases, using Partial or Strict NBT Ingredients does not give enough control. For this, we have NBT Predicate Ingredients.
This system allows you to query NBT contents during recipe matching to validate more advanced queries on ItemStacks.

!!! note
    To test your items in-game, you can use the give and ftblibrary commands, e.g. `/give @p dirt{"attributes": {"strength":16, "sound":"crunch.wav" } }` to give yourself an item with custom NBT or `/ftblibrary nbtedit hand` for a graphical editor
## Usage
### Equals
For JavaScript, custom overloads were made:

- `.eq_string(key, value)`  
- `.eq_int(key, value)`  
- `.eq_float(key, value)`  
- `.eq_byte(key, value)`  
- `.eq_double(key, value)`  
- `.eq_tag(key, value)`  

All of these also have an `.neq_[...](key, value)` function.  
In Java, these are also available, as well as simpler `.[n]eq(key, [type] value)` overloads.  


=== "JavaScript"
    ```js title="gt_recipes.js"
    // In order to use the NBT Predicates, we have to load their holder class
    const $NBT = Java.loadClass("com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates");
    
    ServerEvents.recipes(event => {
        event.recipes.gtceu.assembler('test_nbt')
            .NBTPredicateInput('minecraft:dirt', $NBT.eq_string("charge", "23"))
            .itemOutputs('minecraft:stick')
            .duration(100)
            .EUt(30)
    })
    
    ```

=== "Java"
    ```java title="GTRecipes.java"

    public static void init(Consumer<FinishedRecipe> provider) {
        MIXER_RECIPES.recipeBuilder("test_nbt")
                .NBTPredicateInput(new ItemStack(Items.dirt, 1), NBTPredicates.eq("charge", 23))
                .outputItems(new ItemStack(Items.STICK))
                .duration(100)
                .EUt(30)
                .save(provider);
    }

    ```
