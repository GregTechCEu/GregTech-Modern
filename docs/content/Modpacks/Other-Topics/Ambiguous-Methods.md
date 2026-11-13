---
title: Ambiguous Methods
---

## Ambiguous Methods
Sometimes in KJS, you run into ambiguous methods when calling functions. 
This happens when there's multiple overloads (e.g. methods with the same name but different types) and KubeJS isn't sure which function to call with your arguments.

For example, when you do:
```js

GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('unboxinator')
        .tiers(GTValues.LV)
        .definition((tier, builder) => builder
            .tooltips(Component.literal("I am a machine")))
    // Rest of the machine
})
```

you'd get the error:
```
Error in 'GTCEuStartupEvents.registry': The choice of Java method com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder.tooltips matching JavaScript argument types (net.minecraft.network.chat.MutableComponent) is ambiguous; candidate methods are: 
    class com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder tooltips(java.util.List)
    class com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder tooltips(net.minecraft.network.chat.Component[])
```

In this case, there's ambiguity between the following 2 java functions:
```java
    public SELF tooltips(@Nullable Component... components) {
        // ...
    }

    public SELF tooltips(List<? extends Component> components) {
        // ...
    }
```

You would want to select one of the two, and this can be done in the following way:
```js
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('unboxinator')
        .tiers(GTValues.LV)
        .definition((tier, builder) => builder
            ["tooltips(java.util.List)"]([Component.literal("I am a machine")]))
    // Rest of the machine
})
```
or
```js
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('unboxinator')
        .tiers(GTValues.LV)
        .definition((tier, builder) => builder
            ["tooltips(net.minecraft.network.chat.Component[])"]([Component.literal("I am a machine")]))
    // Rest of the machine
})
```

Because of the way javascript indexing works, `.foo` and `["foo"]` are the same thing, so you can just keep chaning your functions afterward, since it's just a "normal" builder method, just called in a more specific way.

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