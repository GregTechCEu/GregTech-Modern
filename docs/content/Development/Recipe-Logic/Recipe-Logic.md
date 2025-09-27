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
                if (runDelay > 0) {
                    runDelay--;
                } else {
                    handleRecipeWorking();
                }
            }
            if (progress >= duration) {
                onRecipeFinish();
            }
        } else if (lastRecipe != null) {
            findAndHandleRecipe();
        } // Code for rechecking
    }
    // Logic for unsubscribing if needed
}
```

We will dissect this method in [Recipe Searching](./Recipe-Searching.md) and [Recipe Execution](./Recipe-Execution.md).