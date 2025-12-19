---
title: Element Creation
---
# Element Creation

Elements are the base of GT materials. Registering an element will NOT add any items. By default, all periodic table elements are registered by GT, but some of them are unused and don't have any properties attached.

Elements can be created using `GTElements#createAndRegister` for ease of use. If you choose to declare them yourself, keep in mind you need to register your elements to `GTRegistries.ELEMENTS`

Elements have the following parameters:

2.  `long protons` -> Amount of Protons (use -1 if it is not an element that will get a material).
3.  `long neutrons` -> Amount of Neutrons (use -1 if it is not an element that will get a material).
4.  `long halfLifeSeconds` -> Amount of Half Life this Material has in Seconds. -1 for stable Materials
5.  `String decayTo` -> String representing the Elements this element decays to. Separated by an '&' Character
1.  `String name` -> Name of the Element
6.  `String symbol` -> Symbol of the element. Used to represent the element in chemical formulas (eg. H for hydrogen, Zn for Zinc)
7.  `boolean isIsotope` -> Is this element an isotope? (eg. Uranium-235 and Uranium-238)


??? example
    ```java title="ModElements.java"
    public static final Element Xz = GTElements.createAndRegister(16, 18, -1, null, "XYZ", "Xz", false); 
    ```

When a material will be created from this element, the above properties will affect the auto-generated recipes.


!!! info "Mass number"
    
    The mass number of an element is the sum of its protons and neutrons. This affects the duration of some recipes, including wiremills and benders. A higher mass number usually  means the recipe takes longer to run. This affects any materials that are composed of the element as well

## Registration
Once your Element(s) are declared, they can be registered using `IGTAddon#registerElements`. See [here](../Setup.md#registering-your-content)