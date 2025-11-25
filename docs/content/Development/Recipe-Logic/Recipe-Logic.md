---
title: "Recipe Logic"
---

Any `WorkableMachine` has a `RecipeLogic` as a trait. These machines have a `TickableSubscription` that calls `recipeLogic.serverTick`.
A (slightly simplified) version of `recipeLogic.serverTick` can be seen below:
```java title="RecipeLogic.java"
public void serverTick() {
    if (!isSuspend()) {
        if (!isIdle() && lastRecipe != null) {
            if (progress < duration) {
                handleRecipeWorking();
            }
            if (progress >= duration) {
                onRecipeFinish();
            }
        } else {
            findAndHandleRecipe();
        } // Code for re-doing previous recipe
    }
    // Logic for unsubscribing if needed
}
```

We will dissect this method in [Recipe Searching](./Recipe-Searching.md) and [Recipe Execution](./Recipe-Execution.md).