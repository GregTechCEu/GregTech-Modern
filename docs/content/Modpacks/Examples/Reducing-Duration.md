---
title: "Machines Duration Reduction"
---


# Reducing Duration Of All Machine Recipes

## Reducing Script

```js title="Reduce_Duration.js"
ServerEvents.recipes(event => {
    event.forEachRecipe({ mod: 'gtceu' }, recipe => { // (1)
        try { // (2)
            var newDuration = recipe.get("duration") // (3)
            recipe.set("duration", newDuration/10) // (4)
        } catch (err) { // (5)
            console.log(recipe.id + " has no duration field, skipped.")
        }
    })
})
```

1. A function to run code for every recipe in gregtech.
2. Uses a try to avoid using recipes that don't have a duration, like crafting.
3. Gets a variable of the duration current duration to change.
4. Edits the recipes duration to a tenth of the old recipes duration.
5. Catches the error if the recipe has no duration and logs it.