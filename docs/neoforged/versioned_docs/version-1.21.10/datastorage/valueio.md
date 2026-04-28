---
sidebar_position: 3
---
# Value I/O

The Value I/O system is a standardized serialization method to manipulate data of some backing object, such as [`CompoundTag`s for NBT][nbt].

## Inputs and Outputs

The Value I/O system is made up of two parts: a `ValueOutput` that writes to the object during serialization, and a `ValueInput` that reads from the object during deserialization. Implementing methods typically take in the `ValueOutput` or `ValueInput` as its only parameter, returning nothing. The value I/O expects the backing object to be a dictionary of string keys to object values. Using the provided methods, the value I/O then reads or writes information to the backing object.

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    // Write data to the output
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    // Read data from the input
}

// For some Entity subclass
@Override
protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    // Write data to the output
}

@Override
protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    // Read data from the input
}
```

### Primitives

Value I/O contains methods for reading and writing certain primitives. `ValueOutput` methods are prefixed with `put*`, taking in the key and the primitive value. `ValueInput` methods are named as `get*Or`, taking in the key and a default if none is present.

| Java Type | `ValueOutput` | `ValueInput`                 |
|:---------:|:-------------:|:----------------------------:|
| `boolean` | `putBoolean`  | `getBooleanOr`               |
| `byte`    | `putByte`     | `getByteOr`                  |
| `short`   | `putShort`    | `getShortOr`                 |
| `int`     | `putInt`      | `getInt`\*, `getIntOr`       |
| `long`    | `putLong`     | `getLong`\*, `getLongOr`     |
| `float`   | `putFloat`    | `getFloatOr`                 |
| `double`  | `putDouble`   | `getDoubleOr`                |
| `String`  | `putString`   | `getString`\*, `getStringOr` |
| `int[]`   | `putIntArray` | `getIntArray`\*              |

\* These `ValueInput` methods return an `Optional`-wrapped primitive instead of taking and passing back some fallback.

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    
    // Write data to the output
    output.putBoolean(
        // The string key
        "boolValue",
        // The value associated with this key
        true
    );
    output.putString("stringValue", "Hello world!");
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    // Read data from the input

    // Defaults to false if not present
    boolean boolValue = input.getBooleanOr(
        // The string key to retrieve
        "boolValue",
        // The default value to return if the key is not present
        false
    );

    // Defaults to 'Dummy!' if not present
    String stringValue = input.getStringOr("stringValue", "Dummy!");
    // Returns an optional-wrapped value
    Optional<String> stringValueOpt = input.getString("stringValue");
}
```

### Codecs

[`Codec`s][codec] can also be used to store and read values from the value I/O. In vanilla, all `Codec`s are handled using a `RegistryOps`, allowing the storage of datapack entries. `ValueOutput#store` and `storeNullable` take in the key, the codec to write the object, and the object itself. `storeNullable` will not write anything if the object is `null`. `ValueInput#read` can read the object by taking in the key and the codec, returning an `Optional`-wrapped object.

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    
    // Write data to the output
    output.storeNullable("codecValue", Rarity.CODEC, Rarity.EPIC);
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    // Read data from the input
    Optional<Rarity> codecValue = input.read("codecValue", Rarity.CODEC);
}
```

`ValueOutput` and `ValueInput` also provide a `store` / `read` method for `MapCodec`s. Compared to the `Codec`, the `MapCodec` variant merges the values onto the current root.

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    
    // Write data to the output
    output.store(
        SingleFile.MAP_CODEC,
        new SingleFile(ResourceLocation.fromNamespaceAndPath("examplemod", "example"))
    );
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    // Read data from the input

    // No key is needed as they are stored on the root value access
    Optional<SingleFile> file = input.read(SingleFile.MAP_CODEC);
    // This is present as `SingleFile` writes the `resource` parameter
    String resource = input.getStringOr("resource", "Not present!");
}
```

:::warning
The `MapCodec` will write any keys to the value access, potentially overwriting existing data. Make sure that any keys within the `MapCodec` are distinct from other keys.
:::

### Lists

Lists can be created and read from through one of two methods: child value I/Os or [`Codec`s].

A list is created via `ValueOutput#childrenList`, taking in some key. This returns a `ValueOutput.ValueOutputList`, which acts as a write-only list of value objects. A new value object can be added to the list via `ValueOutputList#addChild`. This returns a `ValueOutput` to write the value object data to. The list can then be read using `ValueInput#childrenList`, or `childrenListOrEmpty` to default to an empty list when not present. These methods return a `ValueInput.ValueInputList`, which acts as a read-only iterable or stream (via `stream`).

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    
    // Write data to the output

    // Create List
    ValueOutput.ValueOutputList listValue = output.childrenList("listValue");
    // Add elements
    ValueOutput childIdx0 = listValue.addChild();
    childIdx0.putBoolean("boolChild", false);
    ValueOutput childIdx1 = listValue.addChild();
    childIdx1.putInt("boolChild", true);
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    // Read data from the input

    // Read values of list
    for (ValueInput childInput : input.childrenListOrEmpty("listValue")) {
        boolean boolChild = childInput.getBooleanOr("boolChild", false);
    }
}
```

`Codec`s provide a list variant for data objects via `ValueOutput#list`. This takes in a key and some `Codec`, returning a `ValueOutput.TypedOutputList`. A `TypedOutputList` is the same as `ValueOutputList`, except it operates on the data object instead of using another value I/O. Elements can be added to the list via `TypedOutputList#add`. Then, similarly, the list can then be read using `ValueInput#list` or `listOrEmpty`, returning a `TypedValueInput`.

