---
title: Modpack Creation
---



# Addon and Modpack Creation

GTCEu Modern offers an extensive Java API for addons and modpacks to use, 
and also offers integration with KubeJS for creating machines, recipes, materials, and other GregTech content.  

Refer to this section for information on how to use it, as well as for examples.

## Addon Template

A basic template for creating Java addons and modpack coremods is provided [here](https://github.com/GregTechCEu/GregTech-Addon-Template).

## General Notes

Sometimes, calling a specific method is always required when adding (or modifying) something.  
These methods are marked with `// [*]` in the docs, like in the following example:

```js
ServerEvents.exampleEvent(event => {
    event.create('example', builder => {
        builder.requiredMethod(42) // [*] (1)
        builder.otherRequiredMethod(42) // [*]
        
        builder.optionalMethod() // (2)
    })
})
```

1. These methods are required
2. This method is optional and doesn't have to be called in all cases


## Beyond the Docs

While we try to keep this documentation up to date and as complete as possible, it may not always contain all of the latest information.

Please also check the [Beyond the Docs](./Beyond-The-Docs.md) page for additional references.
