---
title: Datafixers
---

# What are datafixers?
Datafixers are Mojang's solution to resolving save data layout changes, such as items being renamed or entities' NBT fields moving.<br>
> From [Documented DataFixerUpper's overview summary](https://kvverti.github.io/Documented-DataFixerUpper/snapshot/overview-summary.html):<br>
Data Fixer Upper (or DFU for short) is a library for creating transformations between structures, regardless of the specific schemas or storage formats the structures use. Users of DFU will generally use two sets of types: the serialization types and the data fix types. An overview of the relevant packages is given below, but see the documentation of those packages and types for more details.

TL;DR: DataFixerUpper is a full typed functional programming language written entirely in Java. It's overengineered, but can accomplish every change Mojang could ever want to do.<br>


# How to add datafixers

Register new datafixers in [`GTDataFixers.addFixers`](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/common/data/datafixer/GTDataFixers.java#L108). Remember to bump [`GTCEu.GT_DATA_VERSION`](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/GTCEu.java#L50)
in every PR that changes existing save data and add a schema & fixer for it here.

For changes that do not change named types or add new (block) entities, use [`SAME_NAMESPACED`](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/common/data/datafixer/GTDataFixers.java#L71) for the schema.
Register machines (and other block entities) in a schema overriding `Schema#registerBlockEntities`, like [`V2_3` does](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/common/datafixer/schemas/V2_3.java#L21).

!!! danger
    Do **NOT** add new machines to [`V0`](https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/src/main/java/com/gregtechceu/gtceu/common/datafixer/schemas/V0.java)! They should be in new, correctly versioned schemas!

!!! note "References"
    Reference vanilla schemas for more information on them (examples below). Also check how they're used in vanilla's `net.minecraft.util.datafix.DataFixers` class.  
    `net.minecraft.util.datafix.schemas.V1460`, `net.minecraft.util.datafix.schemas.V1906`


## Adding new (block) entity types

Note how only fields that use other registered (named) types (such as item/fluid stacks) are defined. The same should be done for all types.    
If the type doesn't depend on any named types, register it with `schema.register()` like this:
??? example "registerBlockEntities"
    ```java
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        // ...
        schema.register(map, "gtceu:your_id_here", () -> traitHolder(schema)); // (1)
        // ...
        return map;
    }
    ```
    
    1. Do not use `schema.registerSimple()`! All machines must have the trait holder fields.

!!! tip
    When adding new (block) entity types in a schema, always also add a {@link AddNewChoices} fixer of the appropriate type(s).
