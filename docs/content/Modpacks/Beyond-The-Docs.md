---
title: Beyond the Docs
---


# Beyond the Docs

While we try to keep this documentation up to date and as complete as possible, it may not always contain all of the latest information.

As an additional resource to these docs, you can also reference our KubeJS integration directly in the source code:  
[`src/main/java/com/gregtechceu/gtceu/integration/kjs`](https://github.com/GregTechCEu/GregTech-Modern/tree/1.20.1/src/main/java/com/gregtechceu/gtceu/integration/kjs)

Continue reading for a few important places you may want to check.


## Builders

!!! link "Builders"
    [`src/main/java/com/gregtechceu/gtceu/integration/kjs/builders`](https://github.com/GregTechCEu/GregTech-Modern/tree/1.20.1/src/main/java/com/gregtechceu/gtceu/integration/kjs/builders)

If you're not sure what methods and fields are available on one of our builders, you can find all of them in this directory.


## Material Builder

!!! link "Material Builder"
    [`src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/Material.java`](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/api/data/chemical/material/Material.java)

The material builder is not located in the KJS integration package.  
Please reference the nested `Material.Builder` class instead.


## Bindings & Type Wrappers

!!! link "GregTechKubeJSPlugin"
    [`src/main/java/com/gregtechceu/gtceu/integration/kjs/GregTechKubeJSPlugin.java`](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/integration/kjs/GregTechKubeJSPlugin.java)

- For a list of our custom bindings, see `GregTechKubeJSPlugin.registerBindings()`
- For a list of our type wrappers and their accepted inputs, see `GregTechKubeJSPlugin.registerTypeWrappers()`