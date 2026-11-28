---
title: Content Modification
---

Many addons and modpacks often wish to modify the content of base GTM and other addons.

The `ContentModificationEvent` can be used to perform modification of multiple types of GT content when they are registered.

# Machine Modification

If you want to modify an existing machine's definition, you can do so using the `ContentModificationEvent.Machine` forge event or the
`GTCEuStartupEvents.machineModification` KJS event. 

It is fired right before a machine gets registered, so that its builder is still accessible through the `event.getBuilder()` method. For examples of methods that machine builders
have, see [Custom Machines](../Examples/Custom-Machines.md).
For a full list of methods, see [source](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/registry/registrate/MachineBuilder.java).

Machine traits are an easy way to add extensible and generic behaviours to a machine without modifying the machine class.
For more info on machine traits, see the [docs page](../../Development/General-Topics/Machine-Trait-System.md)

To add a trait during the machine content modification event, do the following:
```java
@SubscribeEvent
public static void modifyMachines(ContentModificationEvent.Machine event) {
    
    // Add a machine trait to all lathes. 
    if (event.getId().getPath().endsWith("lathe")) {
        event.afterCreation(m -> {
            m.addTrait(new CustomMachineTrait());
        });
    }
}
```

# Tag Prefix Modification

To modify tag prefixes, use the `ContentModificationEvent.TagPrefix` forge event.

# Recipe Type & Category Modification

To modify recipe types & categories, use the `ContentModificationEvent.RecipeType` and `ContentModificationEvent.RecipeCategory` events.
