---
sidebar_position: 4
---
# Saved Data

The Saved Data (SD) system can be used to save additional data on levels.

_If the data is specific to some block entities, chunks, or entities, consider using a [data attachment](attachments) instead._

## `SavedData`

Each SD implementation must subtype the `SavedData` class. This can be implemented like any other object, with your own fields and methods, but if you want to store the data or change to disk, then you must call `setDirty`. `setDirty` notifies the game that there are changes that need to be written. If not called, then the data will only persist as long as the current level (or world in case of the Overworld level) is loaded.

```java
// For some saved data implementation
public class ExampleSavedData extends SavedData {

    public void foo() {
        // Change data in saved data
        // Call set dirty if data changes
        this.setDirty();
    }
}
```

## `SavedDataType`

As the `SavedData` is simply an object, there needs to be some sort of associated identifier. Additionally, we also need to read and write the data to disk. This is where the `SavedDataType` comes in. It takes in the identifier of the saved data, a default constructor for when no data is present, and a [codec] used to encode and decode the data. The identifier is treated as the path location within the associated world folder and level dimension like so: `./<world_folder>/<level_name>/data/<identifier>.dat`. Any missing directories will be created, including those used as part of the identifier.

:::note
There is an additional fourth parameter for the `DataFixTypes`, but as NeoForge does not support data fixers, all vanilla use cases have been patched to allow null values.
:::

There are two variations of the `SavedDataType` constructor. The first takes in a simple `Supplier` for the constructor and a regular `Codec` for the disk handling. However, if you want to store the current `ServerLevel` or world seed, there is an overload that takes in a `Function` for both, supplying a `SavedData.Context`.

```java
// For some saved data implementation
public class NoContextExampleSavedData extends SavedData {

    public static final SavedDataType<NoContextExampleSavedData> ID = new SavedDataType<>(
        // The identifier of the saved data
        // Used as the path within the level's `data` folder
        "example",
        // The initial constructor
        NoContextExampleSavedData::new,
        // The codec used to serialize the data
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("val1").forGetter(sd -> sd.val1),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("val2").forGetter(sd -> sd.val2)
        ).apply(instance, NoContextExampleSavedData::new))
    );

    // Initial constructor
    public NoContextExampleSavedData() {
        // ...
    }

    // Data constructor
    public NoContextExampleSavedData(int val1, Block val2) {
        // ...
    }

    public void foo() {
        // Change data in saved data
        // Call set dirty if data changes
        this.setDirty();
    }
}

// For some saved data implementation
public class ContextExampleSavedData extends SavedData {

    public static final SavedDataType<ContextExampleSavedData> ID = new SavedDataType<>(
        // The identifier of the saved data
        // Used as the path within the level's `data` folder
        "example",
        // The initial constructor
        ContextExampleSavedData::new,
        // The codec used to serialize the data
        ctx -> RecordCodecBuilder.create(instance -> instance.group(
            RecordCodecBuilder.point(ctx),
            Codec.INT.fieldOf("val1").forGetter(sd -> sd.val1),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("val2").forGetter(sd -> sd.val2)
        ).apply(instance, ContextExampleSavedData::new))
    );

    // Initial constructor
    public ContextExampleSavedData(SavedData.Context ctx) {
        // ...
    }

    // Data constructor
    public ContextExampleSavedData(SavedData.Context ctx, int val1, Block val2) {
        // ...
    }

    public void foo() {
        // Change data in saved data
        // Call set dirty if data changes
        this.setDirty();
    }
}
```

## Attaching to a Level

Any `SavedData` is loaded and/or attached to a level dynamically. As such, if one is never created on a level, then it will not exist.

`SavedData`s are created and loaded from the `DimensionDataStorage`, which can be accessed by calling either `ServerChunkCache#getDataStorage` or `ServerLevel#getDataStorage`. From there, you can get or create an instance of your SD by calling `DimensionDataStorage#computeIfAbsent`, passing in the `SavedDataType`. This will attempt to get the current instance of the SD if present or create a new one and load all available data.

```java
// In some method with access to the DimensionDataStorage
netherDataStorage.computeIfAbsent(ContextExampleSavedData.ID);
```

If a SD is not specific to a level, the SD should be attached to the Overworld, which can be obtained from `MinecraftServer#overworld`. The Overworld is the only dimension that is never fully unloaded and as such makes it perfect to store multi-level data on.

[codec]: codecs.md
