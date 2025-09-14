---
title: Custom Machine Behavior
---

!!! Warning
    Custom Machine Behavior is currently only supported in Java.

Sometimes, you want to do something in a machine that's not possible via Recipe Conditions or Recipe Modifiers. For this, Custom Machine Behavior might be the correct tool.

It works by registering a custom TickableSubscription, which gets called every tick.
Here, we want to make a greenhouse that, while running, also turns all the dirt above it into grass.
```java

public class Greenhouse extends WorkableElectricMultiblockMachine {

    private TickableSubscription tickSubscription;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!isRemote()) {
            tickSubscription = this.subscribeServerTick(this::turnGreenery);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (!isRemote()) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }
    
    private void turnGreenery(){
        if(!getRecipeLogic().isActive()) return;
        BlockPos currentPosition = getRecipeLogic().getMachine().getHolder().getCurrentPos();
        BlockPos abovePosition = currentPosition.above();
        if(this.getLevel().getBlockState(abovePosition).equals(Blocks.DIRT.defaultBlockState())){
            this.getLevel().setBlock(abovePosition, Blocks.GRASS.defaultBlockState(), 3);
        }
    }
}
```
The `tickSubscription` field is a reference to the current subscription that should be called every tick. When this subscription is created, we tell the server to run `this.turnGreenery()` every tick.  

In there, we simply check if the recipe logic is active, and if so, we turn the block above the machine into grass if it was already dirt.  

To use it, you would do:
```java
    public static final MultiblockMachineDefinition GREENHOUSE = REGISTRATE
        .multiblock("green_house", Greenhouse::new)
        .rotationState(RotationState.ALL)
        .recipeType(MyRecipeTypes.GREENHOUSE)
        .recipeModifiers(OC_PERFECT_SUBTICK, BATCH_MODE)
        .appearanceBlock(CASING_PTFE_INERT)
        .pattern(definition -> {
            var casing = blocks(CASING_PTFE_INERT.get()).setMinGlobalLimited(10);
            var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                    .or(Predicates.autoAbilities(true, false, false));
            return FactoryBlockPattern.start()
                    .aisle("XSX", "XXX", "XXX")
                    .aisle("XXX", "XXX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', casing.or(abilities))
                    .build();
        })
        .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                GTCEu.id("block/multiblock/large_chemical_reactor"))
        .register();
```