:::note
The main difference between a `TypedValueOutput` / `TypedValueInput` and a `Codec#listOf` is how errors are handled. For a `Codec#listOf`, a failed entry will result in the entire object being marked as an error `DataResult`. Meanwhile, a typed value I/O handles the error typically through a `ProblemReporter`. In vanilla, `Codec#listOf` provides more flexibility since `ProblemReporter`s are specified when creating the value I/O. However, custom value I/O usage can implement either depending on the use case.
:::

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    
    // Write data to the output

    // Create List
    ValueOutput.TypedInputList<Rarity> listValue = output.list("listValue", Rarity.CODEC);
    // Add elements
    listValue.add(Rarity.COMMON);
    listValue.add(Rarity.EPIC);
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    // Read data from the input

    // Read values of list
    for (Rarity rarity : input.listOrEmpty("listValue", Rarity.CODEC)) {
        // ...
    }
}
```

:::warning
Lists are still written to the `ValueOutput` even when empty. If you don't want to write the list, then the `TypedOutputList` or `ValueOutputList` should check if it `isEmpty`, then call `discard` with the list key.

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    
    // Write data to the output

    // Create List
    ValueOutput.TypedInputList<Rarity> listValue = output.list("listValue", Rarity.CODEC);
    
    // Check if list is empty
    if (listValue.isEmpty()) {
        // Discard from output
        output.discard("listValue");
    }
}
```
:::

### Objects

Objects can be created and read from via children. `ValueOutput#child` creates a new `ValueObject` given a key. Then, the object can be read using `ValueInput#child`, or `childOrEmpty` if it should default to an `ValueInput` with an empty backing value.

```java
// For some BlockEntity subclass
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    
    // Write data to the output

    // Create object
    ValueOutput objectValue = output.child("objectValue");
    // Add data to object
    objectValue.putBoolean("boolChild", true);
    objectValue.putInt("intChild", 20);
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);

    // Read data from the input

    // Read object
    ValueInput objectValue = input.childOrEmpty("objectValue");
    // Get data from object
    boolean boolChild = objectValue.getBooleanOr("boolChild", false);
    int intChild = objectValue.getIntOr("intChild", 0);
}
```

## ValueIOSerializable

`ValueIOSerializable` is a NeoForge-added interface for objects that can be serialized and deserialized using value I/Os. NeoForge uses this API to handle [data attachments][attachments]. The interface provides two methods: `serialize` to write the object to a `ValueOutput`, and `deserialize` to read the object from a `ValueInput`.

```java
public class ExampleObject implements ValueIOSerializable {
    
    @Override
    public void serialize(ValueOutput output) {
        // Write the object data here
    }

    @Override
    public void deserialize(ValueInput input) {
        // Read the object data here
    }
}
```

## Implementations

### NBT

Value I/O for [NBTs][nbt] is handled via `TagValueOutput` and `TagValueInput`.

A `TagValueOutput` can be created via `createWithContext` or `createWithoutContext`, `createWithContext` means that the output has access to the `HolderLookup.Provider`, which provides the all registries entries (static and datapack), while `createWithoutContext` does not provide any datapack access. Vanilla only uses `createWithContext`. Once the `ValueOutput` has been used, the `CompoundTag` can be retrieved via `TagValueOutput#buildResult`. A `TagValueInput`, on the other hand, can be created via `create`, taking in the `HolderLookup.Provider` and the `CompoundTag` the input is accessing.

Both value I/Os also take in a `ProblemReporter`. The `ProblemReporter` is used to collect all internal errors during the read/write process. Currently, this only tracks `Codec` errors. How the errors are handled is up to the modder. Vanilla implementations throw if the `ProblemReporter` is not empty.

```java
// Assume we have access to a HolderLookup.Provider lookupProvider

TagValueOutput output = TagValueOutput.createWithContext(
    ProblemReporter.DISCARDING, // Choose to discard all errors
    lookupProvider
);

// Write to the output...

CompoundTag tag = output.buildResult();

// Collect the errors
ProblemReporter.Collector reporter = new ProblemReporter.Collector(
    // Optionally takes in the root path element
    // Some objects (e.g., block entities, entities) have a #problemPath() method that can be supplied
    new RootFieldPathElement("example_object")
);

TagValueInput input = TagValueInput.create(
    reporter,
    lookupProvider,
    tag
);

// Read from the input...
```

[attachments]: attachments.md
[codec]: codecs.md
[nbt]: nbt.md
