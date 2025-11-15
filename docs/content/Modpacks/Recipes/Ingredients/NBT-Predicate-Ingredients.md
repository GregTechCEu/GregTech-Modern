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

- `.eqString(key, value)`  
- `.eqInt(key, value)`  
- `.eqFloat(key, value)`  
- `.eqByte(key, value)`  
- `.eqDouble(key, value)`  
- `.eqTag(key, value)`  

All of these also have an `.neq[...](key, value)` function.  
In Java, these are also available, as well as simpler `.[n]eq(key, [type] value)` overloads.  


=== "JavaScript"
    ```js title="gt_recipes.js"
    
    ServerEvents.recipes(event => {
        event.recipes.gtceu.assembler('test_nbt')
            .inputItemNbtPredicate('minecraft:dirt', NBTPredicates.eqString("charge", "23"))
            .itemOutputs('minecraft:stick')
            .duration(100)
            .EUt(30)
    })
    
    ```

=== "Java"
    ```java title="GTRecipes.java"

    public static void init(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("test_nbt")
                .inputItemNbtPredicate(new ItemStack(Items.dirt, 1), NBTPredicates.eq("charge", "23"))
                .outputItems(new ItemStack(Items.STICK))
                .duration(100)
                .EUt(30)
                .save(provider);
    }

    ```

### Number Comparison
The following number comparison operators exist:

- `.lte(key, number)`: Less Than or Equal to  
- `.lt(key, number)`: Less Than  
- `.gte(key, number)`: Greater Than or Equal to  
- `.gt(key, number)`: Greater Than  

=== "JavaScript"
    ```js title="gt_recipes.js"

    ServerEvents.recipes(event => {
        event.recipes.gtceu.assembler('test_nbt')
            .inputItemNbtPredicate('minecraft:dirt', NBTPredicates.lt("charge", 23))
            .itemOutputs('minecraft:stick')
            .duration(100)
            .EUt(30)
    })
    
    ```

=== "Java"
    ```java title="GTRecipes.java"

    public static void init(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("test_nbt")
                .inputItemNbtPredicate(new ItemStack(Items.dirt, 1), NBTPredicates.lt("charge", 23))
                .outputItems(new ItemStack(Items.STICK))
                .duration(100)
                .EUt(30)
                .save(provider);
    }

    ```


### Any/All
The following list operators exist: 

- `.all(NBTPredicate...)`
- `.any(NBTPredicate...)`

=== "JavaScript"
    ```js title="gt_recipes.js"

    ServerEvents.recipes(event => {
        event.recipes.gtceu.assembler('test_nbt')
            .inputItemNbtPredicate('minecraft:dirt', 
                NBTPredicates.all([
                    NBTPredicates.lt("charge", 23),
                    NBTPredicates.eqString("color", "blue")
                ]))
            .itemOutputs('minecraft:stick')
            .duration(100)
            .EUt(30)
    })
    
    ```

=== "Java"
    ```java title="GTRecipes.java"

    public static void init(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("test_nbt")
                .inputItemNbtPredicate(new ItemStack(Items.dirt, 1),
                    NBTPredicates.all([
                        NBTPredicates.lt("charge", 23),
                        NBTPredicates.eqString("color", "blue")
                    ]))
                .outputItems(new ItemStack(Items.STICK))
                .duration(100)
                .EUt(30)
                .save(provider);
    }

    ```


### Not
The negation operators exists: 

- `.not(NBTPredicate)`


=== "JavaScript"
    ```js title="gt_recipes.js"

    ServerEvents.recipes(event => {
        event.recipes.gtceu.assembler('test_nbt')
            .inputItemNbtPredicate(new ItemStack(Items.dirt, 1),
                NBTPredicates.not(
                    NBTPredicates.all([
                        NBTPredicates.lt("charge", 23),
                        NBTPredicates.eqString("color", "blue")
                    ])
                )
            )
            .itemOutputs('minecraft:stick')
            .duration(100)
            .EUt(30)
    })
    
    ```

=== "Java"
    ```java title="GTRecipes.java"

    public static void init(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("test_nbt")
                .inputItemNbtPredicate(new ItemStack(Items.dirt, 1),
                    NBTPredicates.not(
                        NBTPredicates.all([
                            NBTPredicates.lt("charge", 23),
                            NBTPredicates.eqString("color", "blue")
                        ])
                    )
                )
                .outputItems(new ItemStack(Items.STICK))
                .duration(100)
                .EUt(30)
                .save(provider);
    }

    ```


### Key Navigation
You can use `.` to navigate nested tags, and `[i]` to index into lists. so
```
{ "machine": 
    { "states" : 
       [ 
          {"color": "green"},
          {"color": "red"},
       ]
    }
}
```
would match
`.eq("machine.states[0].color", "green")`
