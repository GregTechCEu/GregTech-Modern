---
title: Ambiguous Methods
---

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
!!! Note
    Generics don't exist in compiled code, so e.g. a call to `memoize(Supplier<T> delegate)` would turn into `["memoize(Supplier)"](...)`