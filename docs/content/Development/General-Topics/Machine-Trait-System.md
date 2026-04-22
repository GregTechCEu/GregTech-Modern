---
title: Machine Trait System
---

# Machine Trait System

The machine trait system allows for attaching extra capabilities and behaviours to a machine by attaching traits to machine instances. Traits can listen for specific machine events, interactions, and provide capabilities, allowing for machine functionality to be implemented via a composition-based design.

## Attaching and using machine traits

Attaching traits must be done before machine instances are fully initialised, and are typically attached in the constructor.

### Attaching and using traits:

Attaching a machine trait to a machine is done with the `MetaMachine::attachTrait` and `MetaMachine::attachPersistentTrait` methods.

When attaching traits, a `callbackPriority` value can be given. Traits with a higher priority will have their events and interactions called
first, which may prevent traits with a lower priority from handling some events.

!!! warning
    Traits must be attached to a machine instance via either `attachTrait` or `attachPersistentTrait`.  
    If a trait is created without either of these methods being called, the trait will not be valid.

```java
public class CustomMachine extends MetaMachine {
    
  @SaveField
  protected final NotifiableFluidTank tank;

  public CustomMachine(BlockEntityCreationInfo info, int capacity) {
    super(info);
    
    // Because the tank field is annotated with `@SaveField`, the fluid tank will be saved into the machines data.
    this.tank = attachTrait(new NotifiableFluidTank(1, capacity, IO.BOTH));
    
    // Registers an auto output trait that provides fluid output behaviour for the given fluid tank.
    // Instead of using an annotated field to save traits, they can also be registered to be saved.
    // The trait save name should remain the same, otherwise the trait save data won't be loaded.  
    AutoOutputTrait autoOutput = attachPersistentTrait("autoOutput", AutoOutputTrait.ofFluids(tank));
    
    autoOutput.setFluidOutputDirection(Direction.DOWN);
    autoOutput.setFluidOutputDirectionValidator(d -> d == Direction.DOWN);
  }
  
  public void usingTraits() {
      MetaMachine machine = getMachine();

      // Most trait objects have a `TYPE` static field, it can be used to get traits with a specific type.
      AutoOutputTrait autoOutputTrait = machine.getTrait(AutoOutputTrait.TYPE);
      Optional<RecipeLogic> recipeLogicOptional = machine.getTrait(RecipeLogic.TYPE);

      // Gets all traits with the specified type.
      List<NotifiableItemStackHandler> allItemStackHandlers = machine.getTraits(NotifiableItemStackHandler.TYPE);
      
      List<MachineTrait> allTraits = machine.getAllTraits();
  }
}
```

### Creating custom traits 

Custom machine traits are created by extending `MachineTrait` or `MultiblockMachineTrait`

Machine traits have access to a number of machine events and callbacks, but some extra behaviours can be added by having a trait implement a trait feature interface. For example, `IInteractionTrait` to add custom interaction behaviour. The full list of trait features is in `api/machine/trait/feature`.

```java 
public class CustomMachineTrait extends MachineTrait implements IInteractionTrait {
    
    // Machine traits should have a type object defined, unless a parent class of this machine trait already defines a type
    public static final MachineTraitType<CustomMachineTrait> TYPE = new MachineTraitType<>(CustomMachineTrait.class);
    
    public CustomMachineTrait() {
        
    }
    
    // Machine traits must also define a getter for the trait type
    @Override
    public MachineTraitType<CustomMachineTrait> getType() {
        return TYPE;
    }

    // A list of classes or interfaces which a machine must be in order for this trait to be attached.
    // A machine trait must be at least one of these interfaces/classes.
    // By default, traits can be attached to any machine.
    @Override
    protected List<Class<?>> validMachineClasses() {
        return List.of(CustomMachine.class);
    }

    // An example of a machine trait event callback.
    // Traits with a higher trait priority will have their events called first, 
    // which may block lower priority traits from receiving events.
    // All traits default to a priority of 1.
    @Override
    public Pair<@Nullable GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        var toolType = context.getToolType();
        if (toolType.contains(GTToolType.WRENCH)) {
            return Pair.of(GTToolType.WRENCH, onWrenchClick(context));
        }
        return IInteractionTrait.super.onToolClick(context);
    }
    
    private InteractionResult onWrenchClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }
}
```