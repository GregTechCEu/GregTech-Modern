---
title: Predicates
---
`PatternPredicate`s are the main system of turning some potential block in a multiblock `IBlockPattern` into a valid or invalid state.

`PatternPredicate` can be composed of one or more `BasePredicate`s which means any of those `BasePredicate`s can succeed for the `PatternPredicate` to succeed (returning no `PatternError`).

```java title="CustomPredicate.java"

public static PatternPredicate customPredicate() {
    return new PatternPredicate("MyDebugName", // (1)
            (blockInfo) -> { // (2)
        BlockState state = blockInfo.getBlockState();
        if (state.getBlock() == Blocks.OAK_WOOD) {
            return null; // (3)
        }
        return new BlockMatchingError(blockInfo.getBlockPos(), Blocks.OAK_WOOD); // (4)
    }, new BlockInfo(Blocks.OAK_WOOD)); // (5)
}
```
1. Debug name of the predicate (used in the terminal preview), optional.
2. The condition for if the PatternPredicate matches.
3. If the predicate succeeds it MUST return null;
4. If the predicate fails it will return some `PatternError` (see [Pattern Errors](./PatternError.md))
5. The list of all valid candidates for this predicate (used for terminal previewing and autobuilding).



`BasePredicate`s are composed of two values, 
1. The `Predicate<BlockInfo, PatternError> predicateError` which runs for each block state this predicate is assigned to and returns that specific error.
2. The `List<BlockInfo> candidates` which are all the valid blocks for that predicate.
3. `BasePredicate`s can also have a global min and max value, which causes specific `PatternError`s if there are too much or not enough of that predicate match succeeding. 

There are several helper methods to make various PatternPredicates:

```java title="PredicateShortcuts.java"

PatternPredicate any(); // (1)
PatternPredicate air(); // (2)

PatternPredicate controller(MultiblockMachineDefinition def); // (3)
PatternPredicate machines(MachineDefinition... definitions); // (4)

PatternPredicate blocks(String debugName, Block... blocks); // (5)
PatternPredicate blocks(Block... blocks); // (6)
PatternPredicate states(BlockState... allowedStates); // (7)
PatternPredicate fluids(Fluid... fluids); // (8)

PatternPredicate blockTag(TagKey<Block> tag); // (9)
PatternPredicate fluidTag(TagKey<Fluid> tag); // (10)

PatternPredicate abilities(PartAbility... abilities); // (11)
PatternPredicate ability(PartAbility ability, int... tiers); // (12)
PatternPredicate autoAbilities(GTRecipeType[] recipeType,
                               boolean checkEnergyIn, boolean checkEnergyOut,
                               boolean checkItemIn, boolean checkItemOut,
                               boolean checkFluidIn, boolean checkFluidOut); // (13)
PatternPredicate autoAbilities(GTRecipeType... recipeType); // (14)

PatternPredicate autoAbilities(boolean checkMaintenance, boolean checkMuffler,
                               boolean checkParallel); // (15)

PatternPredicate heatingCoils(); // (16)
PatternPredicate cleanroomFilters(); // (17)
PatternPredicate powerSubstationBatteries(); // (18)
PatternPredicate dataHatchPredicate(); // (19)
PatternPredicate frames(Material... frameMaterials); // (20)
```
1. Any block matches, returns no error.
2. Only AIR can exist here.
3. Shortcut for `blocks(definition.getBlock())` and sets the controller to that specific predicate.
4. Must match any of these machines.
5. Must match any of these blocks.
6. Must match any of these blocks.
7. Must match any of these block states.
8. Must match any of these fluids.
9. Must match the block tag.
10. Must match the fluid tag.
11. Must be one of the blocks in the `PartAbility` (see [Part Abilities](./PartAbility.md))
12. Must be one of the blocks in the `PartAbility` (see [Part Abilities](./PartAbility.md)) with one of those tier values.
13. Fills predicate with the EU, Item and Fluid PartAbilities based on the RecipeType's recipe capability max values.
14. Fills predicate with any of those RecipeType's recipe capabilities, see // (13)
15. Fills predicate with Maintenance, Muffler and ParallelHatch PartAbilities.
16. Fills predicate with CoilBlocks (used in Electric Blast Furnace, Cracking Unit, Rotary Hearth, etc.)
17. Fills predicate with Cleanroom Filter casings.
18. Fills predicate with PowerSubstation batteries.
19. Fills predicate with Data Access and Optical Reception part abilities.
20. Fills predicate with any GT frame matching those materials or any pipe with one of those frame box materials.

```java title="ComplexPredicate.java"

PatternPredicate myCustomPredicate = Predicates.heatingCoils()
        .or(Predicates.blocks(Blocks.DIRT)) // (1)
        .or(Predicates.frames(GTMaterials.Steel).setExactLimit(20)) // (2)
        .or(Predicates.autoAbilities(true, false, true))
        .setMinGlobalLimited(40); // (3)

```
1. To join multiple valid predicates, use `.or(PatternPredicate)` to chain them.
2. A `BasePredicate` can have its own min and max counts separate to the rest of the `BasePredicates`.
3. Sets the min count to 40 for the whole `Predicate`.