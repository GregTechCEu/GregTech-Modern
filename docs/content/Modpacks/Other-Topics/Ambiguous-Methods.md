---
title: Ambiguous Methods
---

## Ambiguous Methods
Sometimes in KJS, you run into ambiguous methods when calling functions. 
This happens when there's multiple overloads (e.g. methods with the same name but different types) and KubeJS isn't sure which function to call with your arguments.

For example, when you do:
```js

GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('unboxinator', 'multiblock')
        .tooltips(Component.literal("I am a multiblock"))
    // Rest of the multiblock
})
```

you'd get the error:
```
Error in 'GTCEuStartupEvents.registry': The choice of Java method com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder.tooltips matching JavaScript argument types (net.minecraft.network.chat.MutableComponent) is ambiguous; candidate methods are: 
    class com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder tooltips(java.util.List)
    class com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder tooltips(net.minecraft.network.chat.Component[])
```

In this case, there's ambiguity between the following 2 java functions:
```java
    public MachineBuilder<DEFINITION> tooltips(@Nullable Component... components) {
        return tooltips(Arrays.asList(components));
    }

    public MachineBuilder<DEFINITION> tooltips(List<? extends @Nullable Component> components) {
        tooltips.addAll(components.stream().filter(Objects::nonNull).toList());
        return this;
    }
```

You would want to select one of the two, and this can be done in the following way:
```js
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('unboxinator', 'multiblock')
        ["tooltips(java.util.List)"]([Component.literal("I am a multiblock")])
        // Rest of the multiblock
})
```
or
```js
GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('unboxinator', 'multiblock')
        ["tooltips(net.minecraft.network.chat.Component[])"]([Component.literal("I am a multiblock")])
        // Rest of the multiblock
})
```

Because of the way javascript indexing works, `.foo` and `["foo"]` are the same thing, so you can just keep chaning your functions afterward, since it's just a "normal" builder method, just called in a more specific way.

## Ambiguous Constructors
This same problem can occur when trying to call a constructor.  
For example, when you do
```js
const $MaterialIconSet = Java.loadClass("com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet")

GTCEuStartupEvents.registry("gtceu:material_icon_set", event => {
  let unboxiniumIconSet = new $MaterialIconSet("kubejs:unboxinium")
  // Rest of the script
})
```
You'd get the following error:
```
dev.latvian.mods.rhino.EvaluatorException: The choice of Java constructor com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet matching JavaScript argument types (string) is ambiguous; candidate constructors are: 
    MaterialIconSet(java.lang.String)
    MaterialIconSet(net.minecraft.resources.ResourceLocation) (startup_scripts:example.js#4)
```

You would want to select one of the two, and this can be done in the following way:
```js
GTCEuStartupEvents.registry("gtceu:material_icon_set", event => {
  let unboxiniumIconSet = $MaterialIconSet["(java.lang.String)"]("kubejs:unboxinium")
  // Rest of the script
})
```
or
```js
GTCEuStartupEvents.registry("gtceu:material_icon_set", event => {
  let unboxiniumIconSet = $MaterialIconSet["(net.minecraft.resources.ResourceLocation)"]("kubejs:unboxinium")
  // Rest of the script
})
```
!!! Note
    Generics don't exist in compiled code, so e.g. a call to `memoize(Supplier<T> delegate)` would turn into `["memoize(Supplier)"](...)`