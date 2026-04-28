---
sidebar_position: 1
---
# Named Binary Tag (NBT)

NBT is a format introduced in the earliest days of Minecraft, written by Notch himself. It is widely used throughout the Minecraft codebase for data storage.

## Specification

The NBT spec is similar to the JSON spec, with a few differences:

- Distinct types for bytes, shorts, longs and floats exist, suffixed by `b`, `s`, `l` and `f`, respectively, similar to how they would be represented in Java code.
    - Doubles may also be suffixed with `d`, but this is not required, similar to Java code. The optional `i` suffix available in Java for integers is not permitted.
    - The suffixes are not case-sensitive. So for example, `64b` is the same as `64B`, and `0.5F` is the same as `0.5f`.
- Booleans do not exist, they are instead represented by bytes. `true` becomes `1b`, `false` becomes `0b`.
    - The current implementation treats all non-zero values as `true`, so `2b` would be treated as `true` as well.
- There is no `null` equivalent in NBT.
- Quotes around keys are optional. So a JSON property `"duration": 20` can become both `duration: 20` and `"duration": 20` in NBT.
- What is known in JSON as a sub-object is known in NBT as a **compound tag** (or just compound).
- NBT lists cannot mix and match types, unlike in JSON. The list type is determined by the first element, or defined in code.
    - However, lists of lists can mix and match different list types. So a list of two lists, where the first one is a list of strings and the second one is a list of bytes, is allowed.
- There are special **array** types that are different from lists, but follow their scheme of containing elements in square brackets. There are three array types:
    - Byte arrays, denoted by a `B;` at the beginning of the array. Example: `[B;0b,30b]`
    - Integer arrays, denoted by a `I;` at the beginning of the array. Example: `[I;0,-300]`
    - Long arrays, denoted by an `L;` at the beginning of the array. Example: `[L;0l,240l]`
- Trailing commas in lists, arrays and compound tags are allowed.

## NBT Files

Minecraft uses `.nbt` files extensively, for example for structure files in [datapacks][datapack]. Region files (`.mca`) that contain the contents of a region (i.e. a collection of chunks), as well as the various `.dat` files used in different places by the game, are NBT files as well.

NBT files are typically compressed with GZip. As such, they are binary files and cannot be edited directly.

## NBT in Code

Like in JSON, all NBT objects are children of an enclosing object. So let's create one:

```java
CompoundTag tag = new CompoundTag();
```

We can now put our data into that tag:

```java
tag.putInt("Color", 0xffffff);
tag.putString("Level", "minecraft:overworld");
tag.putDouble("IAmRunningOutOfIdeasForNamesHere", 1d);
```

Several helpers exist here, for example, `putIntArray` also has a convenience method that takes a `List<Integer>` in addition to the standard variant that takes an `int[]`.

Of course, we can also get values from that tag:

```java
int color = tag.getInt("Color");
String level = tag.getString("Level");
double d = tag.getDouble("IAmRunningOutOfIdeasForNamesHere");
```

Number types will return 0 if absent. Strings will return `""` if absent. More complex types (lists, arrays, compounds) will throw an exception if absent.

As such, we want to safeguard by checking if a tag element exists:

```java
boolean hasColor = tag.contains("Color");
boolean hasColorMoreExplicitly = tag.contains("Color", Tag.TAG_INT);
```

The `TAG_INT` constant is defined in `Tag`, which is the super interface for all tag types. Most tag types besides `CompoundTag` are mostly internal, for example `ByteTag` or `StringTag`, though the direct `CompoundTag#get` and `#put` methods can work with them if you ever stumble across some.

There is one obvious exception, though: `ListTag`s. Working with these is special because when getting a list tag through `CompoundTag#getList`, you must also specify the list type. So getting a list of strings, for example, would work like this:

```java
ListTag list = tag.getList("SomeListHere", Tag.TAG_STRING);
```

Similarly, when creating a `ListTag`, you must also specify the list type during creation:

```java
ListTag list = new ListTag(List.of("Value1", "Value2"), Tag.TAG_STRING);
```

Finally, working with `CompoundTag`s inside other `CompoundTag`s directly utilizes `CompoundTag#get` and `#put`:

```java
tag.put("Tag", new CompoundTag());
tag.get("Tag");
```

## Usages of NBT

NBT is used in a lot of places in Minecraft. Some of the most common examples include [`BlockEntity`][blockentity]s and [`Entity`s][entity].

:::note
`ItemStack`s abstract away the usage of NBT into [data components][datacomponents].
:::

## See Also

- [NBT Format on the Minecraft Wiki][nbtwiki]

[blockentity]: ../blockentities/index.md
[datapack]: ../resources/index.md#data
[datacomponents]: ../items/datacomponents.md
[entity]: ../entities/index.md
[nbtwiki]: https://minecraft.wiki/w/NBT_format
