---
title: Patterns
---
Multiblocks are MetaMachines that, for the sake of structure checking, are composed of two main features, the `IBlockPattern`s they need to form and the `PatternState` they exist in. Multiblocks can hold one or more patterns, each with a corresponding `PatternState`. When creating a multiblock definition, one will usually define a main structure (the part the controller lives in) and any amount of "sub"structures.

There are two main forms of `IBlockPattern` in base GT Modern, `BlockPattern` and `ExpandablePattern`.

BlockPatterns are a list of slices which are comprised of a list of strings and a mapping of each character to some `PatternPredicate`.

ExpandablePatterns are a pattern that checks in some axis aligned cuboid around the pattern's origin and for each block in the bounds runs a `Predicate<PatternPredicate>`.

When creating a `PatternDefinition` for either a `BlockPattern` or `ExpandablePattern` in a `MultiblockMachineDefinition`, you need to define the iteration directionality and the origin of the pattern. The pattern `OriginOffset` is where each block in the pattern should be relative to (usually where the controller appears in the pattern).

```java title="ExampleBlockPattern.java"

public static MultiblockMachineDefinition MY_MACHINE = REGISTRATE
        .multiblock("my_machine", (creationInfo) -> new MyMachineClass(creationInfo))
        .rotationState(RotationState.ALL)
        .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
        .appearanceBlock(GTBlocks.CASING_INVAR_HEATPROOF)
        .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT) // (1)
                .slice("XXX", "CCC", "CCC", "XXX") // (2)
                .sliceRepeatable(1, 3, "XXX", "C#C", "C#C", "XMX") // (3)
                .slice("XSX", "CCC", "CCC", "XXX")
                .slice("XSXX", "CCCC", "CCCC", "XXXX") // (7)
                .where('S', Predicates.controller(definition)) // (4)
                .where('X', Predicates.blocks(GTBlocks.CASING_INVAR_HEATPROOF)
                        .or(Predicates.autoAbilities(GTRecipeTypes.ASSEMBLER_RECIPES))) // (5)
                .where('M', Predicates.autoAbilities(false, true, false))
                .where('A', Predicates.blockTag(CustomTags.CLEANROOM_FLOORS)) // (6)
                .build())
        .register();
```
1. The three directions passed in `.start()` are the directions to traverse per-slice, per-string-in-slice, and per-char-in-string respectively. The directions are relative based on the controllers front and upwards facing. Calling `.start()` with no arguments uses `BACK, UP, RIGHT`.
2. How the 2-D view of this specific slice's mapping looks.
3. A slice with an optional amount of repeats allowed, first value is min allowed(can be zero to make the whole slice optional), second is max allowed.
4. `.where(char, PatternPredicate)` is how the block info to predicate evaluation mapping happens.
5. An example complex PatternPredicate.
6. This char mapping will throw an exception as 'A' does not appear in the pattern slices.
7. This slice view will throw an exception as all slice's widths and heights must be the same respectively.