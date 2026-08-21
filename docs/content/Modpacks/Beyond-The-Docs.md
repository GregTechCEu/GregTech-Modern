---
title: Beyond the Docs
---


# Beyond the Docs

While we try to keep this documentation up to date and as complete as possible, it does not contain every detail about the GregTech API.

Looking through the GregTech source code is often a good way to find more detail about the API,
and many Java methods have JavaDoc markup which provides extra information about the API.

Continue reading for a few important places you may want to check.

## KJS Integration

As an additional resource to these docs, you can also reference our KubeJS integration directly in the source code:  
[`src/main/java/com/gregtechceu/gtceu/integration/kjs`](https://github.com/GregTechCEu/GregTech-Modern/tree/1.20.1/src/main/java/com/gregtechceu/gtceu/integration/kjs)

## KJS Builders

!!! link "Builders"
    [`src/main/java/com/gregtechceu/gtceu/integration/kjs/builders`](https://github.com/GregTechCEu/GregTech-Modern/tree/1.20.1/src/main/java/com/gregtechceu/gtceu/integration/kjs/builders)

If you're not sure what methods and fields are available on one of our builders, you can find all of them in this directory.

## KJS Bindings & Type Wrappers

!!! link "GregTechKubeJSPlugin"
    [`src/main/java/com/gregtechceu/gtceu/integration/kjs/GregTechKubeJSPlugin.java`](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/integration/kjs/GregTechKubeJSPlugin.java)

- For a list of our custom bindings, see `GregTechKubeJSPlugin.registerBindings()`
- For a list of our type wrappers and their accepted inputs, see `GregTechKubeJSPlugin.registerTypeWrappers()`
