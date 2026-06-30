---
title: Structure Checking
---
The `MultiblockControllerMachine` class has a specific ordering in how it checks and forms the multiblocks.
!!! NOTE
    Structure checking/forming only happens on the server side, if you need that information on client it MUST be synced through the annotation system (non ui) or MUI synced values (ui)

1. When the controller is first placed `checkAndFormStructure()` is called.

2. For every `IBlockPattern` in its definition, if that corresponding `PatternState` is not already formed:
   1. Each `IBlockPattern` is run through `checkStructurePattern(name)` 
   2. If that structure is valid the structure is run through `formStructure(name)`

3. The `PatternState` is added to the `MultiblockWorldSaveData` for `BlockState` listening


In `checkStructurePattern`, the pattern will use its iteration method and ensure every `PatternPredicate` succeeds, for each block position that matches the predicate, that block's information will be put in the `PatternState`'s cache for future use, like retrieving multi parts for recipe logic handler collection. When a predicate fails, it puts the failure reason(s) into the `PatternState` and stops the match iteration early.

In `formStructure`, every `IMultiPart` found in the `PatternState` cache is attached to that specific controller.

```java title="MultiPartGathering.java"

public void formStructure(String substructureName) {
    ...
    if (patternState.getState() == PatternState.CheckState.VALID_UNCACHED) {
        forEachMultiPart(substructureName, part -> { // (1)
            if (parts.contains(part)) return true;

            if (part.hasController(getBlockPos()) && !part.canShared(this, substructureName)) {
                invalidateStructure(substructureName);
                return false;
            }

            if (shouldAddPartToController(part)) {
                this.parts.add(part);
            }
            return true;
        });
        
        for (var part : parts) {
            ...
            part.addedToController(this, substructureName);
        }
    ...
}
```

1. Execute some runnable for every multipart in this multiblock pattern structure.

Machine classes can extend `formStructure(name)` for additional information gathering, like the coiled multiblocks. 
```java title="WorkableCoilElectricMultiblock.java"
public class CoilWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine { // (1)
    @Getter
    private ICoilType coilType = CoilBlock.CoilType.CUPRONICKEL;
    @SyncToClient
    @Getter
    private int coilTier = 1;

    @Override
    public void formStructure(@NotNull String substructureName) { // (2)
        super.formStructure(substructureName);
        var cache = patternStates.get(substructureName).getCache(); // (3)
        ICoilType coilType = null;
        for (var entry : cache.long2ObjectEntrySet()) {
            var state = entry.getValue().getBlockState();
            if (state.getBlock() instanceof CoilBlock coil) {
                if (GTCEuAPI.HEATING_COILS.containsKey(coil.coilType)) {
                    if (coilType == null) coilType = coil.coilType;
                    else {
                        if (coilType != coil.coilType) {
                            // (4)
                            patternStates.get(substructureName).setError(
                                    new CoilMatchingError(BlockPos.of(entry.getLongKey()), coilType, coil.coilType));
                            invalidateStructure(substructureName); // (5)
                            return;
                        }
                    }
                }
            }
        }
        if (coilType != null) {
            this.coilType = coilType;
            this.coilTier = coilType.getTier();
            getSyncDataHolder().markClientSyncFieldDirty("coilTier");
        }
    }
}
```

1. WorkableElectricMultiblockMachine extends MultiblockControllerMachine, where `formStructure()` is.

2. `formStructure` can behave differently depending on what name is passed, for example if you want sub structures to have different forming logic.

3. How to retrieve information from the pattern cache.

4. If you want to explicitly invalidate a structure with custom reasoning, make sure to set the `PatternError` so that users know why.

5. Call `invalidateStructure` manually so that that specific substructure is not formed.


Structure check and forming only occurs under a few conditions:

 - the controller is first loaded into world.

 - a block state change occurred somewhere in the bounds of the pattern cache (only for predicates that are not ANY).

 - The multiblock controller has its front or upwards facing rotated.

Structure checking also no longer happens off thread or asynchronously.
