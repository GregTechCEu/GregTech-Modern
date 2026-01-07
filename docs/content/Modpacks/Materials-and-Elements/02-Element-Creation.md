---
title: Element Creation
---


## Element Creation
!!! Note
    You can add only elements that are not yet present on the periodic table.
    For those elements, see GTElements.
Elements are the base of GT materials. Registering an element WILL NOT add any items.

```js
GTCEuStartupEvents.registry('gtceu:element', event => {
   event.create('test_element')
        .protons(27)
        .neutrons(177)
        .halfLifeSeconds(-1)
        .decayTo(null)
        .symbol('test')
        .isIsotope(false)
})
```

1.  `.create(String name)` ->  The element name.
2.  `.protons(int protons)` -> Proton Count. Use `-1` if it is an element that will not get a material.
3.  `.neutrons(int neutrons)` -> Neutron Count. Use `-1` if it is an element that will not get a material
4.  `.halfLifeSeconds(int seconds)` -> Half Life Decay in Seconds. After N seconds, half of the material will have decayed. Use `-1` if your element doesn't decay.
5.  `.decayTo(Material material)` -> Material to decay to. Use `null` if your element doesn't decay.
6.  `.symbol(String symbol)` -> Atomic Symbol, which will be displayed as in chemical formulas.
7.  `.isIsotope(boolean isotope)` -> Whether the element is an isotope, e.g. Uranium 235 and Uranium 238.

When a material will be created from this element, the above properties will affect the auto-generated recipes.