---
title: Ambiguous Methods
---

## Ambiguous Methods
Sometimes in KJS, you run into ambiguous methods when calling functions. 
This happens when there's multiple overloads (e.g. methods with the same name but different types) and KubeJS isn't sure which function to call with your arguments.

For example, when you do:
```js
GTCEuStartupEvents.materialModification(event => {
    TagPrefix.ingot.get().removeIgnored(GTMaterials.Iron)
})
```

you'd get the error:
```
Error in 'GTCEuStartupEvents.materialModification': dev.latvian.mods.rhino.EvaluatorException: The choice of Java method com.gregtechceu.gtceu.api.data.tag.TagPrefix.removeIgnored matching JavaScript argument types (com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry) is ambiguous; candidate methods are: 
    void removeIgnored(net.minecraft.core.Holder)
    void removeIgnored(com.gregtechceu.gtceu.api.data.chemical.material.Material)
```

In this case, there's ambiguity between the following 2 java functions, because `GTMaterials.Iron` is a registry entry that KubeJS can pass as either:
```java
    public void removeIgnored(Material material) {
        // ...
    }

    public void removeIgnored(Holder<Material> material) {
        // ...
    }
```

You would want to select one of the two, and this can be done in the following way:
```js
GTCEuStartupEvents.materialModification(event => {
    TagPrefix.ingot.get()["removeIgnored(net.minecraft.core.Holder)"](GTMaterials.Iron)
})
```
or
```js
GTCEuStartupEvents.materialModification(event => {
    TagPrefix.ingot.get()["removeIgnored(com.gregtechceu.gtceu.api.data.chemical.material.Material)"](GTMaterials.Iron.get())
})
```

Because of the way javascript indexing works, `.foo` and `["foo"]` are the same thing, so if the method is a builder method you can just keep chaining your functions afterward, since it's just a "normal" method, just called in a more specific way.

## Ambiguous Constructors
This same problem can occur when trying to call a constructor.  
For example, when you do
```js
GTCEuStartupEvents.craftingComponents(event => {
    event.setMaterialEntry(GTCraftingComponents.PLATE, GTValues.UEV, new MaterialEntry('plate', 'gtceu:infinity'))
})
```
You'd get the following error:
```
dev.latvian.mods.rhino.EvaluatorException: The choice of Java constructor com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry matching JavaScript argument types (string,string) is ambiguous; candidate constructors are: 
    MaterialEntry(com.gregtechceu.gtceu.api.data.tag.TagPrefix,com.gregtechceu.gtceu.api.data.chemical.material.Material)
    MaterialEntry(net.minecraft.core.Holder,net.minecraft.core.Holder)
    ... (startup_scripts:example.js#2)
```

You would want to select one of them, and this can be done in the following way:
```js
GTCEuStartupEvents.craftingComponents(event => {
    event.setMaterialEntry(GTCraftingComponents.PLATE, GTValues.UEV,
        MaterialEntry["(com.gregtechceu.gtceu.api.data.tag.TagPrefix,com.gregtechceu.gtceu.api.data.chemical.material.Material)"]('plate', 'gtceu:infinity'))
})
```
!!! Note
    Generics don't exist in compiled code, so e.g. a call to `memoize(Supplier<T> delegate)` would turn into `["memoize(Supplier)"](...)`