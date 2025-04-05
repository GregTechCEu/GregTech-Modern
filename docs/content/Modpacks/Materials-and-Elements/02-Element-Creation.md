---
title: Element Creation
---


## Element Creation

Elements are the base of GT materials. Registering an element WILL NOT add any items.
To make a new element(NOTE: you can add only elements that are NOT present on the periodic table),
write an `event.create()` call in the registry function like in the example below.
Inside the parentheses the following parameters are introduced:

1.  Element Name -> use '' or "" to write the element name.
2.  Proton Count(use -1 if it is not an element that will get a material).
3.  Neutron Count(use -1 if it is not an element that will get a material).
4.  Half Life Seconds(decay stuff. Use -1 if you don't need to use decay).
5.  Material to decay to(more decay stuff. Use null).
6.  Atomic Symbol(what will be displayed as in chemical formulas) -> use '' or "" to write the atomic symbol.
7.  Is isotope(ex. Uranium 235 and Uranium 238. Use false if you are not making an isotope)

When a material will be created from this element, the above properties will affect the auto-generated recipes.

```js
GTCEuStartupEvents.registry('gtceu:element', event => {
   event.create('test_element', 27, 177, -1, null, 'test', false) // (1)
})
```

1. Element Name, Protons, Neutrons, Half Life Seconds, Decay To, Atomic Symbol, Is Isotope
