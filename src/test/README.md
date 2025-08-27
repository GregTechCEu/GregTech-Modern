# Guide to Writing GameTests in GTCEu

---

## Table of Contents

1. [Defining a Test Class](#defining-a-test-class)
2. [Writing Test Methods](#writing-test-methods)
3. [Structure Templates & Schematics](#structure-templates--schematics)
4. [Using GameTestHelper](#using-gametesthelper)
5. [Example: RecipeLogicTest](#example-recipelogictest)
6. [Running & Debugging Tests](#running--debugging-tests)
7. [Best Practices](#best-practices)

---

## Defining a Test Class

- Put it in the same class path as the main element you're trying to test, e.g. `test/java/com/gregtechceu/gtceu/api/machine/trait/RecipeLogicTest.java`
- Annotate with `@GameTestHolder(GTCEu.MOD_ID)` to register the class for testing.
- Use `@PrefixGameTestTemplate(false)` to ignore the automatic template prefix
    - Without annotation: looks for `data/gtceu/structures/MyTests.basicBlockTest.singleblock.nbt`
    - With annotation: looks for `data/gtceu/structures/singleblock.nbt`

```java
@GameTestHolder(GTCEu.MOD_ID)
@PrefixGameTestTemplate(false)
public class MyTests {
  // Test methods go here
}
```

---

## Writing Test Methods

- Annotate each test method with `@GameTest`.
- Parameters:
    - `template`: name of the structure or test chamber (in `data/gtceu/structures/`).
    - `batch` (default 'defaultBatch'): group name for running subsets, see '@BeforeBatch'
    - `timeoutTicks` (default 100): fail if not succeeded within ticks.
    - `required`: (default true) fail the test run if this test fails.
    - `setupTicks`: (default 0) wait N ticks before starting the test.

For a more complete list, see [this gist](https://gist.github.com/SizableShrimp/60ad4109e3d0a23107a546b3bc0d9752)

```java
@GameTestHolder(GTCEu.MOD_ID)
@PrefixGameTestTemplate(false)
public class MyTests {
    
  @GameTest(template = "singleblock")
  public static void basicBlockTest(GameTestHelper helper) {
    // Your test logic...
    helper.succeed();
  }
}
```

---

## Structure Templates & Schematics
The testing system works with NBT files. You can use one that already exists, or make your own.
To make your own templates:
1. Spawn in a Structure Block in your game client `/give Dev minecraft:structure_block`
2. Build whatever you want to template above it 
3. Open the structure block and navigate to the "SAVE" profile
4. Edit the relative offset and the structure size
5. Press "Save"
6. Copy `run/saves/[worldname]/generated/minecraft/structures/[name]`
7. Paste it in `src/test/resources/data/gtceu/structures/[name]`
8. Reference via `@GameTest(template = "[name]")`
9. Don't forget to git add it when committing :)

---

## Using GameTestHelper

`GameTestHelper` gives you assertions and world/manipulation utilities, including but not limited to:

- `helper.getBlockState(BlockPos pos)`
- `helper.getBlockEntity(BlockPos pos)`
- `helper.setBlock(BlockPos pos, BlockState state)`
- `helper.runAfterDelay(long delay, Runnable task)`
- `helper.runAtTickTime(long tick, Runnable task)`
- `helper.assertTrue(boolean condition, String msg)`
- `helper.assertFalse(boolean condition, String msg)`
- `helper.assertSameBlockState(BlockState expected, BlockState actual, String msg)`
- `helper.assertSame[....]` a lot of these methods exist for minecraft primitives.
- `helper.succeed()` / `helper.fail(String message)`

It can also help you interact with the world, spawn mock/fake players, assert conditions, etc. etc.

You can also write your own utility methods for repeated patterns (e.g., checking inventories). 
These go in `test/java/com/gregtechceu/gtceu/gametest/util/TestUtils.java`

## Ensuring something does (not) happen
To ensure something does happen:
```
helper.succeedWhen(()->{ 
    helper.assertTrue(thething, ...);
});
```

To ensure something does not happen:
```
helper.onEachTick(()->{
    helper.assertTrue(thing that should not happen doesn't happen);
});
TestUtils.succeedAfterTest(helper);
```
If your test has a timeout value different from 100, you can pass that as a second argument succeedAfterTest


---

## Example: RecipeLogicTest

Here is a relatively complex test using the testing system. 

```java
@GameTestHolder(GTCEu.MOD_ID)
@PrefixGameTestTemplate(false)
public class RecipeLogicTest {    
    @GameTest(template = "singleblock_chem_reactor")
    public static void recipeLogicSingleBlockTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(0, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity metaMachineBlockEntity)) {
            helper.fail("wrong block at relative pos [0,1,0]!");
            return;
        }
        MetaMachine machine = metaMachineBlockEntity.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine recipeLogicMachine)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
    
        // force insert the recipe into the manager.
        GTRecipeType type = recipeLogicMachine.getRecipeType();
        type.getLookup().removeAllRecipes();
        type.getLookup().addRecipe(type
                .recipeBuilder(GTCEu.id("test-singleblock"))
                .id(GTCEu.id("test-singleblock"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(512).duration(1)
                .buildRawRecipe());
    
        RecipeLogic recipeLogic = recipeLogicMachine.getRecipeLogic();
    
        recipeLogic.findAndHandleRecipe();
    
        // no recipe found
        helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(recipeLogic.getLastRecipe() == null,
                "Recipe logic has somehow found a recipe, when there should be none");
    
        // put an item in the inventory that will trigger recipe recheck
        NotifiableItemStackHandler inputSlots = getInputSlot(recipeLogicMachine);
        NotifiableItemStackHandler outputSlots = getOutputSlot(recipeLogicMachine);
    
        inputSlots.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        inputSlots.onContentsChanged();
    
        recipeLogic.findAndHandleRecipe();
        helper.assertFalse(recipeLogic.getLastRecipe() == null,
                "Last recipe is empty, even though recipe logic should've found a recipe.");
        helper.assertTrue(recipeLogic.isActive(), "Recipelogic is inactive, when it should be active.");
        int stackCount = inputSlots.getStackInSlot(0).getCount();
        helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s)".formatted(stackCount));
    
        // Save a reference to the old recipe so we can make sure it's getting reused
        GTRecipe prev = recipeLogic.getLastRecipe();
    
        // Finish the recipe, the output should generate, and the next iteration should begin
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.getLastRecipe().equals(prev), "lastRecipe is wrong");
        helper.assertTrue(TestUtils.isItemStackEqual(
                        outputSlots.getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "wrong output stack.");
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic is not active, when it should be.");
    
        // Complete the second iteration, but the machine stops because its output is now full
        outputSlots.setStackInSlot(0,
                new ItemStack(Blocks.STONE, 63));
        outputSlots.setStackInSlot(1,
                new ItemStack(Blocks.STONE, 64));
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "RecipeLogic is active, when it shouldn't be.");
    
        // Try to process again and get failed out because of full buffer.
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "Recipelogic is active, when it shouldn't be.");
    
        // Some room is freed in the output bus, so we can continue now.
        outputSlots.setStackInSlot(0, ItemStack.EMPTY);
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic didn't start running again");
        recipeLogic.serverTick();
        helper.assertTrue(
                TestUtils.isItemStackEqual(
                        outputSlots.getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "Wrong stack.");
    
        // Finish.
        helper.succeed();
    }
}
```

---

## Running & Debugging Tests

1. **Launch the GameTest server**
    - Run the “Game Tests” run configuration.
    - In logs you’ll see registered tests and pass/fail summaries.

2. **Use Client Commands**
    - Launch the client normally and use one of the following commands:
    - `/test run <modid>:<batch or test name>`
    - `/test runAll`

3. **Inspect Failure Reports**
    - The log will pinpoint the tick and assertion message.
    - You can run the "Game Tests" configuration with debug mode on, to step through the tests.

---

## Best Practices

- **Use descriptive names**: `@GameTest(template="…") public static void myFeaturedoSomething()`.
- **Limit test time**: avoid long loops, use `timeoutTicks` if needed.
- **Group related tests** with `batch` parameter.
- **Reuse helper methods** for common patterns (e.g., inventory insertion).
- **Assert early, clearly and often**: fail fast on mismatches to simplify debugging.
